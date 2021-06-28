package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCDRQ;

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
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;

public class CdrqData {
	
	private static Logger log = Logger.getLogger(CdrqData.class);
	
	private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    
    private Integer cardCnt;
    
    
    private CdrqDataBean dataBean = new CdrqDataBean();
    
    
    public CdrqData() {}
    
    
    public Integer getCardCnt() {
		return cardCnt;
	}

	public CdrqDataBean getDataBean() {
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
		this.cardCnt = this.getTbCardCnt(conn, map.get("CARD_NO").toString(), map.get("EXPIRY_DATE").toString());
	}
	
	/**
	 * Conversion data to JavaBean
	 * @param map Import data
	 * @throws Exception
	 */
	public void convertBeanFromMap(Map<String, Object> map) throws Exception
	{
		dataBean.setReqType(map.get("REQ_TYPE").toString());
		dataBean.setCardNo(map.get("CARD_NO").toString());
		dataBean.setExpiryDate(map.get("EXPIRY_DATE").toString());
		dataBean.setFullFileName(map.get("FULL_FILE_NAME").toString());
	}
    
	/**
	 * make a SQL Syntax List
	 * @param conn
	 * @return List of SQL Syntax
	 * @throws Exception
	 */
	public List<String> makeSqlList(Connection conn, String rCode) throws Exception 
	{
		List<String> sqls = new ArrayList<String>();
		sqls.add(makeTbMbCardBalReqInsertSQL(rCode));
		return sqls;
	}
	
	/**
	 * make a table insert SQL Syntax
	 * @return SQL Syntax
	 * @throws Exception
	 */
	private String makeTbMbCardBalReqInsertSQL(String rCode) throws Exception 
	{
		return "INSERT INTO TB_MB_CARD_BAL_REQ " +
				"(CARD_NO, EXPIRY_DATE, REQ_TYPE, " +
				"IMP_DATE, IMP_TIME, IMP_FILE_NAME, " +
				"EXP_DATE, EXP_TIME, EXP_FILE_NAME, RCODE) " +
				"VALUES " +
				"('" + dataBean.getCardNo() + "', " +
				"'" + this.formatExpiryDate(dataBean.getExpiryDate()) + "', " +
				"'" + dataBean.getReqType() + "', " +
				"'" + sysDate + "', " +
				"'" + sysTime + "', " +
				"'" + dataBean.getFullFileName() + "', " +
				"'00000000', " +
				"'000000', " + 
				"'', " + 
				"'" + rCode + "')";
	}
	
	/**
	 * Retrieves the maximum number of rows
	 * @param conn
	 * @param cardNo
	 * @param expiryDate
	 * @return Total record
	 * @throws Exception
	 */
	private Integer getTbCardCnt(Connection conn, String cardNo, String expiryDate) throws Exception 
	{
		
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		TbCardInfo info = new TbCardInfo();
		info.setCardNo(cardNo);
		info.setExpiryDate(this.formatExpiryDate(expiryDate));
		
		log.debug("CardNo: " + info.getCardNo() + "ExpiryDate_4: " + expiryDate + " ExpiryDate_6: " + info.getExpiryDate());
		
		return new TbCardMgr(conn).queryMultiple(info, result);
	}
	
	/**
	 * Format to AD date
	 * @param type 
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
		day = Calendar.DAY_OF_MONTH;
		if (type == 1) day = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
		if (type == 0) day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		return String.valueOf(year) + mm + StringUtils.leftPad(String.valueOf(day), 2, '0');*/
	}
}
