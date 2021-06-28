package tw.com.hyweb.svc.yhdp.batch.GenAutoRld;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.service.db.info.TbCardBalInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbCardBalMgr;
import tw.com.hyweb.service.db.mgr.TbOnlTxnDtlMgr;
import tw.com.hyweb.service.db.mgr.TbOnlTxnMgr;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class GenAutoRld extends AbstractBatchBasic {

	private static final Logger log = Logger.getLogger(GenAutoRld.class);

	private static final String SPRING_PATH = "config" + File.separator
			+ "batch" + File.separator + "GenAutoRld" + File.separator
			+ "spring.xml";

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private Connection conn = null;
	private String batchDate = null;
	private String sysDate = null;
	private String sysTime = null;
    private int sleepTime = 500;
    private static final String P_CODE = "7737";
    private static final String TXN_CODE = "8717";
    private static final String BONUS_SDATE = "00010101";
    private static final String BONUS_EDATE = "99991231";
    
	public static void main(String[] args) {

	    GenAutoRld instance = getInstance();

		instance.setBatchDate(System.getProperty("date"));

		instance.run(null);

		System.exit(1);
	}

	public static GenAutoRld getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		GenAutoRld instance = (GenAutoRld) apContext.getBean("GenAutoRld");
		return instance;
	}

	@Override
	public void process(String[] arg0) throws Exception {
		try {
			init();
			action();
		} finally {
			if (conn != null)
				ReleaseResource.releaseDB(conn);
		}
	}

	private void init() throws Exception {
		try {
			BatchUtil.getNow();
			if (StringUtil.isEmpty(batchDate)) {
				batchDate = BatchUtil.sysDay;
			} else if (!BatchUtil.checkChristianDate(batchDate)) {
				String msg = "Invalid date for option -Ddate!";
				throw new Exception(msg);
			}
			sysDate = BatchUtil.sysDay;
			sysTime = BatchUtil.sysTime;

			conn = BatchUtil.getConnection();

		} catch (Exception e) {
			throw new Exception("init():" + e);
		}
	}

	private void action() throws Exception {

	    String currentDate = DateUtil.getTodayString();
	    String hostDate = currentDate.substring(0, 8);
	    String hostTime = currentDate.substring(8);
		StringBuffer sqlCmd = new StringBuffer();
		try {
			Vector<String> params = new Vector<String>();
			
			StringBuffer memSql = new StringBuffer();
			memSql.append("SELECT MEM_ID FROM TB_MEMBER WHERE NEW_TRANS_FLAG = '1'");
    		
    		if (Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
    			memSql.append(" AND JOB_ID IS NULL");
    			memSql.append(" AND JOB_TIME IS NULL");
    		}
    		else{
	        	if(!StringUtil.isEmpty(getBatchResultInfo().getJobId()) 
				&& !getBatchResultInfo().getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
	        		memSql.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobId()));
		    		if(!StringUtil.isEmpty(getBatchResultInfo().getJobTime()) 
					&& !getBatchResultInfo().getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
		    			memSql.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobTime()));
			    	}
		    	}
		    	if(!StringUtil.isEmpty(getBatchResultInfo().getMemId())){
		    		memSql.append(" AND MEM_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getMemId()));
		    	}
    		}

			sqlCmd.append(" SELECT A.CARD_NO, A.EXPIRY_DATE, A.LMS_INVOICE_NO,");
			sqlCmd.append(" A.TXN_DATE, A.TXN_TIME, A.AUTOLOAD_VALUES, A.AUTOLOAD_ATC,");
			sqlCmd.append(" A.ACQ_MEM_ID, A.ISS_MEM_ID, A.MERCH_ID, A.TERM_ID,");
			sqlCmd.append(" A.BATCH_NO, A.CHIP_POINT1_BEFORE");
			sqlCmd.append(" FROM TB_ONL_TXN A, (").append(memSql).append(") B");
			sqlCmd.append(" WHERE A.IMP_FILE_DATE = ?");
			sqlCmd.append(" AND A.P_CODE = '7647'");
			sqlCmd.append(" AND A.AUTOLOAD_LMS_NO IS NULL");
			sqlCmd.append(" AND A.AUTOLOAD_ATC IS NOT NULL");
			sqlCmd.append(" AND A.AUTOLOAD_VALUES IS NOT NULL");
			sqlCmd.append(" AND A.AUTOLOAD_VALUES > 0");
			sqlCmd.append(" AND A.ACQ_MEM_ID = B.MEM_ID");
			sqlCmd.append(" ORDER BY A.MERCH_ID, A.TERM_ID");
		    
		    params.add(batchDate);
		    
		    String bonusId = "";
		    Vector vector = DbUtil.select(sqlCmd.toString(), params, conn);
		    if(null != vector && vector.size() > 0)
		    {
		    	String batchNo = SequenceGenerator.getBatchNoByType(conn, SequenceGenerator.TYPE_BATCH);
		    	
		        for(int idx = 0; idx < vector.size() ; idx++)
		        {
		            TbOnlTxnInfo txnInfo = genOnlTxn((Vector) vector.get(idx), hostDate, hostTime);
		            if(null != txnInfo)
		            {
		                bonusId = getBonusId(txnInfo.getCardNo());
		                TbOnlTxnDtlInfo txnDtlInf = genOnlTxnDtl(txnInfo, hostDate, hostTime);
                        if(null != txnDtlInf)
                        {
                            String lmsInvoNo = SequenceGenerator.getLmsInvoiceNo(conn, hostDate);
                            txnInfo.setLmsInvoiceNo(lmsInvoNo);
                            txnInfo.setBatchNo(batchNo);
                            txnDtlInf.setLmsInvoiceNo(lmsInvoNo);
                            txnDtlInf.setBonusId(bonusId);
                            try {
                                updateBal(txnInfo, bonusId);
                                updateOrigTxn(txnInfo);
                                insertTxn(txnInfo, txnDtlInf);
                                insertTermBatch(txnInfo, hostDate, hostTime);
                                conn.commit();
                            } catch (SQLException e) {
                                log.error("", e);
                                conn.rollback();
                            }
                        }
		            }
		        }
		    }
			
		} catch (SQLException e) {
			remarkFail();
			throw new Exception("action() SQL execute failed. " , e);
		}
		catch (Exception e) {
            remarkFail();
            throw new Exception("action() execute failed. " , e);
        }
	}
	
	public boolean checkIsTermBatch(TbOnlTxnInfo txnInfo) throws SQLException
    {
        String sqlCmd = "select count(1) from tb_term_batch where txn_src='B' AND term_id = ? and merch_id = ? and batch_no = ? and term_settle_date = ?";
        Vector<String> parms = new Vector<String>();
        parms.add(txnInfo.getTermId());
        parms.add(txnInfo.getMerchId());
        parms.add(txnInfo.getBatchNo());
        parms.add(txnInfo.getTermSettleDate());
        int  count = DbUtil.getInteger(sqlCmd, parms, conn);        
        return count > 0 ? true : false;
    }
	
	/*
	 * 新增term_batch
	 */
	public void insertTermBatch(TbOnlTxnInfo txnInfo, String hostDate, String hostTime) throws SQLException
	{
	    if(!checkIsTermBatch(txnInfo))
	    {
	        TbTermBatchMgr termBatchMgr = new TbTermBatchMgr(conn);
	        TbTermBatchInfo termBatchInfo = new TbTermBatchInfo();
	        termBatchInfo.setMerchId(txnInfo.getMerchId());
	        termBatchInfo.setTermId(txnInfo.getTermId());
	        termBatchInfo.setTermSettleDate(txnInfo.getTermSettleDate());
	        termBatchInfo.setTermSettleTime(txnInfo.getTermSettleTime());
	        termBatchInfo.setStatus("1");
	        termBatchInfo.setImpFileName("GenAutoRld");
	        termBatchInfo.setInfile("GenAutoRld");
	        termBatchInfo.setTxnSrc("B");
	        termBatchInfo.setBatchNo(txnInfo.getBatchNo());
	        termBatchInfo.setTermSettleFlag("1");
	        termBatchInfo.setParMon(hostDate.substring(4,6));//for partition {01~12}
	        termBatchInfo.setParDay(hostDate.substring(6,8));//for partition {01~31}
            termBatchInfo.setTermUpDate(hostDate);
            termBatchInfo.setImpFileName(termBatchInfo.getImpFileName());
            termBatchMgr.insert(termBatchInfo);
	    }
	}

	/*
	 * 更新餘額
	 */
	public void updateBal(TbOnlTxnInfo txnInfo, String bonusId) throws SQLException
	{
	    String sqlCmd = "update tb_card_bal set cr_bonus_qty = cr_bonus_qty + ? where card_no = ? and expiry_date = ? and bonus_id = ?  and bonus_sdate = ? and bonus_edate = ?";
        Vector params = new Vector();
        params.add(txnInfo.getTxnAmt());
        params.add(txnInfo.getCardNo());
        params.add(txnInfo.getExpiryDate());
        params.add(bonusId);
        params.add(BONUS_SDATE);
        params.add(BONUS_EDATE);
        SqlResult sr = DbUtil.sqlAction(sqlCmd, params, conn);
        if (null == sr || 0 == sr.getRecordCount())
        {
            TbCardBalInfo balInfo = new TbCardBalInfo();
            TbCardBalMgr balMgr = new TbCardBalMgr(conn);
            balInfo.setCardNo(txnInfo.getCardNo());
            balInfo.setExpiryDate(txnInfo.getExpiryDate());
            balInfo.setBonusId(bonusId);
            balInfo.setCrBonusQty(txnInfo.getTxnAmt());
            balInfo.setBonusSdate(BONUS_SDATE);
            balInfo.setBonusEdate(BONUS_EDATE);
            balMgr.insert(balInfo);
        }
	}
	
	/*
	 * 註記已處理過自動加值
	 */
	public void updateOrigTxn(TbOnlTxnInfo txnInfo) throws SQLException
    {
        String sqlCmd = "update tb_onl_txn set AUTOLOAD_LMS_NO = ? where card_no = ? and expiry_date = ? and lms_invoice_no = ? ";
        Vector<String> parms = new Vector<String>();
        parms.add(txnInfo.getLmsInvoiceNo());//auto reload txn lms invoice no
        parms.add(txnInfo.getCardNo());
        parms.add(txnInfo.getExpiryDate());
        parms.add(txnInfo.getOrigLmsInvoiceNo());//orig purch txn lms invoice no
        txnInfo.setOrigLmsInvoiceNo(null);
        DbUtil.sqlAction(sqlCmd, parms, conn);
    }
	
	public void insertTxn(TbOnlTxnInfo txnInfo, TbOnlTxnDtlInfo txnDtlInfo) throws SQLException
    {
	    TbOnlTxnMgr txnMgr = new TbOnlTxnMgr(conn);
	    TbOnlTxnDtlMgr txnDtkMgr = new TbOnlTxnDtlMgr(conn);
	    txnMgr.insert(txnInfo);
	    txnDtkMgr.insert(txnDtlInfo);
    }
	
	public TbOnlTxnInfo genOnlTxn(Vector record, String hostDate, String hostTime)
	{
	    int idx = 0;
	    TbOnlTxnInfo txnInfo = new TbOnlTxnInfo();
	    txnInfo.setPCode(P_CODE);
	    txnInfo.setCardNo((String)record.get(idx++));
	    txnInfo.setExpiryDate((String)record.get(idx++));
	    txnInfo.setOrigLmsInvoiceNo((String)record.get(idx++));
	    txnInfo.setTxnDate((String)record.get(idx++));
	    txnInfo.setTxnTime((String)record.get(idx++));
	    txnInfo.setTxnAmt((Number)record.get(idx++));
	    txnInfo.setAtc((String)record.get(idx++));
	    txnInfo.setAcqMemId((String)record.get(idx++));
	    txnInfo.setIssMemId((String)record.get(idx++));
	    txnInfo.setMerchId((String)record.get(idx++));
	    txnInfo.setTermId((String)record.get(idx++));
	    txnInfo.setBatchNo((String)record.get(idx++));
	    txnInfo.setChipPoint1After((Number)record.get(idx++));
	    txnInfo.setChipPoint1Cr(txnInfo.getTxnAmt());
	    
	    double beforeBal = txnInfo.getChipPoint1After().doubleValue() - txnInfo.getTxnAmt().doubleValue();
	    
	    txnInfo.setChipPoint1Before(beforeBal);
	    txnInfo.setTermDate(txnInfo.getTxnDate());
	    txnInfo.setTermTime(txnInfo.getTxnTime());
	    txnInfo.setImpFileDate(hostDate);
	    txnInfo.setImpFileTime(hostTime);
	    txnInfo.setImpFileName("GenAutoRld:" + txnInfo.getOrigLmsInvoiceNo());
	    txnInfo.setTermSettleDate(hostDate);
	    txnInfo.setTermSettleTime("000000");
	    txnInfo.setTxnSrc("B");
	    txnInfo.setOnlineFlag("F");
	    txnInfo.setTxnAccessMode("L");
	    txnInfo.setParMon(hostDate.substring(4,6));//for partition {01~12}
	    txnInfo.setParDay(hostDate.substring(6,8));//for partition {01~31}
	    txnInfo.setAdviceFlag("00");
	    txnInfo.setStatus("1");
	    return txnInfo;
	}
	
	public TbOnlTxnDtlInfo genOnlTxnDtl(TbOnlTxnInfo txnInfo, String hostDate, String hostTime)
    {
        TbOnlTxnDtlInfo txnDtlInfo = new TbOnlTxnDtlInfo();
        txnDtlInfo.setCardNo(txnInfo.getCardNo());
        txnDtlInfo.setExpiryDate(txnInfo.getExpiryDate());
        txnDtlInfo.setBalanceId(txnInfo.getCardNo());
        txnDtlInfo.setBonusQty(txnInfo.getTxnAmt());
        txnDtlInfo.setBonusQty(txnInfo.getTxnAmt());
        txnDtlInfo.setBonusSdate(BONUS_SDATE);
        txnDtlInfo.setBonusEdate(BONUS_EDATE);
        txnDtlInfo.setPCode(P_CODE);
        txnDtlInfo.setTxnCode(TXN_CODE);
        txnDtlInfo.setBonusBase("C");
        txnDtlInfo.setBalanceType("C");
        txnDtlInfo.setRegionId("TWN");
        txnDtlInfo.setIsDw("1");
        txnDtlInfo.setBonusBeforeQty(txnInfo.getChipPoint1Before());
        txnDtlInfo.setBonusAfterQty(txnInfo.getChipPoint1After());
        txnDtlInfo.setBonusCrQty(txnInfo.getTxnAmt());
        txnDtlInfo.setBonusDbQty(0);
        txnDtlInfo.setParMon(hostDate.substring(4,6));//for partition {01~12}
        txnDtlInfo.setParDay(hostDate.substring(6,8));//for partition {01~31}
        return txnDtlInfo;
    }
	
   public String getBonusId(String cardNo) throws SQLException
    {
        String sqlCmd ="select a.ECASH_BONUS_ID from  tb_card_product a, tb_card b WHERE a.card_product = b.card_product and b.card_no = ?";
        Vector<String> params = new Vector<String>();
        params.add(cardNo);
        return DbUtil.getString(sqlCmd, params, conn); 
    }
	   
	public String genLmsInvoiceNo()
	{
	    String lmsInvoiceNo = "";
	    return lmsInvoiceNo;
	}
	
	private void remarkFail() throws SQLException {
		try {
			conn.rollback();
			log.info("rollback!");
		} catch (SQLException e) {
			throw new SQLException("remarkFail() rollback failed." + e);
		}
	}

	public String getBatchDate() {
		return batchDate;
	}

	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}

	public String getSysDate() {
		return sysDate;
	}

	public void setSysDate(String sysDate) {
		this.sysDate = sysDate;
	}

	public String getSysTime() {
		return sysTime;
	}

	public void setSysTime(String sysTime) {
		this.sysTime = sysTime;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
}

