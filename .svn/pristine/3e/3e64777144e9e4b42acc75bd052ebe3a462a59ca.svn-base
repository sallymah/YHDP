/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */
package tw.com.hyweb.core.cp.online.loyalty.controller.batchupload;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;

/**
 * OfflineAdviceTest
 */
public class OfflineAdviceTest extends TestCase
{

    /**
     * Constructor
     * @param arg0
     */
    public OfflineAdviceTest(String arg0)
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
    public void testOfflineAdvice()  throws Exception
    {
        DefMainMock.initDefMainNoSocket("loyalty");
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml","del.TB_REDEEM_FUNC.xml","del.TB_REDEEM_FUNC_DTL.xml"});
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_REDEEM_FUNC.xml","TB_REDEEM_FUNC_DTL.xml","TB_BONUS.xml","TB_BONUS_BASE.xml"});
        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CARD_NO = '5433810004') ");
        UnitTestUtil.execSql("update TB_CUST_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CUST_ID = '7777777777777777777777777777777777777777') ");
        UnitTestUtil.execSql("update TB_ACCT_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (ACCT_ID = '7777777777777777777777777777777777777777') ");

        LMSContext ctx = UnitTestUtil.readToContext("offlineAdvice.xml");
        BaseController offlineAdvice = (BaseController) DefMain.getBean("OfflineAdvice");
        LMSContext ctxResp = (LMSContext) offlineAdvice.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());

        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
    }
}
