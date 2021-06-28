package tw.com.hyweb.cp.ws.enduser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.service.db.info.TbBonusInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbBonusMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.util.DbUtil;
                                                                                              
@WebService(endpointInterface = "tw.com.hyweb.cp.ws.enduser.BalanceService",serviceName="BalanceService")

public class BalanceServiceImpl implements BalanceService{

	private static final Logger log = Logger.getLogger(BalanceServiceImpl.class);

	private final ServiceObject cardBalanceService;
    private final String table = "TB_CARD_BAL";
    private Connection connection = null;
	
	public BalanceServiceImpl(Map<String, String> Fields, DataSource dataSource) throws SQLException
	{		
		this.cardBalanceService = new ServiceObject(table, Fields);
		connection = dataSource.getConnection();
	}

	public String QueryCardBalance(String cardNo,String expiryDate) throws Exception {
		
		log.debug(expiryDate);
		List<Map<String, String>> balInfoList = new ArrayList();
		
		for (Map<String, String> balInfo : cardBalanceService.queryAll(connection, EndUserUtil.getCardPk(cardNo,expiryDate)))
		{
			balInfo.put("BONUS_NAME", getBonusName(balInfo.get("BONUS_ID")));
			log.debug(getBonusName(balInfo.get("BONUS_ID")));
			balInfoList.add(balInfo);
		}
		
		return EndUserUtil.objectToJasonString(balInfoList);
	}
	
	private String getBonusName(String bonusId) throws SQLException
	{
		TbBonusMgr mgr = new TbBonusMgr(connection);
		TbBonusInfo bonusInfo = mgr.querySingle(bonusId);
		
		if (bonusInfo == null)
			return "";
		else
			return bonusInfo.getBonusName();
	}

}
