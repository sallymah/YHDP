package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBTCB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.info.TbAutoReloadRespInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.info.TbTransPK;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbMerchMgr;
import tw.com.hyweb.service.db.mgr.TbTransDtlMgr;
import tw.com.hyweb.service.db.mgr.TbTransMgr;

public class BTCBData
{
    private static Logger log = Logger.getLogger(BTCBData.class);
    
    private String sysDate = DateUtils.getSystemDate();
    private String sysTime = DateUtils.getSystemTime();

    private Connection conn;
    private final Map<String, Object> fileData;
    private final String fullFileName;
    
    private BTCBDataBean dataBean;

    public BTCBData(Connection connection, Map<String, Object> fileData, String fullFileName) throws Exception
    {
    	initail(fileData);
    	this.conn = connection;
    	this.fileData = fileData;
    	this.fullFileName = fullFileName;
    }
    
    private void initail(Map<String, Object> fileData) throws Exception 
    {
    	String expiryDate = DateUtils.getLastDayOfMonth("20"+(String)fileData.get("EXPIRY_DATE"));
    	
    	dataBean = new BTCBDataBean();

    	dataBean.setMemName((String)fileData.get("MEM_NAME"));
    	dataBean.setCardNo((String)fileData.get("CARD_NO"));
    	dataBean.setExpiryDate(expiryDate);
    	dataBean.setLmsInvoiceNo((String)fileData.get("LMS_INVOICE_NO"));
    	dataBean.setMerchLocName((String)fileData.get("MERCH_LOC_NAME"));
    	dataBean.setMerchId((String)fileData.get("MERCH_ID"));
    	dataBean.setPCode((String)fileData.get("P_CODE"));
    	dataBean.setReasonCode((String)fileData.get("REASON_CODE"));	
    	dataBean.setTxnAmt((Double)fileData.get("TXN_AMT"));
    	dataBean.setTxnDate((String)fileData.get("TXN_DATE"));
    	dataBean.setTxnTime((String)fileData.get("TXN_TIME"));
	}

	public int getCardInfoCnt() throws Exception
    {
    	int count = 0;	
    	Vector<TbCardInfo> cardRs = new Vector<TbCardInfo>();
    	
    	String cardWhere = "CARD_NO='" + dataBean.getCardNo() + "'";
    	cardWhere = cardWhere + " and EXPIRY_DATE='" + dataBean.getExpiryDate() + "'";
    	count = new TbCardMgr(conn).queryMultiple(cardWhere, cardRs);
    	
    	return count;
    }
    
    public int getMemberInfoCnt() throws SQLException
    {
    	int count = 0;  	
    	Vector<TbMemberInfo> memRs = new Vector<TbMemberInfo>();
    	
    	String memWhere = "EXISTS ( SELECT * FROM TB_MERCH WHERE MERCH_ID = '"+ dataBean.getMerchId() + "' AND TB_MEMBER.MEM_ID = TB_MERCH.MEM_ID )";
    	count = new TbMemberMgr(conn).queryMultiple(memWhere, memRs);
    	
    	return count;
    }
    
    public int getMerchInfoCnt() throws SQLException
    {
    	int count = 0;
    	
    	Vector<TbMerchInfo> merchRs = new Vector<TbMerchInfo>();
    	String merchWhere = "MERCH_ID='" + dataBean.getMerchId() + "'";
    	count = new TbMerchMgr(conn).queryMultiple(merchWhere, merchRs);
    	
    	return count;
    }

	public Map<String, Object> getFileData() {
		return fileData;
	}

    public List handleBTCB(Connection conn, String batchDate) throws Exception 
    { 	
    	TbAutoReloadRespInfo autoReloadRespInfo = makeBTCBData(batchDate);
    	    	
        List sqls = new ArrayList();
        log.info("sql: " + autoReloadRespInfo.toInsertSQL());
        sqls.add(autoReloadRespInfo.toInsertSQL());
        
        return sqls;
    }   
    
