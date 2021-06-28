package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbFileInfoPK;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbPersoFactoryInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.perso.CgYhdpSsf;
import tw.com.hyweb.svc.yhdp.batch.framework.perso.HsmAdapterECB;
import tw.com.hyweb.svc.yhdp.batch.framework.perso.PersoFileData;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor.JobRunner;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor.JobRunnerFactory;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor.ThreadPoolJobExecutor;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;

public class YhdpPersoFeebackDataAction {
	
	private static final Logger LOGGER = Logger.getLogger(CheckPersoFeebackJob.class);
	
	private final String PERSO_FACTORY_RREMOTE = "PERSO_FACTORY";
	private final int HSM_MAX_LENGTH = 65512;
	
	protected final String MEMGROUPID_SPECIAL = "22222";
    protected final String MEMID_SPECIAL = "00000000";

	protected String configFilename = "config/batch/MappingInfos.xml";
	private static final String THREAS_SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "preoperation" + File.separator + "CheckPersoFeeback" + File.separator + "thread.xml";

	protected String encoding = "UTF-8";
	
	private String origFile;
	private String converFile;
    
	private ArrayList<PersoFileData> persoFileDataList = new ArrayList<PersoFileData>();
	
	private ThreadPoolJobExecutor threadPoolJobExecutor = null;
	private JobRunnerFactory jobRunnerFactory = new JobRunnerFactory();
	
    private final HsmAdapterECB hsmAdapterECB;
    private final CgYhdpSsf cgYhdpSsf;
    
	public YhdpPersoFeebackDataAction(HsmAdapterECB hsmAdapterECB, CgYhdpSsf cgYhdpSsf){
    	this.hsmAdapterECB = hsmAdapterECB;
    	this.cgYhdpSsf = cgYhdpSsf;
    	
	}

