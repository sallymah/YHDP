package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.validator;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.Rcode;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class LockCardBlacklistValidator implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(LockCardBlacklistValidator.class);
    private static final String SQL_STR = "select status from TB_BLACKLIST_SETTING where CARD_NO = ? and EXPIRY_DATE = ?";
    
    /* (no)n-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doActionTest(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        LMSContext sctx = (LMSContext) ctx;
        TbCardInfo cardInfo = sctx.getCardInfo();
        String pan = cardInfo.getCardNo();
        String expir = cardInfo.getExpiryDate();

        Vector<String> params = new Vector<String>();
        params.add(pan);
        params.add(expir);

        String status = DbUtil.getString(SQL_STR, params, sctx.getConnection());

        /* 卡片落在黑名單檔內 */
        if (StringUtil.isEmpty(status))
        {
            sctx.setErrDesc("not in TB_BLACKLIST_SETTING");
            logger.warn(sctx.getErrDesc());
            sctx.setRcode(Rcode.INVALID_CARD_STATUS);
            throw new TxException(sctx.getErrDesc());
        }
        else if(status.equalsIgnoreCase(YHDPUtil.CARD_STATUS_BLACKLIST))
        {
            sctx.setErrDesc("TB_BLACKLIST_SETTING status is B");
            logger.warn(sctx.getErrDesc());
            sctx.setRcode(Rcode.INVALID_CARD_STATUS);
            throw new TxException(sctx.getErrDesc());
        }
    }
}