package tw.com.hyweb.svc.yhdp.batch.AutoSettleAccount;

public class TransferOlnTxn {
    private String merchId;
    private String termId;
    private String batchNo;
    private String count;
    private String sumTxnAmt;
    private String termSettleDate;
    private String termSettleTime;
    
	public TransferOlnTxn( String merchId, String termId, String batchNo, String count, String sumTxnAmt ) {
		
		this.merchId = merchId;
		this.termId = termId;
		this.batchNo = batchNo;
		this.count = count;
		this.sumTxnAmt = sumTxnAmt;
	
	}
	
	public TransferOlnTxn() {
		
	}
	
	public String getMerchId() {
		return merchId;
	}
	public void setMerchId(String merchId) {
		this.merchId = merchId;
	}
	public String getTermId() {
		return termId;
	}
	public void setTermId(String termId) {
		this.termId = termId;
	}
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getSumTxnAmt() {
		return sumTxnAmt;
	}
	public void setSumTxnAmt(String sumTxnAmt) {
		this.sumTxnAmt = sumTxnAmt;
	}

	public String getTermSettleDate() {
		return termSettleDate;
	}

	public void setTermSettleDate(String termSettleDate) {
		this.termSettleDate = termSettleDate;
	}

	public String getTermSettleTime() {
		return termSettleTime;
	}

	public void setTermSettleTime(String termSettleTime) {
		this.termSettleTime = termSettleTime;
	}
	
	

}
