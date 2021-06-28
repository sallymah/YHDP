/*
 * Version: 1.0.0
 * Date: 2007-10-15
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import tw.com.hyweb.service.db.info.TbLifeCycleMonSumInfo;
import tw.com.hyweb.service.db.info.TbMerchSumInfo;
import tw.com.hyweb.service.db.info.TbMerchSumDtlInfo;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * <pre>
 * SumMerchResultInfo javabean
 * sumMerchInfo:SumMerchInfo object
 * merchSumInfos:List, each element is TbMerchSumInfo object
 * merchSumDtlInfos:List, each element is TbMerchSumDtlInfo object
 * </pre>
 * author: Ivan
 */
public class SumLifeCycleResultInfo implements Serializable {
    private SumLifeCycleInfo sumLifeCycleInfo = null;
    private TbLifeCycleMonSumInfo lifeCycleMonSumInfo = null;

	public SumLifeCycleResultInfo() {
    }

    public SumLifeCycleInfo getSumLifeCycleInfo() {
        return sumLifeCycleInfo;
    }

    public void setSumLifeCycleInfo(SumLifeCycleInfo sumLifeCycleInfo) {
        this.sumLifeCycleInfo = sumLifeCycleInfo;
    }

    public TbLifeCycleMonSumInfo getLifeCycleMonSumInfo() {
		return lifeCycleMonSumInfo;
	}

	public void setLifeCycleMonSumInfo(TbLifeCycleMonSumInfo lifeCycleMonSumInfo) {
		this.lifeCycleMonSumInfo = lifeCycleMonSumInfo;
	}
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SumLifeCycleResultInfo that = (SumLifeCycleResultInfo) o;

        if (sumLifeCycleInfo != null ? !sumLifeCycleInfo.equals(that.sumLifeCycleInfo) : that.sumLifeCycleInfo != null) {
            return false;
        }
        if (lifeCycleMonSumInfo != null ? !lifeCycleMonSumInfo.equals(that.lifeCycleMonSumInfo) : that.lifeCycleMonSumInfo != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (sumLifeCycleInfo != null ? sumLifeCycleInfo.hashCode() : 0);
        result = 31 * result + (lifeCycleMonSumInfo != null ? lifeCycleMonSumInfo.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SumMerchResultInfo: ");
        sb.append(" sumMerchInfo:" + sumLifeCycleInfo);
        sb.append(" lifeCycleMonSumInfo:" + lifeCycleMonSumInfo);
        sb.append("]");
        return sb.toString();
    }
}
