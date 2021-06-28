package tw.com.hyweb.iff.impfiles;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.StringUtils;
import tw.com.hyweb.iff.Constants;
import tw.com.hyweb.iff.IFFUtils;
import tw.com.hyweb.service.db.info.TbHgCardMapInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbHgCardMapMgr;
import tw.com.hyweb.service.db.mgr.TbIffFeedbackDtlMgr;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class YhdpPersoFeebackDataCheck 
{
	private final Logger LOGGER = Logger.getLogger(YhdpPersoFeebackDataCheck.class);

	private String rcode = Constants.RCODE_0000_OK;
	private String errDesc = "errdesc";
	
	public TbPersoInfo checkPerso(Connection conn, String persoBatchNo) {
		// TODO Auto-generated method stub
		TbPersoInfo persoInfo= null;
		
		try{
			
			persoInfo = new TbPersoMgr(conn).querySingle(persoBatchNo);
			
			//只處理OTA的PERSO  CHIP_VERSION = '3' (卡片版本 OTA卡)
			if (persoInfo != null && persoInfo.getChipVersion().equals("3")){
				for(int checkIdx=0; checkIdx<1; checkIdx++)
				{
/*					if(persoInfo.getPersoType().equals("4")) {
						if(checkDataForPhoneCard(conn, persoInfo.getPersoBatchNo())) {
							rcode = Constants.RCODE_2716_DATACOUNT_ERR;
							break;
						}
					}*/
					//檢查製卡數量是否超出PERSO設定
					if(!checkPersoCountConsisWithIffForBanking(conn, persoInfo)) {
						rcode = Constants.RCODE_2716_DATACOUNT_ERR;
						errDesc = "TB_IFF_FEEDBACK does not exist.";
						LOGGER.error(errDesc);
						break;
					}
					
					//檢查電信業者是否存在
					if(!checkMnoMember(conn, persoInfo)) {
					    errDesc = "MnoMember does not exist.";
						rcode = Constants.RCODE_2716_DATACOUNT_ERR;
						LOGGER.error(errDesc);
						break;
					}
				}
			}
			else{
			    errDesc = "TB_PERSO does not exist.";
				rcode = Constants.RCODE_2716_DATACOUNT_ERR;
				LOGGER.error(errDesc);
			}
		}
		catch (SQLException e) {
			if (rcode.equals(Constants.RCODE_0000_OK)) {
			    errDesc = e.getMessage();
				rcode = Constants.RCODE_1001_SQL_ERR;
				LOGGER.error("checkPerso error: ",e);
			}
		}
		catch (Exception e) {
		    errDesc = e.getMessage();
			rcode = Constants.RCODE_2791_GENERALDB_ERR;
			LOGGER.error("checkPerso error: ",e);
		}
		return persoInfo;
	}
	
	private boolean checkPersoCountConsisWithIffForBanking(Connection conn, TbPersoInfo persoInfo) throws Exception 
	{
		String feebackConutSql = "select COUNT(1) from TB_IFF_FEEDBACK where PERSO_BATCH_NO='" + persoInfo.getPersoBatchNo() + "'";
		int feebackConut = DbUtil.getInteger(feebackConutSql, conn);
		if(feebackConut != 1){
			return false;
		}
		
		String sqlCmd = "select IFF_QTY from TB_IFF_FEEDBACK where PERSO_BATCH_NO='" + persoInfo.getPersoBatchNo() + "'";
		int iffQty = DbUtil.getInteger(sqlCmd, conn);
		
		if((1 + iffQty) > persoInfo.getPersoQty().intValue()) {
			return false;
    	}
		return true;
	}
	
	private boolean checkMnoMember(Connection conn, TbPersoInfo persoInfo) throws Exception 
	{
		String memderConutSql = " SELECT COUNT(1) from TB_MEMBER " +
								" WHERE TELECOM_CODE='" + persoInfo.getTelecomCode() + "'" +
								" AND MEM_ID = '" + persoInfo.getTelecomMemId() + "'" +
								" AND SUBSTR(MEM_TYPE,9,1)='1'";
		
		int feebackConut = DbUtil.getInteger(memderConutSql, conn);
		if(feebackConut != 1){
			return false;
		}
		return true;
	}
	
	public void checkDataLine(Connection conn, TbPersoInfo persoInfo, String dataLine, YhdpPersoFeedBackBean dataBean)
	{
		String hgCardStatus = "";
		//"field02"
		String cardNo = dataBean.getCardNo();
		LOGGER.info("cardNo: " + cardNo);

		try{
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
					    errDesc = "barcode1 [" + barcode1 + "] not exist tb_hg_card_map";
			    		LOGGER.error(errDesc);
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
	

				if(cardNo.substring(5).equals("FFFFFFFFFFF")) {
				    errDesc="cardNo.substring(5) [" + cardNo.substring(5) + "] shouldn't be FFFFFFFFFFF";
		    		LOGGER.error(errDesc);
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
		    	}
				
				//Mifare Id"field14"
				String mifareId = dataBean.getMifareId();
				LOGGER.info("mifareId: " + mifareId);
				if(mifareId.equals("00000000")) {
				    errDesc="mifareId [" + mifareId + "] shouldn't be 00000000";
				    LOGGER.error(errDesc);
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
		    	}
				
				//電信卡需檢核Mifare Id是否在tb_teleco_card_dtl
				//OTA_電信卡不需檢查
				/*StringBuffer sb = new StringBuffer();
				sb.append("select count(*) from TB_TELCO_CARD m, TB_TELCO_CARD_DTL d");
				sb.append(" where m.IMP_TELCO_FILE_NAME = d.IMP_TELCO_FILE_NAME");
				sb.append(" and m.PERSO_BATCH_NO = '").append(persoInfo.getPersoBatchNo()).append("'");
				sb.append(" and d.MIFARE_UL_UID = '").append(mifareId).append("'");
				//OTA MIFARE_UL_UID 可重複使用
				//sb.append(" and d.IFF_FILE_NAME is null");
				
				String sqlCmd = sb.toString();
				int telcoCardCnt = DbUtil.getInteger(sqlCmd, conn);
				
				if(telcoCardCnt <= 0) {
					LOGGER.error("MIFARE_UL_UID [" + mifareId + "] has be used in TB_TELCO_CARD_DTL or not existed");
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
		    	}*/
				
				//20170620 改為判斷HgCardGroupId是否有填
	        	if(!StringUtil.isEmpty(persoInfo.getHgCardGroupId())) {
					if(!hgCardStatus.equals("2")) {
					    errDesc="HG card map status [" + hgCardStatus + "] should be 2";
			    		LOGGER.error(errDesc);
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
			    	}
				}
				
				//非銀行卡檢核
				if(!persoInfo.getPersoType().equals("3")) {
					
					//卡片是否符合製卡設定檔
					if(!cardMaterialNo.equals(persoInfo.getCardMaterialNo())) {
					    errDesc="card material No. [" + persoInfo.getCardMaterialNo() + "] of setting of perso doesn't match file(" + cardMaterialNo + ")";
						LOGGER.error(errDesc);
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
					
					//卡的物料是否為補滿0
					String zeroString = StringUtils.paddingRightString("", '0', 18);
					if(cardMaterialNo.equals(zeroString)) {
					    errDesc="card Material No [" + cardMaterialNo + "] shouldn't be " + zeroString;
					    LOGGER.error(errDesc);
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
					
					//卡的物料是否為補滿空白
					String emptyString = StringUtils.paddingRightString("", ' ', 18);
					if(cardMaterialNo.equals(emptyString)) {
					    errDesc="card Material No [" + cardMaterialNo + "] shouldn't be " + emptyString;
					    LOGGER.error(errDesc);
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
				}
				else {					
					int iffFeedbackDtlInfoCnt = new TbIffFeedbackDtlMgr(conn).getCount("CARD_NO='" + cardNo + "'");
					if(iffFeedbackDtlInfoCnt >0) {
					    errDesc="card No [" + cardNo + "] has existed in TB_IFF_FEEDBACK_DTL";
					    LOGGER.error(errDesc);
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
					
					int cardInfoCnt = new TbCardMgr(conn).getCount("CARD_NO='" + cardNo + "'");
					if(cardInfoCnt >0) {
					    errDesc="card No [" + cardNo + "] has existed in TB_CARD";
					    LOGGER.error(errDesc);
			    		rcode =  Constants.RCODE_2710_INVALID_ERR;
			    		break;
					}
				}
				//SIR_ID 不允許空值
				if(StringUtil.isEmpty(dataBean.getSirId())){
				    errDesc="Sir id is null. ";
					LOGGER.error(errDesc);
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
				}
				//ICC_ID 不允許空值
				if(StringUtil.isEmpty(dataBean.getIccId())){
				    errDesc="Icc id is null. ";
					LOGGER.error(errDesc);
		    		rcode =  Constants.RCODE_2710_INVALID_ERR;
		    		break;
				}
				
				//SERVICE_ID 不允許空值
                if(StringUtil.isEmpty(dataBean.getServiceId())){
                    errDesc="Service id is null. ";
                    LOGGER.error(errDesc);
                    rcode =  Constants.RCODE_2710_INVALID_ERR;
                    break;
                }
			}
	
			if(!rcode.equals(Constants.RCODE_0000_OK)) {
				throw new Exception("one record's checking is wrong !" + errDesc);
			}
		}
		catch (SQLException e) {
			if (rcode.equals(Constants.RCODE_0000_OK)){
				rcode = Constants.RCODE_1001_SQL_ERR;
				LOGGER.error("checkPerso error: ",e);
			}
		}
		catch (Exception e) {
			if (rcode.equals(Constants.RCODE_0000_OK)){
				rcode = Constants.RCODE_2791_GENERALDB_ERR;
				LOGGER.error("checkPerso error: ",e);
			}
		}
	}
	
	public String getRcode() {
		return rcode;
	}

	public void setRcode(String rcode) {
		this.rcode = rcode;
	}

	public TbPersoInfo checkIff(Connection conn, String persoBatchNo, String dataLine, YhdpPersoFeedBackBean dataBean) throws Exception {
		// TODO Auto-generated method stub
		TbPersoInfo persoInfo = checkPerso(conn, persoBatchNo);
		if (getRcode().equals(Constants.RCODE_0000_OK)){
			checkDataLine(conn, persoInfo, dataLine, dataBean);
		}
		return persoInfo;
	}
	
	/**
     * @return the errDesc
     */
    public String getErrDesc()
    {
        return errDesc;
    }

    /**
     * @param errDesc the errDesc to set
     */
    public void setErrDesc(String errDesc)
    {
        this.errDesc = errDesc;
    }
}