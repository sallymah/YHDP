/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpLptsam;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.batch.util.StringUtils;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCust.CustData;
import tw.com.hyweb.util.BatchUtils;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpLptsam(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpLptsam extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpLptsam.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpLptsam" + File.separator + "spring.xml";

    private String batchDate = "";
    private String time = "";
    
    private LptsamData lptsamData = null;
      
    
    public ImpLptsam()
    {
    }

    
    public ExecuteSqlsInfo beforeHandleDataLine() throws Exception
    {
    	batchDate = System.getProperty("date");
        if (StringUtil.isEmpty(batchDate)) {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }   	       
        
        return null;
    }
    
    public ExecuteSqlsInfo afterHandleDataLine() throws Exception
    {    	
    	 return null;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception 
    {
    	if(time.equals("")) {
    		time = BatchUtils.getNextSec(DateUtils.getSystemTime(),1);
        	log.debug("time="+time);
    	}
    	else {
    		time = BatchUtils.getNextSec(time,1);
        	log.debug("time="+time);
    	}
  
    	lptsamData = new LptsamData(conn, getLptsamValues(lineInfo), inctlInfo.getFullFileName());
    	lptsamData.setUptTime(time);
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	LptsamChecker checker = new LptsamChecker(lptsamData, getLptsamFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getLptsamValues(DataLineInfo dataline)
    {
        Map<String, String> lptsamValues = new HashMap<String, String>();
        lptsamValues.put("ACTION_STATUS", (String) dataline.getFieldData("field02"));
        lptsamValues.put("MEMBER_GROUP_ID", (String) dataline.getFieldData("field03"));
        lptsamValues.put("CID", (String) dataline.getFieldData("field04"));
        lptsamValues.put("STATUS", (String) dataline.getFieldData("field05"));
        lptsamValues.put("SAM_TYPE", (String) dataline.getFieldData("field06"));
        
        
        return lptsamValues;
    }

    private Map<String, FieldInfo> getLptsamFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> lptsamFieldInfos = new HashMap<String, FieldInfo>();
        lptsamFieldInfos.put("ACTION_STATUS", dataline.getMappingInfo().getField("field02"));
        lptsamFieldInfos.put("MEMBER_GROUP_ID", dataline.getMappingInfo().getField("field03"));
        lptsamFieldInfos.put("CID", dataline.getMappingInfo().getField("field04"));
        lptsamFieldInfos.put("STATUS", dataline.getMappingInfo().getField("field05"));
        lptsamFieldInfos.put("SAM_TYPE", dataline.getMappingInfo().getField("field06"));
        
        return lptsamFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
    	sqlsInfo.setCommit(false);
    	sqlsInfo.setSavepoint(true);
        String fileDate = getImpFileInfo().getInctlInfo().getFileDate();
        sqlsInfo.setSqls(lptsamData.handleCust(conn, batchDate, fileDate));
        sqlsInfos.add(sqlsInfo);
        
        log.info("handleDataLine:" + sqlsInfos);   
        
        return sqlsInfos;
    }
    
    public static ImpLptsam getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpLptsam instance = (ImpLptsam) apContext.getBean("impLptsam");
        return instance;
    }

    public static void main(String[] args) {
    	ImpLptsam impLptsam = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impLptsam = getInstance();
            }
            else {
            	impLptsam = new ImpLptsam();
            }
            impLptsam.setFileName("LPTSAM");
            impLptsam.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpLptsam run fail:" + ignore.getMessage(), ignore);
        }
    }
}
