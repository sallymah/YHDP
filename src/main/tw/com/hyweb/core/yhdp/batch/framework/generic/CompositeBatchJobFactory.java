/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/21
 */
package tw.com.hyweb.core.yhdp.batch.framework.generic;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tw.com.hyweb.core.yhdp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.yhdp.batch.framework.generic.BatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author Clare
 * 
 */
public class CompositeBatchJobFactory implements BatchJobFactory
{
    private final List<BatchJobFactory> factories;
    private final Queue<BatchJobFactory> factoryQueue;

    private BatchJobFactory now;

    public CompositeBatchJobFactory(List<BatchJobFactory> factories)
    {
        this.factories = factories;

        factoryQueue = new LinkedList<BatchJobFactory>(factories);
        now = factoryQueue.poll();
    }

    /**
     * 呼叫所有factory的init進行初始化
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchJobFactory#init(java.sql.Connection,
     *      java.lang.String)
     */
    public void init(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        for (BatchJobFactory factory : factories)
        {
            factory.init(connection, batchDate, tbBatchResultInfo);
        }
    }

    /**
     * 判斷目前的factory是否還有job，沒有的話換下個factory直至都沒job為止
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchJobFactory#hasNext()
     */
    public boolean hasNext() throws Exception
    {
        boolean hasNext = now.hasNext();

        if (!hasNext && factoryQueue.isEmpty())
        {
            return false;
        }
        else if (!hasNext && !factoryQueue.isEmpty())
        {
            now = factoryQueue.poll();

            return hasNext();
        }

        return true;
    }

    /**
     * 傳回目前factory的下個job
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchJobFactory#next(java.sql.Connection,
     *      java.lang.String)
     */
    public BatchJob next(Connection connection, String batchDate) throws Exception
    {
        return now.next(connection, batchDate);
    }

    /**
     * 攔截所有的exception，確保所有的factory都destroy之後丟出最後的exception
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchJobFactory#destroy()
     */
    public void destroy() throws Exception
    {
        Exception exception = null;

        for (BatchJobFactory factory : factories)
        {
            try
            {
                factory.destroy();
            }
            catch (Exception e)
            {
                exception = e;
            }
        }

        if (exception != null)
        {
            throw exception;
        }
    }
}
