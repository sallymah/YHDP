/*
 * $Id: CompositeBatchHandler.java 13924 2009-05-12 02:42:54Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.yhdp.batch.framework.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;


/**
 * @author Clare
 * @version $Revision$
 */
public class CompositeBatchHandler implements BatchHandler
{
    private final List<BatchHandler> handlers;

    /**
     * @param handlers
     */
    public CompositeBatchHandler(List<BatchHandler> handlers)
    {
        this.handlers = handlers;
    }

    /**
     * 
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.generic.BatchHandler#handle(java
     *      .sql.Connection, java.lang.String)
     */
    public BatchHandleResult handle(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        List<BatchHandleResult> results = new ArrayList<BatchHandleResult>();

        for (BatchHandler handler : handlers)
        {
            results.add(handler.handle(connection, batchDate, tbBatchResultInfo));
        }

        return new BatchHandleResult(getCompositeErrorDesc(results), getCompositeRcode(results));
    }

    /**
     * @param results
     * @return
     */
    private String getCompositeRcode(List<BatchHandleResult> results)
    {
        for (BatchHandleResult result : results)
        {
            if (!Constants.RCODE_0000_OK.equals(result.getRcode()))
            {
                return Constants.RCODE_2001_WARN;
            }
        }

        return Constants.RCODE_0000_OK;
    }

    /**
     * @param results
     * @return
     */
    private String getCompositeErrorDesc(List<BatchHandleResult> results)
    {
        StringBuilder errorDesc = new StringBuilder();

        for (BatchHandleResult result : results)
        {
            errorDesc.append("[");
            errorDesc.append(result.getErrorDescribe() + ":" + result.getRcode());
            errorDesc.append("]");
        }

        return errorDesc.toString();
    }
}
