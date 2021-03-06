package tw.com.hyweb.svc.yhdp.batch.summary.procCardIssSum;

import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.util.string.StringUtil;

public class ProcCardIssSumJobFactory extends CursorBatchJobFactory {

	private static final Logger logger = Logger.getLogger(ProcCardIssSumJobFactory.class);
	
	@Override
	protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT FEE_CONFIG_ID, FEE_CODE, ISS_MEM_ID, ACQ_MEM_ID, RESET_PER_YEAR, FAIL_CARD_FLAG, VALID_SDATE FROM TB_FEE_CARD_ISSUE_CFG ");
		sql.append("WHERE ").append(StringUtil.toSqlValueWithSQuote(batchDate)).append(" BETWEEN VALID_SDATE AND VALID_EDATE");
		
		return sql.toString();
	}

	@Override
	protected BatchJob makeBatchJob(Map<String, String> resultMap) throws Exception {
		// TODO Auto-generated method stub
		return new ProcCardIssSumJob(resultMap);
	}

}
