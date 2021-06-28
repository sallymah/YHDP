package tw.com.hyweb.svc.yhdp.batch.summary.checkUnusualTXN;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbRiskInfoInfo;
import tw.com.hyweb.service.db.mgr.TbRiskInfoMgr;
import tw.com.hyweb.svc.yhdp.batch.util.BatchDateUtil;
import tw.com.hyweb.util.ReleaseResource;


public class CheckTxnJob extends GenericBatchJob {
	private final Map<String, String> result ;
	private int seqNo = 0;
	private final String CHECK_CONTINUE = "21";
	private final String CHECK_BALANCE = "22";
	private final String CHECK_CROSS_REGION = "23";

	public CheckTxnJob(Map<String, String> result, int seqNo )
    {
        this.result = result;
        this.seqNo = seqNo;
    }
	
	public void action(Connection connection, String batchDate) throws Exception
    {
		//取得上一筆交易
		Object[] lastTxnInfo = getLastTxnInfo(connection);
		boolean lastTxnInfoflag = true;
		//是否有上一筆交易，沒有則不需要下面的檢測
		if ( lastTxnInfo[0] == null )
			lastTxnInfoflag = false;
		if ( lastTxnInfoflag ){

			//交易序號不連續(21)
			int atc = Integer.valueOf(result.get("ATC").toString());
			int lastAtc = Integer.valueOf(lastTxnInfo[2].toString());
			if ( atc-1 != lastAtc ){
				TbRiskInfoMgr riskInfoMgr = new TbRiskInfoMgr(connection);
				riskInfoMgr.insert(getInsertRiskInfoSQL(CHECK_CONTINUE, lastTxnInfo, batchDate));
			}
			
			//卡片餘額不連貫(22)
			boolean checkBalanceflag = true;
			Object[] CheckBalanceData = getCheckBalance(connection, lastTxnInfo);
			for ( int i = 0; i < CheckBalanceData.length; i++ ){
				if ( CheckBalanceData[i] == null )
					checkBalanceflag = false;
			}
			if ( checkBalanceflag ){
				double lastAftBal = Double.valueOf(CheckBalanceData[0].toString());
				double currBefBal = Double.valueOf(CheckBalanceData[1].toString());
				
				if ( currBefBal != lastAftBal){
					TbRiskInfoMgr riskInfoMgr = new TbRiskInfoMgr(connection);
					riskInfoMgr.insert(getInsertRiskInfoSQL(CHECK_BALANCE,lastTxnInfo, batchDate));
				}
			}
			
			//短時間內異地交易(23)
			boolean checkCrossRegionflag = true;
			Object[]  CheckCrossRegionData = getCheckCrossRegion(connection, lastTxnInfo);
			for ( int i = 0; i < CheckCrossRegionData.length; i++ ){
				if ( CheckCrossRegionData[i] == null )
					checkCrossRegionflag = false;
			}
			if ( checkCrossRegionflag ){
				String txnTime = result.get("TERM_DATE").toString() + result.get("TERM_TIME").toString();
				String lastTxnTime = lastTxnInfo[4].toString() + lastTxnInfo[5].toString();
				int time = Integer.valueOf(BatchDateUtil.getGapMinutes(lastTxnTime, txnTime)); //計算此交易與上一筆交易相差幾分鐘
				int timeLimit =  Integer.valueOf(CheckCrossRegionData[2].toString());
				if ( time < timeLimit ){
					TbRiskInfoMgr riskInfoMgr = new TbRiskInfoMgr(connection);
					riskInfoMgr.insert(getInsertRiskInfoSQL(CHECK_CROSS_REGION, lastTxnInfo, batchDate));
				}
			}		
    	}
    }
	
	private Object[] getLastTxnInfo(Connection connection) throws Exception{
		Object[] lastTxnInfo = new Object[6];
		
		String sql = "SELECT CARD_NO, LMS_INVOICE_NO, ATC, MERCH_ID, TERM_DATE, TERM_TIME FROM ( " +
				"SELECT * FROM TB_ONL_TXN " +
				"WHERE CARD_NO = '"+ result.get("CARD_NO") +"' " +
				"AND ATC < '"+ result.get("ATC") +"' " +
				"AND P_CODE IN (SELECT P_CODE FROM TB_P_CODE_DEF WHERE ATC_FLAG='1') " +
				"ORDER BY ATC DESC, TERM_DATE DESC, TERM_TIME DESC) " +
				"WHERE ROWNUM=1";

    	Statement stmt = null;
        ResultSet rs = null;
        
        stmt = connection.createStatement();
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
        	lastTxnInfo[0] = rs.getString(1); //CARD_NO
        	lastTxnInfo[1] = rs.getString(2); //LMS_INVOICE_NO
        	lastTxnInfo[2] = rs.getString(3); //ATC
        	lastTxnInfo[3] = rs.getString(4); //MERCH_ID
        	lastTxnInfo[4] = rs.getString(5); //TERM_DATE
        	lastTxnInfo[5] = rs.getString(6); //TERM_TIME
        }
        ReleaseResource.releaseDB(null, stmt, rs);
		
