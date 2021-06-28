/*
 * $Id: CardDataGeneratorImpl.java 14219 2009-08-31 03:35:16Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */


package tw.com.hyweb.svc.yhdp.batch.persoOTA;

import java.sql.Connection;
import java.sql.SQLException;

import tw.com.hyweb.service.db.info.TbCpDeliveryInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbCpDeliveryMgr;


/**
 * @author Kevin
 * @version $Revision: 14219 $
 * 
 */
public class YhdpCpDeliveryDataGenerator
{	
    /**
     * Generate cp delivery data.
     * @throws SQLException 
     * 
     * @see tw.com.hyweb.core.cp.batch.perso.CardDataGenerator#makeCardData(java.sql.Connection,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      )
     */
    public void addCpDeliveryData(Connection conn, String cardNo, String batchDate, TbPersoInfo persoInfo) throws SQLException
    {
        TbCpDeliveryInfo cpDelevery = new TbCpDeliveryInfo();
        cpDelevery.setCardNo(cardNo);
        cpDelevery.setProcDate(batchDate);
        cpDelevery.setPurchaseOrderNo(persoInfo.getPurchaseOrderNo());
        //cpDelevery.setPackageMaterialNo(persoInfo.getPackageMaterialNo());
        cpDelevery.setCardMaterialNo(persoInfo.getCardMaterialNo());
            
        new TbCpDeliveryMgr(conn).insert(cpDelevery);
    }  
}
