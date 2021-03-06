/*
 * Version: 1.0.0
 * Date: 2015-02-10
 */

package tw.com.hyweb.svc.yhdp.batch.summary; 

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbCardProductInfo;
import tw.com.hyweb.service.db.mgr.TbCardProductMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * SumAppointReload
 * </pre>
 * author: Kevin
 */
public class SumAppointReload extends AbstractBatchBasic 
{
    private static Logger log = Logger.getLogger(SumAppointReload.class);
    private String batchDate = "";
    private String RecoverLevel = "";  //ALL 復原全部 or ERR 復原錯誤部分

    private Connection conn = null;
    
    public SumAppointReload() {
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

    public void process(String[] args) throws Exception 
    {  	   	
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

            Vector<TbCardProductInfo> cardProductInfos = new Vector<TbCardProductInfo>();
            new TbCardProductMgr(conn).queryAll(cardProductInfos);

            log.info("cardProductInfos:"+cardProductInfos);
            for (int idx = 0; idx < cardProductInfos.size(); idx++) 
            {
            	TbCardProductInfo cardProductInfo = cardProductInfos.get(idx);
            	ArrayList<SumAppointReloadResultInfo> resultInfos = SumAppointReloadUtil.getSumAppointReloadResultInfo(conn, cardProductInfo, batchDate);
                if (resultInfos == null) {
                    // should not happen
                    log.info("resultInfo is null:" + cardProductInfo);
                    conn.commit();
                }
                
                log.info("resultInfos: " + resultInfos);
                
                for(SumAppointReloadResultInfo resultInfo : resultInfos) {
                	boolean ok = SumAppointReloadUtil.handleSumAppointReloadResultInfo(conn, resultInfo);
                    if (!ok) {
                        // handle one AppointReload error
                        log.info("handleSumAppointReloadResultInfo fail!");
                        log.info("sumAppointReloadInfo:" + cardProductInfo);
                        log.info("resultInfo:" + resultInfo);
                        conn.commit();
                    }
                }   
            }
        }
        catch (Exception ignore) {
            log.error("SumAppointReload error:" + ignore.getMessage(), ignore);
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
            SumAppointReload sar = new SumAppointReload();
            sar.setBatchDate(batchDate);
            sar.setRecoverLevel(System.getProperty("recover"));
            sar.run(null);
        }
        catch (Exception e) {
            log.error("SumAppointReload error:" + e.getMessage(), e);
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
    
        sql.append("delete TB_APPOINT_RELOAD_SUM");
        sql.append(" where PROC_DATE='").append(batchDate).append("'");
        try
         {
        	 //Delete TB_APPOINT_RELOAD_SUM
             log.info(" recoverData():"+sql.toString());
             DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
             
             //Delete TB_APPOINT_RELOAD_DTL_SUM
             log.info(" recoverData():"+sql.toString());
             DBService.getDBService().sqlAction(sql.toString().replaceAll("TB_APPOINT_RELOAD_SUM", "TB_APPOINT_RELOAD_SUM_DTL"), connSelf, false);
             
             connSelf.commit();
         }
        catch (SQLException e)
         {
             connSelf.rollback();
             throw new Exception("recoverData():delete TB_APPOINT_RELOAD_SUM and TB_APPOINT_RELOAD_DTL_SUM. "+e);
         }   
        
        
    }
    
}
