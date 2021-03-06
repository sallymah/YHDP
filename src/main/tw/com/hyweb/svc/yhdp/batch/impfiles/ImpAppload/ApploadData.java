package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpAppload;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.BatchSequenceGenerator;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.yhdp.batch.framework.BatchException;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ImpFileInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.service.db.info.TbAppointReloadDtlInfo;
import tw.com.hyweb.service.db.info.TbAppointReloadInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbInctlErrInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.mgr.TbAppointReloadMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbInctlErrMgr;
import tw.com.hyweb.util.BatchUtils;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;

public class ApploadData
{
    private static Logger log = Logger.getLogger(ApploadData.class);

    private final String CARD_STATUS_OPENED = "3";
    private final String CARD_LIFE_CYCLE_OPENED = "3";
    private final String CARD_LIFE_CYCLE_DOING_TXN = "5";
    private String sysDate = DateUtils.getSystemDate();   
    private String sysTime = DateUtils.getSystemTime();
    private ErrorDescInfo descInfo;
    private String errorDesc; 
    private int failCnt = 0; 

	private final Map<String, String> fileData;
	private final TbInctlInfo inctlInfo;
	private final DataLineInfo lineInfo;

	private ImpFileInfo impFileInfo;
	private TbCardInfo cardInfo;
	
	public TbCardInfo getCardInfo() {
		return cardInfo;
	}

	public void setCardInfo(TbCardInfo cardInfo) {
		this.cardInfo = cardInfo;
	}


	private String cardNo;
	//private String expiryDate;
	private String arSerno;
	private int sumBonusQty;
	private int exchangeLimit;
      
    public ApploadData(Connection conn, Map<String, String> fileData, 
    					ImpFileInfo impFileInfo, TbInctlInfo inctlInfo, DataLineInfo lineInfo, int exchangeLimit) throws Exception
    {
    	this.fileData = fileData;
    	this.impFileInfo = impFileInfo;
    	this.inctlInfo = inctlInfo;
    	this.lineInfo = lineInfo;
    	this.exchangeLimit = exchangeLimit;
    	
    	initialValue(conn);
    }

	private void initialValue(Connection conn) throws Exception 
	{
		//cardNo = "986" + getFileData().get("CARD_NO");
		String barCode1 = getFileData().get("BARCODE1");
    	cardInfo = getCardInfo(conn, barCode1);
    	//expiryDate = getExpiryDate(conn);
    	arSerno = DateUtils.getSystemDate().substring(2,8) + BatchSequenceGenerator.getSequenceString("SEQ_APPOINTRELOAD_ARSERNO", 6);
    	sumBonusQty = sumAppReloadBonusQty(conn, cardNo);
	}

	private int sumAppReloadBonusQty(Connection conn, String cardNo) throws Exception 
	{	
		String sqlCmd = "select sum(BONUS_QTY) AS SUM_QTY from tb_appoint_reload_dtl "
				+ "where ar_serno in (select ar_serno from tb_appoint_reload where card_no='" + cardNo + "' and status = '0')";	
		int sumBonusQty = DbUtil.getInteger(sqlCmd, conn);	
		return sumBonusQty;
	}
	
	private TbCardInfo getCardInfo(Connection conn, String barCode1) throws Exception 
	{	
		TbCardInfo info = null;
		
		String sqlCmd = "SELECT card_no FROM tb_hg_card_map WHERE barcode1 like '%" + barCode1 + "'";
		Vector list = DbUtil.select(sqlCmd, conn);
		
		if(list != null && list.size() < 2)
		{
			Vector record = (Vector)list.get(0);
			cardNo = (String)record.get(0); 
			
			Vector<TbCardInfo> result = new Vector<TbCardInfo>();
	    	new TbCardMgr(conn).queryMultiple("card_no='" + cardNo + "'", result);
	    	
	    	info = result.get(0);
		}
    	return info;
	}

