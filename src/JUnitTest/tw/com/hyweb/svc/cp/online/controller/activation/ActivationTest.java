/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */

package tw.com.hyweb.svc.cp.online.controller.activation;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.CacheTbSysConfig;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BonusInfo;
import tw.com.hyweb.core.cp.online.loyalty.util.AmountUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.iso.field.BerTLVMsg;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;
import tw.com.hyweb.svc.cp.online.util.SVCTxCode;
import tw.com.hyweb.util.ISOUtil;

/**
 * CalcAmtTest
 */
public class ActivationTest extends TestCase
{

    /**
     * @param arg0
     */
    public ActivationTest(String arg0)
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
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml",});
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
     * {@link tw.com.hyweb.svc.cp.online.controller.activation.CalcAmt#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.core.cp.online.loyalty.LMSContext)} 的測試方法。
     * @throws Exception
     */
    public void testActivation() throws Exception
    {
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_SYS_CONFIG.xml","TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_BONUS_BASE.xml","TB_BONUS.xml","TB_BONUS_DTL.xml"});
        UnitTestUtil.execSql("update TB_CARD set status = '2', preload_dw_date='' where CARD_NO='5433810004' and EXPIRY_DATE='21080101' ");
        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY = 0 where CARD_NO='5433810004' and EXPIRY_DATE='21080101' and BONUS_ID='0000000000' and BONUS_SDATE='00000000' and BONUS_EDATE='99991231'");
        UnitTestUtil.execSql("delete from TB_BONUS_BASE where CARD_PRODUCT='7777' and BONUS_BASE='H'");

        LMSContext ctx = UnitTestUtil.readToContext("activation.xml");
        CacheTbSysConfig.getInstance().reload(ctx.getConnection());
        BaseController sale = (BaseController) DefMain.getBean("Activation");
        LMSContext respCtx = (LMSContext) sale.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());
        assertTrue(AmountUtil.amtValue(respCtx.getIsoMsg().getString(4))==AmountUtil.amtValue(respCtx.getLMSMsg().getHexStr(LMSTag.LoyaltyTransactionAmount)));
        BonusInfo cardFeeInfo = (BonusInfo) ctx.getSrcMsgProperties().get(Constants.CARD_FEE_INFO);
        BonusInfo preloadAmtInfo = (BonusInfo) ctx.getSrcMsgProperties().get(Constants.PRELOAD_AMT_INFO);
        assertTrue(AmountUtil.amtValue(respCtx.getIsoMsg().getString(4))==cardFeeInfo.getQty()+preloadAmtInfo.getQty());
        BerTLVMsg tlvMsg = respCtx.getLMSMsg().find(LMSTag.ChipPointsResponseArea);
        assertNotNull(tlvMsg);
        assertTrue(ISOUtil.hexString(tlvMsg.getValue()).indexOf(String.valueOf(SVCTxCode.PRELOAD_AMT))>=0);

        System.out.println("cardFeeInfo:"+cardFeeInfo.getQty());
        System.out.println("preloadAmtInfo:"+preloadAmtInfo.getQty());
        //reversal
        UnitTestUtil.cleanCtx(ctx);
        ctx = UnitTestUtil.makeReversalCtx(ctx);
        BaseController activationReversal = (BaseController) DefMain.getBean("ActivationReversal");
        activationReversal.process(ctx, UnitTestUtil.getContextSender());
        assertTrue(ctx.checkRcode());
    }

}
