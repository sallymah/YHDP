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
public class SumLifeCycleInfo implements Serializable {
	private String batchDate = "";
	private String lastMonth = "";
	private String coBrandEntId = "";
	private String cardProduct = "";
    private int numOfNew3 = 0;
	private int numOfNew5 = 0;
    private int numOfNew6 = 0;
    private int numOfNew7 = 0;
    private int numOfNew8 = 0;

    public SumLifeCycleInfo() {
    }

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }
    
    public String getLastMonth() {
		return lastMonth;
	}

	public void setLastMonth(String lastMonth) {
		this.lastMonth = lastMonth;
	}
    
    public String getCoBrandEntId() {
		return coBrandEntId;
	}

	public void setCoBrandEntId(String coBrandEntId) {
		this.coBrandEntId = coBrandEntId;
	}

	public String getCardProduct() {
		return cardProduct;
	}

	public void setCardProduct(String cardProduct) {
		this.cardProduct = cardProduct;
	}

    public int getNumOfNew3() {
		return numOfNew3;
	}

	public void setNumOfNew3(int numOfNew3) {
		this.numOfNew3 = numOfNew3;
	}

	public int getNumOfNew5() {
		return numOfNew5;
	}

	public void setNumOfNew5(int numOfNew5) {
		this.numOfNew5 = numOfNew5;
	}

	public int getNumOfNew6() {
		return numOfNew6;
	}

	public void setNumOfNew6(int numOfNew6) {
		this.numOfNew6 = numOfNew6;
	}

	public int getNumOfNew7() {
		return numOfNew7;
	}

	public void setNumOfNew7(int numOfNew7) {
		this.numOfNew7 = numOfNew7;
	}

	public int getNumOfNew8() {
		return numOfNew8;
	}

	public void setNumOfNew8(int numOfNew8) {
		this.numOfNew8 = numOfNew8;
	}
	
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SumLifeCycleInfo that = (SumLifeCycleInfo) o;

        if (batchDate != null ? !batchDate.equals(that.batchDate) : that.batchDate != null) {
            return false;
        }
        if (coBrandEntId != null ? !coBrandEntId.equals(that.coBrandEntId) : that.coBrandEntId != null) {
            return false;
        }   
        if (cardProduct != null ? !cardProduct.equals(that.cardProduct) : that.cardProduct != null) {
            return false;
        }   
        return true;
    }

    public int hashCode() {
        int result;
        result = (batchDate != null ? batchDate.hashCode() : 0);
        result = 31 * result + (coBrandEntId != null ? coBrandEntId.hashCode() : 0);
        result = 31 * result + (cardProduct != null ? cardProduct.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SumLifeCycleInfo: ");
        sb.append(" batchDate:" + batchDate);
        sb.append(" coBrandEntId:" + coBrandEntId);
        sb.append(" cardProduct:" + cardProduct);
        sb.append(" numOfNew3:" + numOfNew3);
        sb.append(" numOfNew5:" + numOfNew5);
        sb.append(" numOfNew6:" + numOfNew6);
        sb.append(" numOfNew7:" + numOfNew7);
        sb.append(" numOfNew8:" + numOfNew8);
        sb.append("]");
        return sb.toString();
    }
}
