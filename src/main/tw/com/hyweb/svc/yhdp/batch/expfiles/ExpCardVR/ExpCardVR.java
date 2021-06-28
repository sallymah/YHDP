package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpCardVR;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.service.db.DBService;

import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ExpCardVR extends AbstractExpFile {
	private static Logger log = Logger.getLogger(ExpCardVR.class);
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
            "expfiles" + File.separator + "ExpCardVR" + File.separator + "spring.xml";
    private static final String CVERP_FN = "CVERP";
    private static final String CARD_IN_APPEND = "CARD_IN";

    private String encoding = "";
    private String memId = "";
    private List memIds = null;
    private HashMap memId2Seqno = new HashMap();
    
    private String lastPackingNo = " ";
    private String lastBoxNo = " ";
    private String lastPackageNo = " ";
    
    private Integer cntPackingNo = 0;
    private Integer cntBoxNo = 0;
    private Integer cntPackageNo = 0;
    private Integer cntCardNo = 0;
	
	public ExpCardVR() {}

	
	public String getEncoding() 
	{
		return encoding;
	}

	public void setEncoding(String encoding) 
	{
		try {
            "".getBytes(encoding);
        }
        catch (Exception ignore) {
            // invalid encoding
            encoding = "UTF-8";
        }
		this.encoding = encoding;
	}

	public List getMemIds() 
	{
		return memIds;
	}

	public void setMemIds(List memIds) 
	{
		this.memIds = memIds;
	}

	public String getMemId() 
	{
		return memId;
	}

	public void setMemId(String memId) 
	{
		this.memId = memId;
	}

	private void beforeHandling() throws SQLException 
	{
		Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
                		"WHERE MEM_ID = " + StringUtil.toSqlValueWithSQuote(memId) + " " +
                		"AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(CVERP_FN) + " " +
                		"AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
        log.info(seqnoSql);
        try {
        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        	stmt = conn.createStatement();
            rs = stmt.executeQuery(seqnoSql);
            while (rs.next()) {
            	seqno = rs.getString(1);
             }
        } catch (SQLException ignore) {
        	log.warn("SQLException:" + ignore.getMessage(), ignore);
        } finally {
        	ReleaseResource.releaseDB(conn, stmt, rs);
        }
        memId2Seqno.put(memId, seqno);
        memIds.add(memId);
        log.info("memIds: " + memIds);
        log.info("memId2Seqno: " + memId2Seqno);
	}
	
	@Override
	public ExpFileSetting makeExpFileSetting() 
	{
		ExpFileSetting efs = new ExpFileSetting();
        // check file size by expFile.length() % fileInfo.dataLen == 0 && (totalRecords == recordCount) depends on checkFlag
        // checkFlag:boolean:true
        // 所以若產生的檔案是 fixed length, 請設為 true, 若是 variable length, 請設為 false, default is true
        efs.setCheckFlag(false);
        // fileEncoding:String:UTF-8
        // 產生檔案時的編碼, default is UTF-8
        if (StringUtil.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        efs.setFileEncoding(encoding);
        // usingTempFile, 是否使用 temp file, default true
        // usingTempFile:boolean:true
        // 是否啟動 temp file 的動作
        efs.setUsingTempFile(true);
        // 若用 temp file, 預設先 pending 的字串, default ".TMP"
        // tempFilePending:String
        // 若啟動 temp file 的動作, 要如何 pending temp file 的檔名
        efs.setTempFilePending(".TMP");
        // recordsPerFlush:int:-1
        // 產生檔案時處理多少筆記錄後做一次 flush 動作, <= 0 時不 enable flush 動作, default is -1
        efs.setRecordsPerFlush(-1);
        // recordsPerFile:int:-1
        // 多少筆產生一個 file, default 全部一個檔案(<= 0)
        // 若有設 recordsPerFile, 每個 expFileInfo 的 seqnoStart, seqnoEnd 一定要給
        efs.setRecordsPerFile(-1);
        efs.setLineSeparator("\r\n");
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        for (int i = 0; i < memIds.size(); i++) {
            String memId = (String) memIds.get(i);
            String seqno = (String) memId2Seqno.get(memId);
            if (seqno == null) {
                log.warn("can't find in memId2Seqno or memId2Count, ignore for '" + memId + "'");
                continue;
            }
            
    		try {
    			String queryInFileNameSQL = 
    	        		"SELECT INFILE_NAME,PURCHASE_ORDER_NO " + 
    					"FROM TB_CP_DELIVERY " + 
    					"WHERE OUTFILE_NAME IS NULL AND INFILE_NAME IS NOT NULL " +
    					"AND PURCHASE_ORDER_NO <> '0000000000' " +
//    					"AND CARD_MATERIAL_NO <> '000000000000000000' " +
//    					"AND (TRIM(PACKING_NO) IS NOT NULL AND TRIM(PACKAGE_NO) IS NOT NULL AND TRIM(BOX_NO) IS NOT NULL AND TRIM(PACKAGE_MATERIAL_NO) IS NOT NULL AND TRIM(CARD_MATERIAL_NO) IS NOT NULL) " + 
    					"GROUP BY INFILE_NAME,PURCHASE_ORDER_NO ";
    			log.debug(queryInFileNameSQL);
    			
    			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    			stmt = conn.createStatement();
    		    rs = stmt.executeQuery(queryInFileNameSQL);
    		    int cntSeq = Integer.valueOf(seqno);
    		    while (rs.next()) {
    		    	String inFileName = rs.getString(1).trim();
    		    	String purchaseOrderNo = rs.getString(2).trim();
    		    	if (isBlankOrNull(inFileName) || isBlankOrNull(purchaseOrderNo)) 
    		    	{
    		    		log.warn("inFileName fail: " + inFileName);
    		    		log.warn("purchaseOrderNo fail: " + purchaseOrderNo);
    		    		continue;
    		    	}
    		    	
    		    	ExpFileInfo expFileInfo = new ExpFileInfo();
    		    	expFileInfo.setMemId(memId);
    		    	expFileInfo.setFileName(CVERP_FN);
    		    	expFileInfo.setFileDate(batchDate);
    		    	expFileInfo.setSeqno(StringUtils.leftPad(String.valueOf(cntSeq), 2, "0"));
    		    	++cntSeq;
    		    	String fullFileName = CARD_IN_APPEND + "." + expFileInfo.getFileDate() + expFileInfo.getSeqno();
    		    	expFileInfo.setFullFileName(fullFileName);
//    		    	log.info("fullFileName: " + fullFileName);
    		    	expFileInfo.setInFileName(inFileName);
    		    	expFileInfo.setPurchaseOrderNo(purchaseOrderNo);
    		    	String selectSQL = 
                    		"SELECT PACKING_NO, BOX_NO, PACKAGE_MATERIAL_NO, PACKAGE_NO, CARD_MATERIAL_NO, CARD_NO " +
                    		"FROM TB_CP_DELIVERY " +
                    		"WHERE OUTFILE_NAME IS NULL AND INFILE_NAME = " + StringUtil.toSqlValueWithSQuote(expFileInfo.getInFileName()) + " " + 
                    		"ORDER BY PACKING_NO, BOX_NO, PACKAGE_NO, CARD_NO ";
                    log.info(selectSQL);
                    expFileInfo.setSelectSQL(selectSQL);
    		    	efs.addExpFileInfo(expFileInfo);
    		     }
    		} catch (SQLException ignore) {
    			log.warn("SQLException:" + ignore.getMessage(), ignore);
    			return null;
    		} finally {
    			ReleaseResource.releaseDB(conn, stmt, rs);
    		}
        }
        return efs;
	}
	
	public String outputBeforeFile() 
	{
		String purchaseOrderNo = expFileInfo.getPurchaseOrderNo();
		StringBuffer header = new StringBuffer();
        header.append("H");
        header.append(StringUtils.rightPad(purchaseOrderNo, 10, " "));
        return header.toString();
    }
	
    public String outputEndFile() 
    {
    	StringBuffer trailer = new StringBuffer();
    	trailer.append("F");
    	trailer.append(StringUtils.leftPad(String.valueOf(cntPackingNo), 5, " "));
    	trailer.append(StringUtils.leftPad(String.valueOf(cntBoxNo), 6, " "));
    	trailer.append(StringUtils.leftPad(String.valueOf(cntPackageNo), 8, " "));
    	trailer.append(StringUtils.leftPad(String.valueOf(cntCardNo), 9, " "));
        return trailer.toString();
    }
	
	@Override
	public String outputOneRecord(List record) throws Exception 
	{
		String packingNo = "";
		String boxNo = "";
		String packageMaterialNo = "";
		String packageNo = "";
		String cardMaterialNo = "";
		String cardNo = "";
		
		if (!isBlankOrNull(record.get(0).toString())) packingNo = record.get(0).toString().trim();
		if (!isBlankOrNull(record.get(1).toString())) boxNo = record.get(1).toString().trim();
		if (!isBlankOrNull(record.get(2).toString())) packageMaterialNo = record.get(2).toString().trim();
		if (!isBlankOrNull(record.get(3).toString())) packageNo = record.get(3).toString().trim();
		if (!isBlankOrNull(record.get(4).toString())) cardMaterialNo = record.get(4).toString().trim();
		if (!isBlankOrNull(record.get(5).toString())) cardNo = record.get(5).toString().trim();
		
        StringBuffer sb = new StringBuffer();
    	sb.append("D");
        sb.append(StringUtils.leftPad(packingNo, 10, ' '));
        sb.append(StringUtils.leftPad(boxNo, 16, ' '));
        sb.append(StringUtils.leftPad(packageMaterialNo, 18, ' '));
        sb.append(StringUtils.leftPad(packageNo, 16, ' '));
        sb.append(StringUtils.rightPad(cardMaterialNo, 18, ' '));
        sb.append(StringUtils.leftPad(cardNo, 16, ' '));
        
        if (!lastPackingNo.equals(packingNo)
        		&& !isBlankOrNull(record.get(0).toString())) 
			++ cntPackingNo;
		
		if ((!lastPackingNo.equals(packingNo)
        		&& !isBlankOrNull(record.get(0).toString())) 
				|| (!lastBoxNo.equals(boxNo) 
						&& !isBlankOrNull(record.get(1).toString()))) 
			++ cntBoxNo;
		
		if ((!lastPackingNo.equals(packingNo)
        		&& !isBlankOrNull(record.get(0).toString())) 
        		|| (!lastBoxNo.equals(boxNo) 
						&& !isBlankOrNull(record.get(1).toString()))
				|| (!lastPackageNo.equals(packageNo) 
						&& !isBlankOrNull(record.get(3).toString()))) 
			++ cntPackageNo;
		
		lastPackingNo = packingNo;
		lastBoxNo = boxNo;
		lastPackageNo = packageNo;
		++ cntCardNo;
        return sb.toString();
	}
	
	@Override
	public void actionsAfterFile() throws Exception 
	{
		lastPackingNo = " ";
	    lastBoxNo = " ";
	    lastPackageNo = " ";
	    
		cntPackingNo = 0;
		cntBoxNo = 0;
		cntPackageNo = 0;
		cntCardNo = 0;
    }
	
	@Override
	public void actionsAfterInfo() throws Exception 
	{
		Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "UPDATE TB_CP_DELIVERY " +
    			"SET OUTFILE_NAME = '" + expFileInfo.getFullFileName() + "' " +
    			"WHERE INFILE_NAME = '" + expFileInfo.getInFileName() + "' " +
    			"AND OUTFILE_NAME IS NULL";
        log.info(sql);
        try {
        	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        	stmt = conn.createStatement();
        	stmt.executeUpdate(sql);
        	conn.commit();
        } catch (SQLException ignore) {
        	log.warn("SQLException:" + ignore.getMessage(), ignore);
        	conn.rollback();
        } finally {
        	ReleaseResource.releaseDB(conn, stmt, rs);
        }
    }
	
	@Override
	public List outputDtlRecord(List record) 
	{
		return null;
	}
	
	private boolean isBlankOrNull(String value) 
	{
		return (value == null || value.trim().equals(""));
	}
	
	private static ExpCardVR getInstance() 
	{
		ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
		ExpCardVR instance = (ExpCardVR) apContext.getBean("expCardVR");
        return instance;
	}
	
	public static void main(String[] args) 
	{
		ExpCardVR expCardVR = null;
        try {
            String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            else if (!DateUtil.isValidDate(batchDate)) {
                log.info("invalid batchDate('" + batchDate + "') using system date!");
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	expCardVR = getInstance();
            }
            else {
            	expCardVR = new ExpCardVR();
            }
            
            expCardVR.setBatchDate(batchDate);
            // 註: 此 method 一定要先呼叫
            expCardVR.beforeHandling();
            expCardVR.run(args);
        }
        catch (Exception ignore) {
            log.warn("ExpCardVR run fail:" + ignore.getMessage(), ignore);
        }
	}
}
