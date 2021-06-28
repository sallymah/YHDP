/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */
package tw.com.hyweb.svc.cp.online.controller.settlement;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;

/**
 * SettlementTest
 */
public class SettlementTest extends TestCase
{

    /**
     * @param arg0
     */
    public SettlementTest(String arg0)
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
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml","del.TB_TERM_BATCH.xml"});
        //DbUnitUtil.refreshDbFromFiles(new String[]{"TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_REDEEM_FUNC.xml","TB_REDEEM_FUNC_DTL.xml","TB_BONUS.xml","TB_BONUS_BASE.xml"});
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testEmptyTxn() throws Exception
    {

        LMSContext ctx = UnitTestUtil.readToContext("settle.xml");
        BaseController settlement = (BaseController) DefMain.getBean("Settlement");
        LMSContext respCtx = (LMSContext) settlement.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());
        System.out.println(respCtx);

        DefMainMock.initDefMainNoSocket("loyalty");
        LMSContext ctx96 = UnitTestUtil.readToContext("settle96.xml");
        BaseController settlement96 = (BaseController) DefMain.getBean("Settlement96");
        LMSContext respCtx96 = (LMSContext) settlement.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx96.getRcode(),ctx96.checkRcode());
        System.out.println(respCtx96);
    }

}
