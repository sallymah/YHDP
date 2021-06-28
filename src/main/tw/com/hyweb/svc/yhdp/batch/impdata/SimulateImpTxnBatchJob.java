/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/17
 */
package tw.com.hyweb.svc.yhdp.batch.impdata;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;

/**
 * @author Sally
 * 
 */
public class SimulateImpTxnBatchJob extends GenericBatchJob
{
    private static final String[] FIELDS_CONDITION = new String[] {"MERCH_ID", "TERM_ID", "BATCH_NO", "TERM_SETTLE_DATE", "TERM_SETTLE_TIME"};

   
    private final TbTermBatchInfo termBatch;

    private final String batchCondition;
    private final Object[] conditionValues;
    private String impFileName = "";
    private static final String IMP_FN = "SIM_TXN";
    
   
    public SimulateImpTxnBatchJob(TbTermBatchInfo termBatch) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        
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
     * 
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJob#action(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
    	impFileName= IMP_FN + "_" + batchDate;
       Object[] parameterValues = {impFileName,termBatch.getMerchId(),termBatch.getTermId(),termBatch.getBatchNo(),termBatch.getTermSettleDate(),termBatch.getTermSettleTime()};
        executeUpdate(connection, getUpdateTermBatchSQL(),parameterValues);
    }

  
    /**
     * 傳回update TB_TERM_BATCH的SQL
     * 
     * @return
     */
    private String getUpdateTermBatchSQL()
    {
    	return "update TB_TERM_BATCH set IMP_FILE_NAME=? where MERCH_ID=? AND TERM_ID=? AND BATCH_NO=? AND TERM_SETTLE_DATE=? AND TERM_SETTLE_TIME=?";
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
