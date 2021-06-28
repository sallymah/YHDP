package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.redeem;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.service.db.info.TbCardBalInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.mgr.TbCardBalMgr;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.BalanceDbUtil;
import tw.com.hyweb.svc.yhdp.online.util.Constant;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.tag.AwardCoupon;
import tw.com.hyweb.svc.yhdp.online.util.tag.AwardCouponList;
import tw.com.hyweb.util.ArraysUtil;

public class UpdateChipBalance implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(UpdateChipBalance.class);
    
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
        updateCouponRedeem(ctx);
        updateCounterRedeem(ctx);
        updateCouponAward(ctx);
        updateCounterAward(ctx);
        updateCounterReload(ctx);
    }
    
    public void updateCouponRedeem(LMSContext ctx) throws SQLException, TxException
    {
        try{
            updateChipBonusBalanceC(ctx,ctx.getChipCouponRedeem(), Constant.TYPE_NOT_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
        }
        catch(SQLException sqle)
        {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
            {
                //判斷SQLCODE 重複才不處理
                logger.debug("don't care duplicate");
                ctx.setRcode(Rcode.OK);
            }
            else
            {
                throw sqle;
            }
        }
    }
    
    public void updateCounterRedeem(LMSContext ctx) throws SQLException, TxException
    {
        try{
            updateChipBonusBalanceC(ctx,ctx.getChipCounterRedeem(), Constant.TYPE_NOT_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
        }
        catch(SQLException sqle)
        {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
            {
                //判斷SQLCODE 重複才不處理
                logger.debug("don't care duplicate");
                ctx.setRcode(Rcode.OK);
            }
            else
            {
                throw sqle;
            }
        }
    }
    
    public void updateCounterReload(LMSContext ctx) throws SQLException, TxException
    {
        try{
            updateChipBonusBalanceC(ctx,ctx.getChipCounterReload(), Constant.TYPE_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
        }
        catch(SQLException sqle)
        {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
            {
                //判斷SQLCODE 重複才不處理
                logger.debug("don't care duplicate");
                ctx.setRcode(Rcode.OK);
            }
            else
            {
                throw sqle;
            }
        }
    }
    
    public void updateCounterAward(LMSContext ctx) throws SQLException, TxException
    {
        try{
            updateChipBonusBalanceC(ctx,ctx.getChipCounterAward(), Constant.TYPE_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
        }
        catch(SQLException sqle)
        {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
            {
                //判斷SQLCODE 重複才不處理
                logger.debug("don't care duplicate");
                ctx.setRcode(Rcode.OK);
            }
            else
            {
                throw sqle;
            }
        }
    }
    
    public void updateCouponAward(LMSContext ctx) throws SQLException, TxException
    {
        try{
            updateChipBonusBalanceC(ctx,ctx.getChipCouponAward(), Constant.TYPE_CR, Constant.TYPE_ADD, Constant.TYPE_NOT_CHK_DOWNLOAD);
        }
        catch(SQLException sqle)
        {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
            {
                //判斷SQLCODE 重複才不處理
                logger.debug("don't care duplicate");
                ctx.setRcode(Rcode.OK);
            }
            else
            {
                throw sqle;
            }
        }
    }  
    
    /**
     * 更新晶片點數(卷)餘額與新增
     *
     * @param ctx
     * @param cardNo
     * @param expiryDate
     * @param chipBonusId
     * @throws SQLException
     * @throws TxException
     */
    public static void updateChipBonusBalanceC(LMSContext ctx, AwardCouponList chipCouponList, boolean isCr, boolean isAdd, boolean isCheckDw) throws SQLException,
                    TxException
    {
        if (chipCouponList != null && chipCouponList.size() >0)
        {           
            TbCardInfo cardInfo = ctx.getCardInfo();
            String cardNo = cardInfo.getCardNo();
            String expiryDate = cardInfo.getExpiryDate();
            String sql = BalanceDbUtil.genUpdateCardBalSql(isCr, isAdd);
            Vector<String> params = new Vector<String>();
            SqlResult sr = null;
            DBService dbService = DBService.getDBService();
            TbCardBalInfo cardBal = null;
            double point = 0;
            AwardCoupon ac = null;
            TbCardBalMgr cardBalMgr = new TbCardBalMgr(ctx.getConnection());
            for (int i = 0; i < chipCouponList.size(); i++)
            {
                ac = chipCouponList.get(i);
                if(ac != null && ac.getId() != null && ac.getId().length() > 0 && ac.getQty() != 0)
                {    
                    if((isCheckDw && ac.getIsDw()) || isCheckDw == false)
                    {
                        logger.debug(ac.toString());
                        point = ac.getQty();
                        params.clear();
                        params.add((new BigDecimal(point)).toString());
                        params.add(cardNo);
                        params.add(expiryDate);
                        params.add(ac.getId());
                        params.add(ac.getStartDate());
                        params.add(ac.getEndDate());
                        sr = dbService.sqlAction(sql, params, ctx.getConnection());
                        if (null==sr || 0 == sr.getRecordCount())
                        {
                            cardBal = new TbCardBalInfo();
                            cardBal.setCardNo(cardNo);
                            cardBal.setExpiryDate(expiryDate);
                            cardBal.setBonusId(ac.getId());
                            cardBal.setBonusSdate(ac.getStartDate());
                            cardBal.setBonusEdate(ac.getEndDate());
                            if(isCr)
                            {
                                cardBal.setCrBonusQty(ac.getQty());
                            }
                            else
                            {
                                cardBal.setDbBonusQty(ac.getQty());
                            }
                            cardBalMgr.insert(cardBal);
                        }
                        else
                        {
                            logger.debug("update tb_card_bal count:"+sr.getRecordCount());
                        }
                    }
                }
            }
        }
    } 
}