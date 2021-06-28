/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/21
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.getTheSameColumns;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Clare
 * 
 */
public class DayCutUtils
{
    private static final String[] defaultExcludeFields = { "CUT_DATE", "CUT_TIME", "CUT_RCODE", "PAR_MON", "PAR_DAY" };

    private static Map<String[], List<String>> tablesFieldsMap = new HashMap<String[], List<String>>();

    /**
     * 找出source table需要過檔至destination table的欄位名稱 (相同欄位 - 設定排除的相同欄位 -
     * 過檔相關的欄位及PAR_MON、PAR_DAY)
     * 
     * @param connection
     * @param sourceTable
     * @param destinationTable
     * @param excludeFields
     * @return
     * @throws SQLException
     */
    public static List<String> getCutFields(Connection connection, List<String> excludeFields, String... tables) throws SQLException
    {
        List<String> cutFields = new ArrayList<String>();
        cutFields.addAll(getTheSameFields(connection, tables));
        cutFields.removeAll(excludeFields);
        cutFields.removeAll(Arrays.asList(defaultExcludeFields));

        return cutFields;
    }

    /**
     * 找出所有table相同的欄位名稱
     * 
     * @param connection
     * @param table1
     * @param destinationTable
     * @return
     * @throws SQLException
     */
    public static List<String> getTheSameFields(Connection connection, String... tables) throws SQLException
    {
        if (!tablesFieldsMap.containsKey(tables))
        {
            tablesFieldsMap.put(tables, getTheSameColumns(connection, tables));
        }

        return tablesFieldsMap.get(tables);
    }
}
