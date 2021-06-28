package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpErpt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbGrantsTxnInfo;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.service.db.info.TbTermUptInfo;
import tw.com.hyweb.service.db.mgr.TbMerchMgr;
import tw.com.hyweb.util.ReleaseResource;

public class ErptData
{
    private static Logger log = Logger.getLogger(ErptData.class);

    private final Map<String, String> fileData;
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    private Vector<TbMerchInfo> merchResult = new Vector<TbMerchInfo>();

    private int merchCount;
    private final String fullFileName;

       
    public ErptData(Connection connection, Map<String, String> fileData, String fullFileName) throws SQLException
    {
    	this.fileData = fileData;
    	this.fullFileName = fullFileName;
    }
    
	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getMerchInfoCount() {
		return merchCount;
	}
    public List handleGrants(Connection connection, String batchDate) throws Exception {
    	
    	List sqls = new ArrayList();
    	
    	TbGrantsTxnInfo insertGrantSQL = insertGrantInfo(batchDate);
    	sqls.add(insertGrantSQL.toInsertSQL());
        return sqls;
    }   
    
    private TbGrantsTxnInfo insertGrantInfo(String batchDate) {
    	TbGrantsTxnInfo grantsInfo = new TbGrantsTxnInfo();
    	grantsInfo.setAccountCode((String)fileData.get("ACCOUNT_CODE"));
    	grantsInfo.setSettleDate((String)fileData.get("SETTLE_DATE"));
    	grantsInfo.setAcqMemId((String)fileData.get("ACQ_MEM_ID"));
    	grantsInfo.setTtlCnt(Integer.valueOf((String)fileData.get("TTL_CNT")));
    	grantsInfo.setTtlAmt(Double.valueOf((String)fileData.get("TTL_AMT")));
    	grantsInfo.setSubsidyAmt(Integer.valueOf((String)fileData.get("SUBSIDY_AMT")));
    	grantsInfo.setImpDate(sysDate);
    	grantsInfo.setImpTime(sysTime);
    	grantsInfo.setImpFileName(fullFileName);
    	return grantsInfo;
    }
}
