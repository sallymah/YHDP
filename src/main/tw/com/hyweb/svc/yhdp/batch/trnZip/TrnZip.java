package tw.com.hyweb.svc.yhdp.batch.trnZip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.expfiles.OutctlBean;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.service.db.info.TbZipLogInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbOutctlMgr;
import tw.com.hyweb.service.db.mgr.TbZipLogMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.traffics.Members;
import tw.com.hyweb.svc.yhdp.batch.framework.traffics.TrnRcodes;
import tw.com.hyweb.svc.yhdp.batch.framework.traffics.FilenameBean;
import tw.com.hyweb.svc.yhdp.batch.trnUnZip.DensityUtils;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;



public class TrnZip extends AbstractBatchBasic {
	private static Logger log = Logger.getLogger(TrnZip.class);
	protected OutctlBean outctlBean = null;
	
	protected String batchDate = "";
	protected Connection conn = null;
	// 所有 TB_FILE_INFO.IN_OUT = config 帶入的設定
    protected String fileInfoInOut = "";
    //protected String tempDir = "";
    protected String workDir = "";
    //protected Vector fileInfos = null;
    // 要加 fileName 的設定, 沒設, 都處理, 有設, 只處理有設的 fileTypes
    protected List filenameBeans = new ArrayList();
    
    protected TbFileInfoInfo fileZipInfo = null;
    protected TbFileInfoInfo fileInfo = null;
    protected FilenameBean filenameBean = null;
    
    protected List physicalFiles = null;
    
    protected HashMap fileName2fileNameZip = new HashMap();
    protected HashMap fileName2info = new HashMap();
    
    //檢查檔案大小是否超過設定最大上限
    protected boolean checkFileSize = true;
    //檔案最大上限
    private static long MAX_SIZE = 10000000000L;
    protected final String MEMGROUPID_SPECIAL = "22222";
    protected final String MEMID_SPECIAL = "00000000";
    protected final String MERCHID_SPECIAL = "111111111111111";
    protected final String PERSO_FACTORY_RREMOTE = "PERSO_FACTORY";
    
    protected String unzipSubFileName = ".ZIP";
    
    //protected String seqno = "01";
    
    protected Vector outctlInfos;
    private HashMap expName2ZipLog = new HashMap();
    
    private HashMap fullFileName2Zip = new HashMap();
    // for current run
    protected TbOutctlInfo outctlInfo = null;
    
    protected final Members members;
    
    public TrnZip( Members members ) {
    	this.members = members;
    }
    
