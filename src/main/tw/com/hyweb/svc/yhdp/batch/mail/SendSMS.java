package tw.com.hyweb.svc.yhdp.batch.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jpos.iso.ISOUtil;

import tw.com.hyweb.core.cp.online.loyalty.LMSContext;
import tw.com.hyweb.core.cp.online.loyalty.TxException;
import tw.com.hyweb.core.cp.online.loyalty.controller.IBizAction;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbMailSettingInfo;
import tw.com.hyweb.service.db.info.TbSmsSettingInfo;
import tw.com.hyweb.service.db.mgr.TbSmsSettingMgr;
import tw.com.hyweb.util.ArraysUtil;
import tw.com.hyweb.util.string.StringUtil;

public class SendSMS implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(SendSMS.class);
    private String sysId = "";
    private String srcAddress = "";
    private String[] destAddress = null;
    private String smsUrl = "";

    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        MailContext mctx = (MailContext)ctx;
        TbMailSettingInfo mailSetInfo = mctx.getMailSetInfo();
        StringBuffer smsMsg = mctx.getSmsMsgBody();
        if(mailSetInfo.getSmsFg().equals("1"))
        {
            if(null != smsMsg && smsMsg.length() > 0)
            {
                getSmsSetting(mctx);
                if(null != destAddress && destAddress.length > 0)
                {
                    return true;
                }
                else
                {
                    logger.error("tb_sms_setting is empty");
                }
            }
        }
        return false;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws TxException
    {
        logger.debug("doAction");           
        try {
            if(null != destAddress && destAddress.length > 0)
            {
                for(int idx = 0; idx < destAddress.length; idx++)
                {
                    sendSms(ctx, destAddress[idx]);
                }
            }
        }
        catch (Exception e)
        {
            logger.debug("",e);
            throw new TxException("",e);
        }
    }
    
    //HTTP POST request
    private boolean sendSms(LMSContext ctx, String destAddress) throws Exception
    {
        MailContext mctx = (MailContext)ctx;
        OutputStreamWriter osw = null;
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        try {
            StringBuffer smsMsg = mctx.getSmsMsgBody();
            String xml = genReqXml(sysId, srcAddress, destAddress, smsMsg.toString());
            HttpURLConnection httpConn = null;
            URL url = new URL(smsUrl);
            if(null != url)
            {
                httpConn = (HttpURLConnection)url.openConnection();
                httpConn.setRequestMethod("POST");
                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);
                OutputStream ops = httpConn.getOutputStream();
                 osw = new OutputStreamWriter(ops);
                logger.debug("xml="+xml);
                osw.write("xml="+xml);
                osw.flush();
                
                String line = "";  
                InputStream is = httpConn.getInputStream();  
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {  
                    sb.append(line);  
                }  
                checkResponse(sb.toString());
                logger.debug(sb.toString());  
            }
        } 
        catch (Exception e)
        {
            logger.debug("", e);
            throw e;
        }
        finally
        {
            if(null != osw)   
            {
                osw.close();
            }
            if(null != br)   
            {
                br.close();
            }
        }
        return false;
    }
    
    public boolean checkResponse(String xmlResponse)
    {
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        Element root = null;
        List<Element> submitRes = null;
        String respCode = "";
        String errDesc = "";
        
        if(!StringUtil.isEmpty(xmlResponse))
        {
            Reader in = new StringReader(xmlResponse);
            try
            {
                doc = (Document) builder.build(in);
            }
            catch (JDOMException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            root = doc.getRootElement();
            submitRes = root.getChildren();    
            logger.info("============= parser response xml =============");
            if(null != submitRes && submitRes.size() > 0)
            {
                for(Element elm : submitRes)
                {
                    if(elm.getName().equals("ResultCode"))
                    {
                        respCode = elm.getText();
                    }
                    else if(elm.getName().equals("ResultText"))
                    {
                        errDesc = elm.getText();
                    }
                    else if(elm.getName().equals("MessageId"))
                    {
                        
                    }
                    logger.info(elm.getName() + " value:" + elm.getText());      
                }
            }
            logger.info("============= parser response xml =============");
        }
            
        if(!respCode.equalsIgnoreCase("00000"))
        {
            logger.error("ResultCode:" + respCode + ", ResultText:" + errDesc);
            logger.error("send Sms Error");
            return false;
        }

        return true;
    }
    
    public String genReqXml(String SysId, String SrcAddress, String destAddress, String smsBody) throws UnsupportedEncodingException
    {
        byte[] msg = Base64.encodeBase64(smsBody.getBytes("UTF-8"));
        String xml = "";
        Element reqMsg = new Element("SmsSubmitReq");
        Document doc = new Document(reqMsg);    
        reqMsg.addContent(getElement("SysId", SysId));
        reqMsg.addContent(getElement("SrcAddress", SrcAddress));
        reqMsg.addContent(getElement("DestAddress", destAddress));
        reqMsg.addContent(getElement("SmsBody", new String(msg)));
        reqMsg.addContent(getElement("DrFlag", "true"));
        reqMsg.addContent(getElement("FirstFailFlag", "false"));
        XMLOutputter xmlOutput = new XMLOutputter();
        xml = xmlOutput.outputString(doc);
        return xml;
    }
    
    public void getSmsSetting(LMSContext ctx) throws SQLException
    {
        MailContext mctx = (MailContext)ctx;
        TbMailSettingInfo mailSetInfo = mctx.getMailSetInfo();
        String where = "status = '1' and data_flow = '"+ mailSetInfo .getDataFlow() + "'";
        Vector<TbSmsSettingInfo> result = new Vector<TbSmsSettingInfo>();
        TbSmsSettingMgr smsMgr = new TbSmsSettingMgr(ctx.getConnection());
        smsMgr.queryMultiple(where, result);
       
        for(int i = 0; i < result.size(); i++)
        {
            TbSmsSettingInfo smsInfo = result.get(i);
            if(null != smsInfo)
            {
                if(!StringUtil.isEmpty(smsInfo.getDataFlow()))
                {
                    smsUrl = smsInfo.getUrl();
                    sysId = smsInfo.getSysId();
                    srcAddress = smsInfo.getSrcAddress();
                    destAddress = destAddressParser(smsInfo.getDestAddress()); 
                    logger.debug(smsInfo);
                }
            }
        }
    }
    
    public String[] destAddressParser(String psrserStr)
    {
        if(!StringUtil.isEmpty(psrserStr))
        {
            String parserAry = psrserStr.replace(","," ");
            return ArraysUtil.toStrArray(parserAry);
        }
        return null;
    }
    
    public Element getElement(String name, String text) 
    {
        Element element = new Element(name);
        element.setText(text);
        return  element;
    }
    
    public String getSysId()
    {
        return sysId;
    }

    public void setSysId(String sysId)
    {
        this.sysId = sysId;
    }

    public String getSrcAddress()
    {
        return srcAddress;
    }

    public void setSrcAddress(String srcAddress)
    {
        this.srcAddress = srcAddress;
    }

    public String[] getDestAddress()
    {
        return destAddress;
    }

    public void setDestAddress(String[] destAddress)
    {
        this.destAddress = destAddress;
    }
    
    public String getSmsUrl()
    {
        return smsUrl;
    }

    public void setSmsUrl(String smsUrl)
    {
        this.smsUrl = smsUrl;
    }
}