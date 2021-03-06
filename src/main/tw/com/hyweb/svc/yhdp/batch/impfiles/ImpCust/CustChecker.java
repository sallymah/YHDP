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
    	/*?????????????????????  ????????????S ??????ID??????*/
        //checkCreditDebit(connection);
        
        /*??????????????????*/
        checkCardInfo(conn);
        
        /*??????????????????*/
        //checkCustInfo(conn);
        
        /*????????????????????????????????????????????????????????????????????????????????????????????????*/
        checkTelcoCardInfo(conn);
        
        /*??????MerchId????????????*/
        checkMerchInfo(conn);
        
        /*20170316???????????????*/
        checkCountryCodeInfo(conn);
        /* ??????Bonus id(field05)*/
        //checkBonusInfo(connection);
        
        /*??????????????????????????????*/
        //checkSponsorInfo(connection);
        
        /*????????????????????????????????????*/
        //checkMerchInfo(connection);
        
        /*??????TB_CARD_BAL?????????????????????????????????????????????????????????*/
        //checkCardBalQty(connection);
        
        return descInfos;    
    }
    
}
