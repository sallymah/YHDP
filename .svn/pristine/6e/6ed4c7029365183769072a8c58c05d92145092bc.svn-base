package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMPMerch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.yhdp.batch.util.BatchUtil;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbPersoFactoryInfo;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/*
 * H14N01-2012BA012-0113-小額支付-Batch系統設計規格書-BatchSDD 4.1.1	匯入檔案 (特店匯入檔)
 */
public class ImpMPMerch extends AbstractImpFile {

	private static Logger log = Logger.getLogger(ImpMPMerch.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
		    "impfiles" + File.separator + "ImpMPMerch" + File.separator + "spring.xml";
	
	private String batchDate = "";
	private String batchTime = "";

	private MPMerchData data = new MPMerchData();
	private MPMerchChecker checker = new MPMerchChecker();
	
	
	public ImpMPMerch() {}
	
	
	@Override
	public ExecuteSqlsInfo beforeHandleDataLine() throws Exception 
	{
		batchDate = System.getProperty("date");
        if (StringUtil.isEmpty(batchDate)) 
            batchDate = DateUtil.getTodayString().substring(0, 8);
			
        data = new MPMerchData();
        checker = new MPMerchChecker();
        batchTime=DateUtil.getTodayString().substring(8, 14);
        final String MEMGROUPID_SPECIAL = "22222";
        final String MEMID_SPECIAL = "00000000";
        final String MERCHID_SPECIAL = "111111111111111";
        final String PERSO_FACTORY_RREMOTE = "PERSO_FACTORY";
        
        String workDir = "";
        String relativePath = impFileInfo.getFileInfo().getLocalPath();
        try {
            workDir = BatchUtil.getWorkDirectory();
        } catch (Exception ignore) {
            log.warn("should not happen:" + ignore.getMessage(), ignore);
        }
        if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1) {
            // find "22222"
            relativePath = relativePath.replaceAll(MEMGROUPID_SPECIAL, inctlInfo.getMemGroupId());
        }
        if (fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1) {
            // find "00000000"
            relativePath = relativePath.replaceAll(MEMID_SPECIAL, inctlInfo.getMemId());
        }
        if (fileInfo.getLocalPath().indexOf(MERCHID_SPECIAL) != -1) {
            // find "111111111111111"
            relativePath = relativePath.replaceAll(MERCHID_SPECIAL, inctlInfo.getMerchId());
        }
        
        if (fileInfo.getLocalPath().indexOf(PERSO_FACTORY_RREMOTE) != -1){
        	String persoFactoryId = inctlInfo.getFullFileName().substring(3, 5);
        	try {
				for (TbPersoFactoryInfo persoFactoryInfo : getTbPersoFactoryInfos(persoFactoryId))
					relativePath = relativePath.replaceAll(PERSO_FACTORY_RREMOTE, persoFactoryInfo.getRemoteFolder());
			} catch (SQLException e) {
				log.warn("get TbPersoFactory warn:" + e.getMessage(), e);
			}
        }
        
        workDir = pendingEndSep(workDir);
        relativePath = pendingEndSep(relativePath);
        
        String normalPath = FilenameUtils.normalize(workDir + relativePath + inctlInfo.getFullFileName());
        String headerRec = null;
        String trailorRec = null;
        FileReader fr = null;
        BufferedReader reader = null;
        try {
        	fr = new FileReader(normalPath);
            reader = new BufferedReader(fr);
            headerRec = reader.readLine();
            String line = null;
    		while ((line = reader.readLine ()) != null) 
    			trailorRec = line;
    		data.setHeaderRec(headerRec);
    		data.setTrailorRec(trailorRec);
        } finally {
        	ReleaseResource.releaseIO(reader);
        	ReleaseResource.releaseIO(fr);
        }
        
//        ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
//        sqlsInfo.setCommit(false);
//        sqlsInfo.setSavepoint(true);
//        sqlsInfo.setSqls(data.updateMerch(inctlInfo.getMemId(),conn));
       

//        List<String> list = data.updateMerch(inctlInfo.getMemId(),conn);
//        log.info("cnt :"+list.size());
//        if( list.size() > 0 )
//        {
//          ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
//          sqlsInfo.setCommit(false);
//          sqlsInfo.setSavepoint(true);
//          sqlsInfo.setSqls(list);
//          log.info("sqlsInfo :"+sqlsInfo);
//          return sqlsInfo;
//        }
        return null;
	}
	
