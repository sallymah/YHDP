package tw.com.hyweb.svc.yhdp.batch.cipher;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbOutctlMgr;
import tw.com.hyweb.util.CipherUtils;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class Encrypt extends AbstractBatchBasic {

	private static Logger log = Logger.getLogger(Encrypt.class);
	private static final String SPRING_PATH = "config" + File.separator
			+ "batch" + File.separator + "Cipher" + File.separator + "Encrypt"
			+ File.separator + "spring.xml";

	private final String sysDate = DateUtils.getSystemDate();
	private final String sysTime = DateUtils.getSystemTime();

	private String namePattern = "CIPHER";
	private String split = "_";
	private String iv = "0000000000000000";
	private String pinKeyId = "P3EncKey";
	private String in_out = "O";
	private String cipher_flag = "1";
	private String servletUrl = "http://YhdpSS:36888/YHDP_SSServlet/SS";
	private Integer slot = 0;
	private String pin = "BAA6B7BBAFCECDCCCBF7F7F7F7F7F7F7F7";
	
	private Connection conn = null;
	private Vector<TbFileInfoInfo> fileInfos = null;
	private Map<String, TbFileInfoInfo> fileCipherInfos = null;
	private String tempDir = "";
	private String workDir = "";
	
	private List jobMemIds;

	public Encrypt() {
	}

	public String getNamePattern() {
		return namePattern;
	}

	public void setNamePattern(String namePattern) {
		this.namePattern = namePattern;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}

	public String getPinKeyId() {
		return pinKeyId;
	}

	public void setPinKeyId(String pinKeyId) {
		this.pinKeyId = pinKeyId;
	}

	public String getIn_out() {
		return in_out;
	}

	public void setIn_out(String in_out) {
		this.in_out = in_out;
	}

	public String getCipher_flag() {
		return cipher_flag;
	}

	public void setCipher_flag(String cipher_flag) {
		this.cipher_flag = cipher_flag;
	}

	public String getServletUrl() {
		return servletUrl;
	}

	public void setServletUrl(String servletUrl) {
		this.servletUrl = servletUrl;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	private void init() throws Exception {
		try {
			conn = DBService.getDBService().getConnection(
					Layer1Constants.DSNAME_BATCH);

			StringBuffer where = new StringBuffer();
			where.append("IN_OUT = ");
			where.append(StringUtil.toSqlValueWithSQuote(in_out));
			where.append(" and cipher_flag = ");
			where.append(StringUtil.toSqlValueWithSQuote(cipher_flag));
			Vector<TbFileInfoInfo> tempInfos = new Vector<TbFileInfoInfo>();
			TbFileInfoMgr mgr = new TbFileInfoMgr(conn);
			mgr.queryMultiple(where.toString(), tempInfos);

			fileInfos = new Vector<TbFileInfoInfo>();
			fileCipherInfos = new HashMap<String, TbFileInfoInfo>();
			filterFileInfos(tempInfos);

			tempDir = BatchUtil.getTempDirectory();
			if (StringUtil.isEmpty(tempDir))
				throw new Exception("no tempDir setting!");
			workDir = BatchUtil.getWorkDirectory();
			if (StringUtil.isEmpty(workDir))
				throw new Exception("no workDir setting!");

			tempDir = Utils.formatPath(tempDir);
			workDir = Utils.formatPath(workDir);

		} catch (Exception ignore) {
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

	private void filterFileInfos(Vector<TbFileInfoInfo> allFileInfos) {

		StringBuffer logMsg = new StringBuffer();

		if (allFileInfos == null || allFileInfos.size() == 0) {
			logMsg.delete(0, logMsg.length());
			logMsg.append("TB_FILE_INFO.CIPHER_FLAG = ");
			logMsg.append(cipher_flag);
			logMsg.append(" is null! ");
			log.warn(logMsg);
			return;
		}

		StringBuffer namePattern = new StringBuffer();
		namePattern.append("[a-zA-Z0-9]+");
		namePattern.append("[").append(split).append("]+");
		namePattern.append(this.namePattern);
		Vector<TbFileInfoInfo> tempCipherInfos = new Vector<TbFileInfoInfo>();
		for (int i = 0; i < allFileInfos.size(); i++) {
			TbFileInfoInfo fileInfo = allFileInfos.get(i);
			if (fileInfo.getFileName().matches(namePattern.toString()))
				tempCipherInfos.add(fileInfo);
			else
				fileInfos.add(fileInfo);
		}

		if (tempCipherInfos.size() == 0) {
			logMsg.delete(0, logMsg.length());
			logMsg.append("TbFileInfo Pattern [");
			logMsg.append(namePattern.toString());
			logMsg.append("] settings are missing! ");
			log.warn(logMsg);
			return;
		}

		if (fileInfos.size() != tempCipherInfos.size()) {
			for (int i = 0; i < fileInfos.size(); i++) {
				String fileName = fileInfos.get(i).getFileName();
				boolean isMatch = false;
				int j = tempCipherInfos.size();
				do {
					isMatch = false;
					String cipherName = tempCipherInfos.get(--j).getFileName();
					cipherName = cipherName.substring(0,
							cipherName.indexOf(split));
					if (fileName.equals(cipherName)) {
						isMatch = true;
						break;
					}
				} while (j > 0);

				if (!isMatch) {
					fileInfos.remove(i);
					logMsg.delete(0, logMsg.length());
					logMsg.append("TbFileInfo fileName [");
					logMsg.append(fileName);
					logMsg.append("] cipher settings are missing! ");
					log.warn(logMsg);
				}
			}
		}

		convertFileInfo(tempCipherInfos);
	}

	private Map<String, TbFileInfoInfo> convertFileInfo(
			Vector<TbFileInfoInfo> tempInfos) {
		for (int i = 0; i < tempInfos.size(); i++) {
			TbFileInfoInfo value = tempInfos.get(i);
			String key = value.getFileName().substring(0,
					value.getFileName().indexOf(split));
			fileCipherInfos.put(key, value);
		}
		return fileCipherInfos;
	}

	@Override
	public void process(String[] arg0) throws Exception {
		try {
			this.init();

			for (int i = 0; i < fileInfos.size(); i++) {
				TbFileInfoInfo fileInfo = (TbFileInfoInfo) fileInfos.get(i);
				fileInfo.setOkFlag(Layer1Constants.OKFLAG_NOCHECK);
				List<File> files = Utils.setMatchFiles(workDir, fileInfo, jobMemIds);
				this.handleMatchFiles(files, fileInfo);
			}
		} finally {
			if (conn != null)
				ReleaseResource.releaseDB(conn);
		}
	}

	private void handleMatchFiles(List<File> files, TbFileInfoInfo fileInfo)
			throws Exception {

		StringBuffer logMsg = new StringBuffer();

		if (files.isEmpty() && files.size() == 0) {
			logMsg.delete(0, logMsg.length());
			logMsg.append("files is null!!");
			log.warn(logMsg);
			return;
		}

		for (File file : files) {
			int cnt = getOutctlCnt(fileInfo.getFileName(), file.getName());
			if (cnt == 0) {
				logMsg.delete(0, logMsg.length());
				logMsg.append("SELECT * FROM TB_OUTCTL ");
				logMsg.append("WHERE FULL_FILE_NAME = ");
				logMsg.append(StringUtil.toSqlValueWithSQuote(file.getName()));
				logMsg.append(" AND FILE_NAME = ");
				logMsg.append(StringUtil.toSqlValueWithSQuote(fileInfo
						.getFileName()));
				logMsg.append(" is empty!");
				log.warn(logMsg);
				continue;
			}

			String fileNamePattern = fileInfo.getFileNamePattern();
			String cipherfileNamePattern = fileCipherInfos.get(
					fileInfo.getFileName()).getFileNamePattern();
			String mark = Utils.checkMark(
					cipherfileNamePattern.replace(fileNamePattern, ""), ".en");
			logMsg.delete(0, logMsg.length());
			logMsg.append("cipher mark is [");
			logMsg.append(mark);
			logMsg.append("] ");
			log.info(logMsg.toString());
			cnt = getOutctlCnt((fileInfo.getFileName() + split + namePattern),
					(file.getName() + mark));
			if (cnt == 1) {
				logMsg.delete(0, logMsg.length());
				logMsg.append("SELECT * FROM TB_OUTCTL ");
				logMsg.append("WHERE FULL_FILE_NAME = ");
				logMsg.append(StringUtil.toSqlValueWithSQuote(file.getName()
						+ mark));
				logMsg.append(" AND FILE_NAME = ");
				logMsg.append(StringUtil.toSqlValueWithSQuote(fileInfo
						.getFileName() + split + namePattern));
				logMsg.append(" is exist!");
				log.warn(logMsg);
				continue;
			}

			String pathName = Utils.normalFileSeparator(file.getAbsolutePath()
					+ mark);
			File copyFile = new File(pathName);
			FileUtils.copyFile(file, copyFile);

			File tmpFile = new File(Utils.normalFileSeparator(file
					.getAbsolutePath() + ".tmp"));
			
			String pinKeyId = "";
			if ( Utils.getPinKeyId(file.getName()) ){
				String[] args = file.getName().split("\\.");
				pinKeyId = args[1]  + "KEY";
			}
			else{
				pinKeyId = this.pinKeyId;
			}
			
			try {
				CipherUtils.encrypt2File(tmpFile.getAbsolutePath(),
						copyFile.getAbsolutePath(), pinKeyId, iv, servletUrl, slot, pin);
			} catch (Exception e) {
				logMsg.delete(0, logMsg.length());
				logMsg.append(copyFile.getAbsoluteFile());
				logMsg.append(" encrypt fail: ");
				logMsg.append(e.getMessage());
				log.warn(logMsg);
				throw e;
			}

			if (file.exists()) {
				file.delete();
			}
			boolean isMoved = tmpFile.renameTo(file);
			if (!isMoved) {
				logMsg.delete(0, logMsg.length());
				logMsg.append(file.getAbsolutePath());
				logMsg.append(" move to ");
				logMsg.append(tmpFile.getAbsolutePath());
				logMsg.append(" fail! ");
				log.warn(logMsg);
			}

			updateAndinsertOutctl(file, copyFile, fileInfo.getFileName());
		}
	}

	private void updateAndinsertOutctl(File file, File copyFile, String fileName)
			throws Exception {

		String fullFileName = file.getName();

		StringBuffer sqlCmd = new StringBuffer();
		sqlCmd.append("insert into TB_OUTCTL ");
		sqlCmd.append("(MEM_ID,FILE_NAME,FILE_DATE,SEQNO,FILE_TYPE,WORK_FLAG,");
		sqlCmd.append("TOT_REC,FILE_SIZE,FULL_FILE_NAME,PROGRAM_NAME,");
		sqlCmd.append("START_DATE,START_TIME,PAR_MON,PAR_DAY,SYS_DATE,SYS_TIME,");
		sqlCmd.append("MEM_GROUP_ID,MERCH_ID) ");
		sqlCmd.append("select ");
		sqlCmd.append("MEM_ID,");
		sqlCmd.append(
				StringUtil.toSqlValueWithSQuote(fileName + split + namePattern))
				.append(",");
		sqlCmd.append("FILE_DATE,SEQNO,FILE_TYPE,3,");
		sqlCmd.append("TOT_REC,");
		sqlCmd.append(copyFile.length()).append(",");
		sqlCmd.append(StringUtil.toSqlValueWithSQuote(copyFile.getName()))
				.append(",");
		sqlCmd.append(StringUtil.toSqlValueWithSQuote("Encrypt")).append(",");
		sqlCmd.append(sysDate).append(",");
		sqlCmd.append(sysTime).append(",");
		sqlCmd.append(sysDate.substring(4, 6)).append(",");
		sqlCmd.append(sysDate.substring(6, 8)).append(",");
		sqlCmd.append(sysDate).append(",");
		sqlCmd.append(sysTime).append(",");
		sqlCmd.append("MEM_GROUP_ID,MERCH_ID ");
		sqlCmd.append("from TB_OUTCTL ");
		sqlCmd.append("where FULL_FILE_NAME = ");
		sqlCmd.append(StringUtil.toSqlValueWithSQuote(fullFileName));
		sqlCmd.append(" and FILE_NAME = ");
		sqlCmd.append(StringUtil.toSqlValueWithSQuote(fileName));
		log.info(sqlCmd);
		DbUtil.sqlAction(sqlCmd.toString(), conn);

		sqlCmd.delete(0, sqlCmd.length());
		sqlCmd.append("update TB_OUTCTL set ");
		sqlCmd.append("FILE_SIZE = ").append(file.length());
		sqlCmd.append(" where FULL_FILE_NAME = ");
		sqlCmd.append(StringUtil.toSqlValueWithSQuote(fullFileName));
		sqlCmd.append(" and FILE_NAME = ");
		sqlCmd.append(StringUtil.toSqlValueWithSQuote(fileName));
		log.info(sqlCmd);
		DbUtil.sqlAction(sqlCmd.toString(), conn);

	}

	private Integer getOutctlCnt(String fileName, String fullFileName)
			throws Exception {
		TbOutctlInfo info = new TbOutctlInfo();
		info.setFullFileName(fullFileName);
		info.setFileName(fileName);
		Vector<TbOutctlInfo> result = new Vector<TbOutctlInfo>();
		return new TbOutctlMgr(conn).queryMultiple(info, result);
	}

	public static Encrypt getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(
				SPRING_PATH);
		Encrypt instance = (Encrypt) apContext.getBean("encrypt");
		return instance;
	}

	public static void main(String[] args) {
		Encrypt encrypt = null;
		try {
			File f = new File(SPRING_PATH);
			if (f.exists() && f.isFile()) {
				encrypt = getInstance();
			} else {
				encrypt = new Encrypt();
			}

			encrypt.run(args);
		} catch (Exception ignore) {
			log.warn("Encrypt run fail:" + ignore.getMessage(), ignore);
		}
	}

}
