package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpCDRP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.svc.yhdp.batch.util.BatchDateUtil;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ExpCDRP extends AbstractExpFile {

	private static Logger log = Logger.getLogger(ExpCDRP.class);
	private static final String SPRING_PATH = "config" + File.separator
			+ "batch" + File.separator + "expfiles" + File.separator
			+ "ExpCDRP" + File.separator + "spring.xml";
	private static final String CDRP_FN = "CDRP";
	private static final String CDRP_APPEND = "CDRP";
	
	private static final String TYPE_AUTOSETTLE = "R";
	private final String batchTime = DateUtils.getSystemTime();
	// 要匯出會員檔的 bank
	private List bankIds = null;
	// key:String(bankId), value:String(seqno)
	private HashMap bankId2Seqno = new HashMap();
	// key:String(bankId), value:String(memId)
	private HashMap<String, String> bankId2MemId = new HashMap<String, String>();
	private HashMap memId2MemGroupId = new HashMap();
	private HashMap memId2BatchNo = new HashMap();
	// key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
	private String memId = "";
	private String encoding = "";
	private List memIds = null;
	private String acqMemId = "";
	private String merchId = "";
	private String termId = "";
	private Integer recCnt = 0;
	
	//現金點永久有效
	private String bonusSdate = "00010101";
	private String bonusEdate = "99991231";

	private Connection Connection = null;
	
	private HashMap<String, HashMap<String, String>> respDayMap = new HashMap<String, HashMap<String, String>>();

	public ExpCDRP() {
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

	public Integer getRecCnt() {
		return recCnt;
	}

	public void setRecCnt(Integer recCnt) {
		this.recCnt = recCnt;
	}

	public List getBankIds() {
		return bankIds;
	}

	public void setBankIds(List bankIds) {
		this.bankIds = bankIds;
	}

	public HashMap<String, HashMap<String, String>> getRespDayMap() {
		return respDayMap;
	}

	public void setRespDayMap(
			HashMap<String, HashMap<String, String>> respDayMap) {
		this.respDayMap = respDayMap;
	}

	private void beforeHandling() throws Exception {
		Connection conn = null;

		try {
			conn = DBService.getDBService().getConnection(
					Layer1Constants.DSNAME_BATCH);

			String sqlCmd = "SELECT MEM_ID, BANK_ID, MEM_GROUP_ID, " +
					"LOST_CARD_RESP_DAY, STOP_CARD_RESP_DAY, EXPIRY_CARD_RESP_DAY, GEN_RDCL_DAY " +
					"FROM TB_MEMBER " +
					"WHERE SUBSTR(MEM_TYPE,3,1)='1' " +
					"AND BANK_ID IS NOT NULL";
			Vector vr = DbUtil.select(sqlCmd, conn);

			for (int idx = 0; idx < vr.size(); idx++) {
				Vector list = (Vector) vr.get(idx);
				String memId = (String) list.get(0);
				String bankId = (String) list.get(1);
				String memGroupId = (String) list.get(2);

				memId2MemGroupId.put(memId, memGroupId);

				if (!StringUtil.isEmpty(bankId)) {
					if (bankId2MemId.get(bankId) == null) {
						bankIds.add(bankId);
						bankId2MemId.put(bankId, memId);
						memId2BatchNo.put(memId, SequenceGenerator.getBatchNoByType(conn, TYPE_AUTOSETTLE));
						String seqno = getFileSeqNo(conn, memId);
						bankId2Seqno.put(bankId, seqno);
						respDayMap.put(StringUtils.leftPad(bankId, 8, '0'),
								this.getRespDayInfo(list));
					} else {
						if (bankId2MemId.get(bankId).equals(memId)) {
							throw new Exception(
									"it doesn't allow different acqMemId");
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		} finally {
			ReleaseResource.releaseDB(conn);
		}
		log.info("bankIds:" + bankIds);
		log.info("bankId2Seqno:" + bankId2Seqno);
		log.info("bankId2MemId:" + bankId2MemId);
		log.info("memId2MemGroupId: " + memId2MemGroupId);
		log.info("memId2BatchNo:" + memId2BatchNo);
		log.info("respDayMap: " + respDayMap.toString());

		acqMemId = Layer2Util.getBatchConfig("UNKNOWN_MEMBER");
		merchId = Layer2Util.getBatchConfig("UNKNOWN_MERCH");
		termId = Layer2Util.getBatchConfig("UNKNOWN_TERM");
	}

	private String getFileSeqNo(Connection conn, String memId) throws Exception {
		String seqno = "01";
		Statement stmt = null;
		ResultSet rs = null;

		String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL"
				+ " WHERE MEM_ID = '"
				+ memId
				+ "'"
				+ " AND FILE_NAME = "
				+ StringUtil.toSqlValueWithSQuote(CDRP_FN)
				+ " AND FILE_DATE = "
				+ StringUtil.toSqlValueWithSQuote(batchDate);
		try {
			stmt = conn.createStatement();
			log.warn("seqnoSql: " + seqnoSql);
			rs = stmt.executeQuery(seqnoSql);
			while (rs.next()) {
				seqno = rs.getString(1);
			}
		} finally {
			ReleaseResource.releaseDB(null, stmt, rs);
		}
		return seqno;
	}

	@Override
	public ExpFileSetting makeExpFileSetting() {
		
		 /*For分收單*/
    	List bankIdList = getBankIdList();
    	
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
		efs.setTempFilePending(".TMP");
		// recordsPerFlush:int:-1
		// 產生檔案時處理多少筆記錄後做一次 flush 動作, <= 0 時不 enable flush 動作, default is -1
		efs.setRecordsPerFlush(-1);
		// recordsPerFile:int:-1
		// 多少筆產生一個 file, default 全部一個檔案(<= 0)
		// 若有設 recordsPerFile, 每個 expFileInfo 的 seqnoStart, seqnoEnd 一定要給
		efs.setRecordsPerFile(-1);
		efs.setLineSeparator("\r\n");

		if (bankIds == null) {
			log.error("there are not any bank id !!");
			return null;
		}

		for (int i = 0; i < bankIds.size(); i++) {
			String bankId = (String) bankIds.get(i);
			  if(!bankIdList.contains(bankId)){
	            	continue;
	            }
			String seqno = (String) bankId2Seqno.get(bankId);
			// Integer count = (Integer) memId2Count.get(memId);
			if (seqno == null) {
				log.warn("can't find in bankId2Seqno or memId2Count, ignore for '"
						+ bankId + "'");
				continue;
			}
			String memId = (String) bankId2MemId.get(bankId);
			ExpFileInfo expFileInfo = new ExpFileInfo();
			expFileInfo.setMemGroupId((String) memId2MemGroupId.get(memId));
			expFileInfo.setMemId(memId);
			expFileInfo.setFileName(CDRP_FN);
			expFileInfo.setFileDate(batchDate);
			expFileInfo.setSeqno(seqno);
			// memId 若不是同樣長度時會有問題
			String fullFileName = CDRP_APPEND + "."
					+ StringUtils.leftPad(memId, 8, '0') + "."
					+ expFileInfo.getFileDate() + expFileInfo.getSeqno();
			expFileInfo.setFullFileName(fullFileName);
			String selectSQL = 
					/*"SELECT REQ.REQ_TYPE, REQ.CARD_NO, REQ.EXPIRY_DATE, REQ.IMP_DATE, REQ.RCODE, CP.ECASH_BONUS_ID, BONUS_SDATE, BONUS_EDATE, QTY, CD.MEM_ID "+
					"FROM TB_MB_CARD_BAL_REQ REQ, "+ 
					"(SELECT TB_CARD.MEM_ID, TB_CARD.CARD_NO, TB_CARD.EXPIRY_DATE, TB_CARD.CARD_PRODUCT, TB_CARD_BAL.BONUS_ID, TB_CARD_BAL.BONUS_SDATE, TB_CARD_BAL.BONUS_EDATE, (CR_BONUS_QTY - DB_BONUS_QTY + BAL_BONUS_QTY) AS QTY FROM TB_CARD , TB_CARD_BAL "+ 
					"WHERE TB_CARD.CARD_NO = TB_CARD_BAL.CARD_NO "+ 
					"AND TB_CARD.EXPIRY_DATE = TB_CARD_BAL.EXPIRY_DATE "+
					"AND BANK_ID = "+ StringUtil.toSqlValueWithSQuote(bankId)+
					") CD, TB_CARD_PRODUCT CP "+
					"WHERE REQ.CARD_NO = CD.CARD_NO "+ 
					"AND CP.ECASH_BONUS_ID = CD.BONUS_ID "+
					"AND REQ.EXPIRY_DATE = CD.EXPIRY_DATE "+
					"AND CD.CARD_PRODUCT = CP.CARD_PRODUCT "+
					"AND REQ.EXP_DATE = '00000000' ";*/
					" SELECT REQ_TYPE, CARD.CARD_NO, CARD.EXPIRY_DATE, IMP_DATE, RCODE, ECASH_BONUS_ID, QTY, MEM_ID"+
					" FROM"+
					" (SELECT TB_CARD.CARD_NO, TB_CARD.EXPIRY_DATE, TB_CARD.MEM_ID, TB_CARD_PRODUCT.ECASH_BONUS_ID FROM TB_CARD, TB_CARD_PRODUCT"+
					" WHERE TB_CARD.BANK_ID = "+ StringUtil.toSqlValueWithSQuote(bankId)+
					" AND TB_CARD_PRODUCT.CARD_PRODUCT = TB_CARD.CARD_PRODUCT) CARD,"+ 
					" (SELECT REQ_TYPE, TB_MB_CARD_BAL_REQ.CARD_NO, TB_MB_CARD_BAL_REQ.EXPIRY_DATE, TB_MB_CARD_BAL_REQ.IMP_DATE, TB_MB_CARD_BAL_REQ.RCODE,"+ 
					" (TB_MB_CARD_BAL_SUM.RM_QTY + TB_MB_CARD_BAL_SUM.NOT_DW_APT_AMT + METRO_LAST_MONTH_REWARD_AMT + METRO_THIS_MONTH_REWARD_AMT) AS QTY"+
					" FROM TB_MB_CARD_BAL_REQ, TB_MB_CARD_BAL_SUM"+
					" WHERE TB_MB_CARD_BAL_SUM.CARD_NO = TB_MB_CARD_BAL_REQ.CARD_NO"+
					" AND TB_MB_CARD_BAL_SUM.EXPIRY_DATE = TB_MB_CARD_BAL_REQ.EXPIRY_DATE"+
					" AND TB_MB_CARD_BAL_SUM.IMP_FILE_NAME = TB_MB_CARD_BAL_REQ.IMP_FILE_NAME"+
					" AND EXP_DATE = '00000000') MB"+
					" WHERE CARD.CARD_NO = MB.CARD_NO"+
					" AND CARD.EXPIRY_DATE = MB.EXPIRY_DATE";
			
			log.info(selectSQL);
			expFileInfo.setSelectSQL(selectSQL);
			efs.addExpFileInfo(expFileInfo);
		}
		return efs;
	}

	public String outputBeforeFile() {
		
		StringBuffer header = new StringBuffer();
		header.append("H0");
		header.append(this.getBatchDate());
		header.append("########"); // 最後再回填recCnt
		header.append(StringUtils.rightPad("", 55, ' '));
		return header.toString();
	}

	@Override
	public String outputOneRecord(List record) throws Exception {
		
		if ( Connection == null || Connection.isClosed()){
    		try {
    			Connection = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    			Connection.setAutoCommit(false);
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
    	}
		
		String reqType = "";
		String cardNo = "";
		String expiryDate = "";
		String impDate = "";
		String rCode = "";
		String bonusId = "";
		double cashBal = 0;
		String issMemId = "";

		if (!isBlankOrNull(record.get(0).toString()))
			reqType = record.get(0).toString().trim();
		if (!isBlankOrNull(record.get(1).toString()))
			cardNo = record.get(1).toString().trim();
		if (!isBlankOrNull(record.get(2).toString()))
			expiryDate = record.get(2).toString().trim();
		if (!isBlankOrNull(record.get(3).toString()))
			impDate = record.get(3).toString().trim();
		if (!isBlankOrNull(record.get(4).toString()))
			rCode = record.get(4).toString().trim();
		if (!isBlankOrNull(record.get(5).toString()))
			bonusId = record.get(5).toString().trim();
		
		if (!isBlankOrNull(record.get(6).toString()))
			cashBal = Double.valueOf(record.get(6).toString());
		issMemId = record.get(7).toString();
		

		try {
			if (!this.isRespDay(reqType, this.getBatchDate(), impDate))
				return ""; // 尚未到回應日期的不予處理, 直接continue下一筆
		} catch (Exception e) {
			log.warn(e.toString());
			return ""; // 日期有問題不予處理
		}

		StringBuffer sb = new StringBuffer();
		sb.append("DT");
		sb.append(StringUtils.leftPad(reqType, 1, '0'));
		sb.append(StringUtils.rightPad(cardNo, 20, ' '));
		sb.append(StringUtils.rightPad(this.formatExpiryDate(expiryDate), 4,
				' '));
		
		if ( cashBal < 0 ){
        	sb.append("-").append(StringUtils.leftPad(takeDecimal(String.valueOf(Math.abs(cashBal)),0), 13, '0'));
        }
        else{
        	sb.append(StringUtils.leftPad(takeDecimal(String.valueOf(cashBal), 0) ,14, '0'));
        }
		
		sb.append(StringUtils.leftPad(impDate, 8, '0'));
		sb.append(StringUtils.leftPad("0", 14, '0'));
		sb.append(StringUtils.rightPad("00", 2, ' '));
		sb.append(StringUtils.leftPad(rCode, 4, '0'));
		sb.append(StringUtils.leftPad("0000", 4, '0'));

		this.setRecCnt(this.getRecCnt() + 1);
		this.refundTxnAction(cardNo, reqType, expiryDate, bonusId, cashBal, issMemId, impDate);
//		this.updateTbMbCardBalReq(expFileInfo.getFullFileName(), cardNo, expiryDate, impDate);

		return sb.toString();
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
	private String takeDecimal( String number, int afterDecimal){
		
    	String afterDecimalNumber = "";
	
    	if (number.contains(".")){
    		if ( afterDecimal > 0 ){
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
    		else{
    			return number.substring(0, number.indexOf("."));
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

	@Override
	public List outputDtlRecord(List record) {
		return null;
	}

	@Override
	public void replaceDataLine() throws Exception {
		String file = expFileInfo.getExpTempFile().toString();
		InputStreamReader isr = null;
		BufferedReader reader = null;
		PrintStream ps = null;
		try {
			isr = new InputStreamReader(new FileInputStream(file),
					expFileInfo.getFileEncoding());
			reader = new BufferedReader(isr);
			ps = new PrintStream(file + ".REP", expFileInfo.getFileEncoding());
			String line = null;
			String temp = null;
			while ((line = reader.readLine()) != null) {
				temp = line.replace("########",
						StringUtil.pendingKey(this.getRecCnt(), 8));
				ps.print(temp); // 將置換過的檔案內容寫回
				ps.print(expFileInfo.getLineSeparator());
			}
		} finally {
			ReleaseResource.releaseIO(ps);
			ReleaseResource.releaseIO(reader);
			ReleaseResource.releaseIO(isr);
		}

		File oldFile = new File(file);
		oldFile.delete();

		File newFile = new File(file + ".REP");
		newFile.renameTo(oldFile);
	}

	@Override
	public void actionsAfterFile() throws Exception {
		super.actionsAfterFile();
	}

	@Override
	public TbOutctlInfo changeOutctlInfo(TbOutctlInfo outctlInfo)
			throws Exception {
		outctlInfo.setTotRec(this.getRecCnt()); // 確實的匯出筆數
		return outctlInfo;
	}

	@Override
	public void actionsAfterInfo() throws Exception {
		
		if (expFileResult.getTotalRecords() > 0){
	        if ( Connection == null || Connection.isClosed()){
	    		try {
	    			Connection = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
	    			Connection.setAutoCommit(false);
				} catch (SQLException e) {
					throw new Exception(e.getMessage());
				}
	    	}
	
	        insertTbTermBatch();
        }
		
		this.setRecCnt(0);
	}
	
	public void insertTbTermBatch() throws SQLException {
    	
    	log.info("=======================insertTbTermBatch=======================");
    	try{
	    	TbTermBatchInfo tbTermBatchInfo = new TbTermBatchInfo();
	    	tbTermBatchInfo.setTxnSrc("B");
	    	tbTermBatchInfo.setMerchId(merchId);
	    	tbTermBatchInfo.setTermId(termId);
	    	tbTermBatchInfo.setBatchNo(memId2BatchNo.get(expFileInfo.getMemId()).toString());
	    	tbTermBatchInfo.setTermSettleDate(batchDate);
	    	tbTermBatchInfo.setTermSettleTime(batchTime);
	    	tbTermBatchInfo.setTermSettleFlag("1");
	    	tbTermBatchInfo.setStatus("1");
	    	tbTermBatchInfo.setCutDate(batchDate);
	    	tbTermBatchInfo.setCutTime(batchTime);
	    	tbTermBatchInfo.setParMon(batchDate.substring(4, 6));
	    	tbTermBatchInfo.setParDay(batchDate.substring(6, 8));
	    	tbTermBatchInfo.setTermUpDate(batchDate);
	    	tbTermBatchInfo.setCutRcode(Constants.RCODE_0000_OK);
	        
	        TbTermBatchMgr tbTermBatchMgr = new TbTermBatchMgr(Connection);   
	        log.info("tbTermBatchInfo: "+tbTermBatchInfo);
	        tbTermBatchMgr.insert(tbTermBatchInfo);
	        
	        Connection.commit();
	        Connection.close();
    	}
    	catch (Exception e)
        {
    		Connection.rollback();
    		Connection.close();
            throw e;
        } 
    }

	/**
	 * Retrieves the value of response days from TB_MEMBER
	 * 
	 * @param bankId
	 * @return the key is LOST_CARD_RESP_DAY, STOP_CARD_RESP_DAY,
	 *         EXPIRY_CARD_RESP_DAY
	 */
	private HashMap<String, String> getRespDayInfo(Vector list) {
		HashMap<String, String> map = new HashMap<String, String>();
		/*Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "SELECT LOST_CARD_RESP_DAY, STOP_CARD_RESP_DAY, EXPIRY_CARD_RESP_DAY, GEN_RDCL_DAY "+
				"FROM TB_MEMBER " +
				"WHERE BANK_ID = "+ StringUtil.toSqlValueWithSQuote(bankId);
		log.debug(sql);
		try {
			conn = DBService.getDBService().getConnection(
					Layer1Constants.DSNAME_BATCH);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				map.put("LOST_CARD_RESP_DAY", rs.getString(1));
				map.put("STOP_CARD_RESP_DAY", rs.getString(2));
				map.put("EXPIRY_CARD_RESP_DAY", rs.getString(3));
				map.put("GEN_RDCL_DAY", rs.getString(4));
			}
		} catch (SQLException ignore) {
			log.warn("SQLException:" + ignore.getMessage(), ignore);
		} finally {
			ReleaseResource.releaseDB(conn, stmt, rs);
		}*/
		
		map.put("LOST_CARD_RESP_DAY", list.get(3).toString());
		map.put("STOP_CARD_RESP_DAY", list.get(4).toString());
		map.put("EXPIRY_CARD_RESP_DAY", list.get(5).toString());
		map.put("GEN_RDCL_DAY", list.get(6).toString());
		
		return map;
	}

	/**
	 * Retrieves the response days by request reason type
	 * 
	 * @param type
	 *            request reason type
	 * @return response days
	 */
	private String getRespDay(String type) {
		String result = null;
		String bankId = null;
		HashMap<String, String> tempMap = new HashMap<String, String>();
		try {
			for (Entry<String, String> entry : bankId2MemId.entrySet()) {
				String value = entry.getValue();
				if (value.equals(expFileInfo.getMemId())) {
					bankId = entry.getKey();
					break;
				}
			}
			tempMap = this.getRespDayMap().get(
					StringUtils.leftPad(bankId, 8, '0'));
			if (tempMap == null || tempMap.size() == 0) {
				log.warn("bankId '" + StringUtils.leftPad(bankId, 8, '0')
						+ "' is null in TB_MEMBER");
				return null;
			}
			// 1: 掛卡, 2: 停卡, 3: 到期卡, 4: 關閉自動加值
			if ("1".equals(type))
				result = tempMap.get("LOST_CARD_RESP_DAY").toString();
			else if ("2".equals(type))
				result = tempMap.get("STOP_CARD_RESP_DAY").toString();
			else if ("3".equals(type))
				result = tempMap.get("EXPIRY_CARD_RESP_DAY").toString();
			else if ("4".equals(type))
				result = tempMap.get("GEN_RDCL_DAY").toString();
		} catch (Exception e) {
			log.warn(e.toString());
			return null;
		}
		return result;
	}

//	/**
//	 * Retrieves the value form TB_CARD_BAL
//	 * 
//	 * @param cardNo
//	 * @param expiryDate
//	 * @param bonusId
//	 * @return BAL_BONUS_QTY
//	 */
//	private double getECashBal(String cardNo, String expiryDate, String bonusId) {
//		Connection conn = null;
//		Statement stmt = null;
//		ResultSet rs = null;
//		String sql = "select (CR_BONUS_QTY - DB_BONUS_QTY + BAL_BONUS_QTY) as QTY "
//				+ "from TB_CARD_BAL "
//				+ "where CARD_NO = "
//				+ StringUtil.toSqlValueWithSQuote(cardNo)
//				+ " "
//				+ "and EXPIRY_DATE = "
//				+ StringUtil.toSqlValueWithSQuote(expiryDate)
//				+ " "
//				+ "and BONUS_ID  = "
//				+ StringUtil.toSqlValueWithSQuote(bonusId)
//				+ " ";
//		log.debug(sql);
//		try {
//			conn = DBService.getDBService().getConnection(
//					Layer1Constants.DSNAME_BATCH);
//			stmt = conn.createStatement();
//			rs = stmt.executeQuery(sql);
//			while (rs.next())
//				return rs.getDouble(1);
//		} catch (SQLException ignore) {
//			log.warn("SQLException:" + ignore.getMessage(), ignore);
//		} finally {
//			ReleaseResource.releaseDB(conn, stmt, rs);
//		}
//		return 0;
//	}

	/**
	 * Returns the value form TB_CARD
	 * 
	 * @param conn
	 * @param cardNo
	 * @return MEM_ID
	 */
	private String getIssMemId(Connection conn, String cardNo) {
		try {
			conn = DBService.getDBService().getConnection(
					Layer1Constants.DSNAME_BATCH);
			Vector<TbCardInfo> result = new Vector<TbCardInfo>();
			TbCardInfo info = new TbCardInfo();
			info.setCardNo(cardNo);
			int cnt = new TbCardMgr(conn).queryMultiple(info, result);
			if (cnt > 0 && result.size() > 0)
				return result.get(0).getMemId();
		} catch (SQLException ignore) {
			log.warn("SQLException:" + ignore.getMessage(), ignore);
		} finally {
			ReleaseResource.releaseDB(conn);
		}
		return "";
	}

	public void rollback()  throws Exception {
    	
    	if ( !Connection.isClosed() && Connection != null ){
    		Connection.rollback();
    		Connection.close();
    	}
    }
	
	/**
	 * insert TB_TRANS, TB_TRANS_DTL, TB_TERM_BATCH
	 * 
	 * @param cardNo
	 * @param reqType
	 * @param expiryDate
	 */
	private void refundTxnAction(String cardNo, String reqType, String expiryDate, 
			String bonusId, Double cashBal, String issMemId, String impDate) {
		
		String lmsInvoiceNo = "";
		
		try {
			lmsInvoiceNo = SequenceGenerator.getLmsInvoiceNo(Connection, batchDate);
			
			String pCode = this.formatPCode(reqType);
			String txnCode = "8577";
//			String txnCode = "8527";
//			if ("5347".equals(pCode)) {
//				txnCode = "8577";
//			}
			// TB_TRANS
			TbTransInfo info = new TbTransInfo();
			info.setTxnSrc("B");
			info.setOnlineFlag("O");
			info.setAcqMemId(acqMemId);
			info.setIssMemId(issMemId);
			info.setMerchId(merchId);
			info.setTermId(termId);
			info.setBatchNo(memId2BatchNo.get(expFileInfo.getMemId()).toString());
			info.setTermSettleDate(batchDate);
			info.setTermSettleTime(batchTime);
			info.setCardNo(cardNo);
			info.setExpiryDate(expiryDate);
			info.setLmsInvoiceNo(lmsInvoiceNo);
			info.setPCode(pCode);
			info.setStatus("1");
			info.setTxnDate(batchDate);
			info.setTxnTime(batchTime);
			info.setRespCode("00");
			info.setParMon(batchDate.substring(4, 6));
			info.setParDay(batchDate.substring(6, 8));
			info.setAdviceFlag("000000");
			info.setBalRcode("0000");
			info.setBalUpdateDwFlag("00");
			info.setBalUpdateGenFlag("00");
			info.setBonusSumRcode("0000");
			info.setCardProductSumRcode("0000");
			info.setCbDate("00000000");
			info.setCheckFlag("0");
			info.setChipCrBonusFlag("00");
			info.setChipDbBonusFlag("00");
			info.setCurrencyCode("901");
			info.setFeeRcode("0000");
			info.setHostCrBonusFlag("00");
			info.setHostDbBonusFlag("00");
			info.setLateUploadFlag("0");
			info.setLocalCurrency("901");
			info.setMerchSumRcode("0000");
			info.setCutDate(batchDate);
			info.setCutTime(batchTime);
			info.setTxnAmt(cashBal);
			DBService.getDBService().sqlAction(info.toInsertSQL(), Connection); 
			log.info(info.toInsertSQL());

			// TB_TRANS_DTL
			TbTransDtlInfo dtlInfo = new TbTransDtlInfo();
			dtlInfo.setCardNo(cardNo);
			dtlInfo.setExpiryDate(expiryDate);
			dtlInfo.setLmsInvoiceNo(lmsInvoiceNo);
			dtlInfo.setPCode(pCode);
			dtlInfo.setTxnCode(txnCode);
			dtlInfo.setRegionId("TWN");
			dtlInfo.setBonusBase("C");
			dtlInfo.setBalanceType("C");
			dtlInfo.setBalanceId(cardNo);
			dtlInfo.setBonusId(bonusId);
			dtlInfo.setBonusSdate(bonusSdate);
			dtlInfo.setBonusEdate(bonusEdate);
			dtlInfo.setBonusQty(cashBal);
			dtlInfo.setTxnRedeemAmt(0);
			dtlInfo.setParMon(batchDate.substring(4, 6));
			dtlInfo.setParDay(batchDate.substring(6, 8));
			dtlInfo.setCutDate(batchDate);

			DBService.getDBService().sqlAction(dtlInfo.toInsertSQL(), Connection); 
			log.info(dtlInfo.toInsertSQL());
			
			String cardBalSql = "UPDATE TB_CARD_BAL SET" +
					" DB_BONUS_QTY = DB_BONUS_QTY + " + cashBal +
					" WHERE CARD_NO = " + StringUtil.toSqlValueWithSQuote(cardNo)+
					" AND EXPIRY_DATE = " + StringUtil.toSqlValueWithSQuote(expiryDate)+
					" AND BONUS_ID = " + StringUtil.toSqlValueWithSQuote(bonusId)+
					" AND BONUS_SDATE = " + StringUtil.toSqlValueWithSQuote(bonusSdate)+
					" AND BONUS_EDATE = " + StringUtil.toSqlValueWithSQuote(bonusEdate);
			
			DBService.getDBService().sqlAction(cardBalSql, Connection); 
			
			String sql = "UPDATE TB_MB_CARD_BAL_REQ SET" +
					" EXP_FILE_NAME = " + StringUtil.toSqlValueWithSQuote(expFileInfo.getFullFileName())+
					", EXP_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate)+
					", EXP_TIME = " + StringUtil.toSqlValueWithSQuote(batchTime)+
					" WHERE CARD_NO = " + StringUtil.toSqlValueWithSQuote(cardNo)+
					" AND EXPIRY_DATE = " + StringUtil.toSqlValueWithSQuote(expiryDate)+
					" AND IMP_DATE = " + StringUtil.toSqlValueWithSQuote(impDate)+
					" AND EXP_DATE = '00000000' ";
			
			DBService.getDBService().sqlAction(sql, Connection); 


		} catch (SQLException ignore) {
			log.warn("SQLException:" + ignore.getMessage(), ignore);
			try {
				Connection.rollback();
				Connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				log.warn("SQLException:" + e.getMessage(), e);
			}
		}
	}

//	/**
//	 * update EXP_FILE_NAME, EXP_DATE, EXP_TIME
//	 * 
//	 * @param fullFileName
//	 * @param cardNo
//	 * @param expiryDate
//	 * @param impDate
//	 */
//	private void updateTbMbCardBalReq(String fullFileName, String cardNo,
//			String expiryDate, String impDate) {
//		Statement stmt = null;
//		ResultSet rs = null;
//		String sql = "UPDATE TB_MB_CARD_BAL_REQ SET" +
//				" EXP_FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fullFileName)+
//				", EXP_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate)+
//				", EXP_TIME = " + StringUtil.toSqlValueWithSQuote(batchTime)+
//				" WHERE CARD_NO = " + StringUtil.toSqlValueWithSQuote(cardNo)+
//				" AND EXPIRY_DATE = " + StringUtil.toSqlValueWithSQuote(expiryDate)+
//				" AND IMP_DATE = " + StringUtil.toSqlValueWithSQuote(impDate)+
//				" AND EXP_DATE = '00000000' ";
//		log.info(sql);
//		try {
//			conn = DBService.getDBService().getConnection(
//					Layer1Constants.DSNAME_BATCH);
//			stmt = conn.createStatement();
//			stmt.executeUpdate(sql);
//			conn.commit();
//		} catch (SQLException ignore) {
//			log.warn("SQLException:" + ignore.getMessage(), ignore);
//		}
//	}

	/**
	 * Format to expire date
	 * 
	 * @param value
	 *            date(yyyyMMdd)
	 * @return date(YYMM)
	 */
	private String formatExpiryDate(String value) {
		if (isBlankOrNull(value))
			return value;
		if (value.length() == 8)
			return value.substring(2, 6);
		return value;
	}

	/**
	 * Format pCode
	 * 
	 * @param type
	 *            regType
	 * @return return 5557 if regType is 1, otherwise 5347
	 */
	private String formatPCode(String type) {
		if ("1".equals(type))
			return "5557";
		return "5347";
	}

	/**
	 * Returns true if this is a response date
	 * 
	 * @param reqType
	 * @param batchDate
	 * @param impDate
	 * @return true if this is a response date, otherwise false
	 * @throws Exception
	 */
	private Boolean isRespDay(String reqType, String batchDate, String impDate)
			throws Exception {
		Integer respDay = 0;
		try {
			respDay = Integer.valueOf(this.getRespDay(reqType));
		} catch (Exception e) {
			log.warn(e.toString());
			return false;
		}
		
		String impDateResult=DateUtil.addDate(impDate, respDay);
		int days=Integer.parseInt(BatchDateUtil.getGapDays(impDateResult, batchDate));
		log.info("impDateResult :"+impDateResult);
		log.info("batchDate :"+batchDate);
		log.info("days:"+days);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//		Calendar batchCal = Calendar.getInstance();
//		Calendar tempCal = Calendar.getInstance();
//		try {
//			batchCal.setTime(sdf.parse(batchDate));
//			tempCal.setTime(sdf.parse(impDate));
//		} catch (Exception e) {
//			log.warn(e.toString());
//			return false;
//		}
//		tempCal.add(Calendar.DAY_OF_YEAR, respDay);
//		if (tempCal.compareTo(batchCal) > 0)
//			return false; // 尚未到回應日期的不予處理
//		else
//			return true;
		if(days>=0)
		{
			return true; // 尚未到回應日期的不予處理
		}
		else {
			return false;
		}
	}

	/**
	 * Returns true if null or empty
	 * 
	 * @param value
	 * @return true if null or empty, otherwise false
	 */
	public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}

	public static ExpCDRP getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(
				SPRING_PATH);
		ExpCDRP instance = (ExpCDRP) apContext.getBean("expCDRP");
		return instance;
	}

	public static void main(String[] args) {
		ExpCDRP expCDRP = null;
		try {
			String batchDate = System.getProperty("date");
			if (StringUtil.isEmpty(batchDate)) {
				batchDate = DateUtil.getTodayString().substring(0, 8);
			} else if (!DateUtil.isValidDate(batchDate)) {
				log.info("invalid batchDate('" + batchDate
						+ "') using system date!");
				batchDate = DateUtil.getTodayString().substring(0, 8);
			}
			String mem_id = System.getProperty("memid");
			if (StringUtil.isEmpty(mem_id))
				mem_id = "";

			File f = new File(SPRING_PATH);
			if (f.exists() && f.isFile()) {
				expCDRP = getInstance();
			} else {
				expCDRP = new ExpCDRP();
			}
			expCDRP.setBatchDate(batchDate);
			expCDRP.setMemId(mem_id);
			// 註: 此 method 一定要先呼叫
			expCDRP.beforeHandling();
			expCDRP.run(args);
		} catch (Exception ignore) {
			log.warn("ExpCDRP run fail:" + ignore.getMessage(), ignore);
		}
	}

}
