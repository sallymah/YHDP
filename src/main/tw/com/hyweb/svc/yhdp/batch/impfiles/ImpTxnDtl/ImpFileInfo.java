/**
 * changelog
 * --------------------
 * 20090602
 * duncan,ivan
 * 1. 處理 DataLineInfo 的 fieldDataSize
 * 2. 加上能讀出 header, trailor 的 methods
 * --------------------
 * 20090310
 * duncan
 * 多考慮分隔符號的檔案的相關處理
 * --------------------
 * 20070626
 * duncan,john
 * bug fix
 * 1. checkFile2701 first, then checkOthers
 * 2. modify readLinesBuff bug! readLen < 0, can't be added back
 * --------------------
 * 20070531
 * duncan
 * add checkEmptyFile property, default is false
 * --------------------
 * 20070329, add ignoreChar(default ' ') setting, 可設定全部都是 ignoreChar 時, 將 ignore, 即轉成空字串
 * 20070329, add defaultValue(default "") setting, 當取得的值是空的或 null, 將以此 defaultValue 值代替
 * 處理上述這二件事
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbInctlErrInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbPersoFactoryInfo;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.MappingInfo;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpFileInfo
 * </pre>
 * author:duncan
 */
public class ImpFileInfo {
    private static Logger log = Logger.getLogger(ImpFileInfo.class);
    public static int PARSER_NONE = 0;
    public static int PARSER_HEX = 1;
    public static int PARSER_BYTE = 2;
    public static int PARSER_TRN = 3;
    private MappingInfo mappingInfo = null;
    private TbFileInfoInfo fileInfo = null;
    private TbInctlInfo inctlInfo = null;
    private File file = null;
    private FileInputStream fileInputStream = null;
    /*private BufferedReader br = null;*/
    private int totRec = 0;
    private int recCnt = 0;
    private int sucCnt = 0;
    private int failCnt = 0;
    private boolean checkOKFile = false;
    private String acqMemId = null;

    private boolean checkEmptyFile = false;

    private List header = null;
    private List trailor = null;
    private List<byte[]> contentList = new ArrayList<byte[]>();//記錄所有交易內容
    
    public ImpFileInfo() {
    }

    public List getHeader() {
        return header;
    }

    public void setHeader(List header) {
        this.header = header;
    }

    public List getTrailor() {
        return trailor;
    }

    public void setTrailor(List trailor) {
        this.trailor = trailor;
    }

    public boolean isCheckEmptyFile() {
        return checkEmptyFile;
    }

    public void setCheckEmptyFile(boolean checkEmptyFile) {
        this.checkEmptyFile = checkEmptyFile;
    }

    public MappingInfo getMappingInfo() {
        return mappingInfo;
    }

    public void setMappingInfo(MappingInfo mappingInfo) {
        this.mappingInfo = mappingInfo;
    }
    
