/**
 * changelog
 * --------------------
 * 20071121
 * duncan,anny,judy,tracy
 * 改變 TB_PERSO_DTL.CARD_STATUS 定義值(2:未開卡, 3:已開卡)
 * 改變 TB_CARD.STATUS 定義值 (2:未開卡)
 * --------------------
 */
/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.core.yhdp.common.misc;

/**
 * <pre>
 * Constants
 * </pre>
 * 
 * author: duncan
 */
public class Constants
{
    // data source name for batch
    public static final String DSNAME_AP = "ap";
    public static final String DSNAME_OWNER = "owner";
    public static final String DSNAME_ONLINES = "onlines";
    public static final String DSNAME_BATCH = "batch";
    public static final String DSNAME_UI = "ui";
    public static final String DSNAME_REPORT = "report";
    public static final String DSNAME_KEYY = "keyy";
    public static final String DSNAME_TRUNC = "trunc";
    public static final String DSNAME_CAMPAIGN = "campaign";
    public static final String DSNAME_COMMON = "common";

    // BONUSID1
    public static final String BONUSID1_CHIPPOINT1 = "1";
    public static final String BONUSID1_CHIPPOINT2 = "2";
    public static final String BONUSID1_HOSTPOINT = "3";
    public static final String BONUSID1_CHIPCOUPON = "6";
    public static final String BONUSID1_HOSTCOUPON = "7";
    public static final String BONUSID1_PAPERCOUPON = "8";

    // TXN_SRC
    // --------------------------------------------------------------
    // EDC transaction (EDC settlement with LMS)
    public static final String TXNSRC_E = "E";

    // Captured transaction
    public static final String TXNSRC_C = "C";

    // Adjust transaction
    public static final String TXNSRC_A = "A";

    // gray
    // EDC transaction from Base24 (EDC no settlement with LMS)
    public static final String TXNSRC_F = "F";

    // Incoming from batch file
    public static final String TXNSRC_B = "B";

    // Manual set some transaction to reload/adjust for Host Bonus
    public static final String TXNSRC_H = "H";

    // Internet transaction
    public static final String TXNSRC_I = "I";

    // ?
    // CSR transaction
    public static final String TXNSRC_CSR = "R";

    // IVR transaction
    public static final String TXNSRC_V = "V";

    // ATM transaction
    public static final String TXNSRC_T = "T";

    // Coupon Extension(for Host & Chip Base coupon extension)
    public static final String TXNSRC_X = "X";

    // ImpAppointList or ProcAppointList
    public static final String TXNSRC_L = "L";

