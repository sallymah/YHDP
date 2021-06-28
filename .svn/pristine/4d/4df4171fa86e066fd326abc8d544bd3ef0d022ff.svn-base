/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;

/**
 * @author Clare
 * 
 */
public class CutTermBatchJobFactory extends CursorBatchJobFactory
{
    private final CutTermBatchFilter filter;
    private final CutTransactionAction action;

    public CutTermBatchJobFactory(CutTermBatchFilter filter, CutTransactionAction action)
    {
        this.filter = filter;
        this.action = action;
    }

    /**
     * 結帳成功且未過檔的批之SQL command
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        return "select MERCH_ID,TERM_ID,BATCH_NO,TERM_SETTLE_DATE,TERM_SETTLE_TIME,ROWID from TB_TERM_BATCH where " + filter.getCutCondition(tbBatchResultInfo);
    }

    /**
     * 傳回一個CutTermBatchJob
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
        return new CutTermBatchJob(getTermBatch(result), action);
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