    public TbFileInfoInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(TbFileInfoInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public TbInctlInfo getInctlInfo() {
        return inctlInfo;
    }

    public void setInctlInfo(TbInctlInfo inctlInfo) {
        this.inctlInfo = inctlInfo;
    }

    public int getTotRec() {
        return totRec;
    }

    public void setTotRec(int totRec) {
        this.totRec = totRec;
    }

    public int getRecCnt() {
        return recCnt;
    }

    public void setRecCnt(int recCnt) {
        this.recCnt = recCnt;
    }

    public int getSucCnt() {
        return sucCnt;
    }

    public void setSucCnt(int sucCnt) {
        this.sucCnt = sucCnt;
    }

    public int getFailCnt() {
        return failCnt;
    }

    public void setFailCnt(int failCnt) {
        this.failCnt = failCnt;
    }

    public boolean isCheckOKFile() {
        return checkOKFile;
    }

    public void setCheckOKFile(boolean checkOKFile) {
        this.checkOKFile = checkOKFile;
    }
    
    public void check() {
        if (mappingInfo == null) {
            throw new IllegalArgumentException("mappingInfo property is null!");
        }
        if (fileInfo == null) {
            throw new IllegalArgumentException("fileInfo property is null!");
        }
        if (inctlInfo == null) {
            throw new IllegalArgumentException("inctlInfo property is null!");
        }
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

    public void computeTotRecByReadFile() {
        BufferedReader tmpbr = null;
        try {
            tmpbr = new BufferedReader(new InputStreamReader(new FileInputStream(file), mappingInfo.getEncoding()));
            while (tmpbr.readLine() != null) {
                totRec++;
            }
            totRec = totRec - mappingInfo.getHeader().getNumLines() - mappingInfo.getTrailor().getNumLines();
            if (totRec < 0) {
                log.warn("totRec(" + totRec + ") < 0");
                totRec = 0;
            }
        }
        catch (Exception ignore) {
            log.warn("computeTotRecByReadFile error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseIO(tmpbr);
        }
    }

    public void readHeaderAndTrailor() {
    	
        ArrayList header = new ArrayList();
        ArrayList trailor = new ArrayList();
        if (mappingInfo.getHeader().isHasHeader() || mappingInfo.getTrailor().isHasTrailor()) {
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
                FileInputStream fis = null;
                try {
                    
                    if(mappingInfo.getHeader().getNumLines() > 0)
                    {
                        header.add(ISOUtil.hexString(contentList.get(0)));//找出第一筆
                    }
                    if(mappingInfo.getTrailor().getNumLines() > 0)
                    {
                        trailor.add(ISOUtil.hexString(contentList.get(totRec-1)));//找出最後一筆
                    }
                    
                    if(mappingInfo.getHeader().getNumLines() > 0)
                    {
                        totRec = totRec - mappingInfo.getHeader().getNumLines();//總筆數減到header
                    }
                    
                    if(mappingInfo.getTrailor().getNumLines() > 0)
                    {
                        totRec = totRec - mappingInfo.getTrailor().getNumLines();//總筆數減到Trailor
                    }
                    
                   /* fis = new FileInputStream(file);
                    for (int i = 0; i < mappingInfo.getHeader().getNumLines(); i++) {
                        byte[] buff = new byte[fileInfo.getDataLen().intValue()];
                        int readLen = fis.read(buff);
                        header.add(new String(buff, 0, readLen, mappingInfo.getEncoding()));
                    }
                    log.debug("skip:"+ totRec);
                    fis.skip(totRec * fileInfo.getDataLen().intValue());
                    for (int i = 0; i < mappingInfo.getTrailor().getNumLines(); i++) {
                        byte[] buff = new byte[fileInfo.getDataLen().intValue()];
                        int readLen = fis.read(buff);
                        trailor.add(new String(buff, 0, readLen, mappingInfo.getEncoding()));
                    }*/
                }
                catch (Exception ignore) {
                    log.warn("readHeaderAndTrailor error:" + ignore.getMessage(), ignore);
                }
                finally {
                    ReleaseResource.releaseIO(fis);
                }
            }
            else {
                // 分隔符號的檔案
                BufferedReader tmpbr = null;
                try {
                    tmpbr = new BufferedReader(new InputStreamReader(new FileInputStream(file), mappingInfo.getEncoding()));
                    for (int i = 0; i < mappingInfo.getHeader().getNumLines(); i++) {
                        header.add(tmpbr.readLine());
                    }
                    for (int i = 0; i < totRec; i++) {
                        tmpbr.readLine();
                    }
                    for (int i = 0; i < mappingInfo.getTrailor().getNumLines(); i++) {
                        trailor.add(tmpbr.readLine());
                    }
                }
                catch (Exception ignore) {
                    log.warn("readHeaderAndTrailor error:" + ignore.getMessage(), ignore);
                }
                finally {
                    ReleaseResource.releaseIO(tmpbr);
                }
            }
        }
        setHeader(header);
        setTrailor(trailor);
    }

    public void openFile(int parserMode) throws IOException {
        // compute totRec
        // bug fix
        if (file.exists() && file.isFile()) {
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
            	totRec = 0;
            	if(parserMode == PARSER_NONE)
            	{
                	if(mappingInfo.getHeader().isHasHeader())
                	{
                	    handleCpcFile(file);//檢查0D0A與下個BYTE是否FF11
                	}
                	else
                	{
                	    handleTxnDtlFile(file);//檢查0D0A與下個BYTE是否FF11
                	}
            	}
            	else if(parserMode == PARSER_HEX)
            	{
            	    handleHexFile(file);
            	}
            	else if(parserMode == PARSER_TRN)
                {
            	    handleTrafficTxnFile(file);
                }
            	else
            	{
            	    handleFile(file);//只檢查0D0A
            	}
            	/*totRec = contentList.size();*/
            	totRec = contentList.size();
            }
            else {
                // 分隔符號的檔案
                computeTotRecByReadFile();
            }
            readHeaderAndTrailor();  
        }

//        if (StringUtil.isEmpty(mappingInfo.getSeparatorString()) && fileInputStream == null) {
//            try {
//                fileInputStream = new FileInputStream(file);
//            }
//            catch (Exception ignore) {
//                log.warn("openFile error:" + ignore.getMessage(), ignore);
//            }
//        }
//        else if (!StringUtil.isEmpty(mappingInfo.getSeparatorString()) && br == null) {
//            // 分隔符號的檔案
//            try {
//                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), mappingInfo.getEncoding()));
//            }
//            catch (Exception ignore) {
//                log.warn("openFile error:" + ignore.getMessage(), ignore);
//            }
//        }
        
//        try {
//            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), mappingInfo.getEncoding()));
//        }
//        catch (Exception ignore) {
//            log.warn("openFile error:" + ignore.getMessage(), ignore);
//        }
    }

    public void closeFile() {
        if (fileInputStream != null) {
            ReleaseResource.releaseIO(fileInputStream);
        }
//        if (br != null) {
//            ReleaseResource.releaseIO(br);
//        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        closeFile();
    }

    /**
     * <pre>
     * 依 fileInfo, inctlInfo property 來決定要抓那一個 File object
     * </pre>
     */
    public void locateFile() {
        if (file == null) {
        	check();
            final String MEMGROUPID_SPECIAL = "22222";
            final String MEMID_SPECIAL = "00000000";
            final String MERCHID_SPECIAL = "111111111111111";
            final String PERSO_FACTORY_RREMOTE = "PERSO_FACTORY";
            
            String workDir = "";
            String relativePath = fileInfo.getLocalPath();
            try {
                workDir = BatchUtil.getWorkDirectory();
            }
            catch (Exception ignore) {
                log.warn("should not happen:" + ignore.getMessage(), ignore);
            }
            workDir = pendingEndSep(workDir);
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

            relativePath = pendingEndSep(relativePath);
            String normalPath = FilenameUtils.normalize(workDir + relativePath + inctlInfo.getFullFileName());
            file = new File(normalPath);
        }
    }

    private List<TbPersoFactoryInfo> getTbPersoFactoryInfos(String persoFactoryId) throws SQLException
    {
    	Connection conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        
    	Vector<TbPersoFactoryInfo> result = new Vector<TbPersoFactoryInfo>();
        new TbPersoFactoryMgr(conn).queryMultiple("PERSO_FACTORY_ID = '"+persoFactoryId+"'", result);
        
        ReleaseResource.releaseDB(conn, null, null);
        
        return result;
    }

    /**
     * <pre>
     * 依 <em>rcode, content</em> 來 insert TB_INCTL_ERR
     * </pre>
     *
     * @param rcode   rcode
     * @param fieldInfo fieldInfo
     * @param content content
     * @throws Exception when occurs exceptions
     */
    private void insertInctErr(String rcode, FieldInfo fieldInfo, String content) throws Exception {
        Connection conn = null;
        ErrorDescInfo descInfo = null;
        TbInctlErrInfo inctlErrInfo = null;
        try {
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            descInfo = ImpFilesUtil.getErrorDescInfo(rcode, fieldInfo, content);
            inctlErrInfo = ImpFilesUtil.makeInctlErrInfo(inctlInfo, null, descInfo);
            ImpFilesUtil.insertInctlErr(conn, true, inctlErrInfo);
        }
        catch (Exception ignore) {
            log.warn("insertInctErr error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
        finally {
            ReleaseResource.releaseDB(conn);
        }
    }

    /**
     * <pre>
     * 檢查檔案是否存在, 檔案存在, return true,
     * 檔案不存在, insert TB_INCTL_ERR, return false
     * </pre>
     *
     * @return true/false
     * @throws Exception when occurs excpetions
     */
    public boolean checkFile2701() throws Exception {
        if (file.exists() && file.isFile()) {
            return true;
        }
        else {
            insertInctErr(Layer1Constants.RCODE_2701_NOTEXIST, null, file.getAbsolutePath());
            return false;
        }
    }

    /**
     * <pre>
     * 判斷檔案大小是否能整除單筆資料的長度, 可以整除, return true
     * 不可以整除, insert TB_INCTL_ERR, return false
     * </pre>
     *
     * @return true/false
     * @throws Exception when occurs excpetions
     */
    public boolean checkFile2702() throws Exception {
        if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
            if (inctlInfo.getFileSize().intValue() % fileInfo.getDataLen().intValue() == 0) {
                return true;
            }
            else {
                insertInctErr(Layer1Constants.RCODE_2702_DIVIDE_ERR, null, "" + inctlInfo.getFileSize().intValue());
                return false;
            }
        }
        else {
            // 分隔符號的檔案
            return true;
        }
    }

    /**
     * <pre>
     * 從檔案取得 custom header
     * List object, each element is String object
     * </pre>
     *
     * @return List object, each element is String object
     */
    private List getCustomLinesFromFile() {
        List lines = new ArrayList();
        if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
            try {
                for (int i = 0; i < mappingInfo.getHeader().getNumLines(); i++) {
                    byte[] buff = new byte[fileInfo.getDataLen().intValue()];
                    int readLen = fileInputStream.read(buff);
                    // back to original position
                    fileInputStream.skip(-readLen);
                    lines.add(new String(buff, 0, readLen, mappingInfo.getEncoding()));
                }
            }
            catch (Exception ignore) {
                log.warn("getCustomLinesFromFile error:" + ignore.getMessage(), ignore);
            }
        }
        else {
            // 分隔符號的檔案
//            try {
//                for (int i = 0; i < mappingInfo.getHeader().getNumLines(); i++) {
//                    lines.add(br.readLine());
//                }
//            }
//            catch (Exception ignore) {
//                log.warn("getCustomLinesFromFile error:" + ignore.getMessage(), ignore);
//            }
        }
        return lines;
    }

    private byte[] readOneLineBuff(boolean isBack) throws Exception {
        List buffs = readLinesBuff(1, isBack);
        return (byte[]) buffs.get(0);
    }

    private List readLinesBuff(int numLines, boolean isBack) throws Exception {
        List buffs = new ArrayList();
        int backLen = 0;
        for (int i = 0; i < numLines; i++) {
            byte[] buff = new byte[fileInfo.getDataLen().intValue()];
            int readLen = fileInputStream.read(buff);
            // compute total back length
            if (isBack && readLen >= 0) {
                backLen += readLen;
            }
            buffs.add(buff);
        }
        if (isBack) {
            // back to original position
            fileInputStream.skip(-backLen);
        }
        return buffs;
    }

    /**
     * <pre>
     * 依 mappingInfo, fileInfo, inctlInfo 檢查有沒有 Header Record,
     * 有, return true, 沒有, return false
     * </pre>
     *
     * @return true/false
     * @throws Exception when occurs exceptions
     */
    public boolean checkFile2703() throws Exception {
        if (mappingInfo.getHeader().isHasHeader() == false) {
            // no header, no check
            return true;
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader() == false) {
            // has header && no custom header
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
                byte[] buff = readOneLineBuff(true);
                final String HEADER_TAG = "H0";
                String headerTag = new String(buff, 0, 2, mappingInfo.getEncoding());
                if (HEADER_TAG.equals(headerTag)) {
                    // has header tag
                    return true;
                }
                else {
                    // no header
                    insertInctErr(Layer1Constants.RCODE_2703_NOHEADER, null, "");
                    return false;
                }
            }
            else {
                // 分隔符號的檔案
                return true;
            }
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
            // has header && custom header
            return true;
        }
        else {
            log.warn("checkFile2703 unknow case!");
            return true;
        }
    }

