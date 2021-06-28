/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpJcicCard;

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
 * ImpJcicCard(For YHDP)
 * </pre>
 * author:Sally
 */
public class ImpJcicCard extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpJcicCard.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpJcicCard" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private JcicCardData jcicCardData = null;
      
    
    public ImpJcicCard()
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
        
    	jcicCardData = new JcicCardData(conn, getJcicValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	JcicCardChecker checker = new JcicCardChecker(jcicCardData, getJcicFieldInfos(lineInfo));
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
        termValues.put("CARD_NO", (String) dataline.getFieldData("field01"));
        termValues.put("NOTIFY_TYPE", (String) dataline.getFieldData("field02"));
        termValues.put("NOTIFY_DATE", (String) dataline.getFieldData("field03"));
        termValues.put("NOTIFY_DESC", (String) dataline.getFieldData("field04"));

        return termValues;
    }

    private Map<String, FieldInfo> getJcicFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> termFieldInfos = new HashMap<String, FieldInfo>();
        termFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field01"));
        termFieldInfos.put("NOTIFY_TYPE", dataline.getMappingInfo().getField("field02"));
        termFieldInfos.put("NOTIFY_CODE", dataline.getMappingInfo().getField("field03"));
        termFieldInfos.put("NOTIFY_DESC", dataline.getMappingInfo().getField("field04"));
 
        return termFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        String fileDate = getImpFileInfo().getInctlInfo().getFileDate();
        sqlsInfo2.setSqls(jcicCardData.handleCard(conn, batchDate, fileDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpJcicCard getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpJcicCard instance = (ImpJcicCard) apContext.getBean("ImpJcicCard");
        return instance;
    }

    public static void main(String[] args) {
    	ImpJcicCard impJcicCard = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impJcicCard = getInstance();
            }
            else {
            	impJcicCard = new ImpJcicCard();
            }
            impJcicCard.setFileName("JCIC_CARD");
            impJcicCard.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpCust run fail:" + ignore.getMessage(), ignore);
        }
    }
}
