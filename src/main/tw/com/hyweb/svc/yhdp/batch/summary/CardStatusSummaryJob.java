/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/6/17
 */
package tw.com.hyweb.svc.yhdp.batch.summary; 

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.util.date.DateUtil;

/**
 * @author Clare
 * 
 */
public class CardStatusSummaryJob extends GenericBatchJob
{
    private final String memberId;
    private final String cardProduct;

    /**
     * @param memberId
     * @param cardProduct
     */
    public CardStatusSummaryJob(String memberId, String cardProduct)
    {
        this.memberId = memberId;
        this.cardProduct = cardProduct;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java
     * .sql.Connection, java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
        Object[] values = getParameterValues(batchDate, getStallDate(connection, batchDate));
        
        executeUpdate(connection, getSQL(), values);
    }

    /**
     * @return
     */
    private String getSQL()
    {
        String newOngoingAmt = "case when FIRST_TXN_DATE=? and STATUS='3' then 1 else 0 end NEW_ONGOING_AMT";
        String inActivateAmt = "case when STATUS_UPDATE_DATE=? and STATUS='9' then 1 else 0 end INACTIVATE_AMT";
        String deActivateAmt = "case when ACTIVE_FLAG='N' and INACTIVE_DATE=? then 1 else 0 end DEACTIVATE_AMT";
        String activateAmt = "case when LAST_TXN_DATE is not null and LAST_TXN_DATE>=? and STATUS='3' then 1 else 0 end ACTIVATE_AMT";
        String stallAmt = "case when ((LAST_TXN_DATE is null and ACTIVE_DATE<?) or (LAST_TXN_DATE is not null and LAST_TXN_DATE<?)) and STATUS='3' then 1 else 0 end STALL_AMT";

        String[] columns = { "NEW_ONGOING_AMT", "INACTIVATE_AMT", "DEACTIVATE_AMT", "ACTIVATE_AMT", "STALL_AMT" };
        String[] cases = { newOngoingAmt, inActivateAmt, deActivateAmt, activateAmt, stallAmt };

        return "insert into TB_CARD_STATUS_SUM(PROC_DATE,MEM_ID,CARD_PRODUCT,NEW_ONGOING_AMT,INACTIVATE_AMT,DEACTIVATE_AMT,ACTIVATE_AMT,STALL_AMT) " + getSummarySQL(columns, getSelectSQL(cases));
    }

    /**
     * @param batchDate
     * @param cardClass
     * @param stallDate
     * @return
     */
    private Object[] getParameterValues(String batchDate, String stallDate)
    {
    	String lastBatchDate = DateUtil.addDate(batchDate, -1);
    	
        Object[] values = new Object[10];
        values[0] = batchDate;
        values[1] = memberId;
        values[2] = cardProduct;
        values[3] = lastBatchDate;
        values[4] = lastBatchDate;
        values[5] = lastBatchDate;
        values[6] = stallDate;
        values[7] = stallDate;
        values[8] = stallDate;
        values[9] = cardProduct;

        return values;
    }

    /**
     * @param columns
     * @param select
     */
    private String getSummarySQL(String[] columns, String select)
    {
        StringBuilder summary = new StringBuilder();

        summary.append("select ");
        summary.append("?,?,?,");

        for (int i = 0; i < columns.length; ++i)
        {
            summary.append("case when sum(" + columns[i] + ") is not null then sum(" + columns[i] + ") else 0 end");

            if (i != columns.length - 1)
            {
                summary.append(",");
            }
            else
            {
                summary.append(" ");
            }
        }

        summary.append("from (" + select + ")");

        return summary.toString();
    }

    /**
     * @param cases
     * @return
     */
    private String getSelectSQL(String[] cases)
    {
        StringBuilder select = new StringBuilder();

        select.append("select ");

        for (int i = 0; i < cases.length; ++i)
        {
            select.append(cases[i]);

            if (i != cases.length - 1)
            {
                select.append(",");
            }
            else
            {
                select.append(" ");
            }
        }

        select.append("from TB_CARD where CARD_PRODUCT=?");

        return select.toString();
    }

    /**
     * @param connection
     * @param batchDate
     * @return
     * @throws SQLException
     * @throws ParseException
     */
    private String getStallDate(Connection connection, String batchDate) throws SQLException, ParseException
    {
        return DateUtils.getPreviousMonthDate(batchDate, Integer.parseInt(Layer2Util.getBatchConfig("TOBE_STALL_MONTHS")));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "card status summary job(mem_id:" + memberId + ", card_product:" + cardProduct + ")";
    }
}
