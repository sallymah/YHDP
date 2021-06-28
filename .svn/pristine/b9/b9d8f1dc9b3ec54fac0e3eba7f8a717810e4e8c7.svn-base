/*
 * Version: 1.0.0
 * Date: 2007-10-15
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * <pre>
 * SumMerchInfo javabean
 * batchDate:String
 * memId:String
 * pcodes:List, each element is String object
 * </pre>
 * author: Ivan
 */
public class SumPaymentRequestInfo implements Serializable {
	private String batchDate = "";
    private int txnCount = 0;
	private int txnAmt = 0;

    public SumPaymentRequestInfo() {
    }

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }
    
    public int getTxnCount() {
		return txnCount;
	}

	public void setTxnCount(int txnCount) {
		this.txnCount = txnCount;
	}

	public int getTxnAmt() {
		return txnAmt;
	}

	public void setTxnAmt(int txnAmt) {
		this.txnAmt = txnAmt;
	}
	
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SumPaymentRequestInfo that = (SumPaymentRequestInfo) o;

        if (batchDate != null ? !batchDate.equals(that.batchDate) : that.batchDate != null) {
            return false;
        }
  
        return true;
    }

    public int hashCode() {
        int result;
        result = (batchDate != null ? batchDate.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SumPaymentRequestInfo: ");
        sb.append(" batchDate:" + batchDate);
        sb.append(" txnCount:" + txnCount);
        sb.append(" txnAmt:" + txnAmt);
        sb.append("]");
        return sb.toString();
    }
}
