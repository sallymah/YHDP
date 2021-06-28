/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */

package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 *  CheckOfflineInfo javabean
 *  tbTransInfo:TbTransInfo
 *  rcodes:List
 * </pre>
 * 
 * author: duncan
 */
public class CheckOfflineInfo implements Serializable
{
    private TbTransInfo tbTransInfo = null;

    private List rcodes = new ArrayList();

    public CheckOfflineInfo()
    {
    }

    /**
     * tbTransInfo getter
     * 
     * @return tbTransInfo
     */
    public TbTransInfo getTbTransInfo()
    {
        return tbTransInfo;
    }

    /**
     * tbTransInfo setter
     * 
     * @param tbTransInfo tbTransInfo
     */
    public void setTbTransInfo(TbTransInfo tbTransInfo)
    {
        this.tbTransInfo = tbTransInfo;
    }

    /**
     * rcodes getter
     * 
     * @return rcodes
     */
    public List getRcodes()
    {
        return rcodes;
    }

    /**
     * rcodes setter
     * 
     * @param rcodes rcodes
     */
    public void setRcodes(List rcodes)
    {
        this.rcodes = rcodes;
    }

    /**
     * add rcode to rcodes
     * 
     * @param rcode rcode
     */
    public void addRcode(String rcode)
    {
        if (StringUtil.isEmpty(rcode) || Constants.RCODE_0000_OK.equals(rcode))
        {
            return;
        }
        rcodes.add(rcode);
    }

    /**
     * override equals method
     * 
     * @param o o
     * @return override
     */
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

        CheckOfflineInfo that = (CheckOfflineInfo) o;

        if (rcodes != null ? !rcodes.equals(that.rcodes) : that.rcodes != null)
        {
            return false;
        }
        if (tbTransInfo != null ? !tbTransInfo.equals(that.tbTransInfo) : that.tbTransInfo != null)
        {
            return false;
        }

        return true;
    }

    /**
     * override hashCode method
     * 
     * @return override
     */
    public int hashCode()
    {
        int result;
        result = (tbTransInfo != null ? tbTransInfo.hashCode() : 0);
        result = 31 * result + (rcodes != null ? rcodes.hashCode() : 0);
        return result;
    }

    /**
     * override toString method
     * 
     * @return override
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[CheckOfflineInfo: ");
        sb.append(" tbTransInfo:" + tbTransInfo);
        sb.append(" rcodes:" + rcodes);
        sb.append("]");
        return sb.toString();
    }
}
