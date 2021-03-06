package tw.com.hyweb.svc.yhdp.batch.errhandle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.errhandle.CheckTxnInfo;
import tw.com.hyweb.core.cp.batch.errhandle.CheckTxnUtility;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsUtil;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class ProcUnbalTermBatch extends AbstractBatchBasic
{
    public static final String UNBAL_HANDLE_TYPE_PARM = "UNBAL_HANDLE_TYPE";

    private static final Logger LOG = Logger.getLogger(ProcUnbalTermBatch.class);
    private static final String SPRING_PATH = FilenameUtils.separatorsToSystem("config/batch/ErrorHandle/ProcUnbalTermBatch/spring.xml");

    private boolean isRecover = false;

    private Connection connection;
    private String batchDate;
    private String batchTime = DateUtil.getTodayString().substring(8);

    private CheckTxnUtility checkTxnUtility;

    public ProcUnbalTermBatch()
    {

    }

    public void process(String[] args) throws Exception
    {
        try
        {
            connection = BatchUtil.getConnection();
            connection.setAutoCommit(false);

            if (isRecover)
            {
                recover();
            }
            else
            {
                processUnbalanceTermBatchs("1", Layer2Util.getBatchConfig(UNBAL_HANDLE_TYPE_PARM));
                /*20091204: ????????????????????????????????????????????????,????????????*/
                processUnbalanceTermBatchs("9",  Layer2Util.getBatchConfig(UNBAL_HANDLE_TYPE_PARM));
            }

            connection.commit();
        }
        catch (Exception e)
        {
            LOG.warn("Exception when process unbalance term batch", e);
            connection.rollback();

            throw e;
        }
        finally
        {
            ReleaseResource.releaseDB(connection);
        }
    }

    private void recover() throws SQLException
    {
        StringBuilder sql = new StringBuilder("");

        sql.append("UPDATE TB_TERM_BATCH ");
        sql.append("SET UNBAL_PROC_DATE = NULL, UNBAL_PROC_RCODE = '0000', UNBAL_CONFIRM_DATE = NULL ");
        sql.append("WHERE TERM_SETTLE_DATE = '" + batchDate + "' AND TXN_SRC = 'E' ");
        sql.append("AND TERM_SETTLE_FLAG = '9' AND TERM_SETTLE_FLAG2 IS NOT NULL ");
        sql.append("AND CUT_DATE IS NULL AND CUT_TIME IS NULL AND UNBAL_PROC_DATE IS NOT NULL AND UNBAL_PROC_RCODE != '0000'");

        DBService.getDBService().sqlAction(sql.toString(), connection, false);
    }

    private void processUnbalanceTermBatchs(String termSettleFlag2, String unbalHandleType) throws SQLException
    {
        for (TbTermBatchInfo info : getTermBatchs(termSettleFlag2))
        {
            processUnbalanceTermBatch(info, unbalHandleType);
        }
    }

    private void processUnbalanceTermBatch(TbTermBatchInfo termBatch, String unbalHandleType)
    {
        try
        {
            termBatch.setUnbalProcDate(batchDate);
            termBatch.setUnbalProcRcode("0000");

            if ("1".equals(unbalHandleType) || "2".equals(unbalHandleType))
            {
                termBatch.setUnbalConfirmDate(batchDate);
            }

            List<CheckTxnInfo> onlCheckInfos = checkTxnUtility.makeOnlCheckTxns(connection, termBatch);
            List<CheckTxnInfo> uploadCheckInfos = checkTxnUtility.makeUploadCheckTxns(connection, termBatch);

            ExecuteSqlsInfo sqls = new ExecuteSqlsInfo();
            sqls.setCommit(true);
            sqls.setSavepoint(true);
            sqls.setSqls(checkTxnUtility.makeInsertCheckTxnSQLs(connection, onlCheckInfos, uploadCheckInfos, unbalHandleType));
            sqls.addSql(termBatch.toUpdateSQL());

            LOG.info("sqlsInfo:" + sqls);
            ExecuteSqlsUtil.executeSqls(connection, sqls);
        }
        catch (Exception e)
        {
            LOG.warn("handleUnbalTermBatch error:" + e.getMessage(), e);
            setRcode(Constants.RCODE_2001_WARN);

            try
            {
                termBatch.setUnbalProcDate(batchDate);
                termBatch.setUnbalProcRcode("2050");

                ExecuteSqlsInfo sqls = new ExecuteSqlsInfo();
                sqls.setCommit(true);
                sqls.setSavepoint(true);
                sqls.addSql(termBatch.toUpdateSQL());

                LOG.info("sqlsInfo:" + sqls);
                ExecuteSqlsUtil.executeSqls(connection, sqls);
            }
            catch (Exception e2)
            {
                LOG.warn("handleUnbalTermBatch error:" + e2.getMessage(), e2);
            }
        }
    }

    private List<TbTermBatchInfo> getTermBatchs(String termSettleFlag) throws SQLException
    {
        Vector<TbTermBatchInfo> result = new Vector<TbTermBatchInfo>();
        new TbTermBatchMgr(connection).queryMultiple(getTermBatchCondition(termSettleFlag), result);

        return result;
    }

    private String getTermBatchCondition(String termSettleFlag2)
    {
        StringBuilder sb = new StringBuilder("");

        sb.append("CONCAT(TERM_SETTLE_DATE, TERM_SETTLE_TIME) <= '" + batchDate + batchTime + "' AND TXN_SRC = 'E' ");
        sb.append("AND TERM_SETTLE_FLAG = '9' AND TERM_SETTLE_FLAG2 = '" + termSettleFlag2 + "' ");
        sb.append("AND CUT_DATE IS NULL AND CUT_TIME IS NULL AND UNBAL_PROC_DATE IS NULL");

        return sb.toString();
    }

    /**
     * @param batchDate
     *            the batchDate to set
     */
    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

    /**
     * @param isRecover
     *            the isRecover to set
     */
    public void setRecover(boolean isRecover)
    {
        this.isRecover = isRecover;
    }

    /**
     * @param checkTxnUtility
     *            the checkTxnUtility to set
     */
    public void setCheckTxnUtility(CheckTxnUtility checkTxnUtility)
    {
        this.checkTxnUtility = checkTxnUtility;
    }

    public static void main(String[] args)
    {
        String batchDate = System.getProperty("date");

        if (StringUtil.isEmpty(batchDate))
        {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }

        ProcUnbalTermBatch process = new ProcUnbalTermBatch();
        process.setBatchDate(batchDate);
        process.setRecover(isRecover());
        process.setCheckTxnUtility((CheckTxnUtility) new FileSystemXmlApplicationContext(SPRING_PATH).getBean("checkTxnUtility"));
        process.run(args);
    }

    private static boolean isRecover()
    {
        if (System.getProperty("recover") != null)
        {
            return "ERR".equals(System.getProperty("recover").toUpperCase());
        }
        else
        {
            return false;
        }
    }
}
