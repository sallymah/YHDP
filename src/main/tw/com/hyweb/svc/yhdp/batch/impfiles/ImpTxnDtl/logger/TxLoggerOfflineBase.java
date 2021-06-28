package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.logger;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnDtlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnErrInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbOnlTxnErrMgr;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.logger.BaseTxLogger;
import tw.com.hyweb.svc.yhdp.online.util.Field;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.ProcCode;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.tag.OfflineUploadTxnInfo;
import tw.com.hyweb.svc.yhdp.online.util.tag.TagFF24;
import tw.com.hyweb.svc.yhdp.online.util.tag.TagFF34;
import tw.com.hyweb.svc.yhdp.online.util.tag.TagFF41;
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
     * 將資料放到TBInfo中設給LMSContext.主要是準備TbOnlTxnInfo.
     * @param ctx
     * @param chipCouponBalUpdateCnt
     * @param lmsAuthCode
     * @throws SQLException 
     * @throws TxException 
     */
    public void fillTxnDb(LMSContext ctx) throws SQLException, TxException
    {
        super.fillTxnDb(ctx);//設定基本值並設到LMSContext
        BATCHContext bctx = (BATCHContext)ctx;
        TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
        String termSettleDate = bctx.getBatchDate();
        String termSettleTime = bctx.getBatchTime();
        if(ctx.isEcaMerchId() && null != bctx.getTermBatchInfo())
        {
            termSettleDate = bctx.getTermBatchInfo().getTermSettleDate();
            termSettleTime = bctx.getTermBatchInfo().getTermSettleTime();
        }
        else
        {
            if(null != bctx.getTermBatchInfo())
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
        }
        
        BerTLV tlv = ctx.getLMSMsg();
        TbOnlTxnInfo onlTxnInfo = ctx.getOnlTxnInfo();
        onlTxnInfo.setOnlineFlag("F");
        onlTxnInfo.setImpFileDate(bctx.getBatchDate());
        onlTxnInfo.setImpFileTime(bctx.getBatchTime());
        onlTxnInfo.setTermDate(bctx.getHostDate());
        onlTxnInfo.setTermTime(bctx.getHostTime());
        onlTxnInfo.setTermSettleDate(termSettleDate);
        onlTxnInfo.setTermSettleTime(termSettleTime);
        onlTxnInfo.setChipPoint1After(bctx.getChipAfterBonusQty());//以 dongle 送上來的交易後餘額為主
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
        if(null != inctlInfo)
        {
            onlTxnInfo.setImpFileName(inctlInfo.getFullFileName());
        }
        if(null != inctlInfo)
        {
            if(!inctlInfo.getFileName().equals("IMPTXNDTL"))
            {
                //入帳日(Hi Life使用
                onlTxnInfo.setRecordDate(tlv.getStr(LMSTag.PosTxnUid));
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
        super.fillTxnDtlDb(ctx);//設定基本值並設到LMSContext
        TbOnlTxnDtlInfo[] onlTxnDtlInfo = ctx.getOnlTxnDtlInfo();
        if(null != onlTxnDtlInfo && onlTxnDtlInfo.length == 1)
        {
            onlTxnDtlInfo[0].setBonusAfterQty(bctx.getChipAfterBonusQty());
            logger.debug("ChipPoint1After:"+onlTxnDtlInfo[0].getBonusAfterQty());
        }
    }
    
    /**
     * 紀錄交易前後變動情形TbTxnTraceInfo
     * @param ctx 
     */
    public void fillTxnTrace(LMSContext ctx)
    {
        super.fillTxnTrace(ctx);//設定基本值並設到LMSContext
    }
    
    /**
     * 當訊息有錯誤時需要insert到TbOnlTxnERR,
     * 當insert TbOnlTxnERR也錯誤時advice和reversal必須要記錄訊息到檔案然後回應00
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
        
        if(tlv.hasTag(LMSTag.HgSerno))
        {
            onlTxnInfo.setHgSerno(tlv.getHexStr(LMSTag.HgSerno));
        }
        
        if(tlv.hasTag(LMSTag.TermProfile))
        {
            TagFF41 ff41 = new TagFF41(tlv.getHexStr(LMSTag.TermProfile));
            onlTxnInfo.setPosApiVersion(ff41.getPostApiVer());
        }
        
        String posTxnUid = tlv.getStr(LMSTag.PosTxnUid);
        if(tlv.hasTag(LMSTag.TransactionType))
        {
          //遠傳pos uid
            if(!StringUtil.isEmpty(posTxnUid))
            {
                onlTxnInfo.setRecordDate(posTxnUid);
            }
        }
        else
        {
            //遠傳pos uid
            if(!StringUtil.isEmpty(posTxnUid))
            {
                onlTxnInfo.setPosSerno(posTxnUid);
            }
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
}
