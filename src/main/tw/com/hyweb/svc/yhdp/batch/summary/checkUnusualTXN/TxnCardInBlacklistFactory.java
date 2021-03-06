package tw.com.hyweb.svc.yhdp.batch.summary.checkUnusualTXN;

import java.util.List;
import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

public class TxnCardInBlacklistFactory extends CursorBatchJobFactory {
	private int seqNo = 0;
	
	private final String[] pcodeList;

    public TxnCardInBlacklistFactory(String[] pcodeList)
    {
        this.pcodeList = pcodeList;
    }
	
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
    			"FROM TB_TRANS A, TB_CARD B,  (SELECT CARD_NO, REG_DATE, REG_TIME FROM TB_BLACKLIST_SETTING WHERE STATUS = '1' ) C " +
    			"WHERE A.CUT_DATE = '"+batchDate+"' " +
    			"AND ( A.TERM_DATE > C.REG_DATE OR ( A.TERM_DATE = C.REG_DATE AND A.TERM_TIME > C.REG_TIME )) " +
    			"AND A.CARD_NO=B.CARD_NO " +
    			"AND B.CARD_NO=C.CARD_NO " +
    			"AND A.P_CODE IN " + getPcodeList(pcodeList);

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
        return new TxnCardInBlacklist(result,seqNo);
    }
    
    private String getPcodeList(String[] pcodeList)
    {
        StringBuilder condition = new StringBuilder();

        condition.append('(');

        for (int i = 0; i < pcodeList.length; ++i)
        {
        	
        	condition.append("'").append(pcodeList[i]).append("'");

            if (i != pcodeList.length - 1)
            {
                condition.append(", ");
            }
        }

        condition.append(')');

        return condition.toString();
    }
}
