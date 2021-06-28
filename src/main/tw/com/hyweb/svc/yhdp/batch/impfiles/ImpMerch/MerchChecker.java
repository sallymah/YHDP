package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMerch;

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
 * author:Kevin
 */
public class MerchChecker
{
    private static Logger log = Logger.getLogger(MerchChecker.class);
    
	private final MerchData merchData;
	private final Map<String, FieldInfo> merchFieldInfos;

	private List descInfos = new ArrayList();

    public MerchChecker(MerchData merchData, Map<String, FieldInfo> merchFieldInfos)
    {
    	this.merchData = merchData; 
    	this.merchFieldInfos = merchFieldInfos;
    }

    private void checkMemInfo(Connection conn) throws SQLException
    {
        if(merchData.getMemInfoCount() == 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, merchFieldInfos.get("MEM_NAME") ,"cardInfo is null:" + merchData.getFileData().get("MEM_NAME"));
        }
    }
    
    private void checkMerchInfo(Connection conn) throws SQLException
    {
        if(merchData.getMerchInfoCount() != 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, merchFieldInfos.get("MEM_NAME") ,"merch id already exists:" + merchData.getMerchId());
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
        /*檢查會員主檔*/
        checkMemInfo(conn);
        
        /*檢查特店主檔*/
        checkMerchInfo(conn);
        
        return descInfos;    
    }
    
}
