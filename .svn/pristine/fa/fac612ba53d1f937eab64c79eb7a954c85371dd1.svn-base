package tw.com.hyweb.svc.yhdp.batch.trnUnZip;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
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
import tw.com.hyweb.service.db.info.TbZipLogInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbZipLogMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.traffics.FilenameBean;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;



public class TrnUnZip extends AbstractBatchBasic {
	private static Logger log = Logger.getLogger(TrnUnZip.class);
	protected OutctlBean outctlBean = null;
	
	protected String batchDate = "";
	protected Connection conn = null;
	// 所有 TB_FILE_INFO.IN_OUT = config 帶入的設定
    protected String fileInfoInOut = "";
    
    protected String workDir = "";
    protected String tempDir = "";
    
    //protected Vector fileInfos = null;
    // 要加 fileName 的設定, 沒設, 都處理, 有設, 只處理有設的 fileTypes
    //protected List fileNames = new ArrayList();
    protected List filenameBeans = new ArrayList();
    
    protected HashMap fileNameZip2fileName = new HashMap();
    protected HashMap fileName2info = new HashMap();
    
    protected FilenameBean filenameBean = null;
    protected TbFileInfoInfo fileZipInfo = null;
    protected TbFileInfoInfo fileInfo = null;
    protected List matchFiles = null;
    //檢查檔案大小是否超過設定最大上限
    protected boolean checkFileSize = true;
    //檔案最大上限
    private static long MAX_SIZE = 10000000000L;
    
    protected String unzipSubFileName = ".ZIP";
    
    private List jobMemIds;
    
    public TrnUnZip ( ){
    }
    
