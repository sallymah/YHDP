package tw.com.hyweb.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.core.yhdp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.info.TbSysConfigInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.info.TbTransPK;
import tw.com.hyweb.service.db.mgr.TbSysConfigMgr;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.service.db.mgr.TbTransDtlMgr;
import tw.com.hyweb.service.db.mgr.TbTransMgr;
import tw.com.hyweb.svc.yhdp.batch.expfiles.beans.ExpDataInfo;

public class BankUtil {
	
	private final static Logger logger = Logger.getLogger(BatchUtils.class); 
	private static Connection conn;
	private static HashMap<String, TbSysConfigInfo> sysConfigInfos = new HashMap<String, TbSysConfigInfo>();
	
	private static String sysDate="";
	private static String sysTime="";
	private static String parMon="";
	private static String parDay="";
	
	public BankUtil() {
	}
	
	static {
		sysDate = DateUtils.getSystemDate();
		sysTime = DateUtils.getSystemTime();
		parMon = sysDate.substring(4,6);
		parDay = sysDate.substring(6,8);
		
		conn = BatchUtil.getConnection();
		
		TbSysConfigMgr mgr = new TbSysConfigMgr(conn);
        Vector<TbSysConfigInfo> results = new Vector<TbSysConfigInfo>();
        try {
			mgr.queryAll(results, "PARM");
			for (TbSysConfigInfo sysConfigInfo : results) {
	            sysConfigInfos.put(sysConfigInfo.getParm(), sysConfigInfo);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}    
	}
	
	public static void simulateTrans(Connection conn, ExpDataInfo dataInfo, String batchDate, String pCode, String txnCode) throws Exception
	{		
		String origLmsInvoiceNo = dataInfo.getLmsInvoiceNo();
		
		TbTransInfo newTransInfo = addTrans(conn, dataInfo, batchDate, pCode);
		addTransDtl(conn, newTransInfo, origLmsInvoiceNo, pCode, txnCode);
		addTermBatch(conn, newTransInfo);		
	}
	
	private static void addTermBatch(Connection conn, TbTransInfo transInfo) throws SQLException 
	{			
		TbTermBatchInfo termBatchInfo = new TbTermBatchInfo();
		termBatchInfo.setBatchNo(transInfo.getBatchNo());
		termBatchInfo.setTermSettleDate(sysDate);
		termBatchInfo.setTermSettleTime(sysTime);
		termBatchInfo.setTermSettleFlag("1");
		termBatchInfo.setStatus("1");
		termBatchInfo.setTxnSrc(transInfo.getTxnSrc());
		termBatchInfo.setMerchId(transInfo.getMerchId());
		termBatchInfo.setTermId(transInfo.getTermId());
		termBatchInfo.setTermUpDate(sysDate);
		termBatchInfo.setCutDate(sysDate);
		termBatchInfo.setCutTime(sysTime);
		termBatchInfo.setParMon(sysDate.substring(4,6));
		termBatchInfo.setParDay(sysDate.substring(6,8));
		
		new TbTermBatchMgr(conn).insert(termBatchInfo);		
	}
	
	private static TbTransInfo addTrans(Connection conn, ExpDataInfo dataInfo, String batchDate, String pCode) throws Exception 
	{	
		String newBatchNo = SequenceGenerator.getBatchNoByType(conn, "R");
		String newLmsInvoiceNo = SequenceGenerator.getLmsInvoiceNo(conn, batchDate);
		
		TbTransPK pk = new TbTransPK();
		pk.setCardNo(dataInfo.getCardNo());
		pk.setExpiryDate(dataInfo.getExpiryDate());
		pk.setLmsInvoiceNo(dataInfo.getLmsInvoiceNo());
		
		TbTransInfo origTransInfo =  new TbTransMgr(conn).querySingle(pk);
		if(origTransInfo == null) throw new Exception("original trans("+ pk.toString() +") is not exist !!");
		
		TbTransInfo newTransInfo = new TbTransInfo();
		
		newTransInfo.setTxnSrc("B");
		newTransInfo.setOnlineFlag("O");
		newTransInfo.setAcqMemId(origTransInfo.getAcqMemId());
		newTransInfo.setIssMemId(origTransInfo.getIssMemId());
		newTransInfo.setMerchId(origTransInfo.getMerchId());
		newTransInfo.setTermId(origTransInfo.getTermId());
		newTransInfo.setBatchNo(newBatchNo);
		newTransInfo.setStatus(origTransInfo.getStatus());
		newTransInfo.setTxnAmt(origTransInfo.getTxnAmt());
		newTransInfo.setRespCode("00");
		newTransInfo.setTermSettleDate(sysDate);
		newTransInfo.setTermSettleTime(sysTime);
		newTransInfo.setCardNo(dataInfo.getCardNo());
		newTransInfo.setExpiryDate(dataInfo.getExpiryDate());
		newTransInfo.setLmsInvoiceNo(newLmsInvoiceNo);
		newTransInfo.setPCode(pCode);
		newTransInfo.setTxnDate(sysDate);
		newTransInfo.setTxnTime(sysTime);
		newTransInfo.setCutDate(sysDate);
		newTransInfo.setCutTime(sysTime);
		newTransInfo.setParMon(parMon);
		newTransInfo.setParDay(parDay);
		
		new TbTransMgr(conn).insert(newTransInfo);	
		
		return newTransInfo;
	}
	
	private static void addTransDtl(Connection conn, TbTransInfo transInfo, String origLmsInvoiceNo, String pCode, String txnCode) throws Exception 
	{
		Vector<TbTransDtlInfo> result = new Vector<TbTransDtlInfo>();
		TbTransDtlInfo info = new TbTransDtlInfo();
		info.setCardNo(transInfo.getCardNo());
		info.setExpiryDate(transInfo.getExpiryDate());
		info.setLmsInvoiceNo(origLmsInvoiceNo);
		
		new TbTransDtlMgr(conn).queryMultiple(info, result);
		if(result.size() == 0) throw new Exception("original transDtl("+ info.toString() +") is not exist !!");
		
		for(TbTransDtlInfo dtlInfo: result) 
		{
			TbTransDtlInfo newTransDtlInfo = new TbTransDtlInfo();
		
			newTransDtlInfo.setCardNo(transInfo.getCardNo());
			newTransDtlInfo.setExpiryDate(transInfo.getExpiryDate());
			newTransDtlInfo.setLmsInvoiceNo(transInfo.getLmsInvoiceNo());
			newTransDtlInfo.setRegionId(dtlInfo.getRegionId());
			newTransDtlInfo.setBonusBase(dtlInfo.getBonusBase());
			newTransDtlInfo.setBonusId(dtlInfo.getBonusId());
			newTransDtlInfo.setBonusSdate(dtlInfo.getBonusSdate());
			newTransDtlInfo.setBonusEdate(dtlInfo.getBonusEdate());
			newTransDtlInfo.setBonusQty(dtlInfo.getBonusQty());
			newTransDtlInfo.setBalanceId(dtlInfo.getBalanceId());
			newTransDtlInfo.setBalanceType(dtlInfo.getBalanceType());
			newTransDtlInfo.setPCode(pCode);
			newTransDtlInfo.setTxnCode(txnCode);
			newTransDtlInfo.setCutDate(sysDate);
			newTransDtlInfo.setParMon(parMon);
			newTransDtlInfo.setParDay(parDay);
			
			new TbTransDtlMgr(conn).insert(newTransDtlInfo);
		}	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
