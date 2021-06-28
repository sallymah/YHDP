/*
 * $Id: GenericBatchJob.java 13512 2009-02-04 01:43:13Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.yhdp.batch.framework.generic;

import java.sql.Connection;

import tw.com.hyweb.core.yhdp.batch.util.DateUtils;


/**
 * @author Clare
 * @version $Revision: 13512 $
 */
public class GenericBatchJob implements BatchJob
{
    protected String jobDate = DateUtils.getSystemDate();
    protected String jobTime = DateUtils.getSystemTime();

    /**
     * default implement, do nothing
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchJob#action(java.sql.Connection,
     *      java.lang.String)
     */
    public void action(Connection connection, String batchDate) throws Exception
    {

    }

    /**
     * default implement, do nothing
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchJob#remarkSuccess(java.sql.Connection,
     *      java.lang.String)
     */
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {

    }

    /**
     * default implement, do nothing
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchJob#remarkFailure(java.sql.Connection,
     *      java.lang.String)
     */
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {

    }
}
