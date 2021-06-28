package tw.com.hyweb.svc.yhdp.batch.summary.computeRmCard;

import java.util.Map;

import tw.com.hyweb.util.Calc;

public class TxnData {
	
	private String termDate = "";
	private String termTime = "";
	private String atc = "";
	private double bonusBeforeQty = 0;
	private double bonusCrQty = 0;
	private double bonusDbQty = 0;
	private double bonusAfterQty = 0;
	private double txnAmt = 0;
	
	public TxnData (Map<String, String> resultMap){
		
		this.termDate = resultMap.get("TERM_DATE").toString();
		this.termTime = resultMap.get("TERM_TIME").toString();
		this.atc = resultMap.get("ATC") != null ? resultMap.get("ATC").toString():"00000000";
		this.bonusBeforeQty = Double.valueOf(resultMap.get("BONUS_BEFORE_QTY").toString());
		this.bonusCrQty = Double.valueOf(resultMap.get("BONUS_CR_QTY").toString());
		this.bonusDbQty = Double.valueOf(resultMap.get("BONUS_DB_QTY").toString());
		this.bonusAfterQty = Double.valueOf(resultMap.get("BONUS_AFTER_QTY").toString());
		this.txnAmt = Double.valueOf(resultMap.get("TXN_AMT").toString());
		
	}
	
	public boolean checkTxn() {
		// TODO Auto-generated method stub
		// bonusBeforeQty + bonusCrQty - bonusDbQty 
		if (Calc.sub(Calc.add(bonusBeforeQty, bonusCrQty), bonusDbQty) == bonusAfterQty){
			return true;
		}
		return false;
	}

	public String getTermDate() {
		return termDate;
	}

	public void setTermDate(String termDate) {
		this.termDate = termDate;
	}

	public String getTermTime() {
		return termTime;
	}

	public void setTermTime(String termTime) {
		this.termTime = termTime;
	}

	public String getAtc() {
		return atc;
	}

	public void setAtc(String atc) {
		this.atc = atc;
	}

	public double getBonusBeforeQty() {
		return bonusBeforeQty;
	}

	public void setBonusBeforeQty(double bonusBeforeQty) {
		this.bonusBeforeQty = bonusBeforeQty;
	}

	public double getBonusCrQty() {
		return bonusCrQty;
	}

	public void setBonusCrQty(double bonusCrQty) {
		this.bonusCrQty = bonusCrQty;
	}

	public double getBonusDbQty() {
		return bonusDbQty;
	}

	public void setBonusDbQty(double bonusDbQty) {
		this.bonusDbQty = bonusDbQty;
	}

	public double getBonusAfterQty() {
		return bonusAfterQty;
	}

	public void setBonusAfterQty(double bonusAfterQty) {
		this.bonusAfterQty = bonusAfterQty;
	}

	public double getTxnAmt() {
		return txnAmt;
	}

	public void setTxnAmt(double txnAmt) {
		this.txnAmt = txnAmt;
	}
	
}
