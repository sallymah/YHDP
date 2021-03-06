package tw.com.hyweb.svc.yhdp.batch.summary.checkUnusualTXN;

import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

public class MemRefundAmtJobFactory extends CursorBatchJobFactory {
	private int seqNo = 0;
	/*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	String sql = "SELECT CARD_NO, SUM(TXN_AMT) ,MAX_REFUND_AMT FROM TB_TRANS,TB_MEMBER "+
    			"WHERE TB_TRANS.ACQ_MEM_ID = TB_MEMBER.MEM_ID "+
    			"AND CUT_DATE = '"+batchDate+"' "+
    			"AND P_CODE = '7646' " +
    			"AND MAX_REFUND_AMT <> 0 "+
    			"GROUP BY TB_TRANS.ACQ_MEM_ID,CARD_NO, MAX_REFUND_AMT "+
    			"HAVING SUM(TXN_AMT) > MAX_REFUND_AMT ";

        return sql.toUpperCase();
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#makeBatchJob(java.util.Map)
     */
    @Override
    protected BatchJob makeBatchJob(Map<String, String> result) throws Exception
    {
    	seqNo++;
        return new MemRefundAmtJob(result,seqNo);
    }
}
