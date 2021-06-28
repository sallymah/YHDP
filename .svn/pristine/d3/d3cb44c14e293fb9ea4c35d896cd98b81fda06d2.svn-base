package tw.com.hyweb.svc.yhdp.batch.expfiles;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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

public class ExpBTP3 extends AbstractExpFile {

	private static Logger log = Logger.getLogger(ExpBTP3.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpBTP3" + File.separator + "spring.xml";
    private static final String BTP3_FN = "BTP3";
    private static final String BTP3_APPEND = "BTP3";
	
    private String memId = "";
    private String encoding = "";
    private List memIds = null;
    private HashMap memId2Seqno = new HashMap();
	
    private String autoReloadDate = null; //自動加值日期
    
    
    public ExpBTP3() {}
    
    
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

	public String getAutoReloadDate() {
		return autoReloadDate;
	}

	public void setAutoReloadDate(String autoReloadDate) {
		this.autoReloadDate = autoReloadDate;
	}

	private void beforeHandling() throws SQLException 
	{
		this.setAutoReloadDate(this.minusDays(batchDate, 1));
		
		Connection conn = null;
    	Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        StringBuffer sql = new StringBuffer();
        sql.append("select BANK_ID from TB_CARD ");
//		sql.append("where AUTO_RELOAD_DATE = ");
//		sql.append(StringUtil.toSqlValueWithSQuote(this.getAutoReloadDate())).append(" ");
//		sql.append("and AUTO_RELOAD_FLAG = ").append(StringUtil.toSqlValueWithSQuote("Y")).append(" ");
		sql.append("Group by BANK_ID ");
		log.info(sql);
    	try {
    		conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    		stmt = conn.createStatement();
    		rs = stmt.executeQuery(sql.toString());
    		while (rs.next()) {
    			seqno = "01";
    			Statement stmt1 = null;
    			ResultSet rs1 = null;
    			String bankId = rs.getString(1);
    			String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
    							" WHERE MEM_ID = " + StringUtil.toSqlValueWithSQuote(bankId) + " " +
	                      		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(BTP3_FN) + " " +
	                      		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
    			log.info(seqnoSql);
    			try {
    				stmt1 = conn.createStatement();
    				rs1 = stmt1.executeQuery(seqnoSql);
    				while (rs1.next()) {
    					seqno = rs1.getString(1);
    				}
    			} catch (SQLException ignore) {
    	    		log.warn("SQLException:" + ignore.getMessage(), ignore);
    	    	} finally {
    				ReleaseResource.releaseDB(null, stmt1, rs1);
    			}
    			memId2Seqno.put(bankId, seqno);       
    			memIds.add(bankId);
    		}
    	} catch (SQLException ignore) {
    		log.warn("SQLException:" + ignore.getMessage(), ignore);
    	} finally {
    		ReleaseResource.releaseDB(conn, stmt, rs);
    	}
        log.info("memIds:" + memIds);
        log.info("memId2Seqno:" + memId2Seqno);
	}
	
	@Override
	public ExpFileSetting makeExpFileSetting() 
	{
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
            expFileInfo.setMemId(StringUtils.leftPad(memId, 8, '0'));
            expFileInfo.setFileName(BTP3_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = BTP3_APPEND + "." + expFileInfo.getMemId() + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL = 
            		"Select CARD_NO from TB_CARD " + 
            		"where AUTO_RELOAD_DATE = " + StringUtil.toSqlValueWithSQuote(this.getAutoReloadDate()) + " " +
            		"and AUTO_RELOAD_FLAG = " + StringUtil.toSqlValueWithSQuote("Y") + " " +
            		"and CARD_NO in " +
            		"(select CARD_NO from TB_CARD where BANK_ID = " + StringUtil.toSqlValueWithSQuote(memId) + ") ";
            log.info(selectSQL);
            expFileInfo.setSelectSQL(selectSQL);
            efs.addExpFileInfo(expFileInfo);
        }
        return efs;
	}

	public String outputBeforeFile() 
	{
    	StringBuffer header = new StringBuffer();
        header.append("H0");
        header.append(this.getBatchDate());
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.rightPad("", 10, ' '));
        return header.toString();
	}
	
	@Override
	public String outputOneRecord(List record) throws Exception 
	{
		String cardNo = "";
		
		if (!isBlankOrNull(record.get(0).toString())) cardNo = record.get(0).toString().trim();
		
		StringBuffer sb = new StringBuffer();
    	sb.append("DT");
        sb.append(StringUtils.rightPad(cardNo, 20, ' '));
        sb.append(StringUtils.rightPad("1", 1, ' '));
        sb.append(StringUtils.rightPad(" ", 1, ' '));
        sb.append(StringUtils.leftPad("0000", 4, '0'));
		return sb.toString();
	}

	@Override
	public List outputDtlRecord(List record) 
	{
		return null;
	}
	
	@Override
	public void actionsAfterFile() throws Exception 
    {
		// 處理完一個檔案後要做什麼事, default 不做任何事
		super.actionsAfterFile();
    }
	
	@Override
	public void actionsAfterInfo() throws Exception 
	{
		// 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
    }
	
	/**
	 * Subtracts the specified amount of day
	 * @param date(yyyyMMdd)
	 * @param days amount of days
	 * @return date(yyyyMMdd)
	 */
	private String minusDays(String date, int days) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    	Calendar cal = Calendar.getInstance();
    	try {
    		cal.setTime(sdf.parse(date));
		} catch (ParseException e) {
			log.warn(e.getMessage());
		}
    	cal.add(Calendar.DAY_OF_YEAR, -days);
    	return sdf.format(cal.getTime());
	}
	
	/**
	 * Returns true if null or empty
	 * @param value String
	 * @return true if null or empty, otherwise false
	 */
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    
    public static ExpBTP3 getInstance() 
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpBTP3 instance = (ExpBTP3) apContext.getBean("expBTP3");
        return instance;
    }
	
	public static void main(String[] args) 
	{
		ExpBTP3 expBTP3 = null;
        try {
            String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            } else if (!DateUtil.isValidDate(batchDate)) {
                log.info("invalid batchDate('" + batchDate + "') using system date!");
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            String memId = System.getProperty("memid");
            if (StringUtil.isEmpty(memId)) memId = "";
            
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	expBTP3 = getInstance();
            }
            else {
            	expBTP3 = new ExpBTP3();
            }
            expBTP3.setBatchDate(batchDate);
            expBTP3.setMemId(memId);
            // 註: 此 method 一定要先呼叫
            expBTP3.beforeHandling();
            expBTP3.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpBTP3 run fail:" + ignore.getMessage(), ignore);
        }
	}
}
