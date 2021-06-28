package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCanceled;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;

public class CanceledData
{
    private static Logger log = Logger.getLogger(CanceledData.class);

    private final Map<String, String> fileData;
    private Vector<TbCardInfo> result = new Vector<TbCardInfo>();
    private int count;
    
    private String sysDate = DateUtils.getSystemDate();
    private String sysTime = DateUtils.getSystemTime();
       
    public CanceledData(Connection connection, Map<String, String> fileData) throws SQLException
    {
    	this.fileData = fileData;
    	this.count = getCardInfo(connection , fileData.get("CARD_NO"));	
    }
    
    private int getCardInfo(Connection connection, String cardNumber) throws SQLException
    {
        TbCardInfo info = new TbCardInfo();
        info.setCardNo(cardNumber);

        return new TbCardMgr(connection).queryMultiple(info, result);
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getCardInfoCount() {
		return count;
	}
	
	public TbCardInfo getCardInfo() {	
		return result.get(0);
	}

    public List handleCanceled(Connection connection, String batchDate) throws Exception {
    	
    	List sqls = new ArrayList();
    	
    	String updateCardSql = updateCardStatus(batchDate);
    	log.info("sql: " + updateCardSql);
        sqls.add(updateCardSql);
        
        return sqls;
    }   
    
    private String updateCardStatus(String batchDate) {
   	
    	String lifeCycle="9";
    	String revLifeCycle = result.get(0).getLifeCycle();
    	String revStatus = result.get(0).getStatus();
    	String sql = "UPDATE TB_CARD SET LIFE_CYCLE='" + lifeCycle 
    			+ "', PREVIOUS_LIFE_CYCLE='" + revLifeCycle 
    			+ "', PREVIOUS_STATUS='" + revStatus 
    			+"', UPT_DATE='" + sysDate 
    			+ "', UPT_TIME='" + sysTime 
    			+ "', UPT_USERID='batch', APRV_DATE='" + sysDate 
    			+ "', APRV_TIME='" + sysTime 
    			+ "', APRV_USERID='batch', UPDATE_TYPE='2' WHERE CARD_NO='" + fileData.get("CARD_NO") + "'";
    	
        return sql;
    }
    
    public List addCardUpt(Connection connection, String batchDate) throws SQLException 
	{
    	List sqls = new ArrayList();
    	
		StringBuilder insertSqlCmd = new StringBuilder();
		
		insertSqlCmd.delete(0, insertSqlCmd.length());
		insertSqlCmd.append("INSERT INTO TB_CARD_UPT(");
		insertSqlCmd.append("ACCT_ID,ACTIVE_CARD_FLAG,ACTIVE_DATE,ACTIVE_FLAG,APP_ACCT_ID,APRV_DATE,APRV_TIME,APRV_USERID,ASSOCIATOR_ID,");
		insertSqlCmd.append("AUTO_RELOAD_FLAG,AUTO_RELOAD_VALUE,BAL_TRANSFER_FLAG,BILL_CUT_DAY,BILL_VALID_DATE,BOX_NO,CARDHOLDER_ID,");
		insertSqlCmd.append("CARD_CAT_ID,CARD_FEE,CARD_LEVEL,CARD_NO,CARD_OPEN_OWNER,CARD_OWNER,CARD_PLAN,CARD_PRODUCT,CARD_TYPE_ID,");
		insertSqlCmd.append("CLEAN_DATE,CO_BRAND_ENT_ID,CREDIT_ACTIVE_DATE,CREDIT_APPROVAL_DATE,CREDIT_CARD_NO,CREDIT_EXPIRY_DATE,");
		insertSqlCmd.append("CUSTOMER_ID,CUST_ID,DELIVERY_STATUS,EXPIRY_DATE,FAIL_CODE,FIRST_RELOAD_DATE,FIRST_TXN_DATE,HG_CARD_NO,");
		insertSqlCmd.append("INACTIVE_DATE,INFILE,KEY_VERSION,LAST_CARD_NO,LAST_EDC_DATE_TIME,LAST_EXPIRY_DATE,LAST_LMS_INVOICE_NO,");
		insertSqlCmd.append("LAST_RELOAD_DATE,LAST_TXN_DATE,LAST_YEAR_CNS_AMT,LIFE_CYCLE,MASS_BATCH_NO,MBR_REG_DATE,MEM_ID,MIFARE_UL_UID,");
		insertSqlCmd.append("MIN_BAL_AMT,NEW_BILL_CUT_DAY,OUTFILE,PBM_PROC_DATE,PB_VALID_EDATE,PB_VALID_SDATE,PERSO_BATCH_NO,PRELOAD_AMT,");
		insertSqlCmd.append("PRELOAD_DW_DATE,PREVIOUS_STATUS,PRIMARY_CARD,REGION_ID,REG_DATE,REG_TIME,RETURN_CARD_WAY,SALE_WAY,");
		insertSqlCmd.append("SHOW_CARD_NO,STATUS,STATUS_UPDATE_DATE,TOTAL_RELOAD_AMT,TOTAL_USE_AMT,UD1,UD2,UD3,UD4,UD5,UPDATE_TYPE,");
		insertSqlCmd.append("UPT_DATE,UPT_TIME,UPT_USERID,VIP_FLAG,WARRANTY_PERIOD,upt_status,aprv_status,");
		insertSqlCmd.append("PREVIOUS_LIFE_CYCLE, IS_SYNC_HG)");
		insertSqlCmd.append(" SELECT");
		insertSqlCmd.append(" ACCT_ID,ACTIVE_CARD_FLAG,ACTIVE_DATE,ACTIVE_FLAG,APP_ACCT_ID,APRV_DATE,APRV_TIME,APRV_USERID,ASSOCIATOR_ID,");
		insertSqlCmd.append("AUTO_RELOAD_FLAG,AUTO_RELOAD_VALUE,BAL_TRANSFER_FLAG,BILL_CUT_DAY,BILL_VALID_DATE,BOX_NO,CARDHOLDER_ID,");
		insertSqlCmd.append("CARD_CAT_ID,CARD_FEE,CARD_LEVEL,CARD_NO,CARD_OPEN_OWNER,CARD_OWNER,CARD_PLAN,CARD_PRODUCT,CARD_TYPE_ID,");
		insertSqlCmd.append("CLEAN_DATE,CO_BRAND_ENT_ID,CREDIT_ACTIVE_DATE,CREDIT_APPROVAL_DATE,CREDIT_CARD_NO,CREDIT_EXPIRY_DATE,");
		insertSqlCmd.append("CUSTOMER_ID,CUST_ID,DELIVERY_STATUS,EXPIRY_DATE,FAIL_CODE,FIRST_RELOAD_DATE,FIRST_TXN_DATE,HG_CARD_NO,");
		insertSqlCmd.append("INACTIVE_DATE,INFILE,KEY_VERSION,LAST_CARD_NO,LAST_EDC_DATE_TIME,LAST_EXPIRY_DATE,LAST_LMS_INVOICE_NO,");
		insertSqlCmd.append("LAST_RELOAD_DATE,LAST_TXN_DATE,LAST_YEAR_CNS_AMT,LIFE_CYCLE,MASS_BATCH_NO,MBR_REG_DATE,MEM_ID,MIFARE_UL_UID,");
		insertSqlCmd.append("MIN_BAL_AMT,NEW_BILL_CUT_DAY,OUTFILE,PBM_PROC_DATE,PB_VALID_EDATE,PB_VALID_SDATE,PERSO_BATCH_NO,PRELOAD_AMT,");
		insertSqlCmd.append("PRELOAD_DW_DATE,PREVIOUS_STATUS,PRIMARY_CARD,REGION_ID,REG_DATE,REG_TIME,RETURN_CARD_WAY,SALE_WAY,");
		insertSqlCmd.append("SHOW_CARD_NO,STATUS,STATUS_UPDATE_DATE,TOTAL_RELOAD_AMT,TOTAL_USE_AMT,UD1,UD2,UD3,UD4,UD5,'2',");
		insertSqlCmd.append("UPT_DATE,UPT_TIME,UPT_USERID,VIP_FLAG,WARRANTY_PERIOD,'2','1',");
		insertSqlCmd.append("PREVIOUS_LIFE_CYCLE, IS_SYNC_HG");
		insertSqlCmd.append(" FROM");
		insertSqlCmd.append(" TB_CARD WHERE UPT_DATE='" + sysDate + "' AND UPT_TIME='" + sysTime + "' AND UPT_USERID='batch'");
		
		log.info("insertSqlCmd: " + insertSqlCmd.toString());
		sqls.add(insertSqlCmd.toString());
		
		return sqls;
	}
    
    private String makeCanceled(String batchDate) {
    	return null;
    }
}
