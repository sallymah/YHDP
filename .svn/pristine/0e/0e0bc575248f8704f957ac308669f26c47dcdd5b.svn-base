package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbAppointReloadInfo;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;

/**
 * <pre>
 * AppointReloadBean javabean
 * appointReloadInfo:TbAppointReloadInfo
 * </pre>
 * author:duncan
 */
public class AppointReloadBean implements IAppointReloadBean
{
    protected TbAppointReloadInfo appointReloadInfo = null;

    public AppointReloadBean()
    {
    }

    public TbAppointReloadInfo getAppointReloadInfo()
    {
        return appointReloadInfo;
    }

    public void setAppointReloadInfo(TbAppointReloadInfo appointReloadInfo)
    {
        if (appointReloadInfo != null) {
            this.appointReloadInfo = (TbAppointReloadInfo) appointReloadInfo.clone();
        }
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(appointReloadInfo.getArSrc())) {
            appointReloadInfo.setArSrc(Constants.ARSRC_BATCH);
        }
        if (StringUtil.isEmpty(appointReloadInfo.getParMon())) {
            appointReloadInfo.setParMon(appointReloadInfo.getValidSdate().substring(4, 6));
        }
        if (StringUtil.isEmpty(appointReloadInfo.getParDay())) {
            appointReloadInfo.setParDay(appointReloadInfo.getValidSdate().substring(6, 8));
        }
        return appointReloadInfo.toInsertSQL();
    }
}
