package tw.com.hyweb.svc.yhdp.batch.splitfile.impTrans;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.svc.yhdp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.StringUtils;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbSourceInctlInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbSourceInctlMgr;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class SplitImpTrans extends AbstractBatchBasic
{
    private static Logger logger = Logger.getLogger(SplitImpTrans.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "SplitFile" + File.separator + "SplitImpTxnDtl" + File.separator + "spring.xml";
    
    private String fileName = null;
    private String deletefileName = null;
    private boolean deleteSpiltFile = true;
    private boolean spiltFile = true;
    private int maxFileRecCount = 0;
    protected String batchDate = "";
    
    protected final String SPECIAL_ALLMEMID = "00000000";
    protected final String DEFAULT_MEMID = "99999999";
    protected final String DEFAULT_MEMGROUPID = "99999";
    protected final String DEFAULT_MERCHID = "999999999999999";

    //protected String okFilePending = ".OK";
    protected String renamePending = ".MOVED";
    // checkFileSize: 是否要加檢查檔案大小是否整除 TB_FILE_INFO.DATA_LEN
    protected boolean checkFileSize = true;
    // renameAfterCopy: 檔案從 temp 目錄 copy 到 work 目錄成功後, temp 目錄中的檔案是否要做 rename 的動作
    protected boolean renameAfterCopy = true;
    // deleteAfterCopy: 檔案從 temp 目錄 copy 到 work 目錄成功後, temp 目錄中的檔案是否要做刪除的動作
    protected boolean deleteAfterCopy = false;
    // 存放如何 parse memId, fileDate, sno 的規則, 每個 TB_FILE_INFO.FILE_NAME 應該都有相對應的一筆設定
    // each object is FilenameBean object
    protected List filenameBeans = new ArrayList();

    // 要加 fileTypes 的設定, 沒設, 都處理, 有設, 只處理有設的 fileTypes
    protected List fileTypes = new ArrayList();

    // 所有 TB_FILE_INFO.IN_OUT = 'I' 的資料
    // each object is TbFileInfoInfo object
    protected Vector fileInfos = null;
    protected Connection conn = null;

    protected String tempDir = "";
    protected String workDir = "";

    protected String sysDate = DateUtil.getTodayString().substring(0, 8);
    protected String sysTime = DateUtil.getTodayString().substring(8, 14);
    // for current run
    protected SplitFilenameBean filenameBean = null;
    protected TbFileInfoInfo fileInfo = null;
    // 記錄 LOCAL_PATH 是否有 "00000000", 有的話記錄從前面算起第幾個, 沒有的話記錄 -1
    // ex: in/CASE1/00000000/, pos = 2
    // ex: in/00000000/CASE2/, pos = 1
    protected int memIdpos = -1;
    protected int memGroupIdpos = -1;
    protected int merchIdpos = -1;
    // 若 LOCAL_PATH 有包含 "00000000", 記錄 "00000000" 之前的字串
    // ex: in/CASE1/00000000/, prefix = in/CASE1
    // ex: in/00000000/CASE2/, prefix = in
    protected String prefix = "";
    // match regular expression files
    protected List matchFiles = null;

	private static final String MEMBER_REPLACE_PATTERN = ".*0{8}.*";
    private static final String MEMBER_REPLACE_MARK_PATTERN = "00000000";
    private static final String MEMBER_GROUP_REPLACE_PATTERN = ".*2{5}.*";
    private static final String MEMBER_GROUP_REPLACE_MARK_PATTERN = "22222";
    private static final String MERCH_REPLACE_PATTERN = ".*1{15}.*";
    private static final String MERCH_REPLACE_MARK_PATTERN = "111111111111111";
    
    private List jobMemIds;
    
    /*public String getOkFilePending()
    {
        return okFilePending;
    }

    public void setOkFilePending(String okFilePending)
    {
        this.okFilePending = okFilePending;
    }*/

    public List getFileTypes()
    {
        return fileTypes;
    }

    public void setFileTypes(List fileTypes)
    {
        this.fileTypes = fileTypes;
    }

    public String getRenamePending()
    {
        return renamePending;
    }

    public void setRenamePending(String renamePending)
    {
        if (StringUtil.isEmpty(renamePending)) {
            return;
        }
        this.renamePending = renamePending;
    }

    public String getBatchDate()
    {
        return batchDate;
    }

    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

    public boolean isRenameAfterCopy()
    {
        return renameAfterCopy;
    }

    public void setRenameAfterCopy(boolean renameAfterCopy)
    {
        this.renameAfterCopy = renameAfterCopy;
    }

    public boolean isDeleteAfterCopy()
    {
        return deleteAfterCopy;
    }

    public void setDeleteAfterCopy(boolean deleteAfterCopy)
    {
        this.deleteAfterCopy = deleteAfterCopy;
    }

    public List getFilenameBeans()
    {
        return filenameBeans;
    }

    public void setFilenameBeans(List filenameBeans)
    {
        this.filenameBeans = filenameBeans;
    }

    public void addFilenameBean(SplitFilenameBean filenameBean) {
        if (filenameBean != null) {
            this.filenameBeans.add(filenameBean);
        }
    }

    protected static String normalFileSeparator(String fn) {
        return fn.replace('\\', '/');
    }

    protected void filterFileInfos(Vector allFileInfos) {
        if (fileTypes == null || fileTypes.size() == 0) {
            // no filter
            fileInfos = allFileInfos;
        }
        else {
            // filter allFileInfos
            fileInfos = new Vector();
            for (int i = 0; i < allFileInfos.size(); i++)
            {
                TbFileInfoInfo fileInfo = (TbFileInfoInfo) allFileInfos.get(i);
                // 符合 fileTypes 設定才加入要處理
                boolean isAdd = false;
                for (int j = 0; j < fileTypes.size(); j++)
                {
                    String fileType = (String) fileTypes.get(j);
                    if (fileType.equals(fileInfo.getFileType())) {
                        isAdd = true;
                        fileInfos.add(fileInfo);
                        break;
                    }
                }
                if (!isAdd) {
                    logger.info("fileInfo for '" + fileInfo.getFileName() + "' not in " + fileTypes + "! no handle this fileInfo!");
                }
            }
        }
    }

    public void initial() throws Exception {
        
        try {
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            TbFileInfoInfo qinfo = new TbFileInfoInfo();
            qinfo.toEmpty();
            qinfo.setInOut(Layer1Constants.INOUT_IN);
            if (!StringUtil.isEmpty(fileName)) {
                logger.info("input fileName:" + fileName);
                qinfo.setFileName(fileName);
            }
            Vector allFileInfos = new Vector();
            TbFileInfoMgr mgr = new TbFileInfoMgr(conn);
            mgr.queryMultiple(qinfo, allFileInfos);
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

            if (renameAfterCopy && deleteAfterCopy) {
                // 二選一, (renameAfterCopy, deleteAfterCopy) = (true, true) -> (false, true)
                renameAfterCopy = false;
                deleteAfterCopy = true;
            }
            deleteFileName();
        }
        catch (Exception ignore) {
            logger.warn("initial error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
        
        //JOB_ID、JOB_TIME、MEM_ID
        jobMemIds = null;
    	
    	if (Layer1Constants.JOB_ID_DEFAULT.equalsIgnoreCase(getBatchResultInfo().getJobId())
		&& Layer1Constants.JOB_TIME_DEFAULT.equalsIgnoreCase(getBatchResultInfo().getJobTime())
		&& StringUtil.isEmpty(getBatchResultInfo().getMemId())){
    		//如無相關設定則為null
    		;
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
    		
            Statement feeStmt = null;
            ResultSet feeRs = null;
            
            try {
            	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            	feeStmt = conn.createStatement();
            	logger.debug("jobMemSql: "+sql.toString());
            	feeRs = feeStmt.executeQuery(sql.toString());
            	while (feeRs.next()) {
            		jobMemIds.add(feeRs.getString(1));
            	}
            }
            finally {
                ReleaseResource.releaseDB(null, feeStmt, feeRs);
            }
    		
    	}
        
    }

    protected void setFilenameBean() {
        filenameBean = null;
        for (int i = 0; i < filenameBeans.size(); i++)
        {
            SplitFilenameBean bean = (SplitFilenameBean) filenameBeans.get(i);
            if (fileInfo.getFileName().equals(bean.getFileName())) {
                filenameBean = bean;
                break;
            }
        }
    }

    protected void computePos() {
        // 記錄 LOCAL_PATH 是否有 "00000000", 有的話記錄從前面算起第幾個, 沒有的話記錄 -1
        // ex: in/CASE1/00000000/, pos = 2
        // ex: in/00000000/CASE2/, pos = 1
        // 若 LOCAL_PATH 有包含 "00000000", 記錄 "00000000" 之前的字串
        // ex: in/CASE1/00000000/, prefix = in/CASE1
        // ex: in/00000000/CASE2/, prefix = in
    	memGroupIdpos = -1;
    	memIdpos = -1;
    	merchIdpos = -1;
        String tmp = normalFileSeparator(fileInfo.getLocalPath());
        String[] tokens = tmp.split("/");
        for (int i = 0; i < tokens.length; i++) {
        	if (tokens[i].equals(MEMBER_GROUP_REPLACE_MARK_PATTERN)) {
        		memGroupIdpos = i;
        	}
            if (tokens[i].equals(MEMBER_REPLACE_MARK_PATTERN)) {
                memIdpos = i;
            }
            if (tokens[i].equals(MERCH_REPLACE_MARK_PATTERN)) {
                merchIdpos = i;
            }
        }
    }

    protected boolean isMatchFile(String fn) {
        boolean ret = false;
        Pattern p = Pattern.compile(fileInfo.getFileNamePattern());
        Matcher m = p.matcher(fn);
        ret = m.matches();
        logger.info("fn:" + fn + " matched:" + ret + " Pattern:" + fileInfo.getFileNamePattern());
        return ret;
    }
    
    public boolean isMatchFile(String fn, TbFileInfoInfo fileInfo) {
        boolean ret = false;
        Pattern p = Pattern.compile(fileInfo.getFileNamePattern());
        Matcher m = p.matcher(fn);
        ret = m.matches();
        logger.info("fn:" + fn + " matched:" + ret);
        return ret;
    }

    private static long MAX_SIZE = 10000000000L;
    protected boolean isSizeOK(File f) {
        /*if (f.length() >= MAX_SIZE) {
            logger.warn("file '" + f.getAbsolutePath() + "' size '" + f.length() + "' too large! ignore!");
            return false;
        }
        if (fileInfo.getDataLen().intValue() != 0) {
            if ((f.length() % fileInfo.getDataLen().intValue()) == 0) {
                return true;
            }
            else {
                logger.warn("f.length() % fileInfo.getDataLen().intValue() != 0! ignore!");
                return false;
            }
        }
        else {
            return true;
        }*/
        return true;
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
        	logger.warn("unknown OK_FLAG(" + fileInfo.getOkFlag() + ")! suppose no check OK file!");
            return true;
        }
    }
	protected void setMatchFiles() throws SQLException {
    	matchFiles = new ArrayList();
    	String InnermostPath = "";
    	if (fileInfo.getLocalPath().matches(MEMBER_GROUP_REPLACE_PATTERN))
    	{
    		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + '/') + (fileInfo.getLocalPath().substring(0, fileInfo.getLocalPath().indexOf(MEMBER_GROUP_REPLACE_MARK_PATTERN)) + '/'));
    		InnermostPath = fileInfo.getLocalPath().substring(fileInfo.getLocalPath().indexOf(MEMBER_GROUP_REPLACE_MARK_PATTERN) + MEMBER_GROUP_REPLACE_MARK_PATTERN.length()) + "/";
    		File parentLocaldir = new File(parentLocalPath);
    		if (parentLocaldir.listFiles() != null){
				for (File memberGroupfile : parentLocaldir.listFiles()){
					if (fileInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)){
						String memberGrouptLocalPath = parentLocalPath + memberGroupfile.getName() + "/";
						InnermostPath = fileInfo.getLocalPath().substring(fileInfo.getLocalPath().indexOf(MEMBER_REPLACE_MARK_PATTERN) + MEMBER_REPLACE_MARK_PATTERN.length()) + "/";
						File memberGrouptLocaldir = new File(memberGrouptLocalPath);
						if ( memberGrouptLocaldir.listFiles() != null ){
							for (File memberfile : memberGrouptLocaldir.listFiles()){
								
								if (jobMemIds != null){
									if (!jobMemIds.contains(memberfile.getName())){
										continue;
									}
								}
								
								if (fileInfo.getLocalPath().matches(MERCH_REPLACE_PATTERN)){
									String memberLocalPath = memberGrouptLocalPath + memberfile.getName() + "/";
									InnermostPath = fileInfo.getLocalPath().substring(fileInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN) + MERCH_REPLACE_MARK_PATTERN.length()) + "/";
									File memberLocaldir = new File(memberLocalPath);
									if ( memberLocaldir.listFiles() != null ){
										for (File merchfile : memberLocaldir.listFiles()){
											merchfile = new File( merchfile.getAbsoluteFile() + "/" +  InnermostPath);
											if (merchfile.isDirectory() && merchfile.listFiles() != null) {
						                        for ( File ls : merchfile.listFiles())
						                        {
						                        	logger.info("ls:" + ls.getAbsolutePath());
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
												logger.info("ls is null!");
										}
									}
									else
										logger.info(memberLocalPath +" is null!");
								}
								else{
									memberfile = new File( memberfile.getAbsoluteFile() + "/" +  InnermostPath);
									if ( memberfile.isDirectory() && memberfile.listFiles() != null ){
										for ( File ls : memberfile.listFiles())
				                        {
											logger.info("ls:" + ls.getAbsolutePath());
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
							logger.info(memberGrouptLocalPath +" is null!");
					}
					else{
						memberGroupfile = new File( memberGroupfile.getAbsoluteFile() + "/" +  InnermostPath);
						if ( memberGroupfile.isDirectory() && memberGroupfile.listFiles() != null ){
							for ( File ls : memberGroupfile.listFiles())
	                        {
								logger.info("ls:" + ls.getAbsolutePath());
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
    			logger.info(parentLocalPath +" is null!");
    			
    	}
    	else if (fileInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)
    			&& !fileInfo.getLocalPath().matches(MEMBER_GROUP_REPLACE_PATTERN)){

    		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + '/') + (fileInfo.getLocalPath().substring(0, fileInfo.getLocalPath().indexOf(MEMBER_REPLACE_MARK_PATTERN)) + '/'));
    		InnermostPath = fileInfo.getLocalPath().substring(fileInfo.getLocalPath().indexOf(MEMBER_REPLACE_MARK_PATTERN) + MEMBER_REPLACE_MARK_PATTERN.length()) + "/";
    		File parentLocaldir = new File(parentLocalPath);
			
			if ( parentLocaldir.listFiles() != null ){
				for (File memberfile : parentLocaldir.listFiles()){
					
					if (jobMemIds != null){
						if (!jobMemIds.contains(memberfile.getName())){
							continue;
						}
					}
					
					if (fileInfo.getLocalPath().matches(MERCH_REPLACE_PATTERN)){
						String memberLocalPath = parentLocaldir + memberfile.getName() + "/";
						InnermostPath = fileInfo.getLocalPath().substring(fileInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN) + MERCH_REPLACE_MARK_PATTERN.length()) + "/";
						File memberLocaldir = new File(memberLocalPath);
						if ( memberLocaldir.listFiles() != null ){
							for (File merchfile : memberLocaldir.listFiles()){
								merchfile = new File( merchfile.getAbsoluteFile() + "/" +  InnermostPath);
								if (merchfile.isDirectory() && merchfile.listFiles() != null) {
			                        for ( File ls : merchfile.listFiles())
			                        {
			                        	logger.info("ls:" + ls.getAbsolutePath());
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
								logger.info("ls:" + ls.getAbsolutePath());
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
    	else if (fileInfo.getLocalPath().matches(MERCH_REPLACE_PATTERN)
    			&& !fileInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)
    			&& !fileInfo.getLocalPath().matches(MEMBER_GROUP_REPLACE_PATTERN)){

    		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + '/') + (fileInfo.getLocalPath().substring(0, fileInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN)) + '/'));
    		InnermostPath = fileInfo.getLocalPath().substring(fileInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN) + MERCH_REPLACE_MARK_PATTERN.length()) + "/";
    		File parentLocaldir = new File(parentLocalPath + InnermostPath);
    		
			if ( parentLocaldir.listFiles() != null ){
				for (File merchfile : parentLocaldir.listFiles()){
					if (merchfile.isDirectory() && merchfile.listFiles() != null) {
                        for ( File ls : merchfile.listFiles())
                        {
                        	logger.info("ls:" + ls.getAbsolutePath());
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
    		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + '/') + (fileInfo.getLocalPath() + '/'));
    		File parentLocaldir = new File(parentLocalPath);
    		if ( parentLocaldir.listFiles() != null){
	    		for ( File ls : parentLocaldir.listFiles())
	            {
	    			logger.info("ls:" + ls.getAbsolutePath());
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
    		else{
    			logger.info("ls is null!");
    		}
    	}
    }
    protected File getDestFile(File matchFile) {
        String srcFullName = normalFileSeparator(matchFile.getAbsolutePath());
        String destFullName = srcFullName.replaceAll(tempDir, workDir);
        return new File(destFullName);
    }

    protected String determineMemId(File matchFile) {
        String memId = DEFAULT_MEMID;
        if (filenameBean.getMemIdStart() != -1 && filenameBean.getMemIdEnd() != -1) {
            // 有自己設定取 memIdStart, memIdEnd
            if (memIdpos != -1) {
                // 有找到 00000000, 又有自己設定取 memIdStart, memIdEnd, 使用 00000000 方式決定
                String temp = normalFileSeparator(matchFile.getAbsolutePath());
                temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                String[] tokens = temp.split("/");
                memId = tokens[memIdpos];
            }
            else {
                // 沒有找到 00000000, 使用自己設定取 memIdStart, memIdEnd
                memId = matchFile.getName().substring(filenameBean.getMemIdStart(), filenameBean.getMemIdEnd());
            }
        }
        else if (filenameBean.getMemIdStart() == -1 && filenameBean.getMemIdEnd() == -1) {
            // 沒有自己設定取 memIdStart, memIdEnd
            if (memIdpos != -1) {
                // 有找到 00000000, 又沒有自己設定取 memIdStart, memIdEnd, 使用 00000000 方式決定
                String temp = normalFileSeparator(matchFile.getAbsolutePath());
                temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                //temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                String[] tokens = temp.split("/");
                memId = tokens[memIdpos];
            }
            else {
                // 沒有找到 00000000, 使用 99999999
                memId = DEFAULT_MEMID;
            }
        }
        return memId;
    }

    protected String determineMemGroupId(File matchFile) {
        String memGroupId = DEFAULT_MEMGROUPID;
        if (filenameBean.getMemGroupIdStart() != -1 && filenameBean.getMemGroupIdEnd() != -1) {
            // 有自己設定取 memGroupIdStart, memGroupIdEnd
            if (memGroupIdpos != -1) {
                // 有找到 00000000, 又有自己設定取 memGroupIdStart, memGroupIdEnd, 使用 00000000 方式決定
                String temp = normalFileSeparator(matchFile.getAbsolutePath());
                temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                String[] tokens = temp.split("/");
                memGroupId = tokens[memGroupIdpos];
            }
            else {
                // 沒有找到 00000000, 使用自己設定取 memGroupIdStart, memGroupIdEnd
                memGroupId = matchFile.getName().substring(filenameBean.getMemGroupIdStart(), filenameBean.getMemGroupIdEnd());
            }
        }
        else if (filenameBean.getMemGroupIdStart() == -1 && filenameBean.getMemGroupIdEnd() == -1) {
            // 沒有自己設定取 memGroupIdStart, memGroupIdEnd
            if (memGroupIdpos != -1) {
                // 有找到 00000000, 又沒有自己設定取 memGroupIdStart, memGroupIdEnd, 使用 00000000 方式決定
                String temp = normalFileSeparator(matchFile.getAbsolutePath());
                temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                //temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                String[] tokens = temp.split("/");
                memGroupId = tokens[memGroupIdpos];
            }
            else {
                // 沒有找到 00000000, 使用 99999999
                memGroupId = DEFAULT_MEMGROUPID;
            }
        }
        return memGroupId;
    }
    
    protected String determineMerchId(File matchFile) {
        String merchId = DEFAULT_MERCHID;
        if (filenameBean.getMerchIdStart() != -1 && filenameBean.getMerchIdEnd() != -1) {
            // 有自己設定取 merchIdStart, merchIdEnd
            if (merchIdpos != -1) {
                // 有找到 00000000, 又有自己設定取 merchIdStart, merchIdEnd, 使用 00000000 方式決定
                String temp = normalFileSeparator(matchFile.getAbsolutePath());
                temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                String[] tokens = temp.split("/");
                merchId = tokens[merchIdpos];
            }
            else {
                // 沒有找到 00000000, 使用自己設定取 merchIdStart, merchIdEnd
                merchId = matchFile.getName().substring(filenameBean.getMerchIdStart(), filenameBean.getMerchIdEnd());
            }
        }
        else if (filenameBean.getMerchIdStart() == -1 && filenameBean.getMerchIdEnd() == -1) {
            // 沒有自己設定取 merchIdStart, merchIdEnd
            if (merchIdpos != -1) {
                // 有找到 00000000, 又沒有自己設定取 merchIdStart, merchIdEnd, 使用 00000000 方式決定
                String temp = normalFileSeparator(matchFile.getAbsolutePath());
                temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                //temp = temp.substring(temp.indexOf(tempDir)+tempDir.length());
                String[] tokens = temp.split("/");
                merchId = tokens[merchIdpos];
            }
            else {
                // 沒有找到 00000000, 使用 99999999
                merchId = DEFAULT_MERCHID;
            }
        }
        return merchId;
    }
    
    protected String getNewSeqno(String memId, String fileName, String fileDate) {
        String sql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_INCTL WHERE MEM_ID = "
            + StringUtil.toSqlValueWithSQuote(memId) + " AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileName) + " AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(fileDate);
        String seqno = "01";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                seqno = rs.getString(1);
            }
        }
        catch (Exception ignore) {
            logger.warn("sql:" + sql);
            logger.warn("getNewSeqno error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return seqno;
    }

    protected TbInctlInfo getInctlInfo(File matchFile, String memGroupId, String memId, String merchId) {
        TbInctlInfo inctlInfo = new TbInctlInfo();
        inctlInfo.setMemGroupId(memGroupId);
        inctlInfo.setMemId(memId);
        inctlInfo.setMerchId(merchId);
        inctlInfo.setFileName(fileInfo.getFileName());
        String fileDate = matchFile.getName().substring(filenameBean.getFileDateStart(), filenameBean.getFileDateEnd());
        if (fileDate.length() == 6) {
            String year = "" + DateUtil.getYear();
            fileDate = year.substring(0, 2) + fileDate;
        }
        inctlInfo.setFileDate(fileDate);
        if (filenameBean.getSeqnoStart() == -1 && filenameBean.getSeqnoEnd() == -1) {
            String seqno = getNewSeqno(memId, filenameBean.getFileName(), fileDate);
            logger.info("getNewSeqno:" + seqno);
            inctlInfo.setSeqno(seqno);
        }
        else {
            inctlInfo.setSeqno(matchFile.getName().substring(filenameBean.getSeqnoStart(), filenameBean.getSeqnoEnd()));
        }
        inctlInfo.setFileType(fileInfo.getFileType());
        inctlInfo.setWorkFlag(Layer1Constants.WORKFLAG_INWORK);
        inctlInfo.setTotRec(new Integer(0));
        inctlInfo.setRecCnt(new Integer(0));
        inctlInfo.setSucCnt(new Integer(0));
        inctlInfo.setFailCnt(new Integer(0));
        inctlInfo.setFileSize(new Integer((int) matchFile.length()));
        inctlInfo.setFullFileName(matchFile.getName());
        inctlInfo.setSysDate(sysDate);
        inctlInfo.setSysTime(sysTime);
        inctlInfo.setParMon(inctlInfo.getFileDate().substring(4, 6));
        inctlInfo.setParDay(inctlInfo.getFileDate().substring(6, 8));
        return inctlInfo;
    }

    protected TbSourceInctlInfo getSourceInctlInfo(File matchFile, String memId) {
        TbSourceInctlInfo inctlInfo = new TbSourceInctlInfo();
        inctlInfo.setMemId(memId);
        String fileDate = matchFile.getName().substring(filenameBean.getFileDateStart(), filenameBean.getFileDateEnd());
        if (fileDate.length() == 6) {
            String year = "" + DateUtil.getYear();
            fileDate = year.substring(0, 2) + fileDate;
        }
        inctlInfo.setFileType(fileInfo.getFileType());
        inctlInfo.setFullFileName(matchFile.getName());
        inctlInfo.setPartFullFileName(matchFile.getName()+"01");
        inctlInfo.setSysDate(sysDate);
        inctlInfo.setSysTime(sysTime);
        return inctlInfo;
    }
    
    public void handleMatchFiles() {
        logger.debug("matchFiles:"+matchFiles.size());
        for (int i = 0; i < matchFiles.size(); i++)
        {
            File matchFile = (File) matchFiles.get(i);
            File destFile = getDestFile(matchFile);
            try {
                // 依 pos, prefix, filenameBean, matchFile 來決定 memId
            	String memId = determineMemId(matchFile);
            	String memGroupId = determineMemGroupId(matchFile);
            	String merchId = determineMerchId(matchFile);
            	if (memGroupIdpos != -1 && filenameBean.getMemGroupIdStart() != -1 && filenameBean.getMemGroupIdEnd() != -1) {
                    // 要檢查此二個 MEM_GROUP_ID 是否一致
                    String fileMemGroupId = matchFile.getName().substring(filenameBean.getMemGroupIdStart(), filenameBean.getMemGroupIdEnd());
                    if (!memGroupId.equals(fileMemGroupId)) {
                    	logger.warn("memGroupId:" + memGroupId + " fileMemGroupId:" + fileMemGroupId + " not matched! this file '" + matchFile + "' ignored!");
                        continue;
                    }
                }
            	if (memIdpos != -1 && filenameBean.getMemIdStart() != -1 && filenameBean.getMemIdEnd() != -1) {
                    // 要檢查此二個 MEM_ID 是否一致
                    String fileMemId = matchFile.getName().substring(filenameBean.getMemIdStart(), filenameBean.getMemIdEnd());
                    if (!memId.equals(fileMemId)) {
                    	logger.warn("memId:" + memId + " fileMemId:" + fileMemId + " not matched! this file '" + matchFile + "' ignored!");
                        continue;
                    }
                }
            	if (merchIdpos != -1 && filenameBean.getMerchIdStart() != -1 && filenameBean.getMerchIdEnd() != -1) {
                    // 要檢查此二個 MERCH_ID 是否一致
                    String fileMerchId = matchFile.getName().substring(filenameBean.getMerchIdStart(), filenameBean.getMerchIdEnd());
                    if (!merchId.equals(fileMerchId)) {
                    	logger.warn("merchId:" + merchId + " fileMerchId:" + fileMerchId + " not matched! this file '" + matchFile + "' ignored!");
                        continue;
                    }
                }
               
                splitFileProc(memId, matchFile);
                
             // get TbInctlInfo and insert
                TbSourceInctlInfo inctlInfo = getSourceInctlInfo(matchFile, memId);
                TbSourceInctlMgr mgr = new TbSourceInctlMgr(conn);
                // check TB_SOURCE_INCTL 是否已存在才搬走
                if (mgr.isExist(inctlInfo.toPK())) {
                    // copy $temp/$file to $work/$file
                    // This method copies the contents of the specified source file to the specified destination file.
                    // The directory holding the destination file is created if it does not exist.
                    // If the destination file exists, then this method will overwrite it.
                    FileUtils.copyFile(matchFile, destFile);
                    logger.debug(matchFile.getAbsoluteFile() + " copy to " + destFile.getAbsoluteFile());
                    //mgr.insert(inctlInfo);
                    conn.commit();
                    if (renameAfterCopy && !deleteAfterCopy) {
                        // rename matchFile
                        File rename = new File(matchFile.getAbsolutePath() + renamePending);
                        matchFile.renameTo(rename);
                        // rename OK file
                        if (Layer1Constants.OKFLAG_CHECK.equals(fileInfo.getOkFlag())) {
                            
                        	String subFileName = "";
                        	String fileOk = "";
                        	if (fileInfo.getSubFileName().contains("/")){
                        		subFileName = fileInfo.getSubFileName().replaceAll("/", "");
                        		fileOk = matchFile.getAbsolutePath().substring(0,matchFile.getAbsolutePath().lastIndexOf(".")) + subFileName;
                        	}
                        	else{
                        		subFileName = fileInfo.getSubFileName();
                        		fileOk = matchFile.getAbsolutePath() + subFileName;
                        	}
                        	
                        	File ok = new File(fileOk);
                            File okrename = new File(fileOk + renamePending);
                            if (ok.isFile() && ok.exists()) {
                                ok.renameTo(okrename);
                            }
                        }
                    }
                    else if (!renameAfterCopy && deleteAfterCopy) {
                        // delete matchFile
                        matchFile.delete();
                        // delete OK file
                        if (Layer1Constants.OKFLAG_CHECK.equals(fileInfo.getOkFlag())) {
                        	
                        	String subFileName = "";
                        	String fileOk = "";
                        	if (fileInfo.getSubFileName().contains("/")){
                        		subFileName = fileInfo.getSubFileName().replaceAll("/", "");
                        		fileOk = matchFile.getAbsolutePath().substring(0,matchFile.getAbsolutePath().lastIndexOf(".")) + subFileName;
                        	}
                        	else{
                        		subFileName = fileInfo.getSubFileName();
                        		fileOk = matchFile.getAbsolutePath() + subFileName;
                        	}                        	
                        	
                            File ok = new File(fileOk);
                            if (ok.isFile() && ok.exists()) {
                                ok.delete();
                            }
                        }
                    }
                }
            }
            catch (Exception ignore) {
                logger.warn("handle one matchFile error:" + ignore.getMessage(), ignore);
            }
        }
    }

    protected boolean isValid() {
        boolean ret = true;
        // check fileDateStart, fileDateEnd
        if (filenameBean.getFileDateStart() == -1 || filenameBean.getFileDateEnd() == -1) {
            logger.warn("filenameBean.getFileDateStart() = -1 or filenameBean.getFileDateEnd() = -1!");
            ret = false;
        }
        else if (filenameBean.getFileDateStart() >= filenameBean.getFileDateEnd()) {
            logger.warn("filenameBean.getFileDateStart() >= filenameBean.getFileDateEnd()!");
            ret = false;
        }
        // check snoStart, snoEnd
        if (filenameBean.getSeqnoStart() == -1 || filenameBean.getSeqnoEnd() == -1) {
            // 可以為 -1, -1
            ret = true;
        }
        else if (filenameBean.getSeqnoStart() >= filenameBean.getSeqnoEnd()) {
            logger.warn("filenameBean.getSeqnoStart() >= filenameBean.getSeqnoEnd()!");
            ret = false;
        }
        return ret;
    }
    public void process(String[] strings) throws Exception
    {
        initial();
        try {
            for (int i = 0; i < fileInfos.size(); i++)
            {
                fileInfo = (TbFileInfoInfo) fileInfos.get(i);
                setFilenameBean();
                if (filenameBean == null) {
                    logger.warn("no filenameBean for '" + fileInfo.getFileName() + "'!");
                    continue;
                }
                if (!isValid()) {
                    logger.warn("invalid filenameBean for '" + fileInfo.getFileName() + "'!");
                    continue;
                }
                // 記錄 LOCAL_PATH 是否有 "00000000", 有的話記錄從前面算起第幾個, 沒有的話記錄 -1
                computePos();
                setMatchFiles();
                handleMatchFiles();
                
            }
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    public String getFileName()
    {
        return this.fileName;
    }
    
    public void setDeletefileName(String deletefileName)
    {
        this.deletefileName = deletefileName;
    }
    
    public String getDeletefileName()
    {
        return this.deletefileName;
    }
    
    public void setMaxFileRecCount(int maxFileRecCount)
    {
        this.maxFileRecCount = maxFileRecCount;
    }
    
    public int getMaxFileRecCount()
    {
        return this.maxFileRecCount;
    }
    
    public void setDeleteSpiltFile(boolean deleteSpiltFile)
    {
        this.deleteSpiltFile = deleteSpiltFile;
    }
    
    public boolean getDeleteSpiltFile()
    {
        return this.deleteSpiltFile;
    }
    
    public void setspiltFile(boolean spiltFile)
    {
        this.spiltFile = spiltFile;
    }
    
    public boolean getspiltFile()
    {
        return this.spiltFile;
    }
    
    public void writeRecord(boolean isClose, BufferedOutputStream bos, byte[] record) throws IOException
    {
//String hexString = ISOUtil.hexString(record);
//logger.debug(hexString);
        bos.write(record);
        bos.write(0x0D);
        bos.write(0x0A);
        bos.flush();
        
        if(isClose)
            bos.close();
    }

    public void splitFileProc(String memId, File matchFile) throws Exception
    {
        if(StringUtil.isEmpty(filenameBean.getFileType()) || filenameBean.getFileType().equalsIgnoreCase("B"))
        {
            splitByteFile(memId, matchFile);
        }
        else
        {
            splitHexFile(memId, matchFile);
        }
    }
    
    public void splitHexFile(String memId, File matchFile) throws Exception
    {
        if(spiltFile == false)
            return;
    
        /*double divNbr = Calc.div(numLines, fileLimitCount, 4);
        //splitFileCnt = splitFileCnt < 1 ? 1 : splitFileCnt;
        int splitFileCnt =  (int)Calc.roundFloat(divNbr, 0, "U");
        logger.debug(matchFile.getName() + " => fileLimitCount:"+fileLimitCount+", total count :"+ numLines + ", split to " + splitFileCnt + " file");
        */
        logger.debug("raw file size :"+matchFile.length() + " file lengeh:"+fileInfo.getDataLen().intValue());
        ArrayList<TbSourceInctlInfo> srcInctlInfoAry = new ArrayList<TbSourceInctlInfo>();
        int readRec = 0;
        int openFileCnt = 0;
//        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
//        DataInputStream dis = null;
        BufferedReader dis = null;
        File writeOutfile =  null;
        TbSourceInctlInfo srcInctlInfo = null;
        
        try {
            logger.debug("raw file:"+ matchFile.getAbsolutePath());
//            bis = new BufferedInputStream(new FileInputStream(matchFile));
            
            /* while ((readLine = tmpbr.readLine()) != null) */
            byte[] buffer = new byte[1024];
            byte[] lineByte;
            String lineStr;
            int idx=0;
            int readByte;
            int endByteCnt = 0;
            String hexString;
            String header  = "HFMTEFD20150716162249                   ";
            String tralar  = "T00000000000000000000000              ";
            String HDEADER_KEY = "HFMTEFD"; //FMTEFD
            String TRALAR_KEY = "T0"; //T0
            String outFilePath ="";
            byte[] headerByte = null ;
            byte[] tralarByte = null ;
            FileInputStream fis = null;
//            dis = new DataInputStream(bis);
            dis = new BufferedReader(new FileReader(matchFile));
//            while(dis.available() != 0) {
            while(dis.ready()) {
            
                hexString = dis.readLine();
                lineByte = hexString.getBytes();
                /*logger.debug(ISOUtil.hexString(buffer));*/
                idx++;
  
                //logger.debug(hexString);
                if(hexString.substring(0, HDEADER_KEY.length()).equals(HDEADER_KEY))
                {
                    headerByte = new byte[lineByte.length];//record total length
                    System.arraycopy(lineByte, 0, headerByte, 0, lineByte.length);
                }
                else if(hexString.substring(0, TRALAR_KEY.length()).equals(TRALAR_KEY))
                {
                    tralarByte = new byte[lineByte.length];//record total length
                    System.arraycopy(lineByte, 0, tralarByte, 0, lineByte.length);
                }
                else
                {
                    //write detail record to file
                    readRec++;
                    
                    if(null == bos)
                    {
                        //open first child file
                        openFileCnt++;
                        outFilePath = matchFile.getParentFile() + File.separator + matchFile.getName() + StringUtils.paddingLeftString(String.valueOf(openFileCnt),'0',2);
                        writeOutfile = new File(outFilePath);
                        logger.debug(outFilePath);
                        bos = new BufferedOutputStream(new FileOutputStream(writeOutfile));
                        logger.debug(writeOutfile.getAbsoluteFile());
                        //writeRecord(false, bos, headerByte);
                        
                        /* insert new child file to source inctl */
                        srcInctlInfo = insertSrcInctl(memId, matchFile, writeOutfile, writeOutfile.getAbsolutePath());
                        if(null != srcInctlInfo)
                        {
                            srcInctlInfoAry.add(srcInctlInfo);
                        }
                    }
                    
                    //write detail record
                    writeRecord(false, bos, lineByte);  
                    
                    //write lineByte
                    if(readRec % maxFileRecCount == 0)
                    {
                        //write tralar to previous file
                        //writeRecord(true, bos, tralar.getBytes());
                        
                        //open next child file
                        openFileCnt++;
                        outFilePath = matchFile.getParentFile() + File.separator + matchFile.getName() + StringUtils.paddingLeftString(String.valueOf(openFileCnt),'0',2);
                        writeOutfile = new File(outFilePath);
                        logger.debug(writeOutfile.getAbsoluteFile());
                        bos = new BufferedOutputStream(new FileOutputStream(outFilePath));
                        
                        /* insert new child file to source inctl */
                        srcInctlInfo = insertSrcInctl(memId, matchFile, writeOutfile, writeOutfile.getAbsolutePath());
                        if(null != srcInctlInfo)
                        {
                            srcInctlInfoAry.add(srcInctlInfo);
                        }
                        
                        //write header to this file 
                        //writeRecord(false, bos, headerByte);
                    }
                }             
            }//while
            
            if(null != bos && readRec > 0)
            {
                //write tralar to last file 
                //writeRecord(false, bos, tralar.getBytes());
            }
    
            /* insert all child file to source inctl */
            if(srcInctlInfoAry != null)
            {
                TbSourceInctlMgr srcInctlInfoMgr = new TbSourceInctlMgr(conn);
                for(TbSourceInctlInfo outSrcInctlInfo : srcInctlInfoAry)
                {
                    srcInctlInfoMgr.insert(outSrcInctlInfo);
                }
            }
        }
        catch (Exception ignore) {
            logger.warn("read file error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
        finally
        {
            if(null != dis)
            {
                dis.close();
                dis = null;
            }
//            
//            if(null != bis)
//            {
//                bis.close();
//                bis = null;
//            }
            
            if(null != bos)
            {
                bos.close();
                bos = null;
            }
        }
    }
    
    public void splitByteFile(String memId, File matchFile) throws Exception
    {
        if(spiltFile == false)
            return;
    
        /*double divNbr = Calc.div(numLines, fileLimitCount, 4);
        //splitFileCnt = splitFileCnt < 1 ? 1 : splitFileCnt;
        int splitFileCnt =  (int)Calc.roundFloat(divNbr, 0, "U");
        logger.debug(matchFile.getName() + " => fileLimitCount:"+fileLimitCount+", total count :"+ numLines + ", split to " + splitFileCnt + " file");
        */
        logger.debug("raw file size :"+matchFile.length() + " file lengeh:"+fileInfo.getDataLen().intValue());
        ArrayList<TbSourceInctlInfo> srcInctlInfoAry = new ArrayList<TbSourceInctlInfo>();
        int readRec = 0;
        int openFileCnt = 0;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        File writeOutfile =  null;
        TbSourceInctlInfo srcInctlInfo = null;
        
        try {
            logger.debug("raw file:"+ matchFile.getAbsolutePath());
            bis = new BufferedInputStream(new FileInputStream(matchFile));
            
            /* while ((readLine = tmpbr.readLine()) != null) */
            byte[] buffer = new byte[1024];
            byte[] lineByte;
            int idx=0;
            int readByte;
            int endByteCnt = 0;
            String hexString;
            String header  = "HTEFD20150716162249                   ";
            String tralar  = "T00000000000000000000000              ";
            String HDEADER_KEY = "4854454644"; //HTEFD
            String TRALAR_KEY = "5430"; //T0
            String outFilePath ="";
            byte[] headerByte = null ;
            byte[] tralarByte = null ;
            int recordType = 1;
            boolean isRecrdEnd = false;
            
            while(bis.available() > 0) {
                
                readByte = bis.read();                
                buffer[idx]=(byte)readByte;
                
                /*logger.debug(ISOUtil.hexString(buffer));*/
                
                if(idx == 0 && readByte == 0x48)
                {
                    recordType = 1;//header
                }
                else if(idx == 0 && readByte == 0x44)
                {
                    recordType = 0;//detail
                }
                else if(idx == 0 && readByte == 0x54)
                {
                    recordType = 2;//tralar
                }
                
                idx++;
                
                if (endByteCnt == 0 && readByte == 0x0D)
                {
                    
                    endByteCnt = 1;
                }
                else if (endByteCnt == 1 && readByte == 0x0A)
                {
                    endByteCnt = 0;
                    bis.mark(4);
                    if(recordType > 0)
                    {
                        isRecrdEnd = true;
                    }
                    else
                    {
                        //check next record header is DO1
                        if(bis.available() > 0)
                        {
                            readByte = bis.read();
                            if(readByte == 0x44)//D
                            {
                                if(bis.available() > 0)
                                {
                                    readByte = bis.read();
                                    if(readByte == 0x30)//0
                                    {
                                        if(bis.available() > 0)
                                        {
                                            readByte = bis.read();
                                            if(readByte == 0x31)//1
                                            {
                                                isRecrdEnd = true;
                                                bis.reset();
                                            }
                                        }
                                    }
                                }
                            }
                            else if(readByte == 0x54)//D
                            {
                                if(bis.available() > 0)
                                {
                                    readByte = bis.read();
                                    //logger.debug(Integer.toHexString(readByte));
                                    if(readByte == 0x30)//0
                                    {
                                        if(bis.available() > 0)
                                        {
                                            readByte = bis.read();
                                            //logger.debug(Integer.toHexString(readByte));
                                            if(readByte == 0x30)//1
                                            {
                                                isRecrdEnd = true;
                                                bis.reset();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if(isRecrdEnd == false)
                        {
                          //maybe ff64 data
                        }
                        bis.reset();
                    }
                    
                    if(isRecrdEnd)
                    {
                        isRecrdEnd = false;
                        //以0D,0A來決定是否換行
                        lineByte = new byte[idx-2];//record total length
                        System.arraycopy(buffer, 0, lineByte, 0, lineByte.length);
                        /*logger.debug(ISOUtil.hexString(lineByte));*/
                        
                        //清空buffer byte
                        System.arraycopy(new byte[1024], 0, buffer, 0, 1024);
                        idx=0;//重置record index
                        
                        if (lineByte.length > 2) {//is not just end of line
                            
                            hexString = ISOUtil.hexString(lineByte);
//                            logger.debug(hexString);
                            if(hexString.substring(0, HDEADER_KEY.length()).equals(HDEADER_KEY))
                            {
                                headerByte = new byte[lineByte.length];//record total length
                                System.arraycopy(lineByte, 0, headerByte, 0, lineByte.length);
                            }
                            else if(hexString.substring(0, TRALAR_KEY.length()).equals(TRALAR_KEY))
                            {
                                tralarByte = new byte[lineByte.length];//record total length
                                System.arraycopy(lineByte, 0, tralarByte, 0, lineByte.length);
                            }
                            else
                            {
                                //write detail record to file
                                readRec++;
                                
                                if(null == bos)
                                {
                                    //open first child file
                                    openFileCnt++;
                                    outFilePath = matchFile.getParentFile() + File.separator + matchFile.getName() + StringUtils.paddingLeftString(String.valueOf(openFileCnt),'0',2);
                                    writeOutfile = new File(outFilePath);
                                    logger.debug(outFilePath);
                                    bos = new BufferedOutputStream(new FileOutputStream(writeOutfile));
                                    logger.debug(writeOutfile.getAbsoluteFile());
                                    writeRecord(false, bos, headerByte);
                                    
                                    /* insert new child file to source inctl */
                                    srcInctlInfo = insertSrcInctl(memId, matchFile, writeOutfile, writeOutfile.getAbsolutePath());
                                    if(null != srcInctlInfo)
                                    {
                                        srcInctlInfoAry.add(srcInctlInfo);
                                    }
                                }
                                
                                //write detail record
                                writeRecord(false, bos, lineByte);  
                                
                                //write lineByte
                                if(readRec % maxFileRecCount == 0)
                                {
                                    //write tralar to previous file
                                    writeRecord(true, bos, tralar.getBytes());
                                    
                                    //open next child file
                                    openFileCnt++;
                                    outFilePath = matchFile.getParentFile() + File.separator + matchFile.getName() + StringUtils.paddingLeftString(String.valueOf(openFileCnt),'0',2);
                                    writeOutfile = new File(outFilePath);
                                    logger.debug(writeOutfile.getAbsoluteFile());
                                    bos = new BufferedOutputStream(new FileOutputStream(outFilePath));
                                    
                                    /* insert new child file to source inctl */
                                    srcInctlInfo = insertSrcInctl(memId, matchFile, writeOutfile, writeOutfile.getAbsolutePath());
                                    if(null != srcInctlInfo)
                                    {
                                        srcInctlInfoAry.add(srcInctlInfo);
                                    }
                                    
                                    //write header to this file 
                                    writeRecord(false, bos, headerByte);
                                }
                            }
                            
                        }else{ 
                            logger.warn("skip empty line.");
                        }
                    }
                }else{//退回
                    endByteCnt = 0;
                }
                
            }//while
            
            if(null != bos && readRec > 0)
            {
                //write tralar to last file 
                writeRecord(false, bos, tralar.getBytes());
            }
    
            /* insert all child file to source inctl */
            if(srcInctlInfoAry != null)
            {
                TbSourceInctlMgr srcInctlInfoMgr = new TbSourceInctlMgr(conn);
                for(TbSourceInctlInfo outSrcInctlInfo : srcInctlInfoAry)
                {
                    srcInctlInfoMgr.insert(outSrcInctlInfo);
                }
            }
        }
        catch (Exception ignore) {
            logger.warn("read file error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
        finally
        {
            if(null != bis)
            {
                bis.close();
                bis = null;
            }
            
            if(null != bos)
            {
                bos.close();
                bos = null;
            }
        }
    }
        
    public TbSourceInctlInfo insertSrcInctl(String merchId, File rawOutfile, File writeOutfile ,String fullFilePath) throws Exception
    {
        String srcFullName = normalFileSeparator(writeOutfile.getAbsolutePath());
        File outfile = new File(srcFullName);
        String programName = getClass().getSimpleName();
        String fileName = writeOutfile.getName();
        String fileDate = writeOutfile.getName().substring(filenameBean.getFileDateStart(), filenameBean.getFileDateEnd());
        if (fileDate.length() == 6) {
            String year = "" + DateUtil.getYear();
            fileDate = year.substring(0, 2) + fileDate;
        }
        
        TbSourceInctlInfo srcInctlInfo = new TbSourceInctlInfo();
        
        srcInctlInfo.setProgramName(programName);
        srcInctlInfo.setMemId(merchId);
        srcInctlInfo.setFileType(fileInfo.getFileType());
        logger.debug("file_size:"+writeOutfile.length());
        srcInctlInfo.setFileSize(new Integer((int)outfile.length()));
        srcInctlInfo.setFullFileName(rawOutfile.getName());
        srcInctlInfo.setPartFullFileName(fileName);
        srcInctlInfo.setStartDate(getBatchResultInfo().getStartDate());
        srcInctlInfo.setStartTime(getBatchResultInfo().getStartTime());
        srcInctlInfo.setSysDate(sysDate);
        srcInctlInfo.setSysTime(sysTime);
        srcInctlInfo.setParMon(fileName.substring(18, 20));
        srcInctlInfo.setParDay(fileName.substring(20, 22));    
        return srcInctlInfo;
    }
    
    public void deleteFileName() throws Exception
    {
        if(deleteSpiltFile == false)
            return;
        
        TbFileInfoInfo qinfo = new TbFileInfoInfo();
        qinfo.toEmpty();
        qinfo.setInOut(Layer1Constants.INOUT_IN);
        if (!StringUtil.isEmpty(deletefileName)) {
            logger.info("input deletefileName:" + deletefileName);
            qinfo.setFileName(deletefileName);
        }
        Vector allFileInfos = new Vector();
        TbFileInfoMgr mgr = new TbFileInfoMgr(conn);
        
        try
        {
            mgr.queryMultiple(qinfo, allFileInfos);
            Vector fileInfos = allFileInfos;
            for (int i = 0; i < fileInfos.size(); i++)
            {
                TbFileInfoInfo fileInfo = (TbFileInfoInfo) fileInfos.get(i);
             // 沒有 00000000, 抓此層符合的檔案
                String sdir2 = FilenameUtils.normalizeNoEndSeparator(tempDir + '/' + fileInfo.getLocalPath());
                //logger.info("del sdir2:" + sdir2);
                File dir2 = new File(sdir2);
                File[] files2 = dir2.listFiles();
                // avoid NullPointException
                if (files2 != null) {
                    logger.info("del  files2.length:" + files2.length);
                    for (int j = 0; j < files2.length; j++)
                    {
                        logger.info("del files2["+j+"]:" + files2[j].getAbsolutePath());
                        if (files2[j].isFile() && isMatchFile(files2[j].getName(), fileInfo)) {
                            files2[j].delete();
                        }
                    }
                }
                else {
                    //logger.info("files2 is null!");
                }                
            }
        }
        catch (SQLException e)
        {
            logger.warn("deleteFileName error:" + e.getMessage(), e);
            throw e;
        }
    }
    
    public static SplitImpTrans getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        SplitImpTrans instance = (SplitImpTrans) apContext.getBean("splitImpTxnDtl");
        logger.debug(SPRING_PATH);
        return instance;
    }
    
    public static void main(String[] args) {
        SplitImpTrans splitTrans = null;
        try {
            
            String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            splitTrans = getInstance();
            splitTrans.setBatchDate(batchDate);
            //filesIn.process(args);
            splitTrans.run(args);
        }
        catch (Exception e) {
            logger.warn("get spring bean error:" + e.getMessage(), e);
            System.exit(-1);
        }
        System.exit(0);
    }
}
