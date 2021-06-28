package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;

/**
 * <pre>
 * TermBatchBean javabean
 * termBatchInfo:TbTermBatchInfo
 * </pre>
 * author:duncan
 */
public class TermBatchBean implements ITermBatchBean
{
    protected TbTermBatchInfo termBatchInfo = null;

    public TermBatchBean()
    {
    }

    public TbTermBatchInfo getTermBatchInfo()
    {
        return termBatchInfo;
    }

    public void setTermBatchInfo(TbTermBatchInfo termBatchInfo)
    {
        if (termBatchInfo != null) {
            this.termBatchInfo = (TbTermBatchInfo) termBatchInfo.clone();
        }
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(termBatchInfo.getMerchId())) {
            termBatchInfo.setMerchId(Layer2Util.getBatchConfig("UNKNOWN_MERCH"));
        }
        if (StringUtil.isEmpty(termBatchInfo.getTermId())) {
            termBatchInfo.setTermId(Layer2Util.getBatchConfig("UNKNOWN_TERM"));
        }
        if (StringUtil.isEmpty(termBatchInfo.getTermUpDate())) {
            termBatchInfo.setTermUpDate(termBatchInfo.getTermSettleDate());
        }
        if (StringUtil.isEmpty(termBatchInfo.getTermSettleFlag())) {
            // 平帳
            termBatchInfo.setTermSettleFlag(Constants.TERMSETTLEFLAG_BALANCED);
        }
        if (StringUtil.isEmpty(termBatchInfo.getStatus())) {
            // 有效
            termBatchInfo.setStatus(Constants.TBSTATUS_VALID);
        }
        if (StringUtil.isEmpty(termBatchInfo.getParMon())) {
            termBatchInfo.setParMon(termBatchInfo.getTermSettleDate().substring(4, 6));
        }
        if (StringUtil.isEmpty(termBatchInfo.getParDay())) {
            termBatchInfo.setParDay(termBatchInfo.getTermSettleDate().substring(6, 8));
        }
        return termBatchInfo.toInsertSQL();
    }
}
