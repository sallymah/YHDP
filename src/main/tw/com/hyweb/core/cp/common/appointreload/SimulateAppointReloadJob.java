/*
 * $Id: SimulateAppointReloadJob.java 3650 2010-09-01 02:40:34Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.common.appointreload;

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
import tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.DateRange;
import tw.com.hyweb.service.db.info.TbAppointReloadDtlInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbAppointReloadDtlMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.svc.yhdp.online.util.Constant;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Anny
 * @version $Revision: 3650 $
 */
public class SimulateAppointReloadJob extends SimulateOnlineJob
{
    private static final Logger LOGGER = Logger.getLogger(SimulateAppointReloadJob.class);
    
	private final Map<String,String> appointReloadInfo; 

	/**
	 * @param terminalBatch
	 */
	public SimulateAppointReloadJob(TbTermBatchInfo terminalBatch, Map<String,String> info) {
		
		super(terminalBatch);
		
		this.appointReloadInfo = info;
		
		LOGGER.info(info);
	}

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#makeTransaction(java.sql.Connection)
	 */
	@Override
	protected TbOnlTxnInfo makeTransaction(Connection connection)
			throws Exception {
		
		TbOnlTxnInfo onlTxnInfo = new TbOnlTxnInfo();
		onlTxnInfo.setCardNo(appointReloadInfo.get("CARD_NO"));
		onlTxnInfo.setExpiryDate(appointReloadInfo.get("EXPIRY_DATE"));
		onlTxnInfo.setPCode(Constants.PCODE_7307);
		onlTxnInfo.setAcqMemId(appointReloadInfo.get("ACQ_MEM_ID"));
		onlTxnInfo.setMerchId(appointReloadInfo.get("MERCH_ID"));
		
		return onlTxnInfo;
	}

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#makeTransactionDetails(java.sql.Connection)
	 */
	@Override
	protected Collection<TbOnlTxnDtlInfo> makeTransactionDetails(
			Connection connection) throws Exception {
		
		List<TbOnlTxnDtlInfo> onlTxnDtlInfos = new ArrayList();	
		
		for (TbAppointReloadDtlInfo infoDtl : makeAppointReloadDetail(connection))
			onlTxnDtlInfos.add(makeTransactionDetail(connection, infoDtl));
		
		return onlTxnDtlInfos;
	}
	
