/**
 * changelog ExpTrnBlklist
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTrnBlklist;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.service.db.DBService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <pre> 
 * ExpTrnBlklist
 * </pre>
 * author:duncan
 */
public class ExpTrnBlklist extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpTrnBlklist.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpTrnBlklist" + File.separator + "spring.xml";
    private static final String BLKLIST_FN = "BLKLIST";
    private static final String BLKLIST_APPEND = "BLKLIST";
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
    
    public ExpTrnBlklist() {
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
        
        Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
                		"WHERE MEM_ID = '" + memId + "'"+
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(BLKLIST_FN) + 
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
        efs.setTempFilePending(".tmp");
        // recordsPerFlush:int:-1
        // 產生檔案時處理多少筆記錄後做一次 flush 動作, <= 0 時不 enable flush 動作, default is -1
        efs.setRecordsPerFlush(-1);
        // recordsPerFile:int:-1
        // 多少筆產生一個 file, default 全部一個檔案(<= 0)
        // 若有設 recordsPerFile, 每個 expFileInfo 的 seqnoStart, seqnoEnd 一定要給
        efs.setRecordsPerFile(-1);
        //efs.setLineSeparator("\r\n");
        //20151224刪除換行符號
        efs.setLineSeparator("");
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
            expFileInfo.setFileName(BLKLIST_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            //String fullFileName = BLKLIST_APPEND + "." + expFileInfo.getFileDate();
            String fullFileName = BLKLIST_APPEND + batchDate + StringUtils.leftPad(expFileInfo.getSeqno(),4,'0') + "." + EXTEND_FILE_NAME;
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL = "SELECT MIFARE_UL_UID, REG_DATE FROM("+
            		"SELECT CARD_NO, MIFARE_UL_UID, REG_DATE FROM( "+
            		"SELECT CARD_NO, BLACKLIST_CODE, BLOCK_DATE, MIFARE_UL_UID, REG_DATE, APRV_USERID FROM( "+
            		"SELECT CARD_NO, BLACKLIST_CODE, BLOCK_DATE, MIFARE_UL_UID, REG_DATE, APRV_USERID FROM( "+
            		"SELECT CARD_NO, BLACKLIST_CODE, BLOCK_DATE, MIFARE_UL_UID, REG_DATE, APRV_USERID FROM( "+
            		"SELECT CARD_NO, BLACKLIST_CODE, BLOCK_DATE, MIFARE_UL_UID, REG_DATE, APRV_USERID FROM( "+
            		"SELECT CARD_NO, BLACKLIST_CODE, BLOCK_DATE, MIFARE_UL_UID, REG_DATE, APRV_USERID FROM( "+
            		"SELECT A1.CARD_NO,BLACKLIST_CODE,B1.MIFARE_UL_UID,B1.REG_DATE , '' AS BLOCK_DATE, '' AS APRV_USERID FROM ( "+
            		"SELECT TB_BLACKLIST_VER.CARD_NO, TB_BLACKLIST_VER.BLACKLIST_CODE, BLOCK_DATE, REG_DATE FROM TB_BLACKLIST_VER,TB_BLACKLIST_SETTING "+
            		"WHERE VERNO = (SELECT MAX(VERNO)CARD_NO FROM TB_BLACKLIST_VER) AND TB_BLACKLIST_SETTING.CARD_NO = TB_BLACKLIST_VER.CARD_NO)A1, "+
            		"(SELECT CARD_NO, MIFARE_UL_UID,REG_DATE  FROM TB_CARD "+
            		"WHERE MIFARE_UL_UID IS NOT NULL "+
            		"AND LENGTH(MIFARE_UL_UID)=8 "+
            		"AND MIFARE_UL_UID <>'00000000' )B1 "+
            		"WHERE A1.CARD_NO = B1.CARD_NO )"+
            		"WHERE BLACKLIST_CODE <> '03' ) "+
            		"WHERE BLOCK_DATE IS NULL AND REG_DATE >= " + DateUtil.addYear(batchDate, -2) + ")"+  
            		"WHERE BLACKLIST_CODE <> '04' OR (BLACKLIST_CODE = '04' AND REG_DATE >= " + DateUtil.addDate(batchDate, -7) +")  )B "+
            		"WHERE ( BLACKLIST_CODE <> '01' OR  APRV_USERID  <> 'CRM')  "+
            		"OR (BLACKLIST_CODE = '01' AND  APRV_USERID  = 'CRM' AND (CARD_NO NOT IN( SELECT CARD_NO FROM TB_CARD_BAL WHERE TB_CARD_BAL.CARD_NO= B.CARD_NO AND CR_BONUS_QTY - DB_BONUS_QTY + BAL_BONUS_QTY = 0) "+
            		"OR REG_DATE >= " + DateUtil.addYear(batchDate, -1) +"))) "+
            		"WHERE BLACKLIST_CODE <> '05' OR (BLACKLIST_CODE = '05' AND REG_DATE >=" + DateUtil.addYear(batchDate, -3) +") )A "+
            		"WHERE NOT EXISTS (SELECT * FROM TB_BAL_TRANSFER_DTL WHERE A.CARD_NO = TB_BAL_TRANSFER_DTL.ORIG_BALANCE_ID AND TB_BAL_TRANSFER_DTL.ORIG_BALANCE_ID LIKE '9%' AND BONUS_QTY = 0) "+
            		"OR (EXISTS (SELECT * FROM TB_BAL_TRANSFER_DTL WHERE A.CARD_NO = TB_BAL_TRANSFER_DTL.ORIG_BALANCE_ID AND TB_BAL_TRANSFER_DTL.ORIG_BALANCE_ID LIKE '9%' AND BONUS_QTY = 0) AND REG_DATE >=  " + DateUtil.addYear(batchDate, -1) + ") "+
            		"AND ROWNUM <= 100000 ORDER BY REG_DATE DESC) "+
            		"ORDER BY CARD_NO ASC";
            
            if(i==0){
            	log.info(selectSQL);
            }
            expFileInfo.setSelectSQL(selectSQL);
            efs.addExpFileInfo(expFileInfo);
        }
        
        return efs;
    }

    public byte[] outputAfterFile() {
        return super.outputAfterFile();
    }

    public byte[] outputBeforeFile() throws Exception {    	

        StringBuffer header = new StringBuffer();
        header.append("01");
        header.append(batchDate + time);
        header.append(StringUtils.leftPad(expFileInfo.getSeqno(),6,'0'));
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords()+1, 8));
        //20151224 刪除該欄位 保留總比數(含檔頭)
        //header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append("00");
        
        String unixTime = BatchUtils.getUnixTime(batchDate+time);
        
        List<byte[]> byteList = new ArrayList();
    	
    	byteList.add(header.toString().getBytes());
    	byteList.add(BatchUtils.reverseByte(BatchUtils.convertHexToString(Integer.toHexString(Integer.parseInt(unixTime)))));
    	
    	int len = 0;
    	for (byte[] srcArray : byteList)
			len += srcArray.length;

		byte[] macArray = new byte[len];

		int destLen = 0;

		for (byte[] srcArray : byteList) {
			System.arraycopy(srcArray, 0, macArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
    	
        return macArray;
    }
    
    @Override
	public byte[] outputDecryptBeforeFile() throws Exception {
    	
    	StringBuffer header = new StringBuffer();
        header.append("01");
        header.append(batchDate + time);
        header.append(StringUtils.leftPad(expFileInfo.getSeqno(),6,'0'));
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords()+1, 8));
        //20151224 刪除該欄位 保留總比數(含檔頭)
        //header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        
        //xor
        byte[] xorData = new byte[1];
        xorData[0] = BatchUtils.encryptForImp(expFileInfo);
        log.debug("ISOUtil.hexString(xorData): "+ISOUtil.hexString(xorData));
        header.append(ISOUtil.hexString(xorData));
        
        String unixTime = BatchUtils.getUnixTime(batchDate+time);
        log.debug("unixTime: "+unixTime);
        List<byte[]> byteList = new ArrayList();
    	
    	byteList.add(header.toString().getBytes());
    	byteList.add(BatchUtils.reverseByte(BatchUtils.convertHexToString(Integer.toHexString(Integer.parseInt(unixTime)))));
    	
    	int len = 0;
    	for (byte[] srcArray : byteList)
			len += srcArray.length;

		byte[] macArray = new byte[len];

		int destLen = 0;

		for (byte[] srcArray : byteList) {
			System.arraycopy(srcArray, 0, macArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
    	
        return macArray;
	}
    
    public byte[] outputEndFile() {    	
        return new byte[0];
    }

    public void actionsAfterInfo() throws Exception {
        // 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
        super.actionsAfterFile();
    }

    public byte[] outputOneRecord(List record) { 

    	String mifareUlUid = "";
    	mifareUlUid = record.get(0).toString();

        List<byte[]> byteList = new ArrayList();
    	
    	byteList.add("10".getBytes());
    	byteList.add(BatchUtils.convertHexToString(mifareUlUid));
    	byteList.add("0".getBytes());
    	
    	int len = 0;
    	for (byte[] srcArray : byteList)
			len += srcArray.length;

		byte[] macArray = new byte[len];

		int destLen = 0;

		for (byte[] srcArray : byteList) {
			System.arraycopy(srcArray, 0, macArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
    	
        return macArray;
        
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpTrnBlklist getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpTrnBlklist instance = (ExpTrnBlklist) apContext.getBean("expTrnBlklist");
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
    	ExpTrnBlklist expTrnBlklist = null;
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
            	expTrnBlklist = getInstance();
            }
            else {
            	expTrnBlklist = new ExpTrnBlklist();
            }
            expTrnBlklist.setBatchDate(batchDate);
            expTrnBlklist.setTime(DateUtil.getTodayString().substring(8, 14));
            // 註: 此 method 一定要先呼叫
            expTrnBlklist.beforeHandling();
            expTrnBlklist.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpTrnBlklist run fail:" + ignore.getMessage(), ignore);
        }
    }

}
