package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.BatchException;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.GenErr2TempDirBean;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.InctlBean;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.MappingLoader;
import tw.com.hyweb.svc.yhdp.online.CacheTbRcode;
import tw.com.hyweb.svc.yhdp.online.CacheTbSysConfig;
import tw.com.hyweb.svc.yhdp.online.ICacheTb;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * impTrans
 * </pre>
 * author:duncan
 */
public class ImpTxnDtl extends AbstractImpFile
{
    private static Logger logger = Logger.getLogger(ImpTxnDtl.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "impfiles" + File.separator + "ImpTxnDtl" + File.separator + "spring.xml";
    
    protected String batchDate = "";
    private Hashtable<String,String> inctlResultMap = new Hashtable<String,String>();
    private JobRunnerFactory jobRunnerFactory = new JobRunnerFactory();
    private int waitThreadSecLimit = 60000;//等待每個tread工作完的的總秒數
    private int waitThreadSleep = 1000;//每一次檢查各tread狀態的秒數
    private IContextListener ctxListener;
    private ArrayList<ICacheTb> cacheTbList = new ArrayList<ICacheTb>();
    
    
    public ImpTxnDtl()
    {
    }


    /**
     * @return cacheTbList
     */
    public ArrayList<ICacheTb> getCacheTbList()
    {
        return cacheTbList;
    }

    /**
     * @param cacheTbList 的設定的 cacheTbList
     */
    public void setCacheTbList(ArrayList<ICacheTb> cacheTbList)
    {
        this.cacheTbList = cacheTbList;
    }
    
    /**
     * @return 傳回 ctxListener。
     */
    public IContextListener getCtxListener()
    {
        return this.ctxListener;
    }

    /**
     * @param lCtxListener 要設定的 ctxListener。
     */
    public void setCtxListener(IContextListener lCtxListener)
    {
        this.ctxListener = lCtxListener;
    }
        
    public int getWaitThreadSleep()
    {
        return waitThreadSleep;
    }

    public void setWaitThreadSleep(int waitThreadSleep)
    {
        this.waitThreadSleep = waitThreadSleep;
    }
    
    public int getWaitThreadSecLimit()
    {
        return waitThreadSecLimit;
    }

    public void setWaitThreadSecLimit(int waitThreadSecLimit)
    {
        this.waitThreadSecLimit = waitThreadSecLimit;
    }
    
    public String getBatchDate()
    {
        return batchDate;
    }

    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

    /**
     * notify observer to reload data
     * @throws SQLException
     */
    public void reload() throws SQLException
    {
        Connection conn=null;
        try
        {
            logger.debug("reload");
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);

            for (int i=0,size=cacheTbList.size();i<size;i++)
            {
                cacheTbList.get(i).reload(conn);
            }
            logger.debug("reload end");
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }       
    }
    
    public void process(String[] args) throws Exception {
        
        //cache table load
        //Layer2Util.loadBatchConfigInfos();
        
        logger.debug("recordsPerCommit:"+recordsPerCommit);
        /* begin threadPool */
        getThreadPoolJobExecutor().startThreadPoolJob();
        
        setLinkControl("I");
        // mappingInfo check
        MappingLoader ml = new MappingLoader();
        ml.setConfigFilename(configFilename);
        ml.setFile(new File(configFilename));
        ml.setEncoding(encoding);
        ml.startLoading();
        mappingInfo = ml.getMappingInfo(fileName);
        if (mappingInfo == null) {
            throw new Exception("mappingInfo(" + fileName + ") is null!");
        }

        // fileInfo check
        fileInfo = ImpFilesUtil.getFileInfoIn(fileName);//TB_IFLE_INFO.LOCAL_PATH
        // no fileInfo
        if (fileInfo == null) {
            throw new Exception("fileInfo(" + fileName + ") is null!");
        }

        // inctlInfos check
        inctlInfos = ImpFilesUtil.getInctlInfosInWorkAndProcessing(fileName, fileDate, seqno, usingLike, getBatchResultInfo());
        if (inctlInfos == null) {
            throw new Exception("inctlInfos(" + fileName + ") is null!");
        }
        else if (inctlInfos.size() == 0) {
            logger.warn("inctlInfos(" + fileName + ") is empty!");
        }
        
        reload();

        // loop each inctlInfo in inctlInfos
        for (int i = 0; i < inctlInfos.size(); i++) {
            try {
                Thread.sleep(500);
                // reset inctlBean
                inctlBean = new InctlBean();
                inctlInfo = (TbInctlInfo) inctlInfos.get(i);
                inctlBean.setRelated(true);
                inctlBean.setBatchResultInfo(getBatchResultInfo());
                // set impFileInfo
                impFileInfo = new ImpFileInfo();
                impFileInfo.setFileInfo(fileInfo);
                impFileInfo.setMappingInfo(mappingInfo);
                impFileInfo.setInctlInfo(inctlInfo);
                impFileInfo.setCheckOKFile(checkOKFile);
                impFileInfo.setCheckEmptyFile(checkEmptyFile);
                // handle impFileInfo
                handleImpFileInfo(inctlBean, impFileInfo, inctlResultMap);
            }
            catch (Exception ignore) {
                thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
                logger.warn("handleImpFileInfo error:" + ignore.getMessage(), ignore);
            }
            finally {
                //impFileInfo.closeFile();
            }
        }

        /*20170413 因遠鑫無對應欄位也暫時相關需求，先行註記不使用
        if (usingErrorHandling) {
            // 加上錯誤要補處理的 code
            GenErr2TempDirBean errBean = null;
            try {
                // 因為 conn 是在開始處理一個檔案的時候才給
                conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
                errBean = new GenErr2TempDirBean();
                String batchDate = DateUtil.getTodayString().substring(0, 8);
                errBean.setBatchDate(batchDate);
                errBean.setConnection(conn);
                errBean.setFileInfo(fileInfo);
                errBean.setMappingInfo(mappingInfo);
                errBean.genErr2TempDir();
            }
            catch (Exception ignore) {
                logger.warn("handle GenErr2TempDirBean.genErr2TempDir error:" + ignore.getMessage(), ignore);
            }
            finally {
                ReleaseResource.releaseDB(conn);
            }
        }*/
        
        checkInctlResult(inctlResultMap);
        
        // set rcode to TB_BATCH_RESULT
        setRcode(thisRcode);
        
    }
    