    public List modifyTrans(Connection conn, String batchDate) throws Exception 
    {   
    	String cbDate = "00000000";
    	
    	if(dataBean.getPCode().equals("5727") 
    			| dataBean.getPCode().equals("5747")) {
    		cbDate = sysDate;
    	}
    	
    	StringBuffer sb = new StringBuffer();
    	sb.append("Update TB_TRANS set");
    	sb.append(" RESP_IMP_DATE='").append(batchDate).append("'");
    	sb.append(" ,CB_REASON_CODE='").append(dataBean.getReasonCode()).append("'");
    	sb.append(" ,CB_DATE='").append(cbDate).append("'");
    	sb.append(" Where CARD_NO='").append(dataBean.getCardNo()).append("'");
    	sb.append(" and EXPIRY_DATE='").append(dataBean.getExpiryDate()).append("'");
    	sb.append(" and LMS_INVOICE_NO='").append(dataBean.getLmsInvoiceNo()).append("'");
    	
    	String sql = sb.toString();
    	    	
        List sqls = new ArrayList();
        log.info("sql: " + sql);
        sqls.add(sql);
        
        return sqls;
    }
    
    private String addTermBatch(TbTransInfo transInfo, String newBatchNo) throws Exception 
	{
		TbTermBatchInfo termBatchInfo = new TbTermBatchInfo();
		termBatchInfo.setBatchNo(newBatchNo);
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
		
		return termBatchInfo.toInsertSQL();	
	}
    
    public List addTrans(Connection conn, String batchDate, String pCode) throws Exception 
    {   
    	List sqls = new ArrayList();

    	TbTransPK pk = new TbTransPK();
		pk.setCardNo(dataBean.getCardNo());
		pk.setExpiryDate(dataBean.getExpiryDate());
		pk.setLmsInvoiceNo(dataBean.getLmsInvoiceNo());
		
		TbTransInfo origTransInfo =  new TbTransMgr(conn).querySingle(pk);
		if(origTransInfo == null) throw new Exception("original trans("+ pk.toString() +") is not exist !!");
		
		TbTransInfo newTransInfo = new TbTransInfo();
		
		String newBatchNo = SequenceGenerator.getBatchNoByType(conn, "R");
		String newLmsInvoiceNo = SequenceGenerator.getLmsInvoiceNo(conn, batchDate);
		
		newTransInfo.setTxnSrc("B");
		newTransInfo.setOnlineFlag("O");
		newTransInfo.setAcqMemId(origTransInfo.getAcqMemId());
		newTransInfo.setIssMemId(origTransInfo.getIssMemId());
		newTransInfo.setMerchId(origTransInfo.getMerchId());
		newTransInfo.setTermId(origTransInfo.getTermId());
		newTransInfo.setStatus(origTransInfo.getStatus());
		newTransInfo.setTxnAmt(origTransInfo.getTxnAmt());
		newTransInfo.setBatchNo(newBatchNo);
		newTransInfo.setRespCode("00");
		newTransInfo.setTermSettleDate(sysDate);
		newTransInfo.setTermSettleTime(sysTime);
		newTransInfo.setCardNo(dataBean.getCardNo());
		newTransInfo.setExpiryDate(dataBean.getExpiryDate());
		newTransInfo.setLmsInvoiceNo(newLmsInvoiceNo);
		newTransInfo.setPCode(pCode);
		newTransInfo.setTxnDate(sysDate);
		newTransInfo.setTxnTime(sysTime);
		newTransInfo.setCutDate(sysDate);
		newTransInfo.setCutTime(sysTime);
		newTransInfo.setParMon(sysDate.substring(4,6));
		newTransInfo.setParDay(sysDate.substring(6,8));
		
		String termBatchSql = addTermBatch(newTransInfo, newBatchNo);
    	log.info("term Batch Sql: " + termBatchSql);
        sqls.add(termBatchSql);
    	
    	String transSql = newTransInfo.toInsertSQL();	
    	log.info("trans Sql: " + transSql);
        sqls.add(transSql);
        
        List<String> transDtlList = addTransDtl(newTransInfo, newBatchNo, newLmsInvoiceNo, dataBean.getLmsInvoiceNo(), pCode);
    	log.info("trans Dtl List: " + transDtlList);
    	for(String transDtlSql: transDtlList)
    	{
    		sqls.add(transDtlSql);
    	}

        return sqls;
    }
    
