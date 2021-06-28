/**
 * changelog ExpSettleOut
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpSettleOut;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.svc.yhdp.batch.expfiles.ExpSettleOut.AbstractExpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;

import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.DbUtil;
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
import java.util.Vector;

/**
 * <pre>
 * ExpAssociator
 * </pre>
 * author:duncan
 */
public class ExpSettleOut extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpSettleOut.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpSettleOut" + File.separator + "spring.xml";
    private static final String SVCSETL_FN = "SVCSETL";
    private static final String SVCSETL_APPEND = "SVCSETL";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List memGroupIds = null;
    // key:String(memId), value:String(seqno)
    private HashMap memId2Seqno = new HashMap();
    // key:String(memGroupId), value:String(carryType)
    private HashMap memGroupId2CarryType = new HashMap();

    private String carryType = "";
    private String carry = "";
    
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

	public List getMemGroupIds() {
        return memGroupIds;
    }

    public void setMemGroupIds(List memGroupIds) {
        this.memGroupIds = memGroupIds;
    }

	private void beforeHandling() throws SQLException {
		
		Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
        	StringBuffer memIGroupdSql = new StringBuffer();
        	memIGroupdSql.append("SELECT TB_MEMBER_GROUP.MEM_GROUP_ID, TB_MEMBER_GROUP.CARRY_TYPE FROM TB_MEMBER, TB_MEMBER_GROUP ");
        	memIGroupdSql.append("WHERE TB_MEMBER_GROUP.MEM_GROUP_ID = TB_MEMBER.MEM_GROUP_ID ");
        	
            if (memGroupIds != null && memGroupIds.size() > 0) {
            	memIGroupdSql.append("AND MEM_GROUP_ID IN (");
            	for ( int i = 0; i< memGroupIds.size(); i++ ){
            		memIGroupdSql.append("'").append(memGroupIds.get(i)).append("'");
            		if ( i < memGroupIds.size() -1 ){
            			memIGroupdSql.append(", ");
            		}
            	}
            	memIGroupdSql.append(") ");
            }
            memIGroupdSql.append("GROUP BY TB_MEMBER_GROUP.MEM_GROUP_ID, TB_MEMBER_GROUP.CARRY_TYPE ");
            log.debug(memIGroupdSql.toString());
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(memIGroupdSql.toString());
            
            while (rs.next()) {
                String memGroupId = rs.getString(1);
                String carryType = rs.getString(2);
                //int count = rs.getInt(2);
                String seqno = "01";
                Statement stmt2 = null;
                ResultSet rs2 = null;
                String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
                		"WHERE MEM_ID = " + StringUtil.toSqlValueWithSQuote(memGroupId + "000") + 
                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(SVCSETL_FN) + 
                		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
                try {
                    stmt2 = conn.createStatement();
                    rs2 = stmt2.executeQuery(seqnoSql);
                    while (rs2.next()) {
                        seqno = rs2.getString(1);
                    }
                }
                finally {
                    ReleaseResource.releaseDB(null, stmt2, rs2);
                }
                //memId2Count.put(memId, new Integer(count));
                memId2Seqno.put(memGroupId + "000", seqno);
                memGroupIds.add(memGroupId);
                memGroupId2CarryType.put(memGroupId, carryType);
            }
        }
        catch (SQLException se) {
            log.warn("beforeHandling() error:" + se.getMessage(), se);
            throw se;
        }
        finally {
            ReleaseResource.releaseDB(conn, stmt, rs);
        }
        log.info("memGroupIds:" + memGroupIds);
        log.info("memId2Seqno:" + memId2Seqno);
    }

    public ExpFileSetting makeExpFileSetting()
    {
    	/*For分收單*/
		List memGroupIdList = getMemGroupIdList();
		
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
        for (int i = 0; i < memGroupIds.size(); i++) {
            String memGroupId = (String) memGroupIds.get(i);
            if (!memGroupIdList.contains(memGroupId)){
				continue;
		 }
            String memId = memGroupId + "000";
            String seqno = (String) memId2Seqno.get(memId);
            //Integer count = (Integer) memId2Count.get(memId);
            if (seqno == null) {
                log.warn("can't find in memId2Seqno or memId2Count, ignore for '" + memId + "'");
                continue;
            }
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemGroupId(memGroupId);
            expFileInfo.setMemId(memId);
            expFileInfo.setFileName(SVCSETL_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = SVCSETL_APPEND + "."+ memGroupId +"." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL = "";
            selectSQL = "SELECT ACCOUT_CODE,TERM_SETTLE_DATE,PROC_DATE,AGENCY,ACQ_MEM_ID,CARD_CAT_ID," +
            		"PRODUCT_TYPE_ID,INDUSTRY_ID,NUM,CURR_CODE,EX_RATE,AMT,CARD_NO,TAX_FEE,EXP_PAY_DATE, AMT_NOTAX " +
            		"FROM TB_SETTLEOUT " +
            		"WHERE PROC_DATE = '" + batchDate + "' "+
            		"AND AMT <> 0 " +
            		"AND ACQ_MEM_ID IN ( SELECT MEM_ID FROM TB_MEMBER WHERE MEM_GROUP_ID = '"+memGroupId+"' )" ;
            log.info(selectSQL);
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
    	
    	carryType = (String) memGroupId2CarryType.get(expFileInfo.getMemGroupId());
    	carry = "";
    	
    	//四捨五入
    	if (carryType.equalsIgnoreCase("O")){
    		carry = "ROUND";
    	}
    	//無條件進位
    	else if (carryType.equalsIgnoreCase("U")){
    		carry = "CEIL";	
    	}
    	//無條件捨去
    	else if (carryType.equalsIgnoreCase("D")){
    		carry = "FLOOR";
		}
    	
    	String sumAmt = getTotalTxnAmt();
    	
        StringBuffer header = new StringBuffer();
        header.append("H0");
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.leftPad(takeDecimal(sumAmt,4),16,'0'));
        header.append(StringUtils.rightPad("", 107, ' '));
        return header.toString();
    }
    
    public String outputEndFile() {    	
    	//120
        StringBuffer header = new StringBuffer();
        header.append("/EOF");
        header.append(StringUtils.rightPad("", 137, ' '));
        
        return header.toString();
        
    }
    
    public void remarkExpData(Connection conn, TbOutctlInfo outctlInfo) {
    	String sql = "";
    	Statement stmt = null;
    	
		try {
			stmt = conn.createStatement();
			sql = "UPDATE TB_SETTLEOUT SET " +
					"CARRY_TYPE = '"+ memGroupId2CarryType.get(expFileInfo.getMemGroupId()) +"', " +
					"EXP_AMT = " + carry + "(ABS(CASE WHEN ACCOUT_CODE IN ('S101','S201','S103') THEN AMT_NOTAX ELSE AMT END))*( CASE WHEN (CASE WHEN ACCOUT_CODE IN ('S101','S201','S103') THEN AMT_NOTAX ELSE AMT END) > 0 THEN 1 ELSE -1 END ), " +
					"FILE_NAME = '"+ outctlInfo.getFullFileName() +"', " +
					"EXP_DATE = '"+ outctlInfo.getSysDate() +"', " +
					"EXP_TIME = '"+ outctlInfo.getSysTime() +"' " +
					"WHERE PROC_DATE = '" + batchDate + "' "+
            		"AND AMT <> 0 " +
            		"AND ACQ_MEM_ID IN ( SELECT MEM_ID FROM TB_MEMBER WHERE MEM_GROUP_ID = '"+expFileInfo.getMemGroupId()+"' )" ;
			
			log.debug("remarkExpData sql: "+sql);
			stmt.executeUpdate(sql);

		} catch (SQLException ignore) {
			log.warn("query totalCount fail for '" + sql + "':" + ignore.getMessage(), ignore);
		}
		finally {
            ReleaseResource.releaseDB(null, stmt, null);
		}
    	return;
	}
    
    public String getTotalTxnAmt(){
    	double totalTxnAmt = 0.0;
    	String txnAmtSQL = null;
    	
		try {
			txnAmtSQL = "SELECT SUM("+ carry +"(ABS(TXN_AMT))*( CASE WHEN TXN_AMT > 0 THEN 1 ELSE -1 END )) FROM ( "+
					"SELECT CASE WHEN ACCOUT_CODE IN ('S101','S201','S103') THEN AMT_NOTAX ELSE AMT END AS TXN_AMT "+
					"FROM ("+ expFileInfo.getSelectSQL() +"))";
			
			log.debug("txnAmtSQL: " + txnAmtSQL);
			Number txnAmt = DbUtil.getNumber(txnAmtSQL, conn);
			txnAmt = txnAmt != null ? txnAmt: 0;
			totalTxnAmt = txnAmt.doubleValue();
			//log.debug("totalTxnAmt: " + totalTxnAmt);

		} catch (SQLException ignore) {
			log.warn("query totalCount fail for '" + txnAmtSQL + "':" + ignore.getMessage(), ignore);
		}
		return String.valueOf(totalTxnAmt);
    }

    public void actionsAfterInfo() throws Exception {
        // 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        carryType = "";
        carry = "";
    	
        super.actionsAfterInfo();
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
        super.actionsAfterFile();
    }

    public String outputOneRecord(List record) { 

    	String accoutCode = "";
    	String termSettleDate = "";
    	String procDate = "";
    	String agency = "";
    	String acqMemId = "";
    	String cardCatId = "";
    	String productTypeId = "";
    	String industryid = "";
    	String num = "";
    	String currCode = "";
    	String exRate = "";
    	String amt = "";
    	String cardNo = "";
    	String taxFee = "";
    	String expPayDate = "";

    	accoutCode = record.get(0).toString();
    	termSettleDate = record.get(1).toString();
    	procDate = record.get(2).toString();
    	agency = record.get(3).toString();
    	acqMemId = record.get(4).toString();
    	cardCatId = record.get(5).toString();
    	productTypeId = record.get(6).toString();
    	if (!isBlankOrNull(record.get(7).toString()))
	    	industryid = record.get(7).toString();
    	if (!isBlankOrNull(record.get(8).toString()))
	    	num = record.get(8).toString();
    	if (!isBlankOrNull(record.get(9).toString()))
	    	currCode = record.get(9).toString();
    	if (!isBlankOrNull(record.get(10).toString()))
	    	exRate = record.get(10).toString();
    	if (!isBlankOrNull(record.get(11).toString()))
	    	amt = record.get(11).toString();
    	if ( !record.get(12).toString().equalsIgnoreCase("0000000000000000") )
    		cardNo = record.get(12).toString();
    	
    	if ( accoutCode.equals("S101") || 
			accoutCode.equals("S201") || 
			accoutCode.equals("S103") ){
    		if (!isBlankOrNull(record.get(13).toString()))
    			taxFee = record.get(13).toString();
    		//改成未稅金額
    		if (!isBlankOrNull(record.get(15).toString()))
    	    	amt = record.get(15).toString();
    	}
    	
    	//log.debug("carryType: " + carryType + "  amt: " + amt);
    	if (carryType.equalsIgnoreCase("U") ||
			carryType.equalsIgnoreCase("O") ||
			carryType.equalsIgnoreCase("D")){
    		amt = String.valueOf(Calc.roundFloat( Double.parseDouble(amt), 0, carryType));
    	}
    	//log.debug("amt: " + amt);
    	
    	if (!isBlankOrNull(record.get(14).toString()))
    		expPayDate = record.get(14).toString();
	    
        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(accoutCode,4,' '));
        sb.append(StringUtils.rightPad(termSettleDate,8,' '));
        sb.append(StringUtils.rightPad(procDate,8,' '));
        sb.append(StringUtils.rightPad(agency,1,' '));
        sb.append(StringUtils.rightPad(acqMemId,8,' '));
        sb.append(StringUtils.rightPad(cardCatId,5,'0'));
        sb.append(StringUtils.rightPad(productTypeId,5,'0'));
        sb.append(StringUtils.rightPad(industryid,2,'0'));
        sb.append(StringUtils.leftPad(num,13,'0'));
        sb.append(StringUtils.rightPad(currCode,3,' '));
        sb.append(StringUtils.leftPad(takeDecimal(exRate,5).replaceAll("\\.", ""),9,'0'));
        sb.append(StringUtils.leftPad(takeDecimal(amt,2).replaceAll("\\.", ""),13,'0'));
        sb.append(StringUtils.rightPad(cardNo,20,' '));
        sb.append(StringUtils.rightPad("", 2, ' '));
        sb.append(StringUtils.leftPad(takeDecimal(taxFee,2).replaceAll("\\.", ""),13,'0'));
        sb.append(StringUtils.rightPad(expPayDate,8,' '));
        sb.append(StringUtils.rightPad("", 17, ' '));
        
        return sb.toString();
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }
	 private List getMemGroupIdList()
	 {
		 Connection conn = null;
		 Statement stmt = null;
	     ResultSet rs = null;
		 ArrayList<String> memGroupIdList= null;				
		    	StringBuffer sqlCmd = new StringBuffer();
		    	if(getBatchResultInfo() != null){
		    		if(Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
		    			sqlCmd.append("SELECT MEM_GROUP_ID FROM TB_MEMBER");
			    		sqlCmd.append(" WHERE JOB_ID IS NULL");
			    		sqlCmd.append(" AND JOB_TIME IS NULL");
		    		}else{
			    		sqlCmd.append("SELECT MEM_GROUP_ID, JOB_ID, MAX(JOB_TIME) FROM TB_MEMBER WHERE 1=1 ");
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
						sqlCmd.append(" GROUP BY MEM_GROUP_ID, JOB_ID");
						if(!StringUtil.isEmpty(getBatchResultInfo().getJobId()) 
								&& !getBatchResultInfo().getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
						       if(!StringUtil.isEmpty(getBatchResultInfo().getJobTime()) 
						    		   && !getBatchResultInfo().getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
						    	   sqlCmd.append(" HAVING MAX(JOB_TIME)= ").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobTime()));
						       }
						}
		    		}
		    		
		    	}
				else{
					 log.warn("tbBatchResultInfo is null.");
				}
		    	try {
					conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
					memGroupIdList = new ArrayList<String>();
					stmt = conn.createStatement();
					log.debug("sqlCmd: " + sqlCmd.toString());
					rs = stmt.executeQuery(sqlCmd.toString());
					while (rs.next()) {
						memGroupIdList.add(rs.getString(1));
					}
				} catch (Exception e) {
					memGroupIdList= new ArrayList<String>();
					log.info("catch: bankIdList is null!");
				}
		    	finally {
					ReleaseResource.releaseDB(conn, stmt, rs);
					return  memGroupIdList;
				}										
	}

	public static ExpSettleOut getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpSettleOut instance = (ExpSettleOut) apContext.getBean("expSettleOut");
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
    	ExpSettleOut expSettleOut = null;
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
            	expSettleOut = getInstance();
            }
            else {
            	expSettleOut = new ExpSettleOut();
            }
                        
            expSettleOut.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            expSettleOut.beforeHandling();
            expSettleOut.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpAssociator run fail:" + ignore.getMessage(), ignore);
        }
    }

}
