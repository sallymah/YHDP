package tw.com.hyweb.core.cp.batch.preoperation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;
 
import tw.com.hyweb.core.campaign.SdateEdateInfo;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.common.campaignadapter.CampaignAdapterImpl;
import tw.com.hyweb.core.cp.common.campaignadapter.CampaignFillerImpl;
import tw.com.hyweb.core.cp.common.campaignadapter.CampaignInfo;
import tw.com.hyweb.core.cp.common.campaignadapter.ICampaignAdapter;
import tw.com.hyweb.core.cp.common.campaignadapter.ICampaignFiller;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.DateRange;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.info.TbAcctBalInfo;
import tw.com.hyweb.service.db.info.TbBonusDtlInfo;
import tw.com.hyweb.service.db.info.TbCardBalInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbCustBalInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.mgr.TbBonusDtlMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.util.string.StringUtil;

public class PreOperationUtil
{

    private static final Logger LOGGER = Logger.getLogger(PreOperationUtil.class);
    
    public static boolean isNullOrEmpty(String value)
    {
        if (value==null || value.trim().equals(""))
       	 return true;

        return false;
    }
    
    public static DateRange getBonusSdateEdate(Connection connection, String txnDate, String balanceType, String cardNo, String expiryDate, String bonusId, String bonusSdate, String bonusEdate) throws SQLException, BatchJobException
    {
    	DateRange range = null;
    	
        if(!StringUtil.isEmpty(cardNo) && !StringUtil.isEmpty(expiryDate))
        {
            TbCardInfo card = getCard(connection, cardNo, expiryDate);

            range = getCampaignBonusDateRange(connection,card, txnDate, bonusId, balanceType, bonusSdate, bonusEdate);
        }
        else
        {
        	range = new DateRange();
        	range.setStartDate(bonusSdate);
        	range.setEndDate(bonusEdate);
        }
        
        return range;
    }

	
	public static DateRange getCampaignBonusDateRange(Connection connection, TbCardInfo card, String txnDate, String bonusId, String balanceType, String bonusSdate, String bonusEdate) throws BatchJobException, SQLException
	{
		CampaignInfo campaignInfo = new CampaignInfo();
		campaignInfo.setCardNo(card.getCardNo());
		campaignInfo.setExpiryDate(card.getExpiryDate());
		campaignInfo.setTransDate(txnDate);
		campaignInfo.setRegionId(card.getRegionId());
		campaignInfo.setCustId(card.getCustId());
		campaignInfo.setAcctId(card.getAcctId());

		ICampaignFiller filler = new CampaignFillerImpl(connection, campaignInfo);
		LOGGER.info("point 1");
		ICampaignAdapter adapter = new CampaignAdapterImpl(connection, filler);
		LOGGER.info("point 2");

		SdateEdateInfo sdateEdateInfo = adapter.getSdateEdateInfo(bonusId, balanceType, bonusSdate, bonusEdate);
		
		if (!StringUtil.isEmpty(sdateEdateInfo.getRcode()) && !Constants.RCODE_0000_OK.equals(sdateEdateInfo.getRcode())) {	
			throw new BatchJobException("getSdateEdateInfo fail : " + sdateEdateInfo.getErrMsg(), sdateEdateInfo.getRcode());
		}

		DateRange dateRange = new DateRange();
		dateRange.setStartDate(sdateEdateInfo.getSDate());
		dateRange.setEndDate(sdateEdateInfo.getEDate());

		return dateRange;
	}
	
    public static TbCardInfo getCard(Connection connection, String cardNumber, String expiryDate) throws SQLException
    {
        TbCardPK pk = new TbCardPK();
        pk.setCardNo(cardNumber);
        pk.setExpiryDate(expiryDate);

        return new TbCardMgr(connection).querySingle(pk);
    }
    
    public static TbBonusDtlInfo getBonusDtlInfo(Connection connection, String bonusId) throws Exception
    {
        Vector<TbBonusDtlInfo> result = new Vector<TbBonusDtlInfo>();
        new TbBonusDtlMgr(connection).queryMultiple("BONUS_ID = '" + bonusId + "'", result);
        
		if (result.size()==0) 
		{
		    throw new Exception("getBonusDtlInfo() fail!! (BONUS_ID='" + bonusId + "'");
		}
        return result.get(0);
    }
    
