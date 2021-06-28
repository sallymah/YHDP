/**
 * changelog
 * --------------------
 * 20090310
 * duncan
 * 多考慮分隔符號的檔案的相關處理
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.framework.impfiles;

import org.apache.log4j.Logger;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbInctlErrInfo;
import tw.com.hyweb.service.db.mgr.TbInctlErrMgr;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.filesin.FilesIn;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsUtil;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.number.NumberUtil;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

/**
 * <pre>
 * GenErr2TempDirBean
 * </pre>
 * author:duncan
 */
public class GenErr2TempDirBean {
    private static Logger log = Logger.getLogger(GenErr2TempDirBean.class);
    protected String batchDate = "";
    protected String tempDir = "";
    protected TbFileInfoInfo fileInfo = null;
    protected MappingInfo mappingInfo = null;
    protected Connection conn = null;
    protected boolean errHandle = false;
    protected int err2TempDirCount = 0;
    protected int memIdType = 0;
    protected static final int MEMIDTYPE_NO = 0;
    protected static final int MEMIDTYPE_INLOCALPATH = 1;
    protected static final int MEMIDTYPE_INFILENAME = 2;
    protected static final String SPECIAL_ALLMEMID = "00000000";
    protected String[] patterns = null;
    protected int maxSeqno = 91;

    protected static final String PROPERTY_STATUS = "status";
    protected static final String PROPERTY_UIPROCDATE = "uiProcDate";
    protected static final String PROPERTY_UIMESSAGE = "uiMessage";

    protected static final String STATUS_INITIAL = "0";
    protected static final String STATUS_UIOK = "1";
    protected static final String STATUS_BATCHOK = "2";

    protected static final String FIX_CONDITION = " AND STATUS = '" + STATUS_UIOK + "' AND UI_PROC_DATE IS NOT NULL AND UI_MESSAGE IS NOT NULL AND LINE_NO != '0'";

    //protected String okFilePending = ".OK";

