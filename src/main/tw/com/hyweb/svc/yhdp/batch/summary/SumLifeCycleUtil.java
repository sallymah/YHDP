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

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbLifeCycleMonSumInfo;
import tw.com.hyweb.service.db.mgr.TbLifeCycleMonSumMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;

/**
 * <pre>
 * SumMerchUtil
 * </pre>
 * author: Ivan
 */
public class SumLifeCycleUtil {
    private static Logger log = Logger.getLogger(SumLifeCycleUtil.class);
    
    private static int buffsize = 512;
    private static String sTable = "TB_CARD";
    private static String sysDate = DateUtils.getSystemDate();
    private static String sysTime = DateUtils.getSystemTime();

    public static List makeSumLifeCycleInfos(Connection conn, String batchDate) {
        Statement stmt = null;
        ResultSet rs = null;
        List lifeCycleSumInfos = new ArrayList();
        String lastMonth = DateUtil.addMonth(batchDate, -1).substring(0, 6);
        String sql = getLifeCycleSumInfosSQL(lastMonth);
        log.info("sql: " + sql);
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                SumLifeCycleInfo lifeCycleSumInfo = new SumLifeCycleInfo();
                lifeCycleSumInfo.setBatchDate(batchDate);
                lifeCycleSumInfo.setLastMonth(lastMonth);
                lifeCycleSumInfo.setCoBrandEntId(rs.getString("CO_BRAND_ENT_ID"));
                lifeCycleSumInfo.setCardProduct(rs.getString("CARD_PRODUCT"));
                lifeCycleSumInfo.setNumOfNew3(rs.getInt("NUM_OF_NEW_3"));
                lifeCycleSumInfo.setNumOfNew5(rs.getInt("NUM_OF_NEW_5"));
                lifeCycleSumInfo.setNumOfNew6(rs.getInt("NUM_OF_NEW_6"));
                lifeCycleSumInfo.setNumOfNew7(rs.getInt("NUM_OF_NEW_7"));
                lifeCycleSumInfo.setNumOfNew8(rs.getInt("NUM_OF_NEW_8"));

                lifeCycleSumInfos.add(lifeCycleSumInfo);
            }
        }
        catch (SQLException se) {
        	lifeCycleSumInfos = null;
            log.error("getLifeCycleSumInfos error:" + sql, se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return lifeCycleSumInfos;
	}

    public static String getLifeCycleSumInfosSQL(String lastMonth) {
		StringBuffer sql = new StringBuffer();
        sql.append("SELECT");
        sql.append(" co_brand_ent_id, card_product, sum(num_of_3) AS NUM_OF_NEW_3,");
        sql.append(" sum(num_of_5) AS NUM_OF_NEW_5, sum(num_of_6) AS NUM_OF_NEW_6,");
        sql.append(" sum(num_of_7) AS NUM_OF_NEW_7, sum(num_of_8) AS NUM_OF_NEW_8");
        sql.append(" FROM (");
        sql.append(" SELECT tb_card_product.co_brand_ent_id , TB_CARD_PRODUCT.card_product, ");
        sql.append(" NVL(num_of_3,0) num_of_3, NVL(num_of_5,0) num_of_5, NVL(num_of_6,0) num_of_6, NVL(num_of_7,0) num_of_7, NVL(num_of_8 ,0) num_of_8");
        sql.append(" FROM (");
        sql.append(" SELECT co_brand_ent_id, card_product,");
        sql.append(" case life_cycle when '3' then 1 else 0 end as num_of_3, case life_cycle when '5' then 1 else 0 end as num_of_5, case life_cycle when '6' then 1 else 0 end as num_of_6, case life_cycle when '7' then 1 else 0 end as num_of_7, case life_cycle when '8' then 1 else 0 end as num_of_8 ");
        sql.append(" FROM TB_CARD");
        sql.append(" WHERE ").append("status_update_date like '").append(lastMonth).append("%'");
        sql.append(" AND life_cycle in ('3','5','6','7','8')) TB_CARD,");
        sql.append(" (SELECT CO_BRAND_ENT_ID, CARD_PRODUCT FROM TB_CARD GROUP BY CO_BRAND_ENT_ID, CARD_PRODUCT)  tb_card_product");
        sql.append(" WHERE TB_CARD_PRODUCT.CARD_PRODUCT=TB_CARD.CARD_PRODUCT(+)");
        sql.append(" AND TB_CARD_PRODUCT.CO_BRAND_ENT_ID=TB_CARD.CO_BRAND_ENT_ID(+))");
        sql.append(" GROUP BY co_brand_ent_id, card_product");
        return sql.toString();
	}

	public static SumLifeCycleResultInfo getSumLifeCycleResultInfo(Connection conn,	SumLifeCycleInfo info, String batchDate) throws SQLException {
		if (info == null) {
            return null;
        }
		
	    String lastMonthDate = batchDate.substring(0,6) + "01";
	    String threeMonBefore = DateUtil.addMonth(batchDate, -3);
        String sixMonBefore = DateUtil.addMonth(batchDate, -6);
        String twelveMonBefore = DateUtil.addMonth(batchDate, -12);
        
        String coBrandEntId = info.getCoBrandEntId();
        String cardProduct = info.getCardProduct();
		
		//String issueWhere = "life_cycle in ('3','5','6','7','8','9','A','B') and co_brand_ent_id = '" + info.getCoBrandEntId() + "' and card_product = '" + info.getCardProduct() + "'";
		//暫時拿掉LIFE_CYCLE='3'
		String issueWhere = "life_cycle in ('3','5','6','7','8','9','A','B') and co_brand_ent_id = '" + coBrandEntId + "' and card_product = '" + cardProduct + "' and active_date < '" + lastMonthDate + "'";
        int issueNum = getLifeCycleCount(conn, info, null, issueWhere);
        
        String currencyWhere = "life_cycle in ('3','5') and co_brand_ent_id = '" + coBrandEntId + "' and card_product = '" + cardProduct + "'";
        int currencyNum = getLifeCycleCount(conn, info, null, currencyWhere);
        
        //String transWhere = "life_cycle='5' and exists (select 1 from tb_onl_txn where card_no=tb_card.card_no and txn_date>='" + DateUtil.addMonth(batchDate, -3) + "') and co_brand_ent_id = '" + info.getCoBrandEntId() + "' and card_product = '" + info.getCardProduct() + "'";
        String transWhere = "life_cycle='5' and exists (select 1 from tb_onl_txn where card_no=tb_card.card_no and txn_date>='" + threeMonBefore + "') and co_brand_ent_id = '" + coBrandEntId + "' and card_product = '" + cardProduct + "'";
        int transNum = getLifeCycleCount(conn, info, null, transWhere);
        
        //String sleepWhere = " life_cycle='5' and not exists (select 1 from tb_onl_txn where card_no=tb_Card.card_no and txn_date>='" + DateUtil.addMonth(batchDate, -3) + "') and exists (select 1 from tb_onl_txn where card_no=tb_card.card_no and txn_date>='" + DateUtil.addMonth(batchDate, -6) + "' and txn_date<'" + DateUtil.addMonth(batchDate, -3) + "') and co_brand_ent_id = '" + info.getCoBrandEntId() + "' and card_product = '" + info.getCardProduct() + "'";
        String sleepFrom = " c, (select distinct card_no,expiry_date from tb_onl_txn where txn_date between '" + sixMonBefore + "' and '" + threeMonBefore + "' minus select distinct card_no,expiry_date from tb_onl_txn where txn_date >='" + threeMonBefore + "') o";
        String sleepWhere = "life_cycle='5' and co_brand_ent_id = '" + coBrandEntId + "' and card_product = '" + cardProduct + "' and o.card_no=c.card_no and o.expiry_date=c.expiry_date" ;
        int sleepNum = getLifeCycleCount(conn, info, sleepFrom, sleepWhere);
        
        //String staticWhere = "life_cycle='5' and not exists (select 1 from tb_onl_txn where card_no=tb_Card.card_no and txn_date>='" + DateUtil.addMonth(batchDate, -6) + "') and exists (select 1 from tb_onl_txn where card_no=tb_card.card_no and txn_date>='" + DateUtil.addMonth(batchDate, -12) + "' and txn_date<'" + DateUtil.addMonth(batchDate, -6) + "') and co_brand_ent_id = '" + info.getCoBrandEntId() + "' and card_product = '" + info.getCardProduct() + "'";
        String staticFrom = " c, (select distinct card_no,expiry_date from tb_onl_txn where txn_date between '" + twelveMonBefore + "' and '" + sixMonBefore + "' minus select distinct card_no,expiry_date from tb_onl_txn where txn_date >='" + sixMonBefore + "') o";
        String staticWhere = "life_cycle='5' and co_brand_ent_id = '" + coBrandEntId + "' and card_product = '" + cardProduct + "' and o.card_no=c.card_no and o.expiry_date=c.expiry_date" ;
        int staticNum = getLifeCycleCount(conn, info, staticFrom, staticWhere);
        
        String lostWhere = "life_cycle='5' and not exists (select 1 from tb_onl_txn where card_no=tb_Card.card_no and txn_date>='" + twelveMonBefore + "') and co_brand_ent_id = '" + coBrandEntId + "' and card_product = '" + cardProduct + "'";
        int lostNum = getLifeCycleCount(conn, info, null, lostWhere);
        
        String effectWhere = "life_cycle in ('3') and co_brand_ent_id = '" + coBrandEntId + "' and card_product = '" + cardProduct + "'";
        int effectNum = getLifeCycleCount(conn, info, null,effectWhere);
        
        TbLifeCycleMonSumInfo lifeCycleInfo = new TbLifeCycleMonSumInfo();
        lifeCycleInfo.setProcMon(info.getLastMonth());
        lifeCycleInfo.setCoBrandEntId(info.getCoBrandEntId());
        lifeCycleInfo.setCardProduct(info.getCardProduct());
        lifeCycleInfo.setNumOfIssue(issueNum);
        lifeCycleInfo.setNumOfCurrency(currencyNum);
        lifeCycleInfo.setNumOfTrans(transNum);
        lifeCycleInfo.setNumOfSleep(sleepNum);
        lifeCycleInfo.setNumOfStatic(staticNum);
        lifeCycleInfo.setNumOfLost(lostNum);
        lifeCycleInfo.setNumOfEffect(effectNum);
        lifeCycleInfo.setNumOfNew3(info.getNumOfNew3());
        lifeCycleInfo.setNumOfNew5(info.getNumOfNew5());
        lifeCycleInfo.setNumOfNew6(info.getNumOfNew6());
        lifeCycleInfo.setNumOfNew7(info.getNumOfNew7());
        lifeCycleInfo.setNumOfNew8(info.getNumOfNew8());
        lifeCycleInfo.setNewIssue(info.getNumOfNew3() + info.getNumOfNew5());
        lifeCycleInfo.setProcDate(sysDate);
        lifeCycleInfo.setProcTime(sysTime);
        
        
        SumLifeCycleResultInfo resultInfo = new SumLifeCycleResultInfo();
        resultInfo.setSumLifeCycleInfo(info);
        resultInfo.setLifeCycleMonSumInfo(lifeCycleInfo);

        return resultInfo;
	}

	private static int getLifeCycleCount(Connection conn, SumLifeCycleInfo info, String from, String where)  throws SQLException 
	{
		int ret = 0;
	    Statement stmt = null;
	    ResultSet rs = null;
	    StringBuffer sql = new StringBuffer(buffsize);
	    sql.append("SELECT COUNT(*) FROM " + sTable);
	    if(from != null) {
	    	 sql.append(from);
	    }
	    sql.append(" WHERE ");
	    sql.append(where);
	    try {
	      log.debug("sql: " + sql.toString());
	      stmt = conn.createStatement();
	      rs = stmt.executeQuery(sql.toString());
	      while (rs.next()) {
	        ret = rs.getInt(1);
	      }
	      log.debug(sTable + ": " + ret + " records exist");
	    }
	    catch (SQLException se) {
	      log.warn(sTable + ": " + se.getMessage(), se);
	      throw se;
	    }
	    finally {
	      ReleaseResource.releaseDB(null, stmt, rs);
	    }
	    return ret;
	}

	public static boolean handleSumLifeCycleResultInfo(Connection conn,	SumLifeCycleResultInfo resultInfo) {
	    if (resultInfo == null) {
            return false;
        }

        boolean autoCommit = false;
        boolean ret = false;
        
        TbLifeCycleMonSumMgr mgr = new TbLifeCycleMonSumMgr(conn);
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
     
            //insert TB_LIFE_CYCLE_MON_SUM
            mgr.insert(resultInfo.getLifeCycleMonSumInfo());
            
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
	}

	public static boolean deleteLifeCycleMonSum(Connection conn, String batchDate) throws Exception {
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
	}

}
