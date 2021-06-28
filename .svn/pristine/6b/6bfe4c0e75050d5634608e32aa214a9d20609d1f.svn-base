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
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;

import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMerchSumDtlInfo;
import tw.com.hyweb.service.db.info.TbMerchSumInfo;
import tw.com.hyweb.service.db.mgr.TbMerchSumDtlMgr;
import tw.com.hyweb.service.db.mgr.TbMerchSumMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * SumMerchUtil
 * </pre>
 * author: Ivan
 */
public class SumMerchUtil {
    private static Logger log = Logger.getLogger(SumMerchUtil.class);

    /**
     * <pre>
     * 取得收單單位列表, each element is SumMerchInfo object
     * </pre>
     * @param batchDate batchDate
     * @return 收單單位列表
     */
    public static List makeSumMerchInfos(String batchDate) {
        // pcode
        String pcodeSQL = "SELECT P_CODE FROM TB_P_CODE_DEF WHERE SUM_FLAG='1' ORDER BY P_CODE";
        Vector v = null;
        try {
            v =  BatchUtil.getInfoList(pcodeSQL, "batch");
        }
        catch (Exception ignore) {
            v = new Vector();
            log.warn("makeSumMerchInfos (sql:" + pcodeSQL + ") error:" + ignore.getMessage(), ignore);
        }
        List pcodes = new ArrayList();
        for (int i = 0; i < v.size(); i++) {
            Vector row = (Vector) v.get(i);
            String pcode = (String) row.get(0);
            pcodes.add(pcode);
        }
        List sumMerchInfos = new ArrayList();
        // ACQ_MEM_ID
        String memberSQL = "SELECT MEM_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1) = '1' ORDER BY MEM_ID";
        try {
            v = BatchUtil.getInfoList(memberSQL, "batch");
        }
        catch (Exception ignore) {
            v = new Vector();
            log.warn("makeSumMerchInfos (sql:" + memberSQL + ") error:" + ignore.getMessage(), ignore);
        }
        for (int i = 0; i < v.size(); i++) {
            Vector row = (Vector) v.get(i);
            String memId = (String) row.get(0);
            SumMerchInfo info = new SumMerchInfo();
            info.setBatchDate(batchDate);
            info.setAcqmemId(memId);
            info.setPcodes(pcodes);
            sumMerchInfos.add(info);
        }
        return sumMerchInfos;
    }

