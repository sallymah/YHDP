package tw.com.hyweb.iff;

public class Constants {
	// R_CODE
    // --------------------------------------------------------------
    // ok rcode
    public static final String RCODE_0000_OK = "0000";

    //程式處理完畢, 但是過程中曾有幾筆資料有錯(remarkFail), 必須 set TB_BATCH_RESULT.rcode=2001 代表輕微錯誤, 
    //上班時間再人工處理就好.
    public static final String RCODE_2001_WARN = "2001";

    public static final String RCODE_2999_FATAL = "2999";
    
    // PreOperation rcode
    
    public static final String RCODE_2030_SimulateBalTransfer_ERR = "2030";
    
    public static final String RCODE_2031_SimulateBalTransfer_BalanceId_ERR = "2031";
    
    public static final String RCODE_2021_SimulateAppointReloadBalance_ERR = "2021";

    public static final String RCODE_2022_SimulateAppointReloadDownload_ERR = "2022";
 
    public static final String RCODE_2010_ExtendBonus_ERR = "2010";
    
    public static final String RCODE_2011_ExtendBonus_ReExtend_ERR = "2011";
    
    public static final String RCODE_2040_ErrorOnlTxn2Cap_ERR = "2040";
    
    public static final String RCODE_20S0_SimulateCardReturn_ERR = "20S0";
    
    // DayCut rcode
    public static final String RCODE_2101_NoDayCut_ERR = "2101";

    public static final String RCODE_2100_DayCut_ERR = "2100";

    // check error rcode
    public static final String RCODE_2191_CARDNO_ERR = "2191";

    public static final String RCODE_2192_CARDSTATUS_ERR = "2192";

    public static final String RCODE_2197_CARDSTATUST_ERR = "2197";
    
    public static final String RCODE_2193_MERCH_ERR = "2193";

    public static final String RCODE_2194_TERM_ERR = "2194";

    public static final String RCODE_2195_ORIGTXN_ERR = "2195";

    public static final String RCODE_2196_BLACKLIST_ERR = "2196";

    // balance rcode
    public static final String RCODE_2200_BAL_ERR = "2200";

    public static final String RCODE_2201_BAL_NOBAL = "2201";

    // 7.1 UpdateErrorData error
    public static final String RCODE_2301_NOSUM_ERR = "2301";

    // 7.2 SumMerch error
    public static final String RCODE_2302_SUMMERCH_ERR = "2302";

    // 7.3 SumStoreCounter error
    public static final String RCODE_2303_SUMSTORECOUNTER_ERR = "2303";

    // 7.4 SumCardProduct error
    public static final String RCODE_2304_SUMCARDPRODUCT_ERR = "2304";
    
    // 7.5 SumBonus error                                            
    public static final String RCODE_2305_SUMBONUS_ERR = "2305"; 
    
    // SumPbnld error                                            
    public static final String RCODE_2306_SUMPBNLD_ERR = "2306"; 
    
    // 8 ProcSettle rcode
    public static final String RECOVER_LEVEL_ALL = "ALL"; //復原全部資料
    
    public static final String RECOVER_LEVEL_ERR = "ERR"; //復原錯誤資料
    
    public static final String RCODE_2400_SETTLE_ERR = "2400"; //交易明細清算失敗(tb_trans)
    
    public static final String RCODE_2401_INVALID_TXN = "2401"; //合法性檢查失敗不清算
    
    public static final String RCODE_2402_NO_SPONSOR = "2402"; //查無出資單位
    
    public static final String RCODE_2403_NO_SETTLE_RATE = "2403"; //查無費率設定

    public static final String RCODE_2404_NO_CR_DB = "2404"; //查無入扣帳帳號
    
    public static final String RCODE_2405_NO_MATCH_TXN_DTL = "2405"; //trans_dtl的交易 在trans沒有對應的資料

    public static final String RCODE_2406_ADJ_QTY_AMT_ERR = "2406"; //調帳時trans_dtl同時設點數和金額
    
    public static final String RCODE_2407_SETTLE_FROM_ERR = "2407"; //SETTLE_FROM 設為錯誤的值

    public static final String RCODE_2410_CAMPAIGN_FAIL = "2410"; //call campaign失敗(return null)

    public static final String RCODE_2411_CAMPAIGN_ERROR = "2411"; //campaign設定錯誤(出資比例總合不是100%)

    public static final String RCODE_2412_MULTI_CAMPAIGN_OF_REDEEM = "2412"; //settle_from="TXN_REDEEM_AMT"(REDEEM交易), 只能設一筆campaign活動

    public static final String RCODE_2420_INSERT_SETTLE_RESULT_ERR = "2420"; //新增處理結果至TB_SETTLE_RESULT出錯
    
    // 9 ProcFee rcode
    public static final String RCODE_2500_FEE_ERR = "2500";
    
    // DB sql command error
    public static final String RCODE_1001_SQL_ERR = "1001";
    
    
    
    // 11.x ImportXXX
    public static final String RCODE_2701_NOTEXIST = "2701";
    public static final String RCODE_2702_DIVIDE_ERR = "2702";
    public static final String RCODE_2703_NOHEADER = "2703";
    public static final String RCODE_2704_REPHEADER = "2704";
    public static final String RCODE_2705_NODATA = "2705";
    public static final String RCODE_2706_DATANUM_ERR = "2706";
    public static final String RCODE_2707_DATADATE_ERR = "2707";
    public static final String RCODE_2708_MANDATORY_ERR = "2708";
    public static final String RCODE_2709_FORMAT_ERR = "2709";
    public static final String RCODE_2710_INVALID_ERR = "2710";
    public static final String RCODE_2711_REPDATA_ERR = "2711";
    public static final String RCODE_2712_SETDATA_ERR = "2712";
    public static final String RCODE_2713_SETTING1_ERR = "2713";
    public static final String RCODE_2714_SETTING2_ERR = "2714";
    public static final String RCODE_2715_DECRYPT_ERR = "2715";
    public static final String RCODE_2716_DATACOUNT_ERR = "2716";
    public static final String RCODE_2791_GENERALDB_ERR = "2791";

    // ImpAppointList
    public static final String RCODE_2721_APPOINT_NOTFOUND_ERR = "2721";

    // ProcAppointList
    public static final String RCODE_2724_COMPUTEPOINTDATES_ERR = "2724";
    public static final String RCODE_2725_INSERTPBNLD_ERR = "2725";
    public static final String RCODE_2727_CALLCAMPAIGN_ERR = "2727";
    public static final String RCODE_2731_CANCELAPI_ERR = "2731";
}
