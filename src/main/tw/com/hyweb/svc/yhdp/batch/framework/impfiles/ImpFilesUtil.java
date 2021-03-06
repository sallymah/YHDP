/**
 * changelog
 * --------------------
 * 20090522
 * duncan
 * modify getInctlInfosInWorkAndProcessing, getInctlInfos, 加上 fileDate, seqno 參數
 * --------------------
 * 20090505
 * duncan
 * modify getInctlInfosInWorkAndProcessing, getInctlInfos, 加上 usingLike 參數
 * --------------------
 * 20090310
 * duncan
 * 多考慮分隔符號的檔案, start, length 不會填
 * --------------------
 * 20081218
 * duncan,anny
 * TB_INCTL_ERR.MESSAGE 有可能超過 1200
 * --------------------
 * 20080529
 * duncan
 * rename PROC_DATE, PROC_TIME to SYS_DATE, SYS_TIME
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.framework.impfiles;

import org.apache.log4j.Logger;

import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.info.*;
import tw.com.hyweb.service.db.mgr.TbInctlErrMgr;
import tw.com.hyweb.service.db.sess.TbFileInfoCtr;
import tw.com.hyweb.service.db.sess.TbInctlCtr;
import tw.com.hyweb.service.db.sess.TbRcodeCtr;
import tw.com.hyweb.util.string.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * <pre>
 * ImpFilesUtil
 * </pre>
 * author: duncan
 */
public class ImpFilesUtil {
    private static Logger log = Logger.getLogger(ImpFilesUtil.class);

    /**
     * <pre>
     * 依據 <em>fileName</em> 取得一個 TbFileInfoInfo object
     * just call getFileInfo(fileName, Layer1Constants.INOUT_IN);
     * </pre>
     *
     * @param fileName fileName
     * @return TbFileInfoInfo object
     */
    public static TbFileInfoInfo getFileInfoIn(String fileName) {
        return getFileInfo(fileName, Layer1Constants.INOUT_IN);
    }

    /**
     * <pre>
     * 依據 <em>fileName</em> 取得一個 TbFileInfoInfo object
     * just call getFileInfo(fileName, Layer1Constants.INOUT_OUT);
     * </pre>
     *
     * @param fileName fileName
     * @return TbFileInfoInfo object
     */
    public static TbFileInfoInfo getFileInfoOut(String fileName) {
        return getFileInfo(fileName, Layer1Constants.INOUT_OUT);
    }

    /**
     * <pre>
     * 依據 <em>fileName, inOut</em> 取得一個 TbFileInfoInfo object
     * inOut:
     * Layer1Constants.INOUT_IN = "I";
     * Layer1Constants.INOUT_OUT = "O";
     * </pre>
     *
     * @param fileName fileName
     * @param inOut    inOut
     * @return TbFileInfoInfo object
     */
    public static TbFileInfoInfo getFileInfo(String fileName, String inOut) {
        if (StringUtil.isEmpty(fileName)) {
            throw new IllegalArgumentException("fileName is not empty!");
        }
        TbFileInfoInfo info = null;
        try {
            TbFileInfoCtr ctr = new TbFileInfoCtr(Layer1Constants.DSNAME_BATCH);
            TbFileInfoPK pk = new TbFileInfoPK();
            pk.setFileName(fileName);
            pk.setInOut(inOut);
            info = ctr.querySingle(pk);
        }
        catch (Exception ignore) {
            info = null;
            log.error("getFileInfo error:" + ignore.getMessage(), ignore);
        }
        return info;
    }

    /**
     * <pre>
     * 依 <em>fileName</em> 來將 TB_INCTL.WORK_FLAG = "1" 的資料取出
     * just call getInctlInfos(fileName, Layer1Constants.WORKFLAG_INWORK);
     * return List object, each element is TbInctlInfo object
     * </pre>
     *
     * @param fileName fileName
     * @param fileDate fileDate
     * @param seqno seqno
     * @param usingLike usingLike
     * @return List object, each element is TbInctlInfo object
     */
    public static List getInctlInfosInWork(String fileName, String fileDate, String seqno, boolean usingLike, TbBatchResultInfo tbBatchResultInfo) {
        return getInctlInfos(fileName, Layer1Constants.WORKFLAG_INWORK, fileDate, seqno, usingLike, tbBatchResultInfo);
    }

