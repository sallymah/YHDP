package tw.com.hyweb.svc.yhdp.batch.mail;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.util.DbUtil;

public class TableSpaceFiller implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(TableSpaceFiller.class);
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        MailContext mctx = (MailContext)ctx;
        StringBuffer message_body = mctx.getMessageBody();
        message_body.append("<br><br>");
        message_body.append("<h3><font color=blue>【"+mctx.getSysName()+"SVC平台資料庫TableSpace檢視】</font></h3><br>");
        message_body.append("<table cellspacing=\"0\" border=\"1\">");
        message_body.append("<tr bgcolor=\"#D3EBE7\"><B>");
        message_body.append("<td>TABLESPACE_NAME</td>");
        message_body.append("<td>TOTAL SPACE</td>");
        message_body.append("<td>FREE SPACE</td>");
        message_body.append("<td>PCTUSED</td>");
        message_body.append("<td align=center>註記</td>");
        message_body.append("</tr>");
        
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT F.TABLESPACE_NAME,round(T.TOMB) TOTAL,round(F.FRMB) FREE ,round((T.TOMB-F.FRMB)/X.MAXMB*100,1) PCTUSED FROM ");
        sb.append("((SELECT TABLESPACE_NAME,SUM(BYTES/1048576) TOMB FROM DBA_DATA_FILES GROUP BY TABLESPACE_NAME) UNION ALL ");
        sb.append("(SELECT TABLESPACE_NAME,SUM(BYTES/1048576) TOMB FROM DBA_TEMP_FILES GROUP BY TABLESPACE_NAME)) T, ");
        sb.append("(SELECT TABLESPACE_NAME,SUM(FRMB) FRMB FROM");
        sb.append(" ((SELECT TABLESPACE_NAME,SUM(BYTES/1048576) FRMB FROM DBA_FREE_SPACE GROUP BY TABLESPACE_NAME) UNION ALL ");
        sb.append("(SELECT TABLESPACE_NAME,0 FROM DBA_TABLESPACES) UNION ALL ");
        sb.append("(SELECT TABLESPACE_NAME,SUM(BYTES_USED/1048576) FRMB FROM V$TEMP_SPACE_HEADER GROUP BY TABLESPACE_NAME) ");
        sb.append(") GROUP BY TABLESPACE_NAME) F, ");
        sb.append("((SELECT TABLESPACE_NAME,SUM(MAXBYTES/1048576) MAXMB FROM DBA_DATA_FILES GROUP BY TABLESPACE_NAME) UNION ALL ");
        sb.append("(SELECT TABLESPACE_NAME,SUM(BYTES/1048576) MAXMB FROM DBA_TEMP_FILES GROUP BY TABLESPACE_NAME)) X ");
        sb.append("WHERE F.TABLESPACE_NAME = T.TABLESPACE_NAME(+) AND F.TABLESPACE_NAME = X.TABLESPACE_NAME(+) ");
        sb.append("ORDER BY PCTUSED,TABLESPACE_NAME");
        Vector result = DbUtil.select(sb.toString(), ctx.getConnection());
        String tableName = "";
        double totCnt = 0;
        double free = 0;
        double pctused = 0;
        if(null != result)
        {
            for(int i = 0; i < result.size(); i++)
            {
                Vector record = (Vector)result.get(i);
                if(null != record)
                {
                    tableName = (String)record.get(0);
                    totCnt = ((Number)record.get(1)).doubleValue();
                    free = ((Number)record.get(2)).doubleValue();
                    pctused = ((Number)record.get(3)).doubleValue();
                    message_body.append("<tr>");
                    message_body.append("<td>"+tableName+"</td>");
                    message_body.append("<td align=right>"+totCnt+"</td>");
                    message_body.append("<td align=right>"+free+"</td>");
                    message_body.append("<td align=right>"+String.valueOf(pctused)+"</td>");
                    
                    if (pctused > 80) {
                        message_body.append("<td>");
                        message_body.append("<span style=\"background-color: rgb(255, 0, 0); color: rgb(255, 255, 255);\"><b>");
                        message_body.append("  超過80％空間使用</b></span></td>");         
                    }else
                        message_body.append("<td></td>");
                    
                    message_body.append("</tr>");
                }
            }
            
        }
        message_body.append("</table>");
    }
}
