/*
 * 晶片電子錢消費抵扣的點別與順序  tag:0003
 */
package tw.com.hyweb.svc.yhdp.batch.parmdown.function;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import tw.com.hyweb.core.cp.batch.parmdown.ParameterFunction;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.service.db.info.TbTermParDefInfo;

public class ECashPointAddressMapping implements ParameterFunction
{
    public List<String> getValues(Connection connection, String batchDate, TbTermParDefInfo define, TbTermInfo terminal) throws SQLException
    {
        List<String> tagValueList = new ArrayList<String>();
        for (HashMap<String,String> hm : getBonusIssInfo(terminal.getMemId()))
        {
            StringBuilder sb = new StringBuilder("");

            String issMemId = hm.get("MEM_ID");
            String chipEcashStep = hm.get("CHIP_ECASH_STEP");
            sb.append( issMemId );
            sb.append( chipEcashStep );
            tagValueList.add(sb.toString());
        }
        return tagValueList;
    }

    @SuppressWarnings("unchecked")
    protected Vector<HashMap> getBonusIssInfo(String acqMemId)
    {
        Vector<HashMap> vtr = new Vector<HashMap>();

        StringBuffer sql = new StringBuffer();
        
        //20080418 改為只下 TB_ACQ_DEF.ACQ_MEM_ID = {acqMemId} 的參數
//        sql.append("select MEM_ID, ECASH_BONUS_ID, POINT1_BONUS_ID, POINT2_BONUS_ID, POINT3_BONUS_ID, POINT4_BONUS_ID" +
//        		" from TB_BONUS_ISS_DEF where BONUS_BASE='C'");
        
        sql.append("select distinct b.MEM_ID, a.CHIP_ECASH_STEP")
           .append(" from TB_BONUS_ISS_DEF b, TB_ACQ_DEF a" )
           .append(" where b.MEM_ID= a.ISS_MEM_ID AND b.BONUS_BASE='C'");
        
        vtr = BatchUtil.getInfoListHashMap(sql.toString());
        
        return vtr;
    }
       
    protected String getChipEcashStep(Connection conn, String acqMemId, String issMemId) throws SQLException
    {
    	String chipEcashStep = "";
    	
    	String sql = String.format("select CHIP_ECASH_STEP from TB_ACQ_DEF " +
    	"where ACQ_MEM_ID='%s' and ISS_MEM_ID='%s'", acqMemId, issMemId );
    	chipEcashStep = DBService.selectSingleValue(DBService.getDBService().select(sql, conn));
    	//System.out.println(sql);
    	return chipEcashStep;
    }
    
    
}
