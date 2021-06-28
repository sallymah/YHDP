/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/24
 */
package tw.com.hyweb.svc.cp.batch;

import java.io.File;

import junit.framework.TestCase;

import org.dbunit.IDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

/**
 * @author Clare
 * 
 */
public abstract class BatchTestCase extends TestCase
{
    protected IDatabaseTester databaseTester;
    protected IDatabaseConnection connection;
    protected IDataSet initialDataSet;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        initialDataSet = new FlatXmlDataSet(new File(getTestDataPath()));

        databaseTester = DbUnitTestUtils.getDatabaseTester();
        databaseTester.setDataSet(initialDataSet);
        databaseTester.onSetup();

        connection = databaseTester.getConnection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        connection.close();

        databaseTester.setDataSet(initialDataSet);
        databaseTester.onTearDown();

        super.tearDown();
    }

    /**
     * initial測試資料路徑
     * 
     * @return
     */
    protected abstract String getTestDataPath();
}
