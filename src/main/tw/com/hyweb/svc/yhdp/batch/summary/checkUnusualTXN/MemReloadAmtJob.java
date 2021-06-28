package tw.com.hyweb.svc.yhdp.batch.summary.checkUnusualTXN;

import java.sql.Connection;
import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbRiskInfoInfo;
import tw.com.hyweb.service.db.mgr.TbRiskInfoMgr;

public class MemReloadAmtJob extends GenericBatchJob {
	private final Map<String, String> result ;
	private int seqNo = 0;

	public MemReloadAmtJob(Map<String, String> result, int seqNo)
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
        tbRiskInfo.setCardNo(result.get("CARD_NO"));
        tbRiskInfo.setWarnCode("14");
        tbRiskInfo.setProcDate(batchDate);
        tbRiskInfo.setProcTime(DateUtils.getSystemTime());
        tbRiskInfo.setStatus("1");
        tbRiskInfo.setSeqno(seqNo);
        
		return tbRiskInfo;

    }
}
