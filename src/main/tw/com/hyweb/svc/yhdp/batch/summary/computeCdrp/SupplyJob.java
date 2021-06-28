package tw.com.hyweb.svc.yhdp.batch.summary.computeCdrp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.BatchSequenceGenerator;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.service.db.info.TbBonusIssDefInfo;
import tw.com.hyweb.service.db.info.TbCardBalInfo;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbMbCardBalSumInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.mgr.TbCardBalMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class SupplyJob {

	private static final Logger logger = Logger.getLogger(SupplyJob.class);
	
	private final String batchDate;
	private final TbMbCardBalSumInfo mbCardBalSumInfo;
	private final InsertTermBatchHandle termBatchHandle;
	private final List<TbBonusIssDefInfo> tbBonusIssDefList;
	private String ecashBonusId; 
	private TbCardInfo cardInfo;
	private List<TbCardBalInfo> cardBalResults;
	private List<String> sqlList = new ArrayList<>();
	
	public SupplyJob (TbMbCardBalSumInfo mbCardBalSumInfo, String batchDate, 
			InsertTermBatchHandle termBatchHandle, List<TbBonusIssDefInfo> tbBonusIssDefList) {
		this.mbCardBalSumInfo = mbCardBalSumInfo;
		this.batchDate = batchDate;
		this.termBatchHandle = termBatchHandle;
		this.tbBonusIssDefList = tbBonusIssDefList;
	}
	
	public void init(Connection conn) throws SQLException {
		
		StringBuffer sql = new StringBuffer();
		Vector<String> params = new Vector<String>();
		
		TbCardPK pk = new TbCardPK();
        pk.setCardNo(mbCardBalSumInfo.getCardNo());
        pk.setExpiryDate(mbCardBalSumInfo.getExpiryDate());
        cardInfo = new TbCardMgr(conn).querySingle(pk);
        
        sql.append(" SELECT ECASH_BONUS_ID FROM TB_CARD_PRODUCT");
        sql.append(" WHERE EXISTS"); 
        sql.append(" (SELECT 1 FROM TB_CARD"); 
        sql.append(" WHERE CARD_NO = ?");
        sql.append(" AND EXPIRY_DATE = ?");
        sql.append(" AND TB_CARD_PRODUCT.CARD_PRODUCT = TB_CARD.CARD_PRODUCT)");
		
		params.add(mbCardBalSumInfo.getCardNo());
		params.add(mbCardBalSumInfo.getExpiryDate());
		
		ecashBonusId = DbUtil.getString(sql.toString(), params, conn);
		
		cardBalResults = makeCardBal(conn);
	}
	
	public void action(Connection conn) throws Exception{
		
		/*指定加值下載*/
		appointAction(conn);
		
		/*北捷回饋金*/
		trtsRewardAction(conn);
	}
	
	public void appointAction(Connection conn) throws Exception{
		double notDwAptAmt = 0;
		
		StringBuffer sql = new StringBuffer();
		Vector params = new Vector<>();
		
		sql.append(" SELECT");
		sql.append(" BONUS_BASE, BALANCE_TYPE, BALANCE_ID, CARD_NO, EXPIRY_DATE, AR_SERNO, ACQ_MEM_ID, MERCH_ID, REGION_ID");
		sql.append(" FROM TB_APPOINT_RELOAD");
		sql.append(" WHERE STATUS= ?");
        //DW_TXN_DATE, DW_TXN_TIME, DW_LMS_INVOICE_NO
		sql.append(" AND DW_TXN_DATE IS NULL");
		sql.append(" AND DW_TXN_TIME IS NULL");
		sql.append(" AND DW_LMS_INVOICE_NO IS NULL");
		sql.append(" AND BATCH_RCODE = ?");
		sql.append(" AND CARD_NO = ?");
		sql.append(" AND EXPIRY_DATE = ?");
		sql.append(" AND ? BETWEEN VALID_SDATE AND VALID_EDATE");
		
		params.add("0");
		params.add(Constants.RCODE_0000_OK);
		params.add(mbCardBalSumInfo.getCardNo());
		params.add(mbCardBalSumInfo.getExpiryDate());
		params.add(mbCardBalSumInfo.getImpDate());
		
		List<HashMap<String, String>> aptList = DbUtil.getInfoListHashMap(sql.toString(), params, conn);
		
		sql.delete(0, sql.length());
		params.clear();
		
		HashMap<String, Double> appBonusQtys = new HashMap<>();
		
		for(HashMap<String, String> apt : aptList){
			
			TbTermBatchInfo terminalBatch = termBatchHandle.getTermBatchInfo("B","P", apt.get("MERCH_ID"));
			String lmsInvoiceNumber = BatchSequenceGenerator.getLmsInvoiceNo(batchDate);
			
			AppointJob appointJob = new AppointJob(batchDate, cardInfo, cardBalResults, 
					tbBonusIssDefList, terminalBatch, lmsInvoiceNumber, apt, appBonusQtys);
			appointJob.init(conn);
			appointJob.action(conn);
			sqlList.addAll(appointJob.getSqlList());
			
		}
		notDwAptAmt = appBonusQtys.get(ecashBonusId) == null ? 0 : appBonusQtys.get(ecashBonusId);
		
		mbCardBalSumInfo.setNotDwAptAmt(notDwAptAmt);
	}
	
	public void trtsRewardAction(Connection conn) throws Exception{
		
		double lastMonReward = 0;
		double thisMonReward = 0;
		
		/*計算上一個月北捷回饋金*/
		String lastSDate = (DateUtil.addMonth(mbCardBalSumInfo.getImpDate(), -1)).substring(0, 6) + "01";
		String lastEDate = lastSDate.substring(0, 6) + DateUtil.getLastDayOfMonth(lastSDate);
		
		if(checkTrtsReward(conn, lastSDate) > 0) {
			;
		}
		else {
			TbTermBatchInfo terminalBatch = termBatchHandle.getTermBatchInfo("B", "P", "001000970003200", "000000D1");
			
			TrtsRewardJob trtsRewardJob = new TrtsRewardJob(batchDate, cardInfo, cardBalResults, 
					tbBonusIssDefList, terminalBatch, ecashBonusId);
			
			lastMonReward = trtsRewardJob.action(conn, lastSDate, lastEDate);
			
			sqlList.addAll(trtsRewardJob.getSqlList());
		}

		/*計算本月北捷回饋金*/
		String thisSDate = mbCardBalSumInfo.getImpDate().substring(0, 6) + "01";
		String thisEDate = mbCardBalSumInfo.getImpDate();
		
		
		if(checkTrtsReward(conn, thisSDate) > 0) {
			;
		}
		else {
			TbTermBatchInfo terminalBatch = termBatchHandle.getTermBatchInfo("B", "B", "001000970003200", "000000D1");
			
			TrtsRewardJob trtsRewardJob = new TrtsRewardJob(batchDate, cardInfo, cardBalResults, 
					tbBonusIssDefList, terminalBatch, ecashBonusId);
			
			thisMonReward = trtsRewardJob.action(conn, thisSDate, thisEDate);
			
			sqlList.addAll(trtsRewardJob.getSqlList());
		}
		logger.debug("lastMonReward: " + lastMonReward + " /thisMonReward: " + thisMonReward);
		
		mbCardBalSumInfo.setMetroLastMonthRewardAmt(lastMonReward);
		mbCardBalSumInfo.setMetroThisMonthRewardAmt(thisMonReward);
	}
	
	public int checkTrtsReward(Connection conn, String sDate) throws SQLException{
		/*下個月初到月底*/
		String nextMonSDate = (DateUtil.addMonth(sDate, 1)).substring(0, 6) + "01";
		String nextMonEDate = nextMonSDate.substring(0, 6) + DateUtil.getLastDayOfMonth(nextMonSDate);
		
		//隔月北捷回饋金 Ex:交易日20200101~20200131北捷交易，回饋金應於20200201~20200228之間匯入
		StringBuffer sb = new StringBuffer();
		Vector params = new Vector<>();
		
		sb.append(" SELECT COUNT(1) FROM TB_TRANS");
		sb.append(" WHERE CARD_NO = ?");
		sb.append(" AND EXPIRY_DATE = ?");
		sb.append(" AND ACQ_MEM_ID = ?");
		sb.append(" AND P_CODE = ?");
		sb.append(" AND TRANS_TYPE = ?");
		sb.append(" AND ((TXN_DATE BETWEEN ? AND ?");
		sb.append(" AND (IMP_FILE_NAME <> ? OR IMP_FILE_NAME IS NULL))");
		
		sb.append(" OR IMP_FILE_NAME = ?)");
		
		
		params.add(cardInfo.getCardNo());
		params.add(cardInfo.getExpiryDate());
		params.add("00100097");
		params.add("7437");
		params.add("FF");
		params.add(nextMonSDate);
		params.add(nextMonEDate);
		params.add("MB_CARD_BAL");
		params.add("MB_CARD_BAL");
		
		return DbUtil.getInteger(sb.toString(), params, conn);
	}
	
	private List<TbCardBalInfo> makeCardBal(Connection conn) throws SQLException
    {
		StringBuffer cardBalSqlCmd = new StringBuffer("");
        cardBalSqlCmd.append(" CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(mbCardBalSumInfo.getCardNo()));
        cardBalSqlCmd.append(" AND EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(mbCardBalSumInfo.getExpiryDate()));
        cardBalSqlCmd.append(" AND BONUS_SDATE <= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        cardBalSqlCmd.append(" AND BONUS_EDATE >= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        Vector<TbCardBalInfo> results = new Vector();

        TbCardBalMgr mgr = new TbCardBalMgr(conn);
        mgr.queryMultiple(cardBalSqlCmd.toString(), results);
        
        return results;
    }

	public List<String> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<String> sqlList) {
		this.sqlList = sqlList;
	}
}
