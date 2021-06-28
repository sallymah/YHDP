package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;

public class CheckIsErrData implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(CheckIsErrData.class);
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        if(ctrl != null)
        {
            BATCHContext bctx = (BATCHContext)ctx;
            String respCode = bctx.getDongleRespCode();
            if(!respCode.equals("0000") && !respCode.equals("00"))
            {
                bctx.setRcode(respCode);
                logger.debug("Record is error data : "+respCode);
                throw new TxException("Record is error data : "+respCode);
            }
        }
    }
}
