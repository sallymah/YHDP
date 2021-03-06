package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBKCD;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.info.TbBlacklistSettingInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardUptInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBlackList.BlackListChecker;
import tw.com.hyweb.util.string.StringUtil;

public class BkcdData {
	
	private static Logger log = Logger.getLogger(BkcdData.class);
	
	private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
	
	private Integer cardCnt;
	
	private BkcdDataBean dataBean = new BkcdDataBean();
	
	
	public BkcdData() {}


	public Integer getCardCnt() {
		return cardCnt;
	}

	public BkcdDataBean getDataBean() {
		return dataBean;
	}
	
	/**
	 * Configuration parameters
	 * @param conn
	 * @param map
	 * @throws Exception
	 */
	public void setData(Connection conn, Map<String, Object> map) throws Exception 
	{
		this.cardCnt = this.getTbCardCnt(conn, map);
	}
	
	/**
	 * Conversion data to JavaBean
	 * @param map Import data
	 * @throws Exception
	 */
	public void convertBeanFromMap(Map<String, Object> map) throws Exception
	{
		dataBean.setStatus(map.get("STATUS").toString());
		dataBean.setCardNo(map.get("CARD_NO").toString());
		dataBean.setExpiryDate(map.get("EXPIRY_DATE").toString());
		dataBean.setFreezeDate(map.get("FREEZE_DATE").toString());
		dataBean.setBankId(map.get("BANK_ID").toString());
		dataBean.setrCode(map.get("R_CODE").toString());
	}
	
	/**
	 * make a SQL Syntax List
	 * @param conn
	 * @return List of SQL Syntax
	 * @throws Exception
	 */
	public List<String> makeSqlList(Connection conn) throws Exception 
	{
		List<String> sqlList = new ArrayList<String>();
		sqlList.addAll(makeTbCardSQL(conn));
		return sqlList;
	}
	
	/**
	 * make a SQL Syntax List
	 * @param conn
	 * @return List of SQL Syntax
	 * @throws Exception
	 */
	private List<String> makeTbCardSQL(Connection conn) throws Exception 
	{
		List<String> sqlList = new ArrayList<String>();
		TbCardInfo info = new TbCardInfo();
		if (!"2".equals(dataBean.getStatus())) 
			info.setExpiryDate(this.formatExpiryDate(dataBean.getExpiryDate()));
		info.setCardNo(dataBean.getCardNo());
		info = makeTbCardInfo(conn, info);
		if ("2".equals(dataBean.getStatus())){
			sqlList.add(
					"UPDATE TB_CARD SET " + 
					"EXPIRY_DATE = " + StringUtil.toSqlValueWithSQuote(this.formatExpiryDate(dataBean.getExpiryDate())) + " " +
					"WHERE CARD_NO = " + StringUtil.toSqlValueWithSQuote(dataBean.getCardNo()) + " ");
		}
		else{
			sqlList.add(info.toUpdateSQL());
			if("1".equals(dataBean.getStatus())){
			sqlList.add(makeBlackListInsertSQL(info));
			}
		}
		sqlList.add(this.makeTbCardUptInsertSQL(info));
		return sqlList;
	}
	
	/**
	 * make a TbCardInfo
	 * @param conn
	 * @param info TbCardInfo
	 * @return TbCardInfo
	 * @throws Exception
	 */
	private TbCardInfo makeTbCardInfo(Connection conn, TbCardInfo info) throws Exception 
	{
		TbCardMgr mgr = new TbCardMgr(conn);
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		int cnt = mgr.queryMultiple(info, result);
		if (cnt > 0 && result.size() > 0) {
			info = result.get(0);
			// ??????????????????
			if ("0".equals(dataBean.getStatus())) {
				info.setAutoReloadFlag("N");
			}
			// ??????
			else if("1".equals(dataBean.getStatus())) {
				info.setStatus("9");
				info.setLifeCycle("9");
				info.setStatusUpdateDate(dataBean.getFreezeDate());
				info.setRegDate(dataBean.getFreezeDate());
			}
			// ??????????????????
			else if ("2".equals(dataBean.getStatus())) {
				info.setExpiryDate(this.formatExpiryDate(dataBean.getExpiryDate()));
			}
			info.setUptUserid("BATCH");
			info.setUptDate(sysDate);
			info.setUptTime(sysTime);
			info.setAprvUserid("BATCH");
			info.setAprvDate(sysDate);
			info.setAprvTime(sysTime);
		}
		return info;
	}
	
