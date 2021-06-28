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
public class StockUptSumDeliveryJob extends GenericBatchJob
{
    private final Map<String, String> deliveryResult;

    /**
     * @param result
     */
    public StockUptSumDeliveryJob(Map<String, String> result)
    {
        this.deliveryResult = result;
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
        
    	
    	Object[] values = getParameterValues(batchDate, deliveryResult.get("CARD_OWNER"));
        
        executeUpdate(connection, getSQL(), values);
    }

    /**
     * @return
     */
    private String getSQL()
    {
    	StringBuffer sql = new StringBuffer();
    	sql.append("Update TB_STOCK_SUM");
    	sql.append(" Set DELIVERY_SOURCE ='"+ deliveryResult.get("DELIVERY_SOURCE") +"'");
    	sql.append(" ,DELIVERY_DEST ='"+ deliveryResult.get("DELIVERY_DEST") +"'");
    	sql.append(" Where PROC_DATE= ?");
    	sql.append(" And CARD_OWNER = ?");
    	
        return sql.toString();
    }

    /**
     * @param batchDate
     * @param cardOwner
     * @return
     */
    private Object[] getParameterValues(String batchDate, String cardOwner)
    {
   
        Object[] values = new Object[2];
        values[0] = batchDate;
        values[1] = cardOwner;

        return values;
    }
    
}
