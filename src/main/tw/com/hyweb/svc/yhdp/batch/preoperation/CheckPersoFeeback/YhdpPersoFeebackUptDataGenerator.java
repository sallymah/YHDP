package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback;

import java.sql.Connection;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbPersoFeedbackUptInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbPersoFeedbackUptMgr;

public class YhdpPersoFeebackUptDataGenerator {
	
	private static final Logger LOGGER = Logger.getLogger(CheckPersoFeebackJob.class);
	
	private final String sysDate = DateUtils.getSystemDate();
	private final String sysTime = DateUtils.getSystemTime();

	public YhdpPersoFeebackUptDataGenerator() {
		
	}
	
	public void addPersoFeedbackUptData(Connection conn, TbInctlInfo inctlInfo, YhdpPersoFeebackDataCheck dataChecker) throws Exception 
	{
		TbPersoInfo info = dataChecker.getPersoInfo();
		TbPersoFeedbackUptInfo persoFeedbackUptinfo = new TbPersoFeedbackUptInfo();
		
		persoFeedbackUptinfo.setMemId(inctlInfo.getMemId());
		persoFeedbackUptinfo.setPersoBatchNo(info.getPersoBatchNo());
		persoFeedbackUptinfo.setIffFileName(inctlInfo.getFullFileName());
		persoFeedbackUptinfo.setPersoQty(info.getPersoQty());
		persoFeedbackUptinfo.setIffQty(dataChecker.getRowDataCount());
		persoFeedbackUptinfo.setCardTypeId(info.getCardTypeId());
		persoFeedbackUptinfo.setCardCatId(info.getCardCatId());
		persoFeedbackUptinfo.setCardProduct(info.getCardProduct());
		persoFeedbackUptinfo.setFeedbackRcode(dataChecker.getRcode());
		persoFeedbackUptinfo.setIffFileProcDate(sysDate);
		persoFeedbackUptinfo.setIffFileProcTime(sysTime);
		persoFeedbackUptinfo.setUptUserid("BATCH");
		persoFeedbackUptinfo.setUptDate(sysDate);
		persoFeedbackUptinfo.setUptTime(sysTime);
		persoFeedbackUptinfo.setUptStatus("1");
		persoFeedbackUptinfo.setAprvStatus("0");
		
		TbPersoFeedbackUptMgr mgr = new TbPersoFeedbackUptMgr(conn);
		mgr.insert(persoFeedbackUptinfo);
	}
}
