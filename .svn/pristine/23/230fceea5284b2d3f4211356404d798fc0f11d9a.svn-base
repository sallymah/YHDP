/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpAppload;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpAppload(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpAppload extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpAppload.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpAppload" + File.separator + "spring.xml";

    private String batchDate = "";
    private ApploadData apploadData = null;
    
    protected int exchangeLimit = 10000;
         
    public int getExchangeLimit() {
		return exchangeLimit;
	}

	public void setExchangeLimit(int exchangeLimit) {
		this.exchangeLimit = exchangeLimit;
	}

	public ImpAppload()
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
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	apploadData = new ApploadData(conn, getApploadValues(lineInfo), getImpFileInfo(), getInctlInfo(), lineInfo, exchangeLimit);
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	ApploadChecker checker = new ApploadChecker(apploadData, getApploadFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getApploadValues(DataLineInfo dataline)
    {
        Map<String, String> apploadValues = new HashMap<String, String>();
        apploadValues.put("EXCHANGE_DATE", (String) dataline.getFieldData("field01"));
        apploadValues.put("BARCODE1", (String) dataline.getFieldData("field02"));
        apploadValues.put("PRODUCT_ID", (String) dataline.getFieldData("field03"));
        apploadValues.put("HG_ORDER_NO", (String) dataline.getFieldData("field04"));
        apploadValues.put("EXCHANGE_SEQNO", (String) dataline.getFieldData("field05"));
        apploadValues.put("EXCHANGE_POINT", (String) dataline.getFieldData("field06"));
        apploadValues.put("BONUS_QTY", (String) dataline.getFieldData("field07"));
        
        return apploadValues;
    }

    private Map<String, FieldInfo> getApploadFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> apploadFieldInfos = new HashMap<String, FieldInfo>();
        apploadFieldInfos.put("EXCHANGE_DATE", dataline.getMappingInfo().getField("field01"));
        apploadFieldInfos.put("BARCODE1", dataline.getMappingInfo().getField("field02"));
        apploadFieldInfos.put("PRODUCT_ID", dataline.getMappingInfo().getField("field03"));
        apploadFieldInfos.put("HG_ORDER_NO", dataline.getMappingInfo().getField("field04"));
        apploadFieldInfos.put("EXCHANGE_SEQNO", dataline.getMappingInfo().getField("field05"));
        apploadFieldInfos.put("EXCHANGE_POINT", dataline.getMappingInfo().getField("field06"));
        apploadFieldInfos.put("BONUS_QTY", dataline.getMappingInfo().getField("field07"));
        
        return apploadFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo, int number) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        String fileDate = getImpFileInfo().getInctlInfo().getFileDate();
        String fileName = getImpFileInfo().getInctlInfo().getFullFileName();
        log.info("handleAppload: ");
        sqlsInfo2.setSqls(apploadData.handleAppload(conn, batchDate, fileName, getApploadFieldInfos(lineInfo), number));
        sqlsInfos.add(sqlsInfo2);
        
        //getImpFileInfo().setFileInfo(fileInfo)
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpAppload getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpAppload instance = (ImpAppload) apContext.getBean("ImpAppload");
        return instance;
    }

    public static void main(String[] args) {
    	ImpAppload impAppload = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impAppload = getInstance();
            }
            else {
            	impAppload = new ImpAppload();
            }
            impAppload.setFileName("HGTOYHDP_HG");
            impAppload.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpAppload run fail:" + ignore.getMessage(), ignore);
        }
    }
}
