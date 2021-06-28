package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBARL;

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

public class ImpBARL extends AbstractImpFile {

	private static Logger log = Logger.getLogger(ImpBARL.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
		    "impfiles" + File.separator + "ImpBARL" + File.separator + "spring.xml";
	
	private String batchDate = "";
	
	private BarlData data = new BarlData();
	private BarlChecker checker = new BarlChecker();
	
	
	public ImpBARL() {}
	
	
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
		tempMap.put("FULL_FILE_NAME", inctlInfo.getFullFileName());
		data = new BarlData();
		data.setData(conn, tempMap);
		data.convertBeanFromMap(tempMap);
		
		checker = new BarlChecker();
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
        values.put("END_DATE", lineInfo.getFieldData("field04").toString().trim());
        values.put("R_CODE", lineInfo.getFieldData("field05").toString().trim());
        return values;
    }
	
	private List log(List descInfos) 
	{
		log.warn("checkDataLine:" + descInfos);
		return descInfos;
	}
	
	public static ImpBARL getInstance() 
	{
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ImpBARL instance = (ImpBARL) apContext.getBean("ImpBARL");
		return instance;
	}
	
	public static void main(String[] args) {
		ImpBARL impBARL = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impBARL = getInstance();
            }
            else {
            	impBARL = new ImpBARL();
            }
            impBARL.setFileName("BARL");
            impBARL.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpBARL run fail:" + ignore.getMessage(), ignore);
        }
	}
}
