package tw.com.hyweb.svc.yhdp.batch.impdata.procPrepaidCharge;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.svc.yhdp.batch.util.BatchUtils;

import tw.com.hyweb.service.db.info.TbPrepaidChargeInfo;
import tw.com.hyweb.service.db.mgr.TbPrepaidChargeMgr;

public class ProcPrepaidChargeJob extends GenericBatchJob {
	private final Map<String, String> result ;
	private final List<String> consPcodes;

	public ProcPrepaidChargeJob(Map<String, String> result, List<String> consPcodes)
    {
        this.result = result;
        this.consPcodes = consPcodes;
    }
	
	public void action(Connection connection, String batchDate) throws Exception
    {
		String pCode = result.get("P_CODE");
		double bonusBeforeQty = Double.parseDouble(result.get("BONUS_BEFORE_QTY"));
		double bonusAfterQty = Double.parseDouble(result.get("BONUS_AFTER_QTY"));
		double bonusQty = Double.parseDouble(result.get("BONUS_QTY"));
		double prepaidCharge = 0; 
		
		if (consPcodes.contains(pCode)){
			//代墊款
			if ( bonusBeforeQty >= 0 )			// 交易前餘額為正
				prepaidCharge = BatchUtils.sub(bonusQty, bonusBeforeQty);
			else								// 交易前餘額為負
				prepaidCharge = bonusQty;
		}
		else{
			//代墊款沖銷
			if ( bonusAfterQty >= 0 )			// 交易後餘額為正
				prepaidCharge = BatchUtils.sub(bonusQty, bonusAfterQty);
			else								// 交易後餘額為負
				prepaidCharge = bonusQty;
		}

		TbPrepaidChargeMgr prepaidChargeMgr = new TbPrepaidChargeMgr(connection);
		prepaidChargeMgr.insert(getInsertPrepaidChargeSQL(batchDate, prepaidCharge));
    }

	private TbPrepaidChargeInfo getInsertPrepaidChargeSQL(String batchDate, double prepaidCharge) throws Exception
    {
		TbPrepaidChargeInfo prepaidChargeInfo = new TbPrepaidChargeInfo();
		prepaidChargeInfo.setAcqMemId(result.get("ACQ_MEM_ID"));
		prepaidChargeInfo.setIssMemId(result.get("ISS_MEM_ID"));
		prepaidChargeInfo.setCardNo(result.get("CARD_NO"));
		prepaidChargeInfo.setExpiryDate(result.get("EXPIRY_DATE"));
		prepaidChargeInfo.setLmsInvoiceNo(result.get("LMS_INVOICE_NO"));
		prepaidChargeInfo.setTxnDate(result.get("TXN_DATE"));
		prepaidChargeInfo.setPCode(result.get("P_CODE"));
		prepaidChargeInfo.setTxnCode(result.get("TXN_CODE"));
		prepaidChargeInfo.setPrepaidCharge(prepaidCharge);
		prepaidChargeInfo.setProcDate(batchDate);
		
		return prepaidChargeInfo;

    }
}
