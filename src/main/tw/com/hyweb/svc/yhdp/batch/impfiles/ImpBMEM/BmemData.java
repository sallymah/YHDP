package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBMEM;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardUptInfo;
import tw.com.hyweb.service.db.info.TbCustInfo;
import tw.com.hyweb.service.db.info.TbCustUptInfo;
import tw.com.hyweb.service.db.info.TbHgCardMapInfo;
import tw.com.hyweb.service.db.info.TbSysConfigInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbCustMgr;
import tw.com.hyweb.service.db.mgr.TbSysConfigMgr;
import tw.com.hyweb.util.BatchUtils;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class BmemData {

	private static Logger log = Logger.getLogger(BmemData.class);

	private final String sysDate = DateUtils.getSystemDate();
	private Map<String, String> tempMap;

	private String isCustEncript = "0"; // TB_CUST是否要作加解密 1:是 / 0:否
	private String expiryDateFlag = "0";
	private String bankCheckHgFlag = "0";
	private String batchDate = "";

	private Integer cardCnt;
	private String sysTime;
	private String sysTimeMs;


//	private BmemDataBean dataBean = new BmemDataBean();

	public BmemData(Map<String, String> tempMap) {
		this.tempMap = tempMap;
	}

/*	public BmemDataBean getDataBean() {
		return dataBean;
	}*/

	public Integer getCardCnt() {
		return cardCnt;
	}

	/**
	 * Configuration parameters
	 * 
	 * @param conn
	 * @param map
	 * @throws Exception
	 */
	public void setData(Connection conn, String batchDate, String expiryDateFlag, String bankCheckHgFlag)
			throws Exception {
		this.batchDate = batchDate;
		this.expiryDateFlag = expiryDateFlag;
		this.bankCheckHgFlag = bankCheckHgFlag;

		this.isCustEncript = this.getSysConfigValue(conn, "IS_CUST_ENCRIPT");
		this.cardCnt = this.getTbCardCnt(conn, tempMap.get("CARD_NO").toString());
		
		tempMap.put("CUST_ID", this.getCustId(conn, tempMap.get("PERSON_ID").toString()));
	}

	/**
	 * Conversion data to JavaBean
	 * 
	 * @param map
	 *            Import data
	 * @throws Exception
	 */
	/*public void convertBeanFromMap(Map<String, Object> map) throws Exception {
		dataBean.setCardNo(map.get("CARD_NO").toString());
		dataBean.setStatus(map.get("STATUS").toString());
		dataBean.setLocName(map.get("LOC_NAME").toString());
		dataBean.setPersonId(map.get("PERSON_ID").toString());
		dataBean.setGender(map.get("GENDER").toString());
		dataBean.setBirthday(map.get("BIRTHDAY").toString());
		dataBean.setCity(map.get("CITY").toString());
		dataBean.setZipCode(this.formatZipCode(map.get("ZIP_CODE").toString()));
		dataBean.setAddress(map.get("ADDRESS").toString());
		dataBean.setTelHome(map.get("TEL_HOME").toString());
		dataBean.setMobile(map.get("MOBILE").toString());
		dataBean.setEmail(map.get("EMAIL").toString());
		dataBean.setDmFlag(map.get("DM_FLAG").toString());
		dataBean.setLegalAgentName(map.get("LEGAL_AGENT_NAME").toString());
		dataBean.setLegalAgentPid(map.get("LEGAL_AGENT_PID").toString());
		dataBean.setLegalAgentMobile(map.get("LEGAL_AGENT_MOBILE").toString());
		dataBean.setLegalAgentPhone(map.get("LEGAL_AGENT_PHONE").toString());
		dataBean.setVipFlag(map.get("VIP_FLAG").toString());
		dataBean.setIssueDate(map.get("ISSUE_DATE").toString());
		dataBean.setExpiryDate(map.get("EXPIRY_DATE").toString());
		dataBean.setMerchId(map.get("MERCH_ID").toString());
		dataBean.setMarriage(map.get("MARRIAGE").toString());
		dataBean.setSaleCode(map.get("SALE_CODE").toString());
		dataBean.setrCode(map.get("R_CODE").toString());
		dataBean.setHgCardNo(map.get("HG_CARD_NO").toString());
	}*/

	/**
	 * make a SQL Syntax List
	 * 
	 * @param conn
	 * @return List of SQL Syntax
	 * @throws Exception
	 */
	public List<String> makeSqlList(Connection conn, HashMap<String, Integer> statusCntList, String fullFileName) throws Exception {
		
		sysTime = DateUtils.getSystemTime();
		sysTimeMs = new SimpleDateFormat("SSS").format(new GregorianCalendar().getTime());
		
		List<String> sqlList = new ArrayList<String>();
		// 0:新卡, 1:續卡, 2:換卡, 要做卡片狀態(TB_CARD)的更新
		String status = tempMap.get("STATUS").toString();
		
		if (!"3".equals(status)){
			sqlList.addAll(this.makeTbCardSQL(conn, fullFileName));
			
			//key ==> 專案代號 + 卡片狀態
			String key = tempMap.get("SALE_CODE").toString() + status;
			if(null == statusCntList.get(key)){
				statusCntList.put(key, 1);
			}
			else{
				statusCntList.put(key, statusCntList.get(key)+1);
			}
		}
		// 3:個資更新, 只處理TB_CUST
		sqlList.addAll(this.makeTbCustSQL(conn,fullFileName));
		return sqlList;
	}

	/**
	 * make a SQL Syntax List
	 * 
	 * @param conn
	 * @return List of SQL Syntax
	 * @throws Exception
	 */
	private List<String> makeTbCardSQL(Connection conn,  String fullFileName) throws Exception {
		
		List<String> sqlList = new ArrayList<String>();
		
		TbCardInfo info = new TbCardInfo();
		TbCardMgr mgr = new TbCardMgr(conn);
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		String where = "CARD_NO = " + StringUtil.toSqlValueWithSQuote(tempMap.get("CARD_NO"));
		mgr.queryMultiple(where, result);
		
		if (result.size() > 0) {
			
			info = result.get(0);
			String expiryDate = this.formatExpiryDate(tempMap.get("EXPIRY_DATE"));
			
			info.setPrimaryCard(tempMap.get("STATUS"));
			//20161013 TB_CARD.ISSUE_DATE時應填入欄位21(MBR_REG_DATE)
			//info.setIssueDate(dataBean.getIssueDate());
			//20170316 TB_CARD.ISSUE_DATE改回SYSDATE
			info.setIssueDate(sysDate);
			info.setCustId(tempMap.get("CUST_ID"));
			if ("1".equals(expiryDateFlag)) {
				info.setExpiryDate(expiryDate);
			}
			info.setCreditExpiryDate(expiryDate);
			info.setHgCardNo(tempMap.get("HG_CARD_NO"));
			info.setSaleCode(tempMap.get("SALE_CODE"));
			
			StringBuffer cardSql = new StringBuffer();
			cardSql.append(" UPDATE TB_CARD SET ");
			cardSql.append(" PRIMARY_CARD = ").append(StringUtil.toSqlValueWithSQuote(tempMap.get("STATUS"))).append(",");
			cardSql.append(" ISSUE_DATE = ").append(StringUtil.toSqlValueWithSQuote(sysDate)).append(",");
			cardSql.append(" MBR_REG_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate)).append(",");
			cardSql.append(" CUST_ID = ").append(StringUtil.toSqlValueWithSQuote(tempMap.get("CUST_ID"))).append(",");
			cardSql.append(" SALE_CODE = ").append(StringUtil.toSqlValueWithSQuote(tempMap.get("SALE_CODE"))).append(",");

			if ("1".equals(expiryDateFlag)) {
				cardSql.append(" EXPIRY_DATE = ").append(expiryDate).append(",");
			}
			
			cardSql.append(" HG_CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(tempMap.get("HG_CARD_NO"))).append(",");
			cardSql.append(" CREDIT_EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(expiryDate)).append(",");
			cardSql.append(" UPT_USERID = ").append(StringUtil.toSqlValueWithSQuote("BATCH")).append(",");
			cardSql.append(" UPT_DATE = ").append(StringUtil.toSqlValueWithSQuote(sysDate)).append(",");
			cardSql.append(" UPT_TIME = ").append(StringUtil.toSqlValueWithSQuote(sysTime)).append(",");
			cardSql.append(" APRV_USERID = ").append(StringUtil.toSqlValueWithSQuote("BATCH")).append(",");
			cardSql.append(" APRV_DATE = ").append(StringUtil.toSqlValueWithSQuote(sysDate)).append(",");
			cardSql.append(" APRV_TIME = ").append(StringUtil.toSqlValueWithSQuote(sysTime));
			
			cardSql.append(" WHERE CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(tempMap.get("CARD_NO")));
			
			sqlList.add(cardSql.toString());
			sqlList.add(this.makeTbCardUptInsertSQL(info));
			//20161219 insert TbHgCardMap
			if (!StringUtil.isEmpty(tempMap.get("HG_CARD_NO"))){
				sqlList.add(this.makeTbHgCardMapInsertSQL(info, fullFileName));
			}
			
		}
		return sqlList;
	}

	/**
	 * make a table insert SQL Syntax
	 * 
	 * @param info
	 *            TbCardInfo
	 * @return SQL Syntax
	 * @throws Exception
	 */
	private String makeTbCardUptInsertSQL(TbCardInfo info) throws Exception {
		TbCardUptInfo upt = new TbCardUptInfo();
		upt.setRegionId(info.getRegionId());
		upt.setMemId(info.getMemId());
		upt.setCustId(info.getCustId());
		upt.setCardProduct(info.getCardProduct());
		upt.setCardNo(info.getCardNo());
		upt.setExpiryDate(info.getExpiryDate());
		upt.setPrimaryCard(info.getPrimaryCard());
		upt.setStatus(info.getStatus());
		upt.setKeyVersion(info.getKeyVersion());
		upt.setBalTransferFlag(info.getBalTransferFlag());
		upt.setPersoBatchNo(info.getPersoBatchNo());
		upt.setPreloadAmt(info.getPreloadAmt());
		upt.setCardFee(info.getCardFee());
		upt.setPreviousStatus(info.getPreviousStatus());
		upt.setLastYearCnsAmt(info.getLastYearCnsAmt());
		upt.setTotalReloadAmt(info.getTotalReloadAmt());
		upt.setTotalUseAmt(info.getTotalUseAmt());
		upt.setCardTypeId(info.getCardTypeId());
		upt.setCardCatId(info.getCardCatId());
		upt.setTestFlag(info.getTestFlag());
		upt.setHgCardNo(info.getHgCardNo());
		upt.setCreditExpiryDate(info.getCreditExpiryDate());
		upt.setSaleCode(info.getSaleCode());
		upt.setIssueDate(info.getIssueDate());
		upt.setIsSyncHg(info.getIsSyncHg());
		upt.setMbrRegDate(batchDate);
		upt.setUptStatus("2");
		upt.setAprvStatus("1");
		upt.setUptUserid("BATCH");
		upt.setUptDate(sysDate);
		upt.setUptTime(sysTime+sysTimeMs);
		upt.setAprvUserid("BATCH");
		upt.setAprvDate(sysDate);
		upt.setAprvTime(sysTime);
		return upt.toInsertSQL();
	}

	/**
	 * make a table insert SQL Syntax
	 * 
	 * @param info
	 *            TbCardInfo
	 * @return SQL Syntax
	 * @throws Exception
	 */
	private String makeTbHgCardMapInsertSQL(TbCardInfo info, String fullFileName) throws Exception {
		TbHgCardMapInfo mapInfo = new TbHgCardMapInfo();
		mapInfo.setCardNo(info.getCardNo());
		mapInfo.setBarcode1(info.getHgCardNo());
		mapInfo.setImpFileName(fullFileName);
		mapInfo.setImpFileDate(batchDate);
		mapInfo.setStatus("2");

		return mapInfo.toInsertSQL();
	}

	/**
	 * make a SQL Syntax List
	 * 
	 * @param conn
	 * @return List of SQL Syntax
	 * @throws Exception
	 */
	private List<String> makeTbCustSQL(Connection conn,String fullFileName) throws Exception {
		
		List<String> sqlList = new ArrayList<String>();
		Vector<TbCustInfo> result = new Vector<TbCustInfo>();
		
		TbCustInfo info = new TbCustInfo();
		info.setCustId(tempMap.get("CUST_ID"));
		
		int cnt = new TbCustMgr(conn).queryMultiple(info, result);
		if (cnt > 0 & result.size() > 0) {
			info = result.get(0);
			info = this.makeTbCustInfo(info,fullFileName);
			sqlList.add(info.toUpdateSQL());
		} else {
			info = this.makeTbCustInfo(info,fullFileName);
			info.setRegionId("TWN");
			sqlList.add(info.toInsertSQL());
		}
		sqlList.add(this.makeTbCustUptInsertSQL(info));
		return sqlList;
	}

	/**
	 * make a TbCustInfo
	 * 
	 * @param info
	 *            TbCustInfo
	 * @return TbCustInfo
	 */
	private TbCustInfo makeTbCustInfo(TbCustInfo info,String fullFileName) {
		
		info.setCustId(tempMap.get("CUST_ID"));
		info.setCountryCode(tempMap.get("COUNTRY_CODE"));						// 國籍
		info.setIndustry(tempMap.get("INDUSTRY"));								// 職業與行業別
		info.setLocName(tempMap.get("LOC_NAME"));								// 持卡人中文姓名
		info.setEngName(tempMap.get("ENG_NAME"));								// 持卡人英文姓名
		info.setPersonId(tempMap.get("PERSON_ID"));								// 身份證明文件號碼
		info.setPersonType(tempMap.get("PERSON_TYPE"));							// 身分證明文件種類
		info.setGender(tempMap.get("GENDER"));									// 性別
		info.setBirthday(tempMap.get("BIRTHDAY"));								// 出生日期
		info.setCity(tempMap.get("CITY"));										// 通訊縣市別
		info.setZipCode(this.formatZipCode(tempMap.get("ZIP_CODE")));			// 通訊郵遞區號
		info.setAddress(tempMap.get("ADDRESS"));								// 通訊地址
		info.setPermanentCity(tempMap.get("PERMANENT_CITY"));					// 戶籍縣市別
		info.setPermanentZipCode(
				this.formatZipCode(tempMap.get("PERMANENT_ZIP_CODE")));			// 戶籍郵遞區號
		info.setPermanentAddress(tempMap.get("PERMANENT_ADDRESS"));				// 戶籍地址
		info.setTelHome(tempMap.get("TEL_HOME"));								// 市內電話
		info.setMobile(tempMap.get("MOBILE"));									// 行動電話
		info.setEmail(tempMap.get("EMAIL"));									// 電子信箱
		info.setDmFlag(tempMap.get("DM_FLAG"));									// 行銷使用註記
		info.setLegalAgentName(tempMap.get("LEGAL_AGENT_NAME"));				// 法定代理人中文姓名
		info.setLegalAgentEngName(tempMap.get("LEGAL_AGENT_ENG_NAME"));			// 法定代理人英文姓名
		info.setLegalAgentPid(tempMap.get("LEGAL_AGENT_PID"));					// 法定代理人身份證明文件號碼
		info.setLegalAgentPidType(tempMap.get("LEGAL_AGENT_PID_TYPE"));			// 法定代理人身分證明文件種類
		info.setLegalAgentCountryCode(tempMap.get("LEGAL_AGENT_COUNTRY_CODE"));	// 法定代理人國籍
		info.setLegalAgentBirthday(tempMap.get("LEGAL_AGENT_BIRTHDAY"));		// 法定代理人生日
		info.setLegalAgentGender(tempMap.get("LEGAL_AGENT_GENDER"));			// 法定代理人性別
		info.setLegalAgentMobile(tempMap.get("LEGAL_AGENT_MOBILE"));			// 法定代理人行動電話
		info.setLegalAgentPhone(tempMap.get("LEGAL_AGENT_PHONE"));				// 法定代理人電話
		info.setVipFlag(tempMap.get("VIP_FLAG"));								// VIP 註記
		info.setMerchId(tempMap.get("MERCH_ID"));								// 特店代號
		info.setMarriage(tempMap.get("MARRIAGE"));								// 婚姻狀態
		info.setMbrRegDate(batchDate);
		info.setVerifyDate(sysDate);	//verifydate 
		info.setImpFileName(fullFileName);
		info.setSyncFlag("0");      
		info.setUptUserid("BATCH"); 
		info.setUptDate(sysDate);   
		info.setUptTime(sysTime);   
		info.setAprvUserid("BATCH");
		info.setAprvDate(sysDate);  
		info.setAprvTime(sysTime);  
		
		if ("1".equals(isCustEncript)) {
			try {
				info.setLocName(BatchUtils.encript(info.getLocName()));
				info.setEngName(BatchUtils.encript(info.getEngName()));
				info.setPersonId(BatchUtils.encript(info.getPersonId()));
				info.setGender(BatchUtils.encript(info.getGender()));
				info.setBirthday(BatchUtils.encript(info.getBirthday()));
				info.setTelHome(BatchUtils.encript(info.getTelHome()));
				info.setMobile(BatchUtils.encript(info.getMobile()));
				info.setEmail(BatchUtils.encript(info.getEmail()));
				info.setZipCode(BatchUtils.encript(info.getZipCode()));
				info.setAddress(BatchUtils.encript(info.getAddress()));
				info.setPermanentAddress(BatchUtils.encript(info.getPermanentAddress()));
				info.setLegalAgentName(BatchUtils.encript(info.getLegalAgentName()));
				info.setLegalAgentEngName(BatchUtils.encript(info.getLegalAgentEngName()));
				info.setLegalAgentPid(BatchUtils.encript(info.getLegalAgentPid()));
				info.setLegalAgentMobile(BatchUtils.encript(info.getLegalAgentMobile()));
				info.setLegalAgentPhone(BatchUtils.encript(info.getLegalAgentPhone()));
			} catch (Exception ignore) {
				log.warn("encript fail : " + ignore.getMessage());
			}
		}
		return info;
	}

	/**
	 * make a table insert SQL Syntax
	 * 
	 * @param info
	 *            TbCustInfo
	 * @returnSQL Syntax
	 */
	private String makeTbCustUptInsertSQL(TbCustInfo info) {
		
		TbCustUptInfo upt = new TbCustUptInfo();
		upt.setRegionId(info.getRegionId());
		upt.setCustId(info.getCustId());
		upt.setPersonType(info.getPersonType());
		upt.setPersonId(info.getPersonId());
		upt.setLocName(info.getLocName());
		upt.setEngName(info.getEngName());
		upt.setCustLevel(info.getCustLevel());
		upt.setGender(info.getGender());
		upt.setBirthday(info.getBirthday());
		upt.setMarriage(info.getMarriage());
		upt.setContact(info.getContact());
		upt.setTelHome(info.getTelHome());
		upt.setTelOffice(info.getTelOffice());
		upt.setMobile(info.getMobile());
		upt.setZipCode(info.getZipCode());
		upt.setAddress(info.getAddress());
		upt.setEmail(info.getEmail());
		upt.setEducation(info.getEducation());
		upt.setIndustry(info.getIndustry());
		upt.setIndustry(info.getIndustry());
		upt.setAnnualIncome(info.getAnnualIncome());
		upt.setUd1(info.getUd1());
		upt.setUd2(info.getUd2());
		upt.setUd3(info.getUd3());
		upt.setUd4(info.getUd4());
		upt.setUd5(info.getUd5());
		upt.setBillCutDay(info.getBillCutDay());
		upt.setUptUserid(info.getUptUserid());
		upt.setUptDate(info.getUptDate());
		//因吃檔可能發生PK衝突，修改為sysTime+sysTimeMs
		upt.setUptTime(info.getUptTime()+sysTimeMs);
		upt.setAprvUserid(info.getAprvUserid());
		upt.setAprvDate(info.getAprvDate());
		upt.setAprvTime(info.getAprvTime());
		upt.setNewBillCutDay(info.getNewBillCutDay());
		upt.setBillValidDate(info.getBillValidDate());
		upt.setMemDay(info.getMemDay());
		upt.setMemo(info.getMemo());
		upt.setCity(info.getCity());
		upt.setLegalAgentName(info.getLegalAgentName());
		upt.setLegalAgentPid(info.getLegalAgentPid());
		upt.setLegalAgentMobile(info.getLegalAgentMobile());
		upt.setLegalAgentPhone(info.getLegalAgentPhone());
		upt.setVipFlag(info.getVipFlag());
		upt.setDmFlag(info.getDmFlag());
		upt.setMbrRegDate(info.getMbrRegDate());
		upt.setMerchId(info.getMerchId());
		upt.setRegRecycleStatus(info.getRegRecycleStatus());
		upt.setRegRecycleDate(info.getRegRecycleDate());
		upt.setSaleCode(info.getSaleCode());
		upt.setSaNo(info.getSaNo());
		upt.setHgAuth(info.getHgAuth());
		upt.setSyncFlag(info.getSyncFlag());
		upt.setCountryCode(info.getCountryCode());
		upt.setPermanentCity(info.getPermanentCity());
		upt.setPermanentZipCode(info.getPermanentZipCode());
		upt.setPermanentAddress(info.getPermanentAddress());
		upt.setLegalAgentPidType(info.getLegalAgentPidType());
		upt.setLegalAgentCountryCode(info.getLegalAgentCountryCode());
		upt.setLegalAgentGender(info.getLegalAgentGender());
		upt.setLegalAgentBirthday(info.getLegalAgentBirthday());
		upt.setLegalAgentEngName(info.getLegalAgentEngName());
		upt.setVerifyDate(sysDate);											//verifydate 
		upt.setImpFileName(info.getImpFileName());
		upt.setUptStatus("2");
		upt.setAprvStatus("1");
		upt.setModifyDesc(formatModifyDesc(tempMap.get("STATUS")));
		upt.setModifySource("BATCH");
		upt.setAprvUserName("BATCH");

		return upt.toInsertSQL();
	}

	/**
	 * Retrieves the value from TB_SYS_CONFIG
	 * 
	 * @param conn
	 * @param parm
	 * @return TB_SYS_CONFIG.VALUE
	 * @throws Exception
	 */
	private String getSysConfigValue(Connection conn, String parm)
			throws Exception {
		Vector<TbSysConfigInfo> result = new Vector<TbSysConfigInfo>();
		TbSysConfigInfo info = new TbSysConfigInfo();
		info.setParm(parm);
		int cnt = new TbSysConfigMgr(conn).queryMultiple(info, result);
		if (cnt > 0 && result.size() > 0)
			return result.get(0).getValue();
		return null;
	}

	/**
	 * Retrieves the maximum number of rows
	 * 
	 * @param conn
	 * @param cardNo
	 * @return Total record
	 * @throws Exception
	 */
	private Integer getTbCardCnt(Connection conn, String cardNo)
			throws Exception {
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		TbCardInfo info = new TbCardInfo();
		info.setCardNo(cardNo);
		return new TbCardMgr(conn).queryMultiple(info, result);
	}
	
	/**
	 * Retrieves the value from TB_CUST or create a new customer id
	 * 
	 * @param conn
	 * @param personId
	 * @return custId
	 * @throws Exception
	 */
	private String getCustId(Connection conn, String personId) throws Exception {
		Vector<TbCustInfo> result = new Vector<TbCustInfo>();
		TbCustMgr mgr = new TbCustMgr(conn);
		TbCustInfo info = new TbCustInfo();
		if ("1".equals(isCustEncript))
			info.setPersonId(BatchUtils.encript(personId));
		else
			info.setPersonId(personId);
		int cnt = mgr.queryMultiple(info, result);
		if (cnt > 0 && result.size() > 0)
			return result.get(0).getCustId();
		else
			return this.createCustId(conn);
	}

	/**
	 * create a new customer id
	 * 
	 * @param conn
	 * @return I + YYY(3碼) + DDD(3碼) + SEQ_NO(6碼)
	 * @throws Exception
	 */
	private String createCustId(Connection conn) throws Exception {
		String result = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new SimpleDateFormat("yyyyMMdd").parse(sysDate));
		String year = String.valueOf(cal.get(Calendar.YEAR)).substring(1, 4);
		String dayOfYear = String.valueOf(cal.get(Calendar.DAY_OF_YEAR));
		String seqno = "";
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select to_char(SEQ_CUST_ID.nextval,'000000') from DUAL ";
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				seqno = rs.getString(1).trim();
				break;
			}
		} catch (SQLException ignore) {
			log.warn("SQLException:" + ignore.getMessage(), ignore);
			seqno = "";
		} finally {
			ReleaseResource.releaseDB(null, stmt, rs);
		}
		result = "I" + StringUtils.leftPad(year, 3, "0")
				+ StringUtils.leftPad(dayOfYear, 3, "0")
				+ StringUtils.leftPad(seqno, 6, "0");
		return result;
	}

	/**
	 * Returns a new string that is a string of given value
	 * 
	 * @param value
	 * @return the specified string
	 */
	private String formatModifyDesc(String value) {
		if (value == null)
			return "";
		if ("0".equals(value))
			return "新卡";
		if ("1".equals(value))
			return "續卡";
		if ("2".equals(value))
			return "換卡";
		if ("3".equals(value))
			return "個資更新";
		return value;
	}

	/**
	 * Returns a new string that is a substring of given value
	 * 
	 * @param value
	 *            zip code
	 * @return the specified substring
	 * @throws Exception
	 */
	private String formatZipCode(String value) {
		if (value.length() < 5)
			return value;
		return value.substring(2, 5);
	}

	/**
	 * Format to AD date
	 * 
	 * @param date
	 *            (yyMM)
	 * @return date(yyyyMMdd)
	 * @throws Exception 
	 */
	private String formatExpiryDate(String date) {
		if (date.length() != 4)
			return null;
		
		String yearMonth = "20" + date;
		String lastDay = StringUtils.leftPad(String.valueOf(DateUtil.getLastDayOfMonth(yearMonth+"01")), 2, '0');
		return yearMonth + lastDay;
	}

	public Map<String, String> getTempMap() {
		return tempMap;
	}

	public void setTempMap(Map<String, String> tempMap) {
		this.tempMap = tempMap;
	}

	public String getBankCheckHgFlag() {
		return bankCheckHgFlag;
	}

	public void setBankCheckHgFlag(String bankCheckHgFlag) {
		this.bankCheckHgFlag = bankCheckHgFlag;
	}
}
