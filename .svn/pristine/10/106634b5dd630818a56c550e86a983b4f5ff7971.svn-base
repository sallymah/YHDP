package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.controller.base.BaseController;
import tw.com.hyweb.svc.yhdp.online.controller.base.BizActions;
import tw.com.hyweb.svc.yhdp.online.logger.TxLoggerOfflineAdvice;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.tag.OfflineUploadTxnInfo;

public class OfflineAdviceProc extends BizActions
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(OfflineAdviceProc.class);
    private static final int FF3C_LEN = 140;
    private List<IBizAction> bizAction;

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doActionTest(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {        
        String uploadTxnData = ctx.getLMSMsg().getHexStr(LMSTag.UploadTransactionData);

        if(null != uploadTxnData && uploadTxnData.length() > 0)
        {
            if(uploadTxnData.length() % FF3C_LEN == 0)
            {               
                int idx = 0;
                int txnCount = uploadTxnData.length() / FF3C_LEN;
                logger.debug("txn Count:"+txnCount);
                
                for(int idx1 = 0; idx1 < txnCount;idx1++)
                {
                    String strTmp = uploadTxnData.substring(idx,idx+=FF3C_LEN);
                    
                    OfflineUploadTxnInfo uploadTxnInfo = new OfflineUploadTxnInfo(strTmp);
                    logger.debug("");
                    logger.debug("ff3c pCode:" + uploadTxnInfo.getPcode());
                    ctx.setOfflineUploadTxnInfo(uploadTxnInfo);              
                    cleanContext(ctx);
                    try {
                        processBiz(ctrl, ctx);
                    }
                    catch (SQLException sql)
                    {
                        ctx.setRcode(uploadTxnInfo.getRcode());
                        throw sql;
                    }
                    catch (TxException txe)
                    {
                        ctx.setRcode(uploadTxnInfo.getRcode());
                        throw txe;
                    }
                    catch (Exception e)
                    {
                        ctx.setRcode(uploadTxnInfo.getRcode());
                        throw e;
                    }
                }
            }
            else
            {
                logger.warn("FF3C UploadTransactionData length:" + uploadTxnData.length() +" mod :"+uploadTxnData.length() % FF3C_LEN);
            }
        }
    }
   
    /**
     * @param ctx
     */
    public LMSContext processBiz(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        int bizActionNum = (bizAction != null)?bizAction.size():0;
        for(int i = 0; i < bizActionNum; i++)
        {
            if(bizAction.get(i).doActionTest(ctx))
            {
                bizAction.get(i).doAction(ctrl, ctx);
            }
        }
        return fillResponse(ctx);
    }

    /**
     * @param ctx
     * @return response LMSContext
     * @throws SQLException
     * @throws TxException
     */
    protected LMSContext fillResponse(LMSContext ctx) throws SQLException ,TxException
    {
        return ctx;
    }    

    public void cleanContext(LMSContext ctx)
    {
        ctx.setChipCounterAward(null);
        ctx.setChipCouponAward(null);
        ctx.setChipPaperCouponAward(null);
        ctx.setChipCounterRedeem(null);
        ctx.setChipCouponRedeem(null);
        ctx.setChipCounterBal(null);
        ctx.setChipCouponBal(null);
        ctx.setChipBonusId(null);
        ctx.setCardInfo(null);
        ctx.setCardProduct(null);
        ctx.setIssuerInfo(null);
    }
    /**
     * @return bizAction
     */
    public List<IBizAction> getBizAction()
    {
        return bizAction;
    }

    /**
     * @param bizAction 的設定的 bizAction
     */
    public void setBizAction(List<IBizAction> bizAction)
    {
        this.bizAction = bizAction;
    }
}
