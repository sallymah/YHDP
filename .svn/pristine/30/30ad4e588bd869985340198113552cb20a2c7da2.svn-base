package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.AmountUtil;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.FF11;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.FF21;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.util.ISOUtil;

public class MsgUtil
{
    private static final Logger logger = Logger.getLogger(MsgUtil.class);
    private static final String ECASH_BONUS_ID = "0000000000";
    private static final String QUANTITY_SIGN_PLUS_ = "00";
    private static final String QUANTITY_SIGN_MINUS_ = "01";
    private static final String START_DATE = "00010101";
    private static final String END_DATE = "99991231";
    
    /*public static BerTLV batch2online(BerTLV bTlv){

        BerTLV bertlv = BerTLV.createInstance();
        String ff11Str = bTlv.getHexStr(0xFF11);
        FF11 ff11 = new FF11(ff11Str);
        ff11.getStan();
        
        String cardno = ff11.getCardNo();
        cardno = ISOUtil.unPadRight(cardno, 'F');
        bertlv.addHexStr(LMSTag.LMSCardNumber, cardno);
        bertlv.addHexStr(LMSTag.LMSCardExpirationDate, ff11.getCardExp());
        bertlv.addHexStr(LMSTag.LMSInvoiceNumber, ff11.getInvoiceNo());
        
        String ff21Str = bTlv.getHexStr(0xFF21);
        FF21 ff21 = new FF21(ff21Str);
        bertlv.addHexStr(LMSTag.LMSProcessingCode, ff21.getPcode());
        bertlv.addHexStr(LMSTag.BatchNumber, ff21.getBatchNo());
        bertlv.addHexStr(LMSTag.IssuerNumber, ff21.getIssNo());
        bertlv.addHexStr(LMSTag.StoreCounterArea, ISOUtil.hexString(ff21.getStoreCntArea().getBytes()));
        bertlv.addHexStr(LMSTag.ICCATC, ff21.getIccAtc());
        bertlv.addHexStr(LMSTag.TerminalTransactionDateTime, ff21.getTermDateTime());
        bertlv.addHexStr(LMSTag.HostTransactionDateTime, ff21.getHostDateTime());
        bertlv.addHexStr(LMSTag.SAMArea, "        ");//補空白
        bertlv.add(LMSTag.ChipCardStatus, ff21.getChipCardSts().getBytes());
        bertlv.add(LMSTag.AccessMode, ff21.getAccessMod().getBytes());
        
         現金交易或點數交易 
        double ecash = Double.parseDouble(ff21.getEcashAmt());
        double lmsamt = Double.parseDouble(ff21.getLmsAmt());
        if (ecash>0){
            bertlv.addHexStr(LMSTag.LoyaltyTransactionAmount, ff21.getEcashAmt());
        }else if (lmsamt>0){
            bertlv.addHexStr(LMSTag.LoyaltyTransactionAmount, ff21.getLmsAmt());
        }else{
            bertlv.addHexStr(LMSTag.LoyaltyTransactionAmount, "0000000000");
        }
        
         遠傳 pos txn uid 
        if (bTlv.hasTag(LMSTag.PosTxnUid)){
            bertlv.add(LMSTag.PosTxnUid, bTlv.getValue(LMSTag.PosTxnUid));
        }
        
         FF40 orig txn data 
        if (bTlv.hasTag(LMSTag.OriginalCardArea)){
            bertlv.addHexStr(LMSTag.OriginalCardArea, bTlv.getHexStr(LMSTag.OriginalCardArea));
        }
        
        if (bTlv.hasTag(0xFF36)){
            String str = bTlv.getHexStr(0xFF36);
            bertlv.addHexStr(LMSTag.ChipPointsTransactionArea, str);
            
            //offline FF49 Chip Points Transaction Area
            int len = 36;
            int size = str.length();
            if (size>0){
                StringBuilder buf = new StringBuilder(); 
                for (int i=1;i<size-1;i++){
                    if (str.length()>=len*i){   
                        buf.append(ISOUtil.padLeft(i+"", 2, '0')+str.substring(0,len*i));
                    }
                }
                bertlv.addHexStr(LMSTag.UploadChipPointsTransactionArea,buf.toString());
            }
        }
        
        //offline FF49 Chip Points Transaction Area
        if(!bertlv.hasTag(LMSTag.UploadChipPointsTransactionArea))
        {
            StringBuilder buf = new StringBuilder(); 
            if (ecash>0){
                buf.append("01" + "0000000000" + ff21.getEcashAmt() + "0001010199991231");
                bertlv.addHexStr(LMSTag.UploadChipPointsTransactionArea,buf.toString());
            }           
        }
        
        if (bTlv.hasTag(LMSTag.ChipPointsBalanceArea)){
            String str = bTlv.getHexStr(LMSTag.ChipPointsBalanceArea);
            bertlv.addHexStr(LMSTag.ChipPointsBalanceArea, str);//交易前餘額
            
            //online FF4A
            int len=40;
            int size = str.length()/len;
            if (size>0){
                StringBuilder buf = new StringBuilder(); 
                for (int i=1;i<size-1;i++){
                    if (str.length()>=len*i){   
                        buf.append(ISOUtil.padLeft(i+"", 2, '0')+str.substring(0,len*i-2));
                    }
                }
                bertlv.addHexStr(LMSTag.UploadChipPointsBalanceArea,buf.toString());
            }
        }
        
        //offline FF4A Chip Points Balance Area
        if(!bertlv.hasTag(LMSTag.UploadChipPointsBalanceArea))
        {
            StringBuilder buf = new StringBuilder(); 
            if (ecash>0){
                buf.append("01" + "0000000000" + "00" + ff21.getEcashBeforeBal() + "0001010199991231");
                bertlv.addHexStr(LMSTag.UploadChipPointsBalanceArea,buf.toString());
            }           
        }
        
        if (bTlv.hasTag(LMSTag.PaperCouponResponseArea)){
            String str = bTlv.getHexStr(0xFF3A);
            bertlv.addHexStr(0xFF3A, str);
            
            //online FF4D
            int len=40;
            int size = str.length()/len;
            if (size>0){
                StringBuilder buf = new StringBuilder(); 
                for (int i=1;i<size-1;i++){
                    if (str.length()>=len*i){   
                        buf.append(ISOUtil.padLeft(i+"", 2, '0')+str.substring(0,len*i));
                    }
                }
                bertlv.addHexStr(0xFF4D,buf.toString());
            }
        }
        if (bTlv.hasTag(0xFF43)){
            bertlv.addHexStr(0xFF43, bTlv.getHexStr(0xFF43));
        }
        if (bTlv.hasTag(0xFF37)){
            String str = bTlv.getHexStr(0xFF37);
            bertlv.addHexStr(0xFF37, str);
            
            //online FF4B
            int len = 36;
            int size = str.length()/len;
            if (size>0){
                StringBuilder buf = new StringBuilder(); 
                for (int i=1;i<size-1;i++){
                    if (str.length()>=len*i){   
                        buf.append(ISOUtil.padLeft(i+"", 2, '0')+str.substring(0,len*i));
                    }
                }
                bertlv.addHexStr(0xFF4B,buf.toString());
            }
        }
        if (bTlv.hasTag(0xFF39)){
            String str = bTlv.getHexStr(0xFF39);
            bertlv.addHexStr(0xFF39, str);//可多組交易前點卷餘額
            
            //online FF5A
            int len=38;
            int size = str.length()/len;
            if (size>0){
                StringBuilder buf = new StringBuilder(); 
                for (int i=1;i<size-1;i++){
                    if (str.length()>=len*i){   
                        String tmp = str.substring(0,len*i-2);
                        buf.append(ISOUtil.padLeft(i+"", 2, '0'));
                        buf.append(tmp.substring(0,10));//Coupon Id
                        buf.append(tmp.substring(26,36));//Coupon Quantity
                        buf.append(tmp.substring(10,26));//Coupon Period
                    }
                }
                bertlv.addHexStr(0xFF5A,buf.toString());
            }
        }
//      if (bTlv.hasTag(0xFF41)){
//          bertlv.addHexStr(0xFF41, bTlv.getHexStr(0xFF41));//可多組交易後點卷餘額
//      }
        if (bTlv.hasTag(0xFF3B)){
            String str = bTlv.getHexStr(0xFF3B);
            bertlv.addHexStr(0xFF3B, str);
            
            //online FF4F
            int len=40;
            int size = str.length()/len;
            if (size>0){
                StringBuilder buf = new StringBuilder(); 
                for (int i=1;i<size-1;i++){
                    if (str.length()>=len*i){   
                        buf.append(ISOUtil.padLeft(i+"", 2, '0')+str.substring(0,len*i));
                    }
                }
                bertlv.addHexStr(0xFF4F,buf.toString());
            }
        }
        if (bTlv.hasTag(0xFF42)){
            bertlv.addHexStr(0xFF42, bTlv.getHexStr(0xFF42));
        }
        if (bTlv.hasTag(0xFF6B)){
            String str = bTlv.getHexStr(0xFF6B);
            bertlv.addHexStr(0xFF6B, str);
            //online FF6E
            int size = str.length()/48;
            if (size>0){
                StringBuilder buf = new StringBuilder(); 
                for (int i=1;i<size-1;i++){
                    if (str.length()>=48*i){    
                        buf.append(ISOUtil.padLeft(i+"", 2, '0')+str.substring(0,48*i));
                    }
                }
                bertlv.addHexStr(0xFF6E,buf.toString());
            }
        }
        if (bTlv.hasTag(0xFF6C)){
            String str = bTlv.getHexStr(0xFF6C);
            bertlv.addHexStr(0xFF6C, str);
            //online FF6F
            int len = 48;
            int size = str.length()/len;
            if (size>0){
                StringBuilder buf = new StringBuilder(); 
                for (int i=1;i<size-1;i++){
                    if (str.length()>=len*i){   
                        buf.append(ISOUtil.padLeft(i+"", 2, '0')+str.substring(0,len*i));
                    }
                }
                bertlv.addHexStr(0xFF6F,buf.toString());
            }
        }
        if (bTlv.hasTag(0xFF34)){
            bertlv.addHexStr(0xFF34, bTlv.getHexStr(0xFF34));
        }
        FF3C ff3c = new FF3C(null);
        ff3c.setPcode(ff21.getPcode());
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
        ff3c.setUpdAwdCoupon("00");//FF6E (from batch FF6B)
        ff3c.setUpdAwdPoint("00");//FF6F  (from batch FF6C)
        ff3c.setUpdChipPointTxn("00");//FF49  (from batch FF36)
        ff3c.setUpdChipPointBal("00");//FF4A  (from batch FF38 FF44)
        ff3c.setUpdChipCouponTxn("00");//FF4B   (from batch FF37)
        ff3c.setUpdChipPointRsp("00");//FF4D  (from batch FF3A)
        ff3c.setUpdChipCouponRsp("00");//FF4F  (from batch FF3B)
        ff3c.setUpdChipCouponBal("00");//FF5A  (from batch FF39 FF41)
        
        int maxsize = 0;
        String str6e = bertlv.getHexStr(0xFF6E);
        int len6e = 50;
        int size6e = 0;
        if (str6e!=null){
            size6e = str6e.length()/len6e;
            if (size6e>maxsize){
                maxsize = size6e; 
            }
        }
        String str6f = bertlv.getHexStr(0xFF6F);
        int len6f = 50;
        int size6f = 0;
        if (str6f!=null){
            size6f = str6f.length()/len6f;
            if (size6f>maxsize){
                maxsize = size6f; 
            }
        }
        String str49 = bertlv.getHexStr(LMSTag.UploadChipPointsTransactionArea);
        int len49 = 38;
        int size49 = 0;
        if (str49!=null){
            size49 = str49.length()/len49;
            if (size49>maxsize){
                maxsize = size49; 
            }
        }
        String str4a = bertlv.getHexStr(LMSTag.UploadChipPointsBalanceArea);
        int len4a = 40;
        int size4a = 0;
        if (str4a!=null){
            size4a = str4a.length()/len4a;
            if (size4a>maxsize){
                maxsize = size4a; 
            }
        }
        String str4b = bertlv.getHexStr(0xFF4B);
        int len4b = 38;
        int size4b = 0;
        if (str4b!=null){
            size4b = str4b.length()/len4b;
            if (size4b>maxsize){
                maxsize = size4b; 
            }
        }
        String str4d = bertlv.getHexStr(0xFF4D);
        int len4d = 42;
        int size4d = 0;
        if (str4d!=null){
            size4d = str4d.length()/len4d;
            if (size4d>maxsize){
                maxsize = size4d; 
            }
        }
        String str4f = bertlv.getHexStr(0xFF4F);
        int len4f = 42;
        int size4f = 0;
        if (str4f!=null){
            size4f = str4f.length()/len4f;
            if (size4f>maxsize){
                maxsize = size4f; 
            }
        }
        String str5a = bertlv.getHexStr(0xFF5A);
        int len5a = 38;
        int size5a = 0;
        if (str5a!=null){
            size5a = str5a.length()/len5a;
            if (size5a>maxsize){
                maxsize = size5a; 
            }
        }
        logger.info("max size:"+maxsize);
        if (maxsize<=0){
            bertlv.addHexStr(0xFF3C ,ff3c.pack());
        }else{
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<maxsize;i++){
                if (size6e>i){
                    int idx = len6e * i;
                    ff3c.setUpdAwdCoupon(str6e.substring(idx,idx+2));//FF6E (from batch FF6B)   
                }else{
                    ff3c.setUpdAwdCoupon("00");//FF6E (from batch FF6B)
                }
                if (size6f>i){
                    int idx = len6f * i;
                    ff3c.setUpdAwdPoint(str6f.substring(idx,idx+2));//FF6F  (from batch FF6C)   
                }else{
                    ff3c.setUpdAwdPoint("00");//FF6F  (from batch FF6C)
                }
                if (size49>i){
                    int idx = len49 * i;
                    ff3c.setUpdChipPointTxn(str49.substring(idx,idx+2));//FF49  (from batch FF36)
                    logger.debug("str49:"+ff3c.getUpdChipPointBal());
                }else{
                    ff3c.setUpdChipPointTxn("00");//FF49  (from batch FF36)
                }
                if (size4a>i){
                    int idx = len4a * i;
                    ff3c.setUpdChipPointBal(str4a.substring(idx, idx+2));
                    logger.debug("size4a:"+ff3c.getUpdChipPointBal());
                }else{
                    ff3c.setUpdChipPointBal("00");//FF4A  (from batch FF38 FF44)
                }
                if (size4b>i){
                    int idx = len4b * i;
                    ff3c.setUpdChipCouponTxn(str4b.substring(idx, idx+2));//FF4B   (from batch FF37)                
                }else{
                    ff3c.setUpdChipCouponTxn("00");//FF4B   (from batch FF37)
                }
                if (size4d>i){
                    int idx = len4d * i;
                    ff3c.setUpdChipPointRsp(str4d.substring(idx,idx+2));//FF4D  (from batch FF3A)   
                }else{
                    ff3c.setUpdChipPointRsp("00");//FF4D  (from batch FF3A)
                }
                if (size4f>i){
                    int idx = len4f * i;
                    ff3c.setUpdChipCouponRsp(str4f.substring(idx, idx+2));//FF4F  (from batch FF3B) 
                }else{
                    ff3c.setUpdChipCouponRsp("00");//FF4F  (from batch FF3B)
                }
                if (size5a>i){
                    int idx = len5a * i;
                    ff3c.setUpdChipCouponBal(str5a.substring(idx,idx+2));//FF5A  (from batch FF39 FF41)     
                }else{
                    ff3c.setUpdChipCouponBal("00");//FF5A  (from batch FF39 FF41)
                }
                sb.append(ff3c.pack());
            }
            bertlv.addHexStr(0xFF3C ,sb.toString());
        }

        return bertlv;
    }*/
    
