/*
 * $Id: SimulateAppointReloadJob.java 3650 2010-09-01 02:40:34Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.common.impfiles;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedback.JobExecutor.JobRunner;
import tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedback.JobExecutor.JobRunnerFactory;
import tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedback.JobExecutor.ThreadPoolJobExecutor;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.MappingInfo;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbFileInfoPK;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbPersoFactoryInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbInctlMgr;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;
import tw.com.hyweb.util.IFFUtils;
import tw.com.hyweb.util.ISOUtil;


/**
 * @author Kevin
 * @version $Revision: 3650 $
 */
public class ImpPersoFeedbackJob extends GenericBatchJob
{
    private static final Logger LOGGER = Logger.getLogger(ImpPersoFeedbackJob.class);
    
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
	private final TbInctlInfo inctlInfo;
	//private final int HSM_MAX_LENGTH = 65512;
	private final int DATA_MAX_LENGTH = 5888;
    private final String PERSO_FACTORY_RREMOTE = "PERSO_FACTORY";
    private final String MEMGROUPID_SPECIAL = "22222";
    private final String MEMID_SPECIAL = "00000000";

    private BufferedInputStream bis = null;
    //private BufferedOutputStream bw = null;
    //private BufferedInputStream br = null;
    
    protected String fileName = "IFF";
	protected MappingInfo mappingInfo = null;
    // mapping config filename
    protected String configFilename = "config/batch/MappingInfos.xml";
    protected String IFF_BANK = "IFF_BANK";
    // mapping config filename encoding
    protected String encoding = "UTF-8";
    
//    private String file;
    private String tmpFile;
    
    private boolean threadFlag;
    private String absolutePath;
    
    private int sucCnt;
    private int failCnt;
    private int totCnt;
    
    private ThreadPoolJobExecutor threadPoolJobExecutor = null;
    private JobRunnerFactory jobRunnerFactory = new JobRunnerFactory();
    private HashMap<String,String> resultMap = new HashMap<String,String>();
    
    
	/**
	 * @param absolutePath 
	 * @param terminalBatch
	 */
	public ImpPersoFeedbackJob(String absolutePath, TbInctlInfo info, boolean threadFlag) 
	{
		this.threadFlag = threadFlag;
		this.inctlInfo = info;
		this.absolutePath = absolutePath;
		
		LOGGER.info(info);
	}   

	/* (non-Javadoc)
	 * @see tw.com.hyweb.core.cp.batch.preoperation.SimulateOnlineJob#preoperation(java.sql.Connection, java.lang.String)
	 */
	protected void preparationn(Connection connection, TbInctlInfo inctlInfo) throws Exception 
	{
		sucCnt=0;
		failCnt=0;
    	totCnt=0;
    	
//    	file="";
    	tmpFile="";
	
    	TbPersoFactoryMgr mgr = new TbPersoFactoryMgr(connection);
    	TbPersoFactoryInfo persoFactoryInfo =  mgr.querySingle(inctlInfo.getFullFileName().substring(3, 5));
    	if(persoFactoryInfo == null && !inctlInfo.getFileName().equals(IFF_BANK)) {
    		throw new Exception("it has no setting in TB_PERSO_FACTORY: " + inctlInfo.getFullFileName().substring(3, 5));
    	}
    	
    	TbFileInfoInfo fileInfo = getFileInfo(connection, inctlInfo.getFileName());
    	//UAT需改為IN_OUT='W'
    	if(fileInfo == null) throw new Exception("TB_FILE_INFO(WHERE FILE_NAME='"+inctlInfo.getFileName()+"' AND IN_OUT='W') NO DATA FOUND");
    	
    	String LocalPath = fileInfo.getLocalPath();
    	if (LocalPath.contains(PERSO_FACTORY_RREMOTE)) {
    		LocalPath = LocalPath.replaceFirst(PERSO_FACTORY_RREMOTE, persoFactoryInfo.getRemoteFolder());
    	}
    	else if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1 || fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1){
        	
	        if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1) {
	            // find "22222"
	        	LocalPath = LocalPath.replaceFirst(MEMGROUPID_SPECIAL, inctlInfo.getMemGroupId());
	        }
	        if (fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1) {
	            // find "00000000"
	        	LocalPath = LocalPath.replaceFirst(MEMID_SPECIAL, inctlInfo.getMemId());
	        }
        }
    	
    	String filePath = absolutePath+ "/" + LocalPath + "/";
    	/*String filePath = FilenameUtils.separatorsToSystem(BatchUtil.getWorkDirectory() + LocalPath + "/");

        String serialNumber = getSerialNumber(connection, batchDate, inctlInfo.getMemId());
        String isamNumber = "01";
        fullFileName = getPersoFileName(fileInfo, inctlInfo);*/
        
