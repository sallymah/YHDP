package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl.controller.base;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.Constant;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISODate;
import tw.com.hyweb.util.ISOUtil;

public class InsertTermBatch implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(InsertTermBatch.class);

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doActionTest(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }

    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        /*記錄交易資料於資料庫*/

        /*String terminalParameterData = ctx.getLMSMsg().getHexStr(LMSTag.TerminalParameterData);*/
        BATCHContext bctx = (BATCHContext)ctx;
        String merchId  = ctx.getLmsMerchantId();
        String termId  = ctx.getLmsTerminalId();
        BerTLV tlv = bctx.getLMSMsg();
        TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
        String betchNo = tlv.getHexStr(LMSTag.BatchNumber);
        /*  if(bctx.isCpc())//中油收單
        {
            betchNo = tlv.getHexStr(LMSTag.BatchNumber);
        }
        else//全家
        {
            betchNo = getMaxBatchNo(bctx, merchId, termId);//全家因為沒有批號，所以txn LOG進來才更新批號
            tlv.addHexStr(LMSTag.BatchNumber, betchNo);
        }*/
        
        TbTermBatchInfo termBatchInfo = new TbTermBatchInfo();
        termBatchInfo.setTxnSrc("B");
        termBatchInfo.setMerchId(merchId);
        termBatchInfo.setTermId(termId);
        termBatchInfo.setTermSettleDate(bctx.getBatchDate());
        termBatchInfo.setTermSettleTime("000000");
        termBatchInfo.setStatus("1");
        termBatchInfo.setInfile(inctlInfo.getFullFileName());
        termBatchInfo.setParMon(bctx.getBatchDate().substring(4, 6));
        termBatchInfo.setParDay(bctx.getBatchDate().substring(6));
        termBatchInfo.setTermUpDate(bctx.getBatchDate());
        termBatchInfo.setImpFileName(inctlInfo.getFullFileName());
        termBatchInfo.setTermSettleFlag("1");
        termBatchInfo.setBatchNo(betchNo);
        ctx.setTermBatchInfo(termBatchInfo);
        /*termBatchInfo.setParameterVersion(null!=terminalParameterData?terminalParameterData.substring(0,6):"000000");*/
        if (!checkIsTermBatch(bctx, termBatchInfo))
        {   //正常結帳需要insert term batch
            TbTermBatchMgr termBatchMgr = new TbTermBatchMgr(ctx.getConnection());
            if(insetTermBatch(termBatchMgr, termBatchInfo, bctx) == false)
            {
                if (!checkIsTermBatch(bctx, termBatchInfo))
                {
                    insetTermBatch(termBatchMgr, termBatchInfo, bctx);
                }
            }
        }
    }
    
    public boolean insetTermBatch(TbTermBatchMgr termBatchMgr, TbTermBatchInfo termBatchInfo, BATCHContext bctx) throws SQLException
    {
        try{
            termBatchMgr.insert(termBatchInfo);
            return true;
        }
        catch(SQLException sqle)
        {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
            {
                //判斷SQLCODE 重複才不處理
                logger.warn("don't care term_batch duplicate:"+termBatchInfo);
                return false;
            }
            else
            {
                throw sqle;
            }
        }
    }
    
    public String getMaxBatchNo(BATCHContext ctx, String mid, String tid) throws SQLException
    {
        String batchNo = null;
        String termSettleDate;
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select batch_no, term_settle_date from tb_term_batch");
        sqlCmd.append(" where batch_no in (select max(batch_no) from tb_term_batch where merch_id = ? and term_id = ? and (substr(BATCH_NO, 1, 1)>='0' and substr(BATCH_NO, 1, 1)<='9'))");
        sqlCmd.append(" and merch_id = ? and term_id = ? and (substr(BATCH_NO, 1, 1)>='0' and substr(BATCH_NO, 1, 1)<='9')");
        Vector<String> parms = new Vector<String>();
        parms.add(mid);
        parms.add(tid);
        parms.add(mid);
        parms.add(tid);
        Vector result = DbUtil.select(sqlCmd.toString(), parms, ctx.getConnection());
        if(null != result && result.size() > 0)
        {
            Vector tmp = (Vector)result.get(0);
            if(null != tmp)
            {
                batchNo = (String) tmp.get(0);
                termSettleDate = (String) tmp.get(1);
                String batchDate = ctx.getBatchDate();
                
                if(Integer.valueOf(termSettleDate) < Integer.valueOf(batchDate))//找出是否有當日的term_batch的batchNo
                {
                    batchNo = ISOUtil.padLeft(String.valueOf((Integer.valueOf(batchNo) + 1)), 6, '0') ;
                    if(batchNo.length() > 6)
                    {
                        batchNo = "000001";
                    }
                }
            }
        }
        else
        {
            batchNo = "000001";
        }
        return batchNo;
    }
    
    public boolean checkIsTermBatch(BATCHContext ctx, TbTermBatchInfo tbTermBatch) throws SQLException
    {
        String sqlCmd = "select count(1) from tb_term_batch where term_id = ? and merch_id = ? and batch_no = ? and term_settle_date = ?";
        Vector<String> parms = new Vector<String>();
        parms.add(tbTermBatch.getTermId());
        parms.add(tbTermBatch.getMerchId());
        parms.add(tbTermBatch.getBatchNo());
        parms.add(tbTermBatch.getTermSettleDate());
        int  count = DbUtil.getInteger(sqlCmd, parms, ctx.getConnection());        
        return count > 0 ? true : false;
    }
}
