package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpLptsam;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbLptsamInfo;
import tw.com.hyweb.util.DbUtil;

/**
 * <pre>
 * ImpCust(For YHDP)
 * </pre>
 * author:Kevin
 */
public class LptsamChecker
{
    private static Logger log = Logger.getLogger(LptsamChecker.class);
    
	private final LptsamData lptsamData;
	private final Map<String, FieldInfo> custFieldInfos;

	private List descInfos = new ArrayList();
	
	private String hgGroupId = "";

    public LptsamChecker(LptsamData lptsamData, Map<String, FieldInfo> custFieldInfos)
    {
    	this.lptsamData = lptsamData; 
    	this.custFieldInfos = custFieldInfos;
    }

    private void checkLptsmInfo(Connection conn) throws SQLException
    {    
    	String cid = lptsamData.getLptsamBean().getCid();
    	
    	if(cid.length() != 16) {
    		String msg = "CID column length != 16: " + cid;
    		addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("CID"), msg);
    		return;
    	}
    	
    	TbLptsamInfo info = lptsamData.getLptsamInfo(conn);
    	if(lptsamData.getLptsamBean().getActionStatus().equals("I")) {
    		
    		if(info != null)
            {
    			if(!info.getMemGroupId().equals(lptsamData.getLptsamBean().getMemGroupId()) && !info.getStatus().equals("9")) {
    				String msg = "action status=I(insert), but lptsamInfo exist: " + cid;
    				addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("CID") ,msg);
    			}
            }
    	}
    	else if(lptsamData.getLptsamBean().getActionStatus().equals("U")) {
    		if(info == null)
            {
    			String msg = "action status=U(update), but lptsamInfo no data found: " + cid;
            	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("CID"), msg);
            }
    	}
    	else {
    		if(info == null)
            {
    			String msg = "action status=D(delete), but lptsamInfo no data found: " + cid;
            	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("CID"), msg);
            }
    		else {
    			if(!info.getMemGroupId().equals(lptsamData.getLptsamBean().getMemGroupId())) 
    			{
    				String msg = "action status=D(delete), but member group id["+ info.getMemGroupId() +"] of table(TB_LPTSAM) is " +
    							 "inconsistent with file [" + lptsamData.getLptsamBean().getMemGroupId() + "]";
    				addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("MEMBER_GROUP_ID"), msg);
    			}
    		}
    	}
    }
    

	private void checkMemberGroup(Connection conn) throws SQLException
	{
		// TODO Auto-generated method stub
		
		Vector<String> params = new Vector<String>();
		StringBuffer sql = new StringBuffer();
		
		sql.append(" SELECT HQ_GROUP_ID");
		sql.append(" FROM TB_MEMBER_GROUP");
		sql.append(" WHERE MEM_GROUP_ID = ?");
		
		params.add(lptsamData.getLptsamBean().getMemGroupId());
		
		Vector result = DbUtil.selectHash(sql.toString(), params, conn);
        
        if(null != result && result.size() > 0)
        {
        	HashMap memGroup = (HashMap) result.get(0);
        	lptsamData.getLptsamBean().setHqGroupId(memGroup.get("HQ_GROUP_ID").toString());
        }
        else {
        	String msg = "member group id["+ lptsamData.getLptsamBean().getMemGroupId() +"] of table(TB_MEMBER_GROUP) is not exist.]";
        	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, custFieldInfos.get("MEMBER_GROUP_ID"), msg);
        }
	}
    
    private void addErrorDescInfo(Connection connection, String rcode, FieldInfo info, String content) throws SQLException
    {
		ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(rcode, info, content);   
        descInfos.add(descInfo);
    }
    
    public List<ErrorDescInfo> checker(Connection conn) throws Exception
    {	
        /*檢查LPTSAM主檔*/
    	checkLptsmInfo(conn);
    	checkMemberGroup(conn);
    	
        return descInfos;    
    }
}
