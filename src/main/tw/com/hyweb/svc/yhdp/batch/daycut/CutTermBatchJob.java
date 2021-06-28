/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;

/**
 * @author Clare
 * 
 */
public class CutTermBatchJob extends GenericBatchJob
{
    private static final String[] FIELDS_CONDITION = new String[] { "MERCH_ID", "TERM_ID", "BATCH_NO", "TERM_SETTLE_DATE", "TERM_SETTLE_TIME" };

    private final CutTransactionAction action;
    private final TbTermBatchInfo termBatch;

    private final String batchCondition;
    private final Object[] conditionValues;

    public CutTermBatchJob(TbTermBatchInfo termBatch, CutTransactionAction action) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        this.action = action;
        this.termBatch = termBatch;

        batchCondition = ConditionCreator.createEqualCondition(termBatch, FIELDS_CONDITION);

        conditionValues = new Object[FIELDS_CONDITION.length];
        conditionValues[0] = termBatch.getMerchId();
        conditionValues[1] = termBatch.getTermId();
        conditionValues[2] = termBatch.getBatchNo();
        conditionValues[3] = termBatch.getTermSettleDate();
        conditionValues[4] = termBatch.getTermSettleTime();
    }

    /**
     * 一次過檔一個批的所有交易
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJob#action(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
        action.cutTransaction(connection, FIELDS_CONDITION, conditionValues, batchDate, jobTime);
    }

    /**
     * 對已過檔的批及此批的交易註記cut date及cut time
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJob#remarkSuccess(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
        action.remarkSuccess(connection, FIELDS_CONDITION, conditionValues, batchDate, jobTime);

        Object[] parameterValues = { batchDate, jobTime, Constants.RCODE_0000_OK, termBatch.getRowid() };
        executeUpdate(connection, getUpdateTermBatchSQL(), parameterValues);
    }

    /**
     * 對過檔失敗的批及這批的所有交易註記rcode
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJob#remarkFailure(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {
        action.remarkFailure(connection, FIELDS_CONDITION, conditionValues, batchDate, jobTime);

        Object[] parameterValues = { batchDate, jobTime, Constants.RCODE_2100_DayCut_ERR, termBatch.getRowid() };
        executeUpdate(connection, getUpdateTermBatchSQL(), parameterValues);
    }

    /**
     * 傳回update TB_TERM_BATCH的SQL
     * 
     * @return
     */
    private String getUpdateTermBatchSQL()
    {
        return "update TB_TERM_BATCH set CUT_DATE=?,CUT_TIME=?,CUT_RCODE=? where ROWID=?";
    }

    /**
     * @return the batchCondition
     */
    public String getBatchCondition()
    {
        return batchCondition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "cut term batch:" + batchCondition;
    }
}
