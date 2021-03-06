package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.controller.base;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbTrafficTxnErrInfo;
import tw.com.hyweb.service.db.info.TbTrafficTxnInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.TransferUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnDetail;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnHeader;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.util.string.StringUtil;

public class trafficTxnFiller  implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(trafficTxnFiller.class);
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    
    public Context validate(Context ctx)
    {
        logger.debug("validate trafficTxnFiller ");
        BATCHContext bctx = (BATCHContext)ctx;
        try
        {
            trafficTxnInfoFiller(bctx);
            trafficTxnInfoErrFiller(bctx);
        }
        catch (Exception e)
        {
            logger.error("",e);
            ctx.setRcode(Rcode.FAIL);
            return ctx;
        }
        return ctx;
    }
    
    public void trafficTxnInfoFiller(BATCHContext bctx)
    {
        TbTrafficTxnInfo trafficTxnInfo = new TbTrafficTxnInfo();
        TrafficTxnHeader trafficHd = bctx.getTrafficTxnHeader();
        TrafficTxnDetail trafficDtl = bctx.getTrafficTxnDetail();
        BerTLV tlv = bctx.getLMSMsg();
        String lmsInvoiceNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
        trafficTxnInfo.setCardNo(bctx.getLMSCardNbr());
        trafficTxnInfo.setLmsInvoiceNo(lmsInvoiceNo);
        if(null != trafficHd)
        {
            if(!StringUtil.isEmpty(trafficHd.getProgramVer()) && trafficHd.getProgramVer().length() < 5)
            {
                trafficTxnInfo.setProgramVersion(trafficHd.getProgramVer());
            }
        }
        if(null != trafficHd)
        {
            trafficTxnInfo.setSourceDate(trafficHd.getSourceDate());
        }
        trafficTxnInfo.setItem(trafficDtl.getDataRecode(TransferUtil.ITEM_D));
        trafficTxnInfo.setTxnType(trafficDtl.getDataRecode(TransferUtil.TXN_TYPE));
        trafficTxnInfo.setTransType(trafficDtl.getDataRecode(TransferUtil.TRANS_TYPE));
        trafficTxnInfo.setTransSysNo(trafficDtl.getDataRecode(TransferUtil.TRANS_SYS_NO));
        trafficTxnInfo.setLocId(bctx.getRealLocId());
        trafficTxnInfo.setDevId(trafficDtl.getDataRecode(TransferUtil.DEV_ID));
        trafficTxnInfo.setSamOsn(trafficDtl.getDataRecode(TransferUtil.SAM_OSN));
        trafficTxnInfo.setSamTransSeq(trafficDtl.getDataRecode(TransferUtil.SAM_TRANS_SEQ));
        trafficTxnInfo.setSamMac(trafficDtl.getDataRecode(TransferUtil.SAM_MAC));
        trafficTxnInfo.setTermTxDate(trafficDtl.getDataRecode(TransferUtil.TERM_TX_DATE));
        trafficTxnInfo.setTermTxSeq(trafficDtl.getDataRecode(TransferUtil.TERM_TX_SEQ));
        trafficTxnInfo.setTermId(trafficDtl.getDataRecode(TransferUtil.TERM_ID));
        trafficTxnInfo.setStoreId(trafficDtl.getDataRecode(TransferUtil.STORE_ID));
        trafficTxnInfo.setTransNoCancel(trafficDtl.getDataRecode(TransferUtil.TRANS_NO_CANCEL));
        trafficTxnInfo.setTransDateCancel(trafficDtl.getDataRecode(TransferUtil.TRANS_DATE_CANCEL));
        trafficTxnInfo.setTxprocessDate(trafficDtl.getDataRecode(TransferUtil.TXPROCESS_DATE));
        trafficTxnInfo.setShiftNumber(trafficDtl.getDataRecode(TransferUtil.SHIFT_NUMBER));
        trafficTxnInfo.setFirstDeductValueF(trafficDtl.getDataRecode(TransferUtil.FIRST_DEDUCT_VALUE_FOR_INTER_TRANSFER));
        trafficTxnInfo.setFirstEntryStationF(trafficDtl.getDataRecode(TransferUtil.FIRST_ENTRY_STATION_FOR_INTER_TRANSFER));
        trafficTxnInfo.setInOutCode(trafficDtl.getDataRecode(TransferUtil.IN_OUT_CODE));
        trafficTxnInfo.setTrtcCtsn(trafficDtl.getDataRecode(TransferUtil.TRTC_CTSN));
        trafficTxnInfo.setRejectCode(trafficDtl.getDataRecode(TransferUtil.REJECT_CODE));
        trafficTxnInfo.setConsumptionAccuPoints(trafficDtl.getDataRecode(TransferUtil.CONSUMPTION_ACCUMAULATED_POINTS));
        trafficTxnInfo.setUserType(trafficDtl.getDataRecode(TransferUtil.USER_TYPE));
        trafficTxnInfo.setTransferFavorAmt(trafficDtl.getDataRecode(TransferUtil.TRANSFER_FAVOR_AMT));
        trafficTxnInfo.setTotalTransactionFare(trafficDtl.getDataRecode(TransferUtil.TOTAL_TRANSACTION_FARE));
        trafficTxnInfo.setOtherFavorAmt(trafficDtl.getDataRecode(TransferUtil.OTHER_FAVOR_AMT));
        trafficTxnInfo.setBvBatchNo(trafficDtl.getDataRecode(TransferUtil.BV_TRANSACTION_BATHCH_NO));
        trafficTxnInfo.setInShuttleCode(trafficDtl.getDataRecode(TransferUtil.IN_SHUTTLE_CODE));
        trafficTxnInfo.setInStationCode(trafficDtl.getDataRecode(TransferUtil.IN_STATION_CODE));
        trafficTxnInfo.setBoardingStopCode(trafficDtl.getDataRecode(TransferUtil.BOARDING_STOP_CODE));
        trafficTxnInfo.setOutShuttleCode(trafficDtl.getDataRecode(TransferUtil.OUT_SHUTTLE_CODE));
        trafficTxnInfo.setOutStationCode(trafficDtl.getDataRecode(TransferUtil.OUT_STATION_CODE));
        trafficTxnInfo.setAlightingStopCode(trafficDtl.getDataRecode(TransferUtil.ALIGHTING_STOP_CODE));
        trafficTxnInfo.setTransferFlag(trafficDtl.getDataRecode(TransferUtil.TRANSFER_FLAG));
        trafficTxnInfo.setCashForInsufficiunt(trafficDtl.getDataRecode(TransferUtil.CASH_FOR_INSUFFICIUNT));
        trafficTxnInfo.setBusLincenseId(trafficDtl.getDataRecode(TransferUtil.BUS_LINCENSE_ID));
        trafficTxnInfo.setBusDriverId(trafficDtl.getDataRecode(TransferUtil.BUS_DRIVER_ID));
        trafficTxnInfo.setBusRouteDoman(trafficDtl.getDataRecode(TransferUtil.BUS_ROUTE_DOMAN));
        trafficTxnInfo.setCustDate(trafficDtl.getDataRecode(TransferUtil.CUST_DATE));
        trafficTxnInfo.setCustDateClass(trafficDtl.getDataRecode(TransferUtil.CUST_DATE_CLASS));
        trafficTxnInfo.setTotalTxnAmt(trafficDtl.getDataRecode(TransferUtil.TOTAL_CASH_TRANSACTION_AMOUNT));
        trafficTxnInfo.setFreeCode(trafficDtl.getDataRecode(TransferUtil.FREECODE));
        trafficTxnInfo.setFreeBusRebate(trafficDtl.getDataRecode(TransferUtil.FREEBUSREBATE));
        trafficTxnInfo.setPriceMargin(trafficDtl.getDataRecode(TransferUtil.PRICE_MARGIN));
        trafficTxnInfo.setTotalTransactionFare(trafficDtl.getDataRecode(TransferUtil.TOTAL_TRANSACTION_FARE));
        trafficTxnInfo.setCardType(trafficDtl.getDataRecode(TransferUtil.CARD_TYPE));
        trafficTxnInfo.setPremiumProvider(trafficDtl.getDataRecode(TransferUtil.PREMIUM_PROVIDER));
        trafficTxnInfo.setUserTypeFavorAmt(trafficDtl.getDataRecode(TransferUtil.USER_TYPE_FAVOR_AMT));
        trafficTxnInfo.setPeakFavorAmt(trafficDtl.getDataRecode(TransferUtil.PEAK_FAVOR_AMT));
        trafficTxnInfo.setBusinessDate(trafficDtl.getDataRecode(TransferUtil.BUSINESS_DATE));
        trafficTxnInfo.setPenaltyAmt(trafficDtl.getDataRecode(TransferUtil.PENALTY_AMT));
        trafficTxnInfo.setCarrigeType(trafficDtl.getDataRecode(TransferUtil.CARRIGE_TYPE));
        trafficTxnInfo.setFavorFlag(trafficDtl.getDataRecode(TransferUtil.FAVOR_FLAG));
        trafficTxnInfo.setPreCheckinFlag(trafficDtl.getDataRecode(TransferUtil.PRE_CHECKIN_FLAG));
        trafficTxnInfo.setPreCheckinDeductFlag(trafficDtl.getDataRecode(TransferUtil.PRE_CHECKIN_DEDUCT_FLAG));
        trafficTxnInfo.setPreCheckinCntFlag(trafficDtl.getDataRecode(TransferUtil.PRE_CHECKIN_COUNT_FLAG));
        trafficTxnInfo.setSpclIdentOrigCnt(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_ORIGINAL_COUNTER));
        trafficTxnInfo.setSpclIdentUsageCnt(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_USAGE_COUNTER));
        trafficTxnInfo.setSpclIdentResetDate(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_RESET_DATE));
        trafficTxnInfo.setSpclIdentActiveDate(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_ACTIVATION_DATE));
        trafficTxnInfo.setSpclIdentArrivalStation(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_ARRIVAL_STATION));
        trafficTxnInfo.setSpclIdentExpDate(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_EXPIRATION_DATE));
        trafficTxnInfo.setSpclIdentDepartureStation(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_DEPARTURE_STATION));
        trafficTxnInfo.setSpclIdentRouteId(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_ROUTE_ID));
        trafficTxnInfo.setBonusUsageStatus(trafficDtl.getDataRecode(TransferUtil.ACCUMULATED_BONUS_USAGE_STATUS));
        trafficTxnInfo.setBonusValidDate(trafficDtl.getDataRecode(TransferUtil.ACCUMULATED_BONUS_VALID_DATE));
        trafficTxnInfo.setBonusSerno(trafficDtl.getDataRecode(TransferUtil.BONUS_PURSE_SEQUENCE_NUMBER));
        trafficTxnInfo.setBonusRemains(trafficDtl.getDataRecode(TransferUtil.BONUS_REMAINS));
        trafficTxnInfo.setBonusTxnAmt(trafficDtl.getDataRecode(TransferUtil.BONUS_TRANSACTION_AMOUNT));
        trafficTxnInfo.setSpecialFlag(trafficDtl.getDataRecode(TransferUtil.SPECIAL_FLAG));
        trafficTxnInfo.setTrafficVer(trafficDtl.getDataRecode(TransferUtil.TRAFFIC_VER));
        bctx.setTbTrafficTxnInfo(trafficTxnInfo);
    }
    
    public void trafficTxnInfoErrFiller(BATCHContext bctx)
    {
        TbTrafficTxnErrInfo trafficTxnInfo = new TbTrafficTxnErrInfo();
        TrafficTxnHeader trafficHd = bctx.getTrafficTxnHeader();
        TrafficTxnDetail trafficDtl = bctx.getTrafficTxnDetail();
        BerTLV tlv = bctx.getLMSMsg();
        String lmsInvoiceNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
        if(null != trafficHd)
        {
            trafficTxnInfo.setSourceDate(trafficHd.getSourceDate());
        }
        trafficTxnInfo.setItem(trafficDtl.getDataRecode(TransferUtil.ITEM_D));
        trafficTxnInfo.setCardNo(bctx.getLMSCardNbr());
        trafficTxnInfo.setLmsInvoiceNo(lmsInvoiceNo);
        trafficTxnInfo.setTxnType(trafficDtl.getDataRecode(TransferUtil.TXN_TYPE));
        trafficTxnInfo.setTransType(trafficDtl.getDataRecode(TransferUtil.TRANS_TYPE));
        trafficTxnInfo.setTransSysNo(trafficDtl.getDataRecode(TransferUtil.TRANS_SYS_NO));
        trafficTxnInfo.setLocId(bctx.getRealLocId());
        trafficTxnInfo.setDevId(trafficDtl.getDataRecode(TransferUtil.DEV_ID));
        trafficTxnInfo.setSamOsn(trafficDtl.getDataRecode(TransferUtil.SAM_OSN));
        trafficTxnInfo.setSamTransSeq(trafficDtl.getDataRecode(TransferUtil.SAM_TRANS_SEQ));
        trafficTxnInfo.setSamMac(trafficDtl.getDataRecode(TransferUtil.SAM_MAC));
        trafficTxnInfo.setTermTxDate(trafficDtl.getDataRecode(TransferUtil.TERM_TX_DATE));
        trafficTxnInfo.setTermTxSeq(trafficDtl.getDataRecode(TransferUtil.TERM_TX_SEQ));
        trafficTxnInfo.setTermId(trafficDtl.getDataRecode(TransferUtil.TERM_ID));
        trafficTxnInfo.setStoreId(trafficDtl.getDataRecode(TransferUtil.STORE_ID));
        trafficTxnInfo.setTransNoCancel(trafficDtl.getDataRecode(TransferUtil.TRANS_NO_CANCEL));
        trafficTxnInfo.setTransDateCancel(trafficDtl.getDataRecode(TransferUtil.TRANS_DATE_CANCEL));
        trafficTxnInfo.setTxprocessDate(trafficDtl.getDataRecode(TransferUtil.TXPROCESS_DATE));
        trafficTxnInfo.setShiftNumber(trafficDtl.getDataRecode(TransferUtil.SHIFT_NUMBER));
        trafficTxnInfo.setFirstDeductValueF(trafficDtl.getDataRecode(TransferUtil.FIRST_DEDUCT_VALUE_FOR_INTER_TRANSFER));
        trafficTxnInfo.setFirstEntryStationF(trafficDtl.getDataRecode(TransferUtil.FIRST_ENTRY_STATION_FOR_INTER_TRANSFER));
        trafficTxnInfo.setInOutCode(trafficDtl.getDataRecode(TransferUtil.IN_OUT_CODE));
        trafficTxnInfo.setTrtcCtsn(trafficDtl.getDataRecode(TransferUtil.TRTC_CTSN));
        trafficTxnInfo.setRejectCode(trafficDtl.getDataRecode(TransferUtil.REJECT_CODE));
        trafficTxnInfo.setConsumptionAccuPoints(trafficDtl.getDataRecode(TransferUtil.CONSUMPTION_ACCUMAULATED_POINTS));
        trafficTxnInfo.setUserType(trafficDtl.getDataRecode(TransferUtil.USER_TYPE));
        trafficTxnInfo.setTransferFavorAmt(trafficDtl.getDataRecode(TransferUtil.TRANSFER_FAVOR_AMT));
        trafficTxnInfo.setTotalTransactionFare(trafficDtl.getDataRecode(TransferUtil.TOTAL_TRANSACTION_FARE));
        trafficTxnInfo.setOtherFavorAmt(trafficDtl.getDataRecode(TransferUtil.OTHER_FAVOR_AMT));
        trafficTxnInfo.setBvBatchNo(trafficDtl.getDataRecode(TransferUtil.BV_TRANSACTION_BATHCH_NO));
        trafficTxnInfo.setInShuttleCode(trafficDtl.getDataRecode(TransferUtil.IN_SHUTTLE_CODE));
        trafficTxnInfo.setInStationCode(trafficDtl.getDataRecode(TransferUtil.IN_STATION_CODE));
        trafficTxnInfo.setBoardingStopCode(trafficDtl.getDataRecode(TransferUtil.BOARDING_STOP_CODE));
        trafficTxnInfo.setOutShuttleCode(trafficDtl.getDataRecode(TransferUtil.OUT_SHUTTLE_CODE));
        trafficTxnInfo.setOutStationCode(trafficDtl.getDataRecode(TransferUtil.OUT_STATION_CODE));
        trafficTxnInfo.setAlightingStopCode(trafficDtl.getDataRecode(TransferUtil.ALIGHTING_STOP_CODE));
        trafficTxnInfo.setTransferFlag(trafficDtl.getDataRecode(TransferUtil.TRANSFER_FLAG));
        trafficTxnInfo.setCashForInsufficiunt(trafficDtl.getDataRecode(TransferUtil.CASH_FOR_INSUFFICIUNT));
        trafficTxnInfo.setBusLincenseId(trafficDtl.getDataRecode(TransferUtil.BUS_LINCENSE_ID));
        trafficTxnInfo.setBusDriverId(trafficDtl.getDataRecode(TransferUtil.BUS_DRIVER_ID));
        trafficTxnInfo.setBusRouteDoman(trafficDtl.getDataRecode(TransferUtil.BUS_ROUTE_DOMAN));
        trafficTxnInfo.setCustDate(trafficDtl.getDataRecode(TransferUtil.CUST_DATE));
        trafficTxnInfo.setCustDateClass(trafficDtl.getDataRecode(TransferUtil.CUST_DATE_CLASS));
        trafficTxnInfo.setTotalTxnAmt(trafficDtl.getDataRecode(TransferUtil.TOTAL_CASH_TRANSACTION_AMOUNT));
        trafficTxnInfo.setFreeCode(trafficDtl.getDataRecode(TransferUtil.FREECODE));
        trafficTxnInfo.setFreeBusRebate(trafficDtl.getDataRecode(TransferUtil.FREEBUSREBATE));
        trafficTxnInfo.setPriceMargin(trafficDtl.getDataRecode(TransferUtil.PRICE_MARGIN));
        trafficTxnInfo.setTotalTransactionFare(trafficDtl.getDataRecode(TransferUtil.TOTAL_TRANSACTION_FARE));
        trafficTxnInfo.setCardType(trafficDtl.getDataRecode(TransferUtil.CARD_TYPE));
        trafficTxnInfo.setPremiumProvider(trafficDtl.getDataRecode(TransferUtil.PREMIUM_PROVIDER));
        trafficTxnInfo.setUserTypeFavorAmt(trafficDtl.getDataRecode(TransferUtil.USER_TYPE_FAVOR_AMT));
        trafficTxnInfo.setPeakFavorAmt(trafficDtl.getDataRecode(TransferUtil.PEAK_FAVOR_AMT));
        trafficTxnInfo.setBusinessDate(trafficDtl.getDataRecode(TransferUtil.BUSINESS_DATE));
        trafficTxnInfo.setPenaltyAmt(trafficDtl.getDataRecode(TransferUtil.PENALTY_AMT));
        trafficTxnInfo.setCarrigeType(trafficDtl.getDataRecode(TransferUtil.CARRIGE_TYPE));
        trafficTxnInfo.setFavorFlag(trafficDtl.getDataRecode(TransferUtil.FAVOR_FLAG));
        trafficTxnInfo.setPreCheckinFlag(trafficDtl.getDataRecode(TransferUtil.PRE_CHECKIN_FLAG));
        trafficTxnInfo.setPreCheckinDeductFlag(trafficDtl.getDataRecode(TransferUtil.PRE_CHECKIN_DEDUCT_FLAG));
        trafficTxnInfo.setPreCheckinCntFlag(trafficDtl.getDataRecode(TransferUtil.PRE_CHECKIN_COUNT_FLAG));
        trafficTxnInfo.setSpclIdentOrigCnt(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_ORIGINAL_COUNTER));
        trafficTxnInfo.setSpclIdentUsageCnt(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_USAGE_COUNTER));
        trafficTxnInfo.setSpclIdentResetDate(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_RESET_DATE));
        trafficTxnInfo.setSpclIdentActiveDate(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_ACTIVATION_DATE));
        trafficTxnInfo.setSpclIdentArrivalStation(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_ARRIVAL_STATION));
        trafficTxnInfo.setSpclIdentExpDate(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_EXPIRATION_DATE));
        trafficTxnInfo.setSpclIdentDepartureStation(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_DEPARTURE_STATION));
        trafficTxnInfo.setSpclIdentRouteId(trafficDtl.getDataRecode(TransferUtil.SPECIAL_IDENTITY_ROUTE_ID));
        trafficTxnInfo.setBonusUsageStatus(trafficDtl.getDataRecode(TransferUtil.ACCUMULATED_BONUS_USAGE_STATUS));
        trafficTxnInfo.setBonusValidDate(trafficDtl.getDataRecode(TransferUtil.ACCUMULATED_BONUS_VALID_DATE));
        trafficTxnInfo.setBonusSerno(trafficDtl.getDataRecode(TransferUtil.BONUS_PURSE_SEQUENCE_NUMBER));
        trafficTxnInfo.setBonusRemains(trafficDtl.getDataRecode(TransferUtil.BONUS_REMAINS));
        trafficTxnInfo.setBonusTxnAmt(trafficDtl.getDataRecode(TransferUtil.BONUS_TRANSACTION_AMOUNT));
        trafficTxnInfo.setSpecialFlag(trafficDtl.getDataRecode(TransferUtil.SPECIAL_FLAG));
        trafficTxnInfo.setTrafficVer(trafficDtl.getDataRecode(TransferUtil.TRAFFIC_VER));
        bctx.setTbTrafficTxnErrInfo(trafficTxnInfo);
    }
}