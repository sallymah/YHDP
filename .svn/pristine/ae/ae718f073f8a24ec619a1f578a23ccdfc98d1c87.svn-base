package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpJcicCust;

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
import tw.com.hyweb.util.BatchUtils;

/**
 * <pre>
 * ImpCust(For YHDP)
 * </pre>
 * author:Kevin
 */
public class JcicCustChecker
{
    private static Logger log = Logger.getLogger(JcicCustChecker.class);
    
	private final JcicCustData jcicCustData;
	private final Map<String, FieldInfo> jcicCustFieldInfos;
	private String cardNo;

	private List descInfos = new ArrayList();

    public JcicCustChecker(JcicCustData jcicCustData, Map<String, FieldInfo> jcicCustFieldInfos)
    {
    	this.jcicCustData = jcicCustData; 
    	this.jcicCustFieldInfos = jcicCustFieldInfos;
    }

    private void checkCustInfo(Connection conn) throws SQLException
    {
        if(jcicCustData.getCustInfoCount() == 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, jcicCustFieldInfos.get("PERSON_ID") ,"cust is not in system"  );
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
    	checkCustInfo(conn);
        
        
        return descInfos;    
    }
    
}
