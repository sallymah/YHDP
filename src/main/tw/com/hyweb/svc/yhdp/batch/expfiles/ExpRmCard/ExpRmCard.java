/**
 * changelog ExpRmCard
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpRmCard;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.service.db.mgr.TbTransDtlMgr;
import tw.com.hyweb.service.db.mgr.TbTransMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;

import tw.com.hyweb.util.BatchUtils;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 * ExpAssociator
 * </pre>
 * author:duncan
 */
public class ExpRmCard extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpRmCard.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpRmCard" + File.separator + "spring.xml";
    
//    private final BatchHandler handler;
    
    private static final String RMCARD_FN = "RMCARD";
    private static final String RMCARD_APPEND = "RMCARD";
    private static final String P_CODE = "7957";
    private static final String TXN_CODE = "8577";
    private static final String TYPE_AUTOSETTLE = "B";
    private static final String bonusId = "1100000161";
    private static final String bonusSDate = "00010101";
    private static final String bonusEDate = "99991231";
    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List memIds = null;
    // key:String(memId), value:String(seqno)
    private HashMap memId2Seqno = new HashMap();
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private String memId = "";
    private String merchId = "";
    private String termId = "";
    private String batchNo = "";
//    private String lostCardBalDay = "0";
    
    private Connection Connection = null;
    
    public ExpRmCard() {
    }
    
    /*public ExpRmCard(BatchHandler handler) {
    	this.handler = handler;
    }*/

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
    
/*    public String getLostCardBalDay() {
		return lostCardBalDay;
	}

	public void setLostCardBalDay(String lostCardBalDay) {
		this.lostCardBalDay = lostCardBalDay;
	}*/

	private void beforeHandling() throws Exception {
		
		Connection conn;
		
		try {
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			conn.setAutoCommit(false);
			
			//模擬指加
//			process(conn);
			
			/*lostCardBalDay = Layer2Util.getBatchConfig("LOSTCARDBALDAY");
			log.info("lostCardBalDay: "+lostCardBalDay);*/
			merchId = Layer2Util.getBatchConfig("UNKNOWN_MERCH");
			log.info("merchId: "+merchId);
			termId = Layer2Util.getBatchConfig("UNKNOWN_TERM");
			log.info("termId: "+termId);
	        batchNo = SequenceGenerator.getBatchNoByType(conn, TYPE_AUTOSETTLE);
	        
	        Statement stmt = null;
	        ResultSet rs = null;
	        String seqno = "01";
	        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
	                		"WHERE MEM_ID = '" + memId + "'"+
	                		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(RMCARD_FN) + 
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
	              ReleaseResource.releaseDB(conn, stmt, rs);
	        }
	        memId2Seqno.put(memId, seqno);
	        memIds.add(memId);
	        
	        log.info("memIds:" + memIds);
	        log.info("memId2Seqno:" + memId2Seqno);
	        
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
        
    }

    public ExpFileSetting makeExpFileSetting() {
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
            String seqno = (String) memId2Seqno.get(memId);
            //Integer count = (Integer) memId2Count.get(memId);
            if (seqno == null) {
                log.warn("can't find in memId2Seqno or memId2Count, ignore for '" + memId + "'");
                continue;
            }
            ExpFileInfo expFileInfo = new ExpFileInfo();
            expFileInfo.setMemId(memId);
            expFileInfo.setFileName(RMCARD_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = RMCARD_APPEND + "." + expFileInfo.getFileDate();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL =
            				/*"SELECT TB_CARD.CARD_NO, TB_CARD.CARD_CAT_ID, TB_CARD_BAL.CR_BONUS_QTY - TB_CARD_BAL.DB_BONUS_QTY + TB_CARD_BAL.BAL_BONUS_QTY , " +
            				"TB_CUST.PERSON_ID, TB_CUST.LOC_NAME, "+
							"TB_CUST.TEL_HOME, TB_CUST.MOBILE, TB_CUST.CITY, TB_CUST.ZIP_CODE, TB_CUST.ADDRESS, TB_BLACKLIST_SETTING.REG_REASON, " +
							"TB_CARD_BAL.EXPIRY_DATE, TB_CARD.MEM_ID, P_CODE, TB_CARD_BAL.BONUS_ID, TB_CARD_BAL.BONUS_SDATE, TB_CARD_BAL.BONUS_EDATE "+
							"FROM TB_TRANS, TB_CARD, TB_CARD_BAL, TB_CUST , TB_BLACKLIST_SETTING "+
							"WHERE P_CODE IN ('9847','9757','9827') "+
							"AND TXN_DATE = '"+BatchUtil.getSomeDay(batchDate,-Integer.parseInt(getLostCardBalDay()))+"' "+
							"AND TB_CARD.CARD_NO = TB_TRANS.CARD_NO "+
							"AND TB_CARD.EXPIRY_DATE = TB_TRANS.EXPIRY_DATE "+
							"AND TB_CARD.CARD_NO = TB_CARD_BAL.CARD_NO (+) "+
							"AND TB_CARD.EXPIRY_DATE = TB_CARD_BAL.EXPIRY_DATE (+) "+
							"AND TB_CARD.CARD_NO = TB_BLACKLIST_SETTING.CARD_NO "+
							"AND TB_CARD.EXPIRY_DATE = TB_BLACKLIST_SETTING.EXPIRY_DATE "+
							"AND TB_CARD.CUST_ID = TB_CUST.CUST_ID (+) " +
							"AND TB_CARD_BAL.REFUND_DATE IS NULL";*/
            		
            		" SELECT TB_CARD.CARD_NO,"+
            		" TB_CARD.CARD_CAT_ID,"+
            		" (TB_RMCARD_BAL_SUM.RM_QTY + TB_RMCARD_BAL_SUM.NOT_DW_APT_AMT + METRO_LAST_MONTH_REWARD_AMT + METRO_THIS_MONTH_REWARD_AMT) AS QTY ,"+
            		" TB_CUST.PERSON_ID,"+
            		" TB_CUST.LOC_NAME,"+
            		" TB_CUST.TEL_HOME,"+
            		" TB_CUST.MOBILE,"+
            		" TB_CUST.CITY,"+
            		" TB_CUST.ZIP_CODE,"+
            		" TB_CUST.ADDRESS,"+
            		" TB_BLACKLIST_SETTING.REG_REASON,"+
            		" TB_CARD.EXPIRY_DATE,"+
            		" TB_CARD.MEM_ID,"+
            		" P_CODE"+
            		" FROM TB_CARD,"+
            		" TB_RMCARD_BAL_SUM,"+
            		" TB_BLACKLIST_SETTING,"+
            		" TB_CUST"+
            		" WHERE PROC_DATE = '"+ batchDate +"'"+
            		" AND TB_CARD.CARD_NO = TB_RMCARD_BAL_SUM.CARD_NO"+
            		" AND TB_CARD.EXPIRY_DATE = TB_RMCARD_BAL_SUM.EXPIRY_DATE"+
            		" AND TB_CARD.CARD_NO = TB_BLACKLIST_SETTING.CARD_NO"+
            		" AND TB_CARD.EXPIRY_DATE = TB_BLACKLIST_SETTING.EXPIRY_DATE"+
            		" AND TB_CARD.CUST_ID = TB_CUST.CUST_ID (+)";
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

    	//452
        StringBuffer header = new StringBuffer();
        header.append("H0");
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.rightPad("", 432, ' '));
        return header.toString();
    }
    
    public String outputEndFile() {    	
    	//452
        StringBuffer header = new StringBuffer();
        header.append("/EOF");
        header.append(StringUtils.rightPad("", 446, ' '));
        return header.toString();
    }

    public void actionsAfterInfo() throws Exception {
        // 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
        
        if (expFileResult.getTotalRecords() > 0){
	        if ( Connection == null){
	    		try {
	    			Connection = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
	    			Connection.setAutoCommit(false);
				} catch (SQLException e) {
					throw new Exception(e.getMessage());
				}
	    	}
	
	        insertTbTermBatch();
        }
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
        super.actionsAfterFile();
    }

    public String outputOneRecord(List record) throws Exception { 
    	
    	if ( Connection == null){
    		try {
    			Connection = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    			Connection.setAutoCommit(false);
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
    	}
    	String cardNo = "";
    	String cardCatId = "";
    	String balBonusQty = "";
    	String personId = "";
    	String locName = "";
    	String telHome = "";
    	String mobile = "";
    	String city = "";
    	String zipCode = "";
    	String address = "";
    	String regReason = "";
    	String expiryDate = "";
    	String issMemId = "";
    	String pCode = "";
    	String balanceId = "";
    	String balanceType = "";
    	
    	if (!isBlankOrNull(record.get(0).toString()))
    		cardNo = record.get(0).toString();
    	if (!isBlankOrNull(record.get(1).toString()))
    		cardCatId = record.get(1).toString();
    	if (!isBlankOrNull(String.valueOf(record.get(2))))
    		balBonusQty = record.get(2).toString();
    	if (!isBlankOrNull(record.get(3).toString()))
    		personId = BatchUtils.decript(record.get(3).toString());
    	if (!isBlankOrNull(record.get(7).toString()))
    		city = record.get(7).toString();
    	if (!isBlankOrNull(record.get(4).toString()))
    		locName = BatchUtils.decript(record.get(4).toString());
    	if (!isBlankOrNull(record.get(5).toString()))
    		telHome = BatchUtils.decript(record.get(5).toString());
    	if (!isBlankOrNull(record.get(6).toString()))
    		mobile = BatchUtils.decript(record.get(6).toString());
    	if (!isBlankOrNull(record.get(8).toString()))
    		zipCode = BatchUtils.decript(record.get(8).toString());
    	if (!isBlankOrNull(record.get(9).toString()))
    		address = BatchUtils.decript(record.get(9).toString());

    	if (!isBlankOrNull(record.get(11).toString()))
    		expiryDate = record.get(11).toString();
    	if (!isBlankOrNull(record.get(12).toString()))
    		issMemId = record.get(12).toString();
    	if (!isBlankOrNull(record.get(13).toString()))
    		pCode = record.get(13).toString();

		balanceId = cardNo;		
		balanceType = "C";
		
		if ( pCode.equals("9847") )
			regReason = "2";
		else if ( pCode.equals("9757") ||  pCode.equals("9827") )
			regReason = "1";
		
        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(cardCatId, 2, ' '));
        sb.append(StringUtils.rightPad(cardNo, 20, ' '));
        sb.append(StringUtils.leftPad(balBonusQty, 5, '0'));
        sb.append(StringUtils.rightPad(personId, 20, ' '));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(locName, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(telHome, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(mobile, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(city, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(zipCode, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(address, 300, ' '),encoding, 300));
        sb.append(StringUtils.rightPad(regReason, 1, ' '));
        
        if ( bonusId.length() != 0 ){
	        String lmsInvoiceNo = SequenceGenerator.getLmsInvoiceNo(Connection, batchDate);
	        //update TB_CARD_BAL
	        updateTbCardBal(cardNo, batchNo, balBonusQty);
	        
	        //insert 一筆 TB_TRANS
	        insertTrans(lmsInvoiceNo, cardNo, expiryDate, batchNo, balBonusQty, issMemId);
	        
	        //insert 一筆 TB_TRANS_DTL
	        insertTransDtl(lmsInvoiceNo, cardNo, expiryDate, batchNo, balBonusQty, issMemId, balanceType, balanceId);
        }
        
        return sb.toString();
    }
    
    public void insertTbTermBatch() throws SQLException {
    	
    	log.info("=======================insertTbTermBatch=======================");
    	try{
	    	TbTermBatchInfo tbTermBatchInfo = new TbTermBatchInfo();
	    	tbTermBatchInfo.setTxnSrc("B");
	    	tbTermBatchInfo.setMerchId(merchId);
	    	tbTermBatchInfo.setTermId(termId);
	    	tbTermBatchInfo.setBatchNo(batchNo);
	    	tbTermBatchInfo.setTermSettleDate(expFileInfo.getFileDate());
	    	tbTermBatchInfo.setTermSettleTime("000000");
	    	tbTermBatchInfo.setTermSettleFlag("1");
	    	tbTermBatchInfo.setStatus("1");
	    	tbTermBatchInfo.setInfile(expFileInfo.getFullFileName());
	    	tbTermBatchInfo.setParMon(tbTermBatchInfo.getTermSettleDate().substring(4, 6));
	    	tbTermBatchInfo.setParDay(tbTermBatchInfo.getTermSettleDate().substring(6, 8));
	    	tbTermBatchInfo.setCutDate(DateUtils.getSystemDate());
	    	tbTermBatchInfo.setCutTime(DateUtils.getSystemTime());
	    	tbTermBatchInfo.setTermUpDate(DateUtils.getSystemDate());
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
    
    public void updateTbCardBal(String cardNo, String batchNo, String balBonusQty) throws Exception
    {
        try {
        	String cardBalSql = "UPDATE TB_CARD_BAL SET " +
        			"DB_BONUS_QTY = DB_BONUS_QTY+"+ balBonusQty+", " +
        			"REFUND_DATE = '"+DateUtils.getSystemDate()+"' "+
            		"WHERE CARD_NO = '"+cardNo+"' " +
            		"AND BONUS_ID = '"+bonusId+"' " +
            		"AND BONUS_SDATE = '"+bonusSDate+"' " +
            		"AND BONUS_EDATE = '"+bonusEDate+"' ";
        	log.info("cardBalSql: "+cardBalSql);
            DBService.getDBService().sqlAction(cardBalSql.toString(), Connection); 
        }
        catch (Exception e)
        {
        	Connection.rollback();
        	Connection.close();
            throw e;
        } 
        finally {
		}
        
    }
    
    public void insertTrans(String lmsInvoiceNo, String cardNo, String expiryDate, 
    		String batchNo, String txnAmt, String issMemId) throws Exception
    {
    	log.info("=======================insertTrans=======================");
        TbTransInfo transInfo = new TbTransInfo();
        try {
            transInfo.setPCode(P_CODE);
            transInfo.setCardNo(cardNo);
            transInfo.setExpiryDate(expiryDate);
            
            transInfo.setTxnDate(DateUtils.getSystemDate());
            transInfo.setTxnTime(DateUtils.getSystemTime());        
            transInfo.setTermDate(DateUtils.getSystemDate());
            transInfo.setTermTime(DateUtils.getSystemTime());
            transInfo.setTermSettleDate(DateUtils.getSystemDate());
            transInfo.setTermSettleTime(DateUtils.getSystemTime());
            transInfo.setCutDate(DateUtils.getSystemDate());
            transInfo.setCutTime(DateUtils.getSystemTime());
            
            transInfo.setParMon(transInfo.getCutDate().substring(4, 6));
            transInfo.setParDay(transInfo.getCutDate().substring(6, 8));

            transInfo.setAcqMemId(memId);
            transInfo.setIssMemId(memId);
            transInfo.setMerchId(merchId);
            transInfo.setTermId(termId);
    
            transInfo.setLmsInvoiceNo(lmsInvoiceNo);
            transInfo.setTxnSrc("B");
            transInfo.setOnlineFlag("O");
            transInfo.setBatchNo(batchNo);
            transInfo.setRespCode("00");
            transInfo.setStatus("1");//reversal status=9    

            transInfo.setCreditAuthAmt(0);// number (14,2) default 0
            transInfo.setTxnAmt(Double.parseDouble(txnAmt));
            transInfo.setInvoiceRefNo("000001");
            transInfo.setAdviceFlag("00");//Digit 1: Update Point status;Digit 2: Update Coupon status(0: success,1: Fail) default="00"
     
            TbTransMgr tbTransMgr = new TbTransMgr(Connection);   
            log.info("transInfo: "+transInfo);
            tbTransMgr.insert(transInfo);
            //transInfo.setRegularId(regularinfo.getRegularId());
        }
        catch (Exception e)
        {
        	Connection.rollback();
        	Connection.close();
            throw e;
        } 
        finally {
		}
    }

    public void insertTransDtl(String lmsInvoiceNo, String cardNo, String expiryDate, 
    		String batchNo, String txnAmt, String issMemId, String balanceType, String balanceId) throws Exception
    {
    	log.info("=====================insertTransDtl=========================");
        TbTransDtlInfo transDtlInfo = new TbTransDtlInfo();
        try {
        	transDtlInfo.setRegionId("TWN");
        	transDtlInfo.setBonusBase("C");
        	transDtlInfo.setBalanceType(balanceType);
        	transDtlInfo.setBalanceId(balanceId);
        	transDtlInfo.setCardNo(cardNo);
        	transDtlInfo.setExpiryDate(expiryDate);
        	transDtlInfo.setLmsInvoiceNo(lmsInvoiceNo);
        	transDtlInfo.setPCode(P_CODE);
        	transDtlInfo.setTxnCode(TXN_CODE);
        	transDtlInfo.setBonusId(bonusId);
        	transDtlInfo.setBonusSdate(bonusSDate);
        	transDtlInfo.setBonusEdate(bonusEDate);
        	transDtlInfo.setBalProcDate("00000000");
        	transDtlInfo.setBonusQty(Integer.parseInt(txnAmt));
        	
            transDtlInfo.setCutDate(DateUtils.getSystemDate());
            transDtlInfo.setParMon(transDtlInfo.getCutDate().substring(4, 6));
            transDtlInfo.setParDay(transDtlInfo.getCutDate().substring(6, 8));
            
            transDtlInfo.setBonusBeforeQty(Double.parseDouble(txnAmt));
            transDtlInfo.setBonusCrQty(0);
            transDtlInfo.setBonusDbQty(Double.parseDouble(txnAmt));
            transDtlInfo.setBonusAfterQty(0);

            TbTransDtlMgr tbTransDtlMgr = new TbTransDtlMgr(Connection);   
            log.info("tbTransDtlMgr: "+tbTransDtlMgr);
            tbTransDtlMgr.insert(transDtlInfo);
        }
        catch (Exception e)
        {
        	Connection.rollback();
        	Connection.close();
            throw e;
        } 
        finally {
		}
    }
    
    public void rollback()  throws Exception {
    	
    	if ( !Connection.isClosed() && Connection != null ){
    		Connection.rollback();
    		Connection.close();
    	}
    }
    
	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpRmCard getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpRmCard instance = (ExpRmCard) apContext.getBean("expRmCard");
        return instance;
    }
	
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals("") || value.equals("null"));
	}
    
/*    private void process(Connection connection) throws Exception
    {
        BatchHandleResult result = handler.handle(connection, batchDate, getBatchResultInfo());

        setErrorDesc(result.getErrorDescribe());
        setRcode(result.getRcode());
    }*/
    
    public static void main(String[] args) {
    	ExpRmCard expRmCard = null;
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
            	expRmCard = getInstance();
            	expRmCard.setBatchDate(batchDate);
            	// 註: 此 method 一定要先呼叫
            	expRmCard.beforeHandling();
            	expRmCard.run(args);
            }
            else {
            	log.error("ExpRmCard not have spring file.");
            }
        }
        catch (Exception ignore) {
            log.warn("ExpRmCard run fail:" + ignore.getMessage(), ignore);
        }
    }
}
