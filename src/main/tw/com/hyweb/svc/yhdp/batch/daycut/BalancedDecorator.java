/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author Clare
 * 
 */
public class BalancedDecorator implements CutTermBatchFilter
{
    private final CutTermBatchFilter filter;
    private final String[] termSettleFlag;

    public BalancedDecorator(CutTermBatchFilter handler, String[] termSettleFlag)
    {
        this.filter = handler;
        this.termSettleFlag = termSettleFlag;
    }

    /**
     * 附加需要平帳之條件
     * 20140425	PM決定，因頂新專案不平帳，並不會上傳到TB_UPLOAD_TXN，
     * 故過檔改為全由TB_ONL_TXN直接過檔，不經由TB_CHECK_TXN。
     * @see tw.com.hyweb.svc.yhdp.batch.daycut.CutTermBatchFilter#getCutCondition(java.lang.String[])
     */
    public String getCutCondition(TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        return filter.getCutCondition(tbBatchResultInfo) + " and TERM_SETTLE_FLAG IN " + getTermSettleFlagCondition(termSettleFlag);
    }
    
    private String getTermSettleFlagCondition(String[] termSettleFlag)
    {
        StringBuilder condition = new StringBuilder();

        condition.append('(');

        for (int i = 0; i < termSettleFlag.length; ++i)
        {
            condition.append("'"+termSettleFlag[i]+"'");

            if (i != termSettleFlag.length - 1)
            {
                condition.append(",");
            }
        }

        condition.append(')');

        return condition.toString();
    }
}
