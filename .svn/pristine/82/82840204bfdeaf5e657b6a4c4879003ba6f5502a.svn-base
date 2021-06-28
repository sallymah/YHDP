/*
 * Version: 3.0.0
 * Date: 2007-01-29
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;

/**
 * 主機紅利依卡片（卡號＋效期）歸戶之餘額管理<br>
 * 
 * @author Anny
 *         Anny,20070126 更改preCondition(),
 *                       呼叫getBalCardInfos參數增加,輸入是晶片或主機
 *         Anny,20070129 改成繼承BalanceProcessor
 */
public class BalHostCard extends BalanceProcessor
{
    private static final Logger logger = Logger.getLogger(BalHostCard.class);

    /**
     * Entry<br>
     * ant runBalHostCard –Ddate=””<br>
     * batchDate={–Ddate請輸入YYYYMMDD；若不輸入，預設為系統日}<br>
     * @throws Exception 
     */
    public BalHostCard(String batchDate, AbstractBatchBasic batch,int cutDay) throws Exception
    {
        super(batch,cutDay);
        try
        {
            // 開始執行
            logger.info("=====BalHostCard Start=====");
            setBatchDate(batchDate);
            process(null);
            logger.info("=====BalHostCard end=====");
        }
        catch (Exception e)
        {
            logger.warn("Exception", e);
            throw new Exception("BalHostCard : "+e , e);
        }
    }

    public void process(String[] arg0) throws Exception
    {
        this.setBonusBase(Constants.BONUSBASE_HOST);
        this.setBalanceType(Constants.BALANCETYPE_CARD);
        this.setTransBalIdField("BALANCE_ID");
        this.setTransBalKeyField("EXPIRY_DATE");
        this.setBalanceTable("TB_CARD_BAL");
        this.setBalanceIdField("CARD_NO");
        this.setBalanceKeyField("EXPIRY_DATE");
        
        /*執行balance*/
        balanceProcess();
    }
}
