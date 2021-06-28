package tw.com.hyweb.svc.yhdp.batch.mail;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;
import tw.com.hyweb.service.db.mgr.TbMailSettingMgr;

public class MailSettingFiller implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(MailSettingFiller.class);
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        MailContext mctx = (MailContext)ctx;
        Vector<TbMailSettingInfo> mailSetInfo = new Vector<TbMailSettingInfo>();
        TbMailSettingMgr mailSetMrg = new TbMailSettingMgr(mctx.getConnection());
        mailSetMrg.queryAll(mailSetInfo);
        if(null != mailSetInfo.get(0))
        {
            mctx.setMailSetInfo(mailSetInfo.get(0));
        }
    }
}
