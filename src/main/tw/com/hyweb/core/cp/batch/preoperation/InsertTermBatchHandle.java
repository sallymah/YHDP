/*
 * $Id: InsertTermBatchHandle.java 2526 2010-01-08 09:58:22Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.batch.preoperation;

import java.sql.Connection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Anny
 * @version $Revision: 2526 $
 */
public class InsertTermBatchHandle
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
        	InsertTermBatch termBatch = new InsertTermBatch(txnSrc, batchNoType, isDayCut);
        	TbTermBatchInfo info = termBatch.insertTerminalBatch(connection, merchId, termId);
        
        	terminalBatchMap.put(info.getMerchId() + info.getTermId() , info);
        	
        	return info;
    	}

    }

}
