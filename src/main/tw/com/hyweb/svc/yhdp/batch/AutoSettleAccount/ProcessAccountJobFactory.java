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
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;




/**
 * @author Chris
 *
 */
public class ProcessAccountJobFactory extends DAOBatchJobFactory
{

	private static Logger LOGGER = Logger.getLogger(ProcessAccountJobFactory.class);
	private String  OnlTxnCondSql = null;
	private static String batchTime = DateUtil.getTodayString().substring(8, 14);
    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getBatchJob(java.lang.Object)
     */
    @Override
    protected BatchJob getBatchJob(Object info) throws Exception
    {
        return new ProcessAutoSettlementJob((TransferOlnTxn) info, OnlTxnCondSql);
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
    	LOGGER.debug("batchTime: "+ batchTime);
    	String autoSettleDay = this.getAutoSettleDay( connection, batchDate, batchTime );
		List<TransferOlnTxn> transferOlnTxns = new ArrayList<TransferOlnTxn>();
		Statement stmt = null;
		ResultSet rs = null;
		
		StringBuffer jobWhereSql = getJobWhereSql(tbBatchResultInfo);
		StringBuffer sql = new StringBuffer();
		
		sql.append("Select merch_id, term_id, batch_no, count(*) AS CNT , sum(txn_amt) AS SUM_AMT from TB_ONL_TXN ");
		sql.append("where ");
		sql.append(getOnlTxnCond(autoSettleDay));

		if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS (SELECT 1 FROM TB_MEMBER");
        	sql.append(" WHERE TB_ONL_TXN.ACQ_MEM_ID = TB_MEMBER.MEM_ID");
        	sql.append(jobWhereSql.toString());
        	sql.append(" )");
        }
		
		sql.append("Group by merch_id, term_id, batch_no");
		LOGGER.info("transferOlnTxnsSql : " + sql.toString());
		try{
	        stmt = connection.createStatement();
	        rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()){
				TransferOlnTxn  transferOlnTxn = new TransferOlnTxn();
				transferOlnTxn.setMerchId(rs.getString(1));
				transferOlnTxn.setTermId(rs.getString(2));
				transferOlnTxn.setBatchNo(rs.getString(3));
				transferOlnTxn.setCount(rs.getString(4));
				transferOlnTxn.setSumTxnAmt(rs.getString(5));
				transferOlnTxn.setTermSettleDate(batchDate);
				transferOlnTxn.setTermSettleTime(batchTime);
				transferOlnTxns.add(transferOlnTxn);
			}

			ReleaseResource.releaseDB(null, stmt, rs);

		}
		catch(SQLException se) {
			throw se;
		}
		
		return transferOlnTxns;
    }
    
    
	private StringBuffer getJobWhereSql(TbBatchResultInfo tbBatchResultInfo) {
		// TODO Auto-generated method stub
		StringBuffer jobWhereSql = new StringBuffer();
        
        if (null != tbBatchResultInfo){
	    	
        	if (Layer1Constants.MEM_LAST.equalsIgnoreCase(tbBatchResultInfo.getMemId())){
        		jobWhereSql.append(" AND JOB_ID IS NULL");
        		jobWhereSql.append(" AND JOB_TIME IS NULL");
    		}
    		else{
	        	if(!StringUtil.isEmpty(tbBatchResultInfo.getJobId()) 
				&& !tbBatchResultInfo.getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
		    		jobWhereSql.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobId()));
		    		if(!StringUtil.isEmpty(tbBatchResultInfo.getJobTime()) 
					&& !tbBatchResultInfo.getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
		    			jobWhereSql.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobTime()));
			    	}
		    	}
		    	if(!StringUtil.isEmpty(tbBatchResultInfo.getMemId())){
		    		jobWhereSql.append(" AND MEM_ID=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getMemId()));
		    	}
    		}
    	}
    	else{
    		LOGGER.warn("tbBatchResultInfo is null.");
    	}
		
		return jobWhereSql;
	}

	public String getOnlTxnCond( String autoSettleDay){
		
		
		StringBuffer setter = new StringBuffer();
		setter.append("txn_date <= '");
		setter.append(autoSettleDay.substring(0, 8));
		setter.append("' and exists (");
		setter.append(" select * from tb_term");
		setter.append(" where auto_settle_flag='1'");
		setter.append(" and tb_onl_txn.merch_id = tb_term.merch_id");
		setter.append(" and tb_onl_txn.term_id = tb_term.term_id )");
		setter.append(" and term_settle_date is null");
		setter.append(" and status in ('1','C','R')");

		OnlTxnCondSql = setter.toString();
		
		return setter.toString();
	}

	private  String getAutoSettleDay( Connection connection, String batchDate, String batchTime ){
		
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
	
	private  int getBatchConfig( Connection connection ) throws SQLException {
		
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
