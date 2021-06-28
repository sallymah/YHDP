package tw.com.hyweb.svc.yhdp.batch.framework.cursorThread;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;

public class JobRunner implements Runnable
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(JobRunner.class);
    private byte[] header;
    private byte[] body;
    private Date timeJobInit;
    private Date timeJobExec;    
    private Hashtable<String, Long> resultMap = null; 
    IContextListener ctxListener;
    
	public BatchJob job;
	public String batchDate = "";
    /**
     * Creates a new InnerJobRunner object.
     * @param lChannel - the channel receive the message
     * @param lHeader - raw data header
     * @param lBody - raw data body
     */
    public JobRunner()
    {
        this.timeJobInit = new Date();
    }
    
    /**
     * Creates a new InnerJobRunner object.
     * @param acceptRemoteIp - the accept client remote ip
     * @param lChannel - the channel receive the message
     * @param lHeader - raw data header
     * @param lBody - raw data body
     */
    public JobRunner(BatchJob job,
    		Hashtable<String, Long> resultMap, String batchDate)
    {
    	this.job = job;
    	this.resultMap = resultMap;
    	this.batchDate = batchDate;
    }

    /**
     * It call connector process() method to handle message,
     * 用來處理raw data byte[]轉換成Context及時間的紀錄
     */
    public void run()
    {
    	this.timeJobExec = new Date();
    	
        try {
            //logger.debug( job.toString() + " is begin working");
            // handle impFileInfo
            handleImpFileInfo();
            resultMap.put(Layer1Constants.RCODE_0000_OK, resultMap.get(Layer1Constants.RCODE_0000_OK) + 1);
        }
        catch (Exception e) {
            logger.error("handleImpFileInfo error:" + e.getMessage(), e);
            resultMap.put(Layer1Constants.RCODE_2001_SOMEDATAERROR, resultMap.get(Layer1Constants.RCODE_2001_SOMEDATAERROR) + 1);
        }
    }

    public void handleImpFileInfo() throws Exception {
    	
    	Connection connection = null;
    	connection = DBService.getDBService().getConnection(BatchUtil.DBUSER_BATCH);
    	Savepoint savepoint = connection.setSavepoint();
        try
        {
            job.action(connection, batchDate);
            job.remarkSuccess(connection, batchDate);
        }
        catch (BatchJobException e)
        {
        	logger.warn("exception when handle batch job: " + job, e);
            connection.rollback(savepoint);

            job.remarkFailure(connection, batchDate, e);

        }
        catch (Exception e)
        {
        	logger.warn("exception when handle batch job: " + job, e);
            connection.rollback(savepoint);

            job.remarkFailure(connection, batchDate, new BatchJobException(e, Layer1Constants.RCODE_2001_SOMEDATAERROR));
        }
        finally
        {
        	ReleaseResource.releaseDB(connection);
        }
    	
    }
    
    /**
     * 處理資料收到但是解不開的情形
     * @param ctx incoming context
     */
    protected void processGarbage(Context ctx)
    {
        logger.info(ctx+"header:"+ISOUtil.hexString(header)+" body:"+ISOUtil.hexString(body));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final JobRunner other = (JobRunner) obj;
        if (!Arrays.equals(this.body, other.body))
            return false;
        if (!Arrays.equals(this.header, other.header))
            return false;
        if (this.timeJobExec == null)
        {
            if (other.timeJobExec != null)
                return false;
        }
        else if (!this.timeJobExec.equals(other.timeJobExec))
            return false;
        if (this.timeJobInit == null)
        {
            if (other.timeJobInit != null)
                return false;
        }
        else if (!this.timeJobInit.equals(other.timeJobInit))
            return false;
        return true;
    }
    
	public BatchJob getJob() {
		return job;
	}

	public void setJob(BatchJob job) {
		this.job = job;
	}

	public String getBatchDate() {
		return batchDate;
	}

	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}
    
}
