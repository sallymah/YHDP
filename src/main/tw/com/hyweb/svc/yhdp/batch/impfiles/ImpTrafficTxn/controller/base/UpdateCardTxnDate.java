package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTrafficTxn.controller.base;

import java.sql.SQLException;
import java.util.Vector;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.util.DbUtil;

public class UpdateCardTxnDate implements IBizAction
{
    int isReload = 0;
    String txnPcodeList = null;
    String reloadPcodeList = null;
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doActionTest(tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    
    /* (non-Javadoc)
     * @see tw.com.hyweb.svc.yhdp.online.controller.IBizAction#doAction(tw.com.hyweb.online.AbstractController, tw.com.hyweb.svc.yhdp.online.LMSContext)
     */
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        TbCardInfo cardInfo = ctx.getCardInfo();
        String firstDate = cardInfo.getFirstTxnDate();
        String lastDate = cardInfo.getLastTxnDate();
        String firstReloadDate = cardInfo.getFirstReloadDate();
        String lastReloadDate = cardInfo.getLastReloadDate();
        String pcode = ctx.getLMSMsg().getHexStr(LMSTag.LMSProcessingCode);
        String sqlCmd = null;
        String sqlUpdate = "";
        
        if (firstDate==null || firstDate.equals("00000000"))
        {
            sqlUpdate += ("FIRST_TXN_DATE = '"+ctx.getHostDate()+"'");
        }

        if (lastDate==null || lastDate.equals("00000000") || (lastDate.compareTo(ctx.getHostDate()) < 0) )
        {
            if(sqlUpdate.length() > 0)
                sqlUpdate += " , ";
            sqlUpdate += ("LAST_TXN_DATE = '"+ctx.getHostDate()+"'");
        }
        
        if(null != reloadPcodeList && reloadPcodeList.indexOf(pcode) >= 0)
        {
            if (firstReloadDate==null || firstReloadDate.equals("00000000"))
            {
                if(sqlUpdate.length() > 0)
                    sqlUpdate += " , ";
                sqlUpdate += ("FIRST_RELOAD_DATE = '"+ctx.getHostDate()+"'");
            }
    
            if (lastReloadDate==null || lastReloadDate.equals("00000000") || (lastReloadDate.compareTo(ctx.getHostDate()) < 0) )
            {
                if(sqlUpdate.length() > 0)
                    sqlUpdate += " , ";
                sqlUpdate += ("LAST_RELOAD_DATE = '"+ctx.getHostDate()+"'");
            }
        }

        if(sqlUpdate.length() > 0)
        {
            sqlCmd = "update TB_CARD set " + sqlUpdate + "where CARD_NO=? and EXPIRY_DATE=? ";
            Vector<String> param = new Vector<String>();
            param.add(ctx.getCardInfo().getCardNo());
            param.add(ctx.getCardInfo().getExpiryDate());
            DbUtil.sqlAction(sqlCmd, param, ctx.getConnection());
        }
    }

    
    /**
     * @param TxnPCodeList the TxnPCodeList to set
     */
    public void setTxnPcodeList(String txnPcodeList)
    {
        this.txnPcodeList = txnPcodeList;
    }
    
    /**
     * @param TxnPCodeList the TxnPCodeList to set
     */
    public String getTxnPcodeList()
    {
        return this.txnPcodeList;
    }

    /**
     * @param ReloadPCodeList the ReloadPCodeList to set
     */
    public void setReloadPcodeList(String reloadPcodeList)
    {
        this.reloadPcodeList = reloadPcodeList;
    }
    
    /**
     * @param ReloadPCodeList the ReloadPCodeList to set
     */
    public String getReloadPcodeList()
    {
        return this.reloadPcodeList;
    }   
}
