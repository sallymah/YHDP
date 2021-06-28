package tw.com.hyweb.svc.yhdp.batch.mail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;
import tw.com.hyweb.service.db.info.TbProgramNameMapInfo;
import tw.com.hyweb.service.db.mgr.TbBatchResultMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class MonitorBatch implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(BatchResultFiller.class);
    Map<String,String> programNmeMap = new HashMap<String,String>();
    ArrayList<TbProgramNameMapInfo> programNmeList = new ArrayList<TbProgramNameMapInfo>();
    String indexFlow = "0";
    String smsHeader = "==BATH Monitor Notify==";
    
    public String getSmsHeader()
    {
        return smsHeader;
    }

    public void setSmsHeader(String smsHeader)
    {
        this.smsHeader = smsHeader;
    }

    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        
        MailContext mctx = (MailContext)ctx;
        TbMailSettingInfo mailSettingInfo = mctx.getMailSetInfo();
        String dataFlow = mailSettingInfo.getDataFlow();
        if(dataFlow.indexOf(indexFlow) != -1)
        {
            getProgramNameMap(ctx);
            return true;
        }
        return false;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        boolean isSendMonitorMail = false;
        MailContext mctx = (MailContext)ctx;
        StringBuffer smsMsg = new StringBuffer();
        smsMsg.append(new String(smsHeader+"\n"));
        //smsMsg.append(new String("==批次監控程式==\n"));
        
        StringBuffer message_body = new StringBuffer();
        mctx.setSmsMsgBody(smsMsg);
        mctx.setMessageBody(message_body);
        TbBatchResultInfo batchResultInfo = null;
        
        String workFg = "";
        String edate = "";
        
        message_body.append("<br><br>");
        message_body.append("<h3><font color=blue>【"+mctx.getSysName()+"SVC平台批次監控程式】</font></h3><br>");
        message_body.append("<table cellspacing=\"0\" border=\"1\">");
        message_body.append("<tr bgcolor=\"#D3EBE7\"><B>");
        message_body.append("<td ALIGN=center>程式名稱</td>");
        message_body.append("<td ALIGN=center>程式開始日期</td>");
        message_body.append("<td ALIGN=center>程式開始時間</td>");
        message_body.append("<td ALIGN=center>程式結束日期</td>");
        message_body.append("<td ALIGN=center>程式結束時間</td>");
        message_body.append("<td ALIGN=center>RCODE</td>");
        message_body.append("<td ALIGN=center>說明</td></B></tr>");
        
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select A.PROGRAM_NAME, A.START_DATE, A.START_TIME, A.END_DATE, A.END_TIME, A.RCODE, A.ERROR_DESC, A.WORK_FLAG");
        sqlCmd.append(" from tb_batch_result A");
        sqlCmd.append(" where A.PROGRAM_NAME = ? and A.START_DATE = '" + ctx.getHostDate() + "'");
        Vector<String> parm;
        Vector result;
        TbProgramNameMapInfo programNameMapInfo;
        for(int iprogram = 0 ; iprogram < programNmeList.size(); iprogram++)
        {
            programNameMapInfo = programNmeList.get(iprogram);
            if(null != programNameMapInfo)
            {
                parm = new Vector<String>();
                parm.add(programNameMapInfo.getProgramName());
                result = DbUtil.select(sqlCmd.toString(), parm, mctx.getConnection());
                logger.debug("result:"+ (null == result ? 0 : result.size()));
                if(null != result && result.size() > 0)
                {
                    for(int idx = 0; idx < result.size(); idx++)
                    {
                        Vector record = (Vector)result.get(idx);
                        logger.debug("record:"+ (null == record ? 0 : record.size()));
                        if(record != null)
                        {
                            batchResultInfo = new TbBatchResultInfo();
                            if(null == mctx.getSmsMsgBody())
                            {
                                smsMsg = new StringBuffer();
                                mctx.setSmsMsgBody(smsMsg);
                            }
                            String newProgName = "";
                            batchResultInfo.setProgramName((String)record.get(0));
                            batchResultInfo.setStartDate((String)record.get(1));
                            batchResultInfo.setStartTime((String)record.get(2));
                            batchResultInfo.setEndDate((String)record.get(3));
                            batchResultInfo.setEndTime((String)record.get(4));
                            batchResultInfo.setRcode((String)record.get(5));
                            batchResultInfo.setErrorDesc((String)record.get(6));
                            batchResultInfo.setWorkFlag((String)record.get(7));
                            logger.debug(batchResultInfo);
                            if(null != batchResultInfo)
                            {
                                String info = "";
                                workFg = batchResultInfo.getWorkFlag() == null ? "0" : batchResultInfo.getWorkFlag();
                                edate = batchResultInfo.getEndDate() == null ? "" : batchResultInfo.getEndDate();
                                newProgName = batchResultInfo.getProgramName().trim() + "(" + (programNameMapInfo.getMappingName()) + ")";
                               
                                if(StringUtil.isEmpty(batchResultInfo.getEndDate()) || Integer.valueOf(workFg) == 9 || StringUtil.isEmpty(batchResultInfo.getRcode()) || !batchResultInfo.getRcode().equals("0000"))
                                {
                                    isSendMonitorMail = true;
                                    message_body.append("<tr>");
                                    message_body.append("<span style='background-color: rgb(255, 0, 0); color: rgb(255, 255, 255);'>");
                                    message_body.append("<td>"+newProgName.trim()+"</td>");
                                    message_body.append("<td ALIGN=center>"+parserDateStr(true,batchResultInfo.getStartDate())+"</td>");
                                    message_body.append("<td ALIGN=center> "+parserDateStr(false,batchResultInfo.getStartTime())+" </td>");
                                    message_body.append("<td ALIGN=center>"+parserDateStr(true,batchResultInfo.getEndDate())+"</td>");
                                    message_body.append("<td ALIGN=center> "+parserDateStr(false,batchResultInfo.getEndTime())+" </td>");
                                    message_body.append("<td ALIGN=center>"+batchResultInfo.getRcode()+"</td>");
                                    message_body.append("<td>"+batchResultInfo.getErrorDesc()+"</td>");
                                    message_body.append("</span>");
                                    message_body.append("</tr>");
                                    
                                    if(StringUtil.isEmpty(batchResultInfo.getEndDate()))
                                    {
                                        smsMsg.append("* "+batchResultInfo.getProgramName().trim() + " unfinished");
                                    }
                                    else
                                    {
                                        smsMsg.append("* "+batchResultInfo.getProgramName().trim() + " execute fail (" + batchResultInfo.getRcode() + ")");
                                    }
                                    smsMsg.append("\n");
                                }
                            }
                        }
                    }
                }
                else
                {
                    isSendMonitorMail = true;
                    message_body.append("<tr>");
                    message_body.append("<span style='background-color: rgb(255, 0, 0); color: rgb(255, 255, 255);'>");
                    message_body.append("<td>"+programNameMapInfo.getProgramName().trim()+"</td>");
                    message_body.append("<td ALIGN=center></td>");
                    message_body.append("<td ALIGN=center></td>");
                    message_body.append("<td ALIGN=center></td>");
                    message_body.append("<td ALIGN=center></td>");
                    message_body.append("<td ALIGN=center></td>");
                    message_body.append("<td>未執行</td>");
                    message_body.append("</span>");
                    message_body.append("</tr>");
                    smsMsg.append("* "+programNameMapInfo.getProgramName().trim() + " no execute");
                    smsMsg.append("\n");
                }
            }
        }
        
        message_body.append("</table>");
        if(!isSendMonitorMail)
        {
            mctx.setSmsMsgBody(null);
            mctx.setMessageBody(null);
        }
        else
        {
            logger.debug(smsMsg.toString());
        }
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
    
    public void getProgramNameMap(LMSContext ctx) throws SQLException
    {
        String sqlCmd = "select * from TB_PROGRAM_NAME_MAP where MONITOR_FG = '1'";
        Vector result = DbUtil.select(sqlCmd, ctx.getConnection());
        Vector record = null;
        TbProgramNameMapInfo programNameInfo = null;
        if(null != result && result.size() > 0)
        {
            logger.debug("TB_PROGRAM_NAME_MAP:"+result.size());
            for(int i = 0; i < result.size(); i++)
            {
                record = (Vector)result.get(i);
                if(null != record)
                {
                    programNameInfo = new TbProgramNameMapInfo();
                    programNameInfo.setProgramName((String) record.get(0));
                    programNameInfo.setMappingName((String) record.get(1));
                    programNmeList.add(programNameInfo);
                }
            }
        }
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