    // Clean Bonus
    public static final String TXNSRC_N = "N";
    // --------------------------------------------------------------

/*
LMS Processing code命名規則：
LMS Processing code為4個digit(ABCD)，命名規則分類如下：

A = {處理類別(7)}
B = {開卡(1)，抵扣(兌換) (2)，管理(3)，其他(5)，銷售(6)，加值(7)，忠誠活動(8)}
C = {INDEX}
D = {其他(5)，退貨(6)，一般(7)，取消(8)，退貨取消(9)}


帳務代碼命名規則：
帳務代碼為4個digit(ABCD)，命名規則分類如下：

A = {帳務類別(8)}
B = {抵扣(兌換) (2)，其他(5)，加值(7)，回饋(8)}
C = {INDEX}
D = {其他(5)，退貨(6)，一般(7)，取消(8)，退貨取消(9)}

功能列表

一般交易：
交易處理類別        LMS PCode   交易帳務類別        交易帳務代碼    功能描述
一般開卡            7107        紅利回饋            8807            回饋點數或coupon
現金開卡            7117        紅利回饋            8807            回饋點數或coupon
                                加值                8707            加值點數
信用開卡            7127        紅利回饋            8807            回饋點數或coupon
                                加值                8707            加值點數
聯名開卡            7137        紅利回饋            8807            回饋點數或coupon
                                加值                8707            加值點數
現金加值            7707        紅利回饋            8807            回饋點數或coupon
                                加值                8707            加值點數
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
信用加值            7717        紅利回饋            8807            回饋點數或coupon
                                加值                8707            加值點數
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
聯名加值            7727        紅利回饋            8807            回饋點數或coupon
                                加值                8707            加值點數
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
一般銷售            7607        紅利回饋            8807            回饋點數或coupon
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
紅利銷售            7617        紅利回饋            8807            回饋點數或coupon
                                紅利抵扣            8207            用紅利來抵扣或折抵交易金額
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
分期銷售            7627        紅利回饋            8807            回饋點數或coupon
                                紅利抵扣            8207            用紅利來抵扣或折抵交易金額
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
紅利兌換銷售        7637        紅利回饋            8807            回饋點數或coupon
                                紅利抵扣            8207            用紅利來抵扣或折抵交易金額
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
現金點券銷售        7657        紅利回饋            8807            回饋點數或coupon
                                紅利抵扣            8207            用紅利來抵扣或折抵交易金額
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
折價點券銷售        7667        紅利回饋            8807            回饋點數或coupon
                                紅利抵扣            8207            用紅利來抵扣或折抵交易金額
                                補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
點數抵扣            7227        紅利抵扣            8207            用紅利來抵扣或折抵交易金額
Coupon使用          7237        紅利抵扣            8207            用紅利來抵扣或折抵交易金額
禮物兌換            7257        紅利抵扣            8207            用紅利來抵扣或折抵交易金額
指定加值            7307        指定加值            8817            指定某個點數或coupon來加值
餘額轉置            7317        餘額轉置            8527            將舊卡的紅利餘額轉置到新卡
來店禮              7807        紅利回饋            8807            回饋點數或coupon
補登                7507        補登(+)             8507            由主機補登正的點數或coupon到晶片卡上。
                                補登(-)             8517            由主機補登負的點數或coupon到晶片卡上。
鎖卡                7517        N/A                 N/A
退卡                7527        退卡                8537            持卡人退還卡片給發卡行
掛卡                7537        N/A                 N/A
查詢卡片狀態        7557        N/A                 N/A
查詢主機紅利餘額    7567        查詢主機紅利餘額    8557
查詢交易可折抵金額  7587        查詢交易可折抵金額  8567
查詢全部可折抵金額  7597        N/A                 N/A
批次上傳            N/A         N/A                 N/A
結帳                7327        N/A                 N/A
參數下載            7337        N/A                 N/A
黑名單下載          7357        N/A                 N/A
開機登入            7367        N/A                 N/A
指定名單            7817        紅利回饋            8807             回饋點數或coupon
UI調整交易          7907        須列帳UI調整交易(+)	8907	           須列帳UI調整交易(+)
                                須列帳UI調整交易(-) 8917             須列帳UI調整交易(-)
                                不列帳UI調整交易(+) 8927             不列帳UI調整交易(+)
                                不列帳UI調整交易(-) 8937             不列帳UI調整交易(-)
核卡禮              7827        紅利回饋            8807             回饋點數或coupon
開卡禮              7837        紅利回饋            8807             回饋點數或coupon
延展效期            7917        延展效期            8957             Coupon延展效期



取消交易：
交易類別            LMS PCode   交易名稱            交易名稱代碼     功能描述
現金加值取消        7708        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
                                加值取消            8708             該交易取消後，沖正該筆交易所加值之點數
信用加值取消        7718        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
                                加值取消            8708             該交易取消後，沖正該筆交易所加值之點數
聯名加值取消        7728        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
                                加值取消            8708             該交易取消後，沖正該筆交易所加值之點數
一般銷售取消        7608        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
紅利銷售取消        7618        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
                                紅利抵扣取消        8208             該交易取消後，沖正該筆交易所抵扣或折抵的紅利
分期銷售取消        7628        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
                                紅利抵扣取消        8208             該交易取消後，沖正該筆交易所抵扣或折抵的紅利
紅利兌換銷售取消    7638        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
                                紅利抵扣取消        8208             該交易取消後，沖正該筆交易所抵扣或折抵的紅利
現金點券銷售取消    7658        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
                                紅利抵扣取消        8208             該交易取消後，沖正該筆交易所抵扣或折抵的紅利
折價點券銷售取消    7668        紅利回饋取消        8808             該交易取消後，沖正該筆交易所得的紅利回饋
                                紅利抵扣取消        8208             該交易取消後，沖正該筆交易所抵扣或折抵的紅利
點數抵扣取消        7208        紅利抵扣取消        8208             該交易取消後，沖正該筆交易所抵扣或折抵的紅利
Coupon使用取消      7218        紅利抵扣取消        8208             該交易取消後，沖正該筆交易所抵扣或折抵的紅利
禮物兌換取消        7238        紅利抵扣取消        8208             該交易取消後，沖正該筆交易所抵扣或折抵的紅利



退貨交易：
交易類別            LMSPCode    交易名稱            交易名稱代碼     功能描述
一般銷售退貨        7606        紅利回饋退貨        8806             該交易退貨後，沖正該筆交易所得的紅利回饋
紅利銷售退貨        7616        紅利回饋退貨        8806             該交易退貨後，沖正該筆交易所得的紅利回饋
                                紅利抵扣退貨        8206             該交易退貨後，沖正該筆交易所抵扣或折抵的紅利
紅利兌換銷售退貨    7636        紅利回饋退貨        8806             該交易退貨後，沖正該筆交易所得的紅利回饋
                                紅利抵扣退貨        8206             該交易退貨後，沖正該筆交易所抵扣或折抵的紅利
現金點券銷售退貨    7656        紅利回饋退貨        8806             該交易退貨後，沖正該筆交易所得的紅利回饋
                                紅利抵扣退貨        8206             該交易退貨後，沖正該筆交易所抵扣或折抵的紅利
折價點券銷售退貨    7666        紅利回饋退貨        8806             該交易退貨後，沖正該筆交易所得的紅利回饋
                                紅利抵扣退貨        8206             該交易退貨後，沖正該筆交易所抵扣或折抵的紅利
點數抵扣退貨        7206        紅利抵扣退貨        8206             該交易退貨後，沖正該筆交易所抵扣或折抵的紅利
Coupon使用退貨      7216        紅利抵扣退貨        8206             該交易退貨後，沖正該筆交易所抵扣或折抵的紅利
禮物兌換退貨        7236        紅利抵扣退貨        8206             該交易退貨後，沖正該筆交易所抵扣或折抵的紅利



退貨取消交易：
交易類別            LMS PCode   交易名稱            交易名稱代碼     功能描述
一般銷售退貨取消    7609        紅利回饋退貨        8809             該交易退貨取消後，沖正該筆交易所得的紅利回饋
 */
    // P_CODE
    // --------------------------------------------------------------
    // 一般交易的 P_CODE
    public static final String PCODE_7107 = "7107";

