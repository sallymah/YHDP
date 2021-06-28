package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.util.ISOUtil;


public class FF11
{
	public String stan="";
	public String cardNo="";
	public String cardExp="";
	public String invoiceNo="";
	public String afterBal="";
	public String pcode;
	
	public FF11(){
	}
	
	public FF11(int len , String str){
		if (str!=null && str.length()>=len){
			int idx=0;
			stan = str.substring(idx, idx+=6);
			cardNo = str.substring(idx, idx+=20);
			cardExp = str.substring(idx, idx+=8);
			invoiceNo = str.substring(idx, idx+=12);
			afterBal = str.substring(idx, idx+=10);
			pcode = str.substring(idx, idx+=4);
		}
	}
	
	public String pack(){
		return stan+cardNo+cardExp+invoiceNo+afterBal+pcode;
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

    public String getPcode() {
        return pcode;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
    }


	@Override
	public String toString() {
		return "FF11 [afterBal=" + afterBal + ", cardExp=" + cardExp
				+ ", cardNo=" + cardNo + ", invoiceNo=" + invoiceNo + ", stan="
				+ stan  + ", pcode=" + pcode + "]";
	}
	
	
	public static void main(String[] args){
		
		String parserStr = "FF111E0000059851422336844805FFFF2099123101181130450500003650007707FF2167303039323031323130303030303739303238303030303133390000010000100120202020202020202020202020202020202000000057201601181130452016011811304500001000000000000000FFFFFFFFFFFFFFFF30334C0000265000000000000000000000FF500430303030FF64080000000000000000";
		System.out.println("parserStr: "+parserStr);
		BerTLV berTLV = BerTLV.createInstance(ISOUtil.hex2byte(parserStr));
		
		System.out.println(""+berTLV);
		
		FF11 off11 = new FF11(MsgUtil.FF11_SIZE, berTLV.getHexStr(0xFF11));
		System.out.println(off11.toString());
		
		FF21 off21 = new FF21(MsgUtil.FF21_SIZE, berTLV.getHexStr(0xFF21));
		System.out.println(off21.toString());
		
		
	}
}
