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
import tw.com.hyweb.service.db.info.TbSettleDefLogInfo;
import tw.com.hyweb.service.db.info.TbSettleDefLogPK;
import tw.com.hyweb.service.db.mgr.TbSettleDefLogMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ProcSettleDefModifyLog extends AbstractBatchBasic{

	private static Logger log = Logger.getLogger(ProcSettleDefModifyLog.class);
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
	            "impdata" + File.separator + "procSettleDefModifyLog" + File.separator + "spring.xml";

	// 要比對的欄位
    private List fileNameLists = null;
    private String batchDate ="";
    private String RecoverLevel = "";
    private String MODIFY_DESC="";
    private String MODIFY_TYPE="";
    private String currData = "";
	private String prevData = "";
	private String uptStatus ="";

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
        ResultSet currSettleInfo  = null;

		String currSettleInfoSql = "SELECT a.SETTLE_CODE, a.SETTLE_CODE_DESC, a.CREDIT_UNIT, a.DEBIT_UNIT,"
				+ "a.ACCOUNT_CODE, b.P_CODE, b.TXN_CODE,a.UPT_DATE,a.UPT_TIME,a.APRV_DATE,a.APRV_TIME,a.UPT_STATUS,"
				+ "a.UPT_USERID, a.APRV_USERID "
				+ "FROM TB_SETTLE_DEF_UPT a, TB_SETTLE_TXN_UPT b "
				+ "WHERE a.SETTLE_CODE=b.SETTLE_CODE "
				+ "AND a.UPT_DATE=b.UPT_DATE "
				+ "AND a.UPT_TIME=b.UPT_TIME "
				+ "AND a.APRV_DATE = '"
				+ BatchUtil.getSomeDay(batchDate, -1)+ "' "
				+ "AND a.APRV_STATUS='1' "
				+ "AND NOT(a.UPT_STATUS='2' and b.UPT_STATUS='3') "
				+ "ORDER BY a.SETTLE_CODE, a.UPT_DATE, a.UPT_TIME";
        				
        try {
        	currSettleInfoStmt = conn.createStatement();
            log.debug("currSettleInfoSql: "+currSettleInfoSql);
            currSettleInfo = currSettleInfoStmt.executeQuery(currSettleInfoSql.toString());
            while (currSettleInfo.next()) {
            	uptStatus = currSettleInfo.getString("UPT_STATUS");
            	
            	if(uptStatus.equals("2")){
            		List diffFileNameLists = new ArrayList();
                	
                	StringBuffer prevSettleInfoSql = new StringBuffer();
                	prevSettleInfoSql.append("SELECT SETTLE_CODE, SETTLE_CODE_DESC, CREDIT_UNIT, DEBIT_UNIT,ACCOUNT_CODE,");
                	prevSettleInfoSql.append("P_CODE,TXN_CODE,UPT_DATE,UPT_TIME,APRV_DATE,APRV_TIME,UPT_STATUS ");
                	prevSettleInfoSql.append("FROM (SELECT a.SETTLE_CODE, a.SETTLE_CODE_DESC, a.CREDIT_UNIT, a.DEBIT_UNIT,");
                	prevSettleInfoSql.append("a.ACCOUNT_CODE, b.P_CODE, b.TXN_CODE,a.UPT_DATE, a.UPT_TIME, a.APRV_DATE,a.APRV_TIME,");
                	prevSettleInfoSql.append("a.UPT_STATUS FROM TB_SETTLE_DEF_UPT a,TB_SETTLE_TXN_UPT b ");
                	prevSettleInfoSql.append("WHERE a.SETTLE_CODE=b.SETTLE_CODE ");
                	prevSettleInfoSql.append("AND a.UPT_DATE=b.UPT_DATE ");
                	prevSettleInfoSql.append("AND a.UPT_TIME=b.UPT_TIME   ");
                	prevSettleInfoSql.append("AND a.SETTLE_CODE ='"+currSettleInfo.getString("SETTLE_CODE")+"' " );
                	prevSettleInfoSql.append("AND b.P_CODE='"+ currSettleInfo.getString("P_CODE")+"' ");
                	prevSettleInfoSql.append("AND a.APRV_STATUS='1' ");
                	prevSettleInfoSql.append("AND NOT(a.UPT_STATUS='2' and b.UPT_STATUS='3') ");
                	prevSettleInfoSql.append("AND a.APRV_DATE||a.APRV_TIME < "+currSettleInfo.getString("APRV_DATE")+"||"+currSettleInfo.getString("APRV_TIME"));
                	prevSettleInfoSql.append(" ORDER BY a.APRV_DATE DESC, a.APRV_TIME DESC) ");
                	prevSettleInfoSql.append("WHERE ROWNUM=1");
                	log.debug("prevSettleInfoSql: "+prevSettleInfoSql.toString());
                	
                	Statement prevSettleInfoStmt = null;
                    ResultSet prevSettleInfo = null;                
                    prevSettleInfoStmt = conn.createStatement();   
                    prevSettleInfo = prevSettleInfoStmt.executeQuery(prevSettleInfoSql.toString());                
					if (prevSettleInfo.next()) {

						String fileName = "";
						// log.debug("UPT_STATUS: "+currSettleInfo.getString("UPT_STATUS"));
						// if(currSettleInfo.getString("UPT_STATUS")=="2")
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
            		insertSettleDefModifyLogs(currSettleInfo, conn);
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
				//異動欄位
				MODIFY_TYPE=diffFileNameLists.get(i).toString();
				//異動前資料
				MODIFY_DESC=prevSettleInfo.getString(diffFileNameLists.get(i).toString());

				TbSettleDefLogPK pk = new TbSettleDefLogPK();
				pk.setSettleCode(currSettleInfo.getString("SETTLE_CODE"));
				pk.setPCode(currSettleInfo.getString("P_CODE"));
				pk.setTxnCode(currSettleInfo.getString("TXN_CODE"));
				pk.setAprvDate(currSettleInfo.getString("APRV_DATE"));
				pk.setAprvTime(currSettleInfo.getString("APRV_TIME"));
				pk.setModifyType(MODIFY_TYPE);

		    	
				TbSettleDefLogInfo tbSettleDefLogInfo = new TbSettleDefLogMgr(connection).querySingle(pk);
		        
		        if (tbSettleDefLogInfo == null){
		        	TbSettleDefLogInfo tbSettleDefLog = new  TbSettleDefLogInfo();
		        	tbSettleDefLog.setSettleCode(currSettleInfo.getString("SETTLE_CODE"));
		        	tbSettleDefLog.setPCode(currSettleInfo.getString("P_CODE"));
		        	tbSettleDefLog.setTxnCode(currSettleInfo.getString("TXN_CODE"));
		        	tbSettleDefLog.setAprvDate(currSettleInfo.getString("APRV_DATE"));
		        	tbSettleDefLog.setAprvTime(currSettleInfo.getString("APRV_TIME"));
		        	tbSettleDefLog.setSettleCodeDesc(currSettleInfo.getString("SETTLE_CODE_DESC"));
		        	tbSettleDefLog.setCreditUnit(currSettleInfo.getString("CREDIT_UNIT"));
		        	tbSettleDefLog.setDebitUnit(currSettleInfo.getString("DEBIT_UNIT"));
		        	tbSettleDefLog.setAccountCode(currSettleInfo.getString("ACCOUNT_CODE"));
		        	tbSettleDefLog.setUptUserid(currSettleInfo.getString("UPT_USERID"));
		        	tbSettleDefLog.setAprvUserid(currSettleInfo.getString("APRV_USERID"));
		        	tbSettleDefLog.setUptStatus(currSettleInfo.getString("UPT_STATUS"));
		        	tbSettleDefLog.setModifyDesc(MODIFY_DESC);
		        	tbSettleDefLog.setModifyType(MODIFY_TYPE);
		        	tbSettleDefLog.setSysDate(sysDate);
		        	tbSettleDefLog.setSysTime(sysTime);
		        	new TbSettleDefLogMgr(connection).insert(tbSettleDefLog);
		        }
			}
		}
	}

	public void insertSettleDefModifyLogs(ResultSet currSettleInfo,
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

		TbSettleDefLogPK pk = new TbSettleDefLogPK();
		pk.setSettleCode(currSettleInfo.getString("SETTLE_CODE"));
		pk.setPCode(currSettleInfo.getString("P_CODE"));
		pk.setTxnCode(currSettleInfo.getString("TXN_CODE"));
		pk.setAprvDate(currSettleInfo.getString("APRV_DATE"));
		pk.setAprvTime(currSettleInfo.getString("APRV_TIME"));
		pk.setModifyType(MODIFY_TYPE);

		TbSettleDefLogInfo tbSettleDefLogInfo = new TbSettleDefLogMgr(
				connection).querySingle(pk);

		if (tbSettleDefLogInfo == null) {
			TbSettleDefLogInfo tbSettleDefLog = new TbSettleDefLogInfo();
			tbSettleDefLog.setSettleCode(currSettleInfo.getString("SETTLE_CODE"));
			tbSettleDefLog.setPCode(currSettleInfo.getString("P_CODE"));
			tbSettleDefLog.setTxnCode(currSettleInfo.getString("TXN_CODE"));
			tbSettleDefLog.setAprvDate(currSettleInfo.getString("APRV_DATE"));
			tbSettleDefLog.setAprvTime(currSettleInfo.getString("APRV_TIME"));
			tbSettleDefLog.setSettleCodeDesc(currSettleInfo.getString("SETTLE_CODE_DESC"));
			tbSettleDefLog.setCreditUnit(currSettleInfo.getString("CREDIT_UNIT"));
			tbSettleDefLog.setDebitUnit(currSettleInfo.getString("DEBIT_UNIT"));
			tbSettleDefLog.setAccountCode(currSettleInfo.getString("ACCOUNT_CODE"));
			tbSettleDefLog.setUptUserid(currSettleInfo.getString("UPT_USERID"));
			tbSettleDefLog.setAprvUserid(currSettleInfo.getString("APRV_USERID"));
			tbSettleDefLog.setUptStatus(currSettleInfo.getString("UPT_STATUS"));
			tbSettleDefLog.setModifyDesc(MODIFY_DESC);
			tbSettleDefLog.setModifyType(MODIFY_TYPE);
			tbSettleDefLog.setSysDate(sysDate);
			tbSettleDefLog.setSysTime(sysTime);
			new TbSettleDefLogMgr(connection).insert(tbSettleDefLog);
		}
	}
	public static ProcSettleDefModifyLog getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(
				SPRING_PATH);
		ProcSettleDefModifyLog instance = (ProcSettleDefModifyLog) apContext
				.getBean("ProcSettleDefModifyLog");
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
		ProcSettleDefModifyLog procSettleDefModifyLog = null;
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
				procSettleDefModifyLog = getInstance();
			} else {
				procSettleDefModifyLog = new ProcSettleDefModifyLog();
			}

			procSettleDefModifyLog.setBatchDate(batchDate);
			procSettleDefModifyLog.setRecoverLevel(System
					.getProperty("recover"));
			procSettleDefModifyLog.run(args);
		} catch (Exception ignore) {
			log.warn("ProcSettleDefModifyLog run fail:" + ignore.getMessage(),
					ignore);
		}
	}
	
	protected void recoverData() throws Exception {
		Connection connSelf = DBService.getDBService().getConnection("batch");

		StringBuffer sql = new StringBuffer();

		// Delete TB_SETTLE_DEF_LOG
		sql.append("DELETE TB_SETTLE_DEF_LOG ");
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
