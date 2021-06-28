package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbIffFeedbackDtlInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbPersoFeedbackUptInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbIffFeedbackDtlMgr;
import tw.com.hyweb.service.db.mgr.TbPersoFeedbackUptMgr;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.bean.YhdpPersoFeedBackBean;

public class YhdpPersoFeebackDataGenerator {
	
	private static final Logger LOGGER = Logger.getLogger(YhdpPersoFeebackDataGenerator.class);
	
	public final static String sysDate = DateUtils.getSystemDate();
	public final static String sysTime = DateUtils.getSystemTime();
	
	public YhdpPersoFeebackDataGenerator() 
	{

	}

	/*public YhdpPersoFeebackDataGenerator(Connection conn, TbInctlInfo inctlInfo, YhdpPersoFeebackDataCheck check, String cardNo) throws Exception 
	{
		try {
			addPersoFeedbackUptData(conn, inctlInfo, check);
			
		} catch (Exception e) {
			LOGGER.error("generate PersoFeedbackUptData fail !");
			throw new Exception(e);
		}

		if(check.getPersoInfo().getPersoType().equals("3")) 
		{
			try {
				addIffFeedbackDtlData(conn, inctlInfo, cardNo);
			} catch (Exception e) {
				LOGGER.error("generate IffFeedbackDtlData fail !");
				throw new Exception(e);
			}
		}
	}*/

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
	
	public static String addIffFeedbackDtlData(TbInctlInfo inctlInfo, YhdpPersoFeedBackBean dataBean) throws Exception 
	{
		TbIffFeedbackDtlInfo iffFeedbackDtlinfo = new TbIffFeedbackDtlInfo();
		
		iffFeedbackDtlinfo.setCardNo(dataBean.getCardNo());
		
		String utExpiryDate = dataBean.getExpiryDate();
		long hexUtExpiryDate = Long.parseLong(utExpiryDate, 16);
		String expiryDate = new SimpleDateFormat("yyyyMMdd").format(new Date(Long.valueOf(hexUtExpiryDate) * 1000L));
		LOGGER.debug("Expiry Date: " + expiryDate);
		iffFeedbackDtlinfo.setExpiryDate(expiryDate);
		
		iffFeedbackDtlinfo.setIffFileName(inctlInfo.getFullFileName());
		iffFeedbackDtlinfo.setIffFileProcDate(sysDate);
		iffFeedbackDtlinfo.setIffFileProcTime(sysTime);
		
		return iffFeedbackDtlinfo.toInsertSQL();
	}
}
