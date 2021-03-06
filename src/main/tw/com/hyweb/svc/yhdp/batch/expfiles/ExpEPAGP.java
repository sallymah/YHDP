/**
 * changelog ExpFTCBData
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

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
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre> 
 * ExpFTCB
 * </pre>
 * author:
 */
public class ExpEPAGP extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpEPAGP.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpEPAGP" + File.separator + "spring.xml";
    private static final String FILE_FN = "EPAGP";
    private static final String FILE_APPEND = "EPAGP";
    
    private String sysDate = DateUtils.getSystemDate();
    private String sysTime = DateUtils.getSystemTime();
    
    private String fullFileName = "";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List memIds = null;
    // 要匯出會員檔的 bank
    private List bankIds = null;
    // key:String(bankId), value:String(seqno)
    private HashMap bankId2Seqno = new HashMap();
    // key:String(bankId), value:String(memId)
    private HashMap bankId2MemId = new HashMap();
    // key:String(memId), value:String(seqno)
    private HashMap memId2Seqno = new HashMap();
	// key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    private HashMap memId2Count = new HashMap();
    private HashMap memId2MemGroupId = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private HashMap key2SationIn = new HashMap();
    private HashMap key2SationOut = new HashMap();
    private Vector transStationInfo = null;
    private List pCodes = null;
    

	private String memId = "";
    
    public ExpEPAGP() {
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
    public List getBankIds() {
		return bankIds;
	}
	public void setBankIds(List bankIds) {
		this.bankIds = bankIds;
	}
    public List getpCodes() {
		return pCodes;
	}
	public void setpCodes(List pCodes) {
		this.pCodes = pCodes;
	}
	public String getStringPCodes() {
		String sPCodes = "";
		
		for(int i = 0; i < pCodes.size(); i++){
			sPCodes = sPCodes + StringUtil.toSqlValueWithSQuote(pCodes.get(i).toString());
			if (i+1 < pCodes.size()){
				sPCodes = sPCodes + ", ";
			}
		}
		
		return sPCodes;
	}

	private void beforeHandling() throws Exception 
    {

        Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
                		"WHERE MEM_ID = '" + memId + "'"+
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(FILE_FN) + 
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
        
		String settleSql = "SELECT STATION_CODE,TRANS_SYS_NO,LOC_ID,IO_STATUS,DESCRIPTION "
				+ "FROM TB_TRANS_STATION ";

		transStationInfo = BatchUtil.getInfoListHashMap(settleSql.toString());
		
		for (int i = 0; i < transStationInfo.size(); i++){			
			HashMap info = (HashMap) transStationInfo.get(i);
			if(info.get("IO_STATUS").equals("I")){
				List key = new ArrayList();
				key.add(info.get("STATION_CODE"));
				key.add(info.get("TRANS_SYS_NO"));
				key.add(info.get("LOC_ID"));				
				key2SationIn.put(key, info);
			}
			else{
				List key = new ArrayList();
				key.add(info.get("STATION_CODE"));
				key.add(info.get("TRANS_SYS_NO"));
				key.add(info.get("LOC_ID"));		
				key2SationOut.put(key, info);
			}
			
		}
		
    }
    
    private String getFileSeqNo(Connection conn, String memId) throws Exception 
    {
    	String seqno = "01";
    	Statement stmt = null;
        ResultSet rs = null;
        
    	String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL" +
						" WHERE MEM_ID = '" + memId + "'"+
						" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(FILE_FN) + 
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
            ReleaseResource.releaseDB(null, stmt, rs);
		}
    	return seqno;
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
            expFileInfo.setFileName(FILE_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = FILE_APPEND + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno() ;
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL = "SELECT ONL.CARD_NO,ONL.ATC,ONL.TXN_DATE,ONL.TXN_TIME,ONL.TXN_AMT,MEM.MEM_ID,MEM.MEM_NAME," +
 				   "TXN.TXN_TYPE,TXN.IN_STATION_CODE,TXN.OUT_STATION_CODE,TXN.TRANS_SYS_NO,TXN.LOC_ID," +
 				   "ONL.CHIP_POINT1_AFTER, TXN.BUS_LINCENSE_ID, TXN.BUS_ROUTE_DOMAN " +
 				   "FROM TB_ONL_TXN ONL, TB_TRAFFIC_TXN TXN, TB_MEMBER MEM " +
 				   "WHERE ONL.IMP_FILE_DATE= '"+batchDate+"' " +
 				   "AND ONL.CARD_NO = TXN.CARD_NO " +
 				   "AND ONL.LMS_INVOICE_NO = TXN.LMS_INVOICE_NO " +
 				   "AND ONL.ACQ_MEM_ID = MEM.MEM_ID " +
 				   "AND ONL.TXN_AMT<> 0 " +
 				   "AND EXISTS (SELECT 1 FROM TB_EPA_GP_CARD WHERE EPA_GP_STATUS = '1' AND CARD_NO=ONL.CARD_NO)";
            if (pCodes.size() > 0){
            	selectSQL = selectSQL + 
            			"AND ONL.P_CODE IN (" + getStringPCodes() + ")";
            }

            
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
        header.append("H");
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(batchDate);
        header.append(sysTime);
        header.append(StringUtils.rightPad("", 290, ' '));
        return header.toString();
    }
    
    public String outputEndFile() 
    {    
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

    public String outputOneRecord(List record) throws Exception 
    { 
    	String cardNo = "";
    	String atc = "";
    	String txnDate = "";
    	String txnTime = "";
    	String txnAmt = "";
    	String memId = "";
    	String memName = "";
    	String txnType ="";
    	String inStationCode = "";
    	String outStationCode = "";
    	String transSysNo="";
    	String locId="";
    	String chipPoint1After = "";
    	String busLincenceId = "";
    	String busRoadDoman = "";
    	
    	cardNo = record.get(0).toString();
    	atc = record.get(1).toString();
    	if (!isBlankOrNull(record.get(2).toString()))
    		txnDate = record.get(2).toString();
    	if (!isBlankOrNull(record.get(3).toString()))
    		txnTime = record.get(3).toString();
    	if (!isBlankOrNull(record.get(4).toString()))
    		txnAmt = record.get(4).toString();
    	if (!isBlankOrNull(record.get(5).toString()))
    		memId = record.get(5).toString();
    	if (!isBlankOrNull(record.get(6).toString()))
    		memName = record.get(6).toString();
    	if (!isBlankOrNull(record.get(7).toString()))
    		txnType = record.get(7).toString();
    	if (!isBlankOrNull(record.get(8).toString()))
    		inStationCode = record.get(8).toString();
    	if (!isBlankOrNull(record.get(9).toString()))
    		outStationCode = record.get(9).toString();
    	if (!isBlankOrNull(record.get(10).toString()))
    		transSysNo = record.get(10).toString();
    	if (!isBlankOrNull(record.get(11).toString()))
    		locId = record.get(11).toString();
    	if (!isBlankOrNull(record.get(12).toString()))
    		chipPoint1After = record.get(12).toString();
    	if (!isBlankOrNull(record.get(13).toString()))
    		busLincenceId = record.get(13).toString();
    	if (!isBlankOrNull(record.get(14).toString()))
    		busRoadDoman = record.get(14).toString();
		
			
		
		if(txnType.equals("01")){
    		;
    	}else{
    		List keyI = new ArrayList<>();
    		keyI.add(inStationCode);
			keyI.add(transSysNo);
			keyI.add(locId);
			HashMap infoI = (HashMap) key2SationIn.get(keyI);
    		
			List keyO = new ArrayList<>();
			keyO.add(outStationCode);
			keyO.add(transSysNo);
			keyO.add(locId);
			HashMap infoO = (HashMap)key2SationOut.get(keyO);
			
			if(infoI != null){
				if (!infoI.isEmpty()){
					inStationCode= infoI.get("DESCRIPTION").toString();
				}
			}
			if(infoO != null){
				if(!infoO.isEmpty()){
					outStationCode= infoO.get("DESCRIPTION").toString();
				}
			}
    	}    		
    	
        StringBuffer sb = new StringBuffer();
        sb.append("D0");
        sb.append(StringUtils.rightPad(cardNo, 20, ' '));
        sb.append(StringUtils.rightPad(atc, 8, ' '));
        sb.append(StringUtils.rightPad(txnDate, 8, ' '));
        sb.append(StringUtils.rightPad(txnTime, 6, ' '));
        sb.append(StringUtils.leftPad(takeDecimal(txnAmt, 0), 10, '0'));
        sb.append(StringUtils.rightPad(memId, 8, ' '));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(memName, 105, ' '),encoding,105));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(inStationCode, 60, ' '),encoding,60));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(outStationCode, 60, ' '),encoding,60));
        sb.append(StringUtils.leftPad(takeDecimal(chipPoint1After, 0), 10, '0'));
        sb.append(StringUtils.rightPad(busLincenceId, 10, ' '));
        sb.append(StringUtils.rightPad(busRoadDoman, 6, ' '));
        
       
        
        return sb.toString();
    }
	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpEPAGP getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpEPAGP instance = (ExpEPAGP) apContext.getBean("expEPAGP");
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
    	ExpEPAGP expMerchData = null;
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
            	expMerchData = new ExpEPAGP();
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