	/*private String getExpiryDate(Connection conn) throws Exception {
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
    	new TbCardMgr(conn).queryMultiple("card_no='" + cardNo + "'", result);

    	if(result.size() >0)
    	{
    		expiryDate = result.get(0).getExpiryDate();
    		return expiryDate;
    	}    	
    	return null;
	}*/

	public Map<String, String> getFileData() {
		return fileData;
	}
	
	/*public String getExpiryDate() {
		return expiryDate;
	}*/
	
	public int getSumBonusQty() {
		return sumBonusQty;
	}
	
	public int getFailCnt() {
		return failCnt;
	}

	public void setFailCnt(int failCnt) {
		this.failCnt = failCnt;
	}

    public List<String> handleAppload(Connection conn, String batchDate, String fileName, 
    							Map<String, FieldInfo> apploadFieldInfos, int number) throws Exception {
    	
    	List<String> sqls = new ArrayList<String>();
    	errorDesc = "";
    	
    	boolean flag = checkData(conn, apploadFieldInfos);
    		
    	if(flag != true) {
    		impFileInfo.setFailCnt(impFileInfo.getFailCnt() + 1);
    		TbInctlErrInfo inctlErrInfo = ImpFilesUtil.makeInctlErrInfo(inctlInfo, lineInfo, descInfo);
            TbInctlErrMgr mgr = new TbInctlErrMgr(conn);
            mgr.insert(inctlErrInfo);
            
            /*String insertCardBalSql = makeCardBal(rs, batchDate, fileName, flag);
        	log.info("sql: " + insertCardBalSql);
        	sqls.add(insertCardBalSql);*/
    	}
    	
    	String insertApploadSql = makeAppload(batchDate, fileName, number, flag);
    	log.info("sql: " + insertApploadSql);
    	sqls.add(insertApploadSql);
    	
    	String insertApploadDtlSql = makeApploadDtl(conn, batchDate, fileName, flag);   
    	log.info("sql: " + insertApploadDtlSql);
    	sqls.add(insertApploadDtlSql);       
        
        return sqls;
    } 

	private boolean checkData(Connection conn, Map<String, FieldInfo> apploadFieldInfos) throws Exception {
    	String productId = getFileData().get("PRODUCT_ID");
    	String hgOrderNo = getFileData().get("HG_ORDER_NO");
    	String exchangeSeqNo = getFileData().get("EXCHANGE_SEQNO");
    	
    	if(cardInfo == null) {
    		errorDesc = "card no (" + cardNo + ") is not exist or tb_hg_card_map record size > 1 !";
    		descInfo = ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2710_INVALID_ERR, 
    				apploadFieldInfos.get("CARD_NO"), errorDesc);
    		
    		return false;
    	}
    	
    	if(!cardInfo.getStatus().equals(CARD_STATUS_OPENED)) {
    		errorDesc = "card (" + cardNo + ") status (" + cardInfo.getStatus() + ") had not opened !";
    		descInfo = ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2710_INVALID_ERR, 
    				apploadFieldInfos.get("CARD_NO"), errorDesc);
    		
