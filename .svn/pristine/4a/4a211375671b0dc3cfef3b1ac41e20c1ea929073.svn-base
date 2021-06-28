/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.columns2String;
import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;
import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.makeColumnsCondition;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Constants;

/**
 * @author Clare
 * 
 */
public class CutSingleTransactionAction implements CutTransactionAction
{
	
	private static final Logger LOGGER = Logger.getLogger(CutSingleTransactionAction.class);
    private final String table;
    private final String tableCondition;

    private List<String> headerDetailMatchFields = Arrays.asList(new String[] { "CARD_NO", "EXPIRY_DATE", "LMS_INVOICE_NO" });
    private List<String> excludeHeaderFields = new ArrayList<String>();
    private List<String> excludeDetailFields = new ArrayList<String>();

    private String dataCondition;

    public CutSingleTransactionAction(String table, String tableCondition, String[] txnSrcs)
    {
        this.table = table;
        this.tableCondition = tableCondition;

        tableCondition = ConditionCreator.mergeCondition(tableCondition, ConditionCreator.createInCondition("TXN_SRC", txnSrcs));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.svc.thig.batch.daycut.CutTransactionAction#cutTransaction
     * (java.sql.Connection, java.lang.String[], java.lang.Object[],
     * java.lang.String, java.lang.String)
     */
    public void cutTransaction(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        dataCondition = getDataCondition(conditionFields);

        insertHeader(connection, conditionFields, conditionValues, cutDate, cutTime, dataCondition);
        insertDetail(connection, cutDate, conditionValues, dataCondition);
    }

    /**
     * @param connection
     * @param conditionFields
     * @param conditionValues
     * @param cutDate
     * @param cutTime
     * @throws SQLException
     */
    private void insertHeader(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime, String dataCondition) throws SQLException
    {
        Object[] parameterValues = new Object[4 + conditionFields.length];
        System.arraycopy(new Object[] { cutDate, cutTime, cutDate.substring(4, 6), cutDate.substring(6, 8) }, 0, parameterValues, 0, 4);
        System.arraycopy(conditionValues, 0, parameterValues, 4, conditionValues.length);
        //LOGGER.info("--------------"+getInsertHeaderSQL(connection, conditionFields, dataCondition));
        executeUpdate(connection, getInsertHeaderSQL(connection, conditionFields, dataCondition), parameterValues);
    }

    /**
     * 傳回insert TB_TRANS的SQL
     * 
     * @param connection
     * @param dataCondition
     * @param cutTime
     * @return
     * @throws SQLException
     */
    private String getInsertHeaderSQL(Connection connection, String[] conditionFields, String dataCondition) throws SQLException
    {
        List<String> cutFields = DayCutUtils.getCutFields(connection, excludeHeaderFields, table, "TB_TRANS");
        String cutFieldsString = columns2String(cutFields);

        return "insert into TB_TRANS(" + cutFieldsString + ",CUT_DATE,CUT_TIME,PAR_MON,PAR_DAY) select " + cutFieldsString + ",?,?,?,? from " + table + " where " + dataCondition;
    }

    /**
     * 根據TB_TRANS的資料逐筆從source detail table過到TB_TRANS_DTL
     * 
     * @param connection
     * @param cutDate
     * @param transInfos
     * @throws SQLException
     */
    private void insertDetail(Connection connection, String cutDate, Object[] values, String dataCondition) throws SQLException
    {
        List<String> cutFields = DayCutUtils.getCutFields(connection, excludeDetailFields, table + "_DTL", "TB_TRANS" + "_DTL");
        String cutFieldsString = columns2String(cutFields);

        executeUpdate(connection, getInsertDetailSQL(cutDate, cutFieldsString, dataCondition), values);
    }

    /**
     * 根據CARD_NO，EXPIRY_DATE，LMS_INVOICE_NO傳回insert TB_TRANS_DTL的SQL
     * 
     * @param connection
     * @param unitCondition
     * @param cutTime
     * @return
     * @throws SQLException
     */
    private String getInsertDetailSQL(String cutDate, String fieldsString, String dataCondition) throws SQLException
    {
        return "insert into TB_TRANS_DTL(" + fieldsString + ",PAR_MON,PAR_DAY,CUT_DATE) select " + fieldsString + ",'" + cutDate.substring(4, 6) + "','" + cutDate.substring(6, 8) + "','" + cutDate + "' from " + table + "_DTL d where exists (select CARD_NO from " + table + " where " + getHeaderDetailMatchCondition() + " and " + dataCondition + ")";
    }

    /**
     * @return
     */
    private String getHeaderDetailMatchCondition()
    {
        StringBuilder matchCondition = new StringBuilder();

        for (int i = 0; i < headerDetailMatchFields.size(); ++i)
        {
            String field = headerDetailMatchFields.get(i);
            
            matchCondition.append(field).append("=d.").append(field);
            matchCondition.append(i != headerDetailMatchFields.size() - 1 ? " and " : "");
        }

        return matchCondition.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.svc.thig.batch.daycut.CutTransactionAction#remarkSuccess(
     * java.sql.Connection, java.lang.String[], java.lang.Object[],
     * java.lang.String, java.lang.String)
     */
    public void remarkSuccess(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        Object[] parameterValues = new Object[3 + conditionFields.length];
        System.arraycopy(new Object[] { cutDate, cutTime, Constants.RCODE_0000_OK }, 0, parameterValues, 0, 3);
        System.arraycopy(conditionValues, 0, parameterValues, 3, conditionValues.length);

        executeUpdate(connection, "update " + table + " set CUT_DATE=?,CUT_TIME=?,CUT_RCODE=? where " + getDataCondition(conditionFields), parameterValues);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.svc.thig.batch.daycut.CutTransactionAction#remarkFailure(
     * java.sql.Connection, java.lang.String[], java.lang.Object[],
     * java.lang.String, java.lang.String)
     */
    public void remarkFailure(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        Object[] parameterValues = new Object[3 + conditionFields.length];
        System.arraycopy(new Object[] { cutDate, cutTime, Constants.RCODE_2100_DayCut_ERR }, 0, parameterValues, 0, 3);
        System.arraycopy(conditionValues, 0, parameterValues, 3, conditionValues.length);

        executeUpdate(connection, "update " + table + " set CUT_DATE=?,CUT_TIME=?,CUT_RCODE=? where " + getDataCondition(conditionFields), parameterValues);
    }

    /**
     * @param conditionFields
     * @return
     */
    private String getDataCondition(String[] conditionFields)
    {
        if (dataCondition == null)
        {
            dataCondition = makeColumnsCondition(conditionFields) + " and " + tableCondition;
        }

        return dataCondition;
    }

    /**
     * @param headerDetailMatchFields
     *            the headerDetailMatchFields to set
     */
    public void setHeaderDetailMatchFields(List<String> headerDetailMatchFields)
    {
        this.headerDetailMatchFields = headerDetailMatchFields;
    }

    /**
     * @param excludeHeaderFields
     *            the excludeHeaderFields to set
     */
    public void setExcludeHeaderFields(List<String> excludeHeaderFields)
    {
        this.excludeHeaderFields = excludeHeaderFields;
    }

    /**
     * @param excludeDetailFields
     *            the excludeDetailFields to set
     */
    public void setExcludeDetailFields(List<String> excludeDetailFields)
    {
        this.excludeDetailFields = excludeDetailFields;
    }
}
