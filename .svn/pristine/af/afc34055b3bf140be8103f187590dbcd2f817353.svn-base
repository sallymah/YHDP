/**
 * For YHDP_BANK
 * 每日代行授權最高次數更新
 * -----------------------
 */
package tw.com.hyweb.svc.yhdp.batch.ProcNumOfAuth;

import java.io.File;
import java.text.*;
import java.util.Calendar;
import java.util.Vector;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * 
 * 所有SQL,全程使用同一個Connection<br/>
 * 成功->{remarkSuccess},全部一起commit<br/>
 * 失敗->{remarkFail},全部一起rollback <br/>
 * usage:<br/>
 *  ant -f buildbatch.xml runProcBlacklist -Ddate=""<br/>
 *  batchDate={-Ddate請輸入YYYYMMDD;若不輸入,預設為系統日}
 * 
 * spring:<br/>
 *  
 * work flow:action()
 * initVersion()
 */
public class ProcNumOfAuth extends AbstractBatchBasic{
	
	private static final Logger logger = Logger.getLogger(ProcNumOfAuth.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator + "ProcNumOfAuth" + File.separator + "spring.xml";
	
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMdd");
	/**
	 * Global Connection, 程式全程都用這個Connection
	 */	
	private Connection conn = null;			
	
	private String batchDate = null;		//批次處理時間
	
	private String sysDate = null;			//存系統日期
	
	private String sysTime = null;			//存系統時間
	
	private int sleepTime = 0;				//由spring設定, commit之後sleep時間		
	
	private String executeDate = "15";


	/**
	 * Main function<br/>
	 * @param args String[]
	 */
	public static void main(String[] args){
		
		ProcNumOfAuth instance = getInstance();
		
		instance.setBatchDate(System.getProperty("date"));
	
		instance.run(null);
		
		
	}
	
	/**
	 * get a ProcBlacklist instance by spring <br/>
	 * @return instance
	 */
	public static ProcNumOfAuth getInstance(){
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ProcNumOfAuth instance = (ProcNumOfAuth) apContext.getBean("ProcNumOfAuth");
		return instance;
	}
	/**
	 * Blacklist process
	 * param argv
	 */
	public void process(String[] argv) throws Exception{
		try{
			init();
			action();
		}
		finally{
			if (conn != null){			//20070528新增
				ReleaseResource.releaseDB(conn);
			}
		}
		 
	}
	/**
     * 初始設定<br/>
     * 若不指定batchDate, 預設為系統日<br/>
     * get connection<br/>
     * @throws Exception
     */
    private void init() throws Exception{
    	try{
	    	BatchUtil.getNow();				//在這就會紀錄sysDay得時間,可以在等一下使用
	    	sysDate = BatchUtil.sysDay;		//設定系統日期
	    	sysTime = BatchUtil.sysTime;	//設定系統時間
	    	
	    	if (StringUtil.isEmpty(batchDate)){
	    		batchDate = BatchUtil.sysDay;
	    	} else if (!BatchUtil.checkChristianDate(batchDate)){
	    		String msg = "Invalid date for option -Ddate!";
	    		throw new Exception(msg);
	    	}
	    	
	    	logger.info("batchDate:" + getBatchDate());
	    	conn = BatchUtil.getConnection();
    	}
    	catch (Exception e)
        {
            throw new Exception("init():" + e);
        }
        logger.info("init() ok.\n");
    }
    
    /**
     * @throws Exception 
     * 
     */
    private void action() throws Exception{
    	try{
    		//每月15日更新設定
    		/*String date = batchDate.substring(6, 8);
    		if(!"15".equals(date)){
    			logger.warn("Not to execute, batchDate is not 15th!");
	    		return;
    		}*/
    		formatExecuteDate();
    		logger.info( "executeDate is :"+ executeDate + ". ");
    		
    		if (!isValidExecuteDate()){
    			logger.info( "batchDate is :" + batchDate + " Date is not 15th!");
    			return;
    		}
    		UpdateMember();						//Update TB_MEMBER代行授權

		    remarkSuccess();		//commit function
    	}
    	catch(SQLException e){
    		remarkFail();			//rollback function
            throw new Exception("action() SQL execute failed. "+e);
            
    	}
    	
    }
	private void formatExecuteDate() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateFormat.parse(batchDate));
		calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(executeDate));
		setExecuteDate(dateFormat.format(calendar.getTime()));
	}

	private boolean isValidExecuteDate() {
		if (getBatchDate().equals(getExecuteDate()))
			return true;
		else
			return false;
	}
    /**
     * 處理UpdateMember
     * @throws Exception 
     *
     */
    private void UpdateMember() throws Exception{
    	Vector memberInfos = new Vector();	//存放已被鎖卡交易	
    	StringBuffer sqlCmd = new StringBuffer();
    	
    	sqlCmd.append("select MEM_ID,NEXT_NUM_OF_AUTH");
    	sqlCmd.append(" from TB_MEMBER where");
    	sqlCmd.append(" NEXT_NUM_OF_AUTH is not null");

    	
    	memberInfos = BatchUtil.getInfoListHashMap(sqlCmd.toString());
    	logger.debug("Select all Info: " + memberInfos);
    	
    	HashMap ht = new HashMap();
    	//Fetach each memberInfos
	    for (int i=0; i<memberInfos.size(); i++){
	    	sqlCmd.delete(0, sqlCmd.length());
	    	ht = (HashMap)memberInfos.get(i);
		    sqlCmd.append("update TB_MEMBER set");
		    sqlCmd.append(" CURR_NUM_OF_AUTH='").append(ht.get("NEXT_NUM_OF_AUTH")).append("'");
		    sqlCmd.append(" ,NEXT_NUM_OF_AUTH=NULL");
		    sqlCmd.append(" where MEM_ID='").append(ht.get("MEM_ID")).append("'");
		  
		    
		    try{	
		    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);	
		    }
		    catch (SQLException e){
		    	throw new SQLException("setUnprocess() update TB_MEMBER failed." + e);
		    }
	    }
	    logger.info("setUnprocess():ok. \n");
    }
    
 
    /**
     * 執行SQLCOMMAND成功處理
     */
    private void remarkSuccess() throws Exception{
    	try{
    		conn.commit();
    		logger.info("commit success");
    		Thread.sleep(getSleepTime());
    	}
    	catch (SQLException e){
    		throw new SQLException("remarkSuccess():conn.commit failed." + e);
    	}
    	catch (InterruptedException e){
    		throw new Exception("remarkSuccess():Thread.sleep() failed." + e);
    	}
    }
    
    /**
     * 執行SQLCOMMAND失敗處理
     */
    private void remarkFail() throws SQLException{
    	try{	//20070604新增SQLException處理
    		conn.rollback();
    		logger.info("rollback!");
    	}
    	catch (SQLException e){
    		throw new SQLException("remarkFail() rollback failed." + e);
    	}
    }
    
   
	public String getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(String executeDate) {
		this.executeDate = executeDate;
	}
	
	public String getBatchDate() {
		return batchDate;
	}

	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}	 
}
