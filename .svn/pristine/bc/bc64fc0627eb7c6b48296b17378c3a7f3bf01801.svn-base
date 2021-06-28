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

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;

/**
 * @author Ivan
 * 
 */
public class StockUptSumNewCardJob extends GenericBatchJob
{
	private final Map<String, String> newCardResult;

    /**
     * @param result
     */
    public StockUptSumNewCardJob(Map<String, String> result)
    {
        this.newCardResult = result;
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
        
    	Object[] values = getParameterValues(batchDate, newCardResult.get("MEM_ID"), newCardResult.get("CARD_OWNER"));
        executeUpdate(connection, getSQL(), values);
    }

    /**
     * @return
     */
    private String getSQL()
    {
    	StringBuffer sql = new StringBuffer();
    	sql.append("Update TB_STOCK_SUM");
    	sql.append(" Set INACTIVATE_CNT ='"+ newCardResult.get("INACTIVATE_CNT") +"'");
    	sql.append(" ,NON_RELOAD_CNT ='"+ newCardResult.get("NON_RELOAD_CNT") +"'");
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
