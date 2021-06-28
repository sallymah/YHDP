/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImEpaGpCard;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;
/**
 * <pre>
 * ImEpaGpCard(For YHDP)
 * </pre>
 */
public class ImEpaGpCard extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImEpaGpCard.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImEpaGpCard" + File.separator + "spring.xml";

    private String batchDate = "";
    private String batchTime = "";
    
    private EpaGpCardData epaGpCardData = null;      
    
    public ImEpaGpCard()
    {
    	
    }
    
    
    
    public ExecuteSqlsInfo beforeHandleDataLine() throws Exception
    {
    	batchDate = System.getProperty("date");
        if (StringUtil.isEmpty(batchDate)) {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }   	       
        batchTime=DateUtil.getTodayString().substring(8);
        
        init(conn);
        
        return null;
    }
    
    private void init(Connection conn) throws Exception{
		try
		{
			StringBuffer sqlCmd=new StringBuffer();
			sqlCmd.append("update TB_EPA_GP_CARD set EPA_GP_STATUS = 0");
			DbUtil.sqlAction(sqlCmd.toString(), conn);
			sqlCmd.delete(0, sqlCmd.length());

		} catch (Exception e) {
			throw new Exception("init fail:"+e);
		}
        log.info("init() ok.\n");
	}



	public ExecuteSqlsInfo afterHandleDataLine() throws Exception
    {    	
		ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
		sqlsInfo2.setCommit(false);
		sqlsInfo2.setSavepoint(true);
		List sqls = new ArrayList();

		StringBuffer sqlCmd=new StringBuffer();
		sqlCmd.append("update TB_EPA_GP_CARD set CANCEL_DATE = '"+batchDate+"', CANCEL_TIME = '"+batchTime+"' ");
		sqlCmd.append("where EPA_GP_STATUS = 0 and RECEIVE_DATE > NVL(CANCEL_DATE, '00000000')");
		sqls.add(sqlCmd.toString());
		sqlsInfo2.setSqls(sqls);
	
    	 return sqlsInfo2;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	epaGpCardData = new EpaGpCardData(conn, getEpaGpCardValues(lineInfo));
    	  	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	EpaGpCardChecker checker = new EpaGpCardChecker(epaGpCardData, getEpaGpCardFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getEpaGpCardValues(DataLineInfo dataline)
    {
        Map<String, String> happyGoValues = new HashMap<String, String>();
        happyGoValues.put("CARD_PHYSICAL_ID", (String) dataline.getFieldData("field01"));
        happyGoValues.put("RECEIVE_DATE", (String) dataline.getFieldData("field02"));
        happyGoValues.put("RECEIVE_CONTENT", (String) dataline.getFieldData("field03"));

        
        return happyGoValues;
    }

	private Map<String, FieldInfo> getEpaGpCardFieldInfos(DataLineInfo dataline)
	{
        Map<String, FieldInfo> epaGpCardFieldInfos = new HashMap<String, FieldInfo>();
    	epaGpCardFieldInfos.put("CARD_PHYSICAL_ID", dataline.getMappingInfo().getField("field01"));
    	epaGpCardFieldInfos.put("RECEIVE_DATE", dataline.getMappingInfo().getField("field02"));
    	epaGpCardFieldInfos.put("RECEIVE_CONTENT", dataline.getMappingInfo().getField("field03"));
        return epaGpCardFieldInfos;
	}

    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();	
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(epaGpCardData.handleGpCard(conn, batchDate,batchTime));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImEpaGpCard getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImEpaGpCard instance = (ImEpaGpCard) apContext.getBean("impEpaGpCard");
        return instance;
    }

    public static void main(String[] args) {
    	ImEpaGpCard impEpaGpCard = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impEpaGpCard = getInstance();
            }
            else {
            	impEpaGpCard = new ImEpaGpCard();
            }
            impEpaGpCard.setFileName("GP_CARD");
            impEpaGpCard.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImEpaGpCard run fail:" + ignore.getMessage(), ignore);
        }
    }

}
