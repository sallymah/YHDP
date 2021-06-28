/*
 * $Id: SimpleBatchProcessor.java 13940 2009-05-13 05:59:00Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.yhdp.batch.framework.generic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.yhdp.batch.util.DateUtils;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Clare
 * @version $Revision: 13940 $
 */
public class SimpleBatchProcessor extends AbstractBatchBasic
{
    private static final Logger LOGGER = Logger.getLogger(SimpleBatchProcessor.class);

    private final BatchHandler handler;

    private DataSource dataSource;
    private Map<String, RecoverHandler> recoverLevelMap = new HashMap<String, RecoverHandler>();

    private String batchDate;
    private String recoverLevel;

    public SimpleBatchProcessor(BatchHandler handler)
    {
        this.handler = handler;

        initialParameter();
    }

    /**
     * initial batchDate及recover level參數值
     */
    private void initialParameter()
    {
        if (StringUtil.isEmpty(System.getProperty("date")))
        {
            batchDate = DateUtils.getSystemDate();
        }
        else
        {
            batchDate = System.getProperty("date");
        }

        recoverLevel = System.getProperty("recover");
    }

    /**
     * 正常的batch處理邏輯，如果沒有指定recover則正常執行batch，有則執行recover
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.IBatchProcess#process(java.lang.String[])
     */
    public void process(String[] argv) throws Exception
    {
        if (dataSource == null)
        {
            dataSource = BatchDataSource.getDataSource();
        }

        Connection connection = null;

        try
        {
            connection = new PreparedStatementManagedConnection(dataSource.getConnection());

            if (StringUtil.isEmpty(recoverLevel))
            {
                process(connection);
            }
            else
            {
                recover(connection);
            }

            connection.commit();
        }
        catch (BatchHandleException e)
        {
            handleError(connection, e, e.getRcode());

            throw e;
        }
        catch (Throwable e)
        {
            handleError(connection, e, Layer1Constants.RCODE_2999_ALLDATAERROR);

            throw new Exception(e);
        }
        finally
        {
            ReleaseResource.releaseDB(connection);
        }
    }

    /**
     * @param connection
     * @param e
     * @param rcode
     * @throws SQLException
     */
    private void handleError(Connection connection, Throwable e, String rcode) throws SQLException
    {
        LOGGER.warn("error when process batch", e);
        connection.rollback();

        setRcode(rcode);
    }

    /**
     * 處理batch process，並set處理結果的error describe及rcode
     * 
     * @param connection
     * @throws Exception
     */
    private void process(Connection connection) throws Exception
    {
        BatchHandleResult result = handler.handle(connection, batchDate, getBatchResultInfo());

        setErrorDesc(result.getErrorDescribe());
        setRcode(result.getRcode());
    }

    /**
     * 根據recover level選擇適當的handler處理
     * 
     * @param connection
     * @throws Exception
     */
    private void recover(Connection connection) throws Exception
    {
        initialRecoverData();

        if (recoverLevelMap.containsKey(recoverLevel.toUpperCase()))
        {
            recoverLevelMap.get(recoverLevel.toUpperCase()).recover(connection, batchDate, getBatchResultInfo());
        }
        else
        {
            throw new IllegalArgumentException("recover level:" + recoverLevel + " is not supported!");
        }
    }

    /**
     * initial recover level map的設定，將所有的level轉為大寫
     */
    private void initialRecoverData()
    {
        Map<String, RecoverHandler> temp = new HashMap<String, RecoverHandler>();

        for (String key : recoverLevelMap.keySet())
        {
            temp.put(key.toUpperCase(), recoverLevelMap.get(key));
        }

        recoverLevelMap.clear();
        recoverLevelMap = temp;
    }

    /**
     * @param dataSource
     *            the dataSource to set
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * @param recoverLevelMap
     *            the recoverLevelMap to set
     */
    public void setRecoverLevelMap(Map<String, RecoverHandler> recoverLevelMap)
    {
        this.recoverLevelMap = recoverLevelMap;
    }
}
