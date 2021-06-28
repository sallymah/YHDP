package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl;

import tw.com.hyweb.util.ISOUtil;


public class FF3C
{
	public String pcode;
	public String batchNo;
	public String issNo;
	public String cardNo;
	public String cardExp;
	public String iccAtc;
	public String termDateTime;
	public String lmsAmt;
	public String invoiceNo;
	public String accessMod;
	public String txnType;
	public String oriInvoiceNo;
	public String tac;
	public String purchaseType;
	public String updAwdCoupon;
	public String updAwdPoint;
	public String updChipPointTxn;
	public String updChipPointBal;
	public String updChipCouponTxn;
	public String updChipPointRsp;
	public String updChipCouponRsp;
	public String updChipCouponBal;
	
	public FF3C(String str){
		if (str!=null && str.length()>=140){
			int idx=0;
			pcode = str.substring(idx, idx+=4);
			batchNo = str.substring(idx, idx+=6);
			issNo = str.substring(idx, idx+=8);
			cardNo = str.substring(idx, idx+=20);
			cardExp = str.substring(idx, idx+=8);
			iccAtc = str.substring(idx, idx+=8);
			termDateTime = str.substring(idx, idx+=14);
			lmsAmt = str.substring(idx, idx+=10);
			invoiceNo = str.substring(idx, idx+=12);
			//accessMod = str.substring(idx, idx+=2);
			accessMod = new String(ISOUtil.hex2byte(str.substring(idx, idx+=2)));
			txnType = str.substring(idx, idx+=2);//00: online 交易	01: offline 交易
			oriInvoiceNo = str.substring(idx, idx+=12);
			tac = str.substring(idx, idx+=16);//8 byte
			purchaseType = str.substring(idx, idx+=2);//01: 第一類 02: 第二類
			updAwdCoupon = str.substring(idx, idx+=2);//FF6E (from batch FF6B)
			updAwdPoint = str.substring(idx, idx+=2);//FF6F  (from batch FF6C)
			updChipPointTxn = str.substring(idx, idx+=2);//FF49  (from batch FF36)
			updChipPointBal = str.substring(idx, idx+=2);//FF4A  (from batch FF38 FF44) 
			updChipCouponTxn = str.substring(idx, idx+=2);//FF4B   (from batch FF37)
			updChipPointRsp = str.substring(idx, idx+=2);//FF4D  (from batch FF3A)
			updChipCouponRsp = str.substring(idx, idx+=2);//FF4F  (from batch FF3B)
			updChipCouponBal = str.substring(idx, idx+=2);//FF5A  (from batch FF39 FF41)
		}
	}
	
	public String pack(){
		return pcode+batchNo+issNo+cardNo+cardExp+iccAtc+termDateTime+lmsAmt+invoiceNo+ISOUtil.hexString(accessMod.getBytes())+txnType+oriInvoiceNo+tac+purchaseType+updAwdCoupon+updAwdPoint+updChipPointTxn+updChipPointBal+updChipCouponTxn+updChipPointRsp+updChipCouponRsp+updChipCouponBal;
	}

	public String getPcode() {
		return pcode;
	}

	public void setPcode(String pcode) {
		this.pcode = pcode;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getIssNo() {
		return issNo;
	}

	public void setIssNo(String issNo) {
		this.issNo = issNo;
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

	public String getIccAtc() {
		return iccAtc;
	}

	public void setIccAtc(String iccAtc) {
		this.iccAtc = iccAtc;
	}

	public String getTermDateTime() {
		return termDateTime;
	}

	public void setTermDateTime(String termDateTime) {
		this.termDateTime = termDateTime;
	}

	public String getLmsAmt() {
		return lmsAmt;
	}

	public void setLmsAmt(String lmsAmt) {
		this.lmsAmt = lmsAmt;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getAccessMod() {
		return accessMod;
	}

	public void setAccessMod(String accessMod) {
		this.accessMod = accessMod;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getOriInvoiceNo() {
		return oriInvoiceNo;
	}

	public void setOriInvoiceNo(String oriInvoiceNo) {
		this.oriInvoiceNo = oriInvoiceNo;
	}

	public String getTac() {
		return tac;
	}

	public void setTac(String tac) {
		this.tac = tac;
	}

	public String getPurchaseType() {
		return purchaseType;
	}

	public void setPurchaseType(String purchaseType) {
		this.purchaseType = purchaseType;
	}

	public String getUpdAwdCoupon() {
		return updAwdCoupon;
	}

	public void setUpdAwdCoupon(String updAwdCoupon) {
		this.updAwdCoupon = updAwdCoupon;
	}

	public String getUpdAwdPoint() {
		return updAwdPoint;
	}

	public void setUpdAwdPoint(String updAwdPoint) {
		this.updAwdPoint = updAwdPoint;
	}

	public String getUpdChipPointTxn() {
		return updChipPointTxn;
	}

	public void setUpdChipPointTxn(String updChipPointTxn) {
		this.updChipPointTxn = updChipPointTxn;
	}

	public String getUpdChipPointBal() {
		return updChipPointBal;
	}

	public void setUpdChipPointBal(String updChipPointBal) {
		this.updChipPointBal = updChipPointBal;
	}

	public String getUpdChipCouponTxn() {
		return updChipCouponTxn;
	}

	public void setUpdChipCouponTxn(String updChipCouponTxn) {
		this.updChipCouponTxn = updChipCouponTxn;
	}

	public String getUpdChipPointRsp() {
		return updChipPointRsp;
	}

	public void setUpdChipPointRsp(String updChipPointRsp) {
		this.updChipPointRsp = updChipPointRsp;
	}

	public String getUpdChipCouponRsp() {
		return updChipCouponRsp;
	}

	public void setUpdChipCouponRsp(String updChipCouponRsp) {
		this.updChipCouponRsp = updChipCouponRsp;
	}

	public String getUpdChipCouponBal() {
		return updChipCouponBal;
	}

	public void setUpdChipCouponBal(String updChipCouponBal) {
		this.updChipCouponBal = updChipCouponBal;
	}

	@Override
	public String toString() {
		return "FF3C [accessMod=" + accessMod + ", batchNo=" + batchNo
				+ ", cardExp=" + cardExp + ", cardNo=" + cardNo + ", iccAtc="
				+ iccAtc + ", invoiceNo=" + invoiceNo + ", issNo=" + issNo
				+ ", lmsAmt=" + lmsAmt + ", oriInvoiceNo=" + oriInvoiceNo
				+ ", pcode=" + pcode + ", purchaseType=" + purchaseType
				+ ", tac=" + tac + ", termDateTime=" + termDateTime
				+ ", txnType=" + txnType + ", updAwdCoupon=" + updAwdCoupon
				+ ", updAwdPoint=" + updAwdPoint + ", updChipCouponBal="
				+ updChipCouponBal + ", updChipCouponRsp=" + updChipCouponRsp
				+ ", updChipCouponTxn=" + updChipCouponTxn
				+ ", updChipPointBal=" + updChipPointBal + ", updChipPointRsp="
				+ updChipPointRsp + ", updChipPointTxn=" + updChipPointTxn
				+ "]";
	}
	
}
