/*
 * Version: 2.0.0
 * Date: 2007-01-10
 */

package tw.com.hyweb.core.yhdp.common.misc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateUtils;

/**
 * 一些與日期相關的常用函式
 */
public class DateUtil
{
    /**
     * <pre>
     *  取得目前的系統時間.
     *  回傳格式為: yyyyMMddHHmmss
     *  其中
     *  HH 為 00 &tilde; 23 (24 小時制)
     * </pre>
     * 
     * @return 目前的系統時間 (yyyyMMddHHmmss)
     */
    public static String getTodayString()
    {
        java.util.Date now = new GregorianCalendar().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(now);
    }

    /**
     * <pre>
     *  取得目前的系統時間，其中時分秒皆為0
     *  回傳格式為: yyyyMMdd000000
     *  其中
     *  HH 為 00 &tilde; 23 (24 小時制)
     * </pre>
     * 
     * @return 目前的系統時間 (yyyyMMdd000000)
     */
    public static String getShortTodayString()
    {
        java.util.Date now = new GregorianCalendar().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(now) + "000000";
    }

    /**
     * <pre>
     *  取得目前的系統時間，並依
     * <em>
     * days
     * </em>
     *  來加減.
     *  例:
     *  20031024, days = 1, 回傳 20031025000000
     *  20031024, days = -1, 回傳 20031023000000
     *  回傳的日期格式為: yyyyMMdd000000
     * </pre>
     * 
     * @param days 要加減的天數
     * @return 加減後的日期 (yyyyMMdd000000)
     */
    public static String getShortTodayString(int days)
    {
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(Calendar.DATE, days);
        java.util.Date now = gc.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(now) + "000000";
    }

    /**
     * <pre>
     * 將<em>
     * dateString
     * </em>
     * 依<em>
     * mode
     * </em>
     * 來加減
     * <em>
     * interval
     * </em>
     * <em>
     * dateString
     * </em>
     * 可為
     * <em>
     * yyyyMMdd
     * </em>
     * ,<em>
     * yyyyMMddHHmm
     * </em>
     * ,<em>
     * yyyyMMddHHmmss
     * </em>
     *  或是空字串。若為空字串或其它不屬於上述三種日期格式時，則以現在的日期進行加減，且回傳的日期格式為
     * <em>
     * yyyyMMddHHmmss
     * </em>
     * 。
     * <em>
     * mode
     * </em>
     * 須為
     * <em>
     * Calendar.DATE
     * </em>
     * (加減
     * <em>
     * interval
     * </em>
     *  天）,
     * <em>
     * Calendar.MONTH
     * </em>
     * (加減
     * <em>
     * interval
     * </em>
     *  月）
     *  或
     * <em>
     * Calendar.YEAR
     * </em>
     * (加減
     * <em>
     * interval
     * </em>
     *  年）
     * <em>
     * interval
     * </em>
     *  須為正或負整數，正整數表是要加多少天(或月或年)，負整數則為減
     * </pre>
     * 
     * @param dateString 要被加減的日期
     * @param mode 要加減日期的模式（日、月、年）
     * @param interval 要加減的數量
     * @return 依dateString的格式回傳，若dateString格式不屬於 <em>yyyyMMdd</em>,
     *         <em>yyyyMMddHHmm</em>, <em>yyyyMMddHHmmss</em>這三種， 則回傳格式為<em>yyyyMMddHHmmss</em>
     */
    public static String addDate(String dateString, int mode, int interval)
    {
        GregorianCalendar gc = null;
        SimpleDateFormat sdf = null;
        dateString = dateString.trim();
        switch (dateString.length())
        {
        case 14:
            gc = new GregorianCalendar(Integer.parseInt(dateString.substring(0, 4)), Integer.parseInt(dateString
                            .substring(4, 6)) - 1, Integer.parseInt(dateString.substring(6, 8)), Integer
                            .parseInt(dateString.substring(8, 10)), Integer.parseInt(dateString.substring(10, 12)),
                            Integer.parseInt(dateString.substring(12, 14)));
            gc.add(mode, interval);
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            return sdf.format(gc.getTime());
        case 12:
            gc = new GregorianCalendar(Integer.parseInt(dateString.substring(0, 4)), Integer.parseInt(dateString
                            .substring(4, 6)) - 1, Integer.parseInt(dateString.substring(6, 8)), Integer
                            .parseInt(dateString.substring(8, 10)), Integer.parseInt(dateString.substring(10, 12)));
            gc.add(mode, interval);
            sdf = new SimpleDateFormat("yyyyMMddHHmm");
            return sdf.format(gc.getTime());
        case 8:
            gc = new GregorianCalendar(Integer.parseInt(dateString.substring(0, 4)), Integer.parseInt(dateString
                            .substring(4, 6)) - 1, Integer.parseInt(dateString.substring(6, 8)));
            gc.add(mode, interval);
            sdf = new SimpleDateFormat("yyyyMMdd");
            return sdf.format(gc.getTime());
        default:
            gc = new GregorianCalendar();
            gc.add(mode, interval);
            sdf = new SimpleDateFormat("yyyyMMddHHmm");
            return sdf.format(gc.getTime());
        }
    }

