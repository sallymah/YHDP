package tw.com.hyweb.svc.yhdp.batch.fee.creditFee;

public class CreditFeeTier {
    private String calRuleId = "";
    private double lowerBound = 0.0;
    private double feeRate = 0.0;
    private double preTierFee = 0.0;
    public String getCalRuleId()
    {
        return calRuleId;
    }
    public void setCalRuleId(String calRuleId)
    {
        this.calRuleId = calRuleId;
    }
    public double getFeeRate()
    {
        return feeRate;
    }
    public void setFeeRate(double feeRate)
    {
        this.feeRate = feeRate;
    }
    public double getLowerBound()
    {
        return lowerBound;
    }
    public void setLowerBound(double lowerBound)
    {
        this.lowerBound = lowerBound;
    }
    public double getPreTierFee()
    {
        return preTierFee;
    }
    public void setPreTierFee(double preTierFee)
    {
        this.preTierFee = preTierFee;
    }
}