    public static String getBalCrDbSql(Connection connection, TbOnlTxnInfo info, TbOnlTxnDtlInfo infoDtl) throws SQLException
    {
        String sqlCmd = null;

        if (infoDtl.getBalanceType().equals(Constants.BALANCETYPE_CARD))
        {
            
            TbCardBalInfo balInfo = new TbCardBalInfo();
            balInfo.setCardNo(infoDtl.getBalanceId());
            balInfo.setExpiryDate(info.getExpiryDate());
            balInfo.setBonusId(infoDtl.getBonusId());
            balInfo.setBonusSdate(infoDtl.getBonusSdate());
            balInfo.setBonusEdate(infoDtl.getBonusEdate());

            sqlCmd = Layer2Util.getCardBalSql(balInfo, infoDtl.getTxnCode(), infoDtl.getBonusQty().doubleValue(), connection);
        }
        else if (infoDtl.getBalanceType().equals(Constants.BALANCETYPE_ACCT))
        {
            TbCustBalInfo balInfo = new TbCustBalInfo();
            balInfo.setCustId(infoDtl.getBalanceId());
            balInfo.setRegionId(infoDtl.getRegionId());
            balInfo.setBonusId(infoDtl.getBonusId());
            balInfo.setBonusSdate(infoDtl.getBonusSdate());
            balInfo.setBonusEdate(infoDtl.getBonusEdate());

            sqlCmd = Layer2Util.getCustBalSql(balInfo, infoDtl.getTxnCode(), infoDtl.getBonusQty().doubleValue(), connection);

        }
        else if (infoDtl.getBalanceType().equals(Constants.BALANCETYPE_CUST))
        {
            TbAcctBalInfo balinfo = new TbAcctBalInfo();
            balinfo.setAcctId(infoDtl.getBalanceId());
            balinfo.setRegionId(infoDtl.getRegionId());
            balinfo.setBonusId(infoDtl.getBonusId());
            balinfo.setBonusSdate(infoDtl.getBonusSdate());
            balinfo.setBonusEdate(infoDtl.getBonusEdate());

            sqlCmd = Layer2Util.getAcctBalSql(balinfo, infoDtl.getTxnCode(), infoDtl.getBonusQty().doubleValue(), connection);
        }

        return sqlCmd;
    }
    
    public static String getBalCrDbSql(Connection connection, TbTransInfo info, TbTransDtlInfo infoDtl) throws SQLException
    {
        String sqlCmd = null;

        if (infoDtl.getBalanceType().equals(Constants.BALANCETYPE_CARD))
        {
            
            TbCardBalInfo balInfo = new TbCardBalInfo();
            balInfo.setCardNo(infoDtl.getBalanceId());
            balInfo.setExpiryDate(info.getExpiryDate());
            balInfo.setBonusId(infoDtl.getBonusId());
            balInfo.setBonusSdate(infoDtl.getBonusSdate());
            balInfo.setBonusEdate(infoDtl.getBonusEdate());

            sqlCmd = Layer2Util.getCardBalSql(balInfo, infoDtl.getTxnCode(), infoDtl.getBonusQty().doubleValue(), connection);
        }
        else if (infoDtl.getBalanceType().equals(Constants.BALANCETYPE_ACCT))
        {
            TbCustBalInfo balInfo = new TbCustBalInfo();
            balInfo.setCustId(infoDtl.getBalanceId());
            balInfo.setRegionId(infoDtl.getRegionId());
            balInfo.setBonusId(infoDtl.getBonusId());
            balInfo.setBonusSdate(infoDtl.getBonusSdate());
            balInfo.setBonusEdate(infoDtl.getBonusEdate());

            sqlCmd = Layer2Util.getCustBalSql(balInfo, infoDtl.getTxnCode(), infoDtl.getBonusQty().doubleValue(), connection);

        }
        else if (infoDtl.getBalanceType().equals(Constants.BALANCETYPE_CUST))
        {
            TbAcctBalInfo balinfo = new TbAcctBalInfo();
            balinfo.setAcctId(infoDtl.getBalanceId());
            balinfo.setRegionId(infoDtl.getRegionId());
            balinfo.setBonusId(infoDtl.getBonusId());
            balinfo.setBonusSdate(infoDtl.getBonusSdate());
            balinfo.setBonusEdate(infoDtl.getBonusEdate());

            sqlCmd = Layer2Util.getAcctBalSql(balinfo, infoDtl.getTxnCode(), infoDtl.getBonusQty().doubleValue(), connection);
        }

        return sqlCmd;
    }
    
}
