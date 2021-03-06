/**
 * changelog ExpAllBlacklist
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;

import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

/**
 * <pre> 
 * ExpAllBlacklist
 * </pre>
 * author:Sally
 */
public class ExpAllBlacklist extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpAllBlacklist.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpAllBlacklist" + File.separator + "spring.xml";
    private static final String BLACKLIST_FN = "ALLBK";
    private static final String BLACKLIST_APPEND = "ALLBK";
    private static final String EXTEND_FILE_NAME = "DAT";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List memIds = null;
    // key:String(memId), value:String(seqno)
    private HashMap memId2Seqno = new HashMap();
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    private HashMap memId2Count = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private HashMap transationInfos = null;
    private String memId = "";
    private String time = "";
    public ExpAllBlacklist() {
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        try {
            "".getBytes(encoding);
        }
        catch (Exception ignore) {
            // invalid encoding
            encoding = "UTF-8";
        }
        this.encoding = encoding;
    }

    public List getMemIds() {
        return memIds;
    }

    public void setMemIds(List memIds) {
        this.memIds = memIds;
    }
    public String getMemId() {
		return memId;
	}

	public void setMemId(String memId) {
		this.memId = memId;
	}
    
    public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	private void beforeHandling() throws SQLException {

        Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        
        // Delete TB_OUTCTL
        String deleteSql = "DELETE TB_OUTCTL " +
        		"WHERE MEM_ID = '" + memId + "'"+
        		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(BLACKLIST_FN) + 
        		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
        try
        {
             log.info(" recoverData():"+deleteSql);
             DBService.getDBService().sqlAction(deleteSql, conn, false);
             conn.commit();
        }
        catch (SQLException e)
        {
        	conn.rollback();
        	throw new SQLException("beforeHandling(): delete TB_OUTCTL. "+e);
        }
        
        Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
                		"WHERE MEM_ID = '" + memId + "'"+
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(BLACKLIST_FN) + 
                		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
        try {
        	stmt = conn.createStatement();
              log.warn("seqnoSql: "+seqnoSql);
              rs = stmt.executeQuery(seqnoSql);
              while (rs.next()) {
                  seqno = rs.getString(1);
             }
        }
        finally {
              ReleaseResource.releaseDB(conn, stmt, rs);
        }
        memId2Seqno.put(memId, seqno);
        memIds.add(memId);
        
        log.info("memIds:" + memIds);
        log.info("memId2Seqno:" + memId2Seqno);
        //log.info("memId2Count:" + memId2Count);
    }

    public ExpFileSetting makeExpFileSetting() {
        ExpFileSetting efs = new ExpFileSetting();
        // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
        // checkFlag:boolean:true
        // 所以若產生的檔案是 fixed length, 請設為 true, 若是 variable length, 請設為 false, default is true
        efs.setCheckFlag(false);
        // fileEncoding:String:UTF-8
        // 產生檔案時的編碼, default is UTF-8
        if (StringUtil.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        efs.setFileEncoding(encoding);
        // usingTempFile, 是否使用 temp file, default true
        // usingTempFile:boolean:true
        // 是否啟動 temp file 的動作
        efs.setUsingTempFile(true);
        // 若用 temp file, 預設先 pending 的字串, default ".TMP"
        // tempFilePending:String
        // 若啟動 temp file 的動作, 要如何 pending temp file 的檔名
        efs.setTempFilePending("");
        // recordsPerFlush:int:-1
        // 產生檔案時處理多少筆記錄後做一次 flush 動作, <= 0 時不 enable flush 動作, default is -1
        efs.setRecordsPerFlush(-1);
        // recordsPerFile:int:-1
        // 多少筆產生一個 file, default 全部一個檔案(<= 0)
        // 若有設 recordsPerFile, 每個 expFileInfo 的 seqnoStart, seqnoEnd 一定要給
        efs.setRecordsPerFile(-1);
        efs.setLineSeparator("\r\n");
        for (int i = 0; i < memIds.size(); i++) {
            String memId = (String) memIds.get(i);
            String seqno = (String) memId2Seqno.get(memId);
            //Integer count = (Integer) memId2Count.get(memId);
            if (seqno == null) {
                log.warn("can't find in memId2Seqno or memId2Count, ignore for '" + memId + "'");
                continue;
            }
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemId(memId);
            expFileInfo.setFileName(BLACKLIST_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            //String fullFileName = BLACKLIST_APPEND + "." + expFileInfo.getFileDate();
            String fullFileName = BLACKLIST_APPEND +expFileInfo.getFileDate()+seqno+"." + EXTEND_FILE_NAME;
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL ="SELECT CARD_NO, MIFARE_UL_UID, BLACKLIST_CODE, REG_DATE, LAST_TXN_DATE ("+
            		"SELECT CARD_NO, MIFARE_UL_UID, BLACKLIST_CODE, REG_DATE, LAST_TXN_DATE FROM (SELECT TB_BLACKLIST_SETTING.CARD_NO, MIFARE_UL_UID, BLACKLIST_CODE, TB_BLACKLIST_SETTING.REG_DATE, LAST_TXN_DATE FROM TB_BLACKLIST_SETTING,TB_CARD" +
            		" WHERE TB_BLACKLIST_SETTING.CARD_NO = TB_CARD.CARD_NO and BLACKLIST_CODE= '04' and TB_BLACKLIST_SETTING.REG_DATE >=" + DateUtil.addDate(batchDate, -7) +" " +
            		"UNION "+
            		"SELECT TB_BLACKLIST_SETTING.CARD_NO, MIFARE_UL_UID, BLACKLIST_CODE, TB_BLACKLIST_SETTING.REG_DATE, LAST_TXN_DATE FROM TB_BLACKLIST_SETTING,TB_CARD "+
            		"WHERE TB_BLACKLIST_SETTING.CARD_NO = TB_CARD.CARD_NO and BLACKLIST_CODE= '03'  "+
            		"UNION "+
            		"SELECT TB_BLACKLIST_SETTING.CARD_NO, MIFARE_UL_UID, BLACKLIST_CODE, TB_BLACKLIST_SETTING.REG_DATE, LAST_TXN_DATE FROM TB_BLACKLIST_SETTING,TB_CARD "+
            		"WHERE TB_BLACKLIST_SETTING.CARD_NO = TB_CARD.CARD_NO and BLACKLIST_CODE= '01' and TB_BLACKLIST_SETTING.APRV_USERID='CRM' "+
            		"AND  (TB_BLACKLIST_SETTING.CARD_NO  IN( SELECT CARD_NO FROM TB_CARD_BAL  "+
            		"WHERE TB_CARD_BAL.CARD_NO= TB_BLACKLIST_SETTING.CARD_NO AND CR_BONUS_QTY - DB_BONUS_QTY + BAL_BONUS_QTY = 0) OR TB_BLACKLIST_SETTING.REG_DATE >=  " + DateUtil.addYear(batchDate, -1) +") "+
            		"UNION "+
            		"SELECT TB_BLACKLIST_SETTING.CARD_NO, MIFARE_UL_UID, BLACKLIST_CODE, TB_BLACKLIST_SETTING.REG_DATE, LAST_TXN_DATE FROM TB_BLACKLIST_SETTING,TB_CARD "+
            		"WHERE TB_BLACKLIST_SETTING.CARD_NO = TB_CARD.CARD_NO and BLACKLIST_CODE= '05' AND  TB_BLACKLIST_SETTING.REG_DATE > = " + DateUtil.addYear(batchDate, -3) +" "+
            		"UNION "+
            		"SELECT TB_BLACKLIST_SETTING.CARD_NO, MIFARE_UL_UID, BLACKLIST_CODE, TB_BLACKLIST_SETTING.REG_DATE, LAST_TXN_DATE FROM TB_BLACKLIST_SETTING,TB_CARD "+
            		"WHERE TB_BLACKLIST_SETTING.CARD_NO = TB_CARD.CARD_NO and TB_BLACKLIST_SETTING.REG_DATE > = " + DateUtil.addYear(batchDate, -2) +" "+
            		"UNION "+
            		"SELECT TB_BLACKLIST_SETTING.CARD_NO, MIFARE_UL_UID, BLACKLIST_CODE, TB_BLACKLIST_SETTING.REG_DATE, LAST_TXN_DATE FROM TB_BLACKLIST_SETTING,TB_CARD "+
            		"WHERE TB_BLACKLIST_SETTING.CARD_NO = TB_CARD.CARD_NO and EXISTS  "+
            		"(SELECT * FROM TB_BAL_TRANSFER_DTL WHERE TB_BLACKLIST_SETTING.CARD_NO = TB_BAL_TRANSFER_DTL.ORIG_BALANCE_ID AND TB_BAL_TRANSFER_DTL.ORIG_BALANCE_ID LIKE '9%' AND BONUS_QTY = 0)) " + 
            		"WHERE ROWNUM <= 100000 ORDER BY REG_DATE DESC )"+
            		"ORDER BY CARD_NO DESC";
            
            if(i==0){
            	log.info(selectSQL);
            }
            expFileInfo.setSelectSQL(selectSQL);
            efs.addExpFileInfo(expFileInfo);
        }
        
        return efs;
    }

    public String outputAfterFile() {
        return super.outputAfterFile();
    }

    public String outputBeforeFile() {

        StringBuffer header = new StringBuffer();
        header.append("H0");
        header.append(batchDate + time);
        header.append(StringUtils.leftPad(expFileInfo.getSeqno(),6,'0'));
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.rightPad("", 14, ' '));
        return header.toString();
    }
    public String outputEndFile() {    	
    	//46
        StringBuffer header = new StringBuffer();
        header.append("/EOF");
        header.append(StringUtils.rightPad("", 42, ' '));
        return header.toString();
    }

    public void actionsAfterInfo() throws Exception {
        // 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
        super.actionsAfterFile();
    }

    public String outputOneRecord(List record) { 

    	String cardNo = "";
    	String mifareId = "";
    	String blaklistcode = "";
    	String regdate = "";
    	String lastTxnDate = "";
    	cardNo = record.get(0).toString();
    	mifareId = record.get(1).toString();   	
    	blaklistcode = record.get(2).toString();
    	regdate = record.get(3).toString();
    	lastTxnDate = record.get(4).toString();
        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(cardNo, 16, ' '));
        sb.append(StringUtils.rightPad(mifareId, 8, ' '));
        sb.append(StringUtils.rightPad(blaklistcode, 2, ' '));
        sb.append(StringUtils.rightPad(regdate, 8, ' '));
        sb.append(StringUtils.rightPad(lastTxnDate, 8, ' '));
        return sb.toString();
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpAllBlacklist getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpAllBlacklist instance = (ExpAllBlacklist) apContext.getBean("expAllBlacklist");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    

    public static void main(String[] args) {
    	ExpAllBlacklist expAllBlacklist = null;
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
            	expAllBlacklist = getInstance();
            }
            else {
            	expAllBlacklist = new ExpAllBlacklist();
            }
            expAllBlacklist.setBatchDate(batchDate);
            expAllBlacklist.setTime(DateUtil.getTodayString().substring(8, 14));
            // 註: 此 method 一定要先呼叫
            expAllBlacklist.beforeHandling();
            expAllBlacklist.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpAllBlacklist run fail:" + ignore.getMessage(), ignore);
        }
    }
}
