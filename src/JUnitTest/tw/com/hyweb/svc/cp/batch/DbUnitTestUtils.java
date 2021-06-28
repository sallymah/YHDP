/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/15
 */
package tw.com.hyweb.svc.cp.batch;

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.Assertion;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author Clare
 * 
 */
public class DbUnitTestUtils
{
    /**
     * 取得dataSourceDatabaseTester
     * 
     * @param schema
     * @return
     */
    public static IDatabaseTester getDatabaseTester()
    {
        return getDatabaseTester("OWNER");
    }

    /**
     * 取得dataSourceDatabaseTester
     * 
     * @param schema
     * @return
     */
    public static IDatabaseTester getDatabaseTester(String schema)
    {
        BasicDataSource dataSource = ((BasicDataSource) new FileSystemXmlApplicationContext("DbUnitData/batch/datasource.xml").getBean("dataSource"));
        dataSource.setDefaultAutoCommit(true);

        DataSourceDatabaseTester dataSourceDatabaseTester = new HywebDataSourceDatabaseTester(dataSource);
        dataSourceDatabaseTester.setSchema(schema);

        return dataSourceDatabaseTester;
    }

    /**
     * 根據預測資料的欄位及條件來比對結果
     * 
     * @param connection
     * @param table
     * @param condition
     * @throws DatabaseUnitException
     * @throws Exception
     * @throws DataSetException
     * @throws SQLException
     */
    public static void assertEquals(IDatabaseConnection connection, ITable table, String condition) throws DatabaseUnitException, Exception, DataSetException, SQLException
    {
        Assertion.assertEquals(new SortedTable(table), new SortedTable(DbUnitTestUtils.getQueryTable(connection, table.getTableMetaData().getTableName(), DbUnitTestUtils.columns2String(table), condition)));
    }

    /**
     * 傳回欲查詢的table資料
     * 
     * @param connection
     * @param tableName
     * @param fields
     * @param condition
     * @return
     * @throws Exception
     * @throws DataSetException
     * @throws SQLException
     */
    private static ITable getQueryTable(IDatabaseConnection connection, String tableName, String fields, String condition) throws Exception, DataSetException, SQLException
    {
        return connection.createQueryTable(tableName, "select " + fields + " from " + tableName + " where " + condition);
    }

    /**
     * 將欄位組成下列格式的字串以便SQL使用：field_1,field_2,....,field_n
     * 
     * @param table
     * @return
     * @throws DataSetException
     */
    private static String columns2String(ITable table) throws DataSetException
    {
        StringBuilder fields = new StringBuilder();

        for (Column column : table.getTableMetaData().getColumns())
        {
            fields.append(column.getColumnName() + ",");
        }

        return fields.deleteCharAt(fields.length() - 1).toString();
    }
}
