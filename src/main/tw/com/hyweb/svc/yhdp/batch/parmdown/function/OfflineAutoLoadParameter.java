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

public class OfflineAutoLoadParameter implements ParameterFunction
{
	private static final Logger LOGGER = Logger.getLogger(OfflineAutoLoadParameter.class);
	
	private final String  RLD_CHK_FLAG_MERCH = "M";
	private Connection conn;

    public List<String> getValues(Connection connection, String batchDate, TbTermParDefInfo define, TbTermInfo terminal) throws SQLException
    {
        List<String> tagValueList = new ArrayList<String>();
        conn = DBService.getDBService().getConnection("batch");             
        ArrayList<HashMap<String, String>> riskByMerchInfos = getOfflineAutoRoladByMerch(terminal.getMemId(),terminal.getMerchId());

        for(int idx=0; idx<riskByMerchInfos.size(); idx++)
        {
            StringBuilder sb = new StringBuilder("");
            if(riskByMerchInfos.get(idx).get("RLD_CHK_FLAG").equals(RLD_CHK_FLAG_MERCH))
            {
	            sb.append(riskByMerchInfos.get(idx).get("OFFLINE_RLD_MAX_AMT"));
	            sb.append("00000000");
            }
            else {
            	ArrayList<HashMap<String, String>> riskByMemInfos = getOfflineAutoRoladByAcq(terminal.getMemId(),terminal.getMerchId());
            	for(int idx2=0; idx2<riskByMemInfos.size(); idx2++)
                {
            		sb.append(riskByMemInfos.get(idx2).get("OFFLINE_RLD_MAX_AMT"));
    	            sb.append("00000000");
                }
            }
            tagValueList.add(sb.toString());
        }
        ReleaseResource.releaseDB(conn, null, null);
        return tagValueList;
    }
    
    protected ArrayList<HashMap<String, String>> getOfflineAutoRoladByAcq(String acqMemId, String string) throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(); 
        
        //20080418 改為只下 TB_ACQ_DEF.ACQ_MEM_ID = {acqMemId} 的參數
//        sql.append("select MEM_ID, ECASH_BONUS_ID, POINT1_BONUS_ID, POINT2_BONUS_ID, POINT3_BONUS_ID, POINT4_BONUS_ID" +
//        		" from TB_BONUS_ISS_DEF where BONUS_BASE='C'");
        
        sql.append("SELECT NVL(OFFLINE_RLD_MAX_AMT,0) OFFLINE_RLD_MAX_AMT")
           .append(" from TB_MEMBER" )
           .append(" where MEM_ID= '" + acqMemId + "'");
        
        LOGGER.info("sql:" + sql.toString());
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery(sql.toString());
        
        while (rs.next()) {
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("OFFLINE_RLD_MAX_AMT", StringUtils.int2String(rs.getDouble("OFFLINE_RLD_MAX_AMT"), 8));
        	list.add(hm);
		}
        ReleaseResource.releaseDB(null, stat, rs);
        return list;
    }

    protected ArrayList<HashMap<String, String>> getOfflineAutoRoladByMerch(String acqMemId, String merchId) throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(); 
        
        //20080418 改為只下 TB_ACQ_DEF.ACQ_MEM_ID = {acqMemId} 的參數
//        sql.append("select MEM_ID, ECASH_BONUS_ID, POINT1_BONUS_ID, POINT2_BONUS_ID, POINT3_BONUS_ID, POINT4_BONUS_ID" +
//        		" from TB_BONUS_ISS_DEF where BONUS_BASE='C'");
        
        sql.append("SELECT RLD_CHK_FLAG, NVL(OFFLINE_RLD_MAX_AMT,0) OFFLINE_RLD_MAX_AMT")
           .append(" from TB_MERCH" )
           .append(" where MEM_ID= '" + acqMemId + "' and merch_id = '" + merchId + "'");
        
        LOGGER.info("sql:" + sql.toString());
           
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery(sql.toString());
        
        while (rs.next()) {
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("RLD_CHK_FLAG", rs.getString("RLD_CHK_FLAG"));
			hm.put("OFFLINE_RLD_MAX_AMT", StringUtils.int2String(rs.getDouble("OFFLINE_RLD_MAX_AMT"), 8));
        	list.add(hm);
		}
        ReleaseResource.releaseDB(null, stat, rs);
        return list;
    }
}
