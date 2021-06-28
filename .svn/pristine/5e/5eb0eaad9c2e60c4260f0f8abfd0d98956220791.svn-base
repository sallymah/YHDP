package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.validator;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.CacheTbSysConfig;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.service.db.info.TbTermUptInfo;
import tw.com.hyweb.service.db.mgr.TbTermMgr;
import tw.com.hyweb.service.db.mgr.TbTermUptMgr;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.Constant;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.validator.CpcTerminalValidator;

public class TerminalValidator implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(CpcTerminalValidator.class);
    private static final String sqlTerm = "select count(*) from TB_TERM where TERM_ID=? and STATUS='1'";
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        logger.debug("validate TerminalValidator ");
        LMSContext lctx = (LMSContext)ctx;
        try
        {
            TbTermInfo termInfo = ((LMSContext)ctx).getTermInfo();
            if (termInfo==null)
            {
                logger.warn("INVALID TERM. number <=0");
                try {
                    insertTerm(lctx);
                }
                catch (SQLException sqle)
                {
                    if (sqle.getSQLState().equals("23000") && sqle.getErrorCode()==1)
                    {
                        ctx.setRcode(Rcode.OK);
                        //pass
                    }
                    else
                    {//非duplicate duplicate
                        throw sqle;
                    }
                }
                /*lctx.setErrDesc("INVALID_TERM (TERM_ID:" + termId + ",MERCH_ID:" + merchId +")");
                ctx.setRcode(Rcode.INVALID_TERM);
                return ctx; */
            }
        }
        catch (SQLException e)
        {
            logger.error(sqlTerm,e);
            ctx.setRcode(Rcode.SQL_FAIL);
        }
        return ctx;
    }
    
    public String getOfflinePcode(String uploadTxnData)
    {
        return uploadTxnData.substring(0, 4);
    }
    
    public void insertTerm(LMSContext ctx) throws SQLException
    {
        BerTLV tlv = ((LMSContext)ctx).getLMSMsg();
        String samId = tlv.getHexStr(LMSTag.SAMArea);
        TbMerchInfo merchInfo = ctx.getMerchInfo();
        String merchId = ctx.getLmsMerchantId();
        String termId = ctx.getLmsTerminalId();
        TbTermInfo termInfo = new TbTermInfo();
        termInfo.setMemId(merchInfo.getMemId());
        termInfo.setMerchId(merchId);
        termInfo.setTermId(termId);
        String func = CacheTbSysConfig.getInstance().getValue("FUNC", "0000110001111111001101111111111010000000");
        termInfo.setFunc(func);
        termInfo.setUptDate(ctx.getHostDate());
        termInfo.setUptTime(ctx.getHostTime());
        termInfo.setUptUserid(Constant.UPT_ONLINE_USER_ID);
        termInfo.setAprvDate(ctx.getHostDate());
        termInfo.setAprvTime(ctx.getHostTime());
        termInfo.setAprvUserid(Constant.UPT_ONLINE_USER_ID);
        termInfo.setLatestSettleDay(ctx.getHostDate());
        termInfo.setSamId(samId);
        termInfo.setStatus("1");
        TbTermMgr termMgr = new TbTermMgr(ctx.getConnection());
        termMgr.insert(termInfo);
        //insertTermUpt(ctx, termInfo);
    }
    
    public void insertTermUpt(LMSContext ctx, TbTermInfo termInfo) throws SQLException
    {
        TbTermUptInfo termUptInfo = new TbTermUptInfo();
        termUptInfo.setMemId(termInfo.getMemId());
        termUptInfo.setMerchId(termInfo.getMerchId());
        termUptInfo.setTermId(termInfo.getTermId());
        termUptInfo.setFunc(termInfo.getFunc());
        termUptInfo.setUptDate(termInfo.getUptDate());
        termUptInfo.setUptTime(termInfo.getUptTime());
        termUptInfo.setUptUserid(termInfo.getUptUserid());
        termUptInfo.setUptStatus(Constant.UPT_STATUS_ADD);
        termUptInfo.setAprvDate(termInfo.getAprvDate());
        termUptInfo.setAprvTime(termInfo.getAprvTime());
        termUptInfo.setAprvUserid(termInfo.getAprvUserid());
        termUptInfo.setStatus(termInfo.getStatus());
        termUptInfo.setAprvStatus(Constant.APRV_STATUS_APPROVED);
        TbTermUptMgr termMgr = new TbTermUptMgr(ctx.getConnection());
        termMgr.insert(termUptInfo);
    }
}