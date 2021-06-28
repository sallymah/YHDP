package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbBalTransferInfo;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;

/**
 * <pre>
 * BalTransferBean javabean
 * balTransferInfo:TbBalTransferInfo
 * </pre>
 * author:duncan
 */
public class BalTransferBean implements IBalTransferBean
{
    protected TbBalTransferInfo balTransferInfo = null;

    public BalTransferBean()
    {
    }

    public TbBalTransferInfo getBalTransferInfo()
    {
        return balTransferInfo;
    }

    public void setBalTransferInfo(TbBalTransferInfo balTransferInfo)
    {
        if (balTransferInfo != null) {
            this.balTransferInfo = (TbBalTransferInfo) balTransferInfo.clone();
        }
    }

    public String getInsertSql() {
        if (StringUtil.isEmpty(balTransferInfo.getBtSrc())) {
            balTransferInfo.setBtSrc(Constants.BTSRC_BATCH);
        }
        if (StringUtil.isEmpty(balTransferInfo.getStatus())) {
            balTransferInfo.setStatus(Constants.BTSTATUS_INITIAL);
        }
        if (StringUtil.isEmpty(balTransferInfo.getParMon())) {
            balTransferInfo.setParMon(balTransferInfo.getValidSdate().substring(4, 6));
        }
        if (StringUtil.isEmpty(balTransferInfo.getParDay())) {
            balTransferInfo.setParDay(balTransferInfo.getValidSdate().substring(6, 8));
        }
        return balTransferInfo.toInsertSQL();
    }
}
