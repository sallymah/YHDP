/*
 * (版權及授權描述)
 *
 * Hyweb Technology Co., Ltd.All Rights Reserved.
 *
 * $History: $
 *
 */
package tw.com.hyweb.svc.cp.online.controller.reload;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.jpos.iso.packager.XMLPackager;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSProcCode;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;
import tw.com.hyweb.svc.cp.online.util.SVCProcCode;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReadUtil;

public class ReloadVoidTest extends TestCase
{

    public ReloadVoidTest(String arg0)
    {
        super(arg0);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        DefMainMock.initDefMainNoSocket("loyalty");
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_BAL_UPDATE_DTL.xml","del.TB_BAL_UPDATE.xml","del.TB_ONL_TXN_DTL.xml","del.TB_ONL_TXN_ERR.xml","del.TB_ONL_TXN.xml","del.TB_TXN_TRACE.xml","del.TB_RELOAD_FUNC.xml"});
    }

    protected void tearDown() throws Exception
    {
        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
        super.tearDown();
    }

    public void testProcessBiz() throws Exception
    {
        reloadTest7708();
    }

    private void reloadTest7708() throws Exception
    {
        LMSContext ctx = reloadTest7707();

        System.out.println("---------------------------------- start 7708 ---------------------------------------");

        ctx.setOnlTxnDtlInfo(null);

        BerTLV tlv = ctx.getLMSMsg();
        String invoNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);

        tlv.delByTag(LMSTag.LMSProcessingCode);
        tlv.addHexStr(LMSTag.LMSProcessingCode, String.valueOf(SVCProcCode.RELOAD_CASH_VOID));
        StringBuffer strb = new StringBuffer();
        strb.append("0200");
        strb.append("" + SVCProcCode.RELOAD_CASH);
        String amt = tlv.getHexStr(LMSTag.LoyaltyTransactionAmount);
        strb.append(ISOUtil.padLeft(amt == null ? "" : amt, 10, '0'));
        strb.append(tlv.getHexStr(LMSTag.LMSInvoiceNumber));
        tlv.addHexStr(LMSTag.OriginalDataArea, strb.toString());

        System.out.println("chip points balance" + tlv.getHexStr(LMSTag.ChipPointsBalanceArea));

        String chipPoints = "000000000000000000000000000000000010000099991231999912319999123199991231";
        tlv.delByTag(LMSTag.ChipPointsBalanceArea);
        tlv.addHexStr(LMSTag.ChipPointsBalanceArea, chipPoints);

        assertTrue(strb.toString(), 30 == strb.toString().length());
        ctx.setTimeTxInit(new Date());
        tlv.delByTag(LMSTag.LMSInvoiceNumber);
        UnitTestUtil.fillTrace(ctx);
        UnitTestUtil.fillLMSInvoiceNo(ctx);
        assertFalse(invoNo.equals(ctx.getLMSMsg().getHexStr(LMSTag.LMSInvoiceNumber)));

        BaseController reloadvoid = (BaseController) DefMain.getBean("ReloadVoid");
        reloadvoid.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:" + ctx.getRcode(), ctx.checkRcode());
    }

    private LMSContext reloadTest7707() throws Exception
    {
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_SYS_CONFIG.xml","TB_ACCT_BAL.xml","TB_ACCT.xml","TB_CARD_BAL.xml","TB_CARD_PRODUCT.xml","TB_CARD.xml","TB_CUST_BAL.xml","TB_CUST.xml","TB_MEMBER.xml","TB_MERCH.xml","TB_MERCH_GROUP_RLN.xml","TB_TERM.xml","TB_BONUS_BASE.xml","TB_BONUS.xml","TB_BONUS_DTL.xml","TB_RELOAD_FUNC.xml", "TB_RELOAD_FUNC_DTL.xml"});

        UnitTestUtil.execSql("update TB_CARD_BAL set CR_BONUS_QTY = 0 where CARD_NO='5433810004' and EXPIRY_DATE='20180101' and BONUS_ID='1100000774' and BONUS_SDATE='00000000' and BONUS_EDATE='99991231'");
        UnitTestUtil.execSql("delete from TB_BONUS_BASE where CARD_PRODUCT='7777' and BONUS_BASE='H'");


        String inputFile = UnitTestUtil.getTestDataDir() + File.separator + DefMain.getApName()+ File.separator + "reload_7707.xml";
        byte[] b = ReadUtil.readFile(inputFile);
        XMLPackager pkger = new XMLPackager();
        LMSContext ctx = (LMSContext) ((LMSContext) DefMain.getBean("Context")).clone();
        ctx.getIsoMsg().setPackager(pkger);
        ctx.getIsoMsg().unpack(b);
        ctx.setTimeTxInit(new Date());
        UnitTestUtil.autoFill(ctx);
        BaseController reload = (BaseController) DefMain.getBean("Reload");
        reload.process(ctx, UnitTestUtil.getContextSender());
        assertTrue("rcode:"+ctx.getRcode(),ctx.checkRcode());
        ctx.setLmsPcode(null);
        return ctx;
    }
}
