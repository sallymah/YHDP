package tw.com.hyweb.svc.yhdp.batch.expfiles;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public  class  ExpLKCD extends AbstractExpFile{
	private static Logger log  = Logger.getLogger(ExpLKCD.class);
	private static final String SPRING_PATH =  "config" + File.separator + "batch" + File.separator 
			+"expfiles" + File.separator  + "ExpLKCD" + File.separator + "spring.xml";
	private static final String FILE_FN = "LKCD";
	private static final String FILE_APPEND = "LKCD";
	
	private List memIds = null;
	// 要匯出會員檔的 bank
    private List bankIds = null;
	// key:String(bankId), value:String(seqno)
    private HashMap bankId2Seqno = new HashMap();
    // key:String(bankId), value:String(memId)
    private HashMap bankId2MemId = new HashMap();
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    private HashMap memId2MemGroupId = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
	private String encoding = "";
	private String memId = "";
	private String pCodeList = "";


	private double total= 0;
	
    public ExpLKCD() {
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
		
		Connection conn = null;
		
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
	@Override
	public ExpFileSetting makeExpFileSetting() {
		
		/*for 分收單*/
		List bankIdList=getBankIdList();
		
		ExpFileSetting efs = new ExpFileSetting();
		
		efs.setCheckFlag(true);
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
            if (seqno == null) {
                log.warn("can't find in bankId2Seqno or memId2Count, ignore for '" + bankId + "'");
                continue;
            }
            String memId = (String) bankId2MemId.get(bankId);
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemId(memId);
            expFileInfo.setMemGroupId((String) memId2MemGroupId.get(memId));
            expFileInfo.setFileName(FILE_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = FILE_APPEND + "." + StringUtils.leftPad(memId, 8, '0') + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL ="SELECT TB_TRANS.CARD_NO, TB_TRANS.EXPIRY_DATE, TXN_DATE, TXN_TIME,P_CODE FROM TB_TRANS, TB_CARD"+         		
					" WHERE CUT_DATE = '"+ batchDate +"' " + 
					" AND P_CODE in ("+ pCodeList +")"+
					" AND TB_TRANS.STATUS = '1'"+
					" AND TB_TRANS.CARD_NO = TB_CARD.CARD_NO"+ 
					" AND TB_CARD.BANK_ID ='"+ bankId +"'" ; 
            		
            		//" AND CARD_NO = (SELECT CARD_NO FROM TB_CARD WHERE BANK_ID ='"+ memId +"')";
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
		
		 StringBuffer header = new StringBuffer();
	     header.append("H0");
	     header.append(batchDate);
	     header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
	     header.append(StringUtils.rightPad("", 50, ' '));
	     return header.toString();
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
		
		String cardNo = "";
		String expiryDate = "";
		String refundDate= ""; 
		String refundTime = "";
		String rCode = "0000";
		
		if (!isBlankOrNull(record.get(0).toString()))
			cardNo = record.get(0).toString();
		if (!isBlankOrNull(record.get(1).toString()))
			expiryDate = record.get(1).toString();
		if (!isBlankOrNull(record.get(2).toString()))
			refundDate = record.get(2).toString();
		if (!isBlankOrNull(record.get(3).toString()))
			refundTime = record.get(3).toString();	
	
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(cardNo, 20, ' '));
        sb.append(StringUtils.rightPad(expiryDate.substring(2,6), 4, ' '));
        sb.append(StringUtils.rightPad(refundDate, 8, ' '));
        sb.append(StringUtils.rightPad(refundTime, 6, ' '));
        sb.append(StringUtils.rightPad(rCode, 4, ' '));
        sb.append(StringUtils.rightPad("", 24, ' '));

		return sb.toString();
	}
		
	public List<String> outputDtlRecord(List record) { 
		
	       return null;
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
	private static ExpLKCD getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ExpLKCD instance  = (ExpLKCD) apContext.getBean("expLKCD");
		return instance;
	}
	
	public static boolean isBlankOrNull(String value) {
			return (value == null || value.trim().equals(""));
	}
	    
	public static void main(String[] args){
		ExpLKCD expLKCD = null;
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
				expLKCD = getInstance();
			}
			else{
				
				expLKCD = new ExpLKCD();
			}
			
			expLKCD.setBatchDate(batchDate);
			
			expLKCD.beforeHandling();
			expLKCD.run(args);
		}
		catch (Exception ignore){
			 log.warn("ExpLKCD run fail:" + ignore.getMessage(), ignore);
		}
	}
}
