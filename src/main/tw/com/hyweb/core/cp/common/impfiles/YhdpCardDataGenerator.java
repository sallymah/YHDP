/*
 * $Id: CardDataGeneratorImpl.java 14219 2009-08-31 03:35:16Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.common.impfiles;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.util.IFFUtils;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Chris
 * @version $Revision: 14219 $
 * 
 */
public class YhdpCardDataGenerator
{
	private static final Logger LOGGER = Logger.getLogger(YhdpCardDataGenerator.class);
	
	private static final String CARD_DELIVERY_FLAG = "1";
	
	public final String PERSO_TYPE_BANK_CARD = "3";
	
    /**
     * Generate card data.
     * @throws Exception 
     * @see tw.com.hyweb.core.cp.batch.perso.CardDataGenerator#makeCardData(java.sql.Connection,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      tw.com.hyweb.service.db.info.TbPersoInfo)
     */
    public void makeCardData(Connection conn, String batchDate, HashMap<String, String> hm, 
    		TbPersoInfo persoInfo, TbInctlInfo inctlInfo) throws Exception
    {
    	String cardNo = hm.get("field02");
		LOGGER.info("2.儲值檔識別碼(PID): " + cardNo);

		String mifareId = hm.get("field14");
		LOGGER.info("14.Mifare晶片序號: " + mifareId);
		
		String unixTime = hm.get("field22");
		long hexUnixTime = Long.parseLong(unixTime, 16);
		String expiryDate = new SimpleDateFormat("yyyyMMdd").format(new Date(Long.valueOf(hexUnixTime) * 1000L));
		LOGGER.info("22.有效日期: " + expiryDate);
		
		String autoReloadFlag = hm.get("field26");
		LOGGER.info("26.自動加值設定: " + autoReloadFlag);
		
		String hexAutoReloadValue = hm.get("field27");
		int autoReloadValue = Integer.parseInt(hexAutoReloadValue, 16);
		LOGGER.info("27.自動加值數值: " + autoReloadValue);
		
		String hexBoxNo = hm.get("field128");
		String boxNo = IFFUtils.hex2ascii(hexBoxNo);
		LOGGER.info("128.盒號: " + boxNo);

		String regionId = getMemberInfo(conn, persoInfo).getRegionId();
		
        TbCardInfo card = new TbCardInfo();
        card.setRegionId(regionId);
        card.setMemId(persoInfo.getMemId());
        card.setCardProduct(persoInfo.getCardProduct());
        card.setCardNo(cardNo);
        card.setIffFileName(inctlInfo.getFullFileName());
        
        card.setMifareUlUid(mifareId);
        card.setBoxNo(boxNo);
        
        //20140219 新增欄位 Kevin
        card.setAprvDate(DateUtils.getSystemDate());
        card.setAprvTime(DateUtils.getSystemTime());
        card.setAprvUserid("batch");
        //card.setExpUptStatus("1");
        
        card.setCardOwner(persoInfo.getMemId());
        card.setCoBrandEntId(persoInfo.getCoBrandEntId());
        card.setCardTypeId(persoInfo.getCardTypeId());
        card.setCardCatId(persoInfo.getCardCatId());

        if(persoInfo.getPersoType().equals(PERSO_TYPE_BANK_CARD)) 
        {
        	card.setExpiryDate(expiryDate);
        	if(autoReloadFlag.equals("02")) {
        		card.setAutoReloadFlag("Y");
        	}
        	else {
        		card.setAutoReloadFlag("N");
        	}
        	card.setAutoReloadValue(autoReloadValue);
        	card.setBankId(persoInfo.getBankId());
        }
        else {
        	card.setExpiryDate(persoInfo.getExpiryDate());
        	card.setAutoReloadFlag(persoInfo.getAutoReloadFlag());
        	card.setAutoReloadValue(persoInfo.getAutoReloadValue());
        }   
        
        //20170620 改為判斷HgCardGroupId是否有填
    	if(!StringUtil.isEmpty(persoInfo.getHgCardGroupId())) {
        	String hexBarcode1 = hm.get("field120");
			String barcode1 = IFFUtils.hex2ascii(hexBarcode1);
			LOGGER.info("barcode1: " + barcode1);
			card.setHgCardNo(barcode1);
        }
        card.setPrimaryCard("1");
        
        card.setStatusUpdateDate(batchDate);
        card.setPersoBatchNo(persoInfo.getPersoBatchNo());
        
        //card.setKeyVersion(persoInfo.getKeyVersion());
        card.setPreloadAmt(persoInfo.getPreloadAmt());
        
        card.setCardFee(persoInfo.getCardFee());
        card.setVipFlag("0");
        card.setPtaUnitNo(persoInfo.getPtaUnitNo());
        card.setTestFlag(persoInfo.getIsTest());
        
        if(persoInfo.getIsTest().equals("1")) {
        	card.setStatus("9");
        	card.setLifeCycle("8");
        	card.setPreviousStatus(persoInfo.getCardStatus());
            card.setPreviousLifeCycle(persoInfo.getCardStatus());
        }
        else {
        	card.setStatus(persoInfo.getCardStatus());
        	//20161207 #424 卡片狀態(3完成開卡,3已開卡)更新為(3完成開卡,5交易中)
        	if ( persoInfo.getCardStatus().equals("3") ){
        		card.setLifeCycle("5");
        	}
        	else{
        		if(persoInfo.getPersoType().equals(PERSO_TYPE_BANK_CARD)) 
                {
        			card.setLifeCycle(card.getStatus());
                }
        		else {
        			card.setLifeCycle("0");
        		}
        	}
        	card.setPreviousStatus("1");
            card.setPreviousLifeCycle("");
        }
        
        //20170620 依據PERSO.IS_SYNC_HG 決定TB_CARD是否同步
        card.setIsSyncHg(persoInfo.getIsSyncHg());
        //card.setDeliveryStatus(persoInfo.getDeliveryFlag());
        
        /*if(CARD_DELIVERY_FLAG.equals(persoInfo.getDeliveryFlag()))
        	card.setCardOwner(persoInfo.getMemId());*/
        
        new TbCardMgr(conn).insert(card);
    }
    