    /**
     * <pre>
     * 依 <em>fileName, fileDate, seqno</em> 來將 TB_INCTL.WORK_FLAG = "1", "2" 的資料取出
     * return List object, each element is TbInctlInfo object
     * </pre>
     *
     * @param fileName fileName
     * @param fileDate fileDate
     * @param seqno seqno
     * @param usingLike usingLike
     * @return List object, each element is TbInctlInfo object
     */
    public static List getInctlInfosInWorkAndProcessing(String fileName, String fileDate, String seqno, boolean usingLike, TbBatchResultInfo tbBatchResultInfo) {
        List inctlInfos = new ArrayList();
        inctlInfos.addAll(getInctlInfos(fileName, Layer1Constants.WORKFLAG_INWORK, fileDate, seqno, usingLike, tbBatchResultInfo));
        
        /* 因為遠鑫不是直接寫tb_trans,所以work_flag ='2'需手動處理,不然會造成tb_inctl_err重覆失敗
         * inctlInfos.addAll(getInctlInfos(fileName, Layer1Constants.WORKFLAG_PROCESSING, fileDate, seqno, usingLike));
         */
        return inctlInfos;
    }
    
    /**
     * <pre>
     * 依 <em>fileName, fileDate, seqno</em> 來將 TB_INCTL.WORK_FLAG = "1", "2" 的資料取出
     * return List object, each element is TbInctlInfo object
     * </pre>
     *
     * @param fileName fileName
     * @param fileDate fileDate
     * @param seqno seqno
     * @param usingLike usingLike
     * @return List object, each element is TbInctlInfo object
     */
    public static List getInctlInfosInWorkAndProcessing(String[] fileName, String fileDate, String seqno, boolean usingLike, TbBatchResultInfo tbBatchResultInfo) {
        List inctlInfos = new ArrayList();
        inctlInfos.addAll(getInctlInfos(fileName, Layer1Constants.WORKFLAG_INWORK, fileDate, seqno, usingLike, tbBatchResultInfo));
        
        /* 因為遠鑫不是直接寫tb_trans,所以work_flag ='2'需手動處理,不然會造成tb_inctl_err重覆失敗
         * inctlInfos.addAll(getInctlInfos(fileName, Layer1Constants.WORKFLAG_PROCESSING, fileDate, seqno, usingLike));
         */
        return inctlInfos;
    }

