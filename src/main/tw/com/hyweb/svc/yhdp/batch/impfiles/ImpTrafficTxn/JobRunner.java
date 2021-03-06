package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn;

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
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.core.cp.online.loyalty.util.Field;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Channel;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.online.ISOMsg;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTmpTransInfo;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.service.db.mgr.TbTmpTransMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.InctlBean;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.AbstractImpFile;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.ImpFileInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficDataFormat;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxn;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnDetail;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnHeader;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.MsgUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSProcCode;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.ProcCode;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISODate;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
* JobRunner:???raw data byte[]????????????????????????Thread??????,?????????Runnable????????????????????????
* ??????????????????,????????????raw data byte[]?????????Context??????????????????
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
    private String batchNo = null;
    private boolean doReject = false;
    private Hashtable<String,String> inctlResultMap = null; 
    IContextListener ctxListener;
    private TrafficTxnHeader trafficHd;
    String betchNo = "";
    
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
     * ????????????raw data byte[]?????????Context??????????????????
     */
    public void run()
    {
        this.timeJobExec = new Date();
        
        try {
            logger.debug(impFileInfo.getInctlInfo().getFullFileName() + " is begin working");
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
        /* INCRL???????????????????????????THREAD??????????????????????????????MAIN THREAD */
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
        logger.debug(impFileInfo.getInctlInfo().getFullFileName() + " is working end");
    }
    
    @Override
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
        logger.debug(lineInfo.getPlainLine());
        BATCHContext ctx = new BATCHContext();
        if(null != trafficHd)
        {
            ctx.setTrafficTxnHeader(trafficHd);//????????????
        }
        ctx.setTbInctlInfo(impFileInfo.getInctlInfo());
        String rcode = "0000";
        List descInfos = null;
        String nowDate = "";
        this.timeJobInit = new Date();
        nowDate = ISODate.formatDate(timeJobInit, "yyyyMMdd");
  
        String lineStr = lineInfo.getPlainLine();
        
        BerTLV berTLV = BerTLV.createInstance(ISOUtil.hex2byte("FF290720150901095806"));
        logger.info(""+berTLV);
        
        Properties p = new Properties();
        p.put("48", "");
        p.put("60", "");
        p.put("61", "");
        p.put("63", "");
        ctx.setTlvFields(p);
        ctx.setTimeoutValue(0);//?????????timeout
        ctx.setConnection(conn);
        
        String termDateTime = DateUtil.getShortTodayString();
        Date termDate;
        if(!termDateTime.equals("00000000000000"))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            termDate = sdf.parse(termDateTime);
        } 
        else
        {
            termDate = timeJobInit;
        }
        
        ctx.setTimeTxInit(termDate);
        ctx.setTimeTxExec(timeJobInit);
        ctx.setTxnSrc(TXN_SRC);
        ctx.setRawData(lineStr);

        /* parser */
        BerTLV onlTLV = MsgUtil.batch2online(berTLV);
        ctx.setLMSMsg(onlTLV);

        ISOMsg isoMsg = ctx.getIsoMsg();
        if(lineStr.substring(0, 4).equals(TrafficTxn.ITEM_HEADER)) 
        {
            isoMsg.setMTI("0200");
            isoMsg.set(Field.PROC_CODE, "888888");
        }
        else
        {
            isoMsg.setMTI("0320");
            isoMsg.set(Field.PROC_CODE, "900001");
        }
        
        String lmsInvoiceNo = null;
        try {
            lmsInvoiceNo  = getLmsInvoiceNo(conn, nowDate);
        }catch (Exception e) {
            logger.error("batchDate:"+ termDateTime + " timeJobInit:"+timeJobInit);
            throw e;
        }
        onlTLV.addHexStr(LMSTag.LMSInvoiceNumber, lmsInvoiceNo);
        
        BerTLV lmsMsg = ctx.getLMSMsg();
        
        logger.info("isoMsg:"+isoMsg);
        logger.info("lmsMsg:"+lmsMsg);
        
        LMSContext respCtx = null;
        try{
            respCtx = (LMSContext) ctxListener.onMessage(ctx);
        }catch(Exception txe){
            logger.error(txe);
        }
        String reqRcode = respCtx.getRcode();
        logger.info("reqRcode:"+reqRcode);
        if(!reqRcode.equals("0000"))
        {
            descInfos = new ArrayList();
            ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(reqRcode, null, "");
            descInfos.add(descInfo);
        }
        String cardNo = null;
        if(null != respCtx &&  null != respCtx.getCardInfo())
        {
            cardNo = respCtx.getCardInfo().getCardNo();
        }
        insertTmpTrans(ctx, lineInfo, inctlInfo, ctx.getRcode(), cardNo, lmsInvoiceNo);
        return descInfos;
    }

    public static String getLmsInvoiceNo(Connection conn, String batchDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        String yy = batchDate.substring(2, 4);
        String jjj = StringUtil.pendingKey(DateUtil.getDayOfYear(batchDate), 3);
        String nnnnnn = SequenceGenerator.getSequenceString(conn, "SEQ_BATCH_LMSINVOICENO", 6);
        return "B" + yy + jjj + nnnnnn;
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
     * (non-Javadoc) ??????tb_inctl???file
     * @see tw.com.hyweb.svc.cp.batch.impfiles.ImpTrans.AbstractImpFile#handleImpFileInfo()
     */
    public boolean handleImpFileInfo() throws Exception {
        boolean autoCommit = false;
        try {
            conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            // ??? workFlag ???????????????????????????
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
            // ??? batchResultInfo ??? inctlInfo ??????, ??? markup inctlInfo start
            
            logger.debug("inctlInfo :" +impFileInfo.getTotRec());
            logger.debug("inctlInfo :" +inctlInfo);
            inctlInfo = inctlBean.makeInctl(inctlInfo);
            updateInctl(Layer1Constants.WORKFLAG_PROCESSING, true);
            
            logger.debug("updateInctl :" +impFileInfo.getInctlInfo().getFullFileName());
            
            // checkFile
            logger.debug("impFileInfo.checkFile()...");
            boolean ret = impFileInfo.checkFile(ImpFileInfo.PARSER_TRN);
            if (!ret) {
                // checkFile fail, ????????????, ?????????
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
                    // ??????????????????????????????
                    try {
                        ExecuteSqlsUtil.executeSqls(conn, beforeSqlsInfo);
                    }catch (Exception ignore) {
                        // ??? rollback ????????? TB_INCTL ???????????????
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
            int totRec = impFileInfo.getTotRec();
                
            logger.debug("TotRec:"+totRec);
            String rcode = "";
            for (int i = start; i < totRec; i++) {
                descInfos = new ArrayList<ErrorDescInfo>();
                DataLineInfo lineInfo = null;
                String lineInfoStr = null;
                isCheckDataLineFail = false;
                logger.info("---------handle line no:"+i+"-------------");
                try {
                    lineInfo = impFileInfo.readOneDataLine();
                    lineInfoStr=lineInfo.getPlainLine();
                    logger.debug(lineInfoStr);
                    
                    String msg = "";
                    /* check header */
					/*
					 * if(!lineInfoStr.substring(0, 4).equals(TrafficTxn.ITEM_HEADER) &&
					 * !lineInfoStr.substring(0, 4).equals(TrafficTxn.ITEM_BUS) &&
					 * !lineInfoStr.substring(0, 4).equals(TrafficTxn.ITEM_BUS_STEP) &&
					 * !lineInfoStr.substring(0, 4).equals(TrafficTxn.ITEM_PARK)) { msg =
					 * "item:"+lineInfoStr.substring(0, 4) + " is not 01,10,11,12"; ErrorDescInfo
					 * descInfo =
					 * ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null,
					 * msg); logger.error("descInfos"+descInfos+", descInfo:"+descInfo);
					 * descInfos.add(descInfo); rcode = Layer1Constants.RCODE_2709_FORMAT_ERR; }
					 */
                    
                    if (descInfos != null && descInfos.size() > 0) {
                        if (doCommit) {
                            insertInctlErrInfo(lineInfo, descInfos);
                            insertTmpTrans(null, lineInfo, inctlInfo, rcode, "", "");
                            isCheckDataLineFail = true;
                        }
                        else {
                            // ??????????????????????????????
                            // ?????? rollback, ??? TB_INCTL_ERR ?????????????????????, ?????????
                            conn.rollback();
                            insertInctlErrInfo(lineInfo, descInfos);
                            break;
                        }
                    }
                    
                    if(isCheckDataLineFail == false)
                    {
                        if (descInfos != null && descInfos.size() == 0) {
                            // handleDataLine
                            List<ErrorDescInfo> tDescInfos = handleDataLine(lineInfo);
                            if (tDescInfos!=null){
                                descInfos.addAll(tDescInfos);
                            }
                        }
                        
                        if (descInfos != null && descInfos.size() > 0) {
                            // checkDataLine fail
                            // set failCnt and insert inctlErrInfo
                            if (doCommit) {
                                insertInctlErrInfo(lineInfo, descInfos);
                            }
                            else {
                                // ??????????????????????????????
                                // ?????? rollback, ??? TB_INCTL_ERR ?????????????????????, ?????????
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
                        // ??????????????????????????????
                        // ?????? rollback, ??? TB_INCTL_ERR ?????????????????????, ?????????
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
     * ??????????????????????????????????????????
     * @param ctx incoming context
     */
    protected void processGarbage(Context ctx)
    {
        logger.info(ctx+"header:"+ISOUtil.hexString(header)+" body:"+ISOUtil.hexString(body));
    }
    

    /**
     * @return ?????? doReject???
     */
    public boolean isDoReject()
    {
        return this.doReject;
    }

    /**
     * @param bReject ???????????? doReject???
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
     * @param timeJobExec ???????????? timeJobExec
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
     * @param timeJobInit ???????????? timeJobInit
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
        
    public boolean checkTxnIsSettle(String mid, String tid, String batchNo) throws SQLException
    {
        String sqlCmd = "select count(1) from tb_onl_txn where merch_id = ? and term_id = ? and batch_no = ? and term_settle_date is null";
        Vector<String> parm = new Vector<String>();
        parm.add(mid);
        parm.add(tid);
        parm.add(batchNo);
        int count =  DbUtil.getInteger(sqlCmd, parm, conn);        
        logger.debug("count:"+count);
        return count > 0 ? true: false;
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
    
    public void InsertTermBatchCpcProc(BATCHContext ctx) throws Exception
    {
        
        BerTLV tlv = ctx.getLMSMsg();       
        String merchId = ctx.getLmsMerchantId();
        String termId = ctx.getLmsTerminalId();

        batchNo = getMaxBatchNo(ctx, merchId, termId);
        //???????????????,????????????            
        String fileName = this.inctlInfo.getFullFileName();
        String settleDate = DateUtils.getSystemDate();
        String settleTime = DateUtils.getSystemTime();
        
        TbTermBatchInfo tbTermBatch = new TbTermBatchInfo();
        tbTermBatch.setTxnSrc("B");
        tbTermBatch.setMerchId(merchId);
        tbTermBatch.setTermId(termId);
        tbTermBatch.setTermSettleDate(settleDate);
        tbTermBatch.setTermSettleTime("000000");
        tbTermBatch.setStatus("1");
        tbTermBatch.setInfile(fileName);
        tbTermBatch.setParDay(tbTermBatch.getTermSettleDate().substring(6, 8));
        tbTermBatch.setParMon(tbTermBatch.getTermSettleDate().substring(4, 6));
        tbTermBatch.setTermUpDate(DateUtils.getSystemDate());
        tbTermBatch.setImpFileName(inctlInfo.getFullFileName());
        tbTermBatch.setTermSettleFlag("1");
        tbTermBatch.setBatchNo(betchNo);
        
        /* ???????????????????????????????????????tb_term_batch */
        TbTermBatchMgr termBatchMgr = new TbTermBatchMgr(conn);
        try {
            termBatchMgr.insert(tbTermBatch);
        } catch (SQLException e) {
            logger.error(tbTermBatch.toInsertSQL());
            throw e;
        }
        
        if(checkTxnIsSettle(merchId, termId, batchNo))//onl_txn???????????????????????????
        {
            settleOnlTxn(merchId, termId, batchNo, settleDate, settleTime);
        }
        conn.commit();
    }
    
    public String getMaxBatchNo(BATCHContext ctx, String mid, String tid) throws SQLException
    {
        String batchNo = null;
        String termSettleDate;
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select batch_no, term_settle_date from tb_term_batch");
        sqlCmd.append(" where batch_no in (select max(batch_no) from tb_term_batch where merch_id = ? and term_id = ? and (substr(BATCH_NO, 1, 1)>='0' and substr(BATCH_NO, 1, 1)<='9')))");
        sqlCmd.append(" and merch_id = ? and term_id = ? and (substr(BATCH_NO, 1, 1)>='0' and substr(BATCH_NO, 1, 1)<='9')");
        Vector<String> parms = new Vector<String>();
        parms.add(mid);
        parms.add(tid);
        parms.add(mid);
        parms.add(tid);
        Vector result = DbUtil.select(sqlCmd.toString(), parms, ctx.getConnection());
        if(null != result && result.size() > 0)
        {
            Vector tmp = (Vector)result.get(0);
            if(null != tmp)
            {
                batchNo = (String) tmp.get(0);
                termSettleDate = (String) tmp.get(1);
                String currDate = ISODate.getANSIDate(ctx.getTimeTxExec());
                
                if(Integer.valueOf(termSettleDate) < Integer.valueOf(currDate))
                {
                    batchNo = ISOUtil.padLeft(String.valueOf((Integer.valueOf(batchNo) + 1)), 6, '0') ;
                    if(batchNo.length() > 6)
                    {
                        batchNo = "000001";
                    }
                }
            }
        }
        else
        {
            batchNo = "000001";
        }
        return batchNo;
    }
    
    public void insertTmpTrans(BATCHContext ctx, DataLineInfo lineInfo, TbInctlInfo inctlInfo, String rcode, String cardNo, String lmsInvoiceNo) throws SQLException
    {
        String dateTime = DateUtil.getTodayString();
        TbTmpTransInfo tmpTransInfo = new TbTmpTransInfo();
        String seqno = inctlInfo.getSeqno();
        tmpTransInfo.setFileName(inctlInfo.getFileName());
        tmpTransInfo.setFullFileName(inctlInfo.getFullFileName());
        tmpTransInfo.setLineData(lineInfo.getPlainLine());
        tmpTransInfo.setImpDate(dateTime.substring(0, 8));
        tmpTransInfo.setImpTime(dateTime.substring(8, 14));
        tmpTransInfo.setSeqno(lineInfo.getLineNo());
        tmpTransInfo.setSepFileSeqno(Integer.valueOf(seqno));
        tmpTransInfo.setRcode(rcode);
        tmpTransInfo.setAprvDate(dateTime.substring(0, 8));
        tmpTransInfo.setAprvTime(dateTime.substring(8, 14));
        tmpTransInfo.setAprvUserid("BATCH");
        tmpTransInfo.setUptDate(dateTime.substring(0, 8));
        tmpTransInfo.setUptTime(dateTime.substring(8, 14));
        tmpTransInfo.setUptUserid("BATCH");
        
        if(!StringUtil.isEmpty(cardNo))
        {
            tmpTransInfo.setCardNo(cardNo);
            tmpTransInfo.setLmsInvoiceNo(lmsInvoiceNo);
        }
         
        if(!StringUtil.isEmpty(tmpTransInfo.getLineData()) && !tmpTransInfo.getLineData().substring(0, 4).equals(TrafficTxn.ITEM_HEADER))
        {
            if(null != ctx)
            {
                BerTLV tlv = ctx.getLMSMsg();
                TbMerchInfo merchInfo = ctx.getMerchInfo();
                if(null != ctx && rcode.equals(Rcode.MAC_ERROR))
                {          
                    TrafficTxnDetail trafficDtl = ctx.getTrafficTxnDetail();
                    logger.debug("atc:"+tlv.getHexStr(LMSTag.ICCATC));
                    tmpTransInfo.setTransNo(tlv.getHexStr(LMSTag.ICCATC));
                    String txnDate = tlv.getHexStr(LMSTag.TerminalTransactionDateTime);
                    tmpTransInfo.setPCode(ctx.getLmsPcode());
                    tmpTransInfo.setTxnDate(txnDate.substring(0, 8));
                    tmpTransInfo.setTxnTime(txnDate.substring(8));
                    tmpTransInfo.setAfterAmt(ctx.getChipAfterBonusQty());
                    tmpTransInfo.setTxnAmt(ctx.getLmsAmt());
                    
                    if(null != merchInfo)
                    {
                        tmpTransInfo.setAcqMemId(merchInfo.getMemId());
                    }
                    
                    if(null != trafficDtl)
                    {
                        tmpTransInfo.setLocId(trafficDtl.getRawDataRecode(TransferUtil.LOC_ID));
                        tmpTransInfo.setTransSysNo(trafficDtl.getDataRecode(TransferUtil.TRANS_SYS_NO));
                        tmpTransInfo.setDevId(trafficDtl.getDataRecode(TransferUtil.DEV_ID));
                        tmpTransInfo.setSamOsn(trafficDtl.getDataRecode(TransferUtil.SAM_OSN));
                        tmpTransInfo.setSamTransSeq(trafficDtl.getDataRecode(TransferUtil.SAM_TRANS_SEQ));
                        tmpTransInfo.setTransType(trafficDtl.getDataRecode(TransferUtil.TRANS_TYPE));
                    }
                }
            }
        }
        else {
            trafficHd = ctx.getTrafficTxnHeader();
        } 
        TbTmpTransMgr tmpTransMgr = new TbTmpTransMgr(conn);
        tmpTransMgr.insert(tmpTransInfo);
    }
}
