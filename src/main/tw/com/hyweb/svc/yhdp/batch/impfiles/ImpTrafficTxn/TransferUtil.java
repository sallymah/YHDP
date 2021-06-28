package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn;

public class TransferUtil
{
    public static final int ITEM_D = 0;
    public static final int TXN_TYPE = 1;
    public static final int MIFARE_ID = 2;
    public static final int PID = 3;
    public static final int TRANS_NO = 4;
    public static final int TRANS_DATE = 5;
    public static final int TRANS_TYPE = 6;
    public static final int BEFORE_BAL = 7;
    public static final int TOPUP_AMT = 8;
    public static final int AFTER_BAL = 9;
    public static final int TRANS_SYS_NO = 10;
    public static final int LOC_ID = 11;
    public static final int DEV_ID = 12;
    public static final int SAM_OSN = 13;
    public static final int SAM_TRANS_SEQ = 14;
    public static final int SAM_MAC = 15;
    public static final int TERM_TX_SEQ = 16;
    public static final int TERM_TX_DATE = 17;
    public static final int TERM_ID = 18;
    public static final int STORE_ID = 19;
    public static final int TRANS_NO_CANCEL = 20;
    public static final int TRANS_DATE_CANCEL = 21;
    public static final int TXPROCESS_DATE = 22;
    public static final int SHIFT_NUMBER = 23;
    public static final int FIRST_ENTRY_STATION_FOR_INTER_TRANSFER = 24;
    public static final int FIRST_DEDUCT_VALUE_FOR_INTER_TRANSFER = 25;
    public static final int IN_OUT_CODE = 26;
    public static final int TRTC_CTSN = 27;
    public static final int REJECT_CODE = 28;
    public static final int CONSUMPTION_ACCUMAULATED_POINTS = 29;
    public static final int USER_TYPE = 30;
    public static final int TRANSFER_FAVOR_AMT = 31;
    public static final int ORIG_TRANSACTION_FARE = 32;
    public static final int OTHER_FAVOR_AMT = 33;
    public static final int BV_TRANSACTION_BATHCH_NO = 34;
    public static final int IN_SHUTTLE_CODE = 35;
    public static final int IN_STATION_CODE = 36;
    public static final int BOARDING_STOP_CODE = 37;
    public static final int OUT_SHUTTLE_CODE = 38;
    public static final int OUT_STATION_CODE = 39;
    public static final int ALIGHTING_STOP_CODE = 40;
    public static final int TRANSFER_FLAG = 41;
    public static final int CASH_FOR_INSUFFICIUNT = 42;
    public static final int BUS_LINCENSE_ID = 43;
    public static final int BUS_DRIVER_ID = 44;
    public static final int BUS_ROUTE_DOMAN = 45;
    public static final int CUST_DATE = 46;
    public static final int CUST_DATE_CLASS = 47;
    public static final int TOTAL_CASH_TRANSACTION_AMOUNT = 48;
    public static final int FREECODE = 49;
    public static final int FREEBUSREBATE = 50;
    public static final int PRICE_MARGIN = 51;
    public static final int TOTAL_TRANSACTION_FARE = 52;
    public static final int CARD_TYPE = 53;
    public static final int PREMIUM_PROVIDER = 54;
    public static final int USER_TYPE_FAVOR_AMT = 55;
    public static final int PEAK_FAVOR_AMT = 56;
    public static final int BUSINESS_DATE = 57;
    public static final int PENALTY_AMT = 58;
    public static final int CARRIGE_TYPE = 59;
    public static final int FAVOR_FLAG = 60;
    public static final int PRE_CHECKIN_FLAG = 61;
    public static final int PRE_CHECKIN_DEDUCT_FLAG = 62;
    public static final int PRE_CHECKIN_COUNT_FLAG = 63;
    public static final int SPECIAL_IDENTITY_ORIGINAL_COUNTER = 64;
    public static final int SPECIAL_IDENTITY_USAGE_COUNTER = 65;
    public static final int SPECIAL_IDENTITY_RESET_DATE = 66;
    public static final int SPECIAL_IDENTITY_ACTIVATION_DATE = 67;
    public static final int SPECIAL_IDENTITY_EXPIRATION_DATE = 68;
    public static final int SPECIAL_IDENTITY_DEPARTURE_STATION = 69;
    public static final int SPECIAL_IDENTITY_ARRIVAL_STATION = 70;
    public static final int SPECIAL_IDENTITY_ROUTE_ID = 71;
    public static final int ACCUMULATED_BONUS_USAGE_STATUS = 72;
    public static final int ACCUMULATED_BONUS_VALID_DATE = 73;
    public static final int BONUS_PURSE_SEQUENCE_NUMBER = 74;
    public static final int BONUS_TRANSACTION_AMOUNT = 75;
    public static final int BONUS_REMAINS = 76;
    public static final int SPECIAL_FLAG = 77;
    public static final int TRAFFIC_VER = 78;
    public static final int SETTLE_DATE = 79;
    public static final int LOC_ID_AP = 80;
    public static final int RETENTION = 81;
    public static final int RESP_CODE = 82;
    
    public static final String TRANS_SYS_NO_TRTC = "02";//北捷
    public static final String TRANS_SYS_NO_KRTC = "0B";//高捷
    public static final String TRANS_SYS_NO_TRA  = "06";//台鐵
    public static final String TRANS_SYS_NO_PARK = "00";//停車場
    
    /*
     * 區分mac與資料parser版本
     */
    public static final int TRAFIC_VER_BUS  = 1;
    public static final int TRAFIC_VER_KRTC = 2;
    
    /* 03 */
    public static final int TRAFIC_VER_TRA  = 3;//最終版號的版本
    
    /* 04 */
    public static final int TRAFIC_VER_BUS_FINAL  = 4;
    
    /* 05 */
    public static final int TRAFIC_VER_TYME  = 5; //桃捷
}
