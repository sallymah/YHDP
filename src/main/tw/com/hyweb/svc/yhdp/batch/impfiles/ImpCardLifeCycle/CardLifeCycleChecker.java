package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCardLifeCycle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbCardInfo;

/**
 * <pre>
 * ImpCardLifeCycle(For YHDP)
 * </pre>
 * author:Kevin
 */
public class CardLifeCycleChecker
{
    private static Logger log = Logger.getLogger(CardLifeCycleChecker.class);
    
	private final CardLifeCycleData cardLifeCycleData;
	private final Map<String, FieldInfo> cardLifeCycleFieldInfos;

	private List descInfos = new ArrayList();

    public CardLifeCycleChecker(CardLifeCycleData cardLifeCycleData, Map<String, FieldInfo> cardLifeCycleFieldInfos)
    {
    	this.cardLifeCycleData = cardLifeCycleData; 
    	this.cardLifeCycleFieldInfos = cardLifeCycleFieldInfos;
    }

    private void checkCardInfo(Connection connection, String cardNo, String storeType) throws Exception
    { 	
    	TbCardInfo cardInfo = cardLifeCycleData.getCardInfo(cardNo);

        if(cardInfo == null)
        {
        	String msg = "cardInfo is null:" + cardNo;
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, cardLifeCycleFieldInfos.get("CARD_NO"), msg);
        }
        else 
        {
        	String preLifeCycle = cardInfo.getPreviousLifeCycle();
        	String lifeCycle = cardInfo.getLifeCycle();
        	String status = cardInfo.getStatus();
        	
        	if (storeType.equals("1")) {
        		if(!preLifeCycle.equals("") && !preLifeCycle.equals("0") && !preLifeCycle.equals("1"))
        		{
        			String msg = "previous card(" + cardNo + ") life cycle is not valid:" + preLifeCycle;
        			addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, cardLifeCycleFieldInfos.get("CARD_NO"), msg);
        		}
        	}
        	else if(storeType.equals("2")) {
        		if(!lifeCycle.equals("1"))
            	{
        			String msg = "card(" + cardNo + ") life cycle is not valid:" + lifeCycle;
        			addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, cardLifeCycleFieldInfos.get("CARD_NO"), msg);
            	}
        	}
        	else if(storeType.equals("3")) {
        		if(!lifeCycle.equals("1"))
            	{
        			String msg = "card(" + cardNo + ") life cycle is not valid:" + lifeCycle;
        			addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, cardLifeCycleFieldInfos.get("CARD_NO"), msg);
            	}
        	}
        	else if(storeType.equals("4")) {
        		if(!lifeCycle.equals("2") && !lifeCycle.equals("4")) 
            	{
        			String msg = "card(" + cardNo + ") life cycle is not valid:" + lifeCycle;
        			addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, cardLifeCycleFieldInfos.get("CARD_NO"), msg);
            	}
        	}
        	else {	
        		if (!"R".equals(status))
    	        {
        			String msg = "card(" + cardNo + ") status is not valid:" + status;
    	        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, cardLifeCycleFieldInfos.get("CARD_NO"), msg);
    	        }
        		else {
        			if(!lifeCycle.equals("7") && !lifeCycle.equals("8"))
                	{
            			String msg = "card(" + cardNo + ") life cycle is not valid:" + lifeCycle;
            			addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, cardLifeCycleFieldInfos.get("CARD_NO"), msg);
                	}
        		}
        	}
        }
    }
    
    private void checkCustomerId(Connection connection, String storeType) throws Exception 
    {
    	String customerId = cardLifeCycleData.getCardLifeCycleBean().getCustomerId();
    	//if (storeType.equals("1") || storeType.equals("3") || storeType.equals("4")) {
    	if (storeType.equals("3") || storeType.equals("4")) {
    		if(customerId.length() != 8)
    		{
    			String msg = "customer id(" + customerId + ") length should be 8:" + customerId.length();
    			addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, cardLifeCycleFieldInfos.get("CUSTOMER_ID"), msg);
    		}
    	}
	}
    
    public static boolean isBlankOrNull(String value) 
    {
		return (value == null || value.trim().equals(""));
	}
    
    private void addErrorDescInfo(Connection connection, String rcode, FieldInfo info, String content) throws SQLException
    {
		ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(rcode, info, content);   
        descInfos.add(descInfo);
    }
    
    public List<ErrorDescInfo> checker(Connection connection) throws Exception
    {	
    	String cardNo = cardLifeCycleData.getCardLifeCycleBean().getCardNo();
    	String storeType = cardLifeCycleData.getCardLifeCycleBean().getStoreType();
    	
        /*檢查卡片主檔*/
        checkCardInfo(connection, cardNo, storeType);
        
        /*檢查銷售客戶*/
        checkCustomerId(connection, storeType);
       
        return descInfos;  
    } 
}
