package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbBalTransferInfo;
import tw.com.hyweb.service.db.info.TbBalTransferDtlInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * BalTransferDtlBean javabean
 * balTransferInfo:TbBalTransferInfo
 * balTransferDtlInfo:TbBalTransferDtlInfo
 * </pre>
 * author:duncan
 */
public class BalTransferDtlBean implements IBalTransferDtlBean
{
    protected TbBalTransferInfo balTransferInfo = null;
    protected TbBalTransferDtlInfo balTransferDtlInfo = null;
    public BalTransferDtlBean()
    {
    }

    public TbBalTransferDtlInfo getBalTransferDtlInfo()
    {
        return balTransferDtlInfo;
    }

    public void setBalTransferDtlInfo(TbBalTransferDtlInfo balTransferDtlInfo)
    {
        if (balTransferDtlInfo != null) {
            this.balTransferDtlInfo = (TbBalTransferDtlInfo) balTransferDtlInfo.clone();
        }
    }

    public TbBalTransferInfo getBalTransferInfo()
    {
        return balTransferInfo;
    }

    public void setBalTransferInfo(TbBalTransferInfo balTransferInfo)
    {
        this.balTransferInfo = balTransferInfo;
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(balTransferDtlInfo.getBonusBase())) {
            balTransferDtlInfo.setBonusBase(balTransferInfo.getBonusBase());
        }
        if (StringUtil.isEmpty(balTransferDtlInfo.getBalanceType())) {
            balTransferDtlInfo.setBalanceType(balTransferInfo.getBalanceType());
        }
        if (StringUtil.isEmpty(balTransferDtlInfo.getOrigBalanceId())) {
            balTransferDtlInfo.setOrigBalanceId(balTransferInfo.getOrigBalanceId());
        }
        if (StringUtil.isEmpty(balTransferDtlInfo.getNewBalanceId())) {
            balTransferDtlInfo.setNewBalanceId(balTransferInfo.getNewBalanceId());
        }
        if (StringUtil.isEmpty(balTransferDtlInfo.getOrigExpiryDate())) {
            balTransferDtlInfo.setOrigExpiryDate(balTransferInfo.getOrigExpiryDate());
        }
        if (StringUtil.isEmpty(balTransferDtlInfo.getNewExpiryDate())) {
            balTransferDtlInfo.setNewExpiryDate(balTransferInfo.getNewExpiryDate());
        }
        if (StringUtil.isEmpty(balTransferDtlInfo.getParMon())) {
            balTransferDtlInfo.setParMon(balTransferInfo.getValidSdate().substring(4, 6));
        }
        if (StringUtil.isEmpty(balTransferDtlInfo.getParDay())) {
            balTransferDtlInfo.setParDay(balTransferInfo.getValidSdate().substring(6, 8));
        }
        return balTransferDtlInfo.toInsertSQL();
    }
}
