package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;


public class FF11
{
	public String stan="";
	public String cardNo="";
	public String cardExp="";
	public String invoiceNo="";
	public String afterBal="";
	
	public FF11(String str){
		if (str!=null && str.length()>=56){
			int idx=0;
			stan = str.substring(idx, idx+=6);
			cardNo = str.substring(idx, idx+=20);
			cardExp = str.substring(idx, idx+=8);
			invoiceNo = str.substring(idx, idx+=12);
			afterBal = str.substring(idx, idx+=10);
		}
	}
	
	public String pack(){
		return stan+cardNo+cardExp+invoiceNo+afterBal;
	}

	public String getStan() {
		return stan;
	}

	public void setStan(String stan) {
		this.stan = stan;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCardExp() {
		return cardExp;
	}

	public void setCardExp(String cardExp) {
		this.cardExp = cardExp;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getAfterBal() {
		return afterBal;
	}

	public void setAfterBal(String afterBal) {
		this.afterBal = afterBal;
	}

	@Override
	public String toString() {
		return "FF11 [afterBal=" + afterBal + ", cardExp=" + cardExp
				+ ", cardNo=" + cardNo + ", invoiceNo=" + invoiceNo + ", stan="
				+ stan + "]";
	}
	
}
