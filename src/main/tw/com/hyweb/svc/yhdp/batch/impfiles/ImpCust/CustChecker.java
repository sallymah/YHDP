package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCust;

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
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpCust(For YHDP)
 * </pre>
 * author:Kevin
 */
public class CustChecker
{
    private static Logger log = Logger.getLogger(CustChecker.class);
    
	private final CustData custData;
	private final Map<String, FieldInfo> custFieldInfos;
	private String cardNo;
	private String countryCode;
	private List descInfos = new ArrayList();

    public CustChecker(CustData custData, Map<String, FieldInfo> custFieldInfos)
    {
    	this.custData = custData; 
    	this.cardNo = custData.getFileData().get("CARD_NO");
    	this.countryCode = custData.getFileData().get("COUNTRY_CODE");
    	this.custFieldInfos = custFieldInfos;
    }

    private void checkCardInfo(Connection conn) throws SQLException
    {
        if(custData.getCardInfoCount() == 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("CARD_NO") ,"cardInfo is null:" + cardNo);
        }
    }
    
   private void checkCountryCodeInfo(Connection conn) throws SQLException
   {
	   log.info(countryCode);
	   if (!StringUtil.isEmpty(countryCode)){
	       if(custData.getCountryCodeCount() == 0)
	       {
	       	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("COUNTRY_CODE") ,"CountryCode is null or Error:" + countryCode);
	       }
	   }
   }
    
    private void checkTelcoCardInfo(Connection conn) throws SQLException
    {
        if(custData.isTelcoCard())
        {
        	if (!custData.isMobileCount()){
        		addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("CARD_NO") ,"Mobile is null in Telco Card:" + cardNo);
        	}
        	else{
        		if (custData.getMobileCount() > 0){
        			addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("CARD_NO") ,"Mobile already exists in Telco Card:" + cardNo);
        		}
        	}
        }
    }
    
    private void checkMerchInfo(Connection conn) throws SQLException
    {
        if(StringUtil.isEmpty(custData.getMerchId()))
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("CARD_NO") ,"merch id does not exist:" + custData.getFileData().get("MERCH_ID"));
        }
    }
    
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    
    private void addErrorDescInfo(Connection connection, String rcode, FieldInfo info, String content) throws SQLException
    {
		ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(rcode, info, content);   
        descInfos.add(descInfo);
    }
    
    public List<ErrorDescInfo> checker(Connection conn) throws Exception
    {	
    	/*檢查扣入帳單位  單位類型S 單位ID必填*/
        //checkCreditDebit(connection);
        
        /*檢查卡片主檔*/
        checkCardInfo(conn);
        
        /*檢查客戶主檔*/
        //checkCustInfo(conn);
        
        /*檢查是否為電信卡，若為電信卡則手機號必填，且手機號不可以被註冊過*/
        checkTelcoCardInfo(conn);
        
        /*檢查MerchId是否存在*/
        checkMerchInfo(conn);
        
        /*20170316檢查國別碼*/
        checkCountryCodeInfo(conn);
        /* 檢查Bonus id(field05)*/
        //checkBonusInfo(connection);
        
        /*檢查出資單位是否合法*/
        //checkSponsorInfo(connection);
        
        /*檢查指定加值特店是否合法*/
        //checkMerchInfo(connection);
        
        /*檢查TB_CARD_BAL這一個點數存不存在、調帳後餘額是否合法*/
        //checkCardBalQty(connection);
        
        return descInfos;    
    }
    
}
