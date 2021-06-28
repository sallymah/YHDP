/*
 * Version: 3.0.0
 * Date: 2007-01-29
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.IBatchProcess;
import tw.com.hyweb.core.cp.batch.framework.IBatchResult;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.string.StringUtil;

/**
 * 餘額管理<br>
 * 
 * @author Anny
 */
public class ProcBalance extends AbstractBatchBasic implements IBatchResult, IBatchProcess
{
    private static final Logger logger = Logger.getLogger(ProcBalance.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "ProcBalance" + File.separator + "spring.xml";
    protected static String batchDate = null;
    private int cutDay = 0;
    protected String recoverLevel;
    
    public String getRecoverLevel()
    {
        return recoverLevel;
    }

    public void setRecoverLevel(String recoverLevel)
    {
        this.recoverLevel = recoverLevel;
    }

    protected String getBatchDate()
    {
        return batchDate;
    }

    protected void setBatchDate()
    {
        BatchUtil.getNow();
        if (StringUtil.isEmpty(System.getProperty("date")))
        {
            batchDate = BatchUtil.sysDay;
        }
        else
        {
            batchDate = System.getProperty("date");
        }
    }
    
    public int getCutDay() {
		return cutDay;
	}

	public void setCutDay(int cutDay) {
		this.cutDay = cutDay;
	}

	/**
     * 將TB_TRANS_DTL註記資訊同步到TB_TRANS
     * @param conn
     * @throws Exception
     */
    private void syncTransAndTransDtl() throws Exception
    {
        Connection conn = null;
        boolean ret = false;
        boolean autoCommit = true;

        try
        {
            conn = BatchUtil.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            StringBuffer sqlCmd = new StringBuffer();
            System.out.println();
            
            // TB_TRANS_DTL發生RCODE , 其對應TB_TRANS 就註記RCODE=2200
            
            sqlCmd.append("update TB_TRANS ");
            sqlCmd.append(" set BAL_RCODE='").append(Constants.RCODE_2200_BAL_ERR).append("',");
            sqlCmd.append(" BAL_PROC_DATE='").append(getBatchDate()).append("'");
            sqlCmd.append(" where ");
            sqlCmd.append("CUT_DATE BETWEEN '");
            sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
            sqlCmd.append("AND '");
            sqlCmd.append(getBatchDate()).append("' ");
            sqlCmd.append(" AND ( CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO ) in (");
            sqlCmd.append(" select CARD_NO,EXPIRY_DATE,LMS_INVOICE_NO");
            sqlCmd.append(" from TB_TRANS_DTL");
            sqlCmd.append(" where ");
            sqlCmd.append("CUT_DATE BETWEEN '");
            sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
            sqlCmd.append("AND '");
            sqlCmd.append(getBatchDate()).append("' ");
            sqlCmd.append(" AND BAL_RCODE !='").append(Constants.RCODE_0000_OK).append("'");
            sqlCmd.append(" AND BAL_PROC_DATE ='").append(getBatchDate()).append("' ");
            sqlCmd.append(" )");
            
            DBService.getDBService().sqlAction(sqlCmd.toString(), conn);
            
            sqlCmd.delete(0, sqlCmd.length()); //清空sqlCmd
            
            //TB_TRANS_DTL全部都成功 , 其對應TB_TRANS 就註記RCODE=0000 
            sqlCmd.append("update TB_TRANS ");
            sqlCmd.append(" set BAL_RCODE = '").append(Constants.RCODE_0000_OK).append("',");
            sqlCmd.append(" BAL_PROC_DATE='").append(getBatchDate()).append("' ");
            sqlCmd.append(" where ");
            sqlCmd.append("CUT_DATE BETWEEN '");
            sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
            sqlCmd.append("AND '");
            sqlCmd.append(getBatchDate()).append("' ");
            sqlCmd.append(" and bal_rcode='").append(Constants.RCODE_0000_OK).append("'");
            sqlCmd.append(" and ( CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO ) in (");
            sqlCmd.append(" select CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO");
            sqlCmd.append(" from TB_TRANS_DTL");
            sqlCmd.append(" where ");
            sqlCmd.append("CUT_DATE BETWEEN '");
            sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
            sqlCmd.append("AND '");
            sqlCmd.append(getBatchDate()).append("' ");
            sqlCmd.append(" AND BAL_RCODE = '").append(Constants.RCODE_0000_OK).append("'");
            sqlCmd.append(" and BAL_PROC_DATE ='").append(getBatchDate()).append("' ");
            sqlCmd.append(" Group by CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO");    
            sqlCmd.append(")");
            
            DBService.getDBService().sqlAction(sqlCmd.toString(), conn);
            
            conn.commit();
            ret = true;
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
            ret = false;
            if (conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ignore2)
                {
                    ;
                }
            }
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.setAutoCommit(autoCommit);
                }
                catch (Exception ignore)
                {
                    ;
                }
                try
                {
                    conn.close();
                }
                catch (Exception ignore)
                {
                    ;
                }
            }
        }       
    }
    
    protected void recoverData() throws Exception
    {
        Connection conn = null;
        boolean autoCommit = true;

        try
        {
            conn = BatchUtil.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            List sqls = new ArrayList();

            // TB_TRANS_DTL
            sqls.add(getRecoverTransDtlSQL());

            // TB_TRANS
            sqls.add(getRecoverTransSQL());

            for (int i = 0; i < sqls.size(); i++)
            {
                String sql = (String) sqls.get(i);
                logger.info(sql);
                DBService.getDBService().sqlAction(sql, conn);
            }

            conn.commit();

        }
        catch (Exception ignore)
        {
            if (conn != null)
            {
                try
                {
                    logger.info("RollBack!!");
                    conn.rollback();
                }
                catch (Exception ignore2)
                {
                    throw new Exception(ignore2);
                }
            }
            
            throw new Exception("recover warn : ",ignore);
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.setAutoCommit(autoCommit);
                }
                catch (Exception ignore)
                {
                    throw new Exception(ignore);
                }
                try
                {
                    conn.close();
                }
                catch (Exception ignore)
                {
                    throw new Exception(ignore);
                }
            }
        }

    }
    

    protected String getRecoverTransDtlSQL()
    {
        StringBuffer sql = new StringBuffer();

        sql.append("Update TB_TRANS_DTL ");
        sql.append(" set BAL_RCODE = '").append(Constants.RCODE_0000_OK).append("',");
        sql.append(" BAL_PROC_DATE ='00000000' ");
        sql.append("WHERE ");
        sql.append(getRecoverCondSQL());

        return sql.toString();
    }

    protected String getRecoverTransSQL()
    {
        StringBuffer sql = new StringBuffer();

        sql.append("Update TB_TRANS ");
        sql.append(" set BAL_RCODE = '").append(Constants.RCODE_0000_OK).append("',");
        sql.append(" BAL_PROC_DATE = null ");
        sql.append("WHERE ");
        sql.append(getRecoverCondSQL());

        return sql.toString();
    }
    
    protected String getRecoverCondSQL()
    {
        StringBuffer recoverCond = new StringBuffer();

        recoverCond.append(" BAL_RCODE !='").append(Constants.RCODE_0000_OK).append("'");
        recoverCond.append(" AND BAL_PROC_DATE ='").append(getBatchDate()).append("' ");

        return recoverCond.toString();

    }

    public static ProcBalance getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ProcBalance instance = (ProcBalance) apContext.getBean("procBalance");
        return instance;
    }
    
    /**
     * Entry<br>
     * ant runBalChipCard –Ddate=””<br>
     * batchDate={–Ddate請輸入YYYYMMDD；若不輸入，預設為系統日}<br>
     * 
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException
    {
        try
        {
            ProcBalance instance = new ProcBalance();
            
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	instance = getInstance();
            }
            else {
            	instance = new ProcBalance();
            }
            
            instance.run(null);
        }
        catch (Exception e)
        {
            logger.warn("", e.fillInStackTrace());
        }
    }

    public void process(String[] arg0) throws Exception
    {        
        // 開始執行
        setBatchDate();
        setRecoverLevel(System.getProperty("recover"));
        
        logger.info("batchDate:"+getBatchDate());
        logger.info("cutDay:"+getCutDay());
        
        if (StringUtil.isEmpty(getRecoverLevel()))
        {       
            logger.info("===UpdateErrorData===");
            UpdateErrorData instance = new UpdateErrorData(getBatchDate(), cutDay);
            
            logger.info("===BalChipCard===");
            BalChipCard instance1 = new BalChipCard(getBatchDate(), this, cutDay);
            
            logger.info("===BalHostCard===");
            BalHostCard instance2 = new BalHostCard(getBatchDate(), this, cutDay);
            
            logger.info("===BalHostAcct===");
            BalHostAcct instance3 = new BalHostAcct(getBatchDate(), this, cutDay);
            
            logger.info("===BalHostCust===");
            BalHostCust instance4 = new BalHostCust(getBatchDate(), this, cutDay);
            
            logger.info("===syncTransAndTransDtl===");
            syncTransAndTransDtl(); 
        }
        else
        {
            logger.info("===Start Recover Data===");
            recoverData();
        }

    }
}
