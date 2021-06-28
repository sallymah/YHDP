package tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedback.JobExecutor;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.impfiles.YhdpBlackListSettingDataGenerator;
import tw.com.hyweb.core.cp.common.impfiles.YhdpCardDataGenerator;
import tw.com.hyweb.core.cp.common.impfiles.YhdpCpDeliveryDataGenerator;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.util.IFFUtils;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;

/**
 * JobRunner:將raw data byte[]封裝起來交給其他Thread呼叫,本身是Runnable所以已經將實做的
 * 方法封裝進來,用來處理raw data byte[]轉換成Context及時間的紀錄
 * 
 * @author user 
 */
public class JobRunner implements Runnable
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(JobRunner.class);
    private static final String WORKFLAG_PROCESSFAIL = "9";
    private static final String WORKFLAG_PROCESSOK = "3";
    private static final String WORKINGFLAG_PROCESSOK = "2";
    private byte[] header;
    private byte[] body;
    private Date timeJobInit;
    private Date timeJobExec;    
    IContextListener ctxListener;
    
	private HashMap<String,String> resultMap;
	private TbInctlInfo inctlInfo;
	private TbPersoInfo persoInfo;
	private List hms;
	private int idx;
	
	private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
	
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
    public JobRunner(HashMap<String,String> resultMap, int idx, TbInctlInfo inctlInfo, TbPersoInfo persoInfo, List hms)
    {
    	this.resultMap = resultMap;
    	this.inctlInfo = inctlInfo;
    	this.persoInfo = persoInfo;
    	this.hms = hms;
    	this.idx = idx;
    }

    /**
     * It call connector process() method to handle message,
     * 用來處理raw data byte[]轉換成Context及時間的紀錄
     */
    public void run()
    {
    	this.timeJobExec = new Date();
    
    	String key = inctlInfo.getFullFileName() + "_" + idx;
    	
        try {
            //logger.debug( job.toString() + " is begin working");
            // handle impFileInfo
        	resultMap.put(key,WORKINGFLAG_PROCESSOK);
            handleImpFileInfo();
            resultMap.put(key,WORKFLAG_PROCESSOK);
        }
        catch (Exception e) {
            logger.error("handleImpFileInfo error:" + e.getMessage(), e);
            resultMap.put(key,WORKFLAG_PROCESSFAIL);
        }
        String result = resultMap.get(key);
        if(result == null || result.length() == 0)
        {
        	resultMap.put(key,WORKFLAG_PROCESSFAIL);
        }
    }

    public void handleImpFileInfo() throws Exception {
    	
    	Connection connection = null;
    	connection = DBService.getDBService().getConnection(BatchUtil.DBUSER_BATCH);
    	
    	Savepoint savepoint = connection.setSavepoint();
        try
        {
        	for( int i = 0; i < hms.size(); i++ ){
        		byte[] dtlBuffer = (byte[]) hms.get(i);
        		HashMap<String, String> hm = IFFUtils.cacheRowData(inctlInfo.getFileName(), ISOUtil.hexString(dtlBuffer));
        		handleDataLine(connection, hm, persoInfo, inctlInfo);
        	}
//        	connection.commit();
        }
        catch (Exception e)
        {
        	logger.warn(inctlInfo.getFullFileName() + "_" + idx + "error: " + e);
            connection.rollback(savepoint);
        }
        finally
        {
        	ReleaseResource.releaseDB(connection);
        }
    }
    
    public void handleDataLine(Connection conn, HashMap<String, String> hm, TbPersoInfo persoInfo, TbInctlInfo inctlInfo) throws Exception
    {		  	
    	//一代卡
    	if(persoInfo.getChipVersion().equals("1")) 
    	{		
    		//測試卡
    		/*if(persoInfo.getIsTest().equals("1")) {
    			new YhdpBlackListSettingDataGenerator().makeBlackListSetgingData(conn, sysDate, sysTime, hm.get("field02"), persoInfo);
    		}*/
			new YhdpCardDataGenerator().updateCardData(conn, hm, persoInfo, inctlInfo);	
            new YhdpCpDeliveryDataGenerator().updateCpDelivery(conn, hm, persoInfo, inctlInfo);     
    	}
    	//二代卡
    	else 
    	{	
    		//測試卡
    		if(persoInfo.getIsTest().equals("1")) {
    			new YhdpBlackListSettingDataGenerator().makeBlackListSetgingData(conn, sysDate, sysTime, hm.get("field02"), persoInfo);
    		}
        	new YhdpCardDataGenerator().makeCardData(conn, sysDate, hm, persoInfo, inctlInfo);
        	new YhdpCpDeliveryDataGenerator().makeCpDeliveryData(conn, sysDate, hm, persoInfo, inctlInfo);
        	updateHgCardMap(conn, hm);
    	}
    	conn.commit();
    }
    
    private void updateHgCardMap(Connection conn, HashMap<String, String> hm) throws Exception 
    {
    	String cardNo = hm.get("field02");
    	logger.info("2.儲值檔識別碼(PID): " + cardNo);
    	
    	String hexBarcode1 = hm.get("field120");
		String barcode1 = IFFUtils.hex2ascii(hexBarcode1);
		logger.info("barcode1: " + barcode1);
    	
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE TB_HG_CARD_MAP set");
		sql.append(" CARD_NO='").append(cardNo).append("'");
		sql.append(", STATUS='").append("1").append("'");
		sql.append(" WHERE BARCODE1='").append(barcode1).append("'");
		
		DBService.getDBService().sqlAction(sql.toString(), conn, false);
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

	public HashMap<String, String> getResultMap() {
		return resultMap;
	}

	public void setResultMap(HashMap<String, String> resultMap) {
		this.resultMap = resultMap;
	}
    
}
