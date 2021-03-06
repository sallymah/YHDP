/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */

/*
 * (版權及授權描述)
 *
 * Copyright 2007 (C) Hyweb. All Rights Reserved.
 *
 * $History: CheckOfflineTxnUtil.java $
 * 
 * ***********************************************
 * 20071011 by rock 
 * 拿掉blacklist的檢查
 * 
 * *****************  Version 2  *****************
 * User: Anny     Date: 2007/01/22     Time:17:40
 * 1.delete check card.status ='T' -> Rcode='2197'
 * 2.add blacklist From SDD
 * 
 * *****************  Version 2  *****************
 * User: Anny     Date: 2007/01/22     Time:17:40
 * 1.add check card.status ='T' -> Rcode='2197'
 * 2.檢查後tb_trans.check_date={batchDate}
 * 
 * *****************  Version 1  *****************
 * User: duncan    Date: 2007/01/03     Time:14:00
 * 
 */

package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.info.TbTransCheckErrInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
//import tw.com.hyweb.service.db.mgr.TbBlacklistSettingMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbMerchMgr;
import tw.com.hyweb.service.db.mgr.TbTermMgr;
import tw.com.hyweb.service.db.mgr.TbTransCheckErrMgr;
import tw.com.hyweb.service.db.mgr.TbTransMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * CheckOfflineTxnUtil
 * </pre>
 */
public class CheckOfflineTxnUtil
{
    private static Logger log = Logger.getLogger(CheckOfflineTxnUtil.class);

    private CheckOfflineTxnUtil()
    {
    }

    /**
     * 是否要做針對離線交易檢查交易合法性－卡號是否存在 "1", "yes", "true" return true otherwise, return
     * false
     * 
     * @param value value
     * @return true or false
     */
    public static boolean isCheckCardNo(String value)
    {
        return isCheckGeneral(value);
    }

    /**
     * 是否要做針對離線交易檢查交易合法性－卡片狀態是否不合法(已餘額轉置) "1", "yes", "true" return true otherwise,
     * return false
     * 
     * @param value value
     * @return true or false
     */
    public static boolean isCheckCardStatueT(String value)
    {
        return isCheckGeneral(value);
    }
    
    /**
     * 是否要做針對離線交易檢查交易合法性－卡片狀態是否合法 "1", "yes", "true" return true otherwise,
     * return false
     * 
     * @param value value
     * @return true or false
     */
    public static boolean isCheckCardStatue(String value)
    {
        return isCheckGeneral(value);
    }

    /**
     * 是否要做針對離線交易檢查交易合法性－特店是否存在 "1", "yes", "true" return true otherwise, return
     * false
     * 
     * @param value value
     * @return true or false
     */
    public static boolean isCheckMerch(String value)
    {
        return isCheckGeneral(value);
    }

    /**
     * 是否要做針對離線交易檢查交易合法性－端末機是否存在 "1", "yes", "true" return true otherwise,
     * return false
     * 
     * @param value value
     * @return true or false
     */
    public static boolean isCheckTerm(String value)
    {
        return isCheckGeneral(value);
    }

    /**
     * 是否要做針對離線交易檢查交易合法性－針對取消交易，驗證原始交易是否存在 "1", "yes", "true" return true
     * otherwise, return false
     * 
     * @param value value
     * @return true or false
     */
    public static boolean isCheckOrigTxn(String value)
    {
        return isCheckGeneral(value);
    }

    /**
     * 是否要做針對離線交易檢查交易合法性－黑名單 "1", "yes", "true" return true otherwise, return
     * false
     * 
     * @param value value
     * @return true or false
     */
//    public static boolean isCheckBlacklist(String value)
//    {
//        return isCheckGeneral(value);
//    }

