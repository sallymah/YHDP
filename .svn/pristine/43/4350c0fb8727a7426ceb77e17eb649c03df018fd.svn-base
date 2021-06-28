package tw.com.hyweb.svc.yhdp.batch.impfiles.ChangeLocalPath;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import tw.com.hyweb.core.ftp.batch.util.BatchUtil;
import tw.com.hyweb.util.DbUtil;

public class FilenameBean {
	
	private static final String SELECT_SQL = "SELECT FILE_NAME_PATTERN, LOCAL_PATH, DATA_LEN FROM TB_FILE_INFO WHERE FILE_NAME = ? AND IN_OUT = ?";
	
	private List<String> fileNameList = new ArrayList<String>();
	
	private String tempDir = "";

	private String fileName = "";
	private String fileNamePattern = "";
	
	private String InOut = "I";
	
	private List<HashMap<String, Object>> changeFileList = new ArrayList<HashMap<String, Object>>();
	
	
	public void toInit(Connection connection) throws Exception {
		// TODO Auto-generated method stub
		
		tempDir = BatchUtil.getTempDirectory();
		
		Vector params = new Vector<>();
		Vector fileInfo = new Vector<>();
		
		params.add(fileName);
		params.add(InOut);
		fileInfo = DbUtil.getInfoListHashMap(SELECT_SQL, params, connection);
		fileNamePattern = (String) ((HashMap) fileInfo.get(0)).get("FILE_NAME_PATTERN");
		
		params.clear();
		fileInfo.clear();
		
		for(String fileName : fileNameList) {
			
			params.add(fileName);
			params.add(InOut);
			fileInfo = DbUtil.getInfoListHashMap(SELECT_SQL, params, connection);
			
			HashMap<String, Object> fileData = new HashMap<String, Object>();
			
			fileData.put("FILE_NAME", fileName);
			fileData.put("LOCAL_PATH", ((HashMap) fileInfo.get(0)).get("LOCAL_PATH").toString());
			fileData.put("DATA_LEN", Integer.valueOf(((HashMap) fileInfo.get(0)).get("DATA_LEN").toString()));
			
			changeFileList.add(fileData);
			
			params.clear();
			fileInfo.clear();
		}
	}
	
	public List<String> getFileNameList() {
		return fileNameList;
	}
	public void setFileNameList(List<String> fileNameList) {
		this.fileNameList = fileNameList;
	}
	public List<HashMap<String, Object>> getChangeFileList() {
		return changeFileList;
	}
	public void setChangeFileList(List<HashMap<String, Object>> changeFileList) {
		this.changeFileList = changeFileList;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileNamePattern() {
		return fileNamePattern;
	}
	public void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;
	}
	public String getTempDir() {
		return tempDir;
	}
	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
}