    private TbMemberInfo getMemberInfo(Connection conn,TbPersoInfo persoInfo) throws Exception 
    {
    	Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();
    	new TbMemberMgr(conn).queryMultiple("MEM_ID = '" + persoInfo.getMemId() + "'", result);       	
    	TbMemberInfo memberInfo = result.get(0);
    	
    	return memberInfo;
    }
    
    public void updateCardData(Connection conn, HashMap<String, String> hm, TbPersoInfo persoInfo, TbInctlInfo inctlInfo) throws Exception 
    {     
    	StringBuffer sql= new StringBuffer();
    	Vector<TbCardInfo> result = new Vector<TbCardInfo>();
    	
    	String cardNo = hm.get("field02");
    	LOGGER.info("2.儲值檔識別碼(PID): " + cardNo);
    	 	
        TbCardMgr mgr = new TbCardMgr(conn);   
        mgr.queryMultiple("CARD_NO='"+ cardNo + "'", result);   
        String lifeCycle = result.get(0).getLifeCycle();
    	
    	String mifareId = hm.get("field14");
    	LOGGER.info("14.Mifare晶片序號: " + mifareId);
    	
    	String hexBoxNo = hm.get("field128");
    	String boxNo = IFFUtils.hex2ascii(hexBoxNo);
    	LOGGER.info("128.盒號: " + boxNo);

	    sql.append("UPDATE TB_CARD SET");
	    sql.append(" BOX_NO =").append(StringUtil.toSqlValueWithSQuote(boxNo));
	    sql.append(",MIFARE_UL_UID =").append(StringUtil.toSqlValueWithSQuote(mifareId));

    	if(persoInfo.getIsTest().equals("1")) {
    		sql.append(",STATUS =").append(StringUtil.toSqlValueWithSQuote("9"));
    		sql.append(",LIFE_CYCLE =").append(StringUtil.toSqlValueWithSQuote("8"));
    		sql.append(",PREVIOUS_STATUS =").append(StringUtil.toSqlValueWithSQuote(persoInfo.getCardStatus()));
    		sql.append(",PREVIOUS_LIFE_CYCLE =").append(StringUtil.toSqlValueWithSQuote(persoInfo.getCardStatus()));
    	}
    	else {
    		//20161207 #424 卡片狀態(3完成開卡,3已開卡)更新為(3完成開卡,5交易中)
    		if ( persoInfo.getCardStatus().equals("3") ){
    			sql.append(",LIFE_CYCLE =").append(StringUtil.toSqlValueWithSQuote("5"));
    		}
    		else{
    			if(persoInfo.getPersoType().equals(PERSO_TYPE_BANK_CARD)) 
                {
    				sql.append(",LIFE_CYCLE =").append(StringUtil.toSqlValueWithSQuote(persoInfo.getCardStatus()));
                }
        		else {
        			sql.append(",LIFE_CYCLE =").append(StringUtil.toSqlValueWithSQuote("0"));
        		}
    		}
    		sql.append(",PREVIOUS_STATUS =").append(StringUtil.toSqlValueWithSQuote("1"));
    	}
    	//20161129 紀錄匯入檔名
    	sql.append(",IFF_FILE_NAME =").append(StringUtil.toSqlValueWithSQuote(inctlInfo.getFullFileName()));
    	
    	sql.append(" WHERE CARD_NO =").append(StringUtil.toSqlValueWithSQuote(cardNo));
    	
    	DBService.getDBService().sqlAction(sql.toString(), conn, false);
    }
}
