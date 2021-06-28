package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpCardVR;

import tw.com.hyweb.service.db.info.TbFileInfoInfo;

import java.io.Serializable;
import java.io.File;

/**
 * <pre>
 * ExpFileInfo javabean
 * must be set
 * key: $memId:$fileName:fileDate:seqno
 * memId:String
 * fileName:String
 * fileDate:String
 * seqno:String
 * fullFileName:String
 * selectSQL:String
 * check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
 * checkFlag:boolean
 * fileEncoding:String
 * usingTempFile, 是否使用 temp file, default true
 * usingTempFile:boolean
 * 若用 temp file, 預設先 pending 的字串, default ".TMP"
 * tempFilePending:String
 * expTempFullFileName:String
 * expFullFileName:String
 * expFile:File
 * recordsPerFlush:int
 * recordsPerFile:int
 * lineSeparator:String
 * </pre>
 * author: duncan
 */
public class ExpFileInfo implements Serializable {
    // must be set
    // key: $memId:$fileName:fileDate:seqno
	private String memGroupId = "";
    private String memId = "";
    private String merchId = "";
    private String fileName = "";
    private String fileDate = "";
    private String seqno = "";
    private String fullFileName = "";
    private String selectSQL = "";
    // 若有給 recordsPerFile 時, 要給 seqnoStart, seqnoEnd
    private int seqnoStart = -1;
    private int seqnoEnd = -1;

    // do not set, set by ExpFileSetting
    // checkFlag
    // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
    private boolean checkFlag = true;
    private String fileEncoding = "UTF-8";
    // usingTempFile, 是否使用 temp file, default true
    private boolean usingTempFile = true;
    // 若用 temp file, 預設先 pending 的字串, default ".TMP"
    private String tempFilePending = ".TMP";
    private String expTempFullFileName = "";
    private String expFullFileName = "";
    private File expFile = null;
    private File expTempFile = null;
    // 處理多少筆做一次 flush 動作, default 不做(<= 0)
    private int recordsPerFlush = -1;
    // 多少筆產生一個 file, default 全部一個檔案(<= 0)
    private int recordsPerFile = -1;
    // lineSeparator
    private String lineSeparator = System.getProperty("line.separator", "\n");
    private TbFileInfoInfo fileInfo = null;
    
    private String inFileName = "";       //匯入檔案(IFF 檔案)
    private String purchaseOrderNo = "";  //採購單號
    
    
    public ExpFileInfo() {
    }

    public int getSeqnoEnd() {
        return seqnoEnd;
    }

    public void setSeqnoEnd(int seqnoEnd) {
        this.seqnoEnd = seqnoEnd;
    }

    public int getSeqnoStart() {
        return seqnoStart;
    }

    public void setSeqnoStart(int seqnoStart) {
        this.seqnoStart = seqnoStart;
    }

    public String getKey() {
        StringBuffer sb = new StringBuffer();
        sb.append(memId);
        sb.append(":");
        sb.append(fileName);
        sb.append(":");
        sb.append(fileDate);
        sb.append(":");
        sb.append(seqno);
        return sb.toString();
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

    public File getExpFile() {
        return expFile;
    }

    public void setExpFile(File expFile) {
        this.expFile = expFile;
    }

    public File getExpTempFile() {
        return expTempFile;
    }

    public void setExpTempFile(File expTempFile) {
        this.expTempFile = expTempFile;
    }

    public String getExpFullFileName() {
        return expFullFileName;
    }

    public void setExpFullFileName(String expFullFileName) {
        this.expFullFileName = expFullFileName;
    }

    public String getExpTempFullFileName() {
        return expTempFullFileName;
    }

    public void setExpTempFullFileName(String expTempFullFileName) {
        this.expTempFullFileName = expTempFullFileName;
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFullFileName() {
        return fullFileName;
    }

    public void setFullFileName(String fullFileName) {
        this.fullFileName = fullFileName;
    }

    public String getMemGroupId() {
		return memGroupId;
	}

	public void setMemGroupId(String memGroupId) {
		this.memGroupId = memGroupId;
	}

	public String getMerchId() {
		return merchId;
	}

	public void setMerchId(String merchId) {
		this.merchId = merchId;
	}

	public String getMemId() {
        return memId;
    }

    public void setMemId(String memId) {
        this.memId = memId;
    }

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

    public String getSeqno() {
        return seqno;
    }

    public void setSeqno(String seqno) {
        this.seqno = seqno;
    }

    public String getSelectSQL() {
        return selectSQL;
    }

    public void setSelectSQL(String selectSQL) {
        if (selectSQL == null || "".equals(selectSQL)) {
            return;
        }
        // 只接受 select sql command
        String tmp = selectSQL.trim().toUpperCase();
        if (!tmp.startsWith("SELECT")) {
            return;
        }
        this.selectSQL = selectSQL;
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

    public TbFileInfoInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(TbFileInfoInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
    
	public String getInFileName() {
		return inFileName;
	}
	
	public void setInFileName(String inFileName) {
		this.inFileName = inFileName;
	}
	
	public String getPurchaseOrderNo() {
		return purchaseOrderNo;
	}

	public void setPurchaseOrderNo(String purchaseOrderNo) {
		this.purchaseOrderNo = purchaseOrderNo;
	}

	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExpFileInfo that = (ExpFileInfo) o;

        if (fileDate != null ? !fileDate.equals(that.fileDate) : that.fileDate != null) {
            return false;
        }
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) {
            return false;
        }
        if (memId != null ? !memId.equals(that.memId) : that.memId != null) {
            return false;
        }
        if (seqno != null ? !seqno.equals(that.seqno) : that.seqno != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (memId != null ? memId.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (fileDate != null ? fileDate.hashCode() : 0);
        result = 31 * result + (seqno != null ? seqno.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ExpFileInfo: ");
        sb.append(" memGroupId:" + memGroupId);
        sb.append(" memId:" + memId);
        sb.append(" merchId:" + merchId);
        sb.append(" fileName:" + fileName);
        sb.append(" fileDate:" + fileDate);
        sb.append(" seqno:" + seqno);
        sb.append(" seqnoStart:" + seqnoStart);
        sb.append(" seqnoEnd:" + seqnoEnd);
        sb.append(" fullFileName:" + fullFileName);
        sb.append(" selectSQL:" + selectSQL);
        sb.append(" checkFlag:" + checkFlag);
        sb.append(" fileEncoding:" + fileEncoding);
        sb.append(" usingTempFile:" + usingTempFile);
        sb.append(" tempFilePending:" + tempFilePending);
        sb.append(" expTempFullFileName:" + expTempFullFileName);
        sb.append(" expFullFileName:" + expFullFileName);
        sb.append(" expFile:" + expFile);
        sb.append(" expTempFile:" + expTempFile);
        sb.append(" recordsPerFlush:" + recordsPerFlush);
        sb.append(" recordsPerFile:" + recordsPerFile);
        sb.append(" inFileName:" + inFileName);
        sb.append(" purchaseOrderNo:" + purchaseOrderNo);
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
        sb.append(" fileInfo:" + fileInfo);
        sb.append("]");
        return sb.toString();
    }
}
