package tw.com.hyweb.starbucks.wstest.balancetransfer;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.json.JSONObject;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class balancetransferJunit
{
    @Before
    public void initLog4j() {
        DOMConfigurator.configureAndWatch("config/log4j.xml", 60000);
        // System.setProperty("log4j.configuration", "config/log4j.xml");
    }

    @Test
    public void testBalTransfer() {
       
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "tw/com/hyweb/starbucks/wstest/balancetransfer/baltransfer-client-beans.xml" });
        tw.com.hyweb.starbucks.wstest.balancetransfer.gen.BalanceTransferService client = (tw.com.hyweb.starbucks.wstest.balancetransfer.gen.BalanceTransferService) context.getBean("client");
        
        Client c = ClientProxy.getClient(client);
        HTTPConduit httpConduit = (HTTPConduit) c.getConduit();
        HTTPClientPolicy policy = (HTTPClientPolicy) context.getBean("clientPolicy");
        httpConduit.setClient(policy);
        
        ArrayList<HashMap<String, Object>> listmain = new ArrayList<HashMap<String, Object>>();
        
        HashMap<String,String> bonusinfo = new HashMap<String,String>();
        bonusinfo.put("bonusID", "3100000228");//ecash
        bonusinfo.put("startDate", "00010101");
        bonusinfo.put("endDate", "99991231");
        bonusinfo.put("qunatity", "1000");
        listmain.add(setRespBeanMap("bonusVO", bonusinfo));
        
        HashMap<String,String> bonusinfo1 = new HashMap<String,String>();
        bonusinfo1.put("bonusID", "3400000012");
        bonusinfo1.put("startDate", "00010101");
        bonusinfo1.put("endDate", "99991231");
        bonusinfo1.put("qunatity", "10");
        listmain.add(setRespBeanMap("bonusVO", bonusinfo1));
        
        HashMap<String,Object> raw = new HashMap<String,Object>();
        raw.put("cardNo", "8813243000000015");
        raw.put("mid", "811009999900001");
        raw.put("tid", "81101001");
        raw.put("expiryDate", "99991231");
        raw.put("origCardNo", "8813243000000016");
        raw.put("origExpiryDate", "99991231");
        raw.put("callTime", "20120916000000");
        raw.put("bonusList", listmain);

        JSONObject jsonObject = JSONObject.fromObject(raw);
        String jsonStr = jsonObject.toString();

        System.out.println("=============== Request Message ===========");
        System.out.println(jsonStr);
        String respMsg = client.balanceTransfer(jsonStr);

        System.out.println(respMsg);
        System.out.println("=============== Response Message ===========");
        
    }
    
    public HashMap<String, Object> setRespBeanMap(String keyName, Object o) {
        HashMap<String, Object> txnVO = new HashMap<String, Object>();
        txnVO.put(keyName, o);
        return txnVO;
    }
}
