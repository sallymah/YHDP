package  tw.com.hyweb.svc.yhdp.batch.expfiles;

/**
 * changelog ExpFund
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

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
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
 * ExpAssociator
 * </pre>
 * author:duncan
 */
public class ExpFund extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpFund.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpFund" + File.separator + "spring.xml";
    private static final String FUND_FN = "FUND";
    private static final String FUND_APPEND = "FUND";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List memIds = null;
    // key:String(memId), value:String(seqno)
    private HashMap memId2Seqno = new HashMap();
    // key:String(memId), value:String(memGroupId)    
    private HashMap memId2MemGroupId = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private String limitMemIds = "";
    private double total=0;
    public ExpFund() {
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
    public String getLimitMemIds() {
		return limitMemIds;
	}

	public void setLimitMemIds(String limitMemIds) {
		this.limitMemIds = limitMemIds;
	}

	private void beforeHandling() throws SQLException {

    	Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    	Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        StringBuffer sql = new StringBuffer();

    	try {
    		sql.append("SELECT MEM_ID, MEM_GROUP_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE IS NOT NULL ");
    		
    		if ( !StringUtil.isEmpty(this.limitMemIds) ){
    			
    			sql.append("AND ");
    			
    			String[] mem_ids = getLimitMemIds().split(",");
    			if ( mem_ids.length > 1 ){
    				sql.append("MEM_ID in (");
    				for( int i =0; i < mem_ids.length; i++ ){
    					sql.append("'" + mem_ids[i] + "'");
    					if( i != mem_ids.length-1 )
    						sql.append(", ");
    				}
    				sql.append(") ");
    			}
    			else
    				sql.append("MEM_ID='" + getLimitMemIds() + "'");
    		}
    		
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
	                      		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(FUND_FN) + 
	                      		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(DateUtil.getTodayString().substring(0, 8));
    			try {
    				stmt1 = conn.createStatement();
    				log.debug("seqnoSql: "+seqnoSql);
    				rs1 = stmt1.executeQuery(seqnoSql);
    				while (rs1.next()) {
    					seqno = rs1.getString(1);
    				}
    			}
    			finally {
    				ReleaseResource.releaseDB(null, stmt1, rs1);
    			}
    			
    			memId2MemGroupId.put(memId, memGroupId);
    			memId2Seqno.put(memId , seqno);       
    			memIds.add(memId);
          	    //--
    		}
    		log.debug("memIds="+memIds);
    	}
    	finally {
    		ReleaseResource.releaseDB(conn, stmt, rs);
    	}
        /*log.info("memIds:" + memIds);
        log.info("memId2MemGroupId:" + memId2MemGroupId);
        log.info("memId2Seqno:" + memId2Seqno);*/
    }

    public ExpFileSetting makeExpFileSetting() {
    	
    	/*For分收單*/
    	List memIdList = getMemIdList();
    	
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
            if(!memIdList.contains(memId)){
            	continue;
            }
            String seqno = (String) memId2Seqno.get(memId);
            //Integer count = (Integer) memId2Count.get(memId);
            if (seqno == null) {
                log.warn("can't find in memId2Seqno or memId2Count, ignore for '" + memId + "'");
                continue;
            }
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemId(memId);
            expFileInfo.setMemGroupId((String) memId2MemGroupId.get(memId));
            expFileInfo.setFileName(FUND_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = FUND_APPEND + "." +memId + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL =
            		"SELECT "+ 
            		"'ST01' AS ACCOUNT_CODE, "+
            		"CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN CREDIT_ID ELSE DEBIT_ID END AS CREDIT_ID, "+ 
            		"CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN DEBIT_ID ELSE CREDIT_ID END AS DEBIT_ID, "+ 
            		"SUM(SETTLE_AMT *(CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN 1 ELSE -1 END)) AS SETTLE_AMT, "+
            		"COUNT(1) AS CNT, EXP_PAY_DATE "+
            		"FROM TB_SETTLE_RESULT "+ 
            		"WHERE EXP_PAY_DATE = '"+ batchDate +"' "+
            		"AND ACCOUNT_CODE IN ('S100','S200') "+ 
            		"AND ACQ_MEM_ID = '"+ memId +"' "+
            		"GROUP BY CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN CREDIT_ID ELSE DEBIT_ID END , "+ 
            		"CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN DEBIT_ID ELSE CREDIT_ID END, "+
            		"EXP_PAY_DATE "+
            		"UNION ALL "+
            		"SELECT "+
            		"'SA01' AS ACCOUNT_CODE, "+ 
            		"CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN CREDIT_ID ELSE DEBIT_ID END AS CREDIT_ID, "+ 
            		"CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN DEBIT_ID ELSE CREDIT_ID END AS DEBIT_ID, "+ 
            		"SUM(SETTLE_AMT *(CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN 1 ELSE -1 END)) AS SETTLE_AMT, "+
            		"COUNT(1) AS CNT, EXP_PAY_DATE "+
            		"FROM TB_SETTLE_RESULT "+ 
            		"WHERE EXP_PAY_DATE = '"+ batchDate +"' "+
            		"AND ACCOUNT_CODE IN ('S109','S209') "+ 
            		"AND ACQ_MEM_ID= '"+ memId +"' "+
            		"GROUP BY CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN CREDIT_ID ELSE DEBIT_ID END , "+ 
            		"CASE WHEN DEBIT_ID = ACQ_MEM_ID THEN DEBIT_ID ELSE CREDIT_ID END, "+
            		"EXP_PAY_DATE "+
            		"UNION ALL "+
            		"SELECT "+ 
            		"'SF01' AS ACCOUNT_CODE, "+
            		"CASE WHEN DEBIT_ID = '"+ memId +"' " + "THEN CREDIT_ID ELSE DEBIT_ID END AS CREDIT_ID, "+ 
            		"CASE WHEN DEBIT_ID = '"+ memId +"' " + "THEN DEBIT_ID ELSE CREDIT_ID END AS DEBIT_ID, "+ 
            		"SUM(FEE_AMT *(CASE WHEN DEBIT_ID = '"+ memId +"' " + "THEN 1 ELSE -1 END)) AS SETTLE_AMT, "+
            		"COUNT(1) AS CNT, EXP_PAY_DATE "+
            		"FROM TB_FEE_RESULT "+
            		"WHERE EXP_PAY_DATE = '"+ batchDate +"' "+
            		"AND (CREDIT_ID = '"+ memId +"' " + "OR DEBIT_ID = '"+ memId +"' " + ") "+
            		"GROUP BY "+ 
            		"CASE WHEN DEBIT_ID = '"+ memId +"' " + "THEN CREDIT_ID ELSE DEBIT_ID END, "+ 
            		"CASE WHEN DEBIT_ID = '"+ memId +"' " + "THEN DEBIT_ID ELSE CREDIT_ID END, "+
            		"EXP_PAY_DATE ";
            
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

    	//96
        StringBuffer header = new StringBuffer();
        header.append("H"); 
        header.append(FUND_FN);
        header.append(StringUtils.rightPad(expFileResult.getExpFileInfo().getMemId().toString(),8,""));
        header.append(StringUtils.rightPad(batchDate,8,""));
        String dateTime = DateUtil.getTodayString();
        header.append(StringUtils.rightPad(dateTime.substring(8, 14),6,"0"));
        header.append(StringUtils.rightPad("", 69, ' '));
        return header.toString();
    }
    
    public String outputEndFile() {    	
    	//96
        StringBuffer header = new StringBuffer();
        header.append("T");
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.leftPad( takeDecimal(String.valueOf(total),2), 14, '0'));
        header.append(StringUtils.rightPad("", 73, ' '));
        return header.toString();
    }

    public void actionsAfterInfo() throws Exception {
		// 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
    	total=0;
        super.actionsAfterFile();
    }

    public String outputOneRecord(List record) { 
    	
    	String accountCode = "";
    	double setl_amt = 0;
    	String credit_id = "";
    	String debit_id = "";
    	String setl_cnt = "";
    	String expPayDate = "";
    	
    	accountCode = record.get(0).toString();
    	setl_amt = Double.parseDouble(record.get(3).toString());
    	
    	if ( setl_amt > 0 ){
    		credit_id = record.get(1).toString();
        	debit_id = record.get(2).toString();
    	}
    	else{
    		credit_id = record.get(2).toString();
        	debit_id = record.get(1).toString();
    	}

    	setl_cnt = record.get(4).toString();
    	expPayDate = record.get(5).toString();
    	
        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append("D01");
        sb.append(StringUtils.rightPad(accountCode, 4, ' '));
        sb.append(StringUtils.rightPad(credit_id, 8, ' '));
        sb.append(StringUtils.rightPad(debit_id, 8, ' '));
        sb.append(StringUtils.leftPad( takeDecimal(String.valueOf(Math.abs(setl_amt)),2), 15, '0'));
        sb.append(StringUtils.leftPad(setl_cnt, 8, '0'));
        sb.append(StringUtils.rightPad(expPayDate, 8, ' '));
        sb.append(StringUtils.rightPad("", 42, ' '));
        total = total+ Math.abs(setl_amt);
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
					log.info("catch: memIdList is null!");
				}
		    	finally {
					ReleaseResource.releaseDB(conn, stmt, rs);
					return  memIdList;
				}										
	}
	public static ExpFund getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpFund instance = (ExpFund) apContext.getBean("expFund");
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
    	ExpFund expFund = null;
        try {
            String batchDate = System.getProperty("date");
            String limitMemIds = System.getProperty("limitMemIds");
            log.debug("date="+batchDate);
            log.debug("limitMemIds="+limitMemIds);
            
            if (StringUtil.isEmpty(limitMemIds)) {
            	limitMemIds="";
            }
            
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            else if (!DateUtil.isValidDate(batchDate)) {
                log.info("invalid batchDate('" + batchDate + "') using system date!");
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	expFund = getInstance();
            }
            else {
            	expFund = new ExpFund();
            }
            expFund.setBatchDate(batchDate);
            expFund.setLimitMemIds(limitMemIds);
            // 註: 此 method 一定要先呼叫
            expFund.beforeHandling();
            expFund.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpFund run fail:" + ignore.getMessage(), ignore);
        }
    }
}
