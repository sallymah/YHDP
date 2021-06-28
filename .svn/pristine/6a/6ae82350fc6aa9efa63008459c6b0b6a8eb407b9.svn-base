package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.validator;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.Rcode;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.DbUtil;

public class BlackListValidator  implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(BlackListValidator.class);
    private static final String SQL_STR = "select status from TB_BLACKLIST_SETTING where CARD_NO = ? and EXPIRY_DATE = ?";
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        LMSContext sctx = (LMSContext) ctx;

        String pan = sctx.getLMSCardNbr();
        String expir = sctx.getLMSCardExpirationDate();

        Vector<String> params = new Vector<String>();
        params.add(pan);
        params.add(expir);

        try
        {
            String status = DbUtil.getString(SQL_STR, params, sctx.getConnection());

            /* 卡片落在黑名單檔內 */
            if (null == status)
            {
                sctx.setErrDesc("not in TB_BLACKLIST_SETTING");
                sctx.setRcode(Rcode.INVALID_CARD_STATUS);
                logger.warn(sctx.getErrDesc());
            }
            else if(status.equalsIgnoreCase(YHDPUtil.CARD_STATUS_BLACKLIST))
            {
                sctx.setErrDesc("TB_BLACKLIST_SETTING status is B");
                sctx.setRcode(Rcode.INVALID_CARD_STATUS);
                logger.warn(sctx.getErrDesc());
            }
        }
        catch (SQLException e)
        {
            ctx.setRcode(Rcode.SQL_FAIL);
        }

        return ctx;
    }
}