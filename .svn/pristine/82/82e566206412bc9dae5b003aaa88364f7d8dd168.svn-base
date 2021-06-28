package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.controller.base;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.online.AbstractController;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.controller.IBizAction;

public class TxnDispAction implements IBizAction
{
    private static final Logger logger = Logger.getLogger(TxnDispAction.class);
    
    private List<IBizAction> foundAction;
    
    private List<IBizAction> notFoundAction;

    public boolean doActionTest(LMSContext ctx) throws SQLException, TxException
    {
        return true;
    }
    
    public void doAction(AbstractController ctrl, LMSContext ctx) throws SQLException, TxException
    {
        logger.debug("doAction");
        
        BATCHContext bctx = (BATCHContext)ctx;
        if(!bctx.getIsDupTxn())
        {
            int actionSize = (null == foundAction)?0:foundAction.size();
            
            for(int i = 0; i < actionSize; i++)
            {
                IBizAction action = foundAction.get(i);
                if(null != action && action.doActionTest(ctx))
                {
                    action.doAction(ctrl, ctx);
                }
            }
        }
        else
        {
            int actionSize = (null == notFoundAction)?0:notFoundAction.size();
            
            for(int i = 0; i < actionSize; i++)
            {
                IBizAction action = notFoundAction.get(i);
                if(null != action && action.doActionTest(ctx))
                {
                    action.doAction(ctrl, ctx);
                }
            }
        }
    }

    public List<IBizAction> getFoundAction()
    {
        return foundAction;
    }

    public void setFoundAction(List<IBizAction> foundAction)
    {
        this.foundAction = foundAction;
    }

    public List<IBizAction> getNotFoundAction()
    {
        return notFoundAction;
    }

    public void setNotFoundAction(List<IBizAction> notFoundAction)
    {
        this.notFoundAction = notFoundAction;
    }
}