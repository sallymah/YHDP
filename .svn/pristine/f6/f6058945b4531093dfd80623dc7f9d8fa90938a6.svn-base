/*
 * Version: 3.0.0
 * Date: 2007-01-29
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;

/**
 * 主機紅利依帳戶（帳戶ID）歸戶之餘額管理<br>
 * 
 * @author Anny
 *         Anny,20070129 改成繼承BalanceProcessor
 */
public class BalHostAcct extends BalanceProcessor
{
    private static final Logger logger = Logger.getLogger(BalHostAcct.class);

    /**
     * Entry<br>
     * ant runBalHostAcct –Ddate=””<br>
     * batchDate={–Ddate請輸入YYYYMMDD；若不輸入，預設為系統日}<br>
     * @throws Exception 
     */
    public BalHostAcct(String batchDate, AbstractBatchBasic batch,int cutDay) throws Exception
    {
    	
        super(batch,cutDay);
        try
        {
            logger.info("=====BalHostAcct Start=====");
            setBatchDate(batchDate);
            process(null);
            logger.info("=====BalHostAcct end=====");
        }
        catch (Exception e)
        {
            logger.warn("Exception", e);
            throw new Exception("BalHostAcct : "+e , e);
        }
    }

    public void process(String[] arg0) throws Exception
    {
        this.setBonusBase(Constants.BONUSBASE_HOST);
        this.setBalanceType(Constants.BALANCETYPE_ACCT);
        this.setTransBalIdField("BALANCE_ID");
        this.setTransBalKeyField("REGION_ID");
        this.setBalanceTable("TB_ACCT_BAL");
        this.setBalanceIdField("ACCT_ID");
        this.setBalanceKeyField("REGION_ID");
        
        /*執行balance*/
        balanceProcess();
    }  
}        
