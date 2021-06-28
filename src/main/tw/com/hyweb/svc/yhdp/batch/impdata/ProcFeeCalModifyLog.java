package tw.com.hyweb.svc.yhdp.batch.impdata;


import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.yhdp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbCustModifyLogInfo;
import tw.com.hyweb.service.db.info.TbCustModifyLogPK;
import tw.com.hyweb.service.db.info.TbFeeCalLogInfo;
import tw.com.hyweb.service.db.info.TbFeeCalLogPK;
import tw.com.hyweb.service.db.info.TbFeeDefLogPK;
import tw.com.hyweb.service.db.info.TbSettleConfigLogInfo;
import tw.com.hyweb.service.db.info.TbSettleConfigLogPK;
import tw.com.hyweb.service.db.mgr.TbCustModifyLogMgr;
import tw.com.hyweb.service.db.mgr.TbFeeCalLogMgr;
import tw.com.hyweb.service.db.mgr.TbSettleConfigLogMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ProcFeeCalModifyLog extends AbstractBatchBasic{

	private static Logger log = Logger.getLogger(ProcFeeCalModifyLog.class);
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
	            "impdata" + File.separator + "procFeeCalModifyLog" + File.separator + "spring.xml";

	// 要比對的欄位
    private List fileNameLists = null;
    private String batchDate = "";
    private String RecoverLevel = "";
    private String MODIFY_DESC = "";
    private String MODIFY_TYPE = "";
    private String currData = "";
    private String prevData = "";
    private String uptStatus = "";
    

	public void process(String[] argv) throws Exception 
	{
		if ( getRecoverLevel()!=null && 
                (RecoverLevel.equals(Constants.RECOVER_LEVEL_ALL)||
                 RecoverLevel.equals(Constants.RECOVER_LEVEL_ERR))
               ) 
		{
			recoverData();
			return;
		}
		
		Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
		conn.setAutoCommit(false);
		
        Statement currFeeCalInfoStmt = null;
        ResultSet currFeeCalInfo = null;

		String currFeeCalInfoSql = "SELECT a.CAL_RULE_ID,a.CAL_DESC,a.CAL_FORMULA,a.CAL_BASE,a.CARRY_DIGIT,"
				+ "a.CARRY_TYPE,b.UPPER_BOUND,b.FEE_RATE,a.UPT_STATUS as CAL_STATUS, b.UPT_STATUS as TIER_STATUS,"
				+ "a.UPT_USERID,a.APRV_USERID,a.APRV_DATE,a.APRV_TIME "
				+ "FROM TB_FEE_CAL_UPT a,TB_FEE_TIER_UPT b "
				+ "WHERE  a.CAL_RULE_ID=b.CAL_RULE_ID "
				+ "AND a.UPT_DATE=b.UPT_DATE AND a.UPT_TIME=b.UPT_TIME "
				+ "AND a.APRV_DATE = '"
				+ BatchUtil.getSomeDay(batchDate, -1)
				+ "' "
				+ "AND a.APRV_STATUS ='1' "
				+ "AND NOT (a.UPT_STATUS='2' AND b.UPT_STATUS='3') "
				+ "ORDER BY a.CAL_RULE_ID,a.APRV_DATE,a.APRV_TIME ";
        try {
        	currFeeCalInfoStmt = conn.createStatement();
            log.debug("currFeeCalInfoSql: "+currFeeCalInfoSql);
            currFeeCalInfo = currFeeCalInfoStmt.executeQuery(currFeeCalInfoSql.toString());

			while (currFeeCalInfo.next()) {
				uptStatus = currFeeCalInfo.getString(9);

				if (uptStatus.equals("2")) {
            		
                	List diffFileNameLists = new ArrayList();
                	
                	StringBuffer prevFeeCalInfoSql = new StringBuffer();
                	prevFeeCalInfoSql.append("SELECT CAL_RULE_ID,CAL_DESC,CAL_FORMULA,CAL_BASE,CARRY_DIGIT,CARRY_TYPE,UPPER_BOUND,FEE_RATE ");
                	prevFeeCalInfoSql.append("FROM(SELECT a.CAL_RULE_ID,a.CAL_DESC,a.CAL_FORMULA,a.CAL_BASE,a.CARRY_DIGIT,a.CARRY_TYPE,");
                	prevFeeCalInfoSql.append("b.UPPER_BOUND, b.FEE_RATE FROM TB_FEE_CAL_UPT a, TB_FEE_TIER_UPT b ");
                	prevFeeCalInfoSql.append("WHERE a.CAL_RULE_ID=b.CAL_RULE_ID ");
                	prevFeeCalInfoSql.append("AND a.UPT_DATE=b.UPT_DATE and a.UPT_TIME=b.UPT_TIME ");
                	prevFeeCalInfoSql.append("AND a.CAL_RULE_ID='"+currFeeCalInfo.getString("CAL_RULE_ID")+"' ");
                	prevFeeCalInfoSql.append("AND b.UPPER_BOUND = '"+currFeeCalInfo.getString("UPPER_BOUND")+"' ");
                	prevFeeCalInfoSql.append("AND a.APRV_STATUS='1' AND NOT (a.UPT_STATUS='2' AND b.UPT_STATUS='3') ");
                	prevFeeCalInfoSql.append("AND a.APRV_DATE||a.APRV_TIME < "+currFeeCalInfo.getString("APRV_DATE")+"||"+currFeeCalInfo.getString("APRV_TIME"));
                	prevFeeCalInfoSql.append(" ORDER BY a.APRV_DATE DESC, a.APRV_TIME DESC) ");
                	prevFeeCalInfoSql.append("WHERE ROWNUM=1 ");
                	//log.debug("prevFeeCalInfoSql: "+prevFeeCalInfoSql.toString());
                	
                	Statement prevFeeCalInfoStmt = null;
                    ResultSet prevFeeCalInfo = null;                
                    prevFeeCalInfoStmt = conn.createStatement();             
                    prevFeeCalInfo = prevFeeCalInfoStmt.executeQuery(prevFeeCalInfoSql.toString());                
                    if (prevFeeCalInfo.next()) {
                    	
                    	String fileName = "";
                    	
                    	for( int i = 0; i < fileNameLists.size(); i++ ){
                    		
                    		fileName = fileNameLists.get(i).toString();
                    		currData = currFeeCalInfo.getString(fileName)==null?"" :currFeeCalInfo.getString(fileName);
                    		prevData = prevFeeCalInfo.getString(fileName)==null?"" :prevFeeCalInfo.getString(fileName);
                    		
                    		if(prevFeeCalInfo != null){
                    			if(!currData.equals(prevData)){
                            		diffFileNameLists.add(fileName);
                            	}
                    		}
                    	}
                    	insertModifyLogs(currFeeCalInfo, prevFeeCalInfo, diffFileNameLists, conn);//處理differFileName
                    }
                    ReleaseResource.releaseDB(null, prevFeeCalInfoStmt, prevFeeCalInfo);
            	}
            	else{
            		//新增-1或刪除-3 insert 原資料
            		insertFeeCalModifyLogs(currFeeCalInfo, conn);
            	}
            }
            conn.commit();
        }
        catch(Throwable e){
        	conn.rollback();
        	throw new RuntimeException(e);  
        }
        finally {        	  
        	ReleaseResource.releaseDB(conn, currFeeCalInfoStmt, currFeeCalInfo);
        }     
	}
	
	public void insertModifyLogs (ResultSet currFeeCalInfo, ResultSet prevFeeCalInfo, 
			List diffFileNameLists, Connection connection) throws SQLException{
		
		if ( diffFileNameLists.size() > 0 ){
			String dateTime = DateUtil.getTodayString();
	        String sysDate = dateTime.substring(0, 8);
	        String sysTime = dateTime.substring(8, 14);
	       
			for ( int i = 0; i < diffFileNameLists.size(); i++ ){
				
				MODIFY_TYPE = diffFileNameLists.get(i).toString();
				MODIFY_DESC = prevFeeCalInfo.getString(diffFileNameLists.get(i).toString());

				TbFeeCalLogPK pk = new TbFeeCalLogPK();
				pk.setCalRuleId(currFeeCalInfo.getString("CAL_RULE_ID"));
				pk.setUpperBound(currFeeCalInfo.getDouble("UPPER_BOUND"));
				pk.setAprvDate(currFeeCalInfo.getString("APRV_DATE"));
				pk.setAprvTime(currFeeCalInfo.getString("APRV_TIME"));
				pk.setModifyDesc(MODIFY_TYPE);
		    	
				TbFeeCalLogInfo tbFeeCalLogInfo = new TbFeeCalLogMgr(connection).querySingle(pk);
		        
		        if (tbFeeCalLogInfo == null){
		        	TbFeeCalLogInfo tbFeeCalLog = new  TbFeeCalLogInfo();
		        	tbFeeCalLog.setCalRuleId(currFeeCalInfo.getString("CAL_RULE_ID"));
		        	tbFeeCalLog.setCalDesc(currFeeCalInfo.getString("CAL_DESC"));
		        	tbFeeCalLog.setCalFormula(currFeeCalInfo.getString("CAL_FORMULA"));
		        	tbFeeCalLog.setCalBase(currFeeCalInfo.getString("CAL_BASE"));
		        	tbFeeCalLog.setCarryType(currFeeCalInfo.getString("CARRY_TYPE"));
		        	tbFeeCalLog.setUpperBound(currFeeCalInfo.getDouble("UPPER_BOUND"));
		        	tbFeeCalLog.setFeeRate(Double.valueOf(currFeeCalInfo.getString("FEE_RATE")));
		        	tbFeeCalLog.setCarryDigit(Integer.valueOf(currFeeCalInfo.getString("CARRY_DIGIT")));
		        	tbFeeCalLog.setAprvDate(currFeeCalInfo.getString("APRV_DATE"));
		        	tbFeeCalLog.setAprvTime(currFeeCalInfo.getString("APRV_TIME"));
		        	tbFeeCalLog.setUptUserid(currFeeCalInfo.getString("UPT_USERID"));
		        	tbFeeCalLog.setAprvUserid(currFeeCalInfo.getString("APRV_USERID"));
		        	tbFeeCalLog.setUptStatus(currFeeCalInfo.getString("CAL_STATUS"));
		        	tbFeeCalLog.setModifyType(MODIFY_TYPE);
		        	tbFeeCalLog.setModifyDesc(MODIFY_DESC);
		        	tbFeeCalLog.setSysDate(sysDate);
		        	tbFeeCalLog.setSysTime(sysTime);
		        	new TbFeeCalLogMgr(connection).insert(tbFeeCalLog);
		        }
			}
		}
	}
	
	public void insertFeeCalModifyLogs (ResultSet currFeeCalInfo,Connection connection) throws SQLException
	{
		String dateTime = DateUtil.getTodayString();
	    String sysDate = dateTime.substring(0, 8);
	    String sysTime = dateTime.substring(8, 14);
		if (uptStatus.equals("1")) {
			MODIFY_TYPE = "0";
			MODIFY_DESC = "新增";
		} else {
			MODIFY_TYPE = "9";
			MODIFY_DESC = "刪除";
		}
		TbFeeCalLogPK pk = new TbFeeCalLogPK();
		pk.setCalRuleId(currFeeCalInfo.getString("CAL_RULE_ID"));
		pk.setUpperBound(currFeeCalInfo.getDouble("UPPER_BOUND"));
		pk.setAprvDate(currFeeCalInfo.getString("APRV_DATE"));
		pk.setAprvTime(currFeeCalInfo.getString("APRV_TIME"));
		pk.setModifyDesc(MODIFY_TYPE);

		TbFeeCalLogInfo tbFeeCalLogInfo = new TbFeeCalLogMgr(connection).querySingle(pk);

		if (tbFeeCalLogInfo == null) {
			TbFeeCalLogInfo tbFeeCalLog = new TbFeeCalLogInfo();
			tbFeeCalLog.setCalRuleId(currFeeCalInfo.getString("CAL_RULE_ID"));
			tbFeeCalLog.setCalDesc(currFeeCalInfo.getString("CAL_DESC"));
			tbFeeCalLog.setCalFormula(currFeeCalInfo.getString("CAL_FORMULA"));
			tbFeeCalLog.setCalBase(currFeeCalInfo.getString("CAL_BASE"));
			tbFeeCalLog.setCarryType(currFeeCalInfo.getString("CARRY_TYPE"));
			tbFeeCalLog.setUpperBound(currFeeCalInfo.getDouble("UPPER_BOUND"));
			tbFeeCalLog.setFeeRate(Double.valueOf(currFeeCalInfo.getString("FEE_RATE")));
			tbFeeCalLog.setCarryDigit(Integer.valueOf(currFeeCalInfo.getString("CARRY_DIGIT")));
			tbFeeCalLog.setAprvDate(currFeeCalInfo.getString("APRV_DATE"));
			tbFeeCalLog.setAprvTime(currFeeCalInfo.getString("APRV_TIME"));
			tbFeeCalLog.setUptUserid(currFeeCalInfo.getString("UPT_USERID"));
			tbFeeCalLog.setAprvUserid(currFeeCalInfo.getString("APRV_USERID"));
			tbFeeCalLog.setUptStatus(currFeeCalInfo.getString("CAL_STATUS"));
			tbFeeCalLog.setModifyType(MODIFY_TYPE);
			tbFeeCalLog.setModifyDesc(MODIFY_DESC);
			tbFeeCalLog.setSysDate(sysDate);
			tbFeeCalLog.setSysTime(sysTime);
			new TbFeeCalLogMgr(connection).insert(tbFeeCalLog);
		}
	}

	public static ProcFeeCalModifyLog getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(
				SPRING_PATH);
		ProcFeeCalModifyLog instance = (ProcFeeCalModifyLog) apContext
				.getBean("ProcFeeCalModifyLog");
		return instance;
	}

	public List getFileNameLists() {
		return fileNameLists;
	}

	public void setFileNameLists(List fileNameLists) {
		this.fileNameLists = fileNameLists;
	}

	public String getBatchDate() {
		return batchDate;
	}

	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}

	public String getRecoverLevel() {
		return RecoverLevel;
	}

	public void setRecoverLevel(String recoverLevel) {
		RecoverLevel = recoverLevel;
	}

	public static void main(String[] args) {
		ProcFeeCalModifyLog procFeeCalModifyLog = null;
		try {
			String batchDate = System.getProperty("date");
			if (StringUtil.isEmpty(batchDate)) {
				batchDate = DateUtil.getTodayString().substring(0, 8);
			} else if (!DateUtil.isValidDate(batchDate)) {
				log.info("invalid batchDate('" + batchDate
						+ "') using system date!");
				batchDate = DateUtil.getTodayString().substring(0, 8);
			}
			File f = new File(SPRING_PATH);
			if (f.exists() && f.isFile()) {
				procFeeCalModifyLog = getInstance();
			} else {
				procFeeCalModifyLog = new ProcFeeCalModifyLog();
			}

			procFeeCalModifyLog.setBatchDate(batchDate);
			procFeeCalModifyLog.setRecoverLevel(System.getProperty("recover"));
			procFeeCalModifyLog.run(args);
		} catch (Exception ignore) {
			log.warn("ProcFeeCalModifyLog run fail:" + ignore.getMessage(),
					ignore);
		}
	}

	protected void recoverData() throws Exception {
		Connection connSelf = DBService.getDBService().getConnection("batch");

		StringBuffer sql = new StringBuffer();

		sql.append("DELETE TB_FEE_CAL_LOG ");
		sql.append("WHERE CURR_MODIFY_DATE='")
				.append(BatchUtil.getSomeDay(batchDate, -1)).append("'");
		try {
			log.info(" recoverData():" + sql.toString());
			DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
			connSelf.commit();
		} catch (SQLException e) {
			connSelf.rollback();
			throw new Exception("recoverData():delete . " + e);
		}
	}
}
