package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.AmountUtil;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl.FF11;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl.FF21;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl.FF3C;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSProcCode;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.util.ISOUtil;

public class MsgUtil
{
    private static final Logger logger = Logger.getLogger(MsgUtil.class);
    public static final int HEADER_SIZE = 66;
//    public static final int DETAIL_SIZE = 248;
    public static final int DETAIL_SIZE = 265;
    private static final String ECASH_BONUS_ID = "0000000000";
    private static final String QUANTITY_SIGN_PLUS_ = "00";
    private static final String QUANTITY_SIGN_MINUS_ = "01";
    private static final String START_DATE = "00010101";
    private static final String END_DATE = "99991231";
    
    public static BerTLV batch2online(BerTLV bTlv){

        StringBuilder buf = new StringBuilder(); 
        BerTLV bertlv = BerTLV.createInstance();
        
       /* String cardno = ff11.getCardNo();
        cardno = ISOUtil.unPadRight(cardno, 'F');
        bertlv.addHexStr(LMSTag.LMSCardNumber, cardno);
        bertlv.addHexStr(LMSTag.LMSCardExpirationDate, ff11.getCardExp());
        bertlv.addHexStr(LMSTag.LMSInvoiceNumber, ff11.getInvoiceNo());
        bertlv.addHexStr(LMSTag.LMSProcessingCode, ff11.getPcode());
        bertlv.addHexStr(LMSTag.BatchNumber, ff21.getBatchNo());
        bertlv.addHexStr(LMSTag.IssuerNumber, ff21.getIssNo());
        bertlv.addHexStr(LMSTag.StoreCounterArea, ISOUtil.hexString(ff21.getStoreCntArea().getBytes()));
        bertlv.addHexStr(LMSTag.ICCATC, ff21.getIccAtc());
        bertlv.addHexStr(LMSTag.TerminalTransactionDateTime, ff21.getTermDateTime());
        bertlv.addHexStr(LMSTag.HostTransactionDateTime, ff21.getHostDateTime());
        bertlv.addHexStr(LMSTag.SAMArea, ff21.getSamArea());//補空白
        bertlv.add(LMSTag.ChipCardStatus, ff21.getChipCardSts().getBytes());
        bertlv.add(LMSTag.AccessMode, ff21.getAccessMod().getBytes());
        bertlv.addHexStr(LMSTag.TransactionType, "03");
        
        
         現金交易或點數交易 
        double ecashBeforeBal = Double.parseDouble(ff21.getEcashBeforeBal());
        double ecash = Double.parseDouble(ff21.getEcashAmt());
        double lmsamt = Double.parseDouble(ff21.getLmsAmt());
        if (ecash>0){
            bertlv.addHexStr(LMSTag.LoyaltyTransactionAmount, ff21.getEcashAmt());
        }else if (lmsamt>0){
            bertlv.addHexStr(LMSTag.LoyaltyTransactionAmount, ff21.getLmsAmt());
        }else{
            bertlv.addHexStr(LMSTag.LoyaltyTransactionAmount, "0000000000");
        }
        
        if (bTlv.hasTag(LMSTag.OriginalDataArea)){
            bertlv.addHexStr(LMSTag.OriginalDataArea, bTlv.getHexStr(LMSTag.OriginalDataArea));
        }
        
        if (bTlv.hasTag(LMSTag.DongleDeviceId)){
            bertlv.add(LMSTag.DongleDeviceId, bTlv.getValue(LMSTag.DongleDeviceId));
        }
        
         遠傳 pos txn uid 
        if (bTlv.hasTag(LMSTag.PosTxnUid)){
            bertlv.add(LMSTag.PosTxnUid, bTlv.getValue(LMSTag.PosTxnUid));
        }
        
         FF40 orig txn data 
        if (bTlv.hasTag(LMSTag.OriginalCardArea)){
            bertlv.addHexStr(LMSTag.OriginalCardArea, bTlv.getHexStr(LMSTag.OriginalCardArea));
        }
        
         晶片餘額 
        if (ecashBeforeBal>0){
            buf.delete(0, buf.length());
            buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_PLUS_ + AmountUtil.format(ecashBeforeBal == 0 ?0 :ecashBeforeBal / 100, "00000000.00") + START_DATE + END_DATE + "00");
            bertlv.addHexStr(LMSTag.ChipPointsBalanceArea,buf.toString());
        }  
        
         儲值金消費金額 
        if (ecash>0){
            buf.delete(0, buf.length());
            buf.append(ECASH_BONUS_ID + AmountUtil.format(ecash == 0 ?0 :ecash / 100, "00000000.00") + START_DATE + END_DATE);
            bertlv.addHexStr(LMSTag.ChipPointsTransactionArea,buf.toString());
        }*/
        
     
        return bertlv;
    }
}
