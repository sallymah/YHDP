/*
 * Version: 1.0.0
 * Date: 2009-06-19
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import tw.com.hyweb.service.db.info.TbProductSumInfo;
import tw.com.hyweb.service.db.info.TbProductSumDtlInfo;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * <pre>
 * SumProductResultInfo javabean
 * sumProductInfo:SumProductInfo object
 * productSumInfos:List, each element is TbProductSumInfo object
 * productSumDtlInfos:List, each element is TbProductSumDtlInfo object
 * </pre>
 * author: Ivan
 */
public class SumProductResultInfo implements Serializable {
    private SumProductInfo sumProductInfo = null;
    private List productSumInfos = new ArrayList();
    private List productSumDtlInfos = new ArrayList();

    public SumProductResultInfo() {
    }

    public SumProductInfo getSumProductInfo() {
        return sumProductInfo;
    }

    public void setSumProductInfo(SumProductInfo sumProductInfo) {
        this.sumProductInfo = sumProductInfo;
    }

    public List getProductSumInfos() {
        return productSumInfos;
    }

    public void setProductSumInfos(List productSumInfos) {
        this.productSumInfos = productSumInfos;
    }

    public void addProductSumInfo(TbProductSumInfo info) {
        if (info != null) {
            productSumInfos.add(info);
        }
    }

    public List getProductSumDtlInfos() {
        return productSumDtlInfos;
    }

    public void setProductSumDtlInfos(List productSumDtlInfos) {
        this.productSumDtlInfos = productSumDtlInfos;
    }

    public void addProductSumDtlInfo(TbProductSumDtlInfo info) {
        if (info != null) {
            productSumDtlInfos.add(info);
        }
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SumProductResultInfo that = (SumProductResultInfo) o;

        if (productSumDtlInfos != null ? !productSumDtlInfos.equals(that.productSumDtlInfos) : that.productSumDtlInfos != null) {
            return false;
        }
        if (productSumInfos != null ? !productSumInfos.equals(that.productSumInfos) : that.productSumInfos != null) {
            return false;
        }
        if (sumProductInfo != null ? !sumProductInfo.equals(that.sumProductInfo) : that.sumProductInfo != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (sumProductInfo != null ? sumProductInfo.hashCode() : 0);
        result = 31 * result + (productSumInfos != null ? productSumInfos.hashCode() : 0);
        result = 31 * result + (productSumDtlInfos != null ? productSumDtlInfos.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SumProductResultInfo: ");
        sb.append(" sumProductInfo:" + sumProductInfo);
        sb.append(" productSumInfos:" + productSumInfos);
        sb.append(" productSumDtlInfos:" + productSumDtlInfos);
        sb.append("]");
        return sb.toString();
    }
}
