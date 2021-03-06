package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpRBMEM;

public class BmemDataBean {
	
	private String custId = "";
	private String cardNo = "";
	private String status = "";
	private String locName = "";
	private String personId = "";
	private String gender = "";
	private String birthday = "";
	private String city = "";
	private String zipCode = "";
	private String address = "";
	private String telHome = "";
	private String mobile = "";
	private String email = "";
	private String dmFlag = "";
	private String legalAgentName = "";
	private String legalAgentPid = "";
	private String legalAgentMobile = "";
	private String legalAgentPhone = "";
	private String vipFlag = "";
//	private String mbrRegDate = "";
	private String issueDate = "";
	private String expiryDate = "";
	private String merchId = "";
	private String marriage = "";
	private String saleCode = "";
	private String rCode = "";
	private String hgCardNo = "";
	
	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLocName() {
		return locName;
	}

	public void setLocName(String locName) {
		this.locName = locName;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTelHome() {
		return telHome;
	}

	public void setTelHome(String telHome) {
		this.telHome = telHome;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDmFlag() {
		return dmFlag;
	}

	public void setDmFlag(String dmFlag) {
		this.dmFlag = dmFlag;
	}

	public String getLegalAgentName() {
		return legalAgentName;
	}

	public void setLegalAgentName(String legalAgentName) {
		this.legalAgentName = legalAgentName;
	}

	public String getLegalAgentPid() {
		return legalAgentPid;
	}

	public void setLegalAgentPid(String legalAgentPid) {
		this.legalAgentPid = legalAgentPid;
	}

	public String getLegalAgentMobile() {
		return legalAgentMobile;
	}

	public void setLegalAgentMobile(String legalAgentMobile) {
		this.legalAgentMobile = legalAgentMobile;
	}

	public String getLegalAgentPhone() {
		return legalAgentPhone;
	}

	public void setLegalAgentPhone(String legalAgentPhone) {
		this.legalAgentPhone = legalAgentPhone;
	}

	public String getVipFlag() {
		return vipFlag;
	}

	public void setVipFlag(String vipFlag) {
		this.vipFlag = vipFlag;
	}

//	public String getMbrRegDate() {
//		return mbrRegDate;
//	}
//
//	public void setMbrRegDate(String mbrRegDate) {
//		this.mbrRegDate = mbrRegDate;
//	}
	
	public String getExpiryDate() {
		return expiryDate;
	}

	public String getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(String issueDate) {
		this.issueDate = issueDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getMerchId() {
		return merchId;
	}

	public void setMerchId(String merchId) {
		this.merchId = merchId;
	}

	public String getMarriage() {
		return marriage;
	}

	public void setMarriage(String marriage) {
		this.marriage = marriage;
	}

	public String getSaleCode() {
		return saleCode;
	}

	public void setSaleCode(String saleCode) {
		this.saleCode = saleCode;
	}

	public String getrCode() {
		return rCode;
	}

	public void setrCode(String rCode) {
		this.rCode = rCode;
	}

	public String getHgCardNo() {
		return hgCardNo;
	}

	public void setHgCardNo(String hgCardNo) {
		this.hgCardNo = hgCardNo;
	}

	public String toString() 
	{
		return "[BmemDataBean] " + 
				"custId = " + getCustId()+ ", " +
				"cardNo = " + getCardNo() + ", " + 
				"status = " + getStatus() + ", " +
				"locName = " + getLocName() + ", " + 
				"gender = " + getGender() + ", " + 
				"birthday = " + getBirthday() + ", " + 
				"city = " + getCity() + ", " +
				"zipCode = " + getZipCode() + ", " +
				"address = " + getAddress() + ", " +
				"telHome = " + getTelHome() + ", " +
				"mobile = " + getMobile() + ", " +
				"email = " + getEmail() + ", " +
				"dmFlag = " + getDmFlag() + ", " +
				"legalAgentName = " + getLegalAgentName() + ", " +
				"legalAgentPid = " + getLegalAgentPid() + ", " +
				"legalAgentMobile = " + getLegalAgentMobile() + ", " +
				"legalAgentPhone = " + getLegalAgentPhone() + ", " +
				"vipFlag = " + getVipFlag() + ", " +
				"issueDate = " + getIssueDate() + ", " +
				"expiryDate = " + getExpiryDate() + ", " +
				"merchId = " + getMerchId() + ", " +
				"marriage = " + getMarriage() + ", " + 
				"saleCode = " + getSaleCode() + ", " +
				"rCode = " + getrCode() + ", " +
				"hgCardNo = " + getHgCardNo() + ". ";
	}
}
