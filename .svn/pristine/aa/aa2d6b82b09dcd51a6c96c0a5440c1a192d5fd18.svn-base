package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpDTXN;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;

/**
 * <pre>
 * ImpBTCB(For YHDP)
 * </pre>
 * author:Kevin
 */
public class DTXNChecker
{
    private static Logger log = Logger.getLogger(DTXNChecker.class);
    
	private final DTXNData dTxnData;
	private final Map<String, FieldInfo> dTxnFieldInfos;
	private final TbInctlInfo tbInctlInfo;

	private List descInfos = new ArrayList();

    public DTXNChecker(DTXNData dTxnData, TbInctlInfo tbInctlInfo, Map<String, FieldInfo> dTxnFieldInfos)
    {
    	this.dTxnData = dTxnData; 
    	this.tbInctlInfo = tbInctlInfo;
    	this.dTxnFieldInfos = dTxnFieldInfos;
    }
    
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    
    private void checkCardInfo(Connection conn) throws Exception
    {
        if(dTxnData.getCardInfoCnt() == 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, dTxnFieldInfos.get("CARD_NO") ,"card information is null:" + (String)dTxnData.getFileData().get("CARD_NO"));
        }
    }
    
    private void checkMemInfo(Connection conn) throws Exception
    {
        if(dTxnData.getMemInfoCnt(tbInctlInfo) == 0)
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, null,"member information is null(BANK_ID):" + tbInctlInfo.getMemId());
        }
    }
    
    private void checkTtlAmt(Connection conn) throws Exception
    {	
        if((double)dTxnData.getFileData().get("TTL_AMT") <=0 )
        {
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, dTxnFieldInfos.get("TTL_AMT"),"TTL_AMT is smaller or equal to 0:" + (double)dTxnData.getFileData().get("TTL_AMT"));
        }
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
        
        /*檢查會員主檔*/
        checkMemInfo(conn);

    	/*檢查扣入帳單位  單位類型S 單位ID必填*/
        //checkCreditDebit(connection);
        
        /* 檢查Bonus id(field05)*/
        //checkBonusInfo(connection);
        
        /*檢查出資單位是否合法*/
        //checkSponsorInfo(connection);
        
        /*檢查指定加值特店是否合法*/
        //checkMerchInfo(connection);
        
        /*檢查TB_CARD_BAL這一個點數存不存在、調帳後餘額是否合法*/
        //checkCardBalQty(connection);

        /*檢查簽帳金額 <= 0 需踢腿*/
        checkTtlAmt(conn);
        
        return descInfos;      
    }
    
}
