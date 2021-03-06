package tw.com.hyweb.svc.yhdp.batch.summary.computeCdrp;

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
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.info.TbMbCardBalSumInfo;
import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;



public class ComputeCdrpJob implements BatchJob {

	private static final Logger logger = Logger.getLogger(ComputeCdrpJob.class);
	private final ComputeCdrpData computeCdrpData ;
	private final List<TbBonusIssDefInfo> tbBonusIssDefList;
	private final InsertTermBatchHandle termBatchHandle;
	private final int beforeDate;
	
	private List<String> sqlList = new ArrayList<>();
	
	
	public ComputeCdrpJob(ComputeCdrpData computeCdrpData, List<TbBonusIssDefInfo> tbBonusIssDefList, InsertTermBatchHandle termBatchHandle, int beforeDate) {
		// TODO Auto-generated constructor stub
		this.computeCdrpData = computeCdrpData;
		this.tbBonusIssDefList = tbBonusIssDefList;
		this.termBatchHandle = termBatchHandle;
		this.beforeDate = beforeDate;
	}
	
	@Override
	public void action(Connection conn, String batchDate)
			throws Exception {
		// TODO Auto-generated method stub
		computeCdrpData.calculateTheDate(DateUtil.addDate(batchDate, beforeDate));
		logger.debug(computeCdrpData.toString());
		
		StringBuffer sql = new StringBuffer();
		Vector<String> params = new Vector<String>();

		List<Map<String, String>> mbCardBalReqs = new ArrayList<Map<String, String>>();
		
		// 1: 掛卡, 2: 停卡, 3: 到期卡, 4: 關閉自動加值
		for (int i = 1; i<= 4 ; i++){
		
			String typr = Integer.toString(i);
			String impDate = computeCdrpData.getTyp2Date().get(typr);
			
			sql.append(" SELECT CARD_NO, EXPIRY_DATE, IMP_FILE_NAME, IMP_DATE, IMP_TIME FROM TB_MB_CARD_BAL_REQ"); 
			sql.append(" WHERE IMP_DATE <= ?");
			sql.append(" AND REQ_TYPE = ?");
			sql.append(" AND EXP_DATE = ?");
			sql.append(" AND EXISTS (SELECT 1 FROM TB_CARD");
			sql.append(" WHERE TB_CARD.BANK_ID = ?");
			sql.append(" AND TB_MB_CARD_BAL_REQ.CARD_NO = TB_CARD.CARD_NO");
			sql.append(" AND TB_MB_CARD_BAL_REQ.EXPIRY_DATE = TB_CARD.EXPIRY_DATE)");
			//已經計算過的不再計算
			sql.append(" AND NOT EXISTS (SELECT 1 FROM TB_MB_CARD_BAL_SUM");
			sql.append(" WHERE TB_MB_CARD_BAL_REQ.CARD_NO = TB_MB_CARD_BAL_SUM.CARD_NO");
			sql.append(" AND TB_MB_CARD_BAL_REQ.EXPIRY_DATE = TB_MB_CARD_BAL_SUM.EXPIRY_DATE");
			sql.append(" AND TB_MB_CARD_BAL_REQ.IMP_FILE_NAME = TB_MB_CARD_BAL_SUM.IMP_FILE_NAME)");
			
			params.add(impDate);
			params.add(typr);
			params.add("00000000");
			params.add(computeCdrpData.getBankId());
			
			mbCardBalReqs.addAll(DbUtil.getInfoListHashMap(sql.toString(), params, conn));

			sql.delete(0, sql.length());
			params.clear();
		}

		for(Map<String, String> mbCardBalReq : mbCardBalReqs){
			
			TbMbCardBalSumInfo info = new TbMbCardBalSumInfo();
			
			String cardNo = mbCardBalReq.get("CARD_NO");
			String expiryDate = mbCardBalReq.get("EXPIRY_DATE");
			String impFileName = mbCardBalReq.get("IMP_FILE_NAME");
			String impDate = mbCardBalReq.get("IMP_DATE");
			String impTime = mbCardBalReq.get("IMP_TIME");
			
			info.setCardNo(cardNo);
			info.setExpiryDate(expiryDate);
			info.setProcDate(batchDate);
			info.setImpFileName(impFileName);
			info.setImpDate(impDate);
			
			//確認是否已做過退卡退費
			sql.append(" SELECT MIN(IMP_DATE) FROM TB_MB_CARD_BAL_REQ");
			sql.append(" WHERE CARD_NO = ?");
			sql.append(" AND EXPIRY_DATE = ?");
			sql.append(" AND (IMP_DATE < ? ");
			sql.append(" OR ( IMP_DATE = ? AND IMP_TIME < ? ))");
			
			params.add(cardNo);
			params.add(expiryDate);
			params.add(impDate);
			params.add(impDate);
			params.add(impTime);
			
			logger.debug(sql.toString());
			logger.debug(params);
			
			String minImpDate = DbUtil.getString(sql.toString(), params, conn);
			sql.delete(0, sql.length());
			params.clear();
			
			if (!StringUtil.isEmpty(minImpDate)){
				//已做過退卡退費，直接回覆需退款金額為 0
				info.setRmQty(0);
				info.setBonusAfterQty(0);
				info.setBalQty(0);
				info.setRiskDesc(info.getRiskDesc()+"The card has been returned. firstImpDate [" + minImpDate + "]. ");
				sqlList.add(info.toInsertSQL());
				continue;
			}
			else{
				//取得系統電子錢(現金)餘額
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
				
				//撈取倒數1-4筆交易
				sql.append(" SELECT * FROM (SELECT * FROM");
//				sql.append(" (SELECT TERM_DATE, TERM_TIME, ATC, TB_TRANS_DTL.BONUS_BEFORE_QTY, TB_TRANS_DTL.BONUS_CR_QTY, TB_TRANS_DTL.BONUS_DB_QTY, TB_TRANS_DTL.BONUS_AFTER_QTY, TXN_AMT");
				sql.append(" (SELECT NVL(TERM_DATE, TXN_DATE) AS TERM_DATE, NVL(TERM_TIME, TXN_TIME) AS TERM_TIME, ATC, TB_TRANS_DTL.BONUS_BEFORE_QTY, TB_TRANS_DTL.BONUS_CR_QTY, TB_TRANS_DTL.BONUS_DB_QTY, TB_TRANS_DTL.BONUS_AFTER_QTY, TXN_AMT");
				sql.append(" FROM TB_TRANS, TB_TRANS_DTL, TB_CARD");
				sql.append(" WHERE TB_TRANS.CARD_NO = ?");
				sql.append(" AND TB_TRANS.EXPIRY_DATE = ?");
				sql.append(" AND TB_TRANS.TXN_SRC <> ?");
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
//				sql.append(" SELECT TERM_DATE, TERM_TIME, ATC, TB_ONL_TXN_DTL.BONUS_BEFORE_QTY, TB_ONL_TXN_DTL.BONUS_CR_QTY, TB_ONL_TXN_DTL.BONUS_DB_QTY, TB_ONL_TXN_DTL.BONUS_AFTER_QTY, TXN_AMT");
				sql.append(" SELECT NVL(TERM_DATE, TXN_DATE) AS TERM_DATE, NVL(TERM_TIME, TXN_TIME) AS TERM_TIME, ATC, TB_ONL_TXN_DTL.BONUS_BEFORE_QTY, TB_ONL_TXN_DTL.BONUS_CR_QTY, TB_ONL_TXN_DTL.BONUS_DB_QTY, TB_ONL_TXN_DTL.BONUS_AFTER_QTY, TXN_AMT");
				sql.append(" FROM TB_ONL_TXN, TB_ONL_TXN_DTL, TB_CARD");
				sql.append(" WHERE TB_ONL_TXN.CARD_NO = ?");
				sql.append(" AND TB_ONL_TXN.EXPIRY_DATE = ?");
				sql.append(" AND TB_ONL_TXN.STATUS IN (?, ?, ?)");
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
				sql.append(" WHERE ROWNUM <= ?");
				
				params.add(cardNo);
				params.add(expiryDate);
				params.add("A");
				params.add(cardNo);
				params.add(expiryDate);
				params.add("1");
				params.add("C");
				params.add("R");
				params.add("4");
				
				List<HashMap> txns = DbUtil.getInfoListHashMap(sql.toString(), params, conn);
				
				sql.delete(0, sql.length());
				params.clear();
				
				//無交易紀錄
				if (txns.size() == 0){
					//餘額為0
					info.setRmQty(info.getBalQty());
					info.setBonusAfterQty(0);
					info.setRiskDesc(info.getRiskDesc()+"The card does not have any Txn.");
					sqlList.add(info.toInsertSQL());
					continue;
				}
				else{
					TxnData backTxn = null;
					List<Double> qtys = new ArrayList<>();
					
					for(int i = 0; i < txns.size(); i++){
						
						TxnData txnData = new TxnData(txns.get(i));
						
						//檢查交易計算是否正確，錯誤跳離該筆交易
						if (!txnData.checkTxn()){
							
							info.setRiskDesc(info.getRiskDesc()+"checkTxn last" + (i+1) + " Txn is wrong. ");
							
							if (i != 0){
								continue;
							}
						}
						
						//倒數第一筆交易
						if (i == 0){
							info.setBonusAfterQty(txnData.getBonusAfterQty());
							//最後一筆交易餘額 = 系統餘額
							if (txnData.getBonusAfterQty() == qty){
								info.setRmQty(txnData.getBonusAfterQty());
								break;
							}
							qtys.add(txnData.getBonusAfterQty());
						}
						else{
							//交易不連貫直接跳離，信任第一筆 (ATC不連續 且 交易金額不連貫)
							if (Calc.sub(Double.valueOf(backTxn.getAtc()), 1) != Double.valueOf(txnData.getAtc())
									&& backTxn.getBonusBeforeQty() != txnData.getBonusAfterQty()){
								
								info.setRmQty(info.getBonusAfterQty());
								info.setRiskDesc(info.getRiskDesc()+"The last" + (i+1) + " Txn is not continuous. ");
								
								break;
							}
							
							//計算餘額
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
							
							//超過兩筆交易後餘額計算相符
							if (qtys.contains(computeQty)){
								
								info.setRmQty(computeQty);
								break;
							}
							qtys.add(computeQty);
						}
						backTxn = txnData;
						
						//倒數第四筆  皆為比對失敗   信任倒數第一筆餘額
						if(i == txns.size()-1){
							info.setRmQty(info.getBonusAfterQty());
							info.setRiskDesc(info.getRiskDesc()+"All Txn after qty are different.");
						}
					}
					txns.clear();
				}
				
				SupplyJob supplyJob = new SupplyJob(info, batchDate, termBatchHandle, tbBonusIssDefList);
				
				supplyJob.init(conn);
				supplyJob.action(conn);
				
				sqlList.addAll(supplyJob.getSqlList());
				
				//計算遺失扣款金額、加值金額
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
			
		}
		mbCardBalReqs.clear();
	}
	
	@Override
	public void remarkSuccess(Connection conn, String batchDate)
			throws Exception {
		// TODO Auto-generated method stub
		if (sqlList.size() > 0){
	    	int[] result = batchSqlAction(sqlList, conn);
	        if(!checkExecuteBatchResult(sqlList, result))
	        {
	            throw new Exception("update batch result false");
	        }
    	}
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
