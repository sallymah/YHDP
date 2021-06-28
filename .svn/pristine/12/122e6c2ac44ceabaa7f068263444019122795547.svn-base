package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.validator;

import org.apache.log4j.Logger;


import com.formosoft.ss.stub.CGYHDPSSFacade;

import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.TransferUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnDetail;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;

/*
 * 程式版本，YHDP使用for MAC驗證邏輯，Mediation填寫
   0x01 : 原統聯(桃)、科技之星及亞通的驗證邏輯
          KEY_ID=tawk11, SingleTAC;IV=464953432D535643 or 5948445043415244
   0x02 : 高捷的邏輯
          KEY_ID=SingleTAC;IV=5948445043415244
   0x03 : 台鐵及已完成MAC調整軌道業者;
          KEY_ID=SingleTAC;IV=5948445043415244
   0x04 : 雙北公車業者及已完成MAC調整公車業者;
          KEY_ID=tawk11;IV=464953432D535643 or 5948445043415244
   0x05 : 桃捷 RSAM+LPTSAM的驗證邏輯;
          KEY_ID=SingleTAC;IV=5948445043415244
          KEY_ID=tawk11;IV=464953432D535643 or 5948445043415244
 */
public class MacValidator implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(MacValidator.class);

    private String macKeyID = "SingleTAC";
    private String busMacKeyId = "twak11";
    private boolean isCheck = true;
    private String servletUrl = "http://YhdpSS:36888/YHDP_SSServlet/SS";
    private int slot = 2;
    private String pin = "BAA6B7BBAFCECDCCCBF7F7F7F7F7F7F7F7";
    private boolean ver5First3 = true;

    /* (non-Javadoc)
     * @see tw.com.hyweb.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        logger.debug("validate");
        try
        {
            int loopCnt = 0;
            String iv1 = ISOUtil.hexString("FISC-SVC".getBytes());//464953432D535643
            String iv2 = ISOUtil.hexString("YHDPCARD".getBytes());//5948445043415244
            
            BATCHContext bctx = (BATCHContext)ctx;
            if(null != bctx.getTbInctlInfo())
            {
                if(!StringUtil.isEmpty(bctx.getTbInctlInfo().getFileName()))
                {
                    if(bctx.getTbInctlInfo().getFileName().equals("R_TRN_TXN"))
                    {
                        //pass
                        logger.debug("regen file not check mac");
                        return  ctx;
                    }
                }
            }
            TrafficTxnDetail tfcTxnDetail = bctx.getTrafficTxnDetail();
            String version = tfcTxnDetail.getDataRecode(TransferUtil.TRAFFIC_VER);
            int intVer = version == null ? 3 : Integer.valueOf(version);
            
            ctx.setRcode("0000");
            if(intVer == TransferUtil.TRAFIC_VER_BUS)
            //if(txnType.equals("01"))//公車
            {
                macValidator(ctx, iv1, busMacKeyId, loopCnt, intVer);//464953432D535643
                
                if(ctx.getRcode().equals(Rcode.MAC_ERROR))
                {
                    loopCnt++;
                    ctx.setRcode("0000");
                    macValidator(ctx, iv2, busMacKeyId, loopCnt, intVer);//5948445043415244
                }
                
                if(ctx.getRcode().equals(Rcode.MAC_ERROR))
                {
                    loopCnt++;
                    ctx.setRcode("0000");
                    macValidator(ctx, iv2, macKeyID, loopCnt, intVer);//5948445043415244
                }
            }//else if(txnType.equals("02") || txnType.equals("08"))//台鐵,停車場,高捷
            else if(intVer == TransferUtil.TRAFIC_VER_KRTC ||
                    intVer == TransferUtil.TRAFIC_VER_TRA)
            {
                macValidator(ctx, iv2, macKeyID, loopCnt, intVer);//464953432D535643
            }
            else if (intVer == TransferUtil.TRAFIC_VER_BUS_FINAL)
            {
                macValidator(ctx, iv1, busMacKeyId, loopCnt, intVer);//464953432D535643
                
                if(ctx.getRcode().equals(Rcode.MAC_ERROR))
                {
                    loopCnt++;
                    ctx.setRcode("0000");
                    macValidator(ctx, iv2, busMacKeyId, loopCnt, intVer);//5948445043415244
                }
            }
            else if (intVer == TransferUtil.TRAFIC_VER_TYME)
            {
                if(ver5First3)
                {
                    V053Check(bctx, iv1, iv2, intVer);
                }
                else {
                    V053Check(bctx, iv1, iv2, intVer);
                }
            }
        }
        catch (Exception e)
        {
            ctx.setRcode(Rcode.FAIL);
            logger.error(" ", e);
        }
        return ctx;
    }
    
    /*
     * version 3 first, second version 4
     */
    public void V053Check(BATCHContext ctx, String iv1, String iv2, int intVer) throws Exception
    {
        int loopCnt = 0;
        macValidator(ctx, iv2, macKeyID, loopCnt, intVer);//464953432D535643 version 3
        if(ctx.getRcode().equals(Rcode.MAC_ERROR))
        {
            loopCnt++;
            ctx.setRcode("0000");
            macValidator(ctx, iv1, busMacKeyId, loopCnt, intVer);//464953432D535643 version 4
            if(ctx.getRcode().equals(Rcode.MAC_ERROR))
            {
                loopCnt++;
                ctx.setRcode("0000");
                macValidator(ctx, iv2, busMacKeyId, loopCnt, intVer);//5948445043415244 version 4
            }
        }
        
    }
    
    /*
     * version 4 first, second version 3
     */
    public void V054Check(BATCHContext ctx, String iv1, String iv2, int intVer) throws Exception
    {
        int loopCnt = 0;
        macValidator(ctx, iv1, busMacKeyId, loopCnt, intVer);//464953432D535643 version 4
        if(ctx.getRcode().equals(Rcode.MAC_ERROR))
        {
            loopCnt++;
            ctx.setRcode("0000");
            macValidator(ctx, iv2, busMacKeyId, loopCnt, intVer);//5948445043415244 version 4
            if(ctx.getRcode().equals(Rcode.MAC_ERROR))
            {
                loopCnt++;
                ctx.setRcode("0000");
                macValidator(ctx, iv2, macKeyID, loopCnt, intVer);//464953432D535643 version 3
            }
        }
    }
    
    public Context macValidator(Context ctx, String iv, String keyID, int loopCnt, int version) throws Exception
    {
        String f64 = ctx.getIsoMsg().getString(64);
        String div = f64;
        if (f64!=null)
        {
            BATCHContext bctx = (BATCHContext)ctx;
            TrafficTxnDetail tfcTxnDetail = bctx.getTrafficTxnDetail();
            String tsamOsn = null;
          
            String raw = appendTxnLog(tfcTxnDetail, version);
            logger.debug("raw data:"+raw);
            logger.debug("f64 len:"+f64.length());
            logger.debug("no f64 raw:"+raw);
            StringBuffer strPad = new StringBuffer();
            int padNum = (8-(raw.length()/2)%8);
            if (padNum>0 && padNum<8)
            {//補1~7個byte 剛好整除時不需要補
                for (int i=0;i<padNum;i++)
                {
                    strPad.append("00");
                }
            }
            String inputDataS = raw+strPad.toString();
            logger.debug("append ox00 raw:"+inputDataS);
            
            String divData = "";
            byte[] divByte = getByte(true, divData);
            if(version == TransferUtil.TRAFIC_VER_BUS ||
                    version == TransferUtil.TRAFIC_VER_TRA ||
                    version == TransferUtil.TRAFIC_VER_BUS_FINAL ||
                    version == TransferUtil.TRAFIC_VER_TYME)
            //if(txnType.equals("01") || txnType.equals("02")  || txnType.equals("08"))//公車,台鐵,停車場
            {
                tsamOsn = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_OSN);
                divData = tsamOsn;
                divByte = getByte(true, divData);
            }
            else
            {
                /*String decTsamOsn = tfcTxnDetail.getDataRecode(TransferUtil.SAM_OSN);
                int tsnOsnLen = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_OSN).length();
                tsamOsn = ISOUtil.padLeft(decTsamOsn, tsnOsnLen, '0');*/
                tsamOsn = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_OSN);
                //divData = ISOUtil.hexString(tsamOsn.substring(8).getBytes());
                divData = tsamOsn;
                divByte = getByte(true, divData);
                logger.debug("divByte:"+divByte.length);
            }
            
            byte[] ivByte = getByte(true, iv);
            byte[] bInputData = getByte(true, inputDataS);
            byte[] bPin = ISOUtil.hex2byte(pin);
            logger.debug(ivByte.length + "," + bInputData.length + "," + divByte.length);
            
            logger.debug("loopCnt:"+loopCnt + " servletUrl="+servletUrl + " ,slot="+slot+ " ,pin="+ pin + " , keyID:"+ keyID + ", iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
            long start = java.util.Calendar.getInstance().getTimeInMillis();
            CGYHDPSSFacade yhdpFacade = new CGYHDPSSFacade(servletUrl);
            int result = yhdpFacade.GenerateTAC_1(slot, bPin, bPin.length, keyID, divByte, ivByte, bInputData);
            YHDPUtil.checkMillisTime("yhdpFacade GenerateTAC_1", 1000, start);
            if (result!=0)
            {
                logger.error("loopCnt:"+loopCnt + " iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
                ctx.setRcode(Rcode.CALL_HSM_FAIL);
                logger.error("loopCnt:"+loopCnt + " hsm return error. hsm ret code= "+ result + ", " + yhdpFacade.SS_GetErrorMsg());
                bctx.setErrDesc("loopCnt:"+loopCnt + " hsm return error. hsm ret code= "+ result + ", " + yhdpFacade.SS_GetErrorMsg());
            }
            else
            {
                byte[] bMac = yhdpFacade.GetTAC();
                if(bMac != null)
                {
                    String sMac = ISOUtil.hexString(bMac).toUpperCase();
                    /* 20160608 sMac = LmsUtil.GenerateTAC_S(((LMSContext)ctx), div, sMac);*/
                    if (sMac!=null && !sMac.equalsIgnoreCase(f64))
                    {
                        logger.error("loopCnt:"+loopCnt +" iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
                        logger.error("loopCnt:"+loopCnt + " mac error. hsm ret mac:["+sMac+"] != edc:["+f64+"]");
                        bctx.setErrDesc("loopCnt:"+loopCnt + " mac error. hsm ret mac:["+sMac+"] != edc:["+f64+"]");
                        ctx.setRcode(Rcode.MAC_ERROR);
                    }
                    else {
                        logger.debug("");
                    }
                }
                else
                {
                    logger.error("loopCnt:"+loopCnt + " iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
                    logger.error("loopCnt:"+loopCnt + " mac error. hsm ret mac:["+bMac+"] is null");
                    logger.error("loopCnt:"+loopCnt + " hsm return error. hsm ret code= "+ result + ", " + yhdpFacade.SS_GetErrorMsg());
                    bctx.setErrDesc("loopCnt:"+loopCnt + " mac error. hsm ret mac:["+bMac+"] is null, hsm ret code= "+ result + ", " + yhdpFacade.SS_GetErrorMsg());
                    ctx.setRcode(Rcode.CALL_HSM_FAIL);
                }
            }
        }
        return ctx;
    }
    
    public String appendTxnLog(TrafficTxnDetail tfcTxnDetail, int version)
    {        
        String txnType = tfcTxnDetail.getDataRecode(TransferUtil.TXN_TYPE);
        StringBuffer sb = new StringBuffer();
        if(version == TransferUtil.TRAFIC_VER_BUS || version == TransferUtil.TRAFIC_VER_TRA ||
                version == TransferUtil.TRAFIC_VER_BUS_FINAL || version == TransferUtil.TRAFIC_VER_TYME)
        //if(txnType.equals("01") || txnType.equals("02") || txnType.equals("08"))//公車,台鐵,停車場
        {
            //(SAM卡押碼內容為當次交易log(17byte) + SAMOSN(8byte) + SAM TSN(6byte))
            String tsamOsn = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_OSN);
            String tsamTsn = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_TRANS_SEQ);
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_NO));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_DATE));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_TYPE));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.TOPUP_AMT));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.AFTER_BAL));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_SYS_NO));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.LOC_ID));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.DEV_ID));
            sb.append(tsamOsn);
            sb.append(tsamTsn);
            StringBuffer logSb = new StringBuffer();
            logSb.append("TRANS_NO:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_NO));
            logSb.append(", TRANS_DATE:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_DATE));
            logSb.append(", TRANS_TYPE:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_TYPE));
            logSb.append(", TOPUP_AMT:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.TOPUP_AMT));
            logSb.append(", AFTER_BAL:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.AFTER_BAL));
            logSb.append(", TRANS_SYS_NO:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_SYS_NO));
            logSb.append(", LOC_ID:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.LOC_ID));
            logSb.append(", DEV_ID:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.DEV_ID));
            logSb.append(", tsamOsn:"+ tsamOsn);
            logSb.append(", tsamTsn:"+ tsamTsn);
            logger.debug("txn log:" + logSb.toString());
        }
        else if(version == TransferUtil.TRAFIC_VER_KRTC) {
            //(SAM卡押碼內容為當次交易log(17byte) + SAMOSN(8byte) + SAM TSN(6byte))
            String tsamOsn = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_OSN);
            int tsnOsnLen = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_OSN).length();
            //String tsamTsn = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_TRANS_SEQ);
            String tsamTsn = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_TRANS_SEQ);
            String tsnStr = new String (ISOUtil.hex2byte(tsamTsn));
            int intTsn = Integer.valueOf(tsnStr);
            String tsnHexStr = Integer.toHexString(intTsn).toUpperCase();
            tsnHexStr = ISOUtil.padLeft(tsnHexStr, tsamTsn.length(), '0');
            String cnvtTsamOsn = ISOUtil.padLeft(tsamOsn, tsnOsnLen, '0');
            sb.append(LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_NO)));
            sb.append(LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_DATE)));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_TYPE));
            sb.append(LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.TOPUP_AMT)));
            sb.append(LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.AFTER_BAL)));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_SYS_NO));
            sb.append(tfcTxnDetail.getRawDataRecode(TransferUtil.LOC_ID));
            sb.append(LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.DEV_ID)));
            sb.append(tsamOsn);
            sb.append(LittleEndian(tsnHexStr));
            StringBuffer logSb = new StringBuffer();
            logSb.append("TRANS_NO:"+ LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_NO)));
            logSb.append(", TRANS_DATE:"+ LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_DATE)));
            logSb.append(", TRANS_TYPE:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_TYPE));
            logSb.append(", TOPUP_AMT:"+ LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.TOPUP_AMT)));
            logSb.append(", AFTER_BAL:"+ LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.AFTER_BAL)));
            logSb.append(", TRANS_SYS_NO:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_SYS_NO));
            logSb.append(", LOC_ID:"+ tfcTxnDetail.getRawDataRecode(TransferUtil.LOC_ID));
            logSb.append(", DEV_ID:"+ LittleEndian(tfcTxnDetail.getRawDataRecode(TransferUtil.DEV_ID)));
            logSb.append(", tsamOsn:"+ tsamOsn);
            logSb.append(", tsamTsn:"+ LittleEndian(tsnHexStr));
            logger.debug("txn log:" + logSb.toString());
        }
        return sb.toString();
    }
    
    public byte[] getByte(boolean isHexToByte, String date)
    {
        byte[] out = null;
        if(isHexToByte == true)
        {
            out = ISOUtil.hex2byte(date);
        }
        else
        {
            out = date.getBytes();
        }
        return out;
    }
    
    public static String LittleEndian(String src)
    {       
        byte[] srcByte = ISOUtil.hex2byte(src); 
        byte[] strLE = new byte[srcByte.length];
        int decIdx = 0;
        for(int idx = srcByte.length - 1; idx >= 0; idx--)
        {
            strLE[decIdx] = srcByte[idx];
            decIdx++;
        }
        return ISOUtil.hexString(strLE);
    }
    
    /**
     * Gets the mac key id.
     * 
     * @return macKeyID
     */
    public String getMacKeyID()
    {
        return this.macKeyID;
    }
    /**
     * Sets the mac key id.
     * 
     * @param macKeyID 的設定的 macKeyID
     */
    public void setMacKeyID(String macKeyID)
    {
        this.macKeyID = macKeyID;
    }
    
    /**
     * Gets the bus mac key id.
     * 
     * @return busMacKeyID
     */
    public String getBusMacKeyId()
    {
        return this.busMacKeyId;
    }
    /**
     * Sets the bus mac key id.
     * 
     * @param busMacKeyID 的設定的 busMacKeyID
     */
    public void setBusMacKeyId(String busMacKeyId)
    {
        this.busMacKeyId = busMacKeyId;
    }
    
    /**
     * Gets the mac key id.
     * 
     * @return macKeyID
     */
    public boolean getIsCheck()
    {
        return this.isCheck;
    }
    /**
     * Sets the mac key id.
     * 
     * @param macKeyID 的設定的 macKeyID
     */
    public void setIsCheck(boolean isCheck)
    {
        this.isCheck = isCheck;
    }
    
    /**
     * @return the servletUrl
     */
    public String getServletUrl()
    {
        return servletUrl;
    }

    /**
     * @param servletUrl the servletUrl to set
     */
    public void setServletUrl(String servletUrl)
    {
        this.servletUrl = servletUrl;
    }

    /**
     * @return the solt
     */
    public int getSlot()
    {
        return slot;
    }

    /**
     * @param solt the solt to set
     */
    public void setSlot(int slot)
    {
        this.slot = slot;
    }

    /**
     * @return the pin
     */
    public String getPin()
    {
        return pin;
    }

    /**
     * @param pin the pin to set
     */
    public void setPin(String pin)
    {
        this.pin = pin;
    }
    
    /**
     * @return the ver5First3
     */
    public boolean isVer5First3()
    {
        return ver5First3;
    }

    /**
     * @param ver5First3 the ver5First3 to set
     */
    public void setVer5First3(boolean ver5First3)
    {
        this.ver5First3 = ver5First3;
    }
}
