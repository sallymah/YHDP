/**
 * changelog
 * --------------------
 * 20090310
 * duncan
 * TB_FILE_INFO.DATA_LEN 可能為 0(例:分隔符號的檔案)
 * --------------------
 * 20081202
 * duncan,jesse
 * 加上能只針對某一個 fileName 來處理
 * --------------------
 * 20081201
 * duncan,anny,judy
 * 若 TB_FILE_INFO 有設依目錄分 MEM_ID, 檔名上又有依 MEM_ID 劃分, 要檢查此二個 MEM_ID 是否一致,
 * 一致檔案才能進來, 若不一致, 檔案不能進來 
 * --------------------
 * 20081114
 * duncan
 * bug fix, 把檢查 seqnoStart, seqnoEnd 拿掉
 * --------------------
 * 20081105
 * duncan
 * 增加若沒給 seqnoStart, seqnoEnd 時的處理
 * --------------------
 * 20080606
 * duncan
 * handle fileDate is YYMMDD case -> convert to YYYYMMDD
 * --------------------
 * 20080529
 * duncan
 * rename PROC_DATE, PROC_TIME to SYS_DATE, SYS_TIME
 * --------------------
 * 20070308
 * tracy, duncan, jesse
 * add okFilePending property, 決定 OK file 檔名要如何 pending
 * --------------------
 * 20070307, 與 tracy 討論結論如下:
 * 1. remove checkFileSize, always check size with TB_FILE_INFO.DATA_LEN, if no check, please set TB_FILE_INFO.DATA_LEN = 1
 * 2. support (renameAfterCopy, deleteAfterCopy) = (false, false), 在 insert TB_INCTL 時, 要 check 是否存在, 存在不 insert, 不存在, insert
 * 3. 會依 (renameAfterCopy, deleteAfterCopy) 的設定來處理 OK file
 * 4. 要加 fileTypes 的設定, 沒設, 都處理, 有設, 只處理有設的 fileTypes (tracy 建議)
 * --------------------
 */
package tw.com.hyweb.core.yhdp.batch.framework.filesin;

import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.yhdp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.yhdp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbInctlMgr;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.File;

import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * <pre>
 * FilesIn javabean
 * okFilePending:String
 * renameAfterCopy:boolean
 * renamePending:String
 * deleteAfterCopy:boolean
 * filenameBeans:List object, each element is FilenameBean object
 * fileTypes:List object, each element is String object
 * 一般處理流程:
 * $temp dir to $work dir
 * for each fileInfo in fileInfos
 * 1. get filenameBean for parse fileName, memId, fileDate, sno
 *   for each file follow regular expression (TB_FILE_INFO.FILE_NAME_PATTERN)
 *     if (check OK file by TB_FILE_INFO.OK_FLAG = true)
 *       copy file from $temp dir to $work dir
 *       write TB_INCTL table by fileInfo, filenameBean
 *       delete file in $temp dir by deleteAfterCopy
 * 2. 若 TB_FILE_INFO.LOCAL_PATH 找到 '00000000' pattern, memId 由檔案實際位置來決定
 * 3. 若 TB_FILE_INFO.LOCAL_PATH 沒找到 '00000000' pattern, memId 由 filenameBean 設定決定
 * 4. 若 TB_FILE_INFO.LOCAL_PATH 沒找到 '00000000' pattern, filenameBean 也沒設定, 用 default centerId(99999999)
 * </pre>
 * author: duncan
 */
public class FilesIn extends AbstractBatchBasic {
    private static Logger log = Logger.getLogger(FilesIn.class);
    protected final String SPECIAL_ALLMEMID = "00000000";
    protected final String DEFAULT_MEMID = "99999999";

    protected String batchDate = "";
    //protected String okFilePending = ".OK";
    protected String renamePending = ".MOVED";
    // checkFileSize: 是否要加檢查檔案大小是否整除 TB_FILE_INFO.DATA_LEN
    protected boolean checkFileSize = true;
    // renameAfterCopy: 檔案從 temp 目錄 copy 到 work 目錄成功後, temp 目錄中的檔案是否要做 rename 的動作
    protected boolean renameAfterCopy = true;
    // deleteAfterCopy: 檔案從 temp 目錄 copy 到 work 目錄成功後, temp 目錄中的檔案是否要做刪除的動作
    protected boolean deleteAfterCopy = false;
    // 存放如何 parse memId, fileDate, sno 的規則, 每個 TB_FILE_INFO.FILE_NAME 應該都有相對應的一筆設定
    // each object is FilenameBean object
    protected List filenameBeans = new ArrayList();

