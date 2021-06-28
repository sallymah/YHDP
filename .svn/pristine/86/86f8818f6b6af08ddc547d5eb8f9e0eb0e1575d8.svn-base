/*
 * Version: 1.0.0
 * Date: 2007-10-15
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import tw.com.hyweb.service.db.info.TbLifeCycleMonSumInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbMerchSumInfo;
import tw.com.hyweb.service.db.info.TbMerchSumDtlInfo;
import tw.com.hyweb.service.db.info.TbPaymentRequestInfo;

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
public class SumPaymentRequestResultInfo implements Serializable {
	private TbMemberInfo memInfos = null;
    private TbPaymentRequestInfo paymentRequestInfo = null;
    
	public TbPaymentRequestInfo getPaymentRequestInfo() {
		return paymentRequestInfo;
	}

	public void setPaymentRequestInfo(TbPaymentRequestInfo paymentRequestInfo) {
		this.paymentRequestInfo = paymentRequestInfo;
	}

	public TbMemberInfo getMemInfos() {
		return memInfos;
	}

	public void setMemInfos(TbMemberInfo memInfos) {
		this.memInfos = memInfos;
	}

	public SumPaymentRequestResultInfo() {
	}
	
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SumPaymentRequestResultInfo that = (SumPaymentRequestResultInfo) o;

        if (paymentRequestInfo != null ? !paymentRequestInfo.equals(that.paymentRequestInfo) : that.paymentRequestInfo != null) {
            return false;
        }
        if (memInfos != null ? !memInfos.equals(that.memInfos) : that.memInfos != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (paymentRequestInfo != null ? paymentRequestInfo.hashCode() : 0);
        result = 31 * result + (memInfos != null ? memInfos.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SumPaymentRequestResultInfo: ");
        sb.append(" paymentRequestInfo:" + paymentRequestInfo);
        sb.append(" memInfos:" + memInfos);
        sb.append("]");
        return sb.toString();
    }
}
