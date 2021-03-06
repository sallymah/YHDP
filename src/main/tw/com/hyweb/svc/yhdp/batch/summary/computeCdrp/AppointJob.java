package tw.com.hyweb.svc.yhdp.batch.summary.computeCdrp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.preoperation.PreOperationUtil;
import tw.com.hyweb.core.cp.batch.util.beans.TransBean;
import tw.com.hyweb.core.cp.batch.util.beans.TransDtlBean;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.DateRange;
import tw.com.hyweb.service.db.info.TbAppointReloadDtlInfo;
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.info.TbCardBalInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.mgr.TbAppointReloadDtlMgr;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.string.StringUtil;

public class AppointJob {
	
	private static final Logger logger = Logger.getLogger(AppointJob.class);
	
	private final String batchDate;
	private final TbCardInfo cardInfo;
	private final List<TbBonusIssDefInfo> tbBonusIssDefList;
	private final TbTermBatchInfo terminalBatch;
	private final String lmsInvoiceNumber;
	private final Map<String, String> apt;
	private final HashMap<String, Double> appBonusQtys;
	private final List<TbCardBalInfo> cardBalResults;

	private List<TbAppointReloadDtlInfo> appDtlList;
	private List<String> sqlList = new ArrayList<>();

	public AppointJob(String batchDate, TbCardInfo cardInfo, List<TbCardBalInfo> cardBalResults, 
			List<TbBonusIssDefInfo> tbBonusIssDefList, TbTermBatchInfo terminalBatch, String lmsInvoiceNumber, 
			Map<String, String> apt, HashMap<String, Double> appBonusQtys) {
		// TODO Auto-generated constructor stub
		this.batchDate = batchDate;
		this.cardInfo = cardInfo;
		this.cardBalResults = cardBalResults;
		this.tbBonusIssDefList = tbBonusIssDefList;
		this.terminalBatch = terminalBatch;
		this.lmsInvoiceNumber = lmsInvoiceNumber;
		this.apt = apt;
		this.appBonusQtys = appBonusQtys;
	}

	public void init(Connection connection) throws SQLException {
		// TODO Auto-generated method stub
		appDtlList = makeAppointReloadDetail(connection);
	}
	
