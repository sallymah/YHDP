package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.controller.base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class UpdateTxnSettleDateTime implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(UpdateTxnSettleDateTime.class);

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doActionTest(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }

    private static final String sqlUpdateTxnSettleDateTime = "Update TB_ONL_TXN set TERM_SETTLE_DATE = ?, TERM_SETTLE_TIME = ? Where merch_id=? and term_id=? and batch_no=? and term_settle_date is null and term_settle_time is null";
    private static final String sqlUpdateTxnSettleDateTimeWEH = "Update TB_ONL_TXN_ERR set TERM_SETTLE_DATE = ?, TERM_SETTLE_TIME = ? Where merch_id=? and term_id=? and batch_no=? and term_settle_date is null and term_settle_time is null and ERR_TYPE= 'A' and onl_rcode in (select rcode from tb_rcode where err_handle_flag=1) ";
    private static final String sqlUptLstSettleDay = "update TB_TERM set SAM_ID =?,LATEST_SETTLE_DAY=? where MERCH_ID=? and TERM_ID=?";
    private static final String sqlUpdateTxnSettleDateTimeFam = "Update TB_ONL_TXN set TERM_SETTLE_DATE = ?, TERM_SETTLE_TIME = ?, BATCH_NO = ? Where txn_src = 'E' and merch_id=? and term_id=? and batch_no=? and term_settle_date is null and term_settle_time is null";
    private static final String sqlUpdateTxnSettleDateTimeWEHFam = "Update TB_ONL_TXN_ERR set TERM_SETTLE_DATE = ?, TERM_SETTLE_TIME = ?, BATCH_NO = ? Where txn_src = 'E' and merch_id=? and term_id=? and batch_no=? and term_settle_date is null and term_settle_time is null and ERR_TYPE= 'A' and onl_rcode in (select rcode from tb_rcode where err_handle_flag=1) ";
    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        BATCHContext bctx = (BATCHContext)ctx;
       /* Vector<String> params = new Vector<String>();
        BerTLV tlv = ctx.getLMSMsg();
        String merchId  = ctx.getLmsMerchantId();
        String termId  = ctx.getLmsTerminalId();
        String batchNo = tlv.getHexStr(LMSTag.BatchNumber);
        String checkBatchNo = tlv.getHexStr(LMSTag.BatchNumber);
       
        params.add(ctx.getTermBatchInfo().getTermSettleDate());
        params.add(ctx.getTermBatchInfo().getTermSettleTime());
        params.add(merchId);
        params.add(termId);
        params.add(batchNo);
        
        if(checkTxnIsSettle(merchId, termId, checkBatchNo, ctx.getConnection()))
        {
            DbUtil.sqlAction(sqlUpdateTxnSettleDateTime, params, ctx.getConnection());
            //error handling
            DbUtil.sqlAction(sqlUpdateTxnSettleDateTimeWEH, params, ctx.getConnection());
        } 20151201 mark shane 全家中油以單筆作結帳 */
        
        if(isUpdateLstestSettleDay(ctx))
        {
            uptLstSettleDay(ctx);//20150217, 改為每次都更新最後結帳日
        }
    }

    /**
     * @param ctx
     * @throws SQLException
     */
    private void uptLstSettleDay(LMSContext ctx) throws SQLException
    {
        BATCHContext bctx = (BATCHContext)ctx;
        String samId = ctx.getLMSMsg().getHexStr(LMSTag.SAMArea);
        Vector<String> params = new Vector<String>();
        String merchId  = ctx.getLmsMerchantId();
        String termId  = ctx.getLmsTerminalId();
        params.add(samId);
        params.add(bctx.getBatchDate());
        params.add(merchId);
        params.add(termId);
        DbUtil.sqlAction(sqlUptLstSettleDay, params, ctx.getConnection());
    }
    
    public boolean isUpdateLstestSettleDay(LMSContext ctx) throws SQLException
    {
        BATCHContext bctx = (BATCHContext)ctx;
        String merchId  = ctx.getLmsMerchantId();
        String termId  = ctx.getLmsTerminalId();
        String sqlCmd = "select LATEST_SETTLE_DAY from tb_term where merch_id = ? and term_id = ?";
        Vector<String> parm = new Vector<String>();
        parm.add(merchId);
        parm.add(termId);
        String settleDay =  DbUtil.getString(sqlCmd, parm, ctx.getConnection());
        if(!StringUtil.isEmpty(settleDay))
        {
            if(settleDay.equals(bctx.getBatchDate()))
            {
                return false;
            }
        }
        return true;
    }
    
    public boolean checkTxnIsSettle(String mid, String tid, String batchNo, Connection conn) throws SQLException
    {
        String sqlCmd = "select count(1) from tb_onl_txn where merch_id = ? and term_id = ? and batch_no = ? and term_settle_date is null";
        Vector<String> parm = new Vector<String>();
        parm.add(mid);
        parm.add(tid);
        parm.add(batchNo);
        int count =  DbUtil.getInteger(sqlCmd, parm, conn);        
        return count > 0 ? true: false;
    }
}