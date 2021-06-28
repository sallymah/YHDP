/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/9/30
 */
package tw.com.hyweb.svc.yhdp.batch.summary;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardProductInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbCardProductMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Rita
 * 
 */
public class CardInvalidEscrowJob extends GenericBatchJob
{
    private static final Logger LOGGER = Logger.getLogger(CardInvalidEscrowJob.class);

    private final TbMemberInfo member;
    private final Integer overMonth;
    private final String pCode1;
    private final String pCode2;
    private static HashMap<String, List> cardProductInfos = new HashMap<String, List>();

    /**
     * @param issuer
     */
    public CardInvalidEscrowJob(TbMemberInfo member, Integer overMonth,String pCode1,String pCode2)
    {
	    this.member = member;
	    this.overMonth=overMonth;
	    this.pCode1=pCode1;
	    this.pCode2=pCode2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java
     * .sql.Connection, java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
    	
    	Vector<String> params = new Vector<String>();
    	String infoCmd="Select CARD_NO, EXPIRY_DATE from TB_TRANS Where CUT_DATE= '"+batchDate+"' AND P_CODE "+pCode1+" AND STATUS ='1' AND ISS_MEM_ID = '"+member.getMemId()+"'"+
    					" union "+
    					"Select CARD_NO, EXPIRY_DATE from TB_CARD Where MEM_ID='"+member.getMemId()+"' AND STATUS='3' AND  PBM_PROC_DATE='00000000' GROUP BY CARD_NO, EXPIRY_DATE";
    		
    	Vector infoList=new Vector();
    	infoList=BatchUtil.getInfoListHashMap(infoCmd.toString());
    	
    	//取得cardProduct
    	if(infoList.size()>0)
    		getCardProducts(connection);
 
    	
    	for(int i = 0;i<infoList.size();i++){
    		String cardNo = ((HashMap)infoList.get(i)).get("CARD_NO").toString();
    		String expireDate = ((HashMap)infoList.get(i)).get("EXPIRY_DATE").toString();
    		
    		String where = "CARD_NO = '" + cardNo+"' AND EXPIRY_DATE = '"+expireDate+"'" ;
    		TbCardMgr cardMgr = new TbCardMgr(connection);
    	    Vector<TbCardInfo> results = new Vector<TbCardInfo>();
    	    cardMgr.queryMultiple(where, results);
    	    
    	    for (int k = 0; k < results.size(); k++) {
    	    	TbCardInfo info = (TbCardInfo) results.get(k);
    	    	
    	    	//找一年前的加值總金額
    	    	String pastQtyCmd ="Select  NVL(sum(BONUS_QTY),0) as qty from TB_TRANS_DTL TD	Where CARD_NO= ? AND EXPIRY_DATE= ? AND P_CODE "+pCode2+" AND BONUS_ID = ? AND EXISTS (SELECT 1 FROM TB_TRANS WHERE TD.CARD_NO=CARD_NO"+
    	    	"	AND TD. EXPIRY_DATE= EXPIRY_DATE 	AND TD.LMS_INVOICE_NO= LMS_INVOICE_NO	AND TXN_DATE <= '"+DateUtils.getPreviousMonthDate(batchDate, overMonth)+"' AND P_CODE "+pCode2+" AND STATUS='1') AND	EXISTS (SELECT 1 FROM TB_TXN_DEF WHERE SIGN='P' "+
    	    	" AND TD.TXN_CODE=TXN_CODE) ";
    	    	params.clear();
    	    	params.add(cardNo);
    	    	params.add(expireDate);
    	    	params.add(((TbCardProductInfo)cardProductInfos.get(info.getCardProduct()).get(0)).getEcashBonusId());
    	    	
    	    	Number pastQty= DbUtil.getNumber(pastQtyCmd, params, connection);
    	    	if(pastQty == null)
    	    		pastQty = 0;
    	    	
    	    	
    	    	//消費累計總和
    	    	String sumQtyCmd ="SELECT NVL(sum(BONUS_QTY),0) as qty FROM TB_TRANS_DTL TD WHERE TD.CARD_NO= ? AND TD.EXPIRY_DATE= ? AND TD.P_CODE "+pCode1+" AND TD.BONUS_ID= ? AND EXISTS (SELECT 1 FROM TB_TRANS WHERE TD.CARD_NO=CARD_NO AND TD. EXPIRY_DATE= EXPIRY_DATE AND TD.LMS_INVOICE_NO= LMS_INVOICE_NO AND P_CODE "+pCode1+" AND STATUS='1')";
    	    	
    	    	params.clear();
    	    	params.add(cardNo);
    	    	params.add(expireDate);
    	    	params.add(((TbCardProductInfo)cardProductInfos.get(info.getCardProduct()).get(0)).getEcashBonusId());
    	    	
    	    	Number sumQty= DbUtil.getNumber(sumQtyCmd, params, connection);
    	    	if(sumQty == null)
    	    		sumQty = 0;
    	    	
    	    	
    	    	/*計算不需列入信託金額
    	    	 * 找一年前的加值總金額 - 消費累計 >0 不需列入信託(TB_.Last_cns_amt )
				        反則需列入入信託

    	    	 */
    	    	Number lastYearCntAmt = pastQty.doubleValue() - sumQty.doubleValue();
    	    	
    	    	if(lastYearCntAmt.doubleValue()> 0){
    	    		
    	    		params.clear();
    	        	String updateCmd = "update TB_CARD set LAST_YEAR_CNS_AMT= ? ,TOTAL_RELOAD_AMT = ? ,TOTAL_USE_AMT = ? ,PBM_PROC_DATE = ? where CARD_NO= ? and EXPIRY_DATE = ? ";
    	        	params.add(lastYearCntAmt.toString());
    	        	params.add(pastQty.toString());
    	        	params.add(sumQty.toString());
    	        	params.add(batchDate);
    	        	params.add(cardNo);
        	    	params.add(expireDate);
    	        	DbUtil.sqlAction(updateCmd, params, connection);
    	        	
    	    	}else{
    	    		
    	    		params.clear();
    	        	String updateCmd = "update TB_CARD set LAST_YEAR_CNS_AMT= 0 ,TOTAL_RELOAD_AMT = ? ,TOTAL_USE_AMT = ? ,PBM_PROC_DATE = ? where CARD_NO= ? and EXPIRY_DATE = ? ";
    	        	params.add(pastQty.toString());
    	        	params.add(sumQty.toString());
    	        	params.add(batchDate);
    	        	params.add(cardNo);
        	    	params.add(expireDate);
    	        	DbUtil.sqlAction(updateCmd, params, connection);
    	    	}
    	    	
    	    }
    	    
    	    
    	}
    	
    	
    	
    	
    }
    public  void loadCardProducts(Connection conn) throws SQLException {
    	cardProductInfos.clear();
    	String where = "MEM_ID = " + StringUtil.toSqlValueWithSQuote(member.getMemId());
        TbCardProductMgr cpMgr = new TbCardProductMgr(conn);
        Vector<TbCardProductInfo> results = new Vector<TbCardProductInfo>();
        cpMgr.queryMultiple(where,results);
        for (int i = 0; i < results.size(); i++) {
        	TbCardProductInfo info = (TbCardProductInfo) results.get(i);
            List value = cardProductInfos.get(info.getEcashBonusId());
            if (value == null) {
                value = new ArrayList();
                value.add(info);
                cardProductInfos.put(info.getCardProduct(), value);
            }
            else {
                value.add(info);
            }
        }
    }

    public HashMap<String, List> getCardProducts(Connection conn) throws SQLException {
        if (cardProductInfos.keySet().size() <= 0) {
        	loadCardProducts(conn);
        }
        return cardProductInfos;
    }
}
