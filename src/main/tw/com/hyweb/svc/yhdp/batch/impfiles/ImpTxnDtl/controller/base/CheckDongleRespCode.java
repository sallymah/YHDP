package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.online.ISOMsg;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.logger.BaseTxLogger;
import tw.com.hyweb.util.ArraysUtil;
import tw.com.hyweb.util.string.StringUtil;

public class CheckDongleRespCode implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(CheckDongleRespCode.class);
    private String[] ignoreRCode;
    private String rcode;
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        boolean isReversal = false;
        TbOnlTxnInfo onlTxnInfo = ctx.getOrigTxnInfo();
        if(null != onlTxnInfo)
        {
            if(onlTxnInfo.getStatus().equals("1"))
            {
                isReversal = true;
            }
        }
        
        BATCHContext bctx = (BATCHContext)ctx;
        
        if(!StringUtil.isEmpty(rcode))
        {
            bctx.setDongleRespCode(rcode);
            logger.debug("replace rcode:"+rcode);
        }
        
        String respCode = bctx.getDongleRespCode();
        ISOMsg isoMsg = ctx.getIsoMsg();
        if(!isReversal && (!respCode.equals("0000") && !respCode.equals("00")))
        {
            //logger.debug(CacheTbRcode.getInstance().getValue(rcode,ctx.getConnection()));
            //無原交易，但交易失敗，只需寫入tb_onl_txn_err
            bctx.setRcode(respCode);
            logger.debug("Record is error data : "+respCode);
            throw new TxException("Record is error data : "+respCode);
        }
        else if(isReversal && (!respCode.equals("0000") && !respCode.equals("00")))
        {
            if(checkIgnoreRcode(respCode))
            {
                //有原交易，但交易失敗卻不需reversal只需寫入tb_onl_txn_err
                bctx.setRcode(respCode);
                logger.debug("Record is error data : "+respCode);
                throw new TxException("Record is error data : "+respCode);
            }
            else
            {
                //有原交易，但交易失敗，但需reversal也要寫入tb_onl_txn_err
                isoMsg.setMTI("0400");
                ((BaseTxLogger)ctrl.getTxLogger()).insertOnlTxnErr(ctx, respCode);
            }
        }
        else if(respCode.equals("0000") || respCode.equals("00"))
        {
            //成功的交易，只需更新imp_file_name與date
            isoMsg.setMTI("0400");
            ctx.setOrigTxnInfo(null);//使後續程式不再作reversal程序
        }
    }
    
    public void setIgnoreRcode(String ignoreRCode)
    {
        this.ignoreRCode = ArraysUtil.toStrArray(ignoreRCode);
        
    }
    
    public void setRcode(String rcode)
    {
        this.rcode = rcode;
    }
    
    public boolean checkIgnoreRcode(String rcode)
    {
        int idx = ArrayUtils.indexOf(ignoreRCode, rcode);
        if(idx != -1)
        {
            return true;
        }
        return false;
    }
}
