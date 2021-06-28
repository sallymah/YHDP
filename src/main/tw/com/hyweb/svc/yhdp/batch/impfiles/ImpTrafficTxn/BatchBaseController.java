package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.Rcode;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextSender;
import tw.com.hyweb.online.TxLogger;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.controller.base.BaseController;
import tw.com.hyweb.svc.yhdp.online.controller.base.FillDataAction;
import tw.com.hyweb.svc.yhdp.online.logger.BaseTxLogger;

public class BatchBaseController extends BaseController
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(BatchBaseController.class);
    private TxLogger txLogger;
    private FillDataAction fillDataAction = new FillDataAction();//given default value
    private List<IBizAction> bizAction;
    
    @Override
    public Context process(Context ctxReq, IContextSender sender)
    {

        boolean isTxSuccessful=false;
        boolean isRespTxSuccessful=false;

        LMSContext respCtx = null;
        Connection conn = null;
        try
        {
            LMSContext ctx = (LMSContext)ctxReq;
            BATCHContext bctx = (BATCHContext)ctx;
            if (ctx==null)
            {
                throw new IllegalArgumentException("context request is null");
            }

            try
            {
                if (logger.isDebugEnabled())
                {
                    BerTLV tlv = ctx.getLMSMsg();
                    String outputStr =tlv!=null?tlv.toString():"NULL";
                    outputStr = outputStr.replaceFirst(":", "\n");
                    outputStr = outputStr.replaceAll(",", ",\n");
                    logger.debug("\n"+outputStr+"\n");
                }

                Date date = ctx.getTimeTxInit();
                conn = ctx.getConnection();
                
                if (!ctx.checkRcode())
                {
                    throw new TxException("context error:"+ctx.getRcode());
                }

                validate(ctx);// 根據XML設定Validator檢查Issuer,Terminal,Merchant等
                if (ctx.checkRcode())
                {
                    beforeBiz(ctx);
                    if (ctx.checkRcode())
                    {
                        respCtx = processBiz(ctx);

                        if(ctx.checkRcode())
                        {
                            isTxSuccessful=true;
                        }

                        if(isTxSuccessful)
                        {
                            afterBiz(respCtx);
                            if(respCtx.checkRcode())
                            {
                                isRespTxSuccessful=true;
                            }
                        }

                        if(isTxSuccessful && isRespTxSuccessful)
                        {
                            getTxLogger().insert(respCtx);// insert txLogger
                        }
                    }
                }
            }
            catch (SQLException sqle)
            {
                logger.error("", sqle);
                ctx.setRcode(Rcode.SQL_FAIL);
                ctx.setException(sqle);
                isTxSuccessful = false;
                if(respCtx != null)
                {
                    respCtx.setRcode(Rcode.SQL_FAIL);
                    respCtx.setException(sqle);
                }
                rollBack(conn);
            }
            catch (TxException txe)
            {
                ctx.setException(txe);
                isTxSuccessful = false;
                if(!isRespTxSuccessful && respCtx != null)
                {
                    logger.warn("respRcode:"+respCtx.getRcode(), txe);
                }
                else
                {
                    logger.warn("rcode:"+ctx.getRcode(), txe);
                }
                rollBack(conn);
            }
            catch (Exception e)
            {
                logger.error("", e);
                isTxSuccessful = false;
                ctx.setRcode(Rcode.FAIL);
                ctx.setException(e);
                rollBack(conn);
            }

            try
            {
                logger.debug("isTxSuccessful:"+isTxSuccessful);
                if (!isTxSuccessful)
                {// 不成功的交易
                    respCtx = fillErrorRespMsg(ctx);
                    doRespErrorAction(respCtx);
                    ((BaseTxLogger)getTxLogger()).insertOnlTxnErr(ctx);
                }
                else if(!isRespTxSuccessful)
                {//不成功的交易
                    ((BaseTxLogger)getTxLogger()).insertOnlTxnErr(respCtx);
                    ctx.setRcode(respCtx.getRcode());
                    respCtx = fillErrorRespMsg(ctx);
                    doRespErrorAction(respCtx);
                }
                               
                if (conn!=null)
                {
                    logger.debug("DB commit.");
                    conn.commit();
                }
            }
            catch(Exception e)
            {
                ctx.setException(e);
                logger.error("", e);
                rollBack(conn);
            }
        }
        catch (Error error)
        {
            logger.fatal(ctxReq.toString(),error);
            throw error;
        }
        return respCtx;
    }

    /**
     * @param ctx
     */
    public LMSContext processBiz(LMSContext ctx) throws SQLException, TxException
    {
        int bizActionNum = (bizAction != null)?bizAction.size():0;
        for(int i = 0; i < bizActionNum; i++)
        {
            if(bizAction.get(i).doActionTest(ctx))
            {
                bizAction.get(i).doAction(this, ctx);
            }
        }
        return fillResponse(ctx);
    }
    
    /**
     * 若沒有設定TxLogger則使用預設TxLogger
     * @return 傳回 txLogger。
     */
    @Override
    public TxLogger getTxLogger()
    {
        return this.txLogger;
    }
    
    /**
     * 若沒有設定TxLogger則使用預設TxLogger
     * @return 傳回 txLogger。
     */
    @Override
    public void setTxLogger(TxLogger txLogger)
    {
        this.txLogger = txLogger;
    }
    
    /**
     * @param ctx
     * @return response LMSContext
     */
    protected LMSContext fillErrorRespMsg(LMSContext ctx)
    {
        return fillDataAction.fillErrorRespMsg(ctx);
    }
   
    /**
     * @param conn
     */
    private void rollBack(Connection conn)
    {
        try
        {
            //ThigBContext tctx = (ThigBContext)ctx; 
            if (conn!=null)
            {
                conn.rollback();
            }
        }
        catch (SQLException e1)
        {
            logger.error(e1);
        }
    }  
    
    /**
     * @return the bizAction
     */
    public List<IBizAction> getBizAction()
    {
        return bizAction;
    }

    /**
     * @param bizAction the bizAction to set
     */
    public void setBizAction(List<IBizAction> bizAction)
    {
        this.bizAction = bizAction;
    }
}
