/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.sql.Connection;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.service.db.mgr.TbCheckTxnMgr;

/**
 * @author Clare
 * 
 */
public class ErrorHandledTermBatchJobFilter extends CutTermBatchJobFactory
{
    private static final Logger LOG = Logger.getLogger(ErrorHandledTermBatchJobFilter.class);

    public ErrorHandledTermBatchJobFilter(CutTermBatchFilter filter, CutTransactionAction action)
    {
        super(filter, action);
    }

    /**
     * 將尚未錯誤處理完的批過濾掉不處理
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#next(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public BatchJob next(Connection connection, String batchDate) throws Exception
    {
        CutTermBatchJob job = (CutTermBatchJob) super.next(connection, batchDate);

        if (new TbCheckTxnMgr(connection).getCount(job.getBatchCondition() + " and UNBAL_PROC_STATUS in ('0','1')") > 0)
        {
            LOG.info("this termBatch not be handled, no cut!");
            LOG.info("condition of termBatch:" + job.getBatchCondition());

            return BatchJob.NULL_JOB;
        }
        else
        {
            return job;
        }
    }
}
