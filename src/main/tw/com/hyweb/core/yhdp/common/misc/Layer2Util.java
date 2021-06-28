/**
 * changelog
 * --------------------
 * 20080901
 * duncan,anny
 * getExpectCutTime minor bug fix
 * --------------------
 * 20071002
 * duncan
 * 移除 BatchUtil 的使用
 * --------------------
 * 20070831
 * rock
 * add isHoliday 若傳入日期為假日則傳回true
 * add getNextBusinessDate() 取得下一個工作日
 * --------------------
 * 20070824
 * duncan
 * 加 getAppointAwardProg(Connection conn, String progId)
 * --------------------
 * 20070814
 * rock
 * handleDXXCycle() 修正為加上負的天數
 * --------------------
 * 20070813
 * rock
 * add getUnit2TransField()
 * --------------------
 * 20070628
 * duncan,tracy,anny
 * add getUnknownXXX methods, 若沒設 UNKNOWN_XXX 設定的話, 傳回 ""
 * --------------------
 * 20070426
 * Layer2Util.getCustAssociatorInfo add associatorId parameter
 * add getBonusInfo(Connection, String) method
 * --------------------
 * 20070327, Layer2Util.getCustAssociatorInfo two methods, add regionId parameter
 * --------------------
 * 20070416, Layer2Util.isValidCycle & Layer2Util.getCycleDate => add Bxx
 *  CYCLE表示法
 *  Dnn nn = 00~99 (baseDate+nn天的日期)
 *  Wnn nn = 01, 02, 03, 04, 05, 06, 00
 *  Mnn nn = 01~28 or 99
 *  Bnn nn = 01, 02, 03, 04, 05, 06, 00
 *  ex. D00表示當天
 *  ex. D01表示隔天
 *  ex. D02表示2天後
 *  ex. W03表示每星期三（區間：上星期三至本週二）
 *  ex. M05表示每個月5號（區間：上個月5號至本月4號）
 *  ex. M99表示每個月月底（區間：上個月月底至本月底前一日）
 *  ex. B01表示隔週一
 * --------------------
 *
 */
/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.core.yhdp.common.misc;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import tw.com.hyweb.service.db.info.*;
import tw.com.hyweb.service.db.mgr.*;
import tw.com.hyweb.service.db.sess.TbCenterCtr;
import tw.com.hyweb.service.db.sess.TbRcodeConfigCtr;
import tw.com.hyweb.service.db.sess.TbSysConfigCtr;
import tw.com.hyweb.service.db.sess.TbTxnDefCtr;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * <pre>
 * Layer2Util
 * </pre>
 * <p/>
 * author: mswu
 */
public class Layer2Util
{
    private static Logger log = Logger.getLogger(Layer2Util.class);

    private Layer2Util()
    {
        ;
    }

    // key: String, TB_RCODE_CONFIG, value: TbRcodeConfigInfo object
    private static HashMap rcodeConfigInfos = new HashMap();

    /**
     * 使用 action 和 info (TB_RCODE_CONFIG) 來產生 rcodeConfigInfos
     */
    public static void loadRcodeConfigInfos()
    {
        rcodeConfigInfos.clear();
        try
        {
            TbRcodeConfigCtr action = new TbRcodeConfigCtr(Constants.DSNAME_COMMON);
            Vector result = new Vector();
            action.queryAll(result, "RCODE");
            for (int i = 0; i < result.size(); i++)
            {
                TbRcodeConfigInfo info = (TbRcodeConfigInfo) result.get(i);
                rcodeConfigInfos.put(info.getRcode(), info);
            }
        }
        catch (Exception ignore)
        {
            log.warn("loadRcodeConfigInfos fail:" + ignore.getMessage(), ignore);
        }
        finally
        {
            ;
        }
    }

    public static HashMap getRcodeConfigInfos() {
        return rcodeConfigInfos;
    }

    private static final String COMPUTE_1 = "1";

    private static final String COMPUTE_0 = "0";