    		return false;
    	}
    	
    	if(!cardInfo.getLifeCycle().equals(CARD_LIFE_CYCLE_OPENED) && !cardInfo.getLifeCycle().equals(CARD_LIFE_CYCLE_DOING_TXN)) {
    		errorDesc = "card (" + cardNo + ") life cycle (" + cardInfo.getLifeCycle() + ") had not opened or doing transaction !";
    		descInfo = ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2710_INVALID_ERR, 
    				apploadFieldInfos.get("CARD_NO"), errorDesc);
    		
    		return false;
		}

    	if(checkUpcase(productId.trim())) {
    		errorDesc = "product id(" + productId + ") is not upcase !";
    		descInfo = ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2710_INVALID_ERR, 
    				apploadFieldInfos.get("PRODUCT_ID"), errorDesc);
    		
    		return false;
    	}
    	
    	if(checkUpcase(hgOrderNo.trim())) {
    		errorDesc = "hg order no(" + hgOrderNo + ") is not upcase !";
    		descInfo = ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2710_INVALID_ERR, 
    				apploadFieldInfos.get("HG_ORDER_NO"), errorDesc);
    		
    		return false;
    	}
    	
    	if(checkExistAppload(conn, hgOrderNo.trim(), exchangeSeqNo.trim())) {
    		errorDesc = "hg order no(" + hgOrderNo + ") + exchange seqno(" + exchangeSeqNo + ") is exist !";
    		descInfo = ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2710_INVALID_ERR, 
    				apploadFieldInfos.get("HG_ORDER_NO"), errorDesc);
    		
    		return false;
    	}	
    	
    	int currCardBal = getSumBonusQty() + Integer.valueOf(getFileData().get("BONUS_QTY"));
    	if(checkExchangeLimit(conn, currCardBal)) {
    		errorDesc = "curren card balance(CARD BALANCE + BONUS_QTY) > Exchange Limit:" + currCardBal;
    		descInfo = ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2710_INVALID_ERR, 
    				apploadFieldInfos.get("BONUS_QTY"), errorDesc);
    		
    		return false;
    	}	
		return true;
	}
	
	private boolean checkExchangeLimit(Connection conn, int currCardBal) throws SQLException 
	{
    	if( currCardBal < exchangeLimit)
        {
        	return false;
        }	
    	return true;
	}

	private boolean checkExistAppload(Connection conn, String hgOrderNo, String exchangeSeqNo) throws Exception {
		TbAppointReloadMgr mgr = new TbAppointReloadMgr(conn);
		String where = "HG_ORDER_NO='" + hgOrderNo + "' AND EXCHANGE_SEQNO='" + exchangeSeqNo + "'";
		if(mgr.getCount(where) <= 0) {
			return false;
		}
		return true;
	}

	private boolean checkUpcase(String str) {
		Pattern ptern = Pattern.compile("[A-Z0-9]*");
		if(ptern.matcher(str).matches())
		{
			return false;
		}
		else {
			return true;
		}	
	}
	
