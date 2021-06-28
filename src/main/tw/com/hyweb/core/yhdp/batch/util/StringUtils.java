/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/21
 */

package tw.com.hyweb.core.yhdp.batch.util;

import java.math.BigDecimal;

import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Clare
 * 
 */
public class StringUtils
{
    /**
     * 在字串左邊補上指定的padding字元到指定的長度
     * 
     * @param value
     * @param padding
     * @param length
     * @return
     */
    public static String paddingLeftString(String value, char padding, int length)
    {
        StringBuilder sb = new StringBuilder(value);

        while (sb.length() < length)
        {
            sb.insert(0, padding);
        }

        return sb.toString();
    }

    /**
     * 在字串右邊補上指定的padding字元到指定的長度
     * 
     * @param value
     * @param padding
     * @param length
     * @return
     */
    public static String paddingRightString(String value, char padding, int length)
    {
        StringBuilder sb = new StringBuilder(value);

        while (sb.length() < length)
        {
            sb.append(padding);
        }

        return sb.toString();
    }

    /**
     * 字串右邊補上空白字元到指定長度
     * 
     * @param value
     * @param length
     * @return
     */
    public static String formatString(String value, int length)
    {
        return paddingRightString(value, ' ', length);
    }

    /**
     * int轉成指定長度的字串，長度不夠左邊補上0
     * 
     * @param value
     * @param length
     * @return
     */
    public static String int2String(Number value, int length)
    {
        return int2String(value.longValue(), length);
    }

    /**
     * int轉成指定長度的字串，長度不夠左邊補上0
     * 
     * @param value
     * @param length
     * @return
     */
    public static String int2String(long value, int length)
    {
        return int2String(Long.toString(value), length);
    }

    /**
     * int轉成指定長度的字串，長度不夠左邊補上0
     * 
     * @param value
     * @param length
     * @return
     */
    public static String int2String(String value, int length)
    {
        return paddingLeftString(value, '0', length);
    }

    /**
     * double轉成指定長度且不帶小數點的字串，長度不夠左邊補0(字串包含小數點後position個位元)
     * 
     * @param value
     * @param position
     * @param length
     * @return
     */
    public static String double2IntString(double value, int position, int length)
    {
        return double2IntString(Double.toString(value), position, length);
    }

    /**
     * double轉成指定長度且不帶小數點的字串，長度不夠左邊補0(字串包含小數點後position個位元)
     * 
     * @param value
     * @param position
     * @param length
     * @return
     */
    public static String double2IntString(Number value, int position, int length)
    {
        return double2IntString(value.toString(), position, length);
    }

    /**
     * double轉成指定長度且不帶小數點的字串，長度不夠左邊補0(字串包含小數點後position個位元)
     * 
     * @param value
     * @param position
     * @param length
     * @return
     */
    public static String double2IntString(String value, int position, int length)
    {
        return int2String(new BigDecimal(value).multiply(new BigDecimal(Math.pow(10, position))).setScale(0, BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString(), length);
    }

    /**
     * 檢查所有字串是否都不是空字串
     * 
     * @param values
     * @return
     */
    public static boolean isStringsNotEmpty(String... values)
    {
        for (String value : values)
        {
            if (StringUtil.isEmpty(value))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * 所有字串是否含有任一空字串
     * 
     * @param values
     * @return
     */
    public static boolean isAnyStringEmpty(String... values)
    {
        for (String value : values)
        {
            if (StringUtil.isEmpty(value))
            {
                return true;
            }
        }

        return false;
    }
}
