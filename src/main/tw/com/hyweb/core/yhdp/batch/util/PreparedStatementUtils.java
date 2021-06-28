/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/23
 */


package tw.com.hyweb.core.yhdp.batch.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.util.ReleaseResource;


/**
 * @author Clare
 * 
 */
public class PreparedStatementUtils
{
    private final static Logger LOGGER = Logger.getLogger(PreparedStatementUtils.class);

    private final static int WARNING_EXECUTION_TIME = 50;

    /**
     * 將所有欄位化為SQL欄位字串：column1,column2,column3....columnn
     * 
     * @param columns
     * @return
     */
    public static String columns2String(String... columns)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < columns.length; ++i)
        {
            sb.append(columns[i]);

            if (i != columns.length - 1)
            {
                sb.append(',');
            }
        }

        return sb.toString();
    }

    /**
     * 將所有欄位化為SQL欄位字串：column1,column2,column3....columnn
     * 
     * @param columns
     * @return
     */
    public static String columns2String(List<String> columns)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < columns.size(); ++i)
        {
            sb.append(columns.get(i));

            if (i != columns.size() - 1)
            {
                sb.append(',');
            }
        }

        return sb.toString();
    }

    /**
     * 將所有值為SQL值字串：value1,value2,value3,...valuen
     * 
     * @param values
     * @return
     */
    public static String values2String(String... values)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; ++i)
        {
            sb.append(values[i]);

            if (i != values.length - 1)
            {
                sb.append(',');
            }
        }

        return sb.toString();
    }

    /**
     * 將所有值為SQL值字串：value1,value2,value3,...valuen
     * 
     * @param values
     * @return
     */
    public static String values2String(List<String> values)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.size(); ++i)
        {
            sb.append(values.get(i));

            if (i != values.size() - 1)
            {
                sb.append(',');
            }
        }

        return sb.toString();
    }

    /**
     * 將欄位名稱轉換成下列的條件式：column1=? and column2=?...
     * 
     * @param columns
     * @return
     */
    public static String makeColumnsCondition(String... columns)
    {
        StringBuilder condition = new StringBuilder();

        for (int i = 0; i < columns.length; ++i)
        {
            condition.append(columns[i]);
            condition.append("=?");

            if (i != columns.length - 1)
            {
                condition.append(" and ");
            }
        }

        return condition.toString();
    }

    /**
     * 傳回指定數量的?,字串
     * 
     * @param amount
     * @return
     */
    public static String getQuestionMarkString(int amount)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= amount; ++i)
        {
            sb.append('?');

            if (i != amount)
            {
                sb.append(',');
            }
        }

        return sb.toString();
    }

    /**
     * 找出table所有的欄位名稱
     * 
     * @param connection
     * @param table
     * @return
     * @throws SQLException
     */
    public static List<String> getColumnNames(Connection connection, String table) throws SQLException
    {
        List<String> fields = new ArrayList<String>();

        Statement stat = null;
        ResultSet rs = null;

        try
        {
            stat = connection.createStatement();
            rs = stat.executeQuery("select * from " + table + " where 0<>0");

            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();

            for (int i = 1; i <= count; ++i)
            {
                fields.add(metaData.getColumnName(i));
            }
        }
        finally
        {
            ReleaseResource.releaseDB(null, stat, rs);
        }

        return fields;
    }

    /**
     * 找出table所有的欄位名稱
     * 
     * @param connection
     * @param table
     * @return
     * @throws SQLException
     */
    public static List<String> getColumnNames(Connection connection, String table, String... excludeColumns) throws SQLException
    {
        List<String> fields = getColumnNames(connection, table);
        fields.removeAll(Arrays.asList(excludeColumns));

        return fields;
    }

    /**
     * 找出所有table相同的欄位名稱
     * 
     * @param connection
     * @param table1
     * @param destinationTable
     * @return
     * @throws SQLException
     */
    public static List<String> getTheSameColumns(Connection connection, String... tables) throws SQLException
    {
        List<String> fields = new ArrayList<String>();

        fields.addAll(getColumnNames(connection, tables[0]));

        for (int i = 1; i < tables.length; ++i)
        {
            fields.retainAll(getColumnNames(connection, tables[i]));
        }

        return fields;
    }

    /**
     * 找出table所有primary key的欄位名稱
     * 
     * @param connection
     * @param table
     * @return
     * @throws SQLException
     */
    public static List<String> getPrimaryKeys(Connection connection, String table) throws SQLException
    {
        List<String> columns = new ArrayList<String>();

        ResultSet rs = null;

        try
        {
            rs = connection.getMetaData().getPrimaryKeys(null, null, table);

            while (rs.next())
            {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        }
        finally
        {
            ReleaseResource.releaseDB(rs);
        }

        return columns;
    }

    /**
     * 將值填入preparedStatement的參數後批次執行SQL並回傳成功筆數
     * 
     * @param connection
     * @param sql
     * @param parameterValues
     * @return 成功筆數
     * @throws SQLException
     */
    public static int[] executeBatchUpdate(Connection connection, String sql, Object[]... parameterValues) throws SQLException
    {
        PreparedStatement preparedStatement = null;

        try
        {
            LOGGER.debug("create prepared statement:" + sql);
            LOGGER.debug("values:" + Arrays.deepToString(parameterValues));

            preparedStatement = connection.prepareStatement(sql);

            for (Object[] values : parameterValues)
            {
                fillParameter(preparedStatement, values);

                preparedStatement.addBatch();
            }

            int[] rowCounts = preparedStatement.executeBatch();

            LOGGER.debug("counts:" + Arrays.toString(rowCounts));

            return rowCounts;
        }
        finally
        {
            ReleaseResource.releaseDB(preparedStatement);
        }
    }

    /**
     * 將值填入preparedStatement的參數後執行SQL並回傳成功筆數
     * 
     * @param connection
     * @param sql
     * @param parameterValues
     * @return 成功筆數
     * @throws SQLException
     */
    public static int executeUpdate(Connection connection, String sql, Object... parameterValues) throws SQLException
    {
        PreparedStatement preparedStatement = null;

        try
        {
            preparedStatement = getPreparedStatement(connection, sql, parameterValues);

            long time = System.currentTimeMillis();

            int rowCount = preparedStatement.executeUpdate();

            time = System.currentTimeMillis() - time;

            if (time >= WARNING_EXECUTION_TIME)
            {
                LOGGER.warn("sql execution time:" + time + "ms for sql:" + sql);
            }

            LOGGER.debug("count:" + rowCount);

            return rowCount;
        }
        finally
        {
            ReleaseResource.releaseDB(preparedStatement);
        }
    }

    /**
     * @param connection
     * @param sql
     * @param parameterValues
     * @return
     * @throws SQLException
     */
    public static List<Map<String, String>> executeQuery(Connection connection, String sql, Object... parameterValues) throws SQLException
    {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try
        {
            preparedStatement = getPreparedStatement(connection, sql, parameterValues);

            long time = System.currentTimeMillis();

            resultSet = preparedStatement.executeQuery();

            time = System.currentTimeMillis() - time;

            if (time >= WARNING_EXECUTION_TIME)
            {
                LOGGER.warn("sql execution time:" + time + "ms for sql:" + sql);
            }

            ResultSetMetaData meta = resultSet.getMetaData();

            List<Map<String, String>> results = new ArrayList<Map<String, String>>();

            while (resultSet.next())
            {
                Map<String, String> result = new HashMap<String, String>();

                for (int i = 1; i <= meta.getColumnCount(); ++i)
                {
                    result.put(meta.getColumnName(i), resultSet.getString(meta.getColumnName(i)));
                }

                results.add(result);
            }

            LOGGER.debug("result:" + results);

            return results;
        }
        finally
        {
            ReleaseResource.releaseDB(null, preparedStatement, resultSet);
        }
    }

    /**
     * 查詢單筆資料，如果無資料回傳null，超過一筆拋出IllegalArgumentException
     * 
     * @param connection
     * @param sql
     * @param parameterValues
     * @return
     * @throws SQLException
     */
    public static Map<String, String> executeQuerySingleData(Connection connection, String sql, Object... parameterValues) throws SQLException
    {
        List<Map<String, String>> result = executeQuery(connection, sql, parameterValues);

        if (result.size() > 1)
        {
            throw new IllegalArgumentException("data size is " + result.size() + ", more than 1!");
        }

        return (result.size() == 0) ? null : result.get(0);
    }

    /**
     * 查詢單一欄位資料，如果無資料回傳null，超過一個欄位則拋出IllegalArgumentException
     * 
     * @param connection
     * @param sql
     * @param parameterValues
     * @return
     * @throws SQLException
     */
    public static List<String> executeQuerySingleColumnData(Connection connection, String sql, Object... parameterValues) throws SQLException
    {
        List<Map<String, String>> results = executeQuery(connection, sql, parameterValues);

        if (results.size() > 1 && results.get(0).keySet().size() > 1)
        {
            throw new IllegalArgumentException("column size is " + results.size() + ", more than 1!");
        }

        List<String> values = new ArrayList<String>();

        for (Map<String, String> result : results)
        {
            String value = null;

            if (result != null)
            {
                for (String temp : result.values())
                {
                    value = temp;
                }
            }

            values.add(value);
        }

        return values;
    }

    /**
     * 查詢單一值，如果無資料回傳null，超過一筆或是一個欄位拋出IllegalArgumentException
     * 
     * @param connection
     * @param sql
     * @param parameterValues
     * @return
     * @throws SQLException
     */
    public static String executeQuerySingleValue(Connection connection, String sql, Object... parameterValues) throws SQLException
    {
        Map<String, String> result = executeQuerySingleData(connection, sql, parameterValues);

        if (result != null && result.size() > 1)
        {
            throw new IllegalArgumentException("field size is " + result.size() + ", more than 1!");
        }

        String value = null;

        if (result != null)
        {
            for (String temp : result.values())
            {
                value = temp;
            }
        }

        return value;
    }

    /**
     * @param connection
     * @param sql
     * @param parameterValues
     * @return
     * @throws SQLException
     */
    private static PreparedStatement getPreparedStatement(Connection connection, String sql, Object... parameterValues) throws SQLException
    {
        LOGGER.debug("create prepared statement:" + sql);
        LOGGER.debug("values:" + Arrays.toString(parameterValues));

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        fillParameter(preparedStatement, parameterValues);

        return preparedStatement;
    }

    /**
     * 根據順序將值填入PreparedStatement的參數
     * 
     * @param preparedStatement
     * @param values
     * @throws SQLException
     */
    private static void fillParameter(PreparedStatement preparedStatement, Object... values) throws SQLException
    {
        for (int i = 0; i < values.length; ++i)
        {
            preparedStatement.setObject(i + 1, values[i]);
        }
    }
}
