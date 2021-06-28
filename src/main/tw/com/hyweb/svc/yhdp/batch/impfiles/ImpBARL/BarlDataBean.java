package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBARL;

public class BarlDataBean {
	
	private String status = "";
	private String cardNo = "";
	private String endDate = "";
	private String rCode = "";
	private String expiryDate  = "";
	private String fullFileName = "";
	
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

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getrCode() {
		return rCode;
	}

	public void setrCode(String rCode) {
		this.rCode = rCode;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	public String getFullFileName() {
		return fullFileName;
	}

	public void setFullFileName(String fullFileName) {
		this.fullFileName = fullFileName;
	}

	public String toString() 
	{
		return "[BarlDataBean] " + 
				"status = " + getStatus() + ", " + 
				"cardNo = " + getCardNo() + ", " + 
				"endDate = " + getEndDate() + ", " + 
				"rCode = " + getrCode() + ", " + 
				"expiryDate = " + getExpiryDate() + ", " + 
				"fullFileName = " + getFullFileName() + ". ";
	}
}
