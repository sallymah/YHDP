package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMPMerch;

import java.awt.image.BandCombineOp;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.viewer.LogTable;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbMemberGroupInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbMerchUptInfo;
import tw.com.hyweb.service.db.mgr.TbMemberGroupMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbMerchMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class MPMerchData {
	
	private static Logger log = Logger.getLogger(MPMerchData.class);
	
	private final String SEQ_MERCH_ID = "SEQ_MERCH_ID";
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    
    private String memId=null; //企業代號(inctl_info.mem_id)
//    private String merchId;
    private String ecaMerchId; //對應特店代碼
    private String headerRec = null;
	private String trailorRec = null;
    private int memCount;
    private int totRec;
    private ArrayList<String> merchList=new ArrayList<String>();
    
	private MPMerchBean bean = new MPMerchBean();
	
	
	public MPMerchData() {}
	
	
	public String getMemId() {
		return memId;
	}

	/*public void setMerchId(String merchId) {
		this.merchId = merchId;
	}
	
	public String getMerchId() {
		return merchId;
	}*/

	public void setMemId(String memId) {
		this.memId = memId;
	}
	
	public String getEcaMerchId() {
		return ecaMerchId;
	}

	public void setEcaMerchId(String ecaMerchId) {
		this.ecaMerchId = ecaMerchId;
	}

	public String getHeaderRec() {
		return headerRec;
	}

	public void setHeaderRec(String headerRec) {
		this.headerRec = headerRec;
	}

	public String getTrailorRec() {
		return trailorRec;
	}

	public void setTrailorRec(String trailorRec) {
		this.trailorRec = trailorRec;
	}

	public int getMemCount() {
		return memCount;
	}

	public void setMemCount(int memCount) {
		this.memCount = memCount;
	}

	public int getTotRec() {
		return totRec;
	}

	public void setTotRec(int totRec) {
		this.totRec = totRec;
	}

	public MPMerchBean getBean() {
		return bean;
	}
	
	/**
	 * make TB_MERCH update SQL Syntax
	 * @param memId TB_INCTL.MEM_ID
	 * @return SQL Syntax
	 * @throws Exception
	 */
	public List<String> updateMerch(String memId,Connection conn) throws Exception 
	{
		String sql=null;
		String MERCH_RULE_FLAG="0";
		StringBuffer sqlcmd=new StringBuffer();
		Statement st=conn.createStatement();
		sqlcmd.append("select MERCH_RULE_FLAG from tb_member where mem_id='"+ memId +"'");
		ResultSet rs=st.executeQuery(sqlcmd.toString());
		log.debug("sqlcmd :"+sqlcmd.toString());

		while(rs.next())
		{
			MERCH_RULE_FLAG=rs.getString(1);
		}
		log.debug("MERCH_RULE_FLAG :"+MERCH_RULE_FLAG);
	
		List<String> list = new ArrayList<String>();
		
		if(MERCH_RULE_FLAG.equals("1"))
		{
			sql = "update TB_MERCH " +
					"set UPT_DATE = " + StringUtil.toSqlValueWithSQuote("99991231") + ", status="+StringUtil.toSqlValueWithSQuote("0")
					+" where MEM_ID = "+ StringUtil.toSqlValueWithSQuote(memId);
			log.debug("update merch sql :"+sql.toString());
			list.add(sql);
		}
		else {
			log.debug("memId :"+memId+", MERCH_RULE_FLAG :"+MERCH_RULE_FLAG+" do not need update merch !!");
		}
		return list;
	}
	
	public void setData(Connection conn, Map<String, Object> map) throws Exception 
	{
		this.memId = map.get("BUS_MEM_ID").toString().trim();
//		this.merchId = map.get("BUS_MEM_ID").toString() + SequenceGenerator.getSequenceString(conn, SEQ_MERCH_ID, 7);
		this.ecaMerchId = this.formatEcaMerchId(conn, this.getMemId(), map.get("MERCH_ID").toString().trim());
		this.memCount = getMemInfo(conn , map.get("BUS_MEM_ID").toString().trim());
	}
	
	private int getMemInfo(Connection conn, String memId) throws SQLException
    {
		Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();
        TbMemberInfo info = new TbMemberInfo();
        info.setMemId(memId);
        return new TbMemberMgr(conn).queryMultiple(info, result);
    }
	
	/**
	 * Conversion data to JavaBean
	 * @param trailer Trailer Record
	 * @throws Exception
	 */
	public void converBeanFromTrailer(String trailer) throws Exception
	{
		if (trailer.length() > 9 && "T".equals(trailer.substring(0, 1)))
		{
			this.setTotRec(Integer.valueOf(trailer.substring(1, 9).trim()));
		}
	}
	
	/**
	 * Conversion data to JavaBean
	 * @param map Import file data
	 * @throws Exception
	 */
	public void convertBeanFromMap(Map<String, Object> map) throws Exception
	{
		String merchId = map.get("MERCH_ID").toString().trim();
		bean.setMerchId(merchId); //特店代號
		bean.setMerchLocName(map.get("MERCH_LOC_NAME").toString().trim());
		bean.setTel(map.get("TEL").toString().trim());
		bean.setFax(map.get("FAX").toString().trim());
		bean.setAddress(map.get("ADDRESS").toString().trim());
		bean.setZipCode(map.get("ZIP_CODE").toString().trim());
		bean.setStatus(map.get("STATUS").toString().trim());
		bean.setEffectiveDate(map.get("EFFECTIVE_DATE").toString().trim());
		bean.setTerminationDate(map.get("TERMINATION_DATE").toString().trim());
		bean.setEcaMerchId(this.getEcaMerchId());
		bean.setRealEcaMerchId(merchId);
		merchList.add(merchId);
//		log.info("merchList getsize :"+merchList.size());
//		log.info("merchList getvalue :"+merchId);

		memId=map.get("BUS_MEM_ID").toString();
	}
	
	/**
	 * make TB_MERCH SQL Syntax
	 * @param conn
	 * @return SQL Syntax
	 * @throws Exception
	 */
	public List<String> makeTbMerchSQL(Connection conn) throws Exception
	{
		
		List<String> sqls=new ArrayList<String>();
		
		TbMerchInfo info = new TbMerchInfo();
		TbMerchUptInfo upt = new TbMerchUptInfo();
		TbMerchMgr mgr = new TbMerchMgr(conn);
		Vector<TbMerchInfo> result = new Vector<TbMerchInfo>();
		String where = 
				"REAL_ECA_MERCH_ID = " + StringUtil.toSqlValueWithSQuote(bean.getMerchId()) + " " +
				"AND MEM_ID = " + StringUtil.toSqlValueWithSQuote(this.getMemId());
		int cnt = mgr.queryMultiple(where, result);
		//特店已存在 update
		if (cnt > 0 && result.size() > 0) {
			info = result.get(0);
			info = makeTbMerchInfo(info);
			sqls.add(info.toUpdateSQL());
			upt = makeTbMerchUptInfo1(info);
			sqls.add(upt.toInsertSQL());
		}
		//不存在insert的時候，才取滾號機取得MerchId
		else {
			info.setMemId(this.getMemId());
			String sql = "SELECT COUNT(1) FROM TB_MERCH WHERE MERCH_ID = ";
			String merchId = memId + SequenceGenerator.getSequenceString(conn, SEQ_MERCH_ID, 7);
			while (DbUtil.getInteger(sql + StringUtil.toSqlValueWithSQuote(merchId), conn) > 0) {
				merchId = memId + SequenceGenerator.getSequenceString(conn, SEQ_MERCH_ID, 7);
			}
			info.setMerchId(merchId);
			sqls.add(makeTbMerchInfo(info).toInsertSQL());	
			upt = makeTbMerchUptInfo2(info);
			sqls.add(upt.toInsertSQL());
		}		

		return sqls;
	}
	
	/**
	 * make TbMerchInfo
	 * @param info
	 * @return TbMerchInfo
	 */
	private TbMerchInfo makeTbMerchInfo(TbMerchInfo info)
	{
		info.setMerchLocName(bean.getMerchLocName());
		info.setMerchAbbrName(bean.getMerchLocName());
		info.setTel(bean.getTel());
		info.setFax(bean.getFax());
		info.setZipCode(bean.getZipCode());
		info.setAddress(bean.getAddress());
		log.info("file status: "+bean.getStatus());
//		info.setStatus(bean.getStatus());
		info.setStatus(convertDescFromStatus(bean.getStatus()));
		if ("".equals(bean.getEffectiveDate()) 
				|| "00000000".equals(bean.getEffectiveDate())) {
			info.setEffectiveDate(sysDate);
		} else if (!"".equals(bean.getEffectiveDate()) 
				|| bean.getEffectiveDate() != null) {
			info.setEffectiveDate(bean.getEffectiveDate());
		} else if ("".equals(info.getEffectiveDate()) 
				|| info.getEffectiveDate() == null) {
			info.setEffectiveDate(sysDate);
		} else {
			info.setEffectiveDate(bean.getEffectiveDate());
		}
		if (bean.getTerminationDate() == null 
				|| "".equals(bean.getTerminationDate())
				|| "00000000".equals(bean.getTerminationDate())) {
			info.setTerminationDate("99991231");
		} else {
			info.setTerminationDate(bean.getTerminationDate());
		}
		info.setEcaMerchId(bean.getEcaMerchId());
		info.setRealEcaMerchId(bean.getRealEcaMerchId());
		info.setUptUserid("BATCH");
		info.setUptDate(sysDate);
		info.setUptTime(sysTime);
		info.setAprvUserid("BATCH");
		info.setAprvDate(sysDate);
		info.setAprvTime(sysTime);
		return info;
	}
	
	/**
	 * make TbMerchUptInfo
	 * @param upt
	 * @return
	 */
	private TbMerchUptInfo makeTbMerchUptInfo1(TbMerchInfo info)
	{
		TbMerchUptInfo upt = new TbMerchUptInfo();
		upt.setMemId(this.getMemId());
		upt.setMerchId(info.getMerchId());
		upt.setBusIdNo(info.getBusIdNo());
		upt.setMerchLocName(bean.getMerchLocName());
		upt.setMerchEngName(info.getMerchEngName());
		upt.setMerchAbbrName(bean.getMerchLocName());
		upt.setCountryCode(info.getCountryCode());
		upt.setMccCode(info.getMccCode());
		upt.setContact(info.getContact());
		upt.setTel(bean.getTel());
		upt.setFax(bean.getFax());
		upt.setZipCode(bean.getZipCode());
		upt.setAddress(bean.getAddress());
		upt.setEmail(info.getEmail());
		log.info("upt status :"+bean.getStatus());
//		upt.setStatus(bean.getStatus());
		upt.setStatus(convertDescFromStatus(bean.getStatus()));
		if ("".equals(bean.getEffectiveDate()) 
						|| "00000000".equals(bean.getEffectiveDate())) {
			upt.setEffectiveDate(sysDate);
		} else if (!"".equals(bean.getEffectiveDate()) 
				|| bean.getEffectiveDate() != null) {
			upt.setEffectiveDate(bean.getEffectiveDate());
		} else if ("".equals(info.getEffectiveDate()) 
				|| info.getEffectiveDate() == null) {
			upt.setEffectiveDate(sysDate);
		} else {
			upt.setEffectiveDate(bean.getEffectiveDate());
		}
		if (bean.getTerminationDate() == null 
				|| "".equals(bean.getTerminationDate())
				|| "00000000".equals(bean.getTerminationDate())) {
			upt.setTerminationDate("99991231");
		} else {
			upt.setTerminationDate(bean.getTerminationDate());
		}
		upt.setUd1(info.getUd1());
		upt.setUd2(info.getUd2());
		upt.setUd3(info.getUd3());
		upt.setUd4(info.getUd4());
		upt.setUd5(info.getUd5());
		upt.setEcaMerchId(bean.getEcaMerchId());
		upt.setRealEcaMerchId(bean.getRealEcaMerchId());
		upt.setUptUserid("BATCH");
		upt.setUptDate(sysDate);
		upt.setUptTime(sysTime);
		upt.setAprvUserid("BATCH");
		upt.setAprvDate(sysDate);
		upt.setAprvTime(sysTime);
		upt.setAprvStatus("1");
		upt.setUptStatus("2");
		upt.setRldMaxAmt(info.getRldMaxAmt());
		upt.setRldChkFlag(info.getRldChkFlag());
		upt.setMacKey(info.getMacKey());
		upt.setMerchType(info.getMerchType());
		upt.setCity(info.getCity());
		upt.setSamLogonTime(info.getSamLogonTime());
		upt.setOfflineMaxCount(info.getOfflineMaxCount());
		upt.setOfflineMaxAmt(info.getOfflineMaxAmt());
		upt.setMerchSrc(info.getMerchSrc());
		upt.setMerchOwner(info.getMerchOwner());
		upt.setTransSysNo(info.getTransSysNo());
		upt.setLocId(info.getLocId());
		upt.setOfflineRldMaxAmt(info.getOfflineRldMaxAmt());
		upt.setMerchMode(info.getMerchMode());
		upt.setParGenDate(info.getParGenDate());
		upt.setParGenTime(info.getParGenTime());
		return upt;
	}
		
	private TbMerchUptInfo makeTbMerchUptInfo2(TbMerchInfo info)
	{
		TbMerchUptInfo upt = new TbMerchUptInfo();
		upt.setMemId(this.getMemId());
		upt.setMerchId(info.getMerchId());
		upt.setMerchLocName(bean.getMerchLocName());
		upt.setMerchAbbrName(bean.getMerchLocName());
		upt.setTel(bean.getTel());
		upt.setFax(bean.getFax());
		upt.setZipCode(bean.getZipCode());
		upt.setAddress(bean.getAddress());
		log.info("upt status :"+bean.getStatus());
//		upt.setStatus(bean.getStatus());
		upt.setStatus(convertDescFromStatus(bean.getStatus()));
		if ("".equals(bean.getEffectiveDate()) 
						|| "00000000".equals(bean.getEffectiveDate())) {
			upt.setEffectiveDate(sysDate);
		} else if (!"".equals(bean.getEffectiveDate()) 
				|| bean.getEffectiveDate() != null) {
			upt.setEffectiveDate(bean.getEffectiveDate());
		} else if ("".equals(info.getEffectiveDate()) 
				|| info.getEffectiveDate() == null) {
			upt.setEffectiveDate(sysDate);
		} else {
			upt.setEffectiveDate(bean.getEffectiveDate());
		}
		if (bean.getTerminationDate() == null 
				|| "".equals(bean.getTerminationDate())
				|| "00000000".equals(bean.getTerminationDate())) {
			upt.setTerminationDate("99991231");
		} else {
			upt.setTerminationDate(bean.getTerminationDate());
		}
		upt.setEcaMerchId(bean.getEcaMerchId());
		upt.setRealEcaMerchId(bean.getRealEcaMerchId());
		upt.setUptUserid("BATCH");
		upt.setUptDate(sysDate);
		upt.setUptTime(sysTime);
		upt.setAprvUserid("BATCH");
		upt.setAprvDate(sysDate);
		upt.setAprvTime(sysTime);
		upt.setAprvStatus("1");
		upt.setUptStatus("1");
		return upt;
	}	
	
	/**
	 * make TB_MERCH update SQL Syntax
	 * @param conn
	 * @return SQL Syntax
	 * @throws Exception
	 */
	public List<String> procVaildMerch(Connection conn) throws Exception
	{
		List<String> list = new ArrayList<String>();
		TbMerchInfo info = new TbMerchInfo();
		TbMerchUptInfo upt = new TbMerchUptInfo();
		TbMerchMgr mgr = new TbMerchMgr(conn);
		Vector<TbMerchInfo> result = new Vector<TbMerchInfo>();
		String where =  
				"MEM_ID = "+ StringUtil.toSqlValueWithSQuote(this.getMemId()) + " " + 
				"and UPT_DATE = " + StringUtil.toSqlValueWithSQuote("99991231");
		int cnt = mgr.queryMultiple(where, result);
		if (cnt > 0 && result.size() > 0) {
			for (int i = 0; i < cnt; i++) 
			{
				info = result.get(i);
				//String merchId = getMemId() + SequenceGenerator.getSequenceString(conn, SEQ_MERCH_ID, 7);
				String merchId = info.getMerchId();
				String sql = 
						"UPDATE TB_MERCH SET " +
						//"MERCH_ID = " + StringUtil.toSqlValueWithSQuote(merchId) + ", " + 
						"STATUS = " + StringUtil.toSqlValueWithSQuote("0") + ", " + 
						//"EFFECTIVE_DATE = " + StringUtil.toSqlValueWithSQuote(sysDate) + ", " + 
						"TERMINATION_DATE = " + StringUtil.toSqlValueWithSQuote(sysDate) + ", " + 
						"UPT_USERID = " + StringUtil.toSqlValueWithSQuote("BATCH") + ", " + 
						"UPT_DATE = " + StringUtil.toSqlValueWithSQuote(sysDate) + ", " + 
						"UPT_TIME = " + StringUtil.toSqlValueWithSQuote(sysTime) + ", " + 
						"APRV_USERID = " + StringUtil.toSqlValueWithSQuote("BATCH") + ", " + 
						"APRV_DATE = " + StringUtil.toSqlValueWithSQuote(sysDate) + ", " + 
						"APRV_TIME = " + StringUtil.toSqlValueWithSQuote(sysTime) + " " + 
						"WHERE ROWID = " + StringUtil.toSqlValueWithSQuote(info.getRowid());
				list.add(sql);
				
				upt.setMemId(this.getMemId());
				upt.setMerchId(merchId);
				upt.setMerchLocName(info.getMerchLocName());
				upt.setTel(info.getTel());
				upt.setFax(info.getFax());
				upt.setZipCode(info.getZipCode());
				upt.setAddress(info.getAddress());
				upt.setEcaMerchId(info.getEcaMerchId());
				upt.setRealEcaMerchId(info.getRealEcaMerchId());
				upt.setStatus("0");
				upt.setEffectiveDate(info.getEffectiveDate());
				upt.setTerminationDate(sysDate);
				upt.setUptUserid("BATCH");
				upt.setUptDate(sysDate);
				upt.setUptTime(sysTime);
				upt.setAprvUserid("BATCH");
				upt.setAprvDate(sysDate);
				upt.setAprvTime(sysTime);
				upt.setUptStatus("2");
				upt.setAprvStatus("1");
				list.add(upt.toInsertSQL());
			}
		}
		log.debug(list.toString());
		return list;
	}
	
	/**
	 * Returns a new string 
	 * @param conn
	 * @param memId
	 * @param merchId
	 * @return the specified substring
	 * @throws Exception
	 */
	private String formatEcaMerchId(Connection conn, String memId, String merchId) throws Exception 
	{
		if (merchId.length() > 8) {
			merchId = merchId.substring(2);
		}
		Map<String, String> temp = this.getMemValues(conn, memId);
		String memGroupId = temp.get("MEM_GROUP_ID").toString().trim();
		String memSimpleCode = temp.get("SIMPLE_CODE").toString().trim();
		String groupSimpleCode = this.getSimpleCode(conn, memGroupId);
		return "000" 
				+ StringUtils.leftPad(groupSimpleCode, 2, '0') 
				+ StringUtils.leftPad(memSimpleCode, 2, '0')
				+ StringUtils.leftPad(merchId, 8, '0');
	}
	
	/**
	 * Retrieves the value from TB_MEMBER
	 * @param conn
	 * @return TB_MEMBER.MEM_GROUP_ID, TB_MEMBER.SIMPLE_CODE
	 * @throws Exception
	 */
	private Map<String, String> getMemValues(Connection conn, String memId) throws Exception 
	{
		Map<String, String> temp = new HashMap<String, String>();
		Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();
    	TbMemberInfo info = new TbMemberInfo();
    	info.setMemId(memId);
    	int cnt = new TbMemberMgr(conn).queryMultiple(info, result);
    	if (cnt > 0 && result.size() > 0) {
    		temp.put("MEM_GROUP_ID", result.get(0).getMemGroupId());
    		temp.put("SIMPLE_CODE", result.get(0).getSimpleCode());
    	}
		return temp;
	}
	
	/**
	 * Retrieves the value from TB_MEMBER_GROUP
	 * @param conn
	 * @param memGroupId
	 * @return TB_MEMBER_GROUP.SIMPLE_CODE
	 * @throws Exception
	 */
	private String getSimpleCode(Connection conn, String memGroupId) throws Exception 
	{
		Vector<TbMemberGroupInfo> result = new Vector<TbMemberGroupInfo>();
		TbMemberGroupInfo info = new TbMemberGroupInfo();
    	info.setMemGroupId(memGroupId);
    	int cnt = new TbMemberGroupMgr(conn).queryMultiple(info, result);
    	if (cnt > 0 && result.size() > 0) 
    		return result.get(0).getSimpleCode();
    	return "";
	}
	
	/**
	 * Conversion status to the specified description.
	 * @param str STATUS
	 * @return 0 or 1
	 */
	private String convertDescFromStatus(String str) 
	{
		if (str == null || "".equals(str)) return "0";
		if ("F".equals(str)) return "0";
		if ("A".equals(str)) return "1";
		if ("0".equals(str)) return "1";
		if ("1".equals(str)) return "0";
		return str;
	}


	public List makeuptMerchStatus(Connection conn,String batchDate,String batchTime) throws SQLException {
		ArrayList<String> lists=new ArrayList<String>();
		StringBuffer uptSqlcmd=new StringBuffer();
		StringBuffer queryMerchSQL=new StringBuffer();
		ResultSet rsNoIn=null;
		String MERCH_RULE_FLAG="0";
		StringBuffer sqlcmd=new StringBuffer();
		Statement st=conn.createStatement();
		sqlcmd.append("select MERCH_RULE_FLAG from tb_member where mem_id='"+ memId +"'");
		ResultSet rs=st.executeQuery(sqlcmd.toString());
		log.debug("sqlcmd :"+sqlcmd.toString());
		conn.createStatement();
		while(rs.next())
		{
			MERCH_RULE_FLAG=rs.getString(1);
		}
		log.debug("MERCH_RULE_FLAG :"+MERCH_RULE_FLAG);
	
		
		if(MERCH_RULE_FLAG.equals("1"))
		{
			queryMerchSQL.append("select mem_id,merch_id,bus_id_no,merch_loc_name,merch_eng_name,merch_abbr_name," +
					"COUNTRY_CODE,MCC_CODE,CONTACT,TEL,FAX,ADDRESS,EMAIL,STATUS,EFFECTIVE_DATE,TERMINATION_DATE," +
					"UD1,UD2,UD3,UD4,UD5,RLD_MAX_AMT," +
					"RLD_CHK_FLAG,MAC_KEY,MERCH_TYPE,CITY,ZIP_CODE,SAM_LOGON_TIME,OFFLINE_MAX_COUNT,OFFLINE_MAX_AMT," +
					"MERCH_SRC,MERCH_OWNER,ECA_MERCH_ID,REAL_ECA_MERCH_ID,TRANS_SYS_NO,LOC_ID,OFFLINE_RLD_MAX_AMT," +
					"MERCH_MODE,PAR_GEN_DATE,PAR_GEN_TIME from tb_merch where mem_id='").append(memId).append("'");
			queryMerchSQL.append(" and REAL_ECA_MERCH_ID not in (");

			uptSqlcmd.append("update tb_merch set status='0',aprv_userid='BATCH',aprv_date='").append(batchDate).append("',");
			uptSqlcmd.append("aprv_time='").append(batchTime).append("',");
			uptSqlcmd.append("upt_userid='BATCH',upt_date='").append(batchDate).append("',");
			uptSqlcmd.append("upt_time='").append(batchTime).append("'").append(" where mem_id='").append(memId).append("'");
			uptSqlcmd.append(" and REAL_ECA_MERCH_ID not in (");
			for(int i=0; i<merchList.size(); i++)
			{
				queryMerchSQL.append("'").append(merchList.get(i)).append("'");
				uptSqlcmd.append("'").append(merchList.get(i)).append("'");
				if(i !=(merchList.size()-1))
				{
					uptSqlcmd.append(",");
					queryMerchSQL.append(",");

				}
				else {
					uptSqlcmd.append(")");
					queryMerchSQL.append(")");
				}
			}
			lists.add(uptSqlcmd.toString());
			
			log.debug("update merchSQL command : "+uptSqlcmd.toString());
			log.debug("queryMerchSQL : "+queryMerchSQL.toString());
			
			rsNoIn=st.executeQuery(queryMerchSQL.toString());
			TbMerchUptInfo merchUptInfo = null;
			while(rsNoIn.next())
			{
				merchUptInfo=new TbMerchUptInfo();
				merchUptInfo.setMemId(rsNoIn.getString(1));
				merchUptInfo.setMerchId(rsNoIn.getString(2));
				merchUptInfo.setBusIdNo(rsNoIn.getString(3));
				merchUptInfo.setMerchLocName(rsNoIn.getString(4));
				merchUptInfo.setMerchEngName(rsNoIn.getString(5));
				merchUptInfo.setMerchAbbrName(rsNoIn.getString(6));
				merchUptInfo.setCountryCode(rsNoIn.getString(7));
				merchUptInfo.setMccCode(rsNoIn.getString(8));
				merchUptInfo.setContact(rsNoIn.getString(9));
				merchUptInfo.setTel(rsNoIn.getString(10));
				merchUptInfo.setFax(rsNoIn.getString(11));
				merchUptInfo.setAddress(rsNoIn.getString(12));
				merchUptInfo.setEmail(rsNoIn.getString(13));
				merchUptInfo.setStatus("0");
				merchUptInfo.setEffectiveDate(rsNoIn.getString(15));
				merchUptInfo.setTerminationDate(rsNoIn.getString(16));
				merchUptInfo.setUd1(rsNoIn.getString(17));
				merchUptInfo.setUd2(rsNoIn.getString(18));
				merchUptInfo.setUd3(rsNoIn.getString(19));
				merchUptInfo.setUd4(rsNoIn.getString(20));
				merchUptInfo.setUd5(rsNoIn.getString(21));
				merchUptInfo.setUptUserid("BATCH");
				merchUptInfo.setUptDate(batchDate);
				merchUptInfo.setUptTime(batchTime);
				merchUptInfo.setAprvUserid("BATCH");
				merchUptInfo.setAprvDate(batchDate);
				merchUptInfo.setAprvTime(batchTime);
				merchUptInfo.setUptStatus("2");
				merchUptInfo.setAprvStatus("1");
				merchUptInfo.setRldMaxAmt(rsNoIn.getDouble(22));
				merchUptInfo.setRldChkFlag(rsNoIn.getString(23));
				merchUptInfo.setMacKey(rsNoIn.getString(24));
				merchUptInfo.setMerchType(rsNoIn.getString(25));
				merchUptInfo.setCity(rsNoIn.getString(26));
				merchUptInfo.setZipCode(rsNoIn.getString(27));
				merchUptInfo.setSamLogonTime(rsNoIn.getInt(28));
				merchUptInfo.setOfflineMaxCount(rsNoIn.getInt(29));
				merchUptInfo.setOfflineMaxAmt(rsNoIn.getDouble(30));
				merchUptInfo.setMerchSrc(rsNoIn.getString(31));
				merchUptInfo.setMerchOwner(rsNoIn.getString(32));
				merchUptInfo.setEcaMerchId(rsNoIn.getString(33));
				merchUptInfo.setRealEcaMerchId(rsNoIn.getString(34));
				merchUptInfo.setTransSysNo(rsNoIn.getString(35));
				merchUptInfo.setLocId(rsNoIn.getString(36));
				merchUptInfo.setOfflineRldMaxAmt(rsNoIn.getDouble(37));
				merchUptInfo.setMerchMode(rsNoIn.getString(38));
				merchUptInfo.setParGenDate(rsNoIn.getString(39));
				merchUptInfo.setParGenTime(rsNoIn.getString(40));
				lists.add(merchUptInfo.toInsertSQL());
				log.debug("merchUpt InsertSQL :"+merchUptInfo.toInsertSQL());
			}	
		}
		log.debug("lists size :"+lists.size());
		return lists;
	}
}
