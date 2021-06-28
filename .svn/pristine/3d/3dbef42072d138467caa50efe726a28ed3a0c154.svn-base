/**
 * changelog ExpAppointReloadResult
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
 * ExpAssociator
 * </pre>
 * author:duncan
 */
public class ExpAppointReloadResult extends AbstractExpFile { 
    private static Logger log = Logger.getLogger(ExpAppointReloadResult.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpAppointReloadResult" + File.separator + "spring.xml";
    private static final String HGTOYHDPResult_HG_FN = "HGTOYHDPResult_HG";
    private static final String HGTOYHDPResult_HG_APPEND = "HGTOYHDPResult_HG";
    private static final String SIGN = "|";

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

    public ExpAppointReloadResult() {
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

        /*Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
                		"WHERE MEM_ID = '" + memId + "'"+
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(HGTOYHDPResult_HG_FN) + 
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
        log.info("memId2Seqno:" + memId2Seqno);*/
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
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String seqnoSql = "SELECT MEM_GROUP_ID,MEM_ID,FULL_FILE_NAME,FILE_DATE,SEQNO FROM TB_INCTL " +
        				"WHERE SYS_DATE = '" + batchDate +"' " +
        				"AND FILE_NAME = 'HGTOYHDP_HG' " +
                		"GROUP BY MEM_GROUP_ID,MEM_ID,FULL_FILE_NAME,FILE_DATE,SEQNO";
        try {
        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        	stmt = conn.createStatement();
              log.debug("seqnoSql: "+seqnoSql);
              rs = stmt.executeQuery(seqnoSql);
              while (rs.next()) {
            	  /*String memGroupId = rs.getString(1);   
            	  String memId = rs.getString(2);*/    
            	  String memGroupId = getMemId().substring(0, 5);
            	  String memId = getMemId();
            	  String fullFileName = rs.getString(3);          	  
            	  String fileDate = rs.getString(4);  
            	  String seqno = rs.getString(5);  
                  ExpFileInfo expFileInfo = new ExpFileInfo();
                  expFileInfo.setMemGroupId(memGroupId);
                  expFileInfo.setMemId(memId);
                  expFileInfo.setFileName(HGTOYHDPResult_HG_FN);
                  expFileInfo.setFileDate(fileDate);
                  expFileInfo.setSeqno(seqno);
                  String outFullFileName =expFileInfo.getFileDate() + expFileInfo.getSeqno() + "_" + HGTOYHDPResult_HG_APPEND + ".txt";
                  // memId 若不是同樣長度時會有問題
                  expFileInfo.setFullFileName(outFullFileName);
                  String selectSQL ="SELECT TB_APPOINT_RELOAD.INFILE, TB_APPOINT_RELOAD.PROC_DATE, TB_APPOINT_RELOAD.EXCHANGE_DATE," +
//                  		  "SUBSTR(TB_HG_CARD_MAP.BARCODE1,4,13) AS BARCODE1, "+
                  		  "NVL(SUBSTR(TB_HG_CARD_MAP.BARCODE1,4,13),TB_APPOINT_RELOAD.CARD_NO) AS BARCODE1, "+
                		  "TB_APPOINT_RELOAD.PRODUCT_ID, TB_APPOINT_RELOAD.HG_ORDER_NO, TB_APPOINT_RELOAD.EXCHANGE_SEQNO, "+
                		  "TB_APPOINT_RELOAD_DTL.EXCHANGE_POINT, "+
                		  "TB_APPOINT_RELOAD_DTL.EXCHANGE_AMT, TB_APPOINT_RELOAD.PROC_FLAG "+
                		  "FROM TB_APPOINT_RELOAD, TB_APPOINT_RELOAD_DTL, TB_HG_CARD_MAP "+
                		  "WHERE TB_APPOINT_RELOAD.INFILE='" + fullFileName +"' " +
                		  "AND TB_APPOINT_RELOAD.BONUS_BASE= TB_APPOINT_RELOAD_DTL.BONUS_BASE "+
                		  "AND TB_APPOINT_RELOAD.BALANCE_TYPE= TB_APPOINT_RELOAD_DTL.BALANCE_TYPE "+
                		  "AND TB_APPOINT_RELOAD.BALANCE_ID= TB_APPOINT_RELOAD_DTL.BALANCE_ID "+
                		  "AND TB_APPOINT_RELOAD.CARD_NO= TB_HG_CARD_MAP.CARD_NO(+) "+
                		  "AND TB_APPOINT_RELOAD.AR_SERNO= TB_APPOINT_RELOAD_DTL.AR_SERNO " +
                		  "ORDER BY LINE_NO ";

                  
                  log.debug("selectSQL: " + selectSQL);

                  expFileInfo.setSelectSQL(selectSQL);
                  efs.addExpFileInfo(expFileInfo);
             }
        }
        finally {
              ReleaseResource.releaseDB(conn, stmt, rs);
              return efs;
        }
    }

    public String outputAfterFile() {
        return super.outputAfterFile();
    }

    public String outputBeforeFile() {    	

    	//636
        StringBuffer header = new StringBuffer();
        /*header.append("H0");
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.rightPad("", 616, ' '));*/
        return header.toString();
    }

    public String outputEndFile() {    	
    	//636
        StringBuffer header = new StringBuffer();
        //header.append("/EOF");
        //header.append(StringUtils.rightPad("", 328, ' '));
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

    	String procDate ="";
    	String exchangeDate ="";
    	String cardNo ="";
    	String productId ="";
    	String hg_orderNo ="";
    	String exchangeSeqno ="";
    	String exchangePoint ="";
    	String exchangeAmt ="";
    	String procFlag ="";

    	if (!isBlankOrNull(record.get(1)))
    		procDate = record.get(1).toString();
    	if (!isBlankOrNull(record.get(2)))
    		exchangeDate = record.get(2).toString();
    	if (!isBlankOrNull(record.get(3)))
    		cardNo = record.get(3).toString();
    	if (!isBlankOrNull(record.get(4)))
    		productId = record.get(4).toString();
    	if (!isBlankOrNull(record.get(5)))
    		hg_orderNo = record.get(5).toString();
    	if (!isBlankOrNull(record.get(6)))
    		exchangeSeqno = record.get(6).toString();
	    if (!isBlankOrNull(record.get(7)))
	    	exchangePoint = record.get(7).toString();
	    if (!isBlankOrNull(record.get(8)))
	    	exchangeAmt = record.get(8).toString();
	    if (!isBlankOrNull(record.get(9)))
	    	procFlag = record.get(9).toString();
	    
        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append(procDate).append(SIGN);
        sb.append(exchangeDate).append(SIGN);
        sb.append(cardNo).append(SIGN);
        sb.append(productId).append(SIGN);
        sb.append(hg_orderNo).append(SIGN);
        sb.append(exchangeSeqno).append(SIGN);
        sb.append(exchangePoint).append(SIGN);
        sb.append(exchangeAmt).append(SIGN);
        sb.append(procFlag);
        
        return sb.toString();
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpAppointReloadResult getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpAppointReloadResult instance = (ExpAppointReloadResult) apContext.getBean("expAppointReloadResult");
        return instance;
    }
	
    public static boolean isBlankOrNull(Object value) {
		return (value == null || value.toString().trim().equals(""));
	}
    
    private String takeDecimal( String number, int afterDecimal){
		
    	String afterDecimalNumber = "";
    	if (number.contains(".")){
			int a = number.indexOf(".")+1;
			if ( number.length()-a > 2 )
			{
				afterDecimalNumber = number.substring(0,a-1) + number.substring(a, a+afterDecimal);
			}
			else
			{
			afterDecimalNumber = number.substring(0,a-1) + 
					StringUtils.rightPad(number.substring(a, number.length()),afterDecimal,"0");
			}
		}
		else
		{
			afterDecimalNumber = number ;
				for ( int i=0; i<afterDecimal; i++)
					afterDecimalNumber = afterDecimalNumber + "0" ;
		}
    	
    	return afterDecimalNumber;	
    } 

    public static void main(String[] args) {
    	ExpAppointReloadResult exptAppointReloadResult = null;
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
            	exptAppointReloadResult = getInstance();
            }
            else {
            	exptAppointReloadResult = new ExpAppointReloadResult();
            }
            exptAppointReloadResult.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            exptAppointReloadResult.beforeHandling();
            exptAppointReloadResult.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpAppointReloadResult run fail:" + ignore.getMessage(), ignore);
        }
    }
}