    /**
     * <pre>
     * 將<em>
     * dateString
     * </em>
     * 來加減
     * <em>
     * interval
     * </em>
     * 天<em>
     * dateString
     * </em>
     * 可為
     * <em>
     * yyyyMMdd
     * </em>
     * ,<em>
     * yyyyMMddHHmm
     * </em>
     * ,<em>
     * yyyyMMddHHmmss
     * </em>
     *  或是空字串。若為空字串或其它不屬於上述三種日期格式時，則以現在的日期進行加減，且回傳的日期格式為
     * <em>
     * yyyyMMddHHmmss
     * </em>
     * 。
     * <em>
     * interval
     * </em>
     *  須為正或負整數，正整數表是要加多少天，負整數則為減
     * </pre>
     * 
     * @param dateString 要被加減的日期
     * @param days 要加減的天數
     * @return 依dateString的格式回傳，若dateString格式不屬於 <em>yyyyMMdd</em>,
     *         <em>yyyyMMddHHmm</em>, <em>yyyyMMddHHmmss</em>這三種， 則回傳格式為<em>yyyyMMddHHmmss</em>
     */
    public static String addDate(String dateString, int days)
    {
        return addDate(dateString, Calendar.DATE, days);
    }

    /**
     * <pre>
     * 將<em>
     * dateString
     * </em>
     * 來加減
     * <em>
     * interval
     * </em>
     * 月<em>
     * dateString
     * </em>
     * 可為
     * <em>
     * yyyyMMdd
     * </em>
     * ,<em>
     * yyyyMMddHHmm
     * </em>
     * ,<em>
     * yyyyMMddHHmmss
     * </em>
     *  或是空字串。若為空字串或其它不屬於上述三種日期格式時，則以現在的日期進行加減，且回傳的日期格式為
     * <em>
     * yyyyMMddHHmmss
     * </em>
     * 。
     * <em>
     * interval
     * </em>
     *  須為正或負整數，正整數表是要加多少月，負整數則為減
     * </pre>
     * 
     * @param dateString 要被加減的日期
     * @param months 要加減的月數
     * @return 依dateString的格式回傳，若dateString格式不屬於 <em>yyyyMMdd</em>,
     *         <em>yyyyMMddHHmm</em>, <em>yyyyMMddHHmmss</em>這三種， 則回傳格式為<em>yyyyMMddHHmmss</em>
     */
    public static String addMonth(String dateString, int months)
    {
        return addDate(dateString, Calendar.MONTH, months);
    }

