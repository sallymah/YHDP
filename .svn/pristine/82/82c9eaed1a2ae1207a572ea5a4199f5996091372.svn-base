package tw.com.hyweb.svc.yhdp.batch.preoperation.RejectApploadExpiryModify;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory;
import tw.com.hyweb.service.db.info.TbAppointReloadInfo;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.mgr.TbAppointReloadMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class RejectApploadExpiryModifyJobFactory extends DAOBatchJobFactory {
	
	private static final Logger logger = Logger.getLogger(RejectApploadExpiryModifyJobFactory.class);
	
	private static final String sysTime = DateUtil.getTodayString().substring(8, 14);
	
	private String jobDate = "01"; 
	private int expiredMonth = 2;
	
	private String batchNo = "";
	
	/*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getDAOInfos
     * (java.sql.Connection, java.lang.String)
     */
    @Override
    protected List<?> getDAOInfos(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	Vector<TbAppointReloadInfo> result = new Vector<TbAppointReloadInfo>();
    	
    	if(Integer.valueOf(jobDate) == Integer.valueOf(batchDate.substring(6, 8))){
    		
    		String expiredEDate = DateUtil.addMonth(batchDate.substring(0, 6) + "01", expiredMonth*-1);
    		
    		StringBuffer where = new StringBuffer();
    		where.append(" STATUS ='0'");
    		where.append(" AND VALID_EDATE < ").append(StringUtil.toSqlValueWithSQuote(expiredEDate));
    		where.append(" AND REJECT_DATE IS NULL");
    		new TbAppointReloadMgr(connection).queryMultiple(where.toString(), result);
    		
    		if(result.size() > 0){
    			batchNo = "M" + batchDate.substring(2) 
    					+ StringUtils.rightPad(DbUtil.getString("SELECT SEQ_APPRLD_BATCH_NO.NEXTVAL FROM DUAL", connection), 5, '0');
    		}
		}
		else{
			logger.debug("BatchDate [" + batchDate + "] is not a working day.");
		}
        return result;
    }
	
    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getBatchJob
     * (java.lang.Object)
     */
    @Override
    protected BatchJob getBatchJob(Object info) throws Exception
    {
        return new RejectApploadExpiryModifyJob((TbAppointReloadInfo)info, sysTime, batchNo);
    }
    

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public int getExpiredMonth() {
		return expiredMonth;
	}

	public void setExpiredMonth(int expiredMonth) {
		this.expiredMonth = expiredMonth;
	}
}
