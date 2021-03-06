/**
 * changelog
 * --------------------
 * 20090606
 * duncan
 * 從相關路徑讀 batch.properties 時讀不到, 改用 classloader 方式來讀
 * --------------------
 * 20070531
 * duncan
 * batch.properties 加上 impfiles.checkemptyfile 設定
 * impfiles.checkemptyfile default is false
 * --------------------
 * 20070529
 * duncan
 * batch.properties 加上 impfiles.percommit.records, impfiles.percommit.sleep 設定
 * impfiles.percommit.records default is 1000
 * impfiles.percommit.sleep default is 500
 * --------------------
 * 20070409
 * add dbUser parameter, for other users, like campaign, onlines, default is batch
 * available users
 *     public static String DBUSER_AP = "ap";
 *     public static String DBUSER_OWNER = "owner";
 *     public static String DBUSER_ONLINES = "onlines";
 *     public static String DBUSER_BATCH = "batch";
 *     public static String DBUSER_UI = "ui";
 *     public static String DBUSER_REPORT = "report";
 *     public static String DBUSER_KEYY = "keyy";
 *     public static String DBUSER_TRUNC = "trunc";
 *     public static String DBUSER_CAMPAIGN = "campaign";
 *
 * getInfoList(String sql)
 * getInfoList(String sql, String dbUser)
 * getInfoListHashMap(String sql)
 * getInfoListHashMap(String sql, String dbUser)
 * --------------------
 */
/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */
/*
 * (版權及授權描述)
 *
 * Copyright 2006 (C) Hyweb Technology Co., Ltd. All Rights Reserved.
 *
 * $History: $
 */
package tw.com.hyweb.core.yhdp.batch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
 
import tw.com.hyweb.core.yhdp.batch.framework.BatchException;
import tw.com.hyweb.core.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.DisposeUtil;

/**
 * 一般共用功能<br>
 * <br>
 * Serve common utility for batch processing<br>
 * 
 * @author Tracy
 */
public class BatchUtil
{
    public static final String PROPERTY_FILE = "config" + File.separator + "batch.properties";

    public static final Logger logger = Logger.getLogger(BatchUtil.class);

    private static final Logger apLogger = Logger.getLogger("batchApEvent");

    private static final Logger noticeLogger = Logger.getLogger("batchNoticeEvent");

    private static final Logger detailLogger = Logger.getLogger("batchDetailEvent");

    public static DBService dbs = DBService.getDBService();

