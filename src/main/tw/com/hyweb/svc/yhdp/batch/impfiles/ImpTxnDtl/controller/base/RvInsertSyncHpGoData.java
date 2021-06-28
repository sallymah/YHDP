package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbCustInfo;
import tw.com.hyweb.service.db.info.TbWsLogInfo;
import tw.com.hyweb.service.db.mgr.TbWsLogMgr;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.sync.InsertSyncHpGoData;
import tw.com.hyweb.util.string.StringUtil;

public class RvInsertSyncHpGoData implements IBizAction
{
    private final static Logger logger = Logger.getLogger(InsertSyncHpGoData.class);

    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        String cardNo = ctx.getLMSCardNbr();
        if(null != cardNo)
        {
            if(null != ctx.getCardInfo())
            {
                if(!StringUtil.isEmpty(ctx.getCardInfo().getIsSyncHg()) && Integer.valueOf(ctx.getCardInfo().getIsSyncHg()) == 1)
                {
                    String mti = ctx.getMTI();
                    if(mti.charAt(3) == '4')
                    {
                        return (ctx.getOrigTxnInfo() != null)&&(!ctx.getOrigTxnInfo().getStatus().equals("9"));
                    }
                }
            }
        }
        return false;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        
        TbWsLogInfo wsLogInfo = new TbWsLogInfo();
        
        BerTLV tlv = ctx.getLMSMsg();
        String lmsInvoiceNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
        String mti = ctx.getIsoMsg().getMTI();
        TbCustInfo custInfo = ctx.getCustInfo();
        String pid = custInfo != null ? custInfo.getPersonId() : "";
        wsLogInfo.setCardNo(ctx.getLMSCardNbr());
        wsLogInfo.setExpiryDate(ctx.getLMSCardExpirationDate());
        wsLogInfo.setLmsInvoiceNo(lmsInvoiceNo);
        wsLogInfo.setMti(mti);
        wsLogInfo.setPCode(ctx.getLmsPcode());
        wsLogInfo.setTxnDate(ctx.getTermTxnDate());
        wsLogInfo.setTxnTime(ctx.getTermTxnTime());
        wsLogInfo.setPersonId(pid);
        wsLogInfo.setHgCardNo(ctx.getCardInfo().getHgCardNo());
        TbWsLogMgr wsLogMgr = new TbWsLogMgr(ctx.getConnection());
        wsLogMgr.insert(wsLogInfo);
    }
}