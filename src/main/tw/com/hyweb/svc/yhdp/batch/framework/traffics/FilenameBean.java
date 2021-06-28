package tw.com.hyweb.svc.yhdp.batch.framework.traffics;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbZipLogInfo;
import tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTrn.ExpTrn;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * FilenameBean javabean
 * fileName:String
 * memIdStart:int
 * memIdEnd:int
 * fileDateStart:int
 * fileDateEnd:int
 * seqnoStart:int
 * seqnoEnd:int
 * </pre>
 * author: duncan
 */
public abstract class FilenameBean implements Serializable
{
	private static Logger log = Logger.getLogger(FilenameBean.class);
	private String separatorString = "";
	
	private String batchDate = "";

	private String fileNameZip = "";
    private String fileName = "";
    private String fileNameR = "";
    private String fileNameRZip = "";
    
    private String fullFileNameZip = "";
    private String fullFileName = "";
    private String fullFileNameR = "";
    private String fullFileNameRZip = "";
    
    protected String seqno = "";
    protected String memGroupId = "";
    protected String memId = "";
    protected String fileDate = "";
    
    private HashMap memId2expSeqno = new HashMap();
    
    protected String expSeqno = "";
    
    private List memIds = null;

    public FilenameBean()
    {
    }

    public String getSeparatorString() {
		return separatorString;
	}
	public void setSeparatorString(String separatorString) {
		this.separatorString = separatorString;
	}
	public String getFileNameZip() {
		return fileNameZip;
	}
	public void setFileNameZip(String fileNameZip) {
		this.fileNameZip = fileNameZip;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileNameR() {
		return fileNameR;
	}
	public void setFileNameR(String fileNameR) {
		this.fileNameR = fileNameR;
	}
	public String getFileNameRZip() {
		return fileNameRZip;
	}
	public void setFileNameRZip(String fileNameRZip) {
		this.fileNameRZip = fileNameRZip;
	}
	public String getFullFileNameZip() {
		return fullFileNameZip;
	}
	public void setFullFileNameZip(String fullFileNameZip) {
		this.fullFileNameZip = fullFileNameZip;
	}
	public String getFullFileName() {
		return fullFileName;
	}
	public void setFullFileName(String fullFileName) {
		this.fullFileName = fullFileName;
	}
	public String getFullFileNameR() {
		return fullFileNameR;
	}
	public void setFullFileNameR(String fullFileNameR) {
		this.fullFileNameR = fullFileNameR;
	}
	public String getFullFileNameRZip() {
		return fullFileNameRZip;
	}
	public void setFullFileNameRZip(String fullFileNameRZip) {
		this.fullFileNameRZip = fullFileNameRZip;
	}
	public String getSeqno() {
		return seqno;
	}
	public void setSeqno(String seqno) {
		this.seqno = seqno;
	}
	public String getMemGroupId() {
		return memGroupId;
	}
	public void setMemGroupId(String memGroupId) {
		this.memGroupId = memGroupId;
	}
	public String getMemId() {
		return memId;
	}
	public void setMemId(String memId) {
		this.memId = memId;
	}
	public String getFileDate() {
		return fileDate;
	}
	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}
	public List getMemIds() {
		return memIds;
	}
	public void setMemIds(List memIds) {
		this.memIds = memIds;
	}
	public String getExpSeqno() {
		return expSeqno;
	}
	public void setExpSeqno(String expSeqno) {
		this.expSeqno = expSeqno;
	}
	public HashMap getMemId2expSeqno() {
		return memId2expSeqno;
	}
	public void setMemId2expSeqno(HashMap memId2expSeqno) {
		this.memId2expSeqno = memId2expSeqno;
	}
	public String getBatchDate() {
		return batchDate;
	}
	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}

	abstract public void initial ( String fullFileName, HashMap TbMembers ) throws Exception;
	
	abstract public void initialZip ( String zipFullFileName, HashMap TbMembers ) throws Exception;
    
    abstract public void clear ( );
    
    abstract public String getFullFileNameRZip(TbZipLogInfo zipLogInfo);
    
    abstract public String getFullFileNameR(TbZipLogInfo zipLogInfo);
    
    
    public String getOutctlSeqno() throws SQLException {
		
    	String expSeqno = "01";
    	
    	if ( memId2expSeqno.get(getMemId()) == null ){
    		Connection conn = null;
        	Statement stmt = null;
            ResultSet rs = null;
            StringBuffer sql = new StringBuffer();

            String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
    				"WHERE MEM_ID = " +  StringUtil.toSqlValueWithSQuote(getMemId()) +
              		" AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(getFileNameR()) + 
              		" AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(getBatchDate());
    		try {
    			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    			stmt = conn.createStatement();
    			log.warn("seqnoSql: "+seqnoSql);
    			rs = stmt.executeQuery(seqnoSql);
    			while (rs.next()) {
    				expSeqno = rs.getString(1);
    			}
    			memId2expSeqno.put(getMemId(), expSeqno);
    		}
        	finally {
        		ReleaseResource.releaseDB(conn, stmt, rs);
        	}
    	}
    	else{
    		int nextSeqNo = Integer.parseInt(memId2expSeqno.get(getMemId()).toString()) + 1;
    		expSeqno = StringUtil.pendingKey(nextSeqNo, 2);
    		memId2expSeqno.put(getMemId(), expSeqno);
    	}
    	
		return expSeqno;
	}
    
	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[FilenameBean: ");
        sb.append(" fileName:" + fileName);
        sb.append(" fileNameR:" + fileNameR);
        sb.append(" separatorString:" + separatorString);
        sb.append(" seqno:" + seqno);
        sb.append(" memGroupId:" + memGroupId);
        sb.append(" memId:" + memId);
        sb.append(" fullFileNameR:" + fullFileNameR);
        sb.append(" memIds:" + memIds);
        sb.append("]");
        return sb.toString();
    }
}
