package  tw.com.hyweb.svc.yhdp.batch.expfiles.ExpAffComBlock;

/**
 * changelog ExpTeff
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.FF11;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.FF21;
import tw.com.hyweb.svc.yhdp.batch.util.BatchUtils;
import tw.com.hyweb.util.ISOUtil;
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
public class ExpAffComBlock extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpAffComBlock.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpAffComBlock" + File.separator + "spring.xml";
    private static final String CARDLOCK_FN = "CARDLOCK";
    private static final String CARDLOCK_APPEND = "CARDLOCK";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List groupIds = null;
    // key:String(memId), value:String(seqno)
    private HashMap groupId2Seqno = new HashMap();
    // key:String(memId), value:String(memGroupId)    
    private HashMap memId2MemGroupId = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private String memId = "";
    private double total=0;
    
    private String configFile;

    
    public ExpAffComBlock() {

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
    public List getGroupIds() {
        return groupIds;
    }
    public void setGroupIds(List groupIds) {
        this.groupIds = groupIds;
    }
    public String getMemId() {
		return memId;
	}
	public void setMemId(String memId) {
		this.memId = memId;
	}
    public String getConfigFile() {
		return configFile;
	}
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	private void beforeHandling() throws SQLException {

    	Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    	Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        StringBuffer sql = new StringBuffer();

    	try {
    		sql.append("SELECT HG_CARD_GROUP_ID  FROM TB_HG_CARD_GROUP WHERE BLOCK_CARD_FILE_FLAG='1'");
    		
    		stmt = conn.createStatement();
    		log.warn("sql: "+sql);
    		rs = stmt.executeQuery(sql.toString());
    		while (rs.next()) {
    			
    			seqno = "01";
    			Statement stmt1 = null;
    			ResultSet rs1 = null;
    			String groupId= rs.getString(1);
    			
    			String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
    							"WHERE MEM_ID = '" +  groupId + "'"+
	                      		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(CARDLOCK_FN) + 
	                      		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
    			try {
    				stmt1 = conn.createStatement();
    				log.warn("seqnoSql: "+seqnoSql);
    				rs1 = stmt1.executeQuery(seqnoSql);
    				while (rs1.next()) {
    					seqno = rs1.getString(1);
    				}
    			}
    			finally {
    				ReleaseResource.releaseDB(null, stmt1, rs1);
    			}
    			//log.warn("memIds="+memIds);
    			
    			groupId2Seqno.put(groupId , seqno);       
    			groupIds.add(groupId);
    			
    		}
    	}
    	finally {
    		ReleaseResource.releaseDB(conn, stmt, rs);
    	}
        log.info("groupIds:" + groupIds);
        log.info("groupId2Seqno:" + groupId2Seqno);
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
        for (int i = 0; i < groupIds.size(); i++) {
            String groupId = (String) groupIds.get(i);
            String seqno = (String) groupId2Seqno.get(groupId);
            //Integer count = (Integer) memId2Count.get(memId);
            if (seqno == null) {
                log.warn("can't find in memId2Seqno or memId2Count, ignore for '" + groupId + "'");
                continue;
            }
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemId(groupId);
            expFileInfo.setFileName(CARDLOCK_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
           
            // memId 若不是同樣長度時會有問題
            String fullFileName = CARDLOCK_APPEND + "." +groupId + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL =
            		" SELECT CARD_NO,REG_DATE,REG_TIME FROM TB_BLACKLIST_SETTING "+
    				"  WHERE EXISTS  (SELECT 1 FROM TB_HG_CARD_MAP WHERE"+
    				"  HG_CARD_GROUP_ID = '"+groupId+"' "+
    				"  AND TB_BLACKLIST_SETTING.CARD_NO=TB_HG_CARD_MAP.CARD_NO)"+
    				"  AND REG_DATE =  '"+DateUtil.addDate(batchDate, -1)+"' ";
            
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

    	//38
		 StringBuffer header = new StringBuffer();
	     header.append("H0");
	     header.append(batchDate);
	     header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
	     header.append(StringUtils.rightPad("", 18, ' '));
	     return header.toString();
    }
    
    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
        super.actionsAfterFile();
    }

    public String outputOneRecord(List record) throws Exception { 
    	String cardNo = "";
		String regDate = "";
		String regTime= ""; 
		
		if (!isBlankOrNull(record.get(0).toString()))
			cardNo = record.get(0).toString();
		if (!isBlankOrNull(record.get(1).toString()))
			regDate = record.get(1).toString();
		if (!isBlankOrNull(record.get(2).toString()))
			regTime = record.get(2).toString();	
	
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(cardNo, 20, ' '));
        sb.append(StringUtils.rightPad(regDate, 8, ' '));
        sb.append(StringUtils.rightPad(regTime, 6, ' '));

		return sb.toString();

    }

    public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
	public static ExpAffComBlock getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpAffComBlock instance = (ExpAffComBlock) apContext.getBean("expAffComBlock");
        return instance;
    }
	

    public static void main(String[] args) {
    	ExpAffComBlock expTeff = null;
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
            	expTeff = getInstance();
            	expTeff.setBatchDate(batchDate);
                // 註: 此 method 一定要先呼叫
                expTeff.beforeHandling();
                expTeff.run(args);
            }
            else {
            	log.error(SPRING_PATH + " is not exist. ");
            }
            
        }
        catch (Exception ignore) {
            log.warn("ExpAffComBlock run fail:" + ignore.getMessage(), ignore);
        }
    }
}
