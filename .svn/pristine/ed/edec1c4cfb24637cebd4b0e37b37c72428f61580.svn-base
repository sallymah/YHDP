package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBalanceUpdate;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.BalanceDbUtil;
import tw.com.hyweb.svc.yhdp.online.util.Constant;

public class UpdateVoidChipBalance implements IBalanceUpdate, IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(UpdateVoidChipBalance.class);

    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }

    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        doBiz(ctx);
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBalanceUpdate#doBiz(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doBiz(LMSContext ctx) throws SQLException, TxException
    {
        /* 晶片紅利餘額更新 */
        String chipMode = ctx.getChipMode();
        if (chipMode != null && chipMode.equals("C"))
        {           
            /* IsCr,IsAdd */
            BalanceDbUtil.updateChipBonusBalanceDirectC(ctx,ctx.getChipCouponAward(), Constant.TYPE_NOT_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
            BalanceDbUtil.updateChipBonusBalanceDirectC(ctx,ctx.getChipPaperCouponAward(), Constant.TYPE_NOT_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
            BalanceDbUtil.updateChipBonusBalanceDirectC(ctx,ctx.getChipCounterAward(), Constant.TYPE_NOT_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
            BalanceDbUtil.updateChipBonusBalanceDirectC(ctx,ctx.getChipCounterReload(), Constant.TYPE_NOT_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
            BalanceDbUtil.updateChipBonusBalanceDirectC(ctx,ctx.getChipCouponRedeem(), Constant.TYPE_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
            BalanceDbUtil.updateChipBonusBalanceDirectC(ctx,ctx.getChipCounterRedeem(), Constant.TYPE_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
        }
    }
}