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
package tw.com.hyweb.svc.yhdp.batch.util.ProcBlacklist;

import java.io.File;
import java.text.*;
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
		    acqMemIdInfos = getAcqMember();		//取目前所有的收單單位資料
		    	
		    //Fetch each acqMemIdInfo
		    HashMap regionPeriodInfo = new HashMap();	//存放某一收單單位的(regionStartDate, regionEndDate, regionCnt);
	    	
		    for (int i=0; i<acqMemIdInfos.size(); i++){
		    	regionPeriodInfo = null;
		    	/*int blacklist_cnt = Integer.parseInt(((HashMap)acqMemIdInfos.get(i)).get("BLACKLIST_CNT").toString());
		    	if (blacklist_cnt > 0){			//檢查某個收單單位的blacklist_cnt是否>0
		    		regionPeriodInfo = getBlacklistRegion((HashMap)acqMemIdInfos.get(i));
		    		genBlacklistVersion((HashMap)acqMemIdInfos.get(i), regionPeriodInfo);
		    	}	*/
		    	regionPeriodInfo = getBlacklistRegion((HashMap)acqMemIdInfos.get(i));
	    		genBlacklistVersion((HashMap)acqMemIdInfos.get(i), regionPeriodInfo);
		    }
		    
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
	    for (int i=0; i<transInfos.size(); i++){
	    	sqlCmd.delete(0, sqlCmd.length());
	    	ht = (HashMap)transInfos.get(i);
		    sqlCmd.append("update TB_BLACKLIST_SETTING set");
		    sqlCmd.append(" BLOCK_DATE='").append(ht.get("TXN_DATE")).append("'");
		    sqlCmd.append(" where MEM_ID='").append(ht.get("ISS_MEM_ID")).append("'");
		    sqlCmd.append(" and CARD_NO='").append(ht.get("CARD_NO")).append("'");
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
     * 取目前所有的收單單位
     * @return l_acqMemIdInfo
     */
    private Vector getAcqMember(){
    	Vector l_acqMemIdInfos = new Vector();	//存放所有收單單位
    	StringBuffer sqlCmd = new StringBuffer();
    	
    	sqlCmd.append("select MEM_ID");
    	sqlCmd.append(" from TB_MEMBER");
    	sqlCmd.append(" where substr(MEM_TYPE, 2, 1)='1'");
    	l_acqMemIdInfos = BatchUtil.getInfoListHashMap(sqlCmd.toString());
    	logger.debug("Get all acqMember: " + l_acqMemIdInfos);
    	return l_acqMemIdInfos;
    }
    
    /**
     * 取出個別收單單位的黑名單區間
     * @return regionPeriodInfo
     * @throws SQLException
     */
    private HashMap getBlacklistRegion(HashMap l_acqMemIdInfo) throws SQLException{
    	Vector reggdateInfo = new Vector();
    	//regionPeriodInfo init
    	HashMap<String, String> regionPeriodInfo = new HashMap<String, String>();	//存放某個收單單位的regionPeriodInfo
    	
    	String regionStartDate = "00000000";
    	String regionEndDate = "00000000";
    	int regionCnt = 0;
    	StringBuffer sqlCmd = new StringBuffer();
    
    	sqlCmd.append("select REG_DATE, count(*) as cnt");
    	sqlCmd.append(" from TB_BLACKLIST_SETTING tbs, TB_ACQ_DEF ad");
    	sqlCmd.append(" where ad.ACQ_MEM_ID='").append(l_acqMemIdInfo.get("MEM_ID")).append("'");
    	sqlCmd.append(" and ad.ISS_MEM_ID=tbs.MEM_ID");
    	sqlCmd.append(" and BLOCK_DATE is null");
    	sqlCmd.append(" and CANCEL_DATE is null");
    	sqlCmd.append(" Group by REG_DATE");
    	sqlCmd.append(" Order by REG_DATE DESC");
    	try{			//20070604新增
    		reggdateInfo = BatchUtil.getInfoListHashMap(sqlCmd.toString(), conn);
    	}
    	catch (SQLException e){
    		throw new SQLException("getBlacklistRegion() select REG_DATE failed." + e);
    	}
    	
    	//Fetch each reggdateInfo
    	for (int i=0; i<reggdateInfo.size(); i++){
	    	if (regionCnt == 0){
	    		regionStartDate = ((HashMap)reggdateInfo.get(i)).get("REG_DATE").toString();
	    		regionEndDate = ((HashMap)reggdateInfo.get(i)).get("REG_DATE").toString();
	    		regionCnt = Integer.parseInt(((HashMap)reggdateInfo.get(i)).get("CNT").toString());
	    	
	    	}
	    	/*else if(Integer.parseInt(l_acqMemIdInfo.get("BLACKLIST_CNT").toString())>= regionCnt + Integer.parseInt(((HashMap)reggdateInfo.get(i)).get("CNT").toString())){
	    		regionStartDate = ((HashMap)reggdateInfo.get(i)).get("REG_DATE").toString();
	    		regionCnt = regionCnt + Integer.parseInt(((HashMap)reggdateInfo.get(i)).get("CNT").toString());
	    		
	    	}*/
	    	else{
	    		break;
	    	}
    	}	
    	regionPeriodInfo.put("regionStartDate", regionStartDate);
    	regionPeriodInfo.put("regionEndDate", regionEndDate);
    	regionPeriodInfo.put("regionCnt", Integer.toString(regionCnt));
    	logger.debug(l_acqMemIdInfo.get("MEM_ID") + "regionPeriodInfo:" + regionPeriodInfo);
    	return regionPeriodInfo;
    }
    
    /**
     * 產生黑名單版本
     * @return
     * @throws SQLException
     */
    private void genBlacklistVersion(HashMap l_acqMemIdInfo, HashMap l_regionPeriodInfo) throws Exception{
    	long maxVerNo = 0;
    	int i = 0;			//迴圈用
    	boolean regionFlag = false;
    	boolean dtlFlag = false; 
    	NumberFormat maxVerNoFormatter = new DecimalFormat("000000");
    	NumberFormat recnoFormatter = new DecimalFormat("0000");
    	
    	StringBuffer sqlCmd = new StringBuffer();
    	
    	sqlCmd.append("select (max(BLACK_VER)+1) as maxVerNo");
    	sqlCmd.append(" from TB_BLACKLIST_VER");
    	sqlCmd.append(" where ACQ_MEM_ID='").append(l_acqMemIdInfo.get("MEM_ID")).append("'");
 
    	Vector v = BatchUtil.getInfoList(sqlCmd.toString());
    	if (((Vector)v.get(0)).get(0) != null){
    		maxVerNo = Long.parseLong((((Vector)v.get(0)).get(0)).toString());	//找出最大版本
    		
    	}
    	else{	//之前沒有版本紀錄,則從1開始
    		maxVerNo = 1;	
    	}
    		
    	logger.debug("acq_mem_id: " + l_acqMemIdInfo.get("MEM_ID") + " maxVerNo: " + maxVerNo);
   
    	Vector blacklistDataInfo = new Vector();

    	sqlCmd.delete(0, sqlCmd.length());
    	sqlCmd.append("select MEM_ID, CARD_NO, EXPIRY_DATE");
    	sqlCmd.append(" from TB_BLACKLIST_SETTING TBS, TB_ACQ_DEF AD");
    	sqlCmd.append(" where AD.ACQ_MEM_ID='").append(l_acqMemIdInfo.get("MEM_ID")).append("'");
    	sqlCmd.append(" and AD.ISS_MEM_ID=TBS.MEM_ID");
    	sqlCmd.append(" and TBS.CANCEL_DATE is null and TBS.BLOCK_DATE is null");
    	sqlCmd.append(" and REG_DATE between '").append(l_regionPeriodInfo.get("regionStartDate")).append("' and '").append(l_regionPeriodInfo.get("regionEndDate")).append("'");	//修改 b:append("and ") a:append(" and ")
    	sqlCmd.append(" order by CARD_NO DESC");	//增加order by 主要為日後比較用Dtl比較用
    	try{	//20070604新增SQLException處理
    		blacklistDataInfo = BatchUtil.getInfoListHashMap(sqlCmd.toString(), conn);
    	}
    	catch (SQLException e){
    		throw new SQLException("genBlacklistVersion()select MEM_ID, CARD_NO, EXPIRY_DATE failed." + e);
    	}
   
    	//=====Mantis No:0001530修改處=====
    	//之前版本為不論黑名單是否和前一版相同都一定會產生新的一組黑名單
    	//這樣不僅浪費空間,又會浪費EDC下載時間
    	//重點:判斷是否要新增黑名單,若新產生的版本和上一個相同則不產生新的
    	if (maxVerNo > 1){	//表示有先前版本
    		regionFlag = checkVerRegion(l_acqMemIdInfo, l_regionPeriodInfo, maxVerNoFormatter.format(maxVerNo-1));
    		logger.debug(l_acqMemIdInfo.get("MEM_ID") + " -> regionFlag: " + regionFlag);
    		if (regionFlag){
    			dtlFlag = checkVerDtl(blacklistDataInfo, l_acqMemIdInfo, l_regionPeriodInfo, maxVerNoFormatter.format(maxVerNo-1));
    			logger.debug(l_acqMemIdInfo.get("MEM_ID") + " -> dtlFlag: " + dtlFlag);
    		}
    	}
    	//=====Mantis No:0001530修改處=====
    	if ((!(regionFlag && dtlFlag)) || maxVerNo <= 1){	//Mantis No:0001530修改處
    		//=====20070409 Mantis No:0001551修改處=====
    		//若最大版本超過999999則丟出Exception..並顯示通知系統管理者
    		//設定rcode=2999表示嚴重錯誤等級
	    	if (maxVerNo >= 1000000){
	    		String errMsg = "ACQ_MEM_ID: " + l_acqMemIdInfo.get("MEM_ID") + 
	    			"'s max version has over max default value 999999. Please call system administrator.";
	    		logger.error(errMsg);
	    		setRcode("2999");
    			setErrorDesc(errMsg);
	    		throw new SQLException(errMsg);
	    	}
	    	//=========================================
    		
    		
    		//Fetch each blackListDataInfo
	    	int recno = 1;
	    	
	    	String strMaxVerNo = maxVerNoFormatter.format(maxVerNo);
	    	String strRecno = recnoFormatter.format(recno);
	    	
		    for (i=0; i<blacklistDataInfo.size(); i++){
		    	sqlCmd.delete(0, sqlCmd.length());
			    sqlCmd.append("insert into TB_BLACKLIST_DTL");
			    sqlCmd.append(" (ACQ_MEM_ID, BLACK_VER, RECNO, CARD_NO, EXPIRY_DATE)");
			    sqlCmd.append(" Values ('").append(l_acqMemIdInfo.get("MEM_ID")).append("', '").append(strMaxVerNo).append("', '").append(strRecno).append("', '").append(((HashMap)blacklistDataInfo.get(i)).get("CARD_NO")).append("', '").append(((HashMap)blacklistDataInfo.get(i)).get("EXPIRY_DATE")).append("')");
			    //logger.info("SQL Insert BL_DTL: " + sqlCmd);
		
			    logger.info("fetach each blacklistDataInfo Insert TB_BLACKLIST_DTL");
			    try{	//20070604新增SQLException處理
			    	DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);		//$20070603修改,增加false參數
			    }
			    catch (SQLException e){
			    	throw new SQLException("genBlacklistVersion() insert into TB_BLACKLIST_DTL failed." + e);
			    }
			 
			    recno++;
			    strRecno = recnoFormatter.format(recno);
			    sqlCmd.delete(0, sqlCmd.length());
			    	
			    //Update TB_BLACKLIST_SETTING
			    sqlCmd.append("update TB_BLACKLIST_SETTING");
			    sqlCmd.append(" set PROC_DATE='").append(batchDate).append("'");
			    sqlCmd.append(" where MEM_ID='").append(((HashMap)blacklistDataInfo.get(i)).get("MEM_ID")).append("'");
			    sqlCmd.append(" and CARD_NO='").append(((HashMap)blacklistDataInfo.get(i)).get("CARD_NO")).append("'");
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
		    }
		    
	    	//Insert TB_BLACKLIST_VER
	    	sqlCmd.delete(0, sqlCmd.length());
	    	if (Integer.parseInt(l_regionPeriodInfo.get("regionCnt").toString()) > 0){
		    	sqlCmd.append("insert into TB_BLACKLIST_VER");
		    	sqlCmd.append(" (ACQ_MEM_ID, BLACK_VER, BLACK_SDATE, BLACK_EDATE, PROC_DATE, PROC_TIME, TTL_REC)");
		    	sqlCmd.append(" values ('").append(l_acqMemIdInfo.get("MEM_ID")).append("', '").append(strMaxVerNo).append("', '").append(l_regionPeriodInfo.get("regionStartDate")).append("', '").append(l_regionPeriodInfo.get("regionEndDate")).append("', '").append(sysDate).append("', '").append(sysTime).append("', ").append(l_regionPeriodInfo.get("regionCnt")).append(")");
		    	try{	//20070604新增SQLException處理
		    		DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);			//$20070603修改,增加false參數
		    	}
		    	catch (SQLException e){
		    		throw new SQLException("genBlacklistVersion() insert into TB_BLACKLIST_VER failed." + e);
		    	}
	    	}
    	}	
    }
    
    /**
     * =====Mantis No:0001530新增處=====
     * 檢查要產生的版本的regionPeriodInfo是否和前一版相同<br/>
     * 利用欄位:<br/>
     *  BLACK_SDATE<br/>
     *  BLACK_EDATE<br/>
     *  TTL_REC<br/>
     * @param l_acqMemIdInfo
     * @param l_regionPeriodInfo
     * @param orgVerNo
     * @return boolean
     */
    private boolean checkVerRegion(HashMap l_acqMemIdInfo, HashMap l_regionPeriodInfo, String orgVerNo){
    	StringBuffer sqlCmd = new StringBuffer();
    	
    	sqlCmd.append("select BLACK_SDATE, BLACK_EDATE, TTL_REC");
    	sqlCmd.append(" from TB_BLACKLIST_VER");
    	sqlCmd.append(" where ACQ_MEM_ID='").append(l_acqMemIdInfo.get("MEM_ID")).append("'");
    	sqlCmd.append(" and BLACK_VER='").append(orgVerNo).append("'");
    	Vector orgRegionPeriodInfo =  BatchUtil.getInfoListHashMap(sqlCmd.toString());
    	logger.info("orgRegionPeriodInfo:" + orgRegionPeriodInfo.toString());
    	logger.info("RegionPeriodInfo:" + l_regionPeriodInfo.toString());
    	//判斷orgRegionPeriod和新產生的regionPeriod是否相同
   
    	if (l_regionPeriodInfo.get("regionStartDate").equals(((HashMap)orgRegionPeriodInfo.get(0)).get("BLACK_SDATE")) && l_regionPeriodInfo.get("regionEndDate").equals(((HashMap)orgRegionPeriodInfo.get(0)).get("BLACK_EDATE")) && (l_regionPeriodInfo.get("regionCnt").toString()).equals((((HashMap)orgRegionPeriodInfo.get(0)).get("TTL_REC")).toString())){
    		logger.info(l_acqMemIdInfo.get("MEM_ID") + "版本Region:true相同" );
    		logger.info("StartDate: " +l_regionPeriodInfo.get("regionStartDate") + "  " +((HashMap)orgRegionPeriodInfo.get(0)).get("BLACK_SDATE"));
    		return true;
    	}
    	else{
    		logger.info(l_acqMemIdInfo.get("MEM_ID") + "版本Region:false不同" );
    		logger.info("StartDate: " +l_regionPeriodInfo.get("regionStartDate") + "  " +((HashMap)orgRegionPeriodInfo.get(0)).get("BLACK_SDATE"));
    		return false;
    	}
    }
    
    /**
     * =====Mantis No:0001530新增處=====
     * 檢查在TB_BLACKLIST_DTL Table內,某一收單單位的orgVerNo的Data<br/>
     * 是不是和現在要產生的Data一樣<br/>
     * 檢查方式:<br/>
     * 利用在TB_BLACKLIST_DTL內的CARD_NO, EXPIRY_DATE判斷是否需要新產生版本<br/>
     * @param l_blacklistDataInfo
     * @param l_regionPeriodInfo
     * @param l_regionPeriodInfo
     * @param orgVerNo
     * @return boolean
     */
    private boolean checkVerDtl(Vector l_blacklistDataInfo, HashMap l_acqMemIdInfo, HashMap l_regionPeriodInfo, String orgVerNo){
    	StringBuffer sqlCmd = new StringBuffer();
    	//判斷要insert的blacklistDataInfo是不是和上一版相同
    	sqlCmd.delete(0, sqlCmd.length());
    	sqlCmd.append("select CARD_NO, EXPIRY_DATE");
    	sqlCmd.append(" from TB_BLACKLIST_DTL");
    	sqlCmd.append(" where ACQ_MEM_ID='").append(l_acqMemIdInfo.get("MEM_ID")).append("'");
    	sqlCmd.append(" and BLACK_VER='").append(orgVerNo).append("'");
    	sqlCmd.append(" order by CARD_NO DESC");		//增加order by 主要為日後比較用Dtl比較用
    	Vector orgBlacklistDataInfo = BatchUtil.getInfoListHashMap(sqlCmd.toString());
    	
    	//比較兩個最新產生的Dtl和原來的Dtl是否相等
    	for (int i=0; i<l_blacklistDataInfo.size(); i++){
    		if (((HashMap)l_blacklistDataInfo.get(i)).get("CARD_NO").equals(((HashMap)orgBlacklistDataInfo.get(i)).get("CARD_NO")) && ((HashMap)l_blacklistDataInfo.get(i)).get("EXPIRY_DATE").equals(((HashMap)orgBlacklistDataInfo.get(i)).get("EXPIRY_DATE"))){
    			logger.info("checkVerDtl() return:" + ((HashMap)l_blacklistDataInfo.get(i)).get("CARD_NO") + ": true");
    		}
    		else{
    			logger.info("checkVerDtl() return:" + ((HashMap)l_blacklistDataInfo.get(i)).get("CARD_NO") + ": false");
    			return false;
    		}
    	}
    	return true;
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
