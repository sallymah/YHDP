/*
 * $Id$
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */
package tw.com.hyweb.svc.cp.batch.parmdown.function;

import java.util.Arrays;

import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.svc.cp.batch.BatchTestCase;

/**
 * @author Clare
 * @version $Revision$
 */
public class AcquireCommonBonusSourceTest extends BatchTestCase
{
    private static final String TEST_DATA_PATH = "DbUnitData/batch/parmdown/ProcParm/";

    /**
     * @throws Exception
     */
    public void testBonusSource() throws Exception
    {
        TbTermInfo terminal = new TbTermInfo();
        terminal.setMemId("54875487");

        assertEquals(Arrays.asList("5487548754", "5487548756", "5487548757"), new AcquireCommonBonusSource().getBonusIds(connection.getConnection(), null, terminal));
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.hyweb.core.cpc.batch.BatchTestCase#getTestDataPath()
     */
    @Override
    protected String getTestDataPath()
    {
        return TEST_DATA_PATH + "reload_function.xml";
    }
}
