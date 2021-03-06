/*
 * Version: 1.0.0
 * Date: 2015-02-10
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbAppointReloadSumDtlInfo;
import tw.com.hyweb.service.db.info.TbAppointReloadSumInfo;
import tw.com.hyweb.service.db.info.TbCardProductInfo;
import tw.com.hyweb.service.db.mgr.TbAppointReloadSumDtlMgr;
import tw.com.hyweb.service.db.mgr.TbAppointReloadSumMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * SumAppointReloadUtil
 * </pre>
 * author: Kevin
 */
public class SumAppointReloadUtil 
{
    private static Logger log = Logger.getLogger(SumAppointReloadUtil.class);
    
    private static int buffsize = 512;
    private static String sTable = "TB_CARD";
    private static String sysDate = DateUtils.getSystemDate();
    private static String sysTime = DateUtils.getSystemTime();

    public static List<TbCardProductInfo> makeCardProductInfos(Connection conn, String batchDate) 
    {
        Statement stmt = null;
        ResultSet rs = null;
        List<TbCardProductInfo> cardProductInfos = new ArrayList<TbCardProductInfo>();
        String sql = getCardProductInfosSQL();
        log.info("sql: " + sql);
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                TbCardProductInfo cardProductInfo = new TbCardProductInfo();
                cardProductInfo.setMemId(rs.getString("MEM_ID"));
                cardProductInfo.setCardProduct(rs.getString("CARD_PRODUCT"));
                cardProductInfos.add(cardProductInfo);
            }
        }
        catch (SQLException se) {
        	cardProductInfos = null;
            log.error("getCardProductInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return cardProductInfos;
	}

    public static String getCardProductInfosSQL() {
		StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" MEM_ID, CARD_PRODUCT ");
        sql.append(" FROM ");
        sql.append(" TB_CARD_PRODUCT ");
        return sql.toString();
	}

	public static ArrayList<SumAppointReloadResultInfo> getSumAppointReloadResultInfo(Connection conn,	TbCardProductInfo info, String batchDate) throws Exception 
	{
		if (info == null) {
            return null;
        }
		
		ArrayList<SumAppointReloadResultInfo> sumAppointReloadResultInfos = new ArrayList<SumAppointReloadResultInfo>();
		
		
		//????????????
		List<TbAppointReloadSumInfo> appReloadSumInfos = getAppointReloadSumInfo(conn, info, batchDate);
		for(TbAppointReloadSumInfo appReloadSumInfo : appReloadSumInfos) {
			SumAppointReloadResultInfo resultInfo = new SumAppointReloadResultInfo();
			resultInfo.setSumAppointReloadInfo(appReloadSumInfo);
			sumAppointReloadResultInfos.add(resultInfo);
		}
		
		//??????????????????
		List<TbAppointReloadSumDtlInfo> appReloadDtlSumInfos = getAppointReloadInfoSumInfo(conn, info, batchDate);
		for(TbAppointReloadSumDtlInfo appReloadDtlSumInfo : appReloadDtlSumInfos) {
			SumAppointReloadResultInfo resultInfo = new SumAppointReloadResultInfo();
			resultInfo.setSumAppointReloadDtlInfo(appReloadDtlSumInfo);
			sumAppointReloadResultInfos.add(resultInfo);
		}
	
		return sumAppointReloadResultInfos;
	}

	private static List<TbAppointReloadSumInfo> getAppointReloadSumInfo(Connection conn, TbCardProductInfo info, String batchDate) throws Exception 
	{
		Statement stmt = null;
        ResultSet rs = null;
        List<TbAppointReloadSumInfo> appointReloadSumInfos = new ArrayList<TbAppointReloadSumInfo>();
        //String lastMonth = DateUtil.addMonth(batchDate, -1).substring(0, 6);
        
		String sql = getAppointReloadSumSql(info, batchDate);
		log.info("sql: " + sql);
		
		try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                TbAppointReloadSumInfo appointReloadSumInfo = new TbAppointReloadSumInfo();
                appointReloadSumInfo.setMemId(rs.getString("EX_MEM_ID"));
                appointReloadSumInfo.setCardProduct(info.getCardProduct());
                appointReloadSumInfo.setProcDate(batchDate);
                appointReloadSumInfo.setImpDate(rs.getString("PROC_DATE"));
                appointReloadSumInfo.setTxnCardCnt(rs.getInt("TXN_CARD_CNT"));
                appointReloadSumInfo.setTxnCnt(rs.getInt("TXN_CNT"));
                appointReloadSumInfo.setTxnAmt(rs.getInt("TXN_AMT"));
                appointReloadSumInfo.setSuccTxnCnt(rs.getInt("SUCC_TXN_CNT"));
                appointReloadSumInfo.setSuccTxnAmt(rs.getInt("SUCC_TXN_AMT"));
                appointReloadSumInfo.setFailTxnCnt(rs.getInt("FAIL_TXN_CNT"));
                appointReloadSumInfo.setFailTxnAmt(rs.getInt("FAIL_TXN_AMT"));
                appointReloadSumInfo.setDwTxnCnt(rs.getInt("DW_TXN_CNT"));
                appointReloadSumInfo.setDwTxnAmt(rs.getInt("DW_TXN_AMT"));
                appointReloadSumInfo.setNotDwTxnCnt(rs.getInt("NOT_DW_TXN_CNT"));
                appointReloadSumInfo.setNotDwTxnAmt(rs.getInt("NOT_DW_TXN_AMT"));
                String parMon = StringUtil.pendingKey(DateUtil.getMonth(), 2);
                String parDay = StringUtil.pendingKey(DateUtil.getDay(), 2);
                appointReloadSumInfo.setParMon(parMon);
                appointReloadSumInfo.setParDay(parDay);
                appointReloadSumInfo.setSysDate(sysDate);
                appointReloadSumInfo.setSysTime(sysTime);

                appointReloadSumInfos.add(appointReloadSumInfo);
            }
        }
        catch (SQLException se) {
        	appointReloadSumInfos = null;
            log.error("getAppointReloadSumInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return appointReloadSumInfos;
	}
	
	private static String getAppointReloadSumSql(TbCardProductInfo info, String batchDate) throws Exception 
	{
		String lastDate = DateUtils.getPreviousDate(batchDate, 1);
		StringBuffer sql = new StringBuffer();
		sql.append("Select ");
		sql.append("APP.proc_Date, APP.ex_mem_id, count(*) as txn_cnt, Sum(APP_DTL.bonus_qty) as txn_amt, ");
		sql.append("Count(distinct APP.card_no) as txn_card_cnt, ");
		sql.append("Sum(case APP.proc_flag when '1' then 1 else 0 end) as succ_txn_cnt, ");
		sql.append("Sum(case APP.proc_flag when '1' then APP_DTL.bonus_qty else 0 end) as succ_txn_amt, ");
		sql.append("Sum(case APP.proc_flag when '0' then 1 else 0 end) as fail_txn_cnt, ");
		sql.append("Sum(case APP.proc_flag when '0' then APP_DTL.bonus_qty else 0 end) as fail_txn_amt, ");
		sql.append("Sum(case APP.proc_flag when '1' then Case APP.status when '1' then 1 else 0 end Else 0 end) as dw_txn_cnt, ");
		sql.append("Sum(case APP.proc_flag when '1' then Case APP.status when '1' then APP_DTL.bonus_qty else 0 end Else 0 end) as dw_txn_amt, ");
		sql.append("Sum(case APP.proc_flag when '1' then Case APP.status when '0' then 1 else 0 end Else 0 end) as not_dw_txn_cnt, ");
		sql.append("Sum(case APP.proc_flag when '1' then Case APP.status when '0' then APP_DTL.bonus_qty else 0 end Else 0 end) as not_dw_txn_amt ");
		sql.append("From ");
		//sql.append("(Select * from TB_APPOINT_RELOAD Where " + Layer2Util.makeParMonDayCond(batchDate, "", false) + " and proc_date like substr('" + lastDate + "', 1, 6)||'%') APP, ");
		sql.append("(Select * from TB_APPOINT_RELOAD Where " + Layer2Util.makeParMonDayCond(batchDate, "", false) + " and proc_date <= '" + batchDate + "') APP, ");
		sql.append("(Select * from TB_APPOINT_RELOAD_DTL Where " + Layer2Util.makeParMonDayCond(batchDate, "", false) + ") APP_DTL, ");
		sql.append("(Select * From TB_CARD Where card_product = '" + info.getCardProduct() + "') TB_CARD, ");
		sql.append("(Select * From TB_CARD_PRODUCT Where card_product = '" + info.getCardProduct() + "') TB_CARD_PRODUCT ");
		sql.append("Where APP.card_no = TB_CARD.card_no ");
		sql.append("And APP.expiry_date = TB_CARD.expiry_date ");
		sql.append("And APP.balance_id = APP_DTL.balance_id ");
		sql.append("And APP.ar_serno = APP_DTL.ar_serno ");
		sql.append("And APP_DTL.bonus_id = TB_CARD_PRODUCT.ecash_bonus_id ");
		sql.append("Group by APP.proc_date, APP.ex_mem_id ");
		
		return sql.toString();
	}

	private static List<TbAppointReloadSumDtlInfo> getAppointReloadInfoSumInfo(Connection conn, TbCardProductInfo info, String batchDate) throws Exception 
	{
		Statement stmt = null;
        ResultSet rs = null;
        List<TbAppointReloadSumDtlInfo> appointReloadSumDtlInfos = new ArrayList<TbAppointReloadSumDtlInfo>();
        //String lastMonth = DateUtil.addMonth(batchDate, -1).substring(0, 6);
        
		String sql = getAppointReloadSumDtlSql(info, batchDate);
		log.info("sql: " + sql);
		
		try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                TbAppointReloadSumDtlInfo appointReloadSumDtlInfo = new TbAppointReloadSumDtlInfo();
                appointReloadSumDtlInfo.setMemId(rs.getString("EX_MEM_ID"));
                appointReloadSumDtlInfo.setCardProduct(info.getCardProduct());
                appointReloadSumDtlInfo.setProcDate(batchDate);
                appointReloadSumDtlInfo.setImpDate(rs.getString("PROC_DATE"));
                appointReloadSumDtlInfo.setBonusId(rs.getString("BONUS_ID"));
                appointReloadSumDtlInfo.setBonusSdate(rs.getString("BONUS_SDATE"));
                appointReloadSumDtlInfo.setBonusEdate(rs.getString("BONUS_EDATE"));
                appointReloadSumDtlInfo.setBonusQty(rs.getInt("BONUS_QTY"));
                String parMon = StringUtil.pendingKey(DateUtil.getMonth(), 2);
                String parDay = StringUtil.pendingKey(DateUtil.getDay(), 2);
                appointReloadSumDtlInfo.setParMon(parMon);
                appointReloadSumDtlInfo.setParDay(parDay);
                appointReloadSumDtlInfo.setSysDate(sysDate);
                appointReloadSumDtlInfo.setSysTime(sysTime);

                appointReloadSumDtlInfos.add(appointReloadSumDtlInfo);
            }
        }
        catch (SQLException se) {
        	appointReloadSumDtlInfos = null;
            log.error("getAppointReloadSumInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return appointReloadSumDtlInfos;
	}

	private static String getAppointReloadSumDtlSql(TbCardProductInfo info, String batchDate) throws Exception 
	{
		String lastDate = DateUtils.getPreviousDate(batchDate, 1);
		StringBuffer sql = new StringBuffer();
		sql.append("Select ");
		sql.append("APP.proc_date, APP.ex_mem_id, APP_DTL.bonus_id, APP_DTL.bonus_sdate, APP_DTL.bonus_edate, ");
		sql.append("sum(APP_DTL.bonus_qty) as bonus_qty ");
		sql.append("From ");
		//sql.append("(Select * from TB_APPOINT_RELOAD Where " + Layer2Util.makeParMonDayCond(batchDate, "", false) + " and proc_date like substr('" + lastDate + "', 1, 6)||'%') APP, ");
		sql.append("(Select * from TB_APPOINT_RELOAD Where " + Layer2Util.makeParMonDayCond(batchDate, "", false) + " and proc_date < '" + batchDate + "') APP, ");
		sql.append("(Select * from TB_APPOINT_RELOAD_DTL Where " + Layer2Util.makeParMonDayCond(batchDate, "", false) + ") APP_DTL, ");
		sql.append("(Select * From TB_CARD Where card_product = '" + info.getCardProduct() + "') TB_CARD ");
		sql.append("Where APP.card_no = TB_CARD.card_no ");
		sql.append("And APP.balance_id = APP_DTL.balance_id ");
		sql.append("And APP.ar_serno = APP_DTL.ar_serno ");
		sql.append("Group by APP.proc_date, APP.ex_mem_id, bonus_id,bonus_sdate, bonus_edate ");
		
		return sql.toString();
	}

	public static boolean handleSumAppointReloadResultInfo(Connection conn,	SumAppointReloadResultInfo resultInfo) {
	    if (resultInfo.getSumAppointReloadInfo() == null && resultInfo.getSumAppointReloadDtlInfo() == null) {
            return false;
        }
        boolean autoCommit = false;
        boolean ret = false;
        
        TbAppointReloadSumMgr mgr = new TbAppointReloadSumMgr(conn);
        TbAppointReloadSumDtlMgr dtlMgr = new TbAppointReloadSumDtlMgr(conn);
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
     
            if(resultInfo.getSumAppointReloadInfo() != null){
	            //insert TB_APPOINT_RELOAD_SUM
	            mgr.insert(resultInfo.getSumAppointReloadInfo());
            }
            
            if(resultInfo.getSumAppointReloadDtlInfo() != null){
	            //insert TB_APPOINT_RELOAD_DTL_SUM
	            dtlMgr.insert(resultInfo.getSumAppointReloadDtlInfo());
            }
            
            ret = true;     
        }
        catch (Exception e) {
            ret = false;
            log.error("handleSumAppointReloadResultInfo error:" + e.getMessage(), e);
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

	/*public static boolean deleteLifeCycleMonSum(Connection conn, String batchDate) throws Exception {
		boolean autoCommit = false;
        boolean ret = false;
        
        String lastMonth = DateUtils.getPreviousMonth(batchDate.substring(0,6));
        
        String sql = "DELETE TB_LIFE_CYCLE_MON_SUM where proc_mon='" + lastMonth + "'";
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
     
            //delete TB_LIFE_CYCLE_MON_SUM
            DbUtil.sqlAction(sql, conn);
            
            ret = true;     
        }
        catch (Exception e) {
            ret = false;
            log.error("handleSumLifeCycleResultInfo error:" + e.getMessage(), e);
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
	}*/

}
