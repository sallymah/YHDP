/*
 * $Id: SimulateTransJob.java 3144 2010-04-29 03:43:39Z 94068 $
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
import tw.com.hyweb.core.cp.batch.util.beans.TransBean;
import tw.com.hyweb.core.cp.batch.util.beans.TransDtlBean;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Anny
 * @version $Revision: 3144 $
 */
public abstract class SimulateTransJob extends GenericBatchJob
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
    public SimulateTransJob(TbTermBatchInfo terminalBatch)
    {
        this.terminalBatch = terminalBatch;
    }

    /**
     * 執行前置處理任務並新增TB_TRANS及TB_TRANS_DTL
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java
     *      .sql.Connection, java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
        lmsInvoiceNumber = BatchSequenceGenerator.getLmsInvoiceNo(batchDate);

        preoperation(connection, batchDate);

        TbTransInfo transaction = addTransaction(connection);
        addDetails(connection, transaction);
    }

    /**
     * 新增TB_TRANS
     * 
     * @param connection
     * @return
     * @throws Exception
     */
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

        if(StringUtil.isEmpty(transaction.getIssMemId()) & !StringUtil.isEmpty(transaction.getCardNo()) & !StringUtil.isEmpty(transaction.getExpiryDate()))
        {
        	transaction.setIssMemId(genCardMemId(connection, transaction.getCardNo(),transaction.getExpiryDate()));
        }
        
        transaction.setCutDate(terminalBatch.getCutDate());
        transaction.setCutTime(terminalBatch.getCutTime());
        
        TransBean bean = new TransBean();
        bean.setTransInfo(transaction);

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
     * 新增所有的TB_TRANS_DTL
     * 
     * @param connection
     * @param transaction
     * @throws Exception
     */
    private void addDetails(Connection connection, TbTransInfo transaction) throws Exception
    {
        for (TbTransDtlInfo detail : makeTransactionDetails(connection))
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
    protected void updateBalance(Connection connection, TbTransInfo transaction, TbTransDtlInfo detail) throws SQLException
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
    private void addDetail(Connection connection, TbTransInfo transaction, TbTransDtlInfo detail) throws SQLException
    {
    	/*找Region id*/
        if(StringUtil.isEmpty(detail.getRegionId()) & !StringUtil.isEmpty(transaction.getCardNo()) & !StringUtil.isEmpty(transaction.getExpiryDate()))
        {
        	detail.setRegionId(genCardRegionId(connection, transaction.getCardNo(),transaction.getExpiryDate()));
        }
        
    	TransDtlBean bean = new TransDtlBean();
        bean.setTransInfo(transaction);
        bean.setTransDtlInfo(detail);

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
     * 建立TB_TRANS
     * 
     * @param connection
     * @return
     */
    protected abstract TbTransInfo makeTransaction(Connection connection) throws Exception;

    /**
     * 建立TB_TRANS_DTL
     * 
     * @param connection
     * @return
     */
    protected abstract Collection<TbTransDtlInfo> makeTransactionDetails(Connection connection) throws Exception;


}
