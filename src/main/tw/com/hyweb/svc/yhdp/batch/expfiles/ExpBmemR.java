package  tw.com.hyweb.svc.yhdp.batch.expfiles;

/**
 * changelog ExpBmemR
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
public class ExpBmemR extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpBmemR.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpBmemR" + File.separator + "spring.xml";
    private static final String BMEM_FN = "BMEM";
    private static final String BMEMR_FN = "BMEMR";
    private static final String BMEMR_APPEND = "BMEMR";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
//    private List memIds = null;
    // key:String(memId), value:String(seqno)
//    private HashMap memId2Seqno = new HashMap();
    // key:String(memId), value:String(memGroupId)    
//    private HashMap memId2MemGroupId = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    
    private List<String> fileNames = new ArrayList<String>();

    private String limitMemIds = "";
    
    private double total=0;
    
    public ExpBmemR() {
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

    /*public List getMemIds() {
        return memIds;
    }

    public void setMemIds(List memIds) {
        this.memIds = memIds;
    }*/
    public String getLimitMemIds() {
		return limitMemIds;
	}

	public void setLimitMemIds(String limitMemIds) {
		this.limitMemIds = limitMemIds;
	}
	
	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

	private void beforeHandling() throws SQLException {

    	/*Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
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
    			
    			String memId= rs.getString(1);
    			String memGroupId= rs.getString(2);

    			seqno = "01";
    			Statement stmt1 = null;
    			ResultSet rs1 = null;
    			
    			String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
    							"WHERE MEM_ID = '" +  memId + "'"+
	                      		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(BmemR_FN) + 
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
    			memIds.add(memId);
//    			memId2Seqno.put(memId , seqno);       
          	    //--
    		}
    		log.debug("memIds="+memIds);
    	}
    	finally {
    		ReleaseResource.releaseDB(conn, stmt, rs);
    	}*/
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
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
    	try {
    		conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    		
    		for(String fileName : fileNames) {
	        	
    			StringBuffer sql = new StringBuffer();
    	        
    	        sql.append(" SELECT MEM_GROUP_ID, MEM_ID, SEQNO, FILE_DATE, FULL_FILE_NAME, TOT_REC, SUC_CNT, FAIL_CNT, RCODE FROM TB_INCTL");
    	        sql.append(" WHERE FILE_NAME = ").append(StringUtil.toSqlValueWithSQuote(fileName));
    	        sql.append(" AND START_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
    	        sql.append(" AND NOT EXISTS(");
    	        sql.append(" SELECT 1 FROM TB_OUTCTL");
    	        sql.append(" WHERE FILE_NAME = ").append(StringUtil.toSqlValueWithSQuote(fileName.replaceAll(BMEM_FN, BMEMR_FN)));
    	        sql.append(" AND TB_OUTCTL.MEM_ID = TB_INCTL.MEM_ID");
    	        sql.append(" AND TB_OUTCTL.FILE_DATE = TB_INCTL.FILE_DATE");
    	        sql.append(" AND TB_OUTCTL.SEQNO = TB_INCTL.SEQNO)");
    			
    			try {
    				stmt = conn.createStatement();
    				log.debug("sql: "+sql.toString());
    				rs = stmt.executeQuery(sql.toString());
    				
    				while (rs.next()) {

    	            	  String memGroupId = rs.getString("MEM_GROUP_ID");
    	            	  String memId = rs.getString("MEM_ID");
    	            	  String seqno = rs.getString("SEQNO");
    	            	  String fileDate = rs.getString("FILE_DATE");
    	            	  String fullFileName = rs.getString("FULL_FILE_NAME");
    	            	  
    	            	  if(!memIdList.contains(memId)) {
    	            		  continue;
    	            	  }
    	                  ExpFileInfo expFileInfo = new ExpFileInfo();
    	                  expFileInfo.setMemId(memId);
    	                  expFileInfo.setMemGroupId(memGroupId);
    	                  expFileInfo.setFileName(fileName.replaceAll(BMEM_FN, BMEMR_FN));
    	                  expFileInfo.setFileDate(fileDate);
    	                  expFileInfo.setSeqno(seqno);

    	                  String fullFileNameR = BMEMR_APPEND + "." +memId + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
    	                  expFileInfo.setFullFileName(fullFileNameR);
    	                  
    	                  int totCnt = Integer.valueOf(rs.getString("TOT_REC")!=null?rs.getString("TOT_REC"):"0");
    	                  int failCnt = Integer.valueOf(rs.getString("FAIL_CNT"));
    	                  int sucCnt = Integer.valueOf(rs.getString("SUC_CNT"));
    	                  String rCode = rs.getString("RCODE");
    	                  
    	                  expFileInfo.setInctlTotCnt(totCnt);
    	                  expFileInfo.setInctlSucCnt(sucCnt);
    	                  expFileInfo.setInctlFailCnt(failCnt);
    	                  expFileInfo.setInctlRCode(rCode);
    	                  
    	                  StringBuffer selectSQL = new StringBuffer();
    	                  selectSQL.append(" SELECT MESSAGE, RCODE FROM TB_IMPORT_DATA_LOG");
    	                  selectSQL.append(" WHERE FILE_NAME = ").append(StringUtil.toSqlValueWithSQuote(fileName));
    	        		  selectSQL.append(" AND FULL_FILE_NAME = ").append(StringUtil.toSqlValueWithSQuote(fullFileName));
    					  selectSQL.append(" ORDER BY LINE_NO");
    	                  
    	                  log.debug("selectSQL: " + selectSQL.toString());

    	                  expFileInfo.setSelectSQL(selectSQL.toString());
    	                  efs.addExpFileInfo(expFileInfo);
    	             }
	        	}
	        	finally {
	        		ReleaseResource.releaseDB(null, stmt, rs);
	        	}
    		}
    	}
    	finally {
    		ReleaseResource.releaseDB(conn);
    		return efs;
		}
        
    }

    public String outputAfterFile() {
        return super.outputAfterFile();
    }

    public String outputBeforeFile() {    	

    	
        StringBuffer header = new StringBuffer();
        header.append("H0");
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append("N");
            
        log.info("fail count: " + expFileInfo.getInctlFailCnt() + ", rcode: " + expFileInfo.getInctlRCode());
        if(expFileInfo.getInctlFailCnt() != 0 || !expFileInfo.getInctlRCode().equals("0000")){	
        	header.append("2999");	
        }
        else {
        	//header.append(StringUtils.rightPad("", 16, '0'));
        	header.append("0000");
        }
        
        //減掉檔頭固定長度25
        int spaceLenth = expFileInfo.getFileInfo().getDataLen().intValue() - 25;
        header.append(StringUtils.rightPad("", spaceLenth, ' '));
        
        return header.toString();
    }
    
    public String outputEndFile() {    	
    	//96
        StringBuffer header = new StringBuffer();
        /*header.append("T");
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.leftPad( takeDecimal(String.valueOf(total),2), 14, '0'));
        header.append(StringUtils.rightPad("", 73, ' '));*/
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

    public String outputOneRecord(List record) {         // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append(record.get(0).toString().substring(0, record.get(0).toString().length()-4));
        if(expFileInfo.getInctlRCode().equals("2999")) {
        	sb.append("2999");
        }
        else {
        	sb.append(record.get(1).toString());
        }

        return sb.toString();
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	private List getMemIdList() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<String> memIdList = null;
		StringBuffer sqlCmd = new StringBuffer();
		if (getBatchResultInfo() != null) {
			sqlCmd.append("SELECT MEM_ID FROM TB_MEMBER WHERE 1=1 ");
			if (Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())) {
				sqlCmd.append(" AND JOB_ID IS NULL");
				sqlCmd.append(" AND JOB_TIME IS NULL");
			} else {
				if (!StringUtil.isEmpty(getBatchResultInfo().getJobId())
						&& !getBatchResultInfo().getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)) {
					sqlCmd.append(" AND JOB_ID=")
							.append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobId()));
					if (!StringUtil.isEmpty(getBatchResultInfo().getJobTime())
							&& !getBatchResultInfo().getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)) {
						sqlCmd.append(" AND JOB_TIME=")
								.append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobTime()));
					}
				}
				if (!StringUtil.isEmpty(getBatchResultInfo().getMemId())) {
					sqlCmd.append(" AND MEM_ID=")
							.append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getMemId()));
				}
			}
		} else {
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
			memIdList = new ArrayList<String>();
			log.info("catch: memIdList is null!");
		} finally {
			ReleaseResource.releaseDB(conn, stmt, rs);
			return memIdList;
		}
	}
	public static ExpBmemR getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpBmemR instance = (ExpBmemR) apContext.getBean("expBmemR");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    public static void main(String[] args) {
    	ExpBmemR expBmemR = null;
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
            	expBmemR = getInstance();
            	expBmemR.setBatchDate(batchDate);
                expBmemR.setLimitMemIds(limitMemIds);
                // 註: 此 method 一定要先呼叫
                expBmemR.beforeHandling();
                expBmemR.run(args);
            }
            else {
            	log.info(SPRING_PATH + " does not exist.");
            }
            
        }
        catch (Exception ignore) {
            log.warn("ExpBmemR run fail:" + ignore.getMessage(), ignore);
        }
    }
}
