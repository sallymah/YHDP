package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMerch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbMerchUptInfo;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbMerchMgr;

public class MerchData
{
    private static Logger log = Logger.getLogger(MerchData.class);

    private final String SEQ_SUB_MERCH_ID = "SEQ_SUB_MERCH_ID";
    private final Map<String, Object> fileData;
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    private Vector<TbMemberInfo> memResult = new Vector<TbMemberInfo>();
    private Vector<TbMerchInfo> merchResult = new Vector<TbMerchInfo>();
    private String merchId;
    private String memId;
	private int memCount;
    private int merchCount;

       
    public MerchData(Connection connection, Map<String, Object> fileData) throws SQLException
    {
    	this.fileData = fileData;
    	this.memCount = getMemInfo(connection , (String)fileData.get("MEM_ID"));
    	this.memId = (String)fileData.get("MEM_ID");
    	this.merchId = memId+SequenceGenerator.getSequenceString(connection, SEQ_SUB_MERCH_ID, 7);
    	this.merchCount = getMerchInfo(connection , memId, (String)fileData.get("MERCH_LOC_NAME"));
    }
    
    
    private int getMemInfo(Connection connection, String memId) throws SQLException
    {
        TbMemberInfo info = new TbMemberInfo();
        info.setMemId(memId);

        return new TbMemberMgr(connection).queryMultiple(info, memResult);
    }
    
    private int getMerchInfo(Connection connection, String memId, String merchLocName) throws SQLException
    {
        TbMerchInfo info = new TbMerchInfo();
        info.setMemId(memId);
        info.setMerchLocName(merchLocName);
        return new TbMerchMgr(connection).queryMultiple(info, merchResult);
    }

	public Map<String, Object> getFileData() {
		return fileData;
	}

	public int getMemInfoCount() {
		return memCount;
	}
	
	public int getMerchInfoCount() {
		return merchCount;
	}
	
	public TbMemberInfo getMemberInfo() {	
		return memResult.get(0);
	}
	
	public TbMerchInfo getMerchInfo() {	
		return merchResult.get(0);
	}
	
	public String getMerchId() {
		return merchId;
	}

    public List handleCust(Connection connection, String batchDate, String fileDate) throws Exception {
    	
    	List sqls = new ArrayList();
    	
    	TbMerchInfo tbMerchInfo = makeMerch(batchDate);
    	sqls.add(tbMerchInfo.toInsertSQL());
    	
    	TbMerchUptInfo tbMerchUptInfo = makeMerchUpt(batchDate);    	
    	sqls.add(tbMerchUptInfo.toInsertSQL());
        
        return sqls;
    }   
    
    private TbMerchInfo makeMerch(String batchDate) {

    	TbMerchInfo info = new TbMerchInfo();
    	info.setMemId(memId);
    	info.setMerchId(merchId);
    	info.setBusIdNo((String)fileData.get("BUS_ID_NO"));
    	info.setMerchLocName((String)fileData.get("MERCH_LOC_NAME"));
    	info.setMerchEngName((String)fileData.get("MERCH_ENG_NAME"));
    	info.setMerchAbbrName((String)fileData.get("MERCH_ABBR_NAME"));
    	info.setContact((String)fileData.get("CONTACT"));
    	info.setTel((String)fileData.get("TEL"));
    	info.setFax((String)fileData.get("FAX"));
    	info.setCity((String)fileData.get("CITY"));
    	info.setZipCode((String)fileData.get("ZIP_CODE"));
    	info.setAddress((String)fileData.get("ADDRESS"));
    	info.setEmail((String)fileData.get("EMAIL"));
    	info.setEffectiveDate((String)fileData.get("EFFECTIVE_DATE"));
    	String SAM_LOGON_TIME = (String)fileData.get("SAM_LOGON_TIME");
    	info.setSamLogonTime(Double.parseDouble(SAM_LOGON_TIME));
    	String OFFLINE_MAX_COUNT = (String)fileData.get("OFFLINE_MAX_COUNT");
    	info.setOfflineMaxCount(Double.parseDouble(OFFLINE_MAX_COUNT));
    	String OFFLINE_MAX_AMT = (String)fileData.get("OFFLINE_MAX_AMT");
    	info.setOfflineMaxAmt(Double.parseDouble(OFFLINE_MAX_AMT));
    	info.setTerminationDate("99991231");
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime	);
    	info.setUptUserid("BATCH");
    	info.setAprvDate(sysDate);
    	info.setAprvTime(sysTime);
    	info.setAprvUserid("BATCH");
    	info.setStatus("1");
    	
        return info;
    }
    
    private TbMerchUptInfo makeMerchUpt(String batchDate) {

    	TbMerchUptInfo info = new TbMerchUptInfo();
    	   	
    	info.setMemId(memId);
    	info.setMerchId(merchId);
    	info.setBusIdNo((String)fileData.get("BUS_ID_NO"));
    	info.setMerchLocName((String)fileData.get("MERCH_LOC_NAME"));
    	info.setMerchEngName((String)fileData.get("MERCH_ENG_NAME"));
    	info.setMerchAbbrName((String)fileData.get("MERCH_ABBR_NAME"));
    	info.setContact((String)fileData.get("CONTACT"));
    	info.setTel((String)fileData.get("TEL"));
    	info.setFax((String)fileData.get("FAX"));
    	info.setCity((String)fileData.get("CITY"));
    	info.setZipCode((String)fileData.get("ZIP_CODE"));
    	info.setAddress((String)fileData.get("ADDRESS"));
    	info.setEmail((String)fileData.get("EMAIL"));
    	info.setEffectiveDate((String)fileData.get("EFFECTIVE_DATE"));
    	info.setSamLogonTime(Double.parseDouble((String)fileData.get("SAM_LOGON_TIME")));
    	info.setOfflineMaxCount(Double.parseDouble((String)fileData.get("OFFLINE_MAX_COUNT")));
    	info.setOfflineMaxAmt(Double.parseDouble((String)fileData.get("OFFLINE_MAX_AMT")));
    	info.setTerminationDate("99991231");
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime	);
    	info.setUptUserid("BATCH");
    	info.setUptStatus("1");
    	info.setAprvDate(info.getUptDate());
    	info.setAprvTime(info.getUptTime());
    	info.setAprvUserid("BATCH");
    	info.setAprvStatus("1");
    	info.setStatus("1");
    	
        return info;
    }
}