    public static final String PCODE_7117 = "7117";

    public static final String PCODE_7127 = "7127";

    public static final String PCODE_7137 = "7137";

    public static final String PCODE_7707 = "7707";

    public static final String PCODE_7717 = "7717";

    public static final String PCODE_7727 = "7727";

    public static final String PCODE_7607 = "7607";

    public static final String PCODE_7617 = "7617";

    public static final String PCODE_7627 = "7627";

    public static final String PCODE_7637 = "7637";

    public static final String PCODE_7657 = "7657";

    public static final String PCODE_7667 = "7667";

    public static final String PCODE_7227 = "7227";

    public static final String PCODE_7237 = "7237";

    public static final String PCODE_7257 = "7257";

    public static final String PCODE_7307 = "7307";

    public static final String PCODE_7317 = "7317";

    public static final String PCODE_7807 = "7807";

    public static final String PCODE_7507 = "7507";

    public static final String PCODE_7517 = "7517";

    public static final String PCODE_7527 = "7527";

    public static final String PCODE_7537 = "7537";

    public static final String PCODE_7557 = "7557";

    public static final String PCODE_7567 = "7567";

    public static final String PCODE_7587 = "7587";

    public static final String PCODE_7597 = "7597";

    public static final String PCODE_7327 = "7327";

    public static final String PCODE_7337 = "7337";

    public static final String PCODE_7357 = "7357";

    public static final String PCODE_7367 = "7367";

    public static final String PCODE_7817 = "7817";

    public static final String PCODE_7818 = "7818";

    public static final String PCODE_7907 = "7907";

    public static final String PCODE_7827 = "7827";

    public static final String PCODE_7837 = "7837";

    public static final String PCODE_7917 = "7917";

