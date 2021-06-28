/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/7/9
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.sql.Connection;

/**
 * @author Clare
 * 
 */
public class AdditionalTransactionRemarker implements CutTransactionAction
{
    private final CutTransactionAction action;
    private final CutTransactionAction addition;

    public AdditionalTransactionRemarker(CutTransactionAction action, CutTransactionAction addition)
    {
        this.action = action;
        this.addition = addition;
    }

    /**
     * @param connection
     * @param conditionFields
     * @param conditionValues
     * @param cutDate
     * @param cutTime
     * @throws Exception
     * @see tw.com.hyweb.svc.yhdp.batch.daycut.CutTransactionAction#cutTransaction(java.sql.Connection,
     *      java.lang.String[], java.lang.Object[], java.lang.String,
     *      java.lang.String)
     */
    public void cutTransaction(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        action.cutTransaction(connection, conditionFields, conditionValues, cutDate, cutTime);
    }

    /**
     * @param connection
     * @param conditionFields
     * @param conditionValues
     * @param cutDate
     * @param cutTime
     * @throws Exception
     * @see tw.com.hyweb.svc.yhdp.batch.daycut.CutTransactionAction#remarkFailure(java.sql.Connection,
     *      java.lang.String[], java.lang.Object[], java.lang.String,
     *      java.lang.String)
     */
    public void remarkFailure(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        action.remarkFailure(connection, conditionFields, conditionValues, cutDate, cutTime);
        addition.remarkFailure(connection, conditionFields, conditionValues, cutDate, cutTime);
    }

    /**
     * @param connection
     * @param conditionFields
     * @param conditionValues
     * @param cutDate
     * @param cutTime
     * @throws Exception
     * @see tw.com.hyweb.svc.yhdp.batch.daycut.CutTransactionAction#remarkSuccess(java.sql.Connection,
     *      java.lang.String[], java.lang.Object[], java.lang.String,
     *      java.lang.String)
     */
    public void remarkSuccess(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        action.remarkSuccess(connection, conditionFields, conditionValues, cutDate, cutTime);
        addition.remarkSuccess(connection, conditionFields, conditionValues, cutDate, cutTime);
    }
}
