package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpJcicCard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import oracle.net.aso.i;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardUptInfo;
import tw.com.hyweb.service.db.info.TbCustInfo;
import tw.com.hyweb.service.db.info.TbCustUptInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbCustMgr;
import tw.com.hyweb.util.BatchUtils;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;

public class JcicCardData
{
    private static Logger log = Logger.getLogger(JcicCardData.class);

    private final Map<String, String> fileData;
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    private Vector<TbCustInfo> custResult = new Vector<TbCustInfo>();
    private String cardNo;
    private int cardCount;
    private TbCardInfo cardInfo;

       
    public JcicCardData(Connection connection, Map<String, String> fileData) throws SQLException
    {
    	this.fileData = fileData;
    	this.cardNo=(String)fileData.get("CARD_NO");
    	this.cardCount = getCardInfo(connection , (String)fileData.get("CARD_NO"));

    }
    
    private int getCardInfo(Connection connection, String cardNo) throws SQLException
    {
         int cardCount = DbUtil.getInteger("SELECT * FROM TB_CARD where CARD_NO ='"+cardNo+"' ", connection);
         
        return cardCount;
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getCardInfoCount() {
		return cardCount;
	}



    public List handleCard(Connection connection, String batchDate, String fileDate) throws Exception {
    	List sqls = new ArrayList();  	

    	String where = " card_no = '" + cardNo + "'";
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		new TbCardMgr(connection).queryMultiple(where, result);

		for (int i = 0; i < result.size(); i++) {
			cardInfo = (TbCardInfo) result.get(i);
			sqls.add(UpdateCardtSQL(batchDate));
			sqls.add(makeInsertCardUptSql());
		}
        return sqls;
    }   
    
    private String makeInsertCardUptSql() {
		
		TbCardUptInfo cardUptInfo = new TbCardUptInfo();
		cardUptInfo.setRegionId(cardInfo.getRegionId());
		cardUptInfo.setCardNo(cardInfo.getCardNo());
		cardUptInfo.setCardProduct(cardInfo.getCardProduct());
		cardUptInfo.setCardPlan(cardInfo.getCardPlan());
		cardUptInfo.setExpiryDate(cardInfo.getExpiryDate());
		cardUptInfo.setCustId(cardInfo.getCustId());
		cardUptInfo.setMemId(cardInfo.getMemId());
		cardUptInfo.setCardOwner(cardInfo.getCardOwner());
		cardUptInfo.setAcctId(cardInfo.getAcctId());
		cardUptInfo.setActiveCardFlag(cardInfo.getActiveCardFlag());
		cardUptInfo.setStatus(cardInfo.getStatus());
		cardUptInfo.setCardholderId(cardInfo.getCardholderId());
		cardUptInfo.setActiveDate(cardInfo.getActiveDate());
		cardUptInfo.setBalTransferFlag(cardInfo.getBalTransferFlag());
		cardUptInfo.setBillCutDay(cardInfo.getBillCutDay());
		cardUptInfo.setCardFee(cardInfo.getCardFee());
		cardUptInfo.setCardLevel(cardInfo.getCardLevel());
		cardUptInfo.setCardOpenOwner(cardInfo.getCardOpenOwner());
		cardUptInfo.setCardPlan(cardInfo.getCardPlan());
		cardUptInfo.setCreditCardNo(cardInfo.getCreditCardNo());
		cardUptInfo.setCreditExpiryDate(cardInfo.getCreditExpiryDate());
		cardUptInfo.setPrimaryCard(cardInfo.getPrimaryCard());
		cardUptInfo.setNotifyDate(fileData.get("NOTIFY_DATE"));
		cardUptInfo.setNotifyType(fileData.get("NOTIFY_TYPE"));
		cardUptInfo.setNotifyDesc(fileData.get("NOTIFY_DESC"));
		cardUptInfo.setPersoBatchNo(cardInfo.getPersoBatchNo());
		cardUptInfo.setCardTypeId(cardInfo.getCardTypeId());
		cardUptInfo.setTestFlag(cardInfo.getTestFlag());
		cardUptInfo.setCardCatId(cardInfo.getCardCatId());
		cardUptInfo.setPreviousStatus(cardInfo.getPreviousStatus());
		cardUptInfo.setCardFee(cardInfo.getCardFee());
		cardUptInfo.setTotalReloadAmt(cardInfo.getTotalReloadAmt());
		cardUptInfo.setAprvStatus("1");
		cardUptInfo.setAprvDate(sysDate);
		cardUptInfo.setAprvTime(sysTime);
		cardUptInfo.setAprvUserid("BATCH");
		cardUptInfo.setUptDate(sysDate);
		cardUptInfo.setUptTime(sysTime);
		cardUptInfo.setUptUserid("BATCH");
		cardUptInfo.setUptStatus("2");
		return cardUptInfo.toInsertSQL();

	}
    private String UpdateCardtSQL(String batchDate) throws SQLException {
	  	
	    	String sql = "UPDATE TB_CARD set NOTIFY_TYPE='"+fileData.get("NOTIFY_TYPE")+"'"
					+ ",NOTIFY_DATE='"+fileData.get("NOTIFY_DATE")+"'"
					+ ",NOTIFY_DESC='"+fileData.get("NOTIFY_DESC")+"'"
					+ ",Upt_Date='"+batchDate+"'"
					+ ",Upt_Time='"+DateUtil.getTodayString().substring(8,14)+"'"
					+ ",Upt_Userid='BATCH'"
					+ ",Aprv_Date='"+batchDate+"'"
					+ ",aprv_time='"+DateUtil.getTodayString().substring(8,14)+"'"
					+ ",aprv_userId='BATCH' where card_no = '"+fileData.get("CARD_NO")+"'";

    	return sql;
	}      
}
