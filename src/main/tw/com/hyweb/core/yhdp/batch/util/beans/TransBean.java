package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;

/**
 * <pre>
 * TransBean javabean
 * transInfo:TbTransInfo
 * </pre>
 * author:duncan
 */
public class TransBean implements ITransBean
{
    protected TbTransInfo transInfo = null;

    public TransBean()
    {
    }

    public TbTransInfo getTransInfo()
    {
        return transInfo;
    }

    public void setTransInfo(TbTransInfo transInfo)
    {
        if (transInfo != null) {
            this.transInfo = (TbTransInfo) transInfo.clone();
        }
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(transInfo.getOnlineFlag())) {
            // 大寫字母O代表非online,offline
            transInfo.setOnlineFlag(Constants.ONLINEFLAG_OTHERS);
        }
        if (StringUtil.isEmpty(transInfo.getAcqMemId())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_MEMBER
            transInfo.setAcqMemId(Layer2Util.getBatchConfig("UNKNOWN_MEMBER"));
        }
        if (StringUtil.isEmpty(transInfo.getIssMemId())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_MEMBER
            transInfo.setIssMemId(Layer2Util.getBatchConfig("UNKNOWN_MEMBER"));
        }
        if (StringUtil.isEmpty(transInfo.getMerchId())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_MERCH
            transInfo.setMerchId(Layer2Util.getBatchConfig("UNKNOWN_MERCH"));
        }
        if (StringUtil.isEmpty(transInfo.getTermId())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_TERM
            transInfo.setTermId(Layer2Util.getBatchConfig("UNKNOWN_TERM"));
        }
        if (StringUtil.isEmpty(transInfo.getCardNo())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_CARD_NO
            transInfo.setCardNo(Layer2Util.getBatchConfig("UNKNOWN_CARD_NO"));
        }
        if (StringUtil.isEmpty(transInfo.getExpiryDate())) {
            // 沒值就參考tb_sys_config裡的UNKNOWN_EXPIRY_DATE
            transInfo.setExpiryDate(Layer2Util.getBatchConfig("UNKNOWN_EXPIRY_DATE"));
        }
        if (StringUtil.isEmpty(transInfo.getRespCode())) {
            // 沒值就填2個zero
            transInfo.setRespCode(StringUtil.pendingKey(0, 2));
        }
        if (StringUtil.isEmpty(transInfo.getStatus())) {
            transInfo.setStatus(Constants.STATUS_SUCCESS);
        }
        if (StringUtil.isEmpty(transInfo.getParMon())) {
            transInfo.setParMon(transInfo.getCutDate().substring(4, 6));
        }
        if (StringUtil.isEmpty(transInfo.getParDay())) {
            transInfo.setParDay(transInfo.getCutDate().substring(6, 8));
        }
        return transInfo.toInsertSQL();
    }
}
