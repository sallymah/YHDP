package tw.com.hyweb.svc.yhdp.batch.impfiles;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.*;
import tw.com.hyweb.service.db.info.*;
import tw.com.hyweb.service.db.mgr.TbBonusDtlMgr;
import tw.com.hyweb.service.db.mgr.TbBonusMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.util.string.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ApploadData
{
    private static Logger log = Logger.getLogger(ApploadData.class);

    private final Map<String, String> fileData;
    private final TbCardInfo cardInfo;
    private final DateRange bonusRange;
    private final String fullFileName;
    
    private String bonusBase = "H";
    private String balanceType = "C";
    private String arSrc ="B";
    private String initStatus = "0"; //未下載
    private String maxEndDate = "99991231";

    public ApploadData(Connection connection, Map<String, String> fileData, String fullFileName) throws SQLException
    {
    	this.fileData = fileData;
    	this.fullFileName = fullFileName;
    	this.cardInfo = getCardInfo(connection , fileData.get("CARD_NO"), fileData.get("EXPIRY_DATE"));
    	this.bonusRange = getBonusSdateEdate(connection, fileData.get("BONUS_ID"),fileData.get("BONUS_SDATE"),fileData.get("BONUS_EDATE"));
    }
    
    private TbCardInfo getCardInfo(Connection connection, String cardNumber, String expiryDate) throws SQLException
    {
        TbCardPK pk = new TbCardPK();
        pk.setCardNo(cardNumber);
        pk.setExpiryDate(expiryDate);

        return new TbCardMgr(connection).querySingle(pk);
    }
    
    private TbBonusInfo getBonusInfo(Connection conn, String bonusId) throws SQLException {

    	TbBonusMgr mgr = new TbBonusMgr(conn);
    	return mgr.querySingle(bonusId);
    }


    private DateRange getBonusSdateEdate(Connection connection, String bonusId, String bonusSdate, String bonusEdate) throws SQLException
    {
    	DateRange range = new DateRange();
    	
        if (StringUtil.isEmpty(bonusSdate) && StringUtil.isEmpty(bonusEdate)) {
            // 去 TB_BONUS_DTL 主檔取得 BONUS 效期定義
            Vector results = new Vector();
            TbBonusDtlMgr mgr = new TbBonusDtlMgr(connection);
            TbBonusDtlInfo qinfo = new TbBonusDtlInfo();
            qinfo.toEmpty();
            qinfo.setBonusId(bonusId);
            mgr.queryMultiple(qinfo, results);
            
            if (results.size() == 0) {
                throw new IllegalArgumentException("bonusDtlInfo is null for '" + bonusId + "'!");
            }
            if (results.size() > 1) {
                throw new IllegalArgumentException("bonusDtlInfo is multiple for '" + bonusId + "'!" + results);
            }
            
            TbBonusDtlInfo bonusDtlInfo = (TbBonusDtlInfo) results.get(0);
            
            range.setStartDate(bonusDtlInfo.getBonusSdate());
            range.setEndDate(bonusDtlInfo.getBonusEdate());
                  
        }
        else{
        	range.setStartDate(bonusSdate);
        	range.setEndDate(bonusEdate);
        }
        
        return range;
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public TbCardInfo getCardInfo() {
		return cardInfo;
	}

    public List handleAppointReload(Connection connection, String batchDate) throws Exception {
    	
    	String arSerno = SequenceGenerator.getArSerno(connection, batchDate);
    	
    	TbAppointReloadInfo apploadInfo = makeAppointReload(arSerno, batchDate);
    	TbAppointReloadDtlInfo apploadDtlInfo = makeAppointReloadDtl(apploadInfo);
    	
        List sqls = new ArrayList();
        sqls.add(apploadInfo.toInsertSQL());
        sqls.add(apploadDtlInfo.toInsertSQL());
        return sqls;
    }
    
    private TbAppointReloadInfo makeAppointReload(String arSerno, String batchDate) {

    	TbAppointReloadInfo info = new TbAppointReloadInfo();
        info.setRegionId(cardInfo.getRegionId());
        info.setBonusBase(bonusBase);
        info.setBalanceType(balanceType);

        info.setBalanceId(fileData.get("CARD_NO"));
        info.setCardNo(fileData.get("CARD_NO"));
        info.setExpiryDate(fileData.get("EXPIRY_DATE"));

        info.setArSerno(arSerno);
        info.setArSrc(arSrc);
        info.setAcqMemId(fileData.get("ACQ_MEM_ID"));
        
        info.setMerchId(fileData.get("MERCH_ID"));

        info.setValidSdate(fileData.get("VALID_SDATE"));
        info.setValidEdate(fileData.get("VALID_EDATE"));
        info.setStatus(initStatus);
        info.setInfile(fullFileName);
        info.setApprldDesc(fileData.get("APPRLD_DESC"));
        info.setParMon(info.getValidSdate().substring(4, 6));
        info.setParDay(info.getValidSdate().substring(6, 8));
        info.setAprvDate(batchDate);
        
        return info;
    }

    private TbAppointReloadDtlInfo makeAppointReloadDtl(TbAppointReloadInfo apploadInfo) {
    	
        TbAppointReloadDtlInfo info = new TbAppointReloadDtlInfo();
        info.setBonusBase(apploadInfo.getBonusBase());
        info.setBalanceType(apploadInfo.getBalanceType());
        info.setBalanceId(apploadInfo.getBalanceId());
        info.setExpiryDate(apploadInfo.getExpiryDate());
        info.setArSerno(apploadInfo.getArSerno());
        
        info.setBonusId(fileData.get("BONUS_ID"));
        info.setBonusSdate(bonusRange.getStartDate());
        info.setBonusEdate(bonusRange.getEndDate());
        info.setBonusQty(Double.valueOf(fileData.get("BONUS_QTY")));
        info.setParMon(fileData.get("VALID_SDATE").substring(4, 6));
        info.setParDay(fileData.get("VALID_SDATE").substring(6, 8));
        
        return info;
    }
    
    
}
