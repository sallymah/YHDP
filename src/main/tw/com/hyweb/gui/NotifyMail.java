/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * *********************************************** 
 */
package tw.com.hyweb.gui;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * NotifyMail
 */
public class NotifyMail
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(NotifyMail.class);

    /** The Constant MAIL_HOST. */
    private static final String MAIL_HOST = "mail.host";

    /** The Constant MAIL_TRANSPORT_PROTOCOL. */
    private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    private CfgInfo cfgInfo;
    private boolean isNotifyMail = false;
    
    /**
     * Instantiates a new notify mail.
     * 
     * @param cfgInfo the cfg info
     */
    public NotifyMail(CfgInfo cfgInfo)
    {
        this.cfgInfo = cfgInfo;
        this.isNotifyMail = this.cfgInfo.getBoolean(CfgInfo.IS_NOTIFY_MAIL);
    }

    /**
     * Mailto.
     * 
     * @param to the to
     * @param messageText the message text
     * @return the string
     */
    public String mailto(String to, String messageText)
    {
        String ret = "";
        if (!isNotifyMail)
        {
            logger.debug("config to enable mail notify");
            return ret;
        } 
        
        String subject = cfgInfo.getProperty(CfgInfo.MAIL_SUBJECT, "Server Abnormal...");
        // String messageText = "<h1>please check the server status...</h1>";
        Properties prop = new Properties();
        String mailServer = cfgInfo.getProperty(CfgInfo.MAIL_SERVER);
        if (mailServer == null)
        {
            ret = "please setting mail server";
            return ret;
        }
        prop.put(MAIL_HOST, mailServer);
        prop.put(MAIL_TRANSPORT_PROTOCOL, "smtp");
        Session mailSession = Session.getDefaultInstance(prop, null);
        Message msg = new MimeMessage(mailSession);
        String from = cfgInfo.getProperty(CfgInfo.MAIL_FROM);
        try
        {
            if (from != null && from.length() > 0)
            {
                msg.setFrom(new InternetAddress(from));
            }
            String mailto = to;
            if (mailto == null)
            {
                mailto = cfgInfo.getProperty(CfgInfo.EMAIL);
            }

            InternetAddress[] address = InternetAddress.parse(mailto, false);
            msg.setRecipients(Message.RecipientType.TO, address);
            if (subject != null)
            {
                msg.setSubject(subject);
            }

            msg.setSentDate(new Date());

            // msg.setText(messageText);
            msg.setContent(messageText, "text/html" + ";charset=big5");

            Transport.send(msg);
            ret = "send mail successful.";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ret = "send mail fail";
        }
        return ret;
    }
}
