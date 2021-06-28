package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.controller.IBizFiller;
import tw.com.hyweb.svc.yhdp.online.controller.base.BaseController;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.LmsDbUtil;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;

public class OriginalTxnDataFiller implements IBizFiller, IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(OriginalTxnDataFiller.class);
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        doBiz(ctx);
        
        if(ctx.getOrigTxnInfo() != null && !ctx.getOrigTxnInfo().getStatus().equals("9"))
        {
            int[] allowCode = ((BaseController)ctrl).getAcceptLmsProcCodeArray();
            checkReversalRefund(allowCode, ctx.getOrigTxnInfo(),ctx);
        }
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IChipBalanceFiller#fillChipBal(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doBiz(LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doBiz(LMSContext ctx)");
        BerTLV tlv = ctx.getLMSMsg();
        String cardNo = ctx.getCardInfo().getCardNo();
        String expiryDate = ctx.getCardInfo().getExpiryDate();
        String lmsInvoiceNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
           
        /* 2.找原始交易資料*/
        ctx.setOrigTxnInfo(LmsDbUtil.getTbOnlTxn(cardNo, expiryDate, lmsInvoiceNo,ctx.getConnection()));
    }
    
    /**
     * 檢查交易和原交易的值
     * 1.pcode是否和原交易相同
     * 2.原交易狀態是否正確
     * @param allowCode
     * @param onlTxnInfo
     * @param ctx
     * @throws TxException
     */
    public static void checkReversalRefund(int[] allowCode,TbOnlTxnInfo onlTxnInfo, LMSContext ctx) throws TxException
    {
        int origPCode = Integer.parseInt(onlTxnInfo.getPCode());
        boolean isFind = false;
        for (int i=0;i<allowCode.length;i++)
        {
            if (origPCode == allowCode[i])
            {
                isFind = true;
                break;
            }
        }
        if (!isFind)
        {
            ctx.setRcode(Rcode.S1801);
            throw new TxException("cannot find original PCode");
        }
        
        if (!onlTxnInfo.getStatus().equals("1"))
        {
            ctx.setRcode(Rcode.S1802);
            throw new TxException("onlTxnInfo.getStatus()!=1");
        }
    }
}