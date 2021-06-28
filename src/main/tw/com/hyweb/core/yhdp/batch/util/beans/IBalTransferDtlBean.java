package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbBalTransferInfo;
import tw.com.hyweb.service.db.info.TbBalTransferDtlInfo;

/**
 * <pre>
 * BalTransferDtlBean javabean
 * balTransferInfo:TbBalTransferInfo
 * balTransferDtlInfo:TbBalTransferDtlInfo
 * </pre>
 * author:duncan
 */
public interface IBalTransferDtlBean extends IBean
{
    TbBalTransferInfo balTransferInfo = null;
    TbBalTransferDtlInfo balTransferDtlInfo = null;

    public TbBalTransferDtlInfo getBalTransferDtlInfo();

    public void setBalTransferDtlInfo(TbBalTransferDtlInfo balTransferDtlInfo);

    public TbBalTransferInfo getBalTransferInfo();

    public void setBalTransferInfo(TbBalTransferInfo balTransferInfo);
}
