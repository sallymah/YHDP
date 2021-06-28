package tw.com.hyweb.svc.yhdp.batch.ReCalculateBonus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.service.db.info.TbCardBalInfo;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ReCalculateBonusJob implements BatchJob {
	
	private static final Logger logger = Logger.getLogger(ReCalculateBonusJob.class);
	private final Map<String, String> result ;

	public ReCalculateBonusJob(Map<String, String> resultMap) {
		// TODO Auto-generated constructor stub
		this.result = resultMap;
	}

	@Override
	public void action(Connection conn, String batchDate) throws Exception {
		// TODO Auto-generated method stub
		String cardNo = result.get("CARD_NO");
		String expiryDate = result.get("EXPIRY_DATE");
		
		//update or insert TB_CARD_BAL
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT REFUND_DATE, SUM(CR_BONUS_QTY) AS CR_BONUS_QTY, SUM(DB_BONUS_QTY) AS DB_BONUS_QTY, SUM(BAL_BONUS_QTY) AS BAL_BONUS_QTY, COUNT(*) AS CNT");
		sql.append(" FROM TB_CARD_BAL");
		sql.append(" WHERE CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(cardNo));
		sql.append(" AND EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(expiryDate));
		sql.append(" AND BONUS_ID = '1100000001' ");
		sql.append(" GROUP BY REFUND_DATE");
		
		Vector balResult = DbUtil.select(sql.toString(), conn);

		if (balResult.size() > 1){
			throw new Exception(cardNo + ", " + expiryDate + " REFUND_DATE have 2 day.");
		}
		
		logger.debug(cardNo + " " + expiryDate);
		logger.debug("balResult: " + balResult.get(0));
		Vector bal = (Vector) balResult.get(0);
		
		String refundDate = bal.get(0).toString();
		double cr = Double.valueOf(bal.get(1).toString());
		double db = Double.valueOf(bal.get(2).toString());
		double qty = Double.valueOf(bal.get(3).toString());
		int cnt = Integer.valueOf(bal.get(4).toString());
		
		if (cnt > 1){
			StringBuffer updateSql = new StringBuffer();
			updateSql.append("UPDATE TB_CARD_BAL SET ");
			updateSql.append(" CR_BONUS_QTY = ").append(cr);
			updateSql.append(" ,DB_BONUS_QTY = ").append(db);
			updateSql.append(" ,BAL_BONUS_QTY = ").append(qty);
			
			updateSql.append(" WHERE CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(cardNo));
			updateSql.append(" AND EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(expiryDate));
			updateSql.append(" AND BONUS_ID = '1100000001' ");
			updateSql.append(" AND BONUS_SDATE = '00010101' ");
			updateSql.append(" AND BONUS_EDATE = '99991231' ");
			
//			logger.debug(updateSql.toString());
			DbUtil.sqlAction(updateSql.toString(), conn);
		}
		else{
//			logger.debug("insert");
			
			TbCardBalInfo info = new TbCardBalInfo();
			info.setCardNo(cardNo);
			info.setExpiryDate(expiryDate);
			info.setBonusId("1100000001");
			info.setBonusSdate("00010101");
			info.setBonusEdate("99991231");
			info.setCrBonusQty(cr);
			info.setDbBonusQty(db);
			info.setBalBonusQty(qty);
			info.setRefundDate(refundDate);
			
//			logger.debug(info.toInsertSQL());
			DbUtil.sqlAction(info.toInsertSQL(), conn);
		}
	}

	@Override
	public void remarkFailure(Connection arg0, String arg1,
			BatchJobException arg2) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void remarkSuccess(Connection conn, String batchDate) throws Exception {
		// TODO Auto-generated method stub
		//update TB_CAP_TXN_DTL、TB_TRANS_DTL、TB_SETTLE_RESULT
		ArrayList<String> sqlList = new ArrayList<>();
		
		String CardNo = result.get("CARD_NO");
		String expiryDate = result.get("EXPIRY_DATE");
		
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE TB_SETTLE_RESULT SET BONUS_EDATE = '99991231' ");
		sql.append(" WHERE CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(CardNo));
		sql.append(" AND EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(expiryDate));
		sql.append(" AND BONUS_ID = '1100000001' ");
		sql.append(" AND BONUS_SDATE = '00010101' ");
		sql.append(" AND BONUS_EDATE <> '99991231' ");
		sqlList.add(sql.toString());
		
		sql.delete(0, sql.length());
		
		sql.append("UPDATE TB_TRANS_DTL SET BONUS_EDATE = '99991231' ");
		sql.append(" WHERE CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(CardNo));
		sql.append(" AND EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(expiryDate));
		sql.append(" AND BONUS_ID = '1100000001' ");
		sql.append(" AND BONUS_SDATE = '00010101' ");
		sql.append(" AND BONUS_EDATE <> '99991231' ");
		sqlList.add(sql.toString());
		
		sql.delete(0, sql.length());
		
		sql.append("UPDATE TB_CAP_TXN_DTL SET BONUS_EDATE = '99991231' ");
		sql.append(" WHERE CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(CardNo));
		sql.append(" AND EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(expiryDate));
		sql.append(" AND BONUS_ID = '1100000001' ");
		sql.append(" AND BONUS_SDATE = '00010101' ");
		sql.append(" AND BONUS_EDATE <> '99991231' ");
		sqlList.add(sql.toString());
		
		sql.delete(0, sql.length());
		
		sql.append("DELETE TB_CARD_BAL ");
		sql.append(" WHERE CARD_NO = ").append(StringUtil.toSqlValueWithSQuote(CardNo));
		sql.append(" AND EXPIRY_DATE = ").append(StringUtil.toSqlValueWithSQuote(expiryDate));
		sql.append(" AND BONUS_ID = '1100000001' ");
		sql.append(" AND BONUS_SDATE = '00010101' ");
		sql.append(" AND BONUS_EDATE <> '99991231' ");
		sqlList.add(sql.toString());
		
		sql.delete(0, sql.length());
		
		if (sqlList.size() > 0){
			
/*			for(int i = 0 ; i < sqlList.size() ; i++){
				logger.debug(sqlList.get(i));
			}*/
	    	int[] result = batchInsert(sqlList, conn);
	        /*if(!checkExecuteBatchResult(sqlList, result))
	        {
	            throw new Exception("update batch result false");
	        }*/
    	}
	}
	
	public int[] batchInsert(List<String> commandList, Connection conn) throws SQLException
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
