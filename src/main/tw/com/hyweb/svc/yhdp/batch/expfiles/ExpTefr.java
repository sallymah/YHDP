package tw.com.hyweb.svc.yhdp.batch.expfiles;

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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jpos.iso.ISODate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnPK;
import tw.com.hyweb.service.db.info.TbSettleResultInfo;
import tw.com.hyweb.service.db.mgr.TbOnlTxnMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.expfiles.AbstractByteExpFile;

import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl.FF11;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl.MsgUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ExpTefr extends AbstractByteExpFile {
    private static Logger log = Logger.getLogger(ExpTefr.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpTefr" + File.separator + "spring.xml";
    private static final String TEFR_FN = "TEFR";
    private static final String TEFR_APPEND = "TEFR";
    private String filenameBean = "";
    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List memIds = null;
    // key:String(memId), value:String(seqno)
    //private HashMap memId2Seqno = new HashMap();
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    //private HashMap memId2Count = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    
    private HashMap memId2FundType = new HashMap();
    
    // 批次執行時間
    private String batchTime = "";
    
    private String recordsPerFile = "-1";
    
    //SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE='1'
    private List memIdbyTypeList = null;
    
    private int cnt = 0;
    private int sumAmt = 0;
    
    private Connection Connection = null;
    
    private HashMap memId2MemGroupId = new HashMap();
    
    public ExpTefr() {
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

    public List getMemIds() {
        return memIds;
    }

    public void setMemIds(List memIds) {
        this.memIds = memIds;
    }
  
    public String getRecordsPerFile() {
		return recordsPerFile;
	}

	public void setRecordsPerFile(String recordsPerFile) {
		this.recordsPerFile = recordsPerFile;
	}

	public String getFilenameBean() {
		return filenameBean;
	}

	public void setFilenameBean(String filenameBean) {
		this.filenameBean = filenameBean;
	}

	private void beforeHandling() throws SQLException { 
        
        String seqnoSql = "SELECT MEM_ID, MEM_GROUP_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE='1'";
        Statement stmt = null;
        ResultSet rs = null;
        memIdbyTypeList = new ArrayList();
        try {
        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        	stmt = conn.createStatement();
        	log.debug("seqnoSql: "+seqnoSql);
        	rs = stmt.executeQuery(seqnoSql);
        	while (rs.next()) {
        		
        		String memId= rs.getString(1);
    			String memGroupId= rs.getString(2);
        		
        		memIdbyTypeList.add(memId);
        		memId2MemGroupId.put(memId, memGroupId);
        		
        	}
        }
        finally {
            ReleaseResource.releaseDB(conn, stmt, rs);
        }
        
        
        String settleSql = "SELECT ACQ_MEM_ID, P_CODE, FUND_TYPE FROM TB_SETTLE_CONFIG, TB_SETTLE_TXN, TB_MEMBER " +
        		"WHERE ACQ_MEM_ID = MEM_ID " +
        		"AND TB_SETTLE_CONFIG.SETTLE_CODE = TB_SETTLE_TXN.SETTLE_CODE " +
        		"AND SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE='1' " +
        		"AND P_CODE IN ('7707','7647') ";
        
        Statement settleStmt = null;
        ResultSet settleRs = null;
        
        try {
        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        	settleStmt = conn.createStatement();
        	log.debug("seqnoSql: "+settleSql);
        	settleRs = settleStmt.executeQuery(settleSql);
        	while (settleRs.next()) {
        		memId2FundType.put(settleRs.getString(1)+settleRs.getString(2), settleRs.getString(3));
        	}
        }
        finally {
            ReleaseResource.releaseDB(conn, settleStmt, settleRs);
        }
        
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
        String seqnoSql = "SELECT FULL_FILE_NAME FROM TB_TMP_TRANS " +
        				"WHERE EXPORT_DATE = '00000000' " +
        				" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(filenameBean)+
                		" AND IMP_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate)+
//                		" AND EXISTS (SELECT 1 FROM TB_INCTL "+
//                		"WHERE MEM_ID IN (SELECT MEM_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE='1') "+
//                		"AND FULL_FILE_NAME= TB_TMP_TRANS.FULL_FILE_NAME) " +
                		"GROUP BY FULL_FILE_NAME";
        try {
        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        	stmt = conn.createStatement();
              log.debug("seqnoSql: "+seqnoSql);
              rs = stmt.executeQuery(seqnoSql);
              while (rs.next()) {
            	  //TEFR.MMMMMMMM.YYYYMMDDNN
            	  String fullFileName = rs.getString(1);
            	  String fullFileNameR = fullFileName.replaceAll("TEFD",TEFR_APPEND);            	  
            	  String seqno = fullFileName.substring(22);
            	  String memId = fullFileName.substring(5,13);
            	  if (!memIdbyTypeList.contains(memId)){
            		  continue;
            	  }
            	  if (!memIdList.contains(memId)){
            		  continue;
            	  }
            	  String memGroupId = memId2MemGroupId.get(memId).toString();
            	  String fileDate = fullFileName.substring(14,22);
                  ExpFileInfo expFileInfo = new ExpFileInfo();
                  expFileInfo.setMemId(memId);
                  expFileInfo.setMemGroupId(memGroupId);
                  expFileInfo.setFileName(TEFR_FN);
                  expFileInfo.setFileDate(fileDate);
                  expFileInfo.setSeqno(seqno);

                  expFileInfo.setFullFileName(fullFileNameR);
                  String selectSQL ="SELECT LINE_DATA, RCODE FROM TB_TMP_TRANS " +
                  		" WHERE EXPORT_DATE = '00000000' " +
                  		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(filenameBean)+
                  		" AND FULL_FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fullFileName) + 
                  		" AND IMP_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate) + 
                  		" ORDER BY SEQNO";
                  
                  log.debug("selectSQL: " + selectSQL);

                  expFileInfo.setSelectSQL(selectSQL);
                  efs.addExpFileInfo(expFileInfo);
             }
        }
        finally {
              ReleaseResource.releaseDB(conn, stmt, rs);
              return efs;
        }
    }

    public byte[] outputBeforeFile() {    	

        StringBuffer header = new StringBuffer();
        header.append("H");
        header.append(TEFR_FN);
        header.append(DateUtil.getTodayString().substring(0, 8));
        header.append(DateUtil.getTodayString().substring(8,14));
        header.append(StringUtils.rightPad("", 19, ' '));
        return header.toString().getBytes();
    }
    
    public byte[] outputAfterFile() {
    	//24
    	StringBuffer header = new StringBuffer();
    	header.append("T");
    	header.append(StringUtil.pendingKey(cnt, 8));
    	header.append(StringUtil.pendingKey(sumAmt, 15));
    	header.append(StringUtils.rightPad("", 14, ' '));
    	return header.toString().getBytes();
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
    			cnt = 0;
    			sumAmt = 0;
    			
    			Object[] values = new Object[1];
    	        values[0] = expFileInfo.getFullFileName().replaceAll(TEFR_APPEND,"TEFD");    	
    	    	
    	        String SQL = "UPDATE TB_TMP_TRANS SET EXPORT_DATE = '" + ISODate.formatDate(new Date(), "yyyyMMdd") +"' " +
    	        		"WHERE EXPORT_DATE = '00000000' " +
    	        		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(filenameBean)+
    	        		" AND FULL_FILE_NAME = ?" + 
    	          		" AND IMP_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
    	        
    	    	executeUpdate(Connection, SQL, values);
    	    	
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
    	String RCODE = "";
    	String PROC_DATE = "00000000";
    	String EXP_PAY_DATE = "00000000";
    	
    	
    	int amt = 0;
    	
    	LINE_DATA = record.get(0).toString();
    	RCODE = record.get(1).toString();
    	
    	
    	log.debug("LINE_DATA: "+ LINE_DATA);
    	//BerTLV berTLV = BerTLV.createInstance(ISOUtil.hex2byte(LINE_DATA.substring(0, LINE_DATA.indexOf("FF21"))));
    	BerTLV berTLV = null;
    	
    	TbSettleResultInfo tbSettleResultInfo = null;
    	
    	if ( !RCODE.equals("0000")){
    		EXP_PAY_DATE = "00000000";
    	}
    	else{
    		try{
    			berTLV = BerTLV.createInstance(ISOUtil.hex2byte(LINE_DATA));
    			
    			log.debug("FF11: " + berTLV.getHexStr(0xFF11));
        		FF11 ff11 = new FF11(MsgUtil.FF11_SIZE, berTLV.getHexStr(0xFF11));
    	        tbSettleResultInfo = getSettleResult(ff11);
    	        if ( tbSettleResultInfo != null ){
    	        	PROC_DATE = tbSettleResultInfo.getProcDate();
    	        	EXP_PAY_DATE = StringUtil.isEmpty(tbSettleResultInfo.getExpPayDate()) ? "00000000" : tbSettleResultInfo.getExpPayDate();
	    	        amt = tbSettleResultInfo.getSettleAmt().intValue();
    	        }
    	        else if ( tbSettleResultInfo == null 
    	        		&& (ff11.getPcode().equals("7707") || ff11.getPcode().equals("7708") 
    	        				|| ff11.getPcode().equals("7647") || ff11.getPcode().equals("7648") )){
    	        	
    	        	TbOnlTxnInfo tbOnlTxnInfo = getOnlTxn(ff11);
    	        	if ( tbOnlTxnInfo != null ){
    	        		
    	        		String fundType = "";
    	        		if (ff11.getPcode().equals("7707") || ff11.getPcode().equals("7708"))
    	        			fundType = memId2FundType.get(tbOnlTxnInfo.getAcqMemId() + "7707" ).toString();
    	        		else if ( ff11.getPcode().equals("7647") || ff11.getPcode().equals("7648"))
    	        			fundType = memId2FundType.get(tbOnlTxnInfo.getAcqMemId() + "7647").toString();
    	        		
    	        		String expPayDate = Layer2Util.getCycleDate(conn, tbOnlTxnInfo.getTermSettleDate(),fundType);
    	        		
    	        		if (!isBlankOrNull(expPayDate)){
    	        			PROC_DATE = tbOnlTxnInfo.getTermSettleDate();
    	        			EXP_PAY_DATE = expPayDate;
    	        		}
    	        		amt = tbOnlTxnInfo.getTxnAmt().intValue();
    	        		
        	        }
    	        }
        	}
        	catch(Exception e) {
                log.warn("unpack error: ["+LINE_DATA+"]" + e);
            }
    	}
        		
    	List<byte[]> byteList = new ArrayList();
    	
    	byteList.add("D01".getBytes());
    	byteList.add(convertHexToString(LINE_DATA));
    	byteList.add(RCODE.getBytes());
    	byteList.add(PROC_DATE.getBytes());
    	byteList.add(EXP_PAY_DATE.getBytes());
    	
    	int len = 0;
    	for (byte[] srcArray : byteList)
			len += srcArray.length;

		byte[] macArray = new byte[len];

		int destLen = 0;

		for (byte[] srcArray : byteList) {
			System.arraycopy(srcArray, 0, macArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
    	
		cnt++;
		sumAmt = sumAmt + amt;
		
        return macArray;
    }
    
    public TbSettleResultInfo getSettleResult(FF11 ff11) throws Exception{
        // in here, tmp is the only one fileName
    	
    	TbSettleResultInfo tbSettleResultInfo = null;
    	
    	String seqnoSql = "SELECT PROC_DATE, EXP_PAY_DATE, SETTLE_AMT " +
    			"FROM TB_SETTLE_RESULT " +
    			//卡號對方會加FF，需慮掉
    			"WHERE CARD_NO = '" + ff11.getCardNo().replaceAll("F", "") + "' " +
				"AND EXPIRY_DATE = '" + ff11.getCardExp() + "' " +
				"AND LMS_INVOICE_NO = '" + ff11.getInvoiceNo() + "' " +
				"ORDER BY EXP_PAY_DATE";
    	log.debug(seqnoSql);
    	
        Statement stmt = null;
        ResultSet rs = null;
        try {
        	stmt = conn.createStatement();
              log.debug("seqnoSql: "+seqnoSql);
              rs = stmt.executeQuery(seqnoSql);
              while (rs.next()) {
            	  tbSettleResultInfo = new TbSettleResultInfo();
            	  tbSettleResultInfo.setProcDate(rs.getString(1));
            	  tbSettleResultInfo.setExpPayDate(rs.getString(2));
	    	      tbSettleResultInfo.setSettleAmt(rs.getDouble(3));
	    	      break;
              }
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return tbSettleResultInfo;
    }
    
    public TbOnlTxnInfo getOnlTxn(FF11 ff11) throws Exception{
        // in here, tmp is the only one fileName
    	
    	TbOnlTxnInfo tbOnlTxnInfo = null;
    	
    	TbOnlTxnPK tbOnlTxnPK = new TbOnlTxnPK();
    	tbOnlTxnPK.setCardNo(ff11.getCardNo().replaceAll("F", ""));
    	tbOnlTxnPK.setExpiryDate(ff11.getCardExp());
    	tbOnlTxnPK.setLmsInvoiceNo(ff11.getInvoiceNo());
    	
    	TbOnlTxnMgr tbOnlTxnMrg = new TbOnlTxnMgr(conn);
    	tbOnlTxnInfo = tbOnlTxnMrg.querySingle(tbOnlTxnPK);
    	
        return tbOnlTxnInfo;
    }
    
    public void rollback()  throws Exception {
    	
    	if ( !Connection.isClosed() && Connection != null ){
    		Connection.rollback();
    		log.info("close Connection.");
    		Connection.close();
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

	public static ExpTefr getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpTefr instance = (ExpTefr) apContext.getBean("expTefr");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}

    public static void main(String[] args) {
    	ExpTefr expTefr = null;
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
            	expTefr = getInstance();
            }
            else {
            	expTefr = new ExpTefr();
            }
            expTefr.setBatchDate(batchDate);
            expTefr.setBatchTime(DateUtil.getTodayString().substring(8,14));
            // 註: 此 method 一定要先呼叫
            expTefr.beforeHandling();
            expTefr.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpTefr run fail:" + ignore.getMessage(), ignore);
        }
    }
}