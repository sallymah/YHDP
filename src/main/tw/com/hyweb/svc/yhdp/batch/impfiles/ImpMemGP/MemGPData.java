package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMemGP;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.info.TbMemberGroupInfo;
import tw.com.hyweb.service.db.info.TbMemberGroupUptInfo;
import tw.com.hyweb.service.db.mgr.TbMemberGroupMgr;

public class MemGPData
{
    private static Logger log = Logger.getLogger(MemGPData.class);

    private final Map<String, String> fileData;
    private Vector<TbMemberGroupInfo> result = new Vector<TbMemberGroupInfo>();
    private int count;
    private final String fullFileName;
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    private final String SEQ_MEMBER_GROUP_ID = "SEQ_MEMBER_GROUP_ID";
    private String memGroupId = null;
       
    public MemGPData(Connection connection, Map<String, String> fileData, String fullFileName) throws SQLException
    {
    	this.fileData = fileData;
    	this.fullFileName = fullFileName;
    	this.memGroupId = SequenceGenerator.getSequenceString(connection, SEQ_MEMBER_GROUP_ID, 5);
    	this.count = getMemGPInfo(connection , fileData.get("MEM_GROUP_NAME"));    
    }
    
    private int getMemGPInfo(Connection connection, String memGroupName) throws SQLException
    {
    	TbMemberGroupInfo info = new TbMemberGroupInfo();
        info.setMemGroupName(memGroupName);

        return new TbMemberGroupMgr(connection).queryMultiple(info, result);
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getMemGPInfoCount() {
		return count;
	}
	
	public TbMemberGroupInfo getMemGPInfo() {	
		return result.get(0);
	}

    public List handleMemGP(Connection connection, String batchDate) throws Exception {
    	
    	TbMemberGroupInfo tbMemberGroupInfo = makeMemGP(batchDate);
    	TbMemberGroupUptInfo tbMemberGroupUptInfo = makeMemGPUpt(batchDate);
    	    	
        List sqls = new ArrayList();
        sqls.add(tbMemberGroupInfo.toInsertSQL());
        sqls.add(tbMemberGroupUptInfo.toInsertSQL());
        
        return sqls;
    }   
    
    private TbMemberGroupInfo makeMemGP(String batchDate) {

    	TbMemberGroupInfo info = new TbMemberGroupInfo();
    	
    	info.setMemGroupId(memGroupId);
    	info.setMemGroupName(fileData.get("MEM_GROUP_NAME"));
    	info.setBusIdNo(fileData.get("BUS_ID_NO"));
    	info.setContact(fileData.get("CONTACT"));
    	info.setTel(fileData.get("TEL"));
    	info.setFax(fileData.get("FAX"));
    	info.setEmail(fileData.get("EMAIL"));
    	info.setCity(fileData.get("CITY"));
    	info.setZipCode(fileData.get("ZIP_CODE"));
    	info.setRlnEntId(fileData.get("RLN_ENT_ID"));
    	info.setIndustryId(fileData.get("INDUSTRY_ID"));
    	info.setAddress(fileData.get("ADDRESS"));
    	info.setSignDate(fileData.get("SIGN_DATE"));
    	info.setCancelDate(fileData.get("CANCEL_DATE"));
    	info.setEffectiveDate(fileData.get("EFFECTIVE_DATE"));
    	info.setRldMaxAmt(Double.valueOf(fileData.get("RLD_MAX_AMT")));
    	info.setTerminationDate("99991231");
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime	);
    	info.setUptUserid("BATCH");
    	info.setAprvDate(sysDate);
    	info.setAprvTime(sysTime);
    	info.setAprvUserid("BATCH");
    	
        return info;
    }
    
    private TbMemberGroupUptInfo makeMemGPUpt(String batchDate) {

    	TbMemberGroupUptInfo info = new TbMemberGroupUptInfo();
    	
    	info.setMemGroupId(memGroupId);
    	info.setMemGroupName(fileData.get("MEM_GROUP_NAME"));
    	info.setBusIdNo(fileData.get("BUS_ID_NO"));
    	info.setContact(fileData.get("CONTACT"));
    	info.setTel(fileData.get("TEL"));
    	info.setFax(fileData.get("FAX"));
    	info.setEmail(fileData.get("EMAIL"));
    	info.setCity(fileData.get("CITY"));
    	info.setZipCode(fileData.get("ZIP_CODE"));
    	info.setRlnEntId(fileData.get("RLN_ENT_ID"));
    	info.setIndustryId(fileData.get("INDUSTRY_ID"));
    	info.setAddress(fileData.get("ADDRESS"));
    	info.setSignDate(fileData.get("SIGN_DATE"));
    	info.setCancelDate(fileData.get("CANCEL_DATE"));
    	info.setEffectiveDate(fileData.get("EFFECTIVE_DATE"));
    	info.setRldMaxAmt(Double.valueOf(fileData.get("RLD_MAX_AMT")));
    	info.setTerminationDate("99991231");
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime);
    	info.setUptUserid("BATCH");
    	info.setUptStatus("1");
    	info.setAprvDate(sysDate);
    	info.setAprvTime(sysTime);
    	info.setAprvUserid("BATCH");
    	info.setAprvStatus("1");
    	
        return info;
    }
}
