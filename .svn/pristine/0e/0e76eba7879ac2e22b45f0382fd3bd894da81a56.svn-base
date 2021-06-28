package tw.com.hyweb.iff;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.iff.impfiles.YhdpBlackListSettingDataGenerator;
import tw.com.hyweb.iff.impfiles.YhdpCardDataGenerator;
import tw.com.hyweb.iff.impfiles.YhdpCpDeliveryDataGenerator;
import tw.com.hyweb.iff.impfiles.YhdpPersoFeebackDataCheck;
import tw.com.hyweb.iff.impfiles.YhdpPersoFeedBackBean;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbIffFeedbackDtlInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.info.TbTelcoCardDtlInfo;
import tw.com.hyweb.service.db.info.TbTelcoCardInfo;
import tw.com.hyweb.util.DbUtil;

public class IFFaction {
	
	private static Logger log = Logger.getLogger(IFFaction.class);
	
	private static String sysDate = DateUtils.getSystemDate();
    private static String sysTime = DateUtils.getSystemTime();
		
	public static String[] IffFeedback(String persoBatchNo, String dataLine, Connection conn){
		
		sysDate = DateUtils.getSystemDate();
	    sysTime = DateUtils.getSystemTime();
		
		String[] retrueData = new String[3];
		
		String rCode = Constants.RCODE_0000_OK;
		
		YhdpPersoFeebackDataCheck check = null;
		IFFUtils iffUtil = null;
		
		try {
			iffUtil = new IFFUtils();
			iffUtil.initial("OTAIFF");
			
			if (iffUtil.getMappingInfo().getDataLength() == dataLine.length()){
				YhdpPersoFeedBackBean dataBean = iffUtil.organizeRowData(dataLine);
				retrueData[1] = dataBean.getCardNo();
				
				if(checkCardNo(dataBean.getCardNo(), conn))
				{
				    retrueData[2] = "Duplicate Card No:" + dataBean.getCardNo();
	                log.error(retrueData[2]);
	                rCode = Constants.RCODE_1001_SQL_ERR;
				}
				else {
    				check = new YhdpPersoFeebackDataCheck();
    				TbPersoInfo persoInfo = check.checkIff(conn, persoBatchNo, dataLine, dataBean);
    				if(check.getRcode().equals(Constants.RCODE_0000_OK)){
    					handleDataLine(conn, dataBean, persoInfo);
    					updateIffFeeback(conn, persoInfo.getPersoBatchNo());
    				} else {
    				    retrueData[2] = check.getErrDesc();
    				}
    				rCode = check.getRcode();
				}
			}
			else{
			    retrueData[2] = "dataLine length: " + dataLine.length() + "!= " + iffUtil.getMappingInfo().getDataLength();
				log.error(retrueData[2]);
				rCode = Constants.RCODE_2702_DIVIDE_ERR;
			}
			
		}
		catch (SQLException e) {
			log.error("",e);
			if(check != null && !check.getRcode().equals(Constants.RCODE_0000_OK)){
				rCode = check.getRcode();
			}
			rCode = Constants.RCODE_1001_SQL_ERR;
			retrueData[2] = e.getMessage();
		}
		catch (Exception e) {
			log.error("",e);
			if(check != null && !check.getRcode().equals(Constants.RCODE_0000_OK)){
				rCode = check.getRcode();
			}
			rCode = Constants.RCODE_2791_GENERALDB_ERR;
			retrueData[2] = e.getMessage();
		}
		finally{
			retrueData[0]= rCode;
		}
		
		return retrueData;
	}
		
	public static boolean checkCardNo(String cardNo, Connection conn) throws SQLException
	{
	    String sqlCmd ="select count(1) from tb_card where card_no = ?";
	    Vector<String> params =  new Vector<String>();
	    params.add(cardNo);
	    return DbUtil.getInteger(sqlCmd, params, conn) > 0 ? true: false;
	}
	
	public static void handleDataLine(Connection conn, YhdpPersoFeedBackBean dataBean, TbPersoInfo persoInfo) throws Exception
    {		  	
		//OTA_電信卡
		//測試卡
		if(persoInfo.getIsTest().equals("1")) {
			new YhdpBlackListSettingDataGenerator().makeBlackListSetgingData(conn, sysDate, sysTime, dataBean.getHm().get("field01"), persoInfo);
		}
		
		inertIffFeedbackDtlData(conn, dataBean, persoInfo);
		markTelcoCard(conn, dataBean, persoInfo);
		
    	YhdpCardDataGenerator.makeCardData(conn, sysDate, dataBean.getHm(), persoInfo);
    	YhdpCpDeliveryDataGenerator.makeCpDeliveryData(conn, sysDate, dataBean.getHm(), persoInfo);
    	updateHgCardMap(conn, dataBean.getHm());
		
    }
	
