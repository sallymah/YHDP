/*
 * $Id: CardDataGeneratorImpl.java 14219 2009-08-31 03:35:16Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.common.impfiles;

import java.sql.Connection;
import java.sql.SQLException;

import tw.com.hyweb.service.db.info.TbBlacklistSettingInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbBlacklistSettingMgr;


/**
 * @author Kevin
 * @version $Revision: 14219 $
 * 
 */
public class YhdpBlackListSettingDataGenerator
{	
    /**
     * Generate blacklist setting data.
     * @param hm 
     * @param inctlInfo 
     * @throws SQLException 
     * 
     * @see tw.com.hyweb.core.cp.batch.perso.CardDataGenerator#makeCardData(java.sql.Connection,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      )
     */
    public void makeBlackListSetgingData(Connection conn, String sysDate,String sysTime, String cardNo, TbPersoInfo persoInfo) throws SQLException
    {
    	TbBlacklistSettingInfo info = new TbBlacklistSettingInfo();
    	info.setCardNo(cardNo);
    	info.setExpiryDate(persoInfo.getExpiryDate());
    	info.setRegDate(sysDate);
    	info.setRegTime(sysTime);
    	info.setRegUserid("BATCH");
    	info.setRegReason("測試卡回收(加入黑名單)");
    	info.setStatus("1");
    	info.setBlacklistCode("AA");
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime);
    	info.setAprvUserid("BATCH");
    	info.setAprvDate(sysDate);
    	info.setAprvTime(sysTime);   	
    	
    	TbBlacklistSettingMgr mgr = new TbBlacklistSettingMgr(conn);
    	mgr.insert(info);
    }
}
