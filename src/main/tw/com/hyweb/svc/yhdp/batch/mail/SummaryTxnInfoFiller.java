package tw.com.hyweb.svc.yhdp.batch.mail;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;

public class SummaryTxnInfoFiller implements IBizAction
{
	private int pSignCnt;//正向交易筆數
	private int mSignCnt;//負向交易筆數
	private int totalCnt;//總筆數
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(SummaryTxnInfoFiller.class);
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
        String yestDate = DateUtil.addDate(ctx.getHostDate(), -1);
        String sqlCmd = "SELECT TXN_DATE,ONLINE_FLAG," +
        				"SUM(CASE DEF.SIGN WHEN 'P' THEN 1 ELSE 0 END ) AS PSIGN," +
        				"SUM(CASE DEF.SIGN WHEN 'M' THEN 1 ELSE 0 END ) AS MSIGN " +
        				"FROM TB_ONL_TXN TXN,TB_P_CODE_DEF DEF WHERE TXN_DATE='"+yestDate+"' " +
						"AND DEF.P_CODE = TXN.P_CODE AND TXN.STATUS <> '9' " +
						"GROUP BY TXN_DATE,ONLINE_FLAG";
       
        Vector result=BatchUtil.getInfoListHashMap(sqlCmd, ctx.getConnection());
        
        message_body.append("<br><br>");
        message_body.append("<h3><font color=blue>【"+mctx.getSysName()+"SVC平台交易量統計】</font></h3><br>");
        message_body.append("<table cellspacing=\"0\" border=\"1\">");
        message_body.append("<tr bgcolor=\"#D3EBE7\">");
        message_body.append("<td>交易日期</td>");
        message_body.append("<td>ONLINE_FLAG</td>");
        message_body.append("<td>加點筆數</td>");
        message_body.append("<td>扣點筆數</td>");
        message_body.append("</tr>");
        
        if(null != result && result.size() > 0)
        {
        	for (int idx=0; idx<result.size(); idx++)
            {
            	String txnDate=((HashMap)(result.get(idx))).get("TXN_DATE").toString();
            	String onlineFlag=((HashMap)(result.get(idx))).get("ONLINE_FLAG").toString();
            	String pSign=((HashMap)(result.get(idx))).get("PSIGN").toString();
            	String mSign=((HashMap)(result.get(idx))).get("MSIGN").toString();
            	
                message_body.append("<tr>");
                message_body.append("<td>"+txnDate+"</td>");
//                message_body.append("<td align=right>"+onlineFlag+"</td>");
                
                if(onlineFlag.equals("N"))
                {
                    message_body.append("<td align=lift>"+"連線交易"+"</td>");
                }
                else if (onlineFlag.equals("F"))
                {
                    message_body.append("<td align=lift>"+"離線交易"+"</td>");
                }
                else if (onlineFlag.equals("O"))
                {
                	message_body.append("<td align=lift>"+"其他交易"+"</td>");
                }
                else {
                	message_body.append("<td align=lift>"+onlineFlag+"</td>");
				}
                message_body.append("<td align=right>"+pSign+"</td>");
                message_body.append("<td align=right>"+mSign+"</td>");
                pSignCnt+=Integer.parseInt(pSign);
                mSignCnt+=Integer.parseInt(mSign);
                message_body.append("</tr>");            	
            }
            message_body.append("<tr>");
            message_body.append("<td align=right>"+"小計"+"</td>");
            message_body.append("<td></td>");
            message_body.append("<td align=right>"+pSignCnt+"</td>");
            message_body.append("<td align=right>"+mSignCnt+"</td>");
            message_body.append("</tr>");            	           
            totalCnt=pSignCnt+mSignCnt;
            message_body.append("</tr>");            	
        }
        message_body.append("</table>");
        message_body.append("總計: ");
        message_body.append(totalCnt+"筆");

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
