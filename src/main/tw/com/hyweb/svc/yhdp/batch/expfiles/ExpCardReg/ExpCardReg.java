/**
 * changelog ExpCardReg
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpCardReg;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.DBService;

import tw.com.hyweb.util.BatchUtils;
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
public class ExpCardReg extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpCardReg.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpCardReg" + File.separator + "spring.xml";
    private static final String R_TNS_FN = "R_TNS";
    private static final String R_TNS_APPEND = "R_TNS";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List memIds = null;
    // 卡片 vipFlag
    private List vipFlags = null;
    // key:String(memId), value:String(seqno)
    private HashMap memId2Seqno = new HashMap();
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private Connection Connection = null;
    
    private String memId = "";
    
    public ExpCardReg() {
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
    
    public List getVipFlags() {
		return vipFlags;
	}

	public void setVipFlags(List vipFlags) {
		this.vipFlags = vipFlags;
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
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(R_TNS_FN) + 
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
            expFileInfo.setFileName(R_TNS_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = R_TNS_APPEND + "_" + expFileInfo.getFileDate()+DateUtils.getSystemTime()+".TXT";
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL = 
            		//"SELECT TB_CARD.MBR_REG_DATE,TB_CUST.MERCH_ID, TB_CARD.CARD_NO, TB_CUST.PERSON_ID, TB_CARD.APRV_DATE, TB_CARD.VIP_FLAG "+
            		"SELECT TB_CARD.ACTIVE_DATE, TB_CARD.MBR_REG_DATE,TB_CARD.CARD_OPEN_OWNER, TB_CARD.CARD_NO, TB_CUST.PERSON_ID, TB_CARD.VIP_FLAG "+
            		"FROM TB_CUST, TB_CARD "+
            		"WHERE TB_CARD.CUST_ID = TB_CUST.CUST_ID " +
            		"AND TB_CARD.ACTIVE_DATE <> '00000000' " +
            		"AND TB_CARD.MBR_REG_DATE IS NOT NULL " +
            		"AND TB_CARD.CARD_OPEN_OWNER IS NOT NULL ";
            		
            
            if (StringUtil.isEmpty(System.getProperty("date"))) {
            	selectSQL = selectSQL + "AND TB_CARD.EXP_DATE = '00000000' ";
            }
            else
            	selectSQL = selectSQL + "AND TB_CARD.EXP_DATE = '"+ batchDate +"' ";
            		/*"AND (TB_CARD.ISSUE_DATE ='" + BatchUtil.getSomeDay(batchDate,-1) + "' " +
            		"OR TB_CARD.MBR_REG_DATE ='" + BatchUtil.getSomeDay(batchDate,-1) + "') ";*/
            
            selectSQL = selectSQL + "ORDER BY TB_CARD.MBR_REG_DATE,TB_CUST.MERCH_ID, TB_CARD.CARD_NO";
            
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
        /*header.append("H0");
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));*/
        //header.append(StringUtils.rightPad("", 97, ' '));
        return header.toString();
    }
    
    public String outputEndFile() {    	
    	//383
        StringBuffer header = new StringBuffer();
        header.append("/EOF");
        header.append(StringUtils.rightPad("", 378, ' '));
        return header.toString();
    }

    public void actionsAfterInfo() throws Exception {
        // 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
    	
    	if (expFileResult.getTotalRecords() > 0){
	    	try {
	    		
	    		Connection.commit();
		        Connection.close();
		        
			} catch (SQLException e) {
				
				Connection.rollback();
	    		Connection.close();
	            throw e;
	            
			}
    	}
        super.actionsAfterInfo();
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
        super.actionsAfterFile();
    }

    public String outputOneRecord(List record) throws Exception { 
    	
    	if ( Connection == null || Connection.isClosed()){
    		try {
    			log.info("open Connection.");
    			Connection = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    			Connection.setAutoCommit(false);
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
    	}

    	String activeDate = "00000000";
    	String mbrRegDate = "00000000";
    	String compareDate = "";
    	String merchId = "";
    	String cardNo = "";
    	String personId = "";
    	String vipFlag = "";

    	if (!isBlankOrNull(record.get(0).toString()))
    		activeDate = record.get(0).toString();
    	if (!isBlankOrNull(record.get(1).toString()))
    		mbrRegDate = record.get(1).toString();
    	
    	if ( Integer.valueOf(activeDate) > Integer.valueOf(mbrRegDate) ){
    		compareDate = activeDate;
    	}
    	else{
    		compareDate = mbrRegDate;
    	}
    	
    	if (!isBlankOrNull(record.get(2).toString()))
    		merchId = record.get(2).toString();
    	if (!isBlankOrNull(record.get(3).toString()))
    		cardNo = record.get(3).toString();
    	if (!isBlankOrNull(record.get(4).toString()))
    		personId = BatchUtils.decript(record.get(4).toString());
    	if (!isBlankOrNull(record.get(5).toString()))
    		vipFlag = record.get(5).toString();
    	
    	if ( !vipFlags.contains(vipFlag) )
    		vipFlag = "0";
    	
        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtils.rightPad(mbrRegDate, 8, ' '));
        sb.append(StringUtils.rightPad(merchId, 15, ' '));
        sb.append(StringUtils.rightPad(cardNo, 20, ' '));
        sb.append(StringUtils.rightPad(personId, 10, ' '));
        sb.append(StringUtils.rightPad("",20, ' '));
        sb.append(StringUtils.rightPad("",15, ' '));
        sb.append(StringUtils.rightPad("",15, ' '));
        sb.append(StringUtils.rightPad("",8 , ' '));
        sb.append(StringUtils.rightPad("",50, ' '));
        sb.append(StringUtils.rightPad("",5 , ' '));
        sb.append(StringUtils.rightPad("",60, ' '));
        sb.append(StringUtils.rightPad("",1 , ' '));
        sb.append(StringUtils.rightPad("",1 , ' '));
        //sb.append(StringUtils.rightPad("",1 , ' '));
        sb.append(StringUtils.rightPad("",1 , ' '));
        sb.append(StringUtils.rightPad("",15, ' '));
        sb.append(StringUtils.rightPad("",20, ' '));
        sb.append(StringUtils.rightPad("",10, ' '));
        sb.append(StringUtils.rightPad("",15, ' '));
        sb.append(StringUtils.rightPad("",15, ' '));
        sb.append(StringUtils.rightPad("",50, ' '));
        sb.append(StringUtils.rightPad(compareDate, 8, ' '));
        sb.append(StringUtils.leftPad(vipFlag, 20, ' '));
       
        if (StringUtil.isEmpty(System.getProperty("date")))
        	updateCardExpDate(cardNo);
                        
        return sb.toString();
    }

    public void updateCardExpDate(String cardNo ) throws Exception
    {
        try {
        	String cardSql = "UPDATE TB_CARD SET " +
        			"EXP_DATE = '"+ batchDate +"' " +
            		"WHERE CARD_NO = '"+cardNo+"' ";
        	
        	log.info("cardSql: "+cardSql);
            DBService.getDBService().sqlAction(cardSql.toString(), Connection); 
        }
        catch (Exception e)
        {
        	Connection.rollback();
        	Connection.close();
            throw e;
        } 
    }
    
    public void rollback()  throws Exception {
    	
    	if ( Connection != null && !Connection.isClosed()  ){
    		Connection.rollback();
    		Connection.close();
    	}
    }
    
	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpCardReg getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpCardReg instance = (ExpCardReg) apContext.getBean("expCardReg");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    
    public static void main(String[] args) {
    	ExpCardReg expCardReg = null;
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
            	expCardReg = getInstance();
            }
            else {
            	expCardReg = new ExpCardReg();
            }
            expCardReg.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            expCardReg.beforeHandling();
            expCardReg.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpCardReg run fail:" + ignore.getMessage(), ignore);
        }
    }
}