    /**
     * <pre>
     * 將<em>
     * dateString
     * </em>
     * 來加減
     * <em>
     * interval
     * </em>
     * 年<em>
     * dateString
     * </em>
     * 可為
     * <em>
     * yyyyMMdd
     * </em>
     * ,<em>
     * yyyyMMddHHmm
     * </em>
     * ,<em>
     * yyyyMMddHHmmss
     * </em>
     *  或是空字串。若為空字串或其它不屬於上述三種日期格式時，則以現在的日期進行加減，且回傳的日期格式為
     * <em>
     * yyyyMMddHHmmss
     * </em>
     * 。
     * <em>
     * interval
     * </em>
     *  須為正或負整數，正整數表是要加多少年，負整數則為減
     * </pre>
     * 
     * @param dateString 要被加減的日期
     * @param years 要加減的年數
     * @return 依dateString的格式回傳，若dateString格式不屬於 <em>yyyyMMdd</em>,
     *         <em>yyyyMMddHHmm</em>, <em>yyyyMMddHHmmss</em>這三種， 則回傳格式為<em>yyyyMMddHHmmss</em>
     */
    public static String addYear(String dateString, int years)
    {
        return addDate(dateString, Calendar.YEAR, years);
    }

    /**
     * <pre>
     * 把<em>
     * dateString
     * </em>
     *  (格式為 yyyy/MM/dd) 的字串轉換成 yyyyMMdd000000
     * </pre>
     * 
     * @param dateString 輸入的日期字串
     * @return 失敗則傳回 ""
     */
    public static String getShortString(String dateString)
    {
        String ret = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        boolean isOK = false;
        try
        {
            sdf.parse(dateString);
            isOK = true;
        }
        catch (ParseException e)
        {
            isOK = false;
        }
        if (isOK)
        {
            ret = dateString.substring(0, 4) + dateString.substring(5, 7) + dateString.substring(8, 10) + "000000";
        }
        else
        {
            ret = "";
        }
        return ret;
    }

    /**
     * <pre>
     *  取得現在的時間，並以 java.util.Date 回傳
     * </pre>
     * 
     * @return 現在的時間 (java.util.Date)
     */
    public static java.util.Date getTodayDate()
    {
        return new GregorianCalendar().getTime();
    }

    /**
     * <pre>
     * 將含特殊字元的日期字串
     * <em>
     * dateString
     * </em>
     *  如：
     *  (yyyyMMdd | yyyy/MM/dd | yyyy.MM.dd | yyyyMMddHHmmss)，
     *  轉換成 yyyyMMddHHmmss 的格式，若輸入的字串無法轉換時，
     *  則回傳現在的時間。
     * </pre>
     * 
     * @param dateString 含特殊字元的日期字串
     * @return 成功則傳回格式為 yyyyMMddHHmmss 的日期字串；失敗則傳回格式為 yyyyMMddHHmmss 的現在時間
     */
    public static String parseDateString(String dateString)
    {
        String ret = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        if (dateString == null || "".equals(dateString))
        {
            ret = getTodayString();
        }
        else
        {
            dateString = dateString.replaceAll("\\p{Punct}", "");
            dateString = dateString.replaceAll("$0*", "");
            if (dateString.length() == 8)
            {
                dateString += "000000";
            }
            else if (dateString.length() == 14)
            {
                ;
            }
            else
            {
                dateString = "";
            }
            try
            {
                java.util.Date d = sdf.parse(dateString);
                ret = sdf.format(d);
            }
            catch (ParseException e)
            {
                ret = getTodayString();
            }
        }
        return ret;
    }

    /**
     * <pre>
     * 將含特殊字元的日期字串
     * <em>
     * dateString
     * </em>
     *  如：
     *  (yyyyMMdd | yyyy/MM/dd | yyyy.MM.dd | yyyyMMddHHmmss)，
     *  轉換成 yyyyMMddHHmmss 的格式。
     * </pre>
     * 
     * @param dateString 含特殊字元的日期字串
     * @return 成功則傳回格式為 yyyyMMddHHmmss 的日期字串；失敗則傳回 ""
     */
    public static String parseDateString2(String dateString)
    {
        String ret = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        if (dateString == null || "".equals(dateString))
        {
            ret = "";
        }
        else
        {
            dateString = dateString.replaceAll("\\p{Punct}", "");
            if (dateString.length() == 8)
            {
                dateString += "000000";
            }
            else if (dateString.length() == 14)
            {
                ;
            }
            else
            {
                dateString = "";
            }
            try
            {
                java.util.Date d = sdf.parse(dateString);
                ret = sdf.format(d);
            }
            catch (ParseException e)
            {
                ret = "";
            }
        }
        return ret;
    }

