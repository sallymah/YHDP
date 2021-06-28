package tw.com.hyweb.svc.yhdp.batch.mail;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;
import tw.com.hyweb.service.db.mgr.TbBatchResultMgr;

import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class BatchResultFiller implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(BatchResultFiller.class);
    Map<String,String> programNmeMap = new HashMap<String,String>();
    String indexFlow = "0";
    
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
        
        //抓取L3 版本號
        String version = "";
        try {
    		File file = new File("bin" + File.separator + "L3-svc-batch.jar");
    		JarFile jar = new JarFile(file);
			Manifest manifest = jar.getManifest();
			
			Attributes attributes = manifest.getMainAttributes();
			version = attributes.getValue("Built-SVN-Version");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        MailContext mctx = (MailContext)ctx;
        StringBuffer message_body = mctx.getMessageBody();
        TbBatchResultInfo batchResultInfo = null;
        TbBatchResultMgr BatchResultMrg = new TbBatchResultMgr(mctx.getConnection());
        int errDesCnt = 0;
        String workFg = "";
        String edate = "";
        Vector<TbBatchResultInfo> result = new Vector<TbBatchResultInfo>();
        String condiction = "PROGRAM_NAME <> 'NotifyMail' AND START_DATE = '" + ctx.getHostDate() + "'";
        String order = "START_TIME";
        logger.debug(condiction);
        BatchResultMrg.queryMultiple(condiction, result, order);
        message_body.append("<br><br>");
        message_body.append("<h3><font color=blue>【"+mctx.getSysName()+"SVC平台批次程序】</font></h3><br>");
        message_body.append("<h5><font>Build Version: "+ version +"</font></h5><br>");
        message_body.append("<table cellspacing=\"0\" border=\"1\">");
        message_body.append("<tr bgcolor=\"#D3EBE7\"><B>");
        message_body.append("<td ALIGN=center>程式名稱</td>");
        message_body.append("<td ALIGN=center>程式開始日期</td>");
        message_body.append("<td ALIGN=center>程式開始時間</td>");
        message_body.append("<td ALIGN=center>程式結束日期</td>");
        message_body.append("<td ALIGN=center>程式結束時間</td>");
        message_body.append("<td ALIGN=center>RCODE</td>");
        message_body.append("<td ALIGN=center>說明</td></B></tr>");
        if(null != result && result.size() > 0)
        {
            logger.debug(result.size());
            String newProgName = "";
            String sysCfgValue = "";
            for(int i = 0; i < result.size(); i++)
            {
                batchResultInfo = result.get(i);
                if(null != batchResultInfo)
                {
                    String info = "";
                    workFg = batchResultInfo.getWorkFlag() == null ? "0" : batchResultInfo.getWorkFlag();
                    edate = batchResultInfo.getEndDate() == null ? "" : batchResultInfo.getEndDate();
                    sysCfgValue = programNmeMap.get(batchResultInfo.getProgramName().trim());
                    newProgName = batchResultInfo.getProgramName().trim() + "(" + (sysCfgValue == null ? "" : sysCfgValue) + ")";
                    if(StringUtil.isEmpty(batchResultInfo.getEndDate()) || Integer.valueOf(workFg) == 9 || StringUtil.isEmpty(batchResultInfo.getRcode()) || !batchResultInfo.getRcode().equals("0000"))
                    {
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
                    }
                    else
                    {
                        message_body.append("<tr>");
                        message_body.append("<td>"+newProgName.trim()+"</td>");
                        message_body.append("<td ALIGN=center>"+parserDateStr(true,batchResultInfo.getStartDate())+"</td>");
                        message_body.append("<td ALIGN=center>"+parserDateStr(false,batchResultInfo.getStartTime())+"</td>");
                        message_body.append("<td ALIGN=center>"+parserDateStr(true,batchResultInfo.getEndDate())+"</td>");
                        message_body.append("<td ALIGN=center>"+parserDateStr(false,batchResultInfo.getEndTime())+"</td>");
                        message_body.append("<td ALIGN=center>"+batchResultInfo.getRcode()+"</td>");
                        message_body.append("<td>"+batchResultInfo.getErrorDesc()+"</td>");
                        message_body.append("</tr>");
                    }
                }
            }   
        }
        message_body.append("</table>");
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
        String sqlCmd = "select * from TB_PROGRAM_NAME_MAP";
        Vector result = DbUtil.select(sqlCmd, ctx.getConnection());
        Vector record = null;
        
        if(null != result && result.size() > 0)
        {
            logger.debug("TB_PROGRAM_NAME_MAP:"+result.size());
            for(int i = 0; i < result.size(); i++)
            {
                record = (Vector)result.get(i);
                if(null != record)
                {
                    programNmeMap.put((String) record.get(0), (String) record.get(1));
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