    private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };
    private static final ThreadLocal<DateFormat> dateTimeFormat = new ThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmm");
        }
    };
    private static final ThreadLocal<DateFormat> timeFormat = new ThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HHmmss");
        }
    };
    private static final ThreadLocal<DateFormat> dateTimeLogFormat = new ThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
    };

    public static Calendar calendar = null;

    public static String sysDay = "";

    public static String sysTime = "";

    public static String dateTimeLog = "";

    public static long dateTime;

    public static String yesterday = "";

    public static String startDate = "";

    public static String startTime = "";

    public static String workDirectory;

    public static String tempDirectory;

    public static String ftpDirectory;

    public static String programName = null;

    private static final String IMPFILES_PERCOMMIT_RECORDS = "impfiles.percommit.records";
    private static final String IMPFILES_PERCOMMIT_SLEEP = "impfiles.percommit.sleep";
    private static final String IMPFILES_CHECKEMPTYFILE = "impfiles.checkemptyfile";
    private static int recordsPerCommit = -1;
    private static int sleepPerCommit = -1;
    private static boolean checkEmptyFile = false;

    /**
     * Creates a new BatchUtil object<br>
     */
    public BatchUtil()
    {
    }

    /**
     * 設定Main所在之程式名稱<br>
     * Set program name that contains "main"<br>
     * 
     * @param name 程式名稱
     */
    public static void setProgramName(String name)
    {
        programName = name;
    }

    /**
     * 取得Main所在之程式名稱<br>
     * Get program name that contains "main"<br>
     * 
     * @return 程式名稱
     */
    public static String getProgramName()
    {
        return programName;
    }

    /**
     * 取得WORK目錄位置<br>
     * Get WORK root path<br>
     * 
     * @return WORK目錄路徑
     */
    public static String getWorkDirectory() throws Exception
    {
        if (workDirectory == null)
        {
            loadProperty();
        }

        return workDirectory;
    }

    /**
     * 取得TEMP目錄位置<br>
     * Get TEMP root path<br>
     * 
     * @return TEMP目錄路徑
     */
    public static String getTempDirectory() throws Exception
    {
        if (tempDirectory == null)
        {
            loadProperty();
        }

        return tempDirectory;
    }

    /**
     * 取得FTPServer目錄位置<br>
     * Get FTP Server root path<br>
     * 
     * @return FTPServer目錄路徑
     */
    public static String getFTPDirectory() throws Exception
    {
        if (ftpDirectory == null)
        {
            loadProperty();
        }

        return ftpDirectory;
    }

    /**
     * 取得batch.properties設定<br>
     * Load setting from batch.properties<br>
     */
    private static void loadProperty() throws Exception
    {
        InputStream fis = null;
        Properties props = null;

        try
        {
            fis = new FileInputStream(PROPERTY_FILE);
        }
        catch (Exception ignore) {
            fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTY_FILE.replace('\\', '/'));
        }
        try {
            props = new Properties();
            props.load(fis);
            workDirectory = props.getProperty("directory.work");
            tempDirectory = props.getProperty("directory.temp");
            ftpDirectory = props.getProperty("directory.ftpserver");
            try {
                recordsPerCommit = Integer.parseInt(props.getProperty(IMPFILES_PERCOMMIT_RECORDS, "1000"));
                sleepPerCommit = Integer.parseInt(props.getProperty(IMPFILES_PERCOMMIT_SLEEP, "500"));
                String temp = props.getProperty(IMPFILES_CHECKEMPTYFILE, "false");
                if ("true".equalsIgnoreCase(temp) ||
                        "1".equalsIgnoreCase(temp) ||
                        "yes".equalsIgnoreCase(temp)) {
                    checkEmptyFile = true;
                }
                else {
                    checkEmptyFile = false;
                }
            }
            catch (Exception ignore) {
                // using default
                recordsPerCommit = 1000;
                sleepPerCommit = 500;
                checkEmptyFile = false;
            }
        }
        catch (Exception e)
        {
            BatchUtil.eventNotice(getLogger(), "Load " + PROPERTY_FILE + " error! " + e);
            workDirectory = "";
            tempDirectory = "";
            ftpDirectory = "";
            throw new Exception("Load " + PROPERTY_FILE + " error! " + e);
        }
        finally
        {
            DisposeUtil.close(fis);
        }
    }

    /**
     * 記錄Ap Log<br>
     * Write and publish event log that is AP level<br>
     * 
     * @param batchLogger logger
     * @param isSuccess 程式執行結果
     * @param eString Log內容
     */
    public static void eventAP(Logger batchLogger, int step, boolean isSuccess, String eString)
    {
        getNow();

        if (step == 0)
        {
            String log = "[" + dateTimeLog + "][AP][" + programName + "] Status: Start";
            getApLogger().info(log);
            batchLogger.info(log);
        }
        else
        {
            String result = (isSuccess) ? "Complete" : "Error";
            String log = "[" + dateTimeLog + "][AP][" + programName + "] Status: " + result + " " + eString;
            getApLogger().info(log);
            batchLogger.info(log);
        }
    }

    /**
     * 記錄Notice Log<br>
     * Write and publish event log that is Notice level<br>
     * 
     * @param batchLogger logger
     * @param eString Log內容
     */
    public static void eventNotice(Logger batchLogger, String eString)
    {
        getNow();

        String log = "[" + dateTimeLog + "][NOTICE][" + programName + "] " + eString;
        getNoticeLogger().warn(log);
        batchLogger.warn(log);
    }

    /**
     * 記錄Detail Log<br>
     * Write and publish event log that is Detail level<br>
     * 
     * @param batchLogger
     * @param eString
     */
    public static void eventDetail(Logger batchLogger, String eString)
    {
        getNow();

        String log = "[" + dateTimeLog + "][DETAIL][" + programName + "] " + eString;
        getDetailLogger().info(log);
        batchLogger.info(log);
    }

    /**
     * 取得目前系統日期/時間<br>
     * Get system date/time now<br>
     */
    public static void getNow()
    {
        calendar = Calendar.getInstance();
        dateTime = Long.parseLong(dateTimeFormat.get().format(calendar.getTime()));
        sysDay = dateFormat.get().format(calendar.getTime());
        sysTime = timeFormat.get().format(calendar.getTime());
        dateTimeLog = dateTimeLogFormat.get().format(calendar.getTime()); // for
                                                                    // monitor
                                                                    // log
        calendar.add(Calendar.DATE, -1);
        yesterday = dateFormat.get().format(calendar.getTime());
    }

    /**
     * 取得基準日的前幾天/後幾天的日期<br>
     * Get the date of past/later N days from base date<br>
     * 
     * @param baseDay 基準日
     * @param addDays 位移天數
     * @return String 某日YYYYMMDD
     */
    public static String getSomeDay(String baseDay, int addDays)
    {
        String someDay = "";

        try
        {
            Date dStartDate = dateFormat.get().parse(baseDay);
            Calendar calender = Calendar.getInstance();
            calender.setTime(dStartDate);
            calender.add(Calendar.DATE, addDays);
            someDay = dateFormat.get().format(calender.getTime());
        }
        catch (Exception e)
        {
        }

        return someDay;
    }

    /**
     * 取得參考日所屬月分的最後一日<br>
     * Get the last day of the month<br>
     * 
     * example: 傳入"20061120" 傳回"30"<br>
     * 若傳入空值或null，參考日預設為目前系統日<br>
     * 
     * @param date 參考日
     * @return String 某日DD
     */
    public static String getLastDayOfMonth(String date)
    {
        String lastDay = "";
        try
        {
            Calendar calender = Calendar.getInstance();
            if (null==date||"".equals(date))
            {
                date = dateFormat.get().format(calender.getTime());
            }
            calender.setTime(dateFormat.get().parse(date));
            lastDay = String.valueOf(calender.getActualMaximum(Calendar.DAY_OF_MONTH));  
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return lastDay;
    }
    

    /**
     * 取得參考日所屬月分的第幾天<br>
     * Get the last day of the month<br>
     * 
     * example: 傳入"20061101" 傳回"1"<br>
     * 若傳入空值或null，參考日預設為目前系統日<br>
     * 
     * @param date 參考日
     * @return String 第幾日
     */
    public static String getDayOfMonth(String date)
    {
        String day = "";
        try
        {
            Calendar calender = Calendar.getInstance();
            if (null==date||"".equals(date))
            {
                date = dateFormat.get().format(calender.getTime());
            }
            calender.setTime(dateFormat.get().parse(date));
            day = String.valueOf(calender.get(Calendar.DAY_OF_MONTH));  
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return day;
    }

    /**
     * 取得基準日的前幾月/後幾月的日期<br>
     * Get the date of past/later N months from base date<br>
     * 若超過該月的數，傳回該月最後一天(20061031 + 1月 = 20061130)
     * 
     * @param date 基準日
     * @return String 日期YYYYMMDD
     */
    public static String getNextMonthDate(String baseDay, int addMonths)
    {
        String someDay = "";
        Calendar calender = Calendar.getInstance();
        try
        {
            calender.setTime(dateFormat.get().parse(baseDay));
            calender.add(Calendar.MONTH, addMonths);
            someDay = dateFormat.get().format(calender.getTime());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return someDay;
    }
    
    /**
     * 取得參考日是星期幾<br>
     * Get the day of week<br>
     * 
     * example: 
     * 傳入"20061119" 傳回"0"(星期日)<br>
     * 傳入"20061120" 傳回"1"(星期一)<br>
     * 若傳入空值或null，參考日預設為目前系統日<br>
     * 
     * @param date 參考日
     * @return String 星期
     */
    public static String getDayOfWeek(String date)
    {
        String day = "";
        try
        {
            Calendar calender = Calendar.getInstance();
            if (null==date||"".equals(date))
            {
                date = dateFormat.get().format(calender.getTime());
            }
            calender.setTime(dateFormat.get().parse(date));
            day = String.valueOf(calender.get(Calendar.DAY_OF_WEEK)-1);  
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return day;
    }
    
    
    /**
     * 取得民國年月日<br>
     * Get Chinese date<br>
     * 
     * @param christianDate 西元年月日(格式:YYYYMMDD)
     * @return String 民國年月日(格式:YYMMDD)
     */
    public static String getChineseDate(String christianDate)
    {
        String date = christianDate;

        if (checkChristianDate(date))
        {
            if (christianDate.getBytes().length == 8)
            {
                int temp = Integer.parseInt(christianDate) - 19110000;

                if (temp >= 10101)
                {
                    try
                    {
                        date = formatData(String.valueOf(temp), 6, "R", 0);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

        return date;
    }

    /**
     * 取得西元年月日<br>
     * Get Christian date<br>
     * 
     * @param chineseDate 民國年月日(格式:YYMMDD)
     * @return String 西元年月日(格式:YYYYMMDD)
     */
    public static String getChristianDate(String chineseDate)
    {
        String date = chineseDate;

        if (checkChineseDate(date))
        {
            if (chineseDate.getBytes().length == 6)
            {
                int temp = Integer.parseInt(chineseDate) + 19110000;

                if (temp >= 10101)
                {
                    try
                    {
                        date = formatData(String.valueOf(temp), 8, "R", 0);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

        return date;
    }

    /**
     * 檢查數字格式<br>
     * Verify number format<br>
     * 
     * @param str 欲檢查數字格式之字串
     * @return boolean 檢查結果
     */
    public static boolean checkNumber(String str)
    {
        boolean isNumber = false;

        try
        {
            if (Integer.parseInt(str) > 0)
            {
                isNumber = true;
            }
        }
        catch (Exception e)
        {
        }

        return isNumber;
    }

    /**
     * 檢查時間格式<br>
     * Verify time format<br>
     * 
     * @param str 欲檢查時間格式之字串
     * @return boolean 檢查結果
     */
    public static boolean checkTime(String str)
    {
        boolean isTime = false;

        try
        {
            if ((str.length() == 6) && checkNumber(str))
            {
                int hh = Integer.parseInt(str.substring(0, 2));
                int mm = Integer.parseInt(str.substring(2, 4));
                int ss = Integer.parseInt(str.substring(4, 6));

                if (((hh >= 0) && (hh <= 23)) && ((mm >= 0) && (mm <= 59)) && ((ss >= 0) && (ss <= 59)))
                {
                    isTime = true;
                }
            }
        }
        catch (Exception e)
        {
        }

        return isTime;
    }
    

    /**
     * 檢查字串是否為空<br>
     * 若為空字串則回傳true否則為false<br>
     * 
     * @param str 欲檢查時間格式之字串
     * @return boolean 檢查結果
     */
    public static boolean checkStringEmpty(String str)
    {
        return ((str != null) && ! "".equals(str)) ? false : true;
    }
    

    /**
     * 檢查民國年日期格式<br>
     * Verify date format in Chinese<br>
     * 
     * @param str 欲檢查民國年日期格式之字串
     * @return boolean 檢查結果
     */
    public static boolean checkChineseDate(String str)
    {
        boolean isDate = false;

        try
        {
            if ((str.length() == 6) && checkNumber(str))
            {
                int mm = Integer.parseInt(str.substring(2, 4));
                int dd = Integer.parseInt(str.substring(4, 6));

                if (((mm >= 1) && (mm <= 12)) && ((dd >= 1) && (dd <= 31)))
                {
                    isDate = true;
                }
            }
        }
        catch (Exception e)
        {
        }

        return isDate;
    }

    /**
     * 檢查西元年日期格式<br>
     * Verofy date format in Christian<br>
     * 
     * @param str 欲檢查西元年日期格式之字串
     * @return boolean 檢查結果
     */
    public static boolean checkChristianDate(String str)
    {
        boolean isDate = false;

        try
        {
            if ((str.length() == 8) && checkNumber(str))
            {
                int mm = Integer.parseInt(str.substring(4, 6));
                int dd = Integer.parseInt(str.substring(6, 8));

                if (((mm >= 1) && (mm <= 12)) && ((dd >= 1) && (dd <= 31)))
                {
                    isDate = true;
                }
            }
        }
        catch (Exception e)
        {
        }

        return isDate;
    }

    /**
     * 將一天分成多個區段,產生其查詢條件<br>
     * A day will be divided into N time slice for inquiry purpose<br>
     * 
     * @param fieldName
     * @param timeSlice
     * @return LinkedList 查詢條件
     */
    public static LinkedList getTimeSliceCond(String fieldName, int timeSlice)
    {
        LinkedList list = new LinkedList();
        final int endTimeSecs = 86400;

        int newBegin = 0;
        int newEnd = 0;

        while (true)
        {
            newBegin = newEnd;
            newEnd += timeSlice;

            if (newEnd > endTimeSecs)
            {
                break;
            }

            list.add("(" + fieldName + ">='" + BatchUtil.secs2Time(newBegin) + "' and " + fieldName + "<'"
                            + BatchUtil.secs2Time(newEnd) + "')");
        }

        BatchUtil.eventDetail(getLogger(), "divide 24hr data into " + list.size() + " groups (time slice is "
                        + timeSlice + " sec)");

        return list;
    }

    /**
     * 將時間換算成秒數<br>
     * Translate time into N seconds<br>
     * 
     * @param str 時間(格式:HHMMSS)
     * @return int 秒數
     */
    public static int time2Secs(String str)
    {
        int secs = 0;

        try
        {
            if ((str.length() == 6) && checkNumber(str))
            {
                int hh = Integer.parseInt(str.substring(0, 2));
                int mm = Integer.parseInt(str.substring(2, 4));
                int ss = Integer.parseInt(str.substring(4, 6));
                secs = (hh * 60 * 60) + (mm * 60) + ss;
            }
        }
        catch (Exception e)
        {
        }

        return secs;
    }

    /**
     * 將秒數換算成時間<br>
     * Translate N seconds into time<br>
     * 
     * @param secs 秒數
     * @return String 時間(格式:HHMMSS)
     */
    public static String secs2Time(int secs)
    {
        String hhmmss = "";

        int hh = secs / 3600;
        int mm = (secs - (hh * 3600)) / 60;
        int ss = secs - (hh * 3600) - (mm * 60);

        hhmmss = formatNumber(hh, 2) + formatNumber(mm, 2) + formatNumber(ss, 2);

        return hhmmss;
    }

    /**
     * 將傳入的數字格式化 aDigits 位數的字串，不滿位數左補零<br>
     * 若aNumber位數大於aDigits, 直接傳回aNumber<br>
     * Format number<br>
     * 
     * @param aNumber 欲格式化之數字
     * @param aDigits 預期格式化後之長度
     * @return String 長度aDigits之數字
     */
    public static String formatNumber(int aNumber, int aDigits)
    {
        StringBuffer numFormat = new StringBuffer();
        NumberFormat formatter;

        for (int i = 0; i < aDigits; i++)
        {
            numFormat.append("0");
        }

        formatter = new DecimalFormat(numFormat.toString());

        return formatter.format(aNumber);
    }

    /**
     * 取得TB_OUTCTL裡下一個seqno<br>
     * Get next sequence number from TB_OUTCTL<br>
     * 
     * @param memId MEM_ID
     * @param file FILE_NAME
     * @param day FILE_DATE
     * @return int SEQNO
     */
    public static int getOutctlSeqNo(String memId, String file, String day)
    {
        int seqNo = 0;
        String sqlStr = "select SEQNO from TB_OUTCTL " + "where MEM_ID='" + memId + "' " + "and FILE_NAME='" + file
                        + "' " + "and FILE_DATE='" + day + "' " + "and WORK_FLAG='" + Layer1Constants.WORKFLAG_INWORK + "' "
                        + "order by SEQNO desc";

        Vector infoList = getInfoList(sqlStr);

        if (infoList == null)
        {
            seqNo = 1;
        }
        else
        {
            seqNo = Integer.parseInt(((Vector) infoList.get(0)).get(0).toString().trim()) + 1;

            if (seqNo > 99)
            {
                seqNo = 1;
            }
        }

        return seqNo;
    }

    /**
     * 執行SQL指令(不指定Connection)<br>
     * Run SQL command using new connection<br>
     * 
     * @param sqlStr SQL指令
     * @throws Exception
     */
    public static void executeSql(String sqlStr) throws Exception
    {
        Connection conn = null;

        try
        {
            conn = BatchUtil.getConnection();
            dbs.sqlAction(sqlStr, conn);
            conn.commit();
        }
        catch (Exception e)
        {
            throw new Exception("executeSql: fail " + sqlStr);
        }
        finally
        {
            BatchUtil.closeConnection(conn);
        }
    }

    public static final int VECTOR_LIST = 0;
    public static final int HASHMAP_LIST = 1;

    private static String getDBUser(String dbUser) {
        String ret = dbUser;
        // 若是不合法的 dbUser, 預設使用 batch
        if (!DBUSER_AP.equals(dbUser) &&
                !DBUSER_BATCH.equals(dbUser) &&
                !DBUSER_CAMPAIGN.equals(dbUser) &&
                !DBUSER_KEYY.equals(dbUser) &&
                !DBUSER_ONLINES.equals(dbUser) &&
                !DBUSER_OWNER.equals(dbUser) &&
                !DBUSER_REPORT.equals(dbUser) &&
                !DBUSER_TRUNC.equals(dbUser) &&
                !DBUSER_UI.equals(dbUser)) {
            ret = DBUSER_BATCH;
        }
        return ret;
    }

    public static Vector getInfoListGeneral(String sql, int type, String dbUser) {
        Vector infoList = null;

        Connection conn = null;

        try
        {
            // 若是不合法的 dbUser, 預設使用 batch
            conn = DBService.getDBService().getConnection(getDBUser(dbUser));

            SqlResult sqlResult = (SqlResult) dbs.select(sql, conn);
            Hashtable resultInfo = (Hashtable) sqlResult.getResultList();

            if (resultInfo != null)
            {
//                if (sqlResult.getRecordCount() > 0)
//                {
                    if (type == BatchUtil.VECTOR_LIST) {
                        infoList = ((Vector) resultInfo.get(SqlResult.RESULT_VECTOR));
                    }
                    else
                    {
                        infoList = (Vector) resultInfo.get(SqlResult.HASHMAP_RESULT_VECTOR);
                    }
//                }
            }

            resultInfo = null;
        }
        catch (SQLException e)
        {
            throw new BatchException("getInfoList: fail " + e.toString());
        }
        finally
        {
            BatchUtil.closeConnection(conn);
        }

        return infoList;
    }


    public static Vector getInfoListMultiple(String sql, int type, int fetchStart, int fetchSize) {
        Vector infoList = null;

        Connection conn = null;

        try
        {
            conn = BatchUtil.getConnection();

            SqlResult sqlResult = (SqlResult) dbs.selectScrollable(sql, conn, fetchStart, fetchSize);
            Hashtable resultInfo = (Hashtable) sqlResult.getResultList();

            if (resultInfo != null)
            {
                    if (type == BatchUtil.VECTOR_LIST) {
                        infoList = ((Vector) resultInfo.get(SqlResult.RESULT_VECTOR));
                    }
                    else
                    {
                        infoList = (Vector) resultInfo.get(SqlResult.HASHMAP_RESULT_VECTOR);
                    }
            }

            resultInfo = null;
        }
        catch (SQLException e)
        {
            throw new BatchException("getInfoList: fail " + e.toString());
        }
        finally
        {
            BatchUtil.closeConnection(conn);
        }

        return infoList;
    }
    
    public static Vector getInfoListGeneral(String sql, int type, Connection conn) throws SQLException{
        Vector infoList = null;

            SqlResult sqlResult = (SqlResult) dbs.select(sql, conn);
            Hashtable resultInfo = (Hashtable) sqlResult.getResultList();

            if (resultInfo != null)
            {
//                if (sqlResult.getRecordCount() > 0)
//                {
                    if (type == BatchUtil.VECTOR_LIST) {
                        infoList = ((Vector) resultInfo.get(SqlResult.RESULT_VECTOR));
                    }
                    else
                    {
                        infoList = (Vector) resultInfo.get(SqlResult.HASHMAP_RESULT_VECTOR);
                    }
//                }
            }

            resultInfo = null;

        return infoList;
    }
    
    public static Vector getInfoListHashMap(String sql, Connection connection) throws SQLException {
        return getInfoListGeneral(sql, BatchUtil.HASHMAP_LIST, connection);
    }
    
    public static Vector getInfoListHashMap(String sql) {
        return getInfoListHashMap(sql, DBUSER_BATCH);
    }

    public static String DBUSER_AP = "ap";
    public static String DBUSER_OWNER = "owner";
    public static String DBUSER_ONLINES = "onlines";
    public static String DBUSER_BATCH = "batch";
    public static String DBUSER_UI = "ui";
    public static String DBUSER_REPORT = "report";
    public static String DBUSER_KEYY = "keyy";
    public static String DBUSER_TRUNC = "trunc";
    public static String DBUSER_CAMPAIGN = "campaign";

    public static Vector getInfoListHashMap(String sql, String dbUser) {
        return getInfoListGeneral(sql, BatchUtil.HASHMAP_LIST, dbUser);
    }

    /**
     * 取得SQL指令執行結果集<br>
     * Get result set from SQL command<br>
     *
     * @param sqlStr SQL指令
     * @return Vector 結果集
     */
    public static Vector getInfoList(String sqlStr)
    {
        return getInfoList(sqlStr, DBUSER_BATCH);
    }

    public static Vector getInfoList(String sql, String dbUser) {
        return getInfoListGeneral(sql, BatchUtil.VECTOR_LIST, dbUser);
    }
    /**
     * 取得特定檔案的路徑<br>
     * Get file path for specific file<br>
     * 
     * @param fileKeyName FILE_NAME
     * @param ioType TYPE
     * @param memId MEM_ID
     * @return String 檔案的路徑
     * @throws SQLException
     */
    public static String getFileInfoPath(String fileKeyName, String ioType, String memId) throws SQLException
    {
        String path = "";

        String sqlStr = "select LOCAL_PATH from TB_FILE_INFO where FILE_NAME = '" + fileKeyName + "'"
                        + " and IN_OUT = '" + ioType + "'";

       // String[] fileInfos = DbUtil.getRow(sqlStr);
        String[] fileInfos = DbUtil.getRow(sqlStr, getConnection());
        
        if (fileInfos != null)
        {
            if (fileInfos[0] != null)
            {
                path += fileInfos[0];
                // 沒有file_type了
                // if ("B".equalsIgnoreCase(fileInfos[1]))
                // {
                // path = path.replaceFirst("00000000", memId);
                // }
            }
        }

        if (path.equals(""))
        {
            BatchUtil.eventNotice(getLogger(), "getFileInfoPath: fail " + sqlStr);
        }

        return path.toString();
    }

    /**
     * 取得同一類檔案的相關資訊<br>
     * Get file info for specific FILE_TYPE<br>
     * 
     * @throws Exception
     * @return Vector 同一類檔案的相關資訊
     */
    public static Vector getFileInfo(String fileName, String type) throws Exception
    {
        Vector fileInfo = null;

        String whereStr = "";

        if (fileName.trim().length() > 0)
        {
            whereStr += ((whereStr.trim().length() == 0) ? " where " : " and ");
            whereStr += ("file_name in (" + fileName.trim() + ")");
        }

        if (type.trim().length() > 0)
        {
            whereStr += ((whereStr.trim().length() == 0) ? " where " : " and ");
            whereStr += ("IN_OUT in (" + type.trim() + ")");
        }

        String sqlStr = "select FILE_NAME,DATA_LEN,FILE_TYPE " + "from TB_FILE_INFO " + whereStr;

        logger.debug("sqlStr=" + sqlStr);
        Vector infoList = getInfoList(sqlStr);

        if (infoList != null)
        {
            fileInfo = infoList;
        }

        return fileInfo;
    }

    /**
     * Copys a file from source to destination<br>
     * 
     * @param src File
     * @param dst File
     * @throws Exception
     * @return boolean
     */
    public static boolean copyFile(File src, File dst) throws Exception
    {
        boolean isOk = false;
        File newFile = new File(dst, src.getName());
        FileChannel in = null;
        FileChannel out = null;

        String dir = dst.getPath();

        try
        {
            if (new File(dir).exists())
            {
                // do nothing
            }
            else
            {
                if (!new File(dir).mkdirs())
                {
                    throw new Exception("copyFile: fail " + "\nFrom: " + src.getPath() + src.getName() + "\nTo: "
                                    + dst.getPath());
                }
            }

            in = new FileInputStream(src).getChannel();
            out = new FileOutputStream(newFile).getChannel();

            long size = in.size();
            MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
            out.write(buf);
            isOk = true;
        }
        catch (Exception e)
        {
            throw new Exception("copyFile: fail " + "\nFrom: " + src.getPath() + src.getName() + "\nTo: "
                            + dst.getPath());
        }
        finally
        {
            DisposeUtil.close(in);
            DisposeUtil.close(out);
        }

        return isOk;
    }

    /**
     * 格式化字串<br>
     * Format string<br>
     * 
     * @param str Source String
     * @param len Result String Length
     * @param side Source String Side Within Result String. L:left R:right
     * @param pad Source String Padding Within Result String. 0:zero 1:space
     * @return A Well Format String
     */
    public static String formatData(String str, int len, String side, int pad) throws ISOException
    {
        str = str.trim();

        int length = str.getBytes().length;
        String resultStr = ISOUtil.strpad("", len);

        if (length == len)
        {
            resultStr = str;
        }
        else
        {
            if (side.equals("L"))
            {
                if (pad == 1) // 左靠,右補Space
                {
                    if (length > len)
                    {
                        resultStr = str.substring(0, len);
                    }
                    else
                    {
                        resultStr = ISOUtil.strpad(str, len);
                        resultStr = new String(resultStr.getBytes(), 0, len);// 中文佔2bytes
                    }
                }
                else if (pad == 0) // 左靠,右補0
                {
                    if (length > len)
                    {
                        resultStr = str.substring(0, len);
                    }
                    else
                    {
                        resultStr = ISOUtil.zeropadRight(str, len);
                        resultStr = new String(resultStr.getBytes(), 0, len);// 中文佔2bytes
                    }
                }
            }
            else if (side.equals("R"))
            {
                if (pad == 0) // 右靠,左補0
                {
                    if (length > len)
                    {
                        resultStr = str.substring(length - len, length);
                    }
                    else
                    {
                        resultStr = ISOUtil.zeropad(str, len);
                    }
                }
                else if (pad == 1)
                {
                    if (length > len) // 右靠,左補Space
                    {
                        resultStr = str.substring(length - len, length);
                    }
                    else
                    {
                        resultStr = ISOUtil.padleft(str, len, " ".charAt(0));
                    }
                }
            }
        }

        return resultStr;
    }

    /**
     * 格式化字串內容(去除Oracle保留字)<br>
     * Format string to fixed length and replace Oracle reserved word<br>
     * 
     * @param str 原始字串
     * @param len 預期字串長度
     * @return 結果字串
     */
    public static String formatString(String str, int len)
    {
        str = str.trim().replaceAll("'", "`").replaceAll("&", "+");

        byte[] bt = str.getBytes();

        if (len == 0)
        {
            len = bt.length;
        }

        if (bt.length > len)
        {
            str = new String(bt, 0, 200);
        }

        return str;
    }

    /**
     * 格式化TLV欄位值<br>
     * Format TLV<br>
     * 
     * @param byteValue
     * @param len
     * @return TLV value
     * @throws ISOException
     */
    public static String formatTLV(byte[] byteValue, int len) throws ISOException
    {
        String strValue = "";

        if (byteValue == null)
        {
            strValue = "";
        }
        else
        {
            strValue = ISOUtil.hexString(byteValue);
        }

        strValue = formatData(strValue, len, "L", 1);

        return strValue;
    }

    /**
     * new TLVField<br>
     * 
     * @param strTLV String
     * @return TLVField
     */
    /*
     * public static TLVField unpackTLV(String strTLV) throws Exception { byte[]
     * byteTLV = ISOUtil.hex2byte(strTLV); TLVField tg = new TLVField(55);
     * tg.setValue(byteTLV);
     * //System.out.println(ISOUtil.hexString(tg.getFirstTLV(0x91))); return tg; }
     */

    /**
     * Takes a string (or single char) to use as the delimiter and splits the
     * "str" on that delimiter returning a vector similar to Perl's split
     * function<br>
     * 
     * @param splstr String
     * @param str String
     * @return Vector
     */
    public static Vector split(String splstr, String str)
    {
        Vector v = new Vector();
        boolean ok = true;
        int indx1 = 0;
        int indx2 = 0;
        int len = splstr.length();

        str = str.trim();

        while (ok)
        {
            indx2 = str.indexOf(splstr, indx1);

            if (indx2 != -1)
            {
                v.addElement(str.substring(indx1, indx2));
                indx1 = indx2 + len;
            }
            else
            {
                ok = false;
            }
        }

        v.addElement(str.substring(indx1, str.length()));

        return v;
    }

    /**
     * Sub String<br>
     * 
     * @param str string
     * @param from beginIndex - the beginning index, inclusive
     * @param to endIndex - the ending index, exclusive.
     * @return sub string
     */
    public static String subStr(String str, int from, int to)
    {
        if (str != null)
        {
            if (str.length() >= to)
            {
                return str.substring(from, to);
            }
            else
            {
                return str;
            }
        }
        else
        {
            return "";
        }
    }

    /**
     * Sub String<br>
     * 
     * @param str string
     * @param len get string from rigth
     */
    public static String subStrRight(String str, int len)
    {
        String result = "";
        int length = str.trim().getBytes().length;

        if (length > len)
        {
            result = str.substring(length - len, length);
        }
        else
        {
            result = str;
        }

        return result;
    }

    /**
     * 取得新Connection<br>
     * Get new connection<br>
     * 
     * @return 新Connection
     */
    public static Connection getConnection()
    {
        Connection conn = null;

        int i = 0;
        for (;;)
        {
            i++;
            try
            {
                conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
                break;
            }
            catch (SQLException e)
            {
                BatchUtil.eventDetail(getLogger(), "getConnection: try " + i + "times");
                if (i == 3)
                {
                    throw new BatchException(e);
                }
            }
        }

        return conn;
    }

    /**
     * 終止Connection<br>
     * Release connection<br>
     * 
     * @param conn Connection
     */
    public static void closeConnection(Connection conn)
    {
        DBService.getDBService().close(conn);
    }

    /**
     * 將Log寫入Monitor Bridge,每支程式執行完畢,必須記錄執行結果<br>
     * Publish log to Monitor<br>
     * 
     * @return Logger
     */
    private static Logger getApLogger()
    {
        return apLogger;
    }

    /**
     * 將Log寫入Monitor Bridge,每支程式執行過程中發生某種問題,必須記錄問題,由人工進行處理<br>
     * Publish log to Monitor<br>
     * 
     * @return Logger
     */
    private static Logger getNoticeLogger()
    {
        return noticeLogger;
    }

    /**
     * 將Log寫入Monitor Bridge,每支程式執行日誌<br>
     * Publish log to Monitor<br>
     * 
     * @return Logger
     */
    private static Logger getDetailLogger()
    {
        return detailLogger;
    }

    /**
     * 將Log寫入本機檔案<br>
     * Write log to local file<br>
     * 
     * @return Logger
     */
    private static Logger getLogger()
    {
        return logger;
    }

    /**
     * Check Has File Or Not<br>
     * 
     * @param path a path that want to check any file under it
     * @return vc
     */
    public static Vector hasFile(String path)
    {
        return hasFile(new File(path));
    }

    /**
     * Check Has File Or Not<br>
     * 
     * @param file a file that want to check any file under it
     * @return vc
     */
    public static Vector hasFile(File file)
    {
        Vector tmp = new Vector();
        tmp.add(file);
        tmp = hasFile(tmp);
        if (tmp.size() > 0)
        {
            BatchUtil.eventNotice(logger, "hasFile:This Folder has " + tmp.size() + " file(s) to process");
        }
        else
        {
            BatchUtil.eventNotice(logger, "hasFile:This Folder has 0 file(s) to process");
        }
        return tmp;
    }

    /**
     * Check Has File Or Not<br>
     * 
     * @param vc File's Set(put File Object into vc)
     * @return vc
     */
    public static Vector hasFile(Vector vc)
    {
        for (int j = 0; j < vc.size(); j++)
        {
            File tmp1 = (File) vc.get(j);

            if (tmp1.isFile())
            {
                logger.debug(" It's a File = " + vc.get(j).toString());
                // BatchUtil.eventDetail(getLogger(), " It's a File = " +
                // vc.get(j).toString());
            }
            else
            {
                logger.debug(" It's a Dir = " + vc.get(j).toString());
                // BatchUtil.eventDetail(getLogger(), " It's a Dir = " +
                // vc.get(j).toString());
                File[] tmp = tmp1.listFiles();
                vc.remove(j);
                if (tmp != null && tmp.length != 0)
                {
                    for (int i = 0; i < tmp.length; i++)
                    {
                        vc.add(tmp[i]);
                    }
                    logger.debug("[After add file]" + vc.toString());
                    // BatchUtil.eventDetail(getLogger(), j + " [After]is a
                    // Dir=" + vc.toString());
                    vc = hasFile(vc);
                }
                else
                {
                    logger.debug("It's null, >>" + vc.toString());
                    // BatchUtil.eventDetail(getLogger(), "is null");
                }
            }
        }
        return vc;
    }

    public static int getRecordsPerCommit() {
        if (recordsPerCommit == -1) {
            try {
                loadProperty();
            }
            catch (Exception ignore) {
                ;
            }
        }
        return recordsPerCommit;
    }

    public static int getSleepPerCommit() {
        if (sleepPerCommit == -1) {
            try {
                loadProperty();
            }
            catch (Exception ignore) {
                ;
            }
        }
        return sleepPerCommit;
    }

    public static boolean isCheckEmptyFile() {
        try {
            loadProperty();
        }
        catch (Exception ignore) {
            ;
        }
        return checkEmptyFile;
    }
}
