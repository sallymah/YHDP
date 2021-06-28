/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/6/3
 */
package tw.com.hyweb.core.yhdp.batch.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Clare
 * 
 */
public class DateUtils
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("yyyyMM");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss");
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MM");
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd");

    /**
     * 取回系統日期，格式:yyyyMMdd
     * 
     * @return
     */
    public static String getSystemDate()
    {
        return DATE_FORMAT.format(new GregorianCalendar().getTime());
    }

    /**
     * 取回系統時間，格式:HHmmss
     * 
     * @return
     */
    public static String getSystemTime()
    {
        return TIME_FORMAT.format(new GregorianCalendar().getTime());
    }

    /**
     * 取回明天的日期，格式:yyyyMMdd
     * 
     * @return
     * @throws ParseException
     */
    public static String getNextDate() throws ParseException
    {
        return getNextDate(DateUtils.getSystemDate(), 1);
    }

    /**
     * 取回下N天的系統日期，格式：yyMMdd
     * 
     * @param n
     * @return
     * @throws ParseException
     */
    public static String getNextDate(int n) throws ParseException
    {
        return getNextDate(DateUtils.getSystemDate(), n);
    }

    /**
     * 取回指定日期的下N天日期，格式：yyyyMMdd
     * 
     * @param date
     * @param n
     * @return
     * @throws ParseException
     */
    public static String getNextDate(String date, int n) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DATE_FORMAT.parse(date));
        calendar.add(Calendar.DAY_OF_YEAR, n);

        return DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * 取回昨天的系統日期，格式:yyyyMMdd
     * 
     * @return
     * @throws ParseException
     */
    public static String getPreviousDate() throws ParseException
    {
        return getPreviousDate(DateUtils.getSystemDate(), 1);
    }

    /**
     * 取回前N天的系統日期，格式:yyyyMMdd
     * 
     * @return
     * @throws ParseException
     */
    public static String getPreviousDate(int n) throws ParseException
    {
        return getPreviousDate(DateUtils.getSystemDate(), n);
    }

    /**
     * 取回指定日期的前N天日期，格式：yyyyMMdd
     * 
     * @param date
     * @param n
     * @return
     * @throws ParseException
     */
    public static String getPreviousDate(String date, int n) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DATE_FORMAT.parse(date));
        calendar.add(Calendar.DAY_OF_YEAR, -n);

        return DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * 取得上一個月的日期，格式：yyyyMMdd
     * 
     * @param yearMonth
     * @return
     * @throws ParseException
     */
    public static String getPreviousMonthDate(String date) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DATE_FORMAT.parse(date));
        calendar.add(Calendar.MONTH, -1);

        return DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * 取得前幾個月的日期，格式：yyyyMMdd
     * 
     * @param yearMonth
     * @return
     * @throws ParseException
     */
    public static String getPreviousMonthDate(String date, int months) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DATE_FORMAT.parse(date));
        calendar.add(Calendar.MONTH, -months);

        return DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * 取回系統日期的月份，格式:MM
     * 
     * @return
     */
    public static String getSystemMonth()
    {
        return MONTH_FORMAT.format(new GregorianCalendar().getTime());
    }

    /**
     * 取回系統日期的日，格式:dd
     * 
     * @return
     */
    public static String getSystemDay()
    {
        return DAY_FORMAT.format(new GregorianCalendar().getTime());
    }

    /**
     * 取得下一個月份，格式：yyyymm
     * 
     * @param yearMonth
     * @return
     * @throws ParseException
     */
    public static String getNextMonth(String yearMonth) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(YEAR_MONTH_FORMAT.parse(yearMonth));
        calendar.add(Calendar.MONTH, 1);

        return YEAR_MONTH_FORMAT.format(calendar.getTime());
    }

    /**
     * 取得上一個月份，格式：yyyymm
     * 
     * @param yearMonth
     * @return
     * @throws ParseException
     */
    public static String getPreviousMonth(String yearMonth) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(YEAR_MONTH_FORMAT.parse(yearMonth));
        calendar.add(Calendar.MONTH, -1);

        return YEAR_MONTH_FORMAT.format(calendar.getTime());
    }

    /**
     * 取得前幾個月份，格式：yyyymm
     * 
     * @param yearMonth
     * @return
     * @throws ParseException
     */
    public static String getPreviousMonth(String yearMonth, int months) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(YEAR_MONTH_FORMAT.parse(yearMonth));
        calendar.add(Calendar.MONTH, -months);

        return YEAR_MONTH_FORMAT.format(calendar.getTime());
    }

    /**
     * 取得此月的第一天日期，格式：yyyymmdd
     * 
     * @param yearMonth
     * @return
     * @throws ParseException
     */
    public static String getFirstDayOfMonth(String yearMonth) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(YEAR_MONTH_FORMAT.parse(yearMonth));

        return DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * 取得此月的最後一天日期，格式：yyyymmdd
     * 
     * @param yearMonth
     * @return
     * @throws ParseException
     */
    public static String getLastDayOfMonth(String yearMonth) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(YEAR_MONTH_FORMAT.parse(yearMonth));
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        return DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * 判斷該日期是否合法
     * 
     * @param date
     * @return
     */
    public static boolean isValidDate(String date)
    {
        try
        {
            return date.equals(DATE_FORMAT.format(DATE_FORMAT.parse(date)));
        }
        catch (ParseException e)
        {
            return false;
        }
    }
}
