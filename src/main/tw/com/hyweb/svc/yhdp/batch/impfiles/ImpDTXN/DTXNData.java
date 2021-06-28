package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpDTXN;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCreditTxnInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;

public class DTXNData
{
    private static Logger log = Logger.getLogger(DTXNData.class);
    
    private String sysDate = DateUtils.getSystemDate();
    private String sysTime = DateUtils.getSystemTime();

    private Connection conn;
    private final Map<String, Object> fileData;
    private final String fullFileName;
    private Vector<TbMemberInfo> memRs = new Vector<TbMemberInfo>();
    
    private DTXNDataBean dataBean;
       
    public DTXNData(Connection connection, Map<String, Object> fileData, String fullFileName) throws Exception
    {
    	initail(fileData);
    	this.conn = connection;
    	this.fileData = fileData;
    	this.fullFileName = fullFileName;
    }
    
    private void initail(Map<String, Object> fileData) throws Exception 
    {
    	String expiryDate = DateUtils.getLastDayOfMonth("20"+(String)fileData.get("EXPIRY_DATE"));    	
    	dataBean = new DTXNDataBean();
    	
    	dataBean.setCardNo((String)fileData.get("CARD_NO"));
    	dataBean.setExpiryDate(expiryDate);
    	dataBean.setTxnMon((String)fileData.get("TXN_MON"));
    	dataBean.setTtlCnt(Integer.valueOf((String)fileData.get("TTL_CNT")));
    	dataBean.setTtlAmt((double)fileData.get("TTL_AMT"));
    	dataBean.setReasonCode((String)fileData.get("REASON_CODE"));	
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
    
    public int getMemInfoCnt(TbInctlInfo tbInctlInfo) throws Exception
    {
    	int count = 0;

    	memRs.clear();
    	String memWhere = "MEM_ID='" + tbInctlInfo.getMemId() + "'";
    	memWhere = memWhere + " and substr(MEM_TYPE,3,1)='1'";
    	count = new TbMemberMgr(conn).queryMultiple(memWhere, memRs);
    	
    	return count;
    }

	public Map<String, Object> getFileData() {
		return fileData;
	}

    public List handleDTXN(Connection conn, String batchDate) throws Exception 
    {
    	String memId = "";

    	if(memRs.size()!=0) {
    		memId = memRs.get(0).getMemId();
    	}
    	
    	TbCreditTxnInfo tbCreditTxnInfo = makeDTXNData(memId, batchDate);
    	    	
        List sqls = new ArrayList();
        log.info("sql: " + tbCreditTxnInfo.toInsertSQL());
        sqls.add(tbCreditTxnInfo.toInsertSQL());
        
        return sqls;
    }   
    
    private TbCreditTxnInfo makeDTXNData(String memId, String batchDate) throws Exception 
    {    	
    	TbCreditTxnInfo info = new TbCreditTxnInfo();
    	
    	info.setCardNo(dataBean.getCardNo());
    	info.setExpiryDate(dataBean.getExpiryDate());
    	info.setAcqMemId(memId);
    	info.setTxnMon(dataBean.getTxnMon());
    	info.setTtlCnt(dataBean.getTtlCnt());
    	info.setTtlAmt(dataBean.getTtlAmt());
    	info.setStatus("0");
    	info.setImpDate(sysDate);
    	info.setImpTime(sysTime);
    	info.setImpFileName(fullFileName);
    
        return info;
    }
}
