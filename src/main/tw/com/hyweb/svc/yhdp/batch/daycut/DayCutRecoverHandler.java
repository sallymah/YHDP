/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.RecoverHandler;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Clare
 * 
 */
public class DayCutRecoverHandler implements RecoverHandler
{
    private final String[] tables;

    public DayCutRecoverHandler(String[] tables)
    {
        this.tables = tables;
    }

    public void recover(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        for (String table : tables)
        {
            String sql1 = "update " + table + " set CUT_DATE=null,CUT_TIME=null,CUT_RCODE='" + Constants.RCODE_0000_OK 
            		+ "' where CUT_DATE='" + batchDate + "' and CUT_RCODE<>'" + Constants.RCODE_0000_OK + "'";
            
            
            StringBuffer sql = new StringBuffer();
            
            sql.append("UPDATE ").append(table);
            sql.append(" SET");
            sql.append(" CUT_DATE = NULL,");
            sql.append(" CUT_TIME = NULL,");
            sql.append(" CUT_RCODE = ").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
            sql.append(" WHERE CUT_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
            sql.append(" AND CUT_RCODE <> ").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
            
            if (null != tbBatchResultInfo){
            	StringBuffer jobWhereSql = new StringBuffer();
            	
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
	    	    		jobWhereSql.append(" AND TB_MEMBER.MEM_ID=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getMemId()));
	    	    	}
        		}
    	    	
    	    	if(jobWhereSql.length() > 0){
    	    		sql.append(" AND EXISTS(");
    	    		sql.append(" SELECT 1 FROM TB_MERCH, TB_MEMBER");
    	    		sql.append(" WHERE TB_MERCH.MEM_ID = TB_MEMBER.MEM_ID");
    	    		sql.append(" AND ").append(table).append(".MERCH_ID = TB_MERCH.MERCH_ID");
    	    		sql.append(jobWhereSql);
    	    		sql.append(")");    	    		
    	    	}
        	}
        	else{
        		throw new Exception("tbBatchResultInfo is null.");
        	}
            
            
            DBService.getDBService().sqlAction(sql.toString(), connection, false);
            
        }
    }
}
