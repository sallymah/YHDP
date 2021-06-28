/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import java.io.Serializable;

/**
 * <pre>
 *  balanceInfo javabean
 * </pre>
 * 
 * author: anny
 */
public class BalanceInfo implements Serializable
{
    private String balanceId = "";
    
    private String balanceKey = "";

    public BalanceInfo()
    {
    }

    public String getBalanceKey()
    {
        return balanceKey;
    }

    public void setBalanceKey(String balanceKey)
    {
        this.balanceKey = balanceKey;
    }

    public String getBalanceId()
    {
        return balanceId;
    }

    public void setBalanceId(String balanceId)
    {
        this.balanceId = balanceId;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final BalanceInfo that = (BalanceInfo) o;

        if (balanceId != null ? !balanceId.equals(that.balanceId) : that.balanceId != null)
            return false;
        if (balanceKey != null ? !balanceKey.equals(that.balanceKey) : that.balanceKey != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (balanceKey != null ? balanceKey.hashCode() : 0);
        result = 29 * result + (balanceId != null ? balanceId.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[BalanceInfo: ");
        sb.append(" balanceKey:" + balanceKey);
        sb.append(" balanceId:" + balanceId);
        sb.append("]");
        return sb.toString();
    }
}
