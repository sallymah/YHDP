package tw.com.hyweb.svc.yhdp.batch.fee.grantsFee;

public class GrantsFeeConfig {
	
	String feeConfigId;
	String feeCode;
	String feeConfigDesc;
	String acqMemId;
	String allowDeduct;
	double fixedFee;
	String calBase;
	String calRuleId;
	String procCycle;
	String validSdate;
	String validEdate;
	String fundType;
	String accountCode;
	String taxCode;
	String expPayDate;

	public String getFeeConfigId() {
		return feeConfigId;
	}
	public void setFeeConfigId(String feeConfigId) {
		this.feeConfigId = feeConfigId;
	}
	public String getFeeCode() {
		return feeCode;
	}
	public void setFeeCode(String feeCode) {
		this.feeCode = feeCode;
	}
	public String getFeeConfigDesc() {
		return feeConfigDesc;
	}
	public void setFeeConfigDesc(String feeConfigDesc) {
		this.feeConfigDesc = feeConfigDesc;
	}
	public String getAcqMemId() {
		return acqMemId;
	}
	public void setAcqMemId(String acqMemId) {
		this.acqMemId = acqMemId;
	}
	public String getAllowDeduct() {
		return allowDeduct;
	}
	public void setAllowDeduct(String allowDeduct) {
		this.allowDeduct = allowDeduct;
	}
	public double getFixedFee() {
		return fixedFee;
	}
	public void setFixedFee(double fixedFee) {
		this.fixedFee = fixedFee;
	}
	public String getCalBase() {
		return calBase;
	}
	public void setCalBase(String calBase) {
		this.calBase = calBase;
	}
	public String getCalRuleId() {
		return calRuleId;
	}
	public void setCalRuleId(String calRuleId) {
		this.calRuleId = calRuleId;
	}
	public String getProcCycle() {
		return procCycle;
	}
	public void setProcCycle(String procCycle) {
		this.procCycle = procCycle;
	}
	public String getValidSdate() {
		return validSdate;
	}
	public void setValidSdate(String validSdate) {
		this.validSdate = validSdate;
	}
	public String getValidEdate() {
		return validEdate;
	}
	public void setValidEdate(String validEdate) {
		this.validEdate = validEdate;
	}
	public String getFundType() {
		return fundType;
	}
	public void setFundType(String fundType) {
		this.fundType = fundType;
	}
	public String getAccountCode() {
		return accountCode;
	}
	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}
	public String getTaxCode() {
		return taxCode;
	}
	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}
	public String getExpPayDate() {
		return expPayDate;
	}
	public void setExpPayDate(String expPayDate) {
		this.expPayDate = expPayDate;
	}
	
}
