package tw.com.hyweb.svc.yhdp.batch.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.campaign.SdateEdateInfo;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.common.campaignadapter.CampaignAdapterImpl;
import tw.com.hyweb.core.cp.common.campaignadapter.CampaignFillerImpl;
import tw.com.hyweb.core.cp.common.campaignadapter.CampaignInfo;
import tw.com.hyweb.core.cp.common.campaignadapter.ICampaignAdapter;
import tw.com.hyweb.core.cp.common.campaignadapter.ICampaignFiller;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.DateRange;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class BatchDbUtil
{
    private static final Logger logger = Logger.getLogger(BatchDbUtil.class);
        
//    public static void getBonusSdateEdate(RegularAwardContext ctx, TbCardInfo cardInfo) throws BatchJobException, SQLException
//    {
//            RegularInfo regularInfo = ctx.getRegularInfo();
//            DateRange dateRange;
//
//            dateRange = getCampaignBonusDateRange(ctx.getConnection(), cardInfo, ctx.getBatchDate(), regularInfo.getBonusId(), "C", regularInfo.getBonusSdate(), regularInfo.getBonusEdate());
//            regularInfo.setBonusEdate(dateRange.getEndDate());
//            regularInfo.setBonusSdate(dateRange.getStartDate());
//            logger.debug("cal BonusSdate:"+dateRange.getStartDate() +" BonusEdate:"+dateRange.getEndDate());
//          
//    }
    
    public static DateRange getCampaignBonusDateRange(Connection connection, TbCardInfo card, String txnDate, String bonusId, String balanceType, String bonusSdate, String bonusEdate) throws BatchJobException, SQLException
    {
        CampaignInfo campaignInfo = new CampaignInfo();
        campaignInfo.setCardNo(card.getCardNo());
        campaignInfo.setExpiryDate(card.getExpiryDate());
        campaignInfo.setTransDate(txnDate);
        campaignInfo.setRegionId(card.getRegionId());
        campaignInfo.setCustId(card.getCustId());
        campaignInfo.setAcctId(card.getAcctId());

        ICampaignFiller filler = new CampaignFillerImpl(connection, campaignInfo);
        logger.debug("point 1");
        ICampaignAdapter adapter = new CampaignAdapterImpl(connection, filler);
        logger.debug("point 2");

        SdateEdateInfo sdateEdateInfo = adapter.getSdateEdateInfo(bonusId, balanceType, bonusSdate, bonusEdate);
        
        if (!StringUtil.isEmpty(sdateEdateInfo.getRcode()) && !Constants.RCODE_0000_OK.equals(sdateEdateInfo.getRcode())) { 
            throw new BatchJobException("getSdateEdateInfo fail : " + sdateEdateInfo.getErrMsg(), sdateEdateInfo.getRcode());
        }

        DateRange dateRange = new DateRange();
        dateRange.setStartDate(sdateEdateInfo.getSDate());
        dateRange.setEndDate(sdateEdateInfo.getEDate());

        return dateRange;
    }
    
    
    
//    public static void insertCardBal(RegularAwardContext ctx, TbCardInfo cardInfo) throws SQLException
//    {
//        logger.debug("do insertCardBal");
//        String sql = "update TB_CARD_BAL set CR_BONUS_QTY = CR_BONUS_QTY+? where CARD_NO=? and EXPIRY_DATE=? and BONUS_ID=? and BONUS_SDATE=? and BONUS_EDATE=?";
//        String cardNo = cardInfo.getCardNo();
//        String expiryDate = cardInfo.getExpiryDate();
//        TbCardBalInfo bal = new TbCardBalInfo();
//        
//        String id = ctx.getRegularInfo().getBonusId();
//        String sDate = ctx.getRegularInfo().getBonusSdate();
//        String eDate = ctx.getRegularInfo().getBonusEdate();
//        Number qty = ctx.getRegularInfo().getBonusQty();
//        logger.debug(String.format("id=%s,sdate=%s,edate=%s,qty=%s",id,sDate,eDate,qty));
//        Vector<String> params = new Vector<String>();
//        params.add(Double.toString(qty.doubleValue()));
//        params.add(cardNo);
//        params.add(expiryDate);
//        params.add(id);
//        params.add(sDate);//BOCCC 的作法應該是00000000
//        //params.add(pointCoupon[j].getStartDate());
//        params.add(eDate);
//        SqlResult sr = DbUtil.sqlAction(sql, params, ctx.getConnection());
//        if (null==sr || 0 == sr.getRecordCount())
//        {
//            TbCardBalMgr cardBalMgr = new TbCardBalMgr(ctx.getConnection());
//            logger.debug("update count=0 go insert");
//            bal = new TbCardBalInfo();
//            bal.setCardNo(cardNo);
//            bal.setExpiryDate(expiryDate);
//            bal.setBonusId(id);
//            bal.setBonusSdate(sDate);
//            //bal.setBonusSdate(pointCoupon[j].getStartDate());//BOCCC 的作法應該是00000000
//            bal.setBonusEdate(eDate);
//            bal.setCrBonusQty(qty);
//            cardBalMgr.insert(bal);
//        }
//        else
//        {
//            logger.debug("update count:"+sr.getRecordCount());
//        }
//    }
        
    public static ArrayList<TbCardInfo> getCardInfoList(ArrayList<TbCardInfo> list, String sqlCmd,  Vector<String> params,Connection conn) throws SQLException
    {
        Vector result = DbUtil.select(sqlCmd, params, conn);
        
        if(null != result && result.size() > 0)
        {
            for(int i=0; i < result.size() ;i++)
            {
                Vector record = (Vector)result.get(i);
                
                if(null != record)
                {
                    TbCardInfo cardInfo = new TbCardInfo();
                    cardInfo.setCardNo((String)record.get(0));
                    cardInfo.setExpiryDate((String)record.get(1));
                    logger.debug(cardInfo.toString());
                    list.add(cardInfo);               
                }
            }
        }
        
        logger.debug("cardInfoList Size:"+list.size());
        return list;
    }
    
//    public static boolean checkTbTransDuplicate(RegularAwardContext ctx, TbCardInfo cardInfo) throws SQLException
//    {
//        logger.debug("do action");
//        StringBuffer sqlCmd = new  StringBuffer();
//        sqlCmd.append("select * from tb_trans where card_no=? and expiry_date=? and p_code =? and txn_src =? and txn_date= ? and merch_id =?");
//        sqlCmd.append(" and regular_Id =?");
//        logger.debug(sqlCmd.toString());
//        Vector<String> params=  new Vector<String>();
//        params.add(cardInfo.getCardNo());
//        params.add(cardInfo.getExpiryDate());
//        params.add(ctx.getPCode());
//        params.add(ctx.getTxnSrc());
//        params.add(ctx.getBatchDate());//txn_date
//        params.add(ctx.getMerchId());//merch_id
//        params.add(ctx.getRegularInfo().getRegularId());
//        
//        Vector result = DbUtil.select(sqlCmd.toString(), params, ctx.getConnection());
//        
//        if(null != result && result.size() > 0)
//        {
//            logger.debug("duplicate");
//            return true;
//        }
//        return false;
//    }
    
//    public static TbRegularDtlInfo[] getRegular(String regularId, Connection conn) throws SQLException
//    {
//        String sqlCmd = "select * from tb_regular_dtl where regular_id = ?";
//        Vector<String> params=  new Vector<String>();
//        params.add(regularId);
//        
//        Vector result = DbUtil.select(sqlCmd, params, conn);
//        TbRegularDtlInfo[] regularDtlList = null;
//        if(null != result && result.size() > 0)
//        {
//            regularDtlList = new TbRegularDtlInfo[result.size()];
//            for(int i=0; i < result.size() ;i++)
//            {
//                Vector record = (Vector)result.get(i);
//                
//                if(null != record)
//                {
//                    TbRegularDtlInfo regularDtlInfo = new TbRegularDtlInfo();
//                    regularDtlInfo.setRegularId((String)record.get(0));
//                    regularDtlInfo.setRegularDate((String)record.get(1));
//                    regularDtlList[i] = regularDtlInfo;
//                }
//            }
//        }
//        
//        return regularDtlList;
//    }
    
    public static boolean isEmpty(String data)
    {
        if(null != data && data.length() > 0)
        {
            return false;
        }
        return true;
    }
    
//    public static TbTermBatchInfo insertTermBatch(Connection connection, RegularAwardContext ctx) throws SQLException
//    {
//        String merchId = ctx.getMerchId();
//        String termId = ctx.getTermId();
//        TbTermBatchInfo tbTermBatch = new TbTermBatchInfo();
//        tbTermBatch.setTxnSrc(ctx.getTxnSrc());
//        tbTermBatch.setMerchId(merchId);
//        tbTermBatch.setTermId(termId);
//        tbTermBatch.setTermSettleDate(ctx.getHostDate());
//        tbTermBatch.setTermSettleTime(ctx.getHostTime());
//        tbTermBatch.setTermSettleFlag("1");
//        tbTermBatch.setStatus("1");
//        tbTermBatch.setInfile(ctx.getRegularInfo().getRegularId());
//        tbTermBatch.setParDay(ctx.getHostDate().substring(4, 6));
//        tbTermBatch.setParMon(ctx.getHostDate().substring(6, 8));
//        tbTermBatch.setCutDate(ctx.getHostDate());
//        tbTermBatch.setCutTime(ctx.getHostTime());
//        tbTermBatch.setTermUpDate(ctx.getHostDate());
////        tbTermBatch.setRegularId(ctx.getRegularInfo().getRegularId());
//        tbTermBatch.setCutRcode(Constants.RCODE_0000_OK);
//        String batchNo = SequenceGenerator.getBatchNoByType(connection,SequenceGenerator.TYPE_BATCH);
//        tbTermBatch.setBatchNo(batchNo);
//        logger.debug(tbTermBatch.toInsertSQL());
//        TbTermBatchMgr transDtlMgr = new TbTermBatchMgr(connection);
//        transDtlMgr.insert(tbTermBatch);
//        connection.commit();
//        return tbTermBatch;
//    }
    
//    public static void updateTbRegular(Connection connection, RegularAwardContext ctx) throws SQLException
//    {
//        String sqlCmd = "update tb_regular set proc_date = ? , proc_time = ? where regular_id = ?";
//        Vector<String> params = new Vector<String>();
//        params.add(ctx.getHostDate());
//        params.add(ctx.getHostTime());
//        params.add(ctx.getRegularInfo().getRegularId());
//        DbUtil.sqlAction(sqlCmd, params, connection);
//        connection.commit();
//    }
}
