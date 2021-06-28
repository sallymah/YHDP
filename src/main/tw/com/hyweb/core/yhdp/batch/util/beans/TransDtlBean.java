package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * TransDtlBean javabean
 * transInfo:TbTransInfo
 * transDtlInfo:TbTransDtlInfo
 * </pre>
 * author:duncan
 */
public class TransDtlBean implements ITransDtlBean
{
    protected TbTransInfo transInfo = null;
    protected TbTransDtlInfo transDtlInfo = null;

    public TransDtlBean()
    {
    }

    public TbTransDtlInfo getTransDtlInfo()
    {
        return transDtlInfo;
    }

    public void setTransDtlInfo(TbTransDtlInfo transDtlInfo)
    {
        if (transDtlInfo != null) {
            this.transDtlInfo = (TbTransDtlInfo) transDtlInfo.clone();
        }
    }

    public TbTransInfo getTransInfo()
    {
        return transInfo;
    }

    public void setTransInfo(TbTransInfo transInfo)
    {
        this.transInfo = transInfo;
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(transDtlInfo.getCardNo())) {
            if (StringUtil.isEmpty(transInfo.getCardNo())) {
                // 沒值就參考tb_sys_config裡的UNKNOWN_CARD_NO
                transDtlInfo.setCardNo(Layer2Util.getBatchConfig("UNKNOWN_CARD_NO"));
            }
            else {
                // 用transInfo
                transDtlInfo.setCardNo(transInfo.getCardNo());
            }
        }
        if (StringUtil.isEmpty(transDtlInfo.getExpiryDate())) {
            if (StringUtil.isEmpty(transInfo.getExpiryDate())) {
                // 沒值就參考tb_sys_config裡的UNKNOWN_EXPIRY_DATE
                transDtlInfo.setExpiryDate(Layer2Util.getBatchConfig("UNKNOWN_EXPIRY_DATE"));
            }
            else {
                // 用transInfo
                transDtlInfo.setExpiryDate(transInfo.getExpiryDate());
            }
        }
        if (StringUtil.isEmpty(transDtlInfo.getLmsInvoiceNo())) {
            // 用transInfo
            transDtlInfo.setLmsInvoiceNo(transInfo.getLmsInvoiceNo());
        }
        if (StringUtil.isEmpty(transDtlInfo.getPCode())) {
            // 用transInfo
            transDtlInfo.setPCode(transInfo.getPCode());
        }
        if (StringUtil.isEmpty(transDtlInfo.getParMon())) {
            transDtlInfo.setParMon(transInfo.getCutDate().substring(4, 6));
        }
        if (StringUtil.isEmpty(transDtlInfo.getParDay())) {
            transDtlInfo.setParDay(transInfo.getCutDate().substring(6, 8));
        }

        return transDtlInfo.toInsertSQL();
    }
}
