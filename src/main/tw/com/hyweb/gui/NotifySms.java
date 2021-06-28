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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import tw.com.hyweb.util.ISODate;
import tw.com.hyweb.util.PropertiesUtil;

/**
 * NotifySms
 */
public class NotifySms
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(NotifySms.class);
    
    private String smsAddress = "http://sms.systex.com.tw:17760/HTTP_API/input.cgi?"; 
    private String id="API31111639";
    private String passwd="nnx622ux";
//    private String tel = "0931840191";
    private String tel;
    private CfgInfo cfgInfo;
    private boolean isNotifySms=false;
    
    /**
     * Instantiates a new notify sms.
     *
     * @param cfgInfo the cfg info
     */
    public NotifySms(CfgInfo cfgInfo, String tel)
    {
        this.cfgInfo = cfgInfo;
        this.tel = tel;
        this.id = this.cfgInfo.getProperty(CfgInfo.SMS_ID);
        this.passwd = this.cfgInfo.getProperty(CfgInfo.SMS_PASSWD);
        this.isNotifySms = this.cfgInfo.getBoolean(CfgInfo.IS_NOTIFY_SMS);
               
    }
    
    /**
     * Do notify.
     *
     * @param body the body
     * @return the string
     */
    public String doNotify(String body)
    {
        String ret = "";
        URL url;
        HttpURLConnection conn=null;
        BufferedReader in=null;
         
        try
        {
            String[] messageText = composeUrl(body);
            
            for (int i =0;i<messageText.length;i++)
            {
//            	System.out.println(messageText[i]);
                url = new URL(messageText[i]);
                if (!isNotifySms)
                {
                    logger.debug("config to enable sms notify:"+body);
                    return ret;
                }
                conn = (HttpURLConnection) url.openConnection();
                
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                System.out.println(in.readLine());
                String line;
                StringBuffer sb = new StringBuffer();

                while ((line = in.readLine()) != null) 
                {
                    sb.append(line).append("\n");
                }
                in.close();
                ret = sb.toString();
                conn.disconnect();    
            }
            
        }
        catch (IOException e)
        {
            logger.error(e);        
        }
        finally
        {
            if (in!=null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    logger.error(e.getMessage());
                }
            }
        }
        
        return ret;
    }
    
    /**
     * @param args
     * @throws IOException 
     */
//    public static void main(String[] args) throws IOException
//    {
//        String body = "簡訊測試";
//        
//        CfgInfo cfgInfo = new CfgInfo();
//        NotifySms notifySms = new NotifySms(cfgInfo, cfgInfo.getProperty(CfgInfo.TEL));
//        String ret = notifySms.doNotify(body);
//        logger.debug(ret);
//        String[] rets = ret.split("\n");
//        String retcode=""; 
//        for (String var:rets)
//        {
//            if (var.startsWith("msgid"))
//            {
//                retcode = var.substring(1+var.indexOf('='));
//                break;
//            }
//        }
//        if (retcode.indexOf('-')>=0)
//        {
//            logger.warn("retcode="+retcode);
//        }
        
        
        /*
         * response如下:
         * msgid=100
         * MSISDN=0918343916
         * dlvTime=20100804134210
         * statusmsg=訊息已傳進後端資料庫等待發送
         * MsqSeq=1797367
         */
//    }

    /**
     * Compose url.
     *
     * @param body the body
     * @return the string
     */
    protected String[] composeUrl(String body)
    {
        
        String rplbody = body.replace(' ', '+');//space encode to '+' not to %20
        //body = URLEncoder.encode(body,"BIG5");
        
        String time = ISODate.formatDate(new Date(), "yyyyMMddHHmmss");
        String[] tels = this.tel.split(",");
        String[] ret = new String[tels.length];
        for (int i =0;i<tels.length;i++)
        {
            StringBuffer locate=new StringBuffer();
            locate.append(smsAddress);
            locate.append("UserID=").append(this.id);
            locate.append("&UserPWD=").append(this.passwd);
            locate.append("&MSISDN=").append(tels[i]);
            locate.append("&SMSBODY=").append(rplbody);
            locate.append("&Encoding=BIG5");
            locate.append("&RevTime=").append(time);
            
            logger.debug(locate);
            ret[i]=locate.toString();
        }
        
        return ret;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @param passwd the passwd to set
     */
    public void setPasswd(String passwd)
    {
        this.passwd = passwd;
    }

    /**
     * @param tel the tel to set
     */
    public void setTel(String tel)
    {
        this.tel = tel;
    }

}
