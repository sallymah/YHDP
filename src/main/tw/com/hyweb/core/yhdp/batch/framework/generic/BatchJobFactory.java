/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/16
 */
package tw.com.hyweb.core.yhdp.batch.framework.generic;

import java.sql.Connection;

import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author Clare
 * 
 */
public interface BatchJobFactory
{
    /**
     * 回傳batch job前的initial動作
     * 
     * @param connection
     * @param batchDate
     * @throws Exception
     */
    public void init(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception;

    /**
     * 是否還有batch job
     * 
     * @return
     * @throws Exception
     */
    public boolean hasNext() throws Exception;

    /**
     * 傳回下一個batch job
     * 
     * @return
     * @throws Exception
     */
    public BatchJob next(Connection connection, String batchDate) throws Exception;

    /**
     * 當產完所有batch job之後，最後的後續動作
     */
    public void destroy() throws Exception;
}
