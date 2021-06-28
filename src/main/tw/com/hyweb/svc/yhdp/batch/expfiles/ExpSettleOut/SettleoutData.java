package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpSettleOut;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SettleoutData {
	private String accountCode = "";		
	private String termSettleDate = "";
	private String procDate = "";
	private String agency = "";
	private String acqMemId = "";
	private String cardCatId = "";
	private String productTypeId = "";
	private String industryId = "";
	private String amt = "0";
	private String num = "1";
	private String cardNo = "0000000000000000";
	private String expPayDate = "";
	private String amtNotax = "0";
	private String taxFee = "0";
	private String amtWithtax = "0";
	
	public String getAccountCode() {
		return accountCode;
	}
	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}
	public String getTermSettleDate() {
		return termSettleDate;
	}
	public void setTermSettleDate(String termSettleDate) {
		this.termSettleDate = termSettleDate;
	}
	public String getProcDate() {
		return procDate;
	}
	public void setProcDate(String procDate) {
		this.procDate = procDate;
	}
	public String getAgency() {
		return agency;
	}
	public void setAgency(String agency) {
		this.agency = agency;
	}
	public String getAcqMemId() {
		return acqMemId;
	}
	public void setAcqMemId(String acqMemId) {
		this.acqMemId = acqMemId;
	}
	public String getCardCatId() {
		return cardCatId;
	}
	public void setCardCatId(String cardCatId) {
		this.cardCatId = cardCatId;
	}
	public String getProductTypeId() {
		return productTypeId;
	}
	public void setProductTypeId(String productTypeId) {
		this.productTypeId = productTypeId;
	}
	public String getIndustryId() {
		return industryId;
	}
	public void setIndustryId(String industryId) {
		this.industryId = industryId;
	}
	public String getAmt() {
		return amt;
	}
	public void setAmt(String amt) {
		if (!isBlankOrNull(amt))
			this.amt = amt;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		if (!isBlankOrNull(cardNo))
			this.cardNo = cardNo;
	}
	public String getExpPayDate() {
		return expPayDate;
	}
	public void setExpPayDate(String expPayDate) {
		this.expPayDate = expPayDate;
	}
	public String getAmtNotax() {
		return amtNotax;
	}
	public void setAmtNotax(String amtNotax) {
		if (!isBlankOrNull(amtNotax))
			this.amtNotax = amtNotax;
	}
	public String getAmtWithtax() {
		return amtWithtax;
	}
	public void setAmtWithtax(String amtWithtax) {
		if (!isBlankOrNull(amtWithtax))
			this.amtWithtax = amtWithtax;
	}
	public String getTaxFee() {
		return taxFee;
	}
	public void setTaxFee(String taxFee) {
		if (!isBlankOrNull(taxFee))
			this.taxFee = taxFee;
	}
	public void setMapInfo(ResultSet rs) throws SQLException {		
		accountCode = (String) rs.getString(1);		
		termSettleDate = (String) rs.getString(2);
		procDate = (String) rs.getString(3);
		//agency
		acqMemId = (String) rs.getString(4);
		//cardCatId = (String) rs.getString(5).substring(1, 2);
		//productTypeId = (String) rs.getString(5).substring(3, 3);
		//industryId
		amt = rs.getString(6).toString();
	}
	public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
	public void remove() {
		accountCode = "";		
		termSettleDate = "";
		procDate = "";
		agency = "";
		acqMemId = "";
		cardCatId = "";
		productTypeId = "";
		industryId = "";
		amt = "0";
		num = "1";
		cardNo = "0000000000000000";
		expPayDate = "";
		amtNotax = "0";
		taxFee = "0";
		amtWithtax = "0";
	}
}