    // 要加 fileTypes 的設定, 沒設, 都處理, 有設, 只處理有設的 fileTypes
    protected List fileTypes = new ArrayList();

    // 所有 TB_FILE_INFO.IN_OUT = 'I' 的資料
    // each object is TbFileInfoInfo object
    protected Vector fileInfos = null;
    protected Connection conn = null;

    protected String tempDir = "";
    protected String workDir = "";

    protected String sysDate = DateUtil.getTodayString().substring(0, 8);
    protected String sysTime = DateUtil.getTodayString().substring(8, 14);
    // for current run
    protected FilenameBean filenameBean = null;
    protected TbFileInfoInfo fileInfo = null;
    // 記錄 LOCAL_PATH 是否有 "00000000", 有的話記錄從前面算起第幾個, 沒有的話記錄 -1
    // ex: in/CASE1/00000000/, pos = 2
    // ex: in/00000000/CASE2/, pos = 1
    protected int pos = -1;
    // 若 LOCAL_PATH 有包含 "00000000", 記錄 "00000000" 之前的字串
    // ex: in/CASE1/00000000/, prefix = in/CASE1
    // ex: in/00000000/CASE2/, prefix = in
    protected String prefix = "";
    // match regular expression files
    protected List matchFiles = null;

    public FilesIn()
    {
    }

    public List getFileTypes()
    {
        return fileTypes;
    }

    public void setFileTypes(List fileTypes)
    {
        this.fileTypes = fileTypes;
    }

    public String getRenamePending()
    {
        return renamePending;
    }

    public void setRenamePending(String renamePending)
    {
        if (StringUtil.isEmpty(renamePending)) {
            return;
        }
        this.renamePending = renamePending;
    }

    public String getBatchDate()
    {
        return batchDate;
    }

    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

    public boolean isRenameAfterCopy()
    {
        return renameAfterCopy;
    }

    public void setRenameAfterCopy(boolean renameAfterCopy)
    {
        this.renameAfterCopy = renameAfterCopy;
    }

    public boolean isDeleteAfterCopy()
    {
        return deleteAfterCopy;
    }

    public void setDeleteAfterCopy(boolean deleteAfterCopy)
    {
        this.deleteAfterCopy = deleteAfterCopy;
    }

    public List getFilenameBeans()
    {
        return filenameBeans;
    }

    public void setFilenameBeans(List filenameBeans)
    {
        this.filenameBeans = filenameBeans;
    }

    public void addFilenameBean(FilenameBean filenameBean) {
        if (filenameBean != null) {
            this.filenameBeans.add(filenameBean);
        }
    }

    protected static String normalFileSeparator(String fn) {
        return fn.replace('\\', '/');
    }

    protected void filterFileInfos(Vector allFileInfos) {
        if (fileTypes == null || fileTypes.size() == 0) {
            // no filter
            fileInfos = allFileInfos;
        }
        else {
            // filter allFileInfos
            fileInfos = new Vector();
            for (int i = 0; i < allFileInfos.size(); i++)
            {
                TbFileInfoInfo fileInfo = (TbFileInfoInfo) allFileInfos.get(i);
                // 符合 fileTypes 設定才加入要處理
                boolean isAdd = false;
                for (int j = 0; j < fileTypes.size(); j++)
                {
                    String fileType = (String) fileTypes.get(j);
                    if (fileType.equals(fileInfo.getFileType())) {
                        isAdd = true;
                        fileInfos.add(fileInfo);
                        break;
                    }
                }
                if (!isAdd) {
                    log.info("fileInfo for '" + fileInfo.getFileName() + "' not in " + fileTypes + "! no handle this fileInfo!");
                }
            }
        }
    }