	/**
	 * make a table insert SQL Syntax
	 * @param info TbCardInfo
	 * @return SQL Syntax
	 * @throws Exception
	 */
	private String makeTbCardUptInsertSQL(TbCardInfo info) throws Exception 
	{
		TbCardUptInfo upt = new TbCardUptInfo();
		upt.setRegionId(info.getRegionId());
		upt.setMemId(info.getMemId());
		upt.setCustId(info.getCustId());
		upt.setCardProduct(info.getCardProduct());
		upt.setCardNo(info.getCardNo());
		upt.setExpiryDate(info.getExpiryDate());
		upt.setAutoReloadFlag(info.getAutoReloadFlag());
		upt.setPrimaryCard(info.getPrimaryCard());
		upt.setStatus(info.getStatus());
		upt.setLifeCycle(info.getLifeCycle());
		upt.setStatusUpdateDate(info.getStatusUpdateDate());
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
		upt.setIsSyncHg(info.getIsSyncHg());
		upt.setIssueDate(sysDate);
		upt.setUptStatus("2");
		upt.setAprvStatus("1");
		upt.setUptUserid("BATCH");
		upt.setUptDate(sysDate);
		upt.setUptTime(sysTime);
		upt.setAprvUserid("BATCH");
		upt.setAprvDate(sysDate);
		upt.setAprvTime(sysTime);
		return upt.toInsertSQL();
	}
	
	private String makeBlackListInsertSQL(TbCardInfo info) throws Exception 
	{
		TbBlacklistSettingInfo blacList = new TbBlacklistSettingInfo();
		blacList.setCardNo(info.getCardNo());
		blacList.setExpiryDate(info.getExpiryDate());
		blacList.setStatus("1");
		blacList.setRegDate(sysDate);
		blacList.setRegTime(sysTime);
		blacList.setBlacklistCode("02");
		blacList.setRegUserid("BKCD");
		blacList.setUptUserid("BATCH");
		blacList.setUptDate(sysDate);
		blacList.setUptTime(sysTime);
		blacList.setAprvUserid("BATCH");
		blacList.setAprvDate(sysDate);
		blacList.setAprvTime(sysTime);
		return blacList.toInsertSQL();
	}
	
	/**
	 * Retrieves the maximum number of rows
	 * @param conn
	 * @param map
	 * @return Total record
	 * @throws Exception
	 */
	private Integer getTbCardCnt(Connection conn, Map<String, Object> map) throws Exception 
	{
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		TbCardInfo info = new TbCardInfo();
		info.setCardNo(map.get("CARD_NO").toString());
//		if (!"2".equals(map.get("STATUS").toString())) 
//			info.setExpiryDate(this.formatExpiryDate(map.get("EXPIRY_DATE").toString()));
		return new TbCardMgr(conn).queryMultiple(info, result);
	}
	
	/**
	 * Format to AD date
	 * @param date(yyMM)
	 * @return date(yyyyMMdd)
	 */
	private String formatExpiryDate(String date)
	{
		if (date.length() != 4)
			return null;
		
		String yearMonth = "20" + date;
		String lastDay = StringUtils.leftPad(String.valueOf(DateUtil.getLastDayOfMonth(yearMonth+"01")), 2, '0');
		return yearMonth + lastDay;
		
		/*if (date.length() != 4) return null;
		String yy = date.substring(0, 2);
		String mm = date.substring(2, 4);
		int year;
		int month;
		int day;
		try {
			year = Integer.valueOf(yy) + 2000;
			month = Integer.valueOf(mm);
		} catch (NumberFormatException e) {
			log.warn("year is '" + yy + "', month is '" + mm + "'");
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, (month - 1));
		day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		return String.valueOf(year) + mm + StringUtils.leftPad(String.valueOf(day), 2, '0');*/
	}
}
