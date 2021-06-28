package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpHappyGo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ImpFileInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbHgCardGroupInfo;
import tw.com.hyweb.service.db.info.TbHgCardGroupUptInfo;
import tw.com.hyweb.service.db.info.TbHgCardMapInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbHgCardGroupMgr;
import tw.com.hyweb.service.db.mgr.TbHgCardGroupUptMgr;
import tw.com.hyweb.service.db.mgr.TbHgCardMapMgr;

public class HappyGoData
{
    private static Logger log = Logger.getLogger(HappyGoData.class);

    private final Map<String, String> fileData;
    private int cardCount;
    private int hgCardCount;
    private String cardNo;
    private final String fullFileName;
    private String sysDate = DateUtil.getTodayString().substring(0, 8);
    private String sysTime = DateUtil.getTodayString().substring(8, 14);

       
    public HappyGoData(Connection connection, Map<String, String> fileData, String fullFileName) throws SQLException
    {
    	this.fileData = fileData;
    	this.fullFileName = fullFileName;
    	this.cardNo = "986" + fileData.get("BARCODE1").substring(3,16);
    	this.cardCount = getCardInfoCnt(connection , cardNo);
    	this.hgCardCount = getHgCardMapInfoCnt(connection , cardNo);
    }
    
    private int getHgCardMapInfoCnt(Connection connection, String cardNumber) throws SQLException
    {
    	Vector<TbHgCardMapInfo> result = new Vector<TbHgCardMapInfo>();
    	TbHgCardMapInfo info = new TbHgCardMapInfo();
        info.setCardNo(cardNumber);

        return new TbHgCardMapMgr(connection).queryMultiple(info, result);
    }
    
    private int getCardInfoCnt(Connection connection, String cardNumber) throws SQLException
    {
    	Vector<TbCardInfo> result = new Vector<TbCardInfo>();
        TbCardInfo info = new TbCardInfo();
        info.setCardNo(cardNumber);

        return new TbCardMgr(connection).queryMultiple(info, result);
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getCardInfoCount() {
		return cardCount;
	}
	
	public int getHgCardInfoCount() {
		return hgCardCount;
	}
	
	public String getCardNo() {
		return cardNo;
	}

    public List handleHappyGo(Connection conn, String batchDate, ImpFileInfo impFileInfo) throws Exception 
    {
    	String upCaseHgGpId = impFileInfo.getHgGpId().toUpperCase();
    	StringBuffer where = new StringBuffer();
    	where.append("HG_CARD_GROUP_ID='").append(upCaseHgGpId).append("'");
    	TbHgCardGroupMgr hgCardGroupMgr = new TbHgCardGroupMgr(conn);
    	int count = hgCardGroupMgr.getCount(where.toString());
    	if(count == 0) {
    		TbHgCardGroupInfo info = new TbHgCardGroupInfo();
    		info.setHgCardGroupId(upCaseHgGpId);
    		info.setHgCardGroupName(impFileInfo.getHgGpName());
    		info.setAprvDate(sysDate);
    		info.setAprvTime(sysTime);
    		info.setAprvUserid("batch");
    		info.setUptDate(sysDate);
    		info.setUptTime(sysTime);
    		info.setUptUserid("batch");
    		hgCardGroupMgr.insert(info);
    		
    		TbHgCardGroupUptInfo uptInfo = new TbHgCardGroupUptInfo();
    		uptInfo.setHgCardGroupId(upCaseHgGpId);
    		uptInfo.setHgCardGroupName(impFileInfo.getHgGpName());
    		uptInfo.setAprvDate(sysDate);
    		uptInfo.setAprvTime(sysTime);
    		uptInfo.setAprvUserid("batch");
    		uptInfo.setAprvStatus("1");
    		uptInfo.setUptDate(sysDate);
    		uptInfo.setUptTime(sysTime);
    		uptInfo.setUptUserid("batch");
    		uptInfo.setUptStatus("2");
    		
    		TbHgCardGroupUptMgr hgCardGroupUptMgr = new TbHgCardGroupUptMgr(conn);
    		hgCardGroupUptMgr.insert(uptInfo);		
    	}   	
    	TbHgCardMapInfo hgCardMapInfo = makeHappyGo(batchDate, upCaseHgGpId);
    	    	
        List sqls = new ArrayList();
        log.info("sql: " + hgCardMapInfo.toInsertSQL());
        sqls.add(hgCardMapInfo.toInsertSQL());
        
        return sqls;
    }   
    
    private TbHgCardMapInfo makeHappyGo(String batchDate, String hgGpId) 
    {
    	TbHgCardMapInfo info = new TbHgCardMapInfo();
    	
    	info.setBarcode1(fileData.get("BARCODE1"));
    	log.info("BARCODE1: " + fileData.get("BARCODE1"));
    	info.setBarcode2(fileData.get("BARCODE2"));
    	log.info("BARCODE2: " + fileData.get("BARCODE2"));
    	info.setImpFileName(fullFileName);
    	info.setImpFileDate(batchDate);
    	info.setCardNo(cardNo);
    	info.setHgCardGroupId(hgGpId);
    
        return info;
    }
}
