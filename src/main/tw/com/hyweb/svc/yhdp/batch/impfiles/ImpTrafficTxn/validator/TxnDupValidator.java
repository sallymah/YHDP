package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.validator;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.online.loyalty.util.Rcode;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbOnlTxnInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.TransferUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnDetail;
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
            BATCHContext bctx = (BATCHContext)ctx;
            BerTLV tlv = bctx.getLMSMsg();
            String cardNo = tlv.getHexStr(LMSTag.LMSCardNumber);
            String atc = tlv.getHexStr(LMSTag.ICCATC);
            TrafficTxnDetail trafficTxnDtl = bctx.getTrafficTxnDetail();
            
            StringBuffer sqlCmd = new StringBuffer();
            sqlCmd.append("select * from tb_onl_txn a, tb_traffic_txn b where a.card_no = b.card_no and a.lms_invoice_no = b.lms_invoice_no");
            sqlCmd.append(" and a.txn_src ='B' and a.card_no = ? and a.atc = ? and b.trans_sys_no = ? and b.loc_id = ? and b.dev_id = ?");
            sqlCmd.append(" and b.txn_type = ? and a.txn_date = ? and a.txn_time = ?");

            
            Vector<String> param = new Vector<String>();
            param.add(cardNo);
            param.add(atc);
            param.add(trafficTxnDtl.getDataRecode(TransferUtil.TRANS_SYS_NO));
            param.add(bctx.getRealLocId());
            param.add(trafficTxnDtl.getDataRecode(TransferUtil.DEV_ID));
            param.add(trafficTxnDtl.getDataRecode(TransferUtil.TXN_TYPE));
            param.add(ctx.getHostDate());
            param.add(ctx.getHostTime());
  
            StringBuffer sqlKey = new StringBuffer();
            sqlKey.append("CARD_NO:"+cardNo + ",");
            sqlKey.append("TRANS_NO:"+trafficTxnDtl.getDataRecode(TransferUtil.TRANS_NO) + ",");
            sqlKey.append("TRANS_SYS_NO:"+trafficTxnDtl.getDataRecode(TransferUtil.TRANS_SYS_NO) + ",");
            sqlKey.append("LOC_ID:"+bctx.getRealLocId() + ",");
            sqlKey.append("DEV_ID:"+trafficTxnDtl.getDataRecode(TransferUtil.DEV_ID)  + ",");
            sqlKey.append("TXN_TYPE:"+trafficTxnDtl.getDataRecode(TransferUtil.TXN_TYPE)  + ",");
            sqlKey.append("TRANS_DATE:"+ctx.getHostDate()  + ctx.getHostTime()  + ",");
            Vector result = DbUtil.select(sqlCmd.toString(), param,((LMSContext)ctx).getConnection());
            if(null != result && result.size() > 0)
            {
                bctx.setErrDesc("duplicat tb_onl_txn !! ("+ sqlKey.toString() + ")");
                logger.error(bctx.getErrDesc());
                ctx.setRcode(Layer1Constants.RCODE_2711_REPDATA_ERR);
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
