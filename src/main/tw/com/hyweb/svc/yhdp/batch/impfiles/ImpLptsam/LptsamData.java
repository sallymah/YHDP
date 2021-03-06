package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpLptsam;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbLptsamInfo;
import tw.com.hyweb.service.db.info.TbLptsamUptInfo;
import tw.com.hyweb.service.db.mgr.TbLptsamMgr;
import tw.com.hyweb.util.BatchUtils;

public class LptsamData
{
	private static Logger log = Logger.getLogger(LptsamData.class);

    private LptsamBean lptsamBean;
    
    private String uptTime;
	private String date = DateUtils.getSystemDate();
    
    private int lptsamCount;
    
    private String fileName;

	public LptsamData(Connection connection, Map<String, String> fileData, String fileName) throws SQLException
    {
    	initail(fileData);
    	this.fileName = fileName;
    }
	
	public LptsamBean getLptsamBean() {
		return lptsamBean;
	}

	public void setLptsamBean(LptsamBean lptsamBean) {
		this.lptsamBean = lptsamBean;
	}
	
	public String getUptTime() {
		return uptTime;
	}

	public void setUptTime(String uptTime) {
		this.uptTime = uptTime;
	}
    
    private void initail(Map<String, String> fileData) 
    {
    	lptsamBean = new LptsamBean();
    	lptsamBean.setActionStatus(fileData.get("ACTION_STATUS"));
    	lptsamBean.setMemGroupId(fileData.get("MEMBER_GROUP_ID"));
    	lptsamBean.setCid(fileData.get("CID"));
    	lptsamBean.setStatus(fileData.get("STATUS"));
    	lptsamBean.setSamType(fileData.get("SAM_TYPE"));
	}

	public TbLptsamInfo getLptsamInfo(Connection connection) throws SQLException
    {
    	/*if(orgCid.length() >8) {
    		cid = orgCid.substring(orgCid.length()-8, orgCid.length());
    	}
    	else {
    		cid = orgCid;
    	}*/
		TbLptsamInfo lptsamInfo = null;
		
        TbLptsamInfo info = new TbLptsamInfo();
        info.setCid(lptsamBean.getCid().trim());
        info.setSamType(lptsamBean.getSamType());
        
        Vector<TbLptsamInfo> lptsamResult = new Vector<TbLptsamInfo>();
        lptsamCount = new TbLptsamMgr(connection).queryMultiple(info, lptsamResult);
        
        if(lptsamResult.size() >0) {
        	lptsamInfo = lptsamResult.get(0);
        }
        return lptsamInfo;
    }

    public List<String> handleCust(Connection connection, String batchDate, String fileDate) throws Exception {
    	
    	List<String> sqls = new ArrayList<String>();
    	String addLptsamSql = null;
    	String updateLptsamSql = null;
    	String addUptSql = null;
    	
    	log.debug("action status: " + lptsamBean.getActionStatus());
    	
    	if(lptsamBean.getActionStatus().equals("I")) {
    		if(lptsamCount == 1) {
				updateLptsamSql = updateLptsam(true);
	    		log.debug("updateLptsamSql: " + updateLptsamSql);
	        	sqls.add(updateLptsamSql);
	        	
	        	addUptSql = addUptSql("2");
	        	log.debug("addUptSql: " + addUptSql);
	        	sqls.add(addUptSql);
    		}
    		else {
    			addLptsamSql = addLptsam();
        		log.debug("addLptsamSql: " + addLptsamSql);
            	sqls.add(addLptsamSql);
            	
            	addUptSql = addUptSql("1");
            	log.debug("addUptSql: " + addUptSql);
            	sqls.add(addUptSql);
    		}
    	}
    	/*else if(lptsamBean.getActionStatus().equals("U")) {
    		remarkLptsamSql = remarkLptsam();
    		log.debug("remarkLptsamSql: " + remarkLptsamSql);
        	sqls.add(remarkLptsamSql);
        	
        	addUptSql = addUptSql("2");
        	log.debug("addUptSql: " + addUptSql);
        	sqls.add(addUptSql);
    	}*/
    	else {
    		updateLptsamSql = updateLptsam(false);
    		log.debug("updateLptsamSql: " + updateLptsamSql);
        	sqls.add(updateLptsamSql);
        	
        	addUptSql = addUptSql("2");
        	log.debug("addUptSql: " + addUptSql);
        	sqls.add(addUptSql);
    	}   
        return sqls;
    }   
    
