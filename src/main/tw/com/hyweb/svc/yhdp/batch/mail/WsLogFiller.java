package tw.com.hyweb.svc.yhdp.batch.mail;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;
import tw.com.hyweb.service.db.info.TbWsLogInfo;
import tw.com.hyweb.service.db.mgr.TbWsLogMgr;
import tw.com.hyweb.util.BatchUtils;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class WsLogFiller implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(WsLogFiller.class);
    private String condiction = "";
    String indexFlow = "0";
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        MailContext mctx = (MailContext)ctx;
        TbMailSettingInfo mailSettingInfo = mctx.getMailSetInfo();
        String dataFlow = mailSettingInfo.getDataFlow();
        if(dataFlow.indexOf(indexFlow) != -1)
        {
            return true;
        }
        return false;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        MailContext mctx = (MailContext)ctx;
        StringBuffer message_body = mctx.getMessageBody();
        TbWsLogInfo wsLogInfo = null;
        TbWsLogMgr WsLogMrg = new TbWsLogMgr(mctx.getConnection());
        int errDesCnt = 0;
        String workFg = "";
        String edate = "";
        Vector<TbWsLogInfo> result = new Vector<TbWsLogInfo>();
        String yesDay = DateUtil.addDate(ctx.getHostDate(), -1);
        String where = "SYNC_DATE = '" + yesDay + "'";
        String order = "SYNC_DATE, SYNC_TIME";
        where = where + " " + condiction;
        logger.debug(where);
        WsLogMrg.queryMultiple(where, result, order);
        message_body.append("<br><br>");
        message_body.append("<h3><font color=blue>【"+mctx.getSysName()+"DDIM同步明細檔】</font></h3><br>");
        message_body.append("<table cellspacing=\"0\" border=\"1\">");
        message_body.append("<tr bgcolor=\"#D3EBE7\"><B>");
        message_body.append("<td ALIGN=center>卡號</td>");
        message_body.append("<td ALIGN=center>身份證字號</td>");
        message_body.append("<td ALIGN=center>通知原因</td>");
        message_body.append("<td ALIGN=center>同步日期</td>");
        message_body.append("<td ALIGN=center>同步時間</td>");
        message_body.append("<td ALIGN=center>同步原因</td>");
        message_body.append("<td ALIGN=center>錯誤訊息</td>");
        message_body.append("<td ALIGN=center>同步發送次數</td></B></tr>");
        if(null != result && result.size() > 0)
        {
            logger.debug(result.size());
            String newProgName = "";
            String sysCfgValue = "";
            for(int i = 0; i < result.size(); i++)
            {
                wsLogInfo = result.get(i);
                if(null != wsLogInfo)
                {
                    String info = "";
                    String pcodeDesc  = getPcodeDesc(ctx, wsLogInfo.getPCode());
                    String resonCode =  resonDesc(wsLogInfo.getPCode());
                    int cnt = wsLogInfo.getResendCount() != null ? wsLogInfo.getResendCount().intValue() : 0;
                  /*  if(cnt >= 3)
                    {
                        message_body.append("<tr>");
                        message_body.append("<span style='background-color: rgb(255, 0, 0); color: rgb(255, 255, 255);'>");
                        message_body.append("<td ALIGN=center>"+wsLogInfo.getCardNo()+"</td>");
                        message_body.append("<td ALIGN=center>"+ wsLogInfo.getPersonId() +"</td>");
                        message_body.append("<td ALIGN=center>"+ resonCode +"</td>");
                        message_body.append("<td ALIGN=center> "+parserDateStr(true,wsLogInfo.getSyncDate())+" </td>");
                        message_body.append("<td ALIGN=center>"+parserDateStr(false,wsLogInfo.getSyncTime())+"</td>");
                        message_body.append("<td> "+pcodeDesc+" </td>");
                        message_body.append("<td>"+wsLogInfo.getErrDesc()+"</td>");
                        message_body.append("<td ALIGN=center>"+ cnt +"</td>");
                        message_body.append("</span>");
                        message_body.append("</tr>");
                    }
                    else
                    {*/
                        String pid;
                        try
                        {
                            pid = BatchUtils.decript(wsLogInfo.getPersonId());
                            message_body.append("<tr>");
                            message_body.append("<td ALIGN=center>"+wsLogInfo.getCardNo()+"</td>");
                            message_body.append("<td ALIGN=center>"+ pid +"</td>");
                            message_body.append("<td ALIGN=center>"+ resonCode +"</td>");
                            message_body.append("<td ALIGN=center> "+parserDateStr(true,wsLogInfo.getSyncDate())+" </td>");
                            message_body.append("<td ALIGN=center>"+parserDateStr(false,wsLogInfo.getSyncTime())+"</td>");
                            message_body.append("<td> "+pcodeDesc+" </td>");
                            message_body.append("<td>"+wsLogInfo.getErrDesc()+"</td>");
                            message_body.append("<td ALIGN=center>"+ cnt +"</td>");
                            message_body.append("</span>");
                            message_body.append("</tr>");
                        }
                        catch (Exception e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    /*}*/
                }
            }   
        }
        message_body.append("</table>");
    }
    
    public String resonDesc(String pcode)
    {
        String desc = "";
        switch (pcode)
        {
            case "7528"://退卡取消
            case "7247"://復卡
                desc = "復卡";
                break;
            case "9827":
            case "9757":
                desc = "遺失";
                break;
            case "9847" :
                desc = "損毀";
                break;
            case "9767":
            case "7527":
                desc = "停用";
                break;
        }
        return desc;
    }
    
    public String getPcodeDesc(LMSContext ctx, String pcode) throws SQLException
    {
        
        String sqlCmd = "select p_code_desc from tb_p_code_def where p_code = ?";
        Vector params = new Vector(); 
        params.add(pcode);
        Vector result = DbUtil.select(sqlCmd, params, ctx.getConnection());
        if(null != result && result.size() > 0)
        {
            Vector tmp = null;
            tmp = (Vector) result.get(0);
            if (tmp != null)
            {
                String desc = (String)tmp.get(0);
                return desc;
            }
        }
        return "";
    }
    
    public String parserDateStr(boolean isDate, String dateTime)
    {
        String parserStr = "";
        if(!StringUtil.isEmpty(dateTime))
        {
            if(isDate)
            {
                if(dateTime.length() == 8)
                {
                    parserStr = dateTime.substring(0, 4) + "/" + dateTime.substring(4, 6) + "/" + dateTime.substring(6, 8);
                }
            }
            else
            {
                if(dateTime.length() == 6)
                {
                    parserStr = dateTime.substring(0, 2) + ":" + dateTime.substring(2, 4) + ":" + dateTime.substring(4, 6);
                }
            }
        }
        return parserStr;
    }
    
    public void setCondiction(String condiction)
    {
        this.condiction = condiction;
    }
    
    public String getCondiction()
    {
        return this.condiction;
    }
    
    public String getIndexFlow()
    {
        return indexFlow;
    }

    public void setIndexFlow(String indexFlow)
    {
        this.indexFlow = indexFlow;
    }
}
