package tw.com.hyweb.svc.yhdp.batch.expfiles;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.io.File;
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

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnPK;
import tw.com.hyweb.service.db.info.TbSettleResultInfo;
import tw.com.hyweb.service.db.mgr.TbOnlTxnMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.FF11;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.MsgUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ExpFMTefr extends AbstractExpFile {

	private static Logger log = Logger.getLogger(ExpFMTefr.class);
	private static final String SPRING_PATH = "config" + File.separator
			+ "batch" + File.separator + "expfiles" + File.separator
			+ "ExpFMTefr" + File.separator + "spring.xml";
	private static final String FMTEFR_FN = "FMTEFR";
	private static final String FMTEFR_APPEND = "FMTEFR";

	// 產生檔案的 encoding
	private String encoding = "";
	// 要匯出會員檔的 member
	private List memIds = null;
	// key:String(memId), value:String(seqno)
	private HashMap memId2Seqno = new HashMap();
	private String memId = "";
	private String filenameBean = "";
	private String recordsPerFile = "-1";

	private Integer recCnt = 0;
	private Integer sumAmt = 0;
	private List memIdbyTypeList = null;
	private HashMap memId2FundType = null;

	public ExpFMTefr() {
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		try {
			"".getBytes(encoding);
		} catch (Exception ignore) {
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

	public String getFilenameBean() {
		return filenameBean;
	}

	public void setFilenameBean(String filenameBean) {
		this.filenameBean = filenameBean;
	}

	public String getRecordsPerFile() {
		return recordsPerFile;
	}

	public void setRecordsPerFile(String recordsPerFile) {
		this.recordsPerFile = recordsPerFile;
	}

	private void beforeHandling() throws SQLException {
		String seqnoSql = "SELECT MEM_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE='1'";
		Statement stmt = null;
		ResultSet rs = null;
		memIdbyTypeList = new ArrayList();
		try {
			conn = DBService.getDBService().getConnection(
					Layer1Constants.DSNAME_BATCH);
			stmt = conn.createStatement();
			log.debug("seqnoSql: " + seqnoSql);
			rs = stmt.executeQuery(seqnoSql);
			while (rs.next()) {
				memIdbyTypeList.add(rs.getString(1));
			}
		} finally {
			ReleaseResource.releaseDB(conn, stmt, rs);
		}

		memId2FundType = new HashMap();
		String settleSql = "SELECT ACQ_MEM_ID, P_CODE, FUND_TYPE "
				+ "FROM TB_SETTLE_CONFIG, TB_SETTLE_TXN, TB_MEMBER "
				+ "WHERE ACQ_MEM_ID = MEM_ID "
				+ "AND TB_SETTLE_CONFIG.SETTLE_CODE = TB_SETTLE_TXN.SETTLE_CODE "
				+ "AND SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE='1' "
				+ "AND P_CODE IN ('7707','7647') ";
		Statement settleStmt = null;
		ResultSet settleRs = null;
		try {
			conn = DBService.getDBService().getConnection(
					Layer1Constants.DSNAME_BATCH);
			settleStmt = conn.createStatement();
			log.debug("seqnoSql: " + settleSql);
			settleRs = settleStmt.executeQuery(settleSql);
			while (settleRs.next()) {
				memId2FundType.put(
						settleRs.getString(1) + settleRs.getString(2),
						settleRs.getString(3));
			}
		} finally {
			ReleaseResource.releaseDB(conn, settleStmt, settleRs);
		}
	}

	@Override
	public ExpFileSetting makeExpFileSetting() {
		
		/*For分收單*/
		List memIdList = getMemIdList();
		
		ExpFileSetting efs = new ExpFileSetting();
		// check file size by expFile.length() % fileInfo.dataLen == 0 &&
		// (totalRecords == recordCount) depends on checkFlag
		// checkFlag:boolean:true
		// 所以若產生的檔案是 fixed length, 請設為 true, 若是 variable length, 請設為 false,
		// default is true
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
		String fullFileNameSql = "SELECT FULL_FILE_NAME FROM TB_TMP_TRANS " + 
				"WHERE EXPORT_DATE = '00000000' " +
				"AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(filenameBean)+
				" AND IMP_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate)+
				" GROUP BY FULL_FILE_NAME" +
				" UNION " +
				"SELECT SUBSTRB(FULL_FILE_NAME,0,26) FROM TB_INCTL " + 
				"WHERE EXPORT_DATE = '00000000' " +
				"AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(filenameBean) +
				" AND START_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate) + 
				" GROUP BY SUBSTRB(FULL_FILE_NAME,0,26)";
		try {
			conn = DBService.getDBService().getConnection(
					Layer1Constants.DSNAME_BATCH);
			stmt = conn.createStatement();
			log.debug(fullFileNameSql);
			rs = stmt.executeQuery(fullFileNameSql);
			while (rs.next()) {
				// FMTEFD.MMMMMMMM.YYYYMMDDNN
				String fullFileName = rs.getString(1);
				log.info("FullFileName: " + fullFileName);
				String[] args = fullFileName.split("\\.");
				if (args.length != 3)
					continue;
				String memId = args[1];
				log.debug("memId: " + memId);
				if (!memIdList.contains(memId)){
					continue;
				}
				if (!memIdbyTypeList.contains(memId))
					continue;
				String fileDate = args[2].substring(0, 8);
				String seqno = args[2].substring(8, 10);
				String fullFileNameR = fullFileName.replaceAll(args[0],
						FMTEFR_APPEND);
				if (memId.length() < 5) 
					continue;
				String memGroupId = memId.substring(0, 5);
				
				ExpFileInfo expFileInfo = new ExpFileInfo();
				expFileInfo.setMemId(memId);
				expFileInfo.setMemGroupId(memGroupId);
				expFileInfo.setFileName(FMTEFR_FN);
				expFileInfo.setFileDate(fileDate);
				expFileInfo.setSeqno(seqno);
				expFileInfo.setFullFileName(fullFileNameR);
				String selectSQL = "SELECT LINE_DATA, RCODE, substr(LINE_DATA,116,12) AS AMT"
						+ " FROM TB_TMP_TRANS "
						+ " WHERE EXPORT_DATE = '00000000' "
						+ " AND FILE_NAME = "
						+ StringUtil.toSqlValueWithSQuote(filenameBean)
						+ " AND FULL_FILE_NAME = "
						+ StringUtil.toSqlValueWithSQuote(fullFileName)
						+ " AND IMP_DATE = "
						+ StringUtil.toSqlValueWithSQuote(batchDate)
						+ " ORDER BY SEQNO";
				log.debug("selectSQL: " + selectSQL);
				expFileInfo.setSelectSQL(selectSQL);
				efs.addExpFileInfo(expFileInfo);
			}
		} finally {
			ReleaseResource.releaseDB(conn, stmt, rs);
			return efs;
		}
	}

	public String outputBeforeFile() {
		StringBuffer header = new StringBuffer();
		header.append("H");
		header.append(FMTEFR_FN);
		header.append(DateUtil.getTodayString().substring(0, 8));
		header.append(DateUtil.getTodayString().substring(8, 14));
		header.append(StringUtils.rightPad("", 216, ' '));
		return header.toString();
	}

	public String outputEndFile() {
		StringBuffer header = new StringBuffer();
		header.append("T");
		header.append(StringUtil.pendingKey(recCnt, 8));
		header.append(StringUtil.pendingKey(sumAmt, 15));
		header.append(StringUtils.rightPad("", 213, ' '));
		return header.toString();
	}

	@Override
	public String outputOneRecord(List record) throws Exception {

		String lineData = "";
		String rCode = "0000";
		String amt = "0";
		String PROC_DATE = "";
		String EXP_PAY_DATE = "";

		if (!isBlankOrNull(record.get(0).toString()))
			lineData = record.get(0).toString();
		if (!isBlankOrNull(record.get(1).toString()))
			rCode = record.get(1).toString();
		if (!isBlankOrNull(record.get(2).toString()))
			amt = record.get(2).toString();

		BerTLV berTLV = null;
		TbSettleResultInfo tbSettleResultInfo = null;
		int amtCnt = 0;
		if (!"0000".equals(rCode)) {
			EXP_PAY_DATE = "";
			if (!isBlankOrNull(amt))
				amt = formatBal(amt);
			amtCnt = Integer.valueOf(amt);
		} 
		else {
			try {
				berTLV = BerTLV.createInstance(ISOUtil.hex2byte(lineData.substring(217)));
				FF11 ff11 = new FF11(MsgUtil.FF11_SIZE,berTLV.getHexStr(0xFF11));
				log.debug(ff11.toString());

				tbSettleResultInfo = getSettleResult(ff11);
				if (tbSettleResultInfo != null) {
					PROC_DATE = tbSettleResultInfo.getProcDate();
					EXP_PAY_DATE = tbSettleResultInfo.getExpPayDate();
					amtCnt = tbSettleResultInfo.getSettleAmt().intValue();
				} 
				else if (tbSettleResultInfo == null
						&& (ff11.getPcode().equals("7707")
						|| ff11.getPcode().equals("7708")
						|| ff11.getPcode().equals("7647") 
						|| ff11.getPcode().equals("7648"))) 
				{
					TbOnlTxnInfo tbOnlTxnInfo = getOnlTxn(ff11);
					if (tbOnlTxnInfo != null) {
						String fundType = "";
						if (ff11.getPcode().equals("7707")
							|| ff11.getPcode().equals("7708")){
							fundType = memId2FundType.get(tbOnlTxnInfo.getAcqMemId() + "7707").toString();
						}
						else if (ff11.getPcode().equals("7647")
								|| ff11.getPcode().equals("7648")){
							fundType = memId2FundType.get(tbOnlTxnInfo.getAcqMemId() + "7647").toString();
						}
						log.debug("fundType: " + fundType);

						String expPayDate = Layer2Util.getCycleDate(conn,tbOnlTxnInfo.getTermSettleDate(), fundType);
						if (!isBlankOrNull(expPayDate)) {
							PROC_DATE = tbOnlTxnInfo.getTermSettleDate();
							EXP_PAY_DATE = expPayDate;
						}
						amtCnt = tbOnlTxnInfo.getTxnAmt().intValue();
					}
				}
			} catch (Exception e) {
				log.warn(e + " unpack error: [" + lineData + "] ");
			}
		}

		sumAmt = sumAmt + amtCnt;
		recCnt++;
		
		if (isBlankOrNull(PROC_DATE)) 
			PROC_DATE = "";
		if (isBlankOrNull(EXP_PAY_DATE))
			EXP_PAY_DATE = "";
		
		StringBuffer sb = new StringBuffer();
		
		if (lineData.length() >= 217){
			sb.append(lineData.substring(0, 217));
		}
		else{
			sb.append(StringUtils.rightPad(lineData, 217, ' '));
		}
		sb.append(StringUtils.rightPad(rCode, 4, '0'));
		sb.append(StringUtils.rightPad(PROC_DATE, 8, ' '));
		sb.append(StringUtils.rightPad(EXP_PAY_DATE, 8, ' '));
		return sb.toString();
	}
	 private List getMemIdList()
	 {
		 Connection conn = null;
		 Statement stmt = null;
	     ResultSet rs = null;
		 ArrayList<String> memIdList= null;				
		    	StringBuffer sqlCmd = new StringBuffer();
		    	if(getBatchResultInfo() != null){
		    		sqlCmd.append("SELECT MEM_ID FROM TB_MEMBER WHERE 1=1");
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
	@Override
	public List outputDtlRecord(List record) {
		return null;
	}

	public void actionsAfterInfo() throws Exception {
		// 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
		super.actionsAfterInfo();
	}

	public void actionsAfterFile() throws Exception {

		sumAmt = 0;
		recCnt = 0;

		if (conn != null) {
			try {
				String tmpTransSql = "UPDATE TB_TMP_TRANS SET EXPORT_DATE = "
						+ StringUtil.toSqlValueWithSQuote(DateUtil
								.getTodayString().substring(0, 8))
						+ " WHERE EXPORT_DATE = '00000000' "
						+ " AND FILE_NAME = "
						+ StringUtil.toSqlValueWithSQuote(filenameBean)
						+ " AND FULL_FILE_NAME = "
						+ StringUtil.toSqlValueWithSQuote(expFileInfo
								.getFullFileName().replaceAll(FMTEFR_APPEND,
										"FMTEFD")) + " AND IMP_DATE = "
						+ StringUtil.toSqlValueWithSQuote(batchDate);

				executeUpdate(conn, tmpTransSql);
				
				String inctlSql = "UPDATE TB_INCTL SET EXPORT_DATE = "
						+ StringUtil.toSqlValueWithSQuote(DateUtil
								.getTodayString().substring(0, 8))
						+ " WHERE EXPORT_DATE = '00000000' "
						+ " AND FILE_NAME = "
						+ StringUtil.toSqlValueWithSQuote(filenameBean)
						+ " AND FULL_FILE_NAME LIKE "
						+ StringUtil.toSqlValueWithSQuote(expFileInfo
								.getFullFileName().replaceAll(FMTEFR_APPEND,"FMTEFD")+"%") 
						+ " AND START_DATE = "
						+ StringUtil.toSqlValueWithSQuote(batchDate);

				executeUpdate(conn, inctlSql);
				
				
				conn.commit();
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
		}
	}

	public TbOnlTxnInfo getOnlTxn(FF11 ff11) throws Exception {
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

	public TbSettleResultInfo getSettleResult(FF11 ff11) throws Exception {
		// in here, tmp is the only one fileName

		TbSettleResultInfo tbSettleResultInfo = null;

		String sql = "SELECT PROC_DATE, EXP_PAY_DATE, SETTLE_AMT "
				+ "FROM TB_SETTLE_RESULT "
				+
				// 卡號對方會加FF，需慮掉
				"WHERE CARD_NO = '" + ff11.getCardNo().replaceAll("F", "")
				+ "' " + "AND EXPIRY_DATE = '" + ff11.getCardExp() + "' "
				+ "AND LMS_INVOICE_NO = '" + ff11.getInvoiceNo() + "' "
				+ "ORDER BY EXP_PAY_DATE";
		log.debug(sql);

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				tbSettleResultInfo = new TbSettleResultInfo();
				tbSettleResultInfo.setProcDate(rs.getString(1));
				tbSettleResultInfo.setExpPayDate(rs.getString(2));
				tbSettleResultInfo.setSettleAmt(rs.getDouble(3));
				break;
			}
		} finally {
			ReleaseResource.releaseDB(null, stmt, rs);
		}
		return tbSettleResultInfo;
	}

	private String formatBal(String value) {
		if (value.length() == 0)
			return value;
		if ("1".equals(value.substring(0, 1)))
			return "-" + value.substring(1, value.length());
		return value;
	}

	public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}

	public static ExpFMTefr getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(
				SPRING_PATH);
		ExpFMTefr instance = (ExpFMTefr) apContext.getBean("expFmTefr");
		return instance;
	}

	public static void main(String[] args) {
		ExpFMTefr expFmTefr = null;
		try {
			String batchDate = System.getProperty("date");
			if (StringUtil.isEmpty(batchDate)) {
				batchDate = DateUtil.getTodayString().substring(0, 8);
			} else if (!DateUtil.isValidDate(batchDate)) {
				log.info("invalid batchDate('" + batchDate
						+ "') using system date!");
				batchDate = DateUtil.getTodayString().substring(0, 8);
			}
			File f = new File(SPRING_PATH);
			if (f.exists() && f.isFile()) {
				expFmTefr = getInstance();
			} else {
				expFmTefr = new ExpFMTefr();
			}
			expFmTefr.setBatchDate(batchDate);
			// 註: 此 method 一定要先呼叫
			expFmTefr.beforeHandling();
			expFmTefr.run(args);
		} catch (Exception ignore) {
			log.warn("ExpTerm run fail:" + ignore.getMessage(), ignore);
		}
	}
}
