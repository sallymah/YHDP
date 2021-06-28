package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;

/**
 * <pre>
 * interface IOnlTxnDtlBean
 * </pre>
 * author:anny
 */
public interface IOnlTxnDtlBean  extends IBean
{
    TbOnlTxnInfo onlTxnInfo = null;
    
    TbOnlTxnDtlInfo onlTxnDtlInfo = null;

    public TbOnlTxnInfo getOnlTxnInfo();

    public void setOnlTxnInfo(TbOnlTxnInfo onlTxnInfo);

    public TbOnlTxnDtlInfo getOnlTxnDtlInfo();

    public void setOnlTxnDtlInfo(TbOnlTxnDtlInfo onlTxnDtlInfo);
}
