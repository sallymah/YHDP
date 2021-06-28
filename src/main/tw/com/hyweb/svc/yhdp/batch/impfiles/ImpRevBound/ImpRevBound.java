/**
 * changelog
 * --------------------
 * 20140723
 * kevin
 * develop program
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpRevBound;

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
public class ImpRevBound extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpRevBound.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpRevStockIn" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private RevBoundData revBoundData = null;
       
    public ImpRevBound()
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
        sqlsInfo2.setSqls(revBoundData.addCardUpt(conn, batchDate));
        
        log.info("afterHandleDataLine:" + sqlsInfo2);    
        
        return sqlsInfo2;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	revBoundData = new RevBoundData(conn, getRevBoundValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	RevBoundChecker checker = new RevBoundChecker(revBoundData, getRevBoundFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getRevBoundValues(DataLineInfo dataline)
    {
        Map<String, String> revBoundValues = new HashMap<String, String>();
        revBoundValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        
        return revBoundValues;
    }

    private Map<String, FieldInfo> getRevBoundFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> revBoundFieldInfos = new HashMap<String, FieldInfo>();
        revBoundFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        
        return revBoundFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(revBoundData.handleRetBound(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpRevBound getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpRevBound instance = (ImpRevBound) apContext.getBean("ImpRevBound");
        return instance;
    }

    public static void main(String[] args) {
    	ImpRevBound impRevBound = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impRevBound = getInstance();
            }
            else {
            	impRevBound = new ImpRevBound();
            }
            impRevBound.setFileName("RESTOCKIN");
            impRevBound.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpRevBound run fail:" + ignore.getMessage(), ignore);
        }
    }

}
