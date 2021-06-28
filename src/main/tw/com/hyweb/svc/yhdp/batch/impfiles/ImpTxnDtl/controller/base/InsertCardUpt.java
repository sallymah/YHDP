package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardUptInfo;
import tw.com.hyweb.service.db.mgr.TbCardUptMgr;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.Constant;
import tw.com.hyweb.svc.yhdp.online.util.LmsDbUtil;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;

public class InsertCardUpt implements IBizAction
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(InsertCardUpt.class);
    private String updateType = YHDPUtil.UPDATE_TYPE_BOTH;
    
    public InsertCardUpt()
    {
    }
    
    public InsertCardUpt(String updateType)
    {
        this.updateType = updateType;
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        String pan = ctx.getLMSCardNbr();
        String expir = ctx.getLMSCardExpirationDate();
        TbCardInfo cardInfo = LmsDbUtil.getCardInfo(pan, expir, ctx.getConnection());
        TbCardUptInfo cardUptInfo = YHDPUtil.copyCardData(cardInfo);
        if(null != cardUptInfo)
        {
            cardUptInfo.setUptUserid(Constant.UPT_ONLINE_USER_ID);
            cardUptInfo.setUptDate(ctx.getHostDate());
            cardUptInfo.setUptTime(ctx.getHostTime());
            cardUptInfo.setAprvUserid(Constant.UPT_ONLINE_USER_ID);
            cardUptInfo.setAprvDate(ctx.getHostDate());
            cardUptInfo.setAprvTime(ctx.getHostTime());
            cardUptInfo.setUptStatus(Constant.UPT_STATUS_MODIFY);
            cardUptInfo.setAprvStatus(Constant.APRV_STATUS_APPROVED);
            //cardUptInfo.setUpdateType(updateType);
            TbCardUptMgr cardUptMgr = new TbCardUptMgr(ctx.getConnection());
            cardUptMgr.insert(cardUptInfo);
        }
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doActionTest(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        if(ctx.getOrigTxnInfo() != null && !ctx.getOrigTxnInfo().getStatus().equals("9") && null != ctx.getCardInfo())
        {
            return true;
        }
        return false;
    }
}
