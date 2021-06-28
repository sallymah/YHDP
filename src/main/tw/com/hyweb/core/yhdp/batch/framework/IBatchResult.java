/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */
/*
 * (版權及授權描述)
 *
 * Copyright 2006 (C) Hyweb Technology Co., Ltd. All Rights Reserved.
 *
 * $History: $
 *
 */
package tw.com.hyweb.core.yhdp.batch.framework;

import tw.com.hyweb.core.yhdp.batch.framework.BatchException;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * Define all operations to process Batch Result<br>
 * 
 * @author Sonys
 */
public interface IBatchResult
{
    /**
     * insert TB_BATCH_RESULT<br>
     * 
     * @return tbBatchResultInfo
     * @throws BatchException
     */
    public TbBatchResultInfo openBatchResult() throws BatchException;

    /**
     * update TB_BATCH_RESULT<br>
     * 
     * @param errorDesc
     * @param info
     * @throws tw.com.hyweb.core.spb.batch.framework.BatchException
     */
    public void closeBatchResult(String errorDesc, TbBatchResultInfo info) throws BatchException;
}
