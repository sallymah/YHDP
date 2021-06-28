/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/17
 */
package tw.com.hyweb.svc.yhdp.batch.impdata;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

/**
 * @author Clare
 * 
 */
public class ConditionCreator
{
    /**
     * 將所有的condition用and連接起來
     * 
     * @param conditions
     * @return
     */
    public static String mergeCondition(String... conditions)
    {
        StringBuilder condition = new StringBuilder();

        for (int i = 0; i < conditions.length; ++i)
        {
            if (i != 0)
            {
                condition.append(" and ");
            }

            condition.append(conditions[i]);
        }

        return condition.toString();
    }

    /**
     * 對字串類型的資料欄位建立in條件
     * 
     * @param field
     * @param values
     * @return
     */
    public static String createInCondition(String field, String[] values)
    {
        StringBuilder condition = new StringBuilder();

        condition.append(field.toUpperCase());
        condition.append(" in (");

        for (int i = 0; i < values.length; ++i)
        {
            condition.append("'");
            condition.append(values[i]);
            condition.append("'");

            if (i != values.length - 1)
            {
                condition.append(',');
            }
        }

        condition.append(")");

        return condition.toString();
    }

    /**
     * 依欄位名稱從bean裡面抓取property組合成=條件
     * 
     * @param bean
     * @param fields
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static String createEqualCondition(Object bean, String[] fields) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        String[] properties = new String[fields.length];

        for (int i = 0; i < fields.length; ++i)
        {
            properties[i] = convert2PropertyName(fields[i]);
        }

        return createCondition(bean, fields, properties);
    }

    /**
     * 依欄位名稱及對應的property名稱從bean裡面抓取property組合成=條件
     * 
     * @param bean
     * @param fields
     * @param properties
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static String createCondition(Object bean, String[] fields, String[] properties) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        StringBuilder condition = new StringBuilder();

        for (int i = 0; i < fields.length; ++i)
        {
            if (i != 0)
            {
                condition.append(" and ");
            }

            condition.append(fields[i].toUpperCase());
            condition.append("='");
            condition.append(BeanUtils.getProperty(bean, properties[i]));
            condition.append("'");
        }

        return condition.toString();
    }

    /**
     * 將field名稱轉成bean裡property的名稱
     * 
     * @param field
     * @return
     */
    private static String convert2PropertyName(String field)
    {
        StringBuilder propertyName = new StringBuilder(field.toLowerCase());

        int underlineIndex;

        while ((underlineIndex = propertyName.indexOf("_")) != -1)
        {
            propertyName.replace(underlineIndex, underlineIndex + 2, propertyName.substring(underlineIndex + 1, underlineIndex + 2).toUpperCase());
        }

        return propertyName.toString();
    }
}
