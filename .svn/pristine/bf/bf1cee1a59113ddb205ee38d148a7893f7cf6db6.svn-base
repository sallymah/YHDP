/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBTCB;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;

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
 * ImpBTCB(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpBTCB extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpBTCB.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpBTCB" + File.separator + "beans-config.xml";

    private String batchDate = "";
    
    private BTCBData btcbData = null;      
    
    public ImpBTCB()
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
        
    	btcbData = new BTCBData(conn, getBTCBValues(lineInfo), inctlInfo.getFullFileName());
    	  	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	BTCBChecker checker = new BTCBChecker(btcbData, getBTCBFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, Object> getBTCBValues(DataLineInfo dataline)
    {
        Map<String, Object> btcbValues = new HashMap<String, Object>();
        btcbValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        btcbValues.put("EXPIRY_DATE", (String) dataline.getFieldData("field03"));
        btcbValues.put("TXN_DATE", (String) dataline.getFieldData("field04"));
        btcbValues.put("TXN_TIME", (String) dataline.getFieldData("field05"));
        btcbValues.put("TXN_AMT", (Number) dataline.getFieldData("field06"));
        btcbValues.put("MEM_NAME", (String) dataline.getFieldData("field07"));
        btcbValues.put("MERCH_LOC_NAME", (String) dataline.getFieldData("field08"));
        btcbValues.put("MERCH_ID", (String) dataline.getFieldData("field09"));
        btcbValues.put("LMS_INVOICE_NO", (String) dataline.getFieldData("field10"));
        btcbValues.put("P_CODE", (String) dataline.getFieldData("field11"));
        btcbValues.put("REASON_CODE", (String) dataline.getFieldData("field12"));
        
        return btcbValues;
    }

    private Map<String, FieldInfo> getBTCBFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> btcbFieldInfos = new HashMap<String, FieldInfo>();
        btcbFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        btcbFieldInfos.put("EXPIRY_DATE", dataline.getMappingInfo().getField("field03"));
        btcbFieldInfos.put("TXN_DATE", dataline.getMappingInfo().getField("field04"));
        btcbFieldInfos.put("TXN_TIME", dataline.getMappingInfo().getField("field05"));
        btcbFieldInfos.put("TXN_AMT", dataline.getMappingInfo().getField("field06"));
        btcbFieldInfos.put("MEM_NAME", dataline.getMappingInfo().getField("field07"));
        btcbFieldInfos.put("MERCH_LOC_NAME", dataline.getMappingInfo().getField("field08"));
        btcbFieldInfos.put("MERCH_ID", dataline.getMappingInfo().getField("field09"));
        btcbFieldInfos.put("LMS_INVOICE_NO", dataline.getMappingInfo().getField("field10"));
        btcbFieldInfos.put("P_CODE", dataline.getMappingInfo().getField("field11"));
        btcbFieldInfos.put("REASON_CODE", dataline.getMappingInfo().getField("field12"));
        
        return btcbFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();	

    	ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
    	sqlsInfo.setCommit(false);
    	sqlsInfo.setSavepoint(true);
        
    	sqlsInfo.setSqls(btcbData.handleBTCB(conn, batchDate));
        sqlsInfos.add(sqlsInfo);
        
        if (btcbData.getFileData().get("P_CODE").equals("5717") 
        		| btcbData.getFileData().get("P_CODE").equals("5737")) {
        	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
            sqlsInfo2.setCommit(false);
            sqlsInfo2.setSavepoint(true);
            
            sqlsInfo2.setSqls(btcbData.addTrans(conn, batchDate, btcbData.getFileData().get("P_CODE").toString()));
            sqlsInfos.add(sqlsInfo2);
        }

        ExecuteSqlsInfo sqlsInfo3 = new ExecuteSqlsInfo();
        sqlsInfo3.setCommit(false);
        sqlsInfo3.setSavepoint(true);
        
        sqlsInfo3.setSqls(btcbData.modifyTrans(conn, batchDate));
        sqlsInfos.add(sqlsInfo3);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpBTCB getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpBTCB instance = (ImpBTCB) apContext.getBean("processor");
        return instance;
    }

    public static void main(String[] args) {
    	ImpBTCB impBTCB = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impBTCB = getInstance();
            }
            else {
            	impBTCB = new ImpBTCB();
            }
            impBTCB.setFileName("BTCB");
            impBTCB.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpBTCB run fail:" + ignore.getMessage(), ignore);
        }
    }
}
