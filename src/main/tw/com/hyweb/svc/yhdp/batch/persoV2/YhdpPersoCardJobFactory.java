/*
 * 
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/12/1
 */
package tw.com.hyweb.svc.yhdp.batch.persoV2;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author Chris
 *
 */
public class YhdpPersoCardJobFactory implements BatchJobFactory
{
    private final ProcessPersoJobFactory factory;

    /**
     * @param factory 
     */
    public YhdpPersoCardJobFactory(ProcessPersoJobFactory factory)
    {
        this.factory = factory;
    }

    /**
     * @throws Exception
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#destroy()
     */
    public void destroy() throws Exception
    {
        factory.destroy();
    }

    /**
     * @return
     * @throws Exception
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#hasNext()
     */
    public boolean hasNext() throws Exception
    {
        return factory.hasNext();
    }

    /**
     * @param connection
     * @param batchDate
     * @throws Exception
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#init(java.sql.Connection,
     *      java.lang.String)
     */
    public void init(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        factory.init(connection, batchDate, tbBatchResultInfo);
    }

    /**
     * @param connection
     * @param batchDate
     * @return
     * @throws Exception
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#next(java.sql.Connection,
     *      java.lang.String)
     */
    public BatchJob next(Connection connection, String batchDate) throws Exception
    {	
        return new YhdpPersoCardJobDecorator((ProcessPersoJob) factory.next(connection, batchDate));
    }
}
