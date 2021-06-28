/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMerch;

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
 * ImpMerch(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpMerch extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpMerch.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpMerch" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private MerchData merchData = null;
      
    
    public ImpMerch()
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
        
    	merchData = new MerchData(conn, getMerchValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	MerchChecker checker = new MerchChecker(merchData, getMerchFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, Object> getMerchValues(DataLineInfo dataline)
    {
        Map<String, Object> merchValues = new HashMap<String, Object>();
        merchValues.put("MEM_ID", (String) dataline.getFieldData("field02"));
        merchValues.put("BUS_ID_NO", (String) dataline.getFieldData("field03"));
        merchValues.put("MERCH_LOC_NAME", (String) dataline.getFieldData("field04"));
        merchValues.put("MERCH_ENG_NAME", (String) dataline.getFieldData("field05"));
        merchValues.put("MERCH_ABBR_NAME", (String) dataline.getFieldData("field06"));
        merchValues.put("CONTACT", (String) dataline.getFieldData("field07"));
        merchValues.put("TEL", (String) dataline.getFieldData("field08"));
        merchValues.put("FAX", (String) dataline.getFieldData("field09"));
        merchValues.put("CITY", (String) dataline.getFieldData("field10"));
        merchValues.put("ZIP_CODE", (String) dataline.getFieldData("field11"));
        merchValues.put("ADDRESS", (String) dataline.getFieldData("field12"));
        merchValues.put("EMAIL", (String) dataline.getFieldData("field13"));
        merchValues.put("EFFECTIVE_DATE", (String) dataline.getFieldData("field14"));
        merchValues.put("SAM_LOGON_TIME", (String) dataline.getFieldData("field15"));
        merchValues.put("OFFLINE_MAX_COUNT", (String) dataline.getFieldData("field16"));
        merchValues.put("OFFLINE_MAX_AMT", (String) dataline.getFieldData("field17"));
        
        return merchValues;
    }

    private Map<String, FieldInfo> getMerchFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> merchFieldInfos = new HashMap<String, FieldInfo>();
        merchFieldInfos.put("MEM_ID", dataline.getMappingInfo().getField("field02"));
        merchFieldInfos.put("BUS_ID_NO", dataline.getMappingInfo().getField("field03"));
        merchFieldInfos.put("MERCH_LOC_NAME", dataline.getMappingInfo().getField("field04"));
        merchFieldInfos.put("MERCH_ENG_NAME", dataline.getMappingInfo().getField("field05"));
        merchFieldInfos.put("MERCH_ABBR_NAME", dataline.getMappingInfo().getField("field06"));
        merchFieldInfos.put("CONTACT", dataline.getMappingInfo().getField("field07"));
        merchFieldInfos.put("TEL", dataline.getMappingInfo().getField("field08"));
        merchFieldInfos.put("FAX", dataline.getMappingInfo().getField("field09"));
        merchFieldInfos.put("CITY", dataline.getMappingInfo().getField("field10"));
        merchFieldInfos.put("ZIP_CODE", dataline.getMappingInfo().getField("field11"));
        merchFieldInfos.put("ADDRESS", dataline.getMappingInfo().getField("field12"));
        merchFieldInfos.put("EMAIL", dataline.getMappingInfo().getField("field13"));
        merchFieldInfos.put("EFFECTIVE_DATE", dataline.getMappingInfo().getField("field14"));
        merchFieldInfos.put("SAM_LOGON_TIME", dataline.getMappingInfo().getField("field15"));
        merchFieldInfos.put("OFFLINE_MAX_COUNT", dataline.getMappingInfo().getField("field16"));
        merchFieldInfos.put("OFFLINE_MAX_AMT", dataline.getMappingInfo().getField("field17"));

        return merchFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        String fileDate = getImpFileInfo().getInctlInfo().getFileDate();
        sqlsInfo2.setSqls(merchData.handleCust(conn, batchDate, fileDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpMerch getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        log.info("SPRING_PATH: "+SPRING_PATH);
        ImpMerch instance = (ImpMerch) apContext.getBean("ImpMerch");
        return instance;
    }

    public static void main(String[] args) {
    	ImpMerch impMerch = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impMerch = getInstance();
            }
            else {
            	impMerch = new ImpMerch();
            }
            impMerch.setFileName("MERCH");
            impMerch.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpMerch run fail:" + ignore.getMessage(), ignore);
        }
    }
}
