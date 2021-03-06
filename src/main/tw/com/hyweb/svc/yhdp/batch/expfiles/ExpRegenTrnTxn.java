package tw.com.hyweb.svc.yhdp.batch.expfiles;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import oracle.net.aso.g;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.expfiles.AbstractByteExpFile;

import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ExpRegenTrnTxn extends AbstractByteExpFile {
    private static Logger log = Logger.getLogger(ExpRegenTrnTxn.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpRegenTrnTxn" + File.separator + "spring.xml";
    private static final String R_TRN_TXN_FN = "R_TRN_TXN";
    private static final String R_TRN_TXN_APPEND = "R_TRN_TXN";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List regens = new ArrayList<>();
    
    // 批次執行時間
    private String batchTime = "";
    
    private String recordsPerFile = "-1";
    
    //須處理的FileName
    protected List filenameBeans = new ArrayList();
    private String filenames = "";
    
    private Connection Connection = null;
    
    private HashMap regens2SourceDate = new HashMap();
    
    public ExpRegenTrnTxn() {
    }
    
    public String getBatchTime() {
		return batchTime;
	}
	public void setBatchTime(String batchTime) {
		this.batchTime = batchTime;
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
    public String getRecordsPerFile() {
		return recordsPerFile;
	}
	public void setRecordsPerFile(String recordsPerFile) {
		this.recordsPerFile = recordsPerFile;
	}
	public List getFilenameBeans() {
		return filenameBeans;
	}
	public void setFilenameBeans(List filenameBeans) {
		this.filenameBeans = filenameBeans;
	}

	private void beforeHandling() throws SQLException {
		
		for(int i = 0; i < filenameBeans.size(); i++){
			filenames = filenames + StringUtil.toSqlValueWithSQuote(filenameBeans.get(i).toString());
			if ( i < filenameBeans.size()-1 ){
				filenames = filenames + ", ";
			}
		}
		
		StringBuffer sql = new StringBuffer();
		
		sql.append(" SELECT MEM_ID, MEM_GROUP_ID, REGEN_SOURCE_DATE FROM");
		sql.append(" (SELECT * FROM TB_TMP_TRANS");
		sql.append(" WHERE FILE_NAME IN (").append(filenames).append(")");
		sql.append(" AND RCODE = '1007'");
		sql.append(" AND REGEN_STATUS = '2'");
		sql.append(" AND ((APRV_DATE = ").append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-1)));
		sql.append(" AND UPT_SRC='U' ) OR (APRV_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
		sql.append(" AND UPT_SRC='B' ))");
		sql.append(" ) TMP_TRANS,");
		sql.append(" (SELECT * FROM TB_INCTL WHERE FILE_NAME IN (").append(filenames).append(")");
		sql.append(" ) INCTL");
		sql.append(" WHERE TMP_TRANS.FILE_NAME = INCTL.FILE_NAME");
		sql.append(" AND TMP_TRANS.FULL_FILE_NAME = INCTL.FULL_FILE_NAME");
		sql.append(" GROUP BY MEM_ID, MEM_GROUP_ID, REGEN_SOURCE_DATE");
		sql.append(" ORDER BY MEM_ID, MEM_GROUP_ID, REGEN_SOURCE_DATE");

		Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String orderMemId= "";
        String orderSeqno= "001";
        String seqno= "001";
        
        try {
        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        	stmt = conn.createStatement();
        	log.debug("sql: "+sql.toString());
        	rs = stmt.executeQuery(sql.toString());
        	while (rs.next()) {
        		
        		String memId= rs.getString(1);
        		String memGroupId= rs.getString(2);
    			String regenSourceDate = rs.getString(3);
    			List regen = new ArrayList<>();
        		
    			if(!memId.equalsIgnoreCase(orderMemId)){
    				seqno = getFileSeqNo(conn, memId);
    				orderMemId = memId;
    				orderSeqno = seqno;
    			}
    			else{
    				int nextSeqNo = Integer.parseInt(orderSeqno) + 1;
    				seqno = StringUtil.pendingKey(nextSeqNo, 3);
    				orderSeqno = seqno;
    			}
    			
    			regen.add(memId);
    			regen.add(memGroupId);
    			regen.add(seqno);
    			
    			regens.add(regen);
    			regens2SourceDate.put(regen, regenSourceDate);
        	}
        }
        finally {
            ReleaseResource.releaseDB(conn, stmt, rs);
        }
    }

	private String getFileSeqNo(Connection conn, String memId) throws SQLException 
    {
    	String seqno = "001";
    	Statement stmt = null;
        ResultSet rs = null;
    	String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '001'), 3, '0') FROM TB_OUTCTL" +
						" WHERE MEM_ID = '" + memId + "'"+
						" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(R_TRN_TXN_FN) + 
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
		List memIdList = getMemIdList();
		
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
        
        for (int i = 0; i < regens.size(); i++) {
            
        	List regen = (List) regens.get(i);
        	
        	String memId= (String) regen.get(0);
        	
        	if (!memIdList.contains(memId)){
				continue;
			}
        	String memGroupId= (String) regen.get(1);
        	String seqno = (String) regen.get(2);
			String regenSourceDate = (String) regens2SourceDate.get(regen);
        	
            //Integer count = (Integer) memId2Count.get(memId);
            if (seqno == null) {
                log.warn("can't find in memId2Seqno or memId2Count, ignore for '" + memId + "'");
                continue;
            }
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemId(memId);
            expFileInfo.setMemGroupId(memGroupId);
            expFileInfo.setFileName(R_TRN_TXN_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = R_TRN_TXN_FN + "." + expFileInfo.getMemId() + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            
            StringBuffer sql = new StringBuffer();
    		
    		sql.append(" SELECT LINE_DATA FROM");
    		sql.append(" (SELECT * FROM TB_TMP_TRANS");
    		sql.append(" WHERE FILE_NAME IN (").append(filenames).append(")");
    		sql.append(" AND RCODE = '1007'");
    		sql.append(" AND REGEN_STATUS = '2'");
    		sql.append(" AND ((APRV_DATE = ").append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-1)));
    		sql.append(" AND UPT_SRC='U' ) OR (APRV_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
    		sql.append(" AND UPT_SRC='B' ))");
    		sql.append(" AND REGEN_SOURCE_DATE = ").append(StringUtil.toSqlValueWithSQuote(regenSourceDate));
    		sql.append(" ) TMP_TRANS,");
    		sql.append(" (SELECT * FROM TB_INCTL");
    		sql.append(" WHERE FILE_NAME IN (").append(filenames).append(")");
    		sql.append(" AND MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(memId));
    		sql.append(" ) INCTL");
    		sql.append(" WHERE TMP_TRANS.FILE_NAME = INCTL.FILE_NAME");
    		sql.append(" AND TMP_TRANS.FULL_FILE_NAME = INCTL.FULL_FILE_NAME");

            if(i==0){
            	log.info(sql.toString());
            }
            expFileInfo.setSelectSQL(sql.toString());
            efs.addExpFileInfo(expFileInfo);
        }
        
        return efs;
    }
    
    public byte[] outputBeforeFile() {    	

    	List regen = new ArrayList<>();
    	regen.add(expFileInfo.getMemId());
		regen.add(expFileInfo.getMemGroupId());
		regen.add(expFileInfo.getSeqno());
    	
		List<byte[]> byteList = new ArrayList();
    	
    	byteList.add("01".getBytes());
    	byteList.add(regens2SourceDate.get(regen).toString().getBytes());
    	byteList.add(StringUtils.rightPad("", 40, '0').getBytes());
    	byteList.add(ISOUtil.hex2byte((StringUtils.rightPad("", 32, '0'))));
    	
    	
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
    
    public byte[] outputAfterFile() {
    	//24
    	/*StringBuffer header = new StringBuffer();
    	header.append("T");
    	header.append(StringUtil.pendingKey(cnt, 8));
    	header.append(StringUtil.pendingKey(sumAmt, 15));
    	header.append(StringUtils.rightPad("", 14, ' '));
    	return header.toString().getBytes();*/
    	return new byte[0];
    }

    public void actionsAfterInfo() throws Exception {
        // 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
        
        if ( Connection != null){
    		try {
    			Connection.commit();
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
    		 finally {
    			 log.info("close Connection.");
    			 Connection.close();
    		}
    	}
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
    	
    	if ( Connection != null){
    		try {
    			List regen = new ArrayList<>();
    	    	regen.add(expFileInfo.getMemId());
    			regen.add(expFileInfo.getMemGroupId());
    			regen.add(expFileInfo.getSeqno());
    			
    			
    			Object[] values = new Object[6];
    	        values[0] = expFileInfo.getFullFileName();
    	        values[1] = batchDate;
    	        values[2] = batchTime;
    	        values[3] = "3";
    	        values[4] = regens2SourceDate.get(regen);
        		values[5] = expFileInfo.getMemId();
    	    	
    	        StringBuffer updateSql = new StringBuffer();
    	        updateSql.append(" UPDATE TB_TMP_TRANS SET");
    	        updateSql.append(" REGEN_EXP_FILE_NAME= ?,");
    	        updateSql.append(" REGEN_EXP_FILE_DATE= ?,");
    	        updateSql.append(" REGEN_EXP_FILE_TIME= ?,");
    	        updateSql.append(" REGEN_STATUS= ?");
    	        updateSql.append(" WHERE FILE_NAME IN (").append(filenames).append(")");
    	        updateSql.append(" AND RCODE = '1007'");
    	        updateSql.append(" AND REGEN_SOURCE_DATE = ?");
    	        updateSql.append(" AND EXISTS ( SELECT 1 FROM TB_INCTL");
    	        updateSql.append(" WHERE FILE_NAME IN (").append(filenames).append(")");
    	        updateSql.append(" AND MEM_ID = ?");
    	        updateSql.append(" AND TB_TMP_TRANS.FILE_NAME = TB_INCTL.FILE_NAME");
    	        updateSql.append(" AND TB_TMP_TRANS.FULL_FILE_NAME = TB_INCTL.FULL_FILE_NAME)");
    	        
    	    	executeUpdate(Connection, updateSql.toString(), values);
    	    	
    	    	Connection.commit();
    	    	
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
    		 finally {
    			 //Connection.close();
    		}
    	}
    	
        super.actionsAfterFile();
    }

    public byte[] outputOneRecord(List record) throws Exception { 
    	
    	if ( Connection == null || Connection.isClosed()){
    		try {
    			log.info("open Connection.");
    			Connection = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    			Connection.setAutoCommit(false);
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
    	}

    	String LINE_DATA = "";
    	
    	LINE_DATA = record.get(0).toString();
    	
    	
    	log.debug("LINE_DATA: "+ LINE_DATA);
        		
    	List<byte[]> byteList = new ArrayList();
    	
    	byteList.add(ISOUtil.hex2byte((LINE_DATA)));

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
	 @SuppressWarnings("finally")
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
	public static ExpRegenTrnTxn getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpRegenTrnTxn instance = (ExpRegenTrnTxn) apContext.getBean("expRegenTrnTxn");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}

    public static void main(String[] args) {
    	ExpRegenTrnTxn expRegenTrnTxn = null;
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
            	expRegenTrnTxn = getInstance();
            }
            else {
            	expRegenTrnTxn = new ExpRegenTrnTxn();
            }
            expRegenTrnTxn.setBatchDate(batchDate);
            expRegenTrnTxn.setBatchTime(DateUtil.getTodayString().substring(8,14));
            // 註: 此 method 一定要先呼叫
            expRegenTrnTxn.beforeHandling();
            expRegenTrnTxn.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpRegenTrnTxn run fail:" + ignore.getMessage(), ignore);
        }
    }
}