	public List checkDataLine(DataLineInfo lineInfo) throws Exception 
	{
		Map<String, Object> map = getValues(lineInfo);
		map.put("BUS_MEM_ID", inctlInfo.getMemId());
		data.setData(conn, map);
		data.converBeanFromTrailer(data.getTrailorRec());
		data.convertBeanFromMap(map);
		checker.setData(data); //setting data before checking.
		
		List descInfos = super.checkDataLine(lineInfo);
		if (descInfos.size() > 0) return log(descInfos);
		descInfos = checker.checker();
		if (descInfos.size() > 0) return log(descInfos);
		descInfos = checker.checkTotDataNum(impFileInfo.getTotRec());
		if (descInfos.size() > 0) return log(descInfos);
		return descInfos;
	}
	
	@Override
	public List handleDataLine(DataLineInfo lineInfo) throws Exception 
	{
		if (checker.isBlankOrNull(data.getBean().getMerchId()))
			data.convertBeanFromMap(getValues(lineInfo));
		
		List sqlsInfos = new ArrayList();
    	ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(false);
        sqlsInfo.setSavepoint(true);
        sqlsInfo.setSqls(data.makeTbMerchSQL(conn));
        sqlsInfos.add(sqlsInfo);
        log.info("handleDataLine:" + sqlsInfos);
		return sqlsInfos;
	}
	
	@Override
	public ExecuteSqlsInfo afterHandleDataLine() throws Exception 
	{
//		ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
//        sqlsInfo.setCommit(false);
//        sqlsInfo.setSavepoint(true);
//        sqlsInfo.setSqls(data.procVaildMerch(conn));
	    ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(false);
        sqlsInfo.setSavepoint(true);
        sqlsInfo.setSqls(data.makeuptMerchStatus(conn,batchDate,batchTime));

        return sqlsInfo;
    }
	
	/**
	 * get DataLineInfo data
	 * @param lineInfo
	 * @return
	 */
    private Map<String, Object> getValues(DataLineInfo lineInfo)
    {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("MERCH_ID", (String) lineInfo.getFieldData("field03"));
        values.put("MERCH_LOC_NAME", (String) lineInfo.getFieldData("field04"));
        values.put("TEL", (String) lineInfo.getFieldData("field05"));
        values.put("FAX", (String) lineInfo.getFieldData("field07"));
        values.put("ADDRESS", (String) lineInfo.getFieldData("field08"));
        values.put("ZIP_CODE", (String) lineInfo.getFieldData("field09"));
        values.put("STATUS", (String) lineInfo.getFieldData("field10"));
        values.put("EFFECTIVE_DATE", (String) lineInfo.getFieldData("field11"));
        values.put("TERMINATION_DATE", (String) lineInfo.getFieldData("field12"));
        return values;
    }
	
	private List log(List descInfos) 
	{
		log.info("checkDataLine:" + descInfos);
		return descInfos;
	}
	
	private String pendingEndSep(String dir) {
        String ret = "";
        if (dir.endsWith("/") || dir.endsWith("\\")) {
            ret = dir;
        }
        else {
            ret = dir + File.separator;
        }
        return ret;
    }
	
	private List<TbPersoFactoryInfo> getTbPersoFactoryInfos(String persoFactoryId) throws SQLException
    {
    	Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    	Vector<TbPersoFactoryInfo> result = new Vector<TbPersoFactoryInfo>();
        new TbPersoFactoryMgr(conn).queryMultiple("PERSO_FACTORY_ID = '"+persoFactoryId+"'", result);
        ReleaseResource.releaseDB(conn, null, null);
        return result;
    }
	
	public static ImpMPMerch getInstance() 
	{
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ImpMPMerch instance = (ImpMPMerch) apContext.getBean("ImpMPMerch");
		return instance;
	}
	
	public static void main(String[] args) {
		ImpMPMerch impMPMerch = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impMPMerch = getInstance();
            }
            else {
            	impMPMerch = new ImpMPMerch();
            }
            impMPMerch.setFileName("MERC");
            impMPMerch.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpMPMerch run fail:" + ignore.getMessage(), ignore);
        }
	}
}
