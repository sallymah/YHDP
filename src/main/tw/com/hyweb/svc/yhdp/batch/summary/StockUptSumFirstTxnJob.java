/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2010/1/29
 */
package tw.com.hyweb.svc.yhdp.batch.summary; 

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.sql.Connection;
import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;

/**
 * @author Ivan
 * 
 */
public class StockUptSumFirstTxnJob extends GenericBatchJob
{
    private final Map<String, String> firstTxnResult;

    /**
     * @param result
     */
    public StockUptSumFirstTxnJob(Map<String, String> result)
    {
        this.firstTxnResult = result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java
     * .sql.Connection, java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
        
    	
    	Object[] values = getParameterValues(batchDate, firstTxnResult.get("MEM_ID"), firstTxnResult.get("CARD_OWNER"));
        
        executeUpdate(connection, getSQL(), values);
    }

    /**
     * @return
     */
    private String getSQL()
    {
    	StringBuffer sql = new StringBuffer();
    	sql.append("Update TB_STOCK_SUM");
    	sql.append(" Set ACTIVATE_CNT ='"+ firstTxnResult.get("ACTIVATE_CNT") +"'");
    	sql.append(" ,RELOAD_CNT ='"+ firstTxnResult.get("RELOAD_CNT") +"'");
    	sql.append(" Where PROC_DATE= ?");
    	sql.append(" And MEM_ID = ?");
    	sql.append(" And CARD_OWNER = ?");
    	
        return sql.toString();
    }

    /**
     * @param batchDate
     * @param memId
     * @param cardOwner
     * @return
     */
    private Object[] getParameterValues(String batchDate, String memId,String cardOwner)
    {
   
        Object[] values = new Object[3];
        values[0] = batchDate;
        values[1] = memId;
        values[2] = cardOwner;

        return values;
    }
    
}
