/**
 * changelog
 * --------------------
 * 20090606
 * duncan
 * 從相關路徑讀 MappingInfos.xml 時讀不到, 改用 classloader 方式來讀
 * --------------------
 * 20090602
 * duncan,ivan
 * add 2721, 2722 的檢查
 * --------------------
 * 20090522
 * duncan,sonys
 * add fileDate, seqno property
 * --------------------
 * 20090505
 * duncan
 * add usingLike, usingErrorHandling property
 * --------------------
 * 20090204
 * duncan,anny
 * 加上 afterHandleDataLine method 可 overwrite
 * --------------------
 * 20080529
 * duncan
 * 加上處理整個檔案要麻整個成功, 若有一筆失敗就要當做整個檔案失敗, 利用原來的 doCommit 來處理
 * 若要 enable 上述功能, 將 doCommit 設為 false, 預設為 true
 * --------------------
 * 20070907
 * duncan
 * 為了讓修改幅度和變動最小,
 * 作法概念:再產生要匯入的檔案(透過UI fix OK的TB_INCTL_ERR records)放在 temp 目錄, 之後的流程不變
 * 所以到時候基本上 "匯入檔案錯誤處理" 不會有新的程式.
 * 透過 UI fix 的資料基本上會在隔天的匯入檔案中處理.
 * --------------------
 * 20070531
 * duncan
 * 1. add checkEmptyFile property, default is false
 * 2. batch.properties 加上 impfiles.checkemptyfile 設定
 * impfiles.checkemptyfile default is false
 * --------------------
 * 20070529
 * duncan,jim
 * 加上 doCommit property(default is true), for jim
 * --------------------
 * 20070529
 * duncan
 * 使用 batch.properties 上的 impfiles.percommit.records, impfiles.percommit.sleep 設定
 * impfiles.percommit.records default is 1000
 * impfiles.percommit.sleep default is 500
 * --------------------
 * 20070528
 * duncan,tracy
 * 1. 若整檔不處理, 註記 rcode 2999
 * 2. 若部份資料有誤, 註記 rcode 2001, 若全部資料有誤, 註記 rcode 2999
 * 規則:
 * thisRcode, 因為可能一次處理多筆 inctlInfos, 所以整個跑完才註記 thisRcode
 * 若 inctlInfos 中有某一個有發生整檔不處理或 totCnt = failCnt, 註記 thisRcode = '2999'
 * 若有部份資料有誤, 註記 thisRcode = '2001'
 * --------------------
 * 20070411, add checkEmpty, checkDate, validValues setting
 * AbstractImpFile.checkDataLine(DataLineInfo lineInfo) has default rule depends on these setting
 * checkEmpty=[true | false], default is false
 * when checkEmpty=true, check this field value is empty
 * checkDate=[true | false], default is false
 * when checkDate=true, check this field value is valid date when the field value is not empty
 * validValues=[value1,value2,...,valueN], default is empty, separator can be ',', ' '
 * when validValues is not empty, check this field value by these separated values when the field value is not empty
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.framework.ImpErptFileInfo;

import org.apache.log4j.Logger;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbInctlErrInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.mgr.TbInctlErrMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.GenErr2TempDirBean;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.InctlBean;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.MappingInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.MappingLoader;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <pre>
 * AbstractImpFile
 * fileName:String
 * inctlBean:InctlBean
 * fileInfo:TbFileInfoInfo
 * inctlInfos:List
 * mappingInfo:MappingInfo
 * impFileInfo:ImpFileInfo
 * configFilename:String
 * encoding:String
 * inctlInfo:TbInctlInfo
 * recordsPerCommit:int
 * sleepPerCommit:int
 * </pre>
 * author:duncan
 */
public abstract class AbstractImpFile extends AbstractBatchBasic {
    private static Logger log = Logger.getLogger(AbstractImpFile.class);
    protected String fileName = "";
    protected InctlBean inctlBean = null;
    protected TbFileInfoInfo fileInfo = null;
    protected List inctlInfos = new ArrayList();
    protected MappingInfo mappingInfo = null;
    protected ImpFileInfo impFileInfo = null;
    // mapping config filename
    protected String configFilename = "config/batch/MappingInfos.xml";
    // mapping config filename encoding
    protected String encoding = "UTF-8";
    protected TbInctlInfo inctlInfo = null;
    protected int recordsPerCommit = BatchUtil.getRecordsPerCommit();
    // millisecond
    protected int sleepPerCommit = BatchUtil.getSleepPerCommit();
    protected boolean checkOKFile = false;
    protected Connection conn = null;

