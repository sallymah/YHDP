package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl;

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
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Channel;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.online.ISOMsg;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
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
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.FF11;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.FF21;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.ImpFileInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.MsgUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSProcCode;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.LmsDbUtil;
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
    private final static String DETAIL_KEY = "D01"; //D01
    private final static int TRIM_DATA_LEN = 217;
    
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
        BATCHContext ctx = new BATCHContext();
        ctx.setTbInctlInfo(impFileInfo.getInctlInfo());
        Date curDate = new Date();
        String rcode = "0000";
        List descInfos = null;   
        String lineInfoStr = lineInfo.getPlainLine();
        String parserStr = "";
        int index = 0;
        if(lineInfoStr.substring(0, DETAIL_KEY.length()).equals(DETAIL_KEY))//????????? D01????????????
        {
            parserStr = lineInfoStr.substring(TRIM_DATA_LEN);//??????????????????
            index = parserStr.indexOf("FF11");
            if(index == -1)
            {
                logger.warn("cant find FF11");
            }
        }
        else
        {
            parserStr = lineInfoStr;
        }

        ctx.setRawData(parserStr);//??????ff10??????raw data
        BerTLV berTLV = BerTLV.createInstance(ISOUtil.hex2byte(parserStr));
        
        logger.info(""+berTLV);
        FF11 ff11 = new FF11(MsgUtil.FF11_SIZE, berTLV.getHexStr(0xFF11));
        logger.info("ff11:"+ff11);
        FF21 ff21 = new FF21(MsgUtil.FF21_SIZE, berTLV.getHexStr(0xFF21));
        logger.info("ff21:"+ff21);
        
        Properties p = new Properties();
        p.put("48", "");
        p.put("60", "");
        p.put("61", "");
        p.put("63", "");
        ctx.setTlvFields(p);
        ctx.setTimeoutValue(0);//?????????timeout
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
        
        ctx.setTimeTxInit(termDate);//hostDate termDateTime ????????????(???????????????????????????????????????)
        ctx.setTimeTxExec(curDate);
        ctx.setTxnSrc(TXN_SRC);
        
        //logger.debug("raw date:"+parserStr);
        ctx.setDongleRespCode(ff21.getRespCode());
        if(berTLV.hasTag(0xFF10))
        {
            ctx.setFileTxnStatus(berTLV.getStr(0xFF10));
            berTLV.delByTag(0xFF10);
            ctx.setRawData(ISOUtil.hexString(berTLV.pack()));//??????ff10??????raw data
        }
        //logger.debug("pack:"+ISOUtil.hexString(berTLV.pack()));
        
        /* parser */
        BerTLV onlTLV = MsgUtil.batch2online(berTLV);
        ctx.setLMSMsg(onlTLV);

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
        
        ISOMsg isoMsg = ctx.getIsoMsg();
        if(Integer.valueOf(ff11.getPcode()) == LMSProcCode.SALE || 
                Integer.valueOf(ff11.getPcode()) == LMSProcCode.REDEEM_ALL ||
                Integer.valueOf(ff11.getPcode()) == LMSProcCode.REDEEM_PART ||
                Integer.valueOf(ff11.getPcode()) == LMSProcCode.REDEEM ||
                Integer.valueOf(ff11.getPcode()) == LMSProcCode.SALE_FEE ||
                Integer.valueOf(ff11.getPcode()) == LMSProcCode.TRANSFER_REDEEM ||
                Integer.valueOf(ff11.getPcode()) == LMSProcCode.REDEEM_COUPON ||
                Integer.valueOf(ff11.getPcode()) == LMSProcCode.BLACK_LIST)
        {
            isoMsg.setMTI("0320");
            isoMsg.set(Field.PROC_CODE, "900000");
        }
        else if(Integer.valueOf(ff11.getPcode()) == LMSProcCode.OFFLINE_AUTH_RELOAD)
        {
            isoMsg.setMTI("0200");
            isoMsg.set(Field.PROC_CODE, "888888");
        }
        else
        {
            if(checkIsReversal(ctx, ff11.getPcode()))
            {
                isoMsg.setMTI("0400");//??????reversal
                isoMsg.set(Field.PROC_CODE, "888888");
            }
            else
            {
                isoMsg.setMTI("0200");//?????????????????????????????????
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
       
        if(!StringUtil.isEmpty(ff21.getAutoLoadAmt()) && !ff21.getAutoLoadAmt().equals("0000000000"))
        {
            ctx.setAutoLoadAtc(ff21.getAutoLoadAtc());
            ctx.setAutoLoadAmt(ff21.getAutoLoadAmt());
        }

        BerTLV lmsMsg = ctx.getLMSMsg();
        
        logger.info("isoMsg:"+isoMsg);
        logger.info("lmsMsg:"+lmsMsg);
        
        LMSContext respCtx = null;
        try{
            respCtx = (LMSContext) ctxListener.onMessage(ctx);
        }catch(Exception txe){
            logger.error(txe);
			throw txe;
        }
        String reqRcode = respCtx.getRcode();
        logger.info("reqRcode:"+reqRcode);
        if(!reqRcode.equals("0000"))
        {
            descInfos = new ArrayList();
            ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(reqRcode, null, "");
            descInfos.add(descInfo);
        }
        insertTmpTrans(ff21, lineInfo, inctlInfo, ctx.getRcode(), ctx.getRawData());
        return descInfos;
    }
    
    public boolean checkIsReversal(BATCHContext ctx, String pcode) throws SQLException
    {
        if(Integer.valueOf(pcode) != LMSProcCode.SAM_LOGIN1 && Integer.valueOf(pcode) != LMSProcCode.SAM_LOGIN2 &&
                Integer.valueOf(pcode) != LMSProcCode.CARD_REGISTER && Integer.valueOf(pcode) != LMSProcCode.SETTLEMENT &&
                        Integer.valueOf(pcode) != LMSProcCode.PARAMETERS_DOWNLOAD && Integer.valueOf(pcode) != LMSProcCode.INQUERY_CHIP_BAL)
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
            boolean ret = impFileInfo.checkFile(ImpFileInfo.PARSER_HEX);
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
            int index = -1;
            String parserStr = "";
            for (int i = start; i < totRec; i++) {
                logger.debug("start:"+start);
                descInfos = new ArrayList<ErrorDescInfo>();
                DataLineInfo lineInfo = null;
                String lineInfoStr = null;
                isCheckDataLineFail = false;
                logger.info("---------handle line no:"+i+"-------------");
                try {
                    lineInfo = impFileInfo.readOneDataLineForFm();
                    lineInfoStr=lineInfo.getPlainLine();
                    
                    if(lineInfoStr.substring(0, DETAIL_KEY.length()).equals(DETAIL_KEY))//????????? D01????????????
                    {
                        parserStr = lineInfoStr.substring(TRIM_DATA_LEN);//??????????????????
                        index = parserStr.indexOf("FF11");
                        if(index == -1)
                        {
                            logger.warn("cant find FF11");
                        }
                        
                        lineInfoStr = parserStr;
                        logger.debug(lineInfoStr);
                        BerTLV tlv = BerTLV.createInstance(ISOUtil.hex2byte(lineInfoStr));
                        if (tlv == null) {// ?????????
                            ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "parse TLV error");
                            descInfos.add(descInfo);
                            rcode = Layer1Constants.RCODE_2709_FORMAT_ERR;
                        }else{
                            if (!tlv.hasTag(0xFF11) || !tlv.hasTag(0xFF21) ||!tlv.hasTag(0xFF64)){
                                ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "lack mandatory tag FF11 FF21 FF64");
                                logger.error("descInfos"+descInfos+", descInfo:"+descInfo);
                                descInfos.add(descInfo);
                                rcode = Layer1Constants.RCODE_2709_FORMAT_ERR;
                            }
                            if (tlv.hasTag(0xFF11) && tlv.getHexStr(0xFF11).length()!=MsgUtil.FF11_SIZE){
                                ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "FF11 length err.("+ tlv.getHexStr(0xFF11).length() + " != " + MsgUtil.FF11_SIZE);
                                logger.error("descInfos"+descInfos+", descInfo:"+descInfo);
                                logger.error("FF11:"+tlv.getHexStr(0xFF11));
                                descInfos.add(descInfo);
                                rcode = Layer1Constants.RCODE_2709_FORMAT_ERR;
                            }
                            if (tlv.hasTag(0xFF21) && tlv.getHexStr(0xFF21).length()!=MsgUtil.FF21_SIZE){
                                ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "FF21 length err.("+ tlv.getHexStr(0xFF21).length() + " != " + MsgUtil.FF21_SIZE);
                                logger.error("descInfos"+descInfos+", descInfo:"+descInfo);
                                logger.error("FF21:"+tlv.getHexStr(0xFF21));
                                descInfos.add(descInfo);
                                rcode = Layer1Constants.RCODE_2709_FORMAT_ERR;
                            }                       
                            if (tlv.hasTag(0xFF64) && tlv.getHexStr(0xFF64).length()!=MsgUtil.FF64_SIZE){
                                ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(Layer1Constants.RCODE_2709_FORMAT_ERR, null, "FF64 length err.("+ tlv.getHexStr(0xFF64).length() + " != " + MsgUtil.FF64_SIZE);
                                logger.error("descInfos"+descInfos+", descInfo:"+descInfo);
                                logger.error("FF64:"+tlv.getHexStr(0xFF64));
                                descInfos.add(descInfo);
                                rcode = Layer1Constants.RCODE_2709_FORMAT_ERR;
                            }
                        }
                        
                        if (descInfos != null && descInfos.size() > 0) {
                            if (doCommit) {
                                insertInctlErrInfo(lineInfo, descInfos);
                                insertTmpTrans(null, lineInfo, inctlInfo, rcode, null);
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
                        else {
                            // ??????????????????????????????, ????????????????????????
                            ;
                        }
                    }
                    else
                    {
                        logger.debug("not detail pass");
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
                        conn.rollback();
                        insertInctlErrInfo(lineInfo, descInfo);
                        insertTmpTrans(null, lineInfo, inctlInfo, Layer1Constants.RCODE_2791_GENERALDB_ERR, null);
                        conn.commit();
                    }
                    else {
                        // ??????????????????????????????
                        // ?????? rollback, ??? TB_INCTL_ERR ?????????????????????, ?????????
                        conn.rollback();
                        insertInctlErrInfo(lineInfo, descInfo);
                        insertTmpTrans(null, lineInfo, inctlInfo, Layer1Constants.RCODE_2791_GENERALDB_ERR, null);
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
    
    public boolean checkIsTermBatch(BATCHContext ctx, TbTermBatchInfo tbTermBatch) throws SQLException
    {
        String sqlCmd = "select count(1) from tb_term_batch where term_id = ? and merch_id = ? and batch_no = ? and term_settle_date = ?";
        Vector<String> parms = new Vector<String>();
        parms.add(tbTermBatch.getTermId());
        parms.add(tbTermBatch.getMerchId());
        parms.add(tbTermBatch.getBatchNo());
        parms.add(tbTermBatch.getTermSettleDate());
        int  count = DbUtil.getInteger(sqlCmd, parms, ctx.getConnection());        
        return count > 0 ? true : false;
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
        int isCpc = 0;
        BerTLV tlv = ctx.getLMSMsg();
        String betchNo = "";
        String merchId = ctx.getLmsMerchantId();
        String termId = ctx.getLmsTerminalId();
        if(inctlInfo.getFileName().equals("ImpFmTxnDtl"))//??????
        {
            betchNo = tlv.getHexStr(LMSTag.BatchNumber);
            isCpc = 1;
        }
        
        if(inctlInfo.getFileName().equals("IMPFMTXNDTL"))//??????
        {
            betchNo = getMaxBatchNo(ctx, merchId, termId);//?????????????????????????????????online??????,????????????
            isCpc = 2;
        }

        if(isCpc > 0)
        {
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
            if(!checkIsTermBatch(ctx, tbTermBatch))
            {
                TbTermBatchMgr termBatchMgr = new TbTermBatchMgr(conn);
                try {
                    termBatchMgr.insert(tbTermBatch);
                } catch (SQLException e) {
                    logger.error(tbTermBatch.toInsertSQL());
                    throw e;
                }
            }
            
            if(checkTxnIsSettle(merchId, termId, batchNo))//onl_txn???????????????????????????
            {
                settleOnlTxn(merchId, termId, batchNo, settleDate, settleTime);
            }
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
    
    public void insertTmpTrans(FF21 ff21, DataLineInfo lineInfo, TbInctlInfo inctlInfo, String rcode, String rawData) throws SQLException
    {
        String dateTime = DateUtil.getTodayString();
        TbTmpTransInfo tmpTransInfo = new TbTmpTransInfo();
        String seqno = inctlInfo.getSeqno();
        String sepFileSeq = "";
        if(seqno.length() == 4)
        {
            sepFileSeq = seqno.substring(2, 4);
            tmpTransInfo.setFullFileName(inctlInfo.getFullFileName().substring(0, inctlInfo.getFullFileName().length() - 2));
        }
        else
        {
            sepFileSeq = inctlInfo.getFullFileName().substring(inctlInfo.getFullFileName().length() - 2, inctlInfo.getFullFileName().length());
            tmpTransInfo.setFullFileName(inctlInfo.getFullFileName().substring(0, inctlInfo.getFullFileName().length() - 2));
        }
        
        TbTmpTransMgr tmpTransMgr = new TbTmpTransMgr(conn);
        tmpTransInfo.setFileName(inctlInfo.getFileName());
        String plainLin = lineInfo.getPlainLine();
        tmpTransInfo.setLineData(plainLin);
        tmpTransInfo.setImpDate(dateTime.substring(0, 8));
        tmpTransInfo.setImpTime(dateTime.substring(8, 14));
        tmpTransInfo.setSeqno(lineInfo.getLineNo());
        tmpTransInfo.setSepFileSeqno(Integer.valueOf(sepFileSeq));
        if(!rcode.equals("0000"))
        {
            if(null != ff21 && null != ff21.respCode)
            {
                if(ff21.respCode.equalsIgnoreCase(rcode))//???????????????????????????,??????????????????????????????????????????,??????????????????????????????
                {
                    rcode = "0000";
                }
            }
        }
        tmpTransInfo.setRcode(rcode);
        tmpTransMgr.insert(tmpTransInfo);
    }
    
    public void merchTermFiller(BATCHContext ctx, FF21 ff21)
    {
        String locId = ff21.getTid();
        String mid = ff21.getMid();
        ISOMsg isoMsg = ctx.getIsoMsg();
        StringBuffer ecaMerchId = new StringBuffer();
        
        ecaMerchId.append(mid.substring(8));
        ecaMerchId.append(locId);
        
        isoMsg.set(Field.MERCHANT_ID, ecaMerchId.toString());
    }
}