    // 取消交易的 P_CODE
    public static final String PCODE_7708 = "7708";

    public static final String PCODE_7718 = "7718";

    public static final String PCODE_7728 = "7728";

    public static final String PCODE_7608 = "7608";

    public static final String PCODE_7618 = "7618";

    public static final String PCODE_7628 = "7628";

    public static final String PCODE_7638 = "7638";

    public static final String PCODE_7658 = "7658";

    public static final String PCODE_7668 = "7668";

    public static final String PCODE_7208 = "7208";

    public static final String PCODE_7218 = "7218";

    public static final String PCODE_7238 = "7238";

    // 退貨交易的 P_CODE
    public static final String PCODE_7606 = "7606";

    public static final String PCODE_7616 = "7616";

    public static final String PCODE_7636 = "7636";

    public static final String PCODE_7656 = "7656";

    public static final String PCODE_7666 = "7666";

    public static final String PCODE_7206 = "7206";

    public static final String PCODE_7216 = "7216";

    public static final String PCODE_7236 = "7236";

    // 退貨取消交易的 P_CODE
    public static final String PCODE_7609 = "7609";

    // --------------------------------------------------------------

    // TXN_CODE
    // --------------------------------------------------------------
    // 一般交易的 TXN_CODE
    public static final String TXNCODE_8807 = "8807";

    public static final String TXNCODE_8707 = "8707";

    public static final String TXNCODE_8507 = "8507";

    public static final String TXNCODE_8517 = "8517";

    public static final String TXNCODE_8207 = "8207";

    public static final String TXNCODE_8817 = "8817";

    public static final String TXNCODE_8527 = "8527";

    public static final String TXNCODE_8537 = "8537";

    public static final String TXNCODE_8557 = "8557";

    public static final String TXNCODE_8567 = "8567";

    public static final String TXNCODE_8907 = "8907";

    public static final String TXNCODE_8917 = "8917";

    public static final String TXNCODE_8927 = "8927";

    public static final String TXNCODE_8937 = "8937";

    public static final String TXNCODE_8957 = "8957";
    
    // 取消交易的 TXN_CODE
    public static final String TXNCODE_8808 = "8808";

    public static final String TXNCODE_8708 = "8708";

    public static final String TXNCODE_8208 = "8208";
    
    // 退貨交易的 TXN_CODE
    public static final String TXNCODE_8806 = "8806";
    
    public static final String TXNCODE_8206 = "8206";

    // 退貨取消的 TXN_CODE
    public static final String TXNCODE_8809 = "8809";

    // --------------------------------------------------------------

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
    // --------------------------------------------------------------

    // TB_MERCH
    // TB_MERCH.STATUS
    // --------------------------------------------------------------
    public static final String MERCH_EFFECTIVE = "1";

    public static final String MERCH_TERMINATION = "0";

    // --------------------------------------------------------------

    // TB_TERM
    // --------------------------------------------------------------
    // TB_TERM.STATUS
    public static final String TERM_EFFECTIVE = "1";

    public static final String TERM_TERMINATION = "0";

    // --------------------------------------------------------------

    // TB_CARD
    // --------------------------------------------------------------
    // TB_CARD.PRIMARY_CARD
    public static final String PRIMARY_CARD = "1";

    public static final String SECONDARY_CARD = "0";

    // TB_CARD.CARD_LEVEL
    public static final String PLATINUM_CARD = "P";

    public static final String GOLDEN_CARD = "G";

    public static final String REGULAR_CARD = "R";

    // TB_CARD.CARD_PLAN
    public static final String CARDPLAN_VISA = "V";

    public static final String CARDPLAN_MASTER = "M";

    public static final String CARDPLAN_JCB = "J";

    // TB_CARD.STATUS
    public static final String CARDSTATUS_PERSO = "1";

    public static final String CARDSTATUS_NOACTIVATED = "2";

    public static final String CARDSTATUS_ACTIVATED = "3";

    public static final String CARDSTATUS_FAILCARD = "9";

    public static final String CARDSTATUS_BALTRANSFER = "T";
    

    // TB_CARD.BAL_TRANSFER_FLAG
    public static final String BALTRANSFERFLAG_NOTSET = "0";
    public static final String BALTRANSFERFLAG_SET = "1";
    // --------------------------------------------------------------

