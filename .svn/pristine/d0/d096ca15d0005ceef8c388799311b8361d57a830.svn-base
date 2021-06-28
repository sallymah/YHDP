package tw.com.hyweb.svc.yhdp.batch.summary.computeCdrp;

import java.util.HashMap;
import java.util.Map;

import tw.com.hyweb.util.date.DateUtil;

public class ComputeCdrpData {
	
	private String memId = "";
	private String bankId = "";
	private int lostCardRespDay = 0;
	private int stopCardRespDay = 0;
	private int expiryCardRespDay = 0;
	private int genRdclDay = 0;
	
	private String lostCardRespDate = "";
	private String stopCardRespDate = "";
	private String expiryCardRespDate = "";
	private String genRdclDate = "";
	
	private Map<String, String> typ2Date = new HashMap<String, String>();
	
	public ComputeCdrpData(Map<String, String> resultMap) {
		// TODO Auto-generated constructor stub
		this.memId = resultMap.get("MEM_ID").toString();
		this.bankId = resultMap.get("BANK_ID").toString();
		this.lostCardRespDay = Integer.valueOf(resultMap.get("LOST_CARD_RESP_DAY").toString());
		this.stopCardRespDay = Integer.valueOf(resultMap.get("STOP_CARD_RESP_DAY").toString());
		this.expiryCardRespDay = Integer.valueOf(resultMap.get("EXPIRY_CARD_RESP_DAY").toString());
		this.genRdclDay = Integer.valueOf(resultMap.get("GEN_RDCL_DAY").toString());
	}
	
	public void calculateTheDate(String batchDate) {
		// TODO Auto-generated method stub
		lostCardRespDate = DateUtil.addDate(batchDate, -1*lostCardRespDay);
		stopCardRespDate = DateUtil.addDate(batchDate, -1*stopCardRespDay);
		expiryCardRespDate = DateUtil.addDate(batchDate, -1*expiryCardRespDay);
		genRdclDate = DateUtil.addDate(batchDate, -1*genRdclDay);
		
		// 1: 掛卡, 2: 停卡, 3: 到期卡, 4: 關閉自動加值
		typ2Date.put("1", lostCardRespDate);
		typ2Date.put("2", stopCardRespDate);
		typ2Date.put("3", expiryCardRespDate);
		typ2Date.put("4", genRdclDate);
	}

	public String getMemId() {
		return memId;
	}

	public void setMemId(String memId) {
		this.memId = memId;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public int getLostCardRespDay() {
		return lostCardRespDay;
	}

	public void setLostCardRespDay(int lostCardRespDay) {
		this.lostCardRespDay = lostCardRespDay;
	}

	public int getStopCardRespDay() {
		return stopCardRespDay;
	}

	public void setStopCardRespDay(int stopCardRespDay) {
		this.stopCardRespDay = stopCardRespDay;
	}

	public int getExpiryCardRespDay() {
		return expiryCardRespDay;
	}

	public void setExpiryCardRespDay(int expiryCardRespDay) {
		this.expiryCardRespDay = expiryCardRespDay;
	}

	public int getGenRdclDay() {
		return genRdclDay;
	}

	public void setGenRdclDay(int genRdclDay) {
		this.genRdclDay = genRdclDay;
	}
	
	public String getLostCardRespDate() {
		return lostCardRespDate;
	}

	public void setLostCardRespDate(String lostCardRespDate) {
		this.lostCardRespDate = lostCardRespDate;
	}

	public String getStopCardRespDate() {
		return stopCardRespDate;
	}

	public void setStopCardRespDate(String stopCardRespDate) {
		this.stopCardRespDate = stopCardRespDate;
	}

	public String getExpiryCardRespDate() {
		return expiryCardRespDate;
	}

	public void setExpiryCardRespDate(String expiryCardRespDate) {
		this.expiryCardRespDate = expiryCardRespDate;
	}

	public String getGenRdclDate() {
		return genRdclDate;
	}

	public void setGenRdclDate(String genRdclDate) {
		this.genRdclDate = genRdclDate;
	}
	
	public Map<String, String> getTyp2Date() {
		return typ2Date;
	}

	public void setTyp2Date(Map<String, String> typ2Date) {
		this.typ2Date = typ2Date;
	}

	public String toString() {
		return  "\n" + 
				"memId ["+ memId + "]" +
				", bankId ["+ bankId + "]\n" +
				"lostCardRespDay ["+ lostCardRespDay + "]" +
				", lostCardRespDate ["+ lostCardRespDate + "]\n" +
				"stopCardRespDay ["+ stopCardRespDay + "]" +
				", stopCardRespDate ["+ stopCardRespDate + "]\n" +
				"expiryCardRespDay ["+ expiryCardRespDay + "]" +
				", expiryCardRespDate ["+ expiryCardRespDate + "]\n" +
				"genRdclDay ["+ genRdclDay + "]" + 
				", genRdclDate ["+ genRdclDate + "]";
	}
	
}
