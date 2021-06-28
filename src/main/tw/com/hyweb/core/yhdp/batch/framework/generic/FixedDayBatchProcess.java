/*
 * $Id: FixedDayBatchProcess.java 13925 2009-05-12 03:22:47Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.yhdp.batch.framework.generic;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.yhdp.batch.util.DateUtils;
import tw.com.hyweb.core.yhdp.batch.util.StringUtils;
import tw.com.hyweb.util.string.StringUtil;


/**
 * @author Clare
 * @version $Revision: 13925 $
 */
public class FixedDayBatchProcess extends AbstractBatchBasic
{
    private final static Logger LOGGER = Logger.getLogger(FixedDayBatchProcess.class);

    private final AbstractBatchBasic batchProcess;
    private final List<String> days;

    public FixedDayBatchProcess(AbstractBatchBasic batchProcess, String day)
    {
        this.batchProcess = batchProcess;

        this.days = new ArrayList<String>();
        this.days.add(day);
    }

    public FixedDayBatchProcess(AbstractBatchBasic batchProcess, List<String> days)
    {
        this.batchProcess = batchProcess;
        this.days = days;
    }

    /**
     * 如果batchDate的日是指定執行批次的日才執行
     * 
     * @see tw.com.hyweb.core.spb.batch.framework.IBatchProcess#process(java.lang.String[])
     */
    public void process(String[] arg0) throws Exception
    {
        if (isInDays())
        {
            batchProcess.process(arg0);
        }
        else
        {
            LOGGER.warn("today is not in " + days);
        }
    }

    /**
     * is batch day in specific days
     * 
     * @return
     */
    private boolean isInDays()
    {
        String batchDay = getBatchDay();

        for (String day : days)
        {
            if (batchDay.equals(StringUtils.paddingLeftString(day, '0', 2)))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @return
     */
    private String getBatchDay()
    {
        if (StringUtil.isEmpty(System.getProperty("date")))
        {
            return DateUtils.getSystemDate().substring(6, 8);
        }
        else
        {
            return System.getProperty("date").substring(6, 8);
        }
    }
}
