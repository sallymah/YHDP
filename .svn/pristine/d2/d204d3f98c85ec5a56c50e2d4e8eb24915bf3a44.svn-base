/*
 * Version: 1.0.0
 * Date: 2007/3/7
 *
 * (版權及授權描述)
 * Copyright 2007(C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * @Author: andyson
 *
 * ***********************************************
 */
package tw.com.hyweb.core.cp.online.loyalty.controller.pointreload;

import java.util.Vector;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;

/**
 * AppointReloadTest
 */
public class AppointReloadTest extends TestCase
{
    /**
     * @param arg0
     */
    public AppointReloadTest(String arg0)
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
    public void testAppointReload()  throws Exception
    {
        DefMainMock.initDefMainNoSocket("loyalty");
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_CARD_BAL.xml","del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml","del.TB_REDEEM_FUNC.xml","del.TB_REDEEM_FUNC_DTL.xml","del.TB_APPOINT_RELOAD.xml","del.TB_APPOINT_RELOAD_DTL.xml"});
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_REDEEM_FUNC.xml","TB_REDEEM_FUNC_DTL.xml","TB_BONUS.xml","TB_BONUS_BASE.xml","TB_APPOINT_RELOAD.xml","TB_APPOINT_RELOAD_DTL.xml"});

        LMSContext ctx = UnitTestUtil.readToContext("AppointReload.xml");
        BaseController appointReload = (BaseController) DefMain.getBean("AppointReload");
        LMSContext ctxResp = (LMSContext) appointReload.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());

        Vector vec = UnitTestUtil.select("select DW_LMS_INVOICE_NO from TB_APPOINT_RELOAD where BALANCE_TYPE = 'C' and BALANCE_ID = '5433810004' and EXPIRY_DATE = '21080101'");
        assertEquals(ctx.getLMSMsg().getHexStr(LMSTag.LMSInvoiceNumber),((String)((Vector)vec.get(0)).get(0)));

        vec = UnitTestUtil.select("Select CR_BONUS_QTY From TB_CARD_BAL Where CARD_NO='5433810004' and EXPIRY_DATE='21080101' and BONUS_ID='1100000771'");
        assertTrue(((Number)((Vector)vec.get(0)).get(0)).doubleValue()>0);

        vec = UnitTestUtil.select("Select CR_BONUS_QTY From TB_CARD_BAL Where CARD_NO='5433810004' and EXPIRY_DATE='21080101' and BONUS_ID='1100000772'");
        assertTrue(((Number)((Vector)vec.get(0)).get(0)).doubleValue()>0);

        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
    }
}
