package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbAppointReloadInfo;
import tw.com.hyweb.service.db.info.TbAppointReloadDtlInfo;

/**
 * <pre>
 * AppointReloadDtlBean javabean
 * appointReloadInfo:TbAppointReloadInfo
 * appointReloadDtlInfo:TbAppointReloadDtlInfo
 * </pre>
 * author:duncan
 */
public interface IAppointReloadDtlBean extends IBean
{
    TbAppointReloadInfo appointReloadInfo = null;
    TbAppointReloadDtlInfo appointReloadDtlInfo = null;

    public TbAppointReloadInfo getAppointReloadInfo();

    public void setAppointReloadInfo(TbAppointReloadInfo appointReloadInfo);

    public TbAppointReloadDtlInfo getAppointReloadDtlInfo();

    public void setAppointReloadDtlInfo(TbAppointReloadDtlInfo appointReloadDtlInfo);
}
