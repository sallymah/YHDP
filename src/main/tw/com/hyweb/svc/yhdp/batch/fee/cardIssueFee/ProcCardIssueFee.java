package tw.com.hyweb.svc.yhdp.batch.fee.cardIssueFee;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.IBatchProcess;
import tw.com.hyweb.core.cp.batch.framework.IBatchResult;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFeeCardIssueCfgInfo;
import tw.com.hyweb.service.db.info.TbFeeResultInfo;
import tw.com.hyweb.service.db.info.TbFeeTierInfo;
import tw.com.hyweb.service.db.mgr.TbFeeCardIssueCfgMgr;
import tw.com.hyweb.service.db.mgr.TbFeeResultMgr;
import tw.com.hyweb.service.db.mgr.TbFeeTierMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class ProcCardIssueFee extends AbstractBatchBasic
implements IBatchResult, IBatchProcess {

	private final static Logger logger = Logger.getLogger(ProcCardIssueFee.class);
	private static final String SPRING_PATH = "config" + File.separator +
            "batch" + File.separator +
            "ProcCardIssueFee" + File.separator +
            "spring.xml";
	
	private static final String ROUND_HALF_UP = "O";
	
	protected Connection conn = BatchUtil.getConnection();

    protected String batchDate; //process date
    
    protected String recoverLevel; //ALL 復原全部 or ERR 復原錯誤部分

    protected int sleepTime = 0; //由spring設定, commit之後sleep時間
    
    protected int commitCount = 1000; //spring
    
    protected String creditId = "PREMIUM";
    
    /**
     * Main function<br/>
     * @param args String[]
     */
    public static void main(String[] args)
    {
    	ProcCardIssueFee instance = getInstance(); 
        instance.setBatchDate(System.getProperty("date"));
        instance.setRecoverLevel(System.getProperty("recover").toUpperCase());
        instance.run(null); //run work flow
    }
    
    /**
     * get a ProcFee instance by spring <br/>
     * @return instance
     */
    public static ProcCardIssueFee getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ProcCardIssueFee instance = (ProcCardIssueFee) apContext.getBean("ProcCardIssueFee");
        return instance;
    }
	
	@Override
	public void process(String[] argv) throws Exception {
		// TODO Auto-generated method stub
		try
        {
            logger.info("start process()!\n");
            init();
            
            if ( getRecoverLevel()!=null && 
                 (getRecoverLevel().equals(Constants.RECOVER_LEVEL_ALL)||
                  getRecoverLevel().equals(Constants.RECOVER_LEVEL_ERR))
                ) 
            {
                recoverData();
                return;
            }
            
            Vector cardIssueCfgList = preCondition();
            //檢查JOB_ID、JOB_TIME、MEM_ID
            Vector filterCardIssueCfgList = filterFeeConfig(cardIssueCfgList);
            
            logger.info("feeConfigList.size():"+filterCardIssueCfgList.size()+"\n");
            for (int i=0; i<filterCardIssueCfgList.size(); i++)
            {
                logger.info("action("+i+") ***grantsConfig: "+filterCardIssueCfgList.get(i));
                action((TbFeeCardIssueCfgInfo) filterCardIssueCfgList.get(i));
            }
        }
        catch (Exception e)
        {
            throw new Exception(e.getMessage()); //throw to AbstractBatchBasic.run()
        }
        finally
        {
            DBService.getDBService().close(conn);
            logger.info("end process!\n");
        }
	}
	
	private Vector filterFeeConfig(Vector feeConfigList) throws SQLException {
		// TODO Auto-generated method stub
    	
    	Vector feeConfigVtr = null;
    	
    	if (Layer1Constants.JOB_ID_DEFAULT.equalsIgnoreCase(getBatchResultInfo().getJobId())
		&& Layer1Constants.JOB_TIME_DEFAULT.equalsIgnoreCase(getBatchResultInfo().getJobTime())
		&& StringUtil.isEmpty(getBatchResultInfo().getMemId())){
    		return feeConfigList;
    	}
    	else{
    		feeConfigVtr = new Vector();
    		
    		StringBuffer sql = new StringBuffer();
    		sql.append("SELECT MEM_ID FROM TB_MEMBER WHERE 1=1");
    		
    		if (Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
    			sql.append(" AND JOB_ID IS NULL");
        		sql.append(" AND JOB_TIME IS NULL");
    		}
    		else{
	        	if(!StringUtil.isEmpty(getBatchResultInfo().getJobId()) 
				&& !getBatchResultInfo().getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
	        		sql.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobId()));
		    		if(!StringUtil.isEmpty(getBatchResultInfo().getJobTime()) 
					&& !getBatchResultInfo().getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
		    			sql.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobTime()));
			    	}
		    	}
		    	if(!StringUtil.isEmpty(getBatchResultInfo().getMemId())){
		    		sql.append(" AND MEM_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getMemId()));
		    	}
    		}
    		
            Statement feeStmt = null;
            ResultSet feeRs = null;
            Connection conn = null;
            
            try {
            	List memIds = new ArrayList<>();
            	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            	feeStmt = conn.createStatement();
            	logger.debug("seqnoSql: "+sql.toString());
            	feeRs = feeStmt.executeQuery(sql.toString());
            	while (feeRs.next()) {
            		memIds.add(feeRs.getString(1));
            	}

            	for (int i=0; i<feeConfigList.size(); i++)
                {
            		TbFeeCardIssueCfgInfo feeConfigHm = (TbFeeCardIssueCfgInfo)feeConfigList.get(i);
                    if (memIds.contains(feeConfigHm.getAcqMemId())){
                    	feeConfigVtr.add(feeConfigHm);
                    }
                }
            }
            finally {
                ReleaseResource.releaseDB(conn, feeStmt, feeRs);
            }
    		
    	}
    	
		return feeConfigVtr;
	}
	
	private void action(TbFeeCardIssueCfgInfo tbFeeCardIssueCfgInfo) throws Exception {
		// TODO Auto-generated method stub
		
//		HashMap crdbUnitHm = getFeeDef(tbFeeCardIssueCfgInfo.getFeeCode());
		Vector feeTierList = getFeeTierList(tbFeeCardIssueCfgInfo.getCalRuleId());
		
		// SALE_CODE
		List saleCodes = getSaleCodes(tbFeeCardIssueCfgInfo);
		
		//20170901 計算退卡需MAPPING已發卡數量，未處理當日發卡當日退卡，發卡權利金改為處理PROC_DATE = batchDate -1
		// TB_FEE_CARD_ISSUE_SUM 取出當日累計數量
		Vector sumInfoList = getSumInfoList(tbFeeCardIssueCfgInfo, saleCodes); // select TB_FEE_CARD_ISSUE_SUM
		
		for (int i = 0; i < sumInfoList.size(); i++) {
			try {
				HashMap info = (HashMap) sumInfoList.get(i);
				
				// 沒有發卡量則不需計算    20170829 可能有停卡需計算  改為 != 0
				int issCnt = Integer.valueOf(info.get("THIS_ISS_CNT").toString());
				if ("1".equals(tbFeeCardIssueCfgInfo.getFailCardFlag())){
					issCnt = issCnt - Integer.valueOf(info.get("STOP_ISS_CNT").toString());
				}
				if (issCnt != 0 ){
					TbFeeResultInfo feeResultInfo = calFee(tbFeeCardIssueCfgInfo, feeTierList, info);
					insertFeeResult(feeResultInfo); // insert TB_FEE_RESULT
				}
				remarkSuccess(conn, tbFeeCardIssueCfgInfo, saleCodes); // 成功,
			}
			catch (Exception e) {
				logger.warn("action():" + e);
				remarkFail(conn, tbFeeCardIssueCfgInfo); // 失敗, 全部一起rollback
			}
		}
		conn.commit();
	}

	/**
     * 初始設定<br/>
     * 若不指定batchDate, 預設為系統日<br/>
     * get connection<br/>
     * @throws Exception
     */
    protected void init() throws Exception
    {
        BatchUtil.getNow();
        if (StringUtil.isEmpty(getBatchDate())) {
            setBatchDate(BatchUtil.sysDay); //sysDay is Long
        } else if (!BatchUtil.checkChristianDate(getBatchDate())) {
            String msg = "Invalid date for option -Ddate!";
            throw new Exception(msg);
        }

        logger.info("batchDate:"+getBatchDate());
        logger.info("recoverLevel:"+recoverLevel);

        logger.debug("init(): ok.\n");
    }
    
    /**
     * 取出手續費設定<br/>
     * @return salseConfigList
     * @throws Exception
     */
    protected Vector preCondition() throws Exception
    {
        Vector cardIssueCfgList = new Vector();
        StringBuffer where = new StringBuffer();
        where.append(StringUtil.toSqlValueWithSQuote(batchDate)).append(" BETWEEN VALID_SDATE AND VALID_EDATE");
        
        String order = "ACQ_MEM_ID ";
        
        TbFeeCardIssueCfgMgr tbFeeCardIssueCfgMgr = new TbFeeCardIssueCfgMgr(conn);
        tbFeeCardIssueCfgMgr.queryMultiple(where.toString(), cardIssueCfgList, order);
        
        return cardIssueCfgList;
    }
    
