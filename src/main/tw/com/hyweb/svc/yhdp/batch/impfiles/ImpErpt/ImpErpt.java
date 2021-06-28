/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpErpt;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.svc.yhdp.batch.framework.ImpErptFileInfo.AbstractImpFile;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.util.string.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * FOR YHDP 
 * </pre>
 * 
 */
public class ImpErpt extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpErpt.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpErpt" + File.separator + "spring.xml";

    private String batchDate = "";
    	    
    private ErptData erptData = null;  
    
    public ImpErpt()
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
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	erptData = new ErptData(conn, getGrantsValues(lineInfo), inctlInfo.getFullFileName());
    	  	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getGrantsValues(DataLineInfo dataline)
    {
        Map<String, String> grantsValues = new HashMap<String, String>();
        grantsValues.put("ACCOUNT_CODE", (String) dataline.getFieldData("field02"));
        grantsValues.put("SETTLE_DATE", (String) dataline.getFieldData("field03"));
        grantsValues.put("IMPORT_DATE", (String) dataline.getFieldData("field04"));
        grantsValues.put("ACQ_MEM_ID", (String) dataline.getFieldData("field06"));
        grantsValues.put("TTL_CNT", (String) dataline.getFieldData("field07"));
        grantsValues.put("TTL_AMT", (String) dataline.getFieldData("field08"));
        grantsValues.put("SUBSIDY_AMT", (String) dataline.getFieldData("field09"));

        
        return grantsValues;
    }

    private Map<String, FieldInfo> getGrantsFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> grantsFieldInfos = new HashMap<String, FieldInfo>();
        grantsFieldInfos.put("ACCOUNT_CODE", dataline.getMappingInfo().getField("field02"));
        grantsFieldInfos.put("SETTLE_DATE", dataline.getMappingInfo().getField("field03"));
        grantsFieldInfos.put("IMPORT_DATE", dataline.getMappingInfo().getField("field04"));
        grantsFieldInfos.put("ACQ_MEM_ID", dataline.getMappingInfo().getField("field06"));
        grantsFieldInfos.put("TTL_CNT", dataline.getMappingInfo().getField("field07"));
        grantsFieldInfos.put("TTL_AMT", dataline.getMappingInfo().getField("field08"));
        grantsFieldInfos.put("SUBSIDY_AMT", dataline.getMappingInfo().getField("field09"));
        
        return grantsFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();	

    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(erptData.handleGrants(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpErpt getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpErpt instance = (ImpErpt) apContext.getBean("ImpErpt");
        return instance;
    }

    public static void main(String[] args) {
    	ImpErpt impErpt = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impErpt = getInstance();
            }
            else {
            	impErpt = new ImpErpt();
            }
            impErpt.setFileName("ERPT");
            impErpt.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpERPT run fail:" + ignore.getMessage(), ignore);
        }
    }

}
