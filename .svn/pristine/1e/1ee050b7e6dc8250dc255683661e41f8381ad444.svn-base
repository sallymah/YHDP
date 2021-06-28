/**
 * changelog
 * --------------------
 * 20090316
 * duncan
 * 對 TB_OUTCTL.FILE_NAME 可以為 "NAME.XXX", 但 TB_FILE_INFO.FILE_NAME 為 "NAME" 的例子要能 support
 * --------------------
 * 20071205
 * duncan
 * 嚴重 bug fix, checkOutctlInfo 忘了關 Connection
 * --------------------
 * 20070829
 * 取消checkSetting1()中如果fileName count >1丟出exception的機制
 * 充許同一次匯出中可使用不同的TbFileInfo
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTrnTefr;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileResult;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbFileInfoPK;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.service.db.info.TbOutctlPK;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbOutctlMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 * ExpFileSetting javabean
 * check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
 * checkFlag:boolean
 * fileEncoding:String
 * usingTempFile, 是否使用 temp file, default true
 * usingTempFile:boolean
 * 若用 temp file, 預設先 pending 的字串, default ".TMP"
 * tempFilePending:String
 * recordsPerFlush:int
 * recordsPerFile:int
 * lineSeparator:String
 * fileInfo:fileInfo
 * expFileInfos is List object, each element is ExpFileInfo object
 * expFileInfos:List
 * </pre>
 * author: duncan
 */
public class ExpFileSetting {
    // checkFlag
    // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
    private boolean checkFlag = true;
    private String fileEncoding = "UTF-8";
    // usingTempFile, 是否使用 temp file, default true
    private boolean usingTempFile = true;
    // 若用 temp file, 預設先 pending 的字串, default ".TMP"
    private String tempFilePending = ".TMP";
    // 處理多少筆做一次 flush 動作, default 不做(<= 0)
    private int recordsPerFlush = -1;
    // 多少筆產生一個 file, default 全部一個檔案(<= 0)
    private int recordsPerFile = -1;
    // lineSeparator
    private String lineSeparator = System.getProperty("line.separator", "\n");
    // fileInfo for fileName for IN_OUT = 'O'
    //private TbFileInfoInfo fileInfo = null;
    // expFileInfos is List object, each element is ExpFileInfo object
    // sort by ExpFileInfo.getKey()
    private List expFileInfos = new ArrayList();

