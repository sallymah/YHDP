package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.validator;

import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.LmsDbUtil;
import tw.com.hyweb.svc.yhdp.online.util.ProcCode;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.util.ArraysUtil;
import tw.com.hyweb.util.ISODate;

public class CardValidator implements IValidator
{  
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(CardValidator.class);
    private int[] ignoreChipCardStatus = null;
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        logger.debug("validate CardValidator ");
        LMSContext lmsctx = ((LMSContext)ctx);
        TbCardInfo cardInfo = lmsctx.getCardInfo();
        String pan = lmsctx.getLMSCardNbr();
        
        String mti = lmsctx.getMTI();
        String lmsPcode = lmsctx.getLmsPcode();
        
        try
        {
            if (cardInfo==null)
            {//若LMSCtx中cardInfo不存在則設定
                logger.debug("Start Get Card Info");
                long start = java.util.Calendar.getInstance().getTimeInMillis();
                cardInfo = LmsDbUtil.getCardInfo(pan, null,lmsctx.getConnection());
                logger.debug("Get Card time = " + (java.util.Calendar.getInstance().getTimeInMillis() - start));
                lmsctx.setCardInfo(cardInfo);
            }

            if (null==cardInfo)
            {
                ctx.setRcode(Rcode.INVALID_CARD);
                lmsctx.setErrDesc("INVALID CARD  (cardNo:"+pan+")");
                logger.warn(lmsctx.getErrDesc());
                return ctx;
            }

            String expir = cardInfo.getExpiryDate();
            BerTLV tlv = lmsctx.getLMSMsg();
            tlv.addHexStr(LMSTag.LMSCardExpirationDate, expir);
            int iDate = Integer.parseInt(ISODate.formatDate(new Date(), "yyyyMMdd"));
            int iExpir = Integer.parseInt(expir);
            if (iDate > iExpir)
            {
                ctx.setRcode(Rcode.EXPIRED_CARD);
                lmsctx.setErrDesc("EXPIRED CARD  (cardNo:"+pan+", expiryDate:"+expir +")");
                logger.warn(lmsctx.getErrDesc());
                return ctx;
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
    
    public void setIgnoreChipCardStatus(String ignoreChipCardStatus)
    {
        this.ignoreChipCardStatus = ArraysUtil.toIntArray(ignoreChipCardStatus);
    }
}
