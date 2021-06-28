package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.util.BatchDbUtil;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.util.DbUtil;

public class AcquireValidator implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(AcquireValidator.class);
    
    /*
     * (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        logger.debug("validate AcquireValidator");
        LMSContext lctx = (LMSContext)ctx;
        String merchId = lctx.getLmsMerchantId();
        String terminalId = lctx.getLmsTerminalId();
        
        if (merchId!=null && merchId.length()>0 && terminalId!=null && terminalId.length()>0)
        { 
            try
            {                
                TbMemberInfo acquireInfo = BatchDbUtil.getAcquireInfo(ctx.getHostDate(),terminalId,merchId,((LMSContext) ctx).getConnection());
                if(null == acquireInfo)
                {
                    ctx.setRcode(Rcode.INVALID_ACQUIRE);
                    logger.warn("INVALID ACQUIRE. TB_MEMBER,TB_TERM record !=1");
                    return ctx;
                }
                lctx.setAcquireInfo(acquireInfo);
            }
            catch (SQLException e)
            {
                logger.error(" ", e);
                ctx.setRcode(Rcode.SQL_FAIL);
                return ctx;
            }
        }

        return ctx;
    }
}