    private String updateLptsam(boolean updateMemGpflag) throws Exception 
    {
		uptTime = BatchUtils.getNextSec(uptTime,1);
    	log.debug("uptTime="+uptTime);

    	StringBuffer sb = new StringBuffer();
    	sb.append("update TB_LPTSAM set");
    	if(updateMemGpflag) {
    		sb.append(" MEM_GROUP_ID='").append(lptsamBean.getMemGroupId()).append("',");
    	}
    	sb.append(" DEVICE_ID='").append("00000000000000000000").append("'");
    	sb.append(", STATUS='").append(lptsamBean.getStatus()).append("'");
    	sb.append(", HQ_GROUP_ID='").append(lptsamBean.getHqGroupId()).append("'");
    	sb.append(", UPDATE_DATE='").append(date).append("'");
    	sb.append(", UPDATE_TIME='").append(uptTime).append("'");
    	sb.append(", UPT_USERID='").append("BATCH").append("'");
    	sb.append(", UPT_DATE='").append(date).append("'");
    	sb.append(", UPT_TIME='").append(uptTime).append("'");
    	sb.append(", APRV_USERID='").append("BATCH").append("'");
    	sb.append(", APRV_DATE='").append(date).append("'");
    	sb.append(", APRV_TIME='").append(uptTime).append("'");
    	sb.append(" WHERE CID='").append(lptsamBean.getCid()).append("'");
    	sb.append(" AND SAM_TYPE='").append(lptsamBean.getSamType()).append("'");
    	
    	//String sql = "update TB_LPTSAM set MEM_GROUP_ID='" + fileData.get("MEMBER_GROUP_ID") + "', status='"+ fileData.get("STATUS") +"' WHERE CID='" + fileData.get("CID") + "'";	
        return sb.toString();
	}

	private String remarkLptsam() throws Exception 
	{		
    	TbLptsamInfo info = new TbLptsamInfo();
    	info.setCid(lptsamBean.getCid().trim());
    	info.setSamType(lptsamBean.getSamType());
    	info.setStatus(lptsamBean.getStatus());
    	info.setMemGroupId(lptsamBean.getMemGroupId());
    	info.setFileName(fileName);
    	info.setUpdateDate(date);
    	info.setUpdateTime(uptTime);
    	info.setUptUserid("BATCH");
    	info.setUptDate(date);
    	info.setUptTime(uptTime);
    	info.setAprvUserid("BATCH");
    	info.setAprvDate(date);
    	info.setAprvTime(uptTime);
    	
        return info.toUpdateSQL();
    }
    
    private String addLptsam() throws Exception 
    {	    
    	TbLptsamInfo info = new TbLptsamInfo();
    	info.setCid(lptsamBean.getCid().trim());
    	info.setSamType(lptsamBean.getSamType());
    	info.setStatus(lptsamBean.getStatus());
    	info.setMemGroupId(lptsamBean.getMemGroupId());
    	info.setFileName(fileName);
    	info.setHqGroupId(lptsamBean.getHqGroupId());
    	info.setUpdateDate(date);
    	info.setUpdateTime(uptTime);
    	info.setUptUserid("BATCH");
    	info.setUptDate(date);
    	info.setUptTime(uptTime);
    	info.setAprvUserid("BATCH");
    	info.setAprvDate(date);
    	info.setAprvTime(uptTime);
    	
        return info.toInsertSQL();
    }	
    
    private String addUptSql(String uptStatus) throws Exception 
    {	    
    	TbLptsamUptInfo info = new TbLptsamUptInfo();
    	info.setCid(lptsamBean.getCid().trim());
    	info.setSamType(lptsamBean.getSamType());
    	info.setStatus(lptsamBean.getStatus());
    	info.setMemGroupId(lptsamBean.getMemGroupId());
    	info.setFileName(fileName);
    	info.setHqGroupId(lptsamBean.getHqGroupId());
    	info.setUpdateDate(date);
    	info.setUpdateTime(uptTime);
    	info.setUptUserid("BATCH");
    	info.setUptDate(date);
    	info.setUptTime(uptTime);
    	info.setAprvUserid("BATCH");
    	info.setAprvDate(date);
    	info.setAprvTime(uptTime);
    	info.setUptStatus(uptStatus);
    	info.setAprvStatus("1");
    	
        return info.toInsertSQL();
    }
}
