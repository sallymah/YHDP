package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBTCB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;

/**
 * <pre>
 * ImpBTCB(For YHDP)
 * </pre>
 * author:Kevin
 */
public class BTCBChecker
{
    private static Logger log = Logger.getLogger(BTCBChecker.class);
    
	private final BTCBData btcbData;
	private final Map<String, FieldInfo> btcbFieldInfos;

	private List descInfos = new ArrayList();

    public BTCBChecker(BTCBData btcbData, Map<String, FieldInfo> btcbFieldInfos)
    {
    	this.btcbData = btcbData; 
    	this.btcbFieldInfos = btcbFieldInfos;
    }
    
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    
    private void checkCardInfo(Connection conn) throws Exception
    {
        if(btcbData.getCardInfoCnt() == 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, btcbFieldInfos.get("CARD_NO") ,"card information is null:" + (String)btcbData.getFileData().get("CARD_NO"));
        }
    }
    
    private void checkMemInfo(Connection conn) throws SQLException
    {
        if(btcbData.getMemberInfoCnt() == 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, btcbFieldInfos.get("MEM_NAME") ,"member information is null:" + (String)btcbData.getFileData().get("MEM_NAME"));
        }
    }
    
    private void checkMerchInfo(Connection conn) throws SQLException
    {
        if(btcbData.getMerchInfoCnt() == 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, btcbFieldInfos.get("MERCH_LOC_NAME") ,"merchant information is null:" + (String)btcbData.getFileData().get("MERCH_LOC_NAME"));
        }
    }
    
    private void addErrorDescInfo(Connection connection, String rcode, FieldInfo info, String content) throws SQLException
    {
		ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(rcode, info, content);   
        descInfos.add(descInfo);
    }
    
    public List<ErrorDescInfo> checker(Connection conn) throws Exception
    {	
    	/*檢查卡片主檔*/
        checkCardInfo(conn);
    	
    	/*檢查會員主檔*/
        checkMemInfo(conn);
	
    	/*檢查特店特檔*/
        checkMerchInfo(conn);

    	/*檢查扣入帳單位  單位類型S 單位ID必填*/
        //checkCreditDebit(connection);
        
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
