/**
 * changelog
 * --------------------
 * 20090507
 * duncan
 * 修正 batchDate 沒設定時才產生
 * --------------------
 * 20090312
 * duncan
 * bug fix, 避免當 expFileSetting.getExpFileInfos() 沒資料時會出錯
 * --------------------
 * 20080529
 * duncan
 * rename PROC_DATE, PROC_TIME to SYS_DATE, SYS_TIME
 * --------------------
 * 20071212
 * duncan,anny,clare
 * 加上 recoverData 的 default 動作
 * 1. DELETE TB_OUTCTL BASE ON batchDate and fileName
 * 2. DELETE TB_FTP_LOG BASE ON batchDate and fileName
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpCDRP;

import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileResult;
import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileSetting;
import tw.com.hyweb.core.cp.batch.framework.expfiles.OutctlBean;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsUtil;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;
import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

import java.sql.Connection;
import java.util.List;
import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;


/**
 * <pre>
 * AbstractExpFile javabean
 * </pre>
 * author:duncan
 */
public abstract class AbstractExpFile extends AbstractBatchBasic {
    private static Logger log = Logger.getLogger(AbstractExpFile.class);
    protected OutctlBean outctlBean = null;

    protected String batchDate = "";
    protected String recoverLevel = "";

    // thisRcode, 因為可能一次處理多個檔案 , 所以整個跑完才註記 thisRcode
    // 若有某一個檔案有發生錯誤, 註記 thisRcode = '2001'
    protected String thisRcode = Layer1Constants.RCODE_0000_OK;
    
    // millisecond
    protected int sleepPerInfo = 500;
    protected Connection conn = null;

    // 由每支匯出程式決定
    protected ExpFileSetting expFileSetting = null;

    // current run info
    protected ExpFileInfo expFileInfo = null;
    protected ExpFileResult expFileResult = null;
    
    // export tb_trans_dtl data flag
    protected boolean expTxnDtlFlag = true;

	// override methods by sub-class
    public abstract ExpFileSetting makeExpFileSetting();

