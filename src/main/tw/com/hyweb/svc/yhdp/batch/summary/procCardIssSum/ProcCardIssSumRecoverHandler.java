package tw.com.hyweb.svc.yhdp.batch.summary.procCardIssSum;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.RecoverHandler;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

public class ProcCardIssSumRecoverHandler implements RecoverHandler
{
	private final String[] tables;
	
	public ProcCardIssSumRecoverHandler(String[] tables)
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
