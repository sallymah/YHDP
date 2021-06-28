package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.util.BatchDbUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.util.BatchRcode;
import tw.com.hyweb.svc.yhdp.batch.util.BatchDateUtil;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;

public class CheckDueDay implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(CheckIsErrData.class);
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        if(ctrl != null)
        {
            BATCHContext bctx = (BATCHContext)ctx;
            TbMemberInfo memberInfo = bctx.getAcquireInfo();
            if(null == memberInfo)
            {
                getAcqMember(ctx);
                memberInfo = bctx.getAcquireInfo();
                if(null == memberInfo)
                {
                    ctx.setRcode(Rcode.INVALID_ACQUIRE);
                    ctx.setErrDesc("INVALID ACQUIRE. TB_MEMBER,TB_TERM record !=1");
                    throw new TxException(bctx.getErrDesc());
                }
            }
            String hostDate = bctx.getHostDate();
            String batchDate = bctx.getBatchDate();
            String dueDayStr = memberInfo.getDueDay();
            String procMonthStr = memberInfo.getProcMonth();
            if(!StringUtil.isEmpty(procMonthStr))
            {
                int procMonth = Integer.valueOf(procMonthStr);
                StringBuffer dueDate = new StringBuffer();
                StringBuffer startDate = new StringBuffer();
                StringBuffer endDate = new StringBuffer();
                int dueDay = Integer.valueOf(dueDayStr.substring(1));//trim first word
                int lastCurrentMontDay = BatchDateUtil.getLastMonthDay(batchDate, "yyyyMMdd");
                
                if(dueDay == 99)//last day
                {
                    dueDate.append(batchDate.substring(0, 6));//last n month
                    dueDate.append(ISOUtil.padLeft(String.valueOf(lastCurrentMontDay), 2, '0'));//last month day
                }
                else
                {
                    dueDate.append(batchDate.substring(0, 6));//last n month
                    dueDate.append(ISOUtil.padLeft(String.valueOf(dueDay), 2, '0'));//due day
                }
                
                //last N Month
                if(Integer.valueOf(batchDate) <= Integer.valueOf(dueDate.toString()))//系統日<=請款日,區間為上N個月~本月due Day
                {
                    String nMonth = DateUtil.addMonth(batchDate, 0 - procMonth);
                    startDate.append(nMonth.substring(0, 6));//last n month
                    startDate.append("01");
                    endDate.append(batchDate);
                }
                else {//系統日 > 請款日,本月 ~ 本月due Day
                    startDate.append(batchDate.substring(0, 6));
                    startDate.append("01");
                    endDate.append(batchDate);
                }
                logger.debug("procMonth:"+procMonthStr+"/dueDay:"+dueDayStr+ "/txnDate:"+hostDate+ "/startDate:" + startDate.toString() + "~ endDate:"+endDate.toString());
                
                if( (Integer.valueOf(hostDate) >= Integer.valueOf(startDate.toString()) )  &&
                     (Integer.valueOf(hostDate) <= Integer.valueOf(endDate.toString())) )
                {
                    //pass
                }
                else 
                {
                    bctx.setErrDesc("Over Due Day (txnDate:"+hostDate+ "/startDate:" + startDate.toString() + "~ endDate:"+endDate.toString() + ")");
                    bctx.setRcode(BatchRcode.EXPIRY_DUE);
                    throw new TxException(bctx.getErrDesc());
                }
            }
            else
            {
                logger.debug("not check due");
            }
        }
    }
    
    public void getAcqMember(LMSContext ctx) throws SQLException
    {
        BATCHContext bctx = (BATCHContext)ctx;
        String merchId = bctx.getLmsMerchantId();
        String terminalId = bctx.getLmsTerminalId();
        TbMemberInfo acquireInfo = BatchDbUtil.getAcquireInfo(ctx.getHostDate(),terminalId,merchId,((LMSContext) ctx).getConnection());
        bctx.setAcquireInfo(acquireInfo);
    }
}
