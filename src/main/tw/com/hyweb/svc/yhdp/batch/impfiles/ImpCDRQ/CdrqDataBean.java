package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCDRQ;

public class CdrqDataBean {
	
	private String reqType = "";
	private String cardNo = "";
	private String expiryDate = "";
	private String fullFileName = "";
	
	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
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

	public String getFullFileName() {
		return fullFileName;
	}

	public void setFullFileName(String fullFileName) {
		this.fullFileName = fullFileName;
	}

	public String toString() 
	{
		return "[CdrqDataBean] " + 
				"reqType = " + getReqType() + ", " + 
				"cardNo = " + getCardNo() + ", " + 
				"expiryDate = " + getExpiryDate() + ", " + 
				"fullFileName = " + getFullFileName() + ". ";
	}
}
