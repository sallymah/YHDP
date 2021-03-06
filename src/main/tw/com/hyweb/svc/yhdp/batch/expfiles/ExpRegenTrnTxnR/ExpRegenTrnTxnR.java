package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpRegenTrnTxnR;

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
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.util.BatchUtils;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public  class  ExpRegenTrnTxnR extends AbstractExpFile{
	private static Logger log  = Logger.getLogger(ExpRegenTrnTxnR.class);
	private static final String SPRING_PATH =  "config" + File.separator + "batch" + File.separator 
			+"expfiles" + File.separator  + "ExpRegenTrnTxnR" + File.separator + "spring.xml";
	private static final String FILE_FN = "R_TRN_TXNR";
	private static final String FILE_APPEND = "R_TRN_TXNR";
	
	private List memIds = null;
	// 要匯出會員檔的 bank
    private List bankIds = null;
	// key:String(bankId), value:String(seqno)
    
    private HashMap memId2Seqno = new HashMap();
    // key:String(bankId), value:String(memId)
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    private HashMap memId2MemGroupId = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private HashMap transationInfos = null;
	private String encoding = "";
	private String memId = "";
	private String pCodeList = "";


	private double total= 0;
	
    public ExpRegenTrnTxnR() {
    }
    
    public String getEncoding(){
    	return encoding;
    }
    
    public void setEncoding(String encoding){
		try{
			" ".getBytes(encoding);
		}
		
		catch(Exception ignore){
			encoding = "UTF-8";
		}
    	this.encoding = encoding;
    	
    }
    public String getMemId() {
		return memId;
	}

	public void setMemId(String memId) {
		this.memId = memId;
	}
	
	public String getpCodeList() {
		return pCodeList;
	}

	public void setpCodeList(String pCodeList) {
		this.pCodeList = pCodeList;
	}

	public List getMemIds() {
		return memIds;
	}

	public void setMemIds(List memIds) {
		this.memIds = memIds;
	}
	
    public List getBankIds() {
		return bankIds;
	}

	public void setBankIds(List bankIds) {
		this.bankIds = bankIds;
	}
	
	private void beforeHandling() throws Exception{

    	Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    	Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        StringBuffer sql = new StringBuffer();

    	try {
    		sql.append("SELECT MEM_ID, MEM_GROUP_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE = '2' ");
    		if ( !BatchUtils.isBlankOrNull(this.memId) )
    			sql.append("AND MEM_ID = '").append(this.memId).append("'");
    		
    		stmt = conn.createStatement();
    		log.warn("sql: "+sql);
    		rs = stmt.executeQuery(sql.toString());
    		while (rs.next()) {
    			
    			seqno = "01";
    			Statement stmt1 = null;
    			ResultSet rs1 = null;
    			String memId= rs.getString(1);
    			String memGroupId= rs.getString(2);
    			
    			String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
    							"WHERE MEM_ID = '" +  memId + "'"+
	                      		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(FILE_FN) + 
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
    			
    			memId2MemGroupId.put(memId, memGroupId);
    			memId2Seqno.put(memId , seqno);       
    			memIds.add(memId);
    			
    		}
    	}
    	finally {
    		ReleaseResource.releaseDB(conn, stmt, rs);
    	}
        log.info("memIds:" + memIds);
        log.info("memId2MemGroupId:" + memId2MemGroupId);
        log.info("memId2Seqno:" + memId2Seqno);
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
	@Override
	public ExpFileSetting makeExpFileSetting()
	{	
		/*For分收單*/
		List memIdList = getMemIdList();
		
		ExpFileSetting efs = new ExpFileSetting();
		
		efs.setCheckFlag(false);
		//產生檔案的編碼,default 是UTF_8
		if(StringUtil.isEmpty(encoding )){
			encoding = "UTF-8";
		}
		efs.setFileEncoding(encoding);
		//是否要啟動Temp File
		efs.setUsingTempFile(true);
		
		efs.setTempFilePending(".TMP");
		// 產生檔案時處理多少筆記錄後做一次 flush 動作, <= 0 時不 enable flush 動作, default is -1
		efs.setRecordsPerFlush(-1);
		// 多少筆產生一個 file, default 全部一個檔案(<= 0)
		efs.setRecordsPerFile(-1);
		efs.setLineSeparator("\r\n");

        for (int i = 0; i < memIds.size(); i++) {
        	 String memId = (String) memIds.get(i);
        	 if (!memIdList.contains(memId)){
					continue;
			 }
             String seqno = (String) memId2Seqno.get(memId);
 
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemId(memId);
            expFileInfo.setMemGroupId((String) memId2MemGroupId.get(memId));
            expFileInfo.setFileName(FILE_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = StringUtils.leftPad(memId, 8, '0') + "_" + expFileInfo.getFileDate() + "_"+expFileInfo.getSeqno()+"."+"csv";
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL ="SELECT ACQ_MEM_ID,MERCH_ID,TERM_ID,CARD_NO,ATC,P_CODE,TXN_DATE,TXN_TIME,TXN_AMT,LMS_INVOICE_NO,SETTLE_SUCC_DATE FROM TB_TRANS "+         		
					" WHERE ACQ_MEM_ID = '"+ memId +"' " + 
					" AND SETTLE_SUCC_DATE = '"+ batchDate +"' " + 
					" AND EXISTS (SELECT 1 FROM TB_INCTL where FILE_NAME = 'R_TRN_TXN' and TB_TRANS.IMP_FILE_NAME = TB_INCTL.FULL_FILE_NAME) ";
            log.info(selectSQL);
            		
            if(i==0){
            	log.info(selectSQL);
            }
            expFileInfo.setSelectSQL(selectSQL);
            efs.addExpFileInfo(expFileInfo);
        }
        return efs;
	}
	public String outputAfterFile(){
		return super.outputAfterFile();
	}
	public String outputBeforeFile(){
	     return "";
	}
	
	public String outputEndFile(){
        
        return "";
	}
	
    public void actionsAfterInfo() throws Exception {
        // 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
        super.actionsAfterFile();
    }

	@Override
	public String outputOneRecord(List record) {
		
		String acqMemId = "";
		String merchId = "";
		String termId = "";
		String cardNo = "";
		String atc = "";
		String pCode = "";
		String txnDate = "";
		String txnTime = "";
		String txnAmt = "";
		String lmsInvoiceNo = "";
		String settleSuccDate = "";
		
		if (!isBlankOrNull(record.get(0).toString()))
			acqMemId = record.get(0).toString();
		if (!isBlankOrNull(record.get(1).toString()))
			merchId = record.get(1).toString();
		if (!isBlankOrNull(record.get(2).toString()))
			termId = record.get(2).toString();
		if (!isBlankOrNull(record.get(3).toString()))
			cardNo = record.get(3).toString();	
		if (!isBlankOrNull(record.get(4).toString()))
			atc = record.get(4).toString();
		if (!isBlankOrNull(record.get(5).toString()))
			pCode = record.get(5).toString();
		if (!isBlankOrNull(record.get(6).toString()))
			txnDate = record.get(6).toString();
		if (!isBlankOrNull(record.get(7).toString()))
			txnTime = record.get(7).toString();
		if (!isBlankOrNull(record.get(8).toString()))
			txnAmt = record.get(8).toString();
		if (!isBlankOrNull(record.get(9).toString()))
			lmsInvoiceNo = record.get(9).toString();
		if (!isBlankOrNull(record.get(10).toString()))
			settleSuccDate = record.get(10).toString();
		
        StringBuffer sb = new StringBuffer();
        sb.append(acqMemId).append(",");
        sb.append(merchId.substring(8, 10)).append(",");
        sb.append(termId.substring(6, 8)).append(",");
        sb.append(cardNo).append(",");
        sb.append(atc).append(",");
        sb.append(pCode).append(",");
        sb.append(txnDate).append(",");
        sb.append(txnTime).append(",");
        sb.append(txnAmt).append(",");
        sb.append(lmsInvoiceNo).append(",");
        sb.append(settleSuccDate);
		return sb.toString();
	}
		
	public List<String> outputDtlRecord(List record) { 
		
	       return null;
	  }
	private List getMemIdList()
	{
		 Connection conn = null;
		 Statement stmt = null;
	     ResultSet rs = null;
		 ArrayList<String> memIdList= null;				
		    	StringBuffer sqlCmd = new StringBuffer();
		    	if(getBatchResultInfo() != null){
		    		sqlCmd.append("SELECT MEM_ID FROM TB_MEMBER WHERE 1=1 ");
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
					memIdList = new ArrayList<String>();
					stmt = conn.createStatement();
					log.debug("sqlCmd: " + sqlCmd.toString());
					rs = stmt.executeQuery(sqlCmd.toString());
					while (rs.next()) {
						memIdList.add(rs.getString(1));
					}
				} catch (Exception e) {
					memIdList= new ArrayList<String>();
					log.info("catch: bankIdList is null!");
				}
		    	finally {
					ReleaseResource.releaseDB(conn, stmt, rs);
					return  memIdList;
				}										
	}
	private static ExpRegenTrnTxnR getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ExpRegenTrnTxnR instance  = (ExpRegenTrnTxnR) apContext.getBean("expRegenTrnTxnR");
		return instance;
	}
	
	public static boolean isBlankOrNull(String value) {
			return (value == null || value.trim().equals(""));
	}
	    
	public static void main(String[] args){
		ExpRegenTrnTxnR expRegenTrnTxnR = null;
		try{
			String batchDate = System.getProperty("date");
			log.debug("date=" + batchDate);
            
			if(StringUtil.isEmpty(batchDate)){
				batchDate = DateUtil.getTodayString().substring(0,8);
			}
			else if(!DateUtil.isValidDate(batchDate)){
				 log.info("invalid batchDate('" + batchDate + "') using system date!");
	             batchDate = DateUtil.getTodayString().substring(0, 8);
			}
			File f = new File(SPRING_PATH);
			if(f.exists()&& f.isFile()){
				expRegenTrnTxnR = getInstance();
			}
			else{
				
				expRegenTrnTxnR = new ExpRegenTrnTxnR();
			}
			
			expRegenTrnTxnR.setBatchDate(batchDate);
			
			expRegenTrnTxnR.beforeHandling();
			expRegenTrnTxnR.run(args);
		}
		catch (Exception ignore){
			 log.warn("ExpRegenTrnTxnR run fail:" + ignore.getMessage(), ignore);
		}
	}
}
