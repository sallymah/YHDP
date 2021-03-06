package tw.com.hyweb.svc.yhdp.batch.summary.procPrepaidBal;

import java.sql.Connection;
import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.svc.yhdp.batch.util.BatchUtils;

import tw.com.hyweb.service.db.info.TbPrepaidBalInfo;
import tw.com.hyweb.service.db.mgr.TbPrepaidBalMgr;

public class ProcPrepaidBalJob extends GenericBatchJob {
	private final Map<String, String> result ;

	public ProcPrepaidBalJob(Map<String, String> result)
    {
        this.result = result;
    }
	
	public void action(Connection connection, String batchDate) throws Exception
    {
		TbPrepaidBalMgr prepaidBalMgr = new TbPrepaidBalMgr(connection);
		prepaidBalMgr.insert(getInsertPrepaidBalSQL(batchDate));
    }

	private TbPrepaidBalInfo getInsertPrepaidBalSQL(String batchDate) throws Exception
    {
		double revPrepaidQty = Double.parseDouble(result.get("REV_PREPAID_QTY"));
		double incPrepaidQty = Double.parseDouble(result.get("INC_PREPAID_QTY"));
		double prepaidBal = BatchUtils.add(revPrepaidQty, incPrepaidQty);
		
		TbPrepaidBalInfo prepaidBalInfo = new TbPrepaidBalInfo();
		prepaidBalInfo.setAcqMemId(result.get("ACQ_MEM_ID"));
		prepaidBalInfo.setProcMon(DateUtil.addMonth(batchDate, -1).substring(0,6)); //每月一號處理上個月交易
		prepaidBalInfo.setRevPrepaidQty(revPrepaidQty);
		prepaidBalInfo.setIncPrepaidQty(incPrepaidQty);
		prepaidBalInfo.setPrepaidBal(prepaidBal);
		prepaidBalInfo.setProcDate(batchDate);
        
		return prepaidBalInfo;

    }
}
