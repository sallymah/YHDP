/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/16
 */
package tw.com.hyweb.core.yhdp.batch.framework.generic;

import java.sql.Connection;

/**
 * @author Clare
 * 
 */
public interface BatchJob
{
    /**
     * 處理此batch job
     * 
     * @param connection
     * @param batchDate
     * @throws Exception
     */
    public void action(Connection connection, String batchDate) throws Exception;

    /**
     * 註記此batch job處理成功
     * 
     * @param connection
     * @param batchDate
     * @throws Exception
     */
    public void remarkSuccess(Connection connection, String batchDate) throws Exception;

    /**
     * 註記此batch job處理失敗
     * 
     * @param connection
     * @param batchDate
     * @throws Exception
     */
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception;

    public static final BatchJob NULL_JOB = new BatchJob()
    {
        public void action(Connection connection, String batchDate) throws Exception
        {

        }

        public void remarkSuccess(Connection connection, String batchDate) throws Exception
        {

        }

        public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
        {

        }
    };
}
