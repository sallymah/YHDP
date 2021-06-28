package tw.com.hyweb.cp.ws.enduser;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuery;
import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuerySingleData;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ServiceObject {

	private static final Logger log = Logger.getLogger(ServiceObject.class);
	private final Map<String,String> allFields;
    private final Map<String,String> queryFields;
    private final Map<String,String> insertFields;
    private final Map<String,String> updateFields;
    private final Map<String,String> pkFields;
    private final String table;
	
	/*Field's Value: 1111  新增,修改,查詢,pk*/
	public ServiceObject(String table, Map<String, String> Fields)
	{
		this.allFields=Fields;
		this.queryFields=genQueryField(Fields);
		this.insertFields=genInsertField(Fields);
		this.updateFields=genUpdateField(Fields);
		this.pkFields=genPkField(Fields);
		this.table = table;
	}
	
	public Map<String, String> getQueryFields() {
		return queryFields;
	}


	public Map<String, String> getInsertFields() {
		return insertFields;
	}

	public Map<String, String> getUpdateFields() {
		return updateFields;
	}
	
	public Map<String, String> getAllFields() {
		return allFields;
	}
	
	public Map<String, String> getPkFields() {
		return pkFields;
	}
	
	public Map<String,String> genQueryField(Map<String, String> Fields)
	{
		Map<String,String> result = new HashMap();
		
		for (String key : Fields.keySet())
		{
			if ("1".equals(Fields.get(key).substring(2, 3)))
				result.put(key, Fields.get(key));
		}
		
		return result;
	}
	
	public Map<String,String> genInsertField(Map<String, String> Fields)
	{
		Map<String,String> result = new HashMap();
		
		for (String key : Fields.keySet())
		{
			if ("1".equals(Fields.get(key).substring(0, 1)))
				result.put(key, Fields.get(key));
		}
		
		return result;		
	}
	
	public Map<String,String> genUpdateField(Map<String, String> Fields)
	{
		Map<String,String> result = new HashMap();
		
		for (String key : Fields.keySet())
		{
			if ("1".equals(Fields.get(key).substring(1, 2)))
				result.put(key, Fields.get(key));
		}
		
		return result;	
	}
	
	public Map<String,String> genPkField(Map<String, String> Fields)
	{
		Map<String,String> result = new HashMap();
		for (String key : Fields.keySet())
		{
			if ("1".equals(Fields.get(key).substring(3, 4)))
				result.put(key, Fields.get(key));
		}
		return result;	
	}
	
	public Boolean checkHasPk(Map<String, String> info)
	{	
		for (String key : pkFields.keySet())
		{
			if (!info.containsKey(key))
			{
				log.info("table:" + table);
				log.info("info:" + info);
				log.info("pk:" + pkFields);
				return false;
			}
		}
		
		return true;
	}
	
	public Boolean checkFieldRegular(Map<String, String> info, Map<String, String> regularInfo)
	{
		for (String key : info.keySet())
		{
			if (!regularInfo.containsKey(key) && !pkFields.containsKey(key))
			{
				log.info("table:" + table);
				log.info("info:" + info);
				log.info("pk:" + pkFields);
				log.info("regularInfo:" + regularInfo);
				return false;
			}
		}
		
		return true;
	}
	
	public String genUpdateSql(Map<String, String> updateMap)
	{
		String editValue = "";
		for (String key : updateMap.keySet())
		{
			if (updateFields.containsKey(key))
			{
				if (!EndUserUtil.isNullOrEmpty(editValue))
					editValue = editValue + ",";
					
				editValue = editValue + " " + key + " = " + StringUtil.toSqlValueWithNSQuote(updateMap.get(key));
			}

		}		
		
		String sql = "update " + table + " set " + editValue + " where " + EndUserUtil.getCondtionString(getPkMap(updateMap));
		
		return sql;
		
	}

	public String insert(Connection connection, Map<String, String> insertInfo) throws Exception {
	
		String rcode = "";
		
		if (checkHasPk(insertInfo))
		{
			if (checkFieldRegular(insertInfo, insertFields))
			{
				DbUtil.sqlAction(genInsertSql(insertInfo), connection);
				rcode = EndUserRcode.OK;
			}
			else
			{
				rcode = EndUserRcode.FIELD_UNREGULAR;
			}
		}
		else
		{
			rcode = EndUserRcode.PK_ERR;
		}
		
		return rcode;
	}
	
	public String genInsertSql(Map<String, String> insertMap)
	{
		String insertValue = "";
		String insertField = "";
		for (String key : insertMap.keySet())
		{
			if (!EndUserUtil.isNullOrEmpty(insertField))
			{
				insertField = insertField + ",";
				insertValue = insertValue + ",";
			}
			
			insertField = insertField + " " + key;
			insertValue = insertValue + StringUtil.toSqlValueWithNSQuote(insertMap.get(key));

		}		
		
		String sql = "insert into " + table + "(" + insertField + ") values (" + insertValue + ")";
		
		log.info("insert sql: " + sql);
		return sql;
		
	}
	
	public String edit(Connection connection, Map<String, String> editInfo) throws Exception {
		
		String rcode = "";
		
		if (checkHasPk(editInfo))
		{
			if (checkFieldRegular(editInfo, updateFields))
			{
				DbUtil.sqlAction(genUpdateSql(editInfo), connection);
				rcode = EndUserRcode.OK;
			}
			else
			{
				rcode = EndUserRcode.FIELD_UNREGULAR;
			}
		}
		else
		{
			rcode = EndUserRcode.PK_ERR;
		}
		
		return rcode;
	}
	
	
	public String queryPkString(Connection connection, Map<String, String> pkMap) throws Exception
	{
		return EndUserUtil.objectToJasonString(queryPk(connection, pkMap));
	}

	public Map<String, String> queryPk(Connection connection, Map<String, String> pkMap) throws Exception
	{
		String fieldString = "";
		
		for (String key : queryFields.keySet())
		{
			if (!EndUserUtil.isNullOrEmpty(fieldString))
				fieldString = fieldString + ",";
					
			fieldString = fieldString + " " + key ;
		}	
		
		String selectSql = "select " + fieldString + " from " + table + " where " + EndUserUtil.getCondtionString(pkMap);
		
		return executeQuerySingleData(connection, selectSql);	
	}
	
	public String queryAllString(Connection connection, Map<String, String> condMap) throws Exception
	{
		return EndUserUtil.objectToJasonString(queryAll(connection, condMap));
	}
	
	public List<Map<String, String>> queryAll(Connection connection, Map<String, String> condMap) throws Exception
	{
		log.debug("queryAll");
		return query(connection, condMap, "", "", 0);
	}
	
	public String queryString(Connection connection, Map<String, String> condMap, String where, String order, int maxCnt) throws Exception
	{
		return EndUserUtil.objectToJasonString(query(connection, condMap, where, order, maxCnt));
	}
	
	public List<Map<String, String>> query(Connection connection, Map<String, String> condMap, String where, String order, int maxCnt) throws Exception
	{
		String fieldString = "";
		
		for (String key : queryFields.keySet())
		{
			if (!EndUserUtil.isNullOrEmpty(fieldString))
				fieldString = fieldString + ",";
					
			fieldString = fieldString + " " + key ;
		}	
		
		String condString = EndUserUtil.getCondtionString(condMap);
		
		if (!EndUserUtil.isNullOrEmpty(condString) && !EndUserUtil.isNullOrEmpty(where))
			condString = condString + " and ";
		
		condString = condString + where;
		
		String selectSql = "select " + fieldString + " from " + table + " where " + condString;
		
		if (!EndUserUtil.isNullOrEmpty(order))
			selectSql = selectSql + " order by " + order;
		
		if (maxCnt > 0)
			selectSql = "select " + fieldString + " from (" + selectSql + ") where rownum <=" + maxCnt; 
		
		
		return executeQuery(connection, selectSql);
	}

	public Map<String, String> getPkMap(Map<String, String> infoMap)
	{
		Map<String, String>  pkMap = new HashMap<String, String>();
		
		for (String key : pkFields.keySet())
		{
			pkMap.put(key, infoMap.get(key));
		}
		
		return pkMap;
	}
	
	public Map<String, String> filterToRegularFields(Map<String, String> infoMap)
	{
		return EndUserUtil.filterToRegularFields(infoMap, allFields);
	}
	
	
	public Map<String, String> filterToRegularInsertFields(Map<String, String> infoMap)
	{
		return EndUserUtil.filterToRegularFields(infoMap, insertFields);
	}
	
	public Map<String, String> filterToRegularUpdateFields(Map<String, String> infoMap)
	{
		Map<String, String> forFilterFields = new HashMap<String, String>();
		forFilterFields.putAll(pkFields);
		forFilterFields.putAll(updateFields);
		
		return EndUserUtil.filterToRegularFields(infoMap, forFilterFields);
	}
}
