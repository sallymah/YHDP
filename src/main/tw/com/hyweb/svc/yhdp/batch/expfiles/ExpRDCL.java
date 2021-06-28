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


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMbCardReturnInfo;
import tw.com.hyweb.service.db.mgr.TbMbCardReturnMgr;
import tw.com.hyweb.svc.yhdp.batch.expfiles.beans.ExpDataInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre> 
 * ExpSTAN
 * </pre>
 * author:
 */
public class ExpRDCL extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpRDCL.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpRDCL" + File.separator + "spring.xml";
    private static final String FILE_FN = "RDCL";
    private static final String FILE_APPEND = "RDCL";
    
    private String sysDate = DateUtils.getSystemDate();
    private String sysTime = DateUtils.getSystemTime();
    
    private String fullFileName = "";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List memIds = null;
    // 要匯出會員檔的 bank
    private List bankIds = new ArrayList();
    // key:String(bankId), value:String(seqno)
    private HashMap bankId2Seqno = new HashMap();
    // key:String(bankId), value:String(memId)
    private HashMap bankId2MemId = new HashMap();
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    private HashMap memId2MemGroupId = new HashMap();
    private String pCodeList ="";
    //For 收單bankId
    
	public ExpRDCL() {
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

	public String getpCodeList() {
		return pCodeList;
	}

	public void setpCodeList(String pCodeList) {
		this.pCodeList = pCodeList;
	}
    private void beforeHandling() throws Exception 
    {
    	Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        String sb = "SELECT BANK_ID, MEM_ID, MEM_GROUP_ID FROM TB_MEMBER WHERE BANK_ID IS NOT NULL";
        
        try {
        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			stmt = conn.createStatement();
		    log.debug("sb: "+sb);
		    rs = stmt.executeQuery(sb);
		    while (rs.next()){
		    	
		    	String bankId = rs.getString(1);
		    	String memId = rs.getString(2);
		    	String memGroupId = rs.getString(3);
		    	
		    	if(bankIds.contains(bankId)){
		    		throw new Exception("bankId is duplicated. [" + bankId + "]");
		    	}
		    	else{
		    		//從spring 設定收單 或不設全部產出
		    		if (memIds.size() == 0 || memIds.contains(memId)){
			    		bankIds.add(bankId); 
	           		  	bankId2MemId.put(bankId, memId);
	           		  	memId2MemGroupId.put(memId, memGroupId);
	           		  	
	           		  	String seqno = getFileSeqNo(conn, memId);
	           		  	bankId2Seqno.put(bankId, seqno);
		    		}
		    	}
		    }
        }catch (Exception e) {
			// TODO Auto-generated catch block
        	throw e;
		}
		finally {
				
			ReleaseResource.releaseDB(conn, stmt, rs);
		}
		log.info("bankIds:" + bankIds);
	    log.info("bankId2Seqno:" + bankId2Seqno);
	    log.info("memId2MemGroupId:" + memId2MemGroupId);
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
        
        if(bankIds == null) {
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
            log.info("bankId: "+bankId);
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
            
            String selectSQL ="SELECT TB_TRANS.CARD_NO, TB_TRANS.EXPIRY_DATE, TXN_DATE, TXN_TIME, TXN_AMT, ACQ_MEM_ID, MERCH_ID,P_CODE,LMS_INVOICE_NO,TB_CARD.BANK_ID,TXN_SRC FROM TB_TRANS, TB_CARD"+         		
					" WHERE CUT_DATE = '"+ batchDate +"' " + 
            		" AND P_CODE in ("+ pCodeList +")"+ 
					" AND TB_TRANS.STATUS = '1'"+
            		" AND TB_TRANS.CARD_NO = TB_CARD.CARD_NO"+
					" AND TB_CARD.BANK_ID ='"+ bankId +"'";
					
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
	     header.append(StringUtils.rightPad("", 98, ' '));
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
		Number txnAmt = 0.0d;     
		String merchId = "";
		String acqMemId = ""; 
		String lmsInvoiceNo = "";
		String txnSrc = "";
		String rCode = "0000";
		String pCode="";
		String bankId ="";
		int txnFees = 0;
		
		if (!isBlankOrNull(record.get(0).toString()))
			cardNo = record.get(0).toString();
		if (!isBlankOrNull(record.get(1).toString()))
			expiryDate = record.get(1).toString();
		if (!isBlankOrNull(record.get(2).toString()))
			txnDate = record.get(2).toString();
		if (!isBlankOrNull(record.get(3).toString()))
			txnTime = record.get(3).toString();
		txnAmt = (Number) record.get(4);
		if (!isBlankOrNull(record.get(5).toString()))
    		acqMemId = record.get(5).toString();
		merchId = record.get(6).toString();
    	if (!isBlankOrNull(record.get(7).toString()))
    		pCode = record.get(7).toString();
    	if (!isBlankOrNull(record.get(8).toString()))
    		lmsInvoiceNo = record.get(8).toString();
    	if (!isBlankOrNull(record.get(9).toString()))
    		bankId = record.get(9).toString();
    	
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(cardNo, 20, ' '));
        //log.info("card_no: [" + StringUtils.rightPad(cardNo, 20, ' ') + "]");
        sb.append(StringUtils.rightPad(expiryDate.substring(2,6), 4, ' '));
        sb.append(StringUtils.rightPad(txnDate, 8, ' '));
        sb.append(StringUtils.rightPad(txnTime, 6, ' '));
        
        
        if ( txnAmt.doubleValue() < 0 ){
        	sb.append("-").append(StringUtils.leftPad(takeDecimal(String.valueOf(Math.abs(txnAmt.doubleValue())),0), 12, '0'));
        }
        else{
        	sb.append(StringUtils.leftPad(takeDecimal(String.valueOf(txnAmt), 0) ,13, '0'));
        }
        
//        if ( txnAmt.doubleValue() < 0 ){
//        	double calTxnAmt =0 - txnAmt.doubleValue();
//    		String refundAmtStr = StringUtils.leftPad(takeDecimal(String.valueOf(calTxnAmt), 0) ,13, '0');	
//    		sb.append(refundAmtStr.replaceFirst("0", "-"));
//    		//log.info("refundAmt: [" + refundAmtStr.replaceFirst("0", "-") + "]");
//    	}
//        else {
//        	sb.append(StringUtils.leftPad(takeDecimal(String.valueOf(txnAmt), 0) ,13, '0'));
//            //log.info("refundAmt: [" + StringUtils.rightPad(String.valueOf(refundAmt), 13, ' ') + "]");
//        }
        sb.append(StringUtils.leftPad(String.valueOf(txnFees), 14, '0'));
        sb.append(StringUtils.rightPad(bankId, 3, ' '));
        sb.append(StringUtils.rightPad(merchId, 15, ' '));
    
		if (pCode.equals("9897")) {
			//WS自動加值功能關閉
			txnSrc = "CLAR";
		}
		else{
			txnSrc ="YHDP";
		}
		
		log.info(pCode);
		log.info(txnSrc);
        sb.append(StringUtils.rightPad(txnSrc, 4, ' '));
        sb.append(StringUtils.rightPad(rCode, 4, ' '));
        sb.append(StringUtils.rightPad("", 23, ' '));
        
        ExpDataInfo dataInfo = new ExpDataInfo();   
        dataInfo.setCardNo(cardNo);
        dataInfo.setExpiryDate(expiryDate);
        dataInfo.setLmsInvoiceNo(lmsInvoiceNo);
        dataInfo.setAcqMemId(acqMemId);
        dataInfo.setMerchId(merchId);
        dataInfo.setpCode(pCode);
        dataInfo.setTxnAmt(String.valueOf(txnAmt));
        dataInfo.setTxnDate(txnDate);
        dataInfo.setTxnTime(txnTime);
        
        specProcess(dataInfo);
        
		return sb.toString();
    }

	private void specProcess(ExpDataInfo dataInfo) throws Exception 
	{		
		addTbMbcardReturn(dataInfo);
 	}

	private void addTbMbcardReturn(ExpDataInfo dataInfo) throws Exception 
	{
		TbMbCardReturnInfo tbMbCardReturnInfo = new TbMbCardReturnInfo();
		
		tbMbCardReturnInfo.setCardNo(dataInfo.getCardNo());
		tbMbCardReturnInfo.setExpiryDate(dataInfo.getExpiryDate());
		tbMbCardReturnInfo.setTxnDate(dataInfo.getTxnDate());
		tbMbCardReturnInfo.setTxnTime(dataInfo.getTxnTime());
		tbMbCardReturnInfo.setTxnAmt(Double.valueOf(dataInfo.getTxnAmt()));
		tbMbCardReturnInfo.setAcqMemId(dataInfo.getAcqMemId());
		tbMbCardReturnInfo.setMerchId(dataInfo.getMerchId());
		tbMbCardReturnInfo.setPCode(dataInfo.getpCode());
		tbMbCardReturnInfo.setLmsInvoiceNo(dataInfo.getLmsInvoiceNo());
		tbMbCardReturnInfo.setExpDate(sysDate);
		tbMbCardReturnInfo.setExpTime(sysTime);
		tbMbCardReturnInfo.setExpFileName(fullFileName);
		
		new TbMbCardReturnMgr(conn).insert(tbMbCardReturnInfo);	
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

	public static ExpRDCL getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpRDCL instance = (ExpRDCL) apContext.getBean("expRDCL");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    
    private String takeDecimal( String number, int afterDecimal){
		
    	String afterDecimalNumber = "";
	
    	if (number.contains(".")){
    		if ( afterDecimal > 0 ){
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
    		else{
    			return number.substring(0, number.indexOf("."));
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
    	ExpRDCL expMerchData = null;
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
            	expMerchData = new ExpRDCL();
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
