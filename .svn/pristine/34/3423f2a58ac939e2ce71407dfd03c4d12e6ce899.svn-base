/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */
package tw.com.hyweb.core.cp.online.loyalty.controller.samlogon;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;

/**
 * SAMLogon2Test
 */
public class SAMLogon2Test extends TestCase
{

    /**
     * Constructor
     * @param arg0
     */
    public SAMLogon2Test(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    /**
     * @throws Exception
     */
    public void testSamLogon2() throws Exception
    {
        DefMainMock.initDefMainNoSocket("loyalty");

        LMSContext ctx = UnitTestUtil.readToContext("SAMLogon2.xml");
        BaseController samlogon = (BaseController) DefMain.getBean("SAMLogon2");
        LMSContext ctxResp = (LMSContext) samlogon.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());

        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
    }
}
