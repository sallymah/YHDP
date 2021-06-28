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
package tw.com.hyweb.svc.yhdp.batch.ProcRecyclingCards;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
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
import tw.com.hyweb.service.db.mgr.TbBlacklistSettingMgr;
import tw.com.hyweb.service.db.mgr.TbBlacklistSettingUptMgr;
import tw.com.hyweb.service.db.mgr.TbCardRentMgr;
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
 * @author Kevin Yang
 * initVersion()
 */
public class ProcRecyclingCards extends AbstractBatchBasic{
	
	private static final Logger logger = Logger.getLogger(ProcRecyclingCards.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator + "ProcRecyclingCards" + File.separator + "spring.xml";
	
	private static final String DEFAULT_DATE_FORMAT = "00000000";
	
	private static final String RETURN_FLAG_NONRECOVERABLE = "2";
	
	/**
	 * Global Connection, 程式全程都用這個Connection
	 */	
	private Connection conn = null;			
	
	private String batchDate = null;		//批次處理時間
	
	private String sysDate = null;			//存系統日期
	
	private String sysTime = null;			//存系統時間
	
	private int sleepTime = 0;				//由spring設定, commit之後sleep時間	
	/**
	 * 存放所有的卡片借用資訊
	 */
	private Vector<TbCardRentInfo> cardRentInfos = null; 	
	
	/**
	 * Main function<br/>
	 * @param args String[]
	 */
	public static void main(String[] args){
		
		ProcRecyclingCards instance = getInstance();
		
		instance.setBatchDate(System.getProperty("date"));
	
		instance.run(null);
		
		System.exit(1);
	}
	
	/**
	 * get a ProcBlacklist instance by spring <br/>
	 * @return instance
	 */
	public static ProcRecyclingCards getInstance(){
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ProcRecyclingCards instance = (ProcRecyclingCards) apContext.getBean("ProcRecyclingCards");
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
     * 
     */
    private void action() throws Exception 
    {
    	try{
		    setUnprocess();						//處理已經被 online block card的卡片
		    cardRentInfos = getCardRent();		//取已經到期的卡片
		    	
		    //Fetch each cardRentInfos    	
		    for (int idx=0; idx<cardRentInfos.size(); idx++)
		    {
		    	TbCardRentInfo cardRentInfo = cardRentInfos.get(idx);
		    	boolean isExistBlist = true;
		    	int bCount = getBlacklistCount(cardRentInfo);
		    	if(bCount == 0) {
		    		addBlackList(cardRentInfo);
		    		isExistBlist = false;
		    	}
		    	remarkCardRent(cardRentInfo, isExistBlist);
		    	updateCard(cardRentInfo.getCardNo());
		    }
		    
		    remarkSuccess();		//commit function
    	}
    	catch(SQLException e){
    		remarkFail();			//rollback function
            throw new Exception("action() SQL execute failed. "+e);
            
    	}	
    }
    
    private int getBlacklistCount(TbCardRentInfo info) throws SQLException 
    {
		TbBlacklistSettingMgr mgr = new TbBlacklistSettingMgr(conn);
		StringBuffer sb = new StringBuffer();
		
		sb.append("CARD_NO='").append(info.getCardNo()).append("'");
		sb.append(" AND EXPIRY_DATE='").append(info.getExpiryDate()).append("'");
		
		String where = sb.toString();
		int count = mgr.getCount(where);
		return count;
	}

	private void remarkCardRent(TbCardRentInfo tbCardRentInfo, boolean flag) throws SQLException 
    {
    	StringBuffer sqlCmd = new StringBuffer();
    	sqlCmd.append("UPDATE TB_CARD_RENT");
    	sqlCmd.append(" SET PROC_DATE='" + sysDate + "'");
    	sqlCmd.append(", RETURN_FLAG='1'");
    	sqlCmd.append(" WHERE PROC_DATE='" + DEFAULT_DATE_FORMAT + "'");
    	if(!flag) {
    		sqlCmd.append(" AND (END_DATE<='" + batchDate + "'");
        	sqlCmd.append(" OR END_RENT_DATE<='" + batchDate + "')");
    	}

    	try{	
	    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);
	    }
	    catch (SQLException e){
	    	throw new SQLException("updateCardRent() update TB_CARD_RENT failed." + e);
	    }
    	logger.info("updateCardRent():ok. \n");
	}

	private void updateCard(String cardNo) throws SQLException 
    {
		StringBuffer sqlCmd = new StringBuffer();
    	sqlCmd.append("UPDATE TB_CARD ");
    	sqlCmd.append("SET ");
    	sqlCmd.append("PREVIOUS_STATUS = STATUS, ");
    	sqlCmd.append("STATUS = '9', ");
    	sqlCmd.append("PREVIOUS_LIFE_CYCLE = LIFE_CYCLE, ");
    	sqlCmd.append("LIFE_CYCLE = '8' ");
    	sqlCmd.append("WHERE CARD_NO = '" + cardNo + "' ");

    	try {	
	    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);
	    }
	    catch (SQLException e) {
	    	throw new SQLException("UpdateCard() update TB_CARD failed." + e);
	    }
    	logger.info("UpdateCard():ok. \n");
    }
	
