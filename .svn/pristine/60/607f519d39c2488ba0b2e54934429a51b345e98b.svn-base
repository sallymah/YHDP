package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMember;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;

/**
 * <pre>
 * ImpMemGP(For YHDP)
 * </pre>
 * author:Kevin
 */
public class MemberChecker
{
    private static Logger log = Logger.getLogger(MemberChecker.class);
    
	private final MemberData memberData;
	private final Map<String, FieldInfo> memberFieldInfos;

	private List descInfos = new ArrayList();

    public MemberChecker(MemberData memberData, Map<String, FieldInfo> memberFieldInfos)
    {
    	this.memberData = memberData; 
    	this.memberFieldInfos = memberFieldInfos;
    }

    private void checkMemGPInfo(Connection connection) throws SQLException
    {
        if(memberData.getMemGPInfoCount() == 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, memberFieldInfos.get("MEM_GROUP_NAME") ,"memGPInfo is null:" + memberData.getFileData().get("MEM_GROUP_NAME"));
        }
    }
    
    private void checkMemberInfo(Connection connection) throws SQLException
    {
        if(memberData.getMemberInfoCount() != 0)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, memberFieldInfos.get("MEM_GROUP_NAME") ,"memberInfo is null:" + memberData.getFileData().get("MEM_GROUP_NAME"));
        }
    }
    
    private void checkMemType(Connection connection) throws SQLException
    {
    	String pos1 = ((String) memberData.getFileData().get("MEM_TYPE")).substring(2, 3);
    	String pos2 = ((String) memberData.getFileData().get("MEM_TYPE")).substring(3, 4);
    	
        if(!pos1.equals("0") || !pos2.equals("0"))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, memberFieldInfos.get("MEM_TYPE") ,"member type position(3 & 4) != 0:" + memberData.getFileData().get("MEM_TYPE"));
        }
        
        String memType = memberData.getFileData().get("MEM_TYPE").toString();
        String pos3 = "0";
        String pos4 = "0";
        if (memType.length() >= 7) {
        	pos3 = memType.substring(6, 7);
        	String agency = memberData.getFileData().get("AGENCY").toString();
        	if ("1".equals(pos3) && !"3".equals(agency)) {
        		addErrorDescInfo(connection, 
        				Constants.RCODE_2710_INVALID_ERR, 
        				memberFieldInfos.get("AGENCY"), 
        				"member type position(7) = 1 and agency != 3:" + 
        				"MEM_TYPE is " + memberData.getFileData().get("MEM_TYPE").toString() + ", " + 
        				"AGENCY is " + memberData.getFileData().get("AGENCY").toString());
        	}
        }
        
        if (memType.length() >= 8) {
        	pos4 = memType.substring(7, 8);
        	String agency = memberData.getFileData().get("AGENCY").toString();
        	if ("1".equals(pos4) && !"4".equals(agency)) {
        		addErrorDescInfo(connection, 
        				Constants.RCODE_2710_INVALID_ERR, 
        				memberFieldInfos.get("AGENCY"), 
        				"member type position(8) = 1 and agency != 4:" + 
        				"MEM_TYPE is " + memberData.getFileData().get("MEM_TYPE").toString() + ", " + 
        				"AGENCY is " + memberData.getFileData().get("AGENCY").toString());
        	}
        }
        
        if ("1".equals(pos3) && "1".equals(pos4)) {
    		addErrorDescInfo(connection, 
    				Constants.RCODE_2710_INVALID_ERR, 
    				memberFieldInfos.get("MEM_TYPE"), 
    				"member type position(7) = 1 and member type position(8) = 1: " 
    				+ memberData.getFileData().get("MEM_TYPE").toString());
        }
    }
    
    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
    
    private void addErrorDescInfo(Connection connection, String rcode, FieldInfo info, String content) throws SQLException
    {
		ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(rcode, info, content);   
        descInfos.add(descInfo);
    }
    
    public List<ErrorDescInfo> checker(Connection connection) throws Exception
    {	       
        /*檢查會員群組主檔*/
        checkMemGPInfo(connection);

        /* 檢查會員主檔*/
        checkMemberInfo(connection);
        
        /*檢查參加單位型態是否合法*/
        checkMemType(connection);
        
        return descInfos;      
    }
    
}
