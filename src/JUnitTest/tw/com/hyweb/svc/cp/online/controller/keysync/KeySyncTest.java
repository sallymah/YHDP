/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */

package tw.com.hyweb.svc.cp.online.controller.keysync;

import java.util.Vector;

import junit.framework.TestCase;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.controller.base.BaseController;
import tw.com.hyweb.core.cp.online.loyalty.util.DbUnitUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.LMSTag;
import tw.com.hyweb.core.cp.online.loyalty.util.UnitTestUtil;
import tw.com.hyweb.online.DefMain;
import tw.com.hyweb.online.DefMainMock;
import tw.com.hyweb.svc.cp.online.util.tag.TagDF60;

/**
 * KeySyncTest
 */
public class KeySyncTest extends TestCase
{

    /**
     * @param arg0
     */
    public KeySyncTest(String arg0)
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
        DefMain main = (DefMain) DefMain.getBean(DefMain.BEAN_MAIN);
        main.shutdownService();
        super.tearDown();
    }

    /**
     *
     * @throws Exception
     */
    public void testKeySync() throws Exception
    {
        DbUnitUtil.deleteDbFromFilesFields(new String[]{"del.TB_TERM.xml","del.TB_KEK_KEY_VERSION.xml"});
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_TERM.xml","TB_KEK_KEY_VERSION.xml"});
        LMSContext ctx = UnitTestUtil.readToContext("keysync.xml");
        BaseController keysync = (BaseController) DefMain.getBean("KeySync");
        LMSContext respCtx = (LMSContext) keysync.process(ctx, UnitTestUtil.getContextSender());
        TagDF60 df60 = new TagDF60();
        df60.parse(ctx.getLMSMsg().getHexStr(LMSTag.DWK_DATA));
        assertNotNull(df60.getHexVersion());

        Vector vec = UnitTestUtil.select("select KEY_VERSION from TB_TERM where MERCH_ID='"+ctx.getLmsMerchantId()+"' and TERM_ID='"+ctx.getLmsTerminalId()+"' ");
        assertNotNull(((Vector)vec.get(0)).get(0));
        assertNotNull(respCtx.getLMSMsg().getHexStr(LMSTag.DWK_DATA));
        df60.parse(respCtx.getLMSMsg().getHexStr(LMSTag.DWK_DATA));
        System.out.println(df60);
    }

    /**
     * @throws Exception
     */
    public void testKeySyncReversal() throws Exception
    {
        DbUnitUtil.refreshDbFromFiles(new String[]{"TB_TERM.xml","TB_KEK_KEY_VERSION.xml"});
        LMSContext ctx = UnitTestUtil.readToContext("keysync.xml");
        UnitTestUtil.execSql("insert into TB_MANAG_TXN (MERCH_ID,TERM_ID,P_CODE,ISO_FIELD_3,EDC_DATE,EDC_TIME,RESP_CODE,ONL_RCODE,STATUS,PAR_MON,PAR_DAY,FIELD2) values('"+ctx.getLmsMerchantId()+"','"+ctx.getLmsTerminalId()+"','7377','0100','"+ctx.getTermTxnDate()+"','"+ctx.getTermTxnTime()+"','00','0000','1','"+ctx.getParMon()+"','"+ctx.getParDay()+"','"+ctx.getLMSMsg().getHexStr(LMSTag.LMSInvoiceNumber)+"')");
        ctx.getIsoMsg().setMTI("0400");
        BaseController keysync = (BaseController) DefMain.getBean("KeySyncReversal");
        LMSContext respCtx = (LMSContext) keysync.process(ctx, UnitTestUtil.getContextSender());

    }
}
