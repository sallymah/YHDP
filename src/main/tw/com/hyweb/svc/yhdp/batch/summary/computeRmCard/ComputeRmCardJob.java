package tw.com.hyweb.svc.yhdp.batch.summary.computeRmCard;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.util.BatchSequenceGenerator;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbRmcardBalSumInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;



public class ComputeRmCardJob implements BatchJob {

	private static final Logger logger = Logger.getLogger(ComputeRmCardJob.class);
	private final Map<String, String> resultMap;
	private final List<TbBonusIssDefInfo> tbBonusIssDefList;
	private final  InsertTermBatchHandle termBatchHandle;
	private final int beforeDate;
	
	private List<String> sqlList = new ArrayList<>();
	
	public ComputeRmCardJob(Map<String, String> resultMap, List<TbBonusIssDefInfo> tbBonusIssDefList, 
			InsertTermBatchHandle termBatchHandle, int beforeDate) {
		// TODO Auto-generated constructor stub
		this.resultMap = resultMap;
		this.tbBonusIssDefList = tbBonusIssDefList;
		this.termBatchHandle = termBatchHandle;
		this.beforeDate = beforeDate;
	}
	
	@Override
	public void action(Connection conn, String batchDate)
			throws Exception {
		// TODO Auto-generated method stub

		StringBuffer sql = new StringBuffer();
		Vector<String> params = new Vector<String>();
		
		TbRmcardBalSumInfo info = new TbRmcardBalSumInfo();
		
		String cardNo = resultMap.get("CARD_NO");
		String expiryDate = resultMap.get("EXPIRY_DATE");
		String txnDate = resultMap.get("TXN_DATE");
		String pCode = resultMap.get("P_CODE");
		
		info.setCardNo(cardNo);
		info.setExpiryDate(expiryDate);
		info.setProcDate(batchDate);
		info.setTxnDate(txnDate);
		info.setPCode(pCode);
		
		//?????????????????????????????????
		sql.append(" SELECT COUNT(1) FROM TB_RMCARD_BAL_SUM");
		sql.append(" WHERE CARD_NO = ?");
		sql.append(" AND EXPIRY_DATE = ?");
		
		params.add(cardNo);
		params.add(expiryDate);
		
		logger.debug(sql.toString());
		logger.debug(params);
		
		int cnt = DbUtil.getInteger(sql.toString(), params, conn);
		sql.delete(0, sql.length());
		params.clear();
		
		if (cnt > 0){
			logger.warn("CardNo ["+ cardNo + "] Has been processed.");
			return;
		}
		else{
			//?????????????????????(??????)??????
			sql.append(" SELECT NVL (SUM(TB_CARD_BAL.CR_BONUS_QTY - TB_CARD_BAL.DB_BONUS_QTY + TB_CARD_BAL.BAL_BONUS_QTY), 0) AS QTY");
			sql.append(" FROM TB_CARD_BAL, TB_CARD");
			sql.append(" WHERE TB_CARD.CARD_NO = ?");
			sql.append(" AND TB_CARD.EXPIRY_DATE = ?");
			sql.append(" AND TB_CARD_BAL.CARD_NO = TB_CARD.CARD_NO");
			sql.append(" AND TB_CARD_BAL.EXPIRY_DATE = TB_CARD.EXPIRY_DATE");
			sql.append(" AND EXISTS (");
			sql.append(" SELECT 1 FROM TB_CARD_PRODUCT");
			sql.append(" WHERE TB_CARD_PRODUCT.CARD_PRODUCT = TB_CARD.CARD_PRODUCT");
			sql.append(" AND TB_CARD_BAL.BONUS_ID = TB_CARD_PRODUCT.ECASH_BONUS_ID)");
			
			params.add(cardNo);
			params.add(expiryDate);
			
			double qty = DbUtil.getNumber(sql.toString(), params, conn).doubleValue();
			info.setBalQty(qty);
			
			sql.delete(0, sql.length());
			params.clear();
			
			//????????????1-4?????????
			sql.append(" SELECT * FROM (SELECT * FROM");
			sql.append(" (SELECT NVL(TERM_DATE, TXN_DATE) AS TERM_DATE, NVL(TERM_TIME, TXN_TIME) AS TERM_TIME, ATC, TB_TRANS_DTL.BONUS_BEFORE_QTY, TB_TRANS_DTL.BONUS_CR_QTY, TB_TRANS_DTL.BONUS_DB_QTY, TB_TRANS_DTL.BONUS_AFTER_QTY, TXN_AMT");
			sql.append(" FROM TB_TRANS, TB_TRANS_DTL, TB_CARD");
			sql.append(" WHERE TB_TRANS.CARD_NO = ?");
			sql.append(" AND TB_TRANS.EXPIRY_DATE = ?");
			sql.append(" AND TB_TRANS.TXN_SRC <> 'A'");
			sql.append(" AND TB_TRANS.ATC IS NOT NULL");
			sql.append(" AND EXISTS (SELECT 1 FROM TB_P_CODE_DEF WHERE (SIGN IN ('P','M') OR P_CODE = '7307') AND TB_TRANS.P_CODE = TB_P_CODE_DEF.P_CODE)");
			sql.append(" AND TB_CARD.CARD_NO = TB_TRANS.CARD_NO");
			sql.append(" AND TB_CARD.EXPIRY_DATE = TB_TRANS.EXPIRY_DATE");
			sql.append(" AND TB_TRANS_DTL.CARD_NO = TB_TRANS.CARD_NO");
			sql.append(" AND TB_TRANS_DTL.EXPIRY_DATE = TB_TRANS.EXPIRY_DATE");
			sql.append(" AND TB_TRANS_DTL.LMS_INVOICE_NO = TB_TRANS.LMS_INVOICE_NO");
			sql.append(" AND EXISTS (");
			sql.append(" SELECT 1 FROM TB_CARD_PRODUCT");
			sql.append(" WHERE TB_CARD_PRODUCT.CARD_PRODUCT = TB_CARD.CARD_PRODUCT");
			sql.append(" AND TB_TRANS_DTL.BONUS_ID = TB_CARD_PRODUCT.ECASH_BONUS_ID)");
			sql.append(" UNION");
			sql.append(" SELECT NVL(TERM_DATE, TXN_DATE) AS TERM_DATE, NVL(TERM_TIME, TXN_TIME) AS TERM_TIME, ATC, TB_ONL_TXN_DTL.BONUS_BEFORE_QTY, TB_ONL_TXN_DTL.BONUS_CR_QTY, TB_ONL_TXN_DTL.BONUS_DB_QTY, TB_ONL_TXN_DTL.BONUS_AFTER_QTY, TXN_AMT");
			sql.append(" FROM TB_ONL_TXN, TB_ONL_TXN_DTL, TB_CARD");
			sql.append(" WHERE TB_ONL_TXN.CARD_NO = ?");
			sql.append(" AND TB_ONL_TXN.EXPIRY_DATE = ?");
			sql.append(" AND TB_ONL_TXN.STATUS IN ('1','C','R')");
			sql.append(" AND TB_ONL_TXN.ATC IS NOT NULL");
			sql.append(" AND EXISTS (SELECT 1 FROM TB_P_CODE_DEF WHERE (SIGN IN ('P','M') OR P_CODE = '7307') AND TB_ONL_TXN.P_CODE = TB_P_CODE_DEF.P_CODE)");
			sql.append(" AND TB_CARD.CARD_NO = TB_ONL_TXN.CARD_NO");
			sql.append(" AND TB_CARD.EXPIRY_DATE = TB_ONL_TXN.EXPIRY_DATE");
			sql.append(" AND TB_ONL_TXN_DTL.CARD_NO = TB_ONL_TXN.CARD_NO");
			sql.append(" AND TB_ONL_TXN_DTL.EXPIRY_DATE = TB_ONL_TXN.EXPIRY_DATE");
			sql.append(" AND TB_ONL_TXN_DTL.LMS_INVOICE_NO = TB_ONL_TXN.LMS_INVOICE_NO");
			sql.append(" AND EXISTS (");
			sql.append(" SELECT 1 FROM TB_CARD_PRODUCT");
			sql.append(" WHERE TB_CARD_PRODUCT.CARD_PRODUCT = TB_CARD.CARD_PRODUCT");
			sql.append(" AND TB_ONL_TXN_DTL.BONUS_ID = TB_CARD_PRODUCT.ECASH_BONUS_ID))");
			sql.append(" ORDER BY TERM_DATE DESC, TERM_TIME DESC, ATC DESC)");
			sql.append(" WHERE ROWNUM <= 4");
			
			List<Map<String, String>> txns = executeQuery(conn, sql.toString(), new Object[] {cardNo, expiryDate, cardNo, expiryDate});
			
			sql.delete(0, sql.length());
			
			//???????????????
			if (txns.size() == 0){
				//?????????0
				info.setRmQty(info.getBalQty());
				info.setBonusAfterQty(0);
				info.setRiskDesc(info.getRiskDesc()+"The card does not have any Txn.");
			}
			else{
				TxnData backTxn = null;
				List<Double> qtys = new ArrayList<>();
				
				for(int i = 0; i < txns.size(); i++){
					
					TxnData txnData = new TxnData(txns.get(i));
					
					//?????????????????????????????????????????????????????????
					if (!txnData.checkTxn()){
						
						info.setRiskDesc(info.getRiskDesc()+"checkTxn last" + (i+1) + " Txn is wrong. ");
						
						if (i != 0){
							continue;
						}
					}
					
					//?????????????????????
					if (i == 0){
						info.setBonusAfterQty(txnData.getBonusAfterQty());
						//???????????????????????? = ????????????
						if (txnData.getBonusAfterQty() == qty){
							info.setRmQty(txnData.getBonusAfterQty());
							break;
						}
						qtys.add(txnData.getBonusAfterQty());
					}
					else{
						//????????????????????????????????????????????? (ATC????????? ??? ?????????????????????)
						if (Calc.sub(Double.valueOf(backTxn.getAtc()), 1) != Double.valueOf(txnData.getAtc())
								&& backTxn.getBonusBeforeQty() != txnData.getBonusAfterQty()){
							
							info.setRmQty(info.getBonusAfterQty());
							info.setRiskDesc(info.getRiskDesc()+"The last" + (i+1) + " Txn is not continuous. ");
							
							break;
						}
						
						//????????????
						double computeQty = txnData.getBonusAfterQty();
						for(int j = 0; j < i; j++){
							computeQty = Calc.sub(Calc.add(
									computeQty, Double.valueOf(txns.get(j).get("BONUS_CR_QTY").toString())), 
										Double.valueOf(txns.get(j).get("BONUS_DB_QTY").toString()));
							
							if( j == i-1 ){
								logger.debug("lset " + i + ": " + computeQty);
								
								switch (i)
				                {
				                    case 1:
				                    	info.setLast2Qty(computeQty);
				                        break;
				                    case 2:
				                    	info.setLast3Qty(computeQty);
				                        break;
				                    case 3:
				                    	info.setLast4Qty(computeQty);
				                        break;
				                }
							}
						}
						
						//???????????????????????????????????????
						if (qtys.contains(computeQty)){
							
							info.setRmQty(computeQty);
							break;
						}
						qtys.add(computeQty);
					}
					backTxn = txnData;
					
					//???????????????  ??????????????????   ???????????????????????????
					if(i == txns.size()-1){
						info.setRmQty(info.getBonusAfterQty());
						info.setRiskDesc(info.getRiskDesc()+"All Txn after qty are different.");
					}
				}
				txns.clear();
			}
			
			//??????????????????????????????
			/*double notDwAptAmt = appointAction(conn, batchDate, info.getCardNo(), info.getExpiryDate(), info.getTxnDate());
			info.setNotDwAptAmt(notDwAptAmt);*/
			
			SupplyJob supplyJob = new SupplyJob(info, batchDate, termBatchHandle, tbBonusIssDefList);
			
			supplyJob.init(conn);
			supplyJob.action(conn);
			
			sqlList.addAll(supplyJob.getSqlList());
			
			//???????????????????????????????????????
			if(info.getBonusAfterQty() != info.getBalQty()){
				ComputeLostAmtJob computeLostAmtJob = new ComputeLostAmtJob(cardNo, expiryDate);
				computeLostAmtJob.action(conn);
				info.setLostCr(computeLostAmtJob.getLostCr());
				info.setLostDb(computeLostAmtJob.getLostDb());
			}
			
			if (info.getRiskDesc() != null && info.getRiskDesc().length() > 600){
				info.setRiskDesc(info.getRiskDesc().substring(0, 600));
			}
			sqlList.add(info.toInsertSQL());
		}
		
		if (sqlList.size() > 0){
	    	int[] result = batchSqlAction(sqlList, conn);
	        if(!checkExecuteBatchResult(sqlList, result))
	        {
	            throw new Exception("update batch result false");
	        }
    	}
		
	}
/*
	private double appointAction(Connection conn, String batchDate, String cardNo, String expiryDate, String txnDate) throws Exception {
		// TODO Auto-generated method stub
		
		double notDwAptAmt = 0;
		
		StringBuffer sql = new StringBuffer();		
		
		sql.append(" SELECT");
		sql.append(" BONUS_BASE, BALANCE_TYPE, BALANCE_ID, CARD_NO, EXPIRY_DATE, AR_SERNO, ACQ_MEM_ID, MERCH_ID, REGION_ID");
		sql.append(" FROM TB_APPOINT_RELOAD");
		sql.append(" WHERE STATUS= '0'");
        //DW_TXN_DATE, DW_TXN_TIME, DW_LMS_INVOICE_NO
		sql.append(" AND DW_TXN_DATE IS NULL");
		sql.append(" AND DW_TXN_TIME IS NULL");
		sql.append(" AND DW_LMS_INVOICE_NO IS NULL");
		sql.append(" AND BATCH_RCODE = ").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
		sql.append(" AND CARD_NO = ?");
		sql.append(" AND EXPIRY_DATE = ?");
		sql.append(" AND ? BETWEEN VALID_SDATE AND VALID_EDATE");
		
		List<Map<String, String>> aptList = executeQuery(conn, sql.toString(), new Object[] {cardNo, expiryDate, txnDate});
		sql.delete(0, sql.length());
		
		TbCardInfo cardInfo = null;
		String ecashBonusId = "";
		HashMap<String, Double> appBonusQtys = new HashMap();
		
		for(Map<String, String> apt : aptList){
			
			if (cardInfo == null){
				TbCardPK pk = new TbCardPK();
		        pk.setCardNo(apt.get("CARD_NO"));
		        pk.setExpiryDate(apt.get("EXPIRY_DATE"));
		        cardInfo = new TbCardMgr(conn).querySingle(pk);
		        
		        sql.append(" SELECT ECASH_BONUS_ID FROM TB_CARD_PRODUCT");
		        sql.append(" WHERE EXISTS"); 
		        sql.append(" (SELECT 1 FROM TB_CARD"); 
		        sql.append(" WHERE CARD_NO = ?");
		        sql.append(" AND EXPIRY_DATE = ?");
		        sql.append(" AND TB_CARD_PRODUCT.CARD_PRODUCT = TB_CARD.CARD_PRODUCT)");
				
				Vector<String> params = new Vector<>();
				params.add(cardNo);
				params.add(expiryDate);
				
				ecashBonusId = DbUtil.getString(sql.toString(), params, conn);
				sql.delete(0, sql.length());
			}
			
			TbTermBatchInfo terminalBatch = InsertTermBatchHandle.getInsertTermBatchHandle().getTermBatchInfo(conn, "B","P", apt.get("MERCH_ID"));
			String lmsInvoiceNumber = BatchSequenceGenerator.getLmsInvoiceNo(batchDate);
			
			AppointJob appointJob = new AppointJob(batchDate, cardInfo, tbBonusIssDefList, terminalBatch, lmsInvoiceNumber, apt, appBonusQtys);
			appointJob.init(conn);
			appointJob.action(conn);
			sqlList.addAll(appointJob.getSqlList());
			
		}
		notDwAptAmt = appBonusQtys.get(ecashBonusId) == null ? 0 : appBonusQtys.get(ecashBonusId);
		
		return notDwAptAmt;
	}*/