    // thisRcode, 因為可能一次處理多筆 inctlInfos, 所以整個跑完才註記 thisRcode
    // 若 inctlInfos 中有某一個有發生整檔不處理或 totCnt = failCnt, 註記 thisRcode = '2999'
    // 若有部份資料有誤, 註記 thisRcode = '2001'
    protected String thisRcode = Layer1Constants.RCODE_0000_OK;

    // 是否幫忙做幾筆 commit 的動作
    protected boolean doCommit = true;

    protected boolean checkEmptyFile = BatchUtil.isCheckEmptyFile();
    protected boolean checkHeaderFileName = true;

    protected boolean usingLike = false;
    protected boolean usingErrorHandling = false;

    protected String fileDate = "";
    protected String seqno = "";

    protected AbstractImpFile() {
    }

    public List getHeader() {
        return impFileInfo.getHeader();
    }

    public List getTrailor() {
        return impFileInfo.getTrailor();
    }
    
    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public String getSeqno() {
        return seqno;
    }

    public void setSeqno(String seqno) {
        this.seqno = seqno;
    }

    public boolean isUsingErrorHandling() {
        return usingErrorHandling;
    }

    public void setUsingErrorHandling(boolean usingErrorHandling) {
        this.usingErrorHandling = usingErrorHandling;
    }

    public boolean isUsingLike() {
        return usingLike;
    }

    public void setUsingLike(boolean usingLike) {
        this.usingLike = usingLike;
    }

    public boolean isCheckEmptyFile() {
        return checkEmptyFile;
    }

    public void setCheckEmptyFile(boolean checkEmptyFile) {
        this.checkEmptyFile = checkEmptyFile;
    }

    public boolean isCheckHeaderFileName() {
		return checkHeaderFileName;
	}

	public void setCheckHeaderFileName(boolean checkAcqMemId) {
		this.checkHeaderFileName = checkAcqMemId;
	}

	public boolean isDoCommit() {
        return doCommit;
    }

