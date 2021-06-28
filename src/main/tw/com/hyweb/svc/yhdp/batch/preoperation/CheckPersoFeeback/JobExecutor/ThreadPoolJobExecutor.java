package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * ThreadPoolJobExecutor
 */
public class ThreadPoolJobExecutor implements IJobExecutor
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(ThreadPoolJobExecutor.class);

    private int queueSize = 0;
    private int corePoolSize = 3;
    private int maximumPoolSize = 3;
    private long keepAliveTime = 60L;
    private int throughputRate = 1000;
    
    private BlockingQueue<Runnable> workQueue = null;
    
    private RejectedExecutionHandler handler = new RejectedExecutionHandler()
    {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) 
        {
            if (!e.isShutdown()) 
            {
                logger.warn("run rejectedExecution()");
                r.run();
            }
        }
    };
    
    private ThreadPoolExecutor executor;
     
    /**
     * 
     */
    public ThreadPoolJobExecutor()
    {
        if(queueSize > 0)
        {
            workQueue = new ArrayBlockingQueue<Runnable>(queueSize);
        }
        else
        {
            workQueue = new LinkedBlockingQueue<Runnable>();
        }
        
        this.executor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,TimeUnit.SECONDS,workQueue,handler);
        logger.info("corePoolSize:"+ this.executor.getCorePoolSize()+" maximumPoolSize:"+ this.executor.getMaximumPoolSize()+" keepAliveTime:"+keepAliveTime+" queueSize:"+queueSize);
        this.executor.prestartCoreThread();
    }
    
   
    /**
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     */
    public ThreadPoolJobExecutor(int queueSize,int corePoolSize,int maximumPoolSize, long keepAliveTime, int throughputRate)
    {
        this.queueSize = queueSize;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.throughputRate = throughputRate;
        if(queueSize > 0)
        {
            workQueue = new ArrayBlockingQueue<Runnable>(queueSize);
        }
        else
        {
            workQueue = new LinkedBlockingQueue<Runnable>();
        }
        
    }
    
    public void startThreadPoolJob()
    {
        logger.debug("close Thread Pool Job");
        this.executor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,TimeUnit.SECONDS,workQueue,handler);
        this.executor.prestartCoreThread();
    }
    
    public void endThreadPoolJob()
    {
        logger.debug("corePoolSize:"+corePoolSize+" maximumPoolSize:"+maximumPoolSize+" keepAliveTime:"+keepAliveTime+" queueSize:"+queueSize);
        if (executor != null && ! executor.isShutdown()){
        	this.executor.shutdown();
        }
    }
    
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.online.channel.IJobExecutor#execute(tw.com.hyweb.online.channel.JobRunner)
     */
    public boolean execute(JobRunner job)
    {
        this.executor.execute(job);
        logger.info("ActiveCount:"+executor.getActiveCount()+" CompletedCount:"+executor.getCompletedTaskCount()+" Queue().size()"+executor.getQueue().size());        
        return true;
    }
    
    //~-----------------------------------------------------------------------------------------------------------------
    
    /**
     * @return executor
     */
    public ThreadPoolExecutor getExecutor()
    {
        return executor;
    }

    /**
     * @param executor 嚙踝蕭�澈�堊垓嚙踝蕭 executor
     */
    public void setExecutor(ThreadPoolExecutor executor)
    {
        this.executor = executor;
    }
    
    public void setQueueSize(int queueSize)
    {
        this.queueSize = queueSize;
    }
    
    public int getQueueSize()
    {
        return this.queueSize;
    }
    
    public void setKeepAliveTime(long keepAliveTime)
    {
        this.keepAliveTime = keepAliveTime;
    }
    
    public long getKeepAliveTime()
    {
        return this.keepAliveTime;
    }
    
    public void setMaximumPoolSize(int maximumPoolSize)
    {
        this.maximumPoolSize = maximumPoolSize;
    }
    
    public int getMaximumPoolSize()
    {
        return this.maximumPoolSize;
    }
    
    public void setCorePoolSize(int corePoolSize)
    {
        this.corePoolSize = corePoolSize;
    }
    
    public int getCorePoolSize()
    {
        return this.corePoolSize;
    }

	public int getThroughputRate() {
		return throughputRate;
	}

	public void setThroughputRate(int throughputRate) {
		this.throughputRate = throughputRate;
	}

}

