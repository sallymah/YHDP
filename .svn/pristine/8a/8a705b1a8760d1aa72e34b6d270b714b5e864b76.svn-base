/* @author ShaneTsao
 * Version: 1.0.0
 * Date: 2012-08-31
 *//*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 ************************************************
 *
 */
package tw.com.hyweb.starbucks.wstest.querybonus;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 星巴克卡片掛失測試
 *
 * @author <a href=”mailto:shane.tsao@mail.hyweb.com.tw”>Shane Tsao</a>
 * @version $Revision: 1 $ $Date: 2012/08/31 $
 * @see		
 */
public class QuerybonusJunit{
	@Before
	public void initLog4j() {
		DOMConfigurator.configureAndWatch("config/log4j.xml", 60000);
		// System.setProperty("log4j.configuration", "config/log4j.xml");
	}

	@Test
	public void testQuerybonus() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "tw/com/hyweb/starbucks/wstest/querybonus/querybonus-client-beans.xml" });
		tw.com.hyweb.starbucks.wstest.querybonus.gen.QueryBonusService client = (tw.com.hyweb.starbucks.wstest.querybonus.gen.QueryBonusService) context.getBean("client");
		
		Client c = ClientProxy.getClient(client);
		HTTPConduit httpConduit = (HTTPConduit) c.getConduit();
		HTTPClientPolicy policy = (HTTPClientPolicy) context.getBean("clientPolicy");
		httpConduit.setClient(policy);

		HashMap<String,String> raw = new HashMap<String,String>();
		raw.put("cardNo", "8813243000000016");
        raw.put("mid", "811009999900001");
        raw.put("tid", "81101001");
		raw.put("expiryDate", "99991231");
		/*raw.put("cardNo", "5100413010002001");
		raw.put("mid", "103972869180220");
		raw.put("tid", "00000001");*/
		raw.put("callTime", "20120916000000");
		JSONObject jsonObj = JSONObject.fromObject(raw);
		System.out.println("=============== Request Message ===========");
		System.out.println(jsonObj.toString());
		String respMsg = client.queryBonus(jsonObj.toString());
		//String respMsg = client.lost("");

		System.out.println("=============== Response Message ===========");
		System.out.println(respMsg);
		
		
		
        JSONObject jsonObject = JSONObject.fromObject(respMsg);
        Map<String,String> map = jsonObject;
        Object bonusListStr = (Object)map.get("bonusList");
        if(bonusListStr != null)
        {
            JSONArray jsonArray = JSONArray.fromObject(bonusListStr);
            
            if(jsonArray != null && jsonArray.size() > 0)
            {
            
                for(int i = 0; i < jsonArray.size(); i++)
                {
                    Map bonusVOMap = JSONObject.fromObject(jsonArray.get(i));
                    if(bonusVOMap != null && bonusVOMap.size() > 0)
                    {
                        Object bonusVoStr = (Object) bonusVOMap.get("bonusVO");
                        if(bonusVoStr != null)
                        {
                            System.out.println(bonusVoStr.toString());
                            //JSONObject bonusObject = JSONObject.fromObject(bonusVoStr);
                            
                        }
                    }
                }
            }
        }
	}
}
