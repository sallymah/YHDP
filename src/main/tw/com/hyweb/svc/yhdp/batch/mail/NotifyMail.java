package tw.com.hyweb.svc.yhdp.batch.mail;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;
import tw.com.hyweb.service.db.mgr.TbMailSettingMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class NotifyMail extends AbstractBatchBasic
{
    private static final Logger log = Logger.getLogger(NotifyMail.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
             "Mail" + File.separator + "spring.xml";
    private Context ctx;
    private List<IBizAction> bizAction;
    protected String batchDate = "";
    protected String recoverLevel = "";
    private String sysName = "";

    // millisecond
    protected int sleepPerInfo = 500;
    protected Connection conn = null;
        
    @Override
    public void process(String[] arg0) throws Exception
    {
        // impfiles -> linkControl = "O"
        setLinkControl("O");
        // 若 makeExpFileSetting 有提供, 用這個, 不然就用原來的, for spring setting
        if (StringUtil.isEmpty(batchDate)) {
            String tmpDate = System.getProperty("date", "");
            if (StringUtil.isEmpty(tmpDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
                 
            }
            else {
                batchDate = tmpDate;
                if (!DateUtil.isValidDate(batchDate)) {
                    throw new IllegalArgumentException("invalid batchDate(" + batchDate + ")!");
                }
            }
        }
        try {
            String batchTime = DateUtil.getTodayString().substring(8, 14);
            log.debug("batchDate:"+batchDate);
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            MailContext mctx = (MailContext)ctx;
            
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.PATTERN_yyyyMMddHHmmss);
            Date date = sdf.parse(batchDate + batchTime);
            
            mctx.setTimeTxInit(date);
            mctx.setTimeTxExec(date);
            mctx.setSysName(sysName);
            mctx.setConnection(conn);
            log.debug("batchDate:"+mctx.getHostDate());
            Vector<TbMailSettingInfo> result = new Vector<TbMailSettingInfo>();
            TbMailSettingMgr mailSetMrg = new TbMailSettingMgr(mctx.getConnection());
            String where = "STATUS='1'";
            mailSetMrg.queryMultiple(where, result);
            
            for(int i = 0; i < result.size(); i++)
            {
                TbMailSettingInfo mailInfo = result.get(i);
                if(null != mailInfo)
                {
                    if(!StringUtil.isEmpty(mailInfo.getDataFlow()))
                    {
                        mctx.setMailSetInfo(mailInfo);
                        log.debug(mailInfo);
                        processBiz(null , (LMSContext)mctx);
                    }
                }
            }
        }
        catch (Exception ignore) {
            log.warn("do recoverData error:" + ignore.getMessage(), ignore);
            if (conn != null) {
                try {
                    conn.rollback();
                }
                catch (Exception ignore2) {
                    ;
                }
            }
            throw ignore;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        return;     
    }
    
    public static NotifyMail getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        
        NotifyMail instance = (NotifyMail) apContext.getBean("processor");
        return instance;
    }
           
    /**
     * @param ctx
     */
    public LMSContext processBiz(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        int bizActionNum = (bizAction != null)?bizAction.size():0;
        for(int i = 0; i < bizActionNum; i++)
        {
            if(bizAction.get(i).doActionTest(ctx))
            {
                bizAction.get(i).doAction(ctrl, ctx);
            }
        }
        return ctx;
    }
    
    /**
     * @return bizAction
     */
    public List<IBizAction> getBizAction()
    {
        return bizAction;
    }

    /**
     * @param bizAction 的設定的 bizAction
     */
    public void setBizAction(List<IBizAction> bizAction)
    {
        this.bizAction = bizAction;
    }
    
    /**
     * @return bizAction
     */
    public Context getContext()
    {
        return ctx;
    }

    /**
     * @param bizAction 的設定的 bizAction
     */
    public void setContext(Context ctx)
    {
        this.ctx = ctx;
    }
    
    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }
    
    
    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }
    
    /**
     * @param args
     * @throws SQLException 
     */
    public static void main(String[] args) throws SQLException
    {
        NotifyMail notifyMail = null;
        try {
            String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            else if (!DateUtil.isValidDate(batchDate)) {
                log.info("invalid batchDate('" + batchDate + "') using system date!");
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
                notifyMail = getInstance();
            }
            else {
                notifyMail = new NotifyMail();
            }
            notifyMail.setBatchDate(batchDate);
            notifyMail.run(args);
        }
        catch (Exception ignore) {
            log.warn("notifyMail run fail:" + ignore.getMessage(), ignore);
        }
    }
}
