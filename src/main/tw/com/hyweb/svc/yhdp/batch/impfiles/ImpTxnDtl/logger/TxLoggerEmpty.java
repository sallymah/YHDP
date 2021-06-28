package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.logger;

import java.sql.SQLException;

import tw.com.hyweb.online.Context;

public class TxLoggerEmpty extends TxLoggerOfflineBase
{
    /**
     * Record tx log by business rule
     * @param ctxResp
     * @throws SQLException
     */
    @Override
    public void insert(Context ctxResp) throws SQLException
    {

    }
}
