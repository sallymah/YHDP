package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.controller.base;

import java.sql.SQLException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.svc.yhdp.online.CacheTbSysConfig;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.LMSTxCode;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.svc.yhdp.online.util.tag.AwardCoupon;
import tw.com.hyweb.svc.yhdp.online.util.tag.AwardCouponList;

public class ChipCounterRedeemFiller implements IBizAction
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(ChipCounterRedeemFiller.class);

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        String ecashBonusId = ctx.getCardProduct().getEcashBonusId();
        if (null == ecashBonusId || ecashBonusId.length()<=0 || ecashBonusId.equals(YHDPUtil.ECASH_BOUNS_ID))
        {
            ctx.setRcode(Rcode.POINT_COUPON_UNDEFINE);
            throw new TxException("Ecash Bonud Id Undefined !!!");           
        }

        String[] chipBonusId = ctx.getChipBonusId();
        int idx = ArrayUtils.indexOf(chipBonusId, ecashBonusId);
        if(idx != -1)
        {
            double ecashAmt = ctx.getLmsAmt();
            String sdate = CacheTbSysConfig.getInstance().getValue(CacheTbSysConfig.START_DATE);
            String edate = CacheTbSysConfig.getInstance().getValue(CacheTbSysConfig.END_DATE);
            AwardCouponList chipCounterRedeem = new AwardCouponList();
            AwardCoupon ac = new AwardCoupon();
            ac.setId(ecashBonusId);
            ac.setStartDate(sdate);
            ac.setEndDate(edate);
            ac.setQty(ecashAmt);
            ac.setIsDw(true);
            ac.setIsCash(true);
            ac.setTxCode(LMSTxCode.BONUS_REDEEM);
            chipCounterRedeem.add(ac);
            ctx.setChipCounterRedeem(chipCounterRedeem);
            logger.info("reload amt:."+ecashAmt);
        }
    }

    @Override
    public boolean doActionTest(LMSContext ctx) throws SQLException,
            TxException
    {
        // TODO Auto-generated method stub
        return true;
    }
}
