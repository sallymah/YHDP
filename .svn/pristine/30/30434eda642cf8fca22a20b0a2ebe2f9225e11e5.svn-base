package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.FF21;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.tag.OfflineUploadTxnInfo;
import tw.com.hyweb.util.ArraysUtil;
import tw.com.hyweb.util.DbUtil;

public class UpdateOnlTxnImpInfo implements IBizAction
{
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(UpdateOnlTxnImpInfo.class);
    private int[] ignoreLmsProcCode;
    
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        
        if(ctrl != null)
        {
            BATCHContext bctx = (BATCHContext)ctx;
            String termSettleDate = bctx.getTermBatchInfo().getTermSettleDate();
            String termSettleTime = bctx.getTermBatchInfo().getTermSettleTime();
            logger.debug("TbInctlInfo:"+bctx.getTbInctlInfo());
            TbInctlInfo inctlInfo = bctx.getTbInctlInfo();
            String lmsPcode = String.valueOf(ctx.getLmsPcode());
            int intPcode = Integer.parseInt(lmsPcode);
            int idx = ArrayUtils.indexOf(ignoreLmsProcCode, intPcode);
            if(idx == -1)
            {
                StringBuffer sb = new StringBuffer();
                int count = 0;
                String txnNote = ctx.getTxnNote();
                BerTLV tlv = ctx.getLMSMsg();
                String cardNo = ctx.getLMSCardNbr();
                String expiryDate = ctx.getLMSCardExpirationDate();
                String lmsInvoiceNo = tlv.getHexStr(LMSTag.LMSInvoiceNumber);
                sb.append("update TB_ONL_TXN set CHIP_POINT1_AFTER = ?, TERM_SETTLE_DATE = ? , TERM_SETTLE_TIME = ?, IMP_FILE_DATE=?, IMP_FILE_TIME=?, TXN_NOTE =  TXN_NOTE || ?,IMP_FILE_NAME = ?");
                Vector params = new Vector();
                params.add(bctx.getChipAfterBonusQty());
                params.add(termSettleDate);
                params.add(termSettleTime);
                params.add(DateUtils.getSystemDate());
                params.add(DateUtils.getSystemTime());
                params.add(txnNote);
                params.add(inctlInfo.getFullFileName());
                params.add(cardNo);
                params.add(expiryDate);
                params.add(lmsInvoiceNo);
              /*  if(!respCode.equals("0000") && !respCode.equals("00"))
                {
                    sb.append(", status ='F', onl_rcode = ?");//如果此交易在檔案是失敗的,則更新增此筆交易為失敗
                    params.add(respCode);
                }*/
                sb.append("where CARD_NO=? and EXPIRY_DATE=? and LMS_INVOICE_NO=? and IMP_FILE_DATE is null");
                SqlResult sr = DbUtil.sqlAction(sb.toString(), params, ctx.getConnection());
                if (sr!=null){
                    count = sr.getRecordCount();
                }
                
                if(count == 0)
                {
                    logger.debug("update cnt:" + count);
                    ctx.setRcode(Layer1Constants.RCODE_2716_UPDATE_NODATA);
                    throw new TxException("not data found !!");
                }
            }
            else
                logger.debug("pass txn");
        }
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
