/*
 * Version: 1.0.0
 * Date: 2007-10-15
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
public class SumMerch extends AbstractBatchBasic {
    private static Logger log = Logger.getLogger(SumMerch.class);
    private String batchDate = "";
    private String RecoverLevel = "";  //ALL 復原全部 or ERR 復原錯誤部分

    private Connection conn = null;
    
    public SumMerch() {
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
            updateErrorData(conn, null, Constants.RCODE_2301_NOSUM_ERR); //更新不需要彙總的資料
            conn.commit();
            
            List sumMerchInfos = SumMerchUtil.makeSumMerchInfos(batchDate);
            log.info("sumMerchInfos:"+sumMerchInfos);
            for (int i = 0; i < sumMerchInfos.size(); i++) {
                SumMerchInfo sumInfo = (SumMerchInfo) sumMerchInfos.get(i);
                SumMerchResultInfo resultInfo = SumMerchUtil.getSumMerchResultInfo(conn, sumInfo);
                if (resultInfo == null) {
                    // should not happen
                    log.info("resultInfo is null:" + sumInfo);
                    int updateCount = updateErrorData(conn, sumInfo.getAcqmemId(), Constants.RCODE_2302_SUMMERCH_ERR);
                    log.info("updateCount:" + updateCount);
                    conn.commit();
                }
                boolean ok = SumMerchUtil.handleSumMerchResultInfo(conn, resultInfo);
                if (!ok) {
                    // handle one merch error
                    log.info("handleSumMerchResultInfo fail!");
                    log.info("sumInfo:" + sumInfo);
                    log.info("resultInfo:" + resultInfo);
                    int updateCount = updateErrorData(conn, sumInfo.getAcqmemId(), Constants.RCODE_2302_SUMMERCH_ERR);
                    log.info("updateCount:" + updateCount);
                    conn.commit();
                }
            }
        }
        catch (Exception ignore) {
            log.error("SumMerch error:" + ignore.getMessage(), ignore);
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
            SumMerch sm = new SumMerch();
            sm.setBatchDate(batchDate);
            sm.setRecoverLevel(System.getProperty("recover"));
            sm.run(null);
        }
        catch (Exception e) {
            log.error("SumMerch error:" + e.getMessage(), e);
        }
    }
    
    /**
     * 更新不需要彙總的資料
     * 全程使用同一個connection並一起commit
     * 重要：清資料的SQL務必寫入Log File，萬一有錯，方便追查
     * @return 
     */
    protected int updateErrorData(Connection conn, String acqMemId, String rCode) throws Exception
    {
    	Statement stmt = null;
        int updateCount = 0;
        StringBuffer sql = new StringBuffer();
        
        //Update TB_TRANS
        sql.append("update TB_TRANS set ");
        sql.append(" MERCH_SUM_DATE ='").append(batchDate).append("'");
        sql.append(" ,MERCH_SUM_RCODE='").append(rCode).append("'");
        sql.append(" WHERE ").append(Layer2Util.makeParMonDayCond(batchDate,"",false));
        sql.append(" and SETTLE_PROC_DATE ='").append(batchDate).append("'");
        if ( acqMemId != null ){
        	sql.append(" and ACQ_MEM_ID ='").append(acqMemId).append("'");
        }
        else{
        	sql.append(" and SETTLE_RCODE !='").append(Constants.RCODE_0000_OK).append("'");
        }
        sql.append(" and MERCH_SUM_DATE is null");

        try {
            stmt = conn.createStatement();
            updateCount = stmt.executeUpdate(sql.toString());
            log.info("updateErrorTransByMerch update:" + updateCount + " recs.");
        }
        catch (Exception ignore) {
            updateCount = -1;
            log.error("updateErrorTransByMerch error:" + sql.toString(), ignore);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, null);
        }
        return updateCount;
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
        
        //Delete TB_MERCH_SUM
        sql.append("delete TB_MERCH_SUM");
        sql.append(" where PAR_MON='").append(batchDate.substring(4,6)).append("'");
        sql.append(" and PAR_DAY='").append(batchDate.substring(6,8)).append("'");
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
             throw new Exception("recoverData():delete TB_MERCH_SUM. "+e);
         }
        
        sql.delete(0, sql.length()); //清空sqlCmd
        
        sql.append("delete TB_MERCH_SUM_DTL");
        sql.append(" where PAR_MON='").append(batchDate.substring(4,6)).append("'");
        sql.append("  and PAR_DAY='").append(batchDate.substring(6,8)).append("'");
        sql.append("  and PROC_DATE='").append(batchDate).append("'");
        try
         {
             log.info(" recoverData():"+sql.toString());
             DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
             connSelf.commit();
         }
        catch (SQLException e)
         {
             connSelf.rollback();
             throw new Exception("recoverData():delete TB_MERCH_SUM_DTL. "+e);
         }

        sql.delete(0, sql.length()); //清空sqlCmd        
        
        //Update TB_TRANS
        sql.append("update TB_TRANS set ");
        sql.append(" MERCH_SUM_DATE = null");
        sql.append(" ,MERCH_SUM_RCODE='").append(Constants.RCODE_0000_OK).append("'");
        sql.append(" WHERE ").append(Layer2Util.makeParMonDayCond(batchDate,"",false));
        sql.append(" and SETTLE_PROC_DATE='").append(batchDate).append("'");

        try
        {
            log.info(" recoverData():"+sql.toString());
            DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
            connSelf.commit();
        }
        catch (SQLException e)
        {
            connSelf.rollback();
            throw new Exception("recoverData():Update TB_TRANS. "+e);
        }
        
    }
    
}