    /**
     * <pre>
     * 依 <em>fileName, workFlag</em> 來將 TB_INCTL 的資料取出
     * workFlag:
     * Layer1Constants.WORKFLAG_INWORK = "1";
     * Layer1Constants.WORKFLAG_PROCESSING = "2";
     * Layer1Constants.WORKFLAG_PROCESSOK = "3";
     * Layer1Constants.WORKFLAG_DELETED = "6";
     * Layer1Constants.WORKFLAG_PROCESSFAIL = "9";
     * return List object, each element is TbInctlInfo object
     * </pre>
     *
     * @param fileName fileName
     * @param workFlag workFlag
     * @param fileDate fileDate
     * @param seqno seqno
     * @param usingLike usingLike
     * @return List object, each element is TbInctlInfo object
     */
    public static List getInctlInfos(String fileName, String workFlag, String fileDate, String seqno, boolean usingLike, TbBatchResultInfo tbBatchResultInfo) {
        if (StringUtil.isEmpty(fileName)) {
            throw new IllegalArgumentException("fileName is not empty!");
        }
        Vector inctlInfos = new Vector();
        try {
            TbInctlCtr ctr = new TbInctlCtr(Layer1Constants.DSNAME_BATCH);
            String where = "";
            if (usingLike) {
                where += "FILE_NAME LIKE " + StringUtil.toSqlValueWithSQuote("%" + fileName + "%");
            }
            else {
                where += "FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileName);
            }
            if (!StringUtil.isEmpty(fileDate)) {
                where += " AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(fileDate);
            }
            if (!StringUtil.isEmpty(seqno)) {
                where += " AND SEQNO = " + StringUtil.toSqlValueWithSQuote(seqno);
            }
            where += " AND WORK_FLAG = " + StringUtil.toSqlValueWithSQuote(workFlag);
            if(tbBatchResultInfo != null){
            	
            	if (Layer1Constants.JOB_ID_DEFAULT.equalsIgnoreCase(tbBatchResultInfo.getJobId())
            			&& Layer1Constants.JOB_TIME_DEFAULT.equalsIgnoreCase(tbBatchResultInfo.getJobTime())
            			&& StringUtil.isEmpty(tbBatchResultInfo.getMemId())){
            		;
            	}
            	else{
		    		if(Layer1Constants.MEM_LAST.equalsIgnoreCase(tbBatchResultInfo.getMemId())){
		    			where += " AND MEM_ID NOT IN (SELECT MEM_ID FROM TB_MEMBER WHERE JOB_ID IS NOT NULL AND JOB_TIME IS NOT NULL) ";
		    		}else{
		    			
		    			where += " AND MEM_ID IN (SELECT MEM_ID FROM TB_MEMBER WHERE 1=1 ";
			    		if(!StringUtil.isEmpty(tbBatchResultInfo.getJobId()) 
								&& !tbBatchResultInfo.getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
			    			where +=" AND JOB_ID=" + StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobId());
			    			
						       if(!StringUtil.isEmpty(tbBatchResultInfo.getJobTime()) 
						    		   && !tbBatchResultInfo.getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
						    	   where +=" AND JOB_TIME=" + StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobTime());
						       }
						}
						if(!StringUtil.isEmpty(tbBatchResultInfo.getMemId())){
							where +=" AND MEM_ID=" + StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getMemId());
						}	
						where += ")";
		    		}
            	}
            }
            
            ctr.queryMultiple(where, inctlInfos, "FILE_DATE ASC, SEQNO ASC");
        }
        catch (Exception ignore) {
            inctlInfos = null;
            log.error("getInctlInfos error:" + ignore.getMessage(), ignore);
        }
        return inctlInfos;
    }
    
    public static List getInctlInfos(String[] fileName, String workFlag, String fileDate, String seqno, boolean usingLike, TbBatchResultInfo tbBatchResultInfo) {
        if (null == fileName || fileName.length == 0) {
            throw new IllegalArgumentException("fileName is not empty!");
        }
        Vector inctlInfos = new Vector();
        log.info(" tbBatchResultInfo: " + tbBatchResultInfo);
        try {
            TbInctlCtr ctr = new TbInctlCtr(Layer1Constants.DSNAME_BATCH);
            String where = "";
            if (usingLike) {
                where += "FILE_NAME LIKE " + StringUtil.toSqlValueWithSQuote("%" + fileName + "%");
            }
            else {
                StringBuffer whereCmd = new StringBuffer();
                for(int idx = 0; idx < fileName.length; idx++)
                {
                    if(idx > 0) {
                        whereCmd.append(",");
                    }
                    
                    whereCmd.append("'" + fileName[idx] + "'");
                }
                where += "FILE_NAME IN ("+ whereCmd.toString() +")";
            }
            if (!StringUtil.isEmpty(fileDate)) {
                where += " AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(fileDate);
            }
            if (!StringUtil.isEmpty(seqno)) {
                where += " AND SEQNO = " + StringUtil.toSqlValueWithSQuote(seqno);
            }
            where += " AND WORK_FLAG = " + StringUtil.toSqlValueWithSQuote(workFlag);
            log.info(" tbBatchResultInfo: " + tbBatchResultInfo);
            if(tbBatchResultInfo != null){
            	
            	if (Layer1Constants.JOB_ID_DEFAULT.equalsIgnoreCase(tbBatchResultInfo.getJobId())
            			&& Layer1Constants.JOB_TIME_DEFAULT.equalsIgnoreCase(tbBatchResultInfo.getJobTime())
            			&& StringUtil.isEmpty(tbBatchResultInfo.getMemId())){
            		;
            	}
            	else{
		    		if(Layer1Constants.MEM_LAST.equalsIgnoreCase(tbBatchResultInfo.getMemId())){
		    			where += " AND MEM_ID NOT IN (SELECT MEM_ID FROM TB_MEMBER WHERE JOB_ID IS NOT NULL AND JOB_TIME IS NOT NULL) ";
		    		}else{
		    			
		    			where += " AND MEM_ID IN (SELECT MEM_ID FROM TB_MEMBER WHERE 1=1 ";
			    		if(!StringUtil.isEmpty(tbBatchResultInfo.getJobId()) 
								&& !tbBatchResultInfo.getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
			    			where +=" AND JOB_ID=" + StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobId());
			    			
						       if(!StringUtil.isEmpty(tbBatchResultInfo.getJobTime()) 
						    		   && !tbBatchResultInfo.getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
						    	   where +=" AND JOB_TIME=" + StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobTime());
						       }
						}
						if(!StringUtil.isEmpty(tbBatchResultInfo.getMemId())){
							where +=" AND MEM_ID=" + StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getMemId());
						}	
						where += ")";
		    		}
            	}
            }
            ctr.queryMultiple(where, inctlInfos, "FILE_NAME , FILE_DATE ASC, SEQNO ASC");
        }
        catch (Exception ignore) {
            inctlInfos = null;
            log.error("getInctlInfos error:" + ignore.getMessage(), ignore);
        }
        return inctlInfos;
    }

    // key: String, TB_RCODE, value: TbRcodeInfo object
    private static HashMap rcodeInfos = new HashMap();

    /**
     * 使用 ctr 和 info (TB_RCODE) 來產生 rcodeInfos
     */
    public static void loadRcodeInfos() {
        rcodeInfos.clear();
        try {
            TbRcodeCtr ctr = new TbRcodeCtr(Layer1Constants.DSNAME_BATCH);
            Vector result = new Vector();
            ctr.queryAll(result, "RCODE");
            for (int i = 0; i < result.size(); i++) {
                TbRcodeInfo info = (TbRcodeInfo) result.get(i);
                rcodeInfos.put(info.getRcode(), info);
            }
        }
        catch (Exception ignore) {
            log.error("loadRcodeInfos fail:" + ignore.getMessage(), ignore);
        }
        finally {
            ;
        }
    }

    /**
     * <pre>
     * 依 <em>rcode</em> 來取得 TB_RCODE 的一筆資料,
     * 找到傳回 TbRcodeInfo object, 找不到傳回 null
     * </pre>
     *
     * @param rcode rcode
     * @return TbRcodeInfo object
     */
    public static TbRcodeInfo getRcodeInfo(String rcode) {
        if (StringUtil.isEmpty(rcode)) {
            throw new IllegalArgumentException("rcode is not empty!");
        }
        TbRcodeInfo info = (TbRcodeInfo) rcodeInfos.get(rcode);
        return info;
    }

    /**
     * <pre>
     * 依 <em>rcode, fieldInfo, content</em> 來組成一個 ErrorDescInfo object
     * 若找不到 rcode, 傳回 null
     * 若 fieldInfo == null, 不設 position, field 欄位
     * </pre>
     *
     * @param rcode     rcode
     * @param fieldInfo fieldInfo
     * @param content   content
     * @return ErrorDescInfo object
     */
    public static ErrorDescInfo getErrorDescInfo(String rcode, FieldInfo fieldInfo, String content) {
        if (StringUtil.isEmpty(rcode)) {
            throw new IllegalArgumentException("rcode is not empty!");
        }
        TbRcodeInfo rcodeInfo = getRcodeInfo(rcode);
        if (rcodeInfo == null) {
            // todo: maybe
            log.error("can't find rcodeInfo with " + rcode);
            rcodeInfo = new TbRcodeInfo();
            rcodeInfo.setRcode("2791");
            rcodeInfo.setRcodeDesc("General DB error");
        }
        ErrorDescInfo descInfo = new ErrorDescInfo();
        descInfo.setErrorCode(rcode);
        descInfo.setErrorMsg(rcodeInfo.getRcodeDesc());
        if (fieldInfo != null) {
            if (fieldInfo.getStart() <= 0) {
                descInfo.setPosition(fieldInfo.getIndex());
            }
            else {
                descInfo.setPosition(fieldInfo.getStart());
            }
            descInfo.setField(fieldInfo.getDesc());
        }
        descInfo.setContent(content);
        return descInfo;
    }

    /**
     * <pre>
     * 依 <em>inctlInfo, lineInfo, descInfo</em> 來組成一個 TbInctlErrInfo object
     * <em>inctlInfo, descInfo</em> 不能是 null
     * </pre>
     *
     * @param inctlInfo inctlInfo
     * @param lineInfo  lineInfo
     * @param descInfo  descInfo
     * @return TbInctlErrInfo object
     */
    public static TbInctlErrInfo makeInctlErrInfo(TbInctlInfo inctlInfo, DataLineInfo lineInfo, ErrorDescInfo descInfo) {
        if (inctlInfo == null) {
            throw new IllegalArgumentException("inctlInfo is null!");
        }
        if (descInfo == null) {
            throw new IllegalArgumentException("descInfo is null!");
        }
        List descInfos = new ArrayList();
        descInfos.add(descInfo);
        return makeInctlErrInfo(inctlInfo, lineInfo, descInfos);
    }

    /**
     * <pre>
     * 依 <em>inctlInfo, lineInfo, descInfos</em> 來組成一個 TbInctlErrInfo object
     * <em>inctlInfo, descInfos</em> 不能是 null 且 descInfos.size() 不能等於 0
     * descInfos is List object, each element is ErrorDescInfo object
     * </pre>
     *
     * @param inctlInfo inctlInfo
     * @param lineInfo  lineInfo
     * @param descInfos descInfos is List object, each element is ErrorDescInfo object
     * @return TbInctlErrInfo object
     */
    public static TbInctlErrInfo makeInctlErrInfo(TbInctlInfo inctlInfo, DataLineInfo lineInfo, List descInfos) {
        if (inctlInfo == null) {
            throw new IllegalArgumentException("inctlInfo is null!");
        }
        if (descInfos == null || descInfos.size() == 0) {
            throw new IllegalArgumentException("descInfos is null or empty!");
        }
        TbInctlErrInfo inctlErrInfo = new TbInctlErrInfo();
        // set by inctlInfo
        inctlErrInfo.setMemId(inctlInfo.getMemId());
        inctlErrInfo.setFileName(inctlInfo.getFileName());
        inctlErrInfo.setFileDate(inctlInfo.getFileDate());
        inctlErrInfo.setSeqno(inctlInfo.getSeqno());
        inctlErrInfo.setFileType(inctlInfo.getFileType());
        inctlErrInfo.setFullFileName(inctlInfo.getFullFileName());
        // set by lineInfo
        if (lineInfo == null) {
            inctlErrInfo.setLineNo(new Integer(0));
            inctlErrInfo.setMessage("");
            inctlErrInfo.setMessageLen(new Integer(0));
        }
        else {
            inctlErrInfo.setLineNo(new Integer(lineInfo.getLineNo()));
            String message = StringUtil.getMaxString(lineInfo.getPlainLine(), "UTF-8", 1200);
            inctlErrInfo.setMessage(message);
            if (lineInfo.getMappingInfo().getDataLength() <= 0) {
                inctlErrInfo.setMessageLen(new Integer(message.length()));
            }
            else {
                inctlErrInfo.setMessageLen(new Integer(lineInfo.getMappingInfo().getDataLength()));
            }
        }
        String lineSep = System.getProperty("line.separator", "\n");
        // set by descInfo
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < descInfos.size(); i++) {
            if (i > 0) {
                sb.append(lineSep);
            }
            ErrorDescInfo descInfo = (ErrorDescInfo) descInfos.get(i);
            sb.append(descInfo);
        }
        // avoid exceed table field length
        String errorDesc = StringUtil.getMaxString(sb.toString(), "UTF-8", 1200);
        inctlErrInfo.setErrorDesc(errorDesc);
        // set others
        String dateTime = DateUtil.getTodayString();
        String sysDate = dateTime.substring(0, 8);
        String sysTime = dateTime.substring(8, 14);
        String parMon = dateTime.substring(4, 6);
        String parDay = dateTime.substring(6, 8);
        inctlErrInfo.setSysDate(sysDate);
        inctlErrInfo.setSysTime(sysTime);
        inctlErrInfo.setParMon(parMon);
        inctlErrInfo.setParDay(parDay);
        return inctlErrInfo;
    }

    public static boolean insertInctlErr(Connection conn, boolean isCommit, TbInctlErrInfo inctlErrInfo) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (inctlErrInfo == null) {
            throw new IllegalArgumentException("inctlErrInfo is null!");
        }
        boolean ret = false;
        try {
            TbInctlErrMgr mgr = new TbInctlErrMgr(conn);
            mgr.insert(inctlErrInfo);
            if (isCommit) {
                conn.commit();
            }
            ret = true;
        }
        catch (Exception ignore) {
            ret = false;
            log.error("insertInctlErr fail:" + ignore.getMessage(), ignore);
            throw new SQLException(ignore.getMessage());
        }
        return ret;
    }

    /**
     * <pre>
     * 若 <em>data</em> 是在 <em>datas</em> 中, return true, 否則, return false
     * </pre>
     *
     * @param data  data
     * @param datas datas
     * @return true/false
     */
    public static boolean isInArray(String data, String[] datas) {
        for (int i = 0; i < datas.length; i++) {
            if (data.equals(datas[i])) {
                return true;
            }
        }
        return false;
    }

    static {
        loadRcodeInfos();
    }
}
