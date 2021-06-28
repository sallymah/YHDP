/**
 * changelog
 * --------------------
 * 20140723
 * kevin
 * develop program
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpReturnBound;

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
 * ImpReturnBound(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpReturnBound extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpReturnBound.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpRefundIn" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private RetBoundData retBoundData = null;
       
    public ImpReturnBound()
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
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(retBoundData.addCardUpt(conn, batchDate));
        
        log.info("afterHandleDataLine:" + sqlsInfo2);    
        
        return sqlsInfo2;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	retBoundData = new RetBoundData(conn, getRetBoundValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	RetBoundChecker checker = new RetBoundChecker(retBoundData, getRetBoundFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getRetBoundValues(DataLineInfo dataline)
    {
        Map<String, String> returnBoundValues = new HashMap<String, String>();
        returnBoundValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        returnBoundValues.put("CUSTOMER_ID", (String) dataline.getFieldData("field03"));
        returnBoundValues.put("SALE_WAY", (String) dataline.getFieldData("field04"));
        
        return returnBoundValues;
    }

    private Map<String, FieldInfo> getRetBoundFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> returnBoundFieldInfos = new HashMap<String, FieldInfo>();
        returnBoundFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        returnBoundFieldInfos.put("CUSTOMER_ID", dataline.getMappingInfo().getField("field03"));
        returnBoundFieldInfos.put("SALE_WAY", dataline.getMappingInfo().getField("field04"));
        
        return returnBoundFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(retBoundData.handleRetBound(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpReturnBound getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpReturnBound instance = (ImpReturnBound) apContext.getBean("impReturnBound");
        return instance;
    }

    public static void main(String[] args) {
    	ImpReturnBound impReturnBound = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impReturnBound = getInstance();
            }
            else {
            	impReturnBound = new ImpReturnBound();
            }
            impReturnBound.setFileName("REFUNDIN");
            impReturnBound.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpReturnBound run fail:" + ignore.getMessage(), ignore);
        }
    }

}
