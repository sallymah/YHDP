/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/17
 */
package tw.com.hyweb.svc.yhdp.batch.impdata;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.string.StringUtil;

/**
 * 已結帳未有匯入檔案(For YHDP)
 * @author Sally
 * 
 */
public class SimulateImpTxnFactory extends CursorBatchJobFactory
{
	private static Logger log = Logger.getLogger(SimulateImpTxnFactory.class);
   // private final SimulateImpTxnBatchFilter filter;
   

  /*  public SimulateImpTxnFactory(SimulateImpTxnBatchFilter filter)
    {
        this.filter = filter;
       
    }*/

    /**
     * 已結帳未有匯入檔案SQL command
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	
    	StringBuffer sqlCmd = new StringBuffer();
    	sqlCmd.append("SELECT MERCH_ID, TERM_ID, BATCH_NO, TERM_SETTLE_DATE, TERM_SETTLE_TIME  FROM TB_TERM_BATCH");
    	sqlCmd.append(" WHERE (TERM_SETTLE_DATE<>'00000000' OR TERM_SETTLE_DATE IS NOT NULL)");
    	sqlCmd.append(" AND (CUT_DATE ='00000000' OR CUT_DATE IS  NULL)");
    	sqlCmd.append(" AND IMP_FILE_NAME IS NULL ");
    	sqlCmd.append(" AND EXISTS (SELECT * FROM TB_MERCH WHERE MEM_ID in (SELECT MEM_ID FROM TB_MEMBER WHERE TXN_LOG_FLAG='0'");
    	if(tbBatchResultInfo != null){
    		if(Layer1Constants.MEM_LAST.equalsIgnoreCase(tbBatchResultInfo.getMemId())){
	    		sqlCmd.append(" AND JOB_ID IS NULL");
	    		sqlCmd.append(" AND JOB_TIME IS NULL");
    		}else{
	    		if(!StringUtil.isEmpty(tbBatchResultInfo.getJobId()) 
						&& !tbBatchResultInfo.getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
					sqlCmd.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobId()));
				       if(!StringUtil.isEmpty(tbBatchResultInfo.getJobTime()) 
				    		   && !tbBatchResultInfo.getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
				    	   sqlCmd.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobTime()));
				       }
				}
				if(!StringUtil.isEmpty(tbBatchResultInfo.getMemId())){
					sqlCmd.append(" AND MEM_ID=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getMemId()));
				}	
    		}
    	}
    	else{
    		log.warn("tbBatchResultInfo is null.");
    	}
    	sqlCmd.append(")");
    	sqlCmd.append(" AND TB_MERCH.MERCH_ID = TB_TERM_BATCH.MERCH_ID)");
    	
    		return sqlCmd.toString();
    }

    /**
     * 傳回一個BatchJob
     * 
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#makeBatchJob(java.util.Map)
     */
    @Override
    protected BatchJob makeBatchJob(Map<String, String> result) throws SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        return new SimulateImpTxnBatchJob(getTermBatch(result));
    }

    /**
     * 將result set的值組合一個term batch info
     * 
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private TbTermBatchInfo getTermBatch(Map<String, String> result) throws SQLException
    {
        TbTermBatchInfo termBatch = new TbTermBatchInfo();
        termBatch.setMerchId(result.get("MERCH_ID"));
        termBatch.setTermId(result.get("TERM_ID"));
        termBatch.setBatchNo(result.get("BATCH_NO"));
        termBatch.setTermSettleDate(result.get("TERM_SETTLE_DATE"));
        termBatch.setTermSettleTime(result.get("TERM_SETTLE_TIME"));
        termBatch.setRowid(result.get("ROWID"));

        return termBatch;
    }
}
