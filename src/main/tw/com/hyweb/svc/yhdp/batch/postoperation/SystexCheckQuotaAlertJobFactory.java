/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/9/30
 */
package tw.com.hyweb.svc.yhdp.batch.postoperation;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuerySingleValue;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbIssPbmInfo;
import tw.com.hyweb.service.db.mgr.TbIssPbmMgr;

/**
 * @author Anny
 * 20110221: Email通知功能，改為發簡訊通知功能
 */
public class SystexCheckQuotaAlertJobFactory extends DAOBatchJobFactory
{
//    private final String mailServer;
//    private final String mailFrom;

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
    	/*20111221 加入AND ISS_MAX_QUOTA<>0 ，避免除數為0照成錯誤*/
        Vector<TbIssPbmInfo> result = new Vector<TbIssPbmInfo>();
        new TbIssPbmMgr(connection).queryMultiple("PROC_DATE='" + batchDate + "' and (ISS_CURRENT_QUOTA / ISS_MAX_QUOTA ) * 100 > QUOTA_ALERT AND ISS_MAX_QUOTA<>0", result);

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
        return new SystexCheckQuotaAlertJob((TbIssPbmInfo) info);
    }
}
