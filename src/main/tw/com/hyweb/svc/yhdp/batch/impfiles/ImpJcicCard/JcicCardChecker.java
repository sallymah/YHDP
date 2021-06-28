package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpJcicCard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.core.cp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.cp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;

/**
 * <pre>
 * ImpCust(For YHDP)
 * </pre>
 * author:Kevin
 */
public class JcicCardChecker
{
    private static Logger log = Logger.getLogger(JcicCardChecker.class);
    
	private final JcicCardData jcicCardData;
	private final Map<String, FieldInfo> jcicCardFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public JcicCardChecker(JcicCardData jcicCardData, Map<String, FieldInfo> jcicCardFieldInfos)
    {
    	this.jcicCardData = jcicCardData; 
    	this.jcicCardFieldInfos = jcicCardFieldInfos;
    }

    private void checkCardInfo(Connection conn) throws SQLException
    {
        if(jcicCardData.getCardInfoCount() != 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, jcicCardFieldInfos.get("CARD_NO") ,"card No is not valid:" + jcicCardFieldInfos.get("CARD_NO") );
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
        
        
        return descInfos;    
    }
    
}
