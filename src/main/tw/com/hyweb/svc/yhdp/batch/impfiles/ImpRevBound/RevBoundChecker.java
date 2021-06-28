package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpRevBound;

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
 * ImpReturnBound(For YHDP)
 * </pre>
 * author:Kevin
 */
public class RevBoundChecker
{
    private static Logger log = Logger.getLogger(RevBoundChecker.class);
    
	private final RevBoundData revBoundData;
	private final Map<String, FieldInfo> revBoundFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public RevBoundChecker(RevBoundData revBoundData, Map<String, FieldInfo> revBoundFieldInfos)
    {
    	this.revBoundData = revBoundData; 
    	this.cardNo = revBoundData.getFileData().get("CARD_NO");
    	this.revBoundFieldInfos = revBoundFieldInfos;
    }

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(revBoundData.getCardInfoCount() == 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, revBoundFieldInfos.get("CARD_NO") ,"cardInfo is null: " + cardNo);
        }
        else if (!"1".equals(revBoundData.getCardInfo().getLifeCycle()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, revBoundFieldInfos.get("CARD_NO"), "card life cycle is not valid: " + cardNo);
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