		return lastTxnInfo;
	}
	
	
	private Object[] getCheckBalance(Connection connection, Object[] lastTxnInfo) throws Exception{
		
       Object[] values = new Object[2];
 
       String sql = "SELECT BONUS_AFTER_QTY FROM TB_ONL_TXN_DTL " +
       		"WHERE CARD_NO = '"+lastTxnInfo[0]+"' " +
       		"AND LMS_INVOICE_NO = '"+lastTxnInfo[1]+"' ";

       Statement stmt = null;
       ResultSet rs = null;
       
       stmt = connection.createStatement();
       rs = stmt.executeQuery(sql);
       while (rs.next()) {
    	   values[0] = rs.getDouble(1);
       }
       ReleaseResource.releaseDB(null, stmt, rs);
       
       String sql2 = "SELECT BONUS_BEFORE_QTY FROM TB_ONL_TXN_DTL " +
          		"WHERE CARD_NO = '"+result.get("CARD_NO")+"' " +
           		"AND LMS_INVOICE_NO = '"+result.get("LMS_INVOICE_NO")+"' ";

       Statement stmt2 = null;
       ResultSet rs2 = null;
		  
       stmt2 = connection.createStatement();
       rs2 = stmt2.executeQuery(sql2);
       while (rs2.next()) {
    	   values[1] = rs2.getDouble(1);
       }
       ReleaseResource.releaseDB(null, stmt2, rs2);
       
       return values;
    }

	private Object[] getCheckCrossRegion(Connection connection, Object[] lastTxnInfo) throws Exception{
		
		Object[] values = new Object[3];
		int i = 0;
		
		//get REGION_FROM
		i = 0;
		String sql2 = "SELECT RANGE FROM TB_ZIP_CODE " +
				"WHERE ZIP_CODE=(SELECT ZIP_CODE FROM TB_MERCH WHERE MERCH_ID = '"+lastTxnInfo[3]+"')";
		Statement stmt2 = null;
		ResultSet rs2 = null;
	  
		stmt2 = connection.createStatement();
		rs2 = stmt2.executeQuery(sql2);
		while (rs2.next()) {
			values[0] = rs2.getString(1);
			i++;
		}
		ReleaseResource.releaseDB(null, stmt2, rs2);
		if ( i == 0 )
			return values;
   
		//get REGION_TO
		i = 0;
		String sql3 = "SELECT RANGE FROM TB_ZIP_CODE " +
				"WHERE ZIP_CODE=(SELECT ZIP_CODE FROM TB_MERCH WHERE MERCH_ID = '"+result.get("MERCH_ID")+"')";
		Statement stmt3 = null;
		ResultSet rs3 = null;
   
		stmt3 = connection.createStatement();
		rs3 = stmt3.executeQuery(sql3);
		while (rs3.next()) {
			values[1] = rs3.getString(1);
			i++;
		}
		ReleaseResource.releaseDB(null, stmt3, rs3);
		if ( i == 0 || values[0].equals(values[1]))
			return values;
		
		//get TXN_TIME_LIMIT
		i = 0;
		String sql4 = "SELECT TXN_TIME_LIMIT FROM TB_REGION_TXN_TIME_LIMIT "+
				"WHERE REGION_FROM = '"+values[0]+"' " +
				"AND REGION_TO = '"+values[1]+"' ";
		Statement stmt4 = null;
		ResultSet rs4 = null;
   
		stmt4 = connection.createStatement();
		rs4 = stmt4.executeQuery(sql4);
		while (rs4.next()) {
			values[2] = rs4.getString(1);
		}
		ReleaseResource.releaseDB(null, stmt4, rs4);

		return values;
	}
	
	private TbRiskInfoInfo getInsertRiskInfoSQL(String warnCode, Object[] lastTxnInfo, String batchDate) throws Exception
    {
        TbRiskInfoInfo tbRiskInfo = new TbRiskInfoInfo();
        tbRiskInfo.setAcqMemId(result.get("ACQ_MEM_ID"));
        tbRiskInfo.setMerchId(result.get("MERCH_ID"));
        tbRiskInfo.setTermSettleDate(result.get("TERM_SETTLE_DATE"));
        tbRiskInfo.setTermId(result.get("TERM_ID"));
        tbRiskInfo.setBatchNo(result.get("BATCH_NO"));
        tbRiskInfo.setTxnDate(result.get("TERM_DATE"));
        tbRiskInfo.setTxnTime(result.get("TERM_TIME"));
        tbRiskInfo.setLmsInvoiceNo(result.get("LMS_INVOICE_NO"));
        tbRiskInfo.setCoBrandEntId(result.get("CO_BRAND_ENT_ID"));
        tbRiskInfo.setCardTypeId(result.get("CARD_TYPE_ID"));
        tbRiskInfo.setCardCatId(result.get("CARD_CAT_ID"));
        tbRiskInfo.setCardNo(result.get("CARD_NO"));
        tbRiskInfo.setTxnAmt(Double.valueOf(result.get("TXN_AMT").toString()));
        tbRiskInfo.setPCode(result.get("P_CODE"));
        tbRiskInfo.setTxnSrc(result.get("TXN_SRC"));
        tbRiskInfo.setLastLmsInvoiceNo(lastTxnInfo[1].toString());
        tbRiskInfo.setWarnCode(warnCode);
        tbRiskInfo.setProcDate(batchDate);
        tbRiskInfo.setProcTime(DateUtils.getSystemTime());
        tbRiskInfo.setStatus("1");
        tbRiskInfo.setLastAtc(lastTxnInfo[2].toString());
        tbRiskInfo.setSeqno(seqNo);
        
		return tbRiskInfo;

    }
	
}
