/*
 * $Id$
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */
package tw.com.hyweb.svc.yhdp.batch.parmdown.function;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuerySingleColumnData;

import java.sql.Connection;
import java.util.List;

import tw.com.hyweb.core.cp.batch.parmdown.function.BonusSource;
import tw.com.hyweb.service.db.info.TbTermInfo;

/**
 * @author Clare
 * @version $Revision$
 */
public class AcquireCommonBonusSource implements BonusSource
{
    /**
     * 精誠同個發卡下的所有收單所屬的bonus可以互相共用
     * 
     * @see tw.com.hyweb.core.cp.batch.parmdown.function.BonusSource#getBonusIds(java.sql.Connection,
     *      java.lang.String, tw.com.hyweb.service.db.info.TbTermInfo)
     */
    public List<String> getBonusIds(Connection connection, String batchDate, TbTermInfo terminal) throws Exception
    {
        String sql = "select distinct BONUS_ID from TB_RELOAD_FUNC_DTL d where ISS_MEM_ID in (select ISS_MEM_ID from TB_RELOAD_FUNC where ACQ_MEM_ID=?) and exists (select 1 from TB_BONUS where BONUS_ID=d.BONUS_ID and BONUS_NATURE='C')";

        return executeQuerySingleColumnData(connection, sql, terminal.getMemId());
    }
}
