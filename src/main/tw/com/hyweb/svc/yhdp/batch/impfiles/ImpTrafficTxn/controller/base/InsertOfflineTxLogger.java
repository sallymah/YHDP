package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.logger.TxLoggerOfflineBase;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;

public class InsertOfflineTxLogger implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(InsertOfflineTxLogger.class);
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");

        if(ctrl != null)
        {
            TxLoggerOfflineBase txLogger = (TxLoggerOfflineBase) ctrl.getTxLogger();
            txLogger.fillTxnDb(ctx);
            txLogger.fillTxnDtlDb(ctx);
        }
    }
}