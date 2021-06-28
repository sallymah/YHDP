package  tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTeff;

/**
 * changelog ExpTeff
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
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.FF11;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.FF21;
import tw.com.hyweb.svc.yhdp.batch.util.BatchUtils;
import tw.com.hyweb.util.ISOUtil;
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
public class ExpTeff extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpTeff.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpTeff" + File.separator + "spring.xml";
    private static final String TEFF_FN = "TEFF";
    private static final String TEFF_APPEND = "TEFF";

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
    
    private String configFile;
	private RespMacDataFiller respMacDataFiller;
	private Merch2Eca merch2Eca;
    
    public ExpTeff(RespMacDataFiller respMacDataFiller, Merch2Eca merch2Eca) {
    	this.respMacDataFiller = respMacDataFiller;
    	this.merch2Eca = merch2Eca;
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
    public String getConfigFile() {
		return configFile;
	}
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	public RespMacDataFiller getRespMacDataFiller() {
		return respMacDataFiller;
	}
	public void setRespMacDataFiller(RespMacDataFiller respMacDataFiller) {
		this.respMacDataFiller = respMacDataFiller;
	}

	private void beforeHandling() throws SQLException {

    	Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    	Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        StringBuffer sql = new StringBuffer();

    	try {
    		sql.append("SELECT MEM_ID, MEM_GROUP_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1)='1' AND ACQ_TYPE = '1' ");
    		if ( !BatchUtils.isBlankOrNull(this.memId) )
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
	                      		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(TEFF_FN) + 
	                      		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
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
    			//log.warn("memIds="+memIds);
    			
    			memId2MemGroupId.put(memId, memGroupId);
    			memId2Seqno.put(memId , seqno);       
    			memIds.add(memId);
    			
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
            expFileInfo.setFileName(TEFF_FN);
            expFileInfo.setFileDate(batchDate);
            expFileInfo.setSeqno(seqno);
            // memId 若不是同樣長度時會有問題
            String fullFileName = TEFF_APPEND + "." +memId + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
            expFileInfo.setFullFileName(fullFileName);
            String selectSQL =
            		"SELECT "+
    				"TXN.ISS_MEM_ID, TXN.P_CODE, TXN.TERM_DATE, TXN.TERM_TIME, TXN.LMS_INVOICE_NO, TXN.CARD_NO, TXN.EXPIRY_DATE, TXN.ATC, TXN.TXN_DATE, TXN.TXN_TIME, TXN.MERCH_ID, "+
    				"TXN.TERM_ID, TXN.CHIP_POINT1_BEFORE, TXN.TXN_AMT, TXN.CHIP_POINT1_AFTER, TXN.BATCH_NO, TXN.INVOICE_REF_NO, TXN.DEVICE_ID, TXN.TXN_ACCESS_MODE, TXN.RECORD_DATE, " +
    				"TXN.TSAMOSN, "+

    				//tb_card
    				"CARD_CAT_ID, CARD_TYPE_ID, HG_CARD_NO, AUTO_RELOAD_FLAG, CARD.STATUS "+
    				"FROM TB_ONL_TXN TXN, TB_CARD CARD "+
    				"WHERE TXN.ACQ_MEM_ID = '"+memId+"' "+
    				"AND TXN.TXN_DATE = '"+BatchUtil.getSomeDay(batchDate,-1)+"' " +
    				"AND TXN.P_CODE IN ('7707','7717') "+
    				"AND TXN.EXP_FILE_NAME IS NULL "+
    				"AND TXN.CARD_NO = CARD.CARD_NO " +
    				"AND TXN.EXPIRY_DATE = CARD.EXPIRY_DATE";
            
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
        header.append(TEFF_FN);
        //header.append(StringUtils.rightPad(expFileResult.getExpFileInfo().getMemId().toString(),8,""));
        header.append(StringUtils.rightPad(batchDate,8,""));
        String dateTime = DateUtil.getTodayString();
        header.append(StringUtils.rightPad(dateTime.substring(8, 14),6,"0"));
        header.append(StringUtils.rightPad("", 19, ' '));
        return header.toString();
    }
    
    public String outputEndFile() {    	
    	//96
        StringBuffer header = new StringBuffer();
        header.append("T");
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.leftPad( takeDecimal(String.valueOf(total),2), 15, '0'));
        header.append(StringUtils.rightPad("", 14, ' '));
        return header.toString();
    }

    public void actionsAfterInfo() throws Exception {
		// 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
        
        String sql = null;
    	Statement stmt = null;
    	
		try {
			stmt = conn.createStatement();
			sql = 	"UPDATE TB_ONL_TXN SET " +
					"EXP_FILE_NAME = '"+ expFileInfo.getExpFullFileName() +"' " +
					"WHERE ACQ_MEM_ID = '"+expFileInfo.getMemId()+"' "+
	        		"AND TXN_DATE = '"+BatchUtil.getSomeDay(batchDate,-1)+"' " +
	        		"AND P_CODE IN ('7707','7717') "+
	        		"AND EXP_FILE_NAME IS NULL " +
	        		"AND EXISTS (SELECT * FROM TB_CARD " +
	        		"WHERE TB_ONL_TXN.CARD_NO = TB_CARD.CARD_NO " +
	        		"AND TB_ONL_TXN.EXPIRY_DATE = TB_CARD.EXPIRY_DATE)";
			
			log.debug("remarkExpData sql: "+sql);
			stmt.executeUpdate(sql);

		} catch (SQLException ignore) {
			log.warn("query totalCount fail for '" + sql + "':" + ignore.getMessage(), ignore);
		}
		finally {
            ReleaseResource.releaseDB(null, stmt, null);
		}
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
    	total=0;
        super.actionsAfterFile();
    }

    public String outputOneRecord(List record) throws Exception { 

    	String tsamosn = "";
    	String cardCatId = "";
    	String cardTypeId = "";
    	String hgCardNo = "";
    	String autoReloadFlag = "";
    	String status = "";
    	
    	TbOnlTxnInfo tbOnlTxnInfo = new TbOnlTxnInfo();
    	
    	tbOnlTxnInfo.setIssMemId(record.get(0).toString());
    	tbOnlTxnInfo.setPCode(record.get(1).toString());
    	tbOnlTxnInfo.setTermDate(record.get(2).toString());
    	tbOnlTxnInfo.setTermTime(record.get(3).toString());
    	tbOnlTxnInfo.setLmsInvoiceNo(record.get(4).toString());
    	tbOnlTxnInfo.setCardNo(record.get(5).toString());
    	tbOnlTxnInfo.setExpiryDate(record.get(6).toString());
    	tbOnlTxnInfo.setAtc(record.get(7).toString());
    	tbOnlTxnInfo.setTxnDate(record.get(8).toString());
    	tbOnlTxnInfo.setTxnTime(record.get(9).toString());
    	TbMerchInfo tbMerchInfo = (TbMerchInfo) merch2Eca.getMerch2Eca().get(record.get(10));
    	tbOnlTxnInfo.setMerchId(tbMerchInfo.getEcaMerchId());
    	tbOnlTxnInfo.setTermId(record.get(11).toString());
    	tbOnlTxnInfo.setChipPoint1Before(Double.parseDouble(record.get(12).toString()));
    	tbOnlTxnInfo.setTxnAmt(Double.parseDouble(record.get(13).toString()));
    	tbOnlTxnInfo.setChipPoint1After(Double.parseDouble(record.get(14).toString()));
    	tbOnlTxnInfo.setBatchNo(record.get(15).toString());
    	tbOnlTxnInfo.setInvoiceRefNo(record.get(16).toString());
    	tbOnlTxnInfo.setDeviceId(record.get(17).toString());
    	tbOnlTxnInfo.setTxnAccessMode(record.get(18).toString());
    	tbOnlTxnInfo.setRecordDate(record.get(19).toString());
    	if ( !BatchUtils.isBlankOrNull(record.get(20).toString()) )
    		tsamosn = record.get(20).toString();
    	
    	cardCatId = record.get(21).toString();
    	cardTypeId = record.get(22).toString();
    	if ( !BatchUtils.isBlankOrNull(record.get(23).toString()) )
    		hgCardNo = record.get(23).toString();
    	if ( !BatchUtils.isBlankOrNull(record.get(24).toString()) )
    		autoReloadFlag = record.get(24).toString();
    	status = record.get(25).toString();
    	
        // cat master record
        StringBuffer sb = new StringBuffer();
        
        //小白單
        sb.append("D");
        sb.append("01");
        sb.append(StringUtils.rightPad(cardCatId, 2, ' '));
        sb.append(StringUtils.rightPad(cardTypeId, 2, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getPCode(), 4, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getTermDate()+tbOnlTxnInfo.getTermTime(), 14, ' '));
        sb.append("0000");
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getLmsInvoiceNo(), 12, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getCardNo(), 20, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getAtc(), 8, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getTxnDate()+tbOnlTxnInfo.getTxnTime(), 14, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getMerchId(), 15, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getTermId(), 8, ' '));
        
        //交易前餘額  100 -> 000000010000; -100 -> 100000010000
        String beforeBal = "";
        beforeBal = tbOnlTxnInfo.getChipPoint1Before().doubleValue() < 0 ? "1" : "0";
        beforeBal = beforeBal + StringUtils.leftPad(
        		takeDecimal(String.valueOf(Math.abs(tbOnlTxnInfo.getChipPoint1Before().doubleValue())),2), 9,'0');
        sb.append(beforeBal);
        //交易金額
        String amt = "";
        amt = tbOnlTxnInfo.getTxnAmt().doubleValue() < 0 ? "1" : "0";
        amt = amt + StringUtils.leftPad(
        		takeDecimal(String.valueOf(Math.abs(tbOnlTxnInfo.getTxnAmt().doubleValue())),2), 11,'0');
        sb.append(amt);
        //自動加值
        sb.append("0000000000");
        //交易後餘額
        String afterBal = "";
        afterBal = tbOnlTxnInfo.getChipPoint1After().doubleValue() < 0 ? "1" : "0";
        afterBal = afterBal + StringUtils.leftPad(
        		takeDecimal(String.valueOf(Math.abs(tbOnlTxnInfo.getChipPoint1After().doubleValue())),2), 9,'0');
        sb.append(afterBal);
        
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getBatchNo(), 6, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getInvoiceRefNo(), 32, ' '));
        sb.append(StringUtils.rightPad(hgCardNo, 20, ' '));
        sb.append(StringUtils.rightPad(autoReloadFlag.equalsIgnoreCase("Y") ? "1": "2", 1, ' '));
        sb.append(StringUtils.rightPad(tbOnlTxnInfo.getDeviceId(), 10, ' '));
        
        //ssout
        BerTLV berTLV = new BerTLV();
        
        FF11 ff11 = new FF11();
        ff11.setStan(tbOnlTxnInfo.getInvoiceRefNo());
        ff11.setCardNo(StringUtils.rightPad(tbOnlTxnInfo.getCardNo(), 20, 'F'));
        ff11.setCardExp(tbOnlTxnInfo.getExpiryDate());
        ff11.setInvoiceNo(tbOnlTxnInfo.getLmsInvoiceNo());
        ff11.setAfterBal(afterBal);
        ff11.setPcode(tbOnlTxnInfo.getPCode());
        
        berTLV.addHexStr(0xff11, ff11.pack());
        
        FF21 ff21 = new FF21();
        ff21.setRespCode("00");
		ff21.setTid(tbOnlTxnInfo.getTermId());
		ff21.setMid(tbOnlTxnInfo.getMerchId());
		ff21.setBatchNo(tbOnlTxnInfo.getBatchNo());
		ff21.setIssNo(tbOnlTxnInfo.getIssMemId());
		ff21.setStoreCntArea(StringUtils.rightPad("", 18, ' '));//專櫃
		ff21.setIccAtc(StringUtils.leftPad(tbOnlTxnInfo.getAtc(), 8, '0'));
		ff21.setTermDateTime(tbOnlTxnInfo.getTermDate()+tbOnlTxnInfo.getTermTime());
		ff21.setHostDateTime(tbOnlTxnInfo.getTxnDate()+tbOnlTxnInfo.getTxnTime());
		ff21.setEcashAmt(StringUtils.leftPad(amt.substring(0,1)+ amt.substring(amt.length()-9, amt.length()), 10, '0'));
		ff21.setLmsAmt(StringUtils.leftPad("", 10,'0'));
		ff21.setSamArea(StringUtils.rightPad("", 16, ' '));
		ff21.setChipCardSts(StringUtils.leftPad(status, 2,'0'));
		ff21.setAccessMod(tbOnlTxnInfo.getTxnAccessMode());
		ff21.setEcashBeforeBal(beforeBal);
		ff21.setAutoLoadAmt("0000000000");
		ff21.setAutoLoadAtc("00000000");
		
		//log.debug(ff21.toString());
		//log.debug(new FF21(MsgUtil.FF21_SIZE, ff21.pack()).toString());
        
		berTLV.addHexStr(0xff21, ff21.pack());
		
		//berTLV.addHexStr(0xff34, iso8583.getBerTlv48().getHexStr(LMSTag.OriginalDataArea));
		berTLV.addHexStr(0xff50, tbOnlTxnInfo.getDeviceId());
		if ( !BatchUtils.isBlankOrNull(tbOnlTxnInfo.getRecordDate()) )
			berTLV.addHexStr(0xff53, tbOnlTxnInfo.getRecordDate());
		
		String mac = "0000000000000000";
		if ( getRespMacDataFiller() != null )
			mac = getRespMacDataFiller().validate(tsamosn, ISOUtil.hexString(berTLV.pack()));
		//berTLV.addHexStr(0xff41, iso8583.getBerTlv48().getHexStr(LMSTag.TermProfile));
		berTLV.addHexStr(0xff64, mac);
        
		sb.append(ISOUtil.hexString(berTLV.pack()));
		
		total = total+ tbOnlTxnInfo.getTxnAmt().doubleValue();
		
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
    public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpTeff getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpTeff instance = (ExpTeff) apContext.getBean("expTeff");
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
    	ExpTeff expTeff = null;
        try {
            String batchDate = System.getProperty("date");
            String mem_id = System.getProperty("memid");
            log.debug("date="+batchDate);
            log.debug("mem_id="+mem_id);
            
            if (StringUtil.isEmpty(mem_id)) {
            	mem_id="";
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
            	expTeff = getInstance();
            	expTeff.setBatchDate(batchDate);
                expTeff.setMemId(mem_id);
                // 註: 此 method 一定要先呼叫
                expTeff.beforeHandling();
                expTeff.run(args);
            }
            else {
            	log.error(SPRING_PATH + " is not exist. ");
            }
            
        }
        catch (Exception ignore) {
            log.warn("ExpTeff run fail:" + ignore.getMessage(), ignore);
        }
    }
}
