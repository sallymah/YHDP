package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpErpt;

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
 */
public class ErptChecker
{
    private static Logger log = Logger.getLogger(ErptChecker.class);
    
	private final ErptData erptData;
	private final Map<String, FieldInfo> termFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public ErptChecker(ErptData termData, Map<String, FieldInfo> termFieldInfos)
    {
    	this.erptData = termData; 
    	this.termFieldInfos = termFieldInfos;
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
    	//checkMerchInfo(conn);
        
        
        return descInfos;    
    }
    
}
