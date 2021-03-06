/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/9/30
 */
package tw.com.hyweb.svc.yhdp.batch.summary;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleException;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

/**
 * @author Anny
 * 2009/10/26 新增欄位:UNRELOAD_BAL
 * 
 */
public class SystexIssuerPbSummaryHandler implements BatchHandler
{
    private static final Logger LOGGER = Logger.getLogger(SystexIssuerPbSummaryHandler.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler#handle(java
     * .sql.Connection, java.lang.String)
     */
    public BatchHandleResult handle(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws BatchHandleException
    {
        String sql = "insert into TB_ISS_PBM(PROC_DATE,ISS_MEM_ID,PB_VALID_SDATE,PB_VALID_EDATE,ISS_MAX_QUOTA,ISS_CURRENT_QUOTA,QUOTA_ALERT,QUOTA_LIMIT,PB_CHARGE_EMAIL1,PB_CHARGE_EMAIL2,PB_CHARGE_EMAIL3,UNRELOAD_BAL,PAR_MON,PAR_DAY) select ?,MEM_ID,PB_VALID_SDATE,PB_VALID_EDATE,ISS_MAX_QUOTA,ISS_CURRENT_QUOTA,QUOTA_ALERT,QUOTA_LIMIT,PB_CHARGE_EMAIL1,PB_CHARGE_EMAIL2,PB_CHARGE_EMAIL3,UNRELOAD_BAL,'" + batchDate.substring(4, 6) + "','" + batchDate.substring(6, 8) + "' from TB_MEMBER where PBM_FLAG='1'";

        try
        {
            executeUpdate(connection, sql, batchDate);
        }
        catch (SQLException e)
        {
            LOGGER.warn("exception when summary issuer pbm", e);

            throw new BatchHandleException(e, Layer1Constants.RCODE_2999_ALLDATAERROR);
        }

        return BatchHandleResult.DEFAULT_SUCCESS_RESULT;
    }
}
