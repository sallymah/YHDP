/*
 * 
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * *********************************************** 
 */
package tw.com.hyweb.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import tw.com.hyweb.util.DisposeUtil;

/**
 * The Class CfgInfo.
 */
public class CfgInfo extends Properties
{
    public static final String IS_NOTIFY_SMS="monitor.isNotifySms";
    public static final String IS_NOTIFY_MAIL="monitor.isNotifyMail";
    public static final String SMS_ID="monitor.smsId";
    public static final String SMS_PASSWD="monitor.smsPasswd";
    public static final String TEL="monitor.tel"; 
    
    public static final String NOTIFY_PERIOD="monitor.notifyPeriod";
    
    public static final String DAY_CHK_PERIOD="monitor.dayChkPeriod";
    public static final String NIGHT_CHK_PERIOD="monitor.nightChkPeriod";
    public static final String DAY_CHK_TIME="monitor.dayChkTime";
    public static final String NIGHT_CHK_TIME="monitor.nightChkTime";

    public static final String AP_ERR_CNT_DEF="monitor.apErrCntDef";
    public static final String TXN_TIMEOUT_CNT_DEF="monitor.txnTimeoutCntDef";
    public static final String TXN_ERR_CNT_DEF="monitor.txnErrCntDef";
    public static final String DB_ERR_CNT_DEF="monitor.dbErrCntDef";
    public static final String HSM_ERR_CNT_DEF="monitor.hsmErrCntDef";
    public static final String AP_ERR_CNT="monitor.apErrCnt";
    public static final String TXN_TIMEOUT_CNT="monitor.txnTimeoutCnt";
    public static final String TXN_ERR_CNT="monitor.txnErrCnt";
    public static final String DB_ERR_CNT="monitor.dbErrCnt";
    public static final String HSM_ERR_CNT="monitor.hsmErrCnt";
    public static final String MAIL_SERVER="monitor.mailserver";
    public static final String MAIL_FROM="monitor.email.from";
    public static final String MAIL_SUBJECT="monitor.email.subject";
    public static final String EMAIL="monitor.email";
    public static final String IP="monitor.ip";
    public static final String PORT="monitor.port";

    public static final String LAST_CHK_TIME="monitor.lastChkTime";
    public static final String LAST_NOTIFY_TIME="monitor.lastNotifyTime";
    public static final String LAST_TXN_TIME="monitor.lastTxnTime";
    
    public static final String TXN_TIMEOUT_VAL_DEF="monitor.txnTimeoutValDef";
    public static final String TXN_LONGTIMEIDLE_VAL_DEF="monitor.txnLongTimeIdleValDef";
    
    public static final String SUCCESS_RCODE="monitor.successRcode";
    
    public static final String AP_ERR_MSG_DEF="monitor.apErrMsgDef";
    public static final String DB_ERR_MSG_DEF="monitor.dbErrMsgDef";
    public static final String HSM_ERR_MSG_DEF="monitor.hsmErrMsgDef";
    public static final String TXN_ERR_MSG_DEF="monitor.txnErrMsgDef";
    public static final String TIMEOUT_ERR_MSG_DEF="monitor.timeoutMsgDef";
    public static final String IDLE_ERR_MSG_DEF="monitor.idleMsgDef";
    
    String role;
    String link;
//    String ip;
//    String port;
//    
//    String email;
//    String mailServer;
//    
//    String tel;
//    
//    String lastChkTime;
//    String lastNotifyTime;
//    String lastTxnTime;
//    
//    int notifyPeriod=3600;
//    int chkPeriod=300;
//    String chkStime="000000";
//    String chkEtime="235959";
//    int apErrCntDef=1;
//    int txnTimeoutCntDef=1;
//    int txnErrCntDef=1;
//    int dbErrCntDef=1;
//    int hsmErrCntDef=1;
//    
//    int apErrCnt=0;
//    int txnTimeoutCnt=0;
//    int txnErrCnt=0;
//    int dbErrCnt=0;
//    int hsmErrCnt=0;
    
    //private Properties props = new Properties();
    
    /** The Constant MONITOR_CONFIG_PROPERTIES_FILENAME. */
    public static final String MONITOR_CONFIG_PROPERTIES_FILENAME=DefMainGui.getApDirPath()+File.separator+"monitor.xml";
    
    /**
     * Instantiates a new tb mon txn info.
     */
    public CfgInfo()
    {
        load();
    }
    
    /**
     * Gets the props.
     *
     * @return the props
     */
//    public Properties getProps()
//    {
//        return props;
//    }
    
    /**
     * Load.
     */
    public void load()
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(MONITOR_CONFIG_PROPERTIES_FILENAME);
            loadFromXML(fis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            DisposeUtil.close(fis);
        }
    }
    
    /**
     * Store.
     */
    public void store()
    {
        FileOutputStream fos=null;
        try
        {
            fos = new FileOutputStream(MONITOR_CONFIG_PROPERTIES_FILENAME);
            storeToXML(fos, null);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (InvalidPropertiesFormatException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            DisposeUtil.close(fos);
        }        
    }
    
    /**
     * @return the role
     */
    public String getRole()
    {
        return this.role;
    }
    /**
     * @param role the role to set
     */
    public void setRole(String role)
    {
        this.role = role;
    }
    /**
     * @return the link
     */
    public String getLink()
    {
        return this.link;
    }
    /**
     * @param link the link to set
     */
    public void setLink(String link)
    {
        this.link = link;
    }
    
    /**
     * convert the property value to int. if key is null or value is null, 0 will be return.
     *
     * @param key the key
     * @return the int
     */
    public int getInt(String key)
    {
        return Integer.parseInt(getProperty(key,"0"));
    }
    
    /**
     * Gets the int.
     *
     * @param key the key
     * @param def the def
     * @return the int
     */
    public int getInt(String key,String def)
    {
        return Integer.parseInt(getProperty(key,def));
    }
    
    /**
     * Gets the boolean.default is return false.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean getBoolean(String key)
    {
        return getBoolean(key,false);
    }
    
    /**
     * Gets the boolean.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the boolean
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        boolean ret = defaultValue;
        String s = getProperty(key);
        if (s == null)
        {
            return ret;
        }
        s = s.toLowerCase();
        if ((s.equals("true")) || (s.equals("on")) || (s.equals("yes")))
        {
            ret = true;
        }
        else
        {
            ret = false;
        }
        return ret;
    }
}
