package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpAdjustmentTxn;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.core.yhdp.common.misc.*;
import tw.com.hyweb.service.db.info.*;
import tw.com.hyweb.service.db.mgr.*;
import tw.com.hyweb.util.ReleaseResource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * ImpAppload(For THIG, Only 以卡歸戶,主機紅利)
 * </pre>
 * author:Anny
 */
public class AdjustTxnChecker
{
    private static Logger log = Logger.getLogger(AdjustTxnChecker.class);
    
	private final AdjustTxnData adjustTxnData;
	private final Map<String, FieldInfo> adjustTxnFieldInfos;

	private List descInfos = new ArrayList();

    public AdjustTxnChecker(AdjustTxnData adjustTxnData, Map<String, FieldInfo> adjustTxnFieldInfos)
    {
    	this.adjustTxnData = adjustTxnData; 
    	this.adjustTxnFieldInfos = adjustTxnFieldInfos;
    }
    
    private void checkCreditDebit(Connection connection) throws SQLException
    {	
       if (adjustTxnData.getCardInfo()!= null)
       {
    	   
	       if (adjustTxnData.getFileData().get("CREDIT_UNIT").equals("S") && isBlankOrNull(adjustTxnData.getFileData().get("CREDIT_ID")) )
	       {
	    	   addErrorDescInfo(connection, Constants.RCODE_2708_MANDATORY_ERR, adjustTxnFieldInfos.get("CREDIT_ID"), "credit Id is not valid:" + adjustTxnData.getFileData().get("CREDIT_ID"));
	       }
	       else if (adjustTxnData.getFileData().get("DEBIT_UNIT").equals("S") && isBlankOrNull(adjustTxnData.getFileData().get("DEBIT_ID")) )
	       {
	    	   addErrorDescInfo(connection, Constants.RCODE_2708_MANDATORY_ERR, adjustTxnFieldInfos.get("DEBIT_ID"), "debit Id is not valid:" + adjustTxnData.getFileData().get("DEBIT_ID"));
	       }
	       
       }
    }

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(adjustTxnData.getCardInfo() == null)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("CARD_NO") ,"cardInfo is null:" + adjustTxnData.getFileData().get("CARD_NO"));
        }
        else if (!"1".equals(adjustTxnData.getCardInfo().getStatus()) && !"2".equals(adjustTxnData.getCardInfo().getStatus()) && !"3".equals(adjustTxnData.getCardInfo().getStatus()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("CARD_NO"), "card status is not valid:" + adjustTxnData.getFileData().get("CARD_NO"));
        }
    }
    
    private void checkSponsorInfo(Connection connection) throws SQLException
    {	
       if (adjustTxnData.getCardInfo()!= null)
       {
	       if (!adjustTxnData.getFileData().get("ACQ_MEM_ID").equals(adjustTxnData.getCardInfo().getMemId()) && !checkAcqDefInfo(connection, adjustTxnData.getCardInfo().getMemId(), adjustTxnData.getFileData().get("ACQ_MEM_ID")))
	       {
	    	   addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("ACQ_MEM_ID"), "Sponsor Id is not valid:" + adjustTxnData.getFileData().get("ACQ_MEM_ID"));
	       }
       }
    }
    
    private void checkMerchInfo(Connection conn) throws SQLException
    {	
    	TbMerchMgr mgr = new TbMerchMgr(conn);
    	TbMerchInfo merchInfo = mgr.querySingle(adjustTxnData.getFileData().get("MERCH_ID"));
    	
    	if (merchInfo == null)
    	{
    		addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("MERCH_ID"), "merchInfo is null:" + adjustTxnData.getFileData().get("MERCH_ID"));
    	}
    }
    
    private boolean checkAcqDefInfo(Connection connection, String issMemId, String acqMemId) throws SQLException {
    	
    	TbAcqDefPK pk = new TbAcqDefPK();
    	pk.setAcqMemId(acqMemId);
    	pk.setIssMemId(issMemId);
    	
        TbAcqDefInfo acqDefInfo = new TbAcqDefMgr(connection).querySingle(pk);
        
        if (acqDefInfo != null)
        	return true;
        else
        	return false;    	

    }
    
    private void checkBonusInfo(Connection conn) throws SQLException {

    	String bounsId = adjustTxnData.getFileData().get("BONUS_ID");
    	
    	TbBonusMgr mgr = new TbBonusMgr(conn);
    	TbBonusInfo bonusInfo = mgr.querySingle(bounsId);
    	
    	if (bonusInfo == null || (!bounsId.startsWith("3") && !bounsId.startsWith("7")))
    	{
    		addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("BONUS_ID"), "bonusInfo is null:" + adjustTxnData.getFileData().get("BONUS_ID"));
    	}
    }
    
    private void checkCardBalQty(Connection connection) throws SQLException
    {	
       if (adjustTxnData.getCardInfo()!= null)
       {
    	   Statement stmt = null;
           ResultSet rs = null;
           String cardBalQty = null;
           String seqnoSql = "SELECT CR_BONUS_QTY - DB_BONUS_QTY + BAL_BONUS_QTY FROM TB_CARD_BAL " +
           					"WHERE CARD_NO = '"+adjustTxnData.getCardInfo().getCardNo()+"' " +
           					"AND EXPIRY_DATE = '"+adjustTxnData.getCardInfo().getExpiryDate()+"' " +
           					"AND BONUS_ID = '"+adjustTxnData.getFileData().get("BONUS_ID")+"' " +
           					"AND BONUS_SDATE = '"+adjustTxnData.getFileData().get("BONUS_SDATE")+"' " +
           					"AND BONUS_EDATE = '"+adjustTxnData.getFileData().get("BONUS_EDATE")+"' ";
           try {
        	   stmt = connection.createStatement();
        	   log.debug("seqnoSql: "+seqnoSql);
        	   rs = stmt.executeQuery(seqnoSql);
        	   while (rs.next()) {
        		   cardBalQty = rs.getString(1);
        	   }
           }
           finally {
        	   ReleaseResource.releaseDB(null, stmt, rs);
           }

	       if ( cardBalQty != null ){
	           double qty =  Double.parseDouble(cardBalQty);
	           
	    	   if ( adjustTxnData.getFileData().get("SIGN").equals("1") ){
		           qty = qty - Double.parseDouble(adjustTxnData.getFileData().get("BONUS_QTY"));
		           
		           if (qty < 0)
		        	   addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("BONUS_QTY"), "CR-DB+QTY-BONUS_QTY < 0:" + adjustTxnData.getFileData().get("BONUS_QTY"));
	    	   }
	    	   else if (adjustTxnData.getFileData().get("SIGN").equals("0")){
	    		   if (adjustTxnData.getFileData().get("BONUS_ID").startsWith("31")){
		    		   Statement stmt2 = null;
		               ResultSet rs2 = null;
		               String cardBalLimit = "0";
		               String seqnoSql2 = "SELECT CARD_BAL_LIMIT FROM TB_MEMBER " +
		               					"WHERE MEM_ID = '"+adjustTxnData.getFileData().get("ACQ_MEM_ID")+"' ";
		               try {
		            	   stmt2 = connection.createStatement();
		            	   log.debug("seqnoSql: "+seqnoSql2);
		            	   rs2 = stmt2.executeQuery(seqnoSql2);
		            	   while (rs2.next()) {
		            		   cardBalLimit = rs2.getString(1);
		            	   }
		               }
		               finally {
		            	   ReleaseResource.releaseDB(null, stmt2, rs2);
		               }
		               
		               double limit =  Double.parseDouble(cardBalLimit);
		    		   
		    		   qty = qty + Double.parseDouble(adjustTxnData.getFileData().get("BONUS_QTY"));
		    		   
		    		   log.debug("qty:"+qty+"  limit: "+limit);
		
			           if (limit != 0 && qty > limit)
			        	   addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("BONUS_QTY"), "CR-DB+QTY+BONUS_QTY > cardBalLimit:" + adjustTxnData.getFileData().get("BONUS_QTY"));
		    	   
	    		   }
	    		   else{
//	    			   如果不是31點，不需要檢測金額上限
	    			   ;
	    		   }
	    	   }
	    	   else
	    		   addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("SIGN"), "SIGN is error:" + adjustTxnData.getFileData().get("SIGN"));
	       }
	       else
	    	   addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, adjustTxnFieldInfos.get("BONUS_ID"), "BONUS_ID or BONUS_DATE is error:" + adjustTxnData.getFileData().get("BONUS_ID"));
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
        
        /*檢查主檔*/
        checkCardInfo(connection);

        /* 檢查Bonus id(field05)*/
        checkBonusInfo(connection);
        
        /*檢查出資單位是否合法*/
        checkSponsorInfo(connection);
        
        /*檢查指定加值特店是否合法*/
        checkMerchInfo(connection);
        
        /*檢查TB_CARD_BAL這一個點數存不存在、調帳後餘額是否合法*/
        checkCardBalQty(connection);

        
        return descInfos;
        
    }
    
}
