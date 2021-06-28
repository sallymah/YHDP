/**
 * changelog ExpSTANData
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

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbMerchMgr;
import tw.com.hyweb.svc.yhdp.batch.expfiles.beans.ExpDataInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre> 
 * ExpSTAN
 * </pre>
 * author:
 */
public class ExpSTAN extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpSTAN.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpSTAN" + File.separator + "spring.xml";
    private static final String FILE_FN = "STAN";
    private static final String FILE_APPEND = "STAN";
    
    private String sysDate = DateUtils.getSystemDate();
    private String sysTime = DateUtils.getSystemTime();
    
    private String prviousDate = "";
    
    private String fullFileName = "";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 bank
    private List<String> bankIds = null;
    // key:String(bankId), value:String(seqno)
    private HashMap<String, String> bankId2Seqno = new HashMap<String, String>();
    // key:String(bankId), value:String(memId)
    private HashMap<String, String> bankId2MemId = new HashMap<String, String>();

    private HashMap<String, String> memId2MemGroupId = new HashMap<String, String>();

    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private String memId = "";
    
    public ExpSTAN() {
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

    public List<String> getBankIds() {
        return bankIds;
    }

    public void setBankIds(List<String> bankIds) {
        this.bankIds = bankIds;
    }
    public String getMemId() {
		return memId;
	}

	public void setMemId(String memId) {
		this.memId = memId;
	}
    
    private void beforeHandling() throws Exception 
    {
    	Connection conn = null;
        
        prviousDate = DateUtils.getPreviousDate(batchDate,1);
		
		try{
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			
			String sqlCmd = "select MEM_ID, BANK_ID, MEM_GROUP_ID from TB_MEMBER WHERE SUBSTR(MEM_TYPE,3,1)='1'";
			Vector vr = DbUtil.select(sqlCmd, conn);
			
			for(int idx=0; idx<vr.size(); idx++)
			{
				Vector list = (Vector) vr.get(idx);
				String memId=(String) list.get(0);
				String bankId=(String) list.get(1);
				String memGroupId=(String) list.get(2);
				
				memId2MemGroupId.put(memId, memGroupId);
				
				if(bankId != null && bankId != "") {
					if(bankId2MemId.get(bankId) == null){
				           bankIds.add(bankId); 
				           bankId2MemId.put(bankId, memId);
				           String seqno = getFileSeqNo(conn, memId);
				           bankId2Seqno.put(bankId, seqno);
			        }else{
			            if(bankId2MemId.get(bankId).equals(memId)) {
			            	throw new Exception("it doesn't allow different acqMemId");
			            }
			        }
				} 
			}
		}
		catch (Exception e) {
			throw e;
		}
		finally{
			log.info("memId2MemGroupId: " + memId2MemGroupId);
			ReleaseResource.releaseDB(conn);
		}
		log.info("bankIds:" + bankIds);
        log.info("bankId2Seqno:" + bankId2Seqno);
        log.info("bankId2MemId:" + bankId2MemId);
        log.info("memId2MemGroupId: " + memId2MemGroupId);
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

    public ExpFileSetting makeExpFileSetting() 
    {
    	
    	 /*For分收單*/
    	List bankIdList = getBankIdList();
    	
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
        
        if(bankIds.size() == 0) {
        	log.error("there are not any bank id !!");
        	return null;
        }
        for (int i = 0; i < bankIds.size(); i++) {
            String bankId = (String) bankIds.get(i);
            if(!bankIdList.contains(bankId)){
            	continue;
            }
            String seqno = (String) bankId2Seqno.get(bankId);
            //Integer count = (Integer) memId2Count.get(memId);
            if (seqno == null) {
                log.warn("can't find in bankId2Seqno or memId2Count, ignore for '" + bankId + "'");
                continue;
            }
            String memId = (String) bankId2MemId.get(bankId);
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemGroupId((String) memId2MemGroupId.get(memId));
            expFileInfo.setMemId(memId);
            expFileInfo.setFileName(FILE_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            fullFileName = FILE_APPEND + "." + memId + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            
            StringBuffer sb = new StringBuffer();
            sb.append("Select CARD_NO, EXPIRY_DATE, TXN_DATE, TXN_TIME, TXN_AMT, ACQ_MEM_ID,");
            sb.append(" MERCH_ID, LMS_INVOICE_NO, P_CODE, TXN_SRC, RESP_CODE from TB_OFF_RELOAD_AUTH");
            sb.append(" Where TXN_DATE='").append(prviousDate).append("'");
            //sb.append(" and MTI='").append("0200").append("'");
            sb.append(" and P_CODE='").append("7717").append("'");
            sb.append(" and CARD_NO in (select CARD_NO from TB_CARD where BANK_ID='").append(bankId).append("')");
            sb.append(" union");
            sb.append(" Select CARD_NO, EXPIRY_DATE, TXN_DATE, TXN_TIME, TXN_AMT, ACQ_MEM_ID,");
            sb.append(" MERCH_ID, LMS_INVOICE_NO, P_CODE, TXN_SRC, RESP_CODE from TB_ONL_TXN_ERR");
            sb.append(" Where TXN_DATE='").append(prviousDate).append("'");
            //sb.append(" and MTI='").append("0200").append("'");
            sb.append(" and P_CODE='").append("7717").append("'");
            sb.append(" and ONL_RCODE IN (SELECT RCODE FROM TB_RCODE WHERE STAN_FLAG = '1')");
            sb.append(" and CARD_NO in (select CARD_NO from TB_CARD where BANK_ID='").append(bankId).append("')");
           
            
            
            String selectSQL = sb.toString();
            
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
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.rightPad("", 114, ' '));
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
    	String expiryDate = "";
    	String txnDate = "";
    	String txnTime = "";
    	String txnAmt = "";
    	String acqMemId = "";
    	String merchId = "";
    	String lmsInvoiceNo = "";
    	String pCode = "";
    	String txnSrc = "";
    	String respCode = "";

    	cardNo = record.get(0).toString();
    	expiryDate = record.get(1).toString();
    	if (!isBlankOrNull(record.get(2).toString()))
    		txnDate = record.get(2).toString();
    	if (!isBlankOrNull(record.get(3).toString()))
    		txnTime = record.get(3).toString();
    	if (!isBlankOrNull(record.get(4).toString()))
    		txnAmt = record.get(4).toString();
    	if (!isBlankOrNull(record.get(5).toString()))
    		acqMemId = record.get(5).toString();
    	if (!isBlankOrNull(record.get(6).toString()))
    		merchId = record.get(6).toString();
    	if (!isBlankOrNull(record.get(7).toString()))
    		lmsInvoiceNo = record.get(7).toString();
    	if (!isBlankOrNull(record.get(8).toString()))
    		pCode = record.get(8).toString();
    	if (!isBlankOrNull(record.get(9).toString()))
    		txnSrc = record.get(9).toString();
    	if (!isBlankOrNull(record.get(10).toString()))
    		respCode = record.get(10).toString();
    	
    	TbMemberInfo memInfo = new TbMemberMgr(conn).querySingle(acqMemId);
    	TbMerchInfo merchInfo = new TbMerchMgr(conn).querySingle(merchId);

        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(cardNo, 20, ' '));
        sb.append(StringUtils.rightPad(expiryDate.substring(2, 6), 4, ' '));
        sb.append(StringUtils.rightPad(txnDate, 8, ' '));
        sb.append(StringUtils.rightPad(txnTime, 6, ' '));
        sb.append(StringUtils.leftPad(takeDecimal(txnAmt, 0), 14, '0'));
//        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(memInfo.getMemName(), 20, ' '),encoding,20));
//        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(merchInfo.getMerchLocName(), 20, ' '),encoding,20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(Layer2Util.getMaxString(StringUtils.rightPad(memInfo.getMemName(), 20, ' '),encoding,20), 20, ' '),encoding,20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(Layer2Util.getMaxString(StringUtils.rightPad(merchInfo.getMerchLocName(), 20, ' '),encoding,20), 20, ' '),encoding,20));
        sb.append(StringUtils.rightPad(merchId, 15, ' '));
        sb.append(StringUtils.rightPad(lmsInvoiceNo, 12, ' ')); 
        sb.append(StringUtils.rightPad(pCode, 4, ' '));
        if (txnSrc.equals("S")) {
        	sb.append(StringUtils.rightPad("1", 1, ' '));
        }
        else {
        	sb.append(StringUtils.rightPad("0", 1, ' '));
        }
        sb.append(StringUtils.rightPad(respCode, 2, ' '));
        sb.append(StringUtils.rightPad("0000", 4, ' '));

        
        //匯出資料map to bean
        ExpDataInfo dataInfo = new ExpDataInfo();
        dataInfo.setAcqMemId(acqMemId);
        dataInfo.setCardNo(cardNo);
        dataInfo.setExpiryDate(expiryDate);
        dataInfo.setLmsInvoiceNo(lmsInvoiceNo);
        dataInfo.setMerchId(merchId);
        dataInfo.setpCode(pCode);
        dataInfo.setTxnAmt(txnAmt);
        dataInfo.setTxnDate(txnDate);
        dataInfo.setTxnSrc(txnSrc);
        dataInfo.setTxnTime(txnTime);
        dataInfo.setRespCode(respCode);
        
        //特定處理
        specProcess(dataInfo);
        
        return sb.toString();
    }

	private void specProcess(ExpDataInfo dataInfo) throws Exception 
	{		
		modifyTrans(dataInfo);
 	}

	private void modifyTrans(ExpDataInfo dataInfo) throws Exception 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("update TB_TRANS set");
		sb.append(" MB_EXP_FILE_NAME='").append(fullFileName).append("'");
		sb.append(" where CARD_NO='").append(dataInfo.getCardNo()).append("'");
		sb.append(" and EXPIRY_DATE='").append(dataInfo.getExpiryDate()).append("'");
		sb.append(" and LMS_INVOICE_NO='").append(dataInfo.getLmsInvoiceNo()).append("'");
		
		String sql = sb.toString();
		
		log.debug("sql: " + sql);
		DbUtil.sqlAction(sql, conn);
	}
	private List getBankIdList()
	 {
		 Connection conn = null;
		 Statement stmt = null;
	     ResultSet rs = null;
		 ArrayList<String> bankIdList= null;				
		    	StringBuffer sqlCmd = new StringBuffer();
		    	if(getBatchResultInfo() != null){
	    			sqlCmd.append("SELECT BANK_ID FROM TB_MEMBER WHERE BANK_ID IS NOT NULL ");
		    		if(Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
			    		sqlCmd.append(" AND JOB_ID IS NULL");
			    		sqlCmd.append(" AND JOB_TIME IS NULL");
		    		}else{
			    		if(!StringUtil.isEmpty(getBatchResultInfo().getJobId()) 
								&& !getBatchResultInfo().getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
							sqlCmd.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobId()));
						       if(!StringUtil.isEmpty(getBatchResultInfo().getJobTime()) 
						    		   && !getBatchResultInfo().getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
						    	   sqlCmd.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobTime()));
						       }
						}
						if(!StringUtil.isEmpty(getBatchResultInfo().getMemId())){
							sqlCmd.append(" AND MEM_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getMemId()));
						}	
		    		}
		    	}
				else{
					 log.warn("tbBatchResultInfo is null.");
				}
		    	try {
					conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
					bankIdList = new ArrayList<String>();
					stmt = conn.createStatement();
					log.debug("sqlCmd: " + sqlCmd.toString());
					rs = stmt.executeQuery(sqlCmd.toString());
					while (rs.next()) {
						bankIdList.add(rs.getString(1));
					}
				} catch (Exception e) {
					bankIdList= new ArrayList<String>();
					log.info("catch: bankIdList is null!");
				}
		    	finally {
					ReleaseResource.releaseDB(conn, stmt, rs);
					return  bankIdList;
				}										
	}
	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpSTAN getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpSTAN instance = (ExpSTAN) apContext.getBean("expSTAN");
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
    	ExpSTAN expMerchData = null;
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
            	expMerchData = new ExpSTAN();
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
