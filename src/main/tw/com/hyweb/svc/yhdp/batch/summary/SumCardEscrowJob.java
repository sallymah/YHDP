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
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jpos.util.Log;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardProductInfo;
import tw.com.hyweb.service.db.info.TbCardescrowSumPK;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbCardProductMgr;
import tw.com.hyweb.service.db.mgr.TbCardescrowSumMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Rita
 * 
 */
public class SumCardEscrowJob extends GenericBatchJob
{
    private static final Logger LOGGER = Logger.getLogger(SumCardEscrowJob.class);

    private final TbMemberInfo member;
    private final Integer overMonth;
    private final String pCode1;
    private final String pCode2;
    private static HashMap<String, String> cardProductInfos = new HashMap<String, String>();

    /**
     * @param issuer
     */
    public SumCardEscrowJob(TbMemberInfo member, Integer overMonth,String pCode1,String pCode2)
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
    	String lastMonth=DateUtils.getPreviousMonth(batchDate.substring(0,6), overMonth);
    	String lastMonthFirstDate=DateUtils.getFirstDayOfMonth(lastMonth);
    	String lastMonthLastDate=DateUtils.getLastDayOfMonth(lastMonth);
    	String tempMonth=DateUtils.getPreviousMonth(lastMonth, overMonth);//temp_month = {last_month} – 1個月

    	Vector<String> params = new Vector<String>();
    	
    		getCardProducts(connection);
 
    		String where = "MEM_ID = '" + member.getMemId()+"' AND STATUS='3'";
    		TbCardMgr cardMgr = new TbCardMgr(connection);
    	    Vector<TbCardInfo> results = new Vector<TbCardInfo>();
    	    cardMgr.queryMultiple(where, results);
    	    
