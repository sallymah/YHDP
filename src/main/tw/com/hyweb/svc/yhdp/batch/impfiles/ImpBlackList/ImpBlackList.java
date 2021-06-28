/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBlackList;

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
 * ImpBlackList(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpBlackList extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpBlackList.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpBlackList" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private BlackListData blackListData = null;
      
    public ImpBlackList()
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
        
    	blackListData = new BlackListData(conn, getBlackListValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	BlackListChecker checker = new BlackListChecker(blackListData, getBlackListFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getBlackListValues(DataLineInfo dataline)
    {
        Map<String, String> blackListValues = new HashMap<String, String>();
        blackListValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        
        return blackListValues;
    }

    private Map<String, FieldInfo> getBlackListFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> blackListFieldInfos = new HashMap<String, FieldInfo>();
        blackListFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        
        return blackListFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        String fileDate = getImpFileInfo().getInctlInfo().getFileDate();
        sqlsInfo2.setSqls(blackListData.handleBlackList(conn, batchDate, fileDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpBlackList getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpBlackList instance = (ImpBlackList) apContext.getBean("impBlackList");
        return instance;
    }

    public static void main(String[] args) {
    	ImpBlackList impBlackList = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impBlackList = getInstance();
            }
            else {
            	impBlackList = new ImpBlackList();
            }
            impBlackList.setFileName("BLACKLIST");
            impBlackList.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpBlackList run fail:" + ignore.getMessage(), ignore);
        }
    }
}
