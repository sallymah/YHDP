/*
 * Version: 1.0.0
 * Date: 2007-10-15
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;

import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbLifeCycleMonSumInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbMerchSumDtlInfo;
import tw.com.hyweb.service.db.info.TbMerchSumInfo;
import tw.com.hyweb.service.db.info.TbPaymentRequestInfo;
import tw.com.hyweb.service.db.mgr.TbLifeCycleMonSumMgr;
import tw.com.hyweb.service.db.mgr.TbMerchSumDtlMgr;
import tw.com.hyweb.service.db.mgr.TbMerchSumMgr;
import tw.com.hyweb.service.db.mgr.TbPaymentRequestMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * SumMerchUtil
 * </pre>
 * author: Ivan
 */
public class SumPaymentRequestUtil {
    private static Logger log = Logger.getLogger(SumPaymentRequestUtil.class);
    
    private static String sysDate = DateUtils.getSystemDate();
    private static String sysTime = DateUtils.getSystemTime();

    public static SumPaymentRequestResultInfo getSumPaymentRequestResultInfo(Connection conn, TbMemberInfo memInfo, String batchDate) {
		Statement stmt = null;
        ResultSet rs = null;
        
        String endDate = DateUtil.addDate(batchDate, -1);
        String statMon = endDate.substring(0, 6);
        String startDate = statMon + "01";
        String last1Mon = DateUtil.addMonth(endDate, -1).substring(0,6);
        String last3Mon =DateUtil.addMonth(endDate, -3).substring(0,6);
        
        int avgCnt = getAvgCnt(conn, last1Mon, last3Mon);
        double avgAmt = getAvgAmt(conn, last1Mon, last1Mon);
        
        SumPaymentRequestResultInfo resultInfo = new SumPaymentRequestResultInfo();
        String sql = getPaymentRequestSumInfosSQL(memInfo.getMemId(), startDate, endDate);
        log.info("sql: " + sql);
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                TbPaymentRequestInfo paymentRequestInfo = new TbPaymentRequestInfo();
                paymentRequestInfo.setAcqMemId(memInfo.getMemId());
                paymentRequestInfo.setStatMon(statMon);
                paymentRequestInfo.setTxnCnt(rs.getInt("TXN_CNT"));
                paymentRequestInfo.setTxnAmt(rs.getDouble("TXN_AMT"));
                paymentRequestInfo.setAvgCnt(avgCnt);
                paymentRequestInfo.setAvgAmt(avgAmt);
                paymentRequestInfo.setProcDate(sysDate);
                paymentRequestInfo.setProcTime(sysTime);
                
                resultInfo.setMemInfos(memInfo);
                resultInfo.setPaymentRequestInfo(paymentRequestInfo);
            }
        }
        catch (SQLException se) {
        	resultInfo = null;
            log.error("getLifeCycleSumInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return resultInfo;
	}

    private static double getAvgAmt(Connection conn, String last1Mon, String last3Mon) {
    	Statement stmt = null;
        ResultSet rs = null;
        
        String sql = getAvgAmtSQL(conn, last1Mon, last3Mon);
        log.info("sql: " + sql);
        
        try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
	            return rs.getDouble("AVG_AMT");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
			return 0.0;
		}
        finally {
        	ReleaseResource.releaseDB(null, stmt, rs);
        }
        return 0.0;
	}

    private static String getAvgAmtSQL(Connection conn, String last1Mon, String last3Mon) {
		StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" sum(txn_amt)/3 AS AVG_AMT ");
        sql.append(" FROM ");
        sql.append(" TB_PAYMENT_REQUEST ");
        sql.append(" WHERE ").append("stat_mon >='").append(last3Mon).append("'");
        sql.append(" AND ").append("stat_mon <='").append(last1Mon).append("'");
        return sql.toString();
	}
	
	private static int getAvgCnt(Connection conn, String last1Mon, String last3Mon) {
    	Statement stmt = null;
        ResultSet rs = null;
        
        String sql = getAvgCntSQL(last1Mon, last3Mon);
        log.info("sql: " + sql);
        
        try {
        	stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
	            return rs.getInt("AVG_CNT");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
        
        finally {
        	ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        return 0;
	}
	
	private static String getAvgCntSQL(String last1Mon, String last3Mon) {
		StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" sum(txn_cnt)/3 AS AVG_CNT ");
        sql.append(" FROM ");
        sql.append(" TB_PAYMENT_REQUEST ");
        sql.append(" WHERE ").append("stat_mon >='").append(last3Mon).append("'");
        sql.append(" AND ").append("stat_mon <='").append(last1Mon).append("'");
        return sql.toString();
	}

	public static String getPaymentRequestSumInfosSQL(String memId, String lastMonthFirst, String lastMonthLast) {
		StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" CASE P_CODE WHEN '7647' THEN TXN_CNT ELSE TXN_CNT*(-1) END AS TXN_CNT, ");
        sql.append(" CASE P_CODE WHEN '7647' THEN TXN_AMT ELSE TXN_AMT*(-1) END AS TXN_AMT ");
        sql.append(" FROM ");
        sql.append(" TB_MERCH_SUM ");
        sql.append(" WHERE ").append("ACQ_MEM_ID='").append(memId).append("'");
        sql.append(" AND PROC_DATE BETWEEN '").append(lastMonthFirst).append("' AND '").append(lastMonthLast).append("'");
        sql.append(" AND P_CODE in ('7647', '7646')");
        return sql.toString();
	}
	
	public static boolean handleSumPaymentRequestResultInfo(Connection conn, SumPaymentRequestResultInfo resultInfo) {
	    if (resultInfo.getPaymentRequestInfo() == null) {
            return false;
        }

        boolean autoCommit = false;
        boolean ret = false;
        
        TbPaymentRequestMgr mgr = new TbPaymentRequestMgr(conn);
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
     
            //insert TB_PAYMENT_REQUEST
            mgr.insert(resultInfo.getPaymentRequestInfo());
            
            ret = true;     
        }
        catch (Exception e) {
            ret = false;
            log.error("handleSumPaymentRequestResultInfo error:" + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                }
                catch (Exception ignore) {
                    ;
                }
            }
        }
        finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommit);
                }
                catch (Exception ignore) {
                    ;
                }
            }
        }
        return ret;
	}
}