    public void initial() throws Exception {
        String fileName = System.getProperty("fileName");
        try {
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            TbFileInfoInfo qinfo = new TbFileInfoInfo();
            qinfo.toEmpty();
            qinfo.setInOut(Layer1Constants.INOUT_IN);
            if (!StringUtil.isEmpty(fileName)) {
                log.info("input fileName:" + fileName);
                qinfo.setFileName(fileName);
            }
            Vector allFileInfos = new Vector();
            TbFileInfoMgr mgr = new TbFileInfoMgr(conn);
            mgr.queryMultiple(qinfo, allFileInfos);
            // 要加 fileTypes 的設定, 沒設, 都處理, 有設, 只處理有設的 fileTypes (tracy 建議)
            filterFileInfos(allFileInfos);
            tempDir = BatchUtil.getTempDirectory();
            if (StringUtil.isEmpty(tempDir)) {
                throw new Exception("no tempDir setting!");
            }
            tempDir = normalFileSeparator(tempDir);
            workDir = BatchUtil.getWorkDirectory();
            if (StringUtil.isEmpty(workDir)) {
                throw new Exception("no workDir setting!");
            }
            workDir = normalFileSeparator(workDir);
            // support (renameAfterCopy, deleteAfterCopy) = (false, false)
//            if (!renameAfterCopy && !deleteAfterCopy) {
//                // 二選一, (renameAfterCopy, deleteAfterCopy) = (false, false) -> (true, false)
//                log.warn("(renameAfterCopy, deleteAfterCopy) = (false, false) -> (true, false)!");
//                renameAfterCopy = true;
//                deleteAfterCopy = false;
//            }
            if (renameAfterCopy && deleteAfterCopy) {
                // 二選一, (renameAfterCopy, deleteAfterCopy) = (true, true) -> (false, true)
                renameAfterCopy = false;
                deleteAfterCopy = true;
            }
        }
        catch (Exception ignore) {
            log.warn("initial error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
    }

    protected void setFilenameBean() {
        filenameBean = null;
        for (int i = 0; i < filenameBeans.size(); i++)
        {
            FilenameBean bean = (FilenameBean) filenameBeans.get(i);
            if (fileInfo.getFileName().equals(bean.getFileName())) {
                filenameBean = bean;
                break;
            }
        }
    }

    protected void computePos() {
        // 記錄 LOCAL_PATH 是否有 "00000000", 有的話記錄從前面算起第幾個, 沒有的話記錄 -1
        // ex: in/CASE1/00000000/, pos = 2
        // ex: in/00000000/CASE2/, pos = 1
        // 若 LOCAL_PATH 有包含 "00000000", 記錄 "00000000" 之前的字串
        // ex: in/CASE1/00000000/, prefix = in/CASE1
        // ex: in/00000000/CASE2/, prefix = in
        pos = -1;
        prefix = "";
        String tmp = normalFileSeparator(fileInfo.getLocalPath());
        String[] tokens = tmp.split("/");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals(SPECIAL_ALLMEMID)) {
                pos = i;
                break;
            }
            if (i > 0) {
                prefix += '/';
            }
            prefix += tokens[i];
        }
        if (pos == -1) {
            // 找不到 "00000000", reset prefix
            prefix = "";
        }
    }

    protected boolean isMatchFile(String fn) {
        boolean ret = false;
        Pattern p = Pattern.compile(fileInfo.getFileNamePattern());
        Matcher m = p.matcher(fn);
        ret = m.matches();
        log.info("fn:" + fn + " matched:" + ret);
        return ret;
    }

    private static long MAX_SIZE = 10000000000L;
    protected boolean isSizeOK(File f) {
        if (f.length() >= MAX_SIZE) {
            log.warn("file '" + f.getAbsolutePath() + "' size '" + f.length() + "' too large! ignore!");
            return false;
        }
        if (fileInfo.getDataLen().intValue() != 0) {
            if ((f.length() % fileInfo.getDataLen().intValue()) == 0) {
                return true;
            }
            else {
                log.warn("f.length() % fileInfo.getDataLen().intValue() != 0! ignore!");
                return false;
            }
        }
        else {
            return true;
        }
    }

    protected boolean hasOKFile(File f) {
        if (Layer1Constants.OKFLAG_NOCHECK.equals(fileInfo.getOkFlag())) {
            // 不用檢查 OK file
            return true;
        }
        else if (Layer1Constants.OKFLAG_CHECK.equals(fileInfo.getOkFlag())) {
            // 要檢查 OK file
        	String subFileName = "";
        	String file = "";
        	if (fileInfo.getSubFileName().contains("/")){
        		subFileName = fileInfo.getSubFileName().replaceAll("/", "");
        		file = f.getAbsolutePath().substring(0,f.getAbsolutePath().lastIndexOf(".")) + subFileName;
        	}
        	else{
        		subFileName = fileInfo.getSubFileName();
        		file = f.getAbsolutePath() + subFileName;
        	}
            File ok = new File(file);
            if (ok.isFile() && ok.exists()) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            // unknown, suppose 不用檢查 OK file
            log.warn("unknown OK_FLAG(" + fileInfo.getOkFlag() + ")! suppose no check OK file!");
            return true;
        }
    }

