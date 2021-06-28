/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tw.com.hyweb.core.cp.common.misc.Layer2Util;

/**
 * <pre>
 * BalWithBonusInfos javabean
 * </pre>
 * 
 * author: mswu
 *         anny: 20070801 修改為offline交易也需要扣回cr/db(tracy,tiffany他們開會決議)
 */
public class BalWithBonusInfos
{
    // for BalChipCard, BalHostCard
    private BalanceInfo balanceInfo = null;

    // key:String BonusInfo.toKey(), value: List object, each element is
    // BonusInfo object
    private HashMap key2BonusInfos = new HashMap();

    // key:String BonusInfo.toKey(), value: ComputeInfo object
    private HashMap key2ComputeInfo = new HashMap();

    public BalWithBonusInfos()
    {
    }

    public BalanceInfo getBalanceInfo()
    {
        return balanceInfo;
    }

    public void setBalanceInfo(BalanceInfo balanceInfo)
    {
        this.balanceInfo = balanceInfo;
    }

    public void addBonusInfo(BonusInfo bonusInfo)
    {
        if (bonusInfo == null)
        {
            return;
        }
        String key = bonusInfo.toKey();
        if (key == null || "".equals(key))
        {
            return;
        }
        if (key.split(":").length < 3)
        {
            return;
        }
        List list = (List) key2BonusInfos.get(key);
        if (list == null)
        {
            list = new ArrayList();
            list.add(bonusInfo);
            key2BonusInfos.put(key, list);
        }
        else
        {
            list.add(bonusInfo);
        }
    }

    public HashMap getKey2BonusInfos()
    {
        return key2BonusInfos;
    }

    public void setKey2BonusInfos(HashMap key2BonusInfos)
    {
        this.key2BonusInfos = key2BonusInfos;
    }

    public void compute()
    {
        Set keys = key2BonusInfos.keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext())
        {
            String key = (String) iter.next();
            ComputeInfo cinfo = new ComputeInfo();
            List infos = (List) key2BonusInfos.get(key);
            for (int i = 0; i < infos.size(); i++)
            {
                BonusInfo info = (BonusInfo) infos.get(i);
                
                if (Layer2Util.isPlusSign(info.getTxnCode()))
                {
                    cinfo.setCrValue(cinfo.getCrValue() + info.getBonusQtySum());
                    cinfo.setBalValue(cinfo.getBalValue() + info.getBonusQtySum());
                }
                else if (Layer2Util.isMinusSign(info.getTxnCode()))
                {

                    cinfo.setDbValue(cinfo.getDbValue() + info.getBonusQtySum());
                    cinfo.setBalValue(cinfo.getBalValue() - info.getBonusQtySum());
                }
            }
            key2ComputeInfo.put(key, cinfo);
        }
    }

    public HashMap getKey2ComputeInfo()
    {
        return key2ComputeInfo;
    }

    public void setKey2ComputeInfo(HashMap key2ComputeInfo)
    {
        this.key2ComputeInfo = key2ComputeInfo;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[BonusInfos: ");
        sb.append(" balanceInfo:" + balanceInfo);
        sb.append(" key2bonusInfos:"+key2BonusInfos);
        sb.append(" key2ComputeInfo:"+key2ComputeInfo);
        sb.append("]");
        return sb.toString();
    }
}
