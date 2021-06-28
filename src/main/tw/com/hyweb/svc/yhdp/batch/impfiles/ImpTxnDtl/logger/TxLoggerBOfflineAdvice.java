package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnErrInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbOnlTxnDtlMgr;
import tw.com.hyweb.service.db.mgr.TbOnlTxnErrMgr;
import tw.com.hyweb.service.db.mgr.TbOnlTxnMgr;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.logger.TxLoggerOfflineAdvice;
import tw.com.hyweb.svc.yhdp.online.util.Constant;
import tw.com.hyweb.svc.yhdp.online.util.Field;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.ProcCode;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.tag.AwardCouponList;
import tw.com.hyweb.svc.yhdp.online.util.tag.BonusSummaryInfo;
import tw.com.hyweb.svc.yhdp.online.util.tag.OfflineUploadTxnInfo;
import tw.com.hyweb.svc.yhdp.online.util.tag.TagFF24;
import tw.com.hyweb.svc.yhdp.online.util.tag.TagFF34;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;

public class TxLoggerBOfflineAdvice extends TxLoggerOfflineAdvice
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(TxLoggerBOfflineAdvice.class);

    /**
     *
     */
    public TxLoggerBOfflineAdvice()
    {
    }

    /**
     * Record tx log by business rule
     * @param ctxResp
     * @throws SQLException
     */
    @Override
    public void insert(Context ctxResp) throws SQLException
    {
        logger.debug("insert(Context ctxResp)");
        LMSContext lmsCtxResp = (LMSContext) ctxResp;
        LMSContext lmsCtxReq = (LMSContext) lmsCtxResp.getOrgCtx();//get request context
        if (lmsCtxReq==null)
        {
            logger.debug("lmsCtxResp.getOrgCtx() is null");
            lmsCtxReq = lmsCtxResp;
        }
        TbOnlTxnInfo onlTxnInfo = lmsCtxReq.getOnlTxnInfo();
        TbOnlTxnMgr onlTxnMgr = new TbOnlTxnMgr(lmsCtxReq.getConnection());
        TbOnlTxnDtlInfo[] onlTxnInfoDtl = lmsCtxReq.getOnlTxnDtlInfo();
        TbOnlTxnDtlMgr onlTxnDtlMgr = new TbOnlTxnDtlMgr(lmsCtxReq.getConnection());
        if (onlTxnInfo!=null)
        {
            logger.debug("onlTxnMgr.insert(onlTxnInfo)");
            //onlTxnInfo.setResponseTime(Long.toString(System.currentTimeMillis() - lmsCtxReq.getTimeTxExec().getTime()));
            try{
                onlTxnMgr.insert(onlTxnInfo);
            }
            catch(SQLException sqle)
            {
                if (sqle.getSQLState() != null && sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
                {
                    //判斷SQLCODE 重複才不處理
                    logger.debug("don't care duplicate");
                    lmsCtxResp.setRcode(Rcode.OK);
                }
                else
                {
                    logger.error(onlTxnInfo.toString());
                    throw sqle;
                }
            }
            lmsCtxReq.setOnlTxnInfo(null);
        }
        if (onlTxnInfoDtl!=null)
        {
            logger.debug("onlTxnDtlMgr.insert(onlTxnInfoDtl[i])");
            for (int i = 0;i<onlTxnInfoDtl.length;i++)
            {
                try{
                    onlTxnDtlMgr.insert(onlTxnInfoDtl[i]);
                }
                catch(SQLException sqle)
                {
                    if (sqle.getSQLState() != null && sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
                    {
                        //判斷SQLCODE 重複才不處理
                        logger.debug("don't care duplicate");
                        lmsCtxResp.setRcode(Rcode.OK);
                    }
                    else
                    {
                        logger.error(onlTxnInfoDtl[i].toString());
                        throw sqle;
                    }
                }
            }
            lmsCtxReq.setOnlTxnDtlInfo(null);
        }
    }
    /**
     * 當訊息有錯誤時需要insert到TbOnlTxnERR,
     * 當insert TbOnlTxnERR也錯誤時advice和reversal必須要記錄訊息到檔案然後回應00
     * @param ctx
     * @throws SQLException
     */
    public void insertOnlTxnErr(LMSContext ctx)
    {
        if(null != ctx.getOfflineUploadTxnInfo())
        {
            logger.debug("insertOnlTxnErr");
            BATCHContext bctx = (BATCHContext)ctx;
            TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
            OfflineUploadTxnInfo uploadTxnInfo = ctx.getOfflineUploadTxnInfo();
            
            TbTermBatchInfo termBatch = bctx.getTermBatchInfo();
            BerTLV tlv = ctx.getLMSMsg();
            
            String termId = ctx.getLmsTerminalId();
            String merchId = ctx.getLmsMerchantId();
            String cardNo = uploadTxnInfo.getCardNo();
            String expiryDate = uploadTxnInfo.getExpiryDate();
            String lmsInvoiceNo = uploadTxnInfo.getLmsInvoiceNo();
            
            
            String pCode = String.valueOf(uploadTxnInfo.getPcode());
    
            String storedata = tlv.getStr(LMSTag.StoreCounterArea);
            TagFF24 tagFF24 = (storedata != null) ? new TagFF24(storedata) : null;
            String batchNumber = uploadTxnInfo.getBatchNo();
    
            TbOnlTxnErrInfo onlTxnInfo = new TbOnlTxnErrInfo();
            onlTxnInfo.setLmsRawData(ctx.getRawData());
            onlTxnInfo.setTxnSrc(ctx.getTxnSrc());//
            onlTxnInfo.setOnlineFlag("F");//N: online transaction  F: offline transaction
    
            onlTxnInfo.setAcqMemId(ctx.getAcquireInfo() != null ? ctx.getAcquireInfo().getMemId() : "");
            onlTxnInfo.setIssMemId(ctx.getIssuerInfo() != null ? ctx.getIssuerInfo().getMemId() : "");
            onlTxnInfo.setMerchId(merchId);
            onlTxnInfo.setTermId(termId);
            if (null != tagFF24)
            {
                onlTxnInfo.setStoreCounterId(tagFF24.getStoreId());
                onlTxnInfo.setPosId(tagFF24.getPosId());
                onlTxnInfo.setPosSerno(tagFF24.getPosSerno());
            }
            onlTxnInfo.setCreditAuthAmt(ctx.getAuthAmt());// number (14,2) default 0
            onlTxnInfo.setTxnAmt(uploadTxnInfo.getTxnAmt());// number (14,2) default 0
            if(uploadTxnInfo.getPcode() == ProcCode.SALE)
            {
                onlTxnInfo.setTxnRedeemAmt(uploadTxnInfo.getTxnAmt());//number (14,2) default 0
            }
            onlTxnInfo.setTxnAccessMode(uploadTxnInfo.getAccessMode());
            onlTxnInfo.setAtc(uploadTxnInfo.getAtc());
            onlTxnInfo.setBatchNo(batchNumber);
            onlTxnInfo.setCardNo(cardNo);
            onlTxnInfo.setExpiryDate(expiryDate);
            onlTxnInfo.setLmsInvoiceNo(lmsInvoiceNo);
            onlTxnInfo.setPCode(pCode);
            onlTxnInfo.setTxnDate(ctx.getHostDate());
            onlTxnInfo.setTxnTime(ctx.getHostTime());
            onlTxnInfo.setTermDate(uploadTxnInfo.getTermDate());
            onlTxnInfo.setTermTime(uploadTxnInfo.getTermTime());
            
            onlTxnInfo.setInvoiceRefNo(ctx.getIsoMsg().getString(Field.TRACE_NBR));
            
            if (uploadTxnInfo.getOrigLmsInvoiceNo()!=null)
            {
                onlTxnInfo.setOrigLmsInvoiceNo(uploadTxnInfo.getOrigLmsInvoiceNo());
            }
            onlTxnInfo.setProcCode(ctx.getIsoMsg().getString(Field.PROC_CODE));
            onlTxnInfo.setOnlRcode(ctx.getRcode());
            onlTxnInfo.setStatus("1");
            //onlTxnInfo.setRegionId(ctx.getCardInfo().getRegionId());
            onlTxnInfo.setParMon(bctx.getBatchDate().substring(4,6));// for partition {01~12}
            onlTxnInfo.setParDay(bctx.getBatchDate().substring(6,8));// for partition {01~31}
            onlTxnInfo.setErrDesc("err desc");
            onlTxnInfo.setErrType("A");
            onlTxnInfo.setErrProcRcode("0000");
            onlTxnInfo.setImpFileDate(bctx.getBatchDate());
            onlTxnInfo.setImpFileTime(bctx.getBatchTime());
            onlTxnInfo.setImpFileName(inctlInfo.getFullFileName());
            String deviceId = tlv.getHexStr(LMSTag.DongleDeviceId);
            onlTxnInfo.setDeviceId(deviceId);
            onlTxnInfo.setTxnNote(ctx.getTxnNote());
            if(!StringUtil.isEmpty(bctx.getAutoLoadAtc()))
            {
                onlTxnInfo.setAutoloadAtc(bctx.getAutoLoadAtc());
            }
            if(!StringUtil.isEmpty(bctx.getAutoLoadAmt()))
            {
                onlTxnInfo.setAutoloadValues(Double.parseDouble(bctx.getAutoLoadAmt())/100);
            }
            if(null != inctlInfo)
            {
                onlTxnInfo.setImpFileName(inctlInfo.getFullFileName());
            }
            
            try
            {
                onlTxnInfo.setRespCode(Rcode.getRespCode(ctx.getRcode(),ctx.getConnection()));//若為Exception(advice reversal)一定要回00
                TbOnlTxnErrMgr onlTxnMgr = new TbOnlTxnErrMgr(ctx.getConnection());
                onlTxnMgr.insert(onlTxnInfo);
            }
            catch(Exception e)
            {
                onlTxnInfo.setErrDesc("ins onl txn err table error.");
                StringBuffer strb = new StringBuffer();
                strb.append("ins onl txn err table error.\n[");
                strb.append(null!=ctx.getOrgCtx()?ctx.getOrgCtx():ctx);
                strb.append("\n]");
                logger.error(strb.toString(),e);
            }
        }
        else
        {
            logger.debug("insertOnlTxnErr");
            BATCHContext bctx = (BATCHContext)ctx;
            TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
            BerTLV tlv = ctx.getLMSMsg();
            String merchId = ctx.getLmsMerchantId();
            String termId = ctx.getLmsTerminalId();
            TbCardInfo cardInfo = ctx.getCardInfo();
            String cardNo = null;
            String expiryDate = null;
            
            if(cardInfo != null)
            {
                cardNo = ctx.getCardInfo().getCardNo();
                expiryDate = ctx.getCardInfo().getExpiryDate();
            }
            else
            {
                cardNo = ctx.getLMSCardNbr();
                expiryDate = ctx.getLMSCardExpirationDate();
            }

            String lmsInvoiceNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
            String pCode = ctx.getLmsPcode();

            String storedata = tlv.getStr(LMSTag.StoreCounterArea);
            TagFF24 tagFF24 = (storedata != null) ? new TagFF24(storedata) : null;
            String batchNumber = tlv.getHexStr(LMSTag.BatchNumber);
            String atc = tlv.getHexStr(LMSTag.ICCATC);
            String txnAccessMode = tlv.getStr(LMSTag.AccessMode);

            TbOnlTxnErrInfo onlTxnInfo = new TbOnlTxnErrInfo();
            onlTxnInfo.setLmsRawData(ctx.getRawData());
            onlTxnInfo.setTxnSrc(ctx.getTxnSrc());//
            onlTxnInfo.setOnlineFlag("F");//N: online transaction  F: offline transaction

            onlTxnInfo.setAcqMemId(ctx.getAcquireInfo() != null ? ctx.getAcquireInfo().getMemId() : "");
            onlTxnInfo.setIssMemId(ctx.getIssuerInfo() != null ? ctx.getIssuerInfo().getMemId() : "");
            onlTxnInfo.setMerchId(merchId);
            onlTxnInfo.setTermId(termId);
            if (null != tagFF24)
            {
                onlTxnInfo.setStoreCounterId(tagFF24.getStoreId());
                onlTxnInfo.setPosId(tagFF24.getPosId());
                onlTxnInfo.setPosSerno(tagFF24.getPosSerno());
            }
            onlTxnInfo.setBatchNo(batchNumber);
            onlTxnInfo.setCardNo(cardNo);
            onlTxnInfo.setExpiryDate(expiryDate);
            onlTxnInfo.setLmsInvoiceNo(lmsInvoiceNo);
            onlTxnInfo.setAtc(atc);
            onlTxnInfo.setPCode(pCode);
            onlTxnInfo.setTxnDate(ctx.getHostDate());
            onlTxnInfo.setTxnTime(ctx.getHostTime());
            onlTxnInfo.setInvoiceRefNo(ctx.getIsoMsg().getString(Field.TRACE_NBR));
            String origData = tlv.getHexStr(LMSTag.OriginalDataArea);
            if (origData!=null)
            {
                TagFF34 ff34 = new TagFF34(origData);
                onlTxnInfo.setOrigLmsInvoiceNo(ff34.getInvoiceRefNo());
            }
            onlTxnInfo.setProcCode(ctx.getIsoMsg().getString(Field.PROC_CODE));
            onlTxnInfo.setOnlRcode(ctx.getRcode());
            onlTxnInfo.setTxnAccessMode(txnAccessMode);
            onlTxnInfo.setStatus("1");
            //onlTxnInfo.setRegionId(ctx.getCardInfo().getRegionId());
            onlTxnInfo.setParMon(ctx.getParMon());// for partition {01~12}
            onlTxnInfo.setParDay(ctx.getParDay());// for partition {01~31}
            onlTxnInfo.setTermDate(ctx.getTermTxnDate());
            onlTxnInfo.setTermTime(ctx.getTermTxnTime());
            onlTxnInfo.setErrDesc("err desc");
            onlTxnInfo.setErrType("A");
            onlTxnInfo.setErrProcRcode("0000");
            String mti = ctx.getIsoMsg().getMTI();
            onlTxnInfo.setMti(mti);
            onlTxnInfo.setImpFileDate(bctx.getBatchDate());
            onlTxnInfo.setImpFileTime(bctx.getBatchTime());
            String deviceId = tlv.getHexStr(LMSTag.DongleDeviceId);
            onlTxnInfo.setDeviceId(deviceId);
            onlTxnInfo.setTxnNote(ctx.getTxnNote());
            if(null != inctlInfo)
            {
                onlTxnInfo.setImpFileName(inctlInfo.getFullFileName());
            }
            /*TagFF49 ff49 = new TagFF49(tlv.getHexStr(LMSTag.ProductArea));
            if (ff49.getProdCode().length()>0)
            {
                onlTxnInfo.setProductCode(ff49.getProdCode());
                onlTxnInfo.setProductQty(ff49.getProdQty());
            }*/
            try
            {
                onlTxnInfo.setRespCode(Rcode.getRespCode(ctx.getRcode(),ctx.getConnection()));//若為Exception(advice reversal)一定要回00
                TbOnlTxnErrMgr onlTxnMgr = new TbOnlTxnErrMgr(ctx.getConnection());
                onlTxnMgr.insert(onlTxnInfo);
            }
            catch(Exception e)
            {
                onlTxnInfo.setErrDesc("ins onl txn err table error.");
                StringBuffer strb = new StringBuffer();
                strb.append("ins onl txn err table error.\n[");
                strb.append(null!=ctx.getOrgCtx()?ctx.getOrgCtx():ctx);
                strb.append("\n]");
                logger.error(strb.toString(),e);
            }
        }
    }
    
    /**
     * 將資料放到TBInfo中設給LMSContext.主要是準備TbOnlTxnInfo.
     * @param ctx
     * @param isCounterUpdate 
     * @param isCouponUpdate 
     * @param lmsAuthCode
     * @throws SQLException 
     * @throws TxException 
     * @throws  
     */
    public void fillTxnDb(LMSContext ctx) throws SQLException, TxException
    {
        if(null != ctx.getOfflineUploadTxnInfo())
        {            
            BATCHContext bctx = (BATCHContext)ctx;
            TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
            String termSettleDate = bctx.getBatchDate();
            String termSettleTime = bctx.getBatchTime();
            if(ctx.isEcaMerchId() && null != bctx.getTermBatchInfo())//中油全家以檔案連來的日期當結帳日
            {
                termSettleDate = bctx.getTermBatchInfo().getTermSettleDate();
                termSettleTime = bctx.getTermBatchInfo().getTermSettleTime();
            }
            else
            {
                //遠鑫以檔名當結帳日
                String fileNmae = inctlInfo.getFullFileName();
                String[] strs = fileNmae.split("_");
                termSettleDate = strs[3].substring(0, 8);
                termSettleTime = "00000000";
                if(strs[3] != null )
                {
                    if(strs[3].length() == 14)
                        termSettleTime = strs[3].substring(8, 14);
                    else
                        termSettleTime = strs[3].substring(8, 12) + "00";
                }
            }
            
            ctx.setBonusSummaryInfo(null);
            summaryBonusInfo(ctx);
            
            OfflineUploadTxnInfo uploadTxnInfo = ctx.getOfflineUploadTxnInfo();
            
            BerTLV tlv = ctx.getLMSMsg();
            String procCode = ctx.getIsoMsg().getString(Field.PROC_CODE);
            String traceNbr = ctx.getIsoMsg().getString(Field.TRACE_NBR);
            String termId = ctx.getLmsTerminalId();
            String merchId = ctx.getLmsMerchantId();
            String cardNo = uploadTxnInfo.getCardNo();
            String expiryDate = uploadTxnInfo.getExpiryDate();
            String lmsInvoiceNo = uploadTxnInfo.getLmsInvoiceNo();
    
            String batchNumber = uploadTxnInfo.getBatchNo();
            String atc = uploadTxnInfo.getAtc();
            String txnAccessMode = uploadTxnInfo.getAccessMode();
            String currencyCode = "901";

            TbOnlTxnInfo onlTxnInfo = new TbOnlTxnInfo();
            
            onlTxnInfo.setTxnSrc(ctx.getTxnSrc());
            onlTxnInfo.setOnlineFlag("F");
            if (null!=ctx.getAcquireInfo())
            {
                onlTxnInfo.setAcqMemId(ctx.getAcquireInfo().getMemId());
            }
            onlTxnInfo.setIssMemId(ctx.getIssuerInfo() == null?uploadTxnInfo.getIssuerNo() :ctx.getIssuerInfo().getMemId());
            onlTxnInfo.setMerchId(merchId);
            onlTxnInfo.setTermId(termId);

            onlTxnInfo.setBatchNo(batchNumber);
            onlTxnInfo.setCardNo(cardNo);
            onlTxnInfo.setExpiryDate(expiryDate);
            onlTxnInfo.setLmsInvoiceNo(lmsInvoiceNo);
            if(!StringUtil.isEmpty(bctx.getAutoLoadAtc()))
            {
                onlTxnInfo.setAutoloadAtc(bctx.getAutoLoadAtc());
            }
            if(!StringUtil.isEmpty(bctx.getAutoLoadAmt()))
            {
                onlTxnInfo.setAutoloadValues(Double.parseDouble(bctx.getAutoLoadAmt())/100);
            }
            if (uploadTxnInfo.getOrigLmsInvoiceNo() != null && !uploadTxnInfo.getOrigLmsInvoiceNo().equals("000000000000"))
            {
                onlTxnInfo.setOrigLmsInvoiceNo(uploadTxnInfo.getOrigLmsInvoiceNo());
            }
            onlTxnInfo.setAtc(atc);
            onlTxnInfo.setPCode(String.valueOf(uploadTxnInfo.getPcode()));
            onlTxnInfo.setTxnDate(ctx.getHostDate());
            onlTxnInfo.setTxnTime(ctx.getHostTime());
            onlTxnInfo.setTermDate(uploadTxnInfo.getTermDate());
            onlTxnInfo.setTermTime(uploadTxnInfo.getTermTime());
            onlTxnInfo.setLmsAuthCode(ISOUtil.getRdmNum(6));
            onlTxnInfo.setTermSettleDate(termSettleDate);
            onlTxnInfo.setTermSettleTime(termSettleTime);
            /*
             * Ex. The price of a cake = 100
             *    (20 pay in points, 80 pay in credit card)
             *    TXN_AMT=100 CREDIT_AUTH_AMT=80 TXN_REDEEM_AMT=20
             */
            onlTxnInfo.setCreditAuthAmt(ctx.getAuthAmt());// number (14,2) default 0
            onlTxnInfo.setTxnAmt(uploadTxnInfo.getTxnAmt());// number (14,2) default 0
            if(uploadTxnInfo.getPcode() == ProcCode.SALE)
            {
                onlTxnInfo.setTxnRedeemAmt(uploadTxnInfo.getTxnAmt());//number (14,2) default 0
            }
            onlTxnInfo.setInvoiceRefNo(traceNbr);
    
            onlTxnInfo.setProcCode(procCode);
            onlTxnInfo.setRespCode(Rcode.getRespCode(ctx.getRcode(),ctx.getConnection()));//default "00"
            onlTxnInfo.setOnlRcode(ctx.getRcode());//default "0000"
    
            onlTxnInfo.setTxnAccessMode(txnAccessMode);//C: Contact L: Contactless
            onlTxnInfo.setCurrencyCode(currencyCode);//Transaction Currency Code:CHN:156,HKG:344,MAC:446,SGP:702,THA:764,USD:840,TWN:901
    
            onlTxnInfo.setAdviceFlag("00");//Digit 1: Update Point status;Digit 2: Update Coupon status(0: success,1: Fail) default="00"
            onlTxnInfo.setStatus("1");//0: Initial (Default) 1: Success 9: Reversal C: Cancel R : Refund  F: Fail
    
            onlTxnInfo.setParMon(bctx.getParMon());//for partition {01~12}
            onlTxnInfo.setParDay(bctx.getParDay());//for partition {01~31}
            onlTxnInfo.setImpFileDate(bctx.getBatchDate());
            onlTxnInfo.setImpFileTime(bctx.getBatchTime());
            if(null != inctlInfo)
            {
                onlTxnInfo.setImpFileName(inctlInfo.getFullFileName());
            }
            String deviceId = tlv.getHexStr(LMSTag.DongleDeviceId);
            onlTxnInfo.setDeviceId(deviceId);
            onlTxnInfo.setTxnNote(ctx.getTxnNote());
            
            if(null != ctx.getBonusSummaryInfo())
            {
                String[] chipBonusId = ctx.getChipBonusId();
                double[] sumBefore = new double[LMSContext.getMaxPoint()];
                double[] sumBeforeCr = new double[LMSContext.getMaxPoint()];
                double[] sumBeforeDb = new double[LMSContext.getMaxPoint()];
                double[] sumAfter = new double[LMSContext.getMaxPoint()];
                
                BonusSummaryInfo  bonusSummaryInfo = ctx.getBonusSummaryInfo();
                if(null != chipBonusId)
                {
                    for(int idx= 0 ;idx < chipBonusId.length; idx++)
                    {
                        sumBefore[idx] = 0;
                        sumBeforeCr[idx] = 0;
                        sumBeforeDb[idx] = 0; 
                        sumAfter[idx] = 0;
                        if(null != bonusSummaryInfo.getChipPointSumBefore())
                        {
                            sumBefore[idx] = bonusSummaryInfo.getChipPointSumBefore()[idx];
                        }
                        
                        if(null != bonusSummaryInfo.getChipPointSumCr())
                        {
                            sumBeforeCr[idx] = bonusSummaryInfo.getChipPointSumCr()[idx];
                        }
                        
                        if(null != bonusSummaryInfo.getChipPointSumDb())
                        {
                            sumBeforeDb[idx] = bonusSummaryInfo.getChipPointSumDb()[idx];
                        }
                        
                        sumAfter[idx] = sumBefore[idx] + sumBeforeCr[idx] - sumBeforeDb[idx];
                        onlTxnInfo.setChipPoint1Before(sumBefore[0]);
                        onlTxnInfo.setChipPoint2Before(sumBefore[1]);
                        onlTxnInfo.setChipPoint3Before(sumBefore[2]);
                        onlTxnInfo.setChipPoint4Before(sumBefore[3]);
                        
                        onlTxnInfo.setChipPoint1Cr(sumBeforeCr[0]);
                        onlTxnInfo.setChipPoint2Cr(sumBeforeCr[1]);
                        onlTxnInfo.setChipPoint3Cr(sumBeforeCr[2]);
                        onlTxnInfo.setChipPoint4Cr(sumBeforeCr[3]);
                        
                        onlTxnInfo.setChipPoint1Db(sumBeforeDb[0]);
                        onlTxnInfo.setChipPoint2Db(sumBeforeDb[1]);
                        onlTxnInfo.setChipPoint3Db(sumBeforeDb[2]);
                        onlTxnInfo.setChipPoint4Db(sumBeforeDb[3]);
                        
                        onlTxnInfo.setChipPoint1After(sumAfter[0]);
                        onlTxnInfo.setChipPoint2After(sumAfter[1]);
                        onlTxnInfo.setChipPoint3After(sumAfter[2]);
                        onlTxnInfo.setChipPoint4After(sumAfter[3]);
                    }  
                }
            }
            
           
            /* CHIP_CR_BONUS_FLAG */
            boolean bChipCounterAward = false;
            boolean bChipCouponAward = false;
            if(ctx.getChipCounterAward() != null)
            {
                bChipCounterAward = ctx.getChipCounterAward().isOutput();
            }
            
            if(ctx.getChipCouponAward() != null)
            {
                bChipCouponAward = ctx.getChipCouponAward().isOutput();
            }
            String crBonusFlag = (bChipCounterAward?"1":"0") + (bChipCouponAward?"1":"0");
            onlTxnInfo.setChipCrBonusFlag(crBonusFlag);
            
            /* CHIP_DB_BONUS_FLAG */
            boolean bChipCounterRedeem = false;
            boolean bChipCouponRedeem = false;
            if(ctx.getChipCounterRedeem() != null)
            {
                bChipCounterRedeem = ctx.getChipCounterRedeem().isOutput();
            }
            
            if(ctx.getChipCouponRedeem() != null)
            {
                bChipCouponRedeem = ctx.getChipCouponRedeem().isOutput();
            }
            String dbBonusFlag = (bChipCounterRedeem?"1":"0") + (bChipCouponRedeem?"1":"0");
            onlTxnInfo.setChipDbBonusFlag(dbBonusFlag);
            
            logger.debug("ctx.setOnlTxnInfo(onlTxnInfo)");
            ctx.setOnlTxnInfo(onlTxnInfo);
        }
    }
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.logger.BaseTxLogger#fillTxnDtlDb(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    @Override
    public void fillTxnDtlDb(LMSContext ctx)
    {
        if(null != ctx.getOfflineUploadTxnInfo())
        {
            BATCHContext bctx = (BATCHContext)ctx;
            OfflineUploadTxnInfo uploadTxnInfo = ctx.getOfflineUploadTxnInfo();
    
            AwardCouponList chipPaperCouponAward = ctx.getChipPaperCouponAward();
            AwardCouponList chipCounterAward = ctx.getChipCounterAward();
            AwardCouponList chipCouponAward = ctx.getChipCouponAward();
            AwardCouponList chipCounterRedeem = ctx.getChipCounterRedeem();
            AwardCouponList chipCouponRedeem = ctx.getChipCouponRedeem();
         
            /* 共有八種CASE: 晶片點數回饋, 晶片點數抵扣, 晶片點券回饋, 晶片點券抵扣 */
            List<TbOnlTxnDtlInfo> onlTxnInfoDtlList = new ArrayList<TbOnlTxnDtlInfo>();
    
            String pCode = String.valueOf(uploadTxnInfo.getPcode());
            
            String sBalanceId = uploadTxnInfo.getCardNo();
            String redeemTxCodeStr = (String) this.getRedeemTxCode().get(pCode);
            String awardTxnCodeStr = (String) this.getRewardTxCode().get(pCode);
            int redeemTxnCode = Integer.parseInt(redeemTxCodeStr == null?"0":redeemTxCodeStr);
            int awardTxnCode = Integer.parseInt(awardTxnCodeStr == null?"0":awardTxnCodeStr);
            
            BonusSummaryInfo  bonusSummaryInfo = ctx.getBonusSummaryInfo();
            
            /* 回饋點數 */
            setCounterTxnDtl(bctx,onlTxnInfoDtlList, chipCounterAward,bonusSummaryInfo, sBalanceId, awardTxnCode, Constant.TYPE_NOT_REDEEM);
            
            /* 回饋紙卷  */
            setCouponTxnDtl(bctx,onlTxnInfoDtlList, chipPaperCouponAward,bonusSummaryInfo, sBalanceId, awardTxnCode, Constant.TYPE_PAPER_COUPON, Constant.TYPE_NOT_REDEEM);
            
            /* 回饋點卷  */
            setCouponTxnDtl(bctx,onlTxnInfoDtlList, chipCouponAward,bonusSummaryInfo, sBalanceId, awardTxnCode, Constant.TYPE_NOT_PAPER_COUPON, Constant.TYPE_NOT_REDEEM);
            
            /* 抵扣點卷  */
            setCouponTxnDtl(bctx,onlTxnInfoDtlList, chipCouponRedeem,bonusSummaryInfo, sBalanceId, redeemTxnCode, Constant.TYPE_NOT_PAPER_COUPON, Constant.TYPE_REDEEM);
            
            /* 抵扣點數  */
            setCounterTxnDtl(bctx,onlTxnInfoDtlList, chipCounterRedeem,bonusSummaryInfo, sBalanceId, redeemTxnCode, Constant.TYPE_REDEEM);
    
            if (onlTxnInfoDtlList.size()>0)
            {
                TbOnlTxnDtlInfo[] onlTxnDtlInfo = new TbOnlTxnDtlInfo[0];
                onlTxnDtlInfo = onlTxnInfoDtlList.toArray(onlTxnDtlInfo);
                ctx.setOnlTxnDtlInfo(onlTxnDtlInfo);
            }
        }
    }
    
    /**
     * 紀錄交易前後變動情形TbTxnTraceInfo
     * @param ctx 
     */
    public void fillTxnTrace(LMSContext ctx)
    {
        super.fillTxnTrace(ctx);
    }
}
