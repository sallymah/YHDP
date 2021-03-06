package tw.com.hyweb.svc.yhdp.batch.ReCalculateBonus;

import java.util.Map;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;


public class ReCalculateBonusJobFactory extends CursorBatchJobFactory {

	@Override
	protected String getSQL(String arg0, TbBatchResultInfo arg1) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();
		
		sql.append("SELECT CARD_NO, EXPIRY_DATE FROM TB_CARD_BAL WHERE BONUS_ID = '1100000001' AND BONUS_SDATE = '00010101' AND BONUS_EDATE <> '99991231'");
		
		return sql.toString();
	}

	@Override
	protected BatchJob makeBatchJob(Map<String, String> resultMap) throws Exception {
		// TODO Auto-generated method stub
		return new ReCalculateBonusJob(resultMap);
	}
	
}
