/**
 * changelog ExpImportReturn
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpImpReturn;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.svc.yhdp.batch.expfiles.ExpImpReturn.AbstractExpFile;
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
public class ExpImportReturn extends AbstractExpFile { 
    private static Logger log = Logger.getLogger(ExpImportReturn.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpImpReturn" + File.separator + "spring.xml";

    // 產生檔案的 encoding
    private String encoding = "";
    //檔案SIZE檢核
    private boolean checkFlag = true;
	// 要匯出會員檔的 member
    private List memIds = null;
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    private HashMap memId2MemGroupId = new HashMap();
    private String memId = "";
    private String memGroupId = "";


	private TbFileInfoInfo fileInfo = null;
    private TbInctlInfo inctlInfo = null;

	private Connection conn = null;
    public ExpImportReturn() {
    }
    
    public void setEncoding(String encoding) 
    {
        try {
            "".getBytes(encoding);
        }
        catch (Exception ignore) {
            // invalid encoding
            encoding = "UTF-8";
        }
        this.encoding = encoding;
    }
    
    public boolean isCheckFlag() 
    {
		return checkFlag;
	}

	public void setCheckFlag(boolean checkFlag) 
	{
		this.checkFlag = checkFlag;
	}

    public List getMemIds() 
    {
        return memIds;
    }

    public void setMemIds(List memIds) 
    {
        this.memIds = memIds;
    }
    
    public String getMemGroupId() {
		return memGroupId;
	}

	public void setMemGroupId(String memGroupId) 
	{
		this.memGroupId = memGroupId;
	}
    public String getMemId() 
    {
		return memId;
	}

	public void setMemId(String memId) 
	{
		this.memId = memId;
	}
	
	protected static String normalFileSeparator(String fn) 
	{
        return fn.replace('\\', '/');
    }

	private void beforeHandling() throws Exception 
	{
		Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
		Statement stmt = null;
		ResultSet rs = null;
		
		String sb = "SELECT MEM_ID, MEM_GROUP_ID FROM TB_MEMBER ";
		
		try {
			stmt = conn.createStatement();
		    log.debug("sb: "+sb);
		    rs = stmt.executeQuery(sb);
		    while (rs.next()){
		    	memId2MemGroupId.put(rs.getString(1),rs.getString(2));
		    }
		    
	    	if(memId2MemGroupId.size() == 0){
	    		throw new Exception("There is no match memId and memGroupId");
	    	}
		    	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				throw new SQLException(e.getMessage());
			}
			finally {
				log.info("memId2MemGroupId: " + memId2MemGroupId);  
				ReleaseResource.releaseDB(null, stmt, rs);
			}
	}

    public ExpFileSetting makeExpFileSetting() throws SQLException  
    {
    	/*For分收單*/
		List memIdList = getMemIdList();
		
        ExpFileSetting efs = new ExpFileSetting();
        // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
        // checkFlag:boolean:true
        // 所以若產生的檔案是 fixed length, 請設為 true, 若是 variable length, 請設為 false, default is true
        efs.setCheckFlag(checkFlag);
        efs.setCheckOutctlFlag(false);
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
        
		conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
		
        Statement stmt = null;
        ResultSet rs = null;
        try {    
        	stmt = conn.createStatement();
		
        	//String sql="SELECT * from tb_file_info WHERE in_out='I' AND Return_File_Name is not null";
        	StringBuffer sql= new StringBuffer();
        	/*sql.append("select ti.*,RETURN_FILE_NAME FROM TB_INCTL ti,");
        	sql.append("(select fi.FILE_NAME, fi.LOCAL_PATH, fi.RETURN_FILE_NAME, rf.MEM_ID");
        	sql.append(" from TB_RESP_FILE_CONFIG rf, TB_FILE_INFO fi");
        	sql.append(" where rf.FILE_NAME=fi.FILE_NAME and RESP_FLAG='1') tfi");
        	sql.append(" where ti.FILE_NAME=tfi.FILE_NAME and START_DATE='"+batchDate+"'");*/
        	
        	sql.append(" SELECT TI.MEM_ID, TI.FILE_NAME, RETURN_FILE_NAME, FILE_DATE, SEQNO,");
        	sql.append(" FULL_FILE_NAME, TOT_REC, SUC_CNT, FAIL_CNT, RCODE");
        	sql.append(" FROM TB_INCTL TI,");
        	sql.append(" (SELECT FI.FILE_NAME, FI.LOCAL_PATH, FI.RETURN_FILE_NAME, RF.MEM_ID");
        	sql.append(" FROM TB_RESP_FILE_CONFIG RF, TB_FILE_INFO FI");
        	sql.append(" WHERE RF.FILE_NAME=FI.FILE_NAME");
        	sql.append(" AND RESP_FLAG = '1') TFI");
        	sql.append(" WHERE TI.FILE_NAME=TFI.FILE_NAME");
        	sql.append(" AND START_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        	
            log.debug("Sql: "+sql);
            rs = stmt.executeQuery(sql.toString());
                   	
            while (rs.next()) {
            	 //String memId = (String) memIds.get(0);
                 //String seqno = (String) memId2Seqno.get(memId);
                 //Integer count = (Integer) memId2Count.get(memId);
                 /*if (seqno == null) {
                     log.warn("can't find in memId2Seqno or memId2Count, ignore for '" + memId + "'");
                     continue;
                 }*/
            	 String memId = rs.getString("MEM_ID");
            	 if (!memIdList.contains(memId)){
 					continue;
 				 }
                 ExpFileInfo expFileInfo = new ExpFileInfo();
                 expFileInfo.setMemGroupId((String) memId2MemGroupId.get(memId));
                 expFileInfo.setMemId(rs.getString("MEM_ID"));
                 expFileInfo.setFileName(rs.getString("RETURN_FILE_NAME"));
                 expFileInfo.setFileDate(rs.getString("FILE_DATE"));
                 expFileInfo.setSeqno(rs.getString("SEQNO"));
                 
                 //int fail_cnt = Integer.valueOf(rs.getString("fail_cnt")!=null?rs.getString("fail_cnt"):"0");
                 int totCnt = Integer.valueOf(rs.getString("TOT_REC")!=null?rs.getString("TOT_REC"):"0");
                 int failCnt = Integer.valueOf(rs.getString("FAIL_CNT"));
                 int sucCnt = Integer.valueOf(rs.getString("SUC_CNT"));
                 String rCode = rs.getString("RCODE");
                 
                 expFileInfo.setInctlTotCnt(totCnt);
                 expFileInfo.setInctlSucCnt(sucCnt);
                 expFileInfo.setInctlFailCnt(failCnt);
                 expFileInfo.setInctlRCode(rCode);
                
                 // memId 若不是同樣長度時會有問題
                 //String fullFileName = rs.getString("RETURN_FILE_NAME") + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
                 String fullFileName = rs.getString("FULL_FILE_NAME").replace(rs.getString("FILE_NAME"),
                		 rs.getString("RETURN_FILE_NAME"));
                 // String fullFileName = rs.getString("FULL_FILE_NAME").replace(rs.getString("FILE_NAME"), rs.getString("RETURN_FILE_NAME")).replace(rs.getString("FILE_DATE"), batchDate);

                 expFileInfo.setFullFileName(fullFileName);
                 String selectSQL ="select MESSAGE, RCODE from TB_IMPORT_DATA_LOG where FULL_FILE_NAME='"+rs.getString("FULL_FILE_NAME")+"' and FILE_DATE='"+rs.getString("FILE_DATE")+"' order by LINE_NO";
                 
                 expFileInfo.setSelectSQL(selectSQL);
                 
                 expFileInfo.setRecordsPerFile(totCnt);
               
                 efs.addExpFileInfo(expFileInfo);
            }
        }
        finally {
              ReleaseResource.releaseDB(conn, stmt, rs);
        }
        return efs;
    }

    public String outputAfterFile() 
    {
        return super.outputAfterFile();
    }

    public String outputBeforeFile() 
    {    	
        StringBuffer header = new StringBuffer();
        header.append("H0");
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
            
        log.info("fail count: " + expFileInfo.getInctlFailCnt() + ", rcode: " + expFileInfo.getInctlRCode());
        if(expFileInfo.getInctlFailCnt() != 0 || !expFileInfo.getInctlRCode().equals("0000")){	
        	header.append("2999");	
        }
        else {
        	//header.append(StringUtils.rightPad("", 16, '0'));
        	header.append("0000");
        }
        
        //減掉檔頭固定長度24
        int spaceLenth = expFileInfo.getFileInfo().getDataLen().intValue() - 24;
        header.append(StringUtils.rightPad("", spaceLenth, ' '));
        
        return header.toString();
    }

    public String outputEndFile() {    	
       /* StringBuffer header = new StringBuffer();
        header.append("/EOF");
        header.append(StringUtils.rightPad("", 64, ' '));
        return header.toString();*/
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
	
	public static ExpImportReturn getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpImportReturn instance = (ExpImportReturn) apContext.getBean("expImportReturn");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}

    public static void main(String[] args) {
    	ExpImportReturn expImportReturn = null;
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
            	expImportReturn = getInstance();
            }
            else {
            	expImportReturn = new ExpImportReturn();
            }
            if(args.length>0){
            	batchDate=args[0];
    		}
            expImportReturn.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            expImportReturn.beforeHandling();
            expImportReturn.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpMemberData run fail:" + ignore.getMessage(), ignore);
        }
    }
}
