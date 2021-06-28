package tw.com.hyweb.core.yhdp.batch.util.beans;

import tw.com.hyweb.service.db.info.TbBalUpdateInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * BalUpdateBean javabean
 * balUpdateInfo:TbBalUpdateInfo
 * </pre>
 * author:duncan
 */
public class BalUpdateBean implements IBalUpdateBean
{
    protected TbBalUpdateInfo balUpdateInfo = null;

    public BalUpdateBean()
    {
    }

    public TbBalUpdateInfo getBalUpdateInfo()
    {
        return balUpdateInfo;
    }

    public void setBalUpdateInfo(TbBalUpdateInfo balUpdateInfo)
    {
        if (balUpdateInfo != null) {
            this.balUpdateInfo = (TbBalUpdateInfo) balUpdateInfo.clone();
        }
    }

    public String getInsertSql() {
        
        if (StringUtil.isEmpty(balUpdateInfo.getParMon())) {
            balUpdateInfo.setParMon(balUpdateInfo.getTxnDate().substring(4, 6));
        }
        if (StringUtil.isEmpty(balUpdateInfo.getParDay())) {
            balUpdateInfo.setParDay(balUpdateInfo.getTxnDate().substring(6, 8));
        }
        
        return balUpdateInfo.toInsertSQL();
    }
}
