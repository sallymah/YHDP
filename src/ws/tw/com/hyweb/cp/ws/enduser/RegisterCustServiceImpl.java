package tw.com.hyweb.cp.ws.enduser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.util.DbUtil;
                                                                                              
@WebService(endpointInterface = "tw.com.hyweb.cp.ws.enduser.RegisterCustService",serviceName="RegisterCustService")

public class RegisterCustServiceImpl implements RegisterCustService{

	private static final Logger log = Logger.getLogger(RegisterCustServiceImpl.class);

	private final ServiceObject custServiceObject;
	private final ServiceObject assServiceObject;
    private final String custTable = "TB_CUST";
    private final String assTable = "TB_ASSOCIATOR";
    private Connection connection = null;
	
	public RegisterCustServiceImpl(Map<String, String> custFields, Map<String, String> assFields, DataSource dataSource) throws SQLException
	{		
		this.custServiceObject = new ServiceObject(custTable, custFields);
		this.assServiceObject = new ServiceObject(assTable, assFields);
		connection = dataSource.getConnection();
	}

	/* 查詢此卡片是否是註冊為會員(或已經做註冊)*/
	public String CardRegisterOrNot(String cardNo) throws Exception {
		
		TbCardInfo cardInfo = EndUserUtil.getCardInfo(connection, cardNo);
		
		if (cardInfo!=null)
		{
			TbMemberInfo info = EndUserUtil.getMemberInfo(connection, cardInfo.getMemId());
			
			log.info("RegAss:" + info.getRegAss());
			log.info("CustId:" + cardInfo.getCustId());
			
			if ("1".equals(info.getRegAss()))
			{
				
				if (!EndUserUtil.isNullOrEmpty(cardInfo.getCustId()))
				{
					return EndUserRcode.DOUBLE_REGCUST;
				}
				else
				{
					return EndUserRcode.OK;
				}
			}
			else
			{
				return EndUserRcode.NOSUPPORT_REGCUST;
			}
		}
		
		log.info("cardInfo is null");
		return EndUserRcode.NOFOUND_MASTERDATA;
	}

	/*編輯會員資料*/
	public String EditCust(String cardNo, String custFields) throws Exception {
		
		String rcode;
		try {
			
			rcode = null;
			Map<String, String> editMap = genDefaultCust();
			editMap.put("MEM_ID", getMemIdByCard(cardNo));
			editMap.put("ASSOCIATOR_ID", cardNo);
			editMap.put("CUST_ID", getCustIdByCard(cardNo));
			editMap.putAll(EndUserUtil.jsonToMap(custFields));
			
			//Edit TB_CUST
			rcode = custServiceObject.edit(connection, custServiceObject.filterToRegularUpdateFields(editMap));
			
			if (EndUserRcode.OK.equals(rcode))
			{
				//Edit TB_ASSOCIATOR
				rcode = assServiceObject.edit(connection, assServiceObject.filterToRegularUpdateFields(editMap));
				
			}

			if (!EndUserRcode.OK.equals(rcode))
			{
				connection.rollback();
			}
			
			connection.commit();
			
			return rcode;
			
		} catch (Exception e) {
			
			log.info(e);
			connection.rollback();
			throw new Exception(e);
		}
	}

	/*新增會員資料*/
	public String NewCust(String cardNo, String custFields) throws Exception {
		
		String rcode = null;
		
		try {
			//新增TB_CUST
			Map<String, String> newMap = new HashMap<String, String>();

			//CUST_ID = 發卡單位+卡號
			newMap.putAll(genDefaultCust());
			newMap.putAll(genDefaultAss());
			newMap.put("MEM_ID", getMemIdByCard(cardNo));
			newMap.put("ASSOCIATOR_ID", cardNo);
			newMap.put("CUST_ID", newMap.get("MEM_ID") + cardNo);
			newMap.putAll(EndUserUtil.jsonToMap(custFields));
			
			//更新TB_CARD.CUST_ID
			if (updateCardCustId(cardNo, newMap.get("CUST_ID")))
			{
				//insert TB_CUST
				rcode = custServiceObject.insert(connection, custServiceObject.filterToRegularInsertFields(newMap));
				
				if (EndUserRcode.OK.equals(rcode))
				{
					//insert TB_ASSOCIATOR
					rcode = assServiceObject.insert(connection, assServiceObject.filterToRegularInsertFields(newMap));		
				}
			}
			else
			{
				rcode = EndUserRcode.NOFOUND_MASTERDATA;
			}
			
			if (!EndUserRcode.OK.equals(rcode))
			{
				connection.rollback();
			}
			
			connection.commit();
			
			return rcode;

		} catch (Exception e) {
			
			log.info(e);
			connection.rollback();
			throw new Exception(e);
		}
		
	}

	/*查詢客戶資料*/
	public String QueryCust(String cardNo) throws Exception {
		
		Map<String, String> custPkMap= genDefaultCust();
		custPkMap.put("CUST_ID", getCustIdByCard(cardNo));
		
		log.debug("custPkMap" + custPkMap);
		
		if (custPkMap.get("CUST_ID") == null)
			return EndUserUtil.objectToJasonString(new HashMap<String, String>());
		else
			return custServiceObject.queryPkString(connection, custPkMap);
	}
	

	private String getMemIdByCard(String cardNo)
	{
		TbCardInfo cardInfo = EndUserUtil.getCardInfo(connection, cardNo);
		
		log.debug("cardInfo:" + cardInfo.toString());
		if (cardInfo!=null)
			return cardInfo.getMemId();
		else
			return null;
	}
	
	private String getCustIdByCard(String cardNo)
	{

		TbCardInfo cardInfo = EndUserUtil.getCardInfo(connection, cardNo);
		
		log.debug("cardInfo:" + cardInfo);
		if (cardInfo!=null)
			return cardInfo.getCustId();
		else
			return null;
		
	}
	
	private boolean updateCardCustId(String cardNo, String custId)
	{
		String updateSql = "update TB_CARD set CUST_ID = '" + custId + "' where " + 
		                     EndUserUtil.getCondtionString(EndUserUtil.getCardPk(cardNo)) + " and (CUST_ID is null or CUST_ID = '')";
		
		try {
			SqlResult rs = DbUtil.sqlAction(updateSql, connection);
			
			if (rs.getRecordCount() == 1)
				return true;
			else
				return false;
			
		} catch (SQLException e) {
			
			log.error(e.fillInStackTrace());
			return false;
		}
	}
	
	
	private Map<String, String> genDefaultCust()
	{
		Map<String, String> defaultMap = new HashMap<String, String>();
		defaultMap.put("REGION_ID", "TWN");
		
		return defaultMap;
	}
	
	private Map<String, String> genDefaultAss()
	{
		Map<String, String> defaultMap = new HashMap<String, String>();
		defaultMap.put("ASSOCIATOR_LEVEL", "N");
		
		return defaultMap;
	}
}