	@Override
	public void process(String[] argv) throws Exception {

		initial();
		try {
            for (int i = 0; i < filenameBeans.size(); i++)
            {
            	filenameBean = (FilenameBean) filenameBeans.get(i);
            	
            	fileZipInfo = (TbFileInfoInfo) fileName2info.get(filenameBean.getFileNameRZip());
            	fileInfo = (TbFileInfoInfo) fileName2info.get(filenameBean.getFileNameR());
            	
            	if ( fileZipInfo != null 
            			&& fileInfo != null){
            		
            		expName2ZipLog = new HashMap();
                    fullFileName2Zip = new HashMap();
            		// get all outctlInfos with sql "SELECT * FROM TB_OUTCTL WHERE WORK_FLAG = '1' ORDER BY FILE_NAME, PROC_DATE"
                    TbOutctlInfo oqinfo = new TbOutctlInfo();
                    oqinfo.toEmpty();
                    oqinfo.setFileName(fileInfo.getFileName());
                    oqinfo.setWorkFlag(Layer1Constants.OWORKFLAG_INWORK);
                    outctlInfos = new Vector();
                    TbOutctlMgr omgr = new TbOutctlMgr(conn);
                    omgr.queryMultiple(getOutctlWhere(), outctlInfos, " SYS_DATE, MEM_ID");
                    
                    //get Zip_LOG
                    Vector zipLogInfos = new Vector();
                    TbZipLogMgr zipMgr = new TbZipLogMgr(conn);
                    log.info(fileInfo.getFileName());
                    zipMgr.queryMultiple("EXP_FILE_NAME = '"+fileInfo.getFileName()+"' " +
                    		"AND EXP_ZIP_NAME IS NULL AND ZIP_DATE IS NULL "
                    		, zipLogInfos);
                    log.info( zipLogInfos.size());
                    for (int k = 0; k < zipLogInfos.size(); k++){
                    	TbZipLogInfo zipLogInfo = (TbZipLogInfo)zipLogInfos.get(k);
                    	expName2ZipLog.put(zipLogInfo.getExpName(), zipLogInfo);
                    }
                                        
                    for (int j = 0; j < outctlInfos.size(); j++) {
                        outctlInfo = null;
                        outctlInfo = (TbOutctlInfo) outctlInfos.get(j);
                        
                        handleOutctlInfo();
                    }

                    
                    //關閉所有的Zip檔
                    Iterator iter = fullFileName2Zip.keySet().iterator();
                    while(iter.hasNext())
            		{
                    	String physical = (String) iter.next();
                    	
                    	ZipOutputStream zOut = (ZipOutputStream)fullFileName2Zip.get(physical);
                    	zOut.close();
                    	
                    	TbOutctlInfo zipOutctlInfo = makeOutctlInfo(physical);
                    	outctlBean.insertOutctl(conn, false, zipOutctlInfo);
            		}
                    conn.commit();
            	}
            	
            }
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
	}
	
	private String getOutctlWhere()
    {
    	StringBuffer whereSql = new StringBuffer();
    	
    	whereSql.append(" FILE_NAME = ").append(StringUtil.toSqlValueWithSQuote(fileInfo.getFileName()));
    	whereSql.append(" AND WORK_FLAG = ").append(StringUtil.toSqlValueWithSQuote(Layer1Constants.OWORKFLAG_INWORK));
    	
    	StringBuffer jobWhereSql = new StringBuffer();
    	if (null != getBatchResultInfo()){
	    	
        	if (Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
        		jobWhereSql.append(" AND JOB_ID IS NULL");
        		jobWhereSql.append(" AND JOB_TIME IS NULL");
    		}
    		else{
	        	if(!StringUtil.isEmpty(getBatchResultInfo().getJobId()) 
				&& !getBatchResultInfo().getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
		    		jobWhereSql.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobId()));
		    		if(!StringUtil.isEmpty(getBatchResultInfo().getJobTime()) 
					&& !getBatchResultInfo().getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
		    			jobWhereSql.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobTime()));
			    	}
		    	}
		    	if(!StringUtil.isEmpty(getBatchResultInfo().getMemId())){
		    		jobWhereSql.append(" AND MEM_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getMemId()));
		    	}
    		}
    	}
    	else{
    		log.warn("tbBatchResultInfo is null.");
    	}

    	if (jobWhereSql.length() > 0){
    		whereSql.append(" AND EXISTS (SELECT 1 FROM TB_MEMBER WHERE TB_OUTCTL.MEM_ID = TB_MEMBER.MEM_ID");
    		whereSql.append(jobWhereSql.toString());
    		whereSql.append(")");
    	}	
    	
    	log.info(whereSql.toString());
    	return whereSql.toString();
    }
	
	public void handleOutctlInfo() throws Exception{
        try {
            // 決定 physicalFile
        	String relativePath = localPhysicalFile(fileInfo);
        	File physicalFile = new File(FilenameUtils.normalize(workDir + relativePath + outctlInfo.getFullFileName()));
        	if (!physicalFile.exists() || (physicalFile.length() != outctlInfo.getFileSize().longValue())) {
                // if file not exists in 3 and size not match in 4, update outctlInfo.workFlag to '9'
                outctlInfo.setWorkFlag(Layer1Constants.OWORKFLAG_PROCESSFAIL);
                TbOutctlMgr mgr = new TbOutctlMgr(conn);
                mgr.update(outctlInfo);
                conn.commit();
            }
            else {

            	TbZipLogInfo zipLogInfo = (TbZipLogInfo) expName2ZipLog.get(outctlInfo.getFullFileName());
            	//決定ZipFile
            	
            	if ( zipLogInfo != null ){
            		String fullFileNameRZip = filenameBean.getFullFileNameRZip(zipLogInfo);
                	String zipFilePath = localPhysicalFile(fileZipInfo);
                	String physical = FilenameUtils.normalize(workDir + zipFilePath + fullFileNameRZip);
                	//壓縮
                	ZipOutputStream zOut = null;
                	if (fullFileName2Zip.get(physical) == null){
                		zOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(physical)));
                		fullFileName2Zip.put(physical, zOut);
                	}
                	else
                		zOut = (ZipOutputStream) fullFileName2Zip.get(physical);
                		
                	DensityUtils.ZipFile(zOut, physicalFile, filenameBean.getFullFileNameR(zipLogInfo), false);
                	
                    // 8. update outctlInfo.workFlag to '3'
                    outctlInfo.setWorkFlag(Layer1Constants.OWORKFLAG_INTEMP);
                    TbOutctlMgr mgr = new TbOutctlMgr(conn);
                    mgr.update(outctlInfo);
                    
                    zipLogInfo.setExpZipName(fullFileNameRZip);
                    zipLogInfo.setZipDate(batchDate);
                    TbZipLogMgr logMgr = new TbZipLogMgr(conn);
                    logMgr.update(zipLogInfo);
                    
                    //20161017 修改，insert Zip TB_OUTCTL成功在一起commit
                    //conn.commit();
                    
                    
            	}
            	else{
            		log.warn(outctlInfo.getFullFileName() + " not in ZipLog.");
            		outctlInfo.setWorkFlag(Layer1Constants.OWORKFLAG_PROCESSFAIL);
                    TbOutctlMgr mgr = new TbOutctlMgr(conn);
                    mgr.update(outctlInfo);
                    conn.commit();
            	}
            }
        }
        catch (Exception ignore) {
            log.warn("handleOutctlInfo error:" + ignore.getMessage(), ignore);
        }
    }
	
	protected String localPhysicalFile (TbFileInfoInfo fileInfo) throws Exception{
        String relativePath = fileInfo.getLocalPath();
        
        if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1) {
            // find "22222"
            relativePath = relativePath.replaceAll(MEMGROUPID_SPECIAL, outctlInfo.getMemGroupId());
        }
        if (fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1) {
            // find "00000000"
            relativePath = relativePath.replaceAll(MEMID_SPECIAL, outctlInfo.getMemId());
        }
        if (fileInfo.getLocalPath().indexOf(MERCHID_SPECIAL) != -1) {
            // find "111111111111111"
            relativePath = relativePath.replaceAll(MERCHID_SPECIAL, outctlInfo.getMerchId());
        }

        return relativePath = pendingEndSep(relativePath);
    }
	
	protected void localPhysicalFile( Vector allOutctlInfos ) throws Exception{
        
		physicalFiles = new ArrayList();
		String relativePath = fileInfo.getLocalPath();
        
        for ( int i = 0; i < allOutctlInfos.size(); i++ ){
        	
        	TbOutctlInfo oqinfo = (TbOutctlInfo) allOutctlInfos.get(i);
        
	        if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1) {
	            // find "22222"
	            relativePath = relativePath.replaceAll(MEMGROUPID_SPECIAL, oqinfo.getMemGroupId());
	        }
	        if (fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1) {
	            // find "00000000"
	            relativePath = relativePath.replaceAll(MEMID_SPECIAL, oqinfo.getMemId());
	        }
	        if (fileInfo.getLocalPath().indexOf(MERCHID_SPECIAL) != -1) {
	            // find "111111111111111"
	            relativePath = relativePath.replaceAll(MERCHID_SPECIAL, oqinfo.getMerchId());
	        }
	        
	
	        relativePath = pendingEndSep(relativePath);
	        String normalPath = FilenameUtils.normalize(workDir + relativePath + oqinfo.getFullFileName());
	        File physicalFile = new File(normalPath);
	        
	        if (physicalFile.isFile() && isMatchFile(physicalFile.getName())) {
                if (hasOKFile(physicalFile)) {
                    if (checkFileSize && isSizeOK(physicalFile))
                    	physicalFiles.add(physicalFile);
                    else if ( !checkFileSize )
                    	physicalFiles.add(physicalFile);
                }
            }
        }
    }
	

	public void initial() throws Exception {
		String fileName = System.getProperty("fileName");
		
		outctlBean = new OutctlBean();
        outctlBean.setRelated(true);
        outctlBean.setBatchResultInfo(getBatchResultInfo());
		
		try {
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			TbFileInfoInfo qinfo = new TbFileInfoInfo();
            qinfo.toEmpty();
            qinfo.setInOut(fileInfoInOut);
            if (!StringUtil.isEmpty(fileName)) {
                log.info("input fileName:" + fileName);
                qinfo.setFileName(fileName);
            }
            Vector allFileInfos = new Vector();
            TbFileInfoMgr mgr = new TbFileInfoMgr(conn);
            mgr.queryMultiple(qinfo, allFileInfos);
            log.info(allFileInfos.size());
            // 要加 fileTypes 的設定, 沒設, 都處理, 有設, 只處理有設的 fileTypes (tracy 建議)
            filterFileInfos(allFileInfos);
//            tempDir = BatchUtil.getTempDirectory();
//            if (StringUtil.isEmpty(tempDir)) {
//            	throw new Exception("no tempDir setting!");
//            }
//            tempDir = normalFileSeparator(tempDir);
            workDir = BatchUtil.getWorkDirectory();
            if (StringUtil.isEmpty(workDir)) {
            	throw new Exception("no workDir setting!");
            }
            workDir = normalFileSeparator(workDir);
		}
		catch (Exception ignore) {
            log.warn("initial error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
	}
	
	public TbOutctlInfo makeOutctlInfo(String physical) throws Exception {
		
		File zipFile = new File(physical);
		String zipFullFileName = zipFile.getName();
		filenameBean.initialZip(zipFullFileName, members.getSourdeId2Member());
		
		TbOutctlInfo outctlInfo = new TbOutctlInfo();
		
		outctlInfo.setFileName(filenameBean.getFileNameRZip());

		outctlInfo.setFullFileName(zipFullFileName);
		outctlInfo.setFileSize(zipFile.length());
		
		outctlInfo.setMemId(filenameBean.getMemId());
		outctlInfo.setMemGroupId(filenameBean.getMemGroupId());
        
		//20161017 FileDate 改為檔名原則，避免捕吃檔案時造成的PK衝突
		//outctlInfo.setFileDate(batchDate);
		outctlInfo.setFileDate(filenameBean.getFileDate());
        outctlInfo.setSeqno(filenameBean.getSeqno());
        outctlInfo.setFileType(fileZipInfo.getFileType());
        outctlInfo.setWorkFlag(Layer1Constants.OWORKFLAG_INWORK);
        //outctlInfo.setFileSize(new Long(.length()));
        //outctlInfo.setFullFileName(expFileInfo.getFullFileName());
        String dateTime = DateUtil.getTodayString();
        outctlInfo.setSysDate(dateTime.substring(0, 8));
        outctlInfo.setSysTime(dateTime.substring(8, 14));
        
        outctlInfo.setParMon(batchDate.substring(4, 6));
        outctlInfo.setParDay(batchDate.substring(6, 8));
        
        // 和 TB_BATCH_RESULT 關連起來
        outctlInfo = outctlBean.makeOutctl(outctlInfo);
		return outctlInfo;
	}
	
	//過濾不需處理的設定、抓出需ZIP的設定 及 ZIP後的設定
	protected void filterFileInfos(Vector allFileInfos) {
        if (filenameBeans == null || filenameBeans.size() == 0) {
            // 設定檔沒有就不做zip
        	log.warn("--------- No have filenameBeans. ---------");
        }
        else {

        	for (int j = 0; j < filenameBeans.size(); j++)
            {
        		HashMap scratch = new HashMap();
        		FilenameBean filenameBean = (FilenameBean) filenameBeans.get(j);
        		String fileNameR = filenameBean.getFileNameR();
        		String FileNameRZip = filenameBean.getFileNameRZip();
        		for (int i = 0; i < allFileInfos.size(); i++)
                {
        			TbFileInfoInfo fileInfo = (TbFileInfoInfo) allFileInfos.get(i);
        			if ( fileInfo.getFileName().equals(fileNameR) || 
        					fileInfo.getFileName().equals(FileNameRZip)){
        				scratch.put(fileInfo.getFileName(), fileInfo);
        			}
                }
        		
        		//需同時有ZipFileInfo、FileInfo設定才處理。
        		if ( scratch.size() == 2 ){
        			fileName2fileNameZip.put(fileNameR, FileNameRZip);
        			fileName2info.putAll(scratch);
        		}
        		else{
        			log.warn(fileNameR + ", " + FileNameRZip + " Setting Incomplete. scratch:[" + scratch + "]");
        		}
        			
            }
        }
    }
	
//	protected File getDestFile(File matchFile) {
//        String srcFullName = normalFileSeparator(matchFile.getAbsolutePath());
//        String destFullName = srcFullName.replaceAll(tempDir, workDir);
//        return new File(destFullName);
//    }
	
    protected boolean isSizeOK(File f) {
        if (f.length() >= MAX_SIZE) {
            log.warn("file '" + f.getAbsolutePath() + "' size '" + f.length() + "' too large! ignore!");
            return false;
        }
        else {
            return true;
        }
    }
	
	protected boolean hasOKFile(File f) {
        if (Layer1Constants.OKFLAG_NOCHECK.equals(fileInfo.getOkFlag())) {
            // 不用檢查 OK file
            return true;
        }
        else if (Layer1Constants.OKFLAG_CHECK.equals(fileInfo.getOkFlag())) {
            // 要檢查 OK file
        	String subFileName = "";
        	String file = "";
        	if (fileInfo.getSubFileName().contains("/")){
        		subFileName = fileInfo.getSubFileName().replaceAll("/", "");
        		file = f.getAbsolutePath().substring(0,f.getAbsolutePath().lastIndexOf(".")) + subFileName;
        	}
        	else{
        		subFileName = fileInfo.getSubFileName();
        		file = f.getAbsolutePath() + subFileName;
        	}
            File ok = new File(file);
            if (ok.isFile() && ok.exists()) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            // unknown, suppose 不用檢查 OK file
            log.warn("unknown OK_FLAG(" + fileInfo.getOkFlag() + ")! suppose no check OK file!");
            return true;
        }
    }
	protected boolean isMatchFile(String fn) {
        boolean ret = false;
        Pattern p = Pattern.compile(fileInfo.getFileNamePattern());
        Matcher m = p.matcher(fn);
        ret = m.matches();
        log.info("fn:" + fn + " matched:" + ret);
        return ret;
    }
	
	protected static String normalFileSeparator(String fn) {
        return fn.replace('\\', '/');
    }
	
	public String getBatchDate() {
		return batchDate;
	}
	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}
	public String getFileInfoInOut() {
		return fileInfoInOut;
	}
	public void setFileInfoInOut(String fileInfoInOut) {
		this.fileInfoInOut = fileInfoInOut;
	}
//	public String getTempDir() {
//		return tempDir;
//	}
//	public void setTempDir(String tempDir) {
//		this.tempDir = tempDir;
//	}
	public String getWorkDir() {
		return workDir;
	}
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}
	public List getFilenameBeans() {
		return filenameBeans;
	}
	public void setFilenameBeans(List filenameBeans) {
		this.filenameBeans = filenameBeans;
	}
	
	private String pendingEndSep(String dir) {
        String ret = "";
        if (dir.endsWith("/") || dir.endsWith("\\")) {
            ret = dir;
        }
        else {
            ret = dir + "/";
        }
        return ret;
    }

	public static void main(String[] args) {
        try {
            String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            else if (!DateUtil.isValidDate(batchDate)) {
                log.info("invalid batchDate('" + batchDate + "') using system date!");
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            // Gets Spring Setting
            ApplicationContext apContext = new FileSystemXmlApplicationContext(
            		"config" + File.separator + "batch" + File.separator +
                    "TrnZip" + File.separator + "spring.xml");
            TrnZip trnZip = (TrnZip) apContext.getBean("trnZip");
            trnZip.setBatchDate(batchDate);
            //filesIn.process(args);
            trnZip.run(args);
        }
        catch (Exception e) {
            log.warn("get spring bean error:" + e.getMessage(), e);
            System.exit(-1);
        }
        System.exit(0);
    }
}