    private static boolean isCheckGeneral(String value)
    {
        boolean ret = false;
        if (StringUtil.isEmpty(value))
        {
            ret = false;
        }
        else
        {
            if ("1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value))
            {
                ret = true;
            }
            else
            {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－卡號是否存在, new 一個 Connection 來檢查, 合法, 傳回 "0000", 不合法, 傳回
     * "2191"
     * 
     * @param info info
     * @return "0000" or "2191"
     */
    public static String checkCardNo(TbTransInfo info)
    {
        Connection conn = null;
        String ret = Constants.RCODE_2191_CARDNO_ERR;
        try
        {
            conn = BatchUtil.getConnection();
            ret = checkCardNo(conn, info);
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2191_CARDNO_ERR;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－卡號是否存在, 用 conn 來檢查, 合法, 傳回 "0000", 不合法, 傳回 "2191"
     * 
     * @param conn conn
     * @param info info
     * @return "0000" or "2191"
     */
    public static String checkCardNo(Connection conn, TbTransInfo info)
    {
        if (info == null)
        {
            return Constants.RCODE_2191_CARDNO_ERR;
        }

        String ret = Constants.RCODE_2191_CARDNO_ERR;
        StringBuffer where = new StringBuffer();
        try
        {
            TbCardMgr mgr = new TbCardMgr(conn);
            where.append("CARD_NO = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getCardNo()));
            int count = mgr.getCount(where.toString());
            if (count > 0)
            {
                ret = Constants.RCODE_0000_OK;
            }
            else
            {
                ret = Constants.RCODE_2191_CARDNO_ERR;
            }
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2191_CARDNO_ERR;
            log.warn("checkCardNo error:" + where.toString(), ignore);
        }

        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－卡片狀態是否合法, new 一個 Connection 來檢查, 合法, 傳回 "0000", 不合法, 傳回
     * "2192"
     * 
     * @param info info
     * @return "0000" or "2192"
     */
    public static String checkCardStatus(TbTransInfo info)
    {
        Connection conn = null;
        String ret = Constants.RCODE_2192_CARDSTATUS_ERR;
        try
        {
            conn = BatchUtil.getConnection();
            ret = checkCardStatus(conn, info);
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2192_CARDSTATUS_ERR;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－卡片狀態是否合法, 用 conn 來檢查, 合法, 傳回 "0000", 不合法, 傳回 "2192"
     * 
     * @param conn conn
     * @param info info
     * @return "0000" or "2192"
     */
    public static String checkCardStatus(Connection conn, TbTransInfo info)
    {
        /*假設卡片不存在不檢查此問題,20070206*/
        if (checkCardNo(conn, info).equals(Constants.RCODE_2191_CARDNO_ERR))
        {
            return Constants.RCODE_0000_OK;
        }
            
        if (info == null)
        {
            return Constants.RCODE_2192_CARDSTATUS_ERR;
        }

        String ret = Constants.RCODE_2192_CARDSTATUS_ERR;
        StringBuffer where = new StringBuffer();
        try
        {
            TbCardMgr mgr = new TbCardMgr(conn);
            where.append("CARD_NO = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getCardNo()));
            where.append(" AND ");
            where.append("STATUS = ");
            where.append(StringUtil.toSqlValueWithSQuote("3"));
            
            int count = mgr.getCount(where.toString());
            if (count > 0)
            {
                ret = Constants.RCODE_0000_OK;
            }
            else
            {
                ret = Constants.RCODE_2192_CARDSTATUS_ERR;
            }
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2192_CARDSTATUS_ERR;
            log.warn("checkCardStatus error:" + where.toString(), ignore);
        }

        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－特店是否存在, new 一個 Connection 來檢查, 合法, 傳回 "0000", 不合法, 傳回
     * "2193"
     * 
     * @param info info
     * @return "0000" or "2193"
     */
    public static String checkMerch(TbTransInfo info)
    {
        Connection conn = null;
        String ret = Constants.RCODE_2193_MERCH_ERR;
        try
        {
            conn = BatchUtil.getConnection();
            ret = checkMerch(conn, info);
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2193_MERCH_ERR;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－特店是否存在, 用 conn 來檢查, 合法, 傳回 "0000", 不合法, 傳回 "2193"
     * 
     * @param conn conn
     * @param info info
     * @return "0000" or "2193"
     */
    public static String checkMerch(Connection conn, TbTransInfo info)
    {
        if (info == null)
        {
            return Constants.RCODE_2193_MERCH_ERR;
        }

        String ret = Constants.RCODE_2193_MERCH_ERR;
        StringBuffer where = new StringBuffer();
        try
        {
            TbMerchMgr mgr = new TbMerchMgr(conn);
            where.append("MERCH_ID = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getMerchId()));
            int count = mgr.getCount(where.toString());
            if (count > 0)
            {
                ret = Constants.RCODE_0000_OK;
            }
            else
            {
                ret = Constants.RCODE_2193_MERCH_ERR;
            }
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2193_MERCH_ERR;
            log.warn("checkMerch error:" + where.toString(), ignore);
        }

        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－端末機是否存在, new 一個 Connection 來檢查, 合法, 傳回 "0000", 不合法, 傳回
     * "2194"
     * 
     * @param info info
     * @return "0000" or "2194"
     */
    public static String checkTerm(TbTransInfo info)
    {
        Connection conn = null;
        String ret = Constants.RCODE_2194_TERM_ERR;
        try
        {
            conn = BatchUtil.getConnection();
            ret = checkTerm(conn, info);
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2194_TERM_ERR;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－端末機是否存在, 用 conn 來檢查, 合法, 傳回 "0000", 不合法, 傳回 "2194"
     * 
     * @param conn conn
     * @param info info
     * @return "0000" or "2194"
     */
    public static String checkTerm(Connection conn, TbTransInfo info)
    {
        if (info == null)
        {
            return Constants.RCODE_2194_TERM_ERR;
        }

        String ret = Constants.RCODE_2194_TERM_ERR;
        StringBuffer where = new StringBuffer();
        try
        {
            TbTermMgr mgr = new TbTermMgr(conn);
            where.append("MERCH_ID = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getMerchId()));
            where.append(" AND ");
            where.append("TERM_ID = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getTermId()));
            int count = mgr.getCount(where.toString());
            if (count > 0)
            {
                ret = Constants.RCODE_0000_OK;
            }
            else
            {
                ret = Constants.RCODE_2194_TERM_ERR;
            }
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2194_TERM_ERR;
            log.warn("checkTerm error:" + where.toString(), ignore);
        }

        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－針對取消交易，驗證原始交易是否存在, new 一個 Connection 來檢查, 合法, 傳回 "0000",
     * 不合法, 傳回 "2195"
     * 
     * @param info info
     * @return "0000" or "2195"
     */
    public static String checkOrigTxn(TbTransInfo info)
    {
        Connection conn = null;
        String ret = Constants.RCODE_2195_ORIGTXN_ERR;
        try
        {
            conn = BatchUtil.getConnection();
            ret = checkOrigTxn(conn, info);
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2195_ORIGTXN_ERR;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * 針對離線交易檢查交易合法性－針對取消交易，驗證原始交易是否存在, 用 conn 來檢查, 合法, 傳回 "0000", 不合法, 傳回
     * "2195"
     * 
     * @param conn conn
     * @param info info
     * @return "0000" or "2195"
     */
    public static String checkOrigTxn(Connection conn, TbTransInfo info)
    {
        if (info == null)
        {
            return Constants.RCODE_2195_ORIGTXN_ERR;
        }

        // 原交易是空的, 代表此筆不是取消交易, 不用檢查
        if (StringUtil.isEmpty(info.getOrigLmsInvoiceNo()))
        {
            return Constants.RCODE_0000_OK;
        }

        // 查看原交易是否在存在
        String ret = Constants.RCODE_2195_ORIGTXN_ERR;
        StringBuffer where = new StringBuffer();
        try
        {
            TbTransMgr mgr = new TbTransMgr(conn);
            where.append("CARD_NO = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getCardNo()));
            where.append(" AND ");
            where.append("EXPIRY_DATE = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getExpiryDate()));
            where.append(" AND ");
            where.append("LMS_INVOICE_NO = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getOrigLmsInvoiceNo()));
            /*20071226 ANNY 拿掉比對p_code的條件,因為原始交易的PCODE和取消交易不同*/
            /*where.append(" AND ");
            where.append("P_CODE = ");
            where.append(StringUtil.toSqlValueWithSQuote(info.getPCode()));*/
            int count = mgr.getCount(where.toString());
            if (count > 0)
            {
                ret = Constants.RCODE_0000_OK;
            }
            else
            {
                ret = Constants.RCODE_2195_ORIGTXN_ERR;
            }
        }
        catch (Exception ignore)
        {
            ret = Constants.RCODE_2195_ORIGTXN_ERR;
            log.warn("checkOrigTxn error:" + where.toString(), ignore);
        }

        return ret;
    }

    /**
     * SDD 當未定義, 先不做, 固定傳回 "0000" 針對離線交易檢查交易合法性－黑名單, new 一個 Connection 來檢查, 合法,
     * 傳回 "0000", 不合法, 傳回 "2196"
     * 
     * @param info info
     * @return "0000" or "2196"
     */
//    public static String checkBlacklist(TbTransInfo info)
//    {
//        Connection conn = null;
//        String ret = Constants.RCODE_2196_BLACKLIST_ERR;
//        try
//        {
//            conn = BatchUtil.getConnection();
//            ret = checkBlacklist(conn, info);
//        }
//        catch (Exception ignore)
//        {
//            ret = Constants.RCODE_2196_BLACKLIST_ERR;
//        }
//        finally
//        {
//            ReleaseResource.releaseDB(conn);
//        }
//        return ret;
//    }

    /**
     * 針對離線交易檢查交易合法性－黑名單, 用 conn 來檢查, 合法, 傳回 "0000",
     * 不合法, 傳回 "2196"
     * 
     * @param conn conn
     * @param info info
     * @return "0000" or "2196"
     */
//    public static String checkBlacklist(Connection conn, TbTransInfo info)
//    {
//        // todo: DB schema does not define
//        if (info == null)
//        {
//            return Constants.RCODE_2196_BLACKLIST_ERR;
//        }
//
//        String ret = Constants.RCODE_2196_BLACKLIST_ERR;
//        StringBuffer where = new StringBuffer();
//        try
//        {
//            TbBlacklistSettingMgr mgr = new TbBlacklistSettingMgr(conn);
//            where.append("CARD_NO = ");
//            where.append(StringUtil.toSqlValueWithSQuote(info.getCardNo()));
//            where.append(" AND ");
//            where.append("EXPIRY_DATE = ");
//            where.append(StringUtil.toSqlValueWithSQuote(info.getExpiryDate()));
//            where.append(" AND ");
//            where.append("reg_date is not null ");
//            where.append(" AND ");
//            where.append("cancel_date is null ");
//            int count = mgr.getCount(where.toString());
//            if (count > 0)
//            {
//                ret = Constants.RCODE_0000_OK;
//            }
//            else
//            {
//                ret = Constants.RCODE_2196_BLACKLIST_ERR;
//            }
//        }
//        catch (Exception ignore)
//        {
//            ret = Constants.RCODE_2196_BLACKLIST_ERR;
//            log.warn("checkBLACKLIST error:" + where.toString(), ignore);
//        }
//        
//        return Constants.RCODE_0000_OK;
//    }

    /**
     * 依據 conditionInfo 的設定值來對 transInfo 做相對應的檢查, new 一個 Connection 來檢查,
     * 並將檢查的結果用 CheckOfflineInfo 來表示
     * 
     * @param transInfo transInfo
     * @param conditionInfo conditionInfo
     * @return CheckOfflineInfo
     */
    public static CheckOfflineInfo makeCheckOfflineInfo(TbTransInfo transInfo, CheckConditionInfo conditionInfo)
    {
        Connection conn = null;
        CheckOfflineInfo ret = null;
        try
        {
            conn = BatchUtil.getConnection();
            ret = makeCheckOfflineInfo(conn, transInfo, conditionInfo);
        }
        catch (Exception ignore)
        {
            ret = null;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * 依據 conditionInfo 的設定值來對 transInfo 做相對應的檢查, 用 conn 來檢查, 並將檢查的結果用
     * CheckOfflineInfo 來表示
     * 
     * @param transInfo transInfo
     * @param conditionInfo conditionInfo
     * @return CheckOfflineInfo
     */
    public static CheckOfflineInfo makeCheckOfflineInfo(Connection conn, TbTransInfo transInfo,
                    CheckConditionInfo conditionInfo)
    {
        if (transInfo == null || conditionInfo == null)
        {
            return null;
        }

        CheckOfflineInfo checkInfo = null;
        try
        {
            checkInfo = new CheckOfflineInfo();
            checkInfo.setTbTransInfo(transInfo);
            // check card no
            if (isCheckCardNo(conditionInfo.getCheckCardNo()))
            {
                checkInfo.addRcode(checkCardNo(conn, transInfo));
            }
            // check card status 
            if (isCheckCardStatue(conditionInfo.getCheckCardStatus()))
            {
                checkInfo.addRcode(checkCardStatus(conn, transInfo));
            }
            // check merch
            if (isCheckMerch(conditionInfo.getCheckMerch()))
            {
                checkInfo.addRcode(checkMerch(conn, transInfo));
            }
            // check term
            if (isCheckTerm(conditionInfo.getCheckTerm()))
            {
                checkInfo.addRcode(checkTerm(conn, transInfo));
            }
            // check orig txn
            if (isCheckOrigTxn(conditionInfo.getCheckOrigTxn()))
            {
                checkInfo.addRcode(checkOrigTxn(conn, transInfo));
            }
            // check black list
//            if (isCheckBlacklist(conditionInfo.getCheckBlacklist()))
//            {
//                checkInfo.addRcode(checkBlacklist(conn, transInfo));
//            }
        }
        catch (Exception ignore)
        {
            checkInfo = null;
            log.warn("makeCheckOfflineInfo error:" + ignore.getMessage(), ignore);
        }

        return checkInfo;
    }

    /**
     * each element is CheckOfflineInfo object, new 一個 Connection 來處理, 根據
     * cfInfos 裡的每一個 CheckOfflineInfo 的結果, 寫到 TB_TRANS_CHECK_ERR 與更新
     * TB_TRANS.CHECK_FLAG, 成功傳回 true, 失敗傳回 false
     * 
     * @param cfInfos cfInfos
     * @return true or false
     */
    public static boolean handleCheckOfflineInfos(List cfInfos)
    {
        Connection conn = null;
        boolean ret = false;
        try
        {
            conn = BatchUtil.getConnection();
            ret = handleCheckOfflineInfos(conn, cfInfos);
        }
        catch (Exception ignore)
        {
            ret = false;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * each element is CheckOfflineInfo object, 用 conn 來檢查, 根據 cfInfos 裡的每一個
     * CheckOfflineInfo 的結果, 寫到 TB_TRANS_CHECK_ERR 與更新 TB_TRANS.CHECK_FLAG, 成功傳回
     * true, 失敗傳回 false
     * 
     * @param conn conn
     * @param cfInfos cfInfos
     * @return true or false
     */
    public static boolean handleCheckOfflineInfos(Connection conn, List cfInfos)
    {
        if (cfInfos == null || cfInfos.size() == 0)
        {
            return true;
        }

        boolean autoCommit = false;
        boolean ret = false;
        try
        {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            TbTransCheckErrMgr tcemgr = new TbTransCheckErrMgr(conn);
            TbTransMgr tmgr = new TbTransMgr(conn);
            for (int i = 0; i < cfInfos.size(); i++)
            {
                CheckOfflineInfo cfInfo = (CheckOfflineInfo) cfInfos.get(i);
                if (Constants.CHECKFLAG_SUCCESS.equals(cfInfo.getTbTransInfo().getCheckFlag()))
                {
                    // check flag = 0, 成功
                    if (cfInfo.getRcodes().size() == 0)
                    {
                        // do nothing
                        //update TB_TRANS.CHECK_DATE
                        cfInfo.getTbTransInfo().setCheckDate(DateUtil.getTodayString().substring(0, 8));
                        tmgr.update(cfInfo.getTbTransInfo());
                    }
                    else
                    {
                        // insert TB_TRANS_CHECK_ERR
                        for (int j = 0; j < cfInfo.getRcodes().size(); j++)
                        {
                            TbTransCheckErrInfo info = new TbTransCheckErrInfo();
                            info.setCardNo(cfInfo.getTbTransInfo().getCardNo());
                            info.setExpiryDate(cfInfo.getTbTransInfo().getExpiryDate());
                            info.setLmsInvoiceNo(cfInfo.getTbTransInfo().getLmsInvoiceNo());
                            //info.setAtc(cfInfo.getTbTransInfo().getAtc());
                            info.setPCode(cfInfo.getTbTransInfo().getPCode());
                            info.setCheckRcode(cfInfo.getRcodes().get(j).toString());
                            String parMon = StringUtil.pendingKey(DateUtil.getMonth(), 2);
                            String parDay = StringUtil.pendingKey(DateUtil.getDay(), 2);
                            info.setParMon(parMon);
                            info.setParDay(parDay);
                            tcemgr.insert(info);
                        }
                        // update TB_TRANS.CHECK_FLAG & TB_TRANS.CHECK_DATE
                        cfInfo.getTbTransInfo().setCheckFlag(Constants.CHECKFLAG_FAIL);
                        cfInfo.getTbTransInfo().setCheckDate(DateUtil.getTodayString().substring(0, 8));
                        tmgr.update(cfInfo.getTbTransInfo());
                    }
                }
                else if (Constants.CHECKFLAG_FAIL.equals(cfInfo.getTbTransInfo().getCheckFlag()))
                {
                    // check flag = 1, 失敗
                    if (cfInfo.getRcodes().size() == 0)
                    {
                        // update TB_TRANS.CHECK_FLAG
                        cfInfo.getTbTransInfo().setCheckFlag(Constants.CHECKFLAG_SUCCESS);
                        tmgr.update(cfInfo.getTbTransInfo());
                    }
                    else
                    {
                        // insert TB_TRANS_CHECK_ERR
                        for (int j = 0; j < cfInfo.getRcodes().size(); j++)
                        {
                            TbTransCheckErrInfo info = new TbTransCheckErrInfo();
                            info.setCardNo(cfInfo.getTbTransInfo().getCardNo());
                            info.setExpiryDate(cfInfo.getTbTransInfo().getExpiryDate());
                            info.setLmsInvoiceNo(cfInfo.getTbTransInfo().getLmsInvoiceNo());
                            //info.setAtc(cfInfo.getTbTransInfo().getAtc());
                            info.setPCode(cfInfo.getTbTransInfo().getPCode());
                            info.setCheckRcode(cfInfo.getRcodes().get(j).toString());
                            String parMon = StringUtil.pendingKey(DateUtil.getMonth(), 2);
                            String parDay = StringUtil.pendingKey(DateUtil.getDay(), 2);
                            info.setParMon(parMon);
                            info.setParDay(parDay);
                            tcemgr.insert(info);
                        }
                        // 已經是 fail, 不用 update TB_TRANS.CHECK_FLAG
                    }
                }
            }
            conn.commit();
            ret = true;
        }
        catch (Exception e)
        {
            ret = false;
            log.warn("handleCheckOfflineInfos error:" + e.getMessage(), e);
            if (conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ignore)
                {
                    ;
                }
            }
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.setAutoCommit(autoCommit);
                }
                catch (Exception ignore)
                {
                    ;
                }
            }
        }
        return ret;
    }
}