    /**
     * <pre>
     * 依 info 資訊取得收單單位的彙總資料(TB_MERCH_SUM),
     * each elemnt is TbMerchSumInfo object, 會 new 一個 Connection 來做
     * </pre>
     * @param info SumMerchInfo
     * @return List, each element is TbMerchSumInfo object
     */
    public static List getMerchSumInfos(SumMerchInfo info) {
        Connection conn = null;
        List merchSumInfos = new ArrayList();
        try {
            conn = DBService.getDBService().getConnection("batch");
            merchSumInfos = getMerchSumInfos(conn, info);
        }
        catch (Exception ignore) {
            merchSumInfos = null;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        return merchSumInfos;
    }

    private static String getPcodeList(List pcodes) {
        if (pcodes == null || pcodes.size() == 0) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pcodes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("'");
            sb.append(pcodes.get(i));
            sb.append("'");
        }
        return sb.toString();
    }


// 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總
    private static String getMerchSumInfosSQL(SumMerchInfo info) {
    	
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" a.ISS_MEM_ID,a.ACQ_MEM_ID,b.CARD_PRODUCT,a.MERCH_ID,a.P_CODE,a.CURRENCY_CODE,COUNT(*) AS TXN_CNT, SUM(a.CREDIT_AUTH_AMT) AS CREDIT_AUTH_AMT, SUM(a.TXN_AMT) AS TXN_AMT, SUM(a.TXN_REDEEM_AMT) AS TXN_REDEEM_AMT, SUM(a.TXN_PRICE) AS TXN_PRICE ");
        sql.append(" FROM TB_TRANS a ");
        sql.append(" LEFT JOIN TB_CARD b ON a.CARD_NO = b.CARD_NO ");
        sql.append(" WHERE ").append(Layer2Util.makeParMonDayCondWithAlias("a.",info.getBatchDate(),"",false));
        sql.append(" AND a.SETTLE_PROC_DATE = " + StringUtil.toSqlValueWithSQuote(info.getBatchDate()));
        sql.append(" AND a.ACQ_MEM_ID = " + StringUtil.toSqlValueWithSQuote(info.getAcqmemId()));
        String pcodes = getPcodeList(info.getPcodes());
        sql.append(" AND a.P_CODE IN (" + pcodes + ")");
        sql.append(" AND a.MERCH_SUM_DATE IS NULL");
        sql.append(" GROUP BY a.ISS_MEM_ID,a.MERCH_ID,b.CARD_PRODUCT,a.P_CODE,a.ACQ_MEM_ID,a.CURRENCY_CODE");
        sql.append(" ORDER BY a.ISS_MEM_ID,a.MERCH_ID,b.CARD_PRODUCT,a.P_CODE,a.ACQ_MEM_ID,a.CURRENCY_CODE");
        return sql.toString();
    }

    /**
     * <pre>
     * 依 info 資訊取得收單單位的彙總資料(TB_MERCH_SUM),
     * each elemnt is TbMerchSumInfo object.
     * </pre>
     * @param conn conn
     * @param info SumMerchInfo
     * @return List, each element is TbMerchSumInfo object
     */
    public static List getMerchSumInfos(Connection conn, SumMerchInfo info) {
        if (info == null) {
            return null;
        }
        Statement stmt = null;
        ResultSet rs = null;
        List merchSumInfos = new ArrayList();
        String sql = getMerchSumInfosSQL(info);
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String month = info.getBatchDate().substring(4,6);
            String day = info.getBatchDate().substring(6,8);
            while (rs.next()) {
                TbMerchSumInfo merchSumInfo = new TbMerchSumInfo();
                merchSumInfo.setIssMemId(rs.getString("ISS_MEM_ID"));
                merchSumInfo.setAcqMemId(rs.getString("ACQ_MEM_ID"));
                merchSumInfo.setCardProduct(rs.getString("CARD_PRODUCT"));
                merchSumInfo.setMerchId(rs.getString("MERCH_ID"));
                merchSumInfo.setProcDate(info.getBatchDate());
                merchSumInfo.setPCode(rs.getString("P_CODE"));
                merchSumInfo.setTxnCnt(rs.getBigDecimal("TXN_CNT"));
                merchSumInfo.setTxnAmt(rs.getBigDecimal("TXN_AMT"));
                merchSumInfo.setCurrencyCode(rs.getString("CURRENCY_CODE"));
                merchSumInfo.setCreditAuthAmt(rs.getBigDecimal("CREDIT_AUTH_AMT"));
                merchSumInfo.setTxnRedeemAmt(rs.getBigDecimal("TXN_REDEEM_AMT"));
                merchSumInfo.setTxnPrice(rs.getBigDecimal("TXN_PRICE"));
                merchSumInfo.setParMon(month);
                merchSumInfo.setParDay(day);
                merchSumInfos.add(merchSumInfo);
            }
        }
        catch (SQLException se) {
            merchSumInfos = null;
            log.error("getMerchSumInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return merchSumInfos;
    }

    /**
     * <pre>
     * 依 info 資訊取得收單單位的彙總資料(TB_MERCH_SUM_DTL),
     * each elemnt is TbMerchSumDtlInfo object, 會 new 一個 Connection 來做
     * </pre>
     * @param info SumMerchInfo
     * @return List, each element is TbMerchSumDtlInfo object
     */
    public static List getMerchSumDtlInfos(SumMerchInfo info) {
        Connection conn = null;
        List merchSumDtlInfos = new ArrayList();
        try {
            conn = DBService.getDBService().getConnection("batch");
            merchSumDtlInfos = getMerchSumDtlInfos(conn, info);
        }
        catch (Exception ignore) {
            merchSumDtlInfos = null;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        return merchSumDtlInfos;
    }

    private static String getMerchSumDtlInfosSQLGeneral(SumMerchInfo info) {

        StringBuffer sb = new StringBuffer();
        sb.append(" SUM(TB_TRANS_DTL.BONUS_QTY) AS BONUS_QTY, SUM(TB_TRANS_DTL.TXN_REDEEM_AMT) AS TXN_REDEEM_AMT, COUNT(*) AS TTL_CNT");
        sb.append(" FROM (SELECT TB_TRANS.*, TB_CARD.CARD_PRODUCT  FROM TB_TRANS");
        sb.append(" LEFT JOIN TB_CARD ON TB_TRANS.CARD_NO = TB_CARD.CARD_NO ");
        sb.append(" WHERE ").append(Layer2Util.makeParMonDayCondWithAlias("TB_TRANS.",info.getBatchDate(),"",false));
        sb.append(" AND TB_TRANS.SETTLE_PROC_DATE = " + StringUtil.toSqlValueWithSQuote(info.getBatchDate()));
        sb.append(" AND TB_TRANS.ACQ_MEM_ID = " + StringUtil.toSqlValueWithSQuote(info.getAcqmemId()));
        String pcodes = getPcodeList(info.getPcodes());
        sb.append(" AND TB_TRANS.P_CODE IN (" + pcodes + ")");
        sb.append(" AND TB_TRANS.MERCH_SUM_DATE IS NULL");
        sb.append(" ) TB_TRANS, (SELECT * FROM TB_TRANS_DTL");
        sb.append(" WHERE ").append(Layer2Util.makeParMonDayCond(info.getBatchDate(),"",false));
        sb.append(" AND SETTLE_PROC_DATE = " + StringUtil.toSqlValueWithSQuote(info.getBatchDate()));
        sb.append(" AND P_CODE IN (" + pcodes + ")");        
        sb.append(" )TB_TRANS_DTL");
        sb.append(" WHERE TB_TRANS.CARD_NO = TB_TRANS_DTL.CARD_NO");
        sb.append(" AND TB_TRANS.EXPIRY_DATE = TB_TRANS_DTL.EXPIRY_DATE");
        sb.append(" AND TB_TRANS.LMS_INVOICE_NO = TB_TRANS_DTL.LMS_INVOICE_NO");
        sb.append(" AND TB_TRANS.P_CODE = TB_TRANS_DTL.P_CODE");
        return sb.toString();
    }

// 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總
    private static String getMerchSumDtlInfosSQL(SumMerchInfo info) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" TB_TRANS.ISS_MEM_ID,TB_TRANS.ACQ_MEM_ID,TB_TRANS.CARD_PRODUCT,TB_TRANS.MERCH_ID,TB_TRANS.P_CODE, TXN_CODE, BONUS_ID,");
        sql.append(" BONUS_SDATE,BONUS_EDATE,");
        String general = getMerchSumDtlInfosSQLGeneral(info);
        sql.append(general);
        sql.append(" GROUP BY TB_TRANS.ISS_MEM_ID,TB_TRANS.ACQ_MEM_ID,TB_TRANS.MERCH_ID,TB_TRANS.CARD_PRODUCT,TB_TRANS.P_CODE, TXN_CODE, BONUS_ID, BONUS_SDATE,BONUS_EDATE");
        sql.append(" ORDER BY TB_TRANS.ACQ_MEM_ID,TB_TRANS.MERCH_ID,TB_TRANS.CARD_PRODUCT,TB_TRANS.ISS_MEM_ID,TB_TRANS.P_CODE, TXN_CODE, BONUS_ID, BONUS_SDATE,BONUS_EDATE");
        return sql.toString();
    }

    /**
     * <pre>
     * 依 info 資訊取得收單單位的彙總資料(TB_MERCH_SUM_DTL),
     * each elemnt is TbMerchSumDtlInfo object
     * </pre>
     * @param conn conn
     * @param info SumMerchInfo
     * @return List, each element is TbMerchSumDtlInfo object
     */
    public static List getMerchSumDtlInfos(Connection conn, SumMerchInfo info) {
        if (info == null) {
            return null;
        }
        Statement stmt = null;
        ResultSet rs = null;
        List merchSumDtlInfos = new ArrayList();
        String sql = getMerchSumDtlInfosSQL(info);
        //debug 2011/07/01
        log.debug("getMerchSumDtlInfosSQL = " + sql.toString() );

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String month = info.getBatchDate().substring(4,6);
            String day = info.getBatchDate().substring(6,8);
            while (rs.next()) {
                TbMerchSumDtlInfo merchSumDtlInfo = new TbMerchSumDtlInfo();
                merchSumDtlInfo.setIssMemId(rs.getString("ISS_MEM_ID"));
                merchSumDtlInfo.setAcqMemId(info.getAcqmemId());
                merchSumDtlInfo.setCardProduct(rs.getString("CARD_PRODUCT"));
                merchSumDtlInfo.setMerchId(rs.getString("MERCH_ID"));
                merchSumDtlInfo.setProcDate(info.getBatchDate());
                merchSumDtlInfo.setPCode(rs.getString("P_CODE"));
                merchSumDtlInfo.setTxnCode(rs.getString("TXN_CODE"));
                merchSumDtlInfo.setBonusId(rs.getString("BONUS_ID"));
                merchSumDtlInfo.setBonusSdate(rs.getString("BONUS_SDATE"));
                merchSumDtlInfo.setBonusEdate(rs.getString("BONUS_EDATE"));
                merchSumDtlInfo.setBonusQty(rs.getBigDecimal("BONUS_QTY"));
                merchSumDtlInfo.setTxnRedeemAmt(rs.getBigDecimal("TXN_REDEEM_AMT"));
                merchSumDtlInfo.setTtlCnt(rs.getBigDecimal("TTL_CNT"));
                merchSumDtlInfo.setParMon(month);
                merchSumDtlInfo.setParDay(day);
                
                merchSumDtlInfos.add(merchSumDtlInfo);
            }
        }
        catch (SQLException se) {
            merchSumDtlInfos = null;
            log.error("getMerchSumDtlInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return merchSumDtlInfos;
    }

    /**
     * <pre>
     * 1. 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總
     * merchSumInfos:List, each element is TbMerchSumInfo object
     * 2. 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總
     * merchSumDtlInfos:List, each element is TbMerchSumDtlInfo object
     * 會 new 一個 Connection 去做
     * </pre>
     * @param info SumMerchInfo info
     * @return SumMerchResultInfo object
     */
    public static SumMerchResultInfo getSumMerchResultInfo(SumMerchInfo info) {
        Connection conn = null;
        SumMerchResultInfo sumMerchResultInfo = null;
        try {
            conn = DBService.getDBService().getConnection("batch");
            sumMerchResultInfo = getSumMerchResultInfo(conn, info);
        }
        catch (Exception ignore) {
            sumMerchResultInfo = null;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        return sumMerchResultInfo;
    }

    /**
     * <pre>
     * 1. 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總
     * merchSumInfos:List, each element is TbMerchSumInfo object
     * 2. 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總
     * merchSumDtlInfos:List, each element is TbMerchSumDtlInfo object
     * </pre>
     * @param conn conn
     * @param info SumMerchInfo info
     * @return SumMerchResultInfo object
     */
    public static SumMerchResultInfo getSumMerchResultInfo(Connection conn, SumMerchInfo info) {
        if (info == null) {
            return null;
        }
        List merchSumInfos = getMerchSumInfos(conn, info);
        if (merchSumInfos == null) {
            log.info("merchSumInfos is null!");
            return null;
        }
        List merchSumDtlInfos = getMerchSumDtlInfos(conn, info);
        if (merchSumDtlInfos == null) {
            log.info("merchSumDtlInfos is null!");
            return null;
        }
        SumMerchResultInfo resultInfo = new SumMerchResultInfo();
        resultInfo.setSumMerchInfo(info);
        resultInfo.setMerchSumInfos(merchSumInfos);
        resultInfo.setMerchSumDtlInfos(merchSumDtlInfos);
        return resultInfo;
    }

    /**
     * <pre>
     * 依 resultInfo 來處理一個 member 的彙總資料
     * 1. 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總, 新增至TB_MERCH_SUM
     * merchSumInfos:List, each element is TbMerchSumInfo object
     * 2. 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總, 新增至TB_MERCH_SUM_DTL
     * merchSumDtlInfos:List, each element is TbMerchSumDtlInfo object
     * 3. 更新此 member 彙總成功的 TB_TRANS
     * 會 new 一個 Connection 來做
     * </pre>
     * @param resultInfo resultInfo
     * @return 成功傳回 true, 失敗傳回 false
     */
    public static boolean handleSumMerchResultInfo(SumMerchResultInfo resultInfo) {
        Connection conn = null;
        boolean ret = false;
        try {
            conn = DBService.getDBService().getConnection("batch");
            ret = handleSumMerchResultInfo(conn, resultInfo);
        }
        catch (Exception ignore) {
            ret = false;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * <pre>
     * 依 resultInfo 來處理一個 member 的彙總資料
     * 1. 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總, 新增至TB_MERCH_SUM
     * merchSumInfos:List, each element is TbMerchSumInfo object
     * 2. 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總, 新增至TB_MERCH_SUM_DTL
     * merchSumDtlInfos:List, each element is TbMerchSumDtlInfo object
     * 3. 更新此 member 彙總成功的 TB_TRANS
     * </pre>
     * @param conn conn
     * @param resultInfo resultInfo
     * @return 成功傳回 true, 失敗傳回 false
     */
    public static boolean handleSumMerchResultInfo(Connection conn, SumMerchResultInfo resultInfo) {
        if (resultInfo == null) {
            return false;
        }

        boolean ret = false;
        
        try {
            conn.setAutoCommit(false);
            
            int num = 0;
            
            TbMerchSumMgr msmgr = new TbMerchSumMgr(conn);
            TbMerchSumDtlMgr msdmgr = new TbMerchSumDtlMgr(conn);
            
            for (int i = 0; i < resultInfo.getMerchSumInfos().size(); i++) {
            	
                TbMerchSumInfo info = (TbMerchSumInfo) resultInfo.getMerchSumInfos().get(i);
                log.debug("info: "+info);
                for (int j = num; j < resultInfo.getMerchSumDtlInfos().size(); j++) {
                	TbMerchSumDtlInfo info_dtl = (TbMerchSumDtlInfo) resultInfo.getMerchSumDtlInfos().get(j);
                	if (info.getMerchId().equals(info_dtl.getMerchId())) { 
                		log.debug("info_dtl: "+info_dtl);
                		msdmgr.insert(info_dtl);
                		num++;
                	}
                }
                msmgr.insert(info);
                
                updateTransByMerch(conn, info, Constants.RCODE_0000_OK);
                
            } //for

            conn.commit();
            ret = true;
            
        }
        catch (Exception e) {
            ret = false;
            log.error("handleSumMerchResultInfo error:" + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                }
                catch (Exception ignore) {
                    ;
                }
            }
        }
        return ret;
    }

    /**
     * <pre>
     * 更新此 merch 彙總成功的 TB_TRANS
     * </pre>
     * @param conn conn
     * @param info info
     * @param rcode rcode
     * @return 更新的筆數
     * @throws Exception 
     */
    public static int updateTransByMerch(Connection conn, TbMerchSumInfo info, String rcode) throws Exception {
        if (info == null) {
            return 0;
        }
   	
        Statement stmt = null;
        int updateCount = 0;
        StringBuffer sql = new StringBuffer();
                
        sql.append("UPDATE TB_TRANS");
        sql.append(" SET MERCH_SUM_DATE = " + StringUtil.toSqlValueWithSQuote(info.getProcDate()));
        sql.append(" ,MERCH_SUM_RCODE = " + StringUtil.toSqlValueWithSQuote(rcode));
        sql.append(" WHERE ").append(Layer2Util.makeParMonDayCond(info.getProcDate(),"",false));
        sql.append(" AND SETTLE_PROC_DATE = " + StringUtil.toSqlValueWithSQuote(info.getProcDate()));
        sql.append(" AND ACQ_MEM_ID = " + StringUtil.toSqlValueWithSQuote(info.getAcqMemId()));
        sql.append(" AND ISS_MEM_ID = " + StringUtil.toSqlValueWithSQuote(info.getIssMemId()));
        sql.append(" AND P_CODE = " + StringUtil.toSqlValueWithSQuote(info.getPCode()));
    	sql.append(" AND MERCH_ID = " + StringUtil.toSqlValueWithSQuote(info.getMerchId()));
    	sql.append(" AND CARD_NO IN ( SELECT CARD_NO FROM TB_CARD WHERE CARD_PRODUCT = " + StringUtil.toSqlValueWithSQuote(info.getCardProduct()));
        sql.append(") AND MERCH_SUM_DATE IS NULL");

        try {
            stmt = conn.createStatement();
            updateCount = stmt.executeUpdate(sql.toString());
            log.info("updateTransByMerch update:" + updateCount + " recs.");
        }
        catch (Exception ignore) {
            updateCount = -1;
            throw new Exception("updateTransByMerch error:" + sql.toString(), ignore);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, null);
        }
        return updateCount;
    }
}