    private List<String> addTransDtl(TbTransInfo transInfo, 
    		String newBatchNo,String newLmsInvoiceNo, String origLmsInvoiceNo,
    		String pCode) throws Exception 
	{
    	Vector<TbTransDtlInfo> result = new Vector<TbTransDtlInfo>();
		TbTransDtlInfo info = new TbTransDtlInfo();
		info.setCardNo(transInfo.getCardNo());
		info.setExpiryDate(transInfo.getExpiryDate());
		info.setLmsInvoiceNo(origLmsInvoiceNo);
		
		new TbTransDtlMgr(conn).queryMultiple(info, result);
		List<String> list = new ArrayList<String>();
		
		for(TbTransDtlInfo dtlInfo: result) 
		{
			TbTransDtlInfo newTransDtlInfo = new TbTransDtlInfo();
			
			newTransDtlInfo.setCardNo(transInfo.getCardNo());
			newTransDtlInfo.setExpiryDate(transInfo.getExpiryDate());
			newTransDtlInfo.setLmsInvoiceNo(newLmsInvoiceNo);
			newTransDtlInfo.setRegionId(dtlInfo.getRegionId());
			newTransDtlInfo.setBonusBase(dtlInfo.getBonusBase());
			newTransDtlInfo.setBonusId(dtlInfo.getBonusId());
			newTransDtlInfo.setBonusSdate(dtlInfo.getBonusSdate());
			newTransDtlInfo.setBonusEdate(dtlInfo.getBonusEdate());
			newTransDtlInfo.setBonusQty(dtlInfo.getBonusQty());
			newTransDtlInfo.setBalanceId(dtlInfo.getBalanceId());
			newTransDtlInfo.setBalanceType(dtlInfo.getBalanceType());
			newTransDtlInfo.setPCode(pCode);
			newTransDtlInfo.setTxnCode("8716");
			newTransDtlInfo.setCutDate(sysDate);
			newTransDtlInfo.setParMon(sysDate.substring(4,6));
			newTransDtlInfo.setParDay(sysDate.substring(6,8));
			
			list.add(newTransDtlInfo.toInsertSQL());
		}

		return list;	
	}
    
    private TbAutoReloadRespInfo makeBTCBData(String batchDate) throws Exception 
    {
    	Vector<TbMemberInfo> memRs = new Vector<TbMemberInfo>();
    	String memWhere = "EXISTS ( SELECT * FROM TB_MERCH WHERE MERCH_ID = '"+ dataBean.getMerchId() + "' AND TB_MEMBER.MEM_ID = TB_MERCH.MEM_ID )";
    	new TbMemberMgr(conn).queryMultiple(memWhere, memRs);
    	String memId = memRs.get(0).getMemId();
    	
    	Vector<TbMerchInfo> merchRs = new Vector<TbMerchInfo>();
    	String merchWhere = "MERCH_ID='" + dataBean.getMerchId() + "'";
    	new TbMerchMgr(conn).queryMultiple(merchWhere, merchRs);
    	String merchId = merchRs.get(0).getMerchId();
    	
    	TbAutoReloadRespInfo info = new TbAutoReloadRespInfo();
    	
    	info.setCardNo(dataBean.getCardNo());
    	info.setExpiryDate(dataBean.getExpiryDate());
    	info.setTxnDate(dataBean.getTxnDate());
    	info.setTxnTime(dataBean.getTxnTime());
    	info.setTxnAmt(dataBean.getTxnAmt());
    	info.setAcqMemId(memId);
    	info.setMerchId(merchId);
    	info.setLmsInvoiceNo(dataBean.getLmsInvoiceNo());
    	info.setPCode(dataBean.getPCode());
    	info.setCbReasonCode(dataBean.getReasonCode());
    	info.setStatus("0");
    	info.setImpDate(sysDate);
    	info.setImpTime(sysTime);
    	info.setImpFileName(fullFileName);
    
        return info;
    }
}
