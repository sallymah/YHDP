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
import tw.com.hyweb.service.db.mgr.TbCustModifyLogMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ProcCustModifyLog extends AbstractBatchBasic{

	private static Logger log = Logger.getLogger(ProcCustModifyLog.class);
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
	            "impdata" + File.separator + "procCustModifyLog" + File.separator + "spring.xml";

	// 要比對的欄位
    private List fileNameLists = null;
    private String batchDate ="";
    private String RecoverLevel = "";

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
		
        Statement currCustInfoStmt = null;
        ResultSet currCustInfo = null;

        String currCustInfoSql = "SELECT * FROM TB_CUST_UPT " +
        				"WHERE APRV_DATE = '"+BatchUtil.getSomeDay(batchDate,-1)+"' " +
        				"AND APRV_STATUS = '1' " +
        				"AND UPT_STATUS = '2' " +
        				"AND (UPT_USERID <> 'WS' " +
        				"OR (UPT_USERID = 'WS' AND MODIFY_SOURCE = 'CRM'))";
        try {
        	currCustInfoStmt = conn.createStatement();
            log.debug("currCustInfoSql: "+currCustInfoSql);
            currCustInfo = currCustInfoStmt.executeQuery(currCustInfoSql.toString());
           
            while (currCustInfo.next()) {
            	List diffFileNameLists = new ArrayList();
            	
            	StringBuffer prevCustInfoSql = new StringBuffer();
            	prevCustInfoSql.append("SELECT * FROM ( SELECT * FROM TB_CUST_UPT ");
            	prevCustInfoSql.append("WHERE CUST_ID='"+currCustInfo.getString("CUST_ID")+"' AND APRV_STATUS='1' ");
            	prevCustInfoSql.append("AND UPT_STATUS IN ('1','2') ");
            	prevCustInfoSql.append("AND APRV_DATE||APRV_TIME < "+currCustInfo.getString("APRV_DATE")+"||"+currCustInfo.getString("APRV_TIME"));
            	prevCustInfoSql.append(" ORDER BY APRV_DATE DESC, APRV_TIME DESC) ");
            	prevCustInfoSql.append("WHERE ROWNUM=1 ");
            	//log.debug("prevCustInfoSql: "+prevCustInfoSql.toString());
            	
            	Statement prevCustInfoStmt = null;
                ResultSet prevCustInfo = null;                
                prevCustInfoStmt = conn.createStatement();             
                prevCustInfo = prevCustInfoStmt.executeQuery(prevCustInfoSql.toString());                
                if (prevCustInfo.next()) {
                	String currData = "";
                	String prevDate = "";
                	String fileName = "";
                	
                	for( int i = 0; i < fileNameLists.size(); i++ ){
                		
                		fileName = fileNameLists.get(i).toString();
                		
                		currData = currCustInfo.getString(fileName)==null?"" :currCustInfo.getString(fileName);
                		prevDate = prevCustInfo.getString(fileName)==null?"" :prevCustInfo.getString(fileName);
                		
                    	if(!currData.equals(prevDate)){
                    		diffFileNameLists.add(fileName);
                    	}
                	}
                	insertModifyLogs(currCustInfo, prevCustInfo, diffFileNameLists, conn);
                }
                ReleaseResource.releaseDB(null, prevCustInfoStmt, prevCustInfo);
            }
            conn.commit();
        }
        catch(Throwable e){
        	conn.rollback();
        	throw new RuntimeException(e);  
        }
        finally {        	  
        	ReleaseResource.releaseDB(conn, currCustInfoStmt, currCustInfo);
        }     
	
	}
	
	public void insertModifyLogs (ResultSet currCustInfo, ResultSet prevCustInfo, 
			List diffFileNameLists, Connection connection) throws SQLException{
		
		if ( diffFileNameLists.size() > 0 ){
			String dateTime = DateUtil.getTodayString();
	        String sysDate = dateTime.substring(0, 8);
	        String sysTime = dateTime.substring(8, 14);
	        
			for ( int i = 0; i < diffFileNameLists.size(); i++ ){
				
				TbCustModifyLogPK pk = new TbCustModifyLogPK();
				pk.setCustId(currCustInfo.getString("CUST_ID"));
				pk.setFieldName(diffFileNameLists.get(i).toString());
				pk.setCurrModifyDate(currCustInfo.getString("APRV_DATE"));
				pk.setCurrModifyTime(currCustInfo.getString("APRV_TIME"));
		    	
				TbCustModifyLogInfo tbCustModifyLogInfo = new TbCustModifyLogMgr(connection).querySingle(pk);
		        
		        if (tbCustModifyLogInfo == null){
		        	TbCustModifyLogInfo tbCustModifyLog = new  TbCustModifyLogInfo();
		        	tbCustModifyLog.setModifySource(currCustInfo.getString("MODIFY_SOURCE"));
		        	tbCustModifyLog.setCustId(currCustInfo.getString("CUST_ID"));
		        	tbCustModifyLog.setPrevModifyDate(prevCustInfo.getString("APRV_DATE"));
		        	tbCustModifyLog.setPrevModifyTime(prevCustInfo.getString("APRV_TIME"));
		        	tbCustModifyLog.setFieldName(diffFileNameLists.get(i).toString());
		        	tbCustModifyLog.setPrevData(prevCustInfo.getString(diffFileNameLists.get(i).toString()));
		        	tbCustModifyLog.setCurrData(currCustInfo.getString(diffFileNameLists.get(i).toString()));
		        	tbCustModifyLog.setCurrModifyDate(currCustInfo.getString("APRV_DATE"));
		        	tbCustModifyLog.setCurrModifyTime(currCustInfo.getString("APRV_TIME"));
		        	tbCustModifyLog.setModifyUserid(currCustInfo.getString("UPT_USERID"));
		        	if(StringUtil.isEmpty(currCustInfo.getString("APRV_USER_NAME"))){
		        		tbCustModifyLog.setAprvUserName(currCustInfo.getString("UPT_USERID"));
		        	}else{
		        		tbCustModifyLog.setAprvUserName(currCustInfo.getString("APRV_USER_NAME"));
		        	}
		        	tbCustModifyLog.setModifyDesc(currCustInfo.getString("MODIFY_DESC"));
		        	tbCustModifyLog.setSysDate(sysDate);
		        	tbCustModifyLog.setSysTime(sysTime);
		        	new TbCustModifyLogMgr(connection).insert(tbCustModifyLog);
		        }
			}
		}
	}
	public static ProcCustModifyLog getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ProcCustModifyLog instance = (ProcCustModifyLog) apContext.getBean("ProcCustModifyLog");
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
		ProcCustModifyLog procCustModifyLog = null;
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
            	procCustModifyLog = new ProcCustModifyLog();
            }
            
            procCustModifyLog.setBatchDate(batchDate);
            procCustModifyLog.setRecoverLevel(System.getProperty("recover"));
            procCustModifyLog.run(args);
        }
        catch (Exception ignore) {
            log.warn("ProcCustModifyLog run fail:" + ignore.getMessage(), ignore);
        }
    }
	
	protected void recoverData() throws Exception
    {
        Connection connSelf =  DBService.getDBService().getConnection("batch");
        
        StringBuffer sql = new StringBuffer();
        
        //Delete TB_CUST_MODIFY_LOG
        sql.append("DELETE TB_CUST_MODIFY_LOG ");
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
             throw new Exception("recoverData():delete TB_MERCH_SUM. "+e);
         }   
    }
}
