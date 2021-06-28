package tw.com.hyweb.svc.yhdp.batch.framework.cursorThread;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;

public class BatchThreadJobHandler implements BatchHandler{

	private static final Logger LOGGER = Logger.getLogger(BatchThreadJobHandler.class);

    private final BatchJobFactory factory;
    private ThreadPoolJobExecutor threadPoolJobExecutor = null;
    private JobRunnerFactory jobRunnerFactory = null;
    private int fetchCount = 10000;

    private Hashtable<String, Long> resultMap = new Hashtable<String, Long>();

    public BatchThreadJobHandler(BatchJobFactory factory, JobRunnerFactory jobRunnerFactory, ThreadPoolJobExecutor threadPoolJobExecutor)
    {
        this.factory = factory;
        this.jobRunnerFactory = jobRunnerFactory;
        this.threadPoolJobExecutor = threadPoolJobExecutor;
    }
	
    /**
     * �C���B�z�@��batch unit�A�B�z���|�I�sremark success�A���ѫh����rollback�éI�sremark failure
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchHandler#handle(java.sql.Connection,
     *      java.lang.String)
     */
    public BatchHandleResult handle(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        String rcode = Layer1Constants.RCODE_0000_OK;
        
        resultMap.put(Layer1Constants.RCODE_0000_OK, 0L);
        resultMap.put(Layer1Constants.RCODE_2001_SOMEDATAERROR, 0L);

        try
        {
        	getThreadPoolJobExecutor().startThreadPoolJob();

        	//資料總數分批動作
        	factory.divideBatch(connection, batchDate, fetchCount, tbBatchResultInfo);
        	
        	//資料分批撈取
        	while (factory.hasBatchNext()){
        		factory.init(connection, batchDate, tbBatchResultInfo);

                while (factory.hasNext())
                {
                    BatchJob job = factory.next(connection, batchDate);

                    if(getThreadPoolJobExecutor() != null && getThreadPoolJobExecutor().getExecutor() != null)
    		        {
    					if ( resultMap != null){
    		                ThreadPoolExecutor threadPoolExecutor = getThreadPoolJobExecutor().getExecutor();
    		                
    		                Runnable jobRunnerExecute = jobRunnerFactory.create(job, resultMap, batchDate);
    		                
    		                threadPoolExecutor.execute(jobRunnerExecute);
    					}
    	            
    		        }
                }
                
                while (getThreadPoolJobExecutor().getExecutor().getActiveCount() != 0) {
    	    		Thread.sleep(2000);
    	    		LOGGER.debug("ActiveCount:"+getThreadPoolJobExecutor().getExecutor().getActiveCount()
    	    				+" CompletedCount:"+getThreadPoolJobExecutor().getExecutor().getCompletedTaskCount());
    			}

                connection.commit();
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
            
            finally
            {
    	    	if(getThreadPoolJobExecutor() != null && getThreadPoolJobExecutor().getExecutor() != null 
    	                && getThreadPoolJobExecutor().getExecutor().isShutdown() == false)
    	        {
    				getThreadPoolJobExecutor().getExecutor().shutdown();
    				LOGGER.info("threadPoolJobExecutor shutdown!!");
    	        }
    	    	
    	    	LOGGER.debug("Ok Count:" + resultMap.get(Layer1Constants.RCODE_0000_OK) 
    	    			+ " / Fail Count: " +  resultMap.get(Layer1Constants.RCODE_2001_SOMEDATAERROR));
    	    	
    	    	if (rcode.equals(Layer1Constants.RCODE_0000_OK) && resultMap.get(Layer1Constants.RCODE_2001_SOMEDATAERROR) > 0){
    	    		rcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;
    	    	}
    	    	
    	    	LOGGER.debug("========================================end===========================================");
            }
        }

        return new BatchHandleResult("Ok Count:" + resultMap.get(Layer1Constants.RCODE_0000_OK) 
    			+ " / Fail Count: " +  resultMap.get(Layer1Constants.RCODE_2001_SOMEDATAERROR), rcode);
    }
    
	public ThreadPoolJobExecutor getThreadPoolJobExecutor() {
		return threadPoolJobExecutor;
	}

	public void setThreadPoolJobExecutor(ThreadPoolJobExecutor threadPoolJobExecutor) {
		this.threadPoolJobExecutor = threadPoolJobExecutor;
	}

	public JobRunnerFactory getJobRunnerFactory() {
		return jobRunnerFactory;
	}

	public void setJobRunnerFactory(JobRunnerFactory jobRunnerFactory) {
		this.jobRunnerFactory = jobRunnerFactory;
	}

	public int getFetchCount() {
		return fetchCount;
	}

	public void setFetchCount(int fetchCount) {
		this.fetchCount = fetchCount;
	}
}
