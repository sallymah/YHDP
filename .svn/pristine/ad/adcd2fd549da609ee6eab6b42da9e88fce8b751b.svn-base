package tw.com.hyweb.svc.yhdp.batch.impfiles.ChangeLocalPath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.util.DbUtil;

public class ChangeLocalPathJob implements BatchJob {
	
	private static final Logger logger = Logger.getLogger(ChangeLocalPathJob.class);
	
	private final Map<String, String> resultMap;
	private final List filenameBeans;

	public ChangeLocalPathJob(Map<String, String> resultMap, List filenameBeans) {
		this.resultMap = resultMap;
		this.filenameBeans = filenameBeans;
	}

	@Override
	public void action(Connection connection, String batchDate) throws Exception {
		// TODO Auto-generated method stub
		for(int i = 0; i < filenameBeans.size(); i++) {
			
			FilenameBean filenameBean = (FilenameBean)filenameBeans.get(i);
			//檔名邏輯比對需處理邏輯
			if(resultMap.get("FULL_FILE_NAME").toString().matches(filenameBean.getFileNamePattern())) {
				
				String localPath = arrangeFile(resultMap.get("LOCAL_PATH").toString());
				File file = new File(localPath + resultMap.get("FULL_FILE_NAME").toString());
				
				if(file.exists()) {
					
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Big5"));
					String oneLine;
					while((oneLine = br.readLine()) != null) {
						
						int dataLen = oneLine.getBytes("Big5").length + "\r\n".getBytes("Big5").length;
						
						
						for(HashMap<String, Object> changeFileInfo : filenameBean.getChangeFileList()) {
							
							if(dataLen == (int) changeFileInfo.get("DATA_LEN")) {
								
								String changeFolder = arrangeFile(localPath.replace(filenameBean.getFileName(), (String) changeFileInfo.get("FILE_NAME")));
								File changeFile = new File(changeFolder);
								
								if (!changeFile.exists()) {
									changeFile.mkdirs();
				                }
								logger.debug("[" + file.getAbsoluteFile() + "] --> [" + changeFolder + resultMap.get("FULL_FILE_NAME").toString() + "]");
								//移動檔案至新介面資料夾
								logger.debug(file.renameTo(new File(changeFolder + resultMap.get("FULL_FILE_NAME").toString())));
								
								break;
							}
						}
						break;
					}
				}
				else {
					logger.warn("[" + arrangeFile(resultMap.get("LOCAL_PATH").toString()) + resultMap.get("FULL_FILE_NAME").toString() + "] does not exist.");
				}
			}
		}
	}
	
	private void updateFtpLog(Connection conn, String batchDate, String changeFolder) throws SQLException {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		
		sb.append(" UPDATE TB_FTP_LOG");
		sb.append(" SET LOCAL_PATH = ?");
		sb.append(" WHERE FULL_FILE_NAME = ?");
		sb.append(" AND IN_OUT = ?");
		sb.append(" AND FTP_IP = ?");
		sb.append(" AND REMOTE_PATH = ?");
		sb.append(" AND SYS_DATE = ?");
		sb.append(" AND SYS_TIME = ?");
		
		Vector<String> params = new Vector<>();
		
		params.add(changeFolder);
		
		params.add(resultMap.get("FULL_FILE_NAME").toString());
		params.add("I");
		params.add(resultMap.get("FTP_IP").toString());
		params.add(resultMap.get("REMOTE_PATH").toString());
		params.add(resultMap.get("SYS_DATE").toString());
		params.add(resultMap.get("SYS_TIME").toString());
		
		DbUtil.sqlAction(sb.toString(), params, conn);
	}

	public static String arrangeFile(String file){
		
		String[] fileWord = file.split("[\\\\/]");
		String arrangefile = "";
		if (fileWord.length < 1 ){
			arrangefile = "/";
		}
		else{
			for (int i= 0; i < fileWord.length; i++){
				if (fileWord[i].length()>0 || i == 0){
					arrangefile = arrangefile + fileWord[i] + "/";
				}
			}
		}
		
		return arrangefile;
	}

	@Override
	public void remarkFailure(Connection connection, String batchDate, BatchJobException ex) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void remarkSuccess(Connection connection, String batchDate) throws Exception {
		// TODO Auto-generated method stub
	}
	
}