    /**
     * <pre>
     * 檢查 header record 是否重複, 沒有重複, return true
     * 有重複, insert TB_INCTL_ERR, return false
     * </pre>
     *
     * @return true/false
     * @throws Exception when occurs exceptions
     */
    public boolean checkFile2704() throws Exception {
        if (mappingInfo.getHeader().isHasHeader() == false) {
            // no header, no check
            return true;
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader() == false) {
            // has header && no custom header
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
                List buffs = readLinesBuff(2, true);
                byte[] buff1 = (byte[]) buffs.get(0);
                byte[] buff2 = (byte[]) buffs.get(1);
                final String HEADER_TAG = "H0";
                String headerTag1 = new String(buff1, 0, 2, mappingInfo.getEncoding());
                String headerTag2 = new String(buff2, 0, 2, mappingInfo.getEncoding());
                if (HEADER_TAG.equals(headerTag1) && HEADER_TAG.equals(headerTag2)) {
                    // 有重複 header
                    insertInctErr(Layer1Constants.RCODE_2704_REPHEADER, null, "");
                    return false;
                }
                else {
                    // 沒有重複 header
                    return true;
                }
            }
            else {
                // 分隔符號的檔案
                return true;
            }
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
            // has header && custom header, no check
            return true;
        }
        else {
            log.warn("checkFile2704 unknow case!");
            return true;
        }
    }

    /**
     * <pre>
     * 檢查有沒有 data record, 有, return true
     * 沒有, insert TB_INCTL_ERR, return false
     * </pre>
     *
     * @return true/false
     * @throws Exception when occurs exceptions
     */
    public boolean checkFile2705() throws Exception {
        if (!checkEmptyFile) {
            return true;
        }
        if (mappingInfo.getHeader().isHasHeader() == false) {
            if (file.length() > 0) {
                return true;
            }
            else {
                insertInctErr(Layer1Constants.RCODE_2705_NODATA, null, "");
                return false;
            }
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader() == false) {
            // has header && no custom header
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
                if ((file.length() - fileInfo.getDataLen().intValue()) > 0) {
                    return true;
                }
                else {
                    insertInctErr(Layer1Constants.RCODE_2705_NODATA, null, "");
                    return false;
                }
            }
            else {
                // 分隔符號的檔案
                return true;
            }
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
            // has header && custom header
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
                if ((file.length() - fileInfo.getDataLen().intValue() * mappingInfo.getHeader().getNumLines()) > 0) {
                    return true;
                }
                else {
                    insertInctErr(Layer1Constants.RCODE_2705_NODATA, null, "");
                    return false;
                }
            }
            else {
                // 分隔符號的檔案
                return true;
            }
        }
        else {
            log.warn("checkFile2705 unknow case!");
            return true;
        }
    }

    /**
     * <pre>
     * 檢查 header record 資料筆數正不正確, 正確 return true
     * 不正確, insert TB_INCTL_ERR, return false
     * </pre>
     *
     * @return true/false
     * @throws Exception when occurs exceptions
     */
    public boolean checkFile2706() throws Exception {
        if (mappingInfo.getHeader().isHasHeader() == false) {
            // no header, no check
            return true;
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader() == false) {
            // has header && no custom header
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
                byte[] buff = readOneLineBuff(true);
                // 去除掉 header line
                int numLines = (int) (file.length() / fileInfo.getDataLen().intValue()) - 1;
                // see filespec
                int hnumLines = -1;
                try {
                    String str = new String(buff, 10, 8, mappingInfo.getEncoding());
                    hnumLines = Integer.parseInt(str);
                }
                catch (Exception ignore) {
                    hnumLines = -1;
                }
                if (numLines == hnumLines) {
                    // match 資料筆數
                    return true;
                }
                else {
                    // no match 資料筆數
                    // for position and field property
                    FieldInfo fieldInfo = new FieldInfo();
                    fieldInfo.setStart(10);
                    fieldInfo.setDesc("資料筆數");
                    insertInctErr(Layer1Constants.RCODE_2706_DATANUM_ERR, fieldInfo, numLines + " != " + hnumLines);
                    return false;
                }
            }
            else {
                // 分隔符號的檔案
                return true;
            }
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
            // has header && custom header, no check
            return true;
        }
        else {
            log.warn("checkFile2706 unknow case!");
            return true;
        }
    }

    /**
     * <pre>
     * 檢查 header record 處理日期合不合法, 合法, return true
     * 不合法, return false
     * </pre>
     *
     * @return true/false
     * @throws Exception when occurs exceptions
     */
    public boolean checkFile2707() throws Exception {
        if (mappingInfo.getHeader().isHasHeader() == false) {
            // no header, no check
            return true;
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader() == false) {
            // has header && no custom header
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
                byte[] buff = readOneLineBuff(true);
                String fileDate = inctlInfo.getFileDate();
                // see filespec
                String hfileDate = new String(buff, 2, 8, mappingInfo.getEncoding());
                if (fileDate.equals(hfileDate)) {
                    // match 處理日期
                    return true;
                }
                else {
                    // no match 處理日期
                    // for position and field property
                    FieldInfo fieldInfo = new FieldInfo();
                    fieldInfo.setStart(2);
                    fieldInfo.setDesc("處理日期");
                    insertInctErr(Layer1Constants.RCODE_2707_DATADATE_ERR, fieldInfo, "");
                    return false;
                }
            }
            else {
                // 分隔符號的檔案
                return true;
            }
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
            // has header && custom header, no check
            return true;
        }
        else {
            log.warn("checkFile2707 unknow case!");
            return true;
        }
    }
    
    /**
     * <pre>
     * 檢查 header record 收單商與檔名是否相同, return true
     * 不合法, return false
     * </pre>
     *
     * @return true/false
     * @throws Exception when occurs exceptions
     */
    public boolean checkFile2715() throws Exception {
        if (mappingInfo.getHeader().isHasHeader() == false) {
            // no header, no check
            return true;
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader() == false) {
            // has header && no custom header
            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
                byte[] buff = readOneLineBuff(true);
                String fileFileNmae = inctlInfo.getFullFileName();
                String acqMemId = inctlInfo.getFullFileName().substring(0,8);
                // see filespec
                String hacqMemId = new String(buff, 18, 8, mappingInfo.getEncoding());
                this.acqMemId = acqMemId;
                if (acqMemId.equals(hacqMemId)) {
                    // match 收單商戶代碼
                    return true;
                }
                else {
                    // no match 收單商戶代碼
                    // for position and field property
                    FieldInfo fieldInfo = new FieldInfo();
                    fieldInfo.setStart(18);
                    fieldInfo.setDesc("收單商戶代碼");
                    insertInctErr(Layer1Constants.RCODE_2715_ACQMENID_ERR, fieldInfo, "");
                    return false;
                }
            }
            else {
                // 分隔符號的檔案
                return true;
            }
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
            // has header && custom header, no check
            return true;
        }
        else {
            log.warn("checkFile2715 unknow case!");
            return true;
        }
    }
    
    /**
     * <pre>
     * 檢查 file length 和 dataLen 是否一致,
     * 一致, return true, 不一致, return false
     * </pre>
     *
     * @return true/false
     */
    public boolean checkOthers() throws Exception {
//        if (file.length() != inctlInfo.getFileSize().intValue()) {
//            String msg = "fileSize(" + file.length() + ") != inctlInfo fileSize(" + inctlInfo.getFileSize().intValue() + ")";
//            log.warn(msg);
//            insertInctErr(Layer1Constants.RCODE_2713_SETTING1_ERR, null, msg);
//            return false;
//        }
//        if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
//            if (fileInfo.getDataLen().intValue() != mappingInfo.getDataLength()) {
//                String msg = "fileInfo dataLen(" + fileInfo.getDataLen().intValue() + ") != mappingInfo dataLen(" + mappingInfo.getDataLength() + ")";
//                log.warn(msg);
//                insertInctErr(Layer1Constants.RCODE_2714_SETTING2_ERR, null, msg);
//                return false;
//            }
//        }
        // check ok file
        if (isCheckOKFile()) {
            String fullname = file.getAbsolutePath();
            StringBuffer sb = new StringBuffer();
            sb.append(fullname.substring(0, fullname.lastIndexOf(inctlInfo.getFileName())));
            sb.append(inctlInfo.getFileName() + ".OK");
            sb.append(fullname.substring(fullname.lastIndexOf(inctlInfo.getFileName()) + +inctlInfo.getFileName().length()));
            File okFile1 = new File(sb.toString());
            File okFile2 = new File(fullname + ".OK");
            if (!okFile1.exists() && !okFile2.exists()) {
                log.warn("no OK file! okFile1(" + okFile1 + "), okFile2(" + okFile2 + ")");
                return false;
            }
        }
        return true;
    }

    /**
     * <pre>
     * 依 mappingInfo 設定 skip header
     * </pre>
     *
     * @throws Exception when occurs exceptions
     */
    private void skipHeader() throws Exception {
        if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
            int skip = 0;
            if (mappingInfo.getHeader().isHasHeader() == false) {
                // no header
                skip = 0;
            }
            else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader() == false) {
                // has header && no custom header
                /*skip = fileInfo.getDataLen().intValue();*/
                contentList.remove(0);//移除header
            }
            else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
                /*skip = fileInfo.getDataLen().intValue() * mappingInfo.getHeader().getNumLines();*/
                contentList.remove(0);//移除header
            }
            else {
                log.warn("skipHeader unknow case!");
            }
            /*fileInputStream.skip(skip);*/
        }
        else {
            // 分隔符號的檔案
            /*int skipLines = 0;
            if (mappingInfo.getHeader().isHasHeader() == false) {
                // no header
                skipLines = 0;
            }
            else {
                skipLines = mappingInfo.getHeader().getNumLines();
            }
            for (int i = 0; i < skipLines; i++) {
                br.readLine();
            }*/
        }
    }

    /**
     * <pre>
     * 檢查各種條件是否合法, 合法, retun true, 不合法, return false
     * </pre>
     *
     * @return when occurs exceptions
     */
    public boolean checkFile(int parserMode) {
        boolean ret = false;
        try {
            locateFile();
            // contain copmute totRec
            openFile(parserMode);
            boolean tmp = checkFile2701();
            if (tmp == false) {
                failCnt = totRec;
                return false;
            }
//            tmp = checkOthers();
//            if (tmp == false) {
//                failCnt = totRec;
//                return false;
//            }
//            tmp = checkFile2702();
//            if (tmp == false) {
//                failCnt = totRec;
//                return false;
//            }
//            tmp = checkFile2705();
//            if (tmp == false) {
//                failCnt = totRec;
//                return false;
//            }
//            tmp = checkFile2703();
//            if (tmp == false) {
//                failCnt = totRec;
//                return false;
//            }
//            tmp = checkFile2704();
//            if (tmp == false) {
//                failCnt = totRec;
//                return false;
//            }
//            tmp = checkFile2706();
//            if (tmp == false) {
//                failCnt = totRec;
//                return false;
//            }
//            tmp = checkFile2707();
//            if (tmp == false) {
//                failCnt = totRec;
//                return false;
//            }
//            tmp = checkFile2715();
//            if (tmp == false) {
//                failCnt = totRec;
//                return false;
//            }
                        
            // if checkFile OK, skip header
            skipHeader();
            // skip recCnt
//            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
//                fileInputStream.skip(recCnt * fileInfo.getDataLen().intValue());
//            }
//            else {
//                // 分隔符號的檔案
//                for (int i = 0; i < recCnt; i++) {
//                    br.readLine();
//                }
//            }
            ret = true;
        }
        catch (Exception ignore) {
            ret = false;
            log.warn("checkFile error:" + ignore.getMessage(), ignore);
        }
        return ret;
    }

    /**
     * <pre>
     * 依 mappingInfo 設定取得 lineNo
     * </pre>
     *
     * @return lineNo
     */
    private int getLineNo() {
        int lineNo = recCnt;
        if (mappingInfo.getHeader().isHasHeader() == false) {
            // no header
            lineNo = recCnt;
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader() == false) {
            // has header && no custom header
            lineNo = recCnt + 1;
        }
        else if (mappingInfo.getHeader().isHasHeader() && mappingInfo.getHeader().isHasCustHeader()) {
            lineNo = recCnt + mappingInfo.getHeader().getNumLines();
        }
        else {
            log.warn("getLineNo unknow case!");
        }
        return lineNo;
    }

    private int getDiv(int numFraction) {
        int ret = 1;
        for (int i = 0; i < numFraction; i++) {
            ret *= 10;
        }
        return ret;
    }
    
    public DataLineInfo readOneDataLine() throws Exception {
		DataLineInfo lineInfo = null;

		String record = ISOUtil.hexString(contentList.get(recCnt));
		if (recCnt < totRec) {
			recCnt++;
			try {
				lineInfo = new DataLineInfo();
				lineInfo.setMappingInfo(mappingInfo);
				lineInfo.setLineNo(getLineNo());

				// set plainLine
				lineInfo.setPlainLine(record);
				// set fieldDataSize
				lineInfo.setFieldDataSize(mappingInfo.getFields().size());
			} catch (Exception ignore) {
				lineInfo = null;
				log.warn("readOneDataLine error:" + ignore.getMessage(),ignore);
				throw ignore;
			}
		}

		return lineInfo;
	}
    
    public DataLineInfo readOneDataLineForFm() throws Exception {
        DataLineInfo lineInfo = null;

        String record = new String(contentList.get(recCnt));
        if (recCnt < totRec) {
            recCnt++;
            try {
                lineInfo = new DataLineInfo();
                lineInfo.setMappingInfo(mappingInfo);
                lineInfo.setLineNo(getLineNo());

                // set plainLine
                lineInfo.setPlainLine(record);
                // set fieldDataSize
                lineInfo.setFieldDataSize(mappingInfo.getFields().size());
            } catch (Exception ignore) {
                lineInfo = null;
                log.warn("readOneDataLine error:" + ignore.getMessage(),ignore);
                throw ignore;
            }
        }

        return lineInfo;
    }
    
    /**
     * <pre>
     * 若此檔案還有資料可抓, 依 mappingInfo 設定來抓檔案一筆資料,
     * 成功, return DataLineInfo object, 不成功, return null
     * 或 throw exception
     * </pre>
     *
     * @return DataLineInfo object
     * @throws Exception when occurs exceptions
     */