    /**
     * <pre>
     * 檢查
     * <em>
     * dateString
     * </em>
     *  是否符合 yyyyMMddHHmmss 的格式
     * </pre>
     * 
     * @param dateString 待檢查的日期字串
     * @return 若符合格式則回傳 <code>true</code>，若不符則回傳 <code>false</code>
     */
    public static boolean isDateString(String dateString)
    {
        return isDateString(dateString, PATTERN_yyyyMMddHHmmss);
    }

    public static final String PATTERN_yyyyMMddHHmmss = "yyyyMMddHHmmss";

    public static final String PATTERN_yyyyMMdd = "yyyyMMdd";

    public static final String PATTERN_HHmmss = "HHmmss";

    public static boolean isDateString(String dateString, String pattern)
    {
        boolean ret = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        if (dateString == null || "".equals(dateString))
        {
            ret = false;
        }
        else
        {
            try
            {
                sdf.parse(dateString);
                ret = true;
            }
            catch (Exception e)
            {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * <pre>
     * 把日期字串
     * <em>
     * dateString
     * </em>
     *  (其格式必須為 yyyyMMddhhmmss) 格式化成
     *  yyyy
     * <em>
     * sep
     * </em>
     * MM
     * <em>
     * sep
     * </em>
     * dd
     *  若
     * <em>
     * dateString
     * </em>
     *  格式不為 yyyyMMddhhmmss 時, 則傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格式化的日期字串
     * @param sep 分隔符號
     * @return 格式化後的字串，但若 <em>dateString</em> 不為 yyyyMMddhhmmss, 則傳回 ""
     */
    public static String formatDate(String dateString, String sep)
    {
        String ret = "";
        if (isDateString(dateString) == false)
        {
            ret = "";
        }
        else
        {
            ret = dateString.substring(0, 4) + sep + dateString.substring(4, 6) + sep + dateString.substring(6, 8);
        }
        return ret;
    }

    /**
     * <pre>
     * 把日期字串
     * <em>
     * dateString
     * </em>
     *  (其格式必須為 yyyyMMddhhmmss) 格式化成
     *  yyyy
     * <em>
     * sep
     * </em>
     * MM
     * <em>
     * sep
     * </em>
     * dd hh:mm:ss
     *  若
     * <em>
     * dateString
     * </em>
     *  格式不為 yyyyMMddhhmmss, 則傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格式化的日期字串
     * @param sep 分隔符號
     * @return 格式化後的字串，但若 <em>dateString</em> 不為 yyyyMMddhhmmss 時, 則傳回 ""
     */
    public static String formatDateTime(String dateString, String sep)
    {
        String ret = "";
        if (isDateString(dateString) == false)
        {
            ret = "";
        }
        else
        {
            ret = formatDate(dateString, sep) + " " + dateString.substring(8, 10) + ":" + dateString.substring(10, 12)
                            + ":" + dateString.substring(12, 14);
        }
        return ret;
    }

    /**
     * <pre>
     * 把<em>
     * dateString
     * </em>
     *  (其格式必須為 yyyyMMddhhmmss) 格式化成
     *  yyyy年MM月dd日
     *  若
     * <em>
     * dateString
     * </em>
     *  格式不為 yyyyMMddhhmmss, 傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格化的日期字串
     * @return 格式化後的日期字串，若 <em>dateString</em> 格式不為 yyyyMMddhhmmss, 傳回 ""
     */
    public static String formatWDate(String dateString)
    {
        String ret = "";
        if (isDateString(dateString) == false)
        {
            ret = "";
        }
        else
        {
            ret = dateString.substring(0, 4) + "年" + dateString.substring(4, 6) + "月" + dateString.substring(6, 8)
                            + "日";
        }
        return ret;
    }

    /**
     * <pre>
     * 把<em>
     * dateString
     * </em>
     *  (其格式必須為 yyyyMMddhhmmss) 格式化成
     *  yyyy年MM月dd日hh時mm分ss秒
     *  若
     * <em>
     * dateString
     * </em>
     *  格式不為 yyyyMMddhhmmss, 傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格式化的日期字串
     * @return 格式化後的日期字串，若 <em>dateString</em> 格式不為 yyyyMMddhhmmss, 傳回 ""
     */
    public static String formatWDateTime(String dateString)
    {
        String ret = "";
        if (isDateString(dateString) == false)
        {
            ret = "";
        }
        else
        {
            ret = formatWDate(dateString) + dateString.substring(8, 10) + "時" + dateString.substring(10, 12) + "分"
                            + dateString.substring(12, 14) + "秒";
        }
        return ret;
    }

    /**
     * <pre>
     * 把<em>
     * dateString
     * </em>
     *  (其格式必須為 yyyyMMddhhmmss) 格式化成
     *  民國 (yyyy - 1911)年MM月dd日
     *  若
     * <em>
     * dateString
     * </em>
     *  格式不為 yyyyMMddhhmmss, 傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格式化的日期字串
     * @return 格式化後的日期字串，若 <em>dateString</em> 格式不為 yyyyMMddhhmmss, 傳回 ""
     */
    public static String formatCDate(String dateString)
    {
        String ret = "";
        try
        {
            if (isDateString(dateString) == false)
            {
                ret = "";
            }
            else
            {
                String yyyy = dateString.substring(0, 4);
                int year = 0;
                year = Integer.parseInt(yyyy);
                year -= 1911;
                ret = "民國" + year + "年" + dateString.substring(4, 6) + "月" + dateString.substring(6, 8) + "日";
            }
        }
        catch (Exception ex)
        {
            ret = "";
        }
        return ret;
    }

    /**
     * <pre>
     * 把<em>
     * dateString
     * </em>
     *  (其格式必須為 yyyyMMddhhmmss) 格式化成
     *  民國 (yyyy - 1911)年MM月dd日hh時mm分ss秒
     *  若
     * <em>
     * dateString
     * </em>
     *  格式不為 yyyyMMddhhmmss, 傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格式化的日期字串
     * @return 格式化後的日期字串，若 <em>dateString</em> 格式不為 yyyyMMddhhmmss, 傳回 ""
     */
    public static String formatCDateTime(String dateString)
    {
        String ret = "";
        try
        {
            if (isDateString(dateString) == false)
            {
                ret = "";
            }
            else
            {
                ret = formatCDate(dateString) + dateString.substring(8, 10) + "時" + dateString.substring(10, 12) + "分"
                                + dateString.substring(12, 14) + "秒";
            }
        }
        catch (Exception ex)
        {
            ret = "";
        }
        return ret;
    }

    /**
     * <pre>
     * 把<em>
     * dateString
     * </em>
     *  (其格式必須為 yyyyMMddhhmmss) 格式化成
     *  (yyyy - 1911)
     * <em>
     * sep
     * </em>
     * MM
     * <em>
     * sep
     * </em>
     * dd
     *  若
     * <em>
     * dateString
     * </em>
     *  格式不為 yyyyMMddhhmmss, 傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格式化的日期字串
     * @return 格式化後的日期字串，若 <em>dateString</em> 格式不為 yyyyMMddhhmmss, 傳回 ""
     */
    public static String formatCDate(String dateString, String sep)
    {
        String ret = "";
        try
        {
            if (isDateString(dateString) == false)
            {
                ret = "";
            }
            else
            {
                String yyyy = dateString.substring(0, 4);
                int year = 0;
                year = Integer.parseInt(yyyy);
                year -= 1911;
                ret = year + sep + dateString.substring(4, 6) + sep + dateString.substring(6, 8);
            }
        }
        catch (Exception ex)
        {
            ret = "";
        }
        return ret;
    }

    /**
     * <pre>
     * 把<em>
     * dateString
     * </em>
     *  (其格式必須為 yyyyMMddhhmmss) 格式化成
     *  (yyyy - 1911)
     * <em>
     * sep
     * </em>
     * MM
     * <em>
     * sep
     * </em>
     * dd hh:mm:ss
     *  若
     * <em>
     * dateString
     * </em>
     *  格式不為 yyyyMMddhhmmss, 傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格式化的日期字串
     * @return 格式化後的日期字串，若 <em>dateString</em> 格式不為 yyyyMMddhhmmss, 傳回 ""
     */
    public static String formatCDateTime(String dateString, String sep)
    {
        String ret = "";
        try
        {
            if (isDateString(dateString) == false)
            {
                ret = "";
            }
            else
            {
                ret = formatCDate(dateString, sep) + " " + dateString.substring(8, 10) + ":"
                                + dateString.substring(10, 12) + ":" + dateString.substring(12, 14);
            }
        }
        catch (Exception ex)
        {
            ret = "";
        }
        return ret;
    }

    /**
     * <pre>
     * 取得目前的系統年份(民國年)
     * </pre>
     * 
     * @return 目前的系統年份(民國年)
     */
    public static String getCYear()
    {
        String ret = "";
        GregorianCalendar now = new GregorianCalendar();
        int year = now.get(Calendar.YEAR);
        year -= 1911;
        if (year >= 100)
        {
            ret = "" + year;
        }
        else
        {
            ret = "0" + year;
        }
        return ret;
    }

    /**
     * <pre>
     * 取得目前的系統年份(西元年)
     * </pre>
     * 
     * @return 目前的系統年份(西元年)
     */
    public static int getYear()
    {
        GregorianCalendar now = new GregorianCalendar();
        int year = now.get(Calendar.YEAR);
        return year;
    }

    /**
     * <pre>
     * 取得目前的系統月份
     * </pre>
     * 
     * @return 目前的系統月份
     */
    public static int getMonth()
    {
        GregorianCalendar now = new GregorianCalendar();
        int month = now.get(Calendar.MONTH);
        return (month + 1);
    }

    /**
     * <pre>
     * 取得目前的系統日
     * </pre>
     * 
     * @return 目前的系統日
     */
    public static int getDay()
    {
        GregorianCalendar now = new GregorianCalendar();
        int day = now.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    /**
     * <pre>
     *  取得目前的系統日期，並以 int 型態回傳，
     *  例：20040101
     * </pre>
     * 
     * @return 目前的系統日期
     */
    public static int getDate()
    {
        GregorianCalendar now = new GregorianCalendar();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int ret = year * 10000 + (month + 1) * 100 + day;
        return ret;
    }

    /**
     * <pre>
     *  取得目前的系統日期，並以 long 型態回傳，
     *  例：20040101010101
     * </pre>
     * 
     * @return 目前的系統日期
     */
    public static long getDateTime()
    {
        String s = getTodayString();
        long ret = 0;
        try
        {
            ret = Long.parseLong(s);
        }
        catch (Exception ignore)
        {
            ret = 0;
        }
        ;
        return ret;
    }

    /**
     * <pre>
     * 把<em>
     * dateString
     * </em>
     *  (格式為 yyyyMMddHHmmss | yyyy/MM/dd | yyyy.MM.dd | yyyyMMdd | cccMMdd | ccMMdd | ccMM | ccc | cc) 的字串
     *  轉換成 yyyyMMddHHmmss, 若 format 不符合, 傳回 &quot;&quot;
     * </pre>
     * 
     * @param dateString 待格式化的日期字串
     * @return 格式化後的日期字串
     */
    public static String convertDateString(String dateString)
    {
        String ret = "";
        dateString = dateString.replaceAll("\\p{Punct}", "");
        int dateInt = 0;
        if (dateString.length() < 8)
        {
            try
            {
                dateInt = Integer.parseInt(dateString);
            }
            catch (Exception ex)
            {
                return ret;
            }
        }
        switch (dateString.length())
        {
        case 14: // yyyyMMddhhmmss
        case 10: // yyyy/MM/dd | yyyy.MM.dd
        case 8: // yyyyMMdd
            ret = parseDateString2(dateString);
            break;
        case 7: // cccMMdd
            ret = Integer.toString(19110000 + dateInt);
            ret = parseDateString2(ret);
            break;
        case 6: // ccMMdd
            ret = Integer.toString(19110000 + dateInt);
            ret = parseDateString2(ret);
            break;
        case 4: // ccMM
            // 補日 (+1)
            ret = Integer.toString(19110000 + dateInt * 100 + 1);
            ret = parseDateString2(ret);
            break;
        case 3: // ccc
        case 2: // cc
            // 補月日 (+101)
            ret = Integer.toString(19110000 + dateInt * 10000 + 101);
            ret = parseDateString2(ret);
            break;
        default:
            ret = "";
        }
        return ret;
    }

    private DateUtil()
    {
    }

    public static final String DF_yyyyMMdd = "yyyyMMdd";

    private static final String[] DF_DEFAULTS = { DF_yyyyMMdd };

    /**
     * <pre>
     *  just call isValidDate(date, DF_DEFAULTS);
     * </pre>
     * 
     * @param date 要檢查的字串
     * @return 合法的日期字串, return true, 不合法的日期字串, return false
     */
    public static boolean isValidDate(String date)
    {
        return isValidDate(date, DF_DEFAULTS);
    }

    /**
     * <pre>
     * 若<em>
     * date
     * </em>
     * 符合
     * <em>
     * patterns
     * </em>
     *  其中一個表示法, return true,
     *  若不符合, return false
     * </pre>
     * 
     * @param date 要檢查的字串
     * @param patterns 日期樣式
     * @return 合法的日期字串, return true, 不合法的日期字串, return false
     */
    public static boolean isValidDate(String date, String[] patterns)
    {
        boolean ret = false;
        java.util.Date d = null;
        try
        {
            d = DateUtils.parseDate(date, patterns);
            // 和原來的字串 check, 因為 java 會自動 convert 算法
            for (int i = 0; i < patterns.length; i++) {
                String tmpdate = date2String(d, patterns[i]);
                if (date.equals(tmpdate)) {
                    ret = true;
                }
            }
        }
        catch (Exception ignore)
        {
            ret = false;
        }
        return ret;
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DF_yyyyMMdd);

    /**
     * <pre>
     *  just call string2Date(date, DF_yyyyMMdd);
     * </pre>
     * 
     * @param date 要轉換的字串
     * @return 成功傳回 java.util.Date object, 失敗傳回 null
     */
    public static java.util.Date string2Date(String date)
    {
        return string2Date(date, DF_yyyyMMdd);
    }

    /**
     * <pre>
     * 若<em>
     * date
     * </em>
     * 可用
     * <em>
     * pattern
     * </em>
     *  轉換的話, 傳回相對應的 java.util.Date object,
     *  失敗傳回 null
     * </pre>
     * 
     * @param date 要轉換的字串
     * @param pattern 所用的日期樣式
     * @return 成功傳回 java.util.Date object, 失敗傳回 null
     */
    public static java.util.Date string2Date(String date, String pattern)
    {
        java.util.Date ret = null;
        try
        {
            if (DF_yyyyMMdd.equals(pattern))
            {
                ret = dateFormat.parse(date);
            }
            else
            {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                ret = sdf.parse(date);
            }
        }
        catch (Exception e)
        {
            ret = null;
        }
        return ret;
    }

    /**
     * <pre>
     *  just call date2String(d, DF_yyyyMMdd);
     * </pre>
     * 
     * @param d 要轉換的日期
     * @return 成功傳回 yyyyMMdd 字串, 失敗傳回 ""
     */
    public static String date2String(java.util.Date d)
    {
        return date2String(d, DF_yyyyMMdd);
    }

    /**
     * <pre>
     * 若<em>
     * d</em>
     * 可用
     * <em>
     * pattern
     * </em>
     *  轉換的話, 傳回相對應樣式的字串,
     *  失敗傳回 &quot;&quot;
     * </pre>
     * 
     * @param d 要轉換的日期
     * @param pattern 所用的日期樣式
     * @return 成功傳回 yyyyMMdd 字串, 失敗傳回 ""
     */
    public static String date2String(java.util.Date d, String pattern)
    {
        String ret = "";
        try
        {
            if (DF_yyyyMMdd.equals(pattern))
            {
                ret = dateFormat.format(d);
            }
            else
            {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                ret = sdf.format(d);
            }
        }
        catch (Exception ignore)
        {
            ret = "";
        }
        return ret;
    }

    /**
     * <pre>
     *  just call getDayOfWeek(date, DF_yyyyMMdd);
     * </pre>
     * 
     * @param date yyyyMMdd 日期表示字串
     * @return 取得 <em>date</em> 是星期幾
     */
    public static int getDayOfWeek(String date)
    {
        return getDayOfWeek(date, DF_yyyyMMdd);
    }

    /**
     * <pre>
     * 依<em>
     * pattern
     * </em>
     * 來取得
     * <em>
     * date
     * </em>
     *  是星期幾
     *  0:星期日
     *  1:星期一
     *  2:星期二
     *  3:星期三
     *  4:星期四
     *  5:星期五
     *  6:星期六
     * </pre>
     * 
     * @param date yyyyMMdd 日期表示字串
     * @return 取得 <em>date</em> 是星期幾
     */
    public static int getDayOfWeek(String date, String pattern)
    {
        int ret = getDayGeneral(date, pattern, Calendar.DAY_OF_WEEK);
        if (ret != -1)
        {
            ret -= 1;
        }
        return ret;
    }

    public static int getDayOfMonth(String date)
    {
        return getDayOfMonth(date, DF_yyyyMMdd);
    }

    public static int getDayOfMonth(String date, String pattern)
    {
        return getDayGeneral(date, pattern, Calendar.DAY_OF_MONTH);
    }

    public static int getDayOfYear(String date) {
        return getDayOfYear(date, DF_yyyyMMdd);
    }
    
    public static int getDayOfYear(String date, String pattern) {
        return getDayGeneral(date, pattern, Calendar.DAY_OF_YEAR);
    }

    private static int getDayGeneral(String date, String pattern, int type)
    {
        int ret = -1;
        if (!isValidDate(date, new String[] { pattern }))
        {
            return -1;
        }
        java.util.Date d = string2Date(date, pattern);
        if (d != null)
        {
            Calendar calender = Calendar.getInstance();
            calender.setTime(d);
            ret = calender.get(type);
        }
        return ret;
    }

    public static int getLastDayOfMonth(String date)
    {
        return getLastDayOfMonth(date, DF_yyyyMMdd);
    }

    public static int getLastDayOfMonth(String date, String pattern)
    {
        return getActualMaximun(date, pattern, Calendar.DAY_OF_MONTH);
    }

    public static String normalizeDate(String date) {
        if (date == null || "".equals(date)) {
            return "";
        }
        if (date.length() == 8) {
            return date;
        }
        String ret = "";
        if (date.length() == 4) {
            String tmp = "20" + date + "01";
            int days = getLastDayOfMonth(tmp);
            String sdays = "";
            if (days < 10) {
                sdays = "0" + days;
            }
            else {
                sdays = "" + days;
            }
//            ret = "20" + date + sdays;
//            20120517 endy 說 改成99991231
            ret = "99" + date + sdays;
        }
        return ret;
    }
    
    private static int getActualMaximun(String date, String pattern, int type)
    {
        int ret = -1;
        if (!isValidDate(date, new String[] { pattern }))
        {
            return -1;
        }
        java.util.Date d = string2Date(date, pattern);
        if (d != null)
        {
            Calendar calender = Calendar.getInstance();
            calender.setTime(d);
            ret = calender.getActualMaximum(type);
        }
        return ret;
    }

    private static int getActualMinimun(String date, String pattern, int type)
    {
        int ret = -1;
        if (!isValidDate(date, new String[] { pattern }))
        {
            return -1;
        }
        java.util.Date d = string2Date(date, pattern);
        if (d != null)
        {
            Calendar calender = Calendar.getInstance();
            calender.setTime(d);
            ret = calender.getActualMinimum(type);
        }
        return ret;
    }
}
