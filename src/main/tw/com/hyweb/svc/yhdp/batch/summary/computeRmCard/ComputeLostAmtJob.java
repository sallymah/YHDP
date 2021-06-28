package tw.com.hyweb.svc.yhdp.batch.summary.computeRmCard;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.string.StringUtil;

public class ComputeLostAmtJob {
	
	private final String cardNo;
	private final String expiryDate;
	protected double lostCr = 0;
	protected double lostDb = 0;

	public ComputeLostAmtJob(String cardNo, String expiryDate) {
		// TODO Auto-generated constructor stub
		this.cardNo = cardNo;
		this.expiryDate = expiryDate;
	}

	public void action(Connection connection) throws SQLException {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();		
		
		sql.append(" SELECT BONUS_BEFORE_QTY, BONUS_AFTER_QTY FROM");
		sql.append(" (SELECT NVL(TERM_DATE, TXN_DATE) AS TERM_DATE, NVL(TERM_TIME, TXN_TIME) AS TERM_TIME, ATC, TB_TRANS_DTL.BONUS_BEFORE_QTY, TB_TRANS_DTL.BONUS_AFTER_QTY, TXN_AMT");
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
		sql.append(" SELECT NVL(TERM_DATE, TXN_DATE) AS TERM_DATE, NVL(TERM_TIME, TXN_TIME) AS TERM_TIME, ATC, TB_ONL_TXN_DTL.BONUS_BEFORE_QTY, TB_ONL_TXN_DTL.BONUS_AFTER_QTY, TXN_AMT");
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
		sql.append(" ORDER BY TERM_DATE, TERM_TIME, ATC");
		
		List<Map<String, String>> txnList = executeQuery(connection, sql.toString(), new Object[] {cardNo, expiryDate, cardNo, expiryDate});
		sql.delete(0, sql.length());
		
		Map<String, String> pkTxn = null;
		double lostAmt = 0;
		
		for(Map<String, String> txn : txnList){
			if(pkTxn == null){
				lostAmt = Calc.sub(Double.valueOf(StringUtil.isEmpty(txn.get("BONUS_BEFORE_QTY")) ? "0": txn.get("BONUS_BEFORE_QTY")), 0);
				pkTxn = txn;
			}
			else{
				lostAmt = Calc.sub(
						Double.valueOf(StringUtil.isEmpty(txn.get("BONUS_BEFORE_QTY")) ? "0": txn.get("BONUS_BEFORE_QTY")), 
						Double.valueOf(StringUtil.isEmpty(pkTxn.get("BONUS_AFTER_QTY")) ? "0": pkTxn.get("BONUS_AFTER_QTY")));
			}
			if(lostAmt > 0){
				lostCr = Calc.add(lostCr, Math.abs(lostAmt));
			}
			else if (lostAmt < 0){
				lostDb = Calc.add(lostDb, Math.abs(lostAmt));
			}
			pkTxn = txn;
		}
	}

	public double getLostCr() {
		return lostCr;
	}

	public void setLostCr(double lostCr) {
		this.lostCr = lostCr;
	}

	public double getLostDb() {
		return lostDb;
	}

	public void setLostDb(double lostDb) {
		this.lostDb = lostDb;
	}
}
