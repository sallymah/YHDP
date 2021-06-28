package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpOutBound;

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
 * ImpOutBound(For YHDP)
 * </pre>
 * author:Kevin
 */
public class OutBoundChecker
{
    private static Logger log = Logger.getLogger(OutBoundChecker.class);
    
	private final OutBoundData outBoundData;
	private final Map<String, FieldInfo> outBoundFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public OutBoundChecker(OutBoundData outBoundData, Map<String, FieldInfo> outBoundFieldInfos)
    {
    	this.outBoundData = outBoundData; 
    	this.cardNo = outBoundData.getFileData().get("CARD_NO");
    	this.outBoundFieldInfos = outBoundFieldInfos;
    }

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(outBoundData.getCardInfoCount() == 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, outBoundFieldInfos.get("CARD_NO") ,"cardInfo is null: " + cardNo);
        }
        
        log.info("cardNo: " + outBoundData.getCardInfo().getCardNo() + ", life_cycle: " + outBoundData.getCardInfo().getLifeCycle());
        if (!"".equals(outBoundData.getCardInfo().getLifeCycle()) && !"0".equals(outBoundData.getCardInfo().getLifeCycle()) && !"1".equals(outBoundData.getCardInfo().getLifeCycle()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, outBoundFieldInfos.get("CARD_NO"), "card life cycle is not valid: " + cardNo);
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
