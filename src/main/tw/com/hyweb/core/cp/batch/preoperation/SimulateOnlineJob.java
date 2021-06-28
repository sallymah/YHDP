/*
 * $Id: SimulateOnlineJob.java 2520 2010-01-08 06:05:14Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.batch.preoperation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.BatchSequenceGenerator;
import tw.com.hyweb.core.cp.batch.util.beans.OnlTxnBean;
import tw.com.hyweb.core.cp.batch.util.beans.OnlTxnDtlBean;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Anny
 * @version $Revision: 2520 $
 */
public abstract class SimulateOnlineJob extends GenericBatchJob
{
    protected final TbTermBatchInfo terminalBatch;

    protected String lmsInvoiceNumber;
    
    private boolean balFlag = true;
    
	public boolean isBalFlag() {
		return balFlag;
	}

	public void setBalFlag(boolean balFlag) {
		this.balFlag = balFlag;
	}
	
    /**
     * @param terminalBatch
     */
    public SimulateOnlineJob(TbTermBatchInfo terminalBatch)
    {
        this.terminalBatch = terminalBatch;
    }

    /**
     * 執行前置處理任務並新增TB_ONL_TXN及TB_ONL_TXN_DTL
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java
     *      .sql.Connection, java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
        lmsInvoiceNumber = BatchSequenceGenerator.getLmsInvoiceNo(batchDate);

        preoperation(connection, batchDate);

        TbOnlTxnInfo transaction = addTransaction(connection);
        addDetails(connection, transaction);
    }

    /**
     * 新增TB_ONL_TXN
     * 
     * @param connection
     * @return
     * @throws Exception
     */
    private TbOnlTxnInfo addTransaction(Connection connection) throws Exception
    {
        TbOnlTxnInfo transaction = makeTransaction(connection);
        transaction.setTxnSrc(terminalBatch.getTxnSrc());
        transaction.setBatchNo(terminalBatch.getBatchNo());
        transaction.setTermSettleDate(terminalBatch.getTermSettleDate());
        transaction.setTermSettleTime(terminalBatch.getTermSettleTime());
        transaction.setLmsInvoiceNo(lmsInvoiceNumber);
        
        if (StringUtil.isEmpty(transaction.getTxnDate()))
        	transaction.setTxnDate(terminalBatch.getTermSettleDate());
        
        if (StringUtil.isEmpty(transaction.getTxnTime()))
        	transaction.setTxnTime(terminalBatch.getTermSettleTime());

        if(StringUtil.isEmpty(transaction.getIssMemId()) & !StringUtil.isEmpty(transaction.getCardNo()) & !StringUtil.isEmpty(transaction.getExpiryDate()))
        {
        	transaction.setIssMemId(genCardMemId(connection, transaction.getCardNo(),transaction.getExpiryDate()));
        }
        
        OnlTxnBean bean = new OnlTxnBean();
        bean.setOnlTxnInfo(transaction);

        DBService.getDBService().sqlAction(bean.getInsertSql(), connection, false);

        return transaction;
    }

    private String genCardMemId(Connection connection, String cardNo, String expiryDate) throws SQLException
    {
        TbCardPK pk = new TbCardPK();
        pk.setCardNo(cardNo);
        pk.setExpiryDate(expiryDate);
        
        return new TbCardMgr(connection).querySingle(pk).getMemId();
    }
    
    private String genCardRegionId(Connection connection, String cardNo, String expiryDate) throws SQLException
    {
        TbCardPK pk = new TbCardPK();
        pk.setCardNo(cardNo);
        pk.setExpiryDate(expiryDate);
        
        return new TbCardMgr(connection).querySingle(pk).getRegionId();
    }
    
    /**
     * 新增所有的TB_ONL_TXN_DTL
     * 
     * @param connection
     * @param transaction
     * @throws Exception
     */
    private void addDetails(Connection connection, TbOnlTxnInfo transaction) throws Exception
    {
        for (TbOnlTxnDtlInfo detail : makeTransactionDetails(connection))
        {
        	if (balFlag)
        		updateBalance(connection, transaction, detail);
        	
            addDetail(connection, transaction, detail);
        }
    }

    /**
     * 更新餘額
     * 
     * @param connection
     * @param transaction
     * @param detail
     * @throws SQLException
     */
    protected void updateBalance(Connection connection, TbOnlTxnInfo transaction, TbOnlTxnDtlInfo detail) throws SQLException
    {    	
    	DBService.getDBService().sqlAction(PreOperationUtil.getBalCrDbSql(connection, transaction, detail), connection);
    }
    
    /**
     * 
     * 
     * @param connection
     * @param transaction
     * @param detail
     * @throws SQLException
     */
    private void addDetail(Connection connection, TbOnlTxnInfo transaction, TbOnlTxnDtlInfo detail) throws SQLException
    {
    	/*找Region id*/
        if(StringUtil.isEmpty(detail.getRegionId()) & !StringUtil.isEmpty(transaction.getCardNo()) & !StringUtil.isEmpty(transaction.getExpiryDate()))
        {
        	detail.setRegionId(genCardRegionId(connection, transaction.getCardNo(),transaction.getExpiryDate()));
        }
        
    	OnlTxnDtlBean bean = new OnlTxnDtlBean();
        bean.setOnlTxnInfo(transaction);
        bean.setOnlTxnDtlInfo(detail);

        DBService.getDBService().sqlAction(bean.getInsertSql(), connection, false);
    }
    

    /**
     * 前置處理任務
     * 
     * @param connection
     * @param batchDate
     */
    protected abstract void preoperation(Connection connection, String batchDate) throws Exception;

    /**
     * 建立TB_ONL_TXN
     * 
     * @param connection
     * @return
     */
    protected abstract TbOnlTxnInfo makeTransaction(Connection connection) throws Exception;

    /**
     * 建立TB_ONL_TXN_DTL
     * 
     * @param connection
     * @return
     */
    protected abstract Collection<TbOnlTxnDtlInfo> makeTransactionDetails(Connection connection) throws Exception;

}
