/*
 * $Id: SimulateCardReturnFactory.java 2528 2010-01-08 10:02:01Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */

package tw.com.hyweb.svc.yhdp.batch.preoperation;

import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.preoperation.SimulateTransJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author  Anny
 * @version $Revision: 2528 $
 */
public class SimulateCardReturnFactory extends SimulateTransJobFactory{

    private static final Logger LOGGER = Logger.getLogger(SimulateCardReturnFactory.class);
    

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
	 */
	@Override
	protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {
		
        StringBuffer sqlCmd = new StringBuffer("");
        
        sqlCmd.append("select ");
        
        sqlCmd.append(" ACQ_MEM_ID, MERCH_ID, ISS_MEM_ID, CARD_NO, EXPIRY_DATE, TXN_DATE, TXN_TIME, RETURN_AMT, BAL_AMT, CARD_RETURN_FEE_AMT, (BAL_AMT - CARD_RETURN_FEE_AMT) as TXN_PRICE");
        
        sqlCmd.append(" from TB_CARD_RETURN where");
        
        sqlCmd.append(" batch_rcode = '0000' and proc_date = '00000000'");
        
        LOGGER.info(sqlCmd.toString());
		return sqlCmd.toString();
	}
	

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#makeBatchJob(java.util.Map)
	 */
	@Override
	protected BatchJob makeBatchJob(Map<String, String> info) throws Exception {
		
		LOGGER.info(info);
		return new SimulateCardReturnJob(termBatchHandle.getTermBatchInfo("U", "B", info.get("MERCH_ID")), info);
	}

}
