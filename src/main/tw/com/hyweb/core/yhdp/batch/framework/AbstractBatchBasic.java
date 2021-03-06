/**
 * changelog
 * --------------------
 * 20081224
 * duncan
 * 開放 errorDesc
 * --------------------
 * 20080908
 * duncan
 * little modify for CPC 車隊卡, 但這個修改對其他沒影響
 * --------------------
 */
/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */
/*
changelog:
$date:$user
20061214:duncan
1. add rcode, tbBatchResultInfo properties
20080304:clare
1. support changing of ProgramName
*/

/*
* (版權及授權描述)
*
* Copyright 2006 (C) Hyweb Technology Co., Ltd. All Rights Reserved.
*
* $History: AbstractBatchBasic.java $
*
* *****************  Version 2  *****************
* User: Rock         2006/10/27 (YYYY/MM/DD) Time: 11:30
* dao重gen，db欄位名稱變更須改程式
* 拿掉PROC_MODE，WORK_FLAG=2=>1
*
* *****************  Version 1  *****************
* User: Robert       2006/08/29 (YYYY/MM/DD) Time: 15:41
*
*/
package tw.com.hyweb.core.yhdp.batch.framework;

import java.util.Date;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jpos.iso.ISODate;

import tw.com.hyweb.util.DisposeUtil;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.mgr.TbBatchResultMgr;
import tw.com.hyweb.service.db.DBService;

import java.sql.Connection;

/**
 * General program that only use Batch Result<br>
 * 
 * @author Robert
 */
public abstract class AbstractBatchBasic implements IBatchProcess, IBatchResult
{
    private static final Logger logger = Logger.getLogger(AbstractBatchBasic.class);

    private static final String BATCH_RESULT_WORK_FLAG_START = "0";

    private static final String BATCH_RESULT_WORK_FLAG_END = "1";

    private static final String BATCH_RESULT_WORK_FLAG_FAIL = "9";

    private String errorDesc = "";

    private static final String RCODE_0000 = "0000";
    private static final String RCODE_2999 = "2999";
    private String rcode = RCODE_0000;
    private TbBatchResultInfo batchResultInfo = null;
    // linkControl
    private String linkControl = "N";

    private String programName = System.getProperty("targetName");
    
    private String errDesc;
    
    public AbstractBatchBasic()
    {
    }

    /**
     * Run<br>
     * 
     * @param argv
     */
    public void run(String[] argv)
    {
    	if(programName == null || programName.equals("")){
    		programName = getClass().getSimpleName();
    	}
    	
    	String level = System.getProperty("logMode");
        if (!StringUtil.isEmpty(level)) {
        	defineLog4jThreshold(level);
        }
    	
    	logger.info("begin running");

        try
        {
            batchResultInfo = openBatchResult();
            process(argv);
        }
        catch (Exception e)
        {
            logger.error("run batch fail:" + e.getMessage(), e);
            batchResultInfo.setWorkFlag(BATCH_RESULT_WORK_FLAG_FAIL);
            if (RCODE_0000.equals(rcode)) {
                setRcode(RCODE_2999);
            }
            if (!StringUtil.isEmpty(errorDesc)) {
                setErrorDesc(errorDesc + " " + e.toString());
            }
            else {
                setErrorDesc(e.toString());
            }
        }
        finally
        {
            try
            {
            	if(getErrorDesc().getBytes().length > 250){
            		byte[] vResult = new byte[250];
                    System.arraycopy( getErrorDesc().getBytes(), 0, vResult, 0, 250);
            		errDesc = new String(vResult);
            	}
            	else {
            		errDesc = getErrorDesc();
            	}
                closeBatchResult(errDesc, batchResultInfo);
            }
            catch (Exception ie)
            {
                logger.error("close batch result fail:" + ie.fillInStackTrace());
            }
            finally
            {
                logger.info("end running\n");
            }
        }
    }
    
    /**
     * define Log4j Threshold
     * Priority order is DEBUG < INFO < WARN < ERROR < FATAL
     * */
    public void defineLog4jThreshold(String level)
    {
    	if (Level.toLevel(level).toString().equalsIgnoreCase(level)){
    		
    		Appender apd = logger.getParent().getAppender("FILE-BATCH");
            ((DailyRollingFileAppender)apd).setThreshold(Level.toLevel(level));
    		
            
            /*Enumeration loggerEnum = LogManager.getCurrentLoggers();
	    
    		if(null != loggerEnum)
    		{
    			while(loggerEnum.hasMoreElements())
    			{
	    			Logger tmpLogger = (Logger)loggerEnum.nextElement();
	
	    			if(null != tmpLogger)
	    			{
		    			Enumeration appenderEnume = tmpLogger.getAllAppenders();
		
		    			if(null != appenderEnume)
		    			{
		    				while(appenderEnume.hasMoreElements())
			    			{
		    					Appender appender = (Appender)appenderEnume.nextElement();
			
				    			if(null != appender && appender.getName().equals("FILE-BATCH"))
				    			{
				    				((DailyRollingFileAppender)appender).setThreshold(Level.toLevel(level));
				    			}
			    			}
		    			}
	    			}
    			}             
			}*/
    	}
    	else{
    		logger.warn("This level does not exist: [" + level + "]");
    	}
	}

