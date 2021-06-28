/**
 * <pre>
 * ImpHappyGo(For YHDP)
 * </pre>
 * author:Kevin
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpOutBound;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpOutBound(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpOutBound extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpOutBound.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpOutBound" + File.separator + "spring.xml";
    
    private String sysDate = DateUtils.getSystemDate();
    private String sysTime = DateUtils.getSystemTime();

    private String batchDate = "";
    
    private OutBoundData outBoundData = null;
         
    public ImpOutBound()
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
        sqlsInfo2.setSqls(outBoundData.addCardUpt(conn, batchDate));
        
        log.info("afterHandleDataLine:" + sqlsInfo2);    
        
        return sqlsInfo2;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	outBoundData = new OutBoundData(conn, getOutBoundValues(lineInfo), sysDate, sysTime);
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	//更新CUSTOMER_ID和SALES_WAY
    	modifyCardField(conn, outBoundData);
    	
    	OutBoundChecker checker = new OutBoundChecker(outBoundData, getOutBoundFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    private void modifyCardField(Connection conn, OutBoundData importData) throws SQLException 
    {
    	StringBuffer sb = new StringBuffer();
    	sb.append("UPDATE TB_CARD SET");
    	sb.append(" CUSTOMER_ID='" + importData.getFileData().get("CUSTOMER_ID") + "',");
    	sb.append(" SALE_WAY='" + importData.getFileData().get("SALE_WAY") + "',");
    	sb.append(" UPT_DATE='" + sysDate + "',").append(" UPT_TIME='" + sysTime + "',");
    	sb.append(" APRV_DATE='" + sysDate + "',").append(" APRV_TIME='" + sysTime + "',");
    	sb.append(" UPT_USERID='batch',").append(" APRV_USERID='batch',").append(" UPDATE_TYPE='2'");
    	sb.append(" WHERE CARD_NO='" + importData.getFileData().get("CARD_NO") + "'");
    	
    	log.info("sql: " + sb.toString());
    	
    	SqlResult sr = DbUtil.sqlAction(sb.toString(), conn);	
    	
    	log.info("update count: " + sr.getRecordCount());
	}

	/**
     * @param dataline
     * @return
     */
    private Map<String, String> getOutBoundValues(DataLineInfo dataline)
    {
        Map<String, String> outBoundValues = new HashMap<String, String>();
        outBoundValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        outBoundValues.put("CUSTOMER_ID", (String) dataline.getFieldData("field03"));
        outBoundValues.put("SALE_WAY", (String) dataline.getFieldData("field04"));
        
        return outBoundValues;
    }

    private Map<String, FieldInfo> getOutBoundFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> outBoundFieldInfos = new HashMap<String, FieldInfo>();
        outBoundFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        outBoundFieldInfos.put("CUSTOMER_ID", dataline.getMappingInfo().getField("field03"));
        outBoundFieldInfos.put("SALE_WAY", dataline.getMappingInfo().getField("field04"));
        
        return outBoundFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(outBoundData.handleOutBound(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpOutBound getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpOutBound instance = (ImpOutBound) apContext.getBean("impOutBound");
        return instance;
    }

    public static void main(String[] args) {
    	ImpOutBound impOutBound = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impOutBound = getInstance();
            }
            else {
            	impOutBound = new ImpOutBound();
            }
            impOutBound.setFileName("STOCKOUT");
            impOutBound.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpOutBound run fail:" + ignore.getMessage(), ignore);
        }
    }

}
