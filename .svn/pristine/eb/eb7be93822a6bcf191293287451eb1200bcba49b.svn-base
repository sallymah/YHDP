package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.validator;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.AmountUtil;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.util.LMSProcCode;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ChipBeforeBalValidator  implements IValidator
{
    private static final int IdStrLen=10;
    private static final int SignStrLen=2;
    private static final int QtyStrLen=10;
    private static final int PeriodStrLen=16;
    private static final int StorePosStrLen=2;
    private static final String ECASH_BONUS_ID = "0000000000";
    private static final String QUANTITY_SIGN_PLUS_ = "00";
    private static final String QUANTITY_SIGN_MINUS_ = "01";
    private static final String START_DATE = "00010101";
    private static final String END_DATE = "99991231";
    
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(ChipBeforeBalValidator.class);
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.core.cp.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        LMSContext sctx = (LMSContext) ctx;
        BATCHContext btx = (BATCHContext) sctx;
        
        try
        { 
            TbMemberInfo acqMemberInfo = sctx.getAcquireInfo();
            logger.debug("NewTransFlag:"+acqMemberInfo.getNewTransFlag());
            if(Integer.valueOf(acqMemberInfo.getNewTransFlag()) == 1)//是否要轉換交易前餘額邏輯
            {
                if(!StringUtil.isEmpty(btx.getAutoLoadAmt()) && !btx.getAutoLoadAmt().equals("0000000000"))
                {
                    if(!StringUtil.isEmpty(btx.getAutoLoadAtc())  && !btx.getAutoLoadAtc().equals("00000000"))
                    {
                        logger.debug("LmsPcode:"+btx.getLmsPcode());
                        if(Integer.valueOf(btx.getLmsPcode()) ==  LMSProcCode.SALE)
                        {
                            logger.debug("getAutoRldChipBalBefore");
                            getAutoRldChipBalBefore(btx);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(" ", e);
            ctx.setRcode(Rcode.FAIL);
            return ctx;
        }
        return ctx;
    }
    
    /* 消費限定 */
    public double getAutoRldChipBalBefore(BATCHContext ctx)
    {
        StringBuilder buf = new StringBuilder();
        String chipBalArea = ctx.getLMSMsg().getHexStr(LMSTag.ChipPointsBalanceArea);
        if (chipBalArea != null)
        {
            int idx = 0;
            String bonusId = chipBalArea.substring(idx,idx+=IdStrLen);
            String sign = chipBalArea.substring(idx,idx+=SignStrLen);
            double qty = Integer.parseInt(chipBalArea.substring(idx,idx+=QtyStrLen))/100;
            double autoRldBefAmt = sign.equals(YHDPUtil.NEGATIVE_SING) ? -1 * qty: qty;//00正,01負
            //20171223遠鑫Angus不願意改前台把自動加值交易前金額帶在消費的交易前金額，所以必需重新計算
            double autoAmt = Double.parseDouble(ctx.getAutoLoadAmt()) / 100;
            double ecashBeforeBal = autoRldBefAmt + autoAmt; //實際消費交易前餘額 = 本筆消費交易交易前餘額(帶自動加值) + 加值金額
            
            /* 晶片餘額 */
            if(ecashBeforeBal >= 0)
            {
                buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_PLUS_ + AmountUtil.format(ecashBeforeBal == 0 ?0 :ecashBeforeBal , "00000000.00") + START_DATE + END_DATE + "00");
            }
            else
            {
                ecashBeforeBal = -ecashBeforeBal;
                buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_MINUS_ + AmountUtil.format(ecashBeforeBal == 0 ?0 :ecashBeforeBal , "00000000.00") + START_DATE + END_DATE + "00");
            }
            ctx.getLMSMsg().delByTag(LMSTag.ChipPointsBalanceArea);
            ctx.getLMSMsg().addHexStr(LMSTag.ChipPointsBalanceArea,buf.toString());
            logger.debug("real chip before bal:"+ecashBeforeBal);
        }
        else
        {
            double autoAmt = Double.parseDouble(ctx.getAutoLoadAmt()) / 100;
            double ecashBeforeBal = 0 + autoAmt; //實際消費交易前餘額 = 本筆消費交易交易前餘額(帶自動加值) + 加值金額
            
            /* 晶片餘額 */
            if(ecashBeforeBal >= 0)
            {
                buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_PLUS_ + AmountUtil.format(ecashBeforeBal == 0 ?0 :ecashBeforeBal , "00000000.00") + START_DATE + END_DATE + "00");
            }
            else
            {
                ecashBeforeBal = -ecashBeforeBal;
                buf.append(ECASH_BONUS_ID + QUANTITY_SIGN_MINUS_ + AmountUtil.format(ecashBeforeBal == 0 ?0 :ecashBeforeBal , "00000000.00") + START_DATE + END_DATE + "00");
            }
            ctx.getLMSMsg().delByTag(LMSTag.ChipPointsBalanceArea);
            ctx.getLMSMsg().addHexStr(LMSTag.ChipPointsBalanceArea,buf.toString());
            logger.debug("real chip before bal:"+ecashBeforeBal);
        }
        return 0;
    }
}