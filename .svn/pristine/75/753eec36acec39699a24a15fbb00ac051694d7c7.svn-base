package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.validator;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbTermBatchMgr;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.TransferUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;

import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;

public class termBatchValidator implements IValidator
{  
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(termBatchValidator.class);
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        logger.debug("validate CardValidator ");
        BATCHContext bctx = ((BATCHContext)ctx);
        try {
            String merchId = bctx.getLmsMerchantId();
            String termId = bctx.getLmsTerminalId();
            BerTLV tlv = bctx.getLMSMsg();
            TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
            String betchNo = getMaxBatchNo(bctx, merchId, termId);//交通一檔一批
            tlv.delByTag(LMSTag.BatchNumber);
            tlv.addHexStr(LMSTag.BatchNumber, ISOUtil.padLeft(betchNo, 6, '0'));//補0
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
            bctx.setTermBatchInfo(termBatchInfo);
            
            if (!checkIsTermBatch(bctx, termBatchInfo))
            {//正常結帳需要insert term batch
                TbTermBatchMgr termBatchMgr = new TbTermBatchMgr(bctx.getConnection());
                try{
                    termBatchMgr.insert(termBatchInfo);
                }
                catch(SQLException sqle)
                {
                    if (sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
                    {
                        //判斷SQLCODE 重複才不處理
                        logger.debug("don't care term_batch duplicate");
                    }
                    else
                    {
                        throw sqle;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            logger.error("",e);
            ctx.setRcode(Rcode.SQL_FAIL);
            return ctx;
        }

        return ctx;
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