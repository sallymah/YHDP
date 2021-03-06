package tw.com.hyweb.svc.yhdp.batch.summary.computeCdrp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.mgr.TbBonusIssDefMgr;

public class ComputeCdrpJobFactory extends CursorBatchJobFactory {

	private static final Logger logger = Logger.getLogger(ComputeCdrpJobFactory.class);
	
	private List<TbBonusIssDefInfo> tbBonusIssDefList;
	private InsertTermBatchHandle termBatchHandle;
	
	private int beforeDate = 0;
	
	public void init(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {

		super.init(connection, batchDate, tbBatchResultInfo);
		
		if(hasNext()) {
			setTbBonusIssDefList(getTbBonusIssDefInfo(connection));
			logger.debug("tbBonusIssDefList: " + tbBonusIssDefList);
			
			setTermBatchHandle(new InsertTermBatchHandle(connection, true));
		}
	}
	/*
	public ComputeCdrpJobFactory() throws Exception {
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
		sql.append(" SELECT MEM_ID, BANK_ID, LOST_CARD_RESP_DAY, STOP_CARD_RESP_DAY, EXPIRY_CARD_RESP_DAY, GEN_RDCL_DAY");
		sql.append(" FROM TB_MEMBER");
		sql.append(" WHERE BANK_ID IS NOT NULL");
//		sql.append(" AND MEM_ID = '00100805'");
		
		logger.debug(sql.toString());
		
		return sql.toString();
	}

	@Override
	protected BatchJob makeBatchJob(Map<String, String> resultMap) throws Exception {
		// TODO Auto-generated method stub
		ComputeCdrpData computeCdrpData = new ComputeCdrpData(resultMap);
		
		return new ComputeCdrpJob(computeCdrpData, getTbBonusIssDefList(), getTermBatchHandle(), getBeforeDate());
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
}