package tw.com.hyweb.svc.yhdp.batch.AutoSettlement;

import java.sql.Connection;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.batch.util.beans.TermBatchBean;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;

/**
 * @author Clare
 * 
 */
public class ProcessAutoSettlementJob extends GenericBatchJob {

	private TransferOlnTxn accountInsertSetting;
	private String OnlTxnCondSql;
	public static final String TYPE_AUTOSETTLE = "E";

	private String sysDate = DateUtils.getSystemDate();
    private String sysTime = DateUtils.getSystemTime();
	private String impFileName = null;

	public ProcessAutoSettlementJob(TransferOlnTxn accountInsertSetting,
			String OnlTxnCondSql) {
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
	public void action(Connection connection, String batchDate)
			throws Exception {
		try {
			impFileName = "SIM_CPC_TXN_" + batchDate;

			String batchNo = accountInsertSetting.getBatchNo();
			if (isBlankOrNull(batchNo)) {
				batchNo = SequenceGenerator.getBatchNoByType(connection,
						TYPE_AUTOSETTLE);
			}

			int count = Integer.parseInt(accountInsertSetting.getCount());
			double sumTxnAmt = Double.parseDouble(accountInsertSetting
					.getSumTxnAmt());

			TbTermBatchInfo terminalBatch = new TbTermBatchInfo();
			terminalBatch.setMerchId(accountInsertSetting.getMerchId());
			terminalBatch.setTermId(accountInsertSetting.getTermId());
			terminalBatch.setBatchNo(batchNo);
			terminalBatch.setTxnSrc("B");
			terminalBatch.setTermSettleDate(accountInsertSetting
					.getTermSettleDate());
			terminalBatch.setTermSettleTime(accountInsertSetting
					.getTermSettleTime());
			terminalBatch.setHostTtlCnt(count);
			terminalBatch.setHostTtlAmt(sumTxnAmt);
			terminalBatch.setEdcTtlCnt(count);
			terminalBatch.setEdcTtlAmt(sumTxnAmt);
			terminalBatch.setTermSettleFlag("1");
			terminalBatch.setImpFileName(impFileName);

			TermBatchBean termBatchBean = new TermBatchBean();
			termBatchBean.setTermBatchInfo(terminalBatch);

			DBService.getDBService().sqlAction(termBatchBean.getInsertSql(),
					connection, false);
		} catch (Exception e) {
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
	public void remarkSuccess(Connection connection, String batchDate)
			throws Exception {

		DBService.getDBService().sqlAction(getUpdateOnlTxnSql(batchDate),
				connection, false);

		DBService.getDBService().sqlAction(getUpdateTermSql(batchDate),
				connection, false);
	}

	private String getUpdateOnlTxnSql(String batchDate) {
		StringBuffer setter = new StringBuffer();
		setter.append("Update TB_ONL_TXN ");
		setter.append("set ");
		setter.append("term_settle_date = '");
		setter.append(accountInsertSetting.getTermSettleDate());
		setter.append("', ");
		setter.append("term_settle_time = '");
		setter.append(accountInsertSetting.getTermSettleTime());
		setter.append("', imp_file_name = '");
		setter.append(impFileName);
		setter.append("', imp_file_date = '");
		setter.append(batchDate);
		setter.append("', imp_file_time = '");
		setter.append(sysTime);
		setter.append("' WHERE ");
		setter.append(OnlTxnCondSql);
		setter.append(" AND merch_id = '");
		setter.append(accountInsertSetting.getMerchId());
		setter.append("' AND term_id = '");
		setter.append(accountInsertSetting.getTermId());
		setter.append("' AND batch_no = '");
		setter.append(accountInsertSetting.getBatchNo());
		setter.append("'");
		return setter.toString();
	}

	private String getUpdateTermSql(String batchDate) {
		StringBuffer setter = new StringBuffer();
		setter.append("update TB_TERM ");
		setter.append("set LATEST_SETTLE_DAY = '");
		setter.append(batchDate);
		setter.append("' where ");
		setter.append("merch_id = '");
		setter.append(accountInsertSetting.getMerchId());
		setter.append("' and term_id = '");
		setter.append(accountInsertSetting.getTermId());
		setter.append("'");
		return setter.toString();
	}

	/**
	 * @return the persoSetting
	 */
	public TransferOlnTxn getAccountInsertSetting() {
		return accountInsertSetting;
	}

	public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}

}
