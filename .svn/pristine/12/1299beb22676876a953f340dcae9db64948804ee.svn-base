package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBlackList;

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
public class BlackListChecker
{
    private static Logger log = Logger.getLogger(BlackListChecker.class);
    
	private final BlackListData blackListData;
	private final Map<String, FieldInfo> blackListFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public BlackListChecker(BlackListData blackListData, Map<String, FieldInfo> blackListFieldInfos)
    {
    	this.blackListData = blackListData; 
    	this.cardNo = blackListData.getFileData().get("CARD_NO");
    	this.blackListFieldInfos = blackListFieldInfos;
    }

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(blackListData.getCardInfoCount() == 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, blackListFieldInfos.get("CARD_NO") ,"cardInfo is null:" + cardNo);
        }
        
        if (!"2".equals(blackListData.getCardInfo().getStatus()) && !"3".equals(blackListData.getCardInfo().getStatus()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, blackListFieldInfos.get("CARD_NO"), "card status is not valid:" + cardNo);
        }
        
        if (!"0".equals(blackListData.getCardInfo().getLifeCycle()) && !"1".equals(blackListData.getCardInfo().getLifeCycle()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, blackListFieldInfos.get("CARD_NO"), "card life cycle is not valid:" + cardNo);
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
        /*檢查卡片主檔*/
        checkCardInfo(connection);

        return descInfos;     
    }
    
}
