package tw.com.hyweb.svc.yhdp.batch.summary.computeRmCard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.batch.util.beans.TermBatchBean;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.util.string.StringUtil;

public final class InsertTermBatchHandle
{
    private static final Logger LOGGER = Logger.getLogger(InsertTermBatchHandle.class);
    
    private Connection connection = null;
    private boolean isDayCut = false;
    
    //String : MERCH_ID + TERM_ID
    protected HashMap<String, TbTermBatchInfo> terminalBatchMap = new HashMap();

    InsertTermBatchHandle(Connection connection, boolean isDayCut)
    {
    	this.connection = connection;
    	this.isDayCut = isDayCut;
    }
    
    public TbTermBatchInfo getTermBatchInfo(String txnSrc, String batchNoType) throws Exception
    {
    	return getTermBatchInfo(txnSrc, batchNoType, "", "");
    }

    public TbTermBatchInfo getTermBatchInfo(String txnSrc, String batchNoType, String merchId) throws Exception
    {
    	return getTermBatchInfo(txnSrc, batchNoType, merchId, "");
    }
    
	/**
     * 先insert一筆termBatch
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#init
     *      (java.sql.Connection, java.lang.String)
     */
    public TbTermBatchInfo getTermBatchInfo(String txnSrc, String batchNoType, String merchId, String termId) throws Exception
    {
        if (StringUtil.isEmpty(termId)) {
            termId = Layer2Util.getBatchConfig("UNKNOWN_TERM");
        }
        if (StringUtil.isEmpty(merchId)) {
            merchId = Layer2Util.getBatchConfig("UNKNOWN_MERCH");
        }

    	if (terminalBatchMap.containsKey(merchId + termId))
    		return terminalBatchMap.get(merchId + termId);
    	else
    	{
        	TbTermBatchInfo info = getInsertTermBatch(txnSrc, batchNoType, isDayCut, merchId, termId);
        
        	terminalBatchMap.put(info.getMerchId() + info.getTermId() , info);
        	
        	return info;
    	}

    }

	private TbTermBatchInfo getInsertTermBatch(String txnSrc, String batchNoType, 
			boolean isDayCut, String merchId, String termId) throws SQLException {
		// TODO Auto-generated method stub
		TbTermBatchInfo terminalBatch = new TbTermBatchInfo();
        terminalBatch.setTxnSrc(txnSrc);
        terminalBatch.setBatchNo(SequenceGenerator.getBatchNoByType(connection,batchNoType));
        terminalBatch.setTermSettleDate(DateUtils.getSystemDate());
        terminalBatch.setTermSettleTime(DateUtils.getSystemTime());
        terminalBatch.setTermId(termId);
        terminalBatch.setMerchId(merchId);
        
        if (isDayCut)
        {
	        terminalBatch.setCutDate(DateUtils.getSystemDate());
	        terminalBatch.setCutTime(DateUtils.getSystemTime());
        }
        
        TermBatchBean bean = new TermBatchBean();
        bean.setTermBatchInfo(terminalBatch);
       
        DBService.getDBService().sqlAction(bean.getInsertSql(), connection, false);
        
        return  bean.getTermBatchInfo();
	}

}