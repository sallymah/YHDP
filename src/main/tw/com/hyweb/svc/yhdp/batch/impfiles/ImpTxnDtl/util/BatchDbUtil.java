package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.util.DbUtil;

public class BatchDbUtil
{
    private static final Logger logger = Logger.getLogger(BatchDbUtil.class);
    private static final String sqlGetAcquireInfo = "select A.MEM_ID,MAX_UNSETTLE_DAY,RLD_MAX_AMT,RLD_LIMIT_FLAG,A.MEM_GROUP_ID,A.SAM_LOGON_TIME,A.OFFLINE_MAX_AMT,A.OFFLINE_MAX_COUNT,SIMPLE_CODE, OFFLINE_RLD_MAX_AMT, DUE_DAY, PROC_MONTH, NEW_TRANS_FLAG from  TB_MEMBER A, TB_TERM B Where A.mem_id=B.mem_id and B.term_id=? and B.merch_id=? and substr(A.mem_type,2,1)='1' and ? between NVL(A.EFFECTIVE_DATE,'00010101') and NVL(A.TERMINATION_DATE,'99991231')";
    /**
     * 
     * @param termId
     * @param merchId
     * @param conn
     * @return TbMemberInfo{mem_id}
     * @throws SQLException
     */
    public static TbMemberInfo getAcquireInfo(String hostDate, String termId,String merchId,Connection conn) throws SQLException
    {       
        TbMemberInfo member = null;
        Vector<String> params = new Vector<String>();
        params.add(termId);
        params.add(merchId);
        params.add(hostDate);
        Vector result = DbUtil.select(sqlGetAcquireInfo,params,conn);
        if (result!=null && result.size()>0)
        {
            Vector record = (Vector) result.get(0);
            member = new TbMemberInfo();
            member.setMemId((String)record.get(0));
            member.setMaxUnsettleDay((Number)record.get(1));
            member.setRldMaxAmt((Number)record.get(2));
            member.setRldLimitFlag((String)record.get(3));
            member.setMemGroupId((String)record.get(4));
            member.setSamLogonTime((Number)record.get(5));
            member.setOfflineMaxAmt((Number)record.get(6));
            member.setOfflineMaxCount((Number)record.get(7));
            member.setSimpleCode((String)record.get(8));
            member.setOfflineRldMaxAmt((Number)record.get(9));
            member.setDueDay((String)record.get(10));
            member.setProcMonth((String)record.get(11));
            member.setNewTransFlag((String)record.get(12));
        }
        else
        {
            logger.warn(sqlGetAcquireInfo);
            logger.warn(String.format("cannot find acquirer info with termId=%s,merchId=%s",termId,merchId));
        }
        return member;
    }

}
