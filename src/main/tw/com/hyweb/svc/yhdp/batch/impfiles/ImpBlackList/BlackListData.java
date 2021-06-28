package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBlackList;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbBlacklistSettingInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;

public class BlackListData
{
    private static Logger log = Logger.getLogger(BlackListData.class);

    private final Map<String, String> fileData;
    private final String time = DateUtils.getSystemTime();
    private Vector<TbCardInfo> result = new Vector<TbCardInfo>();
    private int count;
       
    public BlackListData(Connection connection, Map<String, String> fileData) throws SQLException
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

    public List handleBlackList(Connection connection, String batchDate, String fileDate) throws Exception {
    	
    	List sqls = new ArrayList();
    	
    	String updateCardSql = updateCardStatus(batchDate);
    	log.info("sql: " + updateCardSql);
    	sqls.add(updateCardSql);
    	
    	String insertBlackListSql = makeBlackList(batchDate, fileDate);   
    	log.info("sql: " + insertBlackListSql);
    	sqls.add(insertBlackListSql);       
        
        return sqls;
    }   
    
    private String updateCardStatus(String batchDate) {
    	
    	String lifeCycle="8";
    	String revLifeCycle = result.get(0).getLifeCycle();
    	String revStatus = result.get(0).getStatus();
    	String sql = "UPDATE TB_CARD SET LIFE_CYCLE='" + lifeCycle 
    			+ "', PREVIOUS_LIFE_CYCLE='" + revLifeCycle 
    			+ "', PREVIOUS_STATUS='" + revStatus 
    			+ "' WHERE CARD_NO='" + fileData.get("CARD_NO") + "'";
    	
        return sql;	
    }
    
    private String makeBlackList(String batchDate, String fileDate) {

    	TbBlacklistSettingInfo info = new TbBlacklistSettingInfo();
    	
    	info.setCardNo(fileData.get("CARD_NO"));
    	info.setExpiryDate(getCardInfo().getExpiryDate());
    	info.setRegUserid("batch");
    	info.setRegDate(fileDate);
    	info.setRegTime(time);
    	info.setBlacklistCode("01");
    	
        return info.toInsertSQL();
    }
}
