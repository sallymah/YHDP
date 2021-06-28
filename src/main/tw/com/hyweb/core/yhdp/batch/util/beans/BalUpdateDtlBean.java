package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbBalUpdateInfo;
import tw.com.hyweb.service.db.info.TbBalUpdateDtlInfo;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;

/**
 * <pre>
 * BalUpdateDtlBean javabean
 * balUpdateInfo:TbBalUpdateInfo
 * balUpdateDtlInfo:TbBalUpdateDtlInfo
 * </pre>
 * author:duncan
 */
public class BalUpdateDtlBean implements IBalUpdateDtlBean
{
    protected TbBalUpdateInfo balUpdateInfo = null;
    protected TbBalUpdateDtlInfo balUpdateDtlInfo = null;

    public BalUpdateDtlBean()
    {
    }

    public TbBalUpdateInfo getBalUpdateInfo()
    {
        return balUpdateInfo;
    }

    public void setBalUpdateInfo(TbBalUpdateInfo balUpdateInfo)
    {
        this.balUpdateInfo = balUpdateInfo;
    }

    public TbBalUpdateDtlInfo getBalUpdateDtlInfo()
    {
        return balUpdateDtlInfo;
    }

    public void setBalUpdateDtlInfo(TbBalUpdateDtlInfo balUpdateDtlInfo)
    {
        if (balUpdateDtlInfo != null) {
            this.balUpdateDtlInfo = (TbBalUpdateDtlInfo) balUpdateDtlInfo.clone();
        }
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(balUpdateDtlInfo.getCardNo())) {
            balUpdateDtlInfo.setCardNo(balUpdateInfo.getCardNo());
        }
        if (StringUtil.isEmpty(balUpdateDtlInfo.getExpiryDate())) {
            balUpdateDtlInfo.setExpiryDate(balUpdateInfo.getExpiryDate());
        }
        if (StringUtil.isEmpty(balUpdateDtlInfo.getLmsInvoiceNo())) {
            balUpdateDtlInfo.setLmsInvoiceNo(balUpdateInfo.getLmsInvoiceNo());
        }
        if (StringUtil.isEmpty(balUpdateDtlInfo.getStatus())) {
            balUpdateDtlInfo.setStatus(Constants.BUDSTATUS_NOT_DOWNLOAD);
        }
        if (StringUtil.isEmpty(balUpdateDtlInfo.getParMon())) {
            balUpdateDtlInfo.setParMon(balUpdateInfo.getTxnDate().substring(4, 6));
        }
        if (StringUtil.isEmpty(balUpdateDtlInfo.getParDay())) {
            balUpdateDtlInfo.setParDay(balUpdateInfo.getTxnDate().substring(6, 8));
        }
        return balUpdateDtlInfo.toInsertSQL();
    }
}
