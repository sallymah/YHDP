package tw.com.hyweb.svc.yhdp.batch.mail;

import java.util.HashMap;
import java.util.Map;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;

public class MailContext extends LMSContext
{

    private StringBuffer messageBody = null;
    private StringBuffer smsMsgBody = null;
    private String sysName = "";
    private DBService dbService;
    private TbMailSettingInfo mailSetInfo;
    private Map programNmeMap = new HashMap();

    public Map getProgramNmeMap()
    {
        return programNmeMap;
    }
    
    public void setProgramNmeMap(Map programNmeMap)
    {
        this.programNmeMap =  programNmeMap;
    }
    
    public TbMailSettingInfo getMailSetInfo()
    {
        return mailSetInfo;
    }
    
    public void setMailSetInfo(TbMailSettingInfo mailSetInfo)
    {
        this.mailSetInfo =  mailSetInfo;
    }
    
    public String getSysName()
    {
        return sysName;
    }
    
    public void setSysName(String sysName)
    {
        this.sysName =  sysName;
    }
    
    public StringBuffer getMessageBody()
    {
        if(null == messageBody)
        {
            messageBody = new StringBuffer();
        }
        return messageBody;
    }
    
    public void setMessageBody(StringBuffer messageBody)
    {
        this.messageBody =  messageBody;
    }
    
    public DBService getDbService()
    {
        return dbService;
    }
    
    public void setDbService(DBService dbService)
    {
        this.dbService =  dbService;
    }
    
    public void setSmsMsgBody(StringBuffer smsMsgBody)
    {
        this.smsMsgBody =  smsMsgBody;
    }
    
    public StringBuffer getSmsMsgBody()
    {
        return this.smsMsgBody;
    }
}
