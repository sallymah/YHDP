/**
 * change log
 * -----------------------
 * 20070604(Commit)
 * 1.修改remarkSuccess()讓Exception的處理更清楚
 * 2.修改getBlacklistRegion() select REG_DATE的Exception處理
 * 3.新增setUnprocess()update TB_BLACKLIST_SETTING SQLExceptiont處理
 * 4.修改所有的Exception
 * -----------------------
 * 20070603(Commit)
 * 1.DBService.getDBService().sqlAction()增加false參數,才能讓user自己設定rollback
 * -----------------------
 * 20070528(Commit)
 * 1.新增process(), ReleaseResource.releaseDB(conn)的使用條件
 * 2.取消原本在process()內的log.info("end process");
 * 3.將setUnprocess()內的transInfo變數改為transInfos
 * 4.在getAcqMember()修改變數l_acqMemIdInfo->l_acqMemIdInfos
 * 5.修改global var acqMemIdInfo->acqMemIdInfos
 * 6.將原本黑名單版本若大於999999則設定rcode=2951,現在改為rcode=2999,表嚴重錯誤等級
 * -----------------------
 * 20070524
 * 1.新增了變數註解
 * -----------------------
 * 20070523
 * 1.在override process()時,因未把Exception往上丟, 以至於當發生Exception時,
 *   TB_BATCH_RESULT.WORK_FLAG未能被UPDATE為9, 因此做修改. 將exception往上丟給run()處理
 * 2.若遇Exception,除了Rcode=2951黑名單版本編號超過限制,其餘都用Rcode=2590黑名單失敗
 * 3.修改程式中log level   
 * -----------------------
 */
package tw.com.hyweb.svc.yhdp.batch.ProcUpdateTmpTrans;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * 依據設定產生所需的黑名單版本 Date:2007/03/28<br/>
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
 * @author Jim Yu
 * initVersion()
 */
public class ProcUpdateTmpTrans extends AbstractBatchBasic{
	
	private static final Logger logger = Logger.getLogger(ProcUpdateTmpTrans.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator + "ProcUpdateTmpTrans" + File.separator + "spring.xml";
	/**
	 * Global Connection, 程式全程都用這個Connection
	 */	
	private Connection conn = null;			
	
	private String batchDate = null;		//批次處理時間
	
	private String sysDate = null;			//存系統日期
	
	private String sysTime = null;			//存系統時間
	
	private int sleepTime = 0;				//由spring設定, commit之後sleep時間	
	
	private String filenames = "";

    protected List filenameBeans = new ArrayList();
    
    
	public List getFilenameBeans() {
		return filenameBeans;
	}
	public void setFilenameBeans(List filenameBeans) {
		this.filenameBeans = filenameBeans;
	}
	/**
	 * Main function<br/>
	 * @param args String[]
	 */
	public static void main(String[] args){
		
		ProcUpdateTmpTrans instance = getInstance();
		
		instance.setBatchDate(System.getProperty("date"));
	
		instance.run(null);
		
		System.exit(1);
	}
	
	/**
	 * get a ProcBlacklist instance by spring <br/>
	 * @return instance
	 */
	public static ProcUpdateTmpTrans getInstance(){

		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ProcUpdateTmpTrans instance = (ProcUpdateTmpTrans) apContext.getBean("ProcUpdateTmpTrans");
		return instance;
	
	}
	
	/**
	 * Blacklist process
	 * param argv
	 */
	public void process(String[] argv) throws Exception{
		try {

			init();
			action();
		
		}
		finally{
			if (conn != null){			
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
    		
    		for(int i = 0; i < filenameBeans.size(); i++){
    			filenames = filenames + StringUtil.toSqlValueWithSQuote(filenameBeans.get(i).toString());
    			if ( i < filenameBeans.size()-1 ){
    				filenames = filenames + ", ";
    			}
    		}
		    updateprocess();				
		//commit function
    	}
    	catch(SQLException e){
    		remarkFail();			//rollback function
            throw new Exception("action() SQL execute failed. "+e);
            
    	}   	
    }
    
    /**
     * 處理online已經lock卡片的資料
     * @throws Exception 
     *
     */
    private void updateprocess() throws Exception{

    	StringBuffer sqlCmd = new StringBuffer();

		    sqlCmd.append("UPDATE TB_TMP_TRANS SET REGEN_STATUS = '2'");
		    sqlCmd.append(",REGEN_SOURCE_DATE= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
		    sqlCmd.append(",APRV_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
		    sqlCmd.append(",UPT_SRC = ").append(StringUtil.toSqlValueWithSQuote("B"));//異動來源註記
		    sqlCmd.append(" WHERE RCODE = '1007' AND  REGEN_STATUS ='0'"); 
		    sqlCmd.append(" AND IMP_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
		    sqlCmd.append(" AND FILE_NAME IN (").append(filenames).append(")");
		    sqlCmd.append(" AND ACQ_MEM_ID IN (SELECT MEM_ID FROM TB_MEMBER WHERE IGNORE_MAC_INVALID ='1' )");
		    try{	//20070604新增SQLExceptiont處理
		    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);	
		    }
		    catch (SQLException e){
		    	throw new SQLException("updateprocess() update TB_TMP_TRANS failed." + e);
		    }
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