    /*
     * 檢查各thread是否完成工作的結果
     */
    public void checkInctlResult(Hashtable<String,String> inctlResultMap)
    {
        int okCnt = 0;
        int failCnt = 0;
        int notWorkingCnt = 0;
        int calWaitSec = 0;
        
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e){}
        logger.warn("wait thread job millis second limit:"+waitThreadSecLimit+", sleep millis second:"+waitThreadSleep);
        int fileSize = inctlResultMap.size();
        while(fileSize > 0)
        {
            okCnt = 0;
            failCnt = 0;
            notWorkingCnt = 0;
            Iterator iter = inctlResultMap.entrySet().iterator(); 
            while (iter.hasNext()) 
            { 
                Map.Entry entry = (Map.Entry) iter.next(); 
                String key = (String)entry.getKey(); 
                String val = (String)entry.getValue();
                switch (Integer.valueOf(val))
                {
                    case 1:
                        notWorkingCnt++;
                        break;
                    case 3:
                        okCnt++;
                        break;
                    case 9:
                        failCnt++;
                        break;
                    default:
                        notWorkingCnt++;
                        break;
                }   
            } 
            logger.warn("Total inctl file size:"+fileSize + " fail count:"+failCnt + " ok count:"+okCnt + " not working count:"+notWorkingCnt);
            if((okCnt + failCnt) == fileSize)
            {
                /* 所有檔案已完成 */
                break;
            }
            
            if(getThreadPoolJobExecutor() == null || getThreadPoolJobExecutor().getExecutor() == null)
            {
                /* thread pool is null */
                thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
                logger.warn("work not complete, thread pool is null:");
                break;
            }
            else if(getThreadPoolJobExecutor().getExecutor().isShutdown())
            {
                /* thread pool shdown */
                thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
                logger.warn("work not complete,thread pool shdown");
                break;
            }
            
            try
            {
                Thread.sleep(waitThreadSleep);
            }
            catch (InterruptedException e)
            {
                
            }
            
            if(waitThreadSecLimit > 0)
            {
                calWaitSec = calWaitSec + waitThreadSleep;
                if(waitThreadSecLimit < calWaitSec)
                {
                    break;
                }
            }
        }
        
        /* 失敗或未完成所有筆數 */
        if(failCnt > 0 || notWorkingCnt > 0)
        {
            thisRcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;
        }
    }
    
    public boolean handleImpFileInfo(InctlBean inctlBean, ImpFileInfo impFileInfo, Hashtable<String,String> inctlResultMap) throws Exception
    {
        if(getThreadPoolJobExecutor() != null && getThreadPoolJobExecutor().getExecutor() != null)
        {
            if(inctlBean != null && fileInfo != null && inctlResultMap != null)
            {
                ThreadPoolExecutor threadPoolExecutor = getThreadPoolJobExecutor().getExecutor();
                
                JobRunner jobRunnerExecute = jobRunnerFactory.create(ctxListener, inctlBean, impFileInfo, inctlResultMap, recordsPerCommit);

                /* 初始化檢查thread處理中狀態 */
                inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(), Layer1Constants.WORKFLAG_INWORK);
                
                logger.debug("total size:"+inctlResultMap.size()+" execute FILE_NAME:"+impFileInfo.getInctlInfo().getFullFileName() + ",FILE_DATE:" + impFileInfo.getInctlInfo().getFileDate() + ",SEQNO:"+impFileInfo.getInctlInfo().getSeqno());
                threadPoolExecutor.execute(jobRunnerExecute);
                int qSize = threadPoolExecutor.getQueue().size();
                long cCnt = threadPoolExecutor.getCompletedTaskCount();
                int aCnt = threadPoolExecutor.getActiveCount();
                int cSize = threadPoolExecutor.getCorePoolSize();
                logger.debug("ActiveCount:"+aCnt+" CompletedCount:"+cCnt+" Queue().size():"+qSize+"cSize:"+cSize);
            }
        }
        else
        {
            throw new BatchException("ThreadPoolJobExecutor is null!!");
        }
        return true;
   
    }
    
    @Override
    public ExecuteSqlsInfo beforeHandleDataLine() throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public static ImpTxnDtl getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpTxnDtl instance = (ImpTxnDtl) apContext.getBean("impTxnDtl");
        return instance;
    }

    public static void main(String[] args) {
        ImpTxnDtl impTrans = null;
        try {
            String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            impTrans = getInstance();
            impTrans.setBatchDate(batchDate);
            impTrans.setFileName("IMPTXNDTL"); //設定MappingInfos.xml tag 對應名稱
            impTrans.run(args);
        }
        catch (Exception ignore) {
            logger.warn("ImpTxnDtl run fail:" + ignore.getMessage(), ignore);
            if(impTrans != null && impTrans.getThreadPoolJobExecutor() != null 
                    && impTrans.getThreadPoolJobExecutor().getExecutor() != null
                    && impTrans.getThreadPoolJobExecutor().getExecutor().isShutdown())
            {
                impTrans.getThreadPoolJobExecutor().getExecutor().shutdown();
            }
        }
    }
}
