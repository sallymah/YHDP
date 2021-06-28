/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author Clare
 * 
 */
public interface CutTermBatchFilter
{
    /**
     * 傳回需要過檔的批之SQL條件式
     * 
     * @param txnSrcs
     * @return
     * @throws Exception
     */
    public String getCutCondition(TbBatchResultInfo tbBatchResultInfo) throws Exception;
}
