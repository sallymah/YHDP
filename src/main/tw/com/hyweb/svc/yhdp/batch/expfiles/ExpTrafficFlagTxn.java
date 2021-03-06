/**
 * changelog ExpTerm
 * --------------------
 * 20081223
 * duncan
 * bug fix, 若有中文, 字數會算錯, 並加上可以設定 encoding 的功能
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.AbstractExpFile;

import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;
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
import java.util.Vector;

/**
 * <pre> 
 * ExpAssociator
 * </pre>
 * author:duncan
 */
public class ExpTrafficFlagTxn extends AbstractExpFile {
    private static Logger log = Logger.getLogger(ExpTrafficFlagTxn.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpTrafficFlagTxn" + File.separator + "spring.xml";
    private static final String TERM_FN = "TERM";
    private static final String TERM_APPEND = "TERM";

    // 產生檔案的 encoding
    private String encoding = "";
    // 要匯出會員檔的 member
    private List<String> busMemId = null;
    private List<String> metroMemId = null;
    // key:String(memId), value:String(seqno)
    private HashMap memId2Seqno = new HashMap();
    // key:String(memId), value:Integer(此 member 有幾張 card 的資料要匯出)
    private HashMap memId2Count = new HashMap();
    // 當此 memId 下的會員資料 <= 10000 時, 才 enable cache 功能
    // key:String(memId:associatorId), value:TbAssociatorInfo
    private HashMap transationInfos = null;
    private String memId = "";
    
    public ExpTrafficFlagTxn() {
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

    public List<String> getbusMemId() {
        return busMemId;
    }

    public void setbusMemId(List<String> busMemId) {
        this.busMemId = busMemId;
    }
    
    public List<String> getMetroMemId() {
        return metroMemId;
    }

    public void setMetroMemId(List<String> metroMemId) {
        this.metroMemId = metroMemId;
    }

    public void doProc() throws SQLException {

        /*FileAppender appender = (FileAppender)log.getAppender("FILE-BATCH");
        log.debug(appender);*/
       // BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log.), "UTF-8"));
        Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        Statement stmt = null;
        ResultSet rs = null;
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("SELECT CARD_NO, LMS_INVOICE_NO, ATC, TXN_DATE, TXN_TIME, A.P_CODE, B.P_CODE_DESC, A.MERCH_ID, C.MERCH_LOC_NAME, TXN_AMT, TERM_ID");
        sqlCmd.append(" FROM TB_ONL_TXN A, TB_P_CODE_DEF B, TB_MERCH C WHERE TXN_SRC='B' AND A.P_CODE = B.P_CODE AND A.MERCH_ID = C.MERCH_ID");
        sqlCmd.append(" AND A.STATUS = '1' AND TXN_DATE >= ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        sqlCmd.append(genMetroMemIdString());
        sqlCmd.append(" order by TXN_DATE || TXN_TIME");
        //log.debug(sqlCmd.toString());
        log.info("TRAF_FLG 卡號,ATC,交易代碼,交易代碼描述,交易日,交易時間,交易序號,交易金額,特店,端末");
        try {
        	Vector result = DbUtil.select(sqlCmd.toString(), conn);
        	TbOnlTxnInfo txnInfo;
        	if(null != result && result.size() > 0)
        	{
        	    for(int idx = 0; idx < result.size() ; idx ++)
        	    {
        	        Vector record = (Vector)result.get(idx);
        	        if(null != record && record.size() > 0)
                    {
        	            txnInfo = new TbOnlTxnInfo();
        	            txnInfo.setCardNo((String)record.get(0));
        	            txnInfo.setLmsInvoiceNo((String)record.get(1));
        	            txnInfo.setAtc((String)record.get(2));
        	            txnInfo.setTxnDate((String)record.get(3));
        	            txnInfo.setTxnTime((String)record.get(4));
        	            txnInfo.setPCode((String)record.get(5));
        	            txnInfo.setProductInfo((String)record.get(6));
        	            txnInfo.setMerchId((String)record.get(7));
        	            txnInfo.setTxnNote((String)record.get(8));
        	            txnInfo.setTxnAmt((Number)record.get(9));
        	            txnInfo.setTermId((String)record.get(10));
        	            checkIsBusTxn(txnInfo, conn);
                    }
        	    }
        	}
        }
        finally {
              ReleaseResource.releaseDB(conn, stmt, rs);
        }
    }
    
    public boolean checkIsBusTxn(TbOnlTxnInfo txnInfo, Connection conn) throws SQLException
    {
        boolean isBus = false;
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("SELECT CARD_NO, LMS_INVOICE_NO, ATC, TXN_DATE, TXN_TIME, A.P_CODE, B.P_CODE_DESC, A.MERCH_ID, C.MERCH_LOC_NAME, TXN_AMT, TERM_ID");
        sqlCmd.append(" FROM TB_ONL_TXN A, TB_P_CODE_DEF B, TB_MERCH C WHERE TXN_SRC='B' AND A.P_CODE = B.P_CODE AND A.MERCH_ID = C.MERCH_ID");
        sqlCmd.append(" AND A.STATUS = '1' AND ATC IN (");
        sqlCmd.append(getAtc(txnInfo.getAtc(), 1));
        sqlCmd.append(",");
        sqlCmd.append(getAtc(txnInfo.getAtc(), -1));
        sqlCmd.append(" ) AND A.CARD_NO = ? AND TXN_DATE >= ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        sqlCmd.append(genBusMmeIdString());
        sqlCmd.append(" order by atc");
        Vector<String> parm = new Vector<String>();
        parm.add(txnInfo.getCardNo());
        
        Vector result = DbUtil.select(sqlCmd.toString(), parm, conn);
        TbOnlTxnInfo txnInfoDtl;
        if(null != result && result.size() > 0)
        {
            log.info("TRAF_FLG");
            log.info("TRAF_FLG ---- 捷運 ---");
            log.info("TRAF_FLG " + txnInfo.getCardNo()+ "," +txnInfo.getAtc()  + "," +txnInfo.getPCode() + "," +txnInfo.getProductInfo()+ "," +txnInfo.getTxnDate()+ "," +txnInfo.getTxnTime()+ "," +txnInfo.getLmsInvoiceNo() + "," +txnInfo.getTxnAmt()+ "," +txnInfo.getTxnNote());
            log.info("TRAF_FLG ---- bus dtl ---");
            for(int idx = 0; idx < result.size() ; idx ++)
            {
                Vector record = (Vector)result.get(idx);
                if(null != record && record.size() > 0)
                {
                    txnInfoDtl = new TbOnlTxnInfo();
                    txnInfoDtl.setCardNo((String)record.get(0));
                    txnInfoDtl.setLmsInvoiceNo((String)record.get(1));
                    txnInfoDtl.setAtc((String)record.get(2));
                    txnInfoDtl.setTxnDate((String)record.get(3));
                    txnInfoDtl.setTxnTime((String)record.get(4));
                    txnInfoDtl.setPCode((String)record.get(5));
                    txnInfoDtl.setProductInfo((String)record.get(6));
                    txnInfoDtl.setMerchId((String)record.get(7));
                    txnInfoDtl.setTxnNote((String)record.get(8));
                    txnInfoDtl.setTxnAmt((Number)record.get(9));
                    txnInfo.setTermId((String)record.get(10));
                    log.info("TRAF_FLG "+  txnInfoDtl.getCardNo()+ "," +txnInfoDtl.getAtc()  + "," +txnInfoDtl.getPCode() + "," +txnInfoDtl.getProductInfo()+ "," +txnInfoDtl.getTxnDate()+ "," +txnInfoDtl.getTxnTime()+ "," +txnInfoDtl.getLmsInvoiceNo() + "," +txnInfoDtl.getTxnAmt() + "," + txnInfoDtl.getTxnNote());
                }
            }
        }
        return isBus;
    }
    
    public String getAtc(String atc, int add)
    {
        int intAtc = Integer.valueOf(atc);
        intAtc += add;
        return StringUtil.toSqlValueWithSQuote(ISOUtil.padLeft(String.valueOf(intAtc), 8, '0'));
    }
    
    public String genBusMmeIdString()
    {
        StringBuffer sb = new StringBuffer();
        
        if(null != busMemId && busMemId.size() > 0)
        {
            for(int idx =0; idx < busMemId.size(); idx++)
            {
                if(!StringUtil.isEmpty(busMemId.get(idx)))
                {
                    if(sb.length() > 0)
                    {
                        sb.append(",");
                    }
                    sb.append(StringUtil.toSqlValueWithSQuote(busMemId.get(idx)));
                }
            }
        }
        if(sb.length() > 0)
        {
            sb.insert(0, " AND ACQ_MEM_ID IN (");
            sb.append(")");
        }
        return sb.toString();
    }
    
    public String genMetroMemIdString()
    {
        StringBuffer sb = new StringBuffer();
        
        if(null != metroMemId && metroMemId.size() > 0)
        {
            for(int idx =0; idx < metroMemId.size(); idx++)
            {
                if(!StringUtil.isEmpty(metroMemId.get(idx)))
                {
                    if(sb.length() > 0)
                    {
                        sb.append(",");
                    }
                    sb.append(StringUtil.toSqlValueWithSQuote(metroMemId.get(idx)));
                }
            }
        }
        if(sb.length() > 0)
        {
            sb.insert(0, " AND ACQ_MEM_ID IN (");
            sb.append(")");
        }
        return sb.toString();
    }
    
    public String outputAfterFile() {
        return super.outputAfterFile();
    }

    public String outputBeforeFile() {    	

    	//197
        StringBuffer header = new StringBuffer();
        header.append("H0");
        header.append(batchDate);
        header.append(StringUtil.pendingKey(expFileResult.getTotalRecords(), 8));
        header.append(StringUtils.rightPad("", 177, ' '));
        return header.toString();
    }

    public String outputEndFile() {    	
    	//197
        StringBuffer header = new StringBuffer();
        header.append("/EOF");
        header.append(StringUtils.rightPad("", 191, ' '));
        return header.toString();
    }
    
    public void actionsAfterInfo() throws Exception {
        // 處理完一個 expFileInfo 後要做什麼事, default 不做任何事
        super.actionsAfterInfo();
    }

    public void actionsAfterFile() throws Exception {
        // 處理完一個檔案後要做什麼事, default 不做任何事
        super.actionsAfterFile();
    }

    public String outputOneRecord(List record) { 

    	String termId = "";
    	String merchId = "";
    	String storeCounterId = "";
    	String ecrId = "";
    	String status = "";
    	String effectiveDate = "";
    	String terminationDate = "";
    	String termVendor = "";
    	String termType = "";
    	String ud1 = "";
    	String ud2 = "";
    	String ud3 = "";
    	String ud4 = "";
    	String ud5 = "";

    	termId = record.get(0).toString();
    	merchId = record.get(1).toString();
    	if (!isBlankOrNull(record.get(2).toString()))
	    	storeCounterId = record.get(2).toString();
    	if (!isBlankOrNull(record.get(3).toString()))
	    	ecrId = record.get(3).toString();
    	status = record.get(4).toString();
    	if (!isBlankOrNull(record.get(5).toString()))
	    	effectiveDate = record.get(5).toString();
    	if (!isBlankOrNull(record.get(6).toString()))
	    	terminationDate = record.get(6).toString();
    	if (!isBlankOrNull(record.get(7).toString()))
	    	termVendor = record.get(7).toString();
    	if (!isBlankOrNull(record.get(8).toString()))
	    	termType = record.get(8).toString();
    	if (!isBlankOrNull(record.get(9).toString()))
	    	ud1 = record.get(9).toString();
    	if (!isBlankOrNull(record.get(10).toString()))
	    	ud2 = record.get(10).toString();
    	if (!isBlankOrNull(record.get(11).toString()))
	    	ud3 = record.get(11).toString();
    	if (!isBlankOrNull(record.get(12).toString()))
	    	ud4 = record.get(12).toString();
    	if (!isBlankOrNull(record.get(13).toString()))
	    	ud5 = record.get(13).toString();

        // cat master record
        StringBuffer sb = new StringBuffer();
        sb.append("DT");
        sb.append(StringUtils.rightPad(termId, 8, ' '));
        sb.append(StringUtils.rightPad(merchId, 15, ' '));
        sb.append(StringUtils.rightPad(storeCounterId, 5, ' '));
        sb.append(StringUtils.rightPad(ecrId, 8, ' '));
        sb.append(StringUtils.rightPad(status, 1, ' '));
        sb.append(StringUtils.rightPad(effectiveDate, 8, ' '));
        sb.append(StringUtils.rightPad(terminationDate, 8, ' '));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(termVendor, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(termType, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud1, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud2, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud3, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud4, 20, ' '),encoding, 20));
        sb.append(Layer2Util.getMaxString(StringUtils.rightPad(ud5, 20, ' '),encoding, 20));
        
        
        return sb.toString();
    }

	public List<String> outputDtlRecord(List record) {      
       return null;
    }

	public static ExpTrafficFlagTxn getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ExpTrafficFlagTxn instance = (ExpTrafficFlagTxn) apContext.getBean("ExpTrafficFlagTxn");
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
        ExpTrafficFlagTxn expTerm = null;
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
            	expTerm = getInstance();
            }
            else {
            	expTerm = new ExpTrafficFlagTxn();
            }
            expTerm.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            expTerm.doProc();
        }
        catch (Exception ignore) {
            log.warn("ExpTerm run fail:" + ignore.getMessage(), ignore);
        }
    }

    @Override
    public ExpFileSetting makeExpFileSetting()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