//        this.file = filePath + inctlInfo.getFullFileName();
//        LOGGER.info("fn: " + file);
        
        this.tmpFile = filePath + inctlInfo.getFullFileName() + ".tmp";
        LOGGER.info("fn: " + tmpFile);
        //insertInControl(connection, batchDate, persoInfo.getMemId(), serialNumber,persoInfo.getPersoQty().intValue());
	}
	
	private TbFileInfoInfo getFileInfo(Connection connection, String fileName) throws SQLException
    {
        TbFileInfoPK pk = new TbFileInfoPK();
        pk.setFileName(fileName);
        pk.setInOut("W");

        return new TbFileInfoMgr(connection).querySingle(pk);
    }
	
    @Override
    public void action(Connection conn, String batchDate) throws Exception
    {
    	preparationn(conn, inctlInfo);
    	
    	TbPersoInfo persoInfo = PreInspection(conn, inctlInfo);

    	if (threadFlag){
    		processThread(inctlInfo, persoInfo);
    	}
    	else{
    		process(conn, inctlInfo, persoInfo);
    	}
    }
    
    /*public boolean descryptProcess() throws Exception 
    {
    	boolean result = false;
		String enString1 = null;
		String enString2 = null;
		
		br = new BufferedInputStream(new FileInputStream(new File(file)));
        bw = new BufferedOutputStream(new FileOutputStream(new File(tmpFile)));
	
		int times = br.available() / HSM_MAX_LENGTH;
		LOGGER.info("times: " + times);
		int bytes = br.available() % HSM_MAX_LENGTH;
		LOGGER.info("bytes: " + bytes);
		
		byte[] buffer1 = new byte[HSM_MAX_LENGTH];
		byte[] buffer2 = new byte[bytes];

		if(times >0) {
			for(int i=0; i<times; i++)
			{
				br.read(buffer1);
				if(bytes == 0) {
					enString1 = descryptECB(ISOUtil.hexString(buffer1));
				}
				else {
					enString1 = descryptECB(ISOUtil.hexString(buffer1));
				}
				
				LOGGER.info("encrypt String1: " + enString1);
				bw.write(ISOUtil.hex2byte(enString1));
			}
		}
		
		if(bytes != 0) {
			br.read(buffer2);
			enString2 = descryptECB(ISOUtil.hexString(buffer2));
			LOGGER.info("encrypt String2: " + enString2);
			bw.write(ISOUtil.hex2byte(enString2));
		}	
		bw.flush();
		br.close();
		bw.close();
		
		return result;
	}*/
    
    /*private String descryptECB(String inputData) throws Exception
	{
    	LOGGER.info("Hsm ip: " + System.getenv("ET_HSM_NETCLIENT_SERVERLIST"));
	   	String encString = "";
	   	HsmAdapter ham = new HsmAdapter(HsmService.getHsmService());
	   	
	   	LOGGER.info("KeyId: " + pinKeyId);
	   	LOGGER.info("Iv: " + iv);
		HsmResult hsmR = ham.CryptoDES3(HsmInterface.DES_DECRYPT_ECB, pinKeyId, inputData, iv);
		if (null != hsmR && hsmR.getValue() == 0)
		{
			encString = hsmR.getString(0);
			LOGGER.debug("encString = " + encString);
		}
		else
		{
			//log.error("HSM process error: " + hsmR.getValue());
			throw new Exception("HSM process error: " + hsmR.getValue());
		}
	   	return encString;
	}*/
    
    private TbPersoInfo PreInspection(Connection conn, TbInctlInfo inctlInfo) throws Exception 
	{		
    	bis = new BufferedInputStream(new FileInputStream(new File(tmpFile)));
    	
    	//bis.skip(1*64);
		byte[] headBuffer = new byte[64];
		bis.read(headBuffer);
		String hexPersoBatchNo = ISOUtil.hexString(headBuffer).substring(36, 72);	
    	String persoBatchNo = IFFUtils.hex2ascii(hexPersoBatchNo);
    	LOGGER.info("persoBatchNo: " + persoBatchNo);
    	
    	TbPersoInfo persoInfo = new TbPersoMgr(conn).querySingle(persoBatchNo);
    	
    	if(persoInfo == null) {
    		throw new Exception("perso batch no is not exist from tb_perso: " + persoBatchNo);
    	}
    	
		return persoInfo;
    	
    }
    
    private void process(Connection conn, TbInctlInfo inctlInfo, TbPersoInfo persoInfo) throws Exception 
	{		
    	int times = bis.available() / DATA_MAX_LENGTH;
    	totCnt = times;
    	LOGGER.info("available: " + bis.available());
    	LOGGER.info("times: " + times);
    	
    	byte[] dtlBuffer = new byte[DATA_MAX_LENGTH];
    	
    	for(int idx=0; idx<times; idx++)
    	{
    		bis.read(dtlBuffer);
    		
    		LOGGER.info(ISOUtil.hexString(dtlBuffer));
    		HashMap<String, String> hm = IFFUtils.cacheRowData(fileName, ISOUtil.hexString(dtlBuffer));
    		
    		handleDataLine(conn, hm, persoInfo, inctlInfo);
			sucCnt++;
    	}
    	bis.close();
	}
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "impfiles" + File.separator + "ImpPersoFeedback" + File.separator + "beans-config.xml";
    
    private void processThread(TbInctlInfo inctlInfo, TbPersoInfo persoInfo) throws Exception 
	{		
    	ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
    	ThreadPoolJobExecutor instance = (ThreadPoolJobExecutor) apContext.getBean("threadPoolJobExecutor");
    	setThreadPoolJobExecutor(instance);
    	
    	bis = new BufferedInputStream(new FileInputStream(new File(tmpFile)));
    	
    	//bis.skip(1*64);
		byte[] headBuffer = new byte[64];
		bis.read(headBuffer);
		String hexPersoBatchNo = ISOUtil.hexString(headBuffer).substring(36, 72);	
    	String persoBatchNo = IFFUtils.hex2ascii(hexPersoBatchNo);
    	LOGGER.info("persoBatchNo: " + persoBatchNo);
    	
    	int times = bis.available() / DATA_MAX_LENGTH;
    	totCnt = times;
    	LOGGER.info("available: " + bis.available());
    	LOGGER.info("times: " + times);
    	
    	byte[] dtlBuffer = new byte[DATA_MAX_LENGTH];
    	
    	
    	getThreadPoolJobExecutor().startThreadPoolJob();
    	
    	List hms = new ArrayList<>();
    	
    	for(int idx=0; idx<times; idx++)
    	{
    		bis.read(dtlBuffer);
    		
    		byte[] handleData = dtlBuffer.clone();
    		
    		hms.add(handleData);
    		
    		if(getThreadPoolJobExecutor() != null && getThreadPoolJobExecutor().getExecutor() != null 
    				&& ((idx%100 == 0) || (idx ==times-1)))
	        {
    			if (resultMap != null){
	    			ThreadPoolExecutor threadPoolExecutor = getThreadPoolJobExecutor().getExecutor();
	                
	                JobRunner jobRunnerExecute = jobRunnerFactory.create(resultMap, idx, inctlInfo, persoInfo, new ArrayList<>(hms));
	                
	                threadPoolExecutor.execute(jobRunnerExecute);
	                
	                hms.clear();
    			}
	        }

			sucCnt++;
    	}
    	bis.close();
    	
    	while (getThreadPoolJobExecutor().getExecutor().getActiveCount() != 0) {
    		Thread.sleep(2000);
    		LOGGER.debug("ActiveCount:"+getThreadPoolJobExecutor().getExecutor().getActiveCount()
    				+" CompletedCount:"+getThreadPoolJobExecutor().getExecutor().getCompletedTaskCount());
		}
    	
    	if(!getThreadPoolJobExecutor().getExecutor().isShutdown()){
	    	getThreadPoolJobExecutor().getExecutor().shutdown();
			LOGGER.info("threadPoolJobExecutor shutdown!!");
    	}
	}
    
    /*private void process(Connection conn, TbInctlInfo inctlInfo) throws Exception 
	{
		String rcode = Constants.RCODE_0000_OK;
		
		MappingLoader ml = new MappingLoader();
        ml.setConfigFilename(configFilename);
        ml.setFile(new File(configFilename));
        ml.setEncoding(encoding);
        ml.startLoading();
        mappingInfo = ml.getMappingInfo(fileName);
        if (mappingInfo == null) {
            throw new Exception("mappingInfo(" + fileName + ") is null!");
        }
		
		if(descryptFlag) {
			bis = new BufferedInputStream(new FileInputStream(new File(tmpFile)));
		}
		else {
			bis = new BufferedInputStream(new FileInputStream(new File(file)));
		}
    	
    	bis.skip(1*64);
    	
    	int times = bis.available() / DATA_MAX_LENGTH;
    	totCnt = times;
    	LOGGER.info("available: " + bis.available());
    	LOGGER.info("times: " + times);
    	
    	byte[] buffer = new byte[DATA_MAX_LENGTH];
    	
    	for(int idx=0; idx<times; idx++)
    	{
    		bis.read(buffer);
    		
    		LOGGER.info(ISOUtil.hexString(buffer));
    		HashMap<String, String> hm = cachFieldValue(ISOUtil.hexString(buffer));
    		
    		handleDataLine(conn, hm);
			sucCnt++;
    		
    		rcode = chechDataLine(conn, idx, ISOUtil.hexString(buffer), hm, inctlInfo);
    		if(rcode.equals(Constants.RCODE_0000_OK)) {
    			handleDataLine(conn, hm);
    			sucCnt++;
    		}
    		else {
    			throw new BatchException("invalid error !");
    		}
    		
    		rcode = chechDataLine(conn, idx, ISOUtil.hexString(buffer), inctlInfo);
    		if(rcode.equals(Constants.RCODE_0000_OK)) {
    			handleDataLine(conn, ISOUtil.hexString(buffer));
    			sucCnt++;
    		}
    		else {
    			throw new BatchException("invalid error !");
    		}
    	}
    	bis.close();
	}*/
    
    /*private HashMap<String, String> cachFieldValue(String dataLine) 
    {
    	int startIdx = 0;
	    int endIdx = 0;
	    
	    HashMap<String, String> hm = new HashMap<String, String>();
    	for(int i=0; i< mappingInfo.getFields().size(); i++)
    	{
	        startIdx = ((FieldInfo)mappingInfo.getFields().get(i)).getStart();  
	        endIdx = startIdx + ((FieldInfo)mappingInfo.getFields().get(i)).getLength();
	        
	        hm.put(((FieldInfo)mappingInfo.getFields().get(i)).getName(), dataLine.substring(startIdx, endIdx)); 
    	}
    	return hm;
	}*/

	/*private String chechDataLine(Connection conn, int idx, String dataLine, HashMap<String, String> hm, TbInctlInfo inctlInfo) throws Exception 
	{
		String cardNo = hm.get("field02");
		LOGGER.info("cardNo: " + cardNo);
		
		//發卡單位編號1
		String issNo = hm.get("field18");
		
		//發卡設備編號2
		String issEquipmentNo = hm.get("field19");
		
		//發行批號2
		String persoBatchNo = hm.get("field20");
		
		//發出日期4
		String activeDate = hm.get("field21");
		
		//有效日期4
		String expiryDate = hm.get("field22");
		
		//卡片格式版本1
		String cardVersion = hm.get("field23");
		
		//保留(卡片狀態:未初始化、初始化、個人化、鎖卡、停卡)1
		String cardStatus = hm.get("field24");
		
		//檢查碼1
		String checkCode1 = hm.get("field25");
		LOGGER.info("檢查碼1: " + checkCode1);
		
		//自動加值設定1
		String autoReloadFlag = hm.get("field26");
		
		//自動加值票值數額2
		String autoReloadValue = hm.get("field27");
		
		//儲存最大票值數額(餘額上限)2
		String balMaxbal = hm.get("field28");
		
		//每筆可扣減最大票值數額2
		String minBalAmt8 = hm.get("field29");
		
		//指定加值設定1
		String appointReloadFlag = hm.get("field30");
		
		//指定加值票值數額2
		String appointReloadValue = hm.get("field31");
		
		//自動加值日期2
		String autoReloadDate = hm.get("field32");
		
		//連續離線自動加值次數上限1
		String olAutoReloadMaxTime = hm.get("field33");
		
		//連續自動加值次數上限1
		String autoReloadMaxTime = hm.get("field34");
				
		//連續指定加值次數上限1
		String appointReloadMaxTime = hm.get("field35");
		
		//檢查碼2
		String checkCode2 = hm.get("field36");
		LOGGER.info("檢查碼2: " + checkCode2);
		
		String checkOrigData1=issNo + issEquipmentNo + persoBatchNo + activeDate + expiryDate + cardVersion + cardStatus;
		LOGGER.info("Before XOR: [" + checkOrigData1 +"]");

		String checkOrigData2=autoReloadFlag + autoReloadValue + balMaxbal + minBalAmt8 + appointReloadFlag + appointReloadValue + autoReloadDate + olAutoReloadMaxTime + autoReloadMaxTime + appointReloadMaxTime;
		LOGGER.info("Before XOR: [" + checkOrigData2 +"]");
		
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		
		TbCardInfo info = new TbCardInfo();
        info.setCardNo(cardNo);
        TbCardMgr mgr = new TbCardMgr(conn);
        int count = mgr.queryMultiple(info, result);  
        
        if(count <= 0) {
        	//addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, "CARD_NO" ,"cardInfo is null: " + cardNo);
        	LOGGER.error("cardInfo is null: " + cardNo);
        	insertInctlErrInfo(conn, idx, dataLine, inctlInfo, Constants.RCODE_2710_INVALID_ERR,"cardInfo is null: " + cardNo);
        	failCnt++;
        	return Constants.RCODE_2710_INVALID_ERR;
        }
        
        lifeCycle = result.get(0).getLifeCycle();
        if(!lifeCycle.equals("")) {
        	//addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, "LIFE_CYCLE", "card life cycle is not valid: " + cardNo);
        	log.error("card life cycle is not valid:  " + cardNo);
        	insertInctlErrInfo(idx, lineInfo, inctlInfo, Constants.RCODE_2710_INVALID_ERR, "life cycle is not valid: " + cardNo);
        	failCnt++;
        	return false;
        }
        
        byte[] tmpByte = new byte[1];
    	tmpByte[0] = XorData(checkOrigData1);
    	String countCeckCode1 = ISOUtil.hexString(tmpByte);
    	LOGGER.info("檢查碼: [" + countCeckCode1 + "]");
    	
    	if(!checkCode1.equals(countCeckCode1)) {
    		LOGGER.error("checkcode1: [" + checkCode1 + "], not match count checkcode1: [" + countCeckCode1 + "]");
    		insertInctlErrInfo(conn, idx, dataLine, inctlInfo, 
    				Constants.RCODE_2710_INVALID_ERR, "checkcode1: [" + checkCode1 + "], not match count checkcode1: [" + countCeckCode1 + "]");
    		failCnt++;
    		return Constants.RCODE_2710_INVALID_ERR;
    	}
		
		byte[] tmpByte2 = new byte[1];
		tmpByte2[0] = XorData(checkOrigData2);
		String countCeckCode2 = ISOUtil.hexString(tmpByte2);
		LOGGER.info("檢查碼: [" + countCeckCode2 + "]");
    	
    	if(!checkCode2.equals(countCeckCode2)) {
    		LOGGER.error("checkCode2: [" + checkCode2 + "], not match count checkcode2: [" + countCeckCode2 + "]");
    		insertInctlErrInfo(conn, idx, dataLine, inctlInfo,
    				Constants.RCODE_2710_INVALID_ERR, "checkCode2: [" + checkCode2 + "], not match count checkcode2: [" + countCeckCode2 + "]");
    		failCnt++;
    		return Constants.RCODE_2710_INVALID_ERR;
    	}
    	return Constants.RCODE_0000_OK;
	}*/
    
    /*private String chechDataLine(Connection conn, int idx, String lineInfo, TbInctlInfo inctlInfo) throws Exception 
	{
		String cardNo = lineInfo.substring(4,20);
		LOGGER.info("cardNo: " + cardNo);
		
		String minBalAmt6 = lineInfo.substring(176,178);
		
		String minBalAmt7 = lineInfo.substring(178,182);
		
		String persoBatchNo = lineInfo.substring(182,186);
		
		String activeDate = lineInfo.substring(186,194);
		
		String expiryDate = lineInfo.substring(194,202);
		
		String cardVersion = lineInfo.substring(202,204);
		
		String cardStatus = lineInfo.substring(204,206);
		
		String checkCode1 = lineInfo.substring(206,208);
		LOGGER.info("檢查碼1: " + checkCode1);
		
		String autoReloadFlag = lineInfo.substring(208,210);
		
		String autoReloadValue = lineInfo.substring(210,214);
		
		String balMaxbal = lineInfo.substring(214,218);
		
		String minBalAmt8 = lineInfo.substring(218,222);
		
		String tmp = lineInfo.substring(222,224);
		
		String tmp2 = lineInfo.substring(224,228);
		
		String tmp3 = lineInfo.substring(228,238);
		
		String checkCode2 = lineInfo.substring(238,240);
		LOGGER.info("檢查碼2: " + checkCode2);
		
		String checkOrigData1=minBalAmt6 + minBalAmt7 + persoBatchNo + activeDate + expiryDate + cardVersion + cardStatus;
		LOGGER.info("Before XOR: [" + checkOrigData1 +"]");

		String checkOrigData2=autoReloadFlag + autoReloadValue + balMaxbal + minBalAmt8 + tmp + tmp2 + tmp3;
		LOGGER.info("Before XOR: [" + checkOrigData2 +"]");
		
		Vector<TbCardInfo> result = new Vector<TbCardInfo>();
		
		TbCardInfo info = new TbCardInfo();
        info.setCardNo(cardNo);
        TbCardMgr mgr = new TbCardMgr(conn);
        int count = mgr.queryMultiple(info, result);  
        
        if(count <= 0) {
        	//addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, "CARD_NO" ,"cardInfo is null: " + cardNo);
        	LOGGER.error("cardInfo is null: " + cardNo);
        	insertInctlErrInfo(conn, idx, lineInfo, inctlInfo, Constants.RCODE_2710_INVALID_ERR,"cardInfo is null: " + cardNo);
        	failCnt++;
        	return Constants.RCODE_2710_INVALID_ERR;
        }
        
        lifeCycle = result.get(0).getLifeCycle();
        if(!lifeCycle.equals("")) {
        	//addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, "LIFE_CYCLE", "card life cycle is not valid: " + cardNo);
        	log.error("card life cycle is not valid:  " + cardNo);
        	insertInctlErrInfo(idx, lineInfo, inctlInfo, Constants.RCODE_2710_INVALID_ERR, "life cycle is not valid: " + cardNo);
        	failCnt++;
        	return false;
        }
        
        byte[] tmpByte = new byte[1];
    	tmpByte[0] = XorData(checkOrigData1);
    	String countCeckCode1 = ISOUtil.hexString(tmpByte);
    	LOGGER.info("檢查碼: [" + countCeckCode1 + "]");
    	
    	if(!checkCode1.equals(countCeckCode1)) {
    		LOGGER.error("checkcode1: [" + checkCode1 + "], not match count checkcode1: [" + countCeckCode1 + "]");
    		insertInctlErrInfo(conn, idx, lineInfo, inctlInfo, 
    				Constants.RCODE_2710_INVALID_ERR, "checkcode1: [" + checkCode1 + "], not match count checkcode1: [" + countCeckCode1 + "]");
    		failCnt++;
    		return Constants.RCODE_2710_INVALID_ERR;
    	}
		
		byte[] tmpByte2 = new byte[1];
		tmpByte2[0] = XorData(checkOrigData2);
		String countCeckCode2 = ISOUtil.hexString(tmpByte2);
		LOGGER.info("檢查碼: [" + countCeckCode2 + "]");
    	
    	if(!checkCode2.equals(countCeckCode2)) {
    		LOGGER.error("checkCode2: [" + checkCode2 + "], not match count checkcode2: [" + countCeckCode2 + "]");
    		insertInctlErrInfo(conn, idx, lineInfo, inctlInfo,
    				Constants.RCODE_2710_INVALID_ERR, "checkCode2: [" + checkCode2 + "], not match count checkcode2: [" + countCeckCode2 + "]");
    		failCnt++;
    		return Constants.RCODE_2710_INVALID_ERR;
    	}
    	return Constants.RCODE_0000_OK;
	}*/
    
    /*public byte XorData(String data)
    {
    	if(data.length() % 2 != 0) {
    		data += "F";
    	}
    	
    	byte[] bytes = ISOUtil.hex2byte(data);
		byte tmpByte = 0;
		
		for(int b=0; b<bytes.length; b++)
		{
			if(b==0) {
				tmpByte = bytes[b];
				continue;
			}
			tmpByte ^= bytes[b];
		}
		return tmpByte;
    }*/
    
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
    }
    
    /*public void handleDataLine(Connection conn, String lineInfo) throws Exception
    {	
		String headStr = lineInfo.substring(0,4);
		LOGGER.info("1.檔頭標籤: " + headStr);
		
		String cardNo = lineInfo.substring(4,20);
		LOGGER.info("2.儲值檔識別碼(PID): " + cardNo);
		
		String balMaxAmt = lineInfo.substring(20,36);
		LOGGER.info("3.餘額上限 (BIL): " + balMaxAmt);
		
		String minBalAmt = lineInfo.substring(36,46);
		LOGGER.info("4.餘額下限 (BIL): " + minBalAmt);
		
		String minBalAmt1= lineInfo.substring(46,54);
		LOGGER.info("5.卡片保固期: " + minBalAmt1);
		
		String minBalAmt2 = lineInfo.substring(54,56);
		LOGGER.info("6.首次消費: " + minBalAmt2);
		
		String minBalAmt3 = lineInfo.substring(56,72);
		LOGGER.info("7.累積離線消費金額上限: " + minBalAmt3);
		
		String minBalAmt4 = lineInfo.substring(72,74);
		LOGGER.info("8.累積離線消費次數上限: " + minBalAmt4);
		
		String minBalAmt5 = lineInfo.substring(74,76);
		LOGGER.info("9.連續離線自動加值次數上限: " + minBalAmt5);
		
		String autoReloadMax = lineInfo.substring(76,78);
		LOGGER.info("10.連續自動加值次數上限: " + autoReloadMax);
		
		String appointReloadMax = lineInfo.substring(78,80);
		LOGGER.info("11.連續指定加值次數上限: " + appointReloadMax);
		
		String mifareId = lineInfo.substring(80,88);
		LOGGER.info("12.Mifare晶片序號: " + mifareId);
		
		String maufacturerCode = lineInfo.substring(88,112);
		LOGGER.info("13.廠商批號Maufacturer code: " + maufacturerCode);
		
		String firstAidGroup = lineInfo.substring(112,144);
		LOGGER.info("14.目錄服務指標(1): " + firstAidGroup);
		
		String secondAidGroup = lineInfo.substring(144,176);
		LOGGER.info("15.目錄服務指標(2): " + secondAidGroup);
		
		String minBalAmt6 = lineInfo.substring(176,178);
		LOGGER.info("16.發卡單位編號: " + minBalAmt6);
		
		String minBalAmt7 = lineInfo.substring(178,182);
		LOGGER.info("17.發卡設備編號: " + minBalAmt7);
		
		String persoBatchNo = lineInfo.substring(182,186);
		LOGGER.info("18.發行批號: " + persoBatchNo);
		
		String activeDate = lineInfo.substring(186,194);
		LOGGER.info("19.發出日期: " + activeDate);
		
		String expiryDate = lineInfo.substring(194,202);
		LOGGER.info("20.有效日期: " + expiryDate);
		
		String cardVersion = lineInfo.substring(202,204);
		LOGGER.info("21.卡片格式版本: " + cardVersion);
		
		String cardStatus = lineInfo.substring(204,206);
		LOGGER.info("22.保留(卡片狀態:未初始化、初始化、個人化、鎖卡、停卡): " + cardStatus);
		
		String checkCode1 = lineInfo.substring(206,208);
		LOGGER.info("23.檢查碼: " + checkCode1);
		
		String autoReloadFlag = lineInfo.substring(208,210);
		LOGGER.info("24.自動加值設定: " + autoReloadFlag);
		
		String autoReloadValue = lineInfo.substring(210,214);
		LOGGER.info("25.自動加值票值數額: " + autoReloadValue);
		
		String balMaxbal = lineInfo.substring(214,218);
		LOGGER.info("26.儲存最大票值數額(餘額上限): " + balMaxbal);
		
		String minBalAmt8 = lineInfo.substring(218,222);
		LOGGER.info("27.每筆可扣減最大票值數額: " + minBalAmt8);
		
		String tmp = lineInfo.substring(222,224);
		LOGGER.info("28.保留: " + tmp);
		
		String tmp2 = lineInfo.substring(224,228);
		LOGGER.info("29.保留: " + tmp2);
		
		String tmp3 = lineInfo.substring(228,238);
		LOGGER.info("30.保留: " + tmp3);
		
		String checkCode2 = lineInfo.substring(238,240);
		LOGGER.info("31.檢查碼: " + checkCode2);
		
		String minBalAmt9 = lineInfo.substring(240,272);
		LOGGER.info("32.防偽驗證資料: " + minBalAmt9);
		
		String useType = lineInfo.substring(272,274);
		LOGGER.info("33.使用者型態(卡種): " + useType);
		
		String useExpiry = lineInfo.substring(274,282);
		LOGGER.info("34.使用者截止日期: " + useExpiry);
		
		String userId = lineInfo.substring(282,294);
		LOGGER.info("35.使用者序號User id: " + userId);
		
		String minBalAmt10 = lineInfo.substring(294,296);
		LOGGER.info("36.發卡企業編號: " + minBalAmt10);
		
		String tmp4 = lineInfo.substring(296,300);
		LOGGER.info("37.保留: " + tmp4);
		
		String cardFee = lineInfo.substring(300,304);
		LOGGER.info("38.卡片押金: " + cardFee);
		
		String cardCatId = lineInfo.substring(304,306);
		LOGGER.info("39.卡別: " + cardCatId);
		
		String rmName = lineInfo.substring(306,308);
		LOGGER.info("40.記名註記: " + rmName);
		
		String birthday = lineInfo.substring(308,312);
		LOGGER.info("41.生日MMDD: " + birthday);
		
		String cardProduct = lineInfo.substring(312,314);
		LOGGER.info("42.產品別: " + cardProduct);
		
		String tmp5 = lineInfo.substring(314,336);
		LOGGER.info("43.保留: " + tmp5);
		
		String cardNo2 = lineInfo.substring(336,352);
		LOGGER.info("44.儲值檔識別碼(PID): " + cardNo2);
		
		String tmp6 = lineInfo.substring(352,368);
		LOGGER.info("45.保留: " + tmp6);
		
		String boxNo = lineInfo.substring(368,408);
		String boxNoStr = convertHexToString(boxNo);
		LOGGER.info("46.盒號: " + boxNoStr.trim());
		
		String space = lineInfo.substring(408,444);
		LOGGER.info("46.空白: " + space);	
	
		updateTbCard(conn, cardNo, boxNoStr.trim());		
    }*/
    
    private void updateHgCardMap(Connection conn, HashMap<String, String> hm) throws Exception 
    {
    	String cardNo = hm.get("field02");
    	LOGGER.info("2.儲值檔識別碼(PID): " + cardNo);
    	
    	String hexBarcode1 = hm.get("field120");
		String barcode1 = IFFUtils.hex2ascii(hexBarcode1);
		LOGGER.info("barcode1: " + barcode1);
    	
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE TB_HG_CARD_MAP set");
		sql.append(" CARD_NO='").append(cardNo).append("'");
		sql.append(", STATUS='").append("1").append("'");
		sql.append(" WHERE BARCODE1='").append(barcode1).append("'");
		
		DBService.getDBService().sqlAction(sql.toString(), conn, false);
	}

	public String convertHexToString(String hex)
	{  
		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
	
		for( int i=0; i<hex.length()-1; i+=2 )
		{
		    //grab the hex in pairs
		    String output = hex.substring(i, (i + 2));
		     //convert hex to decimal
		    int decimal = Integer.parseInt(output, 16);
		    //convert the decimal to character
		    sb.append((char)decimal);
	 
		    temp.append(decimal);
		}
		  //System.out.println("Decimal : " + temp.toString());	 
		return sb.toString();
	 }

    /**
     * 註記處理成功
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess
     *      (java.sql.Connection, java.lang.String)
     */
    @Override
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
    	inctlInfo.setFailCnt(failCnt);
    	inctlInfo.setTotRec(totCnt);
    	inctlInfo.setRecCnt(totCnt);
    	inctlInfo.setSucCnt(sucCnt);
    	inctlInfo.setProgramName("ImpPersoFeedback");
    	inctlInfo.setStartDate(sysDate);
    	inctlInfo.setStartTime(sysTime);
    	inctlInfo.setWorkFlag(Layer1Constants.WORKFLAG_PROCESSOK);
    	
    	if (bis != null){
    		bis.close();
    	}
		new TbInctlMgr(connection).update(inctlInfo);  	
    }
    
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {
    	if (bis != null){
    		bis.close();
    	}
    }
    
    /*private void insertInctlErrInfo(Connection conn, int idx, String lineInfo, TbInctlInfo inctlInfo, String rcode, String desc) throws Exception 
	{
        try {
        	 if (inctlInfo == null) {
                 throw new IllegalArgumentException("inctlInfo is null!");
             }    	 
            TbInctlErrInfo inctlErrInfo = new TbInctlErrInfo();
            // set by inctlInfo
            inctlErrInfo.setMemId(inctlInfo.getMemId());
            inctlErrInfo.setFileName(inctlInfo.getFileName());
            inctlErrInfo.setFileDate(inctlInfo.getFileDate());
            inctlErrInfo.setSeqno(inctlInfo.getSeqno());
            inctlErrInfo.setFileType(inctlInfo.getFileType());
            inctlErrInfo.setFullFileName(inctlInfo.getFullFileName());
            // set by lineInfo
            if (lineInfo == null) {
                inctlErrInfo.setLineNo(new Integer(0));
                inctlErrInfo.setMessage("");
                inctlErrInfo.setMessageLen(new Integer(0));
            }
            else {
                inctlErrInfo.setLineNo(idx++);
                String message = StringUtil.getMaxString(lineInfo, "UTF-8", 1200);
                inctlErrInfo.setMessage(message);
                inctlErrInfo.setMessageLen(new Integer(lineInfo.length()));

            }
            //String lineSep = System.getProperty("line.separator", "\n");
            // set by descInfo
            StringBuffer sb = new StringBuffer();
            sb.append("[errorCode]=" + rcode + " [errorMsg]=資料不合法" + " [content]=" + desc);
            // avoid exceed table field length
            String errorDesc = StringUtil.getMaxString(sb.toString(), "UTF-8", 1200);
            inctlErrInfo.setErrorDesc(errorDesc);
            // set others
            String dateTime = DateUtil.getTodayString();
            String sysDate = dateTime.substring(0, 8);
            String sysTime = dateTime.substring(8, 14);
            String parMon = dateTime.substring(4, 6);
            String parDay = dateTime.substring(6, 8);
            inctlErrInfo.setSysDate(sysDate);
            inctlErrInfo.setSysTime(sysTime);
            inctlErrInfo.setParMon(parMon);
            inctlErrInfo.setParDay(parDay);
            TbInctlErrMgr mgr = new TbInctlErrMgr(conn);
            mgr.insert(inctlErrInfo);
        }
        catch (Exception ignore) {
            LOGGER.warn("insertInctlErrInfo error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
    }*/
    
    public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public ThreadPoolJobExecutor getThreadPoolJobExecutor() {
		return threadPoolJobExecutor;
	}
	public void setThreadPoolJobExecutor(ThreadPoolJobExecutor threadPoolJobExecutor) {
		this.threadPoolJobExecutor = threadPoolJobExecutor;
	}
}