	private static void updateHgCardMap(Connection conn, HashMap<String, String> hm) throws Exception 
    {
    	String cardNo = hm.get("field01");
    	log.info("2.儲值檔識別碼(PID): " + cardNo);
    	
    	String hexBarcode1 = hm.get("field20");
		String barcode1 = IFFUtils.hex2ascii(hexBarcode1);
		log.info("barcode1: " + barcode1);
    	
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE TB_HG_CARD_MAP set");
		sql.append(" CARD_NO='").append(cardNo).append("'");
		sql.append(", STATUS='").append("1").append("'");
		sql.append(" WHERE BARCODE1='").append(barcode1).append("'");
		
		DBService.getDBService().sqlAction(sql.toString(), conn, false);
	}
	
	private static void updateIffFeeback(Connection conn, String persoBatchNo) throws Exception 
    {
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE TB_IFF_FEEDBACK set");
		sql.append(" IFF_QTY= IFF_QTY + 1");
		sql.append(" WHERE PERSO_BATCH_NO='").append(persoBatchNo).append("'");
		
		DBService.getDBService().sqlAction(sql.toString(), conn, false);
	}
	
	public static void inertIffFeedbackDtlData(Connection conn, YhdpPersoFeedBackBean dataBean, TbPersoInfo persoInfo) throws Exception 
	{
		TbIffFeedbackDtlInfo iffFeedbackDtlinfo = new TbIffFeedbackDtlInfo();
		
		iffFeedbackDtlinfo.setCardNo(dataBean.getCardNo());
		
		String utExpiryDate = dataBean.getExpiryDate();
		long hexUtExpiryDate = Long.parseLong(utExpiryDate, 16);
		String expiryDate = new SimpleDateFormat("yyyyMMdd").format(new Date(Long.valueOf(hexUtExpiryDate) * 1000L));
		log.debug("Expiry Date: " + expiryDate);
		iffFeedbackDtlinfo.setExpiryDate(expiryDate);
		
		iffFeedbackDtlinfo.setIffFileName("OTA_" + persoInfo.getPersoBatchNo());
		iffFeedbackDtlinfo.setIffFileProcDate(sysDate);
		iffFeedbackDtlinfo.setIffFileProcTime(sysTime);
		
		DBService.getDBService().sqlAction(iffFeedbackDtlinfo.toInsertSQL(), conn, false);
	}
	
	private static void markTelcoCard(Connection conn, YhdpPersoFeedBackBean dataBean, TbPersoInfo persoInfo) throws Exception 
	{
		Statement stmt = conn.createStatement();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(" UPDATE TB_TELCO_CARD SET");
		sb.append(" QTY = QTY + 1");
		sb.append(" WHERE PERSO_BATCH_NO ='").append(persoInfo.getPersoBatchNo()).append("'");
		
		int updateCount = stmt.executeUpdate(sb.toString());
		
		if (updateCount < 1){
			TbTelcoCardInfo info = new TbTelcoCardInfo();
			info.setImpTelcoFileName("OTA_" + persoInfo.getPersoBatchNo());
			info.setQty(1);
			info.setImpDate(sysDate);
			info.setImpTime(sysTime);
			info.setPersoBatchNo(persoInfo.getPersoBatchNo());
			
			DBService.getDBService().sqlAction(info.toInsertSQL(), conn, false);
		}
		
		
		TbTelcoCardDtlInfo dtlInfo = new TbTelcoCardDtlInfo();
		dtlInfo.setImpTelcoFileName("OTA_" + persoInfo.getPersoBatchNo());
		dtlInfo.setIccid(dataBean.getIccId());
		dtlInfo.setMnoCardNo(dataBean.getMnoCardNo());
		dtlInfo.setMifareUlUid(dataBean.getMifareId());
		dtlInfo.setIffFileName("OTA_" + persoInfo.getPersoBatchNo());
		dtlInfo.setIffFileProcDate(sysDate);
		dtlInfo.setIffFileProcTime(sysTime);
		dtlInfo.setCardNo(dataBean.getCardNo());
		dtlInfo.setExpiryDate(dataBean.getExpiryDate());
		
		DBService.getDBService().sqlAction(dtlInfo.toInsertSQL(), conn, false);
	}
}
