/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */
package tw.com.hyweb.core.cp.online.loyalty.controller.inquiry;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.EmptyController;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;

/**
 * HostCouponInquiryTest
 */
public class HostCouponInquiryTest extends TestCase
{

    /**
     * Constructor
     * @param arg0
     */
    public HostCouponInquiryTest(String arg0)
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

    public void testHostCouponInq() throws Exception
    {
        DefMainMock.initDefMainNoSocket("loyalty");
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_CARD_BAL.xml","del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml","del.TB_REDEEM_FUNC.xml","del.TB_REDEEM_FUNC_DTL.xml"});
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_REDEEM_FUNC.xml","TB_REDEEM_FUNC_DTL.xml","TB_BONUS.xml","TB_BONUS_BASE.xml"});

        LMSContext ctx = UnitTestUtil.readToContext("InquiryBalance_AllCoupon.xml");
        EmptyController ctrler = (EmptyController) DefMain.getBean("InquiryBalance");
        LMSContext respctx = (LMSContext) ctrler.process(ctx, UnitTestUtil.getContextSender());
        String ff3f = respctx.getLMSMsg().getHexStr(LMSTag.HostCouponBalanceArea);
        assertNotNull(ff3f);
        String reqff64 = ctx.getLMSMsg().getHexStr(LMSTag.UploadDownloadController);
        String rspff64 = respctx.getLMSMsg().getHexStr(LMSTag.UploadDownloadController);
        assertNotNull(rspff64);
        assertFalse(reqff64.equals(rspff64));
        System.out.println("ff64:"+rspff64);
    }
}
