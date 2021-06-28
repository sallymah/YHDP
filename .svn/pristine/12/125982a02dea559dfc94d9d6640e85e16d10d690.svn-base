package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.validator;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.Rcode;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class TxnDupValidator  implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(TxnDupValidator.class);
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        logger.debug("validate TxnDupValidator ");
        
        try
        {
            TbOnlTxnInfo onlTxnInfo = null; 
            BATCHContext bctx = (BATCHContext)ctx;
            String dongleRespCode = bctx.getDongleRespCode();
        	BerTLV tlv = bctx.getLMSMsg();
            String cardNo = tlv.getHexStr(LMSTag.LMSCardNumber);
            String cardExp = tlv.getHexStr(LMSTag.LMSCardExpirationDate);
            String lmsInvoice = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
            
            String sqlCmd = "select CARD_NO, LMS_INVOICE_NO, TXN_DATE, TXN_TIME, IMP_FILE_NAME,IMP_FILE_DATE,IMP_FILE_TIME FROM TB_ONL_TXN WHERE CARD_NO = ? AND EXPIRY_DATE=? AND LMS_INVOICE_NO=? ";
            
            Vector<String> param = new Vector<String>();
            param.add(cardNo);
            param.add(cardExp);
            param.add(lmsInvoice);

            Vector result = DbUtil.select(sqlCmd, param,((LMSContext)ctx).getConnection());
            if(null != result && result.size() > 0)
            {
                Vector record = (Vector)result.get(0);
                if(null != record && record.size() > 0)
                {
                    onlTxnInfo = new TbOnlTxnInfo();
                    onlTxnInfo.setCardNo((String)record.get(0));
                    onlTxnInfo.setLmsInvoiceNo((String)record.get(1));
                    onlTxnInfo.setTxnDate((String)record.get(2));
                    onlTxnInfo.setTxnTime((String)record.get(3));
                    onlTxnInfo.setImpFileName((String)record.get(4));
                    onlTxnInfo.setImpFileDate((String)record.get(5));
                    onlTxnInfo.setImpFileTime((String)record.get(6));
                    bctx.setIsDupTxn(true);
                }
            }
                
            if(null != onlTxnInfo && !StringUtil.isEmpty(onlTxnInfo.getImpFileName()))
            {
                if(dongleRespCode.equals("0000") || dongleRespCode.equals("00"))
                {
                    //duplicate
                    bctx.setErrDesc("duplicat tb_onl_txn !! ("+cardNo+", "+ cardExp + ", " + lmsInvoice + ")");
                    logger.error(bctx.getErrDesc());
                    ctx.setRcode(Layer1Constants.RCODE_2711_REPDATA_ERR);
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
}
