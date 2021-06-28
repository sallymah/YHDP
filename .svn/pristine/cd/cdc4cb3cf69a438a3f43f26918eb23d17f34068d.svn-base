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
import tw.com.hyweb.service.db.info.TbSettleConfigLogInfo;
import tw.com.hyweb.service.db.info.TbSettleConfigLogPK;
import tw.com.hyweb.service.db.mgr.TbCustModifyLogMgr;
import tw.com.hyweb.service.db.mgr.TbSettleConfigLogMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ProcSettleConfigModifyLog extends AbstractBatchBasic{

	private static Logger log = Logger.getLogger(ProcSettleConfigModifyLog.class);
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
	            "impdata" + File.separator + "procSettleConfigModifyLog" + File.separator + "spring.xml";

	// 要比對的欄位
    private List fileNameLists = null;
    private String batchDate ="";
    private String RecoverLevel = "";
    private String MODIFY_DESC="";
    private String MODIFY_TYPE="";
    private String currData = "";
	private String prevData = "";
	private String uptStatus = "";
    

	public void process(String[] argv) throws Exception {

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
		
        Statement currSettleInfoStmt = null;
        ResultSet currSettleInfo = null;

		String currSettleInfoSql = "SELECT ISS_MEM_ID,ACQ_MEM_ID,SETTLE_CODE,BONUS_ID,VALID_SDATE,"
				+ "VALID_EDATE,SETTLE_RATE,SETTLE_FROM,CARRY_DIGIT,CARRY_TYPE,FUND_TYPE,"
				+ "UPT_STATUS,UPT_USERID,APRV_USERID,APRV_DATE,APRV_TIME "
				+ "FROM TB_SETTLE_CONFIG_UPT "
				+ "WHERE APRV_DATE = '"
				+ BatchUtil.getSomeDay(batchDate, -1)+ "' "
				+ "AND APRV_STATUS ='1' "
				+ "ORDER BY ISS_MEM_ID,ACQ_MEM_ID,SETTLE_CODE, BONUS_ID,VALID_SDATE,APRV_DATE,APRV_TIME ";
		try {
			currSettleInfoStmt = conn.createStatement();
			log.debug("currSettleInfoSql: " + currSettleInfoSql);
			currSettleInfo = currSettleInfoStmt.executeQuery(currSettleInfoSql.toString());
			while (currSettleInfo.next()) {
				uptStatus = currSettleInfo.getString(12);

				if (uptStatus.equals("2")) {
            		
            		List diffFileNameLists = new ArrayList();
                	
                	StringBuffer prevSettleInfoSql = new StringBuffer();
                	prevSettleInfoSql.append("SELECT VALID_EDATE, SETTLE_RATE,SETTLE_FROM, CARRY_DIGIT, CARRY_TYPE, FUND_TYPE,APRV_DATE,APRV_TIME,BONUS_ID ");
                	prevSettleInfoSql.append("FROM( SELECT VALID_EDATE, SETTLE_RATE,SETTLE_FROM, CARRY_DIGIT, CARRY_TYPE, FUND_TYPE,APRV_DATE,APRV_TIME,BONUS_ID FROM TB_SETTLE_CONFIG_UPT ");
                	prevSettleInfoSql.append("WHERE SETTLE_CODE='"+currSettleInfo.getString("SETTLE_CODE")+"' ");
                	prevSettleInfoSql.append("AND ACQ_MEM_ID = '"+currSettleInfo.getString("ACQ_MEM_ID")+"' ");
                	prevSettleInfoSql.append("AND ISS_MEM_ID ='"+currSettleInfo.getString("ISS_MEM_ID")+"' ");
                	prevSettleInfoSql.append("AND BONUS_ID ='"+currSettleInfo.getString("BONUS_ID")+"' ");
                	prevSettleInfoSql.append("AND VALID_SDATE ='"+currSettleInfo.getString("VALID_SDATE")+"' AND APRV_STATUS='1' " );
                	prevSettleInfoSql.append("AND APRV_DATE||APRV_TIME < "+currSettleInfo.getString("APRV_DATE")+"||"+currSettleInfo.getString("APRV_TIME"));
                	prevSettleInfoSql.append(" ORDER BY APRV_DATE DESC, APRV_TIME DESC) ");
                	prevSettleInfoSql.append("WHERE ROWNUM=1 ");
                	//log.debug("prevCustInfoSql: "+prevCustInfoSql.toString());
                	
                	Statement prevSettleInfoStmt = null;
                    ResultSet prevSettleInfo = null;                
                    prevSettleInfoStmt = conn.createStatement();   
                    log.debug("prevSettleInfoSql: "+prevSettleInfoSql);
                    prevSettleInfo = prevSettleInfoStmt.executeQuery(prevSettleInfoSql.toString());                
					if (prevSettleInfo.next()) {

						String fileName = "";
						for (int i = 0; i < fileNameLists.size(); i++) {
							fileName = fileNameLists.get(i).toString();

							currData = currSettleInfo.getString(fileName) == null ? ""
									: currSettleInfo.getString(fileName);
							prevData = prevSettleInfo.getString(fileName) == null ? ""
									: prevSettleInfo.getString(fileName);
							if (!currData.equals(prevData)) {
								diffFileNameLists.add(fileName);

							}
						}
						insertModifyLogs(currSettleInfo, prevSettleInfo,
								diffFileNameLists, conn);
					}
                    ReleaseResource.releaseDB(null, prevSettleInfoStmt, prevSettleInfo);
            	}
            	else{
            		insertSettleConfigModifyLogs(currSettleInfo, conn);
            	}
            }
            conn.commit();
        }
        catch(Throwable e){
        	conn.rollback();
        	throw new RuntimeException(e);  
        }
        finally {        	  
        	ReleaseResource.releaseDB(conn, currSettleInfoStmt, currSettleInfo);
        }     
	}
	
	public void insertModifyLogs (ResultSet currSettleInfo, ResultSet prevSettleInfo, 
			List diffFileNameLists, Connection connection) throws SQLException{
		
		if ( diffFileNameLists.size() > 0 ){
			String dateTime = DateUtil.getTodayString();
	        String sysDate = dateTime.substring(0, 8);
	        String sysTime = dateTime.substring(8, 14);
	       
			for ( int i = 0; i < diffFileNameLists.size(); i++ ){
				
				MODIFY_TYPE=diffFileNameLists.get(i).toString();
				MODIFY_DESC=prevSettleInfo.getString(diffFileNameLists.get(i).toString());

				TbSettleConfigLogPK pk = new TbSettleConfigLogPK();
				pk.setSettleCode(currSettleInfo.getString("SETTLE_CODE"));
				pk.setAcqMemId(currSettleInfo.getString("ACQ_MEM_ID"));
				pk.setIssMemId(currSettleInfo.getString("ISS_MEM_ID"));
				pk.setBonusId(currSettleInfo.getString("BONUS_ID"));
				pk.setValidSdate(currSettleInfo.getString("VALID_SDATE"));
				pk.setAprvDate(currSettleInfo.getString("APRV_DATE"));
				pk.setAprvTime(currSettleInfo.getString("APRV_TIME"));
				pk.setModifyType(MODIFY_TYPE);
		    	
				TbSettleConfigLogInfo tbSettleConfigLogInfo = new TbSettleConfigLogMgr(connection).querySingle(pk);
		        
		        if (tbSettleConfigLogInfo == null){
		        	TbSettleConfigLogInfo tbSettleConfigLog = new  TbSettleConfigLogInfo();
		        	tbSettleConfigLog.setSettleCode(currSettleInfo.getString("SETTLE_CODE"));
		        	tbSettleConfigLog.setAcqMemId(currSettleInfo.getString("ACQ_MEM_ID"));
		        	tbSettleConfigLog.setIssMemId(currSettleInfo.getString("ISS_MEM_ID"));
		        	tbSettleConfigLog.setBonusId(currSettleInfo.getString("BONUS_ID"));
		        	tbSettleConfigLog.setValidSdate(currSettleInfo.getString("VALID_SDATE"));
		        	tbSettleConfigLog.setAprvDate(currSettleInfo.getString("APRV_DATE"));
		        	tbSettleConfigLog.setAprvTime(currSettleInfo.getString("APRV_TIME"));	
		        	tbSettleConfigLog.setValidEdate(currSettleInfo.getString("VALID_EDATE"));
		        	tbSettleConfigLog.setSettleRate(Double.valueOf(currSettleInfo.getString("SETTLE_RATE")));
		        	tbSettleConfigLog.setCarryDigit(Integer.valueOf(currSettleInfo.getString("CARRY_DIGIT")));
		        	tbSettleConfigLog.setCarryType(currSettleInfo.getString("CARRY_TYPE"));
		        	tbSettleConfigLog.setFundType(currSettleInfo.getString("FUND_TYPE"));
		        	tbSettleConfigLog.setUptUserid(currSettleInfo.getString("UPT_USERID"));
		        	tbSettleConfigLog.setAprvUserid(currSettleInfo.getString("APRV_USERID"));
		        	tbSettleConfigLog.setUptStatus(currSettleInfo.getString("UPT_STATUS"));
		        	tbSettleConfigLog.setModifyType(MODIFY_TYPE);
		        	tbSettleConfigLog.setModifyDesc(MODIFY_DESC);
		        	tbSettleConfigLog.setSysDate(sysDate);
		        	tbSettleConfigLog.setSysTime(sysTime);
		        	new TbSettleConfigLogMgr(connection).insert(tbSettleConfigLog);
		        }
			}
		}
	}

	public void insertSettleConfigModifyLogs(ResultSet currSettleInfo,
			Connection connection) throws SQLException {

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
		TbSettleConfigLogPK pk = new TbSettleConfigLogPK();
		pk.setSettleCode(currSettleInfo.getString("SETTLE_CODE"));
		pk.setAcqMemId(currSettleInfo.getString("ACQ_MEM_ID"));
		pk.setIssMemId(currSettleInfo.getString("ISS_MEM_ID"));
		pk.setBonusId(currSettleInfo.getString("BONUS_ID"));
		pk.setValidSdate(currSettleInfo.getString("VALID_SDATE"));
		pk.setAprvDate(currSettleInfo.getString("APRV_DATE"));
		pk.setAprvTime(currSettleInfo.getString("APRV_TIME"));
		pk.setModifyType(MODIFY_TYPE);

		TbSettleConfigLogInfo tbSettleConfigLogInfo = new TbSettleConfigLogMgr(
				connection).querySingle(pk);

		if (tbSettleConfigLogInfo == null) {
			TbSettleConfigLogInfo tbSettleConfigLog = new TbSettleConfigLogInfo();
			tbSettleConfigLog.setSettleCode(currSettleInfo.getString("SETTLE_CODE"));
			tbSettleConfigLog.setAcqMemId(currSettleInfo.getString("ACQ_MEM_ID"));
			tbSettleConfigLog.setIssMemId(currSettleInfo.getString("ISS_MEM_ID"));
			tbSettleConfigLog.setBonusId(currSettleInfo.getString("BONUS_ID"));
			tbSettleConfigLog.setValidSdate(currSettleInfo.getString("VALID_SDATE"));
			tbSettleConfigLog.setAprvDate(currSettleInfo.getString("APRV_DATE"));
			tbSettleConfigLog.setAprvTime(currSettleInfo.getString("APRV_TIME"));
			tbSettleConfigLog.setValidEdate(currSettleInfo.getString("VALID_EDATE"));
			tbSettleConfigLog.setSettleRate(Double.valueOf(currSettleInfo.getString("SETTLE_RATE")));
			tbSettleConfigLog.setCarryDigit(Integer.valueOf(currSettleInfo.getString("CARRY_DIGIT")));
			tbSettleConfigLog.setCarryType(currSettleInfo.getString("CARRY_TYPE"));
			tbSettleConfigLog.setFundType(currSettleInfo.getString("FUND_TYPE"));
			tbSettleConfigLog.setUptUserid(currSettleInfo.getString("UPT_USERID"));
			tbSettleConfigLog.setAprvUserid(currSettleInfo.getString("APRV_USERID"));
			tbSettleConfigLog.setUptStatus(currSettleInfo.getString("UPT_STATUS"));
			tbSettleConfigLog.setModifyType(MODIFY_TYPE);
			tbSettleConfigLog.setModifyDesc(MODIFY_DESC);
			tbSettleConfigLog.setSysDate(sysDate);
			tbSettleConfigLog.setSysTime(sysTime);
			new TbSettleConfigLogMgr(connection).insert(tbSettleConfigLog);
		}
	}

	public static ProcSettleConfigModifyLog getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(
				SPRING_PATH);
		ProcSettleConfigModifyLog instance = (ProcSettleConfigModifyLog) apContext
				.getBean("ProcSettleConfigModifyLog");
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
		ProcSettleConfigModifyLog procSettleConfigModifyLog = null;
        try {
            String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            else if (!DateUtil.isValidDate(batchDate)) {
                log.info("invalid batchDate('" + batchDate + "') using system date!");
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	procSettleConfigModifyLog = getInstance();
            }
            else {
            	procSettleConfigModifyLog = new ProcSettleConfigModifyLog();
            }
            
            procSettleConfigModifyLog.setBatchDate(batchDate);
            procSettleConfigModifyLog.setRecoverLevel(System.getProperty("recover"));
            procSettleConfigModifyLog.run(args);
        }
        catch (Exception ignore) {
            log.warn("ProcSettleConfigModifyLog run fail:" + ignore.getMessage(), ignore);
        }
    }
	
	protected void recoverData() throws Exception {
		Connection connSelf = DBService.getDBService().getConnection("batch");

		StringBuffer sql = new StringBuffer();

		// Delete TB_SETTLE_CONFIG_LOG
		sql.append("DELETE TB_SETTLE_CONFIG_LOG ");
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
