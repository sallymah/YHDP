package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpRBMEM;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.bank.AbstractImpFile;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ImpBMEM extends AbstractImpFile {

	private static Logger log = Logger.getLogger(ImpBMEM.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
		    "impfiles" + File.separator + "ImpRBMEM" + File.separator + "spring.xml";
	
	private String batchDate = "";
	
	private BmemData data = new BmemData();
	
	public ImpBMEM() {}
	
	
	@Override
	public ExecuteSqlsInfo beforeHandleDataLine() throws Exception 
	{
		batchDate = System.getProperty("date");
        if (StringUtil.isEmpty(batchDate)) 
            batchDate = DateUtil.getTodayString().substring(0, 8);
		return null;
	}
	
	public List checkDataLine(DataLineInfo lineInfo) throws Exception 
	{
		Map<String, Object> tempMap = this.getValues(lineInfo);
		data = new BmemData();
		data.convertBeanFromMap(tempMap);
		
		List descInfos = super.checkDataLine(lineInfo);
		if (descInfos.size() > 0){
			return log(descInfos);
		}
		return descInfos;
	}
	
	@Override
	public List handleDataLine(DataLineInfo lineInfo) throws Exception 
	{
		List<ExecuteSqlsInfo> sqlsInfoList = new ArrayList<ExecuteSqlsInfo>();
    	ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(false);
        sqlsInfo.setSavepoint(true);
        sqlsInfo.setSqls(data.makeSqlList(conn, inctlInfo));
        sqlsInfoList.add(sqlsInfo);
        log.debug("handleDataLine:" + sqlsInfoList);
		return sqlsInfoList;
	}
	
	/**
	 * get DataLineInfo data
	 * @param lineInfo
	 * @return
	 */
    private Map<String, Object> getValues(DataLineInfo lineInfo)
    {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("CARD_NO", lineInfo.getFieldData("field02").toString().trim());
        values.put("STATUS", lineInfo.getFieldData("field03").toString().trim());
        values.put("HG_CARD_NO", lineInfo.getFieldData("field04").toString().trim());
        values.put("LOC_NAME", lineInfo.getFieldData("field05").toString().trim());
        values.put("PERSON_ID", lineInfo.getFieldData("field06").toString().trim());
        values.put("GENDER", lineInfo.getFieldData("field07").toString().trim());
        values.put("BIRTHDAY", lineInfo.getFieldData("field08").toString().trim());
        values.put("CITY", lineInfo.getFieldData("field09").toString().trim());
        values.put("ZIP_CODE", lineInfo.getFieldData("field10").toString().trim());
        values.put("ADDRESS", lineInfo.getFieldData("field11").toString().trim());
        values.put("TEL_HOME", lineInfo.getFieldData("field12").toString().trim());
        values.put("MOBILE", lineInfo.getFieldData("field13").toString().trim());
        values.put("EMAIL", lineInfo.getFieldData("field14").toString().trim());
        values.put("DM_FLAG", lineInfo.getFieldData("field15").toString().trim());
        values.put("LEGAL_AGENT_NAME", lineInfo.getFieldData("field16").toString().trim());
        values.put("LEGAL_AGENT_PID", lineInfo.getFieldData("field17").toString().trim());
        values.put("LEGAL_AGENT_MOBILE", lineInfo.getFieldData("field18").toString().trim());
        values.put("LEGAL_AGENT_PHONE", lineInfo.getFieldData("field19").toString().trim());
        values.put("VIP_FLAG", lineInfo.getFieldData("field20").toString().trim());
        values.put("ISSUE_DATE", lineInfo.getFieldData("field21").toString().trim());
        values.put("EXPIRY_DATE", lineInfo.getFieldData("field22").toString().trim());
        values.put("MERCH_ID", lineInfo.getFieldData("field23").toString().trim());
        values.put("MARRIAGE", lineInfo.getFieldData("field24").toString().trim());
        values.put("SALE_CODE", lineInfo.getFieldData("field25").toString().trim());
        values.put("R_CODE", lineInfo.getFieldData("field26").toString().trim());
        return values;
    }
    
	private List log(List descInfos) 
	{
		log.warn("checkDataLine:" + descInfos);
		return descInfos;
	}
	
	public static ImpBMEM getInstance() 
	{
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ImpBMEM instance = (ImpBMEM) apContext.getBean("ImpRBMEM");
		return instance;
	}
	
	public static void main(String[] args) {
		ImpBMEM impBMEM = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impBMEM = getInstance();
            	impBMEM.setFileName("RBMEM");
            	impBMEM.run(args);
            }
            else{
            	throw new Exception(SPRING_PATH + " is not exists.");
            }
        }
        catch (Exception ignore) {
            log.warn("ImpRBMEM run fail:" + ignore.getMessage(), ignore);
        }
	}
}
