/**
 * changelog
 *
 */

/*
 * Version: 1.0.0
 * Date: 2009-04-01
 */

package tw.com.hyweb.cp.ws.enduser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.util.string.StringUtil;



/**
 * <pre>
 * Layer2Util
 * </pre>
 * <p/>
 * author: Anny
 */
public class EndUserUtil
{
	private static Logger log = Logger.getLogger(EndUserUtil.class);
	
	public static Map<String, String> jsonToMap(String jsonString)
	{
    	
		Map<String, String> fieldsJson = JSONObject.fromObject(jsonString);
		
    	return fieldsJson;
	}
	
	public static String getCondtionString(Map<String, String> condMap)
	{
		String pkCondtion = "";
		
		for (String key : condMap.keySet())
		{
			if (!isNullOrEmpty(pkCondtion))
				pkCondtion = pkCondtion + " and";
				
			pkCondtion = pkCondtion + " " + key + " = " + StringUtil.toSqlValueWithNSQuote(condMap.get(key));
		}
		
		return pkCondtion;
	}
	
	
	public static TbMemberInfo getMemberInfo(Connection connection, String memId)
	{
		TbMemberMgr memMgr = new TbMemberMgr(connection);
		
		TbMemberInfo memInfo = null;
		try {
			
			memInfo = memMgr.querySingle(memId);
			
		} catch (SQLException e) {
			
			log.debug(e);
		}
		
		return memInfo;
		
	}
	
	public static TbCardInfo getCardInfo(Connection connection, String cardNo)
	{
		TbCardPK pk = new TbCardPK();
		pk.setCardNo(cardNo);
		pk.setExpiryDate(getCardPk(cardNo).get("EXPIRY_DATE"));
		
		log.debug("card pk: ");
		log.debug(pk.toString());
		
		TbCardMgr cardMgr = new TbCardMgr(connection);
		
		TbCardInfo cardInfo = null;
		try {
			
			cardInfo = cardMgr.querySingle(pk);
			log.info("cardInfo:"+cardInfo);
			
		} catch (SQLException e) {
			
			log.debug(e);
		}
		
		return cardInfo;
		
	}
	
	public static Map<String, String> getCardPk(String cardNo)
	{
		Map<String, String> cardPKMap = new HashMap<String, String>();
		cardPKMap.put("CARD_NO", cardNo);
		cardPKMap.put("EXPIRY_DATE", "99991231");
		
		return cardPKMap;
	}
	
	public static Map<String, String> getCardPk(String cardNo,String expiryDate)
	{
		if(isNullOrEmpty(expiryDate) == true)
			expiryDate="99991231";
		log.debug(expiryDate);
		Map<String, String> cardPKMap = new HashMap<String, String>();
		cardPKMap.put("CARD_NO", cardNo);
		cardPKMap.put("EXPIRY_DATE", expiryDate);
		
		return cardPKMap;
	}
	
	public static String objectToJasonString (Object object)
	{
		JSONArray queryJson = JSONArray.fromObject(object);
		log.debug(queryJson.toString());
		
		return queryJson.toString();
	}
	
	public static Map<String, String> filterToRegularFields(Map<String, String> infoMap, Map<String, String> regularBaseFields)
	{
		Map<String, String> regularFields = new HashMap<String, String> ();
		
		for (String key : regularBaseFields.keySet())
		{
			if (infoMap.containsKey(key))
				regularFields.put(key, infoMap.get(key));
		}
		
		return regularFields;
	}
	
    public static boolean isNullOrEmpty(String value)
    {
        if (value==null || value.trim().equals(""))
       	 return true;

        return false;
    }
}
