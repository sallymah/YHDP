package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbBalTransferInfo;

/**
 * <pre>
 * BalTransferBean javabean
 * balTransferInfo:TbBalTransferInfo
 * </pre>
 * author:duncan
 */
public interface IBalTransferBean extends IBean
{
    TbBalTransferInfo balTransferInfo = null;

    public TbBalTransferInfo getBalTransferInfo();

    public void setBalTransferInfo(TbBalTransferInfo balTransferInfo);
}
