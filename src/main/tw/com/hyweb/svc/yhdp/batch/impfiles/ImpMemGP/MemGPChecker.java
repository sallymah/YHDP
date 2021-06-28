package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMemGP;

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
 * ImpMemGP(For YHDP)
 * </pre>
 * author:Kevin
 */
public class MemGPChecker
{
    private static Logger log = Logger.getLogger(MemGPChecker.class);
    
	private final MemGPData memGPData;
	private final Map<String, FieldInfo> memGPFieldInfos;

	private List descInfos = new ArrayList();

    public MemGPChecker(MemGPData memGPData, Map<String, FieldInfo> memGPFieldInfos)
    {
    	this.memGPData = memGPData; 
    	this.memGPFieldInfos = memGPFieldInfos;
    }

    private void checkMemGPInfo(Connection connection) throws SQLException
    {
        if(memGPData.getMemGPInfoCount() == 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, memGPFieldInfos.get("MEM_GROUP_NAME") ,"memGPInfo is null:" + memGPData.getFileData().get("MEM_GROUP_NAME"));
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
    	/*檢查扣入帳單位  單位類型S 單位ID必填*/
        //checkCreditDebit(connection);
        
        /*檢查會員群組主檔*/
        checkMemGPInfo(connection);

        /* 檢查Bonus id(field05)*/
        //checkBonusInfo(connection);
        
        /*檢查出資單位是否合法*/
        //checkSponsorInfo(connection);
        
        /*檢查指定加值特店是否合法*/
        //checkMerchInfo(connection);
        
        /*檢查TB_CARD_BAL這一個點數存不存在、調帳後餘額是否合法*/
        //checkCardBalQty(connection);

        
        return descInfos;
        
    }
    
}