    // TB_TERM_BATCH
    // --------------------------------------------------------------
    // TB_TERM_BATCH.TERM_SETTLE_FLAG
    public static final String TERMSETTLEFLAG_BALANCED="1";  //平帳
    public static final String TERMSETTLEFLAG_UNBALANCED="9";  //不平帳
    // TB_TERM_BATCH.STATUS
    public static final String TBSTATUS_VALID = "1";
    public static final String TBSTATUS_INVALID = "9";
    // --------------------------------------------------------------

    // TB_TRANS
    // --------------------------------------------------------------
    // TB_TRANS.CHECK_FLAG
    public static final String CHECKFLAG_SUCCESS = "0";

    public static final String CHECKFLAG_FAIL = "1";

    // TB_TRANS.TXN_ACCESS_MODE
    public static final String TXNACCESSMODE_CONTACT = "C";

    public static final String TXNACCESSMODE_CONTACTLESS = "L";

    // TB_TRANS.CURRENCY_CODE
    public static final String CURRENCYCODE_CHN = "156";

    public static final String CURRENCYCODE_HKG = "344";

    public static final String CURRENCYCODE_MAC = "446";

    public static final String CURRENCYCODE_SGP = "702";

    public static final String CURRENCYCODE_THA = "764";

    public static final String CURRENCYCODE_USD = "840";

    public static final String CURRENCYCODE_TWN = "901";

    // TB_TRANS.STATUS
    public static final String STATUS_INITIAL = "0";

    public static final String STATUS_SUCCESS = "1";

    public static final String STATUS_REVERSAL = "9";

    public static final String STATUS_CANCEL = "C";

    public static final String STATUS_REFUND = "R";

    public static final String STATUS_FAIL = "F";

    // TB_TRANS.ONLINE_FLAG
    public static final String ONLINEFLAG_ONLINE = "N";

    public static final String ONLINEFLAG_OFFLINE = "F";

    public static final String ONLINEFLAG_OTHERS = "O";
    // --------------------------------------------------------------

    // TB_TRANS_DTL
    // --------------------------------------------------------------
    // TB_TRANS_DTL.BONUSBASE
    public static final String BONUSBASE_HOST = "H";

    public static final String BONUSBASE_CHIP = "C";
    public static final String BONUSBASE_UNKNOWN = "N";

    // TB_TRANS_DTL.BALANCE_TYPE
    public static final String BALANCETYPE_CARD = "C";

    public static final String BALANCETYPE_ACCT = "A";

    public static final String BALANCETYPE_CUST = "U";

    // --------------------------------------------------------------

    // TB_TRANS_DTL
    // --------------------------------------------------------------
    // TB_TRANS_DTL.BALANCE_FLAG
    public static final String BALANCEFLAG_NOCOMP = "0";

    public static final String BALANCEFLAG_COMP = "1";

    // TB_TRANS_DTL.SUMMARY_FLAG
    public static final String SUMMARYFLAG_NOCOMP = "0";

    public static final String SUMMARYFLAG_COMP = "1";

    // TB_TRANS_DTL.SETTLEMENT_FLAG
    public static final String SETTLEMENTFLAG_NOCOMP = "0";

    public static final String SETTLEMENTFLAG_COMP = "1";
    // --------------------------------------------------------------

    // TB_FILE_INFO
    // --------------------------------------------------------------
    // TB_FILE_INFO.IN_OUT
    public static final String INOUT_IN = "I";
    public static final String INOUT_OUT = "O";
    // TB_FILE_INFO.OK_FLAG
    public static final String OKFLAG_NOCHECK = "0";
    public static final String OKFLAG_CHECK = "1";
    // --------------------------------------------------------------

    // TB_INCTL
    // --------------------------------------------------------------
    // TB_INCTL.WORK_FLAG
    public static final String WORKFLAG_INWORK = "1";
    public static final String WORKFLAG_PROCESSING = "2";
    public static final String WORKFLAG_PROCESSOK = "3";
    public static final String WORKFLAG_DELETED = "6";
    public static final String WORKFLAG_PROCESSFAIL = "9";
    // --------------------------------------------------------------    

