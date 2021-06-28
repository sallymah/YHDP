/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTerm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpAppload(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpTerm extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpTerm.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpTerm" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private TermData termData = null;
      
    
    public ImpTerm()
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
        
    	termData = new TermData(conn, getTermValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	TermChecker checker = new TermChecker(termData, getTermFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getTermValues(DataLineInfo dataline)
    {
        Map<String, String> termValues = new HashMap<String, String>();
        termValues.put("MEM_ID", (String) dataline.getFieldData("field02"));
        termValues.put("MERCH_LOC_NAME", (String) dataline.getFieldData("field03"));
        termValues.put("STORE_COUNTER_ID", (String) dataline.getFieldData("field04"));
        termValues.put("ECR_ID", (String) dataline.getFieldData("field05"));
        termValues.put("STATUS", (String) dataline.getFieldData("field06"));
        termValues.put("EFFECTIVE_DATE", (String) dataline.getFieldData("field07"));
        termValues.put("TERMINATION_DATE", (String) dataline.getFieldData("field08"));
        termValues.put("TERM_VENDOR", (String) dataline.getFieldData("field09"));
        termValues.put("TERM_TYPE", (String) dataline.getFieldData("field10"));
        termValues.put("FUNC", (String) dataline.getFieldData("field11"));
        
        return termValues;
    }

    private Map<String, FieldInfo> getTermFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> termFieldInfos = new HashMap<String, FieldInfo>();
        termFieldInfos.put("MEM_ID", dataline.getMappingInfo().getField("field02"));
        termFieldInfos.put("MERCH_LOC_NAME", dataline.getMappingInfo().getField("field03"));
        termFieldInfos.put("STORE_COUNTER_ID", dataline.getMappingInfo().getField("field04"));
        termFieldInfos.put("ECR_ID", dataline.getMappingInfo().getField("field05"));
        termFieldInfos.put("STATUS", dataline.getMappingInfo().getField("field06"));
        termFieldInfos.put("EFFECTIVE_DATE", dataline.getMappingInfo().getField("field07"));
        termFieldInfos.put("TERMINATION_DATE", dataline.getMappingInfo().getField("field08"));
        termFieldInfos.put("TERM_VENDOR", dataline.getMappingInfo().getField("field09"));
        termFieldInfos.put("TERM_TYPE", dataline.getMappingInfo().getField("field10"));
        termFieldInfos.put("FUNC", dataline.getMappingInfo().getField("field11"));
        
        return termFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        String fileDate = getImpFileInfo().getInctlInfo().getFileDate();
        sqlsInfo2.setSqls(termData.handleCust(conn, batchDate, fileDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpTerm getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpTerm instance = (ImpTerm) apContext.getBean("ImpTerm");
        return instance;
    }

    public static void main(String[] args) {
    	ImpTerm impTerm = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impTerm = getInstance();
            }
            else {
            	impTerm = new ImpTerm();
            }
            impTerm.setFileName("TERM");
            impTerm.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpCust run fail:" + ignore.getMessage(), ignore);
        }
    }
}