    /**
     * get info from rcodeConfigInfos. if (info != null &&
     * "1".equals(info.getBalanceFlag()) return true, otherswise, return false
     *
     * @param rcode
     * @return
     */
    public static boolean isComputeBalance(String rcode)
    {
        boolean ret = false;
        if (rcode == null || "".equals(rcode))
        {
            return ret;
        }
        TbRcodeConfigInfo info = (TbRcodeConfigInfo) rcodeConfigInfos.get(rcode);
        if (info == null)
        {
            ret = false;
        }
        else
        {
            if (COMPUTE_1.equals(info.getBalanceFlag()))
            {
                ret = true;
            }
            else if (COMPUTE_0.equals(info.getBalanceFlag()))
            {
                ret = false;
            }
            else
            {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * get info from rcodeConfigInfos. if (info != null &&
     * "1".equals(info.getSummaryFlag()) return true, otherswise, return false
     *
     * @param rcode
     * @return
     */
    public static boolean isComputeSummary(String rcode)
    {
        boolean ret = false;
        if (rcode == null || "".equals(rcode))
        {
            return ret;
        }
        TbRcodeConfigInfo info = (TbRcodeConfigInfo) rcodeConfigInfos.get(rcode);
        if (info == null)
        {
            ret = false;
        }
        else
        {
            if (COMPUTE_1.equals(info.getSummaryFlag()))
            {
                ret = true;
            }
            else if (COMPUTE_0.equals(info.getSummaryFlag()))
            {
                ret = false;
            }
            else
            {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * get info from rcodeConfigInfos. if (info != null &&
     * "1".equals(info.getSettlementFlag()) return true, otherswise, return
     * false
     *
     * @param rcode
     * @return
     */
    public static boolean isComputeSettlement(String rcode)
    {
        boolean ret = false;
        if (rcode == null || "".equals(rcode))
        {
            return ret;
        }
        TbRcodeConfigInfo info = (TbRcodeConfigInfo) rcodeConfigInfos.get(rcode);
        if (info == null)
        {
            ret = false;
        }
        else
        {
            if (COMPUTE_1.equals(info.getSettlementFlag()))
            {
                ret = true;
            }
            else if (COMPUTE_0.equals(info.getSettlementFlag()))
            {
                ret = false;
            }
            else
            {
                ret = false;
            }
        }
        return ret;
    }

    // key: String, TB_BATCH_CONFIG.PARM, value: TbBatchConfig info
    private static HashMap batchConfigInfos = new HashMap();

    /**
     * 使用 action 和 info (TB_BATCH_CONFIG) 來產生 batchConfigInfos
     */
    public static void loadBatchConfigInfos()
    {
        batchConfigInfos.clear();
        try
        {
            TbSysConfigCtr action = new TbSysConfigCtr(Constants.DSNAME_COMMON);
            Vector result = new Vector();
            action.queryAll(result, "PARM");
            for (int i = 0; i < result.size(); i++)
            {
                TbSysConfigInfo info = (TbSysConfigInfo) result.get(i);
                batchConfigInfos.put(info.getParm(), info);
            }
        }
        catch (Exception ignore)
        {
            log.warn("loadBatchConfigInfos fail:" + ignore.getMessage(), ignore);
        }
        finally
        {
            ;
        }
    }

    public static HashMap getBatchConfigInfos() {
        return batchConfigInfos;
    }

    /**
     * get info from batchConfigInfos. if (info == null) return "", otherwise,
     * return info.getValue()
     *
     * @param param
     * @return
     */
    public static String getBatchConfig(String param)
    {
        String ret = "";
        if (param == null || "".equals(param))
        {
            return ret;
        }
        TbSysConfigInfo info = (TbSysConfigInfo) batchConfigInfos.get(param);
        if (info == null)
        {
            ret = "";
        }
        else
        {
            ret = info.getValue();
        }
        return ret;
    }

    // key: String, TB_TXN_DEF.TXN_CODE, value: TbTxnDef info
    private static HashMap txnDefInfos = new HashMap();

    /**
     * 使用 action 和 info (TB_TXN_DEF) 來產生 txnDefInfos
     */
    public static void loadTxnDefInfos()
    {
        txnDefInfos.clear();
        try
        {
            TbTxnDefCtr action = new TbTxnDefCtr(Constants.DSNAME_COMMON);
            Vector result = new Vector();
            action.queryAll(result, "TXN_CODE");
            for (int i = 0; i < result.size(); i++)
            {
                TbTxnDefInfo info = (TbTxnDefInfo) result.get(i);
                txnDefInfos.put(info.getTxnCode(), info);
            }
        }
        catch (Exception ignore)
        {
            log.warn("loadTxnDefInfos fail:" + ignore.getMessage(), ignore);
        }
        finally
        {
            ;
        }
    }

    public static HashMap getTxnDefInfos() {
        return txnDefInfos;
    }
    /**
     * author:Anny
     *
     * @param info
     * @param txnCode
     * @param modifyQty
     * @param conn
     * @return
     * @throws SQLException
     */
    public static String getCardBalSql(TbCardBalInfo info, String txnCode, double modifyQty, Connection conn) throws SQLException
    {
        CardBalAction action = new CardBalAction(conn, info);

        return action.getSql(txnCode, modifyQty);
    }

    /**
     * author:Anny
     *
     * @param info
     * @param txnCode
     * @param modifyQty
     * @param conn
     * @return
     * @throws SQLException
     */
    public static String getCustBalSql(TbCustBalInfo info, String txnCode, double modifyQty, Connection conn) throws SQLException
    {
        CustBalAction action = new CustBalAction(conn, info);

        return action.getSql(txnCode, modifyQty);
    }

    /**
     * author:Anny
     *
     * @param info
     * @param txnCode
     * @param modifyQty
     * @param conn
     * @return
     * @throws SQLException
     */
    public static String getAcctBalSql(TbAcctBalInfo info, String txnCode, double modifyQty, Connection conn) throws SQLException
    {
        AcctBalAction action = new AcctBalAction(conn, info);

        return action.getSql(txnCode, modifyQty);
    }

    private static final String SIGN_P = "P";

    private static final String SIGN_M = "M";

    /**
     * get info from txnDefInfos. if (info == null) return false, else if
     * ("P".equals(info.getSign())) return true otherwise, return false
     *
     * @param txnCode
     * @return
     */
    public static boolean isPlusSign(String txnCode)
    {
        boolean ret = false;
        if (txnCode == null || "".equals(txnCode))
        {
            return ret;
        }
        TbTxnDefInfo info = (TbTxnDefInfo) txnDefInfos.get(txnCode);
        if (info == null)
        {
            ret = false;
        }
        else
        {
            if (SIGN_P.equals(info.getSign()))
            {
                ret = true;
            }
            else if (SIGN_M.equals(info.getSign()))
            {
                ret = false;
            }
            else
            {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * get info from txnDefInfos. if (info == null) return false, else if
     * ("M".equals(info.getSign())) return true otherwise, return false
     *
     * @param txnCode
     * @return
     */
    public static boolean isMinusSign(String txnCode)
    {
        boolean ret = false;
        if (txnCode == null || "".equals(txnCode))
        {
            return ret;
        }
        TbTxnDefInfo info = (TbTxnDefInfo) txnDefInfos.get(txnCode);
        if (info == null)
        {
            ret = false;
        }
        else
        {
            if (SIGN_M.equals(info.getSign()))
            {
                ret = true;
            }
            else if (SIGN_P.equals(info.getSign()))
            {
                ret = false;
            }
            else
            {
                ret = false;
            }
        }
        return ret;
    }

    // key: String, TB_CENTER, value: TbCenterInfo object
    private static HashMap centerInfos = new HashMap();

    /**
     * 使用 ctr 和 info (TB_CENTER) 來產生 centerInfos
     */
    public static void loadCenterInfos()
    {
        centerInfos.clear();
        try
        {
            TbCenterCtr action = new TbCenterCtr(Constants.DSNAME_COMMON);
            Vector result = new Vector();
            action.queryAll(result, "CENTER_ID");
            for (int i = 0; i < result.size(); i++)
            {
                TbCenterInfo info = (TbCenterInfo) result.get(i);
                centerInfos.put(info.getCenterId(), info);
            }
        }
        catch (Exception ignore)
        {
            log.warn("loadCenterInfos fail:" + ignore.getMessage(), ignore);
        }
        finally
        {
            ;
        }
    }

    public static HashMap getCenterInfos() {
        return centerInfos;
    }

    public static TbCenterInfo getCenterInfo()
    {
        TbCenterInfo info = null;
        if (centerInfos.keySet().size() > 0)
        {
            Iterator iter = centerInfos.keySet().iterator();
            String key = (String) iter.next();
            info = (TbCenterInfo) centerInfos.get(key);
        }
        return info;
    }

    public static TbCenterInfo getCenterInfo(String centerId)
    {
        TbCenterInfo info = null;
        if (StringUtil.isEmpty(centerId))
        {
            info = null;
        }
        else
        {
            info = (TbCenterInfo) centerInfos.get(centerId);
        }
        return info;
    }

    public static TbBonusInfo getBonusInfo(Connection conn, String bonusId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(bonusId)) {
            throw new IllegalArgumentException("bonusId is empty!");
        }
        TbBonusMgr mgr = new TbBonusMgr(conn);
        TbBonusInfo info = mgr.querySingle(bonusId);
        return info;
    }

    static
    {
        loadRcodeConfigInfos();
        loadBatchConfigInfos();
        loadTxnDefInfos();
        loadCenterInfos();
    }

    private static final String PARAM_CUT_TIME_X = "CUT_TIME_";

    /**
     * <pre>
     *   cutTime = getBatchConfig(“CUT_TIME_” + {txnSrc})
     *   if cutTime = HHMMSS
     *   expectCutTime=程式執行時，從現在往前找這個時間點，之前未過檔的交易才過檔
     *   ex. CUT_TIME_E=230000
     *   若目前時間為11/28 11:40 return 20061127230000
     *   若目前時間為11/28 23:05 return 20061128230000
     *   if cutTimeE = 000000
     *   expectCutTime=程式執行時，從現在往前，所有未過檔的交易均過檔
     *   ex. CUT_TIME_E=000000
     *   若目前時間為11/28 11:40 return 20061128114000
     * </pre>
     *
     * @param txnSrc
     * @return
     */
    public static String getExpectCutTime(String txnSrc)
    {
        String time = getBatchConfig(PARAM_CUT_TIME_X + txnSrc);
        String ret = "";
        if (time == null || "".equals(time) || "000000".equals(time))
        {
            ret = DateUtil.getTodayString();
        }
        else
        {
            try
            {
                long now = Long.parseLong(DateUtil.getTodayString());
                long nowDate = Long.parseLong(DateUtil.getShortTodayString());
                long nowDateMinus1 = Long.parseLong(DateUtil.addDate(DateUtil.getShortTodayString(), -1));
                long txnSrcTime = Long.parseLong(time);
                if (now > (nowDate + txnSrcTime))
                {
                    long tmp = nowDate + txnSrcTime;
                    ret = "" + tmp;
                }
                else
                {
                    long tmp = nowDateMinus1 + txnSrcTime;
                    ret = "" + tmp;
                }
            }
            catch (Exception ignore)
            {
                ret = "";
            }
        }
        return ret;
    }

    private static final String PARAM_CUT_UNBALANCED = "CUT_UNBALANCED";

    /**
     * <pre>
     *   依系統參數 (TB_BATCH_CONFIG) CUT_UNBALANCED 的值來決定傳回值
     *   {&quot;1&quot;, &quot;true&quot;, &quot;yes&quot;} 傳回 true, 否則傳回 false
     * </pre>
     *
     * @return
     */
    public static boolean isCutUnbalanced()
    {
        boolean ret = false;
        String value = getBatchConfig(PARAM_CUT_UNBALANCED);
        if ("1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value))
        {
            ret = true;
        }
        else
        {
            ret = false;
        }
        return ret;
    }

    private static final String CYCLE_D = "D";

    private static final String CYCLE_W = "W";

    private static final String CYCLE_M = "M";

    private static final String CYCLE_B = "B";

    private static boolean isValidCycle(String cycle)
    {
        if (cycle == null || "".equals(cycle))
        {
            return false;
        }
        if (cycle.length() != 3)
        {
            return false;
        }
        if (!cycle.startsWith(CYCLE_D) && !cycle.startsWith(CYCLE_W) &&
            !cycle.startsWith(CYCLE_M) && !cycle.startsWith(CYCLE_B))
        {
            return false;
        }
        int num = 0;
        try
        {
            num = Integer.parseInt(cycle.substring(1));
        }
        catch (Exception ignore)
        {
            return false;
        }
        if (cycle.startsWith(CYCLE_D))
        {
            if (num < 0 || num > 365)
            {
                return false;
            }
        }
        if (cycle.startsWith(CYCLE_W)||cycle.startsWith(CYCLE_B))
        {
            if (num < 0 || num > 6)
            {
                return false;
            }
        }
        if (cycle.startsWith(CYCLE_M))
        {
            if (num == 99)
            {
                ;
            }
            else if (num < 1 || num > 28)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * 如果baseDate是假日傳回true
     * @param conn
     * @param baseDate
     * @return
     */
    public static boolean isHoliDay(Connection conn, String baseDate)
    {
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select HOLIDAY from TB_HOLIDAY where BATCH_DATE=");
        sqlCmd.append(baseDate);

        String holiday = "";
        try {
            Vector vtr = (Vector) DbUtil.getInfoListGeneral(sqlCmd.toString(), DbUtil.VECTOR_LIST, conn);
            holiday = (String) ((Vector) vtr.get(0)).get(0);
        }
        catch (Exception ignore) {
            log.warn("isHoliDay warn:" + ignore.getMessage(), ignore);
            throw new RuntimeException("isHoliDay warn:" + ignore.getMessage());
        }

        if (holiday.equals("0"))
        {
            return false;
        }
        return true;
    }

    /**
     * 取得下一個工作日
     * @param conn
     * @param baseDate
     * @return
     */
    public static String getNextBusinessDate(Connection conn, String baseDate)
    {
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select min(BATCH_DATE) from TB_HOLIDAY where BATCH_DATE>");
        sqlCmd.append(baseDate).append(" and HOLIDAY='0'");

        String theDate = "";
        try {
            Vector settleDateVtr = (Vector) DbUtil.getInfoListGeneral(sqlCmd.toString(), DbUtil.VECTOR_LIST, conn);
            theDate = (String) ((Vector) settleDateVtr.get(0)).get(0);
        }
        catch (Exception ignore) {
            log.warn("getNextBusinessDate warn:" + ignore.getMessage(), ignore);
            throw new RuntimeException("getNextBusinessDate warn:" + ignore.getMessage());
        }

        if (StringUtil.isEmpty(theDate))
        {
            throw new RuntimeException("Result is Empty(TB_HOLIDAY.batchDate).");
        }
        return theDate;
    }

    /**
     * @param baseDate
     * @param cycle
     * @return
     */
    public static DateRange getProcPeriod(String baseDate, String cycle)
    {
        if (!isValidCycle(cycle) || !DateUtil.isValidDate(baseDate))
        {
            return null;
        }
        DateRange ret = null;
        if (cycle.startsWith(CYCLE_D))
        {
            ret = handleDXXCycle(baseDate, cycle);
        }
        else if (cycle.startsWith(CYCLE_W))
        {
            ret = handleWXXCycle(baseDate, cycle);
        }
        else if (cycle.startsWith(CYCLE_M))
        {
            ret = handleMXXCycle(baseDate, cycle);
        }

        return ret;
    }
    
    private static DateRange handleMXXCycle(String baseDate, String cycle)
    {
        DateRange ret = null;
        int cday = Integer.parseInt(cycle.substring(1));
        int bday = DateUtil.getDayOfMonth(baseDate);
        if (cday==99 && baseDate.substring(6).equals(String.valueOf(DateUtil.getLastDayOfMonth(baseDate))))
        {
            ret = new DateRange();
            // 上個月月底至本月底前一日
            ret.setStartDate(DateUtil.addMonth(baseDate, -1).substring(0,6) + String.valueOf(DateUtil.getLastDayOfMonth(DateUtil.addMonth(baseDate, -1))));
            ret.setEndDate(DateUtil.addDate(baseDate, -1));
        }
        else if (bday == cday)
        {
            ret = new DateRange();
            // 上個月
            ret.setStartDate(DateUtil.addMonth(baseDate, -1));
            ret.setEndDate(DateUtil.addDate(baseDate, -1));
        }
        else
        {
            ret = null;
        }
        return ret;
    }

    private static DateRange handleWXXCycle(String baseDate, String cycle)
    {
        DateRange ret = null;
        int cday = Integer.parseInt(cycle.substring(1));
        int bday = DateUtil.getDayOfWeek(baseDate);
        if (bday == cday)
        {
            ret = new DateRange();
            // 上個禮拜
            ret.setStartDate(DateUtil.addDate(baseDate, -7));
            ret.setEndDate(DateUtil.addDate(baseDate, -1));
        }
        else
        {
            ret = null;
        }
        return ret;
    }

    private static DateRange handleDXXCycle(String baseDate, String cycle)
    {
        DateRange ret = null;
        int num = -1 * Integer.parseInt(cycle.substring(1));
        ret = new DateRange();
        ret.setStartDate(DateUtil.addDate(baseDate, num));
        return ret;
    }

    public static String getCycleDate(Connection conn, String baseDate, String cycle)
    {
        if (!isValidCycle(cycle) || !DateUtil.isValidDate(baseDate))
        {
            return "";
        }

        int icycle = Integer.parseInt(cycle.substring(1));

        String batchDate = "";
        if (cycle.startsWith(CYCLE_D))
        {
            // 處理後第幾日撥款
            batchDate = DateUtil.addDate(baseDate, icycle);
        }
        else if (cycle.startsWith(CYCLE_W))
        {
            // W00:Sunday W01:Monday ... W06:Saturday
            int weekDay = icycle;

            int addDay = 0;
            int bday = DateUtil.getDayOfWeek(baseDate);

            /*
            if DayOfWeek(procDate)<= vWeekday,
               vTempSettlmentDate = procDate+(vWeekDay- DayOfWeek(procDate))
            else
               vTempSettlmentDate = procDate+7-( DayOfWeek(procDate)- vWeekDay)
            */
            if (bday <= weekDay)
            {
                // this week
                addDay = weekDay - bday;
                batchDate = DateUtil.addDate(baseDate, addDay);
            }
            else
            {
                // next week
                addDay = 7 - bday + weekDay;
                batchDate = DateUtil.addDate(baseDate, addDay);
            }
        }
        else if (cycle.startsWith(CYCLE_M) && icycle == 99)
        {
            // M99 batchDate = procDate所屬月份的最後一天
            batchDate = baseDate.substring(0, 6) + DateUtil.getLastDayOfMonth(baseDate);
        }
        else if (cycle.startsWith(CYCLE_M) && (icycle >= 1 && icycle <= 28))
        {
            // M00~M28 monthDay只能是1~28
            int bday = DateUtil.getDayOfMonth(baseDate);
            if (bday <= icycle)
            {
                // this month (if today=20061120, monthDay=21 =>
                // batchDate=20061121)
                batchDate = baseDate.substring(0, 6) + String.format("%02d", icycle);
            }
            else
            {
                // next month (if today=20061120, monthDay=19 =>
                // batchDate=20061219)
                batchDate = DateUtil.addMonth(baseDate, 1).substring(0, 6) + String.format("%02d", icycle);
            }
        }
        else if (cycle.startsWith(CYCLE_B))
        {

            int weekDay = icycle;
            int bday = DateUtil.getDayOfWeek(baseDate);
            int addDay = 14 - bday + weekDay;
            batchDate = DateUtil.addDate(baseDate, addDay);
        }

        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select min(BATCH_DATE) from TB_HOLIDAY where BATCH_DATE>=");
        sqlCmd.append(batchDate).append(" and HOLIDAY='0'");

        String settleDate = "";
        try {
            Vector settleDateVtr = (Vector) DbUtil.getInfoListGeneral(sqlCmd.toString(), DbUtil.VECTOR_LIST, conn);
            settleDate = (String) ((Vector) settleDateVtr.get(0)).get(0);
        }
        catch (Exception ignore) {
            log.warn("getCycleDate warn:" + ignore.getMessage(), ignore);
            throw new RuntimeException("getCycleDate warn:" + ignore.getMessage());
        }

        if (StringUtil.isEmpty(settleDate))
        {
            throw new RuntimeException("Result is Empty(TB_HOLIDAY.batchDate).");
        }
        //System.out.println(" settleDate:"+settleDate);
        // logger.debug(cycle+":"+baseDate+"=>"+batchDate+"=>"+settleDate );
        // logger.debug(sqlCmd.toString());

        return settleDate;
    }

    // center
    public static final String UNIT_C = "C";

    // issuer
    public static final String UNIT_I = "I";

    // acquirer
    public static final String UNIT_A = "A";

    // owner
    public static final String UNIT_O = "O";

    // merch
    public static final String UNIT_M = "M";
    
    //bank memId
    public static final String UNIT_U = "U";

    public static boolean isValidUnit(String unit)
    {
        boolean ret = false;
        if (StringUtil.isEmpty(unit))
        {
            ret = false;
        }
        else
        {
            if (unit.equals(UNIT_C) || unit.equals(UNIT_I) || unit.equals(UNIT_A) ||
                unit.equals(UNIT_O) || unit.equals(UNIT_M) || unit.equals(UNIT_U))
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

    public static String getCreditDebitId(Connection conn, String acqMemId, String issMemId, String merchId, String bonusId,
                                          String creditDebitUnit) throws SQLException
    {
        String ret = "";
        if (!isValidUnit(creditDebitUnit))
        {
            return "";
        }
        if (creditDebitUnit.equals(UNIT_C))
        {
            // center
            TbCenterInfo info = getCenterInfo();
            if (info != null)
            {
                ret = info.getCenterId();
            }
        }
        else if (creditDebitUnit.equals(UNIT_I))
        {
            // issuer
            ret = issMemId;
        }
        else if (creditDebitUnit.equals(UNIT_A)||creditDebitUnit.equals(UNIT_U))
        {
            // acquirer
            ret = acqMemId;
        }
        else if (creditDebitUnit.equals(UNIT_O))
        {
            // owner
            TbBonusInfo info = getBonusInfo(conn, bonusId);
            if (info != null)
            {
                ret = info.getMemId();
            }
        }
        else if (creditDebitUnit.equals(UNIT_M))
        {
            // merch
            ret = merchId;
        }
        return ret;
    }


    /**
     * <pre>
     * 依 <em>s, encoding, maxLength</em> 來取得字串
     * 會切斷至中文字的結尾
     * ex:
     * s = "abcd", encoding = "Big5", maxLength = 10, return "abcd"
     * s = "abcd", encoding = "UTF-8", maxLength = 10, return "abcd"
     * s = "中文字" encoding = "Big5", maxLength = 2, return "中"
     * s = "中文字" encoding = "Big5", maxLength = 3, return "中"
     * s = "中文字" encoding = "UTF-8", maxLength = 3, return "中"
     * s = "中文字" encoding = "UTF-8", maxLength = 5, return "中"
     * </pre>
     *
     * @param s         s
     * @param encoding  encoding
     * @param maxLength maxLength
     * @return
     */
    public static String getMaxString(String s, String encoding, int maxLength)
    {
        if (StringUtil.isEmpty(s))
        {
            return s;
        }
        String ret = s;
        try
        {
            byte[] b = s.getBytes(encoding);
            if (b.length <= maxLength)
            {
                ret = s;
            }
            else
            {
                StringBuffer sb = new StringBuffer();
                // reconstruct string and remove last problem char
                String tmp = new String(b, 0, maxLength, encoding);
                for (int i = 0; i < tmp.length(); i++)
                {
                    if (Character.isDefined(tmp.charAt(i)))
                    {
                        if (tmp.charAt(i) < 0xfff0)
                        {
                            sb.append(tmp.charAt(i));
                        }
                    }
                }
                ret = sb.toString();
            }
        }
        catch (Exception ignore)
        {
            ret = s;
            log.warn("getMaxString error:" + ignore.getMessage(), ignore);
        }
        return ret;
    }


    /**
     * @param s
     * @param encoding
     * @param maxLength
     * @param isPendding 是否要補滿空白
     * @return
     */
    public static String getMaxString(String s, String encoding, int maxLength, boolean isPendding)
    {
        String ret = getMaxString(s, encoding, maxLength);
        if (true==isPendding) {
            ret = pendingSpace(ret, maxLength, encoding);
        }
        return ret;
    }


    /**依照encoding計算s的長度，補滿空白
     * @param s
     * @param length
     * @param encoding
     * @return
     */
    public static String pendingSpace(String s, int length, String encoding) {

        int len = length - getStringLen(s, encoding);
        for (int i = 0; i < len; i++) {
            s = s + " ";
        }
        return s;
    }

    /**預設用"Big5"當做編碼方式
     * @param s
     * @param length
     * @return
     */
    public static String pendingSpace(String s, int length) {
        return pendingSpace(s, length, "Big5");
    }

    /**依照encoding計算s的長度
     * @param s
     * @param encoding
     * @return
     */
    public static int getStringLen(String s, String encoding)
    {
        byte[] b = null;
        try
        {
            b = s.getBytes(encoding);
        }
        catch (Exception ignore)
        {
            log.warn("getMaxString error:" + ignore.getMessage(), ignore);
        }

        return b.length;
    }




    /**
     * <pre>
     * 依 <em>cardNo, expiryDate</em> 取得卡種資訊, new 新的 Connection 來處理
     * 成功傳回 TbCardProductInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param cardNo cardNo
     * @param expiryDate expiryDate
     * @return TbCardProductInfo object
     * @throws SQLException SQLException
     */
    public static TbCardProductInfo getCardProduct(String cardNo, String expiryDate) throws SQLException
    {
        Connection conn = null;
        TbCardProductInfo ret = null;
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            ret = getCardProduct(conn, cardNo, expiryDate);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    /**
     * <pre>
     * 依 <em>conn, cardNo, expiryDate</em> 取得卡種資訊
     * 成功傳回 TbCardProductInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param cardNo cardNo
     * @param expiryDate expiryDate
     * @return TbCardProductInfo object
     * @throws SQLException SQLException
     */
    public static TbCardProductInfo getCardProduct(Connection conn, String cardNo, String expiryDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(cardNo)) {
            throw new IllegalArgumentException("cardNo is empty!");
        }
        if (StringUtil.isEmpty(expiryDate)) {
            throw new IllegalArgumentException("expiryDate is empty!");
        }
        Vector results = new Vector();
        StringBuffer where = new StringBuffer();
        where.append("CARD_PRODUCT = (");
        where.append(" SELECT CARD_PRODUCT FROM TB_CARD WHERE ");
        where.append("CARD_NO = " + StringUtil.toSqlValueWithSQuote(cardNo));
        where.append(" AND ");
        where.append("EXPIRY_DATE = " + StringUtil.toSqlValueWithSQuote(expiryDate));
        where.append(")");
        TbCardProductInfo info = null;
        TbCardProductMgr mgr = new TbCardProductMgr(conn);
        mgr.queryMultiple(where.toString(), results);
        if (results.size() == 0) {
            info = null;
        }
        else if (results.size() == 1) {
            info = (TbCardProductInfo) results.get(0);
        }
        else {
            // should not happen here!
            log.warn("getCardProduct has multiple(" + results.size() + ")!");
            info = (TbCardProductInfo) results.get(0);
        }
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, cardProduct</em> 取得卡種資訊
     * 成功傳回 TbCardProductInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param cardProduct cardProduct
     * @return TbCardProductInfo object
     * @throws SQLException SQLException
     */
    public static TbCardProductInfo getCardProduct(Connection conn, String cardProduct) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(cardProduct)) {
            throw new IllegalArgumentException("cardProduct is empty!");
        }
        TbCardProductInfo info = null;
        TbCardProductMgr mgr = new TbCardProductMgr(conn);
        info = mgr.querySingle(cardProduct);
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, merchId, termId</em> 取得端末所屬收單的相關資訊, new 新的 Connection 來處理
     * 成功傳回 TbMemberInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param merchId merchId
     * @param termId termId
     * @return TbMemberInfo object
     * @throws SQLException SQLException
     */
    public static TbMemberInfo getAcquireInfo(Connection conn, String merchId, String termId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(merchId)) {
            throw new IllegalArgumentException("merchId is empty!");
        }
        if (StringUtil.isEmpty(termId)) {
            throw new IllegalArgumentException("termId is empty!");
        }
        Vector results = new Vector();
        StringBuffer where = new StringBuffer();
        where.append("MEM_ID = (");
        where.append(" SELECT MEM_ID FROM TB_TERM WHERE ");
        where.append("MERCH_ID = " + StringUtil.toSqlValueWithSQuote(merchId));
        where.append(" AND ");
        where.append("TERM_ID = " + StringUtil.toSqlValueWithSQuote(termId));
        where.append(")");
        where.append(" AND ");
        where.append("LENGTH(MEM_TYPE) = 5");
        where.append(" AND ");
        where.append("SUBSTR(MEM_TYPE, 2, 1) = '1'");
        TbMemberInfo info = null;
        TbMemberMgr mgr = new TbMemberMgr(conn);
        mgr.queryMultiple(where.toString(), results);
        if (results.size() == 0) {
            info = null;
        }
        else if (results.size() == 1) {
            info = (TbMemberInfo) results.get(0);
        }
        else {
            // should not happen here!
            log.warn("getAcquireInfo has multiple(" + results.size() + ")!");
            info = (TbMemberInfo) results.get(0);
        }
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, cardNo, expiryDate</em> 取得卡片主檔的相關資訊
     * 成功傳回 TbCardInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param cardNo cardNo
     * @param expiryDate expiryDate
     * @return TbCardInfo object
     * @throws SQLException SQLException
     */
    public static TbCardInfo getCardInfo(Connection conn, String cardNo, String expiryDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(cardNo)) {
            throw new IllegalArgumentException("cardNo is empty!");
        }
        if (StringUtil.isEmpty(expiryDate)) {
            throw new IllegalArgumentException("expiryDate is empty!");
        }
        TbCardInfo info = null;
        TbCardMgr mgr = new TbCardMgr(conn);
        TbCardPK pk = new TbCardPK();
        pk.setCardNo(cardNo);
        pk.setExpiryDate(expiryDate);
        info = mgr.querySingle(pk);
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, creditCardNo, creditExpiryDate</em> 取得卡片主檔的相關資訊
     * 成功傳回 TbCardInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param creditCardNo creditCardNo
     * @param creditExpiryDate creditExpiryDate
     * @return TbCardInfo object
     * @throws SQLException SQLException
     */
    public static TbCardInfo getCardInfoByCredit(Connection conn, String creditCardNo, String creditExpiryDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(creditCardNo)) {
            throw new IllegalArgumentException("creditCardNo is empty!");
        }
        if (StringUtil.isEmpty(creditExpiryDate)) {
            throw new IllegalArgumentException("creditExpiryDate is empty!");
        }
        TbCardInfo info = null;
        TbCardMgr mgr = new TbCardMgr(conn);
        Vector results = new Vector();
        StringBuffer where = new StringBuffer();
        where.append("CREDIT_CARD_NO = " + StringUtil.toSqlValueWithSQuote(creditCardNo));
        where.append(" AND ");
        where.append("CREDIT_EXPIRY_DATE = " + StringUtil.toSqlValueWithSQuote(creditExpiryDate));
        mgr.queryMultiple(where.toString(), results);
        if (results.size() == 0) {
            info = null;
        }
        else if (results.size() == 1) {
            info = (TbCardInfo) results.get(0);
        }
        else {
            // should not happen here!
            log.warn("getCardInfoByCredit has multiple(" + results.size() + ")!");
            info = (TbCardInfo) results.get(0);
        }
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, issuerMemId</em> 取得卡片發卡的相關資訊
     * 成功傳回 TbMemberInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param issuerMemId issuerMemId
     * @return TbMemberInfo object
     * @throws SQLException SQLException
     */
    public static TbMemberInfo getIssuerInfo(Connection conn, String issuerMemId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(issuerMemId)) {
            throw new IllegalArgumentException("issuerMemId is empty!");
        }
        Vector results = new Vector();
        StringBuffer where = new StringBuffer();
        where.append("MEM_ID = " + StringUtil.toSqlValueWithSQuote(issuerMemId));
        where.append(" AND ");
        where.append("LENGTH(MEM_TYPE) = 5");
        where.append(" AND ");
        where.append("SUBSTR(MEM_TYPE, 1, 1) = '1'");
        TbMemberInfo info = null;
        TbMemberMgr mgr = new TbMemberMgr(conn);
        mgr.queryMultiple(where.toString(), results);
        if (results.size() == 0) {
            info = null;
        }
        else if (results.size() == 1) {
            info = (TbMemberInfo) results.get(0);
        }
        else {
            // should not happen here!
            log.warn("getIssuerInfo has multiple(" + results.size() + ")!");
            info = (TbMemberInfo) results.get(0);
        }
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, regionId, memId, custId</em> 取得客戶主檔的相關資訊
     * 成功傳回 CustAssociatorInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param regionId regionId
     * @param memId memId
     * @param custId custId
     * @return CustAssociatorInfo object
     * @throws SQLException SQLException
     */
    public static CustAssociatorInfo getCustAssociatorInfo(Connection conn, String regionId, String memId, String custId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(regionId)) {
            throw new IllegalArgumentException("regionId is empty!");
        }
        if (StringUtil.isEmpty(memId)) {
            throw new IllegalArgumentException("memId is empty!");
        }
        if (StringUtil.isEmpty(custId)) {
            throw new IllegalArgumentException("custId is empty!");
        }
        // TB_CUST
        Vector results1 = new Vector();
        StringBuffer where1 = new StringBuffer();
        where1.append("REGION_ID = " + StringUtil.toSqlValueWithSQuote(regionId));
        where1.append(" AND ");
        where1.append("MEM_ID = " + StringUtil.toSqlValueWithSQuote(memId));
        where1.append(" AND ");
        where1.append("CUST_ID = " + StringUtil.toSqlValueWithSQuote(custId));
        TbCustInfo info1 = null;
        TbCustMgr mgr1 = new TbCustMgr(conn);
        mgr1.queryMultiple(where1.toString(), results1);
        if (results1.size() == 0) {
            info1 = null;
        }
        else if (results1.size() == 1) {
            info1 = (TbCustInfo) results1.get(0);
        }
        else {
            // should not happen here!
            log.warn("getCustAssociatorInfo-TbCustInfo has multiple(" + results1.size() + ")!");
            info1 = (TbCustInfo) results1.get(0);
        }

        if (info1 == null) {
            return null;
        }
        CustAssociatorInfo custInfo = new CustAssociatorInfo();
        custInfo.setCustInfo(info1);
        return custInfo;
    }

    /**
     * <pre>
     * 依 <em>conn, merchId</em> 取得特店群組的相關資訊
     * 成功傳回 List object, each element is TbMerchGroupInfo, 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param merchId merchId
     * @return List object, each element is TbMerchGroupInfo
     * @throws SQLException SQLException
     */
    public static List getMerchGroupInfos(Connection conn, String merchId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(merchId)) {
            throw new IllegalArgumentException("merchId is empty!");
        }
        Vector results = new Vector();
        StringBuffer where = new StringBuffer();
        where.append("MERCH_GROUP_ID IN (");
        where.append(" SELECT MERCH_GROUP_ID FROM TB_MERCH_GROUP_RLN WHERE ");
        where.append("MERCH_ID = " + StringUtil.toSqlValueWithSQuote(merchId));
        where.append(")");
        TbMerchGroupMgr mgr = new TbMerchGroupMgr(conn);
        mgr.queryMultiple(where.toString(), results);
        return results;
    }

    /**
     * <pre>
     * 依 <em>conn, cardNo, expiryDate, bonusBase</em> 取得卡片歸戶方式的相關資訊
     * 成功傳回 TbBonusBase object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param cardNo cardNo
     * @param expiryDate expiryDate
     * @param bonusBase bonusBase
     * @return TbBonusBase object
     * @throws SQLException SQLException
     */
    public static TbBonusBaseInfo getBonusBase(Connection conn, String cardNo, String expiryDate, String bonusBase) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(cardNo)) {
            throw new IllegalArgumentException("cardNo is empty!");
        }
        if (StringUtil.isEmpty(expiryDate)) {
            throw new IllegalArgumentException("expiryDate is empty!");
        }
        if (StringUtil.isEmpty(bonusBase)) {
            throw new IllegalArgumentException("bonusBase is empty!");
        }
        if (!Constants.BONUSBASE_CHIP.equals(bonusBase) && !Constants.BONUSBASE_HOST.equals(bonusBase)) {
            throw new IllegalArgumentException("bonusBase not in {" + Constants.BONUSBASE_CHIP + " | " + Constants.BONUSBASE_HOST + "}!");
        }
        Vector results = new Vector();
        StringBuffer where = new StringBuffer();
        where.append("BONUS_BASE = " + StringUtil.toSqlValueWithSQuote(bonusBase));
        where.append(" AND ");
        where.append("CARD_PRODUCT = (");
        where.append(" SELECT CARD_PRODUCT FROM TB_CARD WHERE ");
        where.append("CARD_NO = " + StringUtil.toSqlValueWithSQuote(cardNo));
        where.append(" AND ");
        where.append("EXPIRY_DATE = " + StringUtil.toSqlValueWithSQuote(expiryDate));
        where.append(")");
        TbBonusBaseInfo info = null;
        TbBonusBaseMgr mgr = new TbBonusBaseMgr(conn);
        mgr.queryMultiple(where.toString(), results);
        if (results.size() == 0) {
            info = null;
        }
        else if (results.size() == 1) {
            info = (TbBonusBaseInfo) results.get(0);
        }
        else {
            // should not happen here!
            log.warn("getBonusBase has multiple(" + results.size() + ")!");
            info = (TbBonusBaseInfo) results.get(0);
        }
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, cardProduct, bonusBase</em> 取得卡片歸戶方式的相關資訊
     * 成功傳回 TbBonusBase object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param cardProduct cardProduct
     * @param bonusBase bonusBase
     * @return TbBonusBase object
     * @throws SQLException SQLException
     */
    public static TbBonusBaseInfo getBonusBase(Connection conn, String cardProduct, String bonusBase) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(cardProduct)) {
            throw new IllegalArgumentException("cardProduct is empty!");
        }
        if (StringUtil.isEmpty(bonusBase)) {
            throw new IllegalArgumentException("bonusBase is empty!");
        }
        if (!Constants.BONUSBASE_CHIP.equals(bonusBase) && !Constants.BONUSBASE_HOST.equals(bonusBase)) {
            throw new IllegalArgumentException("bonusBase not in {" + Constants.BONUSBASE_CHIP + " | " + Constants.BONUSBASE_HOST + "}!");
        }
        TbBonusBaseInfo info = null;
        TbBonusBasePK pk = new TbBonusBasePK();
        pk.setCardProduct(cardProduct);
        pk.setBonusBase(bonusBase);
        TbBonusBaseMgr mgr = new TbBonusBaseMgr(conn);
        info = mgr.querySingle(pk);
        return info;
    }

    public static String pendingKey(long value, int length) {
        String ret = "" + value;
        int len = length - ret.length();
        for (int i = 0; i < len; i++) {
            ret = "0" + ret;
        }
        return ret;
    }

    /**
     * <pre>
     * 依 <em>conn, merchId, termId</em> 取得 onus(N:若在 TB_TERM 找到)
     * 或是 offus (F:若在 TB_TERM 找不到),
     * 成功傳回 "N", "F", 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param merchId merchId
     * @param termId termId
     * @return if onus, return "N", if offus, return "F"
     * @throws SQLException SQLException
     */
    public static String getOnusFlagByTerm(Connection conn, String merchId, String termId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(merchId)) {
            throw new IllegalArgumentException("merchId is empty!");
        }
        if (StringUtil.isEmpty(termId)) {
            throw new IllegalArgumentException("termId is empty!");
        }
        // default is F:offus
        // N:onus, F:offus
        String onusFlag = Constants.ONLINEFLAG_OFFLINE;
        TbTermPK pk = new TbTermPK();
        TbTermMgr mgr = new TbTermMgr(conn);
        pk.setMerchId(merchId);
        pk.setTermId(termId);
        if (mgr.isExist(pk)) {
            onusFlag = Constants.ONLINEFLAG_ONLINE;
        }
        else {
            onusFlag = Constants.ONLINEFLAG_OFFLINE;
        }
        return onusFlag;
    }

    public static boolean isInArray(String data, String[] datas) {
        for (int i = 0; i < datas.length; i++) {
            if (data.equals(datas[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * <pre>
     * 依 <em>batchType, batchNo, batchDate, inctlInfo</em> 來組成一個 TbTermBatchInfo object
     * </pre>
     * @param batchType batchType
     * @param batchNo batchNo
     * @param batchDate batchDate
     * @param inctlInfo inctlInfo
     * @return TbTermBatchInfo object
     */
    public static TbTermBatchInfo initTermBatch(String batchType, String batchNo, String batchDate, TbInctlInfo inctlInfo) {
        if (StringUtil.isEmpty(batchNo)) {
            throw new IllegalArgumentException("batchNo is empty!");
        }
        if (!DateUtil.isValidDate(batchDate)) {
            throw new IllegalArgumentException("batchDate is invalid!");
        }
        if (inctlInfo == null) {
            throw new IllegalArgumentException("inctlInfo is null!");
        }
        TbTermBatchInfo info = new TbTermBatchInfo();
        info.setTxnSrc(batchType);
        info.setMerchId(StringUtil.pendingKey(0, 15));
        info.setTermId(StringUtil.pendingKey(0, 8));
        info.setBatchNo(batchNo);
        info.setTermSettleDate(inctlInfo.getFileDate());
        info.setTermSettleTime("000000");
        info.setCutDate(batchDate);
        info.setCutTime("000000");
        info.setInfile(inctlInfo.getFullFileName());
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, progId</em> 取得此回饋活動 TB_AWARD_PROG 的相關資訊,
     * 成功傳回 TbAwardProgInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param progId progId
     * @return TbAwardProgInfo object
     * @throws SQLException SQLException
     */
    public static TbAwardProgInfo getAwardProg(Connection conn, String progId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(progId)) {
            throw new IllegalArgumentException("progId is empty!");
        }
        TbAwardProgInfo info = null;
        TbAwardProgMgr mgr = new TbAwardProgMgr(conn);
        info = mgr.querySingle(progId);
        return info;
    }

    public static TbAwardProgInfo getAppointAwardProg(Connection conn, String progId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(progId)) {
            throw new IllegalArgumentException("progId is empty!");
        }
        TbAwardProgInfo info = null;
        TbAwardProgMgr mgr = new TbAwardProgMgr(conn);
        StringBuffer where = new StringBuffer();
        where.append("PROG_ID = " + StringUtil.toSqlValueWithSQuote(progId));
        where.append(" AND TXN_SRC = 'L' AND NORMAL_APPOINT = 'A'");
        Vector results = new Vector();
        mgr.queryMultiple(where.toString(), results);
        if (results.size() > 0) {
            info = (TbAwardProgInfo) results.get(0);
        }
        return info;
    }

    /**
     * <pre>
     * 依 <em>conn, progId</em> 取得此回饋活動 TB_AWARD_PROG 的相關資訊,
     * 成功傳回 TbRedeemProgInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param progId progId
     * @return TbRedeemProgInfo object
     * @throws SQLException SQLException
     */
    public static TbRedeemProgInfo getRedeemProg(Connection conn, String progId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(progId)) {
            throw new IllegalArgumentException("progId is empty!");
        }
        TbRedeemProgInfo info = null;
        TbRedeemProgMgr mgr = new TbRedeemProgMgr(conn);
        info = mgr.querySingle(progId);
        return info;
    }

    private static boolean isValidCardNo(String cardNo) {
        String upperCase = cardNo.toUpperCase();
        for (int i = 0; i < cardNo.length(); i++) {
            if (!(upperCase.charAt(i) >= '0' && upperCase.charAt(i) <= 'F')) {
                return false;
            }
        }
        return true;
    }

    public static String invertCombineCardNo(String cardNo) {
        if (StringUtil.isEmpty(cardNo)) {
            throw new IllegalArgumentException("cardNo is empty!");
        }
        if (!isValidCardNo(cardNo)) {
            throw new IllegalArgumentException("cardNo is invalid!");
        }
        StringBuffer sb = new StringBuffer();
        sb.append(cardNo);
        for (int i = 0; i < cardNo.length(); i++) {
            try {
                int tmp = Integer.parseInt("" + cardNo.charAt(i), 16);
                tmp = tmp ^ 0x0000000F;
                sb.append(Integer.toHexString(tmp).toUpperCase());
            }
            catch (Exception ignore) {
                ;
            }
        }
        return sb.toString();
    }

    public static int PADDING_LEFT = 0;
    public static int PADDING_RIGHT = 1;
    public static String getHSMString(String data, int leftRight, char pendingChar) {
        String ret;
        if (data.length() % 8 != 0) {
            int len = ((data.length() / 8) + 1) * 8;
            if (leftRight == PADDING_LEFT) {
                ret = StringUtils.leftPad(data, len, pendingChar);
            }
            else if (leftRight == PADDING_RIGHT) {
                ret = StringUtils.rightPad(data, len, pendingChar);
            }
            else {
                ret = StringUtils.rightPad(data, len, pendingChar);
            }
        }
        else {
            ret = data;
        }
        return ret;
    }

    /**
     * <pre>
     * 依 <em>conn, regionId, custId</em> 取得TB_CUST主檔的相關資訊
     * 成功傳回 TbCustInfo object (maybe null, 不存在的話), 失敗丟 SQLException
     * </pre>
     * @param conn conn
     * @param regionId regionId
     * @param custId custId
     * @param memId MemId
     * @return TbCustInfo object
     * @throws SQLException SQLException
     */
    public static TbCustInfo getCustInfo(Connection conn, String regionId, String custId, String memId) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(regionId) || StringUtil.isEmpty(custId))
        {
            return null;
        }
//在id卡的交易中並不會有TbCustInfo 所以必須回應null
//        if (StringUtil.isEmpty(regionId)) {
//            throw new IllegalArgumentException("regionId is empty!");
//        }
//        if (StringUtil.isEmpty(custId)) {
//            throw new IllegalArgumentException("custId is empty!");
//        }
        TbCustInfo info = null;
        TbCustMgr mgr = new TbCustMgr(conn);
        TbCustPK pk = new TbCustPK();
        pk.setRegionId(regionId);
        pk.setCustId(custId);
        //pk.setMemId(memId);
        info = mgr.querySingle(pk);
        return info;
    }

    public static boolean isTrue(String value)
    {
        boolean ret = false;
        if (StringUtil.isEmpty(value))
        {
            ret = false;
        }
        else
        {
            if ("1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) ||
                    "true".equalsIgnoreCase(value))
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

    public static String getBatchDate() {
        String batchDate = System.getProperty("date");
        if (StringUtil.isEmpty(batchDate)) {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }
        if (!DateUtil.isValidDate(batchDate)) {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }
        return batchDate;
    }

    public static TbTermBatchInfo initTermBatch(String batchType, String batchNo, String batchDate) {
        if (StringUtil.isEmpty(batchNo)) {
            throw new IllegalArgumentException("batchNo is empty!");
        }
        if (!DateUtil.isValidDate(batchDate)) {
            throw new IllegalArgumentException("batchDate is invalid!");
        }
        TbTermBatchInfo info = new TbTermBatchInfo();
        info.setTxnSrc(batchType);
        info.setMerchId(StringUtil.pendingKey(0, 15));
        info.setTermId(StringUtil.pendingKey(0, 8));
        info.setBatchNo(batchNo);
        info.setTermSettleDate(batchDate);
        info.setTermSettleTime("000000");
        info.setCutDate(batchDate);
        info.setCutTime("000000");
        return info;
    }

    private static final String UNKNOWN_MEMBER = "UNKNOWN_MEMBER";
    private static final String UNKNOWN_MERCH = "UNKNOWN_MERCH";
    private static final String UNKNOWN_TERM = "UNKNOWN_TERM";
    private static final String UNKNOWN_CARD_NO = "UNKNOWN_CARD_NO";
    private static final String UNKNOWN_EXPIRY_DATE = "UNKNOWN_EXPIRY_DATE";

    public static String getUnknownMemberId() {
        return getBatchConfig(UNKNOWN_MEMBER);
    }

    public static String getUnknownMerchId() {
        return getBatchConfig(UNKNOWN_MERCH);
    }

    public static String getUnknownTermId() {
        return getBatchConfig(UNKNOWN_TERM);
    }

    public static String getUnknownCardNo() {
        return getBatchConfig(UNKNOWN_CARD_NO);
    }

    public static String getUnknownExpiryDate() {
        return getBatchConfig(UNKNOWN_EXPIRY_DATE);
    }


    //tools of txn_note
    public static String getUpdateTxnNoteSql(String txnNoteHead)
    {
        String TXN_NOTE_PATTERN = "\\["+txnNoteHead+"\\].+\\.";
        return "REGEXP_REPLACE(TXN_NOTE, '" + TXN_NOTE_PATTERN + "', '')";
    }

    //tools of txn_note
    public static String makeTxnNote(String txnNoteHead, String note)
    {
        String time = DateUtil.getTodayString().substring(8,14);
        String formatTime = time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(txnNoteHead).append("]");
        sb.append(formatTime);
        sb.append(note);
        sb.append(".");
        return sb.toString(); // ex: [SETTLE]12:30:05NoProcessed.
    }

    public static String getUnit2TransField(String unit)
    {
        if (unit.equals("M")) {
            return "MERCH_ID";
        } else if (unit.equals("A")) {
            return "ACQ_MEM_ID";
        } else if (unit.equals("I")) {
            return "ISS_MEM_ID";
        } else if (unit.equals("C")){
            return "";
        }

        log.debug("Layer2Util.getUnit2TransField() unit:"+unit+", return null!");
        return null;
    }
    
    public static String makeParMonDayCond(String batchDate, String symbo, boolean isAnd)
    {
    	String isOnlyAll = getBatchConfig("ONLY_ALL_FLAG");
    	String settlePartition = getBatchConfig("SETTLE_PARTITION");
    	return makeParMonDayCond(batchDate, isOnlyAll, settlePartition, symbo, isAnd);
    }

    public static String makeParMonDayCond(String batchDate, String isOnlyAll, String settlePartition, String symbo, boolean isAnd)
    {
        StringBuffer cond = new StringBuffer();
        String mon = batchDate.substring(4,6);
        String day = batchDate.substring(6,8);
        cond.append((isAnd==true)?" and ":" "); //呼叫時決定要不要加" and "
        
        if (isOnlyAll.equals("0")||StringUtil.isEmpty(isOnlyAll)||isOnlyAll==null)
        { // isOnlyAll = 0 只清算今天過檔的交易
            cond.append(symbo).append("PAR_MON='").append(mon).append("' and ");
            cond.append(symbo).append("PAR_DAY='").append(day).append("'");
        }
        else if (isOnlyAll.equals("1"))
        { // isOnlyAll = 1 清算所有已過檔的交易
            //只清算前面n個partition, n定義在tb_sys_config.settle_partition
            int sp = Integer.parseInt(settlePartition);
            if (sp<=0) sp=1;
            if (sp>12) sp=12;
            
            int m = Integer.parseInt(mon);
            int startParMon = m;
            
            //計算從那個月開始
            if (sp <= m) {
                startParMon = m - sp + 1;
            } else {
                startParMon = 12 + (m - sp + 1);
            }
            
            //組成字串: ( par_mon=XX and par_mon=XX and .... )
            cond.append(" ( ");
            for ( int i=0, j=startParMon; i<sp; i++) { 
                cond.append((i==0)?"":" or ");
                if (m==j)cond.append("(");
                cond.append(symbo).append("PAR_MON='").append(StringUtil.pendingKey(j, 2)).append("'");
                if (m==j) cond.append(" and ").append(symbo).append("PAR_DAY<='").append(day).append("'");
                if (m==j)cond.append(")"); 
                j = (j==12) ? 1 : j+1; 
            }
            cond.append(" ) ");            
        }
        
        return cond.toString();
    }
    
    public static String makeParMonDayCondWithAlias(String alias,String batchDate, String symbo, boolean isAnd)
    {
    	String isOnlyAll = getBatchConfig("ONLY_ALL_FLAG");
    	String settlePartition = getBatchConfig("SETTLE_PARTITION");
    	return makeParMonDayCondWithAlias(alias, batchDate, isOnlyAll, settlePartition, symbo, isAnd);
    }

    
    public static String makeParMonDayCondWithAlias(String alias,String batchDate, String isOnlyAll, String settlePartition, String symbo, boolean isAnd)
    {
        StringBuffer cond = new StringBuffer();
        String mon = batchDate.substring(4,6);
        String day = batchDate.substring(6,8);
        cond.append((isAnd==true)?" and ":" "); //呼叫時決定要不要加" and "
        
        if (isOnlyAll.equals("0")||StringUtil.isEmpty(isOnlyAll)||isOnlyAll==null)
        { // isOnlyAll = 0 只清算今天過檔的交易
            cond.append(symbo).append(alias).append("PAR_MON='").append(mon).append("' and ");
            cond.append(symbo).append(alias).append("PAR_DAY='").append(day).append("'");
        }
        else if (isOnlyAll.equals("1"))
        { // isOnlyAll = 1 清算所有已過檔的交易
            //只清算前面n個partition, n定義在tb_sys_config.settle_partition
            int sp = Integer.parseInt(settlePartition);
            if (sp<=0) sp=1;
            if (sp>12) sp=12;
            
            int m = Integer.parseInt(mon);
            int startParMon = m;
            
            //計算從那個月開始
            if (sp <= m) {
                startParMon = m - sp + 1;
            } else {
                startParMon = 12 + (m - sp + 1);
            }
            
            //組成字串: ( par_mon=XX and par_mon=XX and .... )
            cond.append(" ( ");
            for ( int i=0, j=startParMon; i<sp; i++) { 
                cond.append((i==0)?"":" or ");
                if (m==j)cond.append("(");
                cond.append(symbo).append(alias).append("PAR_MON='").append(StringUtil.pendingKey(j, 2)).append("'");
                if (m==j) cond.append(" and ").append(symbo).append(alias).append("PAR_DAY<='").append(day).append("'");
                if (m==j)cond.append(")"); 
                j = (j==12) ? 1 : j+1; 
            }
            cond.append(" ) ");            
        }
        
        return cond.toString();
    }
    
}
