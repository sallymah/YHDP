/*
 * $Id: SimulateAppointReloadJob.java 3650 2010-09-01 02:40:34Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpRmCard;

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
import tw.com.hyweb.core.cp.common.misc.DateRange;
import tw.com.hyweb.service.db.info.TbAppointReloadDtlInfo;
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.info.TbCardBalInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.mgr.TbAppointReloadDtlMgr;
import tw.com.hyweb.service.db.mgr.TbCardBalMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Anny
 * @version $Revision: 3650 $
 */
public class SimulateAppointReloadJob extends SimulateTransJob
{
    private static final Logger LOGGER = Logger.getLogger(SimulateAppointReloadJob.class);
    
	private final Map<String,String> appointReloadInfo; 
	private final List<TbBonusIssDefInfo> tbBonusIssDefList;
	private Vector<TbAppointReloadDtlInfo> results;
	private Vector<TbCardBalInfo> cardBalResults;
	private TbCardInfo cardInfo;
	/**
	 * @param terminalBatch
	 */
	public SimulateAppointReloadJob(TbTermBatchInfo terminalBatch, Map<String,String> info, List<TbBonusIssDefInfo> tbBonusIssDefList) {
		
		super(terminalBatch);
		
		this.appointReloadInfo = info;
		this.tbBonusIssDefList = tbBonusIssDefList;
		
		LOGGER.info(info);
	}

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#makeTransaction(java.sql.Connection)
	 */
	@Override
	protected TbTransInfo makeTransaction(Connection connection)
			throws Exception {
		
		TbTransInfo transInfo = new TbTransInfo();
		transInfo.setCardNo(appointReloadInfo.get("CARD_NO"));
		transInfo.setExpiryDate(appointReloadInfo.get("EXPIRY_DATE"));
		transInfo.setPCode(Constants.PCODE_7307);
		
		//需求單號 #394 虛擬指定加值的收單代號為: 00001001
		String acqMemId = (!appointReloadInfo.get("ACQ_MEM_ID").toString().equals("00000000"))
				? appointReloadInfo.get("ACQ_MEM_ID").toString():"00001001";
				
		transInfo.setAcqMemId(acqMemId);
		transInfo.setMerchId(appointReloadInfo.get("MERCH_ID"));
		transInfo.setTermDate(terminalBatch.getTermSettleDate());
		transInfo.setTermTime(terminalBatch.getTermSettleTime());
		
		double[] sumBefore = new double[LMSContext.getMaxPoint()];
		double[] sumBeforeCr = new double[LMSContext.getMaxPoint()];
		double[] sumBeforeDb = new double[LMSContext.getMaxPoint()];
		double[] sumAfter = new double[LMSContext.getMaxPoint()];
		for ( int i = 0; i < LMSContext.getMaxPoint(); i ++ ){
    		 sumBefore[i] = 0;
    		 sumBeforeCr[i] = 0;
    		 sumBeforeDb[i] = 0;
    		 sumAfter[i] = 0;
		}
         
		for ( TbBonusIssDefInfo tbBonusIssDefInfo : tbBonusIssDefList ){
        	 
			if ( cardInfo.getMemId().equals(tbBonusIssDefInfo.getMemId())){
				
				for (TbCardBalInfo infoDtl : cardBalResults){
					
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint1BonusId()))
						sumBefore[0] = sumBefore[0] + infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint2BonusId()))
						sumBefore[1] = sumBefore[1] + infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint3BonusId()))
						sumBefore[2] = sumBefore[2] + infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint4BonusId()))
						sumBefore[3] = sumBefore[3] + infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue();
							
				}
				
				for (TbAppointReloadDtlInfo infoDtl : results){
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint1BonusId()))
						sumBeforeCr[0] = sumBeforeCr[0] + infoDtl.getBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint2BonusId()))
						sumBeforeCr[1] = sumBeforeCr[1] + infoDtl.getBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint3BonusId()))
						sumBeforeCr[2] = sumBeforeCr[2] + infoDtl.getBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint4BonusId()))
						sumBeforeCr[3] = sumBeforeCr[3] + infoDtl.getBonusQty().doubleValue();
							
				}
				
				for ( int i = 0; i < LMSContext.getMaxPoint(); i ++ ){
	        		 sumAfter[i] = sumBefore[i] + sumBeforeCr[i];
	        	 }
			}
		}
		
		transInfo.setChipPoint1Before(sumBefore[0]);
		transInfo.setChipPoint2Before(sumBefore[1]);
		transInfo.setChipPoint3Before(sumBefore[2]);
		transInfo.setChipPoint4Before(sumBefore[3]);

		transInfo.setChipPoint1Cr(sumBeforeCr[0]);
		transInfo.setChipPoint2Cr(sumBeforeCr[1]);
		transInfo.setChipPoint3Cr(sumBeforeCr[2]);
		transInfo.setChipPoint4Cr(sumBeforeCr[3]);

		transInfo.setChipPoint1Db(sumBeforeDb[0]);
		transInfo.setChipPoint2Db(sumBeforeDb[1]);
		transInfo.setChipPoint3Db(sumBeforeDb[2]);
		transInfo.setChipPoint4Db(sumBeforeDb[3]);

		transInfo.setChipPoint1After(sumAfter[0]);
		transInfo.setChipPoint2After(sumAfter[1]);
		transInfo.setChipPoint3After(sumAfter[2]);
		transInfo.setChipPoint4After(sumAfter[3]);
		
		return transInfo;
	}

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#makeTransactionDetails(java.sql.Connection)
	 */
	@Override
	protected Collection<TbTransDtlInfo> makeTransactionDetails(
			Connection connection) throws Exception {
		
		List<TbTransDtlInfo> transDtlInfo = new ArrayList();	
		
		for (TbAppointReloadDtlInfo infoDtl : results)
			transDtlInfo.add(makeTransactionDetail(connection, infoDtl));
		
		return transDtlInfo;
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
        
		results = new Vector();

        TbAppointReloadDtlMgr mgr = new TbAppointReloadDtlMgr(connection);
        mgr.queryMultiple(sqlCmd.toString(), results);
        
        return results;
    }
	
    protected TbTransDtlInfo makeTransactionDetail(Connection connection, TbAppointReloadDtlInfo appointReloadDtlInfo) throws Exception
    {
    	LOGGER.info("call campaign : bonus sdate edate");
    	DateRange range = PreOperationUtil.getBonusSdateEdate(connection, terminalBatch.getTermSettleDate(), appointReloadInfo.get("BALANCE_TYPE"), appointReloadInfo.get("CARD_NO"), appointReloadInfo.get("EXPIRY_DATE"), appointReloadDtlInfo.getBonusId(), appointReloadDtlInfo.getBonusSdate(), appointReloadDtlInfo.getBonusEdate());
    	
    	LOGGER.info("range:" + range);
    	TbTransDtlInfo transDtlInfo = new TbTransDtlInfo();
    	transDtlInfo.setTxnCode(Constants.TXNCODE_8817);
    
    	transDtlInfo.setCutDate(terminalBatch.getCutDate());
    	transDtlInfo.setRegionId(appointReloadInfo.get("REGION_ID"));
    	transDtlInfo.setBonusBase(appointReloadInfo.get("BONUS_BASE"));
    	transDtlInfo.setBalanceType(appointReloadInfo.get("BALANCE_TYPE"));
    	transDtlInfo.setBalanceId(appointReloadInfo.get("BALANCE_ID"));
    	transDtlInfo.setBonusId(appointReloadDtlInfo.getBonusId());
    	transDtlInfo.setBonusSdate(range.getStartDate());
    	transDtlInfo.setBonusEdate(range.getEndDate());
    	transDtlInfo.setBonusQty(appointReloadDtlInfo.getBonusQty());
    	transDtlInfo.setBalProcDate("00000000");
    	
    	transDtlInfo.setBonusBeforeQty(0);
    	for (TbCardBalInfo infoDtl : cardBalResults){
    		if ( infoDtl.getBonusId().equalsIgnoreCase(transDtlInfo.getBonusId()) 
    				&&  infoDtl.getBonusSdate().equalsIgnoreCase(transDtlInfo.getBonusSdate()) 
    				&&  infoDtl.getBonusEdate().equalsIgnoreCase(transDtlInfo.getBonusEdate()) ){
    			
    			transDtlInfo.setBonusBeforeQty(infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue());
    		}
    	}

    	transDtlInfo.setBonusCrQty(appointReloadDtlInfo.getBonusQty());
    	transDtlInfo.setBonusAfterQty(transDtlInfo.getBonusBeforeQty().doubleValue() + transDtlInfo.getBonusCrQty().doubleValue());
    	

    	return transDtlInfo;
    }
    

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#preoperation(java.sql.Connection, java.lang.String)
	 */
	@Override
	protected void preoperation(Connection connection, String batchDate)
			throws Exception {
		// TODO Auto-generated method stub
		
		//需先抓取DTL，計算交易前後金額、TRANS各點數金額
		makeAppointReloadDetail(connection);
		
		TbCardPK pk = new TbCardPK();
        pk.setCardNo(appointReloadInfo.get("CARD_NO"));
        pk.setExpiryDate(appointReloadInfo.get("EXPIRY_DATE"));
        cardInfo =  new TbCardMgr(connection).querySingle(pk);
        
        
        StringBuffer cardBalSqlCmd = new StringBuffer("");
        cardBalSqlCmd.append("CARD_NO = ");
        cardBalSqlCmd.append(StringUtil.toSqlValueWithSQuote(appointReloadInfo.get("CARD_NO")));
        
        cardBalSqlCmd.append("AND EXPIRY_DATE = ");
        cardBalSqlCmd.append(appointReloadInfo.get("EXPIRY_DATE"));
        
        cardBalSqlCmd.append(" AND BONUS_SDATE <= ");
        cardBalSqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        cardBalSqlCmd.append(" AND BONUS_EDATE >= ");
        cardBalSqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        this.cardBalResults = new Vector();

        TbCardBalMgr mgr = new TbCardBalMgr(connection);
        mgr.queryMultiple(cardBalSqlCmd.toString(), cardBalResults);
        
        
        
        StringBuffer sqlCmd = new StringBuffer();
        Vector<String> params = new Vector<String>();
        
        sqlCmd.append("UPDATE TB_CARD set ");
        sqlCmd.append("LAST_TXN_DATE = ? ");
        sqlCmd.append("where CARD_NO = ? and EXPIRY_DATE = ?");

		params.add(batchDate); //LAST_TXN_DATE
		params.add(cardInfo.getCardNo());
		params.add(cardInfo.getExpiryDate());
		DbUtil.sqlAction(sqlCmd.toString(), params, connection);
		
	}

    /**
     * ���O�B�z���\
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess
     *      (java.sql.Connection, java.lang.String)
     */
    @Override
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
    	String sql = "UPDATE TB_APPOINT_RELOAD " +
    			"set status = ?, " +
    			"batch_rcode = ?, " +
    			"dw_txn_date = ?, " +
    			"dw_txn_time = ?, " +
    			"dw_lms_invoice_no = ? " +
    			"where bonus_base = ? " +
    			"and balance_type = ? " +
    			"and balance_id = ? " +
    			"and ar_serno = ?";
    	
    	
        
    	Vector params = new Vector();    	
    	params.add("1");
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
     * ���O�B�z����
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkFailure
     *      (java.sql.Connection, java.lang.String,
     *      tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException)
     */
    @Override
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {
    	
    }
    

}
