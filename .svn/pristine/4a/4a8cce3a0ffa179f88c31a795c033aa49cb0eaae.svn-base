package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;

/**
 * <pre>
 * OnlTxnBean javabean
 * onlTxnInfo:TbOnlTxnInfo
 * </pre>
 * author:duncan
 */
public class OnlTxnBean implements IOnlTxnBean
{
    protected TbOnlTxnInfo onlTxnInfo = null;

    public OnlTxnBean()
    {
    }

    public TbOnlTxnInfo getOnlTxnInfo()
    {
        return onlTxnInfo;
    }

    public void setOnlTxnInfo(TbOnlTxnInfo onlTxnInfo)
    {
        if (onlTxnInfo != null) {
            this.onlTxnInfo = (TbOnlTxnInfo) onlTxnInfo.clone();
        }
    }

    public String getInsertSql() {
                
        if (StringUtil.isEmpty(onlTxnInfo.getOnlineFlag())) {
            // 大寫字母O代表非online,offline
            onlTxnInfo.setOnlineFlag(Constants.ONLINEFLAG_OTHERS);
        }
        if (StringUtil.isEmpty(onlTxnInfo.getAcqMemId())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_MEMBER
            onlTxnInfo.setAcqMemId(Layer2Util.getBatchConfig("UNKNOWN_MEMBER"));
        }
        if (StringUtil.isEmpty(onlTxnInfo.getIssMemId())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_MEMBER
            onlTxnInfo.setIssMemId(Layer2Util.getBatchConfig("UNKNOWN_MEMBER"));
        }
        if (StringUtil.isEmpty(onlTxnInfo.getMerchId())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_MERCH
            onlTxnInfo.setMerchId(Layer2Util.getBatchConfig("UNKNOWN_MERCH"));
        }
        if (StringUtil.isEmpty(onlTxnInfo.getTermId())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_TERM
            onlTxnInfo.setTermId(Layer2Util.getBatchConfig("UNKNOWN_TERM"));
        }
        if (StringUtil.isEmpty(onlTxnInfo.getCardNo())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_CARD_NO
            onlTxnInfo.setCardNo(Layer2Util.getBatchConfig("UNKNOWN_CARD_NO"));
        }
        if (StringUtil.isEmpty(onlTxnInfo.getExpiryDate())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_EXPIRY_DATE
            onlTxnInfo.setExpiryDate(Layer2Util.getBatchConfig("UNKNOWN_EXPIRY_DATE"));
        }
        if (StringUtil.isEmpty(onlTxnInfo.getRespCode())) {
            // 沒值就填2個zero
            onlTxnInfo.setRespCode(StringUtil.pendingKey(0, 2));
        }
        if (StringUtil.isEmpty(onlTxnInfo.getStatus())) {
            onlTxnInfo.setStatus(Constants.STATUS_SUCCESS);
        }
        if (StringUtil.isEmpty(onlTxnInfo.getParMon())) {
            onlTxnInfo.setParMon(onlTxnInfo.getTxnDate().substring(4, 6));
        }
        if (StringUtil.isEmpty(onlTxnInfo.getParDay())) {
            onlTxnInfo.setParDay(onlTxnInfo.getTxnDate().substring(6, 8));
        }
        return onlTxnInfo.toInsertSQL();
    }
}
