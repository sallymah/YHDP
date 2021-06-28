package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbBalUpdateInfo;
import tw.com.hyweb.service.db.info.TbBalUpdateDtlInfo;


/**
 * <pre>
 * interface IBalUpdateDtlBean
 * </pre>
 * author:anny
 */
public interface IBalUpdateDtlBean  extends IBean
{
    TbBalUpdateInfo balUpdateInfo = null;
    
    TbBalUpdateDtlInfo balUpdateDtlInfo = null;

    public TbBalUpdateInfo getBalUpdateInfo();

    public void setBalUpdateInfo(TbBalUpdateInfo balUpdateInfo);

    public TbBalUpdateDtlInfo getBalUpdateDtlInfo();

    public void setBalUpdateDtlInfo(TbBalUpdateDtlInfo balUpdateDtlInfo);
}
