/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.sql.Connection;

/**
 * @author Clare
 * 
 */
public class CompositeCutTransactionAction implements CutTransactionAction
{
    private final CutTransactionAction[] actions;

    public CompositeCutTransactionAction(CutTransactionAction[] actions)
    {
        this.actions = actions;
    }

    public void cutTransaction(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        for (CutTransactionAction action : actions)
        {
            action.cutTransaction(connection, conditionFields, conditionValues, cutDate, cutTime);
        }
    }

    public void remarkSuccess(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        for (CutTransactionAction action : actions)
        {
            action.remarkSuccess(connection, conditionFields, conditionValues, cutDate, cutTime);
        }
    }

    public void remarkFailure(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception
    {
        for (CutTransactionAction action : actions)
        {
            action.remarkFailure(connection, conditionFields, conditionValues, cutDate, cutTime);
        }
    }
}
