package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpAdjustmentTxn;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.callcampaign.BalanceUtil;
import tw.com.hyweb.core.cp.batch.callcampaign.BonusInfo;
import tw.com.hyweb.core.cp.batch.util.beans.TermBatchBean;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.core.yhdp.common.misc.*;
import tw.com.hyweb.service.db.info.*;
import tw.com.hyweb.service.db.mgr.TbBonusDtlMgr;
import tw.com.hyweb.service.db.mgr.TbBonusMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class AdjustTxnData
{
    private static Logger log = Logger.getLogger(AdjustTxnData.class);

    private final Map<String, String> fileData;
    private final TbCardInfo cardInfo;
    private final TbBonusInfo bonusInfo;
    private final DateRange bonusRange;
    private final String fullFileName;
    
    
    private String bonusBase = "H";
    private String balanceType = "C";
    private String txnSrc ="L";
    private String onlineFlag = "O";
    private String Status = "1"; //交易Success
    private String adjustType = "3"; //主機紅利要列帳
    private String quotaFlag = "1"; //是否調整已發行額度  0: 否  1: 是
    private String balUpdateFlag = "N"; //晶片紅利, 是否補登至卡片 Y: 是 N: 否

    
    private String maxEndDate = "99991231";
    
    public AdjustTxnData(Connection connection, Map<String, String> fileData, String fullFileName) throws SQLException
    {
    	this.fileData = fileData;
    	this.fullFileName = fullFileName;
    	this.cardInfo = getCardInfo(connection , fileData.get("CARD_NO"), fileData.get("EXPIRY_DATE"));
    	this.bonusRange = getBonusSdateEdate(connection, fileData.get("BONUS_ID"),fileData.get("BONUS_SDATE"),fileData.get("BONUS_EDATE"));
    	this.bonusInfo = queryBonusInfo(connection, fileData.get("BONUS_ID"));
    	String creditUnit = fileData.get("CREDIT_UNIT");
        String debitUnit = fileData.get("DEBIT_UNIT");
        
    }
    
    private TbCardInfo getCardInfo(Connection connection, String cardNumber, String expiryDate) throws SQLException
    {
        TbCardPK pk = new TbCardPK();
        pk.setCardNo(cardNumber);
        pk.setExpiryDate(expiryDate);

        return new TbCardMgr(connection).querySingle(pk);
    }

    private TbBonusInfo queryBonusInfo(Connection connection, String bonusId) {
        
    	TbBonusInfo bonusInfo = new TbBonusInfo();
    	try {
            TbBonusMgr mgr = new TbBonusMgr(connection);
            bonusInfo = mgr.querySingle(bonusId);
        }
        catch (Exception ignore) {
            bonusInfo = null;
            log.warn("queryBonusInfo error:" + ignore.getMessage());
        }
		return bonusInfo;
    }

    private DateRange getBonusSdateEdate(Connection connection, String bonusId, String bonusSdate, String bonusEdate) throws SQLException
    {
    	DateRange range = new DateRange();
    	
        if (StringUtil.isEmpty(bonusSdate) && StringUtil.isEmpty(bonusEdate)) {
            // 去 TB_BONUS_DTL 主檔取得 BONUS 效期定義
            Vector results = new Vector();
            TbBonusDtlMgr mgr = new TbBonusDtlMgr(connection);
            TbBonusDtlInfo qinfo = new TbBonusDtlInfo();
            qinfo.toEmpty();
            qinfo.setBonusId(bonusId);
            mgr.queryMultiple(qinfo, results);
            
            if (results.size() == 0) {
                throw new IllegalArgumentException("bonusDtlInfo is null for '" + bonusId + "'!");
            }
            if (results.size() > 1) {
                throw new IllegalArgumentException("bonusDtlInfo is multiple for '" + bonusId + "'!" + results);
            }
            
            TbBonusDtlInfo bonusDtlInfo = (TbBonusDtlInfo) results.get(0);
            
            range.setStartDate(bonusDtlInfo.getBonusSdate());
            range.setEndDate(bonusDtlInfo.getBonusEdate());
                  
        }
        else{
        	range.setStartDate(bonusSdate);
        	range.setEndDate(bonusEdate);
        }
        
        return range;
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public TbCardInfo getCardInfo() {
		return cardInfo;
	}

    public List handleAdjustTxn(Connection connection, String batchDate, TbTermBatchInfo termBatchInfo, String termId, String termBatchNo) throws Exception {
    	
    	String arSerno = termBatchNo;
    	String lmsInvoiceNo = SequenceGenerator.getLmsInvoiceNo(connection, batchDate);
    	TbAdjustTxnInfo adjustTxnInfo = makeAdjustTxn(arSerno,lmsInvoiceNo , batchDate, termBatchInfo, termId);
    	TbAdjustTxnDtlInfo adjustTxnDtlInfo = makeAdjustTxnDtl(adjustTxnInfo);
    	
    	String cardBalInfo = makeBalSQL(connection,adjustTxnDtlInfo);
    	
        List sqls = new ArrayList();
        sqls.add(adjustTxnInfo.toInsertSQL());
        sqls.add(adjustTxnDtlInfo.toInsertSQL());

        sqls.add(cardBalInfo);
        
        return sqls;
    }
    
    
    private TbAdjustTxnInfo makeAdjustTxn(String arSerno, String lmsInvoiceNo, String batchDate, TbTermBatchInfo termBatchInfo, String termId) {

    	TbAdjustTxnInfo info = new TbAdjustTxnInfo();
    	
    	info.setTxnSrc(txnSrc);
    	info.setOnlineFlag(onlineFlag);
    	info.setAcqMemId(fileData.get("ACQ_MEM_ID"));
    	info.setIssMemId(cardInfo.getMemId());
    	info.setMerchId(fileData.get("MERCH_ID"));
    	info.setTermId(termId);
    	info.setBatchNo(arSerno);
    	info.setTermSettleDate(termBatchInfo.getTermSettleDate());
    	info.setTermSettleTime(termBatchInfo.getTermSettleTime());
    	info.setCardNo(fileData.get("CARD_NO"));
    	info.setExpiryDate(fileData.get("EXPIRY_DATE"));
    	info.setLmsInvoiceNo(lmsInvoiceNo);
    	info.setPCode(Constants.PCODE_7907);
    	info.setTxnDate(fileData.get("TXN_DATE"));
    	info.setTxnTime(fileData.get("TXN_TIME"));
    	info.setCurrencyCode(Constants.CURRENCYCODE_CHN);
    	info.setStatus(Status);
    	info.setAdjustType(adjustType);
    	info.setQuotaFlag(quotaFlag);
    	info.setMemo(fileData.get("MEMO"));
    	info.setParMon(termBatchInfo.getTermSettleDate().substring(4, 6));
        info.setParDay(termBatchInfo.getTermSettleDate().substring(6, 8));
//        info.setTermDate(fileData.get("TXN_DATE"));
//  	  info.setTermTime(fileData.get("TXN_DATE"));
        //info.setImpFullFileName(fullFileName);
        //info.setImpDate(DateUtil.getTodayString().substring(0, 8));
        //info.setImpTime(DateUtil.getTodayString().substring(8, 14));

        return info;
    }

    private TbAdjustTxnDtlInfo makeAdjustTxnDtl(TbAdjustTxnInfo adjustTxnInfo) throws SQLException {
    	
    	TbAdjustTxnDtlInfo info = new TbAdjustTxnDtlInfo();
    	String txnCode = "";
    	String sign = "";
    	if ( fileData.get("SIGN").equals("0") ){
    		sign = Constants.SIGN_PLUS;
    		txnCode = Constants.TXNCODE_8907;
    	}
    	else if ( fileData.get("SIGN").equals("1") ){
    		sign = Constants.SIGN_MINUS;
    		txnCode = Constants.TXNCODE_8917;
    	}
    	
    	//info.setBatchNo(adjustTxnInfo.getBatchNo());	
    	info.setRegionId("CHN");
    	info.setBonusBase(bonusBase);
    	info.setBalanceType(balanceType);
//    	info.setTermSettleDate(adjustTxnInfo.getTermSettleDate());
//    	info.setTermSettleTime(adjustTxnInfo.getTermSettleTime());
    	info.setBalanceId(fileData.get("CARD_NO"));
    	info.setCardNo(fileData.get("CARD_NO"));
    	info.setExpiryDate(fileData.get("EXPIRY_DATE"));
    	info.setLmsInvoiceNo(adjustTxnInfo.getLmsInvoiceNo());
    	info.setPCode(Constants.PCODE_7907);
    	info.setTxnCode(txnCode);
//    	info.setTxnDate(adjustTxnInfo.getTxnDate());
//    	info.setTxnTime(adjustTxnInfo.getTxnTime());
    	info.setBonusId(fileData.get("BONUS_ID"));
        info.setBonusSdate(bonusRange.getStartDate());
        info.setBonusEdate(bonusRange.getEndDate());
        info.setBonusQty(Double.valueOf(fileData.get("BONUS_QTY")));
    	info.setBalUpdateFlag(balUpdateFlag);
    	info.setCreditUnit(fileData.get("CREDIT_UNIT"));
    	info.setDebitUnit(fileData.get("DEBIT_UNIT"));
    	info.setCreditId(fileData.get("CREDIT_ID"));
    	info.setDebitId(fileData.get("DEBIT_ID"));
    	info.setSign(sign);
    	info.setParMon(adjustTxnInfo.getParMon());
        info.setParDay(adjustTxnInfo.getParDay());
    	
        return info;
    }

    /*private String makeCardBalInfo(TbAdjustTxnDtlInfo adjustTxnDtlInfo) throws SQLException{
      	 
    	StringBuffer cardBalInfo = new StringBuffer();
		
    	cardBalInfo.append("UPDATE TB_CARD_BAL SET ");
    	
    	if ( adjustTxnDtlInfo.getSign().equals("P") )
    		cardBalInfo.append("CR_BONUS_QTY = CR_BONUS_QTY + "+adjustTxnDtlInfo.getBonusQty());
    	else if ( adjustTxnDtlInfo.getSign().equals("M") )
    		cardBalInfo.append("DB_BONUS_QTY = DB_BONUS_QTY + "+adjustTxnDtlInfo.getBonusQty());
    	cardBalInfo.append(" WHERE CARD_NO = '"+ adjustTxnDtlInfo.getCardNo() +"'");
    	cardBalInfo.append(" AND EXPIRY_DATE = '"+ adjustTxnDtlInfo.getExpiryDate() +"'");
    	cardBalInfo.append(" AND BONUS_ID = '"+ adjustTxnDtlInfo.getBonusId() +"'");
    	cardBalInfo.append(" AND BONUS_SDATE = '"+ adjustTxnDtlInfo.getBonusSdate() +"'");
    	cardBalInfo.append(" AND BONUS_EDATE = '"+ adjustTxnDtlInfo.getBonusEdate() +"'");
    	
    	log.info(cardBalInfo.toString());
    	
		return cardBalInfo.toString();
    }*/

    private String makeBalSQL(Connection connection, TbAdjustTxnDtlInfo adjustTxnDtlInfo) throws Exception {
      String balSQL = "";
      // 指定加值的 txnCode
      String txnCode = adjustTxnDtlInfo.getTxnCode();
      BonusInfo aBonusInfo = new BonusInfo();
      aBonusInfo.setBonusId(adjustTxnDtlInfo.getBonusId());
      aBonusInfo.setBonusSDate(adjustTxnDtlInfo.getBonusSdate());
      aBonusInfo.setBonusEDate(adjustTxnDtlInfo.getBonusEdate());
      aBonusInfo.setBonusQtySum((Double) adjustTxnDtlInfo.getBonusQty());
      aBonusInfo.setBonusIdCnt(1);
      aBonusInfo.setTbBonusInfo(bonusInfo);
      
      TbCardBalInfo cardBalInfo = BalanceUtil.makeCardBalInfo(adjustTxnDtlInfo.getCardNo(), adjustTxnDtlInfo.getExpiryDate(), aBonusInfo);
      balSQL = BalanceUtil.makeCardBalSQL(connection, cardBalInfo, txnCode, aBonusInfo.getBonusQtySum());
      
      return balSQL;
  }

    
    
}