/*	private String makeCardBal(ResultSet rs, String batchDate, String fileName, boolean flag) throws Exception {
		
		TbCardBalInfo info = new TbCardBalInfo();
		while (rs.next()) {		
			info.setCardNo("986" + getFileData().get("CARD_NO"));
			info.setExpiryDate(expiryDate);
			info.setBonusId(rs.getString("BONUS_ID"));
			info.setBonusSdate(rs.getString("BONUS_SDATE"));
			info.setBonusEdate(rs.getString("BONUS_EDATE"));
			info.setCrBonusQty(Integer.valueOf(getFileData().get("BONUS_QTY")));
		}
		return info.toInsertSQL();
	}*/

	private String makeAppload(String batchDate, String fileName, int number, boolean flag) throws Exception {

    	TbAppointReloadInfo info = new TbAppointReloadInfo();
    	
    	info.setBonusBase("C");
    	info.setBalanceType("C");
    	info.setRegionId("TWM");
    	//info.setBalanceId("986" + getFileData().get("CARD_NO"));
    	//info.setCardNo("986" + getFileData().get("CARD_NO"));
    	
    	if(cardInfo == null) {
    		info.setBalanceId(getFileData().get("BARCODE1"));
        	info.setCardNo(getFileData().get("BARCODE1"));
    		info.setExpiryDate("20991231");
    	}
    	else {
    		info.setBalanceId(cardNo);
        	info.setCardNo(cardNo);
    		info.setExpiryDate(cardInfo.getExpiryDate());
    	}
    	info.setArSerno(arSerno);
    	info.setArSrc("B");
    	info.setAcqMemId("00000000");
    	info.setMerchId("000000000000000");
    	info.setExMemId(BatchUtils.getSysConfigValue("EX_MEM_ID"));
    	info.setValidSdate(batchDate);
    	info.setLineNo(number);
    	info.setValidEdate("99991231");
    	
    	if(flag) {
    		info.setStatus("0");
    		info.setProcFlag("1");
    	}
    	else {
    		info.setStatus("F");
    		info.setProcFlag("0");
    		info.setErrorDesc(errorDesc);
    	}
    	info.setInfile(fileName);
    	info.setExchangeDate(getFileData().get("EXCHANGE_DATE").trim());
    	info.setProductId(getFileData().get("PRODUCT_ID").trim());
    	info.setHgOrderNo(getFileData().get("HG_ORDER_NO").trim());
    	info.setExchangeSeqno(getFileData().get("EXCHANGE_SEQNO").trim()); 
    	info.setProcDate(sysDate);
    	info.setParMon(sysDate.substring(4, 6));
    	info.setParDay(sysDate.substring(6, 8));
    	info.setUptUserid("BATCH");
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime);
    	info.setAprvUserid("BATCH");
    	info.setAprvDate(sysDate);
    	info.setAprvTime(sysTime);
    	
        return info.toInsertSQL();
    }
    

	private String makeApploadDtl(Connection conn, String batchDate, String fileDate, boolean flag) throws Exception 
	{
    	
    	String sql = "SELECT TB_BONUS_DTL.BONUS_ID, TB_BONUS_DTL.BONUS_SDATE, TB_BONUS_DTL.BONUS_EDATE FROM TB_BONUS_ISS_DEF, TB_BONUS_DTL " +
				 	 "WHERE EXISTS (SELECT * FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,1,1)='1' AND TB_BONUS_ISS_DEF.MEM_ID =TB_MEMBER.MEM_ID) " +
				 	 "AND TB_BONUS_ISS_DEF.POINT1_BONUS_ID=TB_BONUS_DTL.BONUS_ID";
    	Statement stmt = conn.createStatement();
    	ResultSet rs = stmt.executeQuery(sql);
 
	    TbAppointReloadDtlInfo info = new TbAppointReloadDtlInfo();
	    
	    while(rs.next()) {
	    	info.setBonusBase("C");
	    	info.setBalanceType("C");
	    	//info.setBalanceId("986" + getFileData().get("CARD_NO"));
	    	//info.setCardNo("986" + getFileData().get("CARD_NO"));
	    	
	    	if(cardInfo == null) {
	    		info.setBalanceId(getFileData().get("BARCODE1"));
		    	info.setCardNo(getFileData().get("BARCODE1"));
	    		info.setExpiryDate("20991231");
	    	}
	    	else {
	    		info.setBalanceId(cardNo);
		    	info.setCardNo(cardNo);
	    		info.setExpiryDate(cardInfo.getExpiryDate());
	    	}
	    	info.setArSerno(arSerno);
	    	info.setBonusId(rs.getString("BONUS_ID"));
	    	info.setBonusSdate(rs.getString("BONUS_SDATE"));
	    	info.setBonusEdate(rs.getString("BONUS_EDATE"));
	    	info.setExchangePoint(Integer.valueOf(getFileData().get("EXCHANGE_POINT")));
	    	if(flag) {
	    		info.setBonusQty(Integer.valueOf(getFileData().get("BONUS_QTY")));
	    		//info.setExchangePoint(Integer.valueOf(getFileData().get("EXCHANGE_POINT")));
	    	}
	    	else {
	    		info.setBonusQty(0);
	    		//info.setExchangePoint(0);
	    	}
	    	info.setExchangeAmt(Double.valueOf(getFileData().get("BONUS_QTY")));
	    	info.setParMon(sysDate.substring(4, 6));
	    	info.setParDay(sysDate.substring(6, 8));
	    	info.setUptUserid("BATCH");
	    	info.setUptDate(sysDate);
	    	info.setUptTime(sysTime);
	    	info.setAprvUserid("BATCH");
	    	info.setAprvDate(sysDate);
	    	info.setAprvTime(sysTime);
    	}
        ReleaseResource.releaseDB(null,null,rs);

        return info.toInsertSQL();
    }
}
