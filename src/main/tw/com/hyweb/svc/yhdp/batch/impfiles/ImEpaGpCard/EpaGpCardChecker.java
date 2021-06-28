package tw.com.hyweb.svc.yhdp.batch.impfiles.ImEpaGpCard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;

public class EpaGpCardChecker
{
    private static Logger log = Logger.getLogger(EpaGpCardChecker.class);
    
	private final EpaGpCardData epaGpCardData;
	private final Map<String, FieldInfo> epaGpCardFieldInfos;

	private List descInfos = new ArrayList();

	public EpaGpCardChecker(EpaGpCardData epaGpCardData, Map<String, FieldInfo> epaGpCardFieldInfos)
	{
    	this.epaGpCardData = epaGpCardData; 
    	this.epaGpCardFieldInfos = epaGpCardFieldInfos;
	}

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(epaGpCardData.getCardInfoCount() == 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, epaGpCardFieldInfos.get("CARD_PHYSICAL_ID") ,"card number is not exists: " + epaGpCardData.getCardNo());
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
        /*檢查卡片主檔*/
        checkCardInfo(conn);
        return descInfos;
        
    }
    
}
