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
public class ExpCARTXN extends AbstractExpFile { 
    private static Logger log = Logger.getLogger(ExpCARTXN.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpCARTXN" + File.separator + "spring.xml";
    private static final String CARTXN_FN = "CARTXN";
    private static final String CARTXN_APPEND = "CARTXN";

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

    public ExpCARTXN() {
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
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(CARTXN_FN) + 
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
            expFileInfo.setFileName(CARTXN_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = CARTXN_APPEND + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno()+ "." + "csv" ;
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL = "SELECT T.CARD_NO,T.EXPIRY_DATE,T.TXN_DATE,T.TXN_TIME," +
            		"T.TERM_DATE,T.TERM_TIME,P.P_CODE_DESC,T.LMS_INVOICE_NO," +
            		"T.ATC,T.TXN_AMT,decode(T.STATUS, '1', '成功', 'C', '取消', 'R', '被退貨',T.STATUS),T.CHIP_POINT1_AFTER,MG.MEM_GROUP_ID,"+
            		"MG.MEM_GROUP_NAME,ME.MEM_ID, ME.MEM_NAME, T.MERCH_ID,MC.MERCH_LOC_NAME,T.TERM_ID ,T.BATCH_NO "+
            		"FROM TB_ONL_TXN T,TB_P_CODE_DEF P,TB_MERCH MC, TB_MEMBER ME, TB_MEMBER_GROUP MG "+
            		"WHERE T.P_CODE = P.P_CODE AND T.MERCH_ID = MC.MERCH_ID "+
            		"AND MC.MEM_ID = ME.MEM_ID "+
            		"AND ME.MEM_GROUP_ID = MG.MEM_GROUP_ID "+
            		"AND CARD_NO IN (SELECT CARD_NO FROM TB_SUPERVISION_CARD) "+
            		"ORDER BY CARD_NO, TXN_DATE||TXN_TIME ";

            
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
    	//632+2
        StringBuffer header = new StringBuffer();
        header.append("卡號,卡片到期日,交易日期,交易時間,端末交易日期,");
        header.append("端末交易時間,交易類別,交易序號,卡片交易序號,交易金額,");
        header.append("交易狀態,交易後餘額,總公司代碼,總公司名稱,收單單位代碼, ");
        header.append("收單單位名稱,特店代碼,特店名稱,端末代號,批號,");       

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

    	String cardNo = "";
    	String expiryDate = "";
    	String txnDate = "";
    	String txnTime = "";
    	String termDate="";
    	String termTime = "";
    	String pCodeDesc = "";
    	String lmsInvoiveNo = "";
    	String atc = "";
    	String txnAmt = "";
    	String status = "";
    	String chipPoint1After = "";
    	String memGroupId = "";
    	String memGroupName="";
    	String memId = "";
    	String memName = "";
    	String merchId = "";
    	String merchLocName = "";
    	String TermId = "";
    	String batchNo = "";
    	
    	cardNo = record.get(0).toString();
    	expiryDate = record.get(1).toString();
    	txnDate = record.get(2).toString();
		txnTime = record.get(3).toString();
		termDate=record.get(4).toString();
		termTime = record.get(5).toString();
		pCodeDesc = record.get(6).toString();
		lmsInvoiveNo = record.get(7).toString();
		atc = record.get(8).toString();
		txnAmt = record.get(9).toString();
		status = record.get(10).toString();
		chipPoint1After = record.get(11).toString();
		memGroupId = record.get(12).toString();
		memGroupName = record.get(13).toString();
		memId = record.get(14).toString();
		memName = record.get(15).toString();
		merchId = record.get(16).toString();
		merchLocName = record.get(17).toString();
		TermId = record.get(18).toString();
		batchNo = record.get(19).toString();
 
        StringBuffer sb = new StringBuffer();
		sb.append(cardNo).append(",");
    	sb.append(expiryDate).append(",");
    	sb.append(txnDate).append(",");
    	sb.append(txnTime).append(",");
    	sb.append(termDate).append(",");
    	sb.append(termTime).append(",");
    	sb.append(pCodeDesc).append(",");
    	sb.append(lmsInvoiveNo).append(",");
    	sb.append(atc).append(",");
    	sb.append(txnAmt).append(",");
    	sb.append(status).append(",");
    	sb.append(chipPoint1After).append(",");
    	sb.append(memGroupId).append(",");
    	sb.append(memGroupName).append(",");
    	sb.append(memId).append(",");
    	sb.append(memName).append(",");
    	sb.append(merchId).append(",");
    	sb.append(merchLocName).append(",");
    	sb.append(TermId).append(",");
    	sb.append(batchNo).append(",");
    	
       
        return sb.toString();
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpCARTXN getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpCARTXN instance = (ExpCARTXN) apContext.getBean("expCARTXN");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    

    public static void main(String[] args) {
    	ExpCARTXN expCARTXN = null;
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
            	expCARTXN = getInstance();
            }
            else {
            	expCARTXN = new ExpCARTXN();
            }
            expCARTXN.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            expCARTXN.beforeHandling();
            expCARTXN.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpCARTXN run fail:" + ignore.getMessage(), ignore);
        }
    }
}
