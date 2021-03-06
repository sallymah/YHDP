/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2010/1/29
 */
package tw.com.hyweb.svc.yhdp.batch.summary; 

import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.util.date.DateUtil;

/**
 * @author Ivan
 * 本月開卡(ACTIVATE_CNT) 及本月新卡首次加值(RELOAD_CNT) 
 */
public class StockUptSumFirstTxnJobFactory extends CursorBatchJobFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	String procDate = DateUtil.addDate(batchDate, -1);
    	
    	StringBuffer sql = new StringBuffer();
    	sql.append("SELECT MEM_ID,CARD_OWNER,COUNT(CASE WHEN ACTIVE_DATE = '"+ procDate +"' THEN 1 END) AS ACTIVATE_CNT,");
    	sql.append("COUNT(CASE WHEN FIRST_RELOAD_DATE = '"+ procDate +"' THEN 1 END) AS RELOAD_CNT");
    	sql.append(" FROM  TB_CARD");
    	sql.append(" WHERE ((ACTIVE_DATE = '"+ procDate +"') OR (FIRST_RELOAD_DATE = '"+ procDate +"')) AND STATUS IN ('1','2','3')");
    	sql.append(" GROUP BY MEM_ID,CARD_OWNER");

    	return sql.toString();

    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#makeBatchJob(java.util.Map)
     */
    @Override
    protected BatchJob makeBatchJob(Map<String, String> result) throws Exception
    {
        return new StockUptSumFirstTxnJob(result);
    }
}
