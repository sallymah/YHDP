/*
 * $Id: SimulateAppointReloadFactory.java 2594 2010-01-18 11:20:20Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */

package tw.com.hyweb.core.cp.common.appointreload;

import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJobFactory;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author  Anny
 * @version $Revision: 2594 $
 */
public class SimulateAppointReloadFactory extends SimulateOnlineJobFactory{

    private static final Logger LOGGER = Logger.getLogger(SimulateAppointReloadFactory.class);
    private String othersCondition = "";
    

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
	 */
	@Override
	protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {
		
        StringBuffer sqlCmd = new StringBuffer("");
        
        sqlCmd.append("select ");
        
        sqlCmd.append(" BONUS_BASE, BALANCE_TYPE, BALANCE_ID, CARD_NO, EXPIRY_DATE, AR_SERNO, ACQ_MEM_ID, MERCH_ID, REGION_ID");
        
        sqlCmd.append(" from TB_APPOINT_RELOAD where");
        
        if (!StringUtil.isEmpty(othersCondition))
        {
        	sqlCmd.append(othersCondition);
        	sqlCmd.append(" AND ");
        }
        
        sqlCmd.append(" bonus_base = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(Constants.BONUSBASE_HOST));

        sqlCmd.append(" AND valid_sdate <= ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        sqlCmd.append(" AND valid_edate >= ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        sqlCmd.append(" AND status= '0' ");
        
	    sqlCmd.append(" AND proc_date is null ");
	    
	    sqlCmd.append(" AND batch_rcode = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
        
        
        LOGGER.info(sqlCmd.toString());
		return sqlCmd.toString();
	}
	

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#makeBatchJob(java.util.Map)
	 */
	@Override
	protected BatchJob makeBatchJob(Map<String, String> info) throws Exception {
		
		LOGGER.info(info);
		return new SimulateAppointReloadJob(termBatchHandle.getTermBatchInfo("P","P", info.get("MERCH_ID")),info);
	}

	public String getOthersCondition() {
		return othersCondition;
	}

	public void setOthersCondition(String othersCondition) {
		this.othersCondition = othersCondition;
	}
}
