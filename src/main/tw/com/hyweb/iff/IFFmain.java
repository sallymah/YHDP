package tw.com.hyweb.iff;

import java.io.File;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.iff.IFFaction;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.ReleaseResource;

public class IFFmain {
	
	private Logger log = Logger.getLogger(IFFmain.class);
	private final static String SPRING_PATH = "config" + File.separator + "batch" + File.separator + "IFFUtil"  + File.separator + "spring.xml" ;
	
	private void run( ){

		log.debug("=============== Start. ===============");
		
		Connection conn = null;
		String dataLine = "8011222445506587F52D5200010001000120171114999912310101020200000000000000002017000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000030303030F52D5200000000000000000088609851422336844805\n\n";
		String persoBatchNo = "201611251044001068";
		
		try {
			
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			String[] A = IFFaction.IffFeedback(persoBatchNo, dataLine, conn);
			log.debug(A[0]);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			ReleaseResource.releaseDB(conn);
		}
		
		log.debug("=============== End. ===============");
	}
	
	public static void main(String[] args) throws Exception {
		IFFmain iffMain = null;
		File f = new File(SPRING_PATH);
        if (f.exists() && f.isFile()) {
        	iffMain = getInstance();
        	iffMain.run();
        }
	}
	
	public static IFFmain getInstance() {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        IFFmain instance = (IFFmain) apContext.getBean("iffMain");
        return instance;
    }
}
