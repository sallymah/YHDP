package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl;

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
    public static final int FF21_SIZE = 206;
    public static final int FF11_SIZE = 60;
    public static final int FF64_SIZE = 16;
    private static final String KEY_IDX = "01";
    private static final String ECASH_BONUS_ID = "0000000000";
    private static final String QUANTITY_SIGN_PLUS_ = "00";
    private static final String QUANTITY_SIGN_MINUS_ = "01";
    private static final String START_DATE = "00010101";
    private static final String END_DATE = "99991231";
    
    public static BerTLV batch2online(BerTLV bTlv){

        StringBuilder buf = new StringBuilder(); 
        BerTLV bertlv = BerTLV.createInstance();
        String ff11Str = bTlv.getHexStr(0xFF11);
        String ff21Str = bTlv.getHexStr(0xFF21);
        FF11 ff11 = new FF11(MsgUtil.FF11_SIZE, ff11Str);
        FF21 ff21 = new FF21(MsgUtil.FF21_SIZE,ff21Str);
        
        String cardno = ff11.getCardNo();
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
        
        
        /* 現金交易或點數交易 */
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
        
        /* 遠傳 pos txn uid */
        if (bTlv.hasTag(LMSTag.PosTxnUid)){
            bertlv.add(LMSTag.PosTxnUid, bTlv.getValue(LMSTag.PosTxnUid));
        }
        
        /* FF40 orig txn data */
        if (bTlv.hasTag(LMSTag.OriginalCardArea)){
            bertlv.addHexStr(LMSTag.OriginalCardArea, bTlv.getHexStr(LMSTag.OriginalCardArea));
        }
        
        /* FF41 */
        if (bTlv.hasTag(LMSTag.TermProfile)){
            bertlv.addHexStr(LMSTag.TermProfile, bTlv.getHexStr(LMSTag.TermProfile));
        }
        
        /* FF69 */
        if (bTlv.hasTag(LMSTag.HgSerno)){
            bertlv.addHexStr(LMSTag.HgSerno, bTlv.getHexStr(LMSTag.HgSerno));
        }
        String beforeBal = ff21.getEcashBeforeBal();
        double ecashBeforeBal = Double.parseDouble(beforeBal.substring(1));
        String sing = beforeBal.substring(0, 1);
        /* 晶片餘額 */
        if (ecashBeforeBal>0){
            buf.delete(0, buf.length());
            if(Integer.valueOf(sing) == 0)
            {
                buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_PLUS_ + AmountUtil.format(ecashBeforeBal == 0 ?0 :ecashBeforeBal / 100, "00000000.00") + START_DATE + END_DATE + "00");
            }
            else
            {
                buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_MINUS_ + AmountUtil.format(ecashBeforeBal == 0 ?0 :ecashBeforeBal / 100, "00000000.00") + START_DATE + END_DATE + "00");
            }
            bertlv.addHexStr(LMSTag.ChipPointsBalanceArea,buf.toString());
        }  
        
        /* 儲值金消費金額 */
        if (ecash>0){
            buf.delete(0, buf.length());
            buf.append(ECASH_BONUS_ID + AmountUtil.format(ecash == 0 ?0 :ecash / 100, "00000000.00") + START_DATE + END_DATE);
            bertlv.addHexStr(LMSTag.ChipPointsTransactionArea,buf.toString());
        }
        
        /*if(Integer.valueOf(ff11.getPcode()) == LMSProcCode.SALE || 
                Integer.valueOf(ff11.getPcode()) == LMSProcCode.BLACK_LIST)
        {
            FF3C ff3c = new FF3C(null);
            ff3c.setPcode(ff11.getPcode());
            ff3c.setBatchNo(ff21.getBatchNo());
            ff3c.setIssNo(ff21.getIssNo());
            ff3c.setCardNo(ff11.getCardNo());
            ff3c.setCardExp(ff11.getCardExp());
            ff3c.setIccAtc(ff21.getIccAtc());
            ff3c.setTermDateTime(ff21.getTermDateTime());
            ff3c.setLmsAmt(bertlv.getHexStr(LMSTag.LoyaltyTransactionAmount));
            ff3c.setInvoiceNo(ff11.getInvoiceNo());
            ff3c.setAccessMod(ff21.getAccessMod());
            ff3c.setTxnType("01");//00: online 交易   01: offline 交易
            if (bTlv.hasTag(0xFF34)){
                String str34 = bTlv.getHexStr(0xFF34);
                ff3c.setOriInvoiceNo(str34.substring(str34.length()-12));
            }else{
                ff3c.setOriInvoiceNo("000000000000");
            }
            if (bTlv.hasTag(LMSTag.EdcTac)){
                ff3c.setTac(bTlv.getHexStr(LMSTag.EdcTac));
            }else if (bTlv.hasTag(LMSTag.HostAtc)){
                ff3c.setTac(bTlv.getHexStr(LMSTag.HostAtc));
            }else{
                ff3c.setTac(ISOUtil.hexString(new byte[8]));
            }
            ff3c.setPurchaseType("01");//01: 第一類 02: 第二類
            //先放預設值
            ff3c.setUpdAwdCoupon("00");     //FF6E (from batch FF6B 用來放離線回饋點券)
            ff3c.setUpdAwdPoint("00");      //FF6F (from batch FF6C 用來放離線回饋點數)
            ff3c.setUpdChipPointTxn(KEY_IDX);  //FF49 (from batch FF36 用來放交易點數抵扣)
            if (ecash>0){
                buf.delete(0, buf.length());
                buf.append(KEY_IDX + ECASH_BONUS_ID + AmountUtil.format(ecash == 0 ? 0 : ecash/100, "00000000.00") + START_DATE + END_DATE);
                bertlv.addHexStr(LMSTag.UploadChipPointsTransactionArea,buf.toString());
            } 
            
            ff3c.setUpdChipPointBal("01");  //FF4A (from batch FF38 FF44 交易前點數餘額)
            //offline FF4A Chip Points Balance Area
            if (ecashBeforeBal>0){
                buf.delete(0, buf.length());
                buf.append("01" + ECASH_BONUS_ID + QUANTITY_SIGN_PLUS_ + AmountUtil.format(ecashBeforeBal == 0 ? 0 :ecashBeforeBal/100 , "00000000.00") + START_DATE + END_DATE);
                bertlv.addHexStr(LMSTag.UploadChipPointsBalanceArea,buf.toString());
            }  
            
            ff3c.setUpdChipCouponTxn("00"); //FF4B (from batch FF37 用來放交易點券抵扣)
            ff3c.setUpdChipPointRsp("00");  //FF4D (from batch FF3A 用來放連線交易主機回應點數)
            ff3c.setUpdChipCouponRsp("00"); //FF4F (from batch FF3B 用來放連線交易主機回應點券)
            ff3c.setUpdChipCouponBal("00"); //FF5A (from batch FF39 FF41 交易前點券餘額)
            StringBuilder sb = new StringBuilder();
            sb.append(ff3c.pack());
            bertlv.addHexStr(0xFF3C ,sb.toString());
        }*/
        return bertlv;
    }
}
