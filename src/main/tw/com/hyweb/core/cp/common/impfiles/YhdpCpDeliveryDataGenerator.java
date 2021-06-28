/*
 * $Id: CardDataGeneratorImpl.java 14219 2009-08-31 03:35:16Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.common.impfiles;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbCpDeliveryInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbCpDeliveryMgr;
import tw.com.hyweb.util.IFFUtils;


/**
 * @author Kevin
 * @version $Revision: 14219 $
 * 
 */
public class YhdpCpDeliveryDataGenerator
{	
    /**
     * Generate cp delivery data.
     * @param hm 
     * @param inctlInfo 
     * @throws SQLException 
     * 
     * @see tw.com.hyweb.core.cp.batch.perso.CardDataGenerator#makeCardData(java.sql.Connection,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      )
     */
    public void makeCpDeliveryData(Connection conn, String batchDate, HashMap<String, String> hm, TbPersoInfo persoInfo, TbInctlInfo inctlInfo) throws SQLException
    {
    	String cardNo = hm.get("field02");
    	String hexPackageMaterialNo = hm.get("field124");
    	String hexPackageNo = hm.get("field126");
    	String hexPackingNo = hm.get("field127");
    	String hexBoxNo = hm.get("field128");
    	
    	String packageMaterialNo = IFFUtils.hex2ascii(hexPackageMaterialNo);
    	String packageNo = IFFUtils.hex2ascii(hexPackageNo);
    	String packingNo = IFFUtils.hex2ascii(hexPackingNo);
    	String boxNo = IFFUtils.hex2ascii(hexBoxNo);
		
        TbCpDeliveryInfo cpDelevery = new TbCpDeliveryInfo();
        cpDelevery.setCardNo(cardNo);
        cpDelevery.setPackageNo(packageNo);
        cpDelevery.setPackingNo(packingNo);
        cpDelevery.setBoxNo(boxNo);
        cpDelevery.setProcDate(batchDate);
        cpDelevery.setPurchaseOrderNo(persoInfo.getPurchaseOrderNo());
        cpDelevery.setPackageMaterialNo(packageMaterialNo);
        cpDelevery.setCardMaterialNo(persoInfo.getCardMaterialNo());
        cpDelevery.setInfileName(inctlInfo.getFullFileName());
            
        new TbCpDeliveryMgr(conn).insert(cpDelevery);
    }
    
    public void updateCpDelivery(Connection conn, HashMap<String, String> hm, TbPersoInfo persoInfo, TbInctlInfo inctlInfo) throws Exception 
    {     
    	String cardNo = hm.get("field02");
    	String hexPackageMaterialNo = hm.get("field124");
    	String hexPackageNo = hm.get("field126");
    	String hexPackingNo = hm.get("field127");
    	String hexBoxNo = hm.get("field128");
    	
    	String packageMaterialNo = IFFUtils.hex2ascii(hexPackageMaterialNo);
    	String packageNo = IFFUtils.hex2ascii(hexPackageNo);
    	String packingNo = IFFUtils.hex2ascii(hexPackingNo);
    	String boxNo = IFFUtils.hex2ascii(hexBoxNo);
    	
    	StringBuffer sql = new StringBuffer();
    	sql.append("UPDATE TB_CP_DELIVERY SET");
    	sql.append(" PACKAGE_NO='").append(packageNo).append("'");
    	sql.append(" ,PACKING_NO='").append(packingNo).append("'");
    	sql.append(" ,BOX_NO='").append(boxNo).append("'");
    	sql.append(" ,PACKAGE_MATERIAL_NO='").append(packageMaterialNo).append("'");
    	sql.append(" ,INFILE_NAME='").append(inctlInfo.getFullFileName()).append("'");
    	sql.append(" WHERE CARD_NO='").append(cardNo).append("'");
    	
    	DBService.getDBService().sqlAction(sql.toString(), conn, false);
    }
}
