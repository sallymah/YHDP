package tw.com.hyweb.svc.yhdp.batch.AutoSettleAccount;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.beans.TermBatchBean;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.util.date.DateUtil;

/**
 * @author Clare
 *
 */
public class ProcessAutoSettlementJob extends GenericBatchJob
{
    private TransferOlnTxn accountInsertSetting;
    private String OnlTxnCondSql;
    public static final String TYPE_AUTOSETTLE = "E";
    


    public ProcessAutoSettlementJob(TransferOlnTxn accountInsertSetting, String OnlTxnCondSql)
    {
        this.accountInsertSetting = accountInsertSetting;
        this.OnlTxnCondSql = OnlTxnCondSql;

    }

    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
        try
        {
        	String batchNo = accountInsertSetting.getBatchNo();
    		
    		if (isBlankOrNull(batchNo))
    		{
    			batchNo = SequenceGenerator.getBatchNoByType(connection, TYPE_AUTOSETTLE);
    		}
    		
    		int count = Integer.parseInt(accountInsertSetting.getCount());
    		double sumTxnAmt = Double.parseDouble(accountInsertSetting.getSumTxnAmt());

        	TbTermBatchInfo terminalBatch = new TbTermBatchInfo();
        	terminalBatch.setMerchId(accountInsertSetting.getMerchId());
        	terminalBatch.setTermId(accountInsertSetting.getTermId());
        	terminalBatch.setBatchNo(batchNo);
        	terminalBatch.setTxnSrc("B");
            terminalBatch.setTermSettleDate(accountInsertSetting.getTermSettleDate());
            terminalBatch.setTermSettleTime(accountInsertSetting.getTermSettleTime());
            terminalBatch.setHostTtlCnt(count);
            terminalBatch.setHostTtlAmt(sumTxnAmt);
            terminalBatch.setEdcTtlCnt(count);
            terminalBatch.setEdcTtlAmt(sumTxnAmt);
            terminalBatch.setTermSettleFlag("1");

            TermBatchBean termBatchBean = new TermBatchBean();
            termBatchBean.setTermBatchInfo(terminalBatch);
            
            //LOGGER.warn(bean.getInsertSql());
            DBService.getDBService().sqlAction(termBatchBean.getInsertSql(), connection, false);
        }
        catch (Exception e)
        {
            throw new BatchJobException(e, Constants.RCODE_2001_WARN);
        }
    }


    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
		StringBuffer setter = new StringBuffer();
		setter.append("Update TB_ONL_TXN ");
		setter.append("set ");
		setter.append("term_settle_date='");
		setter.append(accountInsertSetting.getTermSettleDate());
		setter.append("', ");
		setter.append("term_settle_time='");
		setter.append(accountInsertSetting.getTermSettleTime());
		setter.append("' WHERE ");
		setter.append(OnlTxnCondSql);
		setter.append(" AND merch_id = '");
		setter.append(accountInsertSetting.getMerchId());
		setter.append("' AND term_id = '");
		setter.append(accountInsertSetting.getTermId());
		setter.append("' AND batch_no = '");
		setter.append(accountInsertSetting.getBatchNo());
		setter.append("'");
		
		//LOGGER.warn(setter.toString());
		DBService.getDBService().sqlAction(setter.toString(), connection, false);
    }


    /**
     * @return the persoSetting
     */
    public TransferOlnTxn getAccountInsertSetting()
    {
        return accountInsertSetting;
    }
    
	public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
	

}
