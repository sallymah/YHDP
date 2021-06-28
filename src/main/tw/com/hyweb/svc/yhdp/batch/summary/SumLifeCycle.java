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

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * SumMerch
 * </pre>
 * author: Ivan
 */
public class SumLifeCycle extends AbstractBatchBasic {
    private static Logger log = Logger.getLogger(SumLifeCycle.class);
    private String batchDate = "";
    private String RecoverLevel = "";  //ALL 復原全部 or ERR 復原錯誤部分

    private Connection conn = null;
    
    public SumLifeCycle() {
    }

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }

	public String getRecoverLevel() {
		return RecoverLevel;
	}

	public void setRecoverLevel(String recoverLevel) {
		RecoverLevel = recoverLevel;
	}    

    public void process(String[] args) throws Exception {  	

        try {
            if ( getRecoverLevel()!=null && 
                    (RecoverLevel.equals(Constants.RECOVER_LEVEL_ALL)||
                     RecoverLevel.equals(Constants.RECOVER_LEVEL_ERR))
                   ) 
               {
                   recoverData();
                   return;
               }
            
            conn =  DBService.getDBService().getConnection("batch");
            boolean ret = SumLifeCycleUtil.deleteLifeCycleMonSum(conn, batchDate);
            if(ret) {
	            List sumLifeCycleInfos = SumLifeCycleUtil.makeSumLifeCycleInfos(conn, batchDate);
	            log.info("sumLifeCycleInfos:"+sumLifeCycleInfos);
	            for (int i = 0; i < sumLifeCycleInfos.size(); i++) {
	            	SumLifeCycleInfo sumLifeCycleInfo = (SumLifeCycleInfo) sumLifeCycleInfos.get(i);
	                SumLifeCycleResultInfo resultInfo = SumLifeCycleUtil.getSumLifeCycleResultInfo(conn, sumLifeCycleInfo, batchDate);
	                if (resultInfo == null) {
	                    // should not happen
	                    log.info("resultInfo is null:" + sumLifeCycleInfo);
	                    conn.commit();
	                }
	                boolean ok = SumLifeCycleUtil.handleSumLifeCycleResultInfo(conn, resultInfo);
	                if (!ok) {
	                    // handle one Life Cycle error
	                    log.info("handleSumLifeCycleResultInfo fail!");
	                    log.info("sumLifeCycleInfo:" + sumLifeCycleInfo);
	                    log.info("resultInfo:" + resultInfo);
	                    conn.commit();
	                }
	            }
            }
        }
        catch (Exception ignore) {
            log.error("SumLifeCycle error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
    }

    
	public static void main(String[] args) {
        try {
           String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            SumLifeCycle sm = new SumLifeCycle();
            sm.setBatchDate(batchDate);
            sm.setRecoverLevel(System.getProperty("recover"));
            sm.run(null);
        }
        catch (Exception e) {
            log.error("SumMerch error:" + e.getMessage(), e);
        }
    }
    
    /**
     * 無論成功或失敗，均可把資料還原：
     * (1) 把當日全部資料清乾淨，程式全部資料重跑
     * (2) 把當日錯誤資料清乾淨，程式只重跑錯誤的部分
     * 
     * 全程使用同一個connection並一起commit
     * 重要：清資料的SQL務必寫入Log File，萬一有錯，方便追查
     */
    protected void recoverData() throws Exception
    {
        Connection connSelf =  DBService.getDBService().getConnection("batch");
        
        StringBuffer sql = new StringBuffer();
        
        //Delete TB_LIFE_CYCLE_MON_SUM
        sql.append("delete TB_LIFE_CYCLE_MON_SUM");
        sql.append(" where PROC_MON='").append(DateUtil.addMonth(batchDate, -1)).append("'");
        sql.append(" and PROC_DATE='").append(batchDate).append("'");
        try
         {
             log.info(" recoverData():"+sql.toString());
             DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
             connSelf.commit();
         }
        catch (SQLException e)
         {
             connSelf.rollback();
             throw new Exception("recoverData():delete TB_LIFE_CYCLE_MON_SUM. "+e);
         }   
    }
    
}
