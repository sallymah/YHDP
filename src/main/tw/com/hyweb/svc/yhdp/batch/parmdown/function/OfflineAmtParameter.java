/*
 * 晶片電子錢消費抵扣的點別與順序  tag:0003
 */
package tw.com.hyweb.svc.yhdp.batch.parmdown.function;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.parmdown.ParameterFunction;
import tw.com.hyweb.core.yhdp.batch.util.StringUtils;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.service.db.info.TbTermParDefInfo;
import tw.com.hyweb.util.ReleaseResource;

public class OfflineAmtParameter implements ParameterFunction
{
	private static final Logger LOGGER = Logger.getLogger(OfflineAmtParameter.class);
	
	private final String  RLD_CHK_FLAG_MERCH = "M";
	private Connection conn;

    public List<String> getValues(Connection connection, String batchDate, TbTermParDefInfo define, TbTermInfo terminal) throws SQLException
    {
        List<String> tagValueList = new ArrayList<String>();
        conn = DBService.getDBService().getConnection("batch");             
        ArrayList<HashMap<String, String>> riskByMerchInfos = getOfflineTxnRiskByMerch(terminal.getMemId(),terminal.getMerchId());

        for(int idx=0; idx<riskByMerchInfos.size(); idx++)
        {
            StringBuilder sb = new StringBuilder("");
            if(riskByMerchInfos.get(idx).get("RLD_CHK_FLAG").equals(RLD_CHK_FLAG_MERCH))
            {
            	sb.append(riskByMerchInfos.get(idx).get("OFFLINE_MAX_AMT"));
	            sb.append(riskByMerchInfos.get(idx).get("OFFLINE_MAX_COUNT"));
	            sb.append(riskByMerchInfos.get(idx).get("SAM_LOGON_TIME"));
            }
            else {
            	ArrayList<HashMap<String, String>> riskByMemInfos = getOfflineTxnRiskByAcq(terminal.getMemId(),terminal.getMerchId());
            	for(int idx2=0; idx2<riskByMemInfos.size(); idx2++)
                {
            		sb.append(riskByMemInfos.get(idx2).get("OFFLINE_MAX_AMT"));
    	            sb.append(riskByMemInfos.get(idx2).get("OFFLINE_MAX_COUNT"));
    	            sb.append(riskByMemInfos.get(idx2).get("SAM_LOGON_TIME"));
                }
            }
            tagValueList.add(sb.toString());
        }
        ReleaseResource.releaseDB(conn, null, null);
        return tagValueList;
    }
    
    protected ArrayList<HashMap<String, String>> getOfflineTxnRiskByAcq(String acqMemId, String string) throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(); 
        
        //20080418 改為只下 TB_ACQ_DEF.ACQ_MEM_ID = {acqMemId} 的參數
//        sql.append("select MEM_ID, ECASH_BONUS_ID, POINT1_BONUS_ID, POINT2_BONUS_ID, POINT3_BONUS_ID, POINT4_BONUS_ID" +
//        		" from TB_BONUS_ISS_DEF where BONUS_BASE='C'");
        
        sql.append("SELECT NVL(OFFLINE_MAX_COUNT,0)OFFLINE_MAX_COUNT, NVL(OFFLINE_MAX_AMT,0)OFFLINE_MAX_AMT, NVL(SAM_LOGON_TIME,0)SAM_LOGON_TIME")
           .append(" from TB_MEMBER" )
           .append(" where MEM_ID= '" + acqMemId + "'");
        
        LOGGER.info("sql:" + sql.toString());
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery(sql.toString());
        
        while (rs.next()) {
			HashMap<String, String> hm = new HashMap<String, String>();
			//20161014 當值為0強制轉換為1
			hm.put("OFFLINE_MAX_COUNT", StringUtils.int2String(rs.getInt("OFFLINE_MAX_COUNT") == 0 ? 1 : rs.getInt("OFFLINE_MAX_COUNT"), 3));
			hm.put("OFFLINE_MAX_AMT", StringUtils.double2IntString(rs.getDouble("OFFLINE_MAX_AMT") == 0 ? 1 : rs.getInt("OFFLINE_MAX_AMT"), 2, 10));
			hm.put("SAM_LOGON_TIME", StringUtils.int2String(rs.getString("SAM_LOGON_TIME"), 3));
        	list.add(hm);
		}
        ReleaseResource.releaseDB(null, stat, rs);
        return list;
    }

    protected ArrayList<HashMap<String, String>> getOfflineTxnRiskByMerch(String acqMemId, String merchId) throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(); 
        
        //20080418 改為只下 TB_ACQ_DEF.ACQ_MEM_ID = {acqMemId} 的參數
//        sql.append("select MEM_ID, ECASH_BONUS_ID, POINT1_BONUS_ID, POINT2_BONUS_ID, POINT3_BONUS_ID, POINT4_BONUS_ID" +
//        		" from TB_BONUS_ISS_DEF where BONUS_BASE='C'");
        
        sql.append("SELECT RLD_CHK_FLAG, NVL(OFFLINE_MAX_COUNT,0)OFFLINE_MAX_COUNT, NVL(OFFLINE_MAX_AMT,0)OFFLINE_MAX_AMT, NVL(SAM_LOGON_TIME,0)SAM_LOGON_TIME")
           .append(" from TB_MERCH" )
           .append(" where MEM_ID= '" + acqMemId + "' and merch_id = '" + merchId + "'");
        
        LOGGER.info("sql:" + sql.toString());
           
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery(sql.toString());
        
        while (rs.next()) {
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("RLD_CHK_FLAG", rs.getString("RLD_CHK_FLAG"));
			//20161014 當值為0強制轉換為1
			hm.put("OFFLINE_MAX_COUNT", StringUtils.int2String(rs.getInt("OFFLINE_MAX_COUNT") == 0 ? 1 : rs.getInt("OFFLINE_MAX_COUNT"), 3));
			hm.put("OFFLINE_MAX_AMT", StringUtils.double2IntString(rs.getDouble("OFFLINE_MAX_AMT") == 0 ? 1 : rs.getInt("OFFLINE_MAX_AMT"), 2, 10));
			hm.put("SAM_LOGON_TIME", StringUtils.int2String(rs.getString("SAM_LOGON_TIME"), 3));
        	list.add(hm);
		}
        ReleaseResource.releaseDB(null, stat, rs);
        return list;
    }
}
