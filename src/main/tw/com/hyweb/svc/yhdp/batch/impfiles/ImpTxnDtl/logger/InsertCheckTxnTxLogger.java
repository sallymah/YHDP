package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.logger;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.logger.TxLoggerOfflineAdvice;
import tw.com.hyweb.svc.yhdp.online.util.tag.OfflineUploadTxnInfo;

public class InsertCheckTxnTxLogger implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(InsertCheckTxnTxLogger.class);
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        OfflineUploadTxnInfo uploadTxnInfo = ctx.getOfflineUploadTxnInfo();
        
        if(uploadTxnInfo.getRcode().equals("0000"))
        {
            if(ctrl != null && null != ctx.getOfflineUploadTxnInfo())
            {
                TxLoggerOfflineAdvice txLogger = (TxLoggerOfflineAdvice) ctrl.getTxLogger();
                //txLogger.fillTxnDb(ctx);
                //txLogger.fillTxnDtlDb(ctx);
                //txLogger.fillTxnTrace(ctx);
                txLogger.insert(ctx);
            }
        }
    }
}
