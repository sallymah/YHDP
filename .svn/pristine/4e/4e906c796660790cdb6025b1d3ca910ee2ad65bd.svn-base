package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbOffReloadAuthInfo;
import tw.com.hyweb.service.db.mgr.TbOffReloadAuthMgr;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.Constant;
import tw.com.hyweb.svc.yhdp.online.util.Field;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;

public class InsertOffLineRldAuth implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(InsertOffLineRldAuth.class);
    private String uptUserId = Constant.UPT_ONLINE_USER_ID;
    private String aprUserId = Constant.UPT_ONLINE_USER_ID;

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doActionTest(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        BATCHContext bctx = (BATCHContext)ctx;
        TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
        BerTLV tlv = ctx.getLMSMsg();
        TbOffReloadAuthInfo info = new TbOffReloadAuthInfo();
        TbOffReloadAuthMgr mgr = new TbOffReloadAuthMgr(ctx.getConnection());
        TbCardInfo cardInfo = ctx.getCardInfo();
        String transType = ctx.getTransType();
        logger.debug("transType:"+transType);
        TbMerchInfo merchInfo = ctx.getMerchInfo();
        
        String lmsInvoiceNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
        String batchNo = tlv.getHexStr(LMSTag.BatchNumber);
        String atc = tlv.getHexStr(LMSTag.ICCATC);
        String invoiceRefNo = ctx.getIsoMsg().getString(Field.TRACE_NBR);
        String termDate = ctx.getHostDate();
        String termTime = ctx.getHostTime();
        String mit = ctx.getMTI();
        Number txnAmt = ctx.getLmsAmt();
      
        info.setCardNo(cardInfo.getCardNo());
        info.setExpiryDate(cardInfo.getExpiryDate());
        info.setBankId(cardInfo.getBankId());
        info.setLmsInvoiceNo(lmsInvoiceNo);
        info.setAcqMemId(merchInfo.getMemId());
        info.setBatchNo(batchNo);
        info.setInvoiceRefNo(invoiceRefNo);
        info.setIssMemId(cardInfo.getMemId());
        info.setAtc(atc);
        info.setAprvDate(bctx.getBatchDate());
        info.setAprvTime(bctx.getBatchTime());
        info.setAprvUserid(aprUserId);
        info.setUptDate(bctx.getBatchDate());
        info.setUptTime(bctx.getBatchTime());
        info.setUptUserid(uptUserId);
        info.setTxnAmt(txnAmt);
        info.setTxnSrc(ctx.getTxnSrc());
        info.setTxnDate(ctx.getHostDate());
        info.setTxnTime(ctx.getHostTime());
        info.setTermDate(termDate);
        info.setTermTime(termTime);
        info.setTermId(ctx.getLmsTerminalId());
        info.setMerchId(ctx.getLmsMerchantId());
        info.setPCode(ctx.getLmsPcode());
        info.setProcCode("888888");
        info.setParMon(ctx.getParMon());
        info.setParDay(ctx.getParMon());
        info.setMti(mit.charAt(1) == '4' ? "0400" : "0200");
        info.setOnlineFlag("F");
        info.setImpFileName(inctlInfo.getFullFileName());
        info.setImpFileDate(bctx.getBatchDate());
        info.setImpFileTime(bctx.getBatchTime());
        try {
            mgr.insert(info);
        } catch(SQLException e) {
            throw e;
        }
    }
    
    public String getUptUserId()
    {
        return uptUserId;
    }

    public void setUptUserId(String uptUserId)
    {
        this.uptUserId = uptUserId;
    }

    public String getAprUserId()
    {
        return aprUserId;
    }

    public void setAprUserId(String aprUserId)
    {
        this.aprUserId = aprUserId;
    }
}