	@Override
	public void remarkSuccess(Connection conn, String batchDate)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void remarkFailure(Connection conn, String batchDate,
			BatchJobException batchJobException) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public int[] batchSqlAction(List<String> commandList, Connection conn) throws SQLException
	{
		int[] ret = null;
		if ((commandList != null) && (commandList.size() > 0) && (conn != null))
		{
			Statement statement = null;
			try
			{
				statement = conn.createStatement();
				long before = System.currentTimeMillis();
				for (int i = 0; i < commandList.size(); i++) {
					if ((commandList.get(i) != null) && (((String)commandList.get(i)).length() > 0))
					{
						logger.debug("SQL Command : " + (String)commandList.get(i));
						statement.addBatch((String)commandList.get(i));
					}
				}
				ret = statement.executeBatch();
				long diffTime = System.currentTimeMillis() - before;
				logger.info("SQL batch insert (count:" + commandList.size() + ") time: " + diffTime + " ms");
			}
			catch (SQLException sqle)
			{
				throw sqle;
			}
			finally
			{
				statement.close();
			}
		}
		return ret;
	}
	
	public boolean checkExecuteBatchResult(List<String> commandList, int[] result)
    {
        if(null != result)
        {
            for(int idx = 0; idx < result.length; idx++)
            {
                if(result[idx] != 1)
                {
                	logger.error(commandList.get(idx));
                	logger.error("update count != 1, result[" + idx + "] ,"+result[idx]);
                    return false;
                }
            }
        }
        return true;
    }
}
