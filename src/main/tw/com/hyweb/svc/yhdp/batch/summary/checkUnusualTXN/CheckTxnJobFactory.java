package tw.com.hyweb.svc.yhdp.batch.summary.checkUnusualTXN;

import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

public class CheckTxnJobFactory extends CursorBatchJobFactory {
	private int seqNo = 0;
    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	String sql = "SELECT A.CARD_NO,A.LMS_INVOICE_NO ,A.ACQ_MEM_ID,A.MERCH_ID,A.TERM_SETTLE_DATE, " +
    			"A.TERM_ID,A.BATCH_NO,A.TERM_DATE,A.TERM_TIME,A.TXN_AMT,A.P_CODE,A.TXN_SRC, A.ATC, " +
    			"B.CO_BRAND_ENT_ID, B.CARD_TYPE_ID, B.CARD_CAT_ID " +
    			"FROM TB_TRANS A, TB_CARD B " +
    			"WHERE A.CUT_DATE = '"+batchDate+"' " +
//    			"AND A.TXN_SRC='E' " +
    			"AND A.CARD_NO=B.CARD_NO " +
    			"AND A.ATC IS NOT NULL " +
    			"AND A.P_CODE in (SELECT P_CODE FROM TB_P_CODE_DEF WHERE ATC_FLAG='1')";

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
        return new CheckTxnJob(result,seqNo);
    }


}
