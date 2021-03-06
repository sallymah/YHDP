package tw.com.hyweb.svc.yhdp.batch.summary.computeCdrp;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.preoperation.PreOperationUtil;
import tw.com.hyweb.core.cp.batch.util.BatchSequenceGenerator;
import tw.com.hyweb.core.cp.batch.util.beans.TransBean;
import tw.com.hyweb.core.cp.batch.util.beans.TransDtlBean;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.info.TbCardBalInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTransDtlInfo;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class TrtsRewardJob {
	
	private static final Logger logger = Logger.getLogger(TrtsRewardJob.class);
	
	private final String TRTS_MEM_ID = "00100097";
	
	private final String batchDate;
	private final TbCardInfo cardInfo;
	private final List<TbCardBalInfo> cardBalResults;
	private final List<TbBonusIssDefInfo> tbBonusIssDefList;
	private final TbTermBatchInfo terminalBatch;
	private final String ecashBonusId;
	
	private List<String> sqlList = new ArrayList<>();
	
	public TrtsRewardJob (String batchDate, TbCardInfo cardInfo, List<TbCardBalInfo> cardBalResults, 
			List<TbBonusIssDefInfo> tbBonusIssDefList, TbTermBatchInfo terminalBatch, String ecashBonusId) {
		
		this.batchDate = batchDate;
		this.cardInfo = cardInfo;
		this.terminalBatch = terminalBatch;
		this.cardBalResults = cardBalResults;
		this.tbBonusIssDefList = tbBonusIssDefList;
		this.ecashBonusId = ecashBonusId;
	}

	public double action(Connection conn, String sDate, String eDate) throws Exception {
		// TODO Auto-generated method stub
		
		StringBuffer sb = new StringBuffer();
		Vector params = new Vector<>();
		
		sb.append(" SELECT BONUS_TXN_AMT, BONUS_REMAINS FROM TB_TRANS");
		sb.append(" WHERE CARD_NO = ?");
		sb.append(" AND EXPIRY_DATE = ?");
		sb.append(" AND TXN_DATE BETWEEN ? AND ?");
		sb.append(" AND ACQ_MEM_ID = ?");
		sb.append(" ORDER BY BONUS_TXN_AMT DESC");
		
		params.add(cardInfo.getCardNo());
		params.add(cardInfo.getExpiryDate());
		params.add(sDate);
		params.add(eDate);
		params.add("00100097");
		
		Vector<HashMap> transList = DbUtil.getInfoListHashMap(sb.toString(), params, conn);
		
		/*???????????????????????????????????????*/
		if(transList.size() == 0) {
			return 0;
		}

		double bonusTxnAmt = 0;
		double bonusRemains = 0;
		
		for (HashMap trans : transList) {
			
			bonusTxnAmt = ((BigDecimal)trans.get("BONUS_TXN_AMT")).doubleValue();
			bonusRemains = ((BigDecimal)trans.get("BONUS_REMAINS")).doubleValue();
			
			break;
		}
		
		/*???????????????*/
		double rate = 0;
		
		if(bonusTxnAmt >= 11 && bonusTxnAmt <= 20) {
			rate = 0.10;
		}
		else if (bonusTxnAmt >= 21 && bonusTxnAmt <= 30) {
			rate = 0.15;
		}
		else if (bonusTxnAmt >= 31 && bonusTxnAmt <= 40) {
			rate = 0.20;
		}
		else if (bonusTxnAmt >= 41 && bonusTxnAmt <= 50) {
			rate = 0.25;
		}
		else if (bonusTxnAmt >= 51) {
			rate = 0.30;
		}
		else {
			rate = 0;
		}
		
		double rewardAmt = Math.round(Calc.mul(bonusRemains, rate));
		
		TbTransInfo transaction = addTransaction(conn, rewardAmt);
		addDetail(conn, transaction, rewardAmt);
		
		return rewardAmt;
	}

	private TbTransInfo addTransaction(Connection conn, double rewardAmt) throws Exception
    {
		String lmsInvoiceNumber = BatchSequenceGenerator.getLmsInvoiceNo(batchDate);
		
    	TbTransInfo transaction = makeTransaction(conn, rewardAmt);


    	transaction.setTxnSrc(terminalBatch.getTxnSrc());

    	transaction.setIssMemId(cardInfo.getMemId());
    	transaction.setAcqMemId(TRTS_MEM_ID);
    	transaction.setMerchId(terminalBatch.getMerchId());
    	transaction.setTermId(terminalBatch.getTermId());
    	transaction.setBatchNo(terminalBatch.getBatchNo());
    	transaction.setTermDate(terminalBatch.getTermSettleDate());
    	transaction.setTermTime(terminalBatch.getTermSettleTime());
    	transaction.setTermSettleDate(terminalBatch.getTermSettleDate());
    	transaction.setTermSettleTime(terminalBatch.getTermSettleTime());

    	transaction.setCardNo(cardInfo.getCardNo());
    	transaction.setExpiryDate(cardInfo.getExpiryDate());
    	transaction.setLmsInvoiceNo(lmsInvoiceNumber);

    	transaction.setPCode("7437");
    	transaction.setTxnAmt(rewardAmt);
    	transaction.setTransType("FF");
    	transaction.setTxnDate(terminalBatch.getTermSettleDate());
    	transaction.setTxnTime(terminalBatch.getTermSettleTime());
        transaction.setTermDate(transaction.getTxnDate());
        transaction.setTermTime(transaction.getTxnTime());
        
        transaction.setCutDate(terminalBatch.getCutDate());
        transaction.setCutTime(terminalBatch.getCutTime());
        
        transaction.setImpFileName("MB_CARD_BAL");
        
        TransBean bean = new TransBean();
        bean.setTransInfo(transaction);

        sqlList.add(bean.getInsertSql());
        
        return transaction;
    }
	
	protected TbTransInfo makeTransaction(Connection conn, double rewardAmt)
			throws Exception {
		
		TbTransInfo transInfo = new TbTransInfo();

		double[] sumBefore = new double[LMSContext.getMaxPoint()];
		double[] sumBeforeCr = new double[LMSContext.getMaxPoint()];
		double[] sumBeforeDb = new double[LMSContext.getMaxPoint()];
		double[] sumAfter = new double[LMSContext.getMaxPoint()];
		for ( int i = 0; i < LMSContext.getMaxPoint(); i ++ ){
    		 sumBefore[i] = 0;
    		 sumBeforeCr[i] = 0;
    		 sumBeforeDb[i] = 0;
    		 sumAfter[i] = 0;
		}
         
		for ( TbBonusIssDefInfo tbBonusIssDefInfo : tbBonusIssDefList ){
        	 
			if (cardInfo.getMemId().equals(tbBonusIssDefInfo.getMemId())){
				
				for (TbCardBalInfo infoDtl : cardBalResults){
					
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint1BonusId()))
						sumBefore[0] = sumBefore[0] + infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint2BonusId()))
						sumBefore[1] = sumBefore[1] + infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint3BonusId()))
						sumBefore[2] = sumBefore[2] + infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue();
					if (infoDtl.getBonusId().equalsIgnoreCase(tbBonusIssDefInfo.getPoint4BonusId()))
						sumBefore[3] = sumBefore[3] + infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue();
							
				}
				
				if (ecashBonusId.equalsIgnoreCase(tbBonusIssDefInfo.getPoint1BonusId()))
					sumBeforeCr[0] = sumBeforeCr[0] + rewardAmt;
				if (ecashBonusId.equalsIgnoreCase(tbBonusIssDefInfo.getPoint2BonusId()))
					sumBeforeCr[1] = sumBeforeCr[1] + rewardAmt;
				if (ecashBonusId.equalsIgnoreCase(tbBonusIssDefInfo.getPoint3BonusId()))
					sumBeforeCr[2] = sumBeforeCr[2] + rewardAmt;
				if (ecashBonusId.equalsIgnoreCase(tbBonusIssDefInfo.getPoint4BonusId()))
					sumBeforeCr[3] = sumBeforeCr[3] + rewardAmt;
				
				for ( int i = 0; i < LMSContext.getMaxPoint(); i ++ ){
	        		 sumAfter[i] = sumBefore[i] + sumBeforeCr[i];
	        	 }
			}
		}
		
		transInfo.setChipPoint1Before(sumBefore[0]);
		transInfo.setChipPoint2Before(sumBefore[1]);
		transInfo.setChipPoint3Before(sumBefore[2]);
		transInfo.setChipPoint4Before(sumBefore[3]);

		transInfo.setChipPoint1Cr(sumBeforeCr[0]);
		transInfo.setChipPoint2Cr(sumBeforeCr[1]);
		transInfo.setChipPoint3Cr(sumBeforeCr[2]);
		transInfo.setChipPoint4Cr(sumBeforeCr[3]);

		transInfo.setChipPoint1Db(sumBeforeDb[0]);
		transInfo.setChipPoint2Db(sumBeforeDb[1]);
		transInfo.setChipPoint3Db(sumBeforeDb[2]);
		transInfo.setChipPoint4Db(sumBeforeDb[3]);

		transInfo.setChipPoint1After(sumAfter[0]);
		transInfo.setChipPoint2After(sumAfter[1]);
		transInfo.setChipPoint3After(sumAfter[2]);
		transInfo.setChipPoint4After(sumAfter[3]);
		
		return transInfo;
	}
	
	private void addDetail(Connection conn, TbTransInfo transaction, double rewardAmt) throws SQLException {
		// TODO Auto-generated method stub
		TbTransDtlInfo detail = makeTransactionDetail(conn, transaction, rewardAmt);
    	sqlList.add(PreOperationUtil.getBalCrDbSql(conn, transaction, detail));
    	sqlList.add(addDetail(conn, transaction, detail));
	}
	
	protected TbTransDtlInfo makeTransactionDetail(Connection connection, TbTransInfo transaction, double rewardAmt) {
		TbTransDtlInfo transDtlInfo = new TbTransDtlInfo();
		transDtlInfo.setTxnCode(Constants.TXNCODE_8817);
		
		transDtlInfo.setCutDate(transaction.getCutDate());
		transDtlInfo.setRegionId("TWN");
		transDtlInfo.setBonusBase("C");
		transDtlInfo.setBalanceType("C");
		transDtlInfo.setBalanceId(transaction.getCardNo());
		transDtlInfo.setCardNo(transaction.getCardNo());
		transDtlInfo.setExpiryDate(transaction.getExpiryDate());
		transDtlInfo.setLmsInvoiceNo(transaction.getLmsInvoiceNo());
		transDtlInfo.setBonusId(ecashBonusId);
		transDtlInfo.setBonusSdate("00010101");
		transDtlInfo.setBonusEdate("99991231");
		transDtlInfo.setBonusQty(rewardAmt);
		transDtlInfo.setBalProcDate("00000000");
		
		transDtlInfo.setBonusBeforeQty(0);
		for (TbCardBalInfo infoDtl : cardBalResults){
			if ( infoDtl.getBonusId().equalsIgnoreCase(transDtlInfo.getBonusId()) 
					&&  infoDtl.getBonusSdate().equalsIgnoreCase(transDtlInfo.getBonusSdate()) 
					&&  infoDtl.getBonusEdate().equalsIgnoreCase(transDtlInfo.getBonusEdate()) ){
				
				transDtlInfo.setBonusBeforeQty(infoDtl.getCrBonusQty().doubleValue()- infoDtl.getDbBonusQty().doubleValue() + infoDtl.getBalBonusQty().doubleValue());
			}
		}
		
		transDtlInfo.setBonusCrQty(rewardAmt);
		transDtlInfo.setBonusAfterQty(transDtlInfo.getBonusBeforeQty().doubleValue() + transDtlInfo.getBonusCrQty().doubleValue());
		
		
		return transDtlInfo;
	}
	
	private String addDetail(Connection conn, TbTransInfo transaction, TbTransDtlInfo detail)
    {
    	/*???Region id*/
        if(StringUtil.isEmpty(detail.getRegionId()) & !StringUtil.isEmpty(transaction.getCardNo()) & !StringUtil.isEmpty(transaction.getExpiryDate()))
        {
        	detail.setRegionId(cardInfo.getRegionId());
        }
        
    	TransDtlBean bean = new TransDtlBean();
        bean.setTransInfo(transaction);
        bean.setTransDtlInfo(detail);

        return bean.getInsertSql();
    }

	public List<String> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<String> sqlList) {
		this.sqlList = sqlList;
	}
}