    // TB_SETTLE_RATE
    // --------------------------------------------------------------
    // TB_SETTLE_RATE.SETTLE_FROM
    public static final String SETTLE_FROM_BONUS_QTY = "BONUS_QTY";
    public static final String SETTLE_FROM_TXN_REDEEM_AMT = "TXN_REDEEM_AMT";
    // TB_SETTLE_RATE.CARRY_TYPE
    public static final String CARRY_TYPE_ROUND_UP= "U";
    public static final String CARRY_TYPE_ROUND_HALF_UP= "O";
    public static final String CARRY_TYPE_ROUND_DOWN= "D";
    // --------------------------------------------------------------
    

    // TB_SETTLE_RESULT
    // --------------------------------------------------------------
    // TB_SETTLE_RESULT.PROG_ID
    public static final String PROG_ID_NO_CAMPAIGN = "000000000000";

    // --------------------------------------------------------------

    //TB_FEE_CONFIG
    // --------------------------------------------------------------
    //TB_FEE_CONFIG.ALLOW_DEBUCT
    public static final String ALLOW_DEBUCT_DISALLOW = "0"; //不可抵扣
    public static final String ALLOW_DEBUCT_ALLOW = "1"; //可以抵扣
    
    //TB_FEE_CONFIG.CAL_BASE
    public static final String CAL_BASE_NUMBER = "N"; //以交易筆數計算手續費
    public static final String CAL_BASE_AMOUNT = "A"; //以交易金額計算手續費
    
    
    //TB_FEE_DEF
    // --------------------------------------------------------------
    //TB_FEE_DEF.SIGN
    //參考TB_FEE_DEF.SIGN

    //TB_FEE_CAL
    // --------------------------------------------------------------
    //TB_FEE_CAL.CAL_FORMULA
    public static final String CAL_FORMULA_FLAT = "F"; //線性計算
    public static final String CAL_FORMULA_PROG = "P"; //累進計算
    
    
    // --------------------------------------------------------------

    // TB_BAL_TRANSFER
    // --------------------------------------------------------------
    // TB_BAL_TRANSFER.BT_SRC
    public static final String BTSRC_UI = "U";
    public static final String BTSRC_BATCH = "B";
    public static final String BTSRC_ONLINE = "O";
    // TB_BAL_TRANSFER.STATUS
    public static final String BTSTATUS_INITIAL = "0";
    public static final String BTSTATUS_DOWNLOADED = "1";
    public static final String BTSTATUS_INVALID = "9";
    // --------------------------------------------------------------

    // TB_AWARD_PROG
    // --------------------------------------------------------------
    // TB_AWARD_PROG.ONUS_FLAG
    public static final String ONUSFLAG_ONUS = "N";
    public static final String ONUSFLAG_OFFUS = "F";
    // --------------------------------------------------------------

    // TB_BAL_UPDATE_DTL
    // --------------------------------------------------------------
    // TB_BAL_UPDATE_DTL.STATUS
    public static final String BUDSTATUS_NOT_DOWNLOAD = "0";
    public static final String BUDSTATUS_DOWNLOADED = "1";
    public static final String BUDSTATUS_NODOWNLOAD_REVERSAL = "9";
    public static final String BUDSTATUS_NODOWNLOAD_FAIL = "F";
    // TB_BAL_UPDATE_DTL.SIGN & TB_FEE_DEF.SIGN
    public static final String SIGN_PLUS = "P";
    public static final String SIGN_MINUS = "M";
    // --------------------------------------------------------------

    // TB_APPOINT_RELOAD
    // --------------------------------------------------------------
    // TB_APPOINT_RELOAD.AR_SRC
    public static final String ARSRC_UI = "U";
    public static final String ARSRC_BATCH = "B";
    public static final String ARSRC_ONLINE = "O";
    // TB_APPOINT_RELOAD.STATUS
    public static final String ARSTATUS_INITIAL = "0";
    public static final String ARSTATUS_DOWNLOADED = "1";
    public static final String ARSTATUS_INVALID = "9";
    public static final String ARSTATUS_INVALID_T = "T";
    // --------------------------------------------------------------

