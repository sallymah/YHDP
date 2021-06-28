/*
 * Version: 1.0.0
 * Date: 2009-06-19
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
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbProductSumInfo;
import tw.com.hyweb.service.db.info.TbProductSumDtlInfo;
import tw.com.hyweb.service.db.mgr.TbProductSumDtlMgr;
import tw.com.hyweb.service.db.mgr.TbProductSumMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * SumProductUtil
 * </pre>
 * author: Ivan
 */
public class SumProductUtil {
    private static Logger log = Logger.getLogger(SumProductUtil.class);

    /**
     * <pre>
     * 取得收單單位列表, each element is SumProductInfo object
     * </pre>
     * @param batchDate batchDate
     * @return 收單單位列表
     */
    public static List makeSumProductInfos(String batchDate) {
        // pcode
        String pcodeSQL = "SELECT P_CODE FROM TB_P_CODE_DEF WHERE SUM_FLAG='1' ORDER BY P_CODE";
        Vector v = null;
        try {
            v =  BatchUtil.getInfoList(pcodeSQL, "batch");
        }
        catch (Exception ignore) {
            v = new Vector();
            log.warn("makeSumProductInfos (sql:" + pcodeSQL + ") error:" + ignore.getMessage(), ignore);
        }
        List pcodes = new ArrayList();
        for (int i = 0; i < v.size(); i++) {
            Vector row = (Vector) v.get(i);
            String pcode = (String) row.get(0);
            pcodes.add(pcode);
        }
        List sumProductInfos = new ArrayList();
        // ACQ_MEM_ID
        String memberSQL = "SELECT MEM_ID FROM TB_MEMBER WHERE SUBSTR(MEM_TYPE,2,1) = '1' ORDER BY MEM_ID";
        try {
            v = BatchUtil.getInfoList(memberSQL, "batch");
        }
        catch (Exception ignore) {
            v = new Vector();
            log.warn("makeSumProductInfos (sql:" + memberSQL + ") error:" + ignore.getMessage(), ignore);
        }
        for (int i = 0; i < v.size(); i++) {
            Vector row = (Vector) v.get(i);
            String memId = (String) row.get(0);
            SumProductInfo info = new SumProductInfo();
            info.setBatchDate(batchDate);
            info.setAcqmemId(memId);
            info.setPcodes(pcodes);
            sumProductInfos.add(info);
        }
        return sumProductInfos;
    }