	@Override
	public void process(String[] argv) throws Exception {

		initial();
		try {
            
            for (int i = 0; i < filenameBeans.size(); i++)
            {
            	filenameBean = (FilenameBean) filenameBeans.get(i);
            	
            	fileZipInfo = (TbFileInfoInfo) fileName2info.get(filenameBean.getFileNameZip());
            	fileInfo = (TbFileInfoInfo) fileName2info.get(filenameBean.getFileName());
            	
            	if (fileZipInfo != null && fileInfo != null){
            		//解壓縮一定要在同一層，不然無法判斷浮動路徑(總公司、收單、特店)
                	if ( fileZipInfo.getLocalPath().equals(fileInfo.getLocalPath()) ){
                		setMatchFiles();
                    	handleMatchFiles();
                	}
                	else{
                		log.warn("LocalPath are different:"
    	            	+ fileZipInfo.getFileName() + " [" + fileZipInfo.getLocalPath() + "], "
    	            	+ fileInfo.getFileName() + " [" + fileInfo.getLocalPath() + "] ");
                	}
            	}
            	else
            		log.warn("["+filenameBean.getFileName() + "] fileName or fileNameZip not in FileInfo. ");
            }
            
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
	}
	
	public void handleMatchFiles() {
		
		for (int i = 0; i < matchFiles.size(); i++)
        {
			File matchFile = (File) matchFiles.get(i);
			File destFile = getDestFile(matchFile);
			
			log.info("matchFile: "+matchFile );
            //File destFile = getDestFile(matchFile);
            try {
            	
            	//如果附檔名為.ZIP需先解壓縮
            	if (fileZipInfo.getFileNamePattern().endsWith(unzipSubFileName) 
            			&& matchFile.getName().endsWith(unzipSubFileName)){
            		
            		String zipCanonicalPath = matchFile.getCanonicalPath();

            		//解壓縮
            		List<File> unZipFiles = DensityUtils.UZipFile(zipCanonicalPath, false);
            		FileUtils.copyFile(matchFile, destFile);
            		matchFile.delete();
            		
            		List<TbZipLogInfo> tbZipLogInfos = new ArrayList();
            		for ( int j = 0; j < unZipFiles.size(); j++ ){
            			
            			//rename
            			if ( unZipFiles.get(j).exists() ){
            				
            				File unZipFile = unZipFiles.get(j);
            				File renameFile = new File (unZipFile.getCanonicalPath() + "_" + getBatchResultInfo().getStartDate()+ getBatchResultInfo().getStartTime());
            				
            				if ( !renameFile.exists() ){
            					unZipFiles.get(j).renameTo(renameFile);
            					
            					TbZipLogInfo tbZipLogInfo = new TbZipLogInfo();
            					tbZipLogInfo.setFileName(fileInfo.getFileName());
            					tbZipLogInfo.setZipName(matchFile.getName());
            					tbZipLogInfo.setFullFileName(unZipFile.getName());
            					tbZipLogInfo.setUnzipDate(batchDate);
            					tbZipLogInfo.setRmName(renameFile.getName());
            					
            					tbZipLogInfos.add(tbZipLogInfo);
            					
            				}
            				else{
            					log.warn("Rename Error: [" + renameFile.getCanonicalPath() + "] is exists.");
            					continue;
            				}
            			}
            			else{
            				log.warn("Rename Error: [" + unZipFiles.get(j).getCanonicalPath() + "] is not exists.");
            				continue;
            			}
            			
            			
            		}
            		//insert TB_ZIP_LOG
            		insertTbZipLog(tbZipLogInfos);
            		
            	}

            }
            catch (Exception ignore) {
                log.warn("handle one matchFile error:" + ignore.getMessage(), ignore);
            }
        }
	}
	
	protected void insertTbZipLog ( List<TbZipLogInfo> tbZipLogInfos ){
		
		Connection conn = null;
		
		try
        {
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			conn.setAutoCommit(false);
			
			TbZipLogMgr tbZipLogMgr = new TbZipLogMgr(conn);
			
			for ( int i = 0; i < tbZipLogInfos.size(); i++ ){
				tbZipLogMgr.insert(tbZipLogInfos.get(i));
			}
			
			conn.commit();
        }
        catch (SQLException sqle)
        {
        	try {
				conn.rollback();
			} catch (SQLException e) {
				log.error("Insert TbZipLog Error: " + e);
			}
        	log.error("Insert TbZipLog Error: " + sqle);
        }
		finally {
			ReleaseResource.releaseDB(conn, null, null);
		}
	}
	
    private static final String MEMBER_REPLACE_PATTERN = ".*0{8}.*";
    private static final String MEMBER_REPLACE_MARK_PATTERN = "00000000";
    private static final String MEMBER_GROUP_REPLACE_PATTERN = ".*2{5}.*";
    private static final String MEMBER_GROUP_REPLACE_MARK_PATTERN = "22222";
    private static final String MERCH_REPLACE_PATTERN = ".*1{15}.*";
    private static final String MERCH_REPLACE_MARK_PATTERN = "111111111111111";
	
	protected void setMatchFiles() throws SQLException {

    	matchFiles = new ArrayList();
    	String InnermostPath = "";
    	if (fileZipInfo.getLocalPath().matches(MEMBER_GROUP_REPLACE_PATTERN))
    	{
    		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + '/') + (fileZipInfo.getLocalPath().substring(0, fileZipInfo.getLocalPath().indexOf(MEMBER_GROUP_REPLACE_MARK_PATTERN)) + '/'));
    		InnermostPath = fileZipInfo.getLocalPath().substring(fileZipInfo.getLocalPath().indexOf(MEMBER_GROUP_REPLACE_MARK_PATTERN) + MEMBER_GROUP_REPLACE_MARK_PATTERN.length()) + "/";
    		File parentLocaldir = new File(parentLocalPath);
    		if (parentLocaldir.listFiles() != null){
				for (File memberGroupfile : parentLocaldir.listFiles()){
					if (fileZipInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)){
						String memberGrouptLocalPath = parentLocalPath + memberGroupfile.getName() + "/";
						InnermostPath = fileZipInfo.getLocalPath().substring(fileZipInfo.getLocalPath().indexOf(MEMBER_REPLACE_MARK_PATTERN) + MEMBER_REPLACE_MARK_PATTERN.length()) + "/";
						File memberGrouptLocaldir = new File(memberGrouptLocalPath);
						if ( memberGrouptLocaldir.listFiles() != null ){
							for (File memberfile : memberGrouptLocaldir.listFiles()){
								
								if (jobMemIds != null){
									if (!jobMemIds.contains(memberfile.getName())){
										continue;
									}
								}
								
								if (fileZipInfo.getLocalPath().matches(MERCH_REPLACE_PATTERN)){
									String memberLocalPath = memberGrouptLocalPath + memberfile.getName() + "/";
									InnermostPath = fileZipInfo.getLocalPath().substring(fileZipInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN) + MERCH_REPLACE_MARK_PATTERN.length()) + "/";
									File memberLocaldir = new File(memberLocalPath);
									if ( memberLocaldir.listFiles() != null ){
										for (File merchfile : memberLocaldir.listFiles()){
											merchfile = new File( merchfile.getAbsoluteFile() + "/" +  InnermostPath);
											if (merchfile.isDirectory() && merchfile.listFiles() != null) {
						                        for ( File ls : merchfile.listFiles())
						                        {
						                            log.info("ls:" + ls.getAbsolutePath());
						                            if (ls.isFile() && isMatchFile(ls.getName())) {
						                                if (hasOKFile(ls)) {
						                                    if (checkFileSize && isSizeOK(ls))
						                                        matchFiles.add(ls);
						                                    else
						                                        matchFiles.add(ls);
						                                }
						                            }
						                        }
						                    }
											else
												log.info("ls is null!");
										}
									}
									else
										log.info(memberLocalPath +" is null!");
								}
								else{
									memberfile = new File( memberfile.getAbsoluteFile() + "/" +  InnermostPath);
									if ( memberfile.isDirectory() && memberfile.listFiles() != null ){
										for ( File ls : memberfile.listFiles())
				                        {
				                            log.info("ls:" + ls.getAbsolutePath());
				                            if (ls.isFile() && isMatchFile(ls.getName())) {
				                                if (hasOKFile(ls)) {
				                                    if (checkFileSize && isSizeOK(ls))
				                                        matchFiles.add(ls);
				                                    else
				                                        matchFiles.add(ls);
				                                }
				                            }
				                        }
									}
								}
							}
						}
						else
							log.info(memberGrouptLocalPath +" is null!");
					}
					else{
						memberGroupfile = new File( memberGroupfile.getAbsoluteFile() + "/" +  InnermostPath);
						if ( memberGroupfile.isDirectory() && memberGroupfile.listFiles() != null ){
							for ( File ls : memberGroupfile.listFiles())
	                        {
	                            log.info("ls:" + ls.getAbsolutePath());
	                            if (ls.isFile() && isMatchFile(ls.getName())) {
	                                if (hasOKFile(ls)) {
	                                    if (checkFileSize && isSizeOK(ls))
	                                        matchFiles.add(ls);
	                                    else
	                                        matchFiles.add(ls);
	                                }
	                            }
	                        }
						}
					}
				}
    		}
    		else
    			log.info(parentLocalPath +" is null!");
    			
    	}
    	else if (fileZipInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)
    			&& !fileZipInfo.getLocalPath().matches(MEMBER_GROUP_REPLACE_PATTERN)){

    		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + '/') + (fileZipInfo.getLocalPath().substring(0, fileZipInfo.getLocalPath().indexOf(MEMBER_REPLACE_MARK_PATTERN)) + '/'));
    		InnermostPath = fileZipInfo.getLocalPath().substring(fileZipInfo.getLocalPath().indexOf(MEMBER_REPLACE_MARK_PATTERN) + MEMBER_REPLACE_MARK_PATTERN.length()) + "/";
    		File parentLocaldir = new File(parentLocalPath);
			
			if ( parentLocaldir.listFiles() != null ){
				for (File memberfile : parentLocaldir.listFiles()){
					
					if (jobMemIds != null){
						if (!jobMemIds.contains(memberfile.getName())){
							continue;
						}
					}
					
					if (fileZipInfo.getLocalPath().matches(MERCH_REPLACE_PATTERN)){
						String memberLocalPath = parentLocaldir + memberfile.getName() + "/";
						InnermostPath = fileZipInfo.getLocalPath().substring(fileZipInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN) + MERCH_REPLACE_MARK_PATTERN.length()) + "/";
						File memberLocaldir = new File(memberLocalPath);
						if ( memberLocaldir.listFiles() != null ){
							for (File merchfile : memberLocaldir.listFiles()){
								merchfile = new File( merchfile.getAbsoluteFile() + "/" +  InnermostPath);
								if (merchfile.isDirectory() && merchfile.listFiles() != null) {
			                        for ( File ls : merchfile.listFiles())
			                        {
			                            log.info("ls:" + ls.getAbsolutePath());
			                            if (ls.isFile() && isMatchFile(ls.getName())) {
			                                if (hasOKFile(ls)) {
			                                    if (checkFileSize && isSizeOK(ls))
			                                        matchFiles.add(ls);
			                                    else
			                                        matchFiles.add(ls);
			                                }
			                            }
			                        }
			                    }
							}
						}
					}
					else{
						memberfile = new File(memberfile.getAbsoluteFile() + InnermostPath );
						if ( memberfile.isDirectory() && memberfile.listFiles() != null ){
							for ( File ls : memberfile.listFiles())
	                        {
	                            log.info("ls:" + ls.getAbsolutePath());
	                            if (ls.isFile() && isMatchFile(ls.getName())) {
	                                if (hasOKFile(ls)) {
	                                    if (checkFileSize && isSizeOK(ls))
	                                        matchFiles.add(ls);
	                                    else
	                                        matchFiles.add(ls);
	                                }
	                            }
	                        }
						}
					}
				}
			}
    	}
    	else if (fileZipInfo.getLocalPath().matches(MERCH_REPLACE_PATTERN)
    			&& !fileZipInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)
    			&& !fileZipInfo.getLocalPath().matches(MEMBER_GROUP_REPLACE_PATTERN)){

    		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + '/') + (fileZipInfo.getLocalPath().substring(0, fileZipInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN)) + '/'));
    		InnermostPath = fileZipInfo.getLocalPath().substring(fileZipInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN) + MERCH_REPLACE_MARK_PATTERN.length()) + "/";
    		File parentLocaldir = new File(parentLocalPath + InnermostPath);
    		
			if ( parentLocaldir.listFiles() != null ){
				for (File merchfile : parentLocaldir.listFiles()){
					if (merchfile.isDirectory() && merchfile.listFiles() != null) {
                        for ( File ls : merchfile.listFiles())
                        {
                            log.info("ls:" + ls.getAbsolutePath());
                            if (ls.isFile() && isMatchFile(ls.getName())) {
                                if (hasOKFile(ls)) {
                                    if (checkFileSize && isSizeOK(ls))
                                        matchFiles.add(ls);
                                    else
                                        matchFiles.add(ls);
                                }
                            }
                        }
                    }
				}
			}
    	}
    	else{
    		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + '/') + (fileZipInfo.getLocalPath() + '/'));
    		File parentLocaldir = new File(parentLocalPath);
    		if ( parentLocaldir.listFiles() != null){
	    		for ( File ls : parentLocaldir.listFiles())
	            {
	                log.info("ls:" + ls.getAbsolutePath());
	                if (ls.isFile() && isMatchFile(ls.getName())) {
	                    if (hasOKFile(ls)) {
	                        if (checkFileSize && isSizeOK(ls))
	                            matchFiles.add(ls);
	                        else
	                            matchFiles.add(ls);
	                    }
	                }
	            }
    		}
    		else
    			log.info("ls is null!");
    	}
    	log.info("matchFiles.size: " + matchFiles.size() );
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
            //log.info(allFileInfos.size());
            // 要加 fileTypes 的設定, 沒設, 都處理, 有設, 只處理有設的 fileTypes (tracy 建議)
            filterFileInfos(allFileInfos);
            tempDir = BatchUtil.getTempDirectory();
            if (StringUtil.isEmpty(tempDir)) {
            	throw new Exception("no tempDir setting!");
            }
            tempDir = normalFileSeparator(tempDir);
            
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
		
		
		//JOB_ID、JOB_TIME、MEM_ID
        jobMemIds = null;
    	
        log.debug("getBatchResultInfo().getJobId(): "+getBatchResultInfo().getJobId());
        log.debug("getBatchResultInfo().getJobTime(): " + getBatchResultInfo().getJobTime());
        log.debug("getBatchResultInfo().getMemId(): " + getBatchResultInfo().getMemId());
        
    	if (Layer1Constants.JOB_ID_DEFAULT.equalsIgnoreCase(getBatchResultInfo().getJobId())
		&& Layer1Constants.JOB_TIME_DEFAULT.equalsIgnoreCase(getBatchResultInfo().getJobTime())
		&& StringUtil.isEmpty(getBatchResultInfo().getMemId())){
    		//如無相關設定則為null
    		log.debug("JobId & JobTime & memId are null.");
    	}
    	else{
    		
    		jobMemIds = new ArrayList();
    		
    		StringBuffer sql = new StringBuffer();
    		sql.append("SELECT MEM_ID FROM TB_MEMBER WHERE 1=1");
    		
    		if (Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
    			sql.append(" AND JOB_ID IS NULL");
        		sql.append(" AND JOB_TIME IS NULL");
    		}
    		else{
	        	if(!StringUtil.isEmpty(getBatchResultInfo().getJobId()) 
				&& !getBatchResultInfo().getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
	        		sql.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobId()));
		    		if(!StringUtil.isEmpty(getBatchResultInfo().getJobTime()) 
					&& !getBatchResultInfo().getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
		    			sql.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobTime()));
			    	}
		    	}
		    	if(!StringUtil.isEmpty(getBatchResultInfo().getMemId())){
		    		sql.append(" AND MEM_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getMemId()));
		    	}
    		}
    		log.debug("jobMemSql: "+sql.toString());
    		
            Statement feeStmt = null;
            ResultSet feeRs = null;
            Connection conn = null;
            
            try {
            	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            	feeStmt = conn.createStatement();
            	feeRs = feeStmt.executeQuery(sql.toString());
            	while (feeRs.next()) {
            		jobMemIds.add(feeRs.getString(1));
            	}
            }
            finally {
                ReleaseResource.releaseDB(conn, feeStmt, feeRs);
            }
    		
    	}
	}
	
	protected File getDestFile(File matchFile) {
        String srcFullName = normalFileSeparator(matchFile.getAbsolutePath());
        String destFullName = srcFullName.replaceAll(tempDir, workDir);
        return new File(destFullName);
    }
	
	//過濾不需處理的設定、抓出需ZIP的設定 及 ZIP後的設定
	protected void filterFileInfos(Vector allFileInfos) {
        if (filenameBeans == null || filenameBeans.size() == 0) {
            // 設定檔沒有就不做unzip
        	log.warn("--------- No have filenameBeans. ---------");
        }
        else {

        	for (int j = 0; j < filenameBeans.size(); j++)
            {
        		HashMap scratch = new HashMap();
        		FilenameBean filenameBean = (FilenameBean) filenameBeans.get(j);
        		String fileName = filenameBean.getFileName();
        		String fileNameZip = filenameBean.getFileNameZip();
        		for (int i = 0; i < allFileInfos.size(); i++)
                {
        			TbFileInfoInfo fileInfo = (TbFileInfoInfo) allFileInfos.get(i);
        			if ( fileInfo.getFileName().equals(fileName) || 
        					fileInfo.getFileName().equals(fileNameZip)){
        				scratch.put(fileInfo.getFileName(), fileInfo);
        			}
                }
        		
        		//需同時有ZipFileInfo、FileInfo設定才處理。
        		if ( scratch.size() == 2 ){
        			fileNameZip2fileName.put(fileNameZip, fileName);
        			fileName2info.putAll(scratch);
        		}
        		else{
        			log.warn(fileName + ", " + fileNameZip + " Setting Incomplete. scratch:[" + scratch + "]");
        		}
        			
            }
        }
    }
	
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
        if (Layer1Constants.OKFLAG_NOCHECK.equals(fileZipInfo.getOkFlag())) {
            // 不用檢查 OK file
            return true;
        }
        else if (Layer1Constants.OKFLAG_CHECK.equals(fileZipInfo.getOkFlag())) {
            // 要檢查 OK file
        	String subFileName = "";
        	String file = "";
        	if (fileZipInfo.getSubFileName().contains("/")){
        		subFileName = fileZipInfo.getSubFileName().replaceAll("/", "");
        		file = f.getAbsolutePath().substring(0,f.getAbsolutePath().lastIndexOf(".")) + subFileName;
        	}
        	else{
        		subFileName = fileZipInfo.getSubFileName();
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
            log.warn("unknown OK_FLAG(" + fileZipInfo.getOkFlag() + ")! suppose no check OK file!");
            return true;
        }
    }
	protected boolean isMatchFile(String fn) {
        boolean ret = false;
        Pattern p = Pattern.compile(fileZipInfo.getFileNamePattern());
        Matcher m = p.matcher(fn);
        ret = m.matches();
        log.info("fn:[" + fn + "], FileNamePattern:["+ fileZipInfo.getFileNamePattern() +"] matched:" + ret);
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
	public String getTempDir() {
		return tempDir;
	}
	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
	public List getFilenameBeans() {
		return filenameBeans;
	}
	public void setFilenameBeans(List filenameBeans) {
		this.filenameBeans = filenameBeans;
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
                            "config/batch/TrnUnZip/spring.xml");
            TrnUnZip trnUnZip = (TrnUnZip) apContext.getBean("trnUnZip");
            trnUnZip.setBatchDate(batchDate);
            //filesIn.process(args);
            trnUnZip.run(args);
        }
        catch (Exception e) {
            log.warn("get spring bean error:" + e.getMessage(), e);
            System.exit(-1);
        }
        System.exit(0);
    }
}