    // TB_CUST
    // --------------------------------------------------------------
    // TB_CUST.CUST_LEVEL
    public static final String CUSTLEVEL_VIP = "V";
    public static final String CUSTLEVEL_MIDDLE = "M";
    public static final String CUSTLEVEL_NORMAL = "N";
    // TB_CUST.GENDER
    public static final String GENDER_FEMALE = "F";
    public static final String GENDER_MALE = "M";
    // TB_CUST.MARRIAGE
    public static final String MARRIAGE_SINGLE = "0";
    public static final String MARRIAGE_MARRIED = "1";
    // TB_CUST.EDUCATION
    public static final String EDUCATION_NA = "0";
    public static final String EDUCATION_HIGHSCHOOL = "1";
    public static final String EDUCATION_JUNIORCOLLEGE = "2";
    public static final String EDUCATION_UNIVERSITY = "3";
    public static final String EDUCATION_MASTER = "4";
    public static final String EDUCATION_DR = "5";
    public static final String EDUCATION_OTHERS = "6";
    // --------------------------------------------------------------

    // TB_BANK_ACCT
    // --------------------------------------------------------------
    // TB_BANK_ACCT.CREDIT_DEBIT_UNIT
    public static final String CREDITDEBITUNIT_CENTER = "C";
    public static final String CREDITDEBITUNIT_ISSUER = "I";
    public static final String CREDITDEBITUNIT_ACQUIRER = "A";
    public static final String CREDITDEBITUNIT_SPONSOR = "S";
    public static final String CREDITDEBITUNIT_OWNER = "O";
    public static final String CREDITDEBITUNIT_MERCHANT = "M";
    // --------------------------------------------------------------

    // TB_PERSO_DTL
    // --------------------------------------------------------------
    // TB_PERSO_DTL.CARD_STATUS
    public static final String PDCARDSTATUS_NOTACTIVED = "2";
    public static final String PDCARDSTATUS_ACTIVED = "3";
    // TB_PERSO_DTL.STATUS
    public static final String PDSTATUS_DATAGEN = "1";
    public static final String PDSTATUS_FILEGEN = "2";
    // --------------------------------------------------------------

    // TB_STORE_COUNTER
    // --------------------------------------------------------------
    // TB_STORE_COUNTER.STATUS
    public static final String STORECOUNTER_TERMINATION = "0";
    public static final String STORECOUNTER_EFFECTIVE = "1";
    // --------------------------------------------------------------

    // TB_OUTCTL
    // --------------------------------------------------------------
    // TB_OUTCTL.WORK_FLAG
    public static final String OWORKFLAG_INWORK = "1";
    public static final String OWORKFLAG_INTEMP = "3";
    public static final String OWORKFLAG_DELETED = "6";
    public static final String OWORKFLAG_PROCESSFAIL = "9";
    // --------------------------------------------------------------

    // TB_BONUS
    // --------------------------------------------------------------
    // TB_BONUS.BONUS_TYPE
    public static final String BONUSTYPE_CHIP = "C";
    public static final String BONUSTYPE_HOST = "H";
    public static final String BONUSTYPE_PAPER = "P";
    // TB_BONUS.BONUS_NATURE
    public static final String BONUSNATURE_POINT = "P";
    public static final String BONUSNATURE_COUPON = "C";
    // --------------------------------------------------------------

    // TB_APPOINT_LIST_CANCEL
    // --------------------------------------------------------------
    // TB_APPOINT_LIST_CANCEL.CANCEL_STATUS
    public static final String CANCELSTATUS_CANCELED = "1";
    public static final String CANCELSTATUS_DOWNLOADED_AUTO = "2";
    public static final String CANCELSTATUS_DOWNLOADED_NOAUTO = "9";
    // --------------------------------------------------------------

    // TB_FILE_CODE
    // --------------------------------------------------------------
    // TB_FILE_CODE.ONUS_FLAG
    public static final String FCONUSFLAG_ALL = "A";
    public static final String FCONUSFLAG_ONUS = "N";
    public static final String FCONUSFLAG_OFFUS = "F";
    // --------------------------------------------------------------

    // --------------------------------------------------------------
    public static final String TRANSFROM_ONLINE = "O";
    public static final String TRANSFROM_BATCH = "B";
    // --------------------------------------------------------------
}