//    /**
//     * @param feeCode
//     * @return targetDefVtr
//     */
//    protected HashMap getFeeDef(String feeCode) throws Exception
//    {
//        StringBuffer sql = new StringBuffer();
//        sql.append("select CREDIT_UNIT, DEBIT_UNIT, ACCOUNT_CODE from TB_FEE_DEF ");
//        sql.append(" where FEE_CODE='").append(feeCode).append("'");
//        Vector targetDefVtr = BatchUtil.getInfoListHashMap(sql.toString());
//        
//        if (targetDefVtr.size()==0) logger.warn("getFeeDef(): no cr db unit ("+feeCode+") in TB_FEE_DEF");
//        return (HashMap) targetDefVtr.get(0);
//    }
    
    protected Vector getFeeTierList(String calRuleId) throws SQLException
    {
        Vector feeTierListVtr = new Vector();
        
        StringBuffer whereSql = new StringBuffer();
        
        whereSql.append(" CAL_RULE_ID = ").append(StringUtil.toSqlValueWithSQuote(calRuleId));
        
        TbFeeTierMgr mgr = new TbFeeTierMgr(conn);
        mgr.queryMultiple(whereSql.toString(), feeTierListVtr, "LOWER_BOUND");
        
        return feeTierListVtr;
    }
    
    protected List getSaleCodes(TbFeeCardIssueCfgInfo tbFeeCardIssueCfgInfo) throws Exception
    {
    	List saleCodes = new ArrayList<>();
    	
    	Statement stmt = null;
        ResultSet rs = null;

        StringBuffer sql = new StringBuffer();
        
        sql.append(" SELECT SALE_CODE FROM TB_FEE_CARD_ISSUE_CFG_DTL");
        sql.append(" WHERE FEE_CONFIG_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getFeeConfigId()));
        sql.append(" AND FEE_CODE = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getFeeCode()));
        sql.append(" AND ISS_MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getIssMemId()));
        sql.append(" AND ACQ_MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getAcqMemId()));
        sql.append(" AND VALID_SDATE = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getValidSdate()));
        		
        try {
        	stmt = conn.createStatement();
        	logger.debug("sql: "+sql.toString());
        	rs = stmt.executeQuery(sql.toString());
        	while (rs.next()) {
        		saleCodes.add(rs.getString(1));
        	}
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
    	
        if (saleCodes.size() < 1){
        	throw new Exception("SaleCode is null. [" 
        			+ tbFeeCardIssueCfgInfo.getFeeConfigId() + ", "
        			+ tbFeeCardIssueCfgInfo.getFeeCode() + ", "
        			+ tbFeeCardIssueCfgInfo.getIssMemId() + ", "
        			+ tbFeeCardIssueCfgInfo.getAcqMemId() + ", "
        			+ tbFeeCardIssueCfgInfo.getValidSdate() + "]");
        }
        
    	return saleCodes;
    }
    
    protected Vector getSumInfoList(TbFeeCardIssueCfgInfo tbFeeCardIssueCfgInfo, List saleCodes) throws SQLException
    {
        Vector sumInfoVtr = new Vector();
        
        StringBuffer sql = new StringBuffer();
        
        sql.append(" SELECT NVL(SUM(ACCU_ISS_CNT),0) AS ACCU_ISS_CNT, NVL(SUM(THIS_ISS_CNT),0) AS THIS_ISS_CNT, NVL(SUM(STOP_ISS_CNT),0) AS STOP_ISS_CNT FROM TB_FEE_CARD_ISSUE_SUM");
        sql.append(" WHERE FEE_CONFIG_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getFeeConfigId()));
        sql.append(" AND FEE_CODE = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getFeeCode()));
        sql.append(" AND MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getAcqMemId()));
        sql.append(" AND PROC_DATE = ").append(StringUtil.toSqlValueWithSQuote(DateUtil.addDate(getBatchDate(), -1)));
        if (saleCodes.size() == 1){
        	sql.append(" AND SALE_CODE = ").append(StringUtil.toSqlValueWithSQuote(saleCodes.get(0).toString()));
    	}
    	else{
    		sql.append(" AND SALE_CODE IN (");
    		for (int i = 0; i < saleCodes.size(); i++){
    			sql.append(StringUtil.toSqlValueWithSQuote(saleCodes.get(i).toString()));
    			if (i < saleCodes.size()-1){
    				sql.append(", ");
    			}
    		}
    		sql.append(")");
    	}
        sql.append(" AND FEE_PROC_DATE IS NULL");
        sql.append(" AND FEE_SUCC_DATE IS NULL");

        logger.debug("SumInfoListSql: " + sql);
        
        sumInfoVtr.addAll(BatchUtil.getInfoListHashMap(sql.toString(), conn));
        
        return sumInfoVtr;
    }
    
    /**
     * 計算手續費
     * @param calBaseValue
     * @param calRuleId
     */
    protected TbFeeResultInfo calFee(TbFeeCardIssueCfgInfo tbFeeCardIssueCfgInfo, Vector feeTierList, HashMap info) throws Exception
    {
    	if (!tbFeeCardIssueCfgInfo.getCalBase().equals(Constants.CAL_BASE_NUMBER)) //N
        {
            throw new Exception(tbFeeCardIssueCfgInfo.getFeeConfigId()+" CalVase is Error: [" + tbFeeCardIssueCfgInfo.getCalBase() +"]");
        }

    	//20170829 可能有停卡需計算
		int issCnt = Integer.valueOf(info.get("THIS_ISS_CNT").toString());
		if ("1".equals(tbFeeCardIssueCfgInfo.getFailCardFlag())){
			issCnt = issCnt - Integer.valueOf(info.get("STOP_ISS_CNT").toString());
		}
		//20170829處理停卡累進問題修改
        //發卡權利金限制計算方式為累進
        //累進Progressive: (calBaseValue-lowerBound)*feeRate+preTierFee
        double thisIssCnt = Math.abs(issCnt);
        double computeIssCnt = Math.abs(issCnt);
        double thisAccuCnt = Double.valueOf(info.get("ACCU_ISS_CNT").toString());
        double computeAccuCnt = Double.valueOf(info.get("ACCU_ISS_CNT").toString());
        double feeAmt = 0.0;
    	
    	TbFeeResultInfo feeResult = new TbFeeResultInfo();
        
    	feeResult.setFeeConfigId(tbFeeCardIssueCfgInfo.getFeeConfigId());
    	feeResult.setFeeCode(tbFeeCardIssueCfgInfo.getFeeCode());
    	feeResult.setProcDate(batchDate);
    	feeResult.setParMon(getBatchDate().substring(4,6));
    	feeResult.setParDay(getBatchDate().substring(6,8));
    	
    	if(issCnt > 0){
	    	feeResult.setCreditUnit(Layer2Util.UNIT_I);
	    	feeResult.setDebitUnit(Layer2Util.UNIT_U);
	    	feeResult.setCreditId(creditId);
	    	feeResult.setDebitId(tbFeeCardIssueCfgInfo.getAcqMemId());
	    	
	    	for( int i= 0; i< feeTierList.size(); i++ ){
	        	TbFeeTierInfo tbFeeTierInfo = (TbFeeTierInfo) feeTierList.get(i);
	        	double upperBound = tbFeeTierInfo.getUpperBound().doubleValue();
	        	double lowerBound = tbFeeTierInfo.getLowerBound().doubleValue();

	        	
	        	if (computeIssCnt > 0){
		        	if ( thisAccuCnt >= upperBound ){
		        		continue;
		        	}
		        	else{
		        		feeResult.setFeeRate(tbFeeTierInfo.getFeeRate());
		        		if ( Calc.add(thisIssCnt, thisAccuCnt) >= upperBound ){
		
		        			double IntervalCnt = Calc.sub(upperBound, computeAccuCnt);
		        			feeAmt = Calc.add(feeAmt, Calc.mul(tbFeeTierInfo.getFeeRate().doubleValue(), IntervalCnt));
		        			computeIssCnt = Calc.sub(computeIssCnt, IntervalCnt);
		        			computeAccuCnt = Calc.add(computeAccuCnt, IntervalCnt);

		        		}
		        		else{
		        			feeAmt = Calc.add(feeAmt, Calc.mul(tbFeeTierInfo.getFeeRate().doubleValue(), computeIssCnt));
		        			break;
		        		}
		        	}
	        	}
	        	else{
	        		break;
	        	}
	        }
    	}
    	else if (issCnt < 0){
    		feeResult.setCreditUnit(Layer2Util.UNIT_U);
	    	feeResult.setDebitUnit(Layer2Util.UNIT_I);
	    	feeResult.setCreditId(tbFeeCardIssueCfgInfo.getAcqMemId());
	    	feeResult.setDebitId(creditId);
	    	
	    	for( int i= feeTierList.size(); i> 0; i-- ){
	    		TbFeeTierInfo tbFeeTierInfo = (TbFeeTierInfo) feeTierList.get(i-1);
	    		double upperBound = tbFeeTierInfo.getUpperBound().doubleValue();
	    		double lowerBound = tbFeeTierInfo.getLowerBound().doubleValue();

	    		//要計算的卡片數量
	    		if (computeIssCnt > 0){
	    			if ( thisAccuCnt <= lowerBound ){
	    				continue;
	    			}
	    			else{
	    				feeResult.setFeeRate(tbFeeTierInfo.getFeeRate());
	    				if ( Calc.sub(thisAccuCnt, thisIssCnt) <= lowerBound ){

	    					double IntervalCnt = Calc.sub(computeAccuCnt, lowerBound);
	    					feeAmt = Calc.add(feeAmt, Calc.mul(tbFeeTierInfo.getFeeRate().doubleValue(), IntervalCnt));
	    					computeIssCnt = Calc.sub(computeIssCnt, IntervalCnt);
	    					computeAccuCnt = Calc.sub(computeAccuCnt, IntervalCnt);

	    				}
	    				else{
	    					feeAmt = Calc.add(feeAmt, Calc.mul(tbFeeTierInfo.getFeeRate().doubleValue(), computeIssCnt));
	    					break;
	    				}
	    			}
	    		}
	    		else{
	    			break;
	    		}
	    	}
    	}
    	
        feeResult.setNumOfTxn(Integer.valueOf(Math.abs(issCnt))); //尚待處理的發卡數量(該日可能只有退卡)
        feeResult.setAmtOfTxn(0); //不會有交易金額
        feeResult.setAccountCode(tbFeeCardIssueCfgInfo.getAccountCode());
        
        //依照carryDigital & carryType 計算小數位
        feeAmt = Calc.roundFloat(feeAmt, 0, ROUND_HALF_UP);
        
        feeResult.setFeeAmt(feeAmt);
        
        return feeResult;
    }
    
    /**
     * 新增TB_FEE_RESULT
     */
    protected void insertFeeResult(TbFeeResultInfo feeResultInfo) throws Exception
    {
        TbFeeResultMgr feeResultMgr = new TbFeeResultMgr(conn);
        
        try
        {
            feeResultMgr.insert(feeResultInfo);
        }
        catch (Exception e)
        {
            logger.warn("setFeeResult() insert:"+e+"\n"+feeResultInfo);
            throw new Exception("insertFeeResult() insert:"+e);
        }
    }
    
    /**
     * 註記計算手續費成功
     */
    protected void remarkSuccess(Connection conn, TbFeeCardIssueCfgInfo tbFeeCardIssueCfgInfo, List saleCodes) throws Exception
    {
        StringBuffer sql = new StringBuffer();
        
        sql.append("UPDATE TB_FEE_CARD_ISSUE_SUM SET");
        
        sql.append(" FEE_PROC_DATE= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        sql.append(" ,FEE_SUCC_DATE= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        sql.append(" ,FEE_RCODE=").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
        
        sql.append(" WHERE FEE_CONFIG_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getFeeConfigId()));
        sql.append(" AND FEE_CODE = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getFeeCode()));
        sql.append(" AND MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getAcqMemId()));
        sql.append(" AND PROC_DATE = ").append(StringUtil.toSqlValueWithSQuote(DateUtil.addDate(getBatchDate(), -1)));
        if (saleCodes.size() == 1){
        	sql.append(" AND SALE_CODE = ").append(StringUtil.toSqlValueWithSQuote(saleCodes.get(0).toString()));
    	}
    	else{
    		sql.append(" AND SALE_CODE IN (");
    		for (int i = 0; i < saleCodes.size(); i++){
    			sql.append(StringUtil.toSqlValueWithSQuote(saleCodes.get(i).toString()));
    			if (i < saleCodes.size()-1){
    				sql.append(", ");
    			}
    		}
    		sql.append(")");
    	}
        sql.append(" AND FEE_PROC_DATE IS NULL");
        sql.append(" AND FEE_SUCC_DATE IS NULL");

        try
        {
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            logger.info("remarkSuccess() "+sql+"\n");
        }
        catch (SQLException e)
        {
            conn.rollback();
            logger.warn(" sql:"+sql);
            throw new Exception("remarkSuccess() "+e);
        }
    }    
    
    
    /**
     * 註記計算手續費失敗
     */
    protected void remarkFail(Connection conn, TbFeeCardIssueCfgInfo tbFeeCardIssueCfgInfo) throws Exception
    {
        conn.rollback();
        
        super.setRcode(Constants.RCODE_2001_WARN);
        
        StringBuffer sql = new StringBuffer();
        
        sql.append("UPDATE TB_FEE_CARD_ISSUE_SUM SET");
        
        sql.append(" FEE_PROC_DATE= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        sql.append(" ,FEE_RCODE=").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_2500_FEE_ERR));
        
        sql.append(" WHERE FEE_CONFIG_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getFeeConfigId()));
        sql.append(" AND FEE_CODE = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getFeeCode()));
        sql.append(" AND MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(tbFeeCardIssueCfgInfo.getAcqMemId()));
        sql.append(" AND PROC_DATE = ").append(StringUtil.toSqlValueWithSQuote(DateUtil.addDate(getBatchDate(), -1)));
        sql.append(" AND FEE_PROC_DATE IS NULL");
        sql.append(" AND FEE_SUCC_DATE IS NULL");
        
        try
        {
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            conn.commit();
        }
        catch (SQLException e)
        {
            conn.rollback();
            logger.warn(" sql:"+sql);
            throw new Exception("remarkFail() "+e);
        }
    }
    
    /**
     * 無論成功或失敗，均可把資料還原：
     * (1) 把當日全部資料清乾淨，程式全部資料重跑
     * (2) 把當日錯誤資料清乾淨，程式只重跑錯誤的部分
     * 
     * 全程使用同一個connection並一起commit
     * 重要：清資料的SQL務必寫入Log File，萬一有錯，方便追查
     */
    protected void recoverData() throws Exception
    {
    	StringBuffer jobWhereSql = new StringBuffer();
    	if (null != getBatchResultInfo()){
	    	
        	if (Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
        		jobWhereSql.append(" AND JOB_ID IS NULL");
        		jobWhereSql.append(" AND JOB_TIME IS NULL");
    		}
    		else{
	        	if(!StringUtil.isEmpty(getBatchResultInfo().getJobId()) 
				&& !getBatchResultInfo().getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
		    		jobWhereSql.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobId()));
		    		if(!StringUtil.isEmpty(getBatchResultInfo().getJobTime()) 
					&& !getBatchResultInfo().getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
		    			jobWhereSql.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getJobTime()));
			    	}
		    	}
		    	if(!StringUtil.isEmpty(getBatchResultInfo().getMemId())){
		    		jobWhereSql.append(" AND MEM_ID=").append(StringUtil.toSqlValueWithSQuote(getBatchResultInfo().getMemId()));
		    	}
    		}
    	}
    	else{
    		logger.warn("tbBatchResultInfo is null.");
    	}
    	
    	
        Connection connSelf = BatchUtil.getConnection();
        
        StringBuffer sql = new StringBuffer();
        
        //RecoverLevel=ALL時 Delete TB_FEE_RESULT
        sql.append("DELETE TB_FEE_RESULT");
        sql.append(" WHERE PROC_DATE= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        sql.append(" AND CREDIT_ID= ").append(StringUtil.toSqlValueWithSQuote(creditId));
        
        if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS(");
        	sql.append(" SELECT 1 FROM TB_FEE_CARD_ISSUE_CFG, TB_MEMBER");
        	sql.append(" WHERE TB_MEMBER.MEM_ID = TB_FEE_CARD_ISSUE_CFG.ACQ_MEM_ID");
        	sql.append(" AND TB_FEE_RESULT.FEE_CONFIG_ID = TB_FEE_CARD_ISSUE_CFG.FEE_CONFIG_ID");
        	sql.append(" AND TB_FEE_RESULT.FEE_CODE = TB_FEE_CARD_ISSUE_CFG.FEE_CODE");
        	sql.append(jobWhereSql.toString());
        	sql.append(" )");
        }
        
        
        try
        {
            logger.info(" recoverData():"+sql.toString());
            DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
        }
        catch (SQLException e)
        {
            connSelf.rollback();
            throw new Exception("recoverData():delete TB_FEE_RESULT. "+e);
        }

        sql.delete(0, sql.length());
        
        //Update TB_FEE_CARD_ISSUE_SUM
        sql.append("UPDATE TB_FEE_CARD_ISSUE_SUM SET ");
        sql.append(" FEE_PROC_DATE = null,");
        sql.append(" FEE_SUCC_DATE = null,");
        sql.append(" FEE_RCODE = ").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
        sql.append(" WHERE FEE_PROC_DATE= ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        
        if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS (");
        	sql.append(" SELECT 1 FROM TB_MEMBER");
        	sql.append(" WHERE TB_MEMBER.MEM_ID = TB_FEE_CARD_ISSUE_SUM.MEM_ID");
        	sql.append(jobWhereSql.toString());
        	sql.append(" )");
        }
        
        try
        {
            logger.info(" recoverData():"+sql.toString());
            DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
            connSelf.commit();
        }
        catch (SQLException e)
        {
            connSelf.rollback();
            throw new Exception("recoverData():Update TB_FEE_CARD_ISSUE_SUM. "+e);
        }
    }

	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	public String getBatchDate() {
		return batchDate;
	}
	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}
	public String getRecoverLevel() {
		return recoverLevel;
	}
	public void setRecoverLevel(String recoverLevel) {
		this.recoverLevel = recoverLevel;
	}
	public int getSleepTime() {
		return sleepTime;
	}
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	public int getCommitCount() {
		return commitCount;
	}
	public void setCommitCount(int commitCount) {
		this.commitCount = commitCount;
	}
	public String getCreditId() {
		return creditId;
	}
	public void setCreditId(String creditId) {
		this.creditId = creditId;
	}
}
