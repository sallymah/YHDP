/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/6/18
 */
package tw.com.hyweb.core.cp.common.framework.generic;

import java.sql.Connection;
import java.util.List;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author Clare
 * 
 */
public abstract class DAOBatchJobFactory implements BatchJobFactory
{
    private List<?> infos;
    private int index;

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory#init(java.sql.Connection,
     *      java.lang.String)
     */
    public void init(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	index = 0;
        infos = getDAOInfos(connection, batchDate, tbBatchResultInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory#hasNext()
     */
    public boolean hasNext() throws Exception
    {
        return index < infos.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory#next(java.sql.Connection,
     *      java.lang.String)
     */
    public BatchJob next(Connection connection, String batchDate) throws Exception
    {
        return getBatchJob(infos.get(index++));
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory#destroy()
     */
    public void destroy() throws Exception
    {

    }

    protected abstract List<?> getDAOInfos(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception;

    protected abstract BatchJob getBatchJob(Object info) throws Exception;
}
