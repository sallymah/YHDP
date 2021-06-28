package tw.com.hyweb.svc.yhdp.batch.summary.computeRmCard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.mgr.TbBonusIssDefMgr;
import tw.com.hyweb.util.string.StringUtil;

public class ComputeRmCardJobFactory extends CursorBatchJobFactory {

	private static final Logger logger = Logger.getLogger(ComputeRmCardJobFactory.class);
	
	private List<TbBonusIssDefInfo> tbBonusIssDefList;
	private InsertTermBatchHandle termBatchHandle;
	
	private int beforeDate = 0;
	private String lostCardBalDay = Layer2Util.getBatchConfig("LOSTCARDBALDAY");
	
	public void init(Connection conn, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {

		super.init(conn, batchDate, tbBatchResultInfo);
		
		if(hasNext()) {
			setTbBonusIssDefList(getTbBonusIssDefInfo(conn));
			logger.debug("tbBonusIssDefList: " + tbBonusIssDefList);
			
			setTermBatchHandle(new InsertTermBatchHandle(conn, true));
		}
	}
	
	/*public ComputeRmCardJobFactory() throws Exception {
		try {
			
			setTbBonusIssDefList(getTbBonusIssDefInfo());
			logger.debug("tbBonusIssDefList: " + tbBonusIssDefList);
			
		} catch (Exception e) {
			
			logger.error("getTbBonusIssDef Error.");
			throw new Exception(e.getMessage());
		}
	}*/
	
	private List<TbBonusIssDefInfo> getTbBonusIssDefInfo(Connection conn) throws SQLException
    {
    	Vector results = new Vector();
    	
    	TbBonusIssDefMgr mgr = new TbBonusIssDefMgr(conn);
    	mgr.queryMultiple("", results);
    	
    	return results;
    }
	
	@Override
	protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT TB_CARD.CARD_NO, TB_CARD.EXPIRY_DATE, TXN_DATE, P_CODE FROM TB_CARD, TB_TRANS");
		sql.append(" WHERE P_CODE IN ('9847','9757','9827')");
		sql.append(" AND TXN_DATE = ").append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-Integer.parseInt(lostCardBalDay))));
		sql.append(" AND TB_CARD.CARD_NO = TB_TRANS.CARD_NO");
		sql.append(" AND TB_CARD.EXPIRY_DATE = TB_TRANS.EXPIRY_DATE");
		sql.append(" AND EXISTS (SELECT 1 FROM TB_BLACKLIST_SETTING");
		sql.append(" WHERE TB_CARD.CARD_NO = TB_BLACKLIST_SETTING.CARD_NO");
		sql.append(" AND TB_CARD.EXPIRY_DATE = TB_BLACKLIST_SETTING.EXPIRY_DATE)");
		sql.append(" ORDER BY TB_CARD.CARD_NO, TB_CARD.EXPIRY_DATE, TXN_DATE, TXN_TIME");
		
		logger.debug(sql.toString());
		
		return sql.toString();
	}

	@Override
	protected BatchJob makeBatchJob(Map<String, String> resultMap) throws Exception {
		// TODO Auto-generated method stub
		return new ComputeRmCardJob(resultMap, getTbBonusIssDefList(), getTermBatchHandle(), getBeforeDate());
	}

	public List<TbBonusIssDefInfo> getTbBonusIssDefList() {
		return tbBonusIssDefList;
	}

	public void setTbBonusIssDefList(List<TbBonusIssDefInfo> tbBonusIssDefList) {
		this.tbBonusIssDefList = tbBonusIssDefList;
	}

	public int getBeforeDate() {
		return beforeDate;
	}

	public void setBeforeDate(int beforeDate) {
		this.beforeDate = beforeDate;
	}

	public InsertTermBatchHandle getTermBatchHandle() {
		return termBatchHandle;
	}

	public void setTermBatchHandle(InsertTermBatchHandle termBatchHandle) {
		this.termBatchHandle = termBatchHandle;
	}

	public String getLostCardBalDay() {
		return lostCardBalDay;
	}

	public void setLostCardBalDay(String lostCardBalDay) {
		this.lostCardBalDay = lostCardBalDay;
	}
	
}