	public void init(Connection conn, TbInctlInfo inctlInfo) throws Exception
    {        
    	TbFileInfoInfo fileInfo = getFileInfo(conn, inctlInfo);
    	if(fileInfo == null){
    		throw new Exception("TB_FILE_INFO(WHERE FILE_NAME='"+ inctlInfo.getFileName() +"' AND IN_OUT='W') NO DATA FOUND");
    	}
    		
		String enString = null;
		
		BufferedInputStream br = null;
		BufferedOutputStream bw= null;
		
		String LocalPath = fileInfo.getLocalPath();
		
		try {
		
			for( int i = 0; i < persoFileDataList.size(); i++ ){
        		
        		PersoFileData persoFileData = persoFileDataList.get(i);
        		
        		//一、二代卡命名原則不同
        		if (inctlInfo.getFileName().equals(persoFileData.getFileName())){
        			
        			TbPersoFactoryInfo persoFactoryInfo = null;
        			TbMemberInfo memberInfo = null;
        			
        			if ((persoFileData.getPersoFactoryStart() >= 0 && persoFileData.getPersoFactoryEnd() >0)
        					&& !(persoFileData.getMemIdStart() >= 0 && persoFileData.getMemIdEnd() >0)){
        				
        				TbPersoFactoryMgr mgr = new TbPersoFactoryMgr(conn);
        		    	persoFactoryInfo =  
        		    			mgr.querySingle(inctlInfo.getFullFileName().substring(persoFileData.getPersoFactoryStart(), persoFileData.getPersoFactoryEnd()));
        		    	if(persoFactoryInfo == null) {
        		    		throw new Exception("it has no setting in TB_PERSO_FACTORY: " 
        		    					+ inctlInfo.getFullFileName().substring(persoFileData.getPersoFactoryStart(), persoFileData.getPersoFactoryEnd()));
        		    	}
        			}
        			//自行製卡
        			else if (!(persoFileData.getPersoFactoryStart() >= 0 && persoFileData.getPersoFactoryEnd() >0)
        					&& (persoFileData.getMemIdStart() >= 0 && persoFileData.getMemIdEnd() >0)){
        				TbMemberMgr mgr = new TbMemberMgr(conn);
        		    	memberInfo =  
        		    			mgr.querySingle(inctlInfo.getFullFileName().substring(persoFileData.getMemIdStart(), persoFileData.getMemIdEnd()));
        		    	if(memberInfo == null) {
        		    		throw new Exception("it has no setting in TB_MEMBER: " 
        		    					+ inctlInfo.getFullFileName().substring(persoFileData.getMemIdStart(), persoFileData.getMemIdEnd()));
        		    	}
        			}
        			else{
        				throw new Exception(persoFileData.getFileName() + " setting is wrong. [" +
        						"PersoFactoryStart: " + persoFileData.getPersoFactoryStart() + ", PersoFactoryEnd: " + persoFileData.getPersoFactoryEnd() + 
        						",MemIdStart: " + persoFileData.getMemIdStart() + ", MemIdEnd: " + persoFileData.getMemIdEnd() + "]");
        			}
        			
		        	//自行製卡
		        	if (memberInfo != null){
		    	    	if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1 || fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1){
		    	        	
		    		        if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1) {
		    		            // find "22222"
		    		        	LocalPath = LocalPath.replaceFirst(MEMGROUPID_SPECIAL, memberInfo.getMemGroupId());
		    		        }
		    		        if (fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1) {
		    		            // find "00000000"
		    		        	LocalPath = LocalPath.replaceFirst(MEMID_SPECIAL, memberInfo.getMemId());
		    		        }
		    	        }
		        	}
		        	else{
	    		    	if (LocalPath.contains(PERSO_FACTORY_RREMOTE)) {
	    		    		LocalPath = LocalPath.replaceFirst(PERSO_FACTORY_RREMOTE, persoFactoryInfo.getRemoteFolder());
	    		    	}
		        	}
    		    	
    		    	String filePath = FilenameUtils.separatorsToSystem(BatchUtil.getWorkDirectory() + LocalPath + "/");
    		    	
    		        this.origFile = filePath + inctlInfo.getFullFileName();
    		        LOGGER.info("fn: " + origFile);
    		        
    		        this.converFile = filePath + inctlInfo.getFullFileName() + ".tmp";
    		        LOGGER.info("fn: " + converFile); 
		        	//
		        	
    		        //init
    	    		br = new BufferedInputStream(new FileInputStream(new File(origFile)));
    	    		bw = new BufferedOutputStream(new FileOutputStream(new File(converFile)));
    	    		
    	    		int times = br.available() / HSM_MAX_LENGTH;
        			//LOGGER.info("times: " + times);
        			int bytes = br.available() % HSM_MAX_LENGTH;
        			//LOGGER.info("bytes: " + bytes);
        			
        			byte[] buffer1 = new byte[HSM_MAX_LENGTH];
        			byte[] buffer2 = new byte[bytes];
        			
        			Object decryptionUtil = null;
        			
    				//自行製卡
        			if (memberInfo != null){
        				cgYhdpSsf.setKeyId(memberInfo.getMemId()+"KEY");
        				decryptionUtil = cgYhdpSsf;
        			}
        			else{
        				decryptionUtil = hsmAdapterECB;
        			}
        			
        			if(times >0) {
        				
        				ApplicationContext apContext = new FileSystemXmlApplicationContext(THREAS_SPRING_PATH);
        		    	ThreadPoolJobExecutor instance = (ThreadPoolJobExecutor) apContext.getBean("threadPoolJobExecutor");
        		    	setThreadPoolJobExecutor(instance);
        		    	
        		    	getThreadPoolJobExecutor().startThreadPoolJob();
            			
        				List hms = new ArrayList<>();
        				HashMap<String,List> resultMap = new HashMap<String,List>();
        				
        				int idx = 0;
        				for(int j=0; j<times; j++)
            			{
/*	            				br.read(buffer1);
            				if (memberInfo != null){
            					//銀行卡加密方式
            					enString1 = cgYhdpSsf.decryptDES3(ISOUtil.hexString(buffer1));
            				}
            				else{
            					enString1 = hsmAdapterECB.descryptECB(ISOUtil.hexString(buffer1));
            				}*/
        					
        					br.read(buffer1);
        					
        					byte[] handleData = buffer1.clone();
        		    		
        		    		hms.add(handleData);
        		    		
        		    		if(getThreadPoolJobExecutor() != null && getThreadPoolJobExecutor().getExecutor() != null)
        			        {
        		    			if (resultMap != null
        		    					&& ((j%getThreadPoolJobExecutor().getThroughputRate() == 0) || (j==times-1)) ){
        			    			ThreadPoolExecutor threadPoolExecutor = getThreadPoolJobExecutor().getExecutor();
        			                
        			                JobRunner jobRunnerExecute = jobRunnerFactory.create(resultMap, idx, memberInfo, decryptionUtil, new ArrayList<>(hms));
        			                
        			                threadPoolExecutor.execute(jobRunnerExecute);
        			                
        			                hms.clear();
        			                
        			                idx++;
        		    			}
        			        }
        		    		else{
        		    			LOGGER.warn("getThreadPoolJobExecutor(): " + getThreadPoolJobExecutor() 
        		    					+ "  getThreadPoolJobExecutor().getExecutor():" + getThreadPoolJobExecutor().getExecutor());
        		    		}
//        					
//            				
//            				LOGGER.info("encrypt String1: " + enString1);
//            				bw.write(ISOUtil.hex2byte(enString1));
            			}
        				
        				while (getThreadPoolJobExecutor().getExecutor().getActiveCount() != 0) {
            	    		Thread.sleep(2000);
            	    		LOGGER.debug("ActiveCount:"+getThreadPoolJobExecutor().getExecutor().getActiveCount()
            	    				+" CompletedCount:"+getThreadPoolJobExecutor().getExecutor().getCompletedTaskCount());
            			}
            	    	
            	    	if(!getThreadPoolJobExecutor().getExecutor().isShutdown()){
            		    	getThreadPoolJobExecutor().getExecutor().shutdown();
            				LOGGER.info("threadPoolJobExecutor shutdown!!");
            	    	}
            			
            	    	//依序寫入檔案
            			for(int j = 0; j < idx; j++){
            				List<String> threadStringList = resultMap.get(Integer.toString(j));
            				if (threadStringList != null && threadStringList.size() >0){
            					for (String enString1 : threadStringList){
            						bw.write(ISOUtil.hex2byte(enString1));
            					}
            				}
            				else{
            					throw new Exception( inctlInfo.getFullFileName() + " Thread have error, index = "+ j);
            				}
            			}
        			}
        			
        			//最後結尾剩下的byte另行處理
        			if(bytes != 0) {
        				br.read(buffer2);
        				if (memberInfo != null){
        					enString = cgYhdpSsf.decryptDES3(ISOUtil.hexString(buffer2));
        				}
        				else{
        					enString = hsmAdapterECB.descryptECB(ISOUtil.hexString(buffer2));
        				}
        				LOGGER.info("encrypt String2: " + enString);
        				bw.write(ISOUtil.hex2byte(enString));
        			}
        			
        			bw.flush();
    			
    		        break;
        		}
        		else{
        			LOGGER.debug("inctlInfo.getFileName(): " + inctlInfo.getFileName()+ "!= persoFileData.getFileName(): " + persoFileData.getFileName());
        		}
        	}
		
		} 
        finally {
        	ReleaseResource.releaseIO(br);
    		ReleaseResource.releaseIO(bw);
        }
    	
    	//解密到TEMP檔
		//IFFUtils.decryptToFile(origFile, converFile, pinKeyId, iv, HSM_MAX_LENGTH);
	
    }
	
	private TbFileInfoInfo getFileInfo(Connection connection, TbInctlInfo inctlInfo) throws SQLException
    {
        TbFileInfoPK pk = new TbFileInfoPK();
        pk.setFileName(inctlInfo.getFileName());
        //pk.setInOut("I");
        pk.setInOut("W");

        return new TbFileInfoMgr(connection).querySingle(pk);
    }
	
	public String getProcFile() 
	{
		return converFile;
	}
	
	public ArrayList<PersoFileData> getPersoFileDataList() {
		return persoFileDataList;
	}

	public void setPersoFileDataList(ArrayList<PersoFileData> persoFileDataList) {
		this.persoFileDataList = persoFileDataList;
	}

	public ThreadPoolJobExecutor getThreadPoolJobExecutor() {
		return threadPoolJobExecutor;
	}

	public void setThreadPoolJobExecutor(ThreadPoolJobExecutor threadPoolJobExecutor) {
		this.threadPoolJobExecutor = threadPoolJobExecutor;
	}

}