    protected void setMatchFiles() {
        log.info("fileInfo:" + fileInfo);
        log.info("pos:" + pos + " prefix:" + prefix);
        matchFiles = new ArrayList();
        if (pos != -1 && !"".equals(prefix)) {
            log.info("contain memId!");
            // 有 00000000, 先抓第一層的目錄, 再依每一層的目錄抓符合的檔案
            int idx = fileInfo.getLocalPath().indexOf(SPECIAL_ALLMEMID);
            String postfix = fileInfo.getLocalPath().substring(idx + 9);
            String sdir = FilenameUtils.normalizeNoEndSeparator(tempDir + '/' + prefix);
            File dir = new File(sdir);
            File[] files = dir.listFiles();
            // avoid NullPointException
            if (files != null) {
                log.info("files.length:" + files.length);
                for (int i = 0; i < files.length; i++)
                {
                    log.info("files[i]:" + files[i].getAbsolutePath());
                    log.info("files[i].getPath:" + files[i].getPath());
                    if (files[i].isDirectory()) {
                        File dir2 = new File(files[i], postfix);
                        File[] files2 = dir2.listFiles();
                        for (int j = 0; j < files2.length; j++)
                        {
                            log.info("files2[j]:" + files2[j].getAbsolutePath());
                            if (files2[j].isFile() && isMatchFile(files2[j].getName())) {
                                if (hasOKFile(files2[j])) {
                                    if (checkFileSize && isSizeOK(files2[j])) {
                                        matchFiles.add(files2[j]);
                                    }
                                    else {
                                        matchFiles.add(files2[j]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                log.info("files is null!");
            }
        }
        else {
            log.info("contain no memId!");
            // 沒有 00000000, 抓此層符合的檔案
            String sdir2 = FilenameUtils.normalizeNoEndSeparator(tempDir + '/' + fileInfo.getLocalPath());
            File dir2 = new File(sdir2);
            File[] files2 = dir2.listFiles();
            // avoid NullPointException
            if (files2 != null) {
                log.info("files2.length:" + files2.length);
                for (int j = 0; j < files2.length; j++)
                {
                    log.info("files2[j]:" + files2[j].getAbsolutePath());
                    if (files2[j].isFile() && isMatchFile(files2[j].getName())) {
                        if (hasOKFile(files2[j])) {
                            if (checkFileSize && isSizeOK(files2[j])) {
                                matchFiles.add(files2[j]);
                            }
                            else {
                                matchFiles.add(files2[j]);
                            }
                        }
                    }
                }
            }
            else {
                log.info("files2 is null!");
            }
        }
    }

    protected File getDestFile(File matchFile) {
        String srcFullName = normalFileSeparator(matchFile.getAbsolutePath());
        String destFullName = srcFullName.replaceAll(tempDir, workDir);
        return new File(destFullName);
    }

    protected String determineMemId(File matchFile) {
        String memId = DEFAULT_MEMID;
        if (filenameBean.getMemIdStart() != -1 && filenameBean.getMemIdEnd() != -1) {
            // 有自己設定取 memIdStart, memIdEnd
            if (pos != -1) {
                // 有找到 00000000, 又有自己設定取 memIdStart, memIdEnd, 使用 00000000 方式決定
                //String temp = normalFileSeparator(matchFile.getAbsolutePath());
                String temp = normalFileSeparator(matchFile.getPath());
                temp = temp.replaceAll(tempDir, "");
                String[] tokens = temp.split("/");
                memId = tokens[pos];
            }
            else {
                // 沒有找到 00000000, 使用自己設定取 memIdStart, memIdEnd
                memId = matchFile.getName().substring(filenameBean.getMemIdStart(), filenameBean.getMemIdEnd());
            }
        }
        else if (filenameBean.getMemIdStart() == -1 && filenameBean.getMemIdEnd() == -1) {
            // 沒有自己設定取 memIdStart, memIdEnd
            if (pos != -1) {
                // 有找到 00000000, 又沒有自己設定取 memIdStart, memIdEnd, 使用 00000000 方式決定
                String temp = normalFileSeparator(matchFile.getAbsolutePath());
                temp = temp.replaceAll(tempDir, "");
                String[] tokens = temp.split("/");
                memId = tokens[pos];
            }
            else {
                // 沒有找到 00000000, 使用 99999999
                memId = DEFAULT_MEMID;
            }
        }
        return memId;
    }

    protected String getNewSeqno(String memId, String fileName, String fileDate) {
        String sql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_INCTL WHERE MEM_ID = "
            + StringUtil.toSqlValueWithSQuote(memId) + " AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileName) + " AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(fileDate);
        String seqno = "01";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                seqno = rs.getString(1);
            }
        }
        catch (Exception ignore) {
            log.warn("sql:" + sql);
            log.warn("getNewSeqno error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return seqno;
    }

    protected TbInctlInfo getInctlInfo(File matchFile, String memId) {
        TbInctlInfo inctlInfo = new TbInctlInfo();
        inctlInfo.setMemId(memId);
        inctlInfo.setFileName(fileInfo.getFileName());
        String fileDate = matchFile.getName().substring(filenameBean.getFileDateStart(), filenameBean.getFileDateEnd());
        if (fileDate.length() == 6) {
            String year = "" + DateUtil.getYear();
            fileDate = year.substring(0, 2) + fileDate;
        }
        inctlInfo.setFileDate(fileDate);
        if (filenameBean.getSeqnoStart() == -1 && filenameBean.getSeqnoEnd() == -1) {
            String seqno = getNewSeqno(memId, filenameBean.getFileName(), fileDate);
            log.info("getNewSeqno:" + seqno);
            inctlInfo.setSeqno(seqno);
        }
        else {
            inctlInfo.setSeqno(matchFile.getName().substring(filenameBean.getSeqnoStart(), filenameBean.getSeqnoEnd()));
        }
        inctlInfo.setFileType(fileInfo.getFileType());
        inctlInfo.setWorkFlag(Layer1Constants.WORKFLAG_INWORK);
        inctlInfo.setTotRec(new Integer(0));
        inctlInfo.setRecCnt(new Integer(0));
        inctlInfo.setSucCnt(new Integer(0));
        inctlInfo.setFailCnt(new Integer(0));
        inctlInfo.setFileSize(new Integer((int) matchFile.length()));
        inctlInfo.setFullFileName(matchFile.getName());
        inctlInfo.setSysDate(sysDate);
        inctlInfo.setSysTime(sysTime);
        inctlInfo.setParMon(inctlInfo.getFileDate().substring(4, 6));
        inctlInfo.setParDay(inctlInfo.getFileDate().substring(6, 8));
        return inctlInfo;
    }

    public void handleMatchFiles() {
        for (int i = 0; i < matchFiles.size(); i++)
        {
            File matchFile = (File) matchFiles.get(i);
            File destFile = getDestFile(matchFile);
            try {
                // 依 pos, prefix, filenameBean, matchFile 來決定 memId
                String memId = determineMemId(matchFile);
                if (pos != -1 && filenameBean.getMemIdStart() != -1 && filenameBean.getMemIdEnd() != -1) {
                    // 要檢查此二個 MEM_ID 是否一致
                    String fileMemId = matchFile.getName().substring(filenameBean.getMemIdStart(), filenameBean.getMemIdEnd());
                    if (!memId.equals(fileMemId)) {
                        log.warn("memId:" + memId + " fileMemId:" + fileMemId + " not matched! this file '" + matchFile + "' ignored!");
                        continue;
                    }
                }
                // get TbInctlInfo and insert
                TbInctlInfo inctlInfo = getInctlInfo(matchFile, memId);
                TbInctlMgr mgr = new TbInctlMgr(conn);
                // check TB_INCTL 是否已存在
                if (!mgr.isExist(inctlInfo.toPK())) {
                    // copy $temp/$file to $work/$file
                    // This method copies the contents of the specified source file to the specified destination file.
                    // The directory holding the destination file is created if it does not exist.
                    // If the destination file exists, then this method will overwrite it.
                    FileUtils.copyFile(matchFile, destFile);
                    mgr.insert(inctlInfo);
                    conn.commit();
                    if (renameAfterCopy && !deleteAfterCopy) {
                        // rename matchFile
                        File rename = new File(matchFile.getAbsolutePath() + renamePending);
                        matchFile.renameTo(rename);
                        // rename OK file
                        if (Layer1Constants.OKFLAG_CHECK.equals(fileInfo.getOkFlag())) {
                        	
                        	String subFileName = "";
                        	String fileOk = "";
                        	if (fileInfo.getSubFileName().contains("/")){
                        		subFileName = fileInfo.getSubFileName().replaceAll("/", "");
                        		fileOk = matchFile.getAbsolutePath().substring(0,matchFile.getAbsolutePath().lastIndexOf(".")) + subFileName;
                        	}
                        	else{
                        		subFileName = fileInfo.getSubFileName();
                        		fileOk = matchFile.getAbsolutePath() + subFileName;
                        	}
                        	
                        	File ok = new File(fileOk);
                            File okrename = new File(fileOk + renamePending);

                            if (ok.isFile() && ok.exists()) {
                                ok.renameTo(okrename);
                            }
                        }
                    }
                    else if (!renameAfterCopy && deleteAfterCopy) {
                        // delete matchFile
                        matchFile.delete();
                        // delete OK file
                        if (Layer1Constants.OKFLAG_CHECK.equals(fileInfo.getOkFlag())) {
                            
                        	String subFileName = "";
                        	String fileOk = "";
                        	if (fileInfo.getSubFileName().contains("/")){
                        		subFileName = fileInfo.getSubFileName().replaceAll("/", "");
                        		fileOk = matchFile.getAbsolutePath().substring(0,matchFile.getAbsolutePath().lastIndexOf(".")) + subFileName;
                        	}
                        	else{
                        		subFileName = fileInfo.getSubFileName();
                        		fileOk = matchFile.getAbsolutePath() + subFileName;
                        	}
                        	
                        	File ok = new File(fileOk);
                            if (ok.isFile() && ok.exists()) {
                                ok.delete();
                            }
                        }
                    }
                }
            }
            catch (Exception ignore) {
                log.warn("handle one matchFile error:" + ignore.getMessage(), ignore);
            }
        }
    }

    protected boolean isValid() {
        boolean ret = true;
        // check fileDateStart, fileDateEnd
        if (filenameBean.getFileDateStart() == -1 || filenameBean.getFileDateEnd() == -1) {
            log.warn("filenameBean.getFileDateStart() = -1 or filenameBean.getFileDateEnd() = -1!");
            ret = false;
        }
        else if (filenameBean.getFileDateStart() >= filenameBean.getFileDateEnd()) {
            log.warn("filenameBean.getFileDateStart() >= filenameBean.getFileDateEnd()!");
            ret = false;
        }
        // check snoStart, snoEnd
        if (filenameBean.getSeqnoStart() == -1 || filenameBean.getSeqnoEnd() == -1) {
            // 可以為 -1, -1
            ret = true;
        }
        else if (filenameBean.getSeqnoStart() >= filenameBean.getSeqnoEnd()) {
            log.warn("filenameBean.getSeqnoStart() >= filenameBean.getSeqnoEnd()!");
            ret = false;
        }
        return ret;
    }

    public void process(String[] strings) throws Exception
    {
        initial();
        try {
            for (int i = 0; i < fileInfos.size(); i++)
            {
                fileInfo = (TbFileInfoInfo) fileInfos.get(i);
                setFilenameBean();
                if (filenameBean == null) {
                    log.warn("no filenameBean for '" + fileInfo.getFileName() + "'!");
                    continue;
                }
                if (!isValid()) {
                    log.warn("invalid filenameBean for '" + fileInfo.getFileName() + "'!");
                    continue;
                }
                // 記錄 LOCAL_PATH 是否有 "00000000", 有的話記錄從前面算起第幾個, 沒有的話記錄 -1
                computePos();
                setMatchFiles();
                handleMatchFiles();
            }
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
    }

    public static void main(String[] args) {
        try {
            String batchDate = System.getProperty("date");
            // Gets Spring Setting
            ApplicationContext apContext = new FileSystemXmlApplicationContext(
                            "config/batch/FilesIn/spring.xml");
            FilesIn filesIn = (FilesIn) apContext.getBean("filesIn");
            filesIn.setBatchDate(batchDate);
            //filesIn.process(args);
            filesIn.run(args);
        }
        catch (Exception e) {
            log.warn("get spring bean error:" + e.getMessage(), e);
            System.exit(-1);
        }
        System.exit(0);
    }
}
