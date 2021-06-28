package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpHappyGo;

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
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
public class HappyGoChecker
{
    private static Logger log = Logger.getLogger(HappyGoChecker.class);
    
	private final HappyGoData happyGoData;
	private final Map<String, FieldInfo> happyGoFieldInfos;

	private List descInfos = new ArrayList();

    public HappyGoChecker(HappyGoData happyGoData, Map<String, FieldInfo> happyGoFieldInfos)
    {
    	this.happyGoData = happyGoData; 
    	this.happyGoFieldInfos = happyGoFieldInfos;
    }

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(happyGoData.getCardInfoCount() != 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, happyGoFieldInfos.get("BARCODE1") ,"card number has exists: " + happyGoData.getCardNo());
        }
    }
    
    private void checkHgCardMapInfo(Connection connection) throws Exception {
		 if(happyGoData.getHgCardInfoCount() != 0)
	        {
	        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, happyGoFieldInfos.get("BARCODE1") ,"happy go card number has exists: " + happyGoData.getCardNo());
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
        
        /*檢查聯名卡主檔*/
        checkHgCardMapInfo(conn);

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
