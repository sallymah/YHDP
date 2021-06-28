/*
 * $Id: SimulateCardReturnJob.java 2529 2010-01-08 10:03:28Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.svc.yhdp.batch.preoperation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.preoperation.PreOperationUtil;
import tw.com.hyweb.core.cp.batch.preoperation.SimulateTransJob;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbBonusDtlInfo;
import tw.com.hyweb.service.db.info.TbCardReturnDtlInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.mgr.TbBonusMgr;
import tw.com.hyweb.service.db.mgr.TbCardReturnDtlMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Anny
 * @version $Revision: 2529 $
 */
public class SimulateCardReturnJob extends SimulateTransJob
{
    private static final Logger LOGGER = Logger.getLogger(SimulateCardReturnJob.class);
    
	private final Map<String,String> cardReturnInfo; 

    private final String pCode = "7527"; //退卡
    private final String txnCode = "8577"; //退卡
    private final String txnCodeFee = "8547"; //退卡手續費
    
	/**
	 * @param terminalBatch
	 */
	public SimulateCardReturnJob(TbTermBatchInfo terminalBatch, Map<String,String> info) {
		
		super(terminalBatch);
		
		this.cardReturnInfo = info;
		
		LOGGER.info(info);
	}

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#makeTransaction(java.sql.Connection)
	 */
	@Override
	protected TbTransInfo makeTransaction(Connection connection)
			throws Exception {
		
		TbTransInfo transInfo = new TbTransInfo();
		transInfo.setCardNo(cardReturnInfo.get("CARD_NO"));
		transInfo.setExpiryDate(cardReturnInfo.get("EXPIRY_DATE"));
		transInfo.setPCode(pCode);
		transInfo.setAcqMemId(cardReturnInfo.get("ACQ_MEM_ID"));
		transInfo.setIssMemId(cardReturnInfo.get("ISS_MEM_ID"));
		transInfo.setTxnDate(cardReturnInfo.get("TXN_DATE"));
		transInfo.setTxnTime(cardReturnInfo.get("TXN_TIME"));
		transInfo.setMerchId(cardReturnInfo.get("MERCH_ID"));
		
		transInfo.setTxnRedeemAmt(Double.valueOf(cardReturnInfo.get("BAL_AMT")));
		transInfo.setTxnPrice(Double.valueOf(cardReturnInfo.get("TXN_PRICE")));
		transInfo.setTxnAmt(Double.valueOf(cardReturnInfo.get("RETURN_AMT")));
		
		return transInfo;
	}

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#makeTransactionDetails(java.sql.Connection)
	 */
	@Override
	protected Collection<TbTransDtlInfo> makeTransactionDetails(
			Connection connection) throws Exception {
		
		
		List<TbTransDtlInfo> transDtlInfos = new ArrayList();	
		
		for (TbCardReturnDtlInfo infoDtl : makeCardReturnDetail(connection))
			transDtlInfos.add(makeTransactionDetail(connection, infoDtl));
			
		transDtlInfos.add(makeTransactionFeeDetail(connection));
		
		return transDtlInfos;
	}
	
    private List<TbCardReturnDtlInfo> makeCardReturnDetail(Connection connection) throws SQLException
    {
        StringBuffer sqlCmd = new StringBuffer("");
        sqlCmd.append("card_no = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(cardReturnInfo.get("CARD_NO")));
        
        sqlCmd.append("AND expiry_date = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(cardReturnInfo.get("EXPIRY_DATE")));
        
        
		Vector<TbCardReturnDtlInfo> results = new Vector();

        TbCardReturnDtlMgr mgr = new TbCardReturnDtlMgr(connection);
        mgr.queryMultiple(sqlCmd.toString(), results);
        
        return results;
    }
    
    private String getBonusType(Connection connection, String bonusId) throws SQLException
    {        
    	return new TbBonusMgr(connection).querySingle(bonusId).getBonusType();
    }
    
    protected TbTransDtlInfo makeTransactionDetail(Connection connection, TbCardReturnDtlInfo cardReturnDtlInfo) throws Exception
    {
    	TbTransDtlInfo transInfoDtl = new TbTransDtlInfo();
    	transInfoDtl.setTxnCode(txnCode);
   
    	transInfoDtl.setBonusBase(getBonusType(connection, cardReturnDtlInfo.getBonusId()));
    	transInfoDtl.setBalanceType(Constants.BALANCETYPE_CARD);
    	transInfoDtl.setBalanceId(cardReturnInfo.get("CARD_NO"));
    	transInfoDtl.setBonusId(cardReturnDtlInfo.getBonusId());
    	transInfoDtl.setBonusSdate(cardReturnDtlInfo.getBonusSdate());
    	transInfoDtl.setBonusEdate(cardReturnDtlInfo.getBonusEdate());
    	transInfoDtl.setBonusQty(cardReturnDtlInfo.getBonusQty());
    	transInfoDtl.setTxnRedeemAmt(cardReturnDtlInfo.getTxnRedeemAmt());
    	
    	return transInfoDtl;
    }

    protected TbTransDtlInfo makeTransactionFeeDetail(Connection connection) throws Exception
    {
    	String feeBonusId = Layer2Util.getBatchConfig("CARD_RETURN_BONUS_ID");
    	TbBonusDtlInfo info = PreOperationUtil.getBonusDtlInfo(connection, feeBonusId);
    	
    	TbTransDtlInfo transInfoDtl = new TbTransDtlInfo();
    	transInfoDtl.setTxnCode(txnCodeFee);
    	
    	transInfoDtl.setBonusBase(Constants.BONUSBASE_HOST);
    	transInfoDtl.setBalanceType(Constants.BALANCETYPE_CARD);
    	transInfoDtl.setBalanceId(cardReturnInfo.get("CARD_NO"));
    	transInfoDtl.setBonusId(feeBonusId);
    	transInfoDtl.setBonusSdate(info.getBonusSdate());
    	transInfoDtl.setBonusEdate(info.getBonusEdate());
    	transInfoDtl.setBonusQty(Double.valueOf(cardReturnInfo.get("CARD_RETURN_FEE_AMT")));
    	
    	return transInfoDtl;
    }

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#preoperation(java.sql.Connection, java.lang.String)
	 */
	@Override
	protected void preoperation(Connection connection, String batchDate)
			throws Exception {

	}

    /**
     * 註記處理成功
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess
     *      (java.sql.Connection, java.lang.String)
     */
    @Override
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
    	String sql = "UPDATE TB_CARD_RETURN set proc_date = ?,  txn_lms_invoice_no = ? where card_no = ? and expiry_date = ?";
        
    	Vector params = new Vector();    	
    	params.add(batchDate);
    	params.add(lmsInvoiceNumber);
    	params.add(cardReturnInfo.get("CARD_NO"));
    	params.add(cardReturnInfo.get("EXPIRY_DATE"));
    	
    	DbUtil.sqlAction(sql, params, connection);
    	
    }

    /**
     * 註記處理失敗
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkFailure
     *      (java.sql.Connection, java.lang.String,
     *      tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException)
     */
    @Override
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {
    	String sql = "UPDATE TB_CARD_RETURN set proc_date = ?, batch_rcode = ? where card_no = ? and expiry_date = ?";
        
    	Vector params = new Vector();    
    	params.add(batchDate);
    	params.add(Constants.RCODE_20S0_SimulateCardReturn_ERR);
    	params.add(cardReturnInfo.get("CARD_NO"));
    	params.add(cardReturnInfo.get("EXPIRY_DATE"));
    	
    	DbUtil.sqlAction(sql, params, connection);
    	
    }
    

}
