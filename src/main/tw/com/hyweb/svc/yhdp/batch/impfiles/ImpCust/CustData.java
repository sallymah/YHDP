package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCust;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jpos.iso.ISOUtil;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCountryInfo;
import tw.com.hyweb.service.db.info.TbCustInfo;
import tw.com.hyweb.service.db.info.TbCustUptInfo;
import tw.com.hyweb.service.db.info.TbTelcoCardDtlInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbCountryMgr;
import tw.com.hyweb.service.db.mgr.TbCustMgr;
import tw.com.hyweb.service.db.mgr.TbCustUptMgr;
import tw.com.hyweb.service.db.mgr.TbTelcoCardDtlMgr;
import tw.com.hyweb.svc.yhdp.online.CacheTbSysConfig;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.BatchUtils;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class CustData
{
    private static Logger log = Logger.getLogger(CustData.class);

    private String UPT_BATCH_USER_ID = "BATCH";
    private String UPT_STATUS_ADD = "1";
    private String UPT_STATUS_MODIFY = "2";
    private String APRV_STATUS_APPROVED = "1";
    private String date = DateUtils.getSystemDate();   
    private String time = DateUtils.getSystemTime();
    
    private String uptTime;
    private String value;
    
    private Map<String, String> fileData;
    
	private Vector<TbCardInfo> cardResult = new Vector<TbCardInfo>();
    private Vector<TbCustInfo> custResult = new Vector<TbCustInfo>();
    private Vector<TbCountryInfo> countryResult = new Vector<TbCountryInfo>();
    private int cardCount;
    private int custCount;
    private int countryCodeCount;


	//電信卡邏輯
    private boolean isTelcoCard;
    private boolean isMobileCount;
    private int mobileCount;
    //ECA_MERCH_ID
    private String merchId;
    
   
    public CustData() { }

    public void initial(Connection conn) throws Exception 
    {
    	this.cardCount = getCardInfo(conn , fileData.get("CARD_NO"));
    	this.custCount = getCustInfo(conn , getCardInfo().getCustId());
    	this.countryCodeCount=getCountryCodeInfo(conn,fileData.get("COUNTRY_CODE"));
    	this.isTelcoCard = isTelcoCard(conn , fileData.get("CARD_NO"));
    	this.isMobileCount = !StringUtil.isEmpty(fileData.get("MOBILE"));
    	this.mobileCount = getMobileCount(conn , fileData.get("MOBILE"));
    	this.merchId = getMerchId(conn , fileData.get("MERCH_ID"));
    	this.value = CacheTbSysConfig.getInstance().getValue("IS_CUST_ENCRIPT");
    }
    
    private int getCardInfo(Connection connection, String cardNumber) throws SQLException
    {
        TbCardInfo info = new TbCardInfo();
        info.setCardNo(cardNumber);
        log.info("card no: " + cardNumber);

        return new TbCardMgr(connection).queryMultiple(info, cardResult);
    }
    
    private int getCustInfo(Connection connection, String custId) throws SQLException
    {
    	if (StringUtil.isEmpty(custId)){
    		return 0;
    	}
        TbCustInfo info = new TbCustInfo();
        info.setCustId(custId);

        return new TbCustMgr(connection).queryMultiple(info, custResult);
    }
    private int getCountryCodeInfo(Connection connection, String countryCode) throws SQLException
    {
    	if (StringUtil.isEmpty(countryCode)){
    		return 0;
    	}
    	TbCountryInfo info = new TbCountryInfo();
        info.setCountryCode(countryCode);

        return new TbCountryMgr(connection).queryMultiple(info, countryResult);
    }
    private boolean isTelcoCard(Connection connection, String cardNumber) throws SQLException
    {
    	StringBuffer sql = new StringBuffer();
    	sql.append(" SELECT COUNT(*) FROM TB_CARD");
    	sql.append(" WHERE CARD_NO=").append(StringUtil.toSqlValueWithSQuote(cardNumber));
    	sql.append(" AND EXISTS (SELECT 1 FROM TB_TELCO_CARD_DTL WHERE TB_CARD.MIFARE_UL_UID=TB_TELCO_CARD_DTL.MIFARE_UL_UID)");
        int count = DbUtil.getInteger(sql.toString(), connection);

        return count > 0;
    }
    
    private int getMobileCount(Connection connection, String mobile) throws SQLException
    {
    	if ( StringUtil.isEmpty(mobile) ){
    		return 0;
    	}

    	Vector<TbCustInfo> mobileResult = new Vector<TbCustInfo>();
    	
    	TbTelcoCardDtlInfo info = new TbTelcoCardDtlInfo();
        info.setMobile(mobile);

        return new TbTelcoCardDtlMgr(connection).queryMultiple(info, mobileResult);
    }
    
    private String getMerchId(Connection connection, String merchId) throws SQLException{
    	String sortMerchId = "";
    	sortMerchId = DbUtil.getString("SELECT MERCH_ID FROM TB_MERCH WHERE MERCH_ID = " + StringUtil.toSqlValueWithSQuote(merchId), connection);
    	if (!StringUtil.isEmpty(sortMerchId)){
    		return sortMerchId;
    	}
    	sortMerchId = DbUtil.getString("SELECT MERCH_ID FROM TB_MERCH WHERE ECA_MERCH_ID = " + StringUtil.toSqlValueWithSQuote(merchId), connection);
    	return sortMerchId;
    }
    
    public String getUptTime() {
		return uptTime;
	}

	public void setUptTime(String uptTime) {
		this.uptTime = uptTime;
	}
    
    public void setFileData(Map<String, String> fileData) {
		this.fileData = fileData;
	}

	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getCardInfoCount() {
		return cardCount;
	}
	
	public int getCustInfoCount() {
		return custCount;
	}
	
	public boolean isTelcoCard() {
		return isTelcoCard;
	}

	public boolean isMobileCount() {
		return isMobileCount;
	}

	public int getMobileCount() {
		return mobileCount;
	}

	public String getMerchId() {
		return merchId;
	}
	
	public TbCardInfo getCardInfo() {	
		return cardResult.get(0);
	}
	
	public TbCustInfo getCustInfo() {	
		return custResult.get(0);
	}
		
	public TbCountryInfo getCountryCodeInfo() {	
		return countryResult.get(0);
	}
	
    public int getCountryCodeCount() {
		return countryCodeCount;
	}

	public void setCountryCodeCount(int countryCodeCount) {
		this.countryCodeCount = countryCodeCount;
	}
    public List handleCust(Connection conn, String batchDate, String fileDate, String fullfilename) throws Exception 
    {
    	Vector<TbCustInfo> result = new Vector<TbCustInfo>();
    	List sqls = new ArrayList();
    	String custSQL = "";	
    	String cardSQL = "";
    	String custUptSQL = "";
    	String cardUptSQL = "";
    	String telcoCardDtlSQL = "";
    	String custId = "";

        log.debug("getCustId="+getCardInfo().getCustId());
    	if(StringUtil.isEmpty(getCardInfo().getCustId())) 
    	{
    		//String where = "PERSON_ID='" + fileData.get("PERSON_ID") + "'";
    		String where = "PERSON_ID='" + BatchUtils.encript(fileData.get("PERSON_ID")) + "'";
    		int custCnt = new TbCustMgr(conn).queryMultiple(where, result);
            log.debug("custCnt="+custCnt);
    		if(custCnt == 0) 
    		{
    			custId = createCustId(conn);
    			
    			TbCustInfo custInfo = insertCust(conn, batchDate, fileDate, custId, fullfilename);
    			
    			if(null != custInfo)
                { 				
        			TbCustUptInfo custUptInfo = YHDPUtil.copyCustData(custInfo);
        			custUptInfo.setImpFileName(fullfilename);
        			custUptInfo.setUptUserid(UPT_BATCH_USER_ID);
                    custUptInfo.setUptDate(date);
                    custUptInfo.setUptTime(time);
                    custUptInfo.setAprvUserid(UPT_BATCH_USER_ID);
                    custUptInfo.setAprvDate(date);
                    custUptInfo.setAprvTime(time);
                    custUptInfo.setUptStatus(UPT_STATUS_ADD);
                    custUptInfo.setAprvStatus(APRV_STATUS_APPROVED);
                    custUptInfo.setSaNo(fileData.get("SA_NO"));
                    custUptInfo.setHgAuth(fileData.get("HG_AUTH"));
                    custUptInfo.setSyncFlag("0");
                    custUptInfo.setCountryCode(fileData.get("COUNTRY_CODE"));
                    TbCustUptMgr custUptMgr = new TbCustUptMgr(conn);
                    custUptMgr.insert(custUptInfo);
                }		
    		}	
    		else {
    			custId = result.get(0).getCustId();
    		}
    		cardSQL = updateCard(conn, batchDate, custId);
        	sqls.add(cardSQL);
        	
        	cardUptSQL = insertCardUpt(custId, UPT_STATUS_MODIFY);
        	sqls.add(cardUptSQL);
    	}else {
    		custSQL = updateCust(batchDate, fileDate, fullfilename);
    		sqls.add(custSQL);
    		
    		cardSQL = updateCard(conn, batchDate, null);
        	sqls.add(cardSQL);
        	
        	custUptSQL = insertCustUpt(UPT_STATUS_MODIFY, fullfilename);
        	sqls.add(custUptSQL);
        	
        	cardUptSQL = insertCardUpt(null,UPT_STATUS_MODIFY);
        	sqls.add(cardUptSQL);
    	}
    	//電信卡TB_TELCO_CARD_DTL
    	if( isTelcoCard ){
    		telcoCardDtlSQL = "UPDATE TB_TELCO_CARD_DTL SET MOBILE = " + StringUtil.toSqlValueWithSQuote(fileData.get("MOBILE"))+
    						" WHERE CARD_NO = " + StringUtil.toSqlValueWithSQuote(fileData.get("CARD_NO"));
    		
    		sqls.add(telcoCardDtlSQL);
    	}
    	
        return sqls;
    }   
    
    private String createCustId(Connection conn) throws Exception 
    {
    	int nexVal = BatchUtils.getNextValFromSeq(conn, "SEQ_CUST_ID");
    	
        Calendar cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat("yyyyMMdd").parse(date));
        String yyy = DateUtils.getSystemDate().substring(1, 4);
        String jDay = ""+cal.get(Calendar.DAY_OF_YEAR);        
        jDay = ISOUtil.padleft(jDay, 3, '0');
        
        String custId = "B" + yyy + jDay + ISOUtil.padleft(""+nexVal, 6, '0');
        
        return custId;
	}

	private TbCustInfo insertCust(Connection conn, String batchDate, String fileDate, String custId, String fullfilename) throws Exception 
    {
    	TbCustInfo custInfo = new TbCustInfo();
    	custInfo.setRegionId(getCardInfo().getRegionId());
        custInfo.setCustId(custId); 
              
        if(Integer.valueOf(value).intValue() == 1) 
        {
        	custInfo.setGender(BatchUtils.encript(fileData.get("GENDER")));
        	custInfo.setBirthday(BatchUtils.encript(fileData.get("BIRTHDAY")));
        	custInfo.setPersonId(BatchUtils.encript(fileData.get("PERSON_ID")));
        	custInfo.setMobile(BatchUtils.encript(fileData.get("MOBILE")));
            custInfo.setTelHome(BatchUtils.encript(fileData.get("TEL_HOME")));
            custInfo.setEmail(BatchUtils.encript(fileData.get("EMAIL")));
            custInfo.setLocName(BatchUtils.encript(fileData.get("LOC_NAME")));
            custInfo.setZipCode(BatchUtils.encript(fileData.get("ZIP_CODE")));
            custInfo.setAddress(BatchUtils.encript(fileData.get("ADDRESS"))); 
            custInfo.setLegalAgentName(BatchUtils.encript(fileData.get("LEGAL_AGENT_NAME")));
            custInfo.setLegalAgentPid(BatchUtils.encript(fileData.get("LEGAL_AGENT_PID")));
            custInfo.setLegalAgentMobile(BatchUtils.encript(fileData.get("LEGAL_AGENT_MOBILE")));
            custInfo.setLegalAgentPhone(BatchUtils.encript(fileData.get("LEGAL_AGENT_PHONE")));
        }
        else 
        {
        	custInfo.setGender(fileData.get("GENDER"));
        	custInfo.setBirthday(fileData.get("BIRTHDAY"));
        	custInfo.setPersonId(fileData.get("PERSON_ID"));
        	custInfo.setMobile(fileData.get("MOBILE"));
            custInfo.setTelHome(fileData.get("TEL_HOME"));
            custInfo.setEmail(fileData.get("EMAIL"));
            custInfo.setLocName(fileData.get("LOC_NAME"));
            custInfo.setZipCode(fileData.get("ZIP_CODE"));
            custInfo.setAddress(fileData.get("ADDRESS"));
            custInfo.setLegalAgentName(fileData.get("LEGAL_AGENT_NAME"));
            custInfo.setLegalAgentPid(fileData.get("LEGAL_AGENT_PID"));
            custInfo.setLegalAgentMobile(fileData.get("LEGAL_AGENT_MOBILE"));
            custInfo.setLegalAgentPhone(fileData.get("LEGAL_AGENT_PHONE"));
        }
        custInfo.setMarriage(fileData.get("MARRIAGE"));
        custInfo.setCity(fileData.get("CITY"));
        custInfo.setDmFlag(fileData.get("DM_FLAG"));
        custInfo.setMerchId(merchId);
        custInfo.setMbrRegDate(fileData.get("MBR_REG_DATE"));
        custInfo.setUptUserid(UPT_BATCH_USER_ID);
        custInfo.setUptDate(date);
        custInfo.setUptTime(DateUtils.getSystemTime());
        custInfo.setAprvUserid(UPT_BATCH_USER_ID);
        custInfo.setAprvDate(date);
        custInfo.setAprvTime(DateUtils.getSystemTime());
        custInfo.setSaNo(fileData.get("SA_NO"));
        custInfo.setHgAuth(fileData.get("HG_AUTH"));
        custInfo.setCountryCode(fileData.get("COUNTRY_CODE"));
        custInfo.setSyncFlag("0");
        custInfo.setImpFileName(fullfilename);
        TbCustMgr custMgr = new TbCustMgr(conn);
        custMgr.insert(custInfo);
        custResult.add(custInfo);
        
        return custInfo;
	}

	private String updateCard(Connection conn, String batchDate, String custId) 
    {
    	String sql = null;
    	Vector<String> parameterValues = new Vector<String>();
    	
    	if(getCustInfo().getRegRecycleStatus().equals("0")) {
    		if(custId != null) {
    			//sql = "UPDATE TB_CARD SET CUST_ID=?, CARDHOLDER_ID=?, VIP_FLAG=? WHERE CARD_NO=?";
    			sql = "UPDATE TB_CARD SET CUST_ID=?, MBR_REG_DATE=?, CARDHOLDER_ID=?, VIP_FLAG=? WHERE CARD_NO=?";
    			parameterValues.add(custId);
        		parameterValues.add(fileData.get("MBR_REG_DATE"));
    		}
    		else {
    			sql = "UPDATE TB_CARD SET CARDHOLDER_ID=?, VIP_FLAG=? WHERE CARD_NO=?";
    			//sql = "UPDATE TB_CARD SET MBR_REG_DATE=?, CARDHOLDER_ID=?, VIP_FLAG=? WHERE CARD_NO=?";
        		//parameterValues.add(fileData.get("MBR_REG_DATE"));
    		}
    	}
    	else {
    		if(custId != null) {
    			sql = "UPDATE TB_CARD SET CUST_ID=?, CARDHOLDER_ID=?, VIP_FLAG=? WHERE CARD_NO=?";
    			//sql = "UPDATE TB_CARD SET CUST_ID=?, MBR_REG_DATE=?, CARDHOLDER_ID=?, VIP_FLAG=? WHERE CARD_NO=?";
    			parameterValues.add(custId);
    			//parameterValues.add(fileData.get("MBR_REG_DATE"));
    		}
    		else {
    			sql = "UPDATE TB_CARD SET CARDHOLDER_ID=?, VIP_FLAG=? WHERE CARD_NO=?";
    		}
    	}
    	parameterValues.add(fileData.get("PERSON_ID"));
    	parameterValues.add(fileData.get("VIP_FLAG"));
    	parameterValues.add(fileData.get("CARD_NO"));
    	
    	String sqlString = DbUtil.transferPrepareStatementToSqlCommand(sql, parameterValues);
	
        return sqlString;
    }
    
    private String updateCust(String batchDate, String fileDate, String fullfilename) throws Exception 
    {   	  	
    	/*String sql = "UPDATE TB_CUST SET LOC_NAME=?, PERSON_ID=?, GENDER=?, BIRTHDAY=?, CITY=?, ZIP_CODE=?, ADDRESS=?, TEL_HOME=?, MOBILE=?, " +
    				 "EMAIL=?, DM_FLAG=?, LEGAL_AGENT_NAME=?, LEGAL_AGENT_PID=?, LEGAL_AGENT_MOBILE=?, LEGAL_AGENT_PHONE=?, VIP_FLAG=?, " +
    				 "MERCH_ID=?, MARRIAGE=?";*/
    	
    	String sql = "UPDATE TB_CUST SET LOC_NAME=?, PERSON_ID=?, GENDER=?, BIRTHDAY=?, CITY=?, ZIP_CODE=?, ADDRESS=?, TEL_HOME=?, MOBILE=?, " +
				 "EMAIL=?, DM_FLAG=?, LEGAL_AGENT_NAME=?, LEGAL_AGENT_PID=?, LEGAL_AGENT_MOBILE=?, LEGAL_AGENT_PHONE=?, " +
				 "MERCH_ID=?, MARRIAGE=?";
    	
    	if(getCustInfo().getRegRecycleStatus().equals("0")) {
    		sql=sql+", REG_RECYCLE_STATUS=?, REG_RECYCLE_DATE=?";
    		//sql=sql+", MBR_REG_DATE=?, REG_RECYCLE_STATUS=?, REG_RECYCLE_DATE=?";
    	}    	
    	sql=sql+",UPT_USERID=?,UPT_DATE=?,UPT_TIME=?,APRV_USERID=?,APRV_DATE=?,APRV_TIME=?,SA_NO=?,HG_AUTH=?,SYNC_FLAG=?,COUNTRY_CODE=?,IMP_FILE_NAME=?";
    				 
    	String where = " WHERE CUST_ID=?"; 	
    	sql = sql + where;
    	
    	Vector<String> parameterValues = new Vector<String>();
    	log.info("value: " + value);
    	if(Integer.valueOf(value).intValue() == 1) {
    		parameterValues.add(BatchUtils.encript(fileData.get("LOC_NAME")));
    		parameterValues.add(BatchUtils.encript(fileData.get("PERSON_ID")));
    	}
    	else {
    		parameterValues.add(fileData.get("LOC_NAME"));
    		parameterValues.add(fileData.get("PERSON_ID"));
    	}
    	
    	if(Integer.valueOf(value).intValue() == 1) {
    		parameterValues.add(BatchUtils.encript(fileData.get("GENDER")));
    		parameterValues.add(BatchUtils.encript(fileData.get("BIRTHDAY")));
    	}
    	else {
    		parameterValues.add(fileData.get("GENDER"));
    		parameterValues.add(fileData.get("BIRTHDAY"));
    	}
    	
    	parameterValues.add(fileData.get("CITY"));
    	if(Integer.valueOf(value).intValue() == 1) {
	    	parameterValues.add(BatchUtils.encript(fileData.get("ZIP_CODE")));
	    	parameterValues.add(BatchUtils.encript(fileData.get("ADDRESS")));
	    	parameterValues.add(BatchUtils.encript(fileData.get("TEL_HOME")));
	    	parameterValues.add(BatchUtils.encript(fileData.get("MOBILE")));
	    	parameterValues.add(BatchUtils.encript(fileData.get("EMAIL")));
    	}
    	else {
    		parameterValues.add(fileData.get("ZIP_CODE"));
	    	parameterValues.add(fileData.get("ADDRESS"));
	    	parameterValues.add(fileData.get("TEL_HOME"));
	    	parameterValues.add(fileData.get("MOBILE"));
	    	parameterValues.add(fileData.get("EMAIL"));
    	}
    	
    	parameterValues.add(fileData.get("DM_FLAG"));
    	if(Integer.valueOf(value).intValue() == 1) {
	    	parameterValues.add(BatchUtils.encript(fileData.get("LEGAL_AGENT_NAME")));
	    	parameterValues.add(BatchUtils.encript(fileData.get("LEGAL_AGENT_PID")));
	    	parameterValues.add(BatchUtils.encript(fileData.get("LEGAL_AGENT_MOBILE")));
	    	parameterValues.add(BatchUtils.encript(fileData.get("LEGAL_AGENT_PHONE")));
    	}
    	else {
    		parameterValues.add(fileData.get("LEGAL_AGENT_NAME"));
	    	parameterValues.add(fileData.get("LEGAL_AGENT_PID"));
	    	parameterValues.add(fileData.get("LEGAL_AGENT_MOBILE"));
	    	parameterValues.add(fileData.get("LEGAL_AGENT_PHONE"));
    	}
    	
    	//parameterValues.add(fileData.get("VIP_FLAG"));
    	parameterValues.add(merchId);
    	parameterValues.add(fileData.get("MARRIAGE"));
    	
    	if(getCustInfo().getRegRecycleStatus().equals("0")) {
    		//parameterValues.add(fileData.get("MBR_REG_DATE"));
    		parameterValues.add("1");
    		parameterValues.add(date);
    	}
    	parameterValues.add(UPT_BATCH_USER_ID);
    	parameterValues.add(date);
    	parameterValues.add(DateUtils.getSystemTime());
    	parameterValues.add(UPT_BATCH_USER_ID);
    	parameterValues.add(date);
    	parameterValues.add(DateUtils.getSystemTime());
    	parameterValues.add(fileData.get("SA_NO"));
    	parameterValues.add(fileData.get("HG_AUTH"));
    	parameterValues.add("0");
    	parameterValues.add(fileData.get("COUNTRY_CODE"));
    	parameterValues.add(fullfilename);
    	parameterValues.add(getCardInfo().getCustId());
    	
    	
    	String sqlString = DbUtil.transferPrepareStatementToSqlCommand(sql, parameterValues);
	
        return sqlString;
    }
    
    private String insertCardUpt(String custId, String uptStatus) throws Exception 
    {   
    	uptTime = BatchUtils.getNextSec(uptTime,1);
	    log.debug("uptTime="+uptTime);
    	
    	StringBuffer sql= new StringBuffer();
    	Vector<String> parameterValues = new Vector<String>();
    	sql.append(" Insert into TB_CARD_UPT (REGION_ID ,MEM_ID ,CUST_ID ,CARDHOLDER_ID ,ASSOCIATOR_ID ,ACCT_ID");
    	sql.append(" ,CARD_PRODUCT ,CARD_NO ,EXPIRY_DATE ,ACTIVE_DATE ,CREDIT_CARD_NO ,CREDIT_EXPIRY_DATE ,CREDIT_ACTIVE_DATE");
    	sql.append(" ,CREDIT_APPROVAL_DATE ,PRIMARY_CARD ,CARD_LEVEL ,CARD_PLAN ,BILL_CUT_DAY ,STATUS ,LIFE_CYCLE ,STATUS_UPDATE_DATE");
    	sql.append(" ,BAL_TRANSFER_FLAG ,FAIL_CODE ,REG_DATE ,REG_TIME ,OUTFILE ,INFILE ,PERSO_BATCH_NO ,NEW_BILL_CUT_DAY");
    	sql.append(" ,BILL_VALID_DATE ,KEY_VERSION ,ACTIVE_FLAG ,INACTIVE_DATE ,CLEAN_DATE ,FIRST_TXN_DATE ,LAST_TXN_DATE");
    	sql.append(" ,UD1 ,UD2 ,UD3 ,UD4 ,UD5 ,DELIVERY_STATUS ,LAST_CARD_NO ,LAST_EXPIRY_DATE ,LAST_LMS_INVOICE_NO ,LAST_EDC_DATE_TIME");
    	sql.append(" ,CARD_FEE ,PRELOAD_AMT ,PRELOAD_DW_DATE ,PREVIOUS_STATUS ,MIFARE_UL_UID ,SHOW_CARD_NO ,PB_VALID_SDATE ,PB_VALID_EDATE");
    	sql.append(" ,ACTIVE_CARD_FLAG ,LAST_RELOAD_DATE ,CARD_OWNER ,CARD_OPEN_OWNER ,FIRST_RELOAD_DATE ,MIN_BAL_AMT ,BOX_NO");
    	sql.append(" ,WARRANTY_PERIOD ,MASS_BATCH_NO ,CO_BRAND_ENT_ID ,MBR_REG_DATE ,HG_CARD_NO ,CARD_TYPE_ID ,CARD_CAT_ID");
    	sql.append(" ,AUTO_RELOAD_FLAG ,AUTO_RELOAD_VALUE ,CUSTOMER_ID ,SALE_WAY ,VIP_FLAG ,RETURN_CARD_WAY ,TEST_FLAG ,BANK_ID");
    	sql.append(" ,AUTO_RLD_END_DATE ,PTA_UNIT_NO ,CREDIT_CARD_TYPE ,PREVIOUS_LIFE_CYCLE ,AUTO_RELOAD_DATE ,ISSUE_DATE, IS_SYNC_HG");
    	sql.append(" ,UPT_USERID,UPT_DATE,UPT_TIME,APRV_USERID,APRV_DATE,APRV_TIME,UPT_STATUS,APRV_STATUS)");
    	if(custId != null) {
    		sql.append(" select REGION_ID ,MEM_ID ,? ,CARDHOLDER_ID ,ASSOCIATOR_ID ,ACCT_ID");
    	}else {
    		sql.append(" select REGION_ID ,MEM_ID ,CUST_ID ,CARDHOLDER_ID ,ASSOCIATOR_ID ,ACCT_ID");
    	}
    	sql.append(" ,CARD_PRODUCT ,CARD_NO ,EXPIRY_DATE ,ACTIVE_DATE ,CREDIT_CARD_NO ,CREDIT_EXPIRY_DATE ,CREDIT_ACTIVE_DATE");
    	sql.append(" ,CREDIT_APPROVAL_DATE ,PRIMARY_CARD ,CARD_LEVEL ,CARD_PLAN ,BILL_CUT_DAY ,STATUS ,LIFE_CYCLE ,STATUS_UPDATE_DATE");
    	sql.append(" ,BAL_TRANSFER_FLAG ,FAIL_CODE ,REG_DATE ,REG_TIME ,OUTFILE ,INFILE ,PERSO_BATCH_NO ,NEW_BILL_CUT_DAY");
    	sql.append(" ,BILL_VALID_DATE ,KEY_VERSION ,ACTIVE_FLAG ,INACTIVE_DATE ,CLEAN_DATE ,FIRST_TXN_DATE ,LAST_TXN_DATE");
    	sql.append(" ,UD1 ,UD2 ,UD3 ,UD4 ,UD5 ,DELIVERY_STATUS ,LAST_CARD_NO ,LAST_EXPIRY_DATE ,LAST_LMS_INVOICE_NO ,LAST_EDC_DATE_TIME");
    	sql.append(" ,CARD_FEE ,PRELOAD_AMT ,PRELOAD_DW_DATE ,PREVIOUS_STATUS ,MIFARE_UL_UID ,SHOW_CARD_NO ,PB_VALID_SDATE ,PB_VALID_EDATE");
    	sql.append(" ,ACTIVE_CARD_FLAG ,LAST_RELOAD_DATE ,CARD_OWNER ,CARD_OPEN_OWNER ,FIRST_RELOAD_DATE ,MIN_BAL_AMT ,BOX_NO");
    	sql.append(" ,WARRANTY_PERIOD ,MASS_BATCH_NO ,CO_BRAND_ENT_ID ,MBR_REG_DATE ,HG_CARD_NO ,CARD_TYPE_ID ,CARD_CAT_ID");
    	sql.append(" ,AUTO_RELOAD_FLAG ,AUTO_RELOAD_VALUE ,CUSTOMER_ID ,SALE_WAY ,VIP_FLAG ,RETURN_CARD_WAY ,TEST_FLAG ,BANK_ID");
    	sql.append(" ,AUTO_RLD_END_DATE ,PTA_UNIT_NO ,CREDIT_CARD_TYPE ,PREVIOUS_LIFE_CYCLE ,AUTO_RELOAD_DATE ,ISSUE_DATE, IS_SYNC_HG");
    	sql.append(" ,?,?,?,?,?,?,?,?");
     	sql.append(" from tb_card where CARD_NO=? ");
    	
     	if(custId != null) {
     		parameterValues.add(custId);
    	}
    	parameterValues.add(UPT_BATCH_USER_ID);
    	parameterValues.add(date);
    	parameterValues.add(uptTime);
    	parameterValues.add(UPT_BATCH_USER_ID);
    	parameterValues.add(date);
    	parameterValues.add(uptTime);
    	parameterValues.add(uptStatus);
    	parameterValues.add(APRV_STATUS_APPROVED);
    	parameterValues.add(getCardInfo().getCardNo());
    	String sqlString = DbUtil.transferPrepareStatementToSqlCommand(sql.toString(), parameterValues);
	    log.debug("sqlString="+sqlString.toString());
        return sqlString;
    }
    
    private String insertCustUpt(String uptStatus, String fullfilename) throws Exception 
    {   
    	uptTime = BatchUtils.getNextSec(uptTime,1);
	    log.debug("uptTime="+uptTime);
    	
    	StringBuffer sql= new StringBuffer();
    	Vector<String> parameterValues = new Vector<String>();
    	sql.append(" Insert into TB_CUST_UPT (REGION_ID,CUST_ID,PERSON_TYPE,PERSON_ID,LOC_NAME,ENG_NAME");
    	sql.append(" ,CUST_LEVEL,GENDER,BIRTHDAY,MARRIAGE,CONTACT,TEL_HOME ");
    	sql.append(" ,TEL_OFFICE,MOBILE,ZIP_CODE,ADDRESS,EMAIL,EDUCATION,OCCUPATION ");
    	sql.append(" ,INDUSTRY,ANNUAL_INCOME,UD1,UD2,UD3,UD4,UD5,BILL_CUT_DAY ");
    	sql.append(" ,UPT_USERID,UPT_DATE,UPT_TIME,APRV_USERID,APRV_DATE ");
    	sql.append(" ,APRV_TIME,NEW_BILL_CUT_DAY,BILL_VALID_DATE,MEM_DAY");
    	sql.append(" ,MEMO,CITY,LEGAL_AGENT_NAME,LEGAL_AGENT_PID,LEGAL_AGENT_MOBILE ");
    	sql.append(" ,LEGAL_AGENT_PHONE,VIP_FLAG,DM_FLAG ,MBR_REG_DATE");
    	sql.append(" ,MERCH_ID,REG_RECYCLE_STATUS,REG_RECYCLE_DATE,");
    	sql.append(" MODIFY_DESC,MODIFY_SOURCE,APRV_USER_NAME,");
    	sql.append(" UPT_STATUS,APRV_STATUS,SA_NO,HG_AUTH,SYNC_FLAG,COUNTRY_CODE,IMP_FILE_NAME)");
    	sql.append(" select REGION_ID,CUST_ID,PERSON_TYPE,PERSON_ID,LOC_NAME,ENG_NAME");
    	sql.append(" ,CUST_LEVEL,GENDER,BIRTHDAY,MARRIAGE,CONTACT,TEL_HOME");
    	sql.append(" ,TEL_OFFICE,MOBILE,ZIP_CODE,ADDRESS,EMAIL,EDUCATION,OCCUPATION ");
    	sql.append(" ,INDUSTRY,ANNUAL_INCOME,UD1,UD2,UD3,UD4,UD5,BILL_CUT_DAY ");
    	sql.append(" ,?,?,?,?,? ");
    	sql.append(" ,?,NEW_BILL_CUT_DAY,BILL_VALID_DATE,MEM_DAY ");
    	sql.append(" ,MEMO,CITY,LEGAL_AGENT_NAME,LEGAL_AGENT_PID,LEGAL_AGENT_MOBILE ");
    	sql.append(" ,LEGAL_AGENT_PHONE,VIP_FLAG,DM_FLAG ,MBR_REG_DATE ");
    	sql.append(" ,MERCH_ID,REG_RECYCLE_STATUS,REG_RECYCLE_DATE,");
    	sql.append(" ?,?,?,?,?,?,?,?,?,?  ");
    	sql.append(" from tb_cust where CUST_ID=? ");
    	
    	parameterValues.add(UPT_BATCH_USER_ID);
    	parameterValues.add(date);
    	parameterValues.add(uptTime);
    	parameterValues.add(UPT_BATCH_USER_ID);
    	parameterValues.add(date);
    	parameterValues.add(uptTime);
    	parameterValues.add("檔案匯入");
    	parameterValues.add("BATCH");
    	parameterValues.add("BATCH");
    	parameterValues.add(uptStatus);
    	parameterValues.add(APRV_STATUS_APPROVED);
    	parameterValues.add(fileData.get("SA_NO"));
    	parameterValues.add(fileData.get("HG_AUTH"));
    	parameterValues.add("0");
    	parameterValues.add(fileData.get("COUNTRY_CODE"));
    	parameterValues.add(fullfilename);
    	parameterValues.add(getCardInfo().getCustId());
    	String sqlString = DbUtil.transferPrepareStatementToSqlCommand(sql.toString(), parameterValues);
	    log.debug("sqlString="+sqlString.toString());
        return sqlString;
    }
    
    
}
