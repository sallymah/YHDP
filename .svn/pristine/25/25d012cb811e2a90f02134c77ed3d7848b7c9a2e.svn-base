/*
 * Version: 1.0.0
 * Date: 2007-10-15
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

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
public class SumMerchResultInfo implements Serializable {
    private SumMerchInfo sumMerchInfo = null;
    private List merchSumInfos = new ArrayList();
    private List merchSumDtlInfos = new ArrayList();

    public SumMerchResultInfo() {
    }

    public SumMerchInfo getSumMerchInfo() {
        return sumMerchInfo;
    }

    public void setSumMerchInfo(SumMerchInfo sumMerchInfo) {
        this.sumMerchInfo = sumMerchInfo;
    }

    public List getMerchSumInfos() {
        return merchSumInfos;
    }

    public void setMerchSumInfos(List merchSumInfos) {
        this.merchSumInfos = merchSumInfos;
    }

    public void addMerchSumInfo(TbMerchSumInfo info) {
        if (info != null) {
            merchSumInfos.add(info);
        }
    }

    public List getMerchSumDtlInfos() {
        return merchSumDtlInfos;
    }

    public void setMerchSumDtlInfos(List merchSumDtlInfos) {
        this.merchSumDtlInfos = merchSumDtlInfos;
    }

    public void addMerchSumDtlInfo(TbMerchSumDtlInfo info) {
        if (info != null) {
            merchSumDtlInfos.add(info);
        }
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SumMerchResultInfo that = (SumMerchResultInfo) o;

        if (merchSumDtlInfos != null ? !merchSumDtlInfos.equals(that.merchSumDtlInfos) : that.merchSumDtlInfos != null) {
            return false;
        }
        if (merchSumInfos != null ? !merchSumInfos.equals(that.merchSumInfos) : that.merchSumInfos != null) {
            return false;
        }
        if (sumMerchInfo != null ? !sumMerchInfo.equals(that.sumMerchInfo) : that.sumMerchInfo != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (sumMerchInfo != null ? sumMerchInfo.hashCode() : 0);
        result = 31 * result + (merchSumInfos != null ? merchSumInfos.hashCode() : 0);
        result = 31 * result + (merchSumDtlInfos != null ? merchSumDtlInfos.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SumMerchResultInfo: ");
        sb.append(" sumMerchInfo:" + sumMerchInfo);
        sb.append(" merchSumInfos:" + merchSumInfos);
        sb.append(" merchSumDtlInfos:" + merchSumDtlInfos);
        sb.append("]");
        return sb.toString();
    }
}
