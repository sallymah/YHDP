package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.validator;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.AmountUtil;
import tw.com.hyweb.core.cp.online.loyalty.util.Rcode;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.ISOMsg;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.TransferUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxn;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnDetail;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnHeader;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.util.Field;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ParserValidator  implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(ParserValidator.class);
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    private static final String ECASH_BONUS_ID = "0000000000";
    private static final String QUANTITY_SIGN_PLUS = "00";
    private static final String QUANTITY_SIGN_MINUS = "01";
    private static final String START_DATE = "00010101";
    private static final String END_DATE = "99991231";
    private Properties transTypePcode = null;//Properties();
    private Properties specialTransTypePcode = null;//Properties();
    
    private TrafficTxn tfcTxn;
    
    public Context validate(Context ctx)
    {
        logger.debug("validate ParserValidator ");
        BATCHContext bctx = (BATCHContext)ctx;
        try
        {
            String rawData = bctx.getRawData();
            boolean isHeader = isHeader(bctx);
            tfcTxn.checkDataLen(bctx);
            if(!ctx.getRcode().equals("0000"))
            {
                return ctx;
            }

            if(isHeader)
            {
                TrafficTxnHeader tfcTxnH = tfcTxn.parserHeader(rawData);
                tfcTxn.dataListToString(isHeader, tfcTxnH.getRawDataList(), tfcTxnH.getDataList());
                bctx.setTrafficTxnHeader(tfcTxnH);
            }
            else
            {
                TrafficTxnDetail tfcTxnDetail = tfcTxn.parserDetail(rawData);
                tfcTxn.dataListToString(isHeader, tfcTxnDetail.getRawDataList(), tfcTxnDetail.getDataList());
                bctx.setTrafficTxnDetail(tfcTxnDetail);
                setTlv(ctx);
                BerTLV lmsMsg = bctx.getLMSMsg();
                ISOMsg isoMsg = ctx.getIsoMsg();
                logger.info("isoMsg:"+isoMsg);
                logger.info("lmsMsg:"+lmsMsg);
            }  
        }
        catch (TxException e)
        {
            logger.error("",e);
            bctx.setErrDesc(e.getMessage());
            ctx.setRcode(Rcode.LACK_FIELD);
            return ctx;
        }
        catch (Exception e)
        {
            logger.error("",e);
            ctx.setRcode(Rcode.SQL_FAIL);
            return ctx;
        }
        return ctx;
    }
    
    public void setTlv(Context ctx) throws ParseException, SQLException
    {
        BATCHContext bctx = (BATCHContext)ctx;
        StringBuilder buf = new StringBuilder();
        BerTLV tlv = bctx.getLMSMsg();
        ISOMsg isoMsg = ctx.getIsoMsg();
        TrafficTxnDetail tfcTxnDetail = bctx.getTrafficTxnDetail();
        convertTraData(bctx);//轉換台鐵
        String termDateTime = tfcTxnDetail.getTransDate();;
        Date termDate = null;
        if(!termDateTime.equals("00000000000000"))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            termDate = sdf.parse(termDateTime);
            ctx.setTimeTxInit(termDate);
        }        
        tlv.addHexStr(LMSTag.TerminalTransactionDateTime, termDateTime);
        String cardno = tfcTxnDetail.getDataRecode(TransferUtil.PID);
        cardno = ISOUtil.unPadRight(cardno, 'F');
        tlv.addHexStr(LMSTag.LMSCardNumber, cardno);
        String specialFlag = tfcTxnDetail.getDataRecode(TransferUtil.SPECIAL_FLAG);
        tlv.delByTag(LMSTag.LMSProcessingCode);
        String pcode;
        
        if(specialFlag.equals("01"))
        {
            pcode = getPcodeFromDb(tfcTxnDetail.getTransType(), bctx.getConnection());
            if(StringUtil.isEmpty(pcode))
            {
                pcode = (String)specialTransTypePcode.getProperty(tfcTxnDetail.getTransType());
                //優惠補助啟用代碼表(0x00不為優惠;0x01優惠交易)
                if(!StringUtil.isEmpty(pcode))
                {
                    tlv.addHexStr(LMSTag.LMSProcessingCode, pcode);
                }
                else
                {
                    tlv.addHexStr(LMSTag.LMSProcessingCode, "9999");
                }
            } else {
                tlv.addHexStr(LMSTag.LMSProcessingCode, pcode);
            }
        }
        else
        {
            pcode = getPcodeFromDb(tfcTxnDetail.getTransType(), bctx.getConnection());
            if(StringUtil.isEmpty(pcode))
            {
                pcode = (String)transTypePcode.getProperty(tfcTxnDetail.getTransType());
                if(!StringUtil.isEmpty(pcode))
                {
                    tlv.addHexStr(LMSTag.LMSProcessingCode, pcode);
                }
                else
                {
                    tlv.addHexStr(LMSTag.LMSProcessingCode, "9999");
                }
            } else {
                tlv.addHexStr(LMSTag.LMSProcessingCode, pcode);
            }
        }
        
        String atc = tfcTxnDetail.getDataRecode(TransferUtil.TRANS_NO);
        if(StringUtil.isEmpty(atc))
        {
            atc = "00000000";
        }
        else
        {
            atc = ISOUtil.padLeft(atc, 8, '0');
        }
        
        isoMsg.set(Field.PROC_CODE, "900000");
        tlv.addHexStr(LMSTag.ICCATC, atc);
        tlv.addHexStr(LMSTag.SAMArea, tfcTxnDetail.getDataRecode(TransferUtil.SAM_OSN));//補空白
        tlv.addHexStr(LMSTag.TSAMOSN, tfcTxnDetail.getDataRecode(TransferUtil.SAM_TRANS_SEQ));//補空白
        tlv.addHexStr(LMSTag.BatchNumber, "000001");//補空白
        tlv.addHexStr(LMSTag.TransactionType, "05");
        
        //目前值太大 isoMsg.set(Field.TRACE_NBR, tfcTxnDetail.getDataRecode(TransferUtil.TERM_TX_SEQ));
        isoMsg.set(Field.TRACE_NBR, "000001");
        
        MerchTermFiller(bctx);
        
        isoMsg.set(64, ISOUtil.hex2byte(tfcTxnDetail.getDataRecode(TransferUtil.SAM_MAC)));
        
        /* 現金交易或點數交易 */
        boolean isBalNegative = false;
        double ecashBeforeBal = Double.parseDouble(tfcTxnDetail.getDataRecode(TransferUtil.BEFORE_BAL));
        if(ecashBeforeBal > 32767){//超過7FFF(32767), 等同負值
            isBalNegative = true;
            ecashBeforeBal = (65536 - ecashBeforeBal);//20161006
        }
        double ecash = Double.parseDouble(tfcTxnDetail.getDataRecode(TransferUtil.TOPUP_AMT));
        String ecashStr = AmountUtil.format(ecash == 0 ? 0 :ecash, "00000000.00");
        String ecashBeforeBalStr = AmountUtil.format(ecashBeforeBal == 0 ? 0 :ecashBeforeBal, "00000000.00");
        if (ecash>0){
            tlv.addHexStr(LMSTag.LoyaltyTransactionAmount, ecashStr);
        }else{
            tlv.addHexStr(LMSTag.LoyaltyTransactionAmount, "0000000000");
        }
        
        /* 20170224 dongle chip after bonus */
        double ecashAfterBal = Double.parseDouble(tfcTxnDetail.getDataRecode(TransferUtil.AFTER_BAL));
        if(ecashAfterBal > 32767){//超過7FFF(32767), 等同負值
            ecashAfterBal = 0 - (65536 - ecashAfterBal);//20161006
        }
        bctx.setChipAfterBonusQty(ecashAfterBal);
        
        /* 晶片餘額 */
        if (!isBalNegative){
            buf.delete(0, buf.length());
            buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_PLUS + ecashBeforeBalStr + START_DATE + END_DATE + "00");
            tlv.addHexStr(LMSTag.ChipPointsBalanceArea,buf.toString());
        }  
        else{
            buf.delete(0, buf.length());
            buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_MINUS + ecashBeforeBalStr + START_DATE + END_DATE + "00");
            tlv.addHexStr(LMSTag.ChipPointsBalanceArea,buf.toString());
        } 
        
        /* 儲值金消費金額 */
  /*      if (ecash>0){
            buf.delete(0, buf.length());
            buf.append(ECASH_BONUS_ID + ecashStr + START_DATE + END_DATE);
            tlv.addHexStr(LMSTag.ChipPointsTransactionArea,buf.toString());
        }*/
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
    public boolean isHeader(BATCHContext ctx)
    {
        return ctx.getRawData().substring(0, 4).equals(TrafficTxn.ITEM_HEADER);
    }
    
    public void setTrafficTxn(TrafficTxn tfcTxn)
    {
        this.tfcTxn = tfcTxn;
    }
    
    public TrafficTxn getTrafficTxn()
    {
        return this.tfcTxn;
    }
    
    public void getMerchTerm(BATCHContext bctx)
    {
        
    }
    
    public TbTermInfo getMerchTid(String acqMemId, Connection conn) throws SQLException
    {       
        TbTermInfo termInfo = null;
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select A.MERCH_ID, A.TERM_ID from TB_TERM  A, TB_MEMBER B where B.mem_id = ? and A.mem_id = B.mem_id");
        Vector<String> params = new Vector<String>();
        params.add(acqMemId);
        
        Vector result = DbUtil.select(sqlCmd.toString(),params,conn);
        if (result!=null && result.size()>0)
        {
            termInfo = new TbTermInfo();
            Vector record = (Vector) result.get(0);
            if(null != record && record.size() > 0)
            {
                termInfo.setMerchId((String)record.get(0));
                termInfo.setTermId((String)record.get(1));
            }
        }
        return termInfo;
    }
    
    public void convertTraData(BATCHContext ctx) throws ParseException
    {
        SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmss");
        TrafficTxnDetail tfcTxnDetail = ctx.getTrafficTxnDetail();
        String version = tfcTxnDetail.getDataRecode(TransferUtil.TRAFFIC_VER);
        int intVer = version == null ? 3 : Integer.valueOf(version);
        logger.debug("convertTraData");
        
        if(intVer == TransferUtil.TRAFIC_VER_TRA || intVer == TransferUtil.TRAFIC_VER_BUS_FINAL)
        //if(txnType.equals("02") || txnType.equals("08"))
        {
            //atc
            String rawData = tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_NO);
            String cnvData =  hex2IntString(LittleEndian(rawData));
            logger.debug("atc ==> " + cnvData);
            tfcTxnDetail.setDataRecode(TransferUtil.TRANS_NO, cnvData);
            
            rawData = tfcTxnDetail.getRawDataRecode(TransferUtil.TRANS_DATE);
            cnvData =  unixTimeToYYYMMDD(LittleEndian(rawData));
            cnvData = cnvtGMT0(sd.parse(cnvData));
            tfcTxnDetail.setDataRecode(TransferUtil.TRANS_DATE, cnvData);
            logger.debug("transDate ==> " + cnvData);
            
            rawData = tfcTxnDetail.getRawDataRecode(TransferUtil.BEFORE_BAL);
            cnvData =  hex2IntString(LittleEndian(rawData));
            logger.debug("BEFORE_BAL ==> " + cnvData);
            tfcTxnDetail.setDataRecode(TransferUtil.BEFORE_BAL, cnvData);
            
            rawData = tfcTxnDetail.getRawDataRecode(TransferUtil.TOPUP_AMT);
            cnvData =  hex2IntString(LittleEndian(rawData));
            logger.debug("TOPUP_AMT ==> " + cnvData);
            tfcTxnDetail.setDataRecode(TransferUtil.TOPUP_AMT, cnvData);
            
            rawData = tfcTxnDetail.getRawDataRecode(TransferUtil.AFTER_BAL);
            cnvData =  hex2IntString(LittleEndian(rawData));
            logger.debug("AFTER_BAL ==> " + cnvData);
            tfcTxnDetail.setDataRecode(TransferUtil.AFTER_BAL, cnvData);
            
            rawData = tfcTxnDetail.getRawDataRecode(TransferUtil.DEV_ID);
            cnvData =  LittleEndian(rawData);
            logger.debug("DEV_ID ==> " + cnvData);
            tfcTxnDetail.setDataRecode(TransferUtil.DEV_ID, cnvData);
            
            cnvData = tfcTxnDetail.getRawDataRecode(TransferUtil.SAM_OSN);
            tfcTxnDetail.setDataRecode(TransferUtil.SAM_OSN, cnvData);
        }
        else if(intVer == TransferUtil.TRAFIC_VER_KRTC)
        //else if(txnType.equals("05"))//高捷transDate - 8小時
        {
            String rawData = tfcTxnDetail.getDataRecode(TransferUtil.TRANS_DATE);
            String cnvData = cnvtGMT0(sd.parse(rawData));
            tfcTxnDetail.setDataRecode(TransferUtil.TRANS_DATE, cnvData);
            logger.debug("transDate ==> " + cnvData);
        }
    }
    
    public String cnvtGMT0(Date date)
    {
        SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmss");
        sd.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        return sd.format(date);
    }
    
    public String unixTimeToYYYMMDD(String lsdHex)
    {
        if(!StringUtil.isEmpty(lsdHex))
        {
            if(!lsdHex.equals("00000000"))
            {
                DateFormat dFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                long unixTime = new BigInteger(lsdHex, 16).longValue()*1000;
                Date currDate = new Date(unixTime);
                return dFormat.format(currDate);
            }
        }
        return "";
    }
    
    public String hex2IntString(String lsdHex)
    {
        int amt = 0;
        if(!StringUtil.isEmpty(lsdHex))
        {
            amt = Integer.parseInt(lsdHex, 16);
        }
        return String.valueOf(amt);
    }
    
    public void MerchTermFiller(BATCHContext ctx)
    {
        TrafficTxnDetail tfcTxnDetail = ctx.getTrafficTxnDetail();
        ISOMsg isoMsg = ctx.getIsoMsg();
        StringBuffer ecaMerchId = new StringBuffer();
        String rawLocId = tfcTxnDetail.getDataRecode(TransferUtil.LOC_ID);
        String rawLocIdAp = tfcTxnDetail.getDataRecode(TransferUtil.LOC_ID_AP);
        String locId = rawLocId;
        if(!StringUtil.isEmpty(rawLocIdAp) && !rawLocIdAp.equals("00"))//因為前台loc_id會帶錯，故此欄位有帶值即以此取代
        {
            locId = rawLocIdAp;
            ctx.setRealLocId(rawLocId);
        }
        ctx.setRealLocId(locId);
        
        /*  if(transSysNo.equals(TransferUtil.TRANS_SYS_NO_KRTC))//捷運需轉為10進位,移除因為LOC_ID統一邏輯 jackie
        {
            locId = String.valueOf(Integer.parseInt(rawLocId, 16));
            //tfcTxnDetail.setDataRecode(TransferUtil.LOC_ID, locId);
        }*/
        ecaMerchId.append(ISOUtil.padLeft(tfcTxnDetail.getDataRecode(TransferUtil.TRANS_SYS_NO), 15, '0'));
        
        isoMsg.set(Field.MERCHANT_ID, ecaMerchId.toString());
        isoMsg.set(Field.TERMINAL_ID, ISOUtil.padLeft(locId, 8, '0'));
    }
    
    /**
     * @return the txnTypePcodeMap
     */
    public Properties getTransTypePcode()
    {
        return transTypePcode;
    }

    /**
     * @param txnTypePcodeMap the txnTypePcodeMap to set
     */
    public void setTransTypePcode(Properties transTypePcode)
    {
        this.transTypePcode = transTypePcode;
    }

    /**
     * @return the specialtxnTypePcodeMap
     */
    public Properties getSpecialTransTypePcode()
    {
        return specialTransTypePcode;
    }

    /**
     * @param specialtxnTypePcodeMap the specialtxnTypePcodeMap to set
     */
    public void setSpecialTransTypePcode(Properties specialTransTypePcode)
    {
        this.specialTransTypePcode = specialTransTypePcode;
    }
    
    public String getPcodeFromDb(String transType, Connection conn) throws SQLException
    {
        String pCode = null;
        if(!StringUtil.isEmpty(transType)) {
            String sqlCmd = "SELECT P_CODE FROM TB_TRANS_TYPE WHERE TRANS_TYPE = ?";
            Vector<String> parms = new Vector<String>();
            parms.add(transType);
            pCode = DbUtil.getString(sqlCmd, parms ,conn);
        }
        return pCode;
    }
}