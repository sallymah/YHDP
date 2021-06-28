package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor;

import java.sql.Connection;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.impfiles.YhdpBlackListSettingDataGenerator;
import tw.com.hyweb.core.cp.common.impfiles.YhdpCardDataGenerator;
import tw.com.hyweb.core.cp.common.impfiles.YhdpCpDeliveryDataGenerator;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.perso.CgYhdpSsf;
import tw.com.hyweb.svc.yhdp.batch.framework.perso.HsmAdapterECB;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.YhdpPersoFeebackDataCheck;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.YhdpPersoFeebackDataGenerator;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.bean.YhdpPersoFeedBackBean;
import tw.com.hyweb.util.IFFUtils;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;

/**
 * JobRunner:將raw data byte[]封裝起來交給其他Thread呼叫,本身是Runnable所以已經將實做的
 * 方法封裝進來,用來處理raw data byte[]轉換成Context及時間的紀錄
 * 
 * @author user 
 */
public class CheckLineJobRunner implements Runnable
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(JobRunner.class);
    //private static final String WORKFLAG_PROCESSFAIL = "9";
    //private static final String WORKFLAG_PROCESSOK = "3";
    //private static final String WORKINGFLAG_PROCESSOK = "2";
    private byte[] header;
    private byte[] body;
    private Date timeJobInit;
    private Date timeJobExec;    
    IContextListener ctxListener;
    
    private ArrayList<String> sqlList;
    private int idx;
    private String dataLine;
    private TbInctlInfo inctlInfo;
    private YhdpPersoFeebackDataCheck check;
    private YhdpPersoFeebackDataGenerator dataGen;
	
	private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
	
    /**
     * Creates a new InnerJobRunner object.
     * @param lChannel - the channel receive the message
     * @param lHeader - raw data header
     * @param lBody - raw data body
     */
    public CheckLineJobRunner()
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
    public CheckLineJobRunner(YhdpPersoFeebackDataCheck check, YhdpPersoFeebackDataGenerator dataGen,
    		ArrayList<String> sqlList, int idx, String dataLine, TbInctlInfo inctlInfo)
    {
    	this.check = check;
    	this.dataGen = dataGen;
    	this.sqlList = sqlList;
    	this.idx = idx;
    	this.dataLine = dataLine;
    	this.inctlInfo = inctlInfo;
    }

    /**
     * It call connector process() method to handle message,
     * 用來處理raw data byte[]轉換成Context及時間的紀錄
     */
    public void run()
    {
    	//resultMap 紀錄解密後內容，如過程失敗則不記錄該idex內容，後續檢查踢ERROR
    	this.timeJobExec = new Date();
    
        try {
        	handleImpFileInfo();
        }
        catch (Exception e) {
            logger.error("handleImpFileInfo error:" + e.getMessage(), e);
        }
    }

    public void handleImpFileInfo() throws Exception {

    	Connection conn = null;
    	
    	try
        {
    		conn = DBService.getDBService().getConnection(BatchUtil.DBUSER_BATCH);
    		
    		if(!dataLine.equals("")) {
				/*HashMap<String, String> hm = IFFUtils.cacheRowData("IFF", dataLine);
				cardNo = hm.get("field02");
				expiryDate = hm.get("field02");*/
				
    			YhdpPersoFeedBackBean dataBean = IFFUtils.organizeRowData(dataLine);
				//index:0 = 第一筆
				check.checkDataLine(conn, idx, dataLine, inctlInfo, dataBean);
				
				if(check.getPersoInfo().getPersoType().equals("3") || check.getPersoInfo().getPersoType().equals("4")) 
				{
					sqlList.add(dataGen.addIffFeedbackDtlData(inctlInfo, dataBean));
				}
				
				if (check.getPersoInfo().getPersoType().equals("4")) {
					sqlList.add(updateTelcoCardDtl(dataBean));
				}
			}
			
        }
        catch (Exception e)
        {
        	logger.warn(Integer.toString(idx) + "error: " + e);
        }
    	finally{
    		ReleaseResource.releaseDB(conn);
    	}
    }
    
    private String updateTelcoCardDtl(YhdpPersoFeedBackBean dataBean) throws Exception 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("update TB_TELCO_CARD_DTL set");
		sb.append(" IFF_FILE_NAME='").append(inctlInfo.getFullFileName()).append("'");
		sb.append(", IFF_FILE_PROC_DATE='").append(dataGen.sysDate).append("'");
		sb.append(", IFF_FILE_PROC_TIME='").append(dataGen.sysTime).append("'");
		sb.append(", CARD_NO='").append(dataBean.getCardNo()).append("'");
		
		String utExpiryDate = dataBean.getExpiryDate();
		long hexUtExpiryDate = Long.parseLong(utExpiryDate, 16);
		String expiryDate = new SimpleDateFormat("yyyyMMdd").format(new Date(Long.valueOf(hexUtExpiryDate) * 1000L));
		sb.append(", EXPIRY_DATE='").append(expiryDate).append("'");
		
		sb.append(" where IMP_TELCO_FILE_NAME='").append(check.getPersoInfo().getImpTelcoFileName()).append("'");
		sb.append(" and MIFARE_UL_UID='").append(dataBean.getMifareId()).append("'");
		sb.append(" and IFF_FILE_NAME is null");
		
		String sqlCmd = sb.toString();
		
		return sqlCmd.toString();
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
        final CheckLineJobRunner other = (CheckLineJobRunner) obj;
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
}
