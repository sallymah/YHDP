package tw.com.hyweb.svc.yhdp.batch.mail;

import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;
import tw.com.hyweb.util.CipherUtil;
import tw.com.hyweb.util.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class SendMail implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(SendMail.class);
    private StringBuffer htmlStr = null;
    private String pwd = "";
    private String usr = "";
    private String subject = "SVC平台批次通知【請勿直接回覆】";

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        MailContext mctx = (MailContext)ctx;
        StringBuffer message_body = mctx.getMessageBody();
        
        if(null != message_body && message_body.length() > 0)
        {
            TbMailSettingInfo mailSetInfo = mctx.getMailSetInfo();
            String pwd = mailSetInfo.getLoginPwd();
            byte[] decPwd = CipherUtil.decrypt(pwd);
            String decString = new String(decPwd);
            mailSetInfo.setLoginPwd(decString);
            return true;
        }
        return false;
    }
    
    public String parserDateStr(boolean isDate, String dateTime)
    {
        String parserStr = "";
        if(isDate)
            parserStr = dateTime.substring(0, 4) + "/" + dateTime.substring(4, 6) + "/" + dateTime.substring(6, 8);
        else
            parserStr = dateTime.substring(0, 2) + ":" + dateTime.substring(2, 4) + ":" + dateTime.substring(4, 6);
        return parserStr;
    }
    
    public void prepareBody(LMSContext ctx)
    {
        MailContext mctx = (MailContext)ctx;
        StringBuffer message_body = mctx.getMessageBody();
        htmlStr = new StringBuffer();
        htmlStr.append("<html><body><pre><font face=\"Arial\"><B>統計時間:");
        htmlStr.append(parserDateStr(true, mctx.getHostDate()) + " " + parserDateStr(false, mctx.getHostTime()));
        htmlStr.append("<B>");
        htmlStr.append(message_body.toString());
        htmlStr.append("</font></pre></body></html>");
        mctx.setMessageBody(null);
        //logger.debug(htmlStr);
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws TxException
    {
        logger.debug("doAction");
        MailContext mctx = (MailContext)ctx;
        TbMailSettingInfo mailSetInfo = mctx.getMailSetInfo();
        if(null != mailSetInfo)
        {
            String message_subject = mctx.getSysName() + subject;
            Session session;
            Properties props = new Properties();
            
            //mailhost為想用的smtp server
            props.put("mail.smtp.host", mailSetInfo.getMailHost());
            
            if(!StringUtil.isEmpty(mailSetInfo.getMailPort()))
                props.put("mail.smtp.port", mailSetInfo.getMailPort());
            
            if(!StringUtil.isEmpty(mailSetInfo.getLoginUsername()) && !StringUtil.isEmpty(mailSetInfo.getLoginPwd()))
            {
                logger.debug("is auth");
                props.put("mail.smtp.socketFactory.port", mailSetInfo.getMailPort()); 
                props.put("mail.smtp.auth", Integer.valueOf(mailSetInfo.getIsAuth()) == 1 ?"true":"false");
                props.put("mail.smtp.socketFactory.class",  "javax.net.ssl.SSLSocketFactory");
                usr = mailSetInfo.getLoginUsername();
                pwd = mailSetInfo.getLoginPwd();
                
                session = Session.getInstance(props, new Authenticator() {  
                    protected PasswordAuthentication getPasswordAuthentication() {  
                        return new PasswordAuthentication(usr, pwd);  
                    }  
                });
            }
            else
            {
                session = Session.getInstance(props, null);
            }
            logger.debug(props.toString());
            session.setDebug(false);
            
            try {
                MimeMessage mesg;
                Multipart multipart = new MimeMultipart();
                MimeBodyPart mbp = new MimeBodyPart();
                
                //建立html格式內容
                prepareBody(ctx);//產生mail context
                mbp.setContent(htmlStr.toString(),"text/html;charset=utf-8");
                multipart.addBodyPart(mbp);
                mesg = new MimeMessage(session);
                
                mesg.setFrom(new InternetAddress(mailSetInfo.getSender()));//寄信人的位址
                InternetAddress[] address= InternetAddress.parse(mailSetInfo.getRecipient());//收信人的位址
                
                if(Integer.valueOf(mailSetInfo.getRecipientType()) == 1)
                {
                    logger.debug("RecipientType: CC");
                    mesg.setRecipients(Message.RecipientType.CC, address);
                }
                else if(Integer.valueOf(mailSetInfo.getRecipientType()) == 2)
                {
                    logger.debug("RecipientType: BCC");
                    mesg.setRecipients(Message.RecipientType.BCC, address);
                }
                else
                {
                    logger.debug("RecipientType: TO");
                    mesg.setRecipients(Message.RecipientType.TO, address);
                }
                mesg.setSentDate(new Date());//寄送日期時間
                mesg.setSubject(message_subject, "utf-8");//主旨
                mesg.setContent(multipart);//
                Transport.send(mesg);
            }
            catch (Exception e)
            {
                logger.debug("",e);
                throw new TxException("",e);
            }
        }
    }
}
