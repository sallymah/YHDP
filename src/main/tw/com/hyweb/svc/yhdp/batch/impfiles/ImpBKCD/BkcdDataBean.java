package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBKCD;

public class BkcdDataBean {
	
	private String status = "";
	private String cardNo = "";
	private String expiryDate = "";
	private String freezeDate = "";
	private String bankId = "";
	private String rCode = "";
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getFreezeDate() {
		return freezeDate;
	}

	public void setFreezeDate(String freezeDate) {
		this.freezeDate = freezeDate;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getrCode() {
		return rCode;
	}

	public void setrCode(String rCode) {
		this.rCode = rCode;
	}

	public String toString() 
	{
		return "[BkcdDataBean] " + 
				"status = " + getStatus() + ", " + 
				"cardNo = " + getCardNo() + ", " + 
				"expiryDate = " + getExpiryDate() + ", " + 
				"freezeDate = " + getFreezeDate() + ", " + 
				"bankId = " + getBankId() + ", " + 
				"rCode = " + getrCode() + ". ";
	}
}
