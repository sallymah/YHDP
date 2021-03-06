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
    	/*?????????????????????  ????????????S ??????ID??????*/
        //checkCreditDebit(connection);
        
        /*??????????????????*/
        checkCardInfo(conn);
        
        /*?????????????????????*/
        checkHgCardMapInfo(conn);

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
