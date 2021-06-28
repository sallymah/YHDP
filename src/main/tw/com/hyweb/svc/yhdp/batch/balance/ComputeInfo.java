/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.<br>
 * User: mswu<br>
 * Date: 2006/11/24<br>
 * Time: 上午 06:36:09 To<br>
 * change this template use File | Settings | File Templates.<br>
 */
public class ComputeInfo implements Serializable
{
    private double crValue = 0;

    private double dbValue = 0;

    private double balValue = 0;

    public ComputeInfo()
    {
    }

    public double getCrValue()
    {
        return crValue;
    }

    public void setCrValue(double crValue)
    {
        this.crValue = crValue;
    }

    public double getDbValue()
    {
        return dbValue;
    }

    public void setDbValue(double dbValue)
    {
        this.dbValue = dbValue;
    }

    public double getBalValue()
    {
        return balValue;
    }

    public void setBalValue(double balValue)
    {
        this.balValue = balValue;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final ComputeInfo that = (ComputeInfo) o;

        if (Double.compare(that.balValue, balValue) != 0)
            return false;
        if (Double.compare(that.crValue, crValue) != 0)
            return false;
        if (Double.compare(that.dbValue, dbValue) != 0)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        long temp;
        temp = crValue != +0.0d ? Double.doubleToLongBits(crValue) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = dbValue != +0.0d ? Double.doubleToLongBits(dbValue) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = balValue != +0.0d ? Double.doubleToLongBits(balValue) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[ComputeInfo: ");
        sb.append(" crValue:" + crValue);
        sb.append(" dbValue:" + dbValue);
        sb.append(" balValue:" + balValue);
        sb.append("]");
        return sb.toString();
    }
}
