package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBKCD;

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
import tw.com.hyweb.util.string.StringUtil;

public class ImpBKCD extends AbstractImpFile {

	private static Logger log = Logger.getLogger(ImpBKCD.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
		    "impfiles" + File.separator + "ImpBKCD" + File.separator + "spring.xml";
	
	private String batchDate = "";
	
	private BkcdData data = new BkcdData();
	private BkcdChecker checker = new BkcdChecker();
	
	
	public ImpBKCD() {}
	
	
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
		data = new BkcdData();
		data.setData(conn, tempMap);
		data.convertBeanFromMap(tempMap);
		
		checker = new BkcdChecker();
		checker.setData(data);
		
		List descInfos = super.checkDataLine(lineInfo);
		if (descInfos.size() > 0) return log(descInfos);
		descInfos = checker.checker(lineInfo);
		if (descInfos.size() > 0) return log(descInfos);
		return descInfos;
	}
	
	@Override
	public List handleDataLine(DataLineInfo lineInfo) throws Exception 
	{
		List<ExecuteSqlsInfo> sqlsInfoList = new ArrayList<ExecuteSqlsInfo>();
    	ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(false);
        sqlsInfo.setSavepoint(true);
        sqlsInfo.setSqls(data.makeSqlList(conn));
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
        values.put("STATUS", lineInfo.getFieldData("field02").toString().trim());
        values.put("CARD_NO", lineInfo.getFieldData("field03").toString().trim());
        values.put("EXPIRY_DATE", lineInfo.getFieldData("field04").toString().trim());
        values.put("FREEZE_DATE", lineInfo.getFieldData("field05").toString().trim());
        values.put("BANK_ID", lineInfo.getFieldData("field07").toString().trim());
        values.put("R_CODE", lineInfo.getFieldData("field08").toString().trim());
        return values;
    }
	
	private List log(List descInfos) 
	{
		log.warn("checkDataLine:" + descInfos);
		return descInfos;
	}
	
	public static ImpBKCD getInstance() 
	{
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ImpBKCD instance = (ImpBKCD) apContext.getBean("ImpBKCD");
		return instance;
	}
	
	public static void main(String[] args) {
		ImpBKCD impBKCD = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impBKCD = getInstance();
            }
            else {
            	impBKCD = new ImpBKCD();
            }
            impBKCD.setFileName("BKCD");
            impBKCD.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpBKCD run fail:" + ignore.getMessage(), ignore);
        }
	}
}
