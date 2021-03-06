/**
 * changelog ExpMerchData
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
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
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
public class ExpMerchData extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpMerchData.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpMerchData" + File.separator + "spring.xml";
    private static final String MERCH_FN = "MERCH";
    private static final String MERCH_APPEND = "MERCH";

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
    
    public ExpMerchData() {
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
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(MERCH_FN) + 
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
        efs.setCheckFlag(true);
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
            expFileInfo.setFileName(MERCH_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = MERCH_APPEND + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL ="SELECT MERCH.MERCH_ID,MERCH.MEM_ID,MERCH.BUS_ID_NO,MERCH.MERCH_LOC_NAME,MERCH.MERCH_ENG_NAME,"+
            		"MERCH.MERCH_ABBR_NAME,MERCH.CONTACT,MERCH.TEL,MERCH.FAX,MERCH.CITY,MERCH.ZIP_CODE,MERCH.ADDRESS,"+
            		"MERCH.EMAIL,MERCH.EFFECTIVE_DATE,MERCH.RLD_MAX_AMT,MERCH.RLD_CHK_FLAG,MEM.INDUSTRY_ID,"+
            		"MERCH.COUNTRY_CODE,MERCH.UD1,MERCH.UD2,MERCH.UD3,MERCH.UD4,MERCH.UD5 FROM TB_MERCH MERCH, TB_MEMBER MEM " +
					"WHERE MERCH.MEM_ID = MEM.MEM_ID " + 
            		"AND MERCH.APRV_DATE ="+ StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-1));
            
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

    	//747
        StringBuffer header = new StringBuffer();
        header.append("H0");
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.rightPad("", 727, ' '));
        return header.toString();
    }
    
    public String outputEndFile() {    	
    	//747
        StringBuffer header = new StringBuffer();
        header.append("/EOF");
        header.append(StringUtils.rightPad("", 741, ' '));
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

    	String merchId = "";
    	String memId = "";
    	String busIdNo = "";
    	String merchLocName = "";
    	String merchEngName = "";
    	String merchAbbrName = "";
    	String contact = "";
    	String tel = "";
    	String fax = "";
    	String city = "";
    	String zipCode = "";
    	String address = "";
    	String email = "";
    	String effectiveDate = "";
    	String rldMaxAmt = "";
    	String rldChkFlag = "";
    	String industryId = "";
    	String countryCode = "";
    	String ud1 = "";
    	String ud2 = "";
    	String ud3 = "";
    	String ud4 = "";
    	String ud5 = "";

    	merchId = record.get(0).toString();
    	memId = record.get(1).toString();
    	if (!isBlankOrNull(record.get(2).toString()))
    		busIdNo = record.get(2).toString();
    	if (!isBlankOrNull(record.get(3).toString()))
	    	merchLocName = record.get(3).toString();
    	if (!isBlankOrNull(record.get(4).toString()))
	    	merchEngName = record.get(4).toString();
    	if (!isBlankOrNull(record.get(5).toString()))
	    	merchAbbrName = record.get(5).toString();
    	if (!isBlankOrNull(record.get(6).toString()))
	    	contact = record.get(6).toString();
    	if (!isBlankOrNull(record.get(7).toString()))
	    	tel = record.get(7).toString();
    	if (!isBlankOrNull(record.get(8).toString()))
	    	fax = record.get(8).toString();
    	if (!isBlankOrNull(record.get(9).toString()))
	    	city = record.get(9).toString();
    	if (!isBlankOrNull(record.get(10).toString()))
	    	zipCode = record.get(10).toString();
    	if (!isBlankOrNull(record.get(11).toString()))
	    	address = record.get(11).toString();
    	if (!isBlankOrNull(record.get(12).toString()))
	    	email = record.get(12).toString();
    	if (!isBlankOrNull(record.get(13).toString()))
	    	effectiveDate = record.get(13).toString();
    	rldMaxAmt = record.get(14).toString();
    	rldChkFlag = record.get(15).toString();
    	if (!isBlankOrNull(record.get(16).toString()))
    		industryId = record.get(16).toString();
    	if (!isBlankOrNull(record.get(17).toString()))
	    	countryCode = record.get(17).toString();
    	if (!isBlankOrNull(record.get(18).toString()))
	    	ud1 = record.get(18).toString();
    	if (!isBlankOrNull(record.get(19).toString()))
	    	ud2 = record.get(19).toString();
    	if (!isBlankOrNull(record.get(20).toString()))
	    	ud3 = record.get(20).toString();
    	if (!isBlankOrNull(record.get(21).toString()))
	    	ud4 = record.get(21).toString();
    	if (!isBlankOrNull(record.get(22).toString()))
	    	ud5 = record.get(22).toString();

        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(merchId, 15, ' '));
        sb.append(StringUtils.rightPad(memId, 8, ' '));
        sb.append(StringUtils.rightPad(busIdNo, 40, ' '));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(merchLocName, 40, ' '),encoding, 40));
        sb.append(StringUtils.rightPad(merchEngName, 60, ' '));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(Layer2Util.getMaxString(StringUtils.rightPad(merchAbbrName, 20, ' '),encoding, 20), 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(contact, 20, ' '),encoding, 20));
        sb.append(StringUtils.rightPad(tel, 20, ' '));
        sb.append(StringUtils.rightPad(fax, 20, ' '));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(city, 20, ' '),encoding, 20));
        sb.append(StringUtils.rightPad(zipCode, 20, ' '));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(address, 300, ' '),encoding, 300));
        sb.append(StringUtils.rightPad(email, 30, ' '));
        sb.append(StringUtils.rightPad(effectiveDate, 8, ' '));
        sb.append(StringUtils.leftPad(takeDecimal(rldMaxAmt,2), 14, '0'));
        sb.append(StringUtils.rightPad(rldChkFlag, 1, ' '));
        sb.append(StringUtils.rightPad(industryId, 4, ' '));
        sb.append(StringUtils.rightPad(countryCode, 3, ' '));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud1, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud2, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud3, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud4, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud5, 20, ' '),encoding, 20));
        
        
        return sb.toString();
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpMerchData getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpMerchData instance = (ExpMerchData) apContext.getBean("expMerchData");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
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
    	ExpMerchData expMerchData = null;
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
            	expMerchData = getInstance();
            }
            else {
            	expMerchData = new ExpMerchData();
            }
            expMerchData.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            expMerchData.beforeHandling();
            expMerchData.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpMerchData run fail:" + ignore.getMessage(), ignore);
        }
    }
}
