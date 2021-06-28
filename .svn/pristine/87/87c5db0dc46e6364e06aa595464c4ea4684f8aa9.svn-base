package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import org.apache.log4j.Logger;

import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;


public class FF21
{
    private static final Logger logger = Logger.getLogger(FF21.class);
	public String respCode;
	public String tid;
	public String mid;
	public String pcode;
	public String batchNo;
	public String issNo;
	public String storeCntArea;
	public String iccAtc;
	public String termDateTime;
	public String hostDateTime;
	public String ecashAmt;
	public String lmsAmt;
	public String samArea;
	public String chipCardSts;
	public String accessMod;
	public String ecashBeforeBal;
	
	public FF21(String str){
		if (str!=null && str.length()>=190){
			int idx = 0;
			String rawRespCode = str.substring(idx, idx+=4);
			if(rawRespCode.equals("00") || rawRespCode.equals("0000"))
            {
                respCode = rawRespCode;
            }
			else if(rawRespCode.substring(0, 2).equals("01"))
			{
			    respCode = rawRespCode;
			    logger.debug("respCode:"+respCode);
			}
			else if(rawRespCode.substring(0, 2).equals("02"))
            {
                respCode = rawRespCode;
                logger.debug("respCode:"+respCode);
            }
			else
			{
			    logger.debug("rawRespCode:"+rawRespCode);
                respCode = new String(ISOUtil.hex2byte(rawRespCode));
                logger.debug("respCode:"+respCode);
			    if(respCode.equals("00") || respCode.equals("0000"))
			    {
			        respCode = ISOUtil.padRight(respCode, 4, '0');
			    }
			    else if(!StringUtil.isEmpty(respCode) && respCode.length() == 2)
			    {
			        respCode = "80" + respCode;
			    }
			    
/*			    if(respCode.equals("80E1"))
			        respCode = "0000";*/
			}
			tid = new String(ISOUtil.hex2byte(str.substring(idx, idx+=16)));
			mid = new String(ISOUtil.hex2byte(str.substring(idx, idx+=30)));
			pcode = str.substring(idx, idx+=4);
			batchNo = str.substring(idx, idx+=6);
			issNo = str.substring(idx, idx+=8);
			storeCntArea = new String(ISOUtil.hex2byte(str.substring(idx, idx+=36)));
			iccAtc = str.substring(idx, idx+=8);
			termDateTime = str.substring(idx, idx+=14);
			hostDateTime = str.substring(idx, idx+=14);
			ecashAmt = str.substring(idx, idx+=10);
			lmsAmt = str.substring(idx, idx+=10);
			samArea = str.substring(idx, idx+=16);
			chipCardSts = new String(ISOUtil.hex2byte(str.substring(idx, idx+=4)));
			accessMod = new String(ISOUtil.hex2byte(str.substring(idx, idx+=2)));
			ecashBeforeBal = str.substring(idx, idx+=10);
		}
	}
	
	public String pack(){
		return ISOUtil.hexString(respCode.getBytes())+ISOUtil.hexString(tid.getBytes())+ISOUtil.hexString(mid.getBytes())+pcode+batchNo+issNo+ISOUtil.hexString(storeCntArea.getBytes())+iccAtc+termDateTime+hostDateTime+ecashAmt+lmsAmt+samArea+ISOUtil.hexString(chipCardSts.getBytes())+ISOUtil.hexString(accessMod.getBytes())+ecashBeforeBal;
	}

	public String getRespCode() {
		return respCode;
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
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

	public String getStoreCntArea() {
		return storeCntArea;
	}

	public void setStoreCntArea(String storeCntArea) {
		this.storeCntArea = storeCntArea;
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

	public String getHostDateTime() {
		return hostDateTime;
	}

	public void setHostDateTime(String hostDateTime) {
		this.hostDateTime = hostDateTime;
	}

	public String getEcashAmt() {
		return ecashAmt;
	}

	public void setEcashAmt(String ecashAmt) {
		this.ecashAmt = ecashAmt;
	}

	public String getLmsAmt() {
		return lmsAmt;
	}

	public void setLmsAmt(String lmsAmt) {
		this.lmsAmt = lmsAmt;
	}

	public String getSamArea() {
		return samArea;
	}

	public void setSamArea(String samArea) {
		this.samArea = samArea;
	}

	public String getChipCardSts() {
		return chipCardSts;
	}

	public void setChipCardSts(String chipCardSts) {
		this.chipCardSts = chipCardSts;
	}

	public String getAccessMod() {
		return accessMod;
	}

	public void setAccessMod(String accessMod) {
		this.accessMod = accessMod;
	}

	public String getEcashBeforeBal() {
		return ecashBeforeBal;
	}

	public void setEcashBeforeBal(String ecashBeforeBal) {
		this.ecashBeforeBal = ecashBeforeBal;
	}

	@Override
	public String toString() {
		return "FF21 [accessMod=" + accessMod + ", batchNo=" + batchNo
				+ ", chipCardSts=" + chipCardSts + ", ecashAmt=" + ecashAmt
				+ ", ecashBeforeBal=" + ecashBeforeBal + ", hostDateTime="
				+ hostDateTime + ", iccAtc=" + iccAtc + ", issNo=" + issNo
				+ ", lmsAmt=" + lmsAmt + ", mid=" + mid + ", pcode=" + pcode
				+ ", respCode=" + respCode + ", samArea=" + samArea
				+ ", storeCntArea=" + storeCntArea + ", termDateTime="
				+ termDateTime + ", tid=" + tid + "]";
	}
	
	public static void main(String[] args){
		String ff21 = "30303831333032303031383133303039393939393030303032737730303030303030303030303030303030303000000000000000000000000002034AFE00054D240000000000";
		FF21 off21 = new FF21(ff21);
		System.out.println(off21.getBatchNo());
	}
}