    protected AbstractExpFile() {
    }

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }

    public int getSleepPerInfo() {
        return sleepPerInfo;
    }

    public void setSleepPerInfo(int sleepPerInfo) {
        this.sleepPerInfo = sleepPerInfo;
    }
    
    public boolean isExpTxnDtlFlag() {
		return expTxnDtlFlag;
	}

	public void setExpTxnDtlFlag(boolean expTxnDtlFlag) {
		this.expTxnDtlFlag = expTxnDtlFlag;
	}

    public ExpFileSetting getExpFileSetting() {
        return expFileSetting;
    }

    public void setExpFileSetting(ExpFileSetting expFileSetting) {
        this.expFileSetting = expFileSetting;
    }

    public String outputBeforeFile() {
        return "";
    }
    public String outputEndFile() {
        return "";
    }

    public abstract String outputOneRecord(List record) throws Exception;
    
    public abstract List outputDtlRecord(List record);
    
    public void replaceDataLine() throws Exception {};
    
    public String outputAfterFile() {
        return "";
    }

    public void actionsAfterFile()  throws Exception {
        return;
    }

    public void actionsAfterInfo()  throws Exception {
        return;
    }
    
    //20131206 資料抓取後檢查內容用check ExpField
    public void checkExpField()  throws Exception {
        return;
    }
    
    public TbOutctlInfo changeOutctlInfo(TbOutctlInfo outctlInfo) throws Exception {
        return outctlInfo;
    }
    // override methods by sub-class

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


    
    public void locateFile() throws Exception {
        expFileInfo.setExpFullFileName(expFileInfo.getFullFileName());
        expFileInfo.setExpTempFullFileName(expFileInfo.getFullFileName() + expFileInfo.getTempFilePending());
        // 決定完整路徑
        final String MEMGROUPID_SPECIAL = "22222";
        final String MEMID_SPECIAL = "00000000";
        final String MERCHID_SPECIAL = "111111111111111";
        String workDir = "";
        String relativePath = "";
        try {
            workDir = BatchUtil.getWorkDirectory();
        }
        catch (Exception ignore) {
            log.warn("should not happen:" + ignore.getMessage(), ignore);
            throw ignore;
        }
        workDir = pendingEndSep(workDir);
        
        relativePath = expFileInfo.getFileInfo().getLocalPath();
        
        if (expFileInfo.getFileInfo().getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1) {
            // find "22222"
            relativePath = relativePath.replaceAll(MEMGROUPID_SPECIAL, expFileInfo.getMemGroupId());
        }
        if (expFileInfo.getFileInfo().getLocalPath().indexOf(MEMID_SPECIAL) != -1) {
            // find "00000000"
            relativePath = relativePath.replaceAll(MEMID_SPECIAL, expFileInfo.getMemId());
        }
        if (expFileInfo.getFileInfo().getLocalPath().indexOf(MERCHID_SPECIAL) != -1) {
            // find "111111111111111"
            relativePath = relativePath.replaceAll(MERCHID_SPECIAL, expFileInfo.getMerchId());
        }
        
        relativePath = pendingEndSep(relativePath);
        String normalPath = "";
        String normalTempPath = "";
        normalPath = FilenameUtils.normalize(workDir + relativePath + expFileInfo.getExpFullFileName());
        normalTempPath = FilenameUtils.normalize(workDir + relativePath + expFileInfo.getExpTempFullFileName());
        expFileInfo.setExpFile(new File(normalPath));
        expFileInfo.setExpTempFile(new File(normalTempPath));
        // create directory
        if (expFileInfo.isUsingTempFile()) {
            FileUtils.forceMkdir(expFileInfo.getExpTempFile().getParentFile());
        }
        else {
            FileUtils.forceMkdir(expFileInfo.getExpFile().getParentFile());
        }
    }

    private BufferedWriter bw = null;
    public void checkExpFile() throws Exception {
        // expFile.length() must <= 10000000000 (fileInfo.fileSize length is 10)
        String fullName = "";
        long fileSize = 0;
        if (expFileInfo.isUsingTempFile()) {
            fullName = expFileInfo.getExpTempFile().getAbsolutePath();
            fileSize = expFileInfo.getExpTempFile().length();
        }
        else {
            fullName = expFileInfo.getExpFile().getAbsolutePath();
            fileSize = expFileInfo.getExpFile().length();
        }
        if (fileSize >= 10000000000L) {
            throw new Exception("fileSize >= 10000000000L for '" + fullName + "'!");
        }
        // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
        if (expFileInfo.isCheckFlag()) {
            if (fileSize % expFileInfo.getFileInfo().getDataLen().intValue() != 0) {
            	log.info("fileSize: " + fileSize);
            	log.info("DataLen: " + expFileInfo.getFileInfo().getDataLen().intValue());
                throw new Exception("fileSize % expFileInfo.getFileInfo().getDataLen().intValue() != 0 for '" + fullName + "'!");
            }
            if (expFileInfo.getRecordsPerFile() <= 0 &&
                    expFileResult.getTotalRecords() != expFileResult.getRecordCount()) {
                throw new Exception("expFileResult.getTotalRecords() != expFileResult.getRecordCount() for '" + fullName + "'!");
            }
        }
    }

    public void renameTempFile() throws Exception {
        // 依 usingTempFile 來決定是否要做 rename 的動作
        if (expFileInfo.isUsingTempFile()) {
            expFileInfo.getExpTempFile().renameTo(expFileInfo.getExpFile());
        }
    }

    // for 拆檔用
    protected int leftRecords = 0;
    public TbOutctlInfo makeOutctlInfo() {
        TbOutctlInfo outctlInfo = new TbOutctlInfo();
        outctlInfo.setMemGroupId(expFileInfo.getMemGroupId());
        outctlInfo.setMemId(expFileInfo.getMemId());
        outctlInfo.setMerchId(expFileInfo.getMerchId());
        outctlInfo.setFileName(expFileInfo.getFileName());
        outctlInfo.setFileDate(expFileInfo.getFileDate());
        outctlInfo.setSeqno(expFileInfo.getSeqno());
        outctlInfo.setFileType(expFileInfo.getFileInfo().getFileType());
        outctlInfo.setWorkFlag(Layer1Constants.OWORKFLAG_INWORK);
        // 要考慮拆檔的關係
        if (expFileInfo.getRecordsPerFile() <= 0) {
            outctlInfo.setTotRec(new Integer(expFileResult.getTotalRecords()));
        }
        else {
            // 決定拆檔的 totRec
            int totRec = 0;
            if (leftRecords >= expFileInfo.getRecordsPerFile()) {
                totRec = expFileInfo.getRecordsPerFile();
            }
            else {
                totRec = leftRecords;
            }
            outctlInfo.setTotRec(totRec);
            leftRecords -= totRec;
        }
        if (expFileInfo.isUsingTempFile()) {
            outctlInfo.setFileSize(new Long(expFileInfo.getExpTempFile().length()));
        }
        else {
            outctlInfo.setFileSize(new Long(expFileInfo.getExpFile().length()));
        }
        outctlInfo.setFullFileName(expFileInfo.getFullFileName());
        String dateTime = DateUtil.getTodayString();
        outctlInfo.setSysDate(dateTime.substring(0, 8));
        outctlInfo.setSysTime(dateTime.substring(8, 14));
        outctlInfo.setParMon(expFileInfo.getFileDate().substring(4, 6));
        outctlInfo.setParDay(expFileInfo.getFileDate().substring(6, 8));
        // 和 TB_BATCH_RESULT 關連起來
        outctlInfo = outctlBean.makeOutctl(outctlInfo);
        return outctlInfo;
    }

    public void closeFileProcess() throws Exception {
        // 處理 outputAfterFile 動作
        try {
            String outputString = outputAfterFile();
            if (!StringUtil.isEmpty(outputString)) {
                bw.write(outputString);
                bw.write(expFileInfo.getLineSeparator());
            }
            bw.close();
            
            replaceDataLine();
            
            // check expFile
            // expFile.length() must <= 10000000000 (fileInfo.fileSize length is 10)
            // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
            checkExpFile();
            // 先 make outctlInfo, for fileSize, 要在 renameTempFile 之前
            TbOutctlInfo outctlInfo = makeOutctlInfo();
            // 依 usingTempFile 來決定是否要做 rename 的動作
            renameTempFile();
            // do actionsAfterFile
            actionsAfterFile();
            // 如有變更任何設定，執行changOutctlInfo 的動作
            outctlInfo = changeOutctlInfo(outctlInfo);
            // finally do insert outctl
            //conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            outctlBean.insertOutctl(conn, false, outctlInfo);
            conn.commit();
        }
        catch (Exception ignore) {
            // rollback and delete file
            try {
                conn.rollback();
                expFileInfo.getExpFile().delete();
//                expFileInfo.getExpTempFile().delete();
            }
            catch (Exception ignore2) {
                ;
            }
            throw ignore;
        }
    }

    public void resetExpFileInfo() throws Exception {
        // reset expFileInfo 的 seqNo, fullFileName
        int nextSeqNo = Integer.parseInt(expFileInfo.getSeqno()) + 1;
        String nextSeqNoString = StringUtil.pendingKey(nextSeqNo, 2);
        expFileInfo.setSeqno(nextSeqNoString);
        String nextFullFileName = expFileInfo.getFullFileName().substring(0, expFileInfo.getSeqnoStart()) +
                nextSeqNoString +
                expFileInfo.getFullFileName().substring(expFileInfo.getSeqnoEnd());
        expFileInfo.setFullFileName(nextFullFileName);
        // 設定 expFileInfo 的 expTempFullFileName, expFullFileName, expFile
        // 依 expFileInfo.usingTempFile 決定完整路徑
        locateFile();
    }

    public void handleOneExpFile() throws Exception {
        // 設定 expFileInfo 的 expTempFullFileName, expFullFileName, expFile
        // 依 expFileInfo.usingTempFile 決定完整路徑
        locateFile();
        try {
        	leftRecords = expFileResult.getTotalRecords();
            if (expFileInfo.isUsingTempFile()) {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(expFileInfo.getExpTempFile()), expFileInfo.getFileEncoding()));
            }
            else {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(expFileInfo.getExpFile()), expFileInfo.getFileEncoding()));
            }
            // 處理 outputBeforeFile 動作
            String outputString = outputBeforeFile();
            if (!StringUtil.isEmpty(outputString)) {
                bw.write(outputString);
                bw.write(expFileInfo.getLineSeparator());
            }
            for (int i = 0; i < expFileResult.getTotalRecords(); i++) {
                // 處理每筆記錄
                List record = expFileResult.getRecord();
                String s = outputOneRecord(record);
                if (!StringUtil.isEmpty(s)) {
                    bw.write(s);
                    bw.write(expFileInfo.getLineSeparator());
                    
                    //export detail
                    if(expTxnDtlFlag){
                    	List sList = outputDtlRecord(record);
                    	for(int j = 0; j<sList.size(); j++){                   		
                    		String sd = sList.get(j).toString();
                    		if (!StringUtil.isEmpty(sd)) {
                                bw.write(sd);
                                bw.write(expFileInfo.getLineSeparator());
                        	}
                    	}
                    }
                }
                expFileResult.setRecordCount(expFileResult.getRecordCount() + 1);
                // 處理 flush 動作
                if (expFileInfo.getRecordsPerFlush() > 0 &&
                        (expFileResult.getRecordCount() % expFileInfo.getRecordsPerFlush() == 0)) {
                    bw.flush();
                }
                // 處理拆檔動作
                if (expFileInfo.getRecordsPerFile() > 0 &&
                        (expFileResult.getRecordCount() % expFileInfo.getRecordsPerFile() == 0)) {
                    // 處理 outputAfterFile 動作
                    // check expFile
                    // expFile.length() must <= 10000000000 (fileInfo.fileSize length is 10)
                    // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
                    // 先 make outctlInfo, for fileSize, 要在 renameTempFile 之前
                    // 依 usingTempFile 來決定是否要做 rename 的動作
                    // do actionsAfterFile
                    // finally do insert outctl
                    closeFileProcess();
                    if (leftRecords > 0) {
                        // reset expFileInfo 的 seqNo, fullFileName
                        // 設定 expFileInfo 的 expTempFullFileName, expFullFileName, expFile
                        // 依 expFileInfo.usingTempFile 決定完整路徑
                        resetExpFileInfo();
                        if (expFileInfo.isUsingTempFile()) {
                            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(expFileInfo.getExpTempFile()), expFileInfo.getFileEncoding()));
                        }
                        else {
                            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(expFileInfo.getExpFile()), expFileInfo.getFileEncoding()));
                        }
                        // 處理 outputBeforeFile 動作
                        String tmp = outputBeforeFile();
                        if (!StringUtil.isEmpty(tmp)) {
                            bw.write(tmp);
                            bw.write(expFileInfo.getLineSeparator());
                        }
                    }
                }
            }
            String endString = outputEndFile();
            if (!StringUtil.isEmpty(endString)) {
                bw.write(endString);
                bw.write(expFileInfo.getLineSeparator());
            }
            
            // 依 recordsPerFile 來決定是否必要再做一次 closeFileProcess
            if (expFileInfo.getRecordsPerFile() <= 0 ||
                    (expFileInfo.getRecordsPerFile() > 0 && expFileResult.getRecordCount() % expFileInfo.getRecordsPerFile() != 0)) {
                // 處理 outputAfterFile 動作
                // check expFile
                // expFile.length() must <= 10000000000 (fileInfo.fileSize length is 10)
                // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
                // 先 make outctlInfo, for fileSize, 要在 renameTempFile 之前
                // 依 usingTempFile 來決定是否要做 rename 的動作
                // do actionsAfterFile
                // finally do insert outctl
                closeFileProcess();
            }
            // do actionsAfterInfo
            actionsAfterInfo();
        }
        catch (Exception ignore) {
            log.warn("handleOneExpFile error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
        finally {
            ReleaseResource.releaseIO(bw);
        }
    }

    private String getFileNames() {
        // return 'fileName1', 'fileName2', ...
        StringBuffer ret = new StringBuffer();
        List fileNames = expFileSetting.getFileNames();
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = (String) fileNames.get(i);
            if (i > 0) {
                ret.append(", ");
            }
            ret.append(StringUtil.toSqlValueWithSQuote(fileName));
        }
        return ret.toString();
    }

    protected String makeDeleteOutctl() {
        // DELETE TB_OUTCTL WHERE FILE_NAME IN ('$fileNames') AND FILE_DATE = '$batchDate'
        String fileNames = getFileNames();
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE TB_OUTCTL WHERE FILE_NAME IN (" + fileNames + ")");
        sql.append(" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate));
//        sql.append(" AND SYS_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate));
        return sql.toString();
    }

    protected String makeDeleteFtpLog() {
        // DELETE TB_FTP_LOG WHERE IN_OUT = 'O' AND FTP_IP IN (SELECT FTP_IP FROM TB_FTP_INFO WHERE FILE_NAME IN ('$fileNames') AND IN_OUT = 'O')
        // AND REMOTE_PATH IN (SELECT REMOTE_PATH FROM TB_FTP_INFO WHERE FILE_NAME IN ('$fileNames') AND IN_OUT = 'O')
        // AND SYS_DATE = '$batchDate'
        String fileNames = getFileNames();
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE TB_FTP_LOG WHERE IN_OUT = " + StringUtil.toSqlValueWithSQuote(Constants.INOUT_OUT));
        sql.append(" AND FTP_IP IN (SELECT FTP_IP FROM TB_FTP_INFO WHERE FILE_NAME IN (" + fileNames + ") AND IN_OUT = " + StringUtil.toSqlValueWithSQuote(Constants.INOUT_OUT) + ")");
        sql.append(" AND REMOTE_PATH IN (SELECT REMOTE_PATH FROM TB_FTP_INFO WHERE FILE_NAME IN (" + fileNames + ") AND IN_OUT = " + StringUtil.toSqlValueWithSQuote(Constants.INOUT_OUT) + ")");
        sql.append(" AND SYS_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate));
        return sql.toString();
    }

    public void recoverData() throws Exception {
        ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(false);
        sqlsInfo.setSavepoint(true);
        sqlsInfo.addSql(makeDeleteOutctl());
        sqlsInfo.addSql(makeDeleteFtpLog());
        log.info("recoverData sqlsInfo:" + sqlsInfo);
        ExecuteSqlsUtil.executeSqls(conn, sqlsInfo); 
    }

    public void process(String[] args) throws Exception {
    	
        // impfiles -> linkControl = "O"
        setLinkControl("O");
        // 若 makeExpFileSetting 有提供, 用這個, 不然就用原來的, for spring setting
        if (makeExpFileSetting() != null) {
            expFileSetting = makeExpFileSetting();
        }
        if (expFileSetting == null) {
            throw new Exception("expFileSetting is null!");
        }
        if (StringUtil.isEmpty(batchDate)) {
            String tmpDate = System.getProperty("date", "");
            if (StringUtil.isEmpty(tmpDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            else {
                batchDate = tmpDate;
                if (!DateUtil.isValidDate(batchDate)) {
                    throw new IllegalArgumentException("invalid batchDate(" + batchDate + ")!");
                }
            }
        }
        recoverLevel = System.getProperty("recover", "");
        if (!StringUtil.isEmpty(recoverLevel)) {
            try {
                conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
                recoverData();
                if (conn != null) {
                    conn.commit();
                }
            }
            catch (Exception ignore) {
                log.warn("do recoverData error:" + ignore.getMessage(), ignore);
                if (conn != null) {
                    try {
                        conn.rollback();
                    }
                    catch (Exception ignore2) {
                        ;
                    }
                }
                throw ignore;
            }
            finally {
                ReleaseResource.releaseDB(conn);
            }
            return;
        }
        if (expFileSetting.getExpFileInfos().size() <= 0) {
            log.info("expFileSetting.getExpFileInfos().size():" + expFileSetting.getExpFileInfos().size());
            log.info("not to execute export action!");
            return;
        }
        // check 相關設定
        expFileSetting.checkSetting();
        
        //20131206 在做動作之前，先檢查每一筆資料內容
        checkExpField();
        
        // 對每一筆 expFileInfo 做動作
        for (int i = 0; i < expFileSetting.getExpFileInfos().size(); i++) {
            try {
                // reset inctlBean
                outctlBean = new OutctlBean();
                outctlBean.setRelated(true);
                outctlBean.setBatchResultInfo(getBatchResultInfo());
                // make a expFileResult by expFileInfo
                conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
                expFileInfo = (ExpFileInfo) expFileSetting.getExpFileInfos().get(i);
                
                expFileResult = new ExpFileResult();
                expFileResult.setConnection(conn);
                expFileResult.setExpFileInfo(expFileInfo);
                // check conn and expFileInfo properties
                // get totalRecords and check totalRecords
                // execute expFileInfo.selectSQL
                expFileResult.startProcess();
                // 設定 expFileInfo 的 expTempFullFileName, expFullFileName, expFile
                // 依 expFileInfo.usingTempFile 決定完整路徑
                // 依 expFileInfo, expFileResult 的設定來處理
                handleOneExpFile();
                Thread.sleep(sleepPerInfo);
            }
            catch (Exception ignore) {
            	thisRcode = Layer1Constants.RCODE_2001_SOMEDATAERROR;
                log.warn("handle one expFileinfo error for '" + expFileInfo + "':" + ignore.getMessage(), ignore);
            }
            finally {
                if (expFileResult != null) {
                    expFileResult.closeResource();
                }
                ReleaseResource.releaseDB(conn);
            }
        }
        // set rcode to TB_BATCH_RESULT
        setRcode(thisRcode);
    }
}
