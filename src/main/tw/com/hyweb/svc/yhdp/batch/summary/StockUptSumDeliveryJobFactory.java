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
 * 
 */
public class StockUptSumDeliveryJobFactory extends CursorBatchJobFactory
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
    	
//    	StringBuffer sql = new StringBuffer();
//    	sql.append("SELECT CARD_OWNER,SUM(DELIVERY_SOURCE) AS DELIVERY_SOURCE,SUM(DELIVERY_DEST) AS DELIVERY_DEST");
//    	sql.append(" FROM (");
//    	sql.append(" SELECT DELIVERY_SOURCE_ID AS CARD_OWNER, COUNT(DELIVERY_SOURCE_ID) AS DELIVERY_SOURCE,0 AS DELIVERY_DEST FROM TB_CARD_DELIVERY WHERE DELIVERY_DATE = '"+ procDate +"'");
//    	sql.append(" GROUP BY DELIVERY_SOURCE_ID");
//    	sql.append(" UNION");
//    	sql.append(" SELECT DELIVERY_DEST_ID AS CARD_OWNER ,0 AS DELIVERY_SOURCE, COUNT(DELIVERY_DEST_ID) AS DELIVERY_DEST FROM TB_CARD_DELIVERY WHERE DELIVERY_DATE = '"+ procDate +"'");
//    	sql.append(" GROUP BY DELIVERY_DEST_ID)");
//    	sql.append(" GROUP BY CARD_OWNER");
    	
    	String sql = "SELECT CARD_OWNER,SUM(DELIVERY_SOURCE) AS DELIVERY_SOURCE,SUM(DELIVERY_DEST) AS DELIVERY_DEST FROM " +
    	"(select DELIVERY_SOURCE_ID AS CARD_OWNER,sum((select DELIVERY_QTY from tb_card_delivery_dtl c2 where c1.DELIVERY_BATCH_NO = c2.DELIVERY_BATCH_NO)) as DELIVERY_SOURCE," +
    	"0 AS DELIVERY_DEST from tb_card_delivery c1 where DELIVERY_DATE ='"+procDate+"' group by DELIVERY_SOURCE_ID " +
    	"UNION select DELIVERY_DEST_ID AS CARD_OWNER, 0 AS DELIVERY_SOURCE,sum((select DELIVERY_QTY from tb_card_delivery_dtl c2 where c1.DELIVERY_BATCH_NO = c2.DELIVERY_BATCH_NO)) as DELIVERY_DEST " +
    	"from tb_card_delivery c1 where DELIVERY_DATE ='"+procDate+"' group by DELIVERY_DEST_ID) GROUP BY CARD_OWNER ORDER BY CARD_OWNER";
    	
    	return sql.toUpperCase();

    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#makeBatchJob(java.util.Map)
     */
    @Override
    protected BatchJob makeBatchJob(Map<String, String> result) throws Exception
    {
        return new StockUptSumDeliveryJob(result);
    }
}
