package tw.com.hyweb.cp.ws.runbatfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.jws.WebService;

import org.apache.log4j.Logger;
                                                                                              
@WebService(endpointInterface = "tw.com.hyweb.cp.ws.runbatfile.RunProgramService",serviceName="RunProgramService")

public class RunProgramServiceImpl implements RunProgramService{

	public static String filePathName ;
	
	private static final Logger log = Logger.getLogger(RunProgramServiceImpl.class);
	
	public void execute(String filename) throws Exception
	{		
		
		String runFileName = filePathName + filename+ ".bat"; 
		
		File indexDir = new File(filePathName);  
		
		Process process = Runtime.getRuntime().exec(runFileName, null, indexDir);
		
        InputStream stream = process.getInputStream();

        BufferedReader reader = new BufferedReader (new InputStreamReader(stream));

        String s;
        while((s = reader.readLine()) != null ) 
        {
        	log.debug(s);
        }
	}

	public String getFilePathName()
	{
		return filePathName;
	}
	
	public void setFilePathName(String filepathname)
	{
		filePathName = filepathname;
	}
}
