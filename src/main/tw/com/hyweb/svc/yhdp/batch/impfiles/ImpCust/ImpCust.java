/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCust;

import java.io.File;
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
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.svc.yhdp.online.CacheTbSysConfig;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpAppload(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpCust extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpCust.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpCust" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private CustData custData = null;
      
    
    public ImpCust()
    {
    }

    
    public ExecuteSqlsInfo beforeHandleDataLine() throws Exception
    {
    	custData = new CustData();
    	custData.setUptTime(DateUtils.getSystemTime());
    	
    	CacheTbSysConfig.getInstance().reload(conn);
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
    	custData.setFileData(getCustValues(lineInfo));
    	custData.initial(conn);
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	CustChecker checker = new CustChecker(custData, getCustFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getCustValues(DataLineInfo dataline)
    {
        Map<String, String> custValues = new HashMap<String, String>();
        custValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        custValues.put("LOC_NAME", (String) dataline.getFieldData("field03"));
        custValues.put("PERSON_ID", (String) dataline.getFieldData("field04"));
        custValues.put("GENDER", (String) dataline.getFieldData("field05"));
        custValues.put("BIRTHDAY", (String) dataline.getFieldData("field06"));
        custValues.put("CITY", (String) dataline.getFieldData("field07"));
        custValues.put("ZIP_CODE", (String) dataline.getFieldData("field08"));
        custValues.put("ADDRESS", (String) dataline.getFieldData("field09"));
        custValues.put("TEL_HOME", (String) dataline.getFieldData("field10"));
        custValues.put("MOBILE", (String) dataline.getFieldData("field11"));
        custValues.put("EMAIL", (String) dataline.getFieldData("field12"));
        custValues.put("DM_FLAG", (String) dataline.getFieldData("field13"));
        custValues.put("LEGAL_AGENT_NAME", (String) dataline.getFieldData("field14"));
        custValues.put("LEGAL_AGENT_PID", (String) dataline.getFieldData("field15"));
        custValues.put("LEGAL_AGENT_MOBILE", (String) dataline.getFieldData("field16"));
        custValues.put("LEGAL_AGENT_PHONE", (String) dataline.getFieldData("field17"));
        custValues.put("VIP_FLAG", (String) dataline.getFieldData("field18"));
        custValues.put("MBR_REG_DATE", (String) dataline.getFieldData("field19"));
        custValues.put("MERCH_ID", (String) dataline.getFieldData("field20"));
        custValues.put("MARRIAGE", (String) dataline.getFieldData("field21"));
        custValues.put("SA_NO", (String) dataline.getFieldData("field22"));
        custValues.put("HG_AUTH", (String) dataline.getFieldData("field23"));
        custValues.put("COUNTRY_CODE", (String) dataline.getFieldData("field24"));
        return custValues;
    }

    private Map<String, FieldInfo> getCustFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> custFieldInfos = new HashMap<String, FieldInfo>();
        custFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        custFieldInfos.put("LOC_NAME", dataline.getMappingInfo().getField("field03"));
        custFieldInfos.put("PERSON_ID", dataline.getMappingInfo().getField("field04"));
        custFieldInfos.put("GENDER", dataline.getMappingInfo().getField("field05"));
        custFieldInfos.put("BIRTHDAY", dataline.getMappingInfo().getField("field06"));
        custFieldInfos.put("CITY", dataline.getMappingInfo().getField("field07"));
        custFieldInfos.put("ZIP_CODE", dataline.getMappingInfo().getField("field08"));
        custFieldInfos.put("ADDRESS", dataline.getMappingInfo().getField("field09"));
        custFieldInfos.put("TEL_HOME", dataline.getMappingInfo().getField("field10"));
        custFieldInfos.put("MOBILE", dataline.getMappingInfo().getField("field11"));
        custFieldInfos.put("EMAIL", dataline.getMappingInfo().getField("field12"));
        custFieldInfos.put("DM_FLAG", dataline.getMappingInfo().getField("field13"));
        custFieldInfos.put("LEGAL_AGENT_NAME", dataline.getMappingInfo().getField("field14"));
        custFieldInfos.put("LEGAL_AGENT_PID", dataline.getMappingInfo().getField("field15"));
        custFieldInfos.put("LEGAL_AGENT_MOBILE", dataline.getMappingInfo().getField("field16"));
        custFieldInfos.put("LEGAL_AGENT_PHONE", dataline.getMappingInfo().getField("field17"));
        custFieldInfos.put("VIP_FLAG", dataline.getMappingInfo().getField("field18"));
        custFieldInfos.put("MBR_REG_DATE", dataline.getMappingInfo().getField("field19"));
        custFieldInfos.put("MERCH_ID", dataline.getMappingInfo().getField("field20"));
        custFieldInfos.put("MARRIAGE", dataline.getMappingInfo().getField("field21"));
        custFieldInfos.put("SA_NO", dataline.getMappingInfo().getField("field22"));
        custFieldInfos.put("HG_AUTH", dataline.getMappingInfo().getField("field23"));
        custFieldInfos.put("COUNTRY_CODE", dataline.getMappingInfo().getField("field24"));
        return custFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        String fileDate = getImpFileInfo().getInctlInfo().getFileDate();
        String fullfilename = getImpFileInfo().getInctlInfo().getFullFileName();
        sqlsInfo2.setSqls(custData.handleCust(conn, batchDate, fileDate, fullfilename));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpCust getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpCust instance = (ImpCust) apContext.getBean("ImpCust");
        return instance;
    }

    public static void main(String[] args) {
    	ImpCust impCust = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impCust = getInstance();
            }
            else {
            	impCust = new ImpCust();
            }
            impCust.setFileName("CUST");
            impCust.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpCust run fail:" + ignore.getMessage(), ignore);
        }
    }
}