    private List<TbAppointReloadDtlInfo> makeAppointReloadDetail(Connection connection) throws SQLException
    {
        StringBuffer sqlCmd = new StringBuffer("");
        sqlCmd.append("bonus_base = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(appointReloadInfo.get("BONUS_BASE")));
        
        sqlCmd.append("AND balance_type = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(appointReloadInfo.get("BALANCE_TYPE")));
        
        sqlCmd.append("AND balance_id = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(appointReloadInfo.get("BALANCE_ID")));
        
        sqlCmd.append("AND ar_serno = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(appointReloadInfo.get("AR_SERNO")));
        
		Vector<TbAppointReloadDtlInfo> results = new Vector();

        TbAppointReloadDtlMgr mgr = new TbAppointReloadDtlMgr(connection);
        mgr.queryMultiple(sqlCmd.toString(), results);
        
        return results;
    }
	
    protected TbOnlTxnDtlInfo makeTransactionDetail(Connection connection, TbAppointReloadDtlInfo appointReloadDtlInfo) throws Exception
    {
    	LOGGER.info("call campaign : bonus sdate edate");
    	DateRange range = PreOperationUtil.getBonusSdateEdate(connection, terminalBatch.getTermSettleDate(), appointReloadInfo.get("BALANCE_TYPE"), appointReloadInfo.get("CARD_NO"), appointReloadInfo.get("EXPIRY_DATE"), appointReloadDtlInfo.getBonusId(), appointReloadDtlInfo.getBonusSdate(), appointReloadDtlInfo.getBonusEdate());
    	
    	LOGGER.info("range:" + range);
    	TbOnlTxnDtlInfo onlTxnInfoDtl = new TbOnlTxnDtlInfo();
    	onlTxnInfoDtl.setTxnCode(Constants.TXNCODE_8817);
    
    	onlTxnInfoDtl.setRegionId(appointReloadInfo.get("REGION_ID"));
    	onlTxnInfoDtl.setBonusBase(appointReloadInfo.get("BONUS_BASE"));
    	onlTxnInfoDtl.setBalanceType(appointReloadInfo.get("BALANCE_TYPE"));
    	onlTxnInfoDtl.setBalanceId(appointReloadInfo.get("BALANCE_ID"));
    	onlTxnInfoDtl.setBonusId(appointReloadDtlInfo.getBonusId());
    	onlTxnInfoDtl.setBonusSdate(range.getStartDate());
    	onlTxnInfoDtl.setBonusEdate(range.getEndDate());
    	onlTxnInfoDtl.setBonusQty(appointReloadDtlInfo.getBonusQty());

    	return onlTxnInfoDtl;
    }
    

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#preoperation(java.sql.Connection, java.lang.String)
	 */
	@Override
	protected void preoperation(Connection connection, String batchDate)
			throws Exception {
		// TODO Auto-generated method stub
		TbCardPK pk = new TbCardPK();
        pk.setCardNo(appointReloadInfo.get("CARD_NO"));
        pk.setExpiryDate(appointReloadInfo.get("EXPIRY_DATE"));
        TbCardInfo cardInfo =  new TbCardMgr(connection).querySingle(pk);
        
        StringBuffer sqlCmd = new StringBuffer();
        Vector<String> params = new Vector<String>();
        
        if ( cardInfo.getStatus().equals("2")){
        	
	        sqlCmd.append("UPDATE TB_CARD set ");
	        //sqlCmd.append("ACTIVE_DATE = ?, STATUS = ?, PREVIOUS_STATUS = ?, ACTIVE_CNT = ACTIVE_CNT + ?, CARD_OPEN_OWNER = ?, STATUS_UPDATE_DATE = ?, UPT_USERID = ?, ");
	        sqlCmd.append("ACTIVE_DATE = ?, STATUS = ?, PREVIOUS_STATUS = ?, CARD_OPEN_OWNER = ?, STATUS_UPDATE_DATE = ?, UPT_USERID = ?, ");
	        //sqlCmd.append("FIRST_TXN_DATE = ?, LAST_TXN_DATE = ?, UPT_DATE = ?, UPT_TIME = ?, APRV_USERID = ?, APRV_DATE = ?, APRV_TIME = ?, EXP_UPT_STATUS = ? ");
	        sqlCmd.append("FIRST_TXN_DATE = ?, LAST_TXN_DATE = ?, UPT_DATE = ?, UPT_TIME = ?, APRV_USERID = ?, APRV_DATE = ?, APRV_TIME = ? ");
	        sqlCmd.append("where CARD_NO = ? and EXPIRY_DATE = ?");
	       
    		params.add(batchDate); //ACTIVE_DATE
    		params.add("3"); //STATUS
    		params.add(cardInfo.getStatus());//PREVIOUS_STATUS
    		//params.add("1"); //ACTIVE_CNT
    		params.add(appointReloadInfo.get("MERCH_ID")); //CARD_OPEN_OWNER
    		params.add(batchDate); //STATUS_UPDATE_DATE
    		
    		params.add("BATCH"); //UPT_USERID
    		params.add(batchDate); //FIRST_TXN_DATE
    		params.add(batchDate); //LAST_TXN_DATE
        	params.add(batchDate); //UPT_DATE
        	params.add(DateUtil.getTodayString().substring(8, 14)); //UPT_TIME
        	params.add("BATCH"); //APRV_USERID
        	params.add(batchDate); //APRV_DATE
        	params.add(DateUtil.getTodayString().substring(8, 14)); //APRV_TIME
        	//params.add(Constant.UPT_STATUS_MODIFY); //EXP_UPT_STATUS
    		
    		params.add(cardInfo.getCardNo());
    		params.add(cardInfo.getExpiryDate());
    		DbUtil.sqlAction(sqlCmd.toString(), params, connection);
    		
        }
        else if (cardInfo.getStatus().equals("3")){
	        sqlCmd.append("UPDATE TB_CARD set ");
	        sqlCmd.append("LAST_TXN_DATE = ? ");
	        sqlCmd.append("where CARD_NO = ? and EXPIRY_DATE = ?");

    		params.add(batchDate); //LAST_TXN_DATE
    		params.add(cardInfo.getCardNo());
    		params.add(cardInfo.getExpiryDate());
    		DbUtil.sqlAction(sqlCmd.toString(), params, connection);
        }

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
    	String sql = "UPDATE TB_APPOINT_RELOAD set status = ?, proc_date = ?, batch_rcode = ?, dw_txn_date = ?, dw_txn_time = ?, dw_lms_invoice_no = ? where bonus_base = ? and balance_type = ? and balance_id = ? and ar_serno = ?";
        
    	Vector params = new Vector();    	
    	params.add("1");
    	params.add(batchDate);
    	params.add(Constants.RCODE_0000_OK);
    	params.add(terminalBatch.getTermSettleDate());
    	params.add(terminalBatch.getTermSettleTime());
    	params.add(lmsInvoiceNumber);
    	params.add(appointReloadInfo.get("BONUS_BASE"));
    	params.add(appointReloadInfo.get("BALANCE_TYPE"));
    	params.add(appointReloadInfo.get("BALANCE_ID"));
    	params.add(appointReloadInfo.get("AR_SERNO"));
    	
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
    	String rcode = Constants.RCODE_2021_SimulateAppointReloadBalance_ERR;
    	if (!StringUtil.isEmpty(batchJobException.getRcode()))
    		rcode = batchJobException.getRcode();
    	
    	String sql = "UPDATE TB_APPOINT_RELOAD set proc_date = ?, batch_rcode = ?, dw_txn_date = ?, dw_txn_time = ?, dw_lms_invoice_no = ? where bonus_base = ? and balance_type = ? and balance_id = ? and ar_serno = ?";
        
    	Vector params = new Vector();    
    	params.add(batchDate);
    	params.add(rcode);
    	params.add(terminalBatch.getTermSettleDate());
    	params.add(terminalBatch.getTermSettleTime());
    	params.add(lmsInvoiceNumber);
    	params.add(appointReloadInfo.get("BONUS_BASE"));
    	params.add(appointReloadInfo.get("BALANCE_TYPE"));
    	params.add(appointReloadInfo.get("BALANCE_ID"));
    	params.add(appointReloadInfo.get("AR_SERNO"));
    	
    	DbUtil.sqlAction(sql, params, connection);
    	
    }
    

}
