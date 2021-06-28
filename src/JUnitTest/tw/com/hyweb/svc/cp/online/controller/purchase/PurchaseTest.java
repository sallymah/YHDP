/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */
package tw.com.hyweb.svc.cp.online.controller.purchase;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;

/**
 * PurchaseTest
 */
public class PurchaseTest extends TestCase
{

    /**
     * @param arg0
     */
    public PurchaseTest(String arg0)
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

    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * 測試電子錢消費時抵扣其他點數。
     * edc參數下在時會有抵扣順序，當電子錢消費金額輸入100元時，需要照下載的順序點數來扣除。
     * 傳給主機時直接帶每一個點抵扣後的點數。
     * @throws Exception
     */
    public void testRedeemPointWithPurchase() throws Exception
    {
        DefMainMock.initDefMainNoSocket("loyalty");
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml","del.TB_REDEEM_FUNC.xml","del.TB_REDEEM_FUNC_DTL.xml"});
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_REDEEM_FUNC.xml","TB_REDEEM_FUNC_DTL.xml","TB_BONUS.xml","TB_BONUS_BASE.xml"});
        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CARD_NO = '5433810004') ");
        UnitTestUtil.execSql("update TB_CUST_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CUST_ID = '7777777777777777777777777777777777777777') ");
        UnitTestUtil.execSql("update TB_ACCT_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (ACCT_ID = '7777777777777777777777777777777777777777') ");

        LMSContext ctx = UnitTestUtil.readToContext("purchase.xml");
        normal(ctx);
        LMSContext ctxRefund = UnitTestUtil.makeVoidRefundCtx(ctx,false);
        refund(ctxRefund);

        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
    }

    /**
     * @throws Exception
     */
    private LMSContext normal(LMSContext ctxParam) throws Exception
    {
        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY=0,DB_BONUS_QTY=0,BAL_BONUS_QTY=1000 where (CARD_NO = '5433810004') ");

        BaseController redeem = (BaseController) DefMain.getBean("Purchase");
        LMSContext ctxResp = (LMSContext) redeem.process(ctxParam, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctxParam.getRcode(),ctxParam.checkRcode());
        String chipPointResp = ctxResp.getLMSMsg().getHexStr(LMSTag.ChipPointsResponseArea);
        assertEquals("99991231999912319999123199991231C11682070000000100000000010000000001000000000100",chipPointResp);
        return ctxResp;
    }

    /**
     * @throws Exception
     */
    private LMSContext refund(LMSContext ctxParam)throws Exception
    {
        BaseController redeemRefund = (BaseController) DefMain.getBean("PurchaseRefund");
        LMSContext ctxResp = (LMSContext) redeemRefund.process(ctxParam, UnitTestUtil.getContextSender());
        return ctxResp;
    }
}
