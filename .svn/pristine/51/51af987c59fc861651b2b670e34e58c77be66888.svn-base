/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */
package tw.com.hyweb.svc.cp.online.validator;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;
import tw.com.hyweb.online.IValidator;
import junit.framework.TestCase;

/**
 * SamValidatorTest
 */
public class SamValidatorTest extends TestCase
{

    /**
     * @param arg0
     */
    public SamValidatorTest(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        DefMainMock.initDefMainNoSocket("loyalty");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
    }

    /**
     * {@link tw.com.hyweb.svc.cp.online.validator.SamValidator#validate(tw.com.hyweb.online.Context)} 的測試方法。
     * @throws Exception
     */
    public void testValidate() throws Exception
    {
        DbUnitUtil.refreshDbFromFile("TB_TERM.xml");
        LMSContext ctx = UnitTestUtil.readToContext("keysync.xml");
        IValidator validator = (IValidator) DefMain.getBean("SamValidator");
        validator.validate(ctx);
        assertTrue("test data exist.",ctx.checkRcode());

    }

}
