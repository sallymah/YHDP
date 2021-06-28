/*
 * $Id: InsertTermBatch.java 2523 2010-01-08 09:26:29Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.batch.preoperation;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.batch.util.beans.TermBatchBean;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;


/**
 * @author Anny
 * @version $Revision: 2523 $
 */
public class InsertTermBatch
{
    private final String txnSrc;
    private final String batchNoType;
    private boolean isDayCut = false;
    
    InsertTermBatch(String txnSrc, String type)
    {
    	this.txnSrc =  txnSrc;
    	this.batchNoType = type;
    }
    
    InsertTermBatch(String txnSrc, String type, boolean isDayCut)
    {
    	this.txnSrc =  txnSrc;
    	this.batchNoType = type;
    	this.isDayCut = isDayCut;
    }

    /**
     * 新增一筆TB_TERM_BATCH
     * 
     * @param connection
     * @throws SQLException
     */
    public TbTermBatchInfo insertTerminalBatch(Connection connection, String merchId, String termId) throws SQLException
    {
    	TbTermBatchInfo terminalBatch = new TbTermBatchInfo();
        terminalBatch.setTxnSrc(txnSrc);
        terminalBatch.setBatchNo(SequenceGenerator.getBatchNoByType(connection,batchNoType));
        terminalBatch.setTermSettleDate(DateUtils.getSystemDate());
        terminalBatch.setTermSettleTime(DateUtils.getSystemTime());
        terminalBatch.setTermId(termId);
        terminalBatch.setMerchId(merchId);
        
        if (isDayCut)
        {
	        terminalBatch.setCutDate(DateUtils.getSystemDate());
	        terminalBatch.setCutTime(DateUtils.getSystemTime());
        }
        
        TermBatchBean bean = new TermBatchBean();
        bean.setTermBatchInfo(terminalBatch);
       
        DBService.getDBService().sqlAction(bean.getInsertSql(), connection, false);
        
        return  bean.getTermBatchInfo();
    }
}