    public void setDoCommit(boolean doCommit) {
        this.doCommit = doCommit;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InctlBean getInctlBean() {
        return inctlBean;
    }

    public void setInctlBean(InctlBean inctlBean) {
        this.inctlBean = inctlBean;
    }

    public TbFileInfoInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(TbFileInfoInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public List getInctlInfos() {
        return inctlInfos;
    }

    public void setInctlInfos(List inctlInfos) {
        this.inctlInfos = inctlInfos;
    }

    public MappingInfo getMappingInfo() {
        return mappingInfo;
    }

    public void setMappingInfo(MappingInfo mappingInfo) {
        this.mappingInfo = mappingInfo;
    }

    public ImpFileInfo getImpFileInfo() {
        return impFileInfo;
    }

    public void setImpFileInfo(ImpFileInfo impFileInfo) {
        this.impFileInfo = impFileInfo;
    }

    public String getConfigFilename() {
        return configFilename;
    }

    public void setConfigFilename(String configFilename) {
        this.configFilename = configFilename;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public TbInctlInfo getInctlInfo() {
        return inctlInfo;
    }

    public void setInctlInfo(TbInctlInfo inctlInfo) {
        this.inctlInfo = inctlInfo;
    }

    public int getRecordsPerCommit() {
        return recordsPerCommit;
    }

    public void setRecordsPerCommit(int recordsPerCommit) {
        this.recordsPerCommit = recordsPerCommit;
    }

    public int getSleepPerCommit() {
        return sleepPerCommit;
    }

    public void setSleepPerCommit(int sleepPerCommit) {
        this.sleepPerCommit = sleepPerCommit;
    }

    public boolean isCheckOKFile() {
        return checkOKFile;
    }

    public void setCheckOKFile(boolean checkOKFile) {
        this.checkOKFile = checkOKFile;
    }

    // 20070411
    private String[] getValuesUsingST(String value, String delim) {
        StringTokenizer st = new StringTokenizer(value, delim);
        String[] ret = new String[st.countTokens()];
        int idx = 0;
        while (st.hasMoreTokens()) {
            ret[idx] = st.nextToken();
            idx++;
        }
        return ret;
    }

    public abstract ExecuteSqlsInfo beforeHandleDataLine() throws Exception;

    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        List descInfos = new ArrayList();
        // check 2721
        if (mappingInfo.getFields().size() != lineInfo.getFieldDataSize()) {
            String content = "fields not match! " + mappingInfo.getFields().size() + " != " + lineInfo.getFieldDataSize();
            ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2721_FIELDDATASIZE_ERR, null, content);
            descInfos.add(descInfo);
        }
        for (int i = 0; i < lineInfo.getMappingInfo().getFields().size(); i++) {
            FieldInfo fieldInfo = (FieldInfo) lineInfo.getMappingInfo().getFields().get(i);
            // checkEmpty
            if (fieldInfo.isCheckEmpty()) {
                if (FieldInfo.TYPE_STRING.equals(fieldInfo.getType())) {
                    // string
                    String value = (String) lineInfo.getFieldData(fieldInfo.getName());
                    if (StringUtil.isEmpty(value)) {
                        ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2708_MANDATORY_ERR, fieldInfo, "");
                        descInfos.add(descInfo);
                    }
                }
                else if (FieldInfo.TYPE_NUMBER.equals(fieldInfo.getType())) {
                    // number
                    Number value = (Number) lineInfo.getFieldData(fieldInfo.getName());
                    if (value == null) {
                        ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2708_MANDATORY_ERR, fieldInfo, "");
                        descInfos.add(descInfo);
                    }
                }
            }
            // checkDate
            if (fieldInfo.isCheckDate()) {
                if (FieldInfo.TYPE_STRING.equals(fieldInfo.getType())) {
                    // string
                    String value = (String) lineInfo.getFieldData(fieldInfo.getName());
                    if (!StringUtil.isEmpty(value) && !DateUtil.isValidDate(value)) {
                        ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, fieldInfo, value);
                        descInfos.add(descInfo);
                    }
                }
                // number 不檢查
            }
            // validValues
            if (!StringUtil.isEmpty(fieldInfo.getValidValues())) {
                if (FieldInfo.TYPE_STRING.equals(fieldInfo.getType())) {
                    // string
                    String value = (String) lineInfo.getFieldData(fieldInfo.getName());
                    String[] values = getValuesUsingST(fieldInfo.getValidValues(), ", ");
                    if (!StringUtil.isEmpty(value) && !ImpFilesUtil.isInArray(value, values)) {
                        ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2710_INVALID_ERR, fieldInfo, value);
                        descInfos.add(descInfo);
                    }
                }
                // number 不檢查
            }
            if (FieldInfo.TYPE_STRING.equals(fieldInfo.getType()) && fieldInfo.getLength() > 0) {
                // check 2722
                String value = (String) lineInfo.getFieldData(fieldInfo.getName());
                if (value.length() > fieldInfo.getLength()) {
                    ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2722_FIELDLENGTH_ERR, fieldInfo, value);
                    descInfos.add(descInfo);
                }
            }
        }
        return descInfos;
    }

    public abstract List handleDataLine(DataLineInfo lineInfo) throws Exception;

    public ExecuteSqlsInfo afterHandleDataLine() throws Exception {
        return null;
    }

    private void insertInctlErrInfo(DataLineInfo lineInfo, List descInfos) throws Exception {
        impFileInfo.setFailCnt(impFileInfo.getFailCnt() + 1);
        try {
            TbInctlErrInfo inctlErrInfo = ImpFilesUtil.makeInctlErrInfo(inctlInfo, lineInfo, descInfos);
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

    protected void updateInctl(String workFlag, boolean commit) throws SQLException {
        inctlInfo.setTotRec(new Integer(impFileInfo.getTotRec()));
        inctlInfo.setRecCnt(new Integer(impFileInfo.getRecCnt()));
        inctlInfo.setSucCnt(new Integer(impFileInfo.getSucCnt()));
        inctlInfo.setFailCnt(new Integer(impFileInfo.getFailCnt()));
        inctlInfo.setWorkFlag(workFlag);
        inctlBean.updateInctl(conn, commit, inctlInfo);
    }

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
                // checkFile fail, 整檔踢退, 不處理
                updateInctl(Layer1Constants.WORKFLAG_PROCESSFAIL, true);
                thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
                return false;
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
                    List descInfos = checkDataLine(lineInfo);
                    if (descInfos != null && descInfos.size() > 0) {
                        // checkDataLine fail
                        // set failCnt and insert inctlErrInfo
                        if (doCommit) {
                            insertInctlErrInfo(lineInfo, descInfos);
                            continue;
                        }
                        else {
                            // 有錯整個檔案都不處理
                            // 先做 rollback, 再 TB_INCTL_ERR 記錄那一筆出錯, 並跳開
                            conn.rollback();
                            insertInctlErrInfo(lineInfo, descInfos);
                            break;
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
                    // run here, handle one lineInfo OK
                    impFileInfo.setSucCnt(impFileInfo.getSucCnt() + 1);
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
            }
            // do last commit
            // updateInctl and do commit
            if (doCommit) {
                updateInctl(Layer1Constants.WORKFLAG_PROCESSOK, false);
            }
            else {
                if (impFileInfo.getFailCnt() > 0) {
                    updateInctl(Layer1Constants.WORKFLAG_PROCESSFAIL, false);
                }
                else {
                    updateInctl(Layer1Constants.WORKFLAG_PROCESSOK, false);
                }
            }
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
            if (impFileInfo.getTotRec() == impFileInfo.getFailCnt()) {
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

    public void process(String[] args) throws Exception {
        // impfiles -> linkControl = "I"
        setLinkControl("I");
        // mappingInfo check
        MappingLoader ml = new MappingLoader();
        ml.setConfigFilename(configFilename);
        ml.setFile(new File(configFilename));
        ml.setEncoding(encoding);
        ml.startLoading();
        mappingInfo = ml.getMappingInfo(fileName);
        if (mappingInfo == null) {
            throw new Exception("mappingInfo(" + fileName + ") is null!");
        }

        // fileInfo check
        fileInfo = ImpFilesUtil.getFileInfoIn(fileName);
        // no fileInfo
        if (fileInfo == null) {
            throw new Exception("fileInfo(" + fileName + ") is null!");
        }

        // inctlInfos check
        inctlInfos = ImpFilesUtil.getInctlInfosInWorkAndProcessing(fileName, fileDate, seqno, usingLike, getBatchResultInfo());
        if (inctlInfos == null) {
            throw new Exception("inctlInfos(" + fileName + ") is null!");
        }
        else if (inctlInfos.size() == 0) {
            log.warn("inctlInfos(" + fileName + ") is empty!");
        }

        // loop each inctlInfo in inctlInfos
        for (int i = 0; i < inctlInfos.size(); i++) {
            try {
                // reset inctlBean
                inctlBean = new InctlBean();
                inctlInfo = (TbInctlInfo) inctlInfos.get(i);
                inctlBean.setRelated(true);
                inctlBean.setBatchResultInfo(getBatchResultInfo());
                // set impFileInfo
                impFileInfo = new ImpFileInfo();
                impFileInfo.setFileInfo(fileInfo);
                impFileInfo.setMappingInfo(mappingInfo);
                impFileInfo.setInctlInfo(inctlInfo);
                impFileInfo.setCheckOKFile(checkOKFile);
                impFileInfo.setCheckEmptyFile(checkEmptyFile);
                impFileInfo.setCheckHeaderFileName(checkHeaderFileName);
                // handle impFileInfo
                handleImpFileInfo();
            }
            catch (Exception ignore) {
                thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
                log.warn("handleImpFileInfo error:" + ignore.getMessage(), ignore);
            }
            finally {
                impFileInfo.closeFile();
            }
        }

        /*20170413 因遠鑫無對應欄位也暫時相關需求，先行註記不使用
        if (usingErrorHandling) {
            // 加上錯誤要補處理的 code
            GenErr2TempDirBean errBean = null;
            try {
                // 因為 conn 是在開始處理一個檔案的時候才給
                conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
                errBean = new GenErr2TempDirBean();
                String batchDate = DateUtil.getTodayString().substring(0, 8);
                errBean.setBatchDate(batchDate);
                errBean.setConnection(conn);
                errBean.setFileInfo(fileInfo);
                errBean.setMappingInfo(mappingInfo);
                errBean.genErr2TempDir();
            }
            catch (Exception ignore) {
                log.warn("handle GenErr2TempDirBean.genErr2TempDir error:" + ignore.getMessage(), ignore);
            }
            finally {
                ReleaseResource.releaseDB(conn);
            }
        }*/

        // set rcode to TB_BATCH_RESULT
        setRcode(thisRcode);
    }
}
