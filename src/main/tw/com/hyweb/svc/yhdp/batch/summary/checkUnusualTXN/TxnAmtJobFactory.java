package tw.com.hyweb.svc.yhdp.batch.summary.checkUnusualTXN;

import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

public class TxnAmtJobFactory extends CursorBatchJobFactory {
	private int seqNo = 0;
	
    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	String sql = "SELECT ACQ_MEM_ID, SUM(TXN_AMT) AS ACQ_TXN_AMT, DAY_MAX_TXN_AMT FROM TB_TRANS,TB_MEMBER " +
    			"WHERE CUT_DATE = '"+batchDate+"' " +
    			"AND P_CODE = '7647' " +
    			"AND TB_TRANS.STATUS = '1' " +
    			"AND TB_TRANS.ACQ_MEM_ID = TB_MEMBER.MEM_ID " +
    			"AND DAY_MAX_TXN_AMT <> 0 " +
    			"GROUP BY ACQ_MEM_ID,DAY_MAX_TXN_AMT " +
    			"HAVING SUM(TXN_AMT) > DAY_MAX_TXN_AMT ";

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
        return new TxnAmtJob(result,seqNo);
    }


}
