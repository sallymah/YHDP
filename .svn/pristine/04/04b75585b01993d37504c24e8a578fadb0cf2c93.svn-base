package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbAppointReloadInfo;
import tw.com.hyweb.service.db.info.TbAppointReloadDtlInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * AppointReloadDtlBean javabean
 * appointReloadInfo:TbAppointReloadInfo
 * appointReloadDtlInfo:TbAppointReloadDtlInfo
 * </pre>
 * author:duncan
 */
public class AppointReloadDtlBean implements IAppointReloadDtlBean
{
    protected TbAppointReloadInfo appointReloadInfo = null;
    protected TbAppointReloadDtlInfo appointReloadDtlInfo = null;

    public AppointReloadDtlBean()
    {
    }

    public TbAppointReloadInfo getAppointReloadInfo()
    {
        return appointReloadInfo;
    }

    public void setAppointReloadInfo(TbAppointReloadInfo appointReloadInfo)
    {
        this.appointReloadInfo = appointReloadInfo;
    }

    public TbAppointReloadDtlInfo getAppointReloadDtlInfo()
    {
        return appointReloadDtlInfo;
    }

    public void setAppointReloadDtlInfo(TbAppointReloadDtlInfo appointReloadDtlInfo)
    {
        if (appointReloadDtlInfo != null) {
            this.appointReloadDtlInfo = (TbAppointReloadDtlInfo) appointReloadDtlInfo.clone();
        }
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(appointReloadDtlInfo.getBonusBase())) {
            appointReloadDtlInfo.setBonusBase(appointReloadInfo.getBonusBase());
        }
        if (StringUtil.isEmpty(appointReloadDtlInfo.getBalanceType())) {
            appointReloadDtlInfo.setBalanceType(appointReloadInfo.getBalanceType());
        }
        if (StringUtil.isEmpty(appointReloadDtlInfo.getBalanceId())) {
            appointReloadDtlInfo.setBalanceId(appointReloadInfo.getBalanceId());
        }
        if (StringUtil.isEmpty(appointReloadDtlInfo.getArSerno())) {
            appointReloadDtlInfo.setArSerno(appointReloadInfo.getArSerno());
        }
        if (StringUtil.isEmpty(appointReloadDtlInfo.getParMon())) {
            appointReloadDtlInfo.setParMon(appointReloadInfo.getValidSdate().substring(4, 6));
        }
        if (StringUtil.isEmpty(appointReloadDtlInfo.getParDay())) {
            appointReloadDtlInfo.setParDay(appointReloadInfo.getValidSdate().substring(6, 8));
        }
        return appointReloadDtlInfo.toInsertSQL();
    }
}
