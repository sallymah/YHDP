package tw.com.hyweb.svc.yhdp.batch.mail;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;
import tw.com.hyweb.util.DbUtil;

public class ReversalInfoFiller implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(ReversalInfoFiller.class);
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
        String sqlCmd = "SELECT count(*) FROM tb_onl_txn WHERE txn_date='" + yestDate + "' and txn_src ='E'";
        Vector<String> params = new Vector<String>();
        
        int txnCnt = DbUtil.getInteger(sqlCmd, ctx.getConnection());
        
        sqlCmd = "SELECT count(*) FROM tb_onl_txn WHERE txn_date='" + yestDate  + "' and (mti='0400' or status='9')";
        params.add(yestDate);
        int reveralCnt = DbUtil.getInteger(sqlCmd, ctx.getConnection());
        
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(1);//小數點一位
        String fraction = "0";
        if(txnCnt != 0)
        {
            double pert = (double)reveralCnt/txnCnt;
            fraction = nf.format(pert);
        }
        else
            fraction = nf.format(0);
        
        message_body.append("<br><br>");
        message_body.append("<h3><font color=blue>【"+mctx.getSysName()+"SVC平台Reversal統計】</font></h3><br>");
        message_body.append("<table cellspacing=\"0\" border=\"1\">");
        message_body.append("<tr bgcolor=\"#D3EBE7\"><B>");
        message_body.append("<td>交易日期</td>");
        message_body.append("<td>總筆數</td>");
        message_body.append("<td>沖正筆數</td>");
        message_body.append("<td>沖正比率</td>");
        message_body.append("</tr>");
        message_body.append("<tr>");
        message_body.append("<td>"+yestDate+"</td>");
        message_body.append("<td align=right>"+txnCnt+"</td>");
        message_body.append("<td align=right>"+reveralCnt+"</td>");
        message_body.append("<td align=right>"+fraction+"</td>");
        message_body.append("</tr>");
        message_body.append("</table>");
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
