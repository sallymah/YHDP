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
package tw.com.hyweb.svc.yhdp.batch.ProcBlacklist;

import java.io.File;
import java.text.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.cxf.binding.corba.wsdl.Array;
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
public class ProcBlacklist extends AbstractBatchBasic{
	
	private static final Logger logger = Logger.getLogger(ProcBlacklist.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator + "ProcBlacklist" + File.separator + "spring.xml";
	/**
	 * Global Connection, 程式全程都用這個Connection
	 */	
	private Connection conn = null;			
	
	private String batchDate = null;		//批次處理時間
	
	private String sysDate = null;			//存系統日期
	
	private String sysTime = null;			//存系統時間
	
	private int sleepTime = 0;				//由spring設定, commit之後sleep時間	
	/**
	 * 存放所有的收單單位資訊(mem_id, blacklist_cnt)
	 */
	private Vector acqMemIdInfos = null; 	
	
	/**
	 * Main function<br/>
	 * @param args String[]
	 */
	public static void main(String[] args){
		
		ProcBlacklist instance = getInstance();
		
		instance.setBatchDate(System.getProperty("date"));
	
		instance.run(null);
		
		System.exit(1);
	}
	
	/**
	 * get a ProcBlacklist instance by spring <br/>
	 * @return instance
	 */
	public static ProcBlacklist getInstance(){
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ProcBlacklist instance = (ProcBlacklist) apContext.getBean("ProcBlacklist");
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
		    setUnprocess();						//處理online已經lock卡片資料
		    //acqMemIdInfos = getAcqMember();		//取目前所有的收單單位資料
		    	
		    //Fetch each acqMemIdInfo
		    HashMap regionPeriodInfo = new HashMap();	//存放某一收單單位的(regionStartDate, regionEndDate, regionCnt);
	    	
		    /*for (int i=0; i<acqMemIdInfos.size(); i++){
		    	regionPeriodInfo = null;
		    	//int blacklist_cnt = Integer.parseInt(((HashMap)acqMemIdInfos.get(i)).get("BLACKLIST_CNT").toString());
		    	//if (blacklist_cnt > 0){			//檢查某個收單單位的blacklist_cnt是否>0
		    		regionPeriodInfo = getBlacklistRegion((HashMap)acqMemIdInfos.get(i));
		    		genBlacklistVersion((HashMap)acqMemIdInfos.get(i), regionPeriodInfo);
		    	//}	
		    }*/
		    
		    //regionPeriodInfo = getBlacklistRegion((HashMap)acqMemIdInfos.get(i));
    		genBlacklistVersion();
		    
		    remarkSuccess();		//commit function
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
    private void setUnprocess() throws Exception{
    	Vector transInfos = new Vector();	//存放已被鎖卡交易	
    	StringBuffer sqlCmd = new StringBuffer();
    	
    	sqlCmd.append("select ISS_MEM_ID, CARD_NO, EXPIRY_DATE, TXN_DATE");
    	sqlCmd.append(" from TB_TRANS where");
    	sqlCmd.append(" CUT_DATE='").append(batchDate).append("'");
    	sqlCmd.append(" and P_CODE='").append(Constants.PCODE_7517).append("'");
    	
    	transInfos = BatchUtil.getInfoListHashMap(sqlCmd.toString());
    	logger.debug("Select all transInfo: " + transInfos);
    	
    	HashMap ht = new HashMap();
    	//Fetach each transInfo
    	//update已鎖卡 TB_CARD.BLOCK_DATE
	    for (int i=0; i<transInfos.size(); i++){
	    	sqlCmd.delete(0, sqlCmd.length());
	    	ht = (HashMap)transInfos.get(i);
		    sqlCmd.append("update TB_BLACKLIST_SETTING set");
		    sqlCmd.append(" BLOCK_DATE='").append(ht.get("TXN_DATE")).append("'");
		    //sqlCmd.append(" where MEM_ID='").append(ht.get("ISS_MEM_ID")).append("'");
		    sqlCmd.append(" where CARD_NO='").append(ht.get("CARD_NO")).append("'");
		    sqlCmd.append(" and EXPIRY_DATE='").append(ht.get("EXPIRY_DATE")).append("'");
		    sqlCmd.append(" and CANCEL_DATE is null");
		    sqlCmd.append(" and CANCEL_TIME is null");
		    
		    try{	//20070604新增SQLExceptiont處理
		    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);		//$20070603修改,增加false參數
		    }
		    catch (SQLException e){
		    	throw new SQLException("setUnprocess() update TB_BLACKLIST_SETTING failed." + e);
		    }
	    }
	    logger.info("setUnprocess():ok. \n");
    }
    
    /**
     * 產生黑名單版本
     * @return
     * @throws SQLException
     */
    private void genBlacklistVersion() throws Exception{
    	long maxVerNo = 0;
    	int i = 0;			//迴圈用
    	boolean regionFlag = false;
    	NumberFormat maxVerNoFormatter = new DecimalFormat("000000");
    	NumberFormat recnoFormatter = new DecimalFormat("0000");
    	
    	StringBuffer sqlCmd = new StringBuffer();
    	
    	sqlCmd.append("select (max(VERNO)+1) as maxVerNo");
    	sqlCmd.append(" from TB_BLACKLIST_VER");
    	//sqlCmd.append(" where ACQ_MEM_ID='").append(l_acqMemIdInfo.get("MEM_ID")).append("'");
 
    	Vector v = BatchUtil.getInfoList(sqlCmd.toString());
    	if (((Vector)v.get(0)).get(0) != null){
    		maxVerNo = Long.parseLong((((Vector)v.get(0)).get(0)).toString());	//找出最大版本
    		
    	}
    	else{	//之前沒有版本紀錄,則從1開始
    		maxVerNo = 1;	
    	}
    		  
    	Vector blacklistDataInfo = new Vector();

    	sqlCmd.delete(0, sqlCmd.length());
    	sqlCmd.append("select CARD_NO, EXPIRY_DATE, BLACKLIST_CODE");
    	sqlCmd.append(" from TB_BLACKLIST_SETTING ");
    	sqlCmd.append(" where STATUS='1'");
    	sqlCmd.append(" and CANCEL_DATE is null and BLOCK_DATE is null");
    	sqlCmd.append(" order by CARD_NO DESC");
    	try{	//20070604新增SQLException處理
    		blacklistDataInfo = BatchUtil.getInfoListHashMap(sqlCmd.toString(), conn);
    	}
    	catch (SQLException e){
    		throw new SQLException("genBlacklistVersion()select CARD_NO, EXPIRY_DATE failed." + e);
    	}
   
    	//=====Mantis No:0001530修改處=====
    	if ((!(regionFlag)) || maxVerNo <= 1){	//Mantis No:0001530修改處
    		//=====20070409 Mantis No:0001551修改處=====
    		//若最大版本超過999999則丟出Exception..並顯示通知系統管理者
    		//設定rcode=2999表示嚴重錯誤等級
	    	if (maxVerNo >= 1000000){
	    		String errMsg = "max version has over max default value 999999. Please call system administrator.";
	    		logger.error(errMsg);
	    		setRcode("2999");
    			setErrorDesc(errMsg);
	    		throw new SQLException(errMsg);
	    	}
	    	//=========================================
    		
    		
    		//Fetch each blackListDataInfo
	    	int recno = 0;
	    	
	    	String strMaxVerNo = maxVerNoFormatter.format(maxVerNo);
	    	
	    	List blackListCardNo = new ArrayList();
	    	
		    for (i=0; i<blacklistDataInfo.size(); i++){
		    	//sqlCmd.delete(0, sqlCmd.length());
			    //sqlCmd.append("insert into TB_BLACKLIST_DTL");
			    //sqlCmd.append(" (ACQ_MEM_ID, BLACK_VER, RECNO, CARD_NO, EXPIRY_DATE)");
			    //sqlCmd.append(" Values ('").append(l_acqMemIdInfo.get("MEM_ID")).append("', '").append(strMaxVerNo).append("', '").append(strRecno).append("', '").append(((HashMap)blacklistDataInfo.get(i)).get("CARD_NO")).append("', '").append(((HashMap)blacklistDataInfo.get(i)).get("EXPIRY_DATE")).append("')");
			    //logger.info("SQL Insert BL_DTL: " + sqlCmd);
		
			    //logger.info("fetach each blacklistDataInfo Insert TB_BLACKLIST_DTL");
			    //try{	//20070604新增SQLException處理
			    //	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);		//$20070603修改,增加false參數
			    //}
			    //catch (SQLException e){
			    //	throw new SQLException("genBlacklistVersion() insert into TB_BLACKLIST_DTL failed." + e);
			    //}
			 
			    recno++;
			    sqlCmd.delete(0, sqlCmd.length());
			    	
			    //Update TB_BLACKLIST_SETTING
			    sqlCmd.append("update TB_BLACKLIST_SETTING");
			    sqlCmd.append(" set BLA_GEN_DATE='").append(sysDate).append("'");
			    sqlCmd.append(" , BLA_GEN_TIME='").append(sysTime).append("'");
			    sqlCmd.append(" where CARD_NO='").append(((HashMap)blacklistDataInfo.get(i)).get("CARD_NO")).append("'");
			    sqlCmd.append(" and EXPIRY_DATE='").append(((HashMap)blacklistDataInfo.get(i)).get("EXPIRY_DATE")).append("'");
			    sqlCmd.append(" and CANCEL_DATE is null and block_date is null");
			    	
			    logger.debug("Update Setting PROC_DATE :" + sqlCmd);
			    //logger.info("fetach each blacklistDataInfo Update TB_BLACKLIST_SETTING");
			    try{	//20070604新增SQLException處理
			    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);			//$20070603修改,增加false參數
			    }
			    catch (SQLException e){
			    	throw new SQLException("genBlacklistVersion() update TB_BLACKLIST_SETTING failed." + e);
			    }
			    
			  	//Insert TB_BLACKLIST_VER
		    	sqlCmd.delete(0, sqlCmd.length());
		    	if (blacklistDataInfo.size() > 0 && !blackListCardNo.contains(((HashMap)blacklistDataInfo.get(i)).get("CARD_NO"))){
			    	sqlCmd.append("insert into TB_BLACKLIST_VER");
			    	sqlCmd.append(" (VERNO, CARD_NO, RECNO, BLACKLIST_CODE)");
			    	sqlCmd.append(" values ('").append(strMaxVerNo).append("', '").append(((HashMap)blacklistDataInfo.get(i)).get("CARD_NO")).append("', '").append(recno).append("', '").append(((HashMap)blacklistDataInfo.get(i)).get("BLACKLIST_CODE")).append("')");
			    	try{	//20070604新增SQLException處理
			    		DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);			//$20070603修改,增加false參數
			    		blackListCardNo.add(((HashMap)blacklistDataInfo.get(i)).get("CARD_NO"));
			    	}
			    	catch (SQLException e){
			    		throw new SQLException("genBlacklistVersion() insert into TB_BLACKLIST_VER failed." + e);
			    	}
		    	}
		    }	
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