    	    for (int k = 0; k < results.size(); k++) {
    	    	TbCardInfo info = (TbCardInfo) results.get(k);
    	    	
    	    	//找前一個加值總金額
    	    	String pastQtyCmd ="Select  NVL(sum(BONUS_QTY),0) as qty from TB_TRANS_DTL TD	Where CARD_NO= ? AND EXPIRY_DATE= ? AND P_CODE "+pCode2+" AND BONUS_ID = ? AND EXISTS (SELECT 1 FROM TB_TRANS WHERE TD.CARD_NO=CARD_NO"+
    	    	"	AND TD. EXPIRY_DATE= EXPIRY_DATE 	AND TD.LMS_INVOICE_NO= LMS_INVOICE_NO	AND TXN_DATE <= '"+lastMonthLastDate+"' AND TXN_DATE >= '"+lastMonthFirstDate+
    	    	"' AND P_CODE "+pCode2+" AND STATUS='1') AND	EXISTS (SELECT 1 FROM TB_TXN_DEF WHERE SIGN='P' "+
    	    	" AND TD.TXN_CODE=TXN_CODE) ";
    	    	params.clear();
    	    	params.add(info.getCardNo());
    	    	params.add(info.getExpiryDate());
    	    	params.add(cardProductInfos.get(info.getCardProduct()));
    	    	
    	    	Number pastQty= DbUtil.getNumber(pastQtyCmd, params, connection);
    	    	//消費累計總和
    	    	String sumQtyCmd ="SELECT NVL(sum(BONUS_QTY),0) as qty FROM TB_TRANS_DTL TD WHERE TD.CARD_NO= ? " +
    	    						" AND TD.EXPIRY_DATE= ? AND TD.P_CODE "+pCode1+" AND TD.BONUS_ID= ? AND EXISTS (SELECT 1 FROM TB_TRANS WHERE TD.CARD_NO=CARD_NO AND " +
    	    						" TD. EXPIRY_DATE= EXPIRY_DATE AND TD.LMS_INVOICE_NO= LMS_INVOICE_NO AND P_CODE "+pCode1+" AND STATUS='1' AND TXN_DATE <= '"+
    	    						lastMonthLastDate+"' AND TXN_DATE >= '"+lastMonthFirstDate+"') AND "+ 
    	    						"EXISTS (SELECT 1 FROM TB_TXN_DEF WHERE SIGN='M' AND TD.TXN_CODE=TXN_CODE)";
    	    	
    	    	params.clear();
    	    	params.add(info.getCardNo());
    	    	params.add(info.getExpiryDate());
    	    	params.add(cardProductInfos.get(info.getCardProduct()));
    	    	
    	    	Number sumQty= DbUtil.getNumber(sumQtyCmd, params, connection);
    	    	
    	    	
    	    	//期初餘額 OPEN_BAL, 取出前期的期末餘額當作本期的期初餘額
    	    	String calOpenBal ="SELECT CLOSE_BAL FROM TB_CARDESCROW_SUM WHERE CARD_NO= ? AND EXPIRY_DATE= ? AND LAST_MONTH=?";
    	    	
    	    	params.clear();
    	    	params.add(info.getCardNo());
    	    	params.add(info.getExpiryDate());
    	    	params.add(tempMonth);
    	    	
    	    	Number openBal= DbUtil.getNumber(calOpenBal, params, connection);
    	    	if(openBal == null)
    	    		openBal=0;
    	    	
    	    	//期末餘額 CLOSE_BAL, 取出每月1日的TB_CARD_BAL餘額當作期末餘額
    	    	String calCloseBal ="SELECT BAL_BONUS_QTY FROM TB_CARD_BAL WHERE CARD_NO= ? AND EXPIRY_DATE= ? AND BONUS_ID=?";
    	    	
    	    	params.clear();
    	    	params.add(info.getCardNo());
    	    	params.add(info.getExpiryDate());
    	    	params.add(cardProductInfos.get(info.getCardProduct()));
    	    	
    	    	Number closeBal= DbUtil.getNumber(calCloseBal, params, connection);
    	    	if(closeBal == null)
    	    		closeBal=0;
    	    	

	    		/*20121107 若Insert成功則成功, 若失敗則跳出程式  rex:said*/
	    		try{	
	    			
	    			params.clear();
	    			String insertCmd = "insert into TB_CARDESCROW_SUM (LAST_MONTH ,CARD_NO,EXPIRY_DATE,SUM_LAST_MONTH_AWARD,SUM_LAST_MONTH_PURCH,PROC_DATE,OPEN_BAL,CLOSE_BAL) values(?,?,?,?,?,?,?,?)";
	    			params.add(lastMonth);
	    			params.add(info.getCardNo());
	    			params.add(info.getExpiryDate());
	    			params.add(pastQty.toString());
	    			params.add(sumQty.toString());
	    			params.add(batchDate);
	    			params.add(openBal.toString());
	    			params.add(closeBal.toString());
	    			DbUtil.sqlAction(insertCmd, params, connection);
	    		} catch (Exception e){
	    			
	     			  LOGGER.warn("insert error:" + e.getMessage());

	    		}
	    			
	    		
    	    }
    	    
    	    
    	
    	
    	
    	
    }
    
    private Boolean queryCardEscrowExists(String cardNo,String expiryDate,String month,Connection conn) {
        Boolean CardEscrowExists = false;
           try {
        	 TbCardescrowSumPK pk = new TbCardescrowSumPK();
           	 pk.setCardNo(cardNo);
           	 pk.setExpiryDate(expiryDate);
           	 pk.setLastMonth(month);
           	 TbCardescrowSumMgr mgr = new TbCardescrowSumMgr(conn);
           	 CardEscrowExists = mgr.isExist(pk);
           	 return CardEscrowExists;
   		} catch (Exception ignore) {
   			  LOGGER.warn("queryCardEscrow error:" + ignore.getMessage());
               return false;
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
                cardProductInfos.put(info.getCardProduct(), info.getEcashBonusId());
        }
    }

    public HashMap<String, String> getCardProducts(Connection conn) throws SQLException {
        	loadCardProducts(conn);
        return cardProductInfos;
    }
}
