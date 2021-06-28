package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCDRQ;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsUtil;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbImportDataLogInfo;
import tw.com.hyweb.service.db.info.TbInctlErrInfo;
import tw.com.hyweb.service.db.mgr.TbImportDataLogMgr;
import tw.com.hyweb.service.db.mgr.TbInctlErrMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class ImpCDRQ extends AbstractImpFile {

	private static Logger log = Logger.getLogger(ImpCDRQ.class);
	
	private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
		    "impfiles" + File.separator + "ImpCDRQ" + File.separator + "spring.xml";
	
	private String batchDate = null;
	private String rCode = "0000";
	
	private CdrqData data = new CdrqData();
	private CdrqChecker checker = new CdrqChecker();
	
	
	public ImpCDRQ() {}
	
	
	@Override
	public ExecuteSqlsInfo beforeHandleDataLine() throws Exception 
	{
		batchDate = System.getProperty("date");
        if (StringUtil.isEmpty(batchDate)) 
            batchDate = DateUtil.getTodayString().substring(0, 8);
		return null;
	}
	
	@Override
	public boolean handleImpFileInfo() throws Exception {
        boolean autoCommit = false;
        try {
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            // 依 workFlag 來決定從那一筆開始
            int start = 0;
            if (Layer1Constants.WORKFLAG_INWORK.equals(inctlInfo.getWorkFlag())) {
                // INWORK
                impFileInfo.setRecCnt(0);
                impFileInfo.setSucCnt(0);
                impFileInfo.setFailCnt(0);
                start = 0;
            }
            else if (Layer1Constants.WORKFLAG_PROCESSING.equals(inctlInfo.getWorkFlag())) {
                // PROCESSING
                impFileInfo.setRecCnt(inctlInfo.getRecCnt().intValue());
                impFileInfo.setSucCnt(inctlInfo.getSucCnt().intValue());
                impFileInfo.setFailCnt(inctlInfo.getFailCnt().intValue());
                start = impFileInfo.getRecCnt();
            }
            // 讓 batchResultInfo 和 inctlInfo 相關, 且 markup inctlInfo start
            inctlInfo = inctlBean.makeInctl(inctlInfo);
            updateInctl(Layer1Constants.WORKFLAG_PROCESSING, true);
            // checkFile
            boolean ret = impFileInfo.checkFile();
            if (!ret) {
            	rCode = impFileInfo.getrCode();
            	impFileInfo.skipHeader();
                // checkFile fail, 整檔踢退, 不處理
            	inctlInfo.setRcode("2999");
                updateInctl(Layer1Constants.WORKFLAG_PROCESSFAIL, true);
                thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
//                return false;  // 有錯誤一樣要寫入table
            }
            // beforeHandleDataLine
            ExecuteSqlsInfo beforeSqlsInfo = beforeHandleDataLine();
            if (beforeSqlsInfo == null) {
                // can be null, just log, do nothing
                log.info("beforeSqlsInfo is null!");
            }
            else {
                // execute beforeSqlsInfo
                if (doCommit) {
                    ExecuteSqlsUtil.executeSqls(conn, beforeSqlsInfo);
                }
                else {
                    // 有錯整個檔案都不處理
                    try {
                        ExecuteSqlsUtil.executeSqls(conn, beforeSqlsInfo);
                    }
                    catch (Exception ignore) {
                        // 做 rollback 並註記 TB_INCTL 為處理失敗
                        conn.rollback();
                        inctlInfo.setRcode("2999");
                        updateInctl(Layer1Constants.WORKFLAG_PROCESSFAIL, true);
                        throw ignore;
                    }
                }
            }
            // handle each DataLineInfo
            for (int i = start; i < impFileInfo.getTotRec(); i++) {
                DataLineInfo lineInfo = null;
                try {
                    lineInfo = impFileInfo.readOneDataLine();
                    setData(lineInfo);
                    if (ret) {
                    	List descInfos = checkDataLine(lineInfo);
                        if (descInfos != null && descInfos.size() > 0) {
                            // set failCnt and insert inctlErrInfo
                            insertInctlErrInfo(lineInfo, descInfos); // 有錯誤一樣要寫入table
                        }
                    }
                    // handleDataLine
                    List sqlsInfos = handleDataLine(lineInfo);
                    if (sqlsInfos == null || sqlsInfos.size() == 0) {
                        log.warn("sqlsInfos is null or empty!");
                    }
                    else {
                        // execute sqlsInfos
                        for (int j = 0; j < sqlsInfos.size(); j++) {
                            ExecuteSqlsInfo sqlsInfo = (ExecuteSqlsInfo) sqlsInfos.get(j);
                            ExecuteSqlsUtil.executeSqls(conn, sqlsInfo);
                        }
                    }
                    
                    insertImportDataLogInfo(conn, lineInfo);
                    
                    // run here, handle one lineInfo OK
                    if ("0000".equals(rCode)) 
                    	impFileInfo.setSucCnt(impFileInfo.getSucCnt() + 1);
                    else
                    	impFileInfo.setFailCnt(impFileInfo.getFailCnt() + 1);

                    if (doCommit) {
                        if (impFileInfo.getRecCnt() % recordsPerCommit == 0) {
                            // updateInctl and do commit
                            updateInctl(Layer1Constants.WORKFLAG_PROCESSING, false);
                            conn.commit();
                            try {
                                Thread.sleep(sleepPerCommit);
                            }
                            catch (Exception ignore) {
                                ;
                            }
                        }
                    }
                    else {
                        // 有錯整個檔案都不處理, 所以不用做任何事
                        ;
                    }
                }
                catch (Exception ignore) {
                    log.warn("handle lineInfo error:" + ignore.getMessage(), ignore);
                    ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2791_GENERALDB_ERR, null, "unknown");
                    // using exception message
                    descInfo.setErrorMsg(ignore.getMessage());
                    // set failCnt and insert inctlErrInfo
                    // should not happen exception in here
                    if (doCommit) {
                        insertInctlErrInfo(lineInfo, descInfo);
                    }
                    else {
                        // 有錯整個檔案都不處理
                        // 先做 rollback, 再 TB_INCTL_ERR 記錄那一筆出錯, 並跳開
                        conn.rollback();
                        insertInctlErrInfo(lineInfo, descInfo);
                        break;
                    }
                }
                
                if (ret) rCode = "0000";
            }
            // do last commit
            // updateInctl and do commit
            if (ret) updateInctl(Layer1Constants.WORKFLAG_PROCESSOK, false);

            conn.commit();
            try {
                Thread.sleep(sleepPerCommit);
            }
            catch (Exception ignore) {
                ;
            }
            // afterHandleDataLine
            ExecuteSqlsInfo afterSqlsInfo = afterHandleDataLine();
            if (afterSqlsInfo == null) {
                // can be null, just log, do nothing
                log.info("afterSqlsInfo is null!");
            }
            else {
                // execute afterSqlsInfo
                ExecuteSqlsUtil.executeSqls(conn, afterSqlsInfo);
                conn.commit();
            }
            // set rcode by xxxCnt
            if (impFileInfo.getTotRec() > 0 
            		&& (impFileInfo.getTotRec() == impFileInfo.getFailCnt())) {
                thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
            }
            else if (impFileInfo.getFailCnt() > 0) {
                if (doCommit) {
                    thisRcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;
                }
                else {
                    thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
                }
            }
        }
        finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommit);
                }
                catch (Exception ignore) {
                    ;
                }
            }
            ReleaseResource.releaseDB(conn);
        }
        return true;
    }
	
	private void setData(DataLineInfo lineInfo)  throws Exception
	{
		Map<String, Object> tempMap = this.getValues(lineInfo);
		tempMap.put("FULL_FILE_NAME", inctlInfo.getFullFileName());
		data = new CdrqData();
		data.setData(conn, tempMap);
		data.convertBeanFromMap(tempMap);
		
		checker = new CdrqChecker();
		checker.setData(data);
	}
	
	public List checkDataLine(DataLineInfo lineInfo) throws Exception 
	{
		List descInfos = super.checkDataLine(lineInfo);
		if (descInfos.size() > 0) {
			ErrorDescInfo descInfo = (ErrorDescInfo) descInfos.get(0);
			rCode = descInfo.getErrorCode();
			return log(descInfos);
		}
		descInfos = checker.checker(lineInfo);
		if (descInfos.size() > 0) {
			ErrorDescInfo descInfo = (ErrorDescInfo) descInfos.get(0);
			rCode = descInfo.getErrorCode();
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
        sqlsInfo.setSqls(data.makeSqlList(conn, rCode));
        sqlsInfoList.add(sqlsInfo);
        log.debug("handleDataLine:" + sqlsInfoList);
		return sqlsInfoList;
	}
	
	@Override
	public ExecuteSqlsInfo afterHandleDataLine() throws Exception 
	{
        return null;
    }
	
	/**
	 * get DataLineInfo data
	 * @param lineInfo
	 * @return
	 */
    private Map<String, Object> getValues(DataLineInfo lineInfo)
    {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("REQ_TYPE", (String) lineInfo.getFieldData("field02"));
        values.put("CARD_NO", (String) lineInfo.getFieldData("field03"));
        values.put("EXPIRY_DATE", (String) lineInfo.getFieldData("field04"));
        return values;
    }
	
	private List log(List descInfos) 
	{
		log.warn("checkDataLine:" + descInfos);
		return descInfos;
	}
	
	private void insertInctlErrInfo(DataLineInfo lineInfo, List descInfos) throws Exception {
//        impFileInfo.setFailCnt(impFileInfo.getFailCnt() + 1);
        try {
            TbInctlErrInfo inctlErrInfo = ImpFilesUtil.makeInctlErrInfo(inctlInfo, lineInfo, descInfos);
            inctlErrInfo.setRcode(rCode);
            TbInctlErrMgr mgr = new TbInctlErrMgr(conn);
            mgr.insert(inctlErrInfo);
        }
        catch (Exception ignore) {
            log.warn("insertInctlErrInfo error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
    }

    private void insertInctlErrInfo(DataLineInfo lineInfo, ErrorDescInfo descInfo) throws Exception {
        List descInfos = new ArrayList();
        descInfos.add(descInfo);
        insertInctlErrInfo(lineInfo, descInfos);
    }
	
	private void insertImportDataLogInfo(Connection conn, DataLineInfo lineInfo) throws Exception 
	{
		TbImportDataLogInfo info = new TbImportDataLogInfo();
		info.setMemId(inctlInfo.getMemId());
		info.setMemGroupId(inctlInfo.getMemGroupId());
		info.setFileName(inctlInfo.getFileName());
		info.setFileDate(inctlInfo.getFileDate());
		info.setSeqno(inctlInfo.getSeqno());
		info.setFileType("X");
		info.setLineNo(lineInfo.getLineNo());
		info.setMessage(lineInfo.getPlainLine());
		info.setMessageLen(lineInfo.getMappingInfo().getDataLength());
		info.setFullFileName(inctlInfo.getFullFileName());
		info.setSysDate(DateUtils.getSystemDate());
		info.setSysTime(DateUtils.getSystemTime());
		info.setRcode(rCode);
		new TbImportDataLogMgr(conn).insert(info);
	}
    
	public static ImpCDRQ getInstance() 
	{
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ImpCDRQ instance = (ImpCDRQ) apContext.getBean("ImpCDRQ");
		return instance;
	}
	
	public static void main(String[] args) {
		ImpCDRQ impCDRQ = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impCDRQ = getInstance();
            }
            else {
            	impCDRQ = new ImpCDRQ();
            }
            impCDRQ.setFileName("CDRQ");
            impCDRQ.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpCDRQ run fail:" + ignore.getMessage(), ignore);
        }
	}
}
