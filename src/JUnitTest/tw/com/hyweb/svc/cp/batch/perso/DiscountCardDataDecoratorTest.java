/*
 * $Id: DiscountCardDataDecoratorTest.java 1908 2009-08-17 06:53:38Z 96004 $
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
 * @version $Revision: 1908 $
 */
public class DiscountCardDataDecoratorTest extends BatchTestCase
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

        dataSet = new FlatXmlDataSet(new File(TEST_DATA_PATH + "card_extend.xml"));
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
    public void testDiscountCardGeneration() throws Exception
    {
        TbPersoSettingInfo persoSetting = new TbPersoSettingInfo();
        persoSetting.setExpiryDate("20991231");
        persoSetting.setCardProduct("5487");

        CardDataGenerator generator = EasyMock.createMock(CardDataGenerator.class);
        EasyMock.expect(generator.makeCardData(EasyMock.isA(Connection.class), EasyMock.eq(""), EasyMock.eq(""), EasyMock.eq("54875487548754875487"), EasyMock.eq(persoSetting))).andReturn(null);
        EasyMock.replay(generator);

        new DiscountCardDataDecorator(generator).makeCardData(connection.getConnection(), "", "", "54875487548754875487", persoSetting);

        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_CARD_EXT"), "CARD_NO='54875487548754875487' and EXPIRY_DATE='20991231'");

        EasyMock.verify(generator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cpc.batch.BatchTestCase#getTestDataPath()
     */
    @Override
    protected String getTestDataPath()
    {
        return TEST_DATA_PATH + "card_product_extend.xml";
    }
}
