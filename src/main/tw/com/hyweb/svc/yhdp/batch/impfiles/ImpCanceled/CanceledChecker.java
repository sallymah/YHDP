package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCanceled;

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
 * ImpAppload(For YHDP)
 * </pre>
 * author:Kevin
 */
public class CanceledChecker
{
    private static Logger log = Logger.getLogger(CanceledChecker.class);
    
	private final CanceledData canceledData;
	private final Map<String, FieldInfo> canceledFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public CanceledChecker(CanceledData canceledData, Map<String, FieldInfo> canceledFieldInfos)
    {
    	this.canceledData = canceledData; 
    	this.cardNo = canceledData.getFileData().get("CARD_NO");
    	this.canceledFieldInfos = canceledFieldInfos;
    }

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(canceledData.getCardInfoCount() == 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, canceledFieldInfos.get("CARD_NO") ,"cardInfo is null:" + cardNo);
        }
        
        if (!"R".equals(canceledData.getCardInfo().getStatus()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, canceledFieldInfos.get("CARD_NO"), "card status is not valid:" + cardNo);
        }
        
        if (!"7".equals(canceledData.getCardInfo().getLifeCycle()) && !"8".equals(canceledData.getCardInfo().getLifeCycle()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, canceledFieldInfos.get("CARD_NO"), "card life cycle is not valid:" + cardNo);
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
    
    public List<ErrorDescInfo> checker(Connection connection) throws Exception
    {	
    	/*?????????????????????  ????????????S ??????ID??????*/
        //checkCreditDebit(connection);
        
        /*??????????????????*/
        checkCardInfo(connection);

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
