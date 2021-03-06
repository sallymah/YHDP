package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpRBMEM;

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
import tw.com.hyweb.service.db.info.TbInctlInfo;
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
	private String sysTime;
	private String sysTimeMs;

	private BmemDataBean dataBean = new BmemDataBean();

	public BmemData() {
	}

	public BmemDataBean getDataBean() {
		return dataBean;
	}

	/**
	 * Conversion data to JavaBean
	 * 
	 * @param map
	 *            Import data
	 * @throws Exception
	 */
	public void convertBeanFromMap(Map<String, Object> map) throws Exception {
		dataBean.setCardNo(map.get("CARD_NO").toString());
		dataBean.setStatus(map.get("STATUS").toString());
	}

	/**
	 * make a SQL Syntax List
	 * 
	 * @param conn
	 * @return List of SQL Syntax
	 * @throws Exception
	 */
	public List<String> makeSqlList(Connection conn, TbInctlInfo inctlInfo) throws Exception {
		
		sysTime = DateUtils.getSystemTime();
		sysTimeMs = new SimpleDateFormat("SSS").format(new GregorianCalendar().getTime());
		
		List<String> sqlList = new ArrayList<String>();
		// 0:??????, 1:??????, 2:??????, ??????????????????(TB_CARD)?????????
		if (!"3".equals(dataBean.getStatus())){
			sqlList.addAll(this.makeTbCardSQL(conn, inctlInfo));
		}
		return sqlList;
	}

	/**
	 * make a SQL Syntax List
	 * 
	 * @param conn
	 * @return List of SQL Syntax
	 * @throws Exception
	 */
	private List<String> makeTbCardSQL(Connection conn,  TbInctlInfo inctlInfo) throws Exception {
		List<String> sqlList = new ArrayList<String>();
		TbCardInfo info = new TbCardInfo();
		TbCardMgr mgr = new TbCardMgr(conn);
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		String where = "CARD_NO = "
				+ StringUtil.toSqlValueWithSQuote(dataBean.getCardNo()) + " ";
		int cnt = mgr.queryMultiple(where, result);
		if (cnt > 0 && result.size() > 0) {
			info = result.get(0);
			info.setIssueDate(inctlInfo.getSysDate());
			info.setUptUserid("RBMEM");
			info.setUptDate(sysDate);
			info.setUptTime(sysTime);
			info.setAprvUserid("RBMEM");
			info.setAprvDate(sysDate);
			info.setAprvTime(sysTime);
			
			sqlList.add(info.toUpdateSQL());
			sqlList.add(this.makeTbCardUptInsertSQL(info));
		}
		else{
			log.warn("["+ dataBean.getCardNo() + "] is not exist.");
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
		upt.setMbrRegDate(info.getMbrRegDate());
		upt.setIsSyncHg(info.getIsSyncHg());
		upt.setUptStatus("2");
		upt.setAprvStatus("1");
		upt.setUptUserid("RBMEM");
		upt.setUptDate(sysDate);
		upt.setUptTime(sysTime+sysTimeMs);
		upt.setAprvUserid("RBMEM");
		upt.setAprvDate(sysDate);
		upt.setAprvTime(sysTime);
		return upt.toInsertSQL();
	}

}
