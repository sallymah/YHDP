/**
 * changelog ExpMemberData
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
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
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
 * ExpAssociator
 * </pre>
 * author:duncan
 */
public class ExpCAPCF extends AbstractExpFile { 
    private static Logger log = Logger.getLogger(ExpCAPCF.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpCAPCF" + File.separator + "spring.xml";
    private static final String CAPCF_FN = "CAPCF";
    private static final String CAPCF_APPEND = "CAPCF";

    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    
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
    
    private Integer totalRecords = 0;
    
    private String memId = "";

    public ExpCAPCF() {
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

	private void beforeHandling() throws SQLException {

        Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
                		"WHERE MEM_ID = '" + memId + "'"+
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(CAPCF_FN) + 
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
        efs.setTempFilePending(".TMP");
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
            expFileInfo.setFileName(CAPCF_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = CAPCF_APPEND + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno()+ "." + "csv" ;
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL = "SELECT txn.ACQ_MEM_ID, mem.MEM_NAME,txn.MERCH_ID,tm.MERCH_LOC_NAME,txn.TERM_ID,txn.BATCH_NO, " +
            		"cap.BATCH_NO,txn.CARD_NO, txn.LMS_INVOICE_NO, txn.ORIG_LMS_INVOICE_NO, txn.TXN_DATE, txn.TXN_TIME, " +
            		"txn.TXN_AMT, txn.P_CODE, pdf.P_CODE_DESC,decode(txn.STATUS, '1', '成功', 'C', '取消', 'R', '被退貨', txn.STATUS), " +
            		"txn.CHIP_POINT1_CR, txn.CHIP_POINT1_BEFORE, txn.CHIP_POINT1_DB," +
            		"txn.CHIP_POINT1_AFTER,  txn.TERM_DATE, txn.TERM_TIME, " +
            		"txn.cut_date , txn.term_settle_date,  cap.TXN_TIME " +
            		"FROM TB_TRANS txn, TB_MEMBER mem, TB_MERCH tm, TB_P_CODE_DEF pdf," +
            		"(SELECT capt.ACQ_MEM_ID, capt.MERCH_ID, capt.TERM_ID, capt.CARD_NO, capt.TXN_DATE,capt.TXN_TIME , capt.TXN_AMT, caps.TXN_TYPE, " +
            		"capt.P_CODE, capt.BATCH_NO FROM TB_CAP_SETTING caps, TB_CAP_TXN capt "+
            		"WHERE capt.LMS_INVOICE_NO = caps.LMS_INVOICE_NO AND capt.CARD_NO = caps.CARD_NO "+
            		"AND capt.EXPIRY_DATE = caps.EXPIRY_DATE) cap " +
            		"WHERE txn.CUT_DATE = '"+ batchDate +"' "+
            		"AND tm.MERCH_ID = txn.MERCH_ID AND mem.mem_id = txn.ACQ_MEM_ID "+
            		"AND cap.MERCH_ID = txn.MERCH_ID AND cap.TERM_ID = txn.TERM_ID "+
            		"AND txn.P_CODE = pdf.P_CODE AND cap.CARD_NO = txn.CARD_NO "+
            		"AND cap.TXN_DATE = txn.TXN_DATE AND cap.TXN_AMT = txn.TXN_AMT "+
            		"AND cap.P_CODE = txn.P_CODE AND cap.TXN_TYPE = '3' "+
            		"ORDER BY txn.TXN_DATE||txn.TXN_TIME, txn.MERCH_ID, txn.CARD_NO ";
            
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
        header.append("收單單位代碼,收單單位名稱,");
        header.append("特店代碼,特店名稱,端末代碼,批號,人工簽單批號,卡號, 交易序號, 原始交易序號, 交易日期 , 交易時間,交易金額 ,");
        header.append("交易類別,交易類型,交易狀態,加值金額, 交易前餘額, 消費金額 ,交易後餘額,端末日期 ,端末時間 ,過檔日期,");
        header.append("結帳日期,人工簽單交易時間,");
        return header.toString();
    }

    public String outputEndFile() {    	

        return null;
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
    	
    	String acqmemId="";
    	String memName = "";
    	String merchId = "";
    	String merchLocName = "";
    	String termId = "";
    	String batchNo = "";
    	String capBatchNo = "";
    	String cardNo = "";
    	String lmsInvoiceNo = "";
    	String origLmsInvoiceNo = "";
    	String txnDate = "";
    	String txnTime = "";
    	String txnAmt = "";
    	String pCode = "";
    	String pCodeDesc = "";
    	String status = "";
    	String chipPoint1Cr ="";
    	String chipPointBefore="";
    	String chipPointDb ="";
    	String chipPointAfter="";
    	String termDate="";
    	String termTime="";
    	String cutDate ="";
    	String termSettleDate="";
    	String capTxnTime="";
    	
    	
    	acqmemId=record.get(0).toString();
    	memName = record.get(1).toString();
    	merchId = record.get(2).toString();
    	if (!isBlankOrNull(record.get(3).toString()))
    		merchLocName = record.get(3).toString();
    	if (!isBlankOrNull(record.get(4).toString()))
    		termId = record.get(4).toString();
    	if (!isBlankOrNull(record.get(5).toString()))
    		batchNo = record.get(5).toString();
	    if (!isBlankOrNull(record.get(6).toString()))
	    	capBatchNo = record.get(6).toString();
	    if (!isBlankOrNull(record.get(7).toString()))
	    	cardNo = record.get(7).toString();
	    if (!isBlankOrNull(record.get(8).toString()))
	    	lmsInvoiceNo = record.get(8).toString();
	    if (!isBlankOrNull(record.get(9).toString()))
	    	origLmsInvoiceNo = record.get(9).toString();
	    if (!isBlankOrNull(record.get(10).toString()))
	    	txnDate = record.get(10).toString();
	    if (!isBlankOrNull(record.get(11).toString()))
	    	txnTime = record.get(11).toString();
	    if (!isBlankOrNull(record.get(12).toString()))
	    	txnAmt = record.get(12).toString();
	    if (!isBlankOrNull(record.get(13).toString()))
	    	pCode = record.get(13).toString();
	    if (!isBlankOrNull(record.get(14).toString()))
	    	pCodeDesc = record.get(14).toString();
	    if (!isBlankOrNull(record.get(15).toString()))
	    	status = record.get(15).toString();
	    if (record.get(16) != null)
	    	chipPoint1Cr = record.get(16).toString();
	    if (record.get(17) != null)
	    	chipPointBefore = record.get(17).toString();
	    if (record.get(18) != null)
	    	chipPointDb = record.get(18).toString();
	    if (record.get(19) != null)
	    	chipPointAfter = record.get(19).toString();
	    if (!isBlankOrNull(record.get(20).toString()))
	    	termDate = record.get(20).toString();
	    if (!isBlankOrNull(record.get(21).toString()))
	    	termTime = record.get(21).toString();
	    if (!isBlankOrNull(record.get(22).toString()))
	    	cutDate = record.get(22).toString();  
	    if (!isBlankOrNull(record.get(23).toString()))
	    	termSettleDate = record.get(23).toString();
	    if (!isBlankOrNull(record.get(24).toString()))
	    	capTxnTime = record.get(24).toString();  
        
        StringBuffer sb = new StringBuffer();
        sb.append(acqmemId ).append(",");
    	sb.append(memName ).append(",");
    	sb.append(merchId ).append(",");
    	sb.append(merchLocName ).append(",");
    	sb.append(termId ).append(",");   	
    	sb.append(batchNo ).append(",");
    	sb.append(capBatchNo ).append(",");
    	sb.append(cardNo ).append(",");
    	sb.append(lmsInvoiceNo ).append(",");
    	sb.append(origLmsInvoiceNo ).append(",");
    	sb.append(txnDate).append(",");
    	sb.append(txnTime).append(",");
    	sb.append(txnAmt).append(",");
    	sb.append(pCode).append(",");
    	sb.append(pCodeDesc).append(",");
    	sb.append(status).append(",");
    	sb.append(chipPoint1Cr).append(",");
    	sb.append(chipPointBefore).append(",");
    	sb.append(chipPointDb).append(",");
    	sb.append(chipPointAfter).append(",");
    	sb.append(termDate).append(",");
    	sb.append(termTime).append(",");
    	sb.append(cutDate).append(",");
    	sb.append(termSettleDate).append(",");
    	sb.append(capTxnTime).append(",");
       
        return sb.toString();
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpCAPCF getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpCAPCF instance = (ExpCAPCF) apContext.getBean("expCAPCF");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    

    public static void main(String[] args) {
    	ExpCAPCF expCAPCF = null;
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
            	expCAPCF = getInstance();
            }
            else {
            	expCAPCF = new ExpCAPCF();
            }
            expCAPCF.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            expCAPCF.beforeHandling();
            expCAPCF.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpCAPCF run fail:" + ignore.getMessage(), ignore);
        }
    }
}
