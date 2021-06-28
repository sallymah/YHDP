package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * OnlTxnDtlBean javabean
 * onlTxnInfo:TbOnlTxnInfo
 * onlTxnDtlInfo:TbOnlTxnDtlInfo
 * </pre>
 * author:duncan
 */
public class OnlTxnDtlBean implements IOnlTxnDtlBean
{
    protected TbOnlTxnInfo onlTxnInfo = null;
    protected TbOnlTxnDtlInfo onlTxnDtlInfo = null;

    public OnlTxnDtlBean()
    {
    }

    public TbOnlTxnInfo getOnlTxnInfo()
    {
        return onlTxnInfo;
    }

    public void setOnlTxnInfo(TbOnlTxnInfo onlTxnInfo)
    {
        this.onlTxnInfo = onlTxnInfo;
    }

    public TbOnlTxnDtlInfo getOnlTxnDtlInfo()
    {
        return onlTxnDtlInfo;
    }

    public void setOnlTxnDtlInfo(TbOnlTxnDtlInfo onlTxnDtlInfo)
    {
        if (onlTxnDtlInfo != null) {
            this.onlTxnDtlInfo = (TbOnlTxnDtlInfo) onlTxnDtlInfo.clone();
        }
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(onlTxnDtlInfo.getCardNo())) {
            if (StringUtil.isEmpty(onlTxnInfo.getCardNo())) {
                // 沒值就參考tb_sys_config裡的UNKNOWN_CARD_NO
                onlTxnDtlInfo.setCardNo(Layer2Util.getBatchConfig("UNKNOWN_CARD_NO"));
            }
            else {
                // 用onlTxnInfo
                onlTxnDtlInfo.setCardNo(onlTxnInfo.getCardNo());
            }
        }
        if (StringUtil.isEmpty(onlTxnDtlInfo.getExpiryDate())) {
            if (StringUtil.isEmpty(onlTxnInfo.getExpiryDate())) {
                // 沒值就參考tb_sys_config裡的UNKNOWN_EXPIRY_DATE
                onlTxnDtlInfo.setExpiryDate(Layer2Util.getBatchConfig("UNKNOWN_EXPIRY_DATE"));
            }
            else {
                // 用onlTxnInfo
                onlTxnDtlInfo.setExpiryDate(onlTxnInfo.getExpiryDate());
            }
        }
        if (StringUtil.isEmpty(onlTxnDtlInfo.getLmsInvoiceNo())) {
            // 用onlTxnInfo
            onlTxnDtlInfo.setLmsInvoiceNo(onlTxnInfo.getLmsInvoiceNo());
        }
        if (StringUtil.isEmpty(onlTxnDtlInfo.getPCode())) {
            // 用onlTxnInfo
            onlTxnDtlInfo.setPCode(onlTxnInfo.getPCode());
        }
        if (StringUtil.isEmpty(onlTxnDtlInfo.getParMon())) {
            onlTxnDtlInfo.setParMon(onlTxnInfo.getTxnDate().substring(4, 6));
        }
        if (StringUtil.isEmpty(onlTxnDtlInfo.getParDay())) {
            onlTxnDtlInfo.setParDay(onlTxnInfo.getTxnDate().substring(6, 8));
        }
        return onlTxnDtlInfo.toInsertSQL();
    }
}
