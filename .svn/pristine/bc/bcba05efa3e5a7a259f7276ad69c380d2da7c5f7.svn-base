/**
 * changelog
 * --------------------
 * 20140723
 * kevin
 * develop program
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpInBound;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
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
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpInBound(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpInBound extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpInBound.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpInBound" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private InBoundData inBoundData = null;
      
    
    public ImpInBound()
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
        sqlsInfo2.setSqls(inBoundData.addCardUpt(conn, batchDate));
        
        log.info("afterHandleDataLine:" + sqlsInfo2);    
        
        return sqlsInfo2;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	inBoundData = new InBoundData(conn, getInBoundValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	InBoundChecker checker = new InBoundChecker(inBoundData, getInBoundFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getInBoundValues(DataLineInfo dataline)
    {
        Map<String, String> inBoundValues = new HashMap<String, String>();
        inBoundValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        //inBoundValues.put("CUSTOMER_ID", (String) dataline.getFieldData("field03"));
        //inBoundValues.put("SALE_WAY", (String) dataline.getFieldData("field04"));
        
        return inBoundValues;
    }

    private Map<String, FieldInfo> getInBoundFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> inBoundFieldInfos = new HashMap<String, FieldInfo>();
        inBoundFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        //inBoundFieldInfos.put("CUSTOMER_ID", dataline.getMappingInfo().getField("field03"));
        //inBoundFieldInfos.put("SALE_WAY", dataline.getMappingInfo().getField("field04"));
        
        return inBoundFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(inBoundData.handleInBound(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpInBound getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpInBound instance = (ImpInBound) apContext.getBean("impInBound");
        return instance;
    }

    public static void main(String[] args) {
    	ImpInBound impInBound = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impInBound = getInstance();
            }
            else {
            	impInBound = new ImpInBound();
            }
            impInBound.setFileName("STOCKIN");
            impInBound.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpInBound run fail:" + ignore.getMessage(), ignore);
        }
    }
}