//    public DataLineInfo readOneDataLine() throws Exception {
//        DataLineInfo lineInfo = null;
//        if (recCnt < totRec) {
//            recCnt++;
//            if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
//                try {
//                    lineInfo = new DataLineInfo();
//                    lineInfo.setMappingInfo(mappingInfo);
//                    lineInfo.setLineNo(getLineNo());
//                    byte[] buff = readOneLineBuff(false);
//                    StringBuffer plainLine = new StringBuffer();
//                    for (int i = 0; i < mappingInfo.getFields().size(); i++) {
//                        FieldInfo fieldInfo = (FieldInfo) mappingInfo.getFields().get(i);
//                        if (FieldInfo.TYPE_STRING.equals(fieldInfo.getType())) {
//                            // type is string
//                            String data = new String(buff, fieldInfo.getStart(), fieldInfo.getLength(), fieldInfo.getEncoding());
//                            plainLine.append(data);
//                            if (fieldInfo.isTrim()) {
//                                // trim
//                                data = data.trim();
//                            }
//                            data = handleIgnoreAndDefault(data, fieldInfo);
//                            lineInfo.addFieldData(fieldInfo.getName(), data);
//                        }
//                        else if (FieldInfo.TYPE_NUMBER.equals(fieldInfo.getType())) {
//                            // type is number
//                            String data = new String(buff, fieldInfo.getStart(), fieldInfo.getLength(), fieldInfo.getEncoding());
//                            plainLine.append(data);
//                            Number n = null;
//                            try {
//                                int d = getDiv(fieldInfo.getNumFraction());
//                                double value = Double.parseDouble(data) / d;
//                                n = new Double(value);
//                            }
//                            catch (Exception ignore) {
//                                n = null;
//                                log.warn("convert to Number error:" + data, ignore);
//                            }
//                            // 處理 defaultValue
//                            if (n == null) {
//                                try {
//                                    n = Double.parseDouble(fieldInfo.getDefaultValue());
//                                }
//                                catch (Exception ignore) {
//                                    n = null;
//                                }
//                            }
//                            lineInfo.addFieldData(fieldInfo.getName(), n);
//                        }
//                    }
//                    // set plainLine
//                    lineInfo.setPlainLine(plainLine.toString());
//                    // set fieldDataSize
//                    lineInfo.setFieldDataSize(mappingInfo.getFields().size());
//                }
//                catch (Exception ignore) {
//                    lineInfo = null;
//                    log.warn("readOneDataLine error:" + ignore.getMessage(), ignore);
//                    throw ignore;
//                }
//            }
//            else {
//                // 分隔符號的檔案
//                try {
//                    lineInfo = new DataLineInfo();
//                    lineInfo.setMappingInfo(mappingInfo);
//                    lineInfo.setLineNo(getLineNo());
//                    String plainLine = br.readLine();
//                    if (plainLine == null) {
//                        plainLine = "";
//                    }
//                    lineInfo.setPlainLine(plainLine);
//                    if (plainLine.endsWith(mappingInfo.getSeparatorString())) {
//                        // 避免 "a#b#c#" 少一個 token
//                        plainLine += " ";
//                    }
//                    String[] tmpTokens = plainLine.split(mappingInfo.getConvertedSeparatorString());
//                    String[] tokens = new String[mappingInfo.getFields().size()];
//                    Arrays.fill(tokens, 0, tokens.length, "");
//                    if (tmpTokens.length != tokens.length) {
//                        log.info("tmpTokens.length(" + tmpTokens.length + ") != tokens.length(" + tokens.length + ")");
//                    }
//                    // set fieldDataSize
//                    lineInfo.setFieldDataSize(tmpTokens.length);
//                    int tmpLength = Math.min(tmpTokens.length, tokens.length);
//                    for (int i = 0; i < tmpLength; i++) {
//                        tokens[i] = tmpTokens[i];
//                    }
//                    for (int i = 0; i < mappingInfo.getFields().size(); i++) {
//                        FieldInfo fieldInfo = (FieldInfo) mappingInfo.getFields().get(i);
//                        if (FieldInfo.TYPE_STRING.equals(fieldInfo.getType())) {
//                            // type is string
//                            String data = tokens[i];
//                            if (fieldInfo.isTrim()) {
//                                // trim
//                                data = data.trim();
//                            }
//                            data = handleIgnoreAndDefault(data, fieldInfo);
//                            lineInfo.addFieldData(fieldInfo.getName(), data);
//                        }
//                        else if (FieldInfo.TYPE_NUMBER.equals(fieldInfo.getType())) {
//                            // type is number
//                            String data = tokens[i];
//                            Number n = null;
//                            try {
//                                int d = getDiv(fieldInfo.getNumFraction());
//                                double value = Double.parseDouble(data) / d;
//                                n = new Double(value);
//                            }
//                            catch (Exception ignore) {
//                                n = null;
//                                log.warn("convert to Number error:" + data, ignore);
//                            }
//                            // 處理 defaultValue
//                            if (n == null) {
//                                try {
//                                    n = Double.parseDouble(fieldInfo.getDefaultValue());
//                                }
//                                catch (Exception ignore) {
//                                    n = null;
//                                }
//                            }
//                            lineInfo.addFieldData(fieldInfo.getName(), n);
//                        }
//                    }
//                }
//                catch (Exception ignore) {
//                    lineInfo = null;
//                    log.warn("readOneDataLine error:" + ignore.getMessage(), ignore);
//                    throw ignore;
//                }
//            }
//        }
//        return lineInfo;
//    }

    private String handleIgnoreAndDefault(String data, FieldInfo fieldInfo) {
        String ret = "";
        // handle ignoreChar
        boolean allIgnoreChar = false;
        if (!StringUtil.isEmpty(data)) {
            int ignoreCharCount = 0;
            for (int i = 0; i < data.length(); i++) {
                if (("" + data.charAt(i)).equals(mappingInfo.getIgnoreChar())) {
                    ignoreCharCount++;
                }
            }
            if (ignoreCharCount == data.length()) {
                allIgnoreChar = true;
            }
            else {
                allIgnoreChar = false;
            }
        }
        else {
            allIgnoreChar = true;
        }
        if (allIgnoreChar) {
            ret = "";
        }
        else {
            ret = data;
        }
        // 處理 defaultValue
        if (StringUtil.isEmpty(ret)) {
            ret = fieldInfo.getDefaultValue();
        }
        return ret;
    }
    
    public String getAcqMemId()
    {
        return this.acqMemId;
    }
    
    private void handleHexFile(File file) throws IOException{
        
//        BufferedInputStream bis = null;
//        DataInputStream dis = null;
    	BufferedReader dis = null;
        try {
//            bis = new BufferedInputStream(new FileInputStream(file));
//            dis = new DataInputStream(bis);
            dis = new BufferedReader(new FileReader(file));
            String record;
            while(dis.ready()){
                record = dis.readLine();
                if(!StringUtil.isEmpty(record))
                {
                    record = record.trim();
                    contentList.add(record.getBytes());
                }
            }
        }
        catch (Exception ignore) {
            log.warn(ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseIO(dis);
//            if(null != dis)
//            {
//                dis.close();
//            }
        }
    }
    
    private void handleFile(File file){
    	    	
    	BufferedInputStream bis = null;
        byte[] buffer = new byte[1024];
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytesRead = 0;
            int byte2 = 0;
            int idx=0;
            byte[] lineByte=null;
            while(bis.available()>0){
            	bytesRead = bis.read();
            	buffer[idx]=(byte)bytesRead;
            	idx++;
            	if (bytesRead==0x0D){
            		idx = chkln(bis, buffer, idx);
            	}
            }
        }
        catch (Exception ignore) {
            log.warn("readHeaderAndTrailor error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseIO(bis);
        }
    }

	private int chkln(BufferedInputStream bis, byte[] buffer, int idx) throws IOException {
		int byte2;
		byte[] lineByte;
		
		bis.mark(0);
		
		byte2 = bis.read();
		buffer[idx]=(byte)byte2;
		idx++;
		if (byte2==0x0A){//以0D,0A來決定是否換行
			lineByte = new byte[idx-2];
			System.arraycopy(buffer, 0, lineByte, 0, lineByte.length);
			/*log.debug(ISOUtil.hexString(lineByte));*/
			//清空buffer byte
			System.arraycopy(new byte[idx], 0, buffer, 0, idx);
			idx=0;
			//處理一個交易
			if (lineByte.length>2){
				contentList.add(lineByte);
				/*log.debug(contentList.size());*/
			}else{
				log.warn("skip empty line.");
			}
		}else{//退回
			bis.reset();
			idx--;
		}
		return idx;
	}
	
	private void handleCpcFile(File file){
        int bufferSize = 1024;
        BufferedInputStream bis = null;
        byte[] buffer = new byte[bufferSize];
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytesRead = 0;
            int byte2 = 0;
            int idx=0;
            byte[] lineByte=null;
            int recordType = 1;
            int endByteCnt = 0;
            boolean isRecrdEnd = false;
            
            while(bis.available()>0){
                bytesRead = bis.read();
                buffer[idx]=(byte)bytesRead;
                
                if(idx == 0 && bytesRead == 0x48)
                {
                    recordType = 1;//header
                }
                else if(idx == 0 && bytesRead == 0x44)
                {
                    recordType = 0;//detail
                }
                else if(idx == 0 && bytesRead == 0x54)
                {
                    recordType = 2;//tralar
                }
                
                idx++;
                
                if (endByteCnt == 0 && bytesRead == 0x0D)
                {
                    endByteCnt = 1;
                    bis.mark(0);
                }
                else if (endByteCnt == 1 && bytesRead == 0x0A)
                {
                    endByteCnt = 0;
                    if(recordType > 0)
                    {
                        isRecrdEnd = true;
                    }
                    else
                    {
                        bis.mark(4);
                        //check next record header is DO1
                        if(bis.available() > 0)
                        {
                            bytesRead = bis.read();
                            if(bytesRead == 0x44)//D
                            {
                                if(bis.available() > 0)
                                {
                                    bytesRead = bis.read();
                                    if(bytesRead == 0x30)//0
                                    {
                                        if(bis.available() > 0)
                                        {
                                            bytesRead = bis.read();
                                            if(bytesRead == 0x31)//1
                                            {
                                                isRecrdEnd = true;
                                                bis.reset();
                                            }
                                        }
                                    }
                                }
                            }
                            else if(bytesRead == 0x54)//T00
                            {
                                if(bis.available() > 0)
                                {
                                    bytesRead = bis.read();
                                    //logger.debug(Integer.toHexString(readByte));
                                    if(bytesRead == 0x30)//0
                                    {
                                        if(bis.available() > 0)
                                        {
                                            bytesRead = bis.read();
                                            //logger.debug(Integer.toHexString(readByte));
                                            if(bytesRead == 0x30)//1
                                            {
                                                isRecrdEnd = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if(isRecrdEnd == false)
                        {
                          //maybe ff64 data
                        }
                        bis.reset();
                    }
                    
                    if(isRecrdEnd)
                    {
                        isRecrdEnd = false;
                        lineByte = new byte[idx-2];
                        System.arraycopy(buffer, 0, lineByte, 0, lineByte.length);
                        /*log.debug(ISOUtil.hexString(lineByte));*/
                        //清空buffer byte
                        System.arraycopy(new byte[bufferSize], 0, buffer, 0, bufferSize);
                        idx=0;
                        //處理一個交易
                        if (lineByte.length>2){
                            contentList.add(lineByte);
                            /*log.debug(contentList.size());*/
                        }else{
                            log.warn("skip empty line.");
                        }
                    }
                }
                else
                {
                    if(endByteCnt == 1)
                    {
                        bis.reset();
                        idx--;
                    }
                    endByteCnt = 0;
                }
            }
        }
        catch (Exception ignore) {
            log.warn("readHeaderAndTrailor error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseIO(bis);
        }
    }
	
	private void handleTxnDtlFile(File file){
	    int bufferSize = 1024;
        BufferedInputStream bis = null;
        byte[] buffer = new byte[bufferSize];
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytesRead = 0;
            int idx=0;
            byte[] lineByte=null;
            int recordType = 1;
            int endByteCnt = 0;
            boolean isRecrdEnd = false;
            
            while(bis.available()>0){
                bytesRead = bis.read();
                buffer[idx]=(byte)bytesRead;
                idx++;
                
                if (endByteCnt == 0 && bytesRead == 0x0D)
                {
                    endByteCnt = 1;
                    bis.mark(0);
                }
                else if (endByteCnt == 1 && bytesRead == 0x0A)
                {
                    endByteCnt = 0;
                    bis.mark(2);
                    //check next record header is FF11
                    if(bis.available() > 0)
                    {
                        bytesRead = bis.read();
                        if(bytesRead == 0xFF)//FF
                        {
                            if(bis.available() > 0)
                            {
                                bytesRead = bis.read();
                                if(bytesRead == 0x11)//11
                                {
                                    isRecrdEnd = true;
                                    bis.reset();
                                }
                            }
                        }
                    }
                    else
                    {
                        isRecrdEnd = true;//最後一筆
                    }
                    bis.reset();
                    
                    if(isRecrdEnd)
                    {
                        isRecrdEnd = false;
                        lineByte = new byte[idx-2];
                        System.arraycopy(buffer, 0, lineByte, 0, lineByte.length);
                        /*log.debug(ISOUtil.hexString(lineByte));*/
                        //清空buffer byte
                        System.arraycopy(new byte[bufferSize], 0, buffer, 0, bufferSize);
                        idx=0;
                        //處理一個交易
                        if (lineByte.length>2){
                            contentList.add(lineByte);
                            /*log.debug(contentList.size());*/
                        }else{
                            log.warn("skip empty line.");
                        }
                    }
                }
                else
                {
                    if(endByteCnt == 1)
                    {
                        bis.reset();
                        idx--;
                    }
                    endByteCnt = 0;
                }
            }
        }
        catch (Exception ignore) {
            log.warn("readHeaderAndTrailor error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseIO(bis);
        }
    }
	
	/*private void handleTrafficTxnFile(File file)
	{
        BufferedInputStream bis = null;
        byte[] buffer = new byte[2048];
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytesRead = 0;
            int idx=0;
            byte[] lineByte=null;
            int endByteCnt = 0;
            boolean isRecrdEnd = false;
            int recordCnt = 0;
            
            while(bis.available()>0){
                bytesRead = bis.read();
                buffer[idx]=(byte)bytesRead;
                idx++;          
                if (endByteCnt == 0 && bytesRead == 0x0D)
                {
                    endByteCnt = 1;
                    bis.mark(1);
                }
                else if (endByteCnt == 1 && bytesRead == 0x0A)
                {
                    endByteCnt = 0;
                    
                    if(bis.available() > 0)
                    {
                        if(recordCnt == 0)
                        {
                            log.debug(idx);
                            log.debug(fileInfo.getDataLen().intValue() - 228);
                            if(idx == (fileInfo.getDataLen().intValue() - 228))//header 總長 (長度含0D0A)
                            {
                                isRecrdEnd = true;
                            }
                            else {
                                endByteCnt = 0;//長度不足
                                isRecrdEnd = false;
                            }
                        }
                        else {
                            log.debug("idx:"+ idx);
                            log.debug(fileInfo);
                            if(idx == (fileInfo.getDataLen().intValue()))//detail 總長 (長度含0D0A)
                            {
                                isRecrdEnd = true;
                            }
                            else {
                                endByteCnt = 0;//長度不足
                                isRecrdEnd = false;
                            }
                        }
                    }
                    else//read eof
                    {
                        isRecrdEnd = true;//最後一筆
                    }
                    //bis.reset();
                    
                    if(isRecrdEnd)
                    {
                        recordCnt++;
                        isRecrdEnd = false;
                        lineByte = new byte[idx-2];
                        System.arraycopy(buffer, 0, lineByte, 0, lineByte.length);
                        log.debug(ISOUtil.hexString(lineByte));
                        idx=0;
                        //清空buffer byte
                        System.arraycopy(new byte[idx], 0, buffer, 0, idx);
                        //處理一個交易
                        if (lineByte.length>2){
                            contentList.add(lineByte);
                            //log.debug("idx:"+ ISOUtil.hexString(lineByte));
                            log.debug(contentList.size());
                        }else{
                            log.warn("skip empty line.");
                        }
                    }
                    else{
                    	log.warn("isRecrdEnd: " + isRecrdEnd);
                    }
                }
                else
                {
                    if(endByteCnt == 1)
                    {
                        endByteCnt = 0;
                        bis.reset();
                        idx--;
                    }
                }
            }
        }
        catch (Exception ignore) {
            log.warn("readHeaderAndTrailor error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseIO(bis);
        }
    }*/
	
	private void handleTrafficTxnFile(File file)
    {
	    int bufferSize = 2048;
        BufferedInputStream bis = null;
        byte[] buffer = new byte[bufferSize];
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytesRead = 0;
            int idx=0;
            byte[] lineByte=null;
            boolean isRecrdEnd = false;
            boolean isHeader = true;
            
            while(bis.available()>0){
                
                bytesRead = bis.read();
                
                if(idx > bufferSize - 1)
                {
                    log.error("read record size[" + idx + "] > 2048");
                    break;
                }
                buffer[idx]=(byte)bytesRead;
                idx++;     
               
                if (isHeader)
                {
                    if(idx == 68)//header 總長 (長度含0D0A)
                    {
                        isRecrdEnd = true;
                        isHeader = false;
                    }
                }
                else {
                    
                    if(idx == fileInfo.getDataLen().intValue())//detail 總長 (長度含0D0A)
                    {
                        isRecrdEnd = true;
                    }
                }
                    
                if(isRecrdEnd)
                {
                    isRecrdEnd = false;
                    lineByte = new byte[idx-2];
                    System.arraycopy(buffer, 0, lineByte, 0, lineByte.length);
//                    log.debug(ISOUtil.hexString(lineByte));
                    
                    //清空buffer byte
                    System.arraycopy(new byte[bufferSize], 0, buffer, 0, bufferSize);
                    idx=0;
                    //處理一個交易
                    if (lineByte.length>2){
                        contentList.add(lineByte);
                        //log.debug("idx:"+ ISOUtil.hexString(lineByte));
//                        log.debug(contentList.size());
                    }else{
                        log.warn("skip empty line.");
                    }
                }
            }
        }
        catch (Exception ignore) {
            log.warn("readHeaderAndTrailor error:" + ignore.getMessage(), ignore);
        }
        finally {
            ReleaseResource.releaseIO(bis);
        }
    }
}
