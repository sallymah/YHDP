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

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * SumMerch
 * </pre>
 * author: Ivan
 */
public class SumPaymentRequest extends AbstractBatchBasic {
    private static Logger log = Logger.getLogger(SumPaymentRequest.class);
    private String batchDate = "";
    private String RecoverLevel = "";  //ALL 復原全部 or ERR 復原錯誤部分

    private Connection conn = null;
    
    public SumPaymentRequest() {
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
            
            if(!batchDate.substring(6, 8).equals("01")) {
            	log.warn("process day is not '01'");
            	return;
            }
            
            conn =  DBService.getDBService().getConnection("batch");
            Vector<TbMemberInfo> memInfos = new Vector<TbMemberInfo>();
            TbMemberMgr mgr = new TbMemberMgr(conn);
            String where = "substr(mem_type, 2, 1) = '1'";
            int acqMemIdCount = mgr.queryMultiple(where, memInfos);
            
            log.info("memInfos:"+memInfos);
            for (int i = 0; i < memInfos.size(); i++) {
            	TbMemberInfo memInfo = (TbMemberInfo) memInfos.get(i);
                SumPaymentRequestResultInfo resultInfo = SumPaymentRequestUtil.getSumPaymentRequestResultInfo(conn, memInfo, batchDate);
                if (resultInfo == null) {
                    // should not happen
                    log.info("resultInfo is null:" + memInfo);
                    conn.commit();
                }
                boolean ok = SumPaymentRequestUtil.handleSumPaymentRequestResultInfo(conn, resultInfo);
                if (!ok) {
                    // handle one Payment Request error
                    log.info("handleSumPaymentRequestResultInfo fail!");
                    log.info("memInfos:" + memInfos);
                    log.info("resultInfo:" + resultInfo);
                    conn.commit();
                }
            }
        }
        catch (Exception ignore) {
            log.error("SumPaymentRequest error:" + ignore.getMessage(), ignore);
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
            SumPaymentRequest sm = new SumPaymentRequest();
            sm.setBatchDate(batchDate);
            sm.setRecoverLevel(System.getProperty("recover"));
            sm.run(null);
        }
        catch (Exception e) {
            log.error("SumPaymentRequest error:" + e.getMessage(), e);
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
        
        //Delete TB_MERCH_SUM
        sql.append("delete TB_PAYMENT_REQUEST");
        sql.append(" where PROC_DATE='").append(batchDate).append("'");
        try
         {
             log.info(" recoverData():"+sql.toString());
             DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
             connSelf.commit();
         }
        catch (SQLException e)
         {
             connSelf.rollback();
             throw new Exception("recoverData():delete TB_PAYMENT_REQUEST. "+e);
         }       
    }
    
}
