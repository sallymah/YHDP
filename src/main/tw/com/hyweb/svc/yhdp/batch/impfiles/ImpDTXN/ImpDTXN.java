/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpDTXN;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;

import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.bank.AbstractImpFile;
import tw.com.hyweb.util.string.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * ImpDTXN(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpDTXN extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpDTXN.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpDTXN" + File.separator + "beans-config.xml";

    private String batchDate = "";
    
    private DTXNData dTxnData = null;      
    
    public ImpDTXN()
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
        
    	dTxnData = new DTXNData(conn, getDTXNValues(lineInfo), inctlInfo.getFullFileName());
    	  	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	DTXNChecker checker = new DTXNChecker(dTxnData, getInctlInfo(), getDTXNFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, Object> getDTXNValues(DataLineInfo dataline)
    {
        Map<String, Object> dTxnValues = new HashMap<String, Object>();
        dTxnValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        dTxnValues.put("EXPIRY_DATE", (String) dataline.getFieldData("field03"));
        dTxnValues.put("TXN_MON", (String) dataline.getFieldData("field04"));
        dTxnValues.put("TTL_CNT", (String) dataline.getFieldData("field05"));
        dTxnValues.put("TTL_AMT", (Number) dataline.getFieldData("field06"));
        dTxnValues.put("REASON_CODE", (String) dataline.getFieldData("field07"));
        
        return dTxnValues;
    }

    private Map<String, FieldInfo> getDTXNFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> dTxnFieldInfos = new HashMap<String, FieldInfo>();
        dTxnFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        dTxnFieldInfos.put("EXPIRY_DATE", dataline.getMappingInfo().getField("field03"));
        dTxnFieldInfos.put("TXN_MON", dataline.getMappingInfo().getField("field04"));
        dTxnFieldInfos.put("TTL_CNT", dataline.getMappingInfo().getField("field05"));
        dTxnFieldInfos.put("TTL_AMT", dataline.getMappingInfo().getField("field06"));
        dTxnFieldInfos.put("REASON_CODE", dataline.getMappingInfo().getField("field07"));
        
        return dTxnFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();	

    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        
        sqlsInfo2.setSqls(dTxnData.handleDTXN(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpDTXN getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpDTXN instance = (ImpDTXN) apContext.getBean("processor");
        return instance;
    }

    public static void main(String[] args) {
    	ImpDTXN impDTXN = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impDTXN = getInstance();
            }
            else {
            	impDTXN = new ImpDTXN();
            }
            impDTXN.setFileName("DTXN");
            impDTXN.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpDTXN run fail:" + ignore.getMessage(), ignore);
        }
    }

}
