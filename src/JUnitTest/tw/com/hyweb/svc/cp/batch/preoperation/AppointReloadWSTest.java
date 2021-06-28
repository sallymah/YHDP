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

import tw.com.hyweb.cp.ws.appointreload.AppointReloadServiceClient;
import tw.com.hyweb.cp.ws.appointreload.AppointReloadServicePortType;
import tw.com.hyweb.svc.cp.batch.BatchTestCase;
import tw.com.hyweb.svc.cp.batch.DbUnitTestUtils;


/**
 * @author Anny
 * 
 */
public class AppointReloadWSTest extends BatchTestCase
{
    private static final String TEST_DATA_PATH = "DbUnitData/batch/AppointReload/";
    
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
    	dataSet = new FlatXmlDataSet(new File(TEST_DATA_PATH + "appointreload.xml"));
        
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

       // System.setProperty("date", "20100108");
        
		QueryDataSet queryDataSet = new QueryDataSet(connection);
		queryDataSet.addTable("TB_APPOINT_RELOAD", "select * from TB_APPOINT_RELOAD where balance_id like 'Anny%'");
		queryDataSet.addTable("TB_APPOINT_RELOAD_DTL", "select * from TB_APPOINT_RELOAD_DTL where balance_id like 'Anny%'");
		queryDataSet.addTable("TB_ONL_TXN", "select * from TB_ONL_TXN where card_no like 'Anny%'");
		queryDataSet.addTable("TB_ONL_TXN_DTL", "select * from TB_ONL_TXN_DTL where card_no like 'Anny%'");
		queryDataSet.addTable("TB_TERM_BATCH", "select * from TB_TERM_BATCH where merch_id like 'Anny%'");
		queryDataSet.addTable("TB_CARD_BAL", "select * from TB_CARD_BAL where card_no like 'Anny%'");

        databaseTester.setDataSet(queryDataSet);
        databaseTester.onTearDown();
		super.tearDown();
		
	}
	
    public void testAppointReloadWSTestTest() throws Exception
    {
        //System.setProperty("date", batchDate);
        //BatchExecutor.main(new String[] { "config/batch/preoperation/SimulateCardReturn/beans-config.xml", "processor" });

		System.out.println("start:");
		
		String URL = "http://10.10.10.31:8080/hyweb-ws/services/AppointReloadService";
		AppointReloadServicePortType service = new AppointReloadServiceClient().getAppointReloadServiceHttpPort(URL);
		Boolean result = service.simulateAppointReload("APPLOAD.111.20100101");
		
		System.out.println("result :" + result);
		
        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_APPOINT_RELOAD"), "balance_id like 'Anny%'");
        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_ONL_TXN"), "card_no like 'Anny%'");
        DbUnitTestUtils.assertEquals(connection, dataSet.getTable("TB_ONL_TXN_DTL"), "card_no like 'Anny%'");
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
