/*
 * (版權及授權描述)
 * Copyright 2010 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2010/1/29
 */
package tw.com.hyweb.svc.yhdp.batch.summary; 

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleException;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author Ivan
 * 先將有效卡量(STATUS IN ('1','2','3'))及失效卡量(STATUS IN ('9','T'))的資料,insert至TB_STOCK_SUM
 */
public class StockAddSumCapacityHandler implements BatchHandler
{
    /**
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler#handle(java
     *      .sql.Connection, java.lang.String)
     */
    public BatchHandleResult handle(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws BatchHandleException
    {
        try
        {
//        	StringBuffer sqlbufer = new StringBuffer();
//        	sqlbufer.append("insert into tb_stock_sum(proc_date,mem_id,card_owner,effective_cnt,ineffective_cnt)");
//        	sql.append(" select '"+ batchDate +"',mem_id,(case when card_owner is null then '000000000000000' else card_owner end) as card_owner,count(case when status in ('1','2','3') then 1 end) as effective_cnt,count(case when status in ('9','T') then 1 end) as ineffective_cnt");
//        	sql.append(" from tb_card ");
//        	sql.append(" group by mem_id, card_owner");
        	
        	
        	String sql = "insert into tb_stock_sum(proc_date,mem_id,card_owner,effective_cnt,ineffective_cnt) " +
        	"select proc_date,mem_id,card_owner,sum(EFFECTIVE_CNT) as EFFECTIVE_CNT,sum(INEFFECTIVE_CNT) as INEFFECTIVE_CNT from" +
        	"(select '"+batchDate+"' as proc_date,c2.MEM_ID,(case when c2.card_owner is null then '000000000000000' else c2.card_owner end) as card_owner," +
        	"(case when EFFECTIVE_CNT is null then 0 else EFFECTIVE_CNT end) as EFFECTIVE_CNT," +
        	"(case when INEFFECTIVE_CNT is null then 0 else INEFFECTIVE_CNT end) as INEFFECTIVE_CNT from " +
        	"(select '"+batchDate+"',mem_id,(case when card_owner is null then '000000000000000' else card_owner end) as card_owner," +
        	"count(case when status in ('1','2','3') then 1 end) as effective_cnt," +
        	"count(case when status in ('9','T') then 1 end) as ineffective_cnt from tb_card " +
        	"group by mem_id,card_product, card_owner) c1," +
        	"(select mem_id,card_owner from(select DISTINCT delivery_source_id as card_owner," +
        	"(select DISTINCT mem_id from tb_card_product ca where ca.card_product = cd.delivery_card_product) as mem_id " +
        	"from tb_card_delivery cd where delivery_card_product in (select DISTINCT card_product from tb_card) " +
        	"UNION " +
        	"select DISTINCT delivery_dest_id as card_owner,(select DISTINCT mem_id from tb_card_product ca where ca.card_product = cd.delivery_card_product) as mem_id " +
        	"from tb_card_delivery cd where delivery_card_product in " +
        	"(select DISTINCT card_product from tb_card)))c2 where c1.mem_id(+) = c2.mem_id and c1.card_owner(+) = c2.card_owner " +
        	"order by mem_id,card_owner) group by proc_date,mem_id,card_owner";
            	
            executeUpdate(connection, sql.toString());
        }
        catch (Exception e)
        {
            throw new BatchHandleException(e);
        }

        return BatchHandleResult.DEFAULT_SUCCESS_RESULT;
    }
}
