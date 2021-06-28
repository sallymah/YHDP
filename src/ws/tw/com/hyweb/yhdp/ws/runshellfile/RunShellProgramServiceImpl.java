package tw.com.hyweb.yhdp.ws.runshellfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.jws.WebService;

import org.apache.log4j.Logger;
                                                                                              
@WebService(endpointInterface = "tw.com.hyweb.yhdp.ws.runshellfile.RunShellProgramService",serviceName="RunShellProgramService")

public class RunShellProgramServiceImpl implements RunShellProgramService{

	public static String filePathName ;

	private static final Logger log = Logger.getLogger(RunShellProgramServiceImpl.class);
	
	public boolean execute(String shellFileName, String fullFileName) throws Exception
	{		
		
		String runFileName = "sh " + filePathName + shellFileName+ ".sh " + fullFileName;
		
		log.debug("start: " + runFileName);
		
		File indexDir = new File(filePathName);  
		
		Process process = Runtime.getRuntime().exec(runFileName, null, indexDir);
		
        InputStream stream = process.getInputStream();
		
        BufferedReader reader = new BufferedReader (new InputStreamReader(stream));
        
        String s;
        while((s = reader.readLine()) != null ) 
        {
        	log.debug(s);
        }
        log.debug("return: " + process.waitFor());
        return true;
	}

	public String getFilePathName()
	{
		return filePathName;
	}
	
	public void setFilePathName(String filepathname)
	{
		RunShellProgramServiceImpl.filePathName = filepathname;
	}
}
