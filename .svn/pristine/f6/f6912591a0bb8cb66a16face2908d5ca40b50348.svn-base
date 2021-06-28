/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.core.yhdp.common.misc;

import java.io.Serializable;

/**
 * <pre>
 *  DateRange javabean
 * </pre>
 * 
 * author: mswu
 */
public class DateRange implements Serializable
{
    private String startDate = "";

    private String endDate = "";

    public DateRange()
    {
    }

    public DateRange(String startDate, String endDate)
    {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final DateRange dateRange = (DateRange) o;

        if (endDate != null ? !endDate.equals(dateRange.endDate) : dateRange.endDate != null)
        {
            return false;
        }
        if (startDate != null ? !startDate.equals(dateRange.startDate) : dateRange.startDate != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (startDate != null ? startDate.hashCode() : 0);
        result = 29 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[DateRange: ");
        sb.append(" startDate:" + startDate);
        sb.append(" endDate:" + endDate);
        sb.append("]");
        return sb.toString();
    }
}
