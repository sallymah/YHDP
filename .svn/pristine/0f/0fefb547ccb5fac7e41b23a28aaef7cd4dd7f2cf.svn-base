package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTrn;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jpos.iso.ISODate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbSettleResultInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.traffics.FilenameBean;
import tw.com.hyweb.svc.yhdp.batch.framework.traffics.Members;
import tw.com.hyweb.svc.yhdp.batch.framework.traffics.TrnRcodes;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.FF11;

import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ExpTrn extends AbstractByteExpFile {
    private static Logger log = Logger.getLogger(ExpTrn.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpTrn" + File.separator + "spring.xml";
    
    // 產生檔案的 encoding
    private String encoding = "";
    
    // 存放如何 parse memId, fileDate, sno 的規則, 每個 TB_FILE_INFO.FILE_NAME 應該都有相對應的一筆設定
    // each object is FilenameBean object
    protected List filenameBeans = new ArrayList();
    
    // 批次執行時間
    private String batchTime = "";
    
    private String recordsPerFile = "-1";
    
    //SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE='1'
    //private List memIdbyTypeList = null;
    
    //
    private HashMap fullFileRName2fullFileName = new HashMap();
    
//    private int cnt = 0;
//    private int sumAmt = 0;
    
    private Connection Connection = null;
    
    protected final TrnRcodes trnRcodes;
    protected final Members members;
    
    public ExpTrn( TrnRcodes trnRcodes, Members members ) {
    	this.trnRcodes = trnRcodes;
    	this.members = members;
    }
    
    public List getFilenameBeans() {
		return filenameBeans;
	}

	public void setFilenameBeans(List filenameBeans) {
		this.filenameBeans = filenameBeans;
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

	private void beforeHandling() throws SQLException {
		
		/*Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        String SQL = "DELETE TB_TMP_TRANS " +
        		"WHERE EXPORT_DATE != '00000000' " +
          		"AND IMP_DATE <= " + StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-30));
    	executeUpdate(conn, SQL);
        conn.commit();*/

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
        efs.setTempFilePending(".tmp");
        // recordsPerFlush:int:-1
        // 產生檔案時處理多少筆記錄後做一次 flush 動作, <= 0 時不 enable flush 動作, default is -1
        efs.setRecordsPerFlush(-1);
        // recordsPerFile:int:-1
        // 多少筆產生一個 file, default 全部一個檔案(<= 0)
        // 若有設 recordsPerFile, 每個 expFileInfo 的 seqnoStart, seqnoEnd 一定要給
        efs.setRecordsPerFile(Integer.parseInt(recordsPerFile));
        efs.setLineSeparator("\r\n");
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {

        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        	
        	for (int i = 0; i < filenameBeans.size(); i++)
            {
            	FilenameBean filenameBean = (FilenameBean) filenameBeans.get(i);
            	filenameBean.setBatchDate(batchDate);
            	
            	
            	String fullFileNameSql = "SELECT TMP.FULL_FILE_NAME FROM TB_TMP_TRANS TMP, TB_ZIP_LOG ZIP "+
		            			"WHERE TMP.FILE_NAME = ZIP.FILE_NAME "+
		            			"AND TMP.FULL_FILE_NAME = ZIP.RM_NAME "+
		            			"AND PROC_DATE IS NULL " +
		            			"AND EXP_NAME IS NULL "+
		            			"AND EXPORT_DATE = '00000000' " +
                				"AND TMP.FILE_NAME = " + StringUtil.toSqlValueWithSQuote(filenameBean.getFileName())+
                        		" AND TMP.IMP_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate)+
                        		" GROUP BY TMP.FULL_FILE_NAME";
            	
            	stmt = conn.createStatement();
            	log.debug("fullFileNameSql: "+fullFileNameSql);
            	rs = stmt.executeQuery(fullFileNameSql);
            	while (rs.next()) {
            		//TEFR.MMMMMMMM.YYYYMMDDNN
            		String fullFileName = rs.getString(1);
            		filenameBean.clear();
            		filenameBean.initial(fullFileName, members.getSourdeId2Member());
            		if (isBlankOrNull(filenameBean.getMemId())){
            			log.warn("============ ["+fullFileName + "] find the corresponding member. ============");
            			continue;
            		}
            		if (!memIdList.contains(filenameBean.getMemId())){
    					continue;
    				}
            		ExpFileInfo expFileInfo = new ExpFileInfo();
            		expFileInfo.setMemId(filenameBean.getMemId());
            		expFileInfo.setMemGroupId(filenameBean.getMemGroupId());
            		expFileInfo.setFileName(filenameBean.getFileNameR());
            		expFileInfo.setFileDate(batchDate);
            		expFileInfo.setSeqno(filenameBean.getExpSeqno());

            		expFileInfo.setFullFileName(filenameBean.getFullFileNameR());
            		
            		String selectSQL ="SELECT LINE_DATA, RCODE, SEQNO, CARD_NO, LMS_INVOICE_NO FROM TB_TMP_TRANS " +
            						" WHERE EXPORT_DATE = '00000000' " +
            						" AND FULL_FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fullFileName) + 
            						" AND IMP_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate) + 
            						" ORDER BY SEQNO";
            		
            		log.debug("selectSQL: " + selectSQL);
	
            		fullFileRName2fullFileName.put(filenameBean.getFullFileNameR(), fullFileName);
            		
            		expFileInfo.setSelectSQL(selectSQL);
            		efs.addExpFileInfo(expFileInfo);
            	}
            }
        }
        catch (Exception ignore) {
        	log.warn("makeExpFileSetting error for '" + expFileInfo + "':" + ignore.getMessage(), ignore);
        }
        finally {
              ReleaseResource.releaseDB(conn, stmt, rs);
        }
        return efs;
    }

    public byte[] outputBeforeFile() {    	

    	//tem_trans 會有，這邊不填
    	return new byte[0];
    }
    
    public byte[] outputAfterFile() {

    	//tem_trans 會有，這邊不填
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
    			Object[] tmpValues = new Object[3];
    			
    			tmpValues[0] = ISODate.formatDate(new Date(), "yyyyMMdd");
    			tmpValues[1] = fullFileRName2fullFileName.get(expFileInfo.getFullFileName());
    			tmpValues[2] = batchDate;
    	    	
    	        String tmpSQL = "UPDATE TB_TMP_TRANS SET EXPORT_DATE = ? " +
    	        		"WHERE EXPORT_DATE = '00000000' " +
    	        		" AND FULL_FILE_NAME = ?" + 
    	          		" AND IMP_DATE = ? ";
    	        
    	    	executeUpdate(Connection, tmpSQL, tmpValues);
    	    	
    	    	
    	    	
//    	    	Object[] zipValues = new Object[5];
//    	    	
//    	    	zipValues[0] = expFileInfo.getFileName();
//    	    	zipValues[1] = expFileInfo.getFullFileName();
//    	    	zipValues[2] = ISODate.formatDate(new Date(), "yyyyMMdd");
//    	    	zipValues[3] = fullFileRName2fullFileName.get(expFileInfo.getFullFileName());
//    	    	zipValues[4] = batchDate;
//    	    	
//    	    	
//    	        String zipSQL = "UPDATE TB_ZIP_LOG SET " +
//    	        		"EXP_FILE_NAME = ?, " +
//    	        		"EXP_NAME = ?, " +
//    	        		"PROC_DATE = ? " +
//    	        		"WHERE EXP_NAME IS NULL " +
//    	        		"AND PROC_DATE IS NULL " +
//    	        		"AND RM_NAME = ? " + 
//    	          		"AND UNZIP_DATE = ? ";
    	    	
    	    	Object[] zipValues = new Object[4];
    	    	
    	    	zipValues[0] = expFileInfo.getFileName();
    	    	zipValues[1] = expFileInfo.getFullFileName();
    	    	zipValues[2] = ISODate.formatDate(new Date(), "yyyyMMdd");
    	    	zipValues[3] = fullFileRName2fullFileName.get(expFileInfo.getFullFileName());
    	    	
    	    	
    	        String zipSQL = "UPDATE TB_ZIP_LOG SET " +
    	        		"EXP_FILE_NAME = ?, " +
    	        		"EXP_NAME = ?, " +
    	        		"PROC_DATE = ? " +
    	        		"WHERE EXP_NAME IS NULL " +
    	        		"AND PROC_DATE IS NULL " +
    	        		"AND RM_NAME = ?";
    	        
    	    	executeUpdate(Connection, zipSQL, zipValues);
    	    	
    	    	
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

    	StringBuffer LINE_DATA = new StringBuffer();
    	String RCODE = "";
    	String SEQNO = "";
    	String CARD_NO = "";
    	String LMS_INVOICE_NO = "";
    	
    	
    	LINE_DATA.append(record.get(0).toString());
    	RCODE = record.get(1).toString();
    	SEQNO = record.get(2).toString();
    	if ( !StringUtil.isEmpty(record.get(3).toString()))
    		CARD_NO = record.get(3).toString();
    	if ( !StringUtil.isEmpty(record.get(4).toString()))
    		LMS_INVOICE_NO = record.get(4).toString();
        		
    	List<byte[]> byteList = new ArrayList();

    	
    	//如果是Head 或者 RCODE = 0000，直接匯出
    	if ( SEQNO.equalsIgnoreCase("1")){
    		;
		}
    	else{
    		if (RCODE.equalsIgnoreCase("0000")){
    			
    			if (!StringUtil.isEmpty(CARD_NO) && !StringUtil.isEmpty(LMS_INVOICE_NO)){
	    			String settleProcDate = getSettleResult(CARD_NO, LMS_INVOICE_NO);
	    			if ( !StringUtil.isEmpty(settleProcDate) ){
	    				LINE_DATA.replace(LINE_DATA.length()-100, LINE_DATA.length()-84, ISOUtil.hexString(settleProcDate.getBytes()));
	    			}
    			}
    		}
    		else{
    			LINE_DATA.replace(LINE_DATA.length()-2, LINE_DATA.length(), trnRcodes.getRcode2RespCode().get(RCODE).toString());
    		}
    	}
    		
    		
    	byteList.add(convertHexToString(LINE_DATA.toString()));

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
    
    public String getSettleResult(String cardNo, String lmsInvoiceNo) throws Exception{
        // in here, tmp is the only one fileName
    	
    	String seqnoSql = "SELECT PROC_DATE " +
    			"FROM TB_SETTLE_RESULT " +
    			//卡號對方會加FF，需慮掉
    			"WHERE CARD_NO = '" + cardNo + "' " +
				"AND LMS_INVOICE_NO = '" + lmsInvoiceNo + "' " +
				"ORDER BY EXP_PAY_DATE";
    	
        return DbUtil.getString(seqnoSql, conn);
    }
    
    
    public void rollback()  throws Exception {
    	
    	if ( Connection != null ){
    		if ( !Connection.isClosed() ){
				Connection.rollback();
				log.info("close Connection.");
				Connection.close();
    		}
    	}
    }
    
    public byte[] convertHexToString(String hex){
    	 
    	byte[] nameByteArray = ISOUtil.hex2byte(hex);
		ByteBuffer byteBuffer = ByteBuffer.allocate(nameByteArray.length + 8);
		byteBuffer.put(nameByteArray);
		
		return nameByteArray;
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
	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpTrn getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpTrn instance = (ExpTrn) apContext.getBean("expTrn");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}

    public static void main(String[] args) {
    	ExpTrn expTrn = null;
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
            	expTrn = getInstance();
            	expTrn.setBatchDate(batchDate);
                expTrn.setBatchTime(DateUtil.getTodayString().substring(8,14));
                // 註: 此 method 一定要先呼叫
                expTrn.beforeHandling();
                expTrn.run(args);
            }
            else {
            	log.error(SPRING_PATH + " is not exist. ");
            }
        }
        catch (Exception ignore) {
            log.warn("ExpTrn run fail:" + ignore.getMessage(), ignore);
        }
    }
}