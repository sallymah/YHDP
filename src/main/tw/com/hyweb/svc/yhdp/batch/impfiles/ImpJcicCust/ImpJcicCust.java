/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpJcicCust;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.core.cp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.cp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpAppload(For YHDP)
 * </pre>
 * author:Sally
 */
public class ImpJcicCust extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpJcicCust.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpJcicCust" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private JcicCustData jcicCustData = null;
      
    
    public ImpJcicCust()
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
        
    	jcicCustData = new JcicCustData(conn, getJcicValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	JcicCustChecker checker = new JcicCustChecker(jcicCustData, getJcicFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getJcicValues(DataLineInfo dataline)
    {
        Map<String, String> termValues = new HashMap<String, String>();
        termValues.put("PERSON_ID", (String) dataline.getFieldData("field01"));
        termValues.put("VERIFY_DATE", (String) dataline.getFieldData("field02"));
        termValues.put("VERIFY_CODE", (String) dataline.getFieldData("field03"));
        termValues.put("VERIFY_CODE_DESC", (String) dataline.getFieldData("field04"));

        return termValues;
    }

    private Map<String, FieldInfo> getJcicFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> termFieldInfos = new HashMap<String, FieldInfo>();
        termFieldInfos.put("PERSON_ID", dataline.getMappingInfo().getField("field01"));
        termFieldInfos.put("VERIFY_DATE", dataline.getMappingInfo().getField("field02"));
        termFieldInfos.put("VERIFY_CODE", dataline.getMappingInfo().getField("field03"));
        termFieldInfos.put("VERIFY_CODE_DESC", dataline.getMappingInfo().getField("field04"));
 
        return termFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        String fileDate = getImpFileInfo().getInctlInfo().getFileDate();
        String fullfilename = getImpFileInfo().getInctlInfo().getFullFileName();
        sqlsInfo2.setSqls(jcicCustData.handleCust(conn, batchDate, fileDate, fullfilename));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpJcicCust getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpJcicCust instance = (ImpJcicCust) apContext.getBean("ImpJcicCust");
        return instance;
    }

    public static void main(String[] args) {
    	ImpJcicCust impJcicCust = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impJcicCust = getInstance();
            }
            else {
            	impJcicCust = new ImpJcicCust();
            }
            impJcicCust.setFileName("JCIC_CUST");
            impJcicCust.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpJcicCust run fail:" + ignore.getMessage(), ignore);
        }
    }
}
