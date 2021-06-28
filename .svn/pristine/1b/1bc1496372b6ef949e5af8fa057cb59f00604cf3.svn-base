package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpAppload;

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

/**
 * <pre>
 * ImpCust(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ApploadChecker
{
    private static Logger log = Logger.getLogger(ApploadChecker.class);
    
	private final ApploadData apploadData;
	private final Map<String, FieldInfo> apploadFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public ApploadChecker(ApploadData apploadData, Map<String, FieldInfo> apploadFieldInfos)
    {
    	this.apploadData = apploadData; 
    	this.apploadFieldInfos = apploadFieldInfos;
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
    	//checkCardInfo(conn);
        
        /*檢查兌換金額*/
        //checkExchangeLimit(conn);

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

/*    private void checkExchangeLimit(Connection conn) throws SQLException {
    	int currCardBal = apploadData.getSumBonusQty() + Integer.valueOf(apploadData.getFileData().get("BONUS_QTY"));
    	if( currCardBal > exchangeLimit)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, apploadFieldInfos.get("BONUS_QTY") ,"curren card balance(CARD BALANCE + BONUS_QTY) > Exchange Limit:" + currCardBal);
        }	
	}*/

	private void checkCardInfo(Connection conn) throws SQLException
    {
        if(apploadData.getCardInfo() == null)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, apploadFieldInfos.get("CARD_NO") ,"cardInfo is null:" + cardNo);
        }
    }    
}
