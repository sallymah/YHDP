/*
 * $Id: SimulateAppointReloadFactory.java 2594 2010-01-18 11:20:20Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */

package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpRmCard;

import java.sql.SQLException;
import java.util.Map;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.preoperation.SimulateTransJobFactory;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.mgr.TbBonusIssDefMgr;
import tw.com.hyweb.util.string.StringUtil;

import java.sql.Connection;

/**
 * @author  Anny
 * @version $Revision: 2594 $
 */
public class SimulateAppointReloadFactory extends SimulateTransJobFactory{

    private static final Logger LOGGER = Logger.getLogger(SimulateAppointReloadFactory.class);
    
    private List<TbBonusIssDefInfo> tbBonusIssDefList;
    private String lostCardBalDay = "0";
    
    public SimulateAppointReloadFactory( ) throws Exception {
    	
		try {
			
			setLostCardBalDay(Layer2Util.getBatchConfig("LOSTCARDBALDAY"));
			LOGGER.info("lostCardBalDay: "+lostCardBalDay);
			
			setTbBonusIssDefList(getTbBonusIssDefInfo());
			LOGGER.debug("tbBonusIssDefList: " + tbBonusIssDefList);
			
			
			
		} catch (Exception e) {
			
			LOGGER.error("getTbBonusIssDef Error.");
			throw new Exception(e.getMessage());
		}
		
	}
    
    private List<TbBonusIssDefInfo> getTbBonusIssDefInfo( ) throws SQLException
    {
    	Connection connection = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    	StringBuffer sqlCmd = new StringBuffer("");
    	//sqlCmd.append("bonus_base = ");
    	
    	Vector results = new Vector();
    	
    	TbBonusIssDefMgr mgr = new TbBonusIssDefMgr(connection);
    	mgr.queryMultiple(sqlCmd.toString(), results);
    	
    	return results;
    }

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#getSQL(java.lang.String)
	 */
	@Override
	protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {
		
        StringBuffer sqlCmd = new StringBuffer("");
        
        sqlCmd.append("SELECT ");
        
        sqlCmd.append("BONUS_BASE, BALANCE_TYPE, BALANCE_ID, CARD_NO, EXPIRY_DATE, AR_SERNO, ACQ_MEM_ID, MERCH_ID, REGION_ID ");
        
        sqlCmd.append("FROM TB_APPOINT_RELOAD ");
        
        /*sqlCmd.append("WHERE BONUS_BASE = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(Constants.BONUSBASE_HOST));*/

        /*sqlCmd.append("WHERE VALID_SDATE <= ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        sqlCmd.append(" AND VALID_EDATE >= ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        sqlCmd.append(" AND STATUS= '0' ");*/
        
        sqlCmd.append(" WHERE STATUS= '0' ");
        
        //DW_TXN_DATE, DW_TXN_TIME, DW_LMS_INVOICE_NO
        sqlCmd.append("AND DW_TXN_DATE IS NULL ");
        sqlCmd.append("AND DW_TXN_TIME IS NULL ");
        sqlCmd.append("AND DW_LMS_INVOICE_NO IS NULL ");
	    
	    sqlCmd.append("AND BATCH_RCODE = ");
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
        
        
        sqlCmd.append(" AND (CARD_NO, EXPIRY_DATE) IN ( ");
        sqlCmd.append("SELECT TB_TRANS.CARD_NO, TB_TRANS.EXPIRY_DATE ");
        sqlCmd.append("FROM TB_TRANS, TB_BLACKLIST_SETTING ");
        
        sqlCmd.append("WHERE P_CODE IN ('9847','9757','9827') ");
        
        sqlCmd.append("AND TXN_DATE = ");	
        sqlCmd.append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-Integer.parseInt(getLostCardBalDay()))));
        
        sqlCmd.append(" AND TB_TRANS.CARD_NO = TB_BLACKLIST_SETTING.CARD_NO )");	
        
        LOGGER.info(sqlCmd.toString());
		return sqlCmd.toString();
	}
	

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#makeBatchJob(java.util.Map)
	 */
	@Override
	protected BatchJob makeBatchJob(Map<String, String> info) throws Exception {
		
		LOGGER.info(info);
		return new SimulateAppointReloadJob(termBatchHandle.getTermBatchInfo("B","P", info.get("MERCH_ID")),info, getTbBonusIssDefList());
	}

	public List<TbBonusIssDefInfo> getTbBonusIssDefList() {
		return tbBonusIssDefList;
	}

	public void setTbBonusIssDefList(List<TbBonusIssDefInfo> tbBonusIssDefList) {
		this.tbBonusIssDefList = tbBonusIssDefList;
	}

	public String getLostCardBalDay() {
		return lostCardBalDay;
	}

	public void setLostCardBalDay(String lostCardBalDay) {
		this.lostCardBalDay = lostCardBalDay;
	}
	
	
}
