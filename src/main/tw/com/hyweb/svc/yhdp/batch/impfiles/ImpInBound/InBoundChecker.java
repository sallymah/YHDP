package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpInBound;

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
 * ImpInBound(For YHDP)
 * </pre>
 * author:Kevin
 */
public class InBoundChecker
{
    private static Logger log = Logger.getLogger(InBoundChecker.class);
    
	private final InBoundData inBoundData;
	private final Map<String, FieldInfo> inBoundFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public InBoundChecker(InBoundData inBoundData, Map<String, FieldInfo> inBoundFieldInfos)
    {
    	this.inBoundData = inBoundData; 
    	this.cardNo = inBoundData.getFileData().get("CARD_NO");
    	this.inBoundFieldInfos = inBoundFieldInfos;
    }

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(inBoundData.getCardInfoCount() == 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, inBoundFieldInfos.get("CARD_NO") ,"cardInfo is null:" + cardNo);
        }
        else if (!"0".equals(inBoundData.getCardInfo().getLifeCycle()) 
        		&& !"1".equals(inBoundData.getCardInfo().getLifeCycle()) 
        		&& !"".equals(inBoundData.getCardInfo().getLifeCycle()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, inBoundFieldInfos.get("CARD_NO"), "card life cycle is not valid:" + cardNo);
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
