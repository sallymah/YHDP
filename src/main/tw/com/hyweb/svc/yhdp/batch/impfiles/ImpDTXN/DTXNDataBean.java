package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpDTXN;

public class DTXNDataBean {
	
	private String cardNo;
	private String expiryDate;
	private String txnMon;
	private int ttlCnt;
	private double ttlAmt;
	private String reasonCode;

	public DTXNDataBean() {
		// TODO Auto-generated constructor stub
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
	
	public String getTxnMon() {
		return txnMon;
	}

	public void setTxnMon(String txnMon) {
		this.txnMon = txnMon;
	}

	public int getTtlCnt() {
		return ttlCnt;
	}

	public void setTtlCnt(int ttlCnt) {
		this.ttlCnt = ttlCnt;
	}

	public double getTtlAmt() {
		return ttlAmt;
	}

	public void setTtlAmt(double ttlAmt) {
		this.ttlAmt = ttlAmt;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	
	@Override
	public String toString() {
		return "DTXNDataBean [cardNo=" + cardNo + ", expiryDate=" + expiryDate
				+ ", txnMon=" + txnMon + ", ttlCnt=" + ttlCnt + ", ttlAmt="
				+ ttlAmt + ", reasonCode=" + reasonCode + "]";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