    public static BerTLV batch2online(BerTLV bTlv){

        StringBuilder buf = new StringBuilder(); 
        BerTLV bertlv = BerTLV.createInstance();
        String ff11Str = bTlv.getHexStr(0xFF11);
        String ff21Str = bTlv.getHexStr(0xFF21);
        FF11 ff11 = new FF11(ff11Str);
        FF21 ff21 = new FF21(ff21Str);
        
        String cardno = ff11.getCardNo();
        cardno = ISOUtil.unPadRight(cardno, 'F');
        bertlv.addHexStr(LMSTag.LMSCardNumber, cardno);
        bertlv.addHexStr(LMSTag.LMSCardExpirationDate, ff11.getCardExp());
        bertlv.addHexStr(LMSTag.LMSInvoiceNumber, ff11.getInvoiceNo());
        bertlv.addHexStr(LMSTag.LMSProcessingCode, ff21.getPcode());
        bertlv.addHexStr(LMSTag.BatchNumber, ff21.getBatchNo());
        bertlv.addHexStr(LMSTag.IssuerNumber, ff21.getIssNo());
        bertlv.addHexStr(LMSTag.StoreCounterArea, ISOUtil.hexString(ff21.getStoreCntArea().getBytes()));
        bertlv.addHexStr(LMSTag.ICCATC, ff21.getIccAtc());
        bertlv.addHexStr(LMSTag.TerminalTransactionDateTime, ff21.getTermDateTime());
        bertlv.addHexStr(LMSTag.HostTransactionDateTime, ff21.getHostDateTime());
        bertlv.addHexStr(LMSTag.SAMArea, ff21.getSamArea());//補空白
        bertlv.add(LMSTag.ChipCardStatus, ff21.getChipCardSts().getBytes());
        bertlv.add(LMSTag.AccessMode, ff21.getAccessMod().getBytes());
        
        
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
          
        return bertlv;
    }
    
    public static void main(String[] args){
        
    }
}
