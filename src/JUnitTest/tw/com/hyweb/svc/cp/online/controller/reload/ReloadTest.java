/*
 * (版權及授權描述)
 *
 * Hyweb Technology Co., Ltd.All Rights Reserved.
 *
 * $History: $
 *
 */
package tw.com.hyweb.svc.cp.online.controller.reload;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.CacheTbSysConfig;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.iso.field.BerTLVMsg;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;
import tw.com.hyweb.svc.cp.online.SVCContext;
import tw.com.hyweb.svc.cp.online.util.SVCTxCode;
import tw.com.hyweb.util.ISOUtil;

public class ReloadTest extends TestCase
{
    public ReloadTest(String arg0)
    {
        super(arg0);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        DefMainMock.initDefMainNoSocket("loyalty");
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml","del.TB_RELOAD_FUNC.xml","del.TB_RELOAD_FUNC_DTL.xml"});
    }

    protected void tearDown() throws Exception
    {
        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
        super.tearDown();
    }

    public void testReload7707() throws Exception
    {
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_SYS_CONFIG.xml","TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_BONUS_BASE.xml","TB_BONUS.xml","TB_BONUS_DTL.xml","TB_RELOAD_FUNC.xml","TB_RELOAD_FUNC_DTL.xml"});

        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY = 0 where CARD_NO='5433810004' and EXPIRY_DATE='21080101' and BONUS_ID='1100000774' and BONUS_SDATE='00000000' and BONUS_EDATE='99991231'");
        UnitTestUtil.execSql("delete from TB_BONUS_BASE where CARD_PRODUCT='7777' and BONUS_BASE='H'");

        LMSContext ctx = UnitTestUtil.readToContext("reload_7707.xml");  // 現金加值

        CacheTbSysConfig.getInstance().reload(ctx.getConnection());
        BaseController reload = (BaseController) DefMain.getBean("Reload");

        SVCContext respCtx = (SVCContext) reload.process(ctx, UnitTestUtil.getContextSender());

        assertTrue("rcode:"+ctx.getRcode(), ctx.checkRcode());

        BerTLVMsg tlvMsg = respCtx.getLMSMsg().find(LMSTag.ChipPointsResponseArea);
        assertNotNull(tlvMsg);

        System.out.println(ISOUtil.hexString(tlvMsg.getValue()));
        System.out.println("bonus reload = " + ISOUtil.hexString(tlvMsg.getValue()).indexOf(String.valueOf(SVCTxCode.BONUS_RELOAD)));
    }

    public void testReload7717() throws Exception
    {
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_SYS_CONFIG.xml","TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_BONUS_BASE.xml","TB_BONUS.xml","TB_BONUS_DTL.xml","TB_RELOAD_FUNC.xml","TB_RELOAD_FUNC_DTL.xml"});

        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY = 0 where CARD_NO='5433810004' and EXPIRY_DATE='21080101' and BONUS_ID='1100000774' and BONUS_SDATE='00000000' and BONUS_EDATE='99991231'");
        UnitTestUtil.execSql("delete from TB_BONUS_BASE where CARD_PRODUCT='7777' and BONUS_BASE='H'");

        LMSContext ctx = UnitTestUtil.readToContext("reload_7717.xml");  // 現金加值

        CacheTbSysConfig.getInstance().reload(ctx.getConnection());
        BaseController reload = (BaseController) DefMain.getBean("Reload");

        SVCContext respCtx = (SVCContext) reload.process(ctx, UnitTestUtil.getContextSender());

        assertTrue("rcode:"+ctx.getRcode(), ctx.checkRcode());

        BerTLVMsg tlvMsg = respCtx.getLMSMsg().find(LMSTag.ChipPointsResponseArea);
        assertNotNull(tlvMsg);

        System.out.println(ISOUtil.hexString(tlvMsg.getValue()));
        System.out.println("bonus reload = " + ISOUtil.hexString(tlvMsg.getValue()).indexOf(String.valueOf(SVCTxCode.BONUS_RELOAD)));
    }
}
