package tw.com.hyweb.svc.yhdp.batch.summary.procPrepaidBal;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.sql.Connection;
import java.sql.Savepoint;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Clare
 * 
 */
public class BatchJobHandler implements BatchHandler
{
    private static final Logger LOGGER = Logger.getLogger(BatchJobHandler.class);

    private final BatchJobFactory factory;

    private int commitPerJob = 1;
    private int sleepTimePerCommit = 0;

    public BatchJobHandler(BatchJobFactory factory)
    {
        this.factory = factory;
    }

    /**
     * 每次處理一個batch unit，處理完會呼叫remark success，失敗則執行rollback並呼叫remark failure
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler#handle(java.sql.Connection,
     *      java.lang.String)
     */
    public BatchHandleResult handle(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        String rcode = Layer1Constants.RCODE_0000_OK;
        String errorDescribe = "";

        int successCount = 0;
        int failureCount = 0;

        try
        {
        	//20170719配合報表，已沖銷資料不顯示增加註記沖銷欄位
        	//update 已沖銷資料
        	try {
        		StringBuffer sql = new StringBuffer();
        		
        		sql.append(" UPDATE TB_PREPAID_CHARGE SET IS_CHARGE = 'Y' WHERE (CARD_NO, EXPIRY_DATE) IN (");
        		sql.append(" SELECT CARD_NO, EXPIRY_DATE");
        		sql.append(" FROM TB_PREPAID_CHARGE, TB_TXN_DEF");
        		sql.append(" WHERE TB_TXN_DEF.TXN_CODE = TB_PREPAID_CHARGE.TXN_CODE");
        		sql.append(" AND PROC_DATE <= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        		sql.append(" AND IS_CHARGE = 'N'");
        		sql.append(" GROUP BY CARD_NO, EXPIRY_DATE");
        		sql.append(" HAVING SUM(CASE WHEN SIGN = 'P' THEN PREPAID_CHARGE WHEN SIGN = 'M' THEN PREPAID_CHARGE*-1 ELSE 0 END) = 0)");
        		sql.append(" AND PROC_DATE <= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        		sql.append(" AND IS_CHARGE = 'N'");
        		
            	executeUpdate(connection, sql.toString());
            	
            	connection.commit();
            	
			} catch (Exception e) {
				LOGGER.error("charge error:", e);
				rcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;
				errorDescribe = errorDescribe + "charge error!! ";
			}
        	
        	if (batchDate.substring(6,8).equals("01")){
        		factory.init(connection, batchDate, tbBatchResultInfo);

                while (factory.hasNext())
                {
                    BatchJob job = factory.next(connection, batchDate);

                    Savepoint savepoint = connection.setSavepoint();

                    try
                    {

                        job.action(connection, batchDate);
                        job.remarkSuccess(connection, batchDate);

                        ++successCount;
                    }
                    catch (BatchJobException e)
                    {
                        LOGGER.warn("exception when handle batch job: " + job, e);
                        connection.rollback(savepoint);

                        job.remarkFailure(connection, batchDate, e);
                        rcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;

                        ++failureCount;
                    }
                    catch (Exception e)
                    {
                        LOGGER.warn("exception when handle batch job: " + job, e);
                        connection.rollback(savepoint);

                        job.remarkFailure(connection, batchDate, new BatchJobException(e, Layer1Constants.RCODE_2001_SOMEDATAERROR));
                        rcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;

                        ++failureCount;
                    }

                    if (commitPerJob > 0 && successCount % commitPerJob == 0)
                    {
                        connection.commit();

                        if (sleepTimePerCommit > 0)
                        {
                            Thread.sleep(sleepTimePerCommit);
                        }
                    }
                }

                connection.commit();
        	}
        	else
        	{
        		LOGGER.warn("[" + batchDate + "] is not the first day of the month.");
        	}
        }
        finally
        {
            try
            {
                factory.destroy();
            }
            catch (Exception e)
            {
                LOGGER.warn("exception when destroy factory", e);
            }
        }
        errorDescribe = errorDescribe + "success:" + successCount + ", failure:" + failureCount;
        
        return new BatchHandleResult(errorDescribe, rcode);
    }

    /**
     * 設定幾個job要commit一次，小於等於0則全部job做完才會commit
     * 
     * @param commitPerJob
     *            the commitPerJob to set
     */
    public void setCommitPerJob(int commitPerJob)
    {
        this.commitPerJob = commitPerJob;
    }

    /**
     * 設定每次commit之後handler要sleep多少ms，小於等於0則不sleep
     * 
     * @param sleepTimePerCommit
     *            the sleepTimePerCommit to set
     */
    public void setSleepTimePerCommit(int sleepTimePerCommit)
    {
        this.sleepTimePerCommit = sleepTimePerCommit;
    }
}