    public GenErr2TempDirBean() {
    }

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }

    public Connection getConnection() {
        return conn;
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public TbFileInfoInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(TbFileInfoInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public MappingInfo getMappingInfo() {
        return mappingInfo;
    }

    public void setMappingInfo(MappingInfo mappingInfo) {
        this.mappingInfo = mappingInfo;
    }

    protected void initial() {
        try {
            tempDir = BatchUtil.getTempDirectory();
            if (StringUtil.isEmpty(tempDir)) {
                throw new Exception("no tempDir setting!");
            }
            tempDir = normalFileSeparator(tempDir);
        }
        catch (Exception ignore) {
            errHandle = false;
            log.warn("get tempDir error:" + ignore.getMessage(), ignore);
        }

        try {
            ApplicationContext apContext = new FileSystemXmlApplicationContext(
                    "config/batch/FilesIn/spring.xml");
            FilesIn filesIn = (FilesIn) apContext.getBean("filesIn");
            //okFilePending = filesIn.getOkFilePending();
        }
        catch (Exception ignore) {
            log.warn("get filesIn error:" + ignore.getMessage(), ignore);
        }

        // 檢查TB_INCTL_ERR是否有STATUS,UI_PROC_DATE,UI_MESSAGE欄位
        try {
            TbInctlErrInfo info = new TbInctlErrInfo();
            BeanUtils.getProperty(info, PROPERTY_STATUS);
            BeanUtils.getProperty(info, PROPERTY_UIPROCDATE);
            BeanUtils.getProperty(info, PROPERTY_UIMESSAGE);
            errHandle = true;
        }
        catch (Exception ignore) {
            errHandle = false;
            log.info("no status, uiProcDate, uiMessage properties!");
        }
    }

    protected int getErr2TempDirCount() {
        int count = 0;
        try {
            TbInctlErrMgr mgr = new TbInctlErrMgr(conn);
            StringBuffer where = new StringBuffer();
            where.append("FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileInfo.getFileName()));
            where.append(FIX_CONDITION);
            count = mgr.getCount(where.toString());
        }
        catch (Exception ignore) {
            count = 0;
            log.warn("getErr2TempDirCount error:" + ignore.getMessage(), ignore);
        }
        return count;
    }

    public int determineMemIdType() {
        int type = MEMIDTYPE_NO;
        patterns = fileInfo.getFileNamePattern().split("\\.");
        if (fileInfo.getLocalPath().indexOf(SPECIAL_ALLMEMID) != -1) {
            type = MEMIDTYPE_INLOCALPATH;
        }
        else if (patterns.length > 2) {
            type = MEMIDTYPE_INFILENAME;
        }
        else {
            type = MEMIDTYPE_NO;
        }
        return type;
    }

    protected int getMaxSeqNo() {
        int maxSeqno = 0;
        String sql = "SELECT TO_NUMBER(MAX(SEQNO)) + 1 FROM TB_INCTL WHERE FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileInfo.getFileName()) +
                " AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                maxSeqno = rs.getInt(1);
            }
        }
        catch (Exception ignore) {
            maxSeqno = 91;
            log.warn("getMaxSeqNo error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return maxSeqno;
    }

    public void genErr2TempDir() throws Exception {
        initial();
        if (!errHandle) {
            return;
        }
        // 取得要產生檔案的筆數
        err2TempDirCount = getErr2TempDirCount();
        if (err2TempDirCount == 0) {
            log.info("err2TempDirCount = 0! no genErr2TempDir process!");
            return;
        }
        // 決定是否有區分 memId
        // 0:沒有區分 memId
        // 1:有區分 memId, memId 在 localPath
        // 2:有區分 memId, memId 在 fileName
        memIdType = determineMemIdType();
        int tmaxSeqno = getMaxSeqNo();
        if (tmaxSeqno > maxSeqno) {
            maxSeqno = tmaxSeqno;
        }
        if (memIdType == MEMIDTYPE_NO) {
            genErr2TempDir0();
        }
        else if (memIdType == MEMIDTYPE_INLOCALPATH || memIdType == MEMIDTYPE_INFILENAME) {
            genErr2TempDir12();
        }
    }

    protected static String normalFileSeparator(String fn) {
        return fn.replace('\\', '/');
    }

    protected String getFullName(String memId) {
        String fullName = tempDir;
        if (!fullName.endsWith("/")) {
            fullName += "/";
        }
        String tmp = normalFileSeparator(fileInfo.getLocalPath());
        if (memIdType == MEMIDTYPE_INLOCALPATH && !StringUtil.isEmpty(memId)) {
            tmp = tmp.replaceAll(SPECIAL_ALLMEMID, memId);
        }
        fullName += tmp;
        if (!fullName.endsWith("/")) {
            fullName += "/";
        }
        new File(fullName).mkdirs();
        fullName += patterns[0];
        if (patterns.length <= 2 && (memIdType == MEMIDTYPE_NO || memIdType == MEMIDTYPE_INLOCALPATH)) {
            fullName += "." + batchDate + StringUtil.pendingKey(maxSeqno, 2);
        }
        else if (patterns.length > 2 && memIdType == MEMIDTYPE_INFILENAME) {
            if ("\\d{10}".equals(patterns[1])) {
                // YYYYMMDDSS在中間
                fullName += "." + batchDate + StringUtil.pendingKey(maxSeqno, 2);
                fullName += "." + memId;
            }
            else if ("\\d{10}".equals(patterns[2])) {
                // YYYYMMDDSS在最後
                fullName += "." + memId;
                fullName += "." + batchDate + StringUtil.pendingKey(maxSeqno, 2);
            }
        }
        fullName = FilenameUtils.normalize(fullName);
        return fullName;
    }

    protected String makeUpdateInctlErrSQL(String memId, String fileName, String fileDate, String seqno, Number lineNo) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE TB_INCTL_ERR SET STATUS = " + StringUtil.toSqlValueWithSQuote(STATUS_INITIAL));
        sql.append(", UI_PROC_DATE = NULL");
        sql.append(" WHERE ");
        sql.append("MEM_ID = " + StringUtil.toSqlValueWithSQuote(memId));
        sql.append(" AND ");
        sql.append("FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileName));
        sql.append(" AND ");
        sql.append("FILE_DATE = " + StringUtil.toSqlValueWithSQuote(fileDate));
        sql.append(" AND ");
        sql.append("SEQNO = " + StringUtil.toSqlValueWithSQuote(seqno));
        sql.append(" AND ");
        sql.append("LINE_NO = " + NumberUtil.toSqlValue(lineNo));
        return sql.toString();
    }

    protected void genErr2TempDir(String where, String fullName) throws Exception {
        // 先使用 $fullName.tmp, 因為怕抓出來的資料長度不對的話要濾掉
        // 所以最後才寫 header
        String tempFullName = fullName + ".tmp";
        File tempf = new File(tempFullName);
        FileOutputStream fos = null;
        Statement stmt = null;
        ResultSet rs = null;
        // 存放要UPDATE TB_INCTL_ERR的sqls
        // 預設會把所有要補處理的status update 2
        // 但若在產生檔案的過程中有問題將會把這筆update UI 未修改
        ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(true);
        sqlsInfo.setSavepoint(true);
        // 所有要補處理的status update 2
        sqlsInfo.addSql("UPDATE TB_INCTL_ERR SET STATUS = " + StringUtil.toSqlValueWithSQuote(STATUS_BATCHOK) + " WHERE " + where);
        String selectSQL = "SELECT MEM_ID, FILE_NAME, FILE_DATE, SEQNO, LINE_NO, UI_MESSAGE FROM TB_INCTL_ERR WHERE " + where +
                " ORDER BY FILE_DATE, SEQNO, LINE_NO";
        int okCount = 0;
        int failCount = 0;
        boolean writeTmpOK = false;
        try {
            fos = new FileOutputStream(tempf);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);
            while (rs.next()) {
                String memId = rs.getString(1);
                String fileName = rs.getString(2);
                String fileDate = rs.getString(3);
                String seqno = rs.getString(4);
                Number lineNo = rs.getBigDecimal(5);
                String logUiMessage = rs.getString(6);
                String uiMessage = logUiMessage + mappingInfo.getLineSep();
                byte[] b = uiMessage.getBytes(mappingInfo.getEncoding());
                if (b.length != mappingInfo.getDataLength()) {
                    failCount++;
                    log.info("b.length(" + b.length + ") != mappingInfo.getDataLength(" + mappingInfo.getDataLength() + ")!");
                    log.info("uiMessage:" + logUiMessage);
                    String updateSQL = makeUpdateInctlErrSQL(memId, fileName, fileDate, seqno, lineNo);
                    sqlsInfo.addSql(updateSQL);
                }
                else {
                    okCount++;
                    fos.write(b);
                }
            }
            writeTmpOK = true;
        }
        finally {
            ReleaseResource.releaseIO(fos);
            if (!writeTmpOK && tempf.exists()) {
                tempf.deleteOnExit();
            }
            ReleaseResource.releaseDB(null, stmt, rs);
        }

        // in here, tmp gen OK
        // 產生真正檔案,並填入正確的header
        String header = "";
        String trailor = "";
        if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
            header = "H0" + batchDate + StringUtil.pendingKey(okCount, 8);
            header = StringUtils.rightPad(header, mappingInfo.getDataLength() - mappingInfo.getLineLength(), mappingInfo.getIgnoreChar());
            header += mappingInfo.getLineSep();
            trailor = "trailorLine" + mappingInfo.getLineSep();
        }
        else {
            // 分隔符號的檔案
            header = "headerLine" + mappingInfo.getLineSep();
            trailor = "trailorLine" + mappingInfo.getLineSep();
        }
        File f = new File(fullName);
        FileInputStream fis = null;
        fos = null;
        FileOutputStream fosOK = null;
        
        File okf = null;
        String subFileName = "";
        String fileOk = "";
        if(fileInfo.getSubFileName().contains("/"))
        {
            subFileName = fileInfo.getSubFileName().replaceAll("/", "");
            fileOk = (new StringBuilder(String.valueOf(fullName.substring(0, fullName.lastIndexOf("."))))).append(subFileName).toString();
        } else
        {
            subFileName = fileInfo.getSubFileName();
            fileOk = (new StringBuilder(String.valueOf(f.getAbsolutePath()))).append(subFileName).toString();
        }
        okf = new File(fileOk);

        boolean writeOK = false;
        try {
            fis = new FileInputStream(tempf);
            fos = new FileOutputStream(f);
            if (mappingInfo.getHeader().isHasHeader() && !mappingInfo.getHeader().isHasCustHeader()) {
                fos.write(header.getBytes(mappingInfo.getEncoding()));
            }
            else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
                for (int i = 0; i < mappingInfo.getHeader().getNumLines(); i++) {
                    fos.write(header.getBytes(mappingInfo.getEncoding()));
                }
            }
            IOUtils.copy(fis, fos);
            if (mappingInfo.getTrailor().isHasTrailor()) {
                for (int i = 0; i < mappingInfo.getTrailor().getNumLines(); i++) {
                    fos.write(trailor.getBytes(mappingInfo.getEncoding()));
                }
            }
            if (Layer1Constants.OKFLAG_CHECK.equals(fileInfo.getOkFlag())) {
                fosOK = new FileOutputStream(okf);
            }
            writeOK = true;
        }
        finally {
            if (!writeOK && tempf.exists()) {
                tempf.deleteOnExit();
            }
            if (!writeOK && f.exists()) {
                f.deleteOnExit();
            }
            if (!writeOK && okf.exists()) {
                okf.deleteOnExit();
            }
            ReleaseResource.releaseIO(fis);
            ReleaseResource.releaseIO(fos);
            ReleaseResource.releaseIO(fosOK);
        }

        // delete tmp file
        if (tempf.exists()) {
            tempf.deleteOnExit();
        }
        try {
            log.info("sqlsInfo:" + sqlsInfo);
            ExecuteSqlsUtil.executeSqls(conn, sqlsInfo);
        }
        catch (Exception ignore) {
            // delete 產生的檔案
            if (f.exists()) {
                f.deleteOnExit();
            }
            if (okf.exists()) {
                okf.deleteOnExit();
            }
            throw ignore;
        }
    }

    public void genErr2TempDir0() throws Exception {
        String where = "FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileInfo.getFileName()) + FIX_CONDITION;
        String fullName = getFullName("");
        genErr2TempDir(where, fullName);
    }

    public void genErr2TempDir12() throws Exception {
        Statement stmt = null;
        ResultSet rs = null;
        String selectSQL = "SELECT MEM_ID, COUNT(*) AS TOTALCOUNT FROM TB_INCTL_ERR WHERE FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileInfo.getFileName()) +
                FIX_CONDITION + " GROUP BY MEM_ID ORDER BY TOTALCOUNT";
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectSQL);
            while (rs.next()) {
                String memId = rs.getString(1);
                Number totalCount = rs.getBigDecimal(2);
                if (totalCount.intValue() == 0) {
                    continue;
                }
                String where = "FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileInfo.getFileName()) +
                        "MEM_ID = " + StringUtil.toSqlValueWithSQuote(memId) + FIX_CONDITION;
                String fullName = getFullName(memId);
                genErr2TempDir(where, fullName);
            }
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
    }
}
