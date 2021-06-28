/*
 * $Id: HostPointRedeemFunction.java 1910 2009-08-17 09:06:19Z 96004 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */
package tw.com.hyweb.svc.yhdp.batch.parmdown.function;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuerySingleValue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import tw.com.hyweb.core.cp.batch.parmdown.ParameterFunction;
import tw.com.hyweb.core.cp.batch.util.StringUtils;
import tw.com.hyweb.service.db.info.TbRedeemFuncInfo;
import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.service.db.info.TbTermParDefInfo;
import tw.com.hyweb.service.db.mgr.TbRedeemFuncMgr;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Clare
 * @version $Revision: 1910 $
 */
public class HostPointRedeemFunction implements ParameterFunction
{
    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.parmdown.ParameterFunction#getValues(java.
     * sql.Connection, java.lang.String,
     * tw.com.hyweb.service.db.info.TbTermParDefInfo,
     * tw.com.hyweb.service.db.info.TbTermInfo)
     */
    public List<String> getValues(Connection connection, String batchDate, TbTermParDefInfo define, TbTermInfo terminal) throws Exception
    {
        List<String> values = new ArrayList<String>();

        for (TbRedeemFuncInfo info : getRedeemFunctions(connection, batchDate, define, terminal))
        {
            String quantity = getQuantity(connection, info);

            if (quantity != null)
            {
                values.add(makeValue(info, quantity).toString());
            }
        }

        return values;
    }

    /**
     * @param info
     * @param quantity
     * @return
     */
    private String makeValue(TbRedeemFuncInfo info, String quantity)
    {
        StringBuilder value = new StringBuilder("");
        value.append(info.getFuncId());
        value.append(StringUtils.double2IntString(quantity, 2, 10));
        value.append(info.getFuncDesc());

        return value.toString();
    }

    /**
     * @param connection
     * @param info
     * @return
     * @throws SQLException
     */
    private String getQuantity(Connection connection, TbRedeemFuncInfo info) throws SQLException
    {
        String sql = "select sum(BONUS_QTY) as BONUS_QTY from TB_REDEEM_FUNC_DTL where ISS_MEM_ID=? and ACQ_MEM_ID=? and P_CODE=? and FUNC_ID=?";

        return executeQuerySingleValue(connection, sql, info.getIssMemId(), info.getAcqMemId(), info.getPCode(), info.getFuncId());
    }

    /**
     * @param connection
     * @param batchDate
     * @param define
     * @param terminal
     * @return
     * @throws SQLException
     */
    private List<TbRedeemFuncInfo> getRedeemFunctions(Connection connection, String batchDate, TbTermParDefInfo define, TbTermInfo terminal) throws SQLException
    {
        Vector<TbRedeemFuncInfo> result = new Vector<TbRedeemFuncInfo>();

        String condition = "ACQ_MEM_ID='" + terminal.getMemId() + "' and '" + batchDate + "' between VALID_SDATE and VALID_EDATE and STATUS='1' and FUNC_TYPE='H' and FUNC_TXN_TYPE='2'";

        if (StringUtil.isEmpty(define.getFilterRule()))
        {
            new TbRedeemFuncMgr(connection).queryMultiple(condition, result);
        }
        else
        {
            new TbRedeemFuncMgr(connection).queryMultiple(condition, result, define.getFilterRule());
        }

        return result;
    }
}