    public ExpFileSetting() {
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public boolean isCheckFlag() {
        return checkFlag;
    }

    public void setCheckFlag(boolean checkFlag) {
        this.checkFlag = checkFlag;
    }

    public List getExpFileInfos() {
        return expFileInfos;
    }

    public void setExpFileInfos(List expFileInfos) {
        this.expFileInfos = expFileInfos;
    }

    public void addExpFileInfo(ExpFileInfo expFileInfo) {
        if (expFileInfo == null) {
            return;
        }
        // must be set (memId, fileName, fileDate, seqno, fullFileName, sqlCmd)
        if (StringUtil.isEmpty(expFileInfo.getMemId())) {
            return;
        }
        if (StringUtil.isEmpty(expFileInfo.getFileName())) {
            return;
        }
        if (StringUtil.isEmpty(expFileInfo.getFileDate())) {
            return;
        }
        if (StringUtil.isEmpty(expFileInfo.getSeqno())) {
            return;
        }
        if (StringUtil.isEmpty(expFileInfo.getFullFileName())) {
            return;
        }
        if (StringUtil.isEmpty(expFileInfo.getSelectSQL())) {
            return;
        }
        this.expFileInfos.add(expFileInfo);
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    /*public TbFileInfoInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(TbFileInfoInfo fileInfo) {
        this.fileInfo = fileInfo;
    }*/

    public int getRecordsPerFile() {
        return recordsPerFile;
    }

    public void setRecordsPerFile(int recordsPerFile) {
        this.recordsPerFile = recordsPerFile;
    }

    public int getRecordsPerFlush() {
        return recordsPerFlush;
    }

    public void setRecordsPerFlush(int recordsPerFlush) {
        this.recordsPerFlush = recordsPerFlush;
    }

    public String getTempFilePending() {
        return tempFilePending;
    }

    public void setTempFilePending(String tempFilePending) {
        this.tempFilePending = tempFilePending;
    }

    public boolean isUsingTempFile() {
        return usingTempFile;
    }

    public void setUsingTempFile(boolean usingTempFile) {
        this.usingTempFile = usingTempFile;
    }

    /*public void checkSetting1() throws Exception {
        // sort by expFileInfo.getKey()
        Collections.sort(expFileInfos, new KeyAscComparator());
        // 檢查 expFileInfos 內的 fileName 是否都一樣
        int fileNameCount = 0;
        String tmp = "";
        for (int i = 0; i < expFileInfos.size(); i++) {
            ExpFileInfo expFileInfo = (ExpFileInfo) expFileInfos.get(i);
            String fileName = expFileInfo.getFileName();
            int idx = fileName.indexOf(".");
            if (idx != -1) {
                fileName = fileName.substring(0, idx);
            }
            if (!tmp.equals(fileName)) {
                tmp = fileName;
                fileNameCount++;
            }
        }
        if (fileNameCount > 1) {
            throw new Exception("fileNameCount '" + fileNameCount + "' > 1!");
        }
        // 查詢 fileInfo 是否存在?
        Connection conn = null;
        try {
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            // in here, tmp is the only one fileName
            TbFileInfoMgr mgr = new TbFileInfoMgr(conn);
            TbFileInfoPK pk = new TbFileInfoPK();
            pk.setFileName(tmp);
            pk.setInOut(Layer1Constants.INOUT_OUT);
            fileInfo = mgr.querySingle(pk);
        }
        catch (Exception ignore) {
            fileInfo = null;
            throw ignore;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
        if (fileInfo == null) {
            throw new Exception("can not get fileInfo for '" + tmp + "'!");
        }
    }*/

    public void resetExpFileInfos() throws Exception {
        // set common properties
        // (checkFlag, fileEncoding, usingTempFile, tempFilePending, recordsPerFlush, recordsPerFile, fileInfo)
        for (int i = 0; i < expFileInfos.size(); i++) {
            ExpFileInfo expFileInfo = (ExpFileInfo) expFileInfos.get(i);
            expFileInfo.setCheckFlag(checkFlag);
            expFileInfo.setFileEncoding(fileEncoding);
            expFileInfo.setUsingTempFile(usingTempFile);
            expFileInfo.setTempFilePending(tempFilePending);
            expFileInfo.setRecordsPerFlush(recordsPerFlush);
            expFileInfo.setRecordsPerFile(recordsPerFile);
            expFileInfo.setLineSeparator(lineSeparator);
            
            // 沒設才幫忙設定 fileInfo
            if (expFileInfo.getFileInfo() == null) {
            	TbFileInfoInfo fileInfo = null;
            	Connection conn = null;
                try {
                    conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
                    // in here, tmp is the only one fileName
                    TbFileInfoMgr mgr = new TbFileInfoMgr(conn);
                    TbFileInfoPK pk = new TbFileInfoPK();
                    pk.setFileName(expFileInfo.getFileName());
                    pk.setInOut(Layer1Constants.INOUT_OUT);
                    fileInfo = mgr.querySingle(pk);
                }
                catch (Exception ignore) {
                    fileInfo = null;
                    throw ignore;
                }
                finally {
                    ReleaseResource.releaseDB(conn);
                }
                if (fileInfo == null) {
                    throw new Exception("can not get fileInfo for '" + expFileInfo.getFileName() + "'!");
                }
            	
                expFileInfo.setFileInfo(fileInfo);
            }
        }
    }

    public void checkSetting2() throws Exception {
        // 檢查若 $memId:$fileName:$fileDate 的設定若超過 1 個, recordsPerFile 必須是 <= 0 (即不可再拆檔案)
        // key:String($memId:$fileName:$fileDate), value:Integer
        HashMap keyCount = new HashMap();
        for (int i = 0; i < expFileInfos.size(); i++) {
            ExpFileInfo expFileInfo = (ExpFileInfo) expFileInfos.get(i);
            // check seqnoStart, seqnoEnd, 若有設 recordsPerFile, seqnoStart, seqnoEnd 一定要給
            if (recordsPerFile > 0) {
                if (expFileInfo.getSeqnoStart() < 0 ||
                        expFileInfo.getSeqnoEnd() < 0 ||
                        expFileInfo.getSeqnoStart() >= expFileInfo.getSeqnoEnd()) {
                    throw new Exception("recordsPerFile > 0, but invalid seqnoStart, seqnoEnd setting for '" + expFileInfo.getKey() + "'!");
                }
            }
            String key = expFileInfo.getMemId() + ":" + expFileInfo.getFileName() + ":" + expFileInfo.getFileDate();
            if (keyCount.get(key) == null) {
                // not found
                keyCount.put(key, new Integer(1));
            }
            else {
                // found
                Integer count = (Integer) keyCount.get(key);
                count = new Integer(count.intValue() + 1);
                keyCount.put(key, count);
                if (count.intValue() > 1 && recordsPerFile > 0) {
                    throw new Exception("recordsPerFile > 0 but count > 1 for '" + key + "'!");
                }
            }
        }
    }

    public void checkOutctlInfo() throws Exception {
        // 針對每一個 expFileInfo 檢查其 TB_OUTCTL 是否存在, 存在將視為整個設定錯誤
        Connection conn = null;
        try {
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            for (int i = 0; i < expFileInfos.size(); i++) {
                ExpFileInfo expFileInfo = (ExpFileInfo) expFileInfos.get(i);
                TbOutctlInfo outctlInfo = null;
                TbOutctlPK pk = new TbOutctlPK();
                pk.setMemId(expFileInfo.getMemId());
                pk.setFileName(expFileInfo.getFileName());
                pk.setFileDate(expFileInfo.getFileDate());
                pk.setSeqno(expFileInfo.getSeqno());
                TbOutctlMgr mgr = new TbOutctlMgr(conn);
                outctlInfo = mgr.querySingle(pk);
                if (outctlInfo != null) {
                    throw new Exception("outctlInfo for '" + expFileInfo.getKey() + "' already exists!");
                }
            }
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
    }

    public void checkSetting() throws Exception {
        // sort by expFileInfo.getKey()
        // 檢查 expFileInfos 內的 fileName 是否都一樣
        // 查詢 fileInfo 是否存在?
        //if (fileInfo == null) {
        //    checkSetting1();
        //}
        // set common properties
        // (checkFlag, fileEncoding, usingTempFile, tempFilePending, recordsPerFlush, recordsPerFile, fileInfo)
        resetExpFileInfos();
        // 檢查若 $memId:$fileName:$fileDate 的設定若超過 1 個, recordsPerFile 必須是 <= 0 (即不可再拆檔案)
        // check seqnoStart, seqnoEnd, 若有設 recordsPerFile, seqnoStart, seqnoEnd 一定要給
        checkSetting2();
        // 針對每一個 expFileInfo 檢查其 TB_OUTCTL 是否存在, 存在將視為整個設定錯誤
        checkOutctlInfo();
    }

    public List getFileNames() {
        List fileNames = new ArrayList();
        for (int i = 0; i < expFileInfos.size(); i++) {
            ExpFileInfo expFileInfo = (ExpFileInfo) expFileInfos.get(i);
            if (!fileNames.contains(expFileInfo.getFileName())) {
                fileNames.add(expFileInfo.getFileName());
            }
        }
        return fileNames;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ExpFileSetting: ");
        sb.append(" checkFlag:" + checkFlag);
        sb.append(" fileEncoding:" + fileEncoding);
        sb.append(" usingTempFile:" + usingTempFile);
        sb.append(" tempFilePending:" + tempFilePending);
        sb.append(" recordsPerFlush:" + recordsPerFlush);
        sb.append(" recordsPerFile:" + recordsPerFile);
        String show = "";
        if ("\n".equals(lineSeparator)) {
            show = "unix";
        }
        else if ("\r".equals(lineSeparator)) {
            show = "mac";
        }
        else if ("\r\n".equals(lineSeparator)) {
            show = "dos";
        }
        else {
            show = "unknown";
        }
        sb.append(" lineSeparator:" + show);
        //sb.append(" fileInfo:" + fileInfo);
        sb.append(" expFileInfos:" + expFileInfos);
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        ExpFileSetting efs = new ExpFileSetting();
        ExpFileInfo expFileInfo = new ExpFileInfo();
        expFileInfo.setMemId("99999999");
        expFileInfo.setFileName("FICASE1");
        expFileInfo.setFileDate("20070312");
        expFileInfo.setSeqno("10");
        expFileInfo.setFullFileName("FICASE1.2007030601");
        expFileInfo.setSelectSQL("SELECT REGION_ID, MEM_ID, CARD_NO, EXPIRY_DATE, RLD_MAX_AMT FROM TB_CARD WHERE MEM_ID = '88000001'");
        efs.addExpFileInfo(expFileInfo);
        efs.checkSetting();

        Connection conn = null;
        ExpFileResult efr = null;
        try {
            efr = new ExpFileResult();
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            efr.setExpFileInfo(expFileInfo);
            efr.setConnection(conn);
            efr.startProcess();
            for (int i = 1; i <= efr.getFieldCount(); i++) {
                System.out.println(efr.getFieldMetaData(i));
            }
            for (int i = 0; i < efr.getTotalRecords(); i++) {
                System.out.println(efr.getRecord());
            }
        }
        finally {
            if (efr != null) {
                efr.closeResource();
            }
            ReleaseResource.releaseDB(conn);
        }
    }
}

