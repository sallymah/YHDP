package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.Field;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Channel;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.online.ISOMsg;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.InctlBean;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.MsgUtil;
import tw.com.hyweb.svc.yhdp.batch.util.BatchDateUtil;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSProcCode;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.LmsDbUtil;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;

/**
 * JobRunner:將raw data byte[]封裝起來交給其他Thread呼叫,本身是Runnable所以已經將實做的
 * 方法封裝進來,用來處理raw data byte[]轉換成Context及時間的紀錄
 * 
 * @author user 
 */
public class JobRunner extends AbstractImpFile implements Runnable
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(JobRunner.class);
    private final DecimalFormat decimalFormat = new DecimalFormat("00000000.00");
    private final String TXN_SRC = "B";
    private Channel channel;
    private String remoteIp;
    private byte[] header;
    private byte[] body;
    private Date timeJobInit;
    private Date timeJobExec;    
    private boolean doReject = false;
    private Hashtable<String,String> inctlResultMap = null; 
    IContextListener ctxListener;
    private boolean isUpdateTermBatch = false;
    private TbTermBatchInfo termBatch = null;
    
    /**
     * Creates a new InnerJobRunner object.
     * @param lChannel - the channel receive the message
     * @param lHeader - raw data header
     * @param lBody - raw data body
     */
    public JobRunner()
    {
        this.timeJobInit = new Date();
    }
    
    /**
     * Creates a new InnerJobRunner object.
     * @param acceptRemoteIp - the accept client remote ip
     * @param lChannel - the channel receive the message
     * @param lHeader - raw data header
     * @param lBody - raw data body
     */
    public JobRunner(IContextListener ctxListener, InctlBean inctlBean, ImpFileInfo impFileInfo, Hashtable<String,String> inctlResultMap, int recordsPerCommit)
    {
        this.inctlBean = inctlBean;
        this.impFileInfo = impFileInfo;
        this.inctlResultMap = inctlResultMap;
        this.inctlInfo = impFileInfo.getInctlInfo();
        this.fileName = impFileInfo.getFileInfo().getFileName();
        this.fileInfo = impFileInfo.getFileInfo();
        this.mappingInfo =  impFileInfo.getMappingInfo();
        this.ctxListener = ctxListener;
        this.recordsPerCommit = recordsPerCommit;
    }

    /**
     * It call connector process() method to handle message,
     * 用來處理raw data byte[]轉換成Context及時間的紀錄
     */
    public void run()
    {
    	this.timeJobExec = new Date();
    	
        try {
            logger.info(impFileInfo.getInctlInfo().getFullFileName() + " is begin working");
            // handle impFileInfo
            if(handleImpFileInfo() == false)
            {
                inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSFAIL);
            }
        }
        catch (Exception e) {
            logger.error("handleImpFileInfo error:" + e.getMessage(), e);
            inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSFAIL);
        }
        finally {
            impFileInfo.closeFile();
            //logger.debug("end FILE_NAMEl:"+impFileInfo.getInctlInfo().getFullFileName() + ",FILE_DATE:" + impFileInfo.getInctlInfo().getFileDate() + ",SEQNO:"+impFileInfo.getInctlInfo().getSeqno());
        }
        /* INCRL的結果如果未記會使THREAD累計到總秒數才會離開MAIN THREAD */
        String result = inctlResultMap.get(impFileInfo.getInctlInfo().getFullFileName());
        if(result == null || result.length() == 0)
        {
            if(null != impFileInfo && null != impFileInfo.getInctlInfo())
            {
                if(impFileInfo.getFailCnt() > 0)
                {
                    inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSFAIL);
                }
                else if(impFileInfo.getInctlInfo().getTotRec() != impFileInfo.getInctlInfo().getSucCnt())
                {
                    inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSFAIL);
                }
                else
                {
                    inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSOK);
                }
            }
            else
            {
                inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSFAIL);
            }
        }
        logger.info(impFileInfo.getInctlInfo().getFullFileName() + " is working end");
    }
    
    @Override
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
        BATCHContext ctx = new BATCHContext();
        ctx.setTbInctlInfo(impFileInfo.getInctlInfo());
        Date curDate = new Date();
    	List descInfos = null;
    	String lineStr = lineInfo.getPlainLine();
    	
    	BerTLV berTLV = BerTLV.createInstance(ISOUtil.hex2byte(lineStr));
    	logger.debug(""+berTLV);
    	FF11 ff11 = new FF11(berTLV.getHexStr(0xFF11));
    	logger.debug("ff11:"+ff11);
    	FF21 ff21 = new FF21(berTLV.getHexStr(0xFF21));
    	logger.debug("ff21:"+ff21);
    	
    	 Properties p = new Properties();
         p.put("48", "");
         p.put("60", "");
         p.put("61", "");
         p.put("63", "");
         ctx.setTlvFields(p);
         ctx.setTimeoutValue(0);//不會有timeout
         ctx.setConnection(conn);
		
		String termDateTime = ff21.getTermDateTime();
		String HostDateTime = ff21.getHostDateTime();
		Date termDate;
		if(!termDateTime.equals("00000000000000"))
		{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            termDate = sdf.parse(termDateTime);
		} 
		else if(!HostDateTime.equals("00000000000000"))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            termDate = sdf.parse(HostDateTime);
        } 
		else
		{
		    termDate = curDate;
		}
		
        ctx.setTimeTxInit(termDate);//hostDate termDateTime 端未時間(鎖卡交易才會以交易時間為主)
		ctx.setTimeTxExec(curDate);
		ctx.setTxnSrc(TXN_SRC);
		ctx.setRawData(lineInfo.getPlainLine());
		logger.debug("raw date:"+lineInfo.getPlainLine());
		ctx.setDongleRespCode(ff21.getRespCode());
        /* parser */
        BerTLV lmsMsg = MsgUtil.batch2online(berTLV);
        ISOMsg isoMsg = ctx.getIsoMsg();
        ctx.setLMSMsg(lmsMsg);
        
        /* 20170224 dongle chip after bonus */
        String afterBal = ff11.getAfterBal();
        double ecashAfterBal = Double.parseDouble(afterBal.substring(1));
        String afterSing = afterBal.substring(0, 1);
        if(ecashAfterBal > 0)
        {
            ecashAfterBal = ecashAfterBal / 100;
        }
        if(Integer.valueOf(afterSing) == 1)
        {
            ecashAfterBal = 0 - ecashAfterBal;
        }
        ctx.setChipAfterBonusQty(ecashAfterBal);
        logger.debug(ecashAfterBal);
		
		if(Integer.valueOf(ctx.getLmsPcode()) == LMSProcCode.SALE || 
                Integer.valueOf(ctx.getLmsPcode()) == LMSProcCode.REDEEM_ALL ||
                Integer.valueOf(ctx.getLmsPcode()) == LMSProcCode.REDEEM_PART ||
                Integer.valueOf(ctx.getLmsPcode()) == LMSProcCode.REDEEM ||
                Integer.valueOf(ctx.getLmsPcode()) == LMSProcCode.SALE_FEE ||
                Integer.valueOf(ctx.getLmsPcode()) == LMSProcCode.TRANSFER_REDEEM ||
                Integer.valueOf(ctx.getLmsPcode()) == LMSProcCode.REDEEM_COUPON ||
                Integer.valueOf(ctx.getLmsPcode()) == LMSProcCode.BLACK_LIST)
        {
		    if(checkIsReversal(ctx, ff21.getPcode()))
            {
                isoMsg.setMTI("0400");//需要reversal
                isoMsg.set(Field.PROC_CODE, "888888");
            }
		    else
		    {
                isoMsg.setMTI("0320");
                isoMsg.set(Field.PROC_CODE, "900000");
		    }
        }
        else
        {
            if(checkIsReversal(ctx, ff21.getPcode()))
            {
                isoMsg.setMTI("0400");//需要reversal
                isoMsg.set(Field.PROC_CODE, "888888");
            }
            else
            {
                isoMsg.setMTI("0200");//只需更新檔案名稱或踢錯
                isoMsg.set(Field.PROC_CODE, "888888");
            }
        }
        isoMsg.set(Field.TRACE_NBR, ff11.getStan());
        isoMsg.set(Field.TERMINAL_ID, ff21.getTid());
        isoMsg.set(Field.MERCHANT_ID, ff21.getMid());
        String ff64 = berTLV.getHexStr(0xFF64);
        if (ff64!=null){
            isoMsg.set(64, ISOUtil.hex2byte(ff64));
        }
    	
        /* update tb_term_Batch */
        if(!isUpdateTermBatch)
        {
            ctx.setTermBatchInfo(InsertTermBatchProc());
            this.termBatch = ctx.getTermBatchInfo();
            isUpdateTermBatch = true;
        }
        else
        {
            ctx.setTermBatchInfo(this.termBatch);
        }
        
    	LMSContext respCtx = null;
    	try{
    	    logger.debug("isoMsg:"+isoMsg);
    	    logger.debug("lmsMsg:"+lmsMsg);
    		respCtx = (LMSContext) ctxListener.onMessage(ctx);
    	}catch(Exception txe){
    		logger.error(txe);
    	}
    	String respRcode = respCtx.getRcode();
    	logger.info("respRcode:"+respRcode);
    	if(!respRcode.equals("0000"))
        {
            descInfos = new ArrayList();
            ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(respRcode, null, "");
            descInfos.add(descInfo);
        }
    	return descInfos;
    }
    
    public boolean checkIsReversal(BATCHContext ctx, String pcode) throws SQLException
    {
        if(Integer.valueOf(pcode) != LMSProcCode.SAM_LOGIN1 && Integer.valueOf(pcode) != LMSProcCode.SAM_LOGIN2 &&
                Integer.valueOf(pcode) != LMSProcCode.CARD_REGISTER && Integer.valueOf(pcode) != LMSProcCode.SETTLEMENT &&
                        Integer.valueOf(pcode) != LMSProcCode.PARAMETERS_DOWNLOAD)
        {
            BerTLV tlv = ctx.getLMSMsg();
            String cardNo = ctx.getLMSCardNbr();
            String expiryDate = ctx.getLMSCardExpirationDate();
            String lmsInvoiceNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
            TbOnlTxnInfo onltxnInfo;
            logger.debug("get orig txn");
            onltxnInfo = LmsDbUtil.getTbOnlTxn(cardNo, expiryDate, lmsInvoiceNo,ctx.getConnection());
            if(onltxnInfo != null && !onltxnInfo.getStatus().equals("9"))
            {
                if(!ctx.getDongleRespCode().equals("00") && !ctx.getDongleRespCode().equals("0000"))
                {
                    logger.debug("is must reversal!!");
                    return true;
                }
            }
        }
        return false;
    }

    public String double2Str(double qty)
    {
        StringBuffer strb = new StringBuffer();
        String p = decimalFormat.format(qty);
        int idx = p.indexOf('.');
        strb.append(p.substring(0,idx)+p.substring(idx+1));
        return strb.toString();
    }

    /*
     * (non-Javadoc) 處理tb_inctl的file
     * @see tw.com.hyweb.svc.cp.batch.impfiles.ImpTrans.AbstractImpFile#handleImpFileInfo()
     */
    public boolean handleImpFileInfo() throws Exception {
        boolean autoCommit = false;
        boolean isTermBatch = false;
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
            logger.debug("updateInctl :" +impFileInfo.getInctlInfo().getFullFileName());
            
            // checkFile
            logger.debug("impFileInfo.checkFile()...");
            boolean ret = impFileInfo.checkFile(ImpFileInfo.PARSER_NONE);
            if (!ret) {
                // checkFile fail, 整檔踢退, 不處理
                updateInctl(Layer1Constants.WORKFLAG_PROCESSFAIL, true);
                thisRcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
                return false;
            }
            // beforeHandleDataLine
            
         // beforeHandleDataLine
            ExecuteSqlsInfo beforeSqlsInfo = beforeHandleDataLine();
            if (beforeSqlsInfo == null) {
                // can be null, just log, do nothing
                logger.info("beforeSqlsInfo is null!");
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
                    }catch (Exception ignore) {
                        // 做 rollback 並註記 TB_INCTL 為處理失敗
                        conn.rollback();
                        updateInctl(Layer1Constants.WORKFLAG_PROCESSFAIL, true);
                        throw ignore;
                    }
                }
            }
                     
            logger.debug("updateInctl :" +impFileInfo.getInctlInfo().getFullFileName());
            
            List<ErrorDescInfo> descInfos = null;
            boolean isCheckDataLineFail = false; 
            // handle each DataLineInfo
            for (int i = start; i < impFileInfo.getTotRec(); i++) {
            	descInfos = new ArrayList<ErrorDescInfo>();
                DataLineInfo lineInfo = null;
                String lineInfoStr = null;
                isCheckDataLineFail = false;
                logger.debug("---------handle line no:"+i+"-------------");
                try {
                    lineInfo = impFileInfo.readOneDataLine();
                    lineInfoStr=lineInfo.getPlainLine();

                	BerTLV tlv = BerTLV.createInstance(ISOUtil.hex2byte(lineInfoStr));
					if (tlv == null) {// 有錯誤
						ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "parse TLV error");
                        descInfos.add(descInfo);
					}else{
						if (!tlv.hasTag(0xFF11) || !tlv.hasTag(0xFF21) ||!tlv.hasTag(0xFF64)){
							ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "lack mandatory tag FF11 FF21 FF64");
	                        descInfos.add(descInfo);
						}
						if (tlv.hasTag(0xFF11) && tlv.getHexStr(0xFF11).length()!=56){
							ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "FF11 len err");
	                        descInfos.add(descInfo);
						}
						if (tlv.hasTag(0xFF21) && tlv.getHexStr(0xFF21).length()!=192){
							ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "FF21 len err");
							logger.info("descInfos"+descInfos+", descInfo:"+descInfo);//del
	                        descInfos.add(descInfo);
						}						
						if (tlv.hasTag(0xFF64) && tlv.getHexStr(0xFF64).length()!=16){
							ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "FF64 len err");
	                        descInfos.add(descInfo);
						}
					}
					
					if (descInfos != null && descInfos.size() > 0) {
                        if (doCommit) {
                            insertInctlErrInfo(lineInfo, descInfos);
                            isCheckDataLineFail = true;
                        }
                        else {
                            // 有錯整個檔案都不處理
                            // 先做 rollback, 再 TB_INCTL_ERR 記錄那一筆出錯, 並跳開
                            conn.rollback();
                            insertInctlErrInfo(lineInfo, descInfos);
                            break;
                        }
                    }
                    
                    if(isCheckDataLineFail == false)
                    {
                    	// handleDataLine
                    	List<ErrorDescInfo> tDescInfos = handleDataLine(lineInfo);
                    	if (tDescInfos!=null){
                    		descInfos.addAll(tDescInfos);
                    	}
                        if (descInfos != null && descInfos.size() > 0) {
                            // checkDataLine fail
                            // set failCnt and insert inctlErrInfo
                            if (doCommit) {
                                insertInctlErrInfo(lineInfo, descInfos);
                            }
                            else {
                                // 有錯整個檔案都不處理
                                // 先做 rollback, 再 TB_INCTL_ERR 記錄那一筆出錯, 並跳開
                                conn.rollback();
                                insertInctlErrInfo(lineInfo, descInfos);
                                break;
                            }
                        }
                        else
                        {
                            // run here, handle one lineInfo OK
                            impFileInfo.setSucCnt(impFileInfo.getSucCnt() + 1);
                        }
                    }
                    
                    if (doCommit) {
                        if (i % recordsPerCommit == 0) {
                            // updateInctl and do commit
                            if (i % 1000 == 0) {
                                updateInctl(Layer1Constants.WORKFLAG_PROCESSING, false);
                            }
                            conn.commit();
                            logger.debug("count:"+String.valueOf(i)+" recordsPerCommit:"+recordsPerCommit);
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
                    logger.error("handle lineInfo error:" + ignore.getMessage(), ignore);
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
                inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSOK);
            }
            else {
                if (impFileInfo.getFailCnt() > 0) {
                    updateInctl(Layer1Constants.WORKFLAG_PROCESSFAIL, false);
                    inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSFAIL);
                }
                else {
                    updateInctl(Layer1Constants.WORKFLAG_PROCESSOK, false);
                    inctlResultMap.put(impFileInfo.getInctlInfo().getFullFileName(),Layer1Constants.WORKFLAG_PROCESSOK);
                }
            }
            conn.commit();
            try {
                Thread.sleep(sleepPerCommit);
            }
            catch (Exception ignore) {
                ;
            }
            /*
            // afterHandleDataLine
            ExecuteSqlsInfo afterSqlsInfo = afterHandleDataLine();
            if (afterSqlsInfo == null) {
                // can be null, just log, do nothing
                logger.info("afterSqlsInfo is null!");
            }
            else {
                // execute afterSqlsInfo
                ExecuteSqlsUtil.executeSqls(conn, afterSqlsInfo);
                conn.commit();
            }
            */
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
    
    /**
     * 處理資料收到但是解不開的情形
     * @param ctx incoming context
     */
    protected void processGarbage(Context ctx)
    {
        logger.info(ctx+"header:"+ISOUtil.hexString(header)+" body:"+ISOUtil.hexString(body));
    }
    

    /**
     * @return 傳回 doReject。
     */
    public boolean isDoReject()
    {
        return this.doReject;
    }

    /**
     * @param bReject 要設定的 doReject。
     */
    public void setDoReject(boolean bReject)
    {
        this.doReject = bReject;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + Arrays.hashCode(this.body);
        result = PRIME * result + Arrays.hashCode(this.header);
        result = PRIME * result + ((this.timeJobExec == null) ? 0 : this.timeJobExec.hashCode());
        result = PRIME * result + ((this.timeJobInit == null) ? 0 : this.timeJobInit.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final JobRunner other = (JobRunner) obj;
        if (!Arrays.equals(this.body, other.body))
            return false;
        if (!Arrays.equals(this.header, other.header))
            return false;
        if (this.timeJobExec == null)
        {
            if (other.timeJobExec != null)
                return false;
        }
        else if (!this.timeJobExec.equals(other.timeJobExec))
            return false;
        if (this.timeJobInit == null)
        {
            if (other.timeJobInit != null)
                return false;
        }
        else if (!this.timeJobInit.equals(other.timeJobInit))
            return false;
        return true;
    }

    /**
     * @return channel
     */
    public Channel getChannel()
    {
        return channel;
    }

    /**
     * @return remoteIp
     */
    public String getRemoteIp()
    {
        return remoteIp;
    }

    /**
     * @return timeJobExec
     */
    public Date getTimeJobExec()
    {
        return timeJobExec;
    }

    /**
     * @param timeJobExec 的設定的 timeJobExec
     */
    public void setTimeJobExec(Date timeJobExec)
    {
        this.timeJobExec = timeJobExec;
    }

    /**
     * @return timeJobInit
     */
    public Date getTimeJobInit()
    {
        return timeJobInit;
    }

    /**
     * @param timeJobInit 的設定的 timeJobInit
     */
    public void setTimeJobInit(Date timeJobInit)
    {
        this.timeJobInit = timeJobInit;
    }

    @Override
    public ExecuteSqlsInfo beforeHandleDataLine() throws Exception
    {
        return null;
    }
    
    public TbTermBatchInfo InsertTermBatchProc() throws SQLException
    {
        TbTermBatchInfo termBatchInfo = null;
        //20140811:SDD沒有但是shean要update file name上去
        String fileName = impFileInfo.getInctlInfo().getFullFileName();
        String[] strs = fileName.split("_");
        String settleExpiryDate = Layer2Util.getBatchConfig("SETTLE_EXPIRY_DATE");
        String settleDate = strs[3].substring(0, 8);
        String settleTimeNoSec = strs[3].substring(8, 12);
        String settleTime = strs[3].substring(8, 12) + "00";
        String batchNo = strs[4].substring(0,6);
        String tid = strs[2];
        String mid = strs[1];
        /* 結帳日往後推n天,批量檔未入的,一律update為同一檔 */
        String lastSettleDate = BatchDateUtil.addDate(strs[3].substring(0, 8), settleExpiryDate==null?0:0-Integer.valueOf(settleExpiryDate));
        
        TbTermBatchInfo origTermBatch = getTermBatch(settleDate, settleTimeNoSec, mid, tid, batchNo, conn);
        if(null != origTermBatch)
        {
            origTermBatch.setTxnSrc("B");
            origTermBatch.setMerchId(mid);
            origTermBatch.setTermId(tid); 
            origTermBatch.setStatus("1");
            origTermBatch.setInfile(inctlInfo.getFullFileName());
            origTermBatch.setParDay(origTermBatch.getTermSettleDate().substring(6, 8));
            origTermBatch.setParMon(origTermBatch.getTermSettleDate().substring(4, 6));
            origTermBatch.setTermUpDate(DateUtils.getSystemDate());
            origTermBatch.setImpFileName(inctlInfo.getFullFileName());
            origTermBatch.setTermSettleFlag("1");
            origTermBatch.setBatchNo(batchNo);
            termBatchInfo = origTermBatch;
        }
        else
        {
            logger.debug("can't found orig term_batch");
            if(null == termBatchInfo)
            {
                TbTermBatchInfo newTermBatch = new TbTermBatchInfo();
                TbTermBatchMgr termBatchMgr = new TbTermBatchMgr(conn);
                newTermBatch.setTxnSrc("B");
                newTermBatch.setMerchId(mid);
                newTermBatch.setTermId(tid);
                newTermBatch.setTermSettleDate(settleDate);
                newTermBatch.setTermSettleTime(settleTime);
                newTermBatch.setStatus("1");
                newTermBatch.setInfile(inctlInfo.getFullFileName());
                newTermBatch.setParDay(newTermBatch.getTermSettleDate().substring(6, 8));
                newTermBatch.setParMon(newTermBatch.getTermSettleDate().substring(4, 6));
                newTermBatch.setTermUpDate(DateUtils.getSystemDate());
                newTermBatch.setImpFileName(inctlInfo.getFullFileName());
                newTermBatch.setTermSettleFlag("1");
                newTermBatch.setBatchNo(batchNo);
                try {
                    logger.info("insert TermBatch:"+newTermBatch);
                    termBatchMgr.insert(newTermBatch);
                } catch (SQLException e) {
                    logger.error(newTermBatch);
                    throw e;
                }
                termBatchInfo = newTermBatch;
            }
        }
        
        
        //如果該批有未結帳的交易，則長一筆TERM_BATCH供結帳
        if(!checkTxnIsSettle(mid, tid, batchNo))//已結帳//未結帳時，主動作結帳
        {
            settleOnlTxn(mid, tid, batchNo, termBatchInfo.getTermSettleDate(), termBatchInfo.getTermSettleTime());//settle online txn
        }

        updateTermBatch(fileName, mid, tid, batchNo, lastSettleDate);//n天前 未結帳的term_batch一律更新為結帳
        conn.commit();
        return termBatchInfo;
    }
    
    /**
     * find the same term_settle_date tb_term_batch
     */
    public TbTermBatchInfo getTermBatch(String settleDate, String settleTime, String mid, String tid, String batchNo, Connection conn) throws SQLException
    {
        TbTermBatchInfo tbTermBatch = null;
        StringBuffer sb = new StringBuffer();
        sb.append("select TERM_SETTLE_DATE,TERM_SETTLE_TIME, MERCH_ID,TERM_ID,cut_date from (");
        sb.append("select * from tb_term_batch where MERCH_ID = ? and TERM_ID = ? and BATCH_NO = ? and  TERM_SETTLE_DATE = ?");
        sb.append(" and  TERM_SETTLE_TIME like '"+ settleTime + "%' ");
        sb.append(" and CUT_DATE is null and IMP_FILE_NAME is NULL order by term_settle_date||term_settle_time desc ");
        sb.append(") where rownum = 1");
                
        Vector<String> params = new Vector<String>();
        params.add(mid);
        params.add(tid);
        params.add(batchNo);
        params.add(settleDate);
        Vector result = DbUtil.select(sb.toString(), params,conn);
        if(null != result && result.size() > 0)
        {
            tbTermBatch = new TbTermBatchInfo();
            for(int idx = 0; idx < result.size(); idx ++)
            {
                Vector record = (Vector)result.get(idx);
                tbTermBatch.setTermSettleDate((String)record.get(0));
                tbTermBatch.setTermSettleTime((String)record.get(1));
            }
        }
        logger.info("getTermBatch:"+tbTermBatch);
        return tbTermBatch;
    }
    
    public void updateTermBatch(String fullFileName, String mid, String tid, String batchNo, String lastSettleDate) throws SQLException
    {
        //該檔同一批交易全UPDATE
        String sqlTermBatch = "update TB_TERM_BATCH set IMP_FILE_NAME=? where MERCH_ID = ? and TERM_ID = ? and BATCH_NO = ? and TERM_SETTLE_DATE >= ? and CUT_DATE is null and IMP_FILE_NAME is NULL";
        String sqlTermBatchLog = "update TB_TERM_BATCH set IMP_FILE_NAME= '%s' where MERCH_ID = '%s' and TERM_ID = '%s' and BATCH_NO = '%s' and TERM_SETTLE_DATE >= '%s' and CUT_DATE is null and IMP_FILE_NAME is NULL";
        Vector<String> params = new Vector<String>();
        params.add(fullFileName);//fileName
        params.add(mid);//merchant
        params.add(tid);//term
        params.add(batchNo);//batchNo
        params.add(lastSettleDate);//結帳日期
        DbUtil.sqlAction(sqlTermBatch, params, conn);
        logger.info("update TermBatch:"+String.format(sqlTermBatchLog, fullFileName, mid, tid, batchNo, lastSettleDate));        
    }
    
    public boolean checkTxnIsSettle(String mid, String tid, String batchNo) throws SQLException
    {
        String sqlCmd = "select count(1) from tb_onl_txn where merch_id = ? and term_id = ? and batch_no = ? and term_settle_date is null";
        Vector<String> parm = new Vector<String>();
        parm.add(mid);
        parm.add(tid);
        parm.add(batchNo);
        Number nbr =  DbUtil.getNumber(sqlCmd, parm, conn);
        int cnt = 0;
        
        if(null != nbr)
            cnt = nbr.intValue();
        
        return cnt == 0? true:false;
    }
    
    
    public void settleOnlTxn(String mid, String tid, String batchNo, String settleDate, String settleTime) throws SQLException
    {
        String sqlUpdateTxnSettleDateTime = "Update TB_ONL_TXN set TERM_SETTLE_DATE = ?, TERM_SETTLE_TIME = ? Where merch_id=? and term_id=? and batch_no=? and term_settle_date is null and term_settle_time is null";
        String sqlUpdateTxnSettleDateTimeWEH = "Update TB_ONL_TXN_ERR set TERM_SETTLE_DATE = ?, TERM_SETTLE_TIME = ? Where merch_id=? and term_id=? and batch_no=? and term_settle_date is null and term_settle_time is null and ERR_TYPE= 'A' and onl_rcode in (select rcode from tb_rcode where err_handle_flag=1) ";
        
        Vector<String> parm = new Vector<String>();
        parm.add(settleDate);
        parm.add(settleTime);
        parm.add(mid);
        parm.add(tid);
        parm.add(batchNo);
        
        DbUtil.sqlAction(sqlUpdateTxnSettleDateTime, parm, conn);
        //error handling
        DbUtil.sqlAction(sqlUpdateTxnSettleDateTimeWEH, parm, conn);
    }
}
