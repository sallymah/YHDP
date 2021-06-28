package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;


import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.util.BatchDbUtil;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IFiller;


/**
 * AcquirerFiller
 */
public class AcquirerFiller implements IFiller
{
    private static final Logger logger = Logger.getLogger(AcquirerFiller.class);
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.loyalty.controller.IAcquirerFiller#fillAcquirer(tw.com.hyweb.core.cp.online.loyalty.LMSContext)
     */
    public void doFill(LMSContext ctx) throws SQLException, TxException
    {
        
        String termId = ctx.getLmsTerminalId();
        String merchId = ctx.getLmsMerchantId();
        if (termId!=null)
        {
            logger.debug("doFill");
            if(null == ctx.getAcquireInfo())
            {
                TbMemberInfo acquireInfo = BatchDbUtil.getAcquireInfo(ctx.getHostDate(),termId,merchId,ctx.getConnection());
                ctx.setAcquireInfo(acquireInfo);
            }
        }
    }

}
