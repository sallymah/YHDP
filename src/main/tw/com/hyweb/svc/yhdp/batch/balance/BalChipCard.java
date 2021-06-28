/*
 * Version: 3.0.0
 * Date: 2007-01-29
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import java.sql.SQLException;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;

/**
 * 晶片紅利依卡片(卡號+效期)歸戶之餘額管理<br>
 * 
 * @author Anny
 *         Anny,20070126 更改preCondition(),
 *                       呼叫getBalCardInfos參數增加,輸入是晶片或主機
 *         Anny,20070129 改成繼承BalanceProcessor
 */
public class BalChipCard extends BalanceProcessor
{
    private static final Logger logger = Logger.getLogger(BalChipCard.class);

    /**
     * Entry<br>
     * ant runBalChipCard –Ddate=””<br>
     * batchDate={–Ddate請輸入YYYYMMDD；若不輸入，預設為系統日}<br>
     * 
     * @throws SQLException
     */
    public BalChipCard(String batchDate, AbstractBatchBasic batch,int cutDay) throws Exception
    {
        super(batch,cutDay);
        try
        {
            // 開始執行
            logger.info("=====BalChipCard Start=====");
            setBatchDate(batchDate);
            process(null);
            logger.info("=====BalChipCard end=====");

        }
        catch (Exception e)
        {
            logger.warn("Exception", e);
            throw new Exception("BalChipCard : "+e , e);
        }
    }
    
    public void process(String[] arg0) throws Exception
    {
        /*設定*/
        setBonusBase(Constants.BONUSBASE_CHIP);
        setBalanceType(Constants.BALANCETYPE_CARD);
        setTransBalIdField("BALANCE_ID");
        setTransBalKeyField("EXPIRY_DATE");
        setBalanceTable("TB_CARD_BAL");
        setBalanceIdField("CARD_NO");
        setBalanceKeyField("EXPIRY_DATE");
        
        /*執行balance*/
        balanceProcess();
    }
}
