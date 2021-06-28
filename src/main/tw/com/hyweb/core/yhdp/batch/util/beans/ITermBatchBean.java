package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbTermBatchInfo;

/**
 * <pre>
 * interface ITermBatchBean
 * </pre>
 * author:anny
 */
public interface ITermBatchBean  extends IBean
{
    TbTermBatchInfo termBatchInfo = null;

    public TbTermBatchInfo getTermBatchInfo();

    public void setTermBatchInfo(TbTermBatchInfo termBatchInfo);
}
