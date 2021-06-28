package tw.com.hyweb.core.yhdp.common.misc;

import tw.com.hyweb.service.db.info.TbCustInfo;

import java.io.Serializable;

/**
 * <pre>
 * CustAssociatorInfo javabean
 * custInfo:TbCustInfo
 * associatorInfo:TbAssociatorInfo
 * </pre>
 * author:duncan
 */
public class CustAssociatorInfo implements Serializable
{
    private TbCustInfo custInfo = null;

    public CustAssociatorInfo()
    {
    }

    public TbCustInfo getCustInfo()
    {
        return custInfo;
    }

    public void setCustInfo(TbCustInfo custInfo)
    {
        this.custInfo = custInfo;
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

        CustAssociatorInfo that = (CustAssociatorInfo) o;

        if (custInfo != null ? !custInfo.equals(that.custInfo) : that.custInfo != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (custInfo != null ? custInfo.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[CustAssociatorInfo: ");
        sb.append(" custInfo:" + custInfo);
        sb.append("]");
        return sb.toString();
    }
}