	private Vector<TbCardRentInfo> getCardRent() throws SQLException 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("PROC_DATE='").append(DEFAULT_DATE_FORMAT).append("'");
		sb.append(" AND (END_DATE<='").append(batchDate).append("'");
		sb.append(" OR END_RENT_DATE<='").append(batchDate).append("')");
		
		String where = sb.toString();
		
		Vector<TbCardRentInfo> result = new Vector<TbCardRentInfo>();
		TbCardRentMgr mgr = new TbCardRentMgr(conn);
		mgr.queryMultiple(where, result);
		
		return result;
	}

	/**
     * 處理已經被 online block card的卡片
     * @throws Exception 
     *
     */
    private void setUnprocess() throws Exception
    {
    	StringBuffer sqlCmd = new StringBuffer();
    	
    	sqlCmd.delete(0, sqlCmd.length());
	    sqlCmd.append("UPDATE TB_CARD_RENT SET");
	    sqlCmd.append(" PROC_DATE='").append(sysDate).append("'");
	    sqlCmd.append(" ,RETURN_FLAG='").append(RETURN_FLAG_NONRECOVERABLE).append("'");
	    sqlCmd.append(" WHERE PROC_DATE='").append(DEFAULT_DATE_FORMAT).append("'");
	    sqlCmd.append(" AND (END_DATE<='").append(batchDate).append("'");
	    sqlCmd.append(" OR END_RENT_DATE<='").append(batchDate).append("')");
	    sqlCmd.append(" AND EXISTS(SELECT 1 FROM TB_TRANS WHERE STATUS='1'");
	    sqlCmd.append(" AND P_CODE='").append(Constants.PCODE_7517).append("'");
	    sqlCmd.append(" AND TB_CARD_RENT.CARD_NO=TB_TRANS.CARD_NO)");
	    
	    try{	
	    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);		
	    }
	    catch (SQLException e){
	    	throw new SQLException("setUnprocess() update TB_BLACKLIST_SETTING failed." + e);
	    }
	    logger.info("setUnprocess():ok. \n");
    }
    
    /**
     * 產生黑名單
     * @param object 
     * @return
     * @throws SQLException
     */
    private void addBlackList(TbCardRentInfo cardRentInfo) throws Exception 
    {
    	TbBlacklistSettingInfo info = new TbBlacklistSettingInfo();
    	info.setCardNo(cardRentInfo.getCardNo());
    	info.setExpiryDate(cardRentInfo.getExpiryDate());
    	info.setRegDate(sysDate);
    	info.setRegTime(sysTime);
    	info.setRegUserid("BATCH");
    	info.setRegReason("測試卡回收(加入黑名單)");
    	info.setStatus("1");
    	info.setBlacklistCode("AA");
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime);
    	info.setAprvUserid("BATCH");
    	info.setAprvDate(sysDate);
    	info.setAprvTime(sysTime);   	
    	
    	TbBlacklistSettingMgr mgr = new TbBlacklistSettingMgr(conn);
    	mgr.insert(info);
    	
    	//insert upt
    	TbBlacklistSettingUptInfo UptInfo = copyCardRentData(info);    	
    	TbBlacklistSettingUptMgr uptMgr = new TbBlacklistSettingUptMgr(conn);
    	uptMgr.insert(UptInfo);
    }
           
    private TbBlacklistSettingUptInfo copyCardRentData(TbBlacklistSettingInfo info) 
    {
    	TbBlacklistSettingUptInfo UptInfo = new TbBlacklistSettingUptInfo();
    	UptInfo.setAprvDate(info.getAprvDate());
    	UptInfo.setAprvStatus("1");
    	UptInfo.setAprvTime(info.getAprvTime());
    	UptInfo.setAprvUserid(info.getAprvUserid());
    	UptInfo.setBlacklistCode(info.getBlacklistCode());
    	UptInfo.setCardNo(info.getCardNo());
    	UptInfo.setExpiryDate(info.getExpiryDate());
    	UptInfo.setRegDate(info.getRegDate());
    	UptInfo.setRegReason(info.getRegReason());
    	UptInfo.setRegTime(info.getRegTime());
    	UptInfo.setRegUserid(info.getRegUserid());
    	UptInfo.setStatus(info.getStatus());
    	UptInfo.setUptDate(info.getUptDate());
    	UptInfo.setUptStatus("1");
    	UptInfo.setUptTime(info.getUptTime());
    	UptInfo.setUptUserid(info.getUptUserid());
    	
		return UptInfo;
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
