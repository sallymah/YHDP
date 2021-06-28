package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.BatchBaseController;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;

public class BatchDispatcher extends BatchBaseController
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(BatchDispatcher.class);
    private List ctrlerList;

    /**
     *
     */
    public BatchDispatcher()
    {

    }
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.BaseController#processBiz(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    @Override
    public LMSContext processBiz(LMSContext ctx) throws SQLException, TxException
    {
        LMSContext respCtx = null;
        if ((this.ctrlerList == null) || (this.ctrlerList.size() == 0))
        {
            logger.error("tx controller not setting.");
            return null;
        }
        int ctrlSize = this.ctrlerList.size();
        BatchBaseController ctrler=null;
        try {
        for (int i = 0; i < ctrlSize; i++)
        {
            ctrler = (BatchBaseController) ctrlerList.get(i);
            if (ctrler.isProcess(ctx))
            {
                respCtx = ctrler.processBiz(ctx);
                break;
            }

            if (i == (ctrlSize - 1))
            {
                respCtx = (LMSContext) ctx.clone();
                respCtx.setRcode(Rcode.INVALID_TXN);
                logger.warn("No txn controller process the message :\n " + ctx);
            }
        }
        }catch(Exception e)
        {
            //logger.warn(" ",e);
            throw e;
        }
        return respCtx;
    }

    /**
     * @param ctrlerList 要設定的 ctrlerList。
     */
    public void setCtrlerList(List ctrlerList)
    {
        this.ctrlerList = ctrlerList;
    }

    /**
     * @return 傳回 ctrlerList。
     */
    public List getCtrlerList()
    {
        return this.ctrlerList;
    }
}