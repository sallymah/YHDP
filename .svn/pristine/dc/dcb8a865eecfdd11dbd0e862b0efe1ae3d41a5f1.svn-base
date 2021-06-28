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
import tw.com.hyweb.service.db.info.TbFeeConfigLogInfo;
import tw.com.hyweb.service.db.info.TbFeeConfigLogPK;
import tw.com.hyweb.service.db.info.TbSettleConfigLogInfo;
import tw.com.hyweb.service.db.info.TbSettleConfigLogPK;
import tw.com.hyweb.service.db.mgr.TbCustModifyLogMgr;
import tw.com.hyweb.service.db.mgr.TbFeeConfigLogMgr;
import tw.com.hyweb.service.db.mgr.TbSettleConfigLogMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ProcFeeConfigModifyLog extends AbstractBatchBasic{

	private static Logger log = Logger.getLogger(ProcFeeConfigModifyLog.class);
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
	            "impdata" + File.separator + "procFeeConfigModifyLog" + File.separator + "spring.xml";

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
		
        Statement currFeeConfigInfoStmt = null;
        ResultSet currFeeConfigInfo = null;

        String currFeeConfigInfoSql = "SELECT ISS_MEM_ID,ACQ_MEM_ID,FEE_CONFIG_ID,FEE_CONFIG_DESC,VALID_SDATE,"+
        				"VALID_EDATE,FIXED_FEE,ALLOW_DEDUCT,CAL_BASE,PROC_CYCLE,FEE_CODE,CAL_RULE_ID,FUND_TYPE," +
        				"UPT_STATUS, UPT_USERID, APRV_USERID, APRV_DATE, APRV_TIME "+
        				"FROM TB_FEE_CONFIG_UPT " +
        				"WHERE APRV_DATE = '"+BatchUtil.getSomeDay(batchDate,-1)+"' " +
        				"AND APRV_STATUS ='1' "+
        				"ORDER BY ISS_MEM_ID,ACQ_MEM_ID,FEE_CONFIG_ID,FEE_CODE,VALID_SDATE,APRV_DATE,APRV_TIME ";
        try {
        	currFeeConfigInfoStmt = conn.createStatement();
            log.debug("currFeeConfigInfoSql: "+currFeeConfigInfoSql);
            currFeeConfigInfo = currFeeConfigInfoStmt.executeQuery(currFeeConfigInfoSql.toString());
            while (currFeeConfigInfo.next()) {
            	uptStatus = currFeeConfigInfo.getString(14);
            	
            	if(uptStatus.equals("2")){
            		List diffFileNameLists = new ArrayList();
                	
                	StringBuffer prevFeeConfigInfoSql = new StringBuffer();
                	prevFeeConfigInfoSql.append("SELECT FEE_CONFIG_DESC,VALID_EDATE, FIXED_FEE,ALLOW_DEDUCT,PROC_CYCLE,CAL_RULE_ID,FUND_TYPE,CAL_BASE,FUND_TYPE ");
                	prevFeeConfigInfoSql.append("FROM(SELECT FEE_CONFIG_DESC,VALID_EDATE, FIXED_FEE,ALLOW_DEDUCT,PROC_CYCLE,CAL_RULE_ID,FUND_TYPE,CAL_BASE FROM TB_FEE_CONFIG_UPT ");
                	prevFeeConfigInfoSql.append("WHERE FEE_CONFIG_ID='"+currFeeConfigInfo.getString("FEE_CONFIG_ID")+"' ");
                	prevFeeConfigInfoSql.append("AND ACQ_MEM_ID = '"+currFeeConfigInfo.getString("ACQ_MEM_ID")+"' ");
                	prevFeeConfigInfoSql.append("AND ISS_MEM_ID ='"+currFeeConfigInfo.getString("ISS_MEM_ID")+"' ");
                	prevFeeConfigInfoSql.append("AND VALID_SDATE ='"+currFeeConfigInfo.getString("VALID_SDATE")+"' AND APRV_STATUS='1' " );
                	prevFeeConfigInfoSql.append("AND APRV_DATE||APRV_TIME < "+currFeeConfigInfo.getString("APRV_DATE")+"||"+currFeeConfigInfo.getString("APRV_TIME"));
                	prevFeeConfigInfoSql.append(" ORDER BY APRV_DATE DESC, APRV_TIME DESC) ");
                	prevFeeConfigInfoSql.append("WHERE ROWNUM=1 ");
                	log.debug("prevFeeConfigInfoSql: "+prevFeeConfigInfoSql.toString());
                	
                	Statement prevFeeConfigInfoStmt = null;
                    ResultSet prevFeeConfigInfo = null;                
                    prevFeeConfigInfoStmt = conn.createStatement();             
                    prevFeeConfigInfo = prevFeeConfigInfoStmt.executeQuery(prevFeeConfigInfoSql.toString());                
                    if (prevFeeConfigInfo.next()) {
                    	
                    	String fileName = "";
                    	
                    	for( int i = 0; i < fileNameLists.size(); i++ ){
                    		
                    		fileName = fileNameLists.get(i).toString();
                    		currData = currFeeConfigInfo.getString(fileName)==null?"" :currFeeConfigInfo.getString(fileName);
                    		prevData = prevFeeConfigInfo.getString(fileName)==null?"" :prevFeeConfigInfo.getString(fileName);

                    		if(!currData.equals(prevData)){
                        		diffFileNameLists.add(fileName);
                        	}
                    	}
                    	insertModifyLogs(currFeeConfigInfo, prevFeeConfigInfo, diffFileNameLists, conn);
                    }
                    ReleaseResource.releaseDB(null, prevFeeConfigInfoStmt, prevFeeConfigInfo);
            	}
            	else{
            		insertFeeConfigModifyLogs(currFeeConfigInfo, conn);
            	}
            	
            }
            conn.commit();
        }
        catch(Throwable e){
        	conn.rollback();
        	throw new RuntimeException(e);  
        }
        finally {        	  
        	ReleaseResource.releaseDB(conn, currFeeConfigInfoStmt, currFeeConfigInfo);
        }     
	
	}
	public void insertFeeConfigModifyLogs(ResultSet currFeeConfigInfo,
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

		TbFeeConfigLogPK pk = new TbFeeConfigLogPK();
		pk.setFeeConfigId(currFeeConfigInfo.getString("FEE_CONFIG_ID"));
		pk.setAcqMemId(currFeeConfigInfo.getString("ACQ_MEM_ID"));
		pk.setIssMemId(currFeeConfigInfo.getString("ISS_MEM_ID"));
		pk.setFeeCode(currFeeConfigInfo.getString("FEE_CODE"));
		pk.setValidSdate(currFeeConfigInfo.getString("VALID_SDATE"));
		pk.setAprvDate(currFeeConfigInfo.getString("APRV_DATE"));
		pk.setAprvTime(currFeeConfigInfo.getString("APRV_TIME"));
		pk.setModifyDesc(MODIFY_TYPE);

		TbFeeConfigLogInfo tbFeeConfigLogInfo = new TbFeeConfigLogMgr(
				connection).querySingle(pk);

		if (tbFeeConfigLogInfo == null) {
			TbFeeConfigLogInfo tbFeeConfigLog = new TbFeeConfigLogInfo();
			tbFeeConfigLog.setFeeConfigId(currFeeConfigInfo.getString("FEE_CONFIG_ID"));
			tbFeeConfigLog.setFeeConfigDesc(currFeeConfigInfo.getString("FEE_CONFIG_DESC"));
			tbFeeConfigLog.setFeeCode(currFeeConfigInfo.getString("FEE_CODE"));
			tbFeeConfigLog.setAcqMemId(currFeeConfigInfo.getString("ACQ_MEM_ID"));
			tbFeeConfigLog.setIssMemId(currFeeConfigInfo.getString("ISS_MEM_ID"));
			tbFeeConfigLog.setAllowDeduct(currFeeConfigInfo.getString("ALLOW_DEDUCT"));
			tbFeeConfigLog.setFixedFee(Double.valueOf(currFeeConfigInfo.getString("FIXED_FEE")));
			tbFeeConfigLog.setCalBase(currFeeConfigInfo.getString("CAL_BASE"));
			tbFeeConfigLog.setCalRuleId(currFeeConfigInfo.getString("CAL_RULE_ID"));
			tbFeeConfigLog.setProcCycle(currFeeConfigInfo.getString("PROC_CYCLE"));
			tbFeeConfigLog.setValidSdate(currFeeConfigInfo.getString("VALID_SDATE"));
			tbFeeConfigLog.setValidEdate(currFeeConfigInfo.getString("VALID_EDATE"));
			tbFeeConfigLog.setAprvDate(currFeeConfigInfo.getString("APRV_DATE"));
			tbFeeConfigLog.setAprvTime(currFeeConfigInfo.getString("APRV_TIME"));
			tbFeeConfigLog.setFundType(currFeeConfigInfo.getString("FUND_TYPE"));
			tbFeeConfigLog.setUptUserid(currFeeConfigInfo.getString("UPT_USERID"));
			tbFeeConfigLog.setAprvUserid(currFeeConfigInfo.getString("APRV_USERID"));
			tbFeeConfigLog.setUptStatus(currFeeConfigInfo.getString("UPT_STATUS"));
			tbFeeConfigLog.setModifyType(MODIFY_TYPE);
			tbFeeConfigLog.setModifyDesc(MODIFY_DESC);
			tbFeeConfigLog.setSysDate(sysDate);
			tbFeeConfigLog.setSysTime(sysTime);
			new TbFeeConfigLogMgr(connection).insert(tbFeeConfigLog);
		}
	}
	public void insertModifyLogs (ResultSet currFeeConfigInfo, ResultSet prevFeeConfigInfo, 
			List diffFileNameLists, Connection connection) throws SQLException{
		
		if ( diffFileNameLists.size() > 0 ){
			String dateTime = DateUtil.getTodayString();
	        String sysDate = dateTime.substring(0, 8);
	        String sysTime = dateTime.substring(8, 14);
	       
			for ( int i = 0; i < diffFileNameLists.size(); i++ ){
				
				MODIFY_TYPE=diffFileNameLists.get(i).toString();
				MODIFY_DESC=prevFeeConfigInfo.getString(diffFileNameLists.get(i).toString());

				TbFeeConfigLogPK pk = new TbFeeConfigLogPK();
				pk.setFeeConfigId(currFeeConfigInfo.getString("FEE_CONFIG_ID"));
				pk.setAcqMemId(currFeeConfigInfo.getString("ACQ_MEM_ID"));
				pk.setIssMemId(currFeeConfigInfo.getString("ISS_MEM_ID"));
				pk.setFeeCode(currFeeConfigInfo.getString("FEE_CODE"));
				pk.setValidSdate(currFeeConfigInfo.getString("VALID_SDATE"));
				pk.setAprvDate(currFeeConfigInfo.getString("APRV_DATE"));
				pk.setAprvTime(currFeeConfigInfo.getString("APRV_TIME"));
				pk.setModifyDesc(MODIFY_TYPE);
		    	
				TbFeeConfigLogInfo tbFeeConfigLogInfo = new TbFeeConfigLogMgr(connection).querySingle(pk);
		        
		        if (tbFeeConfigLogInfo == null){
		        	TbFeeConfigLogInfo tbFeeConfigLog = new  TbFeeConfigLogInfo();
		        	tbFeeConfigLog.setFeeConfigId(currFeeConfigInfo.getString("FEE_CONFIG_ID"));
		        	tbFeeConfigLog.setFeeConfigDesc(currFeeConfigInfo.getString("FEE_CONFIG_DESC"));
		        	tbFeeConfigLog.setFeeCode(currFeeConfigInfo.getString("FEE_CODE"));
		        	tbFeeConfigLog.setAcqMemId(currFeeConfigInfo.getString("ACQ_MEM_ID"));
		        	tbFeeConfigLog.setIssMemId(currFeeConfigInfo.getString("ISS_MEM_ID"));
		        	tbFeeConfigLog.setAllowDeduct(currFeeConfigInfo.getString("ALLOW_DEDUCT"));
		        	tbFeeConfigLog.setFixedFee(Double.valueOf(currFeeConfigInfo.getString("FIXED_FEE")));
		        	tbFeeConfigLog.setCalBase(currFeeConfigInfo.getString("CAL_BASE"));
		        	tbFeeConfigLog.setCalRuleId(currFeeConfigInfo.getString("CAL_RULE_ID"));
		        	tbFeeConfigLog.setProcCycle(currFeeConfigInfo.getString("PROC_CYCLE"));
		        	tbFeeConfigLog.setValidSdate(currFeeConfigInfo.getString("VALID_SDATE"));
		        	tbFeeConfigLog.setValidEdate(currFeeConfigInfo.getString("VALID_EDATE"));
		        	tbFeeConfigLog.setAprvDate(currFeeConfigInfo.getString("APRV_DATE"));
		        	tbFeeConfigLog.setAprvTime(currFeeConfigInfo.getString("APRV_TIME"));	
		        	tbFeeConfigLog.setFundType(currFeeConfigInfo.getString("FUND_TYPE"));
		        	tbFeeConfigLog.setUptUserid(currFeeConfigInfo.getString("UPT_USERID"));
		        	tbFeeConfigLog.setAprvUserid(currFeeConfigInfo.getString("APRV_USERID"));
		        	tbFeeConfigLog.setUptStatus(currFeeConfigInfo.getString("UPT_STATUS"));
		        	tbFeeConfigLog.setModifyType(MODIFY_TYPE);
		        	tbFeeConfigLog.setModifyDesc(MODIFY_DESC);
		        	tbFeeConfigLog.setSysDate(sysDate);
		        	tbFeeConfigLog.setSysTime(sysTime);
		        	new TbFeeConfigLogMgr(connection).insert(tbFeeConfigLog);
		        }
			}
		}
	}
	public static ProcFeeConfigModifyLog getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ProcFeeConfigModifyLog instance = (ProcFeeConfigModifyLog) apContext.getBean("ProcFeeConfigModifyLog");
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
		ProcFeeConfigModifyLog procFeeConfigModifyLog = null;
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
            	procFeeConfigModifyLog = getInstance();
            }
            else {
            	procFeeConfigModifyLog = new ProcFeeConfigModifyLog();
            }
            
            procFeeConfigModifyLog.setBatchDate(batchDate);
            procFeeConfigModifyLog.setRecoverLevel(System.getProperty("recover"));
            procFeeConfigModifyLog.run(args);
        }
        catch (Exception ignore) {
            log.warn("ProcFeeConfigModifyLog run fail:" + ignore.getMessage(), ignore);
        }
    }
	
	protected void recoverData() throws Exception
    {
        Connection connSelf =  DBService.getDBService().getConnection("batch");
        
        StringBuffer sql = new StringBuffer();
        
        //Delete TB_FEE_CONFIG_LOG
        sql.append("DELETE TB_FEE_CONFIG_LOG ");
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
