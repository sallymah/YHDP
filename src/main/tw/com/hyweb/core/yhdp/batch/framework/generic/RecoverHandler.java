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
public interface RecoverHandler
{
    /**
     * 執行recover作業
     * 
     * @param connection
     * @param batchDate
     * @throws Exception
     */
    public void recover(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception;
}
