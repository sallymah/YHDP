package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;


import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.mon.IMonService;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.online.IContextSender;
import tw.com.hyweb.online.IMsgData;
import tw.com.hyweb.online.IStatisticWriter;

/**
 * Mediator:
 *
 * @author user
 */
public class Mediator implements IContextListener
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(Mediator.class);
    private List ctrlerList;
    private IContextSender ctxSender;
    private IMonService monService;

    /**
     * 用來列印時間統計訊息,如果沒有instance則不需要做統計訊息處理
     */
    private IStatisticWriter statisticWriter;
    /**
     * constructor
     */
    public Mediator()
    {
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.online.IContextListener#onMessage(tw.com.hyweb.online.Context)
     */
    public Context onMessage(Context ctx)
    {

        Context ret = null;
        if ((this.ctrlerList == null) || (this.ctrlerList.size() == 0))
        {
            return ret;
        }
        int ctrlSize = this.ctrlerList.size();
        //String inType = ctx.getIncomingType()==IMsgData.TYPE_JMS?"JMS":"TCP";
        //logger.debug("Process a message from "+inType+" uuid:"+ctx.uuid());
        AbstractController ctrler=null;
        for (int i = 0; i < ctrlSize; i++)
        {
            ctrler = (AbstractController) ctrlerList.get(i);
            ret = ctrler.processCtx(ctx,this.ctxSender);
            if (null!=ret)
            {
                break;
            }
            int state = ctx.getState();
            if (state==IMsgData.STATE_TIMEOUT)
            {
                logger.debug("state timeout");
                break;
            }
            if (state==IMsgData.STATE_STOP)
            {
                logger.debug("state stop");
                break;
            }
            if (i == (ctrlSize - 1))
            { //沒有任何controller處理這各訊息
                logger.warn("No controller process the message :\n "+ctx);
                ctx.setRcode("1183");
                ret = ctx;
            }
        }
        if (null!=monService)
        {
            monService.toMon(ctx);
        }
        if (null!=statisticWriter)
        {
            statisticWriter.writeRecord(ctx);
        }
        return ret;
    }

    /**
     * @param ctrlerList 要設定的 ctrlerList。
     */
    public void setCtrlerList(List ctrlerList)
    {
        this.ctrlerList = ctrlerList;
    }

    /**
     * @return 傳回 ctrlerList。
     */
    public List getCtrlerList()
    {
        return this.ctrlerList;
    }

//    /**
//     * @param ctx - ISO8583 Context
//     * @param ctrler - business controller
//     * @return is process ok
//     */
//    protected boolean processRoute(Context ctx, AbstractController ctrler)
//    {
//        return false;
//    }

    /**
     * @return 傳回 ctxSender。
     */
    public IContextSender getCtxSender()
    {
        return ctxSender;
    }

    /**
     * @param ctxSender 要設定的 ctxSender。
     */
    public void setCtxSender(IContextSender ctxSender)
    {
        this.ctxSender = ctxSender;
    }

    /**
     * @return statisticWriter
     */
    public IStatisticWriter getStatisticWriter()
    {
        return statisticWriter;
    }

    /**
     * @param statisticWriter 的設定的 statisticWriter
     */
    public void setStatisticWriter(IStatisticWriter statisticWriter)
    {
        this.statisticWriter = statisticWriter;
    }

    /**
     * @return monService
     */
    public IMonService getMonService()
    {
        return this.monService;
    }

    /**
     * @param monService 的設定的 monService
     */
    public void setMonService(IMonService monService)
    {
        this.monService = monService;
    }

}
