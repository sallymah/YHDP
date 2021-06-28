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
public class SumMerchInfo implements Serializable {
    private String batchDate = "";
    private String acqmemId = "";
    private List pcodes = new ArrayList();

    public SumMerchInfo() {
    }

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }

    public String getAcqmemId() {
		return acqmemId;
	}

	public void setAcqmemId(String acqmemId) {
		this.acqmemId = acqmemId;
	}
		
    public List getPcodes() {
        return pcodes;
    }

    public void setPcodes(List pcodes) {
        this.pcodes = pcodes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SumMerchInfo that = (SumMerchInfo) o;

        if (batchDate != null ? !batchDate.equals(that.batchDate) : that.batchDate != null) {
            return false;
        }
        if (acqmemId != null ? !acqmemId.equals(that.acqmemId) : that.acqmemId != null) {
            return false;
        }   
        if (pcodes != null ? !pcodes.equals(that.pcodes) : that.pcodes != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (batchDate != null ? batchDate.hashCode() : 0);
        result = 31 * result + (acqmemId != null ? acqmemId.hashCode() : 0);
        result = 31 * result + (pcodes != null ? pcodes.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SumMerchInfo: ");
        sb.append(" batchDate:" + batchDate);
        sb.append(" acqmemId:" + acqmemId);
        sb.append(" pcodes:" + pcodes);
        sb.append("]");
        return sb.toString();
    }

}
