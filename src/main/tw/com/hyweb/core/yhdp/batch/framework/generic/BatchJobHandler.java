/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/16
 */


package tw.com.hyweb.core.yhdp.batch.framework.generic;

import java.sql.Connection;
import java.sql.Savepoint;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;


/**
 * @author Clare
 * 
 */
public class BatchJobHandler implements BatchHandler
{
    private static final Logger LOGGER = Logger.getLogger(BatchJobHandler.class);

    private final BatchJobFactory factory;

    private int commitPerJob = 1;
    private int sleepTimePerCommit = 0;

    public BatchJobHandler(BatchJobFactory factory)
    {
        this.factory = factory;
    }

    /**
     * 每次處理一個batch unit，處理完會呼叫remark success，失敗則執行rollback並呼叫remark failure
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchHandler#handle(java.sql.Connection,
     *      java.lang.String)
     */
    public BatchHandleResult handle(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        String rcode = Layer1Constants.RCODE_0000_OK;

        int successCount = 0;
        int failureCount = 0;

        try
        {
            factory.init(connection, batchDate, tbBatchResultInfo);
           
            while (factory.hasNext())
            {
                BatchJob job = factory.next(connection, batchDate);

                Savepoint savepoint = connection.setSavepoint();

                try
                {
                    LOGGER.debug("handle job:" + job);

                    job.action(connection, batchDate);
                    job.remarkSuccess(connection, batchDate);

                    ++successCount;
                }
                catch (BatchJobException e)
                {
                    LOGGER.warn("exception when handle batch job: " + job, e);
                    connection.rollback(savepoint);

                    job.remarkFailure(connection, batchDate, e);
                    rcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;

                    ++failureCount;
                }
                catch (Exception e)
                {
                    LOGGER.warn("exception when handle batch job: " + job, e);
                    connection.rollback(savepoint);

                    job.remarkFailure(connection, batchDate, new BatchJobException(e, Layer1Constants.RCODE_2001_SOMEDATAERROR));
                    rcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;

                    ++failureCount;
                }

                if (commitPerJob > 0 && successCount % commitPerJob == 0)
                {
                    connection.commit();

                    if (sleepTimePerCommit > 0)
                    {
                        Thread.sleep(sleepTimePerCommit);
                    }
                }
            }

            connection.commit();
        }
        finally
        {
            try
            {
                factory.destroy();
            }
            catch (Exception e)
            {
                LOGGER.warn("exception when destroy factory", e);
            }
        }

        return new BatchHandleResult("success:" + successCount + ", failure:" + failureCount, rcode);
    }

    /**
     * 設定幾個job要commit一次，小於等於0則全部job做完才會commit
     * 
     * @param commitPerJob
     *            the commitPerJob to set
     */
    public void setCommitPerJob(int commitPerJob)
    {
        this.commitPerJob = commitPerJob;
    }

    /**
     * 設定每次commit之後handler要sleep多少ms，小於等於0則不sleep
     * 
     * @param sleepTimePerCommit
     *            the sleepTimePerCommit to set
     */
    public void setSleepTimePerCommit(int sleepTimePerCommit)
    {
        this.sleepTimePerCommit = sleepTimePerCommit;
    }
}
