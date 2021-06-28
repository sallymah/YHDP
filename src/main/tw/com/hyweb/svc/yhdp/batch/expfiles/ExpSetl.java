package  tw.com.hyweb.svc.yhdp.batch.expfiles;

/**
 * changelog ExpSetl
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
public class ExpSetl extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpSetl.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpSetl" + File.separator + "spring.xml";
    private static final String SETL_FN = "SETL";
    private static final String SETL_APPEND = "SETL";

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
    private String memId = "";
    private double total=0;
    public ExpSetl() {
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
    
    private void beforeHandling() throws SQLException {

    	Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    	Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        StringBuffer sql = new StringBuffer();

    	try {
    		sql.append("SELECT MEM_ID, MEM_GROUP_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE IS NOT NULL ");
    		if ( this.memId != null )
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
	                      		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(SETL_FN) + 
	                      		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(DateUtil.getTodayString().substring(0, 8));
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
    			log.warn("memIds="+memIds);
    			
    			memId2MemGroupId.put(memId, memGroupId);
    			memId2Seqno.put(memId , seqno);       
    			memIds.add(memId);
          	    //--
    		}
    	}
    	finally {
    		ReleaseResource.releaseDB(conn, stmt, rs);
    	}
        
        log.info("memIds:" + memIds);
        log.info("memId2MemGroupId:" + memId2MemGroupId);
        log.info("memId2Seqno:" + memId2Seqno);
    }

    public ExpFileSetting makeExpFileSetting() 
    {	
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
            if (!memIdList.contains(memId)){
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
            expFileInfo.setFileName(SETL_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = SETL_APPEND + "." +memId + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL =
            			"SELECT P_CODE, CREDIT_ID, DEBIT_ID, SUM(SETTLE_AMT) AS SETL_AMT, COUNT(*) AS SETL_CNT, PROC_DATE "+
        				"FROM TB_SETTLE_RESULT "+
        				"WHERE ACQ_MEM_ID = '"+ memId +"' "+
        				"AND PROC_DATE = '"+ batchDate +"' "+
        				"GROUP BY P_CODE, CREDIT_ID, DEBIT_ID, PROC_DATE ";
            
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
        header.append(SETL_FN);
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
    	
    	String pCode = "";
    	String setl_amt = "";
    	String credit_id = "";
    	String debit_id = "";
    	String setl_cnt = "";
    	String procDate = "";
    	
    	pCode = record.get(0).toString();
    	credit_id = record.get(1).toString();
    	debit_id = record.get(2).toString();
    	setl_amt = record.get(3).toString();
    	setl_cnt = record.get(4).toString();
    	procDate = record.get(5).toString();
    	
        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append("D01");
        sb.append(StringUtils.rightPad(pCode, 4, ' '));
        sb.append(StringUtils.rightPad(credit_id, 8, ' '));
        sb.append(StringUtils.rightPad(debit_id, 8, ' '));
        sb.append(StringUtils.leftPad( takeDecimal(setl_amt,2), 15, '0'));
        sb.append(StringUtils.leftPad(setl_cnt, 8, '0'));
        sb.append(StringUtils.rightPad(procDate, 8, ' '));
        sb.append(StringUtils.rightPad("", 42, ' '));
        total = total+ Double.parseDouble(setl_amt);
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
	public static ExpSetl getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpSetl instance = (ExpSetl) apContext.getBean("expSetl");
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
    	ExpSetl expSetl = null;
        try {
            String batchDate = System.getProperty("date");
            String mem_id = System.getProperty("memid");
            log.debug("date="+batchDate);
            log.debug("mem_id="+mem_id);
            
            if (StringUtil.isEmpty(mem_id)) {
            	mem_id=null;
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
            	expSetl = getInstance();
            }
            else {
            	expSetl = new ExpSetl();
            }
            expSetl.setBatchDate(batchDate);
            expSetl.setMemId(mem_id);
            // 註: 此 method 一定要先呼叫
            expSetl.beforeHandling();
            expSetl.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpSetl run fail:" + ignore.getMessage(), ignore);
        }
    }
}
