package tw.com.hyweb.svc.yhdp.batch.impdata.procPrepaidCharge;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.string.StringUtil;

public class ProcPrepaidChargeJobFactory extends CursorBatchJobFactory
{
	protected final static Logger logger = Logger.getLogger(ProcPrepaidChargeJobFactory.class);
	
	private List<String> consPcodes = null;
    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
     */
    @Override
    protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	String consPcodeList = "";
    	for( int i = 0; i< consPcodes.size(); i++){
    		String pcode = consPcodes.get(i);
    		consPcodeList = consPcodeList + StringUtil.toSqlValueWithSQuote(pcode);
    		if ( i < consPcodes.size()-1 ){
    			consPcodeList = consPcodeList+", ";
    		}
    	}
    	
    	String jobWhereSql = getJobWhereSql(tbBatchResultInfo);
    	
    	StringBuffer sql = new StringBuffer();
    	sql.append(" SELECT TN.ACQ_MEM_ID, TN.ISS_MEM_ID, TN.P_CODE AS P_CODE, TN.TXN_CODE AS TXN_CODE, CD.CARD_NO AS CARD_NO,");
    	sql.append(" CD.EXPIRY_DATE AS EXPIRY_DATE, TN.LMS_INVOICE_NO AS LMS_INVOICE_NO, TXN_DATE,");
    	sql.append(" TN.BONUS_BEFORE_QTY AS BONUS_BEFORE_QTY, TN.BONUS_QTY AS BONUS_QTY, TN.BONUS_AFTER_QTY AS BONUS_AFTER_QTY");
    	sql.append(" FROM");
    	sql.append(" (SELECT TRANS.ACQ_MEM_ID, TRANS.ISS_MEM_ID, DTL.CARD_NO, DTL.EXPIRY_DATE, DTL.LMS_INVOICE_NO, TXN_DATE, ");
    	sql.append(" DTL.P_CODE, DTL.TXN_CODE, DTL.BONUS_ID, DTL.BONUS_BEFORE_QTY, DTL.BONUS_AFTER_QTY, DTL.BONUS_QTY");
    	sql.append(" FROM TB_TRANS TRANS,TB_TRANS_DTL DTL");
    	sql.append(" WHERE DTL.CUT_DATE = ").append(StringUtil.toSqlValueWithSQuote( batchDate ));
    	sql.append(" AND TRANS.CARD_NO = DTL.CARD_NO");
    	sql.append(" AND TRANS.EXPIRY_DATE = DTL.EXPIRY_DATE");
    	sql.append(" AND TRANS.LMS_INVOICE_NO = DTL.LMS_INVOICE_NO");
    	sql.append(" AND ((DTL.P_CODE IN (").append(consPcodeList).append(") AND DTL.TXN_CODE IN (SELECT TXN_CODE FROM TB_TXN_DEF WHERE SIGN = 'M') AND DTL.BONUS_AFTER_QTY<0)");
    	sql.append(" OR (DTL.TXN_CODE IN (SELECT TXN_CODE FROM TB_TXN_DEF WHERE BAL_FLAG = '1' AND SIGN = 'P') AND DTL.BONUS_BEFORE_QTY<0 ))) TN,");
    	sql.append(" (SELECT CARD_NO, EXPIRY_DATE, CARD_PRODUCT FROM TB_CARD) CD,");
    	sql.append(" (SELECT CARD_PRODUCT, ECASH_BONUS_ID FROM TB_CARD_PRODUCT) CP");
    	sql.append(" WHERE TN.CARD_NO = CD.CARD_NO");
    	sql.append(" AND TN.EXPIRY_DATE = CD.EXPIRY_DATE");
    	sql.append(" AND CD.CARD_PRODUCT = CP.CARD_PRODUCT");
    	sql.append(" AND CP.ECASH_BONUS_ID = TN.BONUS_ID");
    	
    	if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS (");
        	sql.append(" SELECT 1 FROM TB_MEMBER");
        	sql.append(" WHERE TB_MEMBER.MEM_ID = TN.ACQ_MEM_ID");
        	sql.append(jobWhereSql);
        	sql.append(" )");
        }
    	
    	
    	sql.append(" ORDER BY TN.CARD_NO, TN.EXPIRY_DATE, TN.LMS_INVOICE_NO");
    	
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
        return new ProcPrepaidChargeJob(result, consPcodes);
    }

	public List getConsPcodes() {
		return consPcodes;
	}
	public void setConsPcodes(List consPcodes) {
		this.consPcodes = consPcodes;
	}
}
