package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMember;

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
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbMemberUptInfo;
import tw.com.hyweb.service.db.mgr.TbMemberGroupMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;

public class MemberData
{
    private static Logger log = Logger.getLogger(MemberData.class);

    private final Map<String, Object> fileData;
    private Vector<TbMemberGroupInfo> memGPResult = new Vector<TbMemberGroupInfo>();
    private Vector<TbMemberInfo> memberResult = new Vector<TbMemberInfo>();
    private int memGPCount;
    private int memberCount;
    private final String fullFileName;
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    private final String SEQ_SUB_MEMBER_ID = "SEQ_SUB_MEMBER_ID";
    private String memGroupId;
    private String memId;
       
    public MemberData(Connection connection, Map<String, Object> fileData, String fullFileName) throws SQLException
    {
    	this.fileData = fileData;
    	this.fullFileName = fullFileName;
    	this.memGPCount = getMemGpInfo(connection , (String)fileData.get("MEM_GROUP_NAME"));
    	this.memberCount = getMemberInfo(connection , (String)fileData.get("MEM_NAME"));
    	this.memGroupId = getMemGPInfo().getMemGroupId();
    	this.memId =  this.memGroupId + SequenceGenerator.getSequenceString(connection, SEQ_SUB_MEMBER_ID, 3);
    }
    
    private int getMemGpInfo(Connection connection, String memGroupName) throws SQLException
    {
    	TbMemberGroupInfo info = new TbMemberGroupInfo();
        info.setMemGroupName(memGroupName);

        return new TbMemberGroupMgr(connection).queryMultiple(info, memGPResult);
    }
    
    private int getMemberInfo(Connection connection, String memName) throws SQLException
    {
    	TbMemberInfo info = new TbMemberInfo();
        info.setMemName(memName);

        return new TbMemberMgr(connection).queryMultiple(info, memberResult);
    }

	public Map<String, Object> getFileData() {
		return fileData;
	}

	public int getMemGPInfoCount() {
		return memGPCount;
	}
	
	public int getMemberInfoCount() {
		return memberCount;
	}
	
	public TbMemberGroupInfo getMemGPInfo() {	
		return memGPResult.get(0);
	}
	
	public TbMemberInfo getMemberInfo() {	
		return memberResult.get(0);
	}

    public List handleMember(Connection connection, String batchDate) throws Exception {
    	
    	TbMemberInfo tbMemberInfo = makeMember(batchDate);
    	TbMemberUptInfo tbMemberUptInfo = makeMemberUpt(batchDate);
    	    	
        List sqls = new ArrayList();
        sqls.add(tbMemberInfo.toInsertSQL());
        sqls.add(tbMemberUptInfo.toInsertSQL());
        
        return sqls;
    }   
    
    private TbMemberInfo makeMember(String batchDate) {

    	TbMemberInfo info = new TbMemberInfo();
    	
//    	int regFees = (Integer)fileData.get("REGFEES")/1000;
    	int regFees = Integer.valueOf(fileData.get("REGFEES").toString());
    	regFees = regFees / 1000;
    	
    	info.setRegionId("TWN");
    	info.setMemGroupId(memGroupId);
    	info.setMemId(memId);
    	info.setMemName((String)fileData.get("MEM_NAME"));
    	info.setMemType((String)fileData.get("MEM_TYPE"));
    	info.setAgency((String)fileData.get("AGENCY"));
    	info.setBusIdNo((String)fileData.get("BUS_ID_NO"));
    	info.setContact((String)fileData.get("CONTACT"));
    	info.setTel((String)fileData.get("TEL"));
    	info.setFax((String)fileData.get("FAX"));
    	info.setEmail((String)fileData.get("EMAIL"));
    	info.setCity((String)fileData.get("CITY"));
    	info.setZipCode((String)fileData.get("ZIP_CODE"));
    	info.setRlnEntId((String)fileData.get("RLN_ENT_ID"));
    	info.setIndustryId((String)fileData.get("INDUSTRY_ID"));
    	info.setAddress((String)fileData.get("ADDRESS"));
    	info.setEffectiveDate((String)fileData.get("EFFECTIVE_DATE"));
    	info.setStatus((String)fileData.get("STATUS"));
    	info.setSamLogonTime((Number)fileData.get("SAM_LOGON_TIME"));
    	info.setRegfees(regFees);
    	info.setFeeRemitDate((String)fileData.get("FEE_REMIT_DATE"));
    	info.setTerminationDate("99991231");
    	info.setUptDate(sysDate);
    	info.setUptTime(sysTime	);
    	info.setUptUserid("BATCH");
    	info.setAprvDate(sysDate);
    	info.setAprvTime(sysTime);
    	info.setAprvUserid("BATCH");
    	
        return info;
    }
    
    private TbMemberUptInfo makeMemberUpt(String batchDate) {

    	TbMemberUptInfo info = new TbMemberUptInfo();
    	
//    	double regFees = (double)fileData.get("REGFEES")/1000;
    	double regFees = Double.parseDouble(fileData.get("REGFEES").toString());
    	regFees = regFees / 1000;
    	
    	info.setRegionId("TWN");
    	info.setMemGroupId(memGroupId);
    	info.setMemId(memId);
    	info.setMemName((String)fileData.get("MEM_NAME"));
    	info.setMemType((String)fileData.get("MEM_TYPE"));
    	info.setAgency((String)fileData.get("AGENCY"));
    	info.setBusIdNo((String)fileData.get("BUS_ID_NO"));
    	info.setContact((String)fileData.get("CONTACT"));
    	info.setTel((String)fileData.get("TEL"));
    	info.setFax((String)fileData.get("FAX"));
    	info.setEmail((String)fileData.get("EMAIL"));
    	info.setCity((String)fileData.get("CITY"));
    	info.setZipCode((String)fileData.get("ZIP_CODE"));
    	info.setRlnEntId((String)fileData.get("RLN_ENT_ID"));
    	info.setIndustryId((String)fileData.get("INDUSTRY_ID"));
    	info.setAddress((String)fileData.get("ADDRESS"));
    	info.setEffectiveDate((String)fileData.get("EFFECTIVE_DATE"));
    	info.setStatus((String)fileData.get("STATUS"));
    	info.setSamLogonTime((Number)fileData.get("SAM_LOGON_TIME"));
    	info.setRegfees(regFees);
    	info.setFeeRemitDate((String)fileData.get("FEE_REMIT_DATE"));
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
