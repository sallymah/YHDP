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
    	/*??????????????????*/
        checkCardInfo(conn);
        
        /*??????????????????*/
        checkMemInfo(conn);

    	/*?????????????????????  ????????????S ??????ID??????*/
        //checkCreditDebit(connection);
        
        /* ??????Bonus id(field05)*/
        //checkBonusInfo(connection);
        
        /*??????????????????????????????*/
        //checkSponsorInfo(connection);
        
        /*????????????????????????????????????*/
        //checkMerchInfo(connection);
        
        /*??????TB_CARD_BAL?????????????????????????????????????????????????????????*/
        //checkCardBalQty(connection);

        /*?????????????????? <= 0 ?????????*/
        checkTtlAmt(conn);
        
        return descInfos;      
    }
    
}
