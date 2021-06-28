package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl.validator;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.Rcode;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSProcCode;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.util.ArraysUtil;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class onlTxnValidator  implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(onlTxnValidator.class);
    private int[] ignoreLmsProcCode;
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        logger.debug("validate onlTxnValidator ");
        try
        {
            boolean isMapping = false;
            BATCHContext bctx = (BATCHContext)ctx;
            String donbleRespCode = bctx.getDongleRespCode();
            int ipcode = Integer.valueOf(bctx.getLmsPcode());
            if(donbleRespCode.equals("0000") ||  donbleRespCode.equals("00"))
            {
                String onlStatus = getOrigTxn(bctx);
                String fileStatus = bctx.getFileTxnStatus();
                if(!StringUtil.isEmpty(onlStatus))
                {
                    if(!StringUtil.isEmpty(fileStatus))
                    {
                        if(onlStatus.equals(fileStatus))
                        {
                            isMapping = true;
                        }
                    }
                }
                else
                {
                    int idx = ArrayUtils.indexOf(ignoreLmsProcCode, ipcode);
                    if(idx != -1)
                    {
                        isMapping = true;//消費一律成功
                    }
                }
                
                if(!isMapping)
                {
                    bctx.setErrDesc("onl.status:" + onlStatus + " != file.status:" + fileStatus);
                    logger.error(bctx.getErrDesc());
                    ctx.setRcode(Layer1Constants.RCODE_2710_INVALID_ERR);
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
    
    public String getOrigTxn(BATCHContext bctx) throws SQLException
    {
        BerTLV tlv = bctx.getLMSMsg();
        String cardNo = tlv.getHexStr(LMSTag.LMSCardNumber);
        String cardExp = tlv.getHexStr(LMSTag.LMSCardExpirationDate);
        String lmsInvoice = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
        String sqlCmd = "select STATUS from TB_ONL_TXN where card_no = ? and expiry_date = ? and lms_invoice_no = ?";
        Vector<String> parms = new Vector<String>();
        parms.add(cardNo);
        parms.add(cardExp);
        parms.add(lmsInvoice);
        return DbUtil.getString(sqlCmd, parms, bctx.getConnection());
    }
    
    /**
     * @return acceptLmsProcCode
     */
    public int[] getIgnoreLmsProcCodeArray()
    {
        return ignoreLmsProcCode;
    }
    
    public void setIgnoreLmsProcCode(String ignoreLmsProcCode)
    {
        this.ignoreLmsProcCode = ArraysUtil.toIntArray(ignoreLmsProcCode);
        
    }
}