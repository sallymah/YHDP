package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.logger;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnErrInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbTrafficTxnErrInfo;
import tw.com.hyweb.service.db.info.TbTrafficTxnInfo;
import tw.com.hyweb.service.db.mgr.TbOnlTxnDtlMgr;
import tw.com.hyweb.service.db.mgr.TbOnlTxnErrMgr;
import tw.com.hyweb.service.db.mgr.TbOnlTxnMgr;
import tw.com.hyweb.service.db.mgr.TbTrafficTxnErrMgr;
import tw.com.hyweb.service.db.mgr.TbTrafficTxnMgr;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.logger.BaseTxLogger;
import tw.com.hyweb.svc.yhdp.online.util.Field;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.tag.TagFF24;
import tw.com.hyweb.svc.yhdp.online.util.tag.TagFF34;
import tw.com.hyweb.util.string.StringUtil;

public class TxLoggerOfflineBase extends BaseTxLogger
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(TxLoggerOfflineBase.class);

    /**
     *
     */
    public TxLoggerOfflineBase()
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
        BATCHContext lmsCtxResp = (BATCHContext) ctxResp;
        BATCHContext lmsCtxReq = (BATCHContext) lmsCtxResp.getOrgCtx();//get request context
        if (lmsCtxReq==null)
        {
            logger.debug("lmsCtxResp.getOrgCtx() is null");
            lmsCtxReq = lmsCtxResp;
        }
        TbOnlTxnInfo onlTxnInfo = lmsCtxReq.getOnlTxnInfo();
        TbOnlTxnMgr onlTxnMgr = new TbOnlTxnMgr(lmsCtxReq.getConnection());
        TbOnlTxnDtlInfo[] onlTxnInfoDtl = lmsCtxReq.getOnlTxnDtlInfo();
        TbOnlTxnDtlMgr onlTxnDtlMgr = new TbOnlTxnDtlMgr(lmsCtxReq.getConnection());
        TbTrafficTxnInfo trafficTxnInfo = lmsCtxReq.getTbTrafficTxnInfo();
        TbTrafficTxnMgr trafficMgr = new TbTrafficTxnMgr(lmsCtxReq.getConnection());
        
        if (onlTxnInfo!=null)
        {
            logger.debug("onlTxnMgr.insert(onlTxnInfo)");
            //onlTxnInfo.setResponseTime(Long.toString(System.currentTimeMillis() - lmsCtxReq.getTimeTxInit().getTime()));
            onlTxnMgr.insert(onlTxnInfo);
        }
        if (onlTxnInfoDtl!=null)
        {
            logger.debug("onlTxnDtlMgr.insert(onlTxnInfoDtl[i])");
            for (int i = 0;i<onlTxnInfoDtl.length;i++)
            {
                onlTxnDtlMgr.insert(onlTxnInfoDtl[i]);
            }
        }
        if (trafficTxnInfo!=null)
        {
            logger.debug("onlTxnMgr.insert(trafficTxnInfo)");
            trafficMgr.insert(trafficTxnInfo);
        }
    }
    
    /**
     * ???????????????TBInfo?????????LMSContext.???????????????TbOnlTxnInfo.
     * @param ctx
     * @param chipCouponBalUpdateCnt
     * @param lmsAuthCode
     * @throws SQLException 
     * @throws TxException 
     */
    public void fillTxnDb(LMSContext ctx) throws SQLException, TxException
    {
        super.fillTxnDb(ctx);//????????????????????????LMSContext
        BATCHContext bctx = (BATCHContext)ctx;
        //TrafficTxnDetail trafficTxnDtl = bctx.getTrafficTxnDetail();
        TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
        TbOnlTxnInfo onlTxnInfo = ctx.getOnlTxnInfo();
        //onlTxnInfo.setAtc(trafficTxnDtl.getDataRecode(TransferUtil.TRANS_NO));//???ISO?????????NUBER?????????3??????4???
        onlTxnInfo.setTermDate(bctx.getHostDate());
        onlTxnInfo.setTermTime(bctx.getHostTime());
        String termSettleDate = bctx.getTermBatchInfo().getTermSettleDate();
        String termSettleTime = bctx.getTermBatchInfo().getTermSettleTime();
        onlTxnInfo.setTermSettleDate(termSettleDate);
        onlTxnInfo.setTermSettleTime(termSettleTime);
        onlTxnInfo.setOnlineFlag("F");
        onlTxnInfo.setImpFileDate(bctx.getBatchDate());
        onlTxnInfo.setImpFileTime(bctx.getBatchTime());
        onlTxnInfo.setParMon(bctx.getBatchDate().substring(4,6));// for partition {01~12}
        onlTxnInfo.setParDay(bctx.getBatchDate().substring(6,8));// for partition {01~31}
        onlTxnInfo.setImpFileName(inctlInfo.getFullFileName());
        onlTxnInfo.setPurchaseType("1");//????????????????????????
        onlTxnInfo.setChipPoint1After(bctx.getChipAfterBonusQty());//??? dongle ?????????????????????????????????
        double sysChipPointBefore = onlTxnInfo.getChipPoint1Before().doubleValue() +  onlTxnInfo.getChipPoint1Cr().doubleValue() - onlTxnInfo.getChipPoint1Db().doubleValue();
        onlTxnInfo.setSysPoint1AfterBal(sysChipPointBefore);
        onlTxnInfo.setResponseTime(null);
        
        if(!StringUtil.isEmpty(bctx.getAutoLoadAtc()))
        {
            onlTxnInfo.setAutoloadAtc(bctx.getAutoLoadAtc());
        }
        if(!StringUtil.isEmpty(bctx.getAutoLoadAmt()))
        {
            onlTxnInfo.setAutoloadValues(Double.parseDouble(bctx.getAutoLoadAmt())/100);
        }
        
        TbTrafficTxnInfo  trafficTxnInfo = bctx.getTbTrafficTxnInfo();
        if(null != trafficTxnInfo) {
        	double qty = 0;
            onlTxnInfo.setTransType(trafficTxnInfo.getTransType());
            if(!StringUtil.isEmpty(trafficTxnInfo.getBonusRemains()))
            {
            	qty = Double.valueOf(trafficTxnInfo.getBonusRemains());
            	onlTxnInfo.setBonusRemains(qty);	
            }
            
            if(!StringUtil.isEmpty(trafficTxnInfo.getBonusTxnAmt()))
            {
            	qty = 0;
            	qty = Double.valueOf(trafficTxnInfo.getBonusTxnAmt());
            	onlTxnInfo.setBonusTxnAmt(qty);	
            }
        }
    }
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.logger.BaseTxLogger#fillTxnDtlDb(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    @Override
    public void fillTxnDtlDb(LMSContext ctx)
    {
        BATCHContext bctx = (BATCHContext)ctx;
        super.fillTxnDtlDb(ctx);//????????????????????????LMSContext
        TbOnlTxnDtlInfo[] onlTxnDtlInfo = ctx.getOnlTxnDtlInfo();
        if(null != onlTxnDtlInfo && onlTxnDtlInfo.length == 1)
        {
            onlTxnDtlInfo[0].setBonusAfterQty(bctx.getChipAfterBonusQty());
            logger.debug("ChipPoint1After:"+onlTxnDtlInfo[0].getBonusAfterQty());
        }
    }
    
    /**
     * ??????????????????????????????TbTxnTraceInfo
     * @param ctx 
     */
    public void fillTxnTrace(LMSContext ctx)
    {
        super.fillTxnTrace(ctx);//????????????????????????LMSContext
    }
    
    /**
     * ???????????????????????????insert???TbOnlTxnERR,
     * ???insert TbOnlTxnERR????????????advice???reversal??????????????????????????????????????????00
     * @param ctx
     * @throws SQLException
     */
    public void insertOnlTxnErr(LMSContext ctx)
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
        String deviceId = tlv.getHexStr(LMSTag.DongleDeviceId);
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
        onlTxnInfo.setParMon(bctx.getBatchDate().substring(4,6));// for partition {01~12}
        onlTxnInfo.setParDay(bctx.getBatchDate().substring(6,8));// for partition {01~31}
        onlTxnInfo.setTermDate(ctx.getTermTxnDate());
        onlTxnInfo.setTermTime(ctx.getTermTxnTime());
        onlTxnInfo.setPurchaseType("1");//????????????????????????
        String errDesc = ctx.getErrDesc();
        if(StringUtil.isEmpty(errDesc))
        {
            errDesc = "error desc";
        }
        else if(!StringUtil.isEmpty(errDesc) && errDesc.length() > 500)
        {
            errDesc = errDesc.substring(0, 499);
        }
        onlTxnInfo.setErrDesc(errDesc);
        String mti = ctx.getIsoMsg().getMTI();

        String chType = "O";
        chType = (mti.charAt(1)=='2')?"N":chType;
        chType = (mti.charAt(1)=='4')?"R":chType;
        chType = (mti.charAt(2)=='2')?"A":chType;
        onlTxnInfo.setErrType(chType);
        onlTxnInfo.setErrProcRcode("0000");
        onlTxnInfo.setMti(mti);
        onlTxnInfo.setDeviceId(deviceId);
        onlTxnInfo.setOnlineFlag("F");
        onlTxnInfo.setImpFileDate(bctx.getBatchDate());
        onlTxnInfo.setImpFileTime(bctx.getBatchTime());
        onlTxnInfo.setImpFileName(inctlInfo.getFullFileName());
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
        
        String posTxnUid = tlv.getStr(LMSTag.PosTxnUid);
        if(tlv.hasTag(LMSTag.TransactionType))
        {
          //??????pos uid
            if(!StringUtil.isEmpty(posTxnUid))
            {
                onlTxnInfo.setRecordDate(posTxnUid);
            }
        }
        else
        {
            //??????pos uid
            if(!StringUtil.isEmpty(posTxnUid))
            {
                onlTxnInfo.setPosSerno(posTxnUid);
            }
        }
        TbTrafficTxnErrInfo trafficTxnInfo = null;
        try
        {
            trafficTxnInfo = bctx.getTbTrafficTxnErrInfo();
            if(null != trafficTxnInfo)
            {
                TbTrafficTxnErrMgr trafficMgr = new TbTrafficTxnErrMgr(ctx.getConnection());
                trafficMgr.insert(trafficTxnInfo);
            }
        }
        catch(Exception e)
        {
            onlTxnInfo.setErrDesc("ins traffic txn err table error.");
            StringBuffer strb = new StringBuffer();
            strb.append("ins traffic txn err table error.\n[");
            strb.append(null!=ctx.getOrgCtx()?ctx.getOrgCtx():ctx);
            strb.append("\n]");
            logger.error(strb.toString(),e);
        }
        
        try
        {
        	if(null != trafficTxnInfo) {
            	double qty = 0;
                onlTxnInfo.setTransType(trafficTxnInfo.getTransType());
                if(!StringUtil.isEmpty(trafficTxnInfo.getBonusRemains()))
                {
                	qty = Double.valueOf(trafficTxnInfo.getBonusRemains());
                	onlTxnInfo.setBonusRemains(qty);	
                }
                
                if(!StringUtil.isEmpty(trafficTxnInfo.getBonusTxnAmt()))
                {
                	qty = 0;
                	qty = Double.valueOf(trafficTxnInfo.getBonusTxnAmt());
                	onlTxnInfo.setBonusTxnAmt(qty);	
                }
            }
            onlTxnInfo.setRespCode(Rcode.getRespCode(ctx.getRcode(),ctx.getConnection()));//??????Exception(advice reversal)????????????00
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