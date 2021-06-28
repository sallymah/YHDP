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
 * 未開卡,未加值卡量
 */
public class StockUptSumNewCardJobFactory extends CursorBatchJobFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {   	
    	StringBuffer sql = new StringBuffer();   	
    	
//    	sql.append("SELECT MEM_ID,CARD_OWNER,COUNT(CASE WHEN ACTIVE_DATE = '00000000' THEN 1 END) AS INACTIVATE_CNT,");
//    	sql.append("COUNT(CASE WHEN FIRST_RELOAD_DATE = '00000000' THEN 1 END) AS NON_RELOAD_CNT");
//    	sql.append(" FROM TB_CARD");
//    	sql.append(" WHERE (ACTIVE_DATE = '00000000' OR FIRST_RELOAD_DATE = '00000000') AND STATUS IN ('1','2','3')");
//    	sql.append(" GROUP BY MEM_ID,CARD_OWNER");

    	sql.append("SELECT MEM_ID,CARD_OWNER,COUNT(CASE WHEN (ACTIVE_DATE = '00000000' OR ACTIVE_DATE IS NULL) THEN 1 END) AS INACTIVATE_CNT,");
    	sql.append("COUNT(CASE WHEN (FIRST_RELOAD_DATE = '00000000' OR FIRST_RELOAD_DATE IS NULL) THEN 1 END) AS NON_RELOAD_CNT");
    	sql.append(" FROM TB_CARD");
    	sql.append(" WHERE ((ACTIVE_DATE = '00000000' OR ACTIVE_DATE IS NULL) OR (FIRST_RELOAD_DATE = '00000000' OR FIRST_RELOAD_DATE IS NULL)) AND STATUS IN ('1','2','3')");
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
        return new StockUptSumNewCardJob(result);
    }
}
