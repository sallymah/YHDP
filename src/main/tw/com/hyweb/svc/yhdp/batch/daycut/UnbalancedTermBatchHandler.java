/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/18
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleException;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.util.date.DateUtil;

/**
 * @author Clare
 * 
 */
public class UnbalancedTermBatchHandler implements BatchHandler
{
    private final BatchHandler handler;
    private final String transactionTable;

    private final CutTermBatchFilter filter;

    public UnbalancedTermBatchHandler(BatchHandler handler, CutTermBatchFilter filter, String transactionTable)
    {
        this.handler = handler;
        this.filter = filter;
        this.transactionTable = transactionTable;
    }

    /**
     * 在過檔之前，先把不平帳的批及交易註記rcode 2101
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler#handle(java.sql.Connection,
     *      java.lang.String)
     */
    public BatchHandleResult handle(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws BatchHandleException
    {
        try
        {
            String setValue = "CUT_DATE='" + batchDate + "',CUT_TIME='" + DateUtil.getTodayString().substring(8) + "',CUT_RCODE='" + Constants.RCODE_2101_NoDayCut_ERR + "'";
            String unbalancedTermBatchCondition = filter.getCutCondition(tbBatchResultInfo) + " and TERM_SETTLE_FLAG='9'";

            String updateTransaction = "update " + transactionTable + " set " + setValue + " where (MERCH_ID,TERM_ID,BATCH_NO,TERM_SETTLE_DATE,TERM_SETTLE_TIME) in (select MERCH_ID,TERM_ID,BATCH_NO,TERM_SETTLE_DATE,TERM_SETTLE_TIME from TB_TERM_BATCH where " + unbalancedTermBatchCondition + ")";
            DBService.getDBService().sqlAction(updateTransaction, connection, false);

            String updateTermBatch = "update TB_TERM_BATCH set " + setValue + " where " + unbalancedTermBatchCondition;
            DBService.getDBService().sqlAction(updateTermBatch, connection, false);

            connection.commit();

            return handler.handle(connection, batchDate, tbBatchResultInfo);
        }
        catch (Exception e)
        {
            throw new BatchHandleException(e);
        }
    }
}
