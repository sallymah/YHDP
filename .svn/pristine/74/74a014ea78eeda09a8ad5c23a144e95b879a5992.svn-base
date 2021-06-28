package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;

public class RefundCardValidator implements IBizAction
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(RefundCardValidator.class);

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        TbCardInfo cardInfo = ctx.getCardInfo();
        //card.status = 9 已停卡,card.LifeCycle = 6 已停卡 不再給退
        if (cardInfo.getStatus().equals(YHDPUtil.CARD_STATUS_STOPPED) && cardInfo.getLifeCycle().equals(YHDPUtil.CARD_LIFECYCLE_STOPPED))
        {
            ctx.setErrDesc("INVALID CARD STATUS (status:"+cardInfo.getStatus()+", lifeCycle:"+ cardInfo.getLifeCycle() + ")");
            ctx.setRcode(Rcode.INVALID_CARD_STATUS);
            logger.warn(ctx.getErrDesc());
            throw new TxException(ctx.getErrDesc());
        }
    }

    @Override
    public boolean doActionTest(LMSContext arg0) throws SQLException, TxException
    {
        // TODO Auto-generated method stub
        return true;
    }
}
