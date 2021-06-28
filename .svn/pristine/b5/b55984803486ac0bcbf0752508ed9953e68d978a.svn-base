/*
 * $Id: CardDataGeneratorImpl.java 14219 2009-08-31 03:35:16Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */


package tw.com.hyweb.svc.yhdp.batch.perso;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.svc.cp.batch.perso.CardDataGenerator;


/**
 * @author Chris
 * @version $Revision: 14219 $
 * 
 */
public class YhdpCardDataGenerator implements CardDataGenerator
{
	private static final String CARD_DELIVERY_FLAG = "1";
	
    /**
     * Generate card data.
     * 
     * @see tw.com.hyweb.core.cp.batch.perso.CardDataGenerator#makeCardData(java.sql.Connection,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      tw.com.hyweb.service.db.info.TbPersoInfo)
     */
    public TbCardInfo makeCardData(Connection connection, String batchDate, String regionId, String cardNumber, TbPersoInfo persoInfo)
    {
        TbCardInfo card = new TbCardInfo();
        card.setRegionId(regionId);
        card.setMemId(persoInfo.getMemId());
        card.setCardProduct(persoInfo.getCardProduct());
        card.setCardNo(cardNumber);
        card.setExpiryDate(persoInfo.getExpiryDate());
        
        //20140219 新增欄位 Kevin
        card.setAprvDate(DateUtils.getSystemDate());
        card.setAprvTime(DateUtils.getSystemTime());
        card.setAprvUserid("batch");
        //card.setExpUptStatus("1");
        
        card.setCardOwner(persoInfo.getMemId());
        card.setCoBrandEntId(persoInfo.getCoBrandEntId());
        card.setCardTypeId(persoInfo.getCardTypeId());
        card.setCardCatId(persoInfo.getCardCatId());
        card.setAutoReloadFlag(persoInfo.getAutoReloadFlag());
        
        if(card.getAutoReloadFlag().equals("Y")){
        	card.setAutoReloadValue(persoInfo.getAutoReloadValue());
        	card.setAutoReloadDate(batchDate);
        }
        
        if(persoInfo.getPersoType().equals("3")){
        	card.setBankId(persoInfo.getBankId());
        }
        //card.setLifeCycle("0");
        
        card.setPrimaryCard("1");

        card.setStatusUpdateDate(batchDate);
        card.setPersoBatchNo(persoInfo.getPersoBatchNo());
        
        //card.setKeyVersion(persoInfo.getKeyVersion());
        card.setPreloadAmt(persoInfo.getPreloadAmt());
        
        card.setCardFee(persoInfo.getCardFee());
        card.setVipFlag("0");
        card.setPtaUnitNo(persoInfo.getPtaUnitNo());
        card.setTestFlag(persoInfo.getIsTest());

    	card.setStatus(persoInfo.getCardStatus());
    	
    	//20170620 依據PERSO.IS_SYNC_HG 決定TB_CARD是否同步
    	card.setIsSyncHg(persoInfo.getIsSyncHg());
    	
    	/*card.setLifeCycle("");
    	card.setPreviousStatus("");
        card.setPreviousLifeCycle("");*/

        //card.setDeliveryStatus(persoInfo.getDeliveryFlag());
        
        /*if(CARD_DELIVERY_FLAG.equals(persoInfo.getDeliveryFlag()))
        	card.setCardOwner(persoInfo.getMemId());*/
            
        return card;
    }  
}
