package tw.com.hyweb.svc.yhdp.batch.summary.procCardIssSum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFeeCardIssueSumInfo;
import tw.com.hyweb.service.db.info.TbFeeCardIssueSumPK;
import tw.com.hyweb.service.db.mgr.TbFeeCardIssueSumMgr;
import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class ProcCardIssSumJob implements BatchJob {
	
	private static final Logger logger = Logger.getLogger(ProcCardIssSumJob.class);
	private final Map<String, String> result ;

	public ProcCardIssSumJob(Map<String, String> result) {
		// TODO Auto-generated constructor stub
		this.result = result;
	}

	@Override
	public void action(Connection connection, String batchDate) throws Exception {
		// TODO Auto-generated method stub
		
		String feeConfigId = result.get("FEE_CONFIG_ID");
		String feeCode = result.get("FEE_CODE");
		String acqMemId = result.get("ACQ_MEM_ID");
		String resetPerYear = result.get("RESET_PER_YEAR");
		String validSdate = result.get("VALID_SDATE");
		String failCardFlag = result.get("FAIL_CARD_FLAG");
		
		List saleCodes = getSaleCodes(connection);
		for (int i = 0; i< saleCodes.size(); i++){
		
			//FEE_CONFIG_ID, FEE_CODE, RESET_PER_YEAR, VALID_SDATE
			String saleCode = saleCodes.get(i).toString();
			
			TbFeeCardIssueSumPK pk = new TbFeeCardIssueSumPK();
			pk.setFeeCode(feeCode);
			pk.setFeeConfigId(feeConfigId);
			pk.setProcDate(DateUtil.addDate(batchDate, -1));
			pk.setSaleCode(saleCode);
			
			TbFeeCardIssueSumInfo yesterdayInfo = new TbFeeCardIssueSumMgr(connection).querySingle(pk);
			
			double ttlIssCnt = 0;
			double accuIssCnt = 0;
			
			if (yesterdayInfo != null){
				
				int stopIssCnt = 0;
				
				if ("1".equals(failCardFlag)){
					logger.debug("[FEE_CONFIG_ID, SALE_CODE]:[" + feeConfigId + ", " + saleCode + "] need clear stop card." );
					
					StringBuffer stopIssSql = new StringBuffer();
					
					stopIssSql.append(" SELECT COUNT(1) FROM TB_CARD");
					stopIssSql.append(" WHERE SALE_CODE = ").append(StringUtil.toSqlValueWithSQuote(saleCode));
					stopIssSql.append(" AND ISSUE_DATE LIKE ").append(StringUtil.toSqlValueWithSQuote(yesterdayInfo.getProcDate().substring(0, 6)+"%"));
					stopIssSql.append(" AND PRIMARY_CARD = ").append(StringUtil.toSqlValueWithSQuote(feeCode.substring(feeCode.length()-1)));
					stopIssSql.append(" AND EXISTS");
					stopIssSql.append(" (SELECT 1 FROM TB_MEMBER");
					stopIssSql.append(" WHERE MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(acqMemId));
					stopIssSql.append(" AND TB_MEMBER.BANK_ID = TB_CARD.BANK_ID)");
					stopIssSql.append(" AND (INACTIVE_DATE = ").append(StringUtil.toSqlValueWithSQuote(yesterdayInfo.getProcDate()));
					stopIssSql.append(" OR REG_DATE = ").append(StringUtil.toSqlValueWithSQuote(yesterdayInfo.getProcDate()));
					stopIssSql.append(" )");
					stopIssSql.append(" AND (CASE WHEN NVL(INACTIVE_DATE,'99991231') < NVL(REG_DATE,'99991231') THEN INACTIVE_DATE ELSE REG_DATE END) = ").append(StringUtil.toSqlValueWithSQuote(yesterdayInfo.getProcDate()));
					
					stopIssCnt = DbUtil.getInteger(stopIssSql.toString(), connection);

					yesterdayInfo.setStopIssCnt(stopIssCnt);
					
					logger.debug("Clear quantity: " + stopIssCnt);
					
					new TbFeeCardIssueSumMgr(connection).update(yesterdayInfo);
				}
				
				ttlIssCnt = Calc.add(yesterdayInfo.getTtlIssCnt().intValue(), yesterdayInfo.getThisIssCnt().intValue());
				
				//??????????????????flage
				if (!(resetPerYear.equals("1") && validSdate.substring(4,8).equals(batchDate.substring(4,8)))){
					accuIssCnt = Calc.sub(Calc.add(yesterdayInfo.getAccuIssCnt().intValue(), yesterdayInfo.getThisIssCnt().intValue()), stopIssCnt);
				}
				else{
					logger.debug("[feeConfigId:"+ feeConfigId +
							", feeCode:"+ feeCode +
							", acqMemId:"+ acqMemId +
							", saleCode:"+ saleCode +
							", resetPerYear:"+ resetPerYear +
							", validSdate:"+ validSdate + "] accuIssCnt clear.");
				}
			}
			
			TbFeeCardIssueSumInfo info = new TbFeeCardIssueSumInfo();
			
			info.setFeeConfigId(feeConfigId);
			info.setFeeCode(feeCode);
			info.setProcDate(batchDate);
			info.setMemId(acqMemId);
			info.setSaleCode(saleCode);
			info.setAccuIssCnt(accuIssCnt);
			info.setThisIssCnt(0);
			info.setStopIssCnt(0); //????????????
			info.setTtlIssCnt(ttlIssCnt);
			String dateTime = DateUtil.getTodayString();
			info.setSysDate(dateTime.substring(0, 8));
			info.setSysTime(dateTime.substring(8, 14));
			
			DBService.getDBService().sqlAction(info.toInsertSQL(), connection, false);
		}
	}
	
	protected List getSaleCodes(Connection connection) throws SQLException
    {
		String feeConfigId = result.get("FEE_CONFIG_ID");
		String feeCode = result.get("FEE_CODE");
		String issMemId = result.get("ISS_MEM_ID");
		String acqMemId = result.get("ACQ_MEM_ID");
		String validSdate = result.get("VALID_SDATE");
		
    	List saleCodes = new ArrayList<>();
    	
    	Statement stmt = null;
        ResultSet rs = null;

        StringBuffer sql = new StringBuffer();
        
        sql.append(" SELECT SALE_CODE FROM TB_FEE_CARD_ISSUE_CFG_DTL");
        sql.append(" WHERE FEE_CONFIG_ID = ").append(StringUtil.toSqlValueWithSQuote(feeConfigId));
        sql.append(" AND FEE_CODE = ").append(StringUtil.toSqlValueWithSQuote(feeCode));
        sql.append(" AND ISS_MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(issMemId));
        sql.append(" AND ACQ_MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(acqMemId));
        sql.append(" AND VALID_SDATE = ").append(StringUtil.toSqlValueWithSQuote(validSdate));
        		
        try {
        	stmt = connection.createStatement();
        	logger.debug("sql: "+sql.toString());
        	rs = stmt.executeQuery(sql.toString());
        	while (rs.next()) {
        		saleCodes.add(rs.getString(1));
        	}
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
    	
    	return saleCodes;
    }

	@Override
	public void remarkFailure(Connection arg0, String arg1,
			BatchJobException arg2) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remarkSuccess(Connection arg0, String arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