    /**
     * <pre>
     * 依 info 資訊取得收單單位的彙總資料(TB_PRODUCT_SUM),
     * each elemnt is TbProductSumInfo object, 會 new 一個 Connection 來做
     * </pre>
     * @param info SumProductInfo
     * @return List, each element is TbProductSumInfo object
     */
    public static List getProductSumInfos(SumProductInfo info) {
        Connection conn = null;
        List productSumInfos = new ArrayList();
        try {
            conn = DBService.getDBService().getConnection("batch");
            productSumInfos = getProductSumInfos(conn, info);
        }
        catch (Exception ignore) {
            productSumInfos = null;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        return productSumInfos;
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
    private static String getProductSumInfosSQL(SumProductInfo info) {
    	
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" ISS_MEM_ID,ACQ_MEM_ID,MERCH_ID,P_CODE,PRODUCT_CODE,CURRENCY_CODE,COUNT(*) AS TXN_CNT, SUM(CREDIT_AUTH_AMT) AS CREDIT_AUTH_AMT, SUM(TXN_AMT) AS TXN_AMT, SUM(TXN_REDEEM_AMT) AS TXN_REDEEM_AMT, SUM(PRODUCT_QTY) AS PRODUCT_QTY");
        sql.append(" FROM TB_TRANS");
        sql.append(" WHERE ").append(Layer2Util.makeParMonDayCond(info.getBatchDate(),"",false));
        sql.append(" AND SETTLE_PROC_DATE = " + StringUtil.toSqlValueWithSQuote(info.getBatchDate()));
        sql.append(" AND ACQ_MEM_ID = " + StringUtil.toSqlValueWithSQuote(info.getAcqmemId()));
        String pcodes = getPcodeList(info.getPcodes());
        sql.append(" AND P_CODE IN (" + pcodes + ")");
        sql.append(" AND PRODUCT_SUM_DATE IS NULL");
        sql.append(" GROUP BY ISS_MEM_ID,ACQ_MEM_ID,MERCH_ID,P_CODE,PRODUCT_CODE,CURRENCY_CODE");
        sql.append(" ORDER BY ACQ_MEM_ID,MERCH_ID,ISS_MEM_ID,P_CODE,PRODUCT_CODE,CURRENCY_CODE");
        return sql.toString();
    }

    /**
     * <pre>
     * 依 info 資訊取得收單單位的彙總資料(TB_PRODUCT_SUM),
     * each elemnt is TbProductSumInfo object.
     * </pre>
     * @param conn conn
     * @param info SumProductInfo
     * @return List, each element is TbProductSumInfo object
     */
    public static List getProductSumInfos(Connection conn, SumProductInfo info) {
        if (info == null) {
            return null;
        }
        Statement stmt = null;
        ResultSet rs = null;
        List productSumInfos = new ArrayList();
        String sql = getProductSumInfosSQL(info);
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String month = info.getBatchDate().substring(4,6);
            String day = info.getBatchDate().substring(6,8);
            while (rs.next()) {
                TbProductSumInfo productSumInfo = new TbProductSumInfo();
                productSumInfo.setIssMemId(rs.getString("ISS_MEM_ID"));
                productSumInfo.setAcqMemId(rs.getString("ACQ_MEM_ID"));
                productSumInfo.setMerchId(rs.getString("MERCH_ID"));
                productSumInfo.setProcDate(info.getBatchDate());
                productSumInfo.setPCode(rs.getString("P_CODE"));
                productSumInfo.setTxnCnt(rs.getBigDecimal("TXN_CNT"));
                productSumInfo.setTxnAmt(rs.getBigDecimal("TXN_AMT"));
                productSumInfo.setCurrencyCode(rs.getString("CURRENCY_CODE"));
                productSumInfo.setCreditAuthAmt(rs.getBigDecimal("CREDIT_AUTH_AMT"));
                productSumInfo.setTxnRedeemAmt(rs.getBigDecimal("TXN_REDEEM_AMT"));
                productSumInfo.setProductCode(rs.getString("PRODUCT_CODE"));
                productSumInfo.setProductQty(rs.getBigDecimal("PRODUCT_QTY"));
                productSumInfo.setParMon(month);
                productSumInfo.setParDay(day);
                productSumInfos.add(productSumInfo);
            }
        }
        catch (SQLException se) {
            productSumInfos = null;
            log.error("getProductSumInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return productSumInfos;
    }

    /**
     * <pre>
     * 依 info 資訊取得收單單位的彙總資料(TB_PRODUCT_SUM_DTL),
     * each elemnt is TbProductSumDtlInfo object, 會 new 一個 Connection 來做
     * </pre>
     * @param info SumProductInfo
     * @return List, each element is TbProductSumDtlInfo object
     */
    public static List getProductSumDtlInfos(SumProductInfo info) {
        Connection conn = null;
        List productSumDtlInfos = new ArrayList();
        try {
            conn = DBService.getDBService().getConnection("batch");
            productSumDtlInfos = getProductSumDtlInfos(conn, info);
        }
        catch (Exception ignore) {
        	productSumDtlInfos = null;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        return productSumDtlInfos;
    }

    private static String getProductSumDtlInfosSQLGeneral(SumProductInfo info) {

        StringBuffer sb = new StringBuffer();
        sb.append(" SUM(BONUS_QTY) AS BONUS_QTY, SUM(TB_TRANS_DTL.TXN_REDEEM_AMT) AS TXN_REDEEM_AMT, COUNT(*) AS TTL_CNT,SUM(PRODUCT_QTY) AS PRODUCT_QTY");
        sb.append(" FROM (SELECT * FROM TB_TRANS");
        sb.append(" WHERE ").append(Layer2Util.makeParMonDayCond(info.getBatchDate(),"",false));
        sb.append(" AND SETTLE_PROC_DATE = " + StringUtil.toSqlValueWithSQuote(info.getBatchDate()));
        sb.append(" AND ACQ_MEM_ID = " + StringUtil.toSqlValueWithSQuote(info.getAcqmemId()));
        String pcodes = getPcodeList(info.getPcodes());
        sb.append(" AND P_CODE IN (" + pcodes + ")");
        sb.append(" AND PRODUCT_SUM_DATE IS NULL");
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
    private static String getProductSumDtlInfosSQL(SumProductInfo info) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" TB_TRANS.ISS_MEM_ID,TB_TRANS.ACQ_MEM_ID,TB_TRANS.MERCH_ID,TB_TRANS.P_CODE, TXN_CODE, BONUS_ID,");
        sql.append(" BONUS_SDATE,BONUS_EDATE,PRODUCT_CODE,");
        String general = getProductSumDtlInfosSQLGeneral(info);
        sql.append(general);
        sql.append(" GROUP BY TB_TRANS.ISS_MEM_ID,TB_TRANS.ACQ_MEM_ID,TB_TRANS.MERCH_ID,TB_TRANS.P_CODE, TXN_CODE, BONUS_ID, BONUS_SDATE,BONUS_EDATE,PRODUCT_CODE");
        sql.append(" ORDER BY TB_TRANS.ACQ_MEM_ID,TB_TRANS.MERCH_ID,TB_TRANS.ISS_MEM_ID,TB_TRANS.P_CODE, TXN_CODE, BONUS_ID, BONUS_SDATE,BONUS_EDATE,PRODUCT_CODE");
        return sql.toString();
    }

    /**
     * <pre>
     * 依 info 資訊取得收單單位的彙總資料(TB_PRODUCT_SUM_DTL),
     * each elemnt is TbProductSumDtlInfo object
     * </pre>
     * @param conn conn
     * @param info SumProductInfo
     * @return List, each element is TbProductSumDtlInfo object
     */
    public static List getProductSumDtlInfos(Connection conn, SumProductInfo info) {
        if (info == null) {
            return null;
        }
        Statement stmt = null;
        ResultSet rs = null;
        List productSumDtlInfos = new ArrayList();
        String sql = getProductSumDtlInfosSQL(info);

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            String month = info.getBatchDate().substring(4,6);
            String day = info.getBatchDate().substring(6,8);
            while (rs.next()) {
                TbProductSumDtlInfo productSumDtlInfo = new TbProductSumDtlInfo();
                productSumDtlInfo.setIssMemId(rs.getString("ISS_MEM_ID"));
                productSumDtlInfo.setAcqMemId(info.getAcqmemId());
                productSumDtlInfo.setMerchId(rs.getString("MERCH_ID"));
                productSumDtlInfo.setProcDate(info.getBatchDate());
                productSumDtlInfo.setPCode(rs.getString("P_CODE"));
                productSumDtlInfo.setTxnCode(rs.getString("TXN_CODE"));
                productSumDtlInfo.setBonusId(rs.getString("BONUS_ID"));
                productSumDtlInfo.setBonusSdate(rs.getString("BONUS_SDATE"));
                productSumDtlInfo.setBonusEdate(rs.getString("BONUS_EDATE"));
                productSumDtlInfo.setProductCode(rs.getString("PRODUCT_CODE"));
                productSumDtlInfo.setBonusQty(rs.getBigDecimal("BONUS_QTY"));
                productSumDtlInfo.setTxnRedeemAmt(rs.getBigDecimal("TXN_REDEEM_AMT"));
                productSumDtlInfo.setTtlCnt(rs.getBigDecimal("TTL_CNT"));
                productSumDtlInfo.setProductQty(rs.getBigDecimal("PRODUCT_QTY"));
                productSumDtlInfo.setParMon(month);
                productSumDtlInfo.setParDay(day);
                
                productSumDtlInfos.add(productSumDtlInfo);
            }
        }
        catch (SQLException se) {
        	productSumDtlInfos = null;
            log.error("getProductSumDtlInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return productSumDtlInfos;
    }

    /**
     * <pre>
     * 1. 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總
     * productSumInfos:List, each element is TbProductSumInfo object
     * 2. 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總
     * productSumDtlInfos:List, each element is TbProductSumDtlInfo object
     * 會 new 一個 Connection 去做
     * </pre>
     * @param info SumProductInfo info
     * @return SumProductResultInfo object
     */
    public static SumProductResultInfo getSumProductResultInfo(SumProductInfo info) {
        Connection conn = null;
        SumProductResultInfo sumProductResultInfo = null;
        try {
            conn = DBService.getDBService().getConnection("batch");
            sumProductResultInfo = getSumProductResultInfo(conn, info);
        }
        catch (Exception ignore) {
        	sumProductResultInfo = null;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        return sumProductResultInfo;
    }

    /**
     * <pre>
     * 1. 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總
     * productSumInfos:List, each element is TbProductSumInfo object
     * 2. 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總
     * productSumDtlInfos:List, each element is TbProductSumDtlInfo object
     * </pre>
     * @param conn conn
     * @param info SumProductInfo info
     * @return SumProductResultInfo object
     */
    public static SumProductResultInfo getSumProductResultInfo(Connection conn, SumProductInfo info) {
        if (info == null) {
            return null;
        }
        List productSumInfos = getProductSumInfos(conn, info);
        if (productSumInfos == null) {
            log.info("productSumInfos is null!");
            return null;
        }
        List productSumDtlInfos = getProductSumDtlInfos(conn, info);
        if (productSumDtlInfos == null) {
            log.info("productSumDtlInfos is null!");
            return null;
        }
        SumProductResultInfo resultInfo = new SumProductResultInfo();
        resultInfo.setSumProductInfo(info);
        resultInfo.setProductSumInfos(productSumInfos);
        resultInfo.setProductSumDtlInfos(productSumDtlInfos);
        return resultInfo;
    }

    /**
     * <pre>
     * 依 resultInfo 來處理一個 member 的彙總資料
     * 1. 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總, 新增至TB_PRODUCT_SUM
     * productSumInfos:List, each element is TbProductSumInfo object
     * 2. 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總, 新增至TB_PRODUCT_SUM_DTL
     * productSumDtlInfos:List, each element is TbProductSumDtlInfo object
     * 3. 更新此 member 彙總成功的 TB_TRANS
     * 會 new 一個 Connection 來做
     * </pre>
     * @param resultInfo resultInfo
     * @return 成功傳回 true, 失敗傳回 false
     */
    public static boolean handleSumProductResultInfo(SumProductResultInfo resultInfo) {
        Connection conn = null;
        boolean ret = false;
        try {
            conn = DBService.getDBService().getConnection("batch");
            ret = handleSumProductResultInfo(conn, resultInfo);
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
     * 1. 依據收單單位與要彙總的P_CODE對TB_TRANS做彙總, 新增至TB_PRODUCT_SUM
     * productSumInfos:List, each element is TbProductSumInfo object
     * 2. 依據收單單位與要彙總的P_CODE對TB_TRANS_DTL做彙總, 新增至TB_PRODUCT_SUM_DTL
     * productSumDtlInfos:List, each element is TbProductSumDtlInfo object
     * 3. 更新此 member 彙總成功的 TB_TRANS
     * </pre>
     * @param conn conn
     * @param resultInfo resultInfo
     * @return 成功傳回 true, 失敗傳回 false
     */
    public static boolean handleSumProductResultInfo(Connection conn, SumProductResultInfo resultInfo) {
        if (resultInfo == null) {
            return false;
        }

        boolean autoCommit = false;
        boolean ret = false;
        
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            int num = 0;
            
            TbProductSumMgr msmgr = new TbProductSumMgr(conn);
            TbProductSumDtlMgr msdmgr = new TbProductSumDtlMgr(conn);
            
            for (int i = 0; i < resultInfo.getProductSumInfos().size(); i++) {
            	
                TbProductSumInfo info = (TbProductSumInfo) resultInfo.getProductSumInfos().get(i);
                
                //update TB_TRANS
                updateTransByProduct(conn, resultInfo.getSumProductInfo(), Constants.RCODE_0000_OK);
                
                //insert TB_PRODUCT_SUM_DTL
                for (int j = num; j < resultInfo.getProductSumDtlInfos().size(); j++) {
                	TbProductSumDtlInfo info_dtl = (TbProductSumDtlInfo) resultInfo.getProductSumDtlInfos().get(j);
                	msdmgr.insert(info_dtl);
                    num++;
                }
                
            	//insert TB_PRODUCT_SUM
                msmgr.insert(info);
                
            } //for
            
            ret = true;
            
        }
        catch (Exception e) {
            ret = false;
            log.error("handleSumProductResultInfo error:" + e.getMessage(), e);
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

    /**
     * <pre>
     * 更新此 Product 彙總成功的 TB_TRANS
     * </pre>
     * @param conn conn
     * @param info info
     * @param rcode rcode
     * @return 更新的筆數
     */
    public static int updateTransByProduct(Connection conn, SumProductInfo info, String rcode) {
        if (info == null) {
            return 0;
        }
   	
        Statement stmt = null;
        ResultSet rs = null;
        int updateCount = 0;
        StringBuffer sql = new StringBuffer();
                
        sql.append("UPDATE TB_TRANS");
        sql.append(" SET PRODUCT_SUM_DATE = " + StringUtil.toSqlValueWithSQuote(info.getBatchDate()));
        sql.append(" ,PRODUCT_SUM_RCODE = " + StringUtil.toSqlValueWithSQuote(rcode));
        sql.append(" WHERE ").append(Layer2Util.makeParMonDayCond(info.getBatchDate(),"",false));
        sql.append(" AND SETTLE_PROC_DATE = " + StringUtil.toSqlValueWithSQuote(info.getBatchDate()));
        sql.append(" AND ACQ_MEM_ID = " + StringUtil.toSqlValueWithSQuote(info.getAcqmemId()));
        sql.append(" AND PRODUCT_SUM_DATE IS NULL");

        try {
            stmt = conn.createStatement();
            updateCount = stmt.executeUpdate(sql.toString());
            log.info("updateTransByProduct update:" + updateCount + " recs.");
        }
        catch (Exception ignore) {
            updateCount = -1;
            log.error("updateTransByProduct error:" + sql.toString(), ignore);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return updateCount;
    }
}
