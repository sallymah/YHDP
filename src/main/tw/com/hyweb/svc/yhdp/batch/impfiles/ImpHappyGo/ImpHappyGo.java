/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpHappyGo;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;

import tw.com.hyweb.util.string.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpHappyGo extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpHappyGo.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpHappyGo" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private HappyGoData happyGoData = null;      
    
    public ImpHappyGo()
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
        
    	happyGoData = new HappyGoData(conn, getHappyGoValues(lineInfo), inctlInfo.getFullFileName());
    	  	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	HappyGoChecker checker = new HappyGoChecker(happyGoData, getHappyGoFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getHappyGoValues(DataLineInfo dataline)
    {
        Map<String, String> happyGoValues = new HashMap<String, String>();
        happyGoValues.put("BARCODE1", (String) dataline.getFieldData("field01"));
        happyGoValues.put("BARCODE2", (String) dataline.getFieldData("field02"));
        
        return happyGoValues;
    }

    private Map<String, FieldInfo> getHappyGoFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> happyGoFieldInfos = new HashMap<String, FieldInfo>();
        happyGoFieldInfos.put("BARCODE1", dataline.getMappingInfo().getField("field01"));
        happyGoFieldInfos.put("BARCODE2", dataline.getMappingInfo().getField("field02"));
        
        return happyGoFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();	
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(happyGoData.handleHappyGo(conn, batchDate, getImpFileInfo()));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpHappyGo getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpHappyGo instance = (ImpHappyGo) apContext.getBean("impHappyGo");
        return instance;
    }

    public static void main(String[] args) {
    	ImpHappyGo impHappyGo = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impHappyGo = getInstance();
            	impHappyGo.setFileName("CARD");
                impHappyGo.run(args);
            }
            else {
            	log.error("[" + SPRING_PATH + "] is not exist.");
            }
            
        }
        catch (Exception ignore) {
            log.warn("ImpHappyGo run fail:" + ignore.getMessage(), ignore);
        }
    }

}
