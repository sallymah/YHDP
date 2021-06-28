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
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;

/**
 * SAMLogonTest
 */
public class SAMLogonTest extends TestCase
{

    /**
     * Constructor
     * @param arg0
     */
    public SAMLogonTest(String arg0)
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
    public void testSamLogon() throws Exception
    {
        DefMainMock.initDefMainNoSocket("loyalty");

        LMSContext ctx = UnitTestUtil.readToContext("SAMLogon.xml");
        BaseController samlogon = (BaseController) DefMain.getBean("SAMLogon");
        LMSContext ctxResp = (LMSContext) samlogon.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());
        assertEquals(ctx.getLmsTerminalId(),ctxResp.getLMSMsg().getHexStr(LMSTag.SAMCertificateRN).substring(8));
        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
    }
}
