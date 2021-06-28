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
public interface BatchHandler
{
    /**
     * 處理batch作業
     * 
     * 
     * @param connection
     * @param batchDate
     * @param tbBatchResultInfo 
     * @return 處理結果
     * @throws Exception
     */
    public BatchHandleResult handle(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception;
}
