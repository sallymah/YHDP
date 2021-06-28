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
package tw.com.hyweb.starbucks.wstest.querycard;

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

import tw.com.hyweb.starbucks.wstest.querycard.gen.QueryCardService;

/**
 * 星巴克交易查詢測試
 *
 * @author <a href=”mailto:shane.tsao@mail.hyweb.com.tw”>Shane Tsao</a>
 * @version $Revision: 1 $ $Date: 2012/08/31 $
 * @see		
 */
public class QueryCardJunit{
	@Before
	public void initLog4j() {
		DOMConfigurator.configureAndWatch("config/log4j.xml", 60000);
		// System.setProperty("log4j.configuration", "config/log4j.xml");
	}

	@Test
	public void testQueryCar() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "tw/com/hyweb/starbucks/wstest/querycard/querycard-client-beans.xml" });
		QueryCardService client = (QueryCardService) context.getBean("client");
		
		Client c = ClientProxy.getClient(client);
		HTTPConduit httpConduit = (HTTPConduit) c.getConduit();
		HTTPClientPolicy policy = (HTTPClientPolicy) context.getBean("clientPolicy");
		httpConduit.setClient(policy);

		HashMap<String,String> raw = new HashMap<String,String>();
		raw.put("cardNo", "8813243000000016");
		raw.put("mid", "811009999900001");
        raw.put("tid", "81101001");
		//raw.put("cardNo", "51004130100020010");
		raw.put("expiryDate", "99991231");
		/*raw.put("mid", "103972869180220");
		raw.put("tid", "00000001");*/
		raw.put("callTime", "20120916000000");
		JSONObject jsonObj = JSONObject.fromObject(raw);
		System.out.println("=============== QueryCar Request Message ===========");
		System.out.println(jsonObj.toString());
		String respMsg = client.queryCard(jsonObj.toString());
		//String respMsg = client.queryCard("");

		System.out.println("=============== Response Message ===========");
		System.out.println(respMsg);
	}
}
