package tw.com.hyweb.svc.yhdp.batch.summary.computeRmCard;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.RecoverHandler;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

public class ComputeRmCardRecoverHandler implements RecoverHandler
{
	private final String[] tables;
	
	public ComputeRmCardRecoverHandler(String[] tables)
	{
	    this.tables = tables;
	}
	
	public void recover(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
	{
	    for (String table : tables)
	    {
	        String sql = "DELETE " + table + " WHERE PROC_DATE = '" + batchDate + "'";
	        DBService.getDBService().sqlAction(sql, connection, false);
	    }
	}
}
