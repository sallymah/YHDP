package tw.com.hyweb.svc.yhdp.batch.AutoSettleAccount;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.jpos.iso.ISODate;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;

/**
 * @author Chris
 *
 */
public class ProcessAutoSettleJobFactory extends DAOBatchJobFactory
{
	private static Logger LOGGER = Logger.getLogger(ProcessAutoSettleJobFactory.class);
	private String  onlTxnCondSql = null;
	
    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getBatchJob(java.lang.Object)
     */
    @Override
    protected BatchJob getBatchJob(Object info) throws Exception
    {
        return new ProcessAutoSettlementJob((TransferOlnTxn) info, onlTxnCondSql);
    }

    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getDAOInfos(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    protected List<?> getDAOInfos(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	String batchTime = DateUtil.getTodayString().substring(8, 14);
    	String autoSettleDay = this.getAutoSettleDay( connection, batchDate, batchTime );
		List<TransferOlnTxn> transferOlnTxns = new ArrayList<TransferOlnTxn>();
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		sql.append("Select merch_id, term_id, batch_no, count(*) AS CNT , sum(txn_amt) AS SUM_AMT from TB_ONL_TXN ");
		sql.append("where ");
		sql.append(getOnlTxnCond(autoSettleDay));
		sql.append("Group by merch_id, term_id, batch_no");
		LOGGER.info("transferOlnTxnsSql : " + sql.toString());
		try{
	        stmt = connection.createStatement();
	        rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()){
				TransferOlnTxn transferOlnTxn = new TransferOlnTxn(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)) ;
				transferOlnTxns.add(transferOlnTxn);
			}

			ReleaseResource.releaseDB(null, stmt, rs);

		}
		catch(SQLException se) {
			throw se;
		}

		
		LOGGER.debug("transferOlnTxns : " + transferOlnTxns.size());
		return transferOlnTxns;
    }
    
    
	public String getOnlTxnCond(String autoSettleDay){
				
		StringBuffer setter = new StringBuffer();
		setter.append("txn_date <= '");
		setter.append(autoSettleDay.substring(0, 8));
		setter.append("' and exists( ");
		setter.append(" select * from tb_term ");
		setter.append("where auto_settle_flag='1' ");
		setter.append("and tb_onl_txn.merch_id = tb_term.merch_id ");
		setter.append("and tb_onl_txn.term_id = tb_term.term_id ) ");
		setter.append("and term_settle_date is null ");
		setter.append("and status in ('1','C','R') ");

		onlTxnCondSql = setter.toString();
		
		return setter.toString();
	}

	private String getAutoSettleDay(Connection connection, String batchDate, String batchTime){
		
		Calendar cal = Calendar.getInstance();
		String autoSettleDay = batchDate + batchTime;
		int year=Integer.parseInt(batchDate.substring(0,4));
		int month=Integer.parseInt(batchDate.substring(4,6));
		int day=Integer.parseInt(batchDate.substring(6,8));
		int cutDay = 0;
		
		try {
			cutDay = getBatchConfig( connection );
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		cal.set(year, month-1, day-cutDay);
		LOGGER.debug("AutoSettleDay : " + ISODate.formatDate(cal.getTime(), "yyyyMMdd"));
		autoSettleDay = ISODate.formatDate(cal.getTime(), "yyyyMMdd") + batchTime ;
		
		return autoSettleDay;

	}
	
	private int getBatchConfig( Connection connection ) throws SQLException {
		
		int cutTime = 0;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT VALUE FROM TB_SYS_CONFIG WHERE PARM='AUTO_SETTLE_DAY_E'");
			while (rs.next())
			{
				cutTime = Integer.parseInt(rs.getString(1));
			}
		}
		finally
		{
			ReleaseResource.releaseDB(null, stmt, rs);
			
		}
		return cutTime;
	}
}
