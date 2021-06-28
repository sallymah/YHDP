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
public class UnbalanceProcessedDecorator implements CutTermBatchFilter
{
    private final CutTermBatchFilter handler;

    public UnbalanceProcessedDecorator(CutTermBatchFilter handler)
    {
        this.handler = handler;
    }

    /**
     * 附加不平帳已處理完之SQL條件
     * 
     * @see tw.com.hyweb.svc.yhdp.batch.daycut.CutTermBatchFilter#getCutCondition()
     */
    public String getCutCondition(TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        return handler.getCutCondition(tbBatchResultInfo) + "and TERM_SETTLE_FLAG='9' and UNBAL_PROC_DATE is not null and UNBAL_PROC_RCODE='0000' and UNBAL_CONFIRM_DATE is not null";
    }
}
