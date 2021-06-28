package tw.com.hyweb.svc.yhdp.batch.summary.procPrepaidBal;

import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.string.StringUtil;

public class ProcPrepaidBalJobFactory extends CursorBatchJobFactory
{
	protected final static Logger logger = Logger.getLogger(ProcPrepaidBalJobFactory.class);
	
//	private List<String> consPcodes = null;
    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	/*String startDate = DateUtil.addMonth(batchDate, -1);
    	String endDate = DateUtil.addDate(batchDate, -1);
    	String consPcodeList = "";
    	for( int i = 0; i< consPcodes.size(); i++){
    		String pcode = consPcodes.get(i);
    		consPcodeList = consPcodeList + StringUtil.toSqlValueWithSQuote(pcode);
    		if ( i < consPcodes.size()-1 ){
    			consPcodeList = consPcodeList+", ";
    		}
    	}*/
    	
    	String jobWhereSql = getJobWhereSql(tbBatchResultInfo);
    	StringBuffer sql = new StringBuffer();
    	
    	
    	sql.append(" SELECT ACQ_MEM_ID,");
		sql.append(" SUM(CASE WHEN SIGN = 'P' THEN PREPAID_CHARGE ELSE 0 END) AS REV_PREPAID_QTY,");
		sql.append(" SUM(CASE WHEN SIGN = 'M' THEN PREPAID_CHARGE*-1 ELSE 0 END) AS INC_PREPAID_QTY");
		sql.append(" FROM TB_PREPAID_CHARGE, TB_TXN_DEF");
		sql.append(" WHERE TB_TXN_DEF.TXN_CODE = TB_PREPAID_CHARGE.TXN_CODE");
		sql.append(" AND IS_CHARGE = 'N'");
		sql.append(" AND PROC_DATE <= '"+ batchDate + "'");
		if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS (");
        	sql.append(" SELECT 1 FROM TB_MEMBER");
        	sql.append(" WHERE TB_MEMBER.MEM_ID = TB_PREPAID_CHARGE.ACQ_MEM_ID");
        	sql.append(jobWhereSql);
        	sql.append(" )");
        }
		sql.append(" GROUP BY ACQ_MEM_ID ");
    	
        return sql.toString();
    }

    protected String getJobWhereSql(TbBatchResultInfo tbBatchResultInfo) {
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
    		logger.warn("tbBatchResultInfo is null.");
    	}
        
		return jobWhereSql.toString();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#makeBatchJob(java.util.Map)
     */
    @Override
    protected BatchJob makeBatchJob(Map<String, String> result) throws Exception
    {
        return new ProcPrepaidBalJob(result);
    }

	/*public List<String> getConsPcodes() {
		return consPcodes;
	}
	public void setConsPcodes(List<String> consPcodes) {
		this.consPcodes = consPcodes;
	}*/
    
}
