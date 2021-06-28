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
import tw.com.hyweb.service.db.info.TbFeeDefLogInfo;
import tw.com.hyweb.service.db.info.TbFeeDefLogPK;
import tw.com.hyweb.service.db.info.TbSettleConfigLogInfo;
import tw.com.hyweb.service.db.info.TbSettleConfigLogPK;
import tw.com.hyweb.service.db.mgr.TbCustModifyLogMgr;
import tw.com.hyweb.service.db.mgr.TbFeeDefLogMgr;
import tw.com.hyweb.service.db.mgr.TbSettleConfigLogMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ProcFeeDefModifyLog extends AbstractBatchBasic{

	private static Logger log = Logger.getLogger(ProcFeeDefModifyLog.class);
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
	            "impdata" + File.separator + "procFeeDefModifyLog" + File.separator + "spring.xml";

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
		
        Statement currFeeDefInfoStmt = null;
        ResultSet currFeeDefInfo = null;

        String currFeeDefInfoSql = "SELECT a.FEE_CODE, a.FEE_CODE_DESC, a.CREDIT_UNIT, a.DEBIT_UNIT," +
				"a.ACCOUNT_CODE, b.P_CODE,a.UPT_DATE,a.UPT_TIME,a.APRV_DATE,a.APRV_TIME,a.UPT_STATUS,a.UPT_USERID, a.APRV_USERID "+
				"FROM TB_FEE_DEF_UPT a, TB_FEE_TXN_UPT b "+
				"WHERE a.FEE_CODE=b.FEE_CODE "+
				"AND a.UPT_DATE=b.UPT_DATE "+
				"AND a.UPT_TIME=b.UPT_TIME "+
				"AND a.APRV_DATE = '"+BatchUtil.getSomeDay(batchDate,-1)+"' " +
				"AND a.APRV_STATUS='1' " +
				"AND NOT(a.UPT_STATUS='2' and b.UPT_STATUS='3') " +
				"ORDER BY a.FEE_CODE, a.UPT_DATE, a.UPT_TIME";
        try {
        	currFeeDefInfoStmt = conn.createStatement();
            log.debug("currFeeDefInfoSql: "+currFeeDefInfoSql);
            currFeeDefInfo = currFeeDefInfoStmt.executeQuery(currFeeDefInfoSql.toString());
            
            while (currFeeDefInfo.next()) {
            	uptStatus = currFeeDefInfo.getString(11);
            	
            	if(uptStatus.equals("2")){
            		List diffFileNameLists = new ArrayList();
                	
                   	StringBuffer prevFeeDefInfoSql = new StringBuffer();
                	prevFeeDefInfoSql.append("SELECT FEE_CODE, FEE_CODE_DESC, CREDIT_UNIT, DEBIT_UNIT,ACCOUNT_CODE,");
                	prevFeeDefInfoSql.append("P_CODE,UPT_DATE,UPT_TIME,APRV_DATE,APRV_TIME,UPT_STATUS ");
                	prevFeeDefInfoSql.append("FROM (SELECT a.FEE_CODE, a.FEE_CODE_DESC, a.CREDIT_UNIT, a.DEBIT_UNIT, ");
                	prevFeeDefInfoSql.append("a.ACCOUNT_CODE, b.P_CODE,a.UPT_DATE, a.UPT_TIME, a.APRV_DATE,a.APRV_TIME, a.UPT_STATUS ");
                	prevFeeDefInfoSql.append("FROM TB_FEE_DEF_UPT a,TB_FEE_TXN_UPT b ");
                	prevFeeDefInfoSql.append("WHERE a.FEE_CODE=b.FEE_CODE ");
                	prevFeeDefInfoSql.append("AND a.UPT_DATE=b.UPT_DATE ");
                	prevFeeDefInfoSql.append("AND a.UPT_TIME=b.UPT_TIME   ");
                	prevFeeDefInfoSql.append("AND a.FEE_CODE ='"+currFeeDefInfo.getString("FEE_CODE")+"' " );
                	prevFeeDefInfoSql.append("AND b.P_CODE='"+ currFeeDefInfo.getString("P_CODE")+"' ");
                	prevFeeDefInfoSql.append("AND a.APRV_STATUS='1' ");
                	prevFeeDefInfoSql.append("AND NOT(a.UPT_STATUS='2' and b.UPT_STATUS='3') ");
                	prevFeeDefInfoSql.append("AND a.APRV_DATE||a.APRV_TIME < "+currFeeDefInfo.getString("APRV_DATE")+"||"+currFeeDefInfo.getString("APRV_TIME"));
                	prevFeeDefInfoSql.append(" ORDER BY a.APRV_DATE DESC, a.APRV_TIME DESC) ");
                	prevFeeDefInfoSql.append("WHERE ROWNUM=1");
                	//log.debug("prevCustInfoSql: "+prevCustInfoSql.toString());
                	
                	Statement prevFeeDefInfoStmt = null;
                    ResultSet prevFeeDefInfo = null;                
                    prevFeeDefInfoStmt = conn.createStatement();  
                    prevFeeDefInfo = prevFeeDefInfoStmt.executeQuery(prevFeeDefInfoSql.toString());                
                    if (prevFeeDefInfo.next()) {
                    	
                    	String fileName = "";
                    	
                    	for( int i = 0; i < fileNameLists.size(); i++ ){
                    		
                    		fileName = fileNameLists.get(i).toString();
                    		currData = currFeeDefInfo.getString(fileName)==null?"" :currFeeDefInfo.getString(fileName);
                    		prevData = prevFeeDefInfo.getString(fileName)==null?"" :prevFeeDefInfo.getString(fileName);
                    		if(prevFeeDefInfo !=null){
    	                		if(!currData.equals(prevData)){
    	                    		diffFileNameLists.add(fileName);
    	                    	}
                    		}
                    	}
                    	insertModifyLogs(currFeeDefInfo, prevFeeDefInfo, diffFileNameLists, conn);
                    }
                    ReleaseResource.releaseDB(null, prevFeeDefInfoStmt, prevFeeDefInfo);
            	}
            	else{
            		insertFeeDefModifyLogs(currFeeDefInfo, conn);
            	}
            	
            }
            conn.commit();
        }
        catch(Throwable e){
        	conn.rollback();
        	throw new RuntimeException(e);  
        }
        finally {        	  
        	ReleaseResource.releaseDB(conn, currFeeDefInfoStmt, currFeeDefInfo);
        }     
	
	}
	
	public void insertModifyLogs (ResultSet currFeeDefInfo, ResultSet prevFeeDefInfo, 
			List diffFileNameLists, Connection connection) throws SQLException{
		
		if ( diffFileNameLists.size() > 0 ){
			String dateTime = DateUtil.getTodayString();
	        String sysDate = dateTime.substring(0, 8);
	        String sysTime = dateTime.substring(8, 14);
	       
			for ( int i = 0; i < diffFileNameLists.size(); i++ ){
				
				MODIFY_TYPE=diffFileNameLists.get(i).toString();
				MODIFY_DESC=prevFeeDefInfo.getString(diffFileNameLists.get(i).toString());

				TbFeeDefLogPK pk = new TbFeeDefLogPK();
				pk.setFeeCode(currFeeDefInfo.getString("FEE_CODE"));
				pk.setPCode(currFeeDefInfo.getString("P_CODE"));
				pk.setAprvDate(currFeeDefInfo.getString("APRV_DATE"));
				pk.setAprvTime(currFeeDefInfo.getString("APRV_TIME"));
				pk.setModifyType(MODIFY_TYPE);
		    	
				TbFeeDefLogInfo tbFeeDefLogInfo = new TbFeeDefLogMgr(connection).querySingle(pk);
		        
		        if (tbFeeDefLogInfo == null){
		        	TbFeeDefLogInfo tbFeeDefLog = new  TbFeeDefLogInfo();
		        	tbFeeDefLog.setFeeCode(currFeeDefInfo.getString("FEE_CODE"));
		        	tbFeeDefLog.setFeeCodeDesc(currFeeDefInfo.getString("FEE_CODE_DESC"));
		        	tbFeeDefLog.setPCode(currFeeDefInfo.getString("P_CODE"));
		        	tbFeeDefLog.setAprvDate(currFeeDefInfo.getString("APRV_DATE"));
		        	tbFeeDefLog.setAprvTime(currFeeDefInfo.getString("APRV_TIME"));
		        	tbFeeDefLog.setCreditUnit(currFeeDefInfo.getString("CREDIT_UNIT"));
		        	tbFeeDefLog.setDebitUnit(currFeeDefInfo.getString("DEBIT_UNIT"));
		        	tbFeeDefLog.setAccountCode(currFeeDefInfo.getString("ACCOUNT_CODE"));
		        	tbFeeDefLog.setUptUserid(currFeeDefInfo.getString("UPT_USERID"));
		        	tbFeeDefLog.setAprvUserid(currFeeDefInfo.getString("APRV_USERID"));
		        	tbFeeDefLog.setUptStatus(currFeeDefInfo.getString("UPT_STATUS"));
		        	tbFeeDefLog.setModifyType(MODIFY_TYPE);
		        	tbFeeDefLog.setModifyDesc(MODIFY_DESC);
		        	tbFeeDefLog.setSysDate(sysDate);
		        	tbFeeDefLog.setSysTime(sysTime);
		        	new TbFeeDefLogMgr(connection).insert(tbFeeDefLog);
		        }
			}
		}
	}

	public void insertFeeDefModifyLogs(ResultSet currFeeDefInfo,
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
		TbFeeDefLogPK pk = new TbFeeDefLogPK();
		pk.setFeeCode(currFeeDefInfo.getString("FEE_CODE"));
		pk.setPCode(currFeeDefInfo.getString("P_CODE"));
		pk.setAprvDate(currFeeDefInfo.getString("APRV_DATE"));
		pk.setAprvTime(currFeeDefInfo.getString("APRV_TIME"));
		pk.setModifyType(MODIFY_TYPE);

		TbFeeDefLogInfo tbFeeDefLogInfo = new TbFeeDefLogMgr(connection)
				.querySingle(pk);

		if (tbFeeDefLogInfo == null) {
			TbFeeDefLogInfo tbFeeDefLog = new TbFeeDefLogInfo();
			tbFeeDefLog.setFeeCode(currFeeDefInfo.getString("FEE_CODE"));
			tbFeeDefLog.setFeeCodeDesc(currFeeDefInfo.getString("FEE_CODE_DESC"));
			tbFeeDefLog.setPCode(currFeeDefInfo.getString("P_CODE"));
			tbFeeDefLog.setAprvDate(currFeeDefInfo.getString("APRV_DATE"));
			tbFeeDefLog.setAprvTime(currFeeDefInfo.getString("APRV_TIME"));
			tbFeeDefLog.setCreditUnit(currFeeDefInfo.getString("CREDIT_UNIT"));
			tbFeeDefLog.setDebitUnit(currFeeDefInfo.getString("DEBIT_UNIT"));
			tbFeeDefLog.setAccountCode(currFeeDefInfo.getString("ACCOUNT_CODE"));
			tbFeeDefLog.setUptUserid(currFeeDefInfo.getString("UPT_USERID"));
			tbFeeDefLog.setAprvUserid(currFeeDefInfo.getString("APRV_USERID"));
			tbFeeDefLog.setUptStatus(currFeeDefInfo.getString("UPT_STATUS"));
			tbFeeDefLog.setModifyType(MODIFY_TYPE);
			tbFeeDefLog.setModifyDesc(MODIFY_DESC);
			tbFeeDefLog.setSysDate(sysDate);
			tbFeeDefLog.setSysTime(sysTime);
			new TbFeeDefLogMgr(connection).insert(tbFeeDefLog);
		}

	}
	public static ProcFeeDefModifyLog getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ProcFeeDefModifyLog instance = (ProcFeeDefModifyLog) apContext.getBean("ProcFeeDefModifyLog");
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
		ProcFeeDefModifyLog procCustModifyLog = null;
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
            	procCustModifyLog = getInstance();
            }
            else {
            	procCustModifyLog = new ProcFeeDefModifyLog();
            }
            
            procCustModifyLog.setBatchDate(batchDate);
            procCustModifyLog.setRecoverLevel(System.getProperty("recover"));
            procCustModifyLog.run(args);
        }
        catch (Exception ignore) {
            log.warn("ProcSettleConfigLog run fail:" + ignore.getMessage(), ignore);
        }
    }
	
	protected void recoverData() throws Exception
    {
        Connection connSelf =  DBService.getDBService().getConnection("batch");
        
        StringBuffer sql = new StringBuffer();
        
        //Delete TB_CUST_MODIFY_LOG
        sql.append("DELETE TB_FEE_DEF_LOG ");
        sql.append("WHERE CURR_MODIFY_DATE='").append(BatchUtil.getSomeDay(batchDate,-1)).append("'");
        try
         {
             log.info(" recoverData():"+sql.toString());
             DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
             connSelf.commit();
         }
        catch (SQLException e)
         {
             connSelf.rollback();
             throw new Exception("recoverData():delete . "+e);
         }   
    }
}
