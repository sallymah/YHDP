package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.Rcode;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextSender;
import tw.com.hyweb.online.IMsgData;
import tw.com.hyweb.online.TxLogger;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.base.BaseController;
import tw.com.hyweb.svc.yhdp.online.controller.base.FillDataAction;
import tw.com.hyweb.svc.yhdp.online.logger.BaseTxLogger;
import tw.com.hyweb.svc.yhdp.online.logger.TxLoggerOfflineAdvice;
import tw.com.hyweb.svc.yhdp.online.util.tag.OfflineUploadTxnInfo;
import tw.com.hyweb.util.DisposeUtil;

/**
 * BaseController:
 *
 * @author user
 */
public class BatchBaseController extends BaseController
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(BatchBaseController.class);
    private TxLogger txLogger;
    private FillDataAction fillDataAction = new FillDataAction();//given default value
    /*
     * (non-Javadoc)
     *
     * @see tw.com.hyweb.online.AbstractController#process(tw.com.hyweb.online.Context,
     *      tw.com.hyweb.online.IContextSender)
     */
  /*  @Override
    public Context process(Context ctxReq, IContextSender sender)
    {

        boolean isTxSuccessful=false;
        boolean isRespTxSuccessful=false;

        LMSContext respCtx = null;
        Connection conn = null;
        try
        {
            LMSContext ctx = (LMSContext)ctxReq;
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

                Date date = ctx.getTimeTxExec();
                if (ctx.getTimeoutValue() > 0 && (System.currentTimeMillis() - date.getTime() > ctx.getTimeoutValue()))
                {
                    logger.warn("the message is timeout.");

                    return respCtx;
                }
                conn = ctx.getConnection();
//                conn = DBService.getDBService().getConnection(LMSContext.DEFAULT_DB_USER);
//                ctx.setConnection(conn);

                if (!ctx.checkRcode())
                {
                    throw new TxException("context error:"+ctx.getRcode());
                }

                validate(ctx);// 根據XML設定Validator檢查Issuer,Terminal,Merchant等
                logger.info("ctx.getRcode():"+ctx.getRcode());
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
                logger.error("SQLException sqle:", sqle);
                ctx.setRcode(Rcode.SQL_FAIL);
                isTxSuccessful = false;
                if(respCtx != null)
                {
                    respCtx.setRcode(Rcode.SQL_FAIL);
                }
                rollBack(conn);
            }
            catch (TxException txe)
            {
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
                logger.error("Exception e:", e);
                isTxSuccessful = false;
                ctx.setRcode(Rcode.FAIL);
                rollBack(conn);
            }

            try
            {
                OfflineUploadTxnInfo uploadTxnInfo = ctx.getOfflineUploadTxnInfo();
                if (!isTxSuccessful)
                {// 不成功的交易
                    respCtx = fillErrorRespMsg(ctx);
                    doRespErrorAction(respCtx);
                    if(null != uploadTxnInfo)
                    {
                        ((TxLoggerOfflineAdvice)getTxLogger()).insertOfflineOnlTxnErr(respCtx);    
                    }
                    else
                    {
                        ((TxLoggerOfflineAdvice)getTxLogger()).insertOnlTxnErr(respCtx);
                    }
                    
                }
                else if(!isRespTxSuccessful)
                {//不成功的交易
                    
                    if(null != uploadTxnInfo)
                    {
                        ((TxLoggerOfflineAdvice)getTxLogger()).insertOfflineOnlTxnErr(respCtx);    
                    }
                    else
                    {
                        ((TxLoggerOfflineAdvice)getTxLogger()).insertOnlTxnErr(respCtx);
                    }
                    
                    ctx.setRcode(respCtx.getRcode());
                }
            }
            catch(Exception e)
            {
                logger.error("", e);
                rollBack(conn);
            }
        }
        catch (Error error)
        {
            logger.fatal(ctxReq.toString(),error);
            throw error;
        }
        finally
        {
            //DisposeUtil.close(conn);
        }
        return respCtx;
    }
    */
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
               /* if (ctx.getTimeoutValue() > 0 && (System.currentTimeMillis() - date.getTime() > ctx.getTimeoutValue()))
                {
                    logger.warn("the message is timeout.");

                    return respCtx;
                }*/
                conn = ctx.getConnection();
//                conn = DBService.getDBService().getConnection(LMSContext.DEFAULT_DB_USER);
//                ctx.setConnection(conn);

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
                    if(bctx.getDongleRespCode().equals("0000") || bctx.getDongleRespCode().equals("00"))
                    {
                        logger.warn("respRcode:"+respCtx.getRcode(), txe);
                    }
                    else
                    {
                        logger.debug("dongle respRcode:"+respCtx.getRcode());//dongle踢錯不需印太多exception
                    }
                }
                else
                {
                    
                    if(bctx.getDongleRespCode().equals("0000") || bctx.getDongleRespCode().equals("00"))
                    {
                        logger.warn("rcode:"+ctx.getRcode(), txe);
                    }
                    else
                    {
                        logger.debug("dongle rcode:"+ctx.getRcode());//dongle踢錯不需印太多exception
                    }
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
}
