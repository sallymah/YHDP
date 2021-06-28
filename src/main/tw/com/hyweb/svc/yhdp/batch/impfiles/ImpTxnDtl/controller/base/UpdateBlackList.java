package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardUptInfo;
import tw.com.hyweb.service.db.mgr.TbCardUptMgr;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.Constant;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.ArraysUtil;
import tw.com.hyweb.util.DbUtil;

public class UpdateBlackList implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(UpdateBlackList.class);
    private int[] acceptLmsProcCode;
    
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
        if (null != acceptLmsProcCode)
        {
            String lmsPcode = ctx.getLmsPcode();
            boolean ret = false;
            int iLmsPcode = Integer.parseInt(lmsPcode);
            ret = ArraysUtil.contains(acceptLmsProcCode, iLmsPcode);
            if(ret)
            {
                updateCardStatus(ctx);
                updateBlackStatus(ctx);
            }
        }
    }
    
    public void updateBlackStatus(LMSContext ctx) throws SQLException, TxException
    {
        String hostDate = ctx.getHostDate();
        String hostTime = ctx.getHostTime();
        TbCardInfo cardInfo = ctx.getCardInfo();
        String cardNo = cardInfo.getCardNo();
        String expiryDate = cardInfo.getExpiryDate();
        
        StringBuffer sqlStr = new StringBuffer();
        sqlStr.append("UPDATE TB_BLACKLIST_SETTING SET ");
        sqlStr.append("STATUS = ?,");
        sqlStr.append("BLOCK_DATE = ? ,BLOCK_TIME = ? ");
        sqlStr.append(" WHERE CARD_NO = ? AND EXPIRY_DATE = ?");

        Vector<String> params = new Vector<String>();
        params.add(YHDPUtil.CARD_STATUS_BLACKLIST);//黑名單
        params.add(hostDate);
        params.add(hostTime);
        params.add(cardNo);
        params.add(expiryDate);

        DbUtil.sqlAction(sqlStr.toString(), params, ctx.getConnection());
    }
    
    public void updateCardStatus(LMSContext ctx) throws SQLException, TxException
    {
        
        String hostDate = ctx.getHostDate();
        String hostTime = ctx.getHostTime();
        TbCardInfo cardInfo = ctx.getCardInfo();
        String cardNo = cardInfo.getCardNo();
        String expiryDate = cardInfo.getExpiryDate();

        StringBuffer sqlStr = new StringBuffer();
        sqlStr.append("UPDATE TB_CARD SET ");
        sqlStr.append("PREVIOUS_STATUS = ?, STATUS = ?, UPDATE_TYPE =?,STATUS_UPDATE_DATE=?,");
        sqlStr.append("UPT_USERID = 'ONLINE' ,");
        sqlStr.append("UPT_DATE = ? ,UPT_TIME = ?, ");
        sqlStr.append("APRV_USERID = 'ONLINE' ,");
        sqlStr.append("APRV_DATE = ? ,APRV_TIME = ?, INACTIVE_DATE =? ");
        sqlStr.append(" WHERE CARD_NO = ? AND EXPIRY_DATE = ?");

        Vector<String> params = new Vector<String>();
        params.add(cardInfo.getStatus());//黑名單
        params.add(YHDPUtil.CARD_STATUS_BLACKLIST);//黑名單
        params.add(YHDPUtil.UPDATE_TYPE_CARD_STATUS);
        params.add(hostDate);
        params.add(hostDate);
        params.add(hostTime);
        params.add(hostDate);
        params.add(hostTime);
        params.add(hostDate);
        params.add(cardNo);
        params.add(expiryDate);

        DbUtil.sqlAction(sqlStr.toString(), params, ctx.getConnection());
        
        TbCardUptInfo cardUptInfo = new TbCardUptInfo();
        cardUptInfo = YHDPUtil.copyCardData(cardInfo);
        if(null != cardUptInfo)
        {
            cardUptInfo.setUptUserid("BATCH");
            cardUptInfo.setUptDate(ctx.getHostDate());
            cardUptInfo.setUptTime(ctx.getHostTime());
            cardUptInfo.setAprvUserid("BATCH");
            cardUptInfo.setAprvDate(ctx.getHostDate());
            cardUptInfo.setAprvTime(ctx.getHostTime());
            cardUptInfo.setInactiveDate(ctx.getHostDate());
            cardUptInfo.setUptStatus(Constant.UPT_STATUS_MODIFY);
            cardUptInfo.setAprvStatus(Constant.APRV_STATUS_APPROVED);
            cardUptInfo.setUpdateType(YHDPUtil.UPDATE_TYPE_CARD_STATUS);
            cardUptInfo.setStatus(YHDPUtil.CARD_STATUS_BLACKLIST);
            cardUptInfo.setCoBrandEntId("00");
            TbCardUptMgr cardUptMgr = new TbCardUptMgr(ctx.getConnection());
            cardUptMgr.insert(cardUptInfo);
        }
    }
    
    public void setAcceptLmsProcCode(String acceptLmsProcCodeL)
    {
        this.acceptLmsProcCode = ArraysUtil.toIntArray(acceptLmsProcCodeL);
    }

    /**
     * @return acceptLmsProcCode
     */
    public int[] getAcceptLmsProcCodeArray()
    {
        return acceptLmsProcCode;
    }
}