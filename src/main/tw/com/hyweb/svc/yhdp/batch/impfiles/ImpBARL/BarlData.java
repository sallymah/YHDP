package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBARL;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbRejectAuthListInfo;
import tw.com.hyweb.service.db.info.TbRejectAuthListUptInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbRejectAuthListMgr;
import tw.com.hyweb.util.string.StringUtil;


public class BarlData {
	
	private static Logger log = Logger.getLogger(BarlData.class);
	
	private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
	
    private Integer cardCnt;
    
	private BarlDataBean dataBean = new BarlDataBean();
	
	
	public BarlData() {}

	
	public Integer getCardCnt() {
		return cardCnt;
	}
	
	public BarlDataBean getDataBean() {
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
		this.cardCnt = this.getTbCardCnt(conn, map.get("CARD_NO").toString());
		dataBean.setExpiryDate(this.getExpiryDate(conn, map.get("CARD_NO").toString()));
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
		dataBean.setEndDate(map.get("END_DATE").toString());
		dataBean.setrCode(map.get("R_CODE").toString());
		dataBean.setFullFileName(map.get("FULL_FILE_NAME").toString());
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
		// 新增
		if ("A".equals(dataBean.getStatus())) 
			sqlList.add(makeTbRejectAuthListInsertSQL());
		// 刪除
		if ("D".equals(dataBean.getStatus())) 
		{
			Vector<TbRejectAuthListInfo> result = new Vector<TbRejectAuthListInfo>();
			TbRejectAuthListMgr mgr = new TbRejectAuthListMgr(conn);
			TbRejectAuthListInfo info = new TbRejectAuthListInfo();
			info.setCardNo(dataBean.getCardNo());
			info.setExpiryDate(dataBean.getExpiryDate());
			int cnt = mgr.queryMultiple(info, result);
			if (cnt > 0 && result.size() > 0)
			{
				sqlList.add(makeTbRejectAuthListDeleteSQL());
				sqlList.add(makeTbRejectAuthListUptInsertSQL(result.get(0)));
			}
		}
		return sqlList;
	}
	
	/**
	 * make a table insert SQL Syntax
	 * @return SQL Syntax
	 * @throws Exception
	 */
	private String makeTbRejectAuthListInsertSQL() throws Exception 
	{
		TbRejectAuthListInfo info = new TbRejectAuthListInfo();
		info.setCardNo(dataBean.getCardNo());
		info.setExpiryDate(dataBean.getExpiryDate());
		info.setRejectReason("57");
		info.setEndDate(dataBean.getEndDate());
		info.setImpFileDate(sysDate);
		info.setImpFileTime(sysTime);
		info.setImpFileName(dataBean.getFullFileName());
		info.setUptUserid("BATCH");
		info.setUptDate(sysDate);
		info.setUptTime(sysTime);
		info.setAprvUserid("BATCH");
		info.setAprvDate(sysDate);
		info.setAprvTime(sysTime);
		return info.toInsertSQL();
	}
	
	/**
	 * make a table delete SQL Syntax
	 * @return SQL Syntax
	 * @throws Exception
	 */
	private String makeTbRejectAuthListDeleteSQL() throws Exception 
	{
		return "Delete TB_REJECT_AUTH_LIST " +
				"where CARD_NO = " + StringUtil.toSqlValueWithSQuote(dataBean.getCardNo()) + " " + 
				"and EXPIRY_DATE = " + StringUtil.toSqlValueWithSQuote(dataBean.getExpiryDate()) + " ";
	}
	
	/**
	 * make a table insert SQL Syntax
	 * @param info TbRejectAuthListInfo
	 * @return SQL Syntax
	 * @throws Exception
	 */
	private String makeTbRejectAuthListUptInsertSQL(TbRejectAuthListInfo info) throws Exception 
	{
		TbRejectAuthListUptInfo upt = new TbRejectAuthListUptInfo();
		upt.setCardNo(info.getCardNo());
		upt.setExpiryDate(info.getExpiryDate());
		upt.setLmsInvoiceNo(info.getLmsInvoiceNo());
		upt.setRejectReason(info.getRejectReason());
		upt.setEndDate(info.getEndDate());
		upt.setImpFileName(info.getImpFileName());
		upt.setImpFileDate(info.getImpFileDate());
		upt.setImpFileTime(info.getImpFileTime());
		upt.setUptStatus("3"); //刪除
		upt.setAprvStatus("1"); //已放行
		upt.setUptUserid("BATCH");
		upt.setUptDate(sysDate);
		upt.setUptTime(sysTime);
		upt.setAprvUserid("BATCH");
		upt.setAprvDate(sysDate);
		upt.setAprvTime(sysTime);
		return upt.toInsertSQL();
	}
	
	/**
	 * Retrieves the maximum number of rows
	 * @param conn
	 * @param cardNo
	 * @return Total record
	 * @throws Exception
	 */
	private Integer getTbCardCnt(Connection conn, String cardNo) throws Exception 
	{
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		TbCardInfo info = new TbCardInfo();
		info.setCardNo(cardNo);
		return new TbCardMgr(conn).queryMultiple(info, result);
	}
	
	/**
	 * Retrieves the value from TB_CARD
	 * @param conn
	 * @param cardNo TB_CARD.CARD_NO
	 * @return TB_CARD.EXPIRY_DATE
	 * @throws Exception
	 */
	private String getExpiryDate(Connection conn, String cardNo) throws Exception 
	{
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		TbCardInfo info = new TbCardInfo();
		info.setCardNo(cardNo);
		int cnt = new TbCardMgr(conn).queryMultiple(info, result);
		if (cnt > 0 && result.size() > 0) 
			return result.get(0).getExpiryDate();
		return null;
	}
}