    /**
     * insert TB_BATCH_RESULT<br>
     * 
     * @return Batch Result DAO
     * @throws Exception
     */
    public TbBatchResultInfo openBatchResult() throws BatchException {
        batchResultInfo = new TbBatchResultInfo();
        Connection conn = null;

        try
        {
        	String jobId = StringUtil.isEmpty(System.getProperty("jobId")) ? 
            		batchResultInfo.getJobIdDefault(): System.getProperty("jobId");
            String jobTime = StringUtil.isEmpty(System.getProperty("jobTime")) ? 
            		batchResultInfo.getJobTimeDefault(): System.getProperty("jobTime");
    		String memId = StringUtil.isEmpty(System.getProperty("memId")) ? 
            		batchResultInfo.getMemIdDefault(): System.getProperty("memId");
    		String recover = StringUtil.isEmpty(System.getProperty("recover")) ? 
            		batchResultInfo.getRecoverValueDefault(): System.getProperty("recover");
            		
            batchResultInfo.setJobId(jobId);
            batchResultInfo.setJobTime(jobTime);
            batchResultInfo.setProgramName(programName);
            batchResultInfo.setStartDate(ISODate.formatDate(new Date(), "yyyyMMdd"));
            batchResultInfo.setStartTime(ISODate.formatDate(new Date(), "HHmmss"));
            batchResultInfo.setWorkFlag(BATCH_RESULT_WORK_FLAG_START);
            batchResultInfo.setRcode(RCODE_0000);
            String parMon = StringUtil.pendingKey(DateUtil.getMonth(), 2);
            String parDay = StringUtil.pendingKey(DateUtil.getDay(), 2);
            batchResultInfo.setParMon(parMon);
            batchResultInfo.setParDay(parDay);
            batchResultInfo.setLinkControl(linkControl);
            batchResultInfo.setMemId(memId);
            batchResultInfo.setRecoverValue(recover);

            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);

            TbBatchResultMgr action = new TbBatchResultMgr(conn);
            action.insert(batchResultInfo);
            conn.commit();
        }
        catch (Exception e)
        {
            logger.error("openBatchResult: fail " + e.toString());
            throw new BatchException("openBatchResult: fail " + e.toString());
        }
        finally
        {
            DisposeUtil.close(conn);
        }

        return batchResultInfo;
    }

    /**
     * update TB_BATCH_RESULT<br>
     * 
     * @param errorDesc
     * @param oldInfo
     * @throws Exception
     */
    public void closeBatchResult(String errorDesc, TbBatchResultInfo oldInfo) throws BatchException {
        Connection conn = null;

        try
        {
            TbBatchResultInfo info = (TbBatchResultInfo) oldInfo.clone();
            info.setEndDate(ISODate.formatDate(new Date(), "yyyyMMdd"));
            info.setEndTime(ISODate.formatDate(new Date(), "HHmmss"));
            // workFlag != 9, 表示可正常結束
            if (!BATCH_RESULT_WORK_FLAG_FAIL.equals(info.getWorkFlag())) {
                info.setWorkFlag(BATCH_RESULT_WORK_FLAG_END);
            }
            else {
                logger.error("[programName:" + info.getProgramName() + ", rcode:" + rcode + ", errorDesc:" + errorDesc + "]");
            }
            info.setErrorDesc(errorDesc);
            info.setRcode(rcode);
            info.setLinkControl(linkControl);

            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);

            TbBatchResultMgr action = new TbBatchResultMgr(conn);
            action.update(info);
            conn.commit();
        }
        catch (Exception e)
        {
            logger.error("closeBatchResult: fail " + e.toString());
            throw new BatchException("closeBatchResult: fail " + e.toString());
        }
        finally
        {
            DisposeUtil.close(conn);
        }
    }

    /**
     * Set error description<br>
     * 
     * @param errorDesc
     */
    public void setErrorDesc(String errorDesc)
    {
        this.errorDesc = errorDesc;
    }

    /**
     * Get error description<br>
     * 
     * @return error description
     */
    public String getErrorDesc()
    {
        return this.errorDesc;
    }

    /**
     * rcode getter
     * @return rcode
     */
    public String getRcode() {
        return rcode;
    }

    /**
     * rcode setter
     * @param rcode rcode value
     */
    public void setRcode(String rcode) {
        this.rcode = rcode;
    }

    /**
     * batchResultInfo getter
     * @return TbBatchResultInfo object
     */
    public TbBatchResultInfo getBatchResultInfo() {
        return batchResultInfo;
    }

    /**
     * linkControl getter
     * @return linkControl
     */
    public String getLinkControl()
    {
        return linkControl;
    }

    /**
     * linkControl setter
     * @param linkControl linkControl
     */
    public void setLinkControl(String linkControl)
    {
        this.linkControl = linkControl;
    }

    /**
     * @param programName the programName to set
     */
    public void setProgramName(String programName)
    {
        this.programName = programName;
    }
}
