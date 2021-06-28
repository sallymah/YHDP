/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/6/4
 */
package tw.com.hyweb.svc.cp.batch.preoperation;

import java.io.File;

import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchExecutor;
import tw.com.hyweb.svc.cp.batch.BatchTestCase;
import tw.com.hyweb.svc.cp.batch.DbUnitTestUtils;


/**
 * @author Anny
 * 
 */
public class SimulateCardReturnTest extends BatchTestCase
{
    private static final String TEST_DATA_PATH = "DbUnitData/batch/CardReturn/";
    
    //private String batchDate = "20090101";

    private IDataSet dataSet;

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cpc.batch.BatchTestCase#setUp()
     */
    protected void setUp() throws Exception
    {
    	super.setUp();
    	dataSet = new FlatXmlDataSet(new File(TEST_DATA_PATH + "cardreturn.xml"));
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cpc.batch.BatchTestCase#tearDown()
     */
    /*protected void tearDown() throws Exception
    {
        System.setProperty("date", "");

        databaseTester.setDataSet(dataSet);
        
        databaseTester.onTearDown();

        super.tearDown();
    }*/

	protected void tearDown() throws Exception {

        System.setProperty("date", "20100108");
        
		QueryDataSet queryDataSet = new QueryDataSet(connection);
		queryDataSet.addTable("TB_CARD_RETURN", "select * from TB_CARD_RETURN where card_no like 'Anny%'");
		queryDataSet.addTable("TB_CARD_RETURN_DTL", "select * from TB_CARD_RETURN_DTL where card_no like 'Anny%'");
		queryDataSet.addTable("TB_TRANS", "select * from TB_TRANS where card_no like 'Anny%'");
		queryDataSet.addTable("TB_TRANS_DTL", "select * from TB_TRANS_DTL where card_no like 'Anny%'");
		queryDataSet.addTable("TB_TERM_BATCH", "select * from TB_TERM_BATCH where merch_id like 'Anny%'");
		queryDataSet.addTable("TB_CARD_BAL", "select * from TB_CARD_BAL where card_no like 'Anny%'");
		queryDataSet.addTable("TB_BONUS", "select * from TB_BONUS where bonus_id like 'P%' or bonus_id like 'C%'");
		queryDataSet.addTable("TB_CARD", "select * from TB_CARD where card_no like 'Anny%'");
		
        databaseTester.setDataSet(queryDataSet);
        databaseTester.onTearDown();
		super.tearDown();
		
	}
	
    public void testSimulateCardReturnTest() throws Exception
    {
        //System.setProperty("date", batchDate);
        BatchExecutor.main(new String[] { "config/batch/preoperation/SimulateCardReturn/beans-config.xml", "processor" });

        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_CARD_RETURN"), "card_no like 'Anny%'");
        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_TERM_BATCH"), "merch_id like 'Anny%'");
        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_TRANS"), "card_no like 'Anny%'");
        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_TRANS_DTL"), "card_no like 'Anny%'");
        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_CARD_BAL"), "card_no like 'Anny%'");
    }
    
    

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cpc.batch.BatchTestCase#getTestDataPath()
     */
    @Override
    protected String getTestDataPath()
    {
        return TEST_DATA_PATH + "initial.xml";
    }
}
