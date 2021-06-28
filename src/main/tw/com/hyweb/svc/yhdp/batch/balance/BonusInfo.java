/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import java.io.Serializable;

/**
 * <pre>
 *  BonusInfo javabean
 * </pre>
 * 
 * author: mswu
 */
public class BonusInfo implements Serializable
{
    private String txnCode = "";

    private String bonusId = "";

    private String bonusSDate = "";

    private String bonusEDate = "";

    private double bonusQtySum = 0;

    public BonusInfo()
    {
    }

    public void toEmpty()
    {
        txnCode = "";
        bonusId = "";
        bonusSDate = "";
        bonusEDate = "";
        bonusQtySum = 0;
    }

    public String toKey()
    {
        return bonusId + ":" + bonusSDate + ":" + bonusEDate;
    }

    public String getTxnCode()
    {
        return txnCode;
    }

    public void setTxnCode(String txnCode)
    {
        this.txnCode = txnCode;
    }

    public String getBonusId()
    {
        return bonusId;
    }

    public void setBonusId(String bonusId)
    {
        this.bonusId = bonusId;
    }

    public String getBonusSDate()
    {
        return bonusSDate;
    }

    public void setBonusSDate(String bonusSDate)
    {
        this.bonusSDate = bonusSDate;
    }

    public String getBonusEDate()
    {
        return bonusEDate;
    }

    public void setBonusEDate(String bonusEDate)
    {
        this.bonusEDate = bonusEDate;
    }

    public double getBonusQtySum()
    {
        return bonusQtySum;
    }

    public void setBonusQtySum(double bonusQtySum)
    {
        this.bonusQtySum = bonusQtySum;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final BonusInfo bonusInfo = (BonusInfo) o;

        if (Double.compare(bonusInfo.bonusQtySum, bonusQtySum) != 0)
            return false;
        if (bonusEDate != null ? !bonusEDate.equals(bonusInfo.bonusEDate) : bonusInfo.bonusEDate != null)
            return false;
        if (bonusId != null ? !bonusId.equals(bonusInfo.bonusId) : bonusInfo.bonusId != null)
            return false;
        if (bonusSDate != null ? !bonusSDate.equals(bonusInfo.bonusSDate) : bonusInfo.bonusSDate != null)
            return false;
        if (txnCode != null ? !txnCode.equals(bonusInfo.txnCode) : bonusInfo.txnCode != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        long temp;
        result = (txnCode != null ? txnCode.hashCode() : 0);
        result = 29 * result + (bonusId != null ? bonusId.hashCode() : 0);
        result = 29 * result + (bonusSDate != null ? bonusSDate.hashCode() : 0);
        result = 29 * result + (bonusEDate != null ? bonusEDate.hashCode() : 0);
        temp = bonusQtySum != +0.0d ? Double.doubleToLongBits(bonusQtySum) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[BonusInfo: ");
        sb.append(" txnCode:" + txnCode);
        sb.append(" bonusId:" + bonusId);
        sb.append(" bonusSDate:" + bonusSDate);
        sb.append(" bonusEDate:" + bonusEDate);
        sb.append(" bonusQtySum:" + bonusQtySum);
        sb.append("]");
        return sb.toString();
    }
}
