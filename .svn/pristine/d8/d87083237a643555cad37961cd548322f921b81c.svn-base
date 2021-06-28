package tw.com.hyweb.svc.yhdp.batch.framework.cursorThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

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
    public ThreadPoolJobExecutor(int queueSize,int corePoolSize,int maximumPoolSize, long keepAliveTime)
    {
        this.queueSize = queueSize;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
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
        logger.debug("corePoolSize:"+corePoolSize+" maximumPoolSize:"+maximumPoolSize+" keepAliveTime:"+keepAliveTime+" queueSize:"+queueSize);
        this.executor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,TimeUnit.SECONDS,workQueue,handler);
        this.executor.prestartCoreThread();
    }
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.online.channel.IJobExecutor#execute(tw.com.hyweb.online.channel.JobRunner)
     */
    public boolean execute(Object job)
    {
        this.executor.execute((Runnable) job);
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
     * @param executor ��身摰�� executor
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

}
