package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.util.StringUtils;
import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbHgCardMapInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbHgCardMapMgr;
import tw.com.hyweb.service.db.mgr.TbIffFeedbackDtlMgr;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.bean.YhdpPersoFeedBackBean;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.IFFUtils;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;

public class YhdpPersoFeebackDataCheck 
{
	private static final Logger LOGGER = Logger.getLogger(CheckPersoFeebackJob.class);

	private final int DATA_MAX_LENGTH = 5888;

	private String rcode;

    private int rowDataCount;
    
    private TbPersoInfo persoInfo;
    
    ArrayList<String> errorSqlList = new ArrayList<>();

	public YhdpPersoFeebackDataCheck() {}
	
	public void basicChecker(Connection conn, TbInctlInfo inctlInfo, String file)  throws Exception
	{
		LOGGER.debug("file: "+ file);
		
		rcode = Constants.RCODE_0000_OK;
		
		if(!new File(file).exists()) {
			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2701_NOTEXIST, 
					"IFF file(" + file + ") is not exist !");
			rcode = Constants.RCODE_2701_NOTEXIST;
		}
		
		if(checkDataHeader(conn, inctlInfo, file)) {
			rcode = Constants.RCODE_2710_INVALID_ERR;
		}
		
		if(persoInfo.getChipVersion().equals("1")) {
    		if(inctlInfo.getFullFileName().indexOf(".1.") == -1){
    			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
    					"IFF file(" + inctlInfo.getFullFileName() + ") does not match(version: " + persoInfo.getChipVersion() + ") !");
    			rcode = Constants.RCODE_2710_INVALID_ERR;
    		}
    	}
    	else {
    		if(inctlInfo.getFullFileName().indexOf(".1.") != -1){
    			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
    					"IFF2 file(" + inctlInfo.getFullFileName() + ") does not match(version: " + persoInfo.getChipVersion() + ") !");
    			rcode = Constants.RCODE_2710_INVALID_ERR;
    		}
    	}
		
		/*if(isExistPersoSetting(conn)) {
			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2705_NODATA, 
					"tb_perso_setting no data found");
			rcode = Constants.RCODE_2705_NODATA;
		}*/
		
		if(persoInfo.getPersoType().equals("4")) {
			if(checkDataForPhoneCard(conn, persoInfo.getPersoBatchNo())) {
				IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2716_DATACOUNT_ERR, 
						"tb_telco_card or tb_telco_card_dtl has no data !");
				rcode = Constants.RCODE_2716_DATACOUNT_ERR;
			}
		}
		
		//確認是否要檢查
		if(persoInfo.getPersoType().equals("3") || persoInfo.getPersoType().equals("4")) {
			if(persoInfo.getPersoType().equals("3")) {
				if(checkPersoCountConsisWithIffForBanking(conn, persoInfo.getPersoBatchNo())) {
					IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2716_DATACOUNT_ERR, 
							"perso(PERSO_TYPE_BANK_CARD) count(" + persoInfo.getPersoQty() + ") < iff feedback count(" + rowDataCount + ") !");
					rcode = Constants.RCODE_2716_DATACOUNT_ERR;
				}
			}
			else {
				if(checkPersoCountConsisWithIffForBanking(conn, persoInfo.getPersoBatchNo())) {
					IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2716_DATACOUNT_ERR, 
							"perso(PERSO_TYPE_PHONE_CARD) count(" + persoInfo.getPersoQty() + ") < iff feedback count(" + rowDataCount + ") !");
					rcode = Constants.RCODE_2716_DATACOUNT_ERR;
				}
			}
			
		}
		else {
			if(checkPersoCountConsisWithIFF(conn, persoInfo.getPersoBatchNo())){
				IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2716_DATACOUNT_ERR, 
						"perso(PERSO_TYPE_STANDARD_CARD) count(" + persoInfo.getPersoQty() + ") != iff feedback count(" + rowDataCount + ") !");
				rcode = Constants.RCODE_2716_DATACOUNT_ERR;
			}
		}
		
		if(!rcode.equals(Constants.RCODE_0000_OK)) {
			throw new Exception("basic checking is wrong !  rcode: "+ rcode );
		}
	}

	private boolean checkDataForPhoneCard(Connection conn, String persoBatchNo) throws SQLException 
	{
		String sqlCmd = "select count(*) from TB_TELCO_CARD m, TB_TELCO_CARD_DTL d where m.PERSO_BATCH_NO='" + persoBatchNo + "' and m.IMP_TELCO_FILE_NAME=d.IMP_TELCO_FILE_NAME";
		int dataCount = DbUtil.getInteger(sqlCmd, conn);
		boolean checkFlag = false;
		if(dataCount <= 0) {
    		checkFlag = true;
    	}
		return checkFlag;
	}

	public void checkDataLine(Connection conn, int idx, String dataLine, TbInctlInfo inctlInfo, YhdpPersoFeedBackBean dataBean)  throws Exception
	{
		String rcode = Constants.RCODE_0000_OK;
		
		String hgCardStatus = "";
		
		//"field02"
		String cardNo = dataBean.getCardNo();
		LOGGER.info("cardNo: " + cardNo);
		
		for(int checkIdx=0; checkIdx<1; checkIdx++)
		{
			//HAPPYGO檢核
			//20170620 改為判斷HgCardGroupId是否有填
        	if(!StringUtil.isEmpty(persoInfo.getHgCardGroupId())) {
				//"field120"
				String hexBarcode1 = dataBean.getHexBarcode1();
				String barcode1 = IFFUtils.hex2ascii(hexBarcode1);
				LOGGER.info("barcode1: " + barcode1);
				
				Vector<TbHgCardMapInfo> result = new Vector<TbHgCardMapInfo>();
				int count = new TbHgCardMapMgr(conn).queryMultiple("BARCODE1='" + barcode1 + "'", result);
				if(count <= 0) {
		    		LOGGER.error("barcode1 [" + barcode1 + "] not exist tb_hg_card_map");
		    		errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
		    				Constants.RCODE_2710_INVALID_ERR, "barcode1 [" + barcode1 + "] not exist tb_hg_card_map"));
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
		    	}
				hgCardStatus = result.get(0).getStatus();
			}

			//卡的物料"field125"
			String hexCardMaterialNo = dataBean.getHexCardMaterialNo();
			String cardMaterialNo = IFFUtils.hex2ascii(hexCardMaterialNo);
			LOGGER.info("card material No: " + cardMaterialNo);
			
			//包的物料"field124"
			String hexPackMaterialNo = dataBean.getHexPackMaterialNo();
			String packMaterialNo = IFFUtils.hex2ascii(hexPackMaterialNo);
			LOGGER.info("pack material No: " + packMaterialNo);
			
			//包號"field126"
			String hexPackNo = dataBean.getHexPackNo();
			String packNo = IFFUtils.hex2ascii(hexPackNo);
			LOGGER.info("packNo: " + packNo);
			
			//箱號"field127"
			String hexChestNo = dataBean.getHexChestNo();
			String chestNo = IFFUtils.hex2ascii(hexChestNo);
			LOGGER.info("chestNo: " + chestNo);
			
			//盒號"field128"
			String hexBoxNo = dataBean.getHexBoxNo();
			String boxNo = IFFUtils.hex2ascii(hexBoxNo);
			LOGGER.info("boxNo: " + boxNo);

			//一代卡
			if(persoInfo.getChipVersion().equals("1")) 
			{
				TbCardPK pk = new TbCardPK();
				pk.setCardNo(cardNo);
				pk.setExpiryDate(persoInfo.getExpiryDate());
				
				TbCardInfo cardInfo = new TbCardMgr(conn).querySingle(pk);
				
				if(cardInfo == null) {
		    		LOGGER.error("cardNo [" + cardNo + "] not exist tb_card");
		    		errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
		    				Constants.RCODE_2710_INVALID_ERR, "cardNo [" + cardNo + "] not exist tb_card"));
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
		    	}

				//非銀行卡檢核
				if(!persoInfo.getPersoType().equals("3")) {
					
					//卡片是否符合製卡設定檔			
					if(!cardMaterialNo.equals(persoInfo.getCardMaterialNo())) {
						LOGGER.error("card material No. [" + persoInfo.getCardMaterialNo() + "] of setting of perso doesn't match file(" + cardMaterialNo + ")");
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
								"card material No. [" + persoInfo.getCardMaterialNo() + "] of setting of perso doesn't match file(" + cardMaterialNo + ")"));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
					
					//卡的物料是否為補滿0
					String zeroString = StringUtils.paddingRightString("", '0', 18);
					if(cardMaterialNo.equals(zeroString)) {
						LOGGER.error("card Material No [" + cardMaterialNo + "] shouldn't be " + zeroString);
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
			    				Constants.RCODE_2710_INVALID_ERR, "card material No. [" + cardMaterialNo + "] shouldn't be " + zeroString));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
					
					//卡的物料是否為補滿空白
					String emptyString = StringUtils.paddingRightString("", ' ', 18);
					if(cardMaterialNo.equals(emptyString)) {
						LOGGER.error("card Material No [" + cardMaterialNo + "] shouldn't be " + emptyString);
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
			    				Constants.RCODE_2710_INVALID_ERR, "card material No. [" + cardMaterialNo + "] shouldn't be " + emptyString));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
				}
			}

			//二代卡
			if(persoInfo.getChipVersion().equals("2")) 
			{
				if(cardNo.substring(5).equals("FFFFFFFFFFF")) {
		    		LOGGER.error("cardNo.substring(5) [" + cardNo.substring(5) + "] shouldn't be FFFFFFFFFFF");
		    		errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
		    				Constants.RCODE_2710_INVALID_ERR, "cardNo.substring(5) [" + cardNo.substring(5) + "] shouldn't be FFFFFFFFFFF"));
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
		    	}
				
				//Mifare Id"field14"
				String mifareId = dataBean.getMifareId();
				LOGGER.info("mifareId: " + mifareId);
				if(mifareId.equals("00000000")) {
		    		LOGGER.error("mifareId [" + mifareId + "] shouldn't be 00000000");
		    		errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
		    				Constants.RCODE_2710_INVALID_ERR, "mifareId [" + mifareId + "] shouldn't be 00000000"));
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
		    	}
				
				//電信卡需檢核Mifare Id是否在tb_teleco_card_dtl
				if(persoInfo.getPersoType().equals("4"))
				{
					StringBuffer sb = new StringBuffer();
					sb.append("select count(*) from TB_TELCO_CARD m, TB_TELCO_CARD_DTL d");
					sb.append(" where m.IMP_TELCO_FILE_NAME = d.IMP_TELCO_FILE_NAME");
					sb.append(" and m.PERSO_BATCH_NO = '").append(persoInfo.getPersoBatchNo()).append("'");
					sb.append(" and d.MIFARE_UL_UID = '").append(mifareId).append("'");
					sb.append(" and d.IFF_FILE_NAME is null");
					
					String sqlCmd = sb.toString();
					int telcoCardCnt = DbUtil.getInteger(sqlCmd, conn);
					
					if(telcoCardCnt <= 0) {
						LOGGER.error("MIFARE_UL_UID [" + mifareId + "] has be used in TB_TELCO_CARD_DTL or not existed");
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
			    				Constants.RCODE_2710_INVALID_ERR, "MIFARE_UL_UID. [" + mifareId + "] has be used in TB_TELCO_CARD_DTL"));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
			    	}
				}
				
				//20170620 改為判斷HgCardGroupId是否有填
	        	if(!StringUtil.isEmpty(persoInfo.getHgCardGroupId())) {
					if(!hgCardStatus.equals("2")) {
			    		LOGGER.error("HG card map status [" + hgCardStatus + "] should be 2");
			    		errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
			    				Constants.RCODE_2710_INVALID_ERR, "HG card map status [" + hgCardStatus + "] should be 2"));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
			    	}
				}
				
				//非銀行卡檢核
				if(!persoInfo.getPersoType().equals("3")) {
					
					//卡片是否符合製卡設定檔
					if(!cardMaterialNo.equals(persoInfo.getCardMaterialNo())) {
						LOGGER.error("card material No. [" + persoInfo.getCardMaterialNo() + "] of setting of perso doesn't match file(" + cardMaterialNo + ")");
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
								"card material No. [" + persoInfo.getCardMaterialNo() + "] of setting of perso doesn't match file(" + cardMaterialNo + ")"));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
					
					//卡的物料是否為補滿0
					String zeroString = StringUtils.paddingRightString("", '0', 18);
					if(cardMaterialNo.equals(zeroString)) {
						LOGGER.error("card Material No [" + cardMaterialNo + "] shouldn't be " + zeroString);
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
			    				Constants.RCODE_2710_INVALID_ERR, "card material No. [" + cardMaterialNo + "] shouldn't be " + zeroString));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
					
					//卡的物料是否為補滿空白
					String emptyString = StringUtils.paddingRightString("", ' ', 18);
					if(cardMaterialNo.equals(emptyString)) {
						LOGGER.error("card Material No [" + cardMaterialNo + "] shouldn't be " + emptyString);
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
			    				Constants.RCODE_2710_INVALID_ERR, "card material No. [" + cardMaterialNo + "] shouldn't be " + emptyString));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
				}
				else {					
					int iffFeedbackDtlInfoCnt = new TbIffFeedbackDtlMgr(conn).getCount("CARD_NO='" + cardNo + "'");
					if(iffFeedbackDtlInfoCnt >0) {
						LOGGER.error("card No [" + cardNo + "] has existed in TB_IFF_FEEDBACK_DTL");
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
			    				Constants.RCODE_2710_INVALID_ERR, "card No. [" + cardNo + "] has existed in TB_IFF_FEEDBACK_DTL"));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
					
					int cardInfoCnt = new TbCardMgr(conn).getCount("CARD_NO='" + cardNo + "'");
					if(cardInfoCnt >0) {
						LOGGER.error("card No [" + cardNo + "] has existed in TB_CARD");
						errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
			    				Constants.RCODE_2710_INVALID_ERR, "card No. [" + cardNo + "] has existed in TB_CARD"));
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
				}
			}
		}

		if(!rcode.equals(Constants.RCODE_0000_OK)) {
			throw new Exception("one record's checking is wrong !");
		}
		
		//發卡單位編號1"field18"
		String issNo = dataBean.getIssNo();
		LOGGER.debug("issNo: " + issNo);
		
		//發卡設備編號2"field19"
		String issEquipmentNo = dataBean.getIssEquipmentNo();
		LOGGER.debug("issEquipmentNo: " + issEquipmentNo);
		
		//發行批號2"field20"
		String persoBatchNo = dataBean.getPersoBatchNo();
		LOGGER.debug("persoBatchNo: " + persoBatchNo);
		
		//發出日期4"field21"
		String activeDate = dataBean.getActiveDate();
		LOGGER.debug("activeDate: " + activeDate);
		
		//有效日期4"field22"
		String expiryDate = dataBean.getExpiryDate();
		LOGGER.debug("expiryDate: " + expiryDate);
		
		//卡片格式版本1"field23"
		String cardVersion = dataBean.getCardVersion();
		LOGGER.debug("cardVersion: " + cardVersion);
		
		//保留(卡片狀態:未初始化、初始化、個人化、鎖卡、停卡)1"field24"
		String cardStatus = dataBean.getCardStatus();
		LOGGER.debug("cardStatus: " + cardStatus);
		
		//檢查碼1"field25
		String checkCode1 = dataBean.getCheckCode1();
		LOGGER.info("檢查碼1: " + checkCode1);
		
		//自動加值設定1"field26"
		String autoReloadFlag = dataBean.getAutoReloadFlag();
		LOGGER.debug("autoReloadFlag: " + autoReloadFlag);
		
		//自動加值票值數額2"field27"
		String autoReloadValue = dataBean.getAutoReloadValue();
		LOGGER.debug("autoReloadValue: " + autoReloadValue);
		
		//儲存最大票值數額(餘額上限)2"field28"
		String balMaxbal = dataBean.getBalMaxbal();
		LOGGER.debug("balMaxbal: " + balMaxbal);
		
		//每筆可扣減最大票值數額2"field29"
		String minBalAmt8 = dataBean.getMinBalAmt8();
		LOGGER.debug("minBalAmt8: " + minBalAmt8);
		
		//指定加值設定1"field30"
		String appointReloadFlag = dataBean.getAppointReloadFlag();
		LOGGER.debug("appointReloadFlag: " + appointReloadFlag);
		
		//指定加值票值數額2"field31"
		String appointReloadValue = dataBean.getAppointReloadValue();
		LOGGER.debug("appointReloadValue: " + appointReloadValue);
		
		//自動加值日期2"field32"
		String autoReloadDate = dataBean.getAutoReloadDate();
		LOGGER.debug("autoReloadDate: " + autoReloadDate);
		
		//連續離線自動加值次數上限1"field33"
		String olAutoReloadMaxTime = dataBean.getOlAutoReloadMaxTime();
		LOGGER.debug("olAutoReloadMaxTime: " + olAutoReloadMaxTime);
		
		//連續自動加值次數上限1"field34"
		String autoReloadMaxTime = dataBean.getAutoReloadMaxTime();
		LOGGER.debug("autoReloadMaxTime: " + autoReloadMaxTime);
				
		//連續指定加值次數上限1"field35"
		String appointReloadMaxTime = dataBean.getAppointReloadMaxTime();
		LOGGER.debug("appointReloadMaxTime: " + appointReloadMaxTime);
		
		//檢查碼2"field36"
		String checkCode2 = dataBean.getCheckCode2();
		LOGGER.info("檢查碼2: " + checkCode2);
		
		String checkOrigData1=issNo + issEquipmentNo + persoBatchNo + activeDate + expiryDate + cardVersion + cardStatus;
		LOGGER.info("Before XOR: [" + checkOrigData1 +"]");

		String checkOrigData2=autoReloadFlag + autoReloadValue + balMaxbal + minBalAmt8 + appointReloadFlag + appointReloadValue + autoReloadDate + olAutoReloadMaxTime + autoReloadMaxTime + appointReloadMaxTime;
		LOGGER.info("Before XOR: [" + checkOrigData2 +"]");
		
		/*Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		
		TbCardInfo info = new TbCardInfo();
        info.setCardNo(cardNo);
        TbCardMgr mgr = new TbCardMgr(conn);
        int count = mgr.queryMultiple(info, result);  
        
        if(count <= 0) {
        	//addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, "CARD_NO" ,"cardInfo is null: " + cardNo);
        	LOGGER.error("cardInfo is null: " + cardNo);
        	IFFUtils.insertInctlErrInfo(idx, dataLine, inctlInfo, Constants.RCODE_2710_INVALID_ERR,"cardInfo is null: " + cardNo);
        	rcode =  Constants.RCODE_2710_INVALID_ERR;
        }

        String lifeCycle = result.get(0).getLifeCycle();
        if(!lifeCycle.equals("0")) {
        	//addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, "LIFE_CYCLE", "card life cycle is not valid: " + cardNo);
        	LOGGER.error("card life cycle is not valid:  " + cardNo);
        	IFFUtils.insertInctlErrInfo(idx, dataLine, inctlInfo, Constants.RCODE_2710_INVALID_ERR, "life cycle is not valid: " + cardNo);
        	rcode =  Constants.RCODE_2710_INVALID_ERR;
        }*/
        
        byte[] tmpByte = new byte[1];
    	tmpByte[0] = IFFUtils.XorData(checkOrigData1);
    	String countCeckCode1 = ISOUtil.hexString(tmpByte);
    	LOGGER.info("檢查碼: [" + countCeckCode1 + "]");
    	
    	if(!checkCode1.equals(countCeckCode1)) {
    		LOGGER.error("checkcode1: [" + checkCode1 + "], not match count checkcode1: [" + countCeckCode1 + "]");
    		errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo, 
    				Constants.RCODE_2710_INVALID_ERR, "checkcode1: [" + checkCode1 + "], not match count checkcode1: [" + countCeckCode1 + "]"));
    		rcode =  Constants.RCODE_2710_INVALID_ERR;
    	}
    	
    	if(!rcode.equals(Constants.RCODE_0000_OK)) {
			throw new Exception("one record's checking is wrong !");
		}
		
		byte[] tmpByte2 = new byte[1];
		tmpByte2[0] = IFFUtils.XorData(checkOrigData2);
		String countCeckCode2 = ISOUtil.hexString(tmpByte2);
		LOGGER.info("檢查碼: [" + countCeckCode2 + "]");
    	
    	if(!checkCode2.equals(countCeckCode2)) {
    		LOGGER.error("checkCode2: [" + checkCode2 + "], not match count checkcode2: [" + countCeckCode2 + "]");
    		errorSqlList.add(IFFUtils.getInctlErrInfo(idx, dataLine, inctlInfo,
    				Constants.RCODE_2710_INVALID_ERR, "checkCode2: [" + checkCode2 + "], not match count checkcode2: [" + countCeckCode2 + "]"));
    		rcode =  Constants.RCODE_2710_INVALID_ERR;
    	}
		
		if(!rcode.equals(Constants.RCODE_0000_OK)) {
			throw new Exception("one record's checking is wrong !");
		}
	}
	
	private boolean isExistPersoSetting(Connection conn, TbInctlInfo inctlInfo, String persoBathNo) throws Exception 
	{
		boolean checkFlag = false;
		TbPersoMgr mgr = new TbPersoMgr(conn);
		persoInfo = mgr.querySingle(persoBathNo);
		if(persoInfo == null) {
			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2705_NODATA, 
					"tb_perso_setting(" + persoBathNo + ") no data found");
			checkFlag = true;
		}		
		return checkFlag;
	}

	private boolean checkDataHeader(Connection conn, TbInctlInfo inctlInfo, String file) throws Exception 
	{
		rcode = Constants.RCODE_0000_OK;
		
		boolean checkFlag = false;
		BufferedInputStream bis = null;
		try {	
	    	//LOGGER.info("available: " + bis.available());
	    	//LOGGER.info("times: " + times);
			
			bis = new BufferedInputStream(new FileInputStream(new File(file)));
			rowDataCount = bis.available() / DATA_MAX_LENGTH;
	    	
	    	byte[] headBuffer = new byte[64];
	    	bis.read(headBuffer);
	    	
	    	String hexCount = ISOUtil.hexString(headBuffer).substring(20, 36);
	    	//int remarkDataCount = Integer.parseInt(hexCount, 16);
	    	int remarkDataCount = Integer.valueOf(IFFUtils.hex2ascii(hexCount));
	    	//LOGGER.info("record count: " + totRecordCnt);
	    	
	    	if(checkDataCount(inctlInfo, remarkDataCount)) {				
				rcode = Constants.RCODE_2710_INVALID_ERR;
				checkFlag = true;
			}
	     	
	    	String hexPersoBatchNo = ISOUtil.hexString(headBuffer).substring(36, 72);	
	    	String persoBatchNo = IFFUtils.hex2ascii(hexPersoBatchNo).trim();
	    	LOGGER.debug("perso batch no: " + persoBatchNo);
	    	
	    	if(isExistPersoSetting(conn, inctlInfo, persoBatchNo)) {				
				rcode = Constants.RCODE_2705_NODATA;
				checkFlag = true;
			}
	    	
	    	String hexLayoutId = ISOUtil.hexString(headBuffer).substring(72, 80);	
	    	String layoutId = IFFUtils.hex2ascii(hexLayoutId).trim();
	    	LOGGER.debug("layout id: [" + layoutId + "]");
	    	
	    	String hexCardFactoryId = ISOUtil.hexString(headBuffer).substring(80, 84);	
	    	String cardFactoryId = IFFUtils.hex2ascii(hexCardFactoryId).trim();
	    	LOGGER.debug("card factory id: [" + cardFactoryId + "]");
	    	
	    	String hexCardCatId = ISOUtil.hexString(headBuffer).substring(84, 86);	
	    	String cardCatId = hexCardCatId.trim();
	    	LOGGER.debug("card cat id: [" + cardCatId + "]");
	    	
	    	String hexPurchaseOrderNo = ISOUtil.hexString(headBuffer).substring(86, 106);	
	    	String purchaseOrderNo = IFFUtils.hex2ascii(hexPurchaseOrderNo).trim();
	    	LOGGER.debug("purchase order no: [" + purchaseOrderNo + "]");
	    	
	    	if(isMathPersoSetting(inctlInfo, layoutId, cardFactoryId, cardCatId, purchaseOrderNo)) {
				rcode = Constants.RCODE_2710_INVALID_ERR;
				checkFlag = true;
	    	}	  
    	}
		finally {
			bis.close();
		}
		return checkFlag;
	}
	
	private boolean checkDataCount(TbInctlInfo inctlInfo, int remarkDataCount) throws Exception 
	{
		boolean checkFlag = false;
		
		if(rowDataCount != remarkDataCount) {
			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
					"remark data count(" + remarkDataCount + ") doesn't match row data count(" + rowDataCount + ")");
			checkFlag = true;
		}
		return checkFlag;
	}

	private boolean isMathPersoSetting(TbInctlInfo inctlInfo, String layoutId, String cardFactoryId, String cardCatId, String purchaseOrderNo) throws Exception 
	{
		if(!persoInfo.getLayoutId().equals(layoutId)) {
			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
					"layout id(" + persoInfo.getLayoutId() + ") of setting of perso doesn't match file(" + layoutId + ")");
			return true;
		}
		
		if(!persoInfo.getCardFactoryId().equals(cardFactoryId)) {
			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
					"card factory id(" + persoInfo.getCardFactoryId() + ")  of setting of perso doesn't match file(" + cardFactoryId + ")");
			return true;
		}
		
		if(!persoInfo.getCardCatId().equals(cardCatId)) {
			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
					"card cat id(" + persoInfo.getCardCatId() + ")  of setting of perso doesn't match file(" + cardCatId + ")");
			return true;
		}
		
		if(!persoInfo.getPurchaseOrderNo().equals(purchaseOrderNo)) {
			IFFUtils.insertInctlErrInfo(0, null, inctlInfo, Constants.RCODE_2710_INVALID_ERR, 
					"purchase order no(" + persoInfo.getPurchaseOrderNo() + ")  of setting of perso doesn't match file(" + purchaseOrderNo + ")");
			return true;
		}
		
		//非銀行卡檢核 - 製卡訂單編號資料正確性
		if(!persoInfo.getPersoType().equals("3")) {		
			String zeroString = StringUtils.paddingRightString("", '0', 10);
			if(purchaseOrderNo.equals(zeroString)) {
				LOGGER.error("purchaseOrderNo [" + purchaseOrderNo + "] shouldn't be " + zeroString);
	    		IFFUtils.insertInctlErrInfo(0, null, inctlInfo,
	    				Constants.RCODE_2710_INVALID_ERR, "purchase Order No. [" + purchaseOrderNo + "] shouldn't be " + zeroString);
	    		return true;
			}
			
			String emptyString = StringUtils.paddingRightString("", ' ', 10);
			if(purchaseOrderNo.equals(emptyString)) {
				LOGGER.error("purchaseOrderNo [" + purchaseOrderNo + "] shouldn't be ["+ emptyString+"]");
	    		IFFUtils.insertInctlErrInfo(0, null, inctlInfo,
	    				Constants.RCODE_2710_INVALID_ERR, "purchase Order No. [" + purchaseOrderNo + "] shouldn't be ["+ emptyString +"]");
	    		return true;
			}
		}
		return false;
	}

	private boolean checkPersoCountConsisWithIffForBanking(Connection conn, String persoBatchNo) throws Exception 
	{
		String sqlCmd = "select IFF_QTY from TB_IFF_FEEDBACK where PERSO_BATCH_NO='" + persoBatchNo + "'";
		int iffQty = DbUtil.getInteger(sqlCmd, conn);
		boolean checkFlag = false;
		if(persoInfo.getPersoQty().intValue() < (rowDataCount + iffQty)) {
    		checkFlag = true;
    	}
		return checkFlag;
	}
	
	private boolean checkPersoCountConsisWithIFF(Connection conn, String persoBatchNo) throws Exception 
	{
		boolean checkFlag = false;
		if(persoInfo.getPersoQty().intValue() != (rowDataCount)) {
    		checkFlag = true;
    	}
		return checkFlag;
	}
	
	public String getOneRecord(int number, BufferedInputStream bis) throws Exception
	{
		String converRecord="";
		byte[] dtlBuffer = new byte[DATA_MAX_LENGTH];
		bis.read(dtlBuffer);

		converRecord = ISOUtil.hexString(dtlBuffer);

		return converRecord;
	}

	public TbPersoInfo getPersoInfo() {
		return persoInfo;
	}

	public void setPersoInfo(TbPersoInfo persoInfo) {
		this.persoInfo = persoInfo;
	}

	public int getRowDataCount() {
		return rowDataCount;
	}

	public void setRowDataCount(int rowDataCount) {
		this.rowDataCount = rowDataCount;
	}

	public ArrayList<String> getErrorSqlList() {
		return errorSqlList;
	}

	public void setErrorSqlList(ArrayList<String> errorSqlList) {
		this.errorSqlList = errorSqlList;
	}

	public String getRcode() {
		return rcode;
	}

	public void setRcode(String rcode) {
		this.rcode = rcode;
	}
	
}
