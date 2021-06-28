/**
 * changelog
 * --------------------
 * 20140723
 * kevin
 * develop program
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCardLifeCycle;

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
 * ImpCardLifeCycle(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpCardLifeCycle extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpCardLifeCycle.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpCardLifeCycle" + File.separator + "beans-config.xml";

    private String batchDate = "";
    
    private CardLifeCycleData cardLifeCycleData = null;
      
    
    public ImpCardLifeCycle()
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
        sqlsInfo2.setSqls(cardLifeCycleData.addCardUpt(conn, batchDate));
        
        log.info("afterHandleDataLine:" + sqlsInfo2);    
        
        return sqlsInfo2;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	cardLifeCycleData = new CardLifeCycleData(conn, getCardLifeCycleValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	CardLifeCycleChecker checker = new CardLifeCycleChecker(cardLifeCycleData, getCardLifeCycleFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getCardLifeCycleValues(DataLineInfo dataline)
    {
        Map<String, String> cardLifeCycleValues = new HashMap<String, String>();
        cardLifeCycleValues.put("STORE_TYPE", (String) dataline.getFieldData("field02"));
        cardLifeCycleValues.put("CARD_NO", (String) dataline.getFieldData("field03"));
        cardLifeCycleValues.put("CUSTOMER_ID", (String) dataline.getFieldData("field04"));
        cardLifeCycleValues.put("SALE_WAY", (String) dataline.getFieldData("field05"));
        cardLifeCycleValues.put("ALTER_DATE", (String) dataline.getFieldData("field06"));
        cardLifeCycleValues.put("ALTER_TIME", (String) dataline.getFieldData("field07"));
        
        return cardLifeCycleValues;
    }

    private Map<String, FieldInfo> getCardLifeCycleFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> cardLifeCycleValuesFieldInfos = new HashMap<String, FieldInfo>();
        cardLifeCycleValuesFieldInfos.put("STORE_TYPE", dataline.getMappingInfo().getField("field02"));
        cardLifeCycleValuesFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field03"));
        cardLifeCycleValuesFieldInfos.put("CUSTOMER_ID", dataline.getMappingInfo().getField("field04"));
        cardLifeCycleValuesFieldInfos.put("SALE_WAY", dataline.getMappingInfo().getField("field05"));
        cardLifeCycleValuesFieldInfos.put("ALTER_DATE", dataline.getMappingInfo().getField("field06"));
        cardLifeCycleValuesFieldInfos.put("ALTER_TIME", dataline.getMappingInfo().getField("field07"));
        
        return cardLifeCycleValuesFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(cardLifeCycleData.handleInBound(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpCardLifeCycle getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpCardLifeCycle instance = (ImpCardLifeCycle) apContext.getBean("processor");
        return instance;
    }

    public static void main(String[] args) {
    	ImpCardLifeCycle impCardLifeCycle = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impCardLifeCycle = getInstance();
            }
            else {
            	impCardLifeCycle = new ImpCardLifeCycle();
            }
            impCardLifeCycle.setFileName("CDST");
            impCardLifeCycle.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpCardLifeCycle run fail:" + ignore.getMessage(), ignore);
        }
    }
}
