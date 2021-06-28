/*
 * $Id: UltraLiteDiscountCardPersoJobDecoratorTest.java 1919 2009-08-18 07:13:49Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */
package tw.com.hyweb.svc.cp.batch.perso;

import java.io.File;
import java.sql.Connection;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.easymock.classextension.EasyMock;

import tw.com.hyweb.service.db.info.TbPersoSettingInfo;
import tw.com.hyweb.svc.cp.batch.BatchTestCase;
import tw.com.hyweb.svc.cp.batch.DbUnitTestUtils;

/**
 * @author Clare
 * @version $Revision: 1919 $
 */
public class UltraLiteDiscountCardPersoJobDecoratorTest extends BatchTestCase
{
    private static final String TEST_DATA_PATH = "DbUnitData/batch/perso/ProcPerso/";

    private IDataSet dataSet;

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.BatchTestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        dataSet = new FlatXmlDataSet(new File(TEST_DATA_PATH + "ultralite.xml"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cp.batch.BatchTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        databaseTester.setDataSet(dataSet);
        databaseTester.onTearDown();

        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testDecorator() throws Exception
    {
        TbPersoSettingInfo persoSetting = new TbPersoSettingInfo();
        persoSetting.setMemId("54875487");
        persoSetting.setCardProduct("5487");
        persoSetting.setPersoBatchNo("20090818001");
        persoSetting.setPersoQty(2);

        ProcessPersoJob job = EasyMock.createMock(ProcessPersoJob.class);
        EasyMock.expect(job.getPersoSetting()).andReturn(persoSetting);
        job.action(EasyMock.isA(Connection.class), EasyMock.eq("20090818"));
        EasyMock.replay(job);

        new UltraLiteDiscountCardPersoJobDecorator(job).action(connection.getConnection(), "20090818");

        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_CARD"), "CARD_NO in ('5487548754875487','5487548754875488') and EXPIRY_DATE='20991231'");
        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_CARD_EXT"), "CARD_NO in ('5487548754875487','5487548754875488') and EXPIRY_DATE='20991231'");

        EasyMock.verify(job);
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cpc.batch.BatchTestCase#getTestDataPath()
     */
    @Override
    protected String getTestDataPath()
    {
        return TEST_DATA_PATH + "ultralite_initial.xml";
    }
}