	private List<TbAppointReloadDtlInfo> makeAppointReloadDetail(Connection connection) throws SQLException
    {
        StringBuffer sqlCmd = new StringBuffer("");
        sqlCmd.append("bonus_base = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(apt.get("BONUS_BASE")));
        
        sqlCmd.append("AND balance_type = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(apt.get("BALANCE_TYPE")));
        
        sqlCmd.append("AND balance_id = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(apt.get("BALANCE_ID")));
        
        sqlCmd.append("AND ar_serno = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(apt.get("AR_SERNO")));
        
        Vector<TbAppointReloadDtlInfo> results = new Vector();

        TbAppointReloadDtlMgr mgr = new TbAppointReloadDtlMgr(connection);
        mgr.queryMultiple(sqlCmd.toString(), results);
        
        return results;
    }
	
	/*private List<TbCardBalInfo> makeCardBal(Connection connection) throws SQLException
    {
		StringBuffer cardBalSqlCmd = new StringBuffer("");
        cardBalSqlCmd.append("CARD_NO = ");
        cardBalSqlCmd.append(StringUtil.toSqlValueWithSQuote(apt.get("CARD_NO")));
        
        cardBalSqlCmd.append("AND EXPIRY_DATE = ");
        cardBalSqlCmd.append(apt.get("EXPIRY_DATE"));
        
        cardBalSqlCmd.append(" AND BONUS_SDATE <= ");
        cardBalSqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        cardBalSqlCmd.append(" AND BONUS_EDATE >= ");
        cardBalSqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        Vector<TbCardBalInfo> results = new Vector();

        TbCardBalMgr mgr = new TbCardBalMgr(connection);
        mgr.queryMultiple(cardBalSqlCmd.toString(), results);
        
        return results;
    }*/

	public void action(Connection connection) throws Exception {
		// TODO Auto-generated method stub
		/*StringBuffer sql = new StringBuffer();
		
		Vector<String> params = new Vector<String>();
        
        sql.append("UPDATE TB_CARD SET ");
        sql.append("LAST_TXN_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        sql.append("WHERE CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(cardInfo.getCardNo()));
        sql.append("AND EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(cardInfo.getExpiryDate()));
		
		sqlList.add(sql.toString());*/
		
		TbTransInfo transaction = addTransaction(connection);
		addDetails(connection, transaction);
		updateAppoint();
	}

	private void updateAppoint() {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();
		
		sql.append(" UPDATE TB_APPOINT_RELOAD");
		sql.append(" SET STATUS = ").append(StringUtil.toSqlValueWithSQuote("1"));
		sql.append(" ,BATCH_RCODE = ").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
		sql.append(" ,DW_TXN_DATE = ").append(StringUtil.toSqlValueWithSQuote(terminalBatch.getTermSettleDate()));
		sql.append(" ,DW_TXN_TIME = ").append(StringUtil.toSqlValueWithSQuote(terminalBatch.getTermSettleTime()));
		sql.append(" ,DW_LMS_INVOICE_NO = ").append(StringUtil.toSqlValueWithSQuote(lmsInvoiceNumber));
		sql.append(" WHERE BONUS_BASE = ").append(StringUtil.toSqlValueWithSQuote(apt.get("BONUS_BASE")));
		sql.append(" AND BALANCE_TYPE = ").append(StringUtil.toSqlValueWithSQuote(apt.get("BALANCE_TYPE")));
		sql.append(" AND BALANCE_ID = ").append(StringUtil.toSqlValueWithSQuote(apt.get("BALANCE_ID")));
		sql.append(" AND AR_SERNO = ").append(StringUtil.toSqlValueWithSQuote(apt.get("AR_SERNO")));
		
		sqlList.add(sql.toString());
	}

	private TbTransInfo addTransaction(Connection connection) throws Exception
    {
    	TbTransInfo transaction = makeTransaction(connection);
        transaction.setTxnSrc(terminalBatch.getTxnSrc());
        transaction.setBatchNo(terminalBatch.getBatchNo());
        transaction.setTermSettleDate(terminalBatch.getTermSettleDate());
        transaction.setTermSettleTime(terminalBatch.getTermSettleTime());
        transaction.setLmsInvoiceNo(lmsInvoiceNumber);
        
        if (StringUtil.isEmpty(transaction.getTxnDate()))
        	transaction.setTxnDate(terminalBatch.getTermSettleDate());
        
        if (StringUtil.isEmpty(transaction.getTxnTime()))
        	transaction.setTxnTime(terminalBatch.getTermSettleTime());
        
        transaction.setTermDate(transaction.getTxnDate());
        transaction.setTermTime(transaction.getTxnTime());

        if(StringUtil.isEmpty(transaction.getIssMemId()) & !StringUtil.isEmpty(transaction.getCardNo()) & !StringUtil.isEmpty(transaction.getExpiryDate()))
        {
        	transaction.setIssMemId(cardInfo.getMemId());
        }
        
        transaction.setCutDate(terminalBatch.getCutDate());
        transaction.setCutTime(terminalBatch.getCutTime());
        
        TransBean bean = new TransBean();
        bean.setTransInfo(transaction);

        sqlList.add(bean.getInsertSql());
        
        return transaction;
    }
	
	protected TbTransInfo makeTransaction(Connection connection)
			throws Exception {
		
		TbTransInfo transInfo = new TbTransInfo();
		transInfo.setCardNo(apt.get("CARD_NO"));
		transInfo.setExpiryDate(apt.get("EXPIRY_DATE"));
		transInfo.setPCode(Constants.PCODE_7307);
		
		//???????????? #394 ????????????????????????????????????: 00001001
		String acqMemId = (!apt.get("ACQ_MEM_ID").toString().equals("00000000"))
				? apt.get("ACQ_MEM_ID").toString():"00001001";
				
		transInfo.setAcqMemId(acqMemId);
		transInfo.setMerchId(apt.get("MERCH_ID"));
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
				
				for (TbAppointReloadDtlInfo infoDtl : appDtlList){
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
	
	private void addDetails(Connection connection, TbTransInfo transaction) throws Exception
    {
        for (TbTransDtlInfo detail : makeTransactionDetails(connection))
        {
        	if (appBonusQtys.get(detail.getBonusId()) == null){
        		appBonusQtys.put(detail.getBonusId(), detail.getBonusCrQty().doubleValue());
        	}
        	else{
        		appBonusQtys.put(detail.getBonusId(), 
        				Calc.add(appBonusQtys.get(detail.getBonusId()), detail.getBonusCrQty().doubleValue()));
        	}
        	sqlList.add(PreOperationUtil.getBalCrDbSql(connection, transaction, detail));
        	sqlList.add(addDetail(connection, transaction, detail));
        }
    }
	
	protected Collection<TbTransDtlInfo> makeTransactionDetails(Connection connection) throws Exception {
		
		List<TbTransDtlInfo> transDtlInfo = new ArrayList();	
		
		for (TbAppointReloadDtlInfo infoDtl : appDtlList){
			transDtlInfo.add(makeTransactionDetail(connection, infoDtl));
		}
		
		return transDtlInfo;
	}
	
	protected TbTransDtlInfo makeTransactionDetail(Connection connection, TbAppointReloadDtlInfo appointReloadDtlInfo) 
					throws Exception
    {
    	logger.info("call campaign : bonus sdate edate");
    	DateRange range = PreOperationUtil.getBonusSdateEdate(connection, terminalBatch.getTermSettleDate(), apt.get("BALANCE_TYPE"), 
    			apt.get("CARD_NO"), apt.get("EXPIRY_DATE"), appointReloadDtlInfo.getBonusId(), 
    			appointReloadDtlInfo.getBonusSdate(), appointReloadDtlInfo.getBonusEdate());
    	
    	logger.info("range:" + range);
    	TbTransDtlInfo transDtlInfo = new TbTransDtlInfo();
    	transDtlInfo.setTxnCode(Constants.TXNCODE_8817);
    
    	transDtlInfo.setCutDate(terminalBatch.getCutDate());
    	transDtlInfo.setRegionId(apt.get("REGION_ID"));
    	transDtlInfo.setBonusBase(apt.get("BONUS_BASE"));
    	transDtlInfo.setBalanceType(apt.get("BALANCE_TYPE"));
    	transDtlInfo.setBalanceId(apt.get("BALANCE_ID"));
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
	
	private String addDetail(Connection connection, TbTransInfo transaction, TbTransDtlInfo detail) throws SQLException
    {
    	/*???Region id*/
        if(StringUtil.isEmpty(detail.getRegionId()) & !StringUtil.isEmpty(transaction.getCardNo()) & !StringUtil.isEmpty(transaction.getExpiryDate()))
        {
        	detail.setRegionId(cardInfo.getRegionId());
        }
        
    	TransDtlBean bean = new TransDtlBean();
        bean.setTransInfo(transaction);
        bean.setTransDtlInfo(detail);

        return bean.getInsertSql();
    }

	public List<String> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<String> sqlList) {
		this.sqlList = sqlList;
	}
}
