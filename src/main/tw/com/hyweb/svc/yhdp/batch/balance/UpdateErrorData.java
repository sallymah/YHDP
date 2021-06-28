/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.svc.yhdp.batch.balance;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.util.ReleaseResource;

/**
 * 將不需要做餘額處理的資料先Update<br>
 * 
 * @author Anny
 */
public class UpdateErrorData
{
    private static final Logger logger = Logger.getLogger(UpdateErrorData.class);

    String batchDate;
    int cutDay;

    public String getBatchDate()
    {
        return batchDate;
    }

    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

    public int getCutDay() {
		return cutDay;
	}

	public void setCutDay(int cutDay) {
		this.cutDay = cutDay;
	}

	/**
     * 初始設定
     */
    private void init()
    {
        if (!BatchUtil.checkChristianDate(getBatchDate()))
        {
            logger.debug("Date Format Error:Please Input YYYYMMDD");
            System.exit(1);
        }

        logger.debug("batchDate:" + batchDate);

        logger.debug("init(): ok.\n");
    }

    public void action() throws Exception
    {        
        Connection conn = null;
        Statement stmt = null;
        boolean autoCommit = false;
        int updateCount = 0;
        StringBuffer sqlCmd = new StringBuffer("");
        
        try
        {
            conn = BatchUtil.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            
            sqlCmd.append("update tb_trans");        
            sqlCmd.append(" set bal_proc_date='").append(getBatchDate()).append("',");
            sqlCmd.append(" bal_rcode ='").append(Constants.RCODE_2201_BAL_NOBAL).append("'");
            sqlCmd.append(" where ");
            sqlCmd.append("CUT_DATE BETWEEN '");
            sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
            sqlCmd.append("AND '");
            sqlCmd.append(getBatchDate()).append("' ");
            sqlCmd.append(" AND check_flag='1'");
            sqlCmd.append(" AND bal_proc_date = null");
            sqlCmd.append(" AND ( card_no, expiry_date, lms_invoice_no ) IN (");
            sqlCmd.append(" SELECT tb_trans.card_no, tb_trans.expiry_date, tb_trans.lms_invoice_no");
            sqlCmd.append(" from tb_trans, tb_trans_check_err where");
            sqlCmd.append(" tb_trans.card_no=tb_trans_check_err.card_no");
            sqlCmd.append(" AND tb_trans.expiry_date=tb_trans_check_err.expiry_date");
            sqlCmd.append(" AND tb_trans.lms_invoice_no=tb_trans_check_err.lms_invoice_no");
            sqlCmd.append(" AND tb_trans_check_err.check_rcode in ");
            sqlCmd.append(" (select rcode from tb_rcode_config where balance_flag='0')");
            sqlCmd.append(" )");
            logger.info(sqlCmd.toString());
            updateCount = stmt.executeUpdate(sqlCmd.toString());
            
            sqlCmd.delete(0, sqlCmd.length()); //清空sqlCmd
            sqlCmd.append("update tb_trans_dtl");        
            sqlCmd.append(" set bal_proc_date='").append(getBatchDate()).append("',");
            sqlCmd.append(" bal_rcode='").append(Constants.RCODE_2201_BAL_NOBAL).append("'");
            sqlCmd.append(" where ");
            sqlCmd.append("CUT_DATE BETWEEN '");
            sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
            sqlCmd.append("AND '");
            sqlCmd.append(getBatchDate()).append("' ");
            sqlCmd.append(" AND (CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO) in");
            sqlCmd.append(" (select CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO");
            sqlCmd.append(" from TB_TRANS ");
            sqlCmd.append(" where ");
            sqlCmd.append("CUT_DATE BETWEEN '");
            sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
            sqlCmd.append("AND '");
            sqlCmd.append(getBatchDate()).append("' ");
            sqlCmd.append(" AND check_flag='1'");
            sqlCmd.append(" AND bal_proc_date = '").append(getBatchDate()).append("'");
            sqlCmd.append(" AND (card_no, expiry_date, lms_invoice_no) IN (");
            sqlCmd.append(" SELECT tb_trans.card_no, tb_trans.expiry_date, tb_trans.lms_invoice_no");
            sqlCmd.append(" from tb_trans, tb_trans_check_err where");
            sqlCmd.append(" tb_trans.card_no=tb_trans_check_err.card_no");
            sqlCmd.append(" AND tb_trans.expiry_date=tb_trans_check_err.expiry_date");
            sqlCmd.append(" AND tb_trans.lms_invoice_no=tb_trans_check_err.lms_invoice_no");
            sqlCmd.append(" AND tb_trans_check_err.check_rcode in ");
            sqlCmd.append(" (select rcode from tb_rcode_config where balance_flag='0')");
            sqlCmd.append(" )");
            sqlCmd.append(" )");
            sqlCmd.append(" AND bal_proc_date ='00000000'");
            logger.info(sqlCmd.toString());
            updateCount = stmt.executeUpdate(sqlCmd.toString());
            
            conn.commit();
        }
        catch (Exception e)
        {
            if (conn != null) {
                try {
                    conn.rollback();
                }
                catch (Exception ignore) {
                    throw new Exception(ignore);
                }
            }
            logger.warn("updateErrorData error:" + sqlCmd, e);
            throw new Exception(e);
        }     
        finally
        {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommit);
                }
                catch (Exception ignore) {
                    throw new Exception(ignore);
                }
            }
            ReleaseResource.releaseDB(conn, stmt, null);
        }    
        
        logger.info("UpdateErrorData updated: " + updateCount + " recs.");
    }

    /**
     * 主要程式流程<br>
     */
    public void process(String[] argv) throws Exception
    {
        init();
        action();
    }

    /**
     * Entry<br>
     * ant runFtpOut –Ddate=””<br>
     * batchDate={–Ddate請輸入YYYYMMDD；若不輸入，預設為系統日}<br>
     * 
     * @throws SQLException
     */
    public UpdateErrorData(String batchDate, int cutDay) throws Exception
    {
        try
        {
            // 開始執行
            setBatchDate(batchDate);
            setCutDay(cutDay);
            process(null);
        }
        catch (Exception e)
        {
            logger.warn("UpdateErrorData:"+e);
            throw new Exception("UpdateErrorData : "+e , e);
        }
    }
}
