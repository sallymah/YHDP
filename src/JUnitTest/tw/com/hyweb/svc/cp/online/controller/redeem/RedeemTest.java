/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */
package tw.com.hyweb.svc.cp.online.controller.redeem;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;

/**
 * RedeemTest
 */
public class RedeemTest extends TestCase
{

    /**
     * @param arg0
     * @throws Exception
     */
    public RedeemTest(String arg0) throws Exception
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
//        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml","del.TB_REDEEM_FUNC.xml","del.TB_REDEEM_FUNC_DTL.xml"});
//        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_ACQ_DEF.xml","TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_REDEEM_FUNC.xml","TB_REDEEM_FUNC_DTL.xml","TB_BONUS.xml","TB_BONUS_BASE.xml"});
//        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CARD_NO = '5433810004') ");
//        UnitTestUtil.execSql("update TB_CUST_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CUST_ID = '7777777777777777777777777777777777777777') ");
//        UnitTestUtil.execSql("update TB_ACCT_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (ACCT_ID = '7777777777777777777777777777777777777777') ");

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
     * test online晶片點數抵扣 夠扣
     * @throws Exception
     */
    public void test7647ChipPoint() throws Exception
    {
        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CARD_NO = '5433810004') ");

        LMSContext ctx = UnitTestUtil.readToContext("redeem_7647ChipPoint.xml");
        BaseController redeem = (BaseController) DefMain.getBean("Purchase");

        LMSContext ctxResp = (LMSContext) redeem.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());
        String chipPointResp = ctxResp.getLMSMsg().getHexStr(LMSTag.ChipPointsResponseArea);
        assertEquals("99991231999912319999123199991231C11682070000000100000000010000000001000000000100",chipPointResp);

    }
    /**
     * test online晶片點券抵扣 夠扣
     * @throws Exception
     */
    public void test7647ChipCoupon() throws Exception
    {
        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CARD_NO = '5433810004') ");

        LMSContext ctx = UnitTestUtil.readToContext("redeem_7647ChipCoupon.xml");
        BaseController redeem = (BaseController) DefMain.getBean("Purchase");

        LMSContext ctxResp = (LMSContext) redeem.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());
        String chipCouponResp = ctxResp.getLMSMsg().getHexStr(LMSTag.ChipCouponResponseArea);
        assertEquals("820763000000070701017712310000000100",chipCouponResp);
    }
    /**
     * test online主機點數抵扣
     * @throws Exception
     */
    public void test7647HostPoint() throws Exception
    {
        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CARD_NO = '5433810004') ");

        LMSContext ctx = UnitTestUtil.readToContext("redeem_7647HostPoint.xml");
        BaseController redeem = (BaseController) DefMain.getBean("Purchase");

        LMSContext ctxResp = (LMSContext) redeem.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());
    }

    /**
     * @throws Exception
     */
    public void testDebug() throws Exception
    {
        LMSContext ctx = UnitTestUtil.readToContext("redeem_debug.xml");
        BaseController redeem = (BaseController) DefMain.getBean("Redeem");
        redeem.process(ctx, UnitTestUtil.getContextSender());
    }

    /**
     * 測試raw data
     * @throws Exception
     */
    public void testDebugRaw()throws Exception
    {
        String raw = "02002020000000C1004888888819045138383838313030323838383830303130303130303030320088FF21027207FF2203080304FF230400008101FF2509168810152000000001FF260420110901FF270420080101FF280412345678FF290720080227111111FF2B050000050000FF2E020901FF31020000FF450143FF570209010097FF21027207FF2203080304FF230400008101FF2509168810152000000001FF260420110901FF270420080101FF280412345678FF290720080227111111FF2B050000050000FF2E020901FF31020000FF450143FF57020901FF2F060304152726850043FF36051100000001FF3818000000260000000000000000000000000000000099991231FF3C050000001600";
        LMSContext ctx = (LMSContext)UnitTestUtil.readHexRawToContext(raw);
        assertNotNull(ctx);
    }
}
