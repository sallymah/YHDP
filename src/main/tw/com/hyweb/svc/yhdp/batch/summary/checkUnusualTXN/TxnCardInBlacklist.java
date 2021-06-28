package tw.com.hyweb.svc.yhdp.batch.summary.checkUnusualTXN;

import java.sql.Connection;
import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbRiskInfoInfo;
import tw.com.hyweb.service.db.mgr.TbRiskInfoMgr;

public class TxnCardInBlacklist  extends GenericBatchJob {
	
	private final Map<String, String> result ;
	private int seqNo = 0;

	public TxnCardInBlacklist(Map<String, String> result,int seqNo)
    {
        this.result = result;
        this.seqNo = seqNo;
    }
	
	public void action(Connection connection, String batchDate) throws Exception
    {
		TbRiskInfoMgr riskInfoMgr = new TbRiskInfoMgr(connection);
		riskInfoMgr.insert(getInsertRiskInfoSQL(batchDate));
    }
	
	private TbRiskInfoInfo getInsertRiskInfoSQL(String batchDate) throws Exception
    {
        TbRiskInfoInfo tbRiskInfo = new TbRiskInfoInfo();
        tbRiskInfo.setAcqMemId(result.get("ACQ_MEM_ID"));
        tbRiskInfo.setMerchId(result.get("MERCH_ID"));
        tbRiskInfo.setTermSettleDate(result.get("TERM_SETTLE_DATE"));
        tbRiskInfo.setTermId(result.get("TERM_ID"));
        tbRiskInfo.setBatchNo(result.get("BATCH_NO"));
        tbRiskInfo.setTxnDate(result.get("TERM_DATE"));
        tbRiskInfo.setTxnTime(result.get("TERM_TIME"));
        tbRiskInfo.setLmsInvoiceNo(result.get("LMS_INVOICE_NO"));
        tbRiskInfo.setCoBrandEntId(result.get("CO_BRAND_ENT_ID"));
        tbRiskInfo.setCardTypeId(result.get("CARD_TYPE_ID"));
        tbRiskInfo.setCardCatId(result.get("CARD_CAT_ID"));
        tbRiskInfo.setCardNo(result.get("CARD_NO"));
        tbRiskInfo.setTxnAmt(Double.valueOf(result.get("TXN_AMT").toString()));
        tbRiskInfo.setPCode(result.get("P_CODE"));
        tbRiskInfo.setTxnSrc(result.get("TXN_SRC"));
        tbRiskInfo.setWarnCode("24");
        tbRiskInfo.setProcDate(batchDate);
        tbRiskInfo.setProcTime(DateUtils.getSystemTime());
        tbRiskInfo.setStatus("1");
        tbRiskInfo.setSeqno(seqNo);
        
		return tbRiskInfo;

    }
	
}