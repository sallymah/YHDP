/*
 * $Id: HostPointRedeemFunctionTest.java 1910 2009-08-17 09:06:19Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */
package tw.com.hyweb.svc.cp.batch.parmdown.function;

import java.util.Arrays;

import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.service.db.info.TbTermParDefInfo;
import tw.com.hyweb.svc.cp.batch.BatchTestCase;

/**
 * @author Clare
 * @version $Revision: 1910 $
 */
public class HostPointRedeemFunctionTest extends BatchTestCase
{
    private static final String TEST_DATA_PATH = "DbUnitData/batch/parmdown/ProcParm/";

    /**
     * @throws Exception
     */
    public void testFunction() throws Exception
    {
        TbTermInfo terminal = new TbTermInfo();
        terminal.setMemId("54875487");

        assertEquals(Arrays.asList("54870000020113test 123", "54880000020557test 456"), new HostPointRedeemFunction().getValues(connection.getConnection(), "20090817", new TbTermParDefInfo(), terminal));
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cpc.batch.BatchTestCase#getTestDataPath()
     */
    @Override
    protected String getTestDataPath()
    {
        return TEST_DATA_PATH + "redeem_function.xml";
    }
}
