/*
 * Version: 1.0.0
 * Date: 2015-02-10
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import java.io.Serializable;

import tw.com.hyweb.service.db.info.TbAppointReloadSumDtlInfo;
import tw.com.hyweb.service.db.info.TbAppointReloadSumInfo;

/**
 * <pre>
 * SumAppointReloadResultInfo javabean
 * sumAppointReloadInfo:SumAppointReloadInfo object
 * appointReloadSumInfos:List, each element is TbAppointReloadSumInfo object
 * appointReloadSumDtlInfos:List, each element is TbAppointReloadSumDtlInfo object
 * </pre>
 * author: Kevin
 */
public class SumAppointReloadResultInfo implements Serializable 
{
    private TbAppointReloadSumInfo sumAppointReloadInfo = null;
    private TbAppointReloadSumDtlInfo sumAppointReloadDtlInfo = null;

	public SumAppointReloadResultInfo() {
    }

    public TbAppointReloadSumInfo getSumAppointReloadInfo() {
        return sumAppointReloadInfo;
    }

    public void setSumAppointReloadInfo(TbAppointReloadSumInfo sumAppointReloadInfo) {
        this.sumAppointReloadInfo = sumAppointReloadInfo;
    }

    public TbAppointReloadSumDtlInfo getSumAppointReloadDtlInfo() {
		return sumAppointReloadDtlInfo;
	}

	public void setSumAppointReloadDtlInfo(TbAppointReloadSumDtlInfo sumAppointReloadDtlInfo) {
		this.sumAppointReloadDtlInfo = sumAppointReloadDtlInfo;
	}
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SumAppointReloadResultInfo that = (SumAppointReloadResultInfo) o;

        if (sumAppointReloadInfo != null ? !sumAppointReloadInfo.equals(that.sumAppointReloadInfo) : that.sumAppointReloadInfo != null) {
            return false;
        }
        if (sumAppointReloadDtlInfo != null ? !sumAppointReloadDtlInfo.equals(that.sumAppointReloadDtlInfo) : that.sumAppointReloadDtlInfo != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (sumAppointReloadInfo != null ? sumAppointReloadInfo.hashCode() : 0);
        result = 31 * result + (sumAppointReloadDtlInfo != null ? sumAppointReloadDtlInfo.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SumAppointReloadResultInfo: ");
        sb.append(" sumAppointReloadInfo:" + sumAppointReloadInfo);
        sb.append(" sumAppointReloadDtlInfo:" + sumAppointReloadDtlInfo);
        sb.append("]");
        return sb.toString();
    }
}
