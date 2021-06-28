/**
 * change log
 * FOR YHDP_BANK
 * 拒授權名單移除
 * -----------------------
s */
package tw.com.hyweb.svc.yhdp.batch.ProcLockedCard;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBlacklistSettingInfo;
import tw.com.hyweb.service.db.info.TbBlacklistSettingUptInfo;
import tw.com.hyweb.service.db.info.TbCardRentInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbRejectAuthListInfo;
import tw.com.hyweb.service.db.info.TbRejectAuthListUptInfo;
import tw.com.hyweb.service.db.mgr.TbBlacklistSettingMgr;
import tw.com.hyweb.service.db.mgr.TbBlacklistSettingUptMgr;
import tw.com.hyweb.service.db.mgr.TbCardRentMgr;
import tw.com.hyweb.service.db.mgr.TbOnlTxnMgr;
import tw.com.hyweb.service.db.mgr.TbRejectAuthListMgr;
import tw.com.hyweb.service.db.mgr.TbRejectAuthListUptMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * 依據設定產生所需歸還黑名單 Date:2015/03/16<br/>
 * 所有SQL,全程使用同一個Connection<br/>
 * 成功->{remarkSuccess},全部一起commit<br/>
 * 失敗->{remarkFail},全部一起rollback <br/>
 * usage:<br/>
 *  ant -f buildbatch.xml runProcRecyclingCards -Ddate=""<br/>
 *  batchDate={-Ddate請輸入YYYYMMDD;若不輸入,預設為系統日}
 * 
 * spring:<br/>
 *  
 * work flow:action()
 * @author 
 * initVersion()
 */
public class ProcLockedCard extends AbstractBatchBasic{
	
	private static final Logger logger = Logger.getLogger(ProcLockedCard.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator + "ProcLockedCard" + File.separator + "spring.xml";
	
	/**
	 * Global Connection, 程式全程都用這個Connection
	 */	
	private Connection conn = null;			
	
	private String batchDate = null;		//批次處理時間
	
	private String sysDate = null;			//存系統日期
	
	private String sysTime = null;			//存系統時間
	
	private int sleepTime = 0;				//由spring設定, commit之後sleep時間	
	/**
	 * 存放所有資料from tb_onl_txn
	 */
	private Vector<TbOnlTxnInfo> cardInfos = null; 	
	private Vector<TbRejectAuthListInfo> rejectAuthList = null;
	/**
	 * Main function<br/>
	 * @param args String[]
	 */
	public static void main(String[] args){
		
		ProcLockedCard instance = getInstance();
		
		instance.setBatchDate(System.getProperty("date"));
	
		instance.run(null);
		
		
	}
	
	/**
	 * get a ProcLockedCard instance by spring <br/>
	 * @return instance
	 */
	public static ProcLockedCard getInstance(){
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ProcLockedCard instance = (ProcLockedCard) apContext.getBean("ProcLockedCard");
		return instance;
	}
	
	/**
	 * Blacklist process
	 * param argv
	 */
	public void process(String[] argv) throws Exception
	{
		try{
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
     * 取得拒授權名單	
     * 
     */
    private void action() throws Exception 
    {
    	try{
		    		
		    cardInfos = getTbOnlTxn();		
		    	
		    //Fetch each cardRentInfos    	
		    for (int idx=0; idx<cardInfos.size(); idx++)
		    {
		    	TbOnlTxnInfo tbOnlTxnInfo = cardInfos.get(idx);
		    	removeRejectAuth(tbOnlTxnInfo);
		    
		    }
		    
		    remarkSuccess();		//commit function
    	}
    	catch(SQLException e){
    		remarkFail();			//rollback function
            throw new Exception("action() SQL execute failed. "+e);
            
    	}	
    }
    
    private void removeRejectAuth(TbOnlTxnInfo info) throws Exception 
    {
    	rejectAuthList = getTbRejectAuthList(info);
    	if(rejectAuthList.size() == 0)
    		return;
    	for (int idx=0; idx<rejectAuthList.size(); idx++)
	    {
    		
	    	TbRejectAuthListInfo tbRejectAuthListInfo = rejectAuthList.get(idx);
	    	addRejectAuthListUpt(tbRejectAuthListInfo);
	    	removeRejectAuthList(tbRejectAuthListInfo);
	    
	    }
		
	}
	
	private Vector<TbOnlTxnInfo> getTbOnlTxn() throws SQLException 
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" P_CODE ='7517' AND　IMP_FILE_DATE='"+ batchDate +"'");

		
		String where = sb.toString();
		
		Vector<TbOnlTxnInfo> result = new Vector<TbOnlTxnInfo>();
		TbOnlTxnMgr mgr = new TbOnlTxnMgr(conn);
		mgr.queryMultiple(where, result);
		
		return result;
	}
   
	private Vector<TbRejectAuthListInfo> getTbRejectAuthList(TbOnlTxnInfo info) throws SQLException 
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" CARD_NO='").append(info.getCardNo()).append("'");

		
		String where = sb.toString();
		
		Vector<TbRejectAuthListInfo> result = new Vector<TbRejectAuthListInfo>();
		TbRejectAuthListMgr mgr = new TbRejectAuthListMgr(conn);
		mgr.queryMultiple(where, result);
		
		return result;
	}
    /**
     * insert TB_REJECT_AUTH_LIST_UPT
     * @param object 
     * @return
     * @throws SQLException
     */
    private void addRejectAuthListUpt(TbRejectAuthListInfo tbRejectAuthInfo) throws Exception 
    {
    	TbRejectAuthListUptInfo info = new TbRejectAuthListUptInfo();
    	info.setCardNo(tbRejectAuthInfo.getCardNo());
    	info.setExpiryDate(tbRejectAuthInfo.getExpiryDate());
    	info.setLmsInvoiceNo(tbRejectAuthInfo.getLmsInvoiceNo());
    	info.setRejectReason(tbRejectAuthInfo.getRejectReason());
    	info.setEndDate(tbRejectAuthInfo.getEndDate());
    	info.setImpFileName(tbRejectAuthInfo.getImpFileName());
    	info.setImpFileDate(tbRejectAuthInfo.getImpFileDate());
    	info.setImpFileTime(tbRejectAuthInfo.getImpFileTime());
    	info.setUptStatus("3");
    	info.setUptUserid(tbRejectAuthInfo.getUptUserid());
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime);
    	info.setAprvStatus("1");
    	info.setAprvUserid("BATCH");
    	info.setAprvDate(sysDate);
    	info.setAprvTime(sysTime);   	
    	
    	TbRejectAuthListUptMgr mgr = new TbRejectAuthListUptMgr(conn);
    	mgr.insert(info);
    	
    	
    }          
    private void removeRejectAuthList(TbRejectAuthListInfo tbRejectAuthInfo) throws Exception 
    {
    	
    	StringBuffer sqlCmd = new StringBuffer();
    	
    	sqlCmd.append("delete TB_REJECT_AUTH_LIST");
    	sqlCmd.append(" where CARD_NO=").append(tbRejectAuthInfo.getCardNo());    	
		    
		    try{	
		    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);	
		    }
		    catch (SQLException e){
		    	throw new SQLException("removeRejectAuthList() delete TB_REJECT_AUTH_LIST failed." + e);
		    }
	    }
   
	/**
     * 執行SQLCOMMAND成功處理
     */
    private void remarkSuccess() throws Exception
    {
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
    private void remarkFail() throws SQLException
    {
    	try{	
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
