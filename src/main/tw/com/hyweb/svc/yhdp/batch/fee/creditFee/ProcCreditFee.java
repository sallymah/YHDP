/*
 * Version: 1.0.0
 * Date: 2007-01-10
 */
 /*
 * (版權及授權描述)
 *
 * Copyright 2006 (C) Hyweb Technology Co., Ltd. All Rights Reserved.
 *
 * $History: $
 * 
 * 20070907
 *  if tb_sys_config.cancel_flag=1 撈交易加上條件，只撈非取消交易的
 *  入扣帳單位非特店，group by時不加merch_id
 *  
 * 20070816
 * tb_fee_config.cal_From沒填 則getAmtOfTxn()回傳0
 * TB_FEE_CONFIG.CAL_FROM改為允許null, 不會影響程式
 * (若計算基準=以”筆數”計算,就不用設定計算來源)
 *  
 * 20070809
 * 修改TB_FEE_RESULT
 * (1) 刪除欄位ISS_MEM_ID、ACQ_MEM_ID、MERCH_ID
 * (2) 修改PK = FEE_CONFIG_ID, PROC_DATE, CREDIT_UNIT, DEBIT_UNIT, CREDIT_ID, DEBIT_ID
 * 因為TB_FEE_RESULT同時有ISS_MEM_ID、ACQ_MEM_ID、MERCH_ID三個欄位
 * (1) 若是收”發卡手續費”, ACQ_MEM_ID、MERCH_ID無法填值
 * (2) 若是收”收單手續費”, ISS_MEM_ID無法填值
 * (3) 若是收”特店手續費”, ISS_MEM_ID無法填值
 * 
 * 20070723 
 * setTxnCondition() fee_proc_date條件拿掉，否則無法做到同一p_code的交易能計算兩次手續費
 * (因為處理第一組時fee_proc_date就被填了)
 * 
 * 20070625
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/fee
 * 目前手續費的入扣帳對象是by交易來設定, 
 * 可是計算時會把一群不同的交易sum起來計算出手續費, 
 * 最後要insert TB_FEE_RESULT時, 會無法決定出入扣帳單位
 * 所以, 必須更正設定欄位, 更正後的設定方式會與”清算”相同
 * TB_FEE_TXN刪除欄位CREDIT_UNIT、DEBIT_UNIT
 * TB_FEE_DEF刪除欄位SIGN
 * TB_FEE_DEF新增欄位CREDIT_UNIT、DEBIT_UNIT
 * 
 * 20070601 
 * setTxnCondition() 條件cut_date 改 settle_proc_date (endy)
 */
package tw.com.hyweb.svc.yhdp.batch.fee.creditFee;

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
import org.jpos.util.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.DateRange;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.IBatchProcess;
import tw.com.hyweb.core.cp.batch.framework.IBatchResult;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbFeeResultInfo;
import tw.com.hyweb.service.db.mgr.TbFeeResultMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.util.BatchDateUtil;
import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * 計算簽帳手續費(銀行)<br/>
 * (1) 未考慮多幣別的問題<br/>
 * (2) 未考慮原交易與取消交易是否計算手續費(清算已判斷過)<br/>
 * (3) 不考慮fund_cycle, 不填fund_date<br/>
 * (4) 同一筆交易可以收多筆手續費<br/>
 * <br/>
 * 
 * spring:<br/>
 * 手續費計算前, 須判斷清算狀態, 有二種設定方式, 依專案需求選用<br/>
 *  已清算處理, 才可算手續費:
 *  settleCond=” and settle_proc_date !='00000000' and settle_rcode=0000 ”<br/>
 *  已清算成功, 才可算手續費:
 *  settleCond=” and settle_succ_date !='00000000' and settle_rcode=0000 ”<br/>
 * <br/>
 * 
 * usage:<br/>
 *  ant –buildbatch.xml runProcFee –Ddate="" -Drecover=""<br/>
 *  batchDate={–Ddate請輸入YYYYMMDD；若不輸入，預設為系統日}<br/>
 *  recoverLevel={–Drecover請輸入ALL:復原全部資料，ERR:復原錯誤部分}<br/>
 * <br/>
 * 
 * work flow:<br/>
 * private txnCondition=null<br/>
 * 手續費設定<br/>
 *  feeConfigInfoList = preCondition<br/>
 * 手續費計算<br/>
 *  action(fetch each feeConfigInfoList)<br/>
 * <br/>
 * @author Rock
 * 
 */
public class ProcCreditFee extends AbstractBatchBasic
                       implements IBatchResult, IBatchProcess
{
    private final static Logger logger = Logger.getLogger(ProcCreditFee.class);

    private static final String SPRING_PATH = "config" + File.separator +
                                              "batch" + File.separator +
                                              "ProcCreditFee" + File.separator +
                                              "spring.xml";
    
    protected Connection conn = BatchUtil.getConnection();

    protected String batchDate; //process date
    
    protected String recoverLevel; //ALL 復原全部 or ERR 復原錯誤部分

    protected int sleepTime = 0; //由spring設定, commit之後sleep時間
    
    protected int commitCount = 1000; //spring
    
    protected String creditId = "CREDIT";
    
    protected TxnQuery txnQuery;
    
    protected String allMember;

    /**
     * Main function<br/>
     * @param args String[]
     */
    public static void main(String[] args)
    {
        ProcCreditFee instance = getInstance(); 
        instance.setBatchDate(System.getProperty("date"));
        instance.setRecoverLevel(System.getProperty("recover").toUpperCase());
        instance.run(null); //run work flow
    }


    /**
     * get a ProcFee instance by spring <br/>
     * @return instance
     */
    public static ProcCreditFee getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ProcCreditFee instance = (ProcCreditFee) apContext.getBean("ProcCreditFee");
        return instance;
    }

    /**
     * Settle process<br/>
     * @param argv String[]
     */
    public void process(String[] argv) throws Exception
    {
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
            
            Vector salseConfigList = preCondition();
            //檢查JOB_ID、JOB_TIME、MEM_ID
            Vector filterSalseConfigList = filterFeeConfig(salseConfigList);
            
            logger.info("feeConfigList.size():"+filterSalseConfigList.size()+"\n");
            for (int i=0; i<filterSalseConfigList.size(); i++)
            {
                logger.info("action("+i+") ***grantsConfig: "+filterSalseConfigList.get(i));
                action((HashMap) filterSalseConfigList.get(i));
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
                    HashMap feeConfigHm = (HashMap)feeConfigList.get(i);
                    if (memIds.contains(feeConfigHm.get("ACQ_MEM_ID"))){
                    	feeConfigVtr.add(feeConfigHm);
                    }
                    if (Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
                    	if (feeConfigHm.get("ACQ_MEM_ID").toString().equalsIgnoreCase(allMember)){
                    		feeConfigVtr.add(feeConfigHm);
                    	}
                    }
                }
            }
            finally {
                ReleaseResource.releaseDB(conn, feeStmt, feeRs);
            }
    		
    	}
    	
		return feeConfigVtr;
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
            System.out.println(msg);
            throw new Exception(msg);
        }

        setAllMember(Layer2Util.getBatchConfig("ALL_MEMBER"));
        
        logger.info("batchDate:"+getBatchDate());
        logger.info("recoverLevel:"+recoverLevel);
        logger.info("ALL_MEMBER:"+getAllMember());

        logger.debug("init(): ok.\n");
    }


    protected TxnQuery newTxnQuery()
    {
        return new TxnQuery(this);
    }

    /**
     * 取出手續費設定<br/>
     * @return salseConfigList
     * @throws Exception
     */
    protected Vector preCondition() throws Exception
    {
        Vector feeConfigList = new Vector();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT TB_FEE_GRANTS_CONFIG.*, TB_MEMBER.BANK_ID ");
        sql.append("FROM TB_FEE_GRANTS_CONFIG, TB_MEMBER ");
        sql.append("WHERE TB_FEE_GRANTS_CONFIG.ACQ_MEM_ID = TB_MEMBER.MEM_ID(+) ");
        sql.append("AND '").append(getBatchDate()).append("' ");
        sql.append("BETWEEN VALID_SDATE AND VALID_EDATE ");
        sql.append("AND FEE_CODE= 'FF02' ");
        
        String order = "ORDER BY ACQ_MEM_ID ";
        
        feeConfigList.addAll(BatchUtil.getInfoListHashMap(sql.toString() + "AND ACQ_MEM_ID<> '" +allMember+ "' " + order, conn));
        feeConfigList.addAll(BatchUtil.getInfoListHashMap(sql.toString() + "AND ACQ_MEM_ID= '" +allMember+ "' " + order, conn));
        
        return feeConfigList;
    }

    /**
     * 每次處理一個手續費設定<br/>
     * 固定每日計算手續費<br/>
     * @param feeConfigInfo
     * @throws Exception
     */
    protected void action(HashMap grantsConfig) throws Exception
    {
    	CreditFeeConfig creditFeeConfig = setCreditFeeConfig(grantsConfig);
        //logger.info("batchDate:"+getBatchDate()+" ProcCycle:"+feeConf.getProcCycle()+" DayOfWeek:"+DateUtil.getDayOfWeek(getBatchDate()));
        
        //DateRange procPeriod = Layer2Util.getProcPeriod(getBatchDate(), feeConf.getProcCycle());
        DateRange procPeriod = BatchDateUtil.getProcPeriod(getBatchDate(), creditFeeConfig.getProcCycle());
        
        if (procPeriod==null) { 
            logger.error("procPeriod is wrong!");
        }
        else
        {
        	logger.info("procPeriod:"+procPeriod);
        	
            txnQuery = newTxnQuery();
            
			txnQuery.setProcPeriod(procPeriod);

			txnQuery.setTxnCondition(creditFeeConfig);

			// 原交易與取消交易清算與否 if cancel_flag=0 將trans交易填上手續費處理日
			//handleOrigCancelTxn(); // no group by...

			// TB_CREDIT_TXN 找出當天有交易的記錄
			Vector txnInfoList = getTxnInfoList(creditFeeConfig); // select TB_CREDIT_TXN
															// where getTxn
									                        // Condition()
			
			
			for (int i = 0; i < txnInfoList.size(); i++) {
				try {
					
					logger.info(txnInfoList.get(i));
					
					txnQuery.setTxnGroupByCondition((HashMap) txnInfoList
							.get(i));
					CreditFeeResult feeResult = calFee(creditFeeConfig);// ***** 計算手續費 *****
					feeResult.setFeeAmt(calDeduct(creditFeeConfig, feeResult
							.getFeeAmt())); // if手續費抵扣最低基本手續費
					TbFeeResultInfo feeResultInfo = setFeeResultInfo(feeResult,
							creditFeeConfig, (HashMap) txnInfoList.get(i));
					insertFeeResult(feeResultInfo); // insert TB_FEE_RESULT

					remarkSuccess(conn, creditFeeConfig, feeResultInfo); // 成功,
																// 全部一起commit

					if (0 == Math.IEEEremainder(i, getCommitCount())) { // i %
																		// commitCount
						logger.debug("sleep:" + getSleepTime());
						Thread.sleep(getSleepTime());
					}
				} catch (Exception e) {
					logger.warn("action():" + e);
					remarkFail(conn, creditFeeConfig); // 失敗, 全部一起rollback
				}
			} // for each merchIdList
       }
       logger.debug("action() ok!\n");
    }
    
    /**
	 * 將撈出的tb_fee_config & tb_fee_def資料放入FeeConfig物件
	 * 
	 * @param feeConfigInfo
	 * @return
	 * @throws Exception
	 */
    protected CreditFeeConfig setCreditFeeConfig(HashMap feeConfigInfo) throws Exception
    {
    	CreditFeeConfig creditFeeConfig = new CreditFeeConfig();
        try
        {
        	creditFeeConfig.setFeeConfigId((String) feeConfigInfo.get("FEE_CONFIG_ID"));
        	creditFeeConfig.setFeeCode((String) feeConfigInfo.get("FEE_CODE"));
        	creditFeeConfig.setAcqMemId((String) feeConfigInfo.get("ACQ_MEM_ID"));
        	creditFeeConfig.setBankId((String) feeConfigInfo.get("BANK_ID"));
        	creditFeeConfig.setSaleCode((String) feeConfigInfo.get("SALE_CODE"));
        	creditFeeConfig.setFeeTarget((String) feeConfigInfo.get("FEE_TARGET"));
        	
        	//20170316 華銀需求，手續費計算法則改為一對多，依據門檻區分 TB_FEE_CAL_RULE
        	creditFeeConfig.setCalRuleId(getCalRuleId(creditFeeConfig));
        	
        	creditFeeConfig.setFeeConfigDesc((String) feeConfigInfo.get("FEE_CONFIG_DESC"));
        	creditFeeConfig.setAllowDeduct((String) feeConfigInfo.get("ALLOW_DEDUCT"));
        	creditFeeConfig.setFixedFee(((Number) feeConfigInfo.get("FIXED_FEE")).doubleValue());
        	creditFeeConfig.setCalBase((String) feeConfigInfo.get("CAL_BASE"));
        	
        	creditFeeConfig.setProcCycle((String) feeConfigInfo.get("PROC_CYCLE"));
        	creditFeeConfig.setValidSdate((String) feeConfigInfo.get("VALID_SDATE"));
        	creditFeeConfig.setValidEdate((String) feeConfigInfo.get("VALID_EDATE"));
        	creditFeeConfig.setCalRangeFlag((String) feeConfigInfo.get("CAL_RANGE_FLAG"));

        	creditFeeConfig.setFundType(feeConfigInfo.get("FUND_TYPE").toString());
        	if ( creditFeeConfig.getFundType() == null  || 
        			creditFeeConfig.getFundType().equalsIgnoreCase("null") || 
        			creditFeeConfig.getFundType().equalsIgnoreCase("000") )
        		creditFeeConfig.setExpPayDate(null);
            else
            	creditFeeConfig.setExpPayDate(Layer2Util.getCycleDate(conn, getBatchDate(), creditFeeConfig.getFundType()));
        	creditFeeConfig.setAccountCode((String) feeConfigInfo.get("ACCOUNT_CODE"));
        	//creditFeeConfig.setTaxCode("TAX_CODE");
        }
        catch (Exception e)
        {
            throw new Exception("setCreditFeeConfig(): "+e);
        }
        return creditFeeConfig;
    }
    
    protected String getCalRuleId(CreditFeeConfig creditFeeConfig) throws SQLException{
    	
    	StringBuffer cardCntSql = new StringBuffer();
    	cardCntSql.append(" SELECT COUNT(DISTINCT CARD.CARD_NO||CARD.EXPIRY_DATE) FROM (");
    	cardCntSql.append(" SELECT CARD_NO, EXPIRY_DATE FROM TB_TRANS WHERE P_CODE IN ('7717','7737') AND STATUS = '1'");
    	cardCntSql.append(" UNION");
    	cardCntSql.append(" SELECT CARD_NO, EXPIRY_DATE FROM TB_CAP_TXN WHERE P_CODE IN ('7717','7737') AND STATUS = '1'");
    	cardCntSql.append(" UNION");
    	cardCntSql.append(" SELECT CARD_NO, EXPIRY_DATE FROM TB_ONL_TXN WHERE P_CODE IN ('7717','7737') AND STATUS = '1') TXN,");
    	cardCntSql.append(" (SELECT CARD_NO, EXPIRY_DATE FROM TB_CARD");
    	cardCntSql.append(" WHERE BANK_ID = ").append(StringUtil.toSqlValueWithSQuote(creditFeeConfig.getBankId()));
    	cardCntSql.append(" AND SALE_CODE = ").append(StringUtil.toSqlValueWithSQuote(creditFeeConfig.getSaleCode()));
    	cardCntSql.append(" ) CARD");
    	cardCntSql.append(" WHERE CARD.CARD_NO = TXN.CARD_NO");
    	cardCntSql.append(" AND CARD.EXPIRY_DATE = TXN.EXPIRY_DATE");
    	
    	int cardCnt = DbUtil.getInteger(cardCntSql.toString(), conn);
    	
    	String calRuleId = "";
    	
    	StringBuffer sql = new StringBuffer();
    	sql.append(" SELECT CAL_RULE_ID FROM TB_FEE_CAL_RULE");
    	sql.append(" WHERE FEE_CONFIG_ID = ").append(StringUtil.toSqlValueWithSQuote(creditFeeConfig.getFeeConfigId()));
    	sql.append(" AND FEE_CODE = ").append(StringUtil.toSqlValueWithSQuote(creditFeeConfig.getFeeCode()));;
    	sql.append(" AND ACQ_MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(creditFeeConfig.getAcqMemId()));;
    	//聯名銀行才需確認首次加值卡數
    	if (creditFeeConfig.getFeeTarget().equals("1")){
	    	sql.append(" AND ").append(cardCnt).append(" BETWEEN LOWER_BOUND AND UPPER_BOUND");
    	}
    	sql.append(" AND ROWNUM = 1");
    	sql.append(" ORDER BY LOWER_BOUND");
    	
    	logger.debug(sql.toString());
    	
    	calRuleId = DbUtil.getString(sql.toString(), conn);
    	
    	if ( StringUtil.isEmpty(calRuleId) ){
    		throw new SQLException("getCalRuleId is null: " + 
	    	creditFeeConfig.getFeeConfigId() + ", " +
	    	creditFeeConfig.getFeeCode() + ", " +
	    	creditFeeConfig.getAcqMemId() + ", " +
	    	creditFeeConfig.getBankId() + ", " +
	    	creditFeeConfig.getSaleCode());
    	}
    	
    	return calRuleId;
    }


    protected String getPCodeList(Vector pCodeVtr)
    {
        StringBuffer pCodeListSb = new StringBuffer();
        for (int i=0; i<pCodeVtr.size(); i++) { 
            pCodeListSb.append((i==0)?"'":", '").append(pCodeVtr.get(i)).append("'");
        }
        //logger.debug("getPCodeList():"+pCodeListSb.toString());
        return pCodeListSb.toString();
    }

    protected Vector getTxnInfoList(CreditFeeConfig creditFeeConfig)
    {
        Vector txnInfoVtr = new Vector();
        
        StringBuffer txnCond = new StringBuffer( txnQuery.getTxnCondition() );
        txnCond.append(" AND (0=(SELECT count(*) from TB_FEE_RESULT where ");
        txnCond.append(" FEE_CONFIG_ID='").append(creditFeeConfig.getFeeConfigId()).append("'");
        txnCond.append(" AND PROC_DATE='").append(getBatchDate()).append("'");
        txnCond.append(" AND CREDIT_ID='").append(creditId).append("'");
        txnCond.append(" AND DEBIT_ID='").append(creditFeeConfig.getAcqMemId()).append("'");
        txnCond.append("))");
        
        String sql = String.format( "SELECT %s FROM TB_CREDIT_TXN WHERE %s GROUP BY %s "
					        		, txnQuery.getTxnGroupByFields()
					        		, txnCond.toString()
					        		, txnQuery.getTxnGroupByFields());
        
        txnInfoVtr = BatchUtil.getInfoListHashMap(sql);
        
        logger.debug("getTxnInfoList() sqlCmd:"+sql);
        logger.debug("getTxnInfoList() "+txnInfoVtr);
        
        return txnInfoVtr;

        /*
        select { 列舉欄位 } from TB_FEE_RESULT
        where proc_date = { batchDate }
        and merch_id = { merchId }
        and fee_config_id = { feeConfigInfo.feeConfigId }
        If 已存在 return 
        */
    }

    protected Vector getCardTxnInfoList(CreditFeeConfig creditFeeConfig)
    {
        Vector txnInfoVtr = new Vector();
        
        StringBuffer txnCond = new StringBuffer( txnQuery.getTxnCondition() );
        txnCond.append(" AND (0=(SELECT count(*) from TB_FEE_RESULT where ");
        txnCond.append(" FEE_CONFIG_ID='").append(creditFeeConfig.getFeeConfigId()).append("'");
        txnCond.append(" AND PROC_DATE='").append(getBatchDate()).append("'");
        txnCond.append(" AND CREDIT_ID='").append(creditId).append("'");
        txnCond.append(" AND DEBIT_ID='").append(creditFeeConfig.getAcqMemId()).append("'");
        txnCond.append("))");
        
        String sql = String.format( "SELECT %s FROM TB_CREDIT_TXN WHERE %s GROUP BY %s "
					        		, txnQuery.getTxnGroupByFields() +", CARD_NO"
					        		, txnCond.toString()
					        		, txnQuery.getTxnGroupByFields() +", CARD_NO");
        
        txnInfoVtr = BatchUtil.getInfoListHashMap(sql);
        
        logger.debug("getTxnInfoList() sqlCmd:"+sql);
        logger.debug("getTxnInfoList() "+txnInfoVtr);
        
        return txnInfoVtr;

        /*
        select { 列舉欄位 } from TB_FEE_RESULT
        where proc_date = { batchDate }
        and merch_id = { merchId }
        and fee_config_id = { feeConfigInfo.feeConfigId }
        If 已存在 return 
        */
    }

    protected double calDeduct(CreditFeeConfig creditFeeConfig, double feeAmt)
    { 
        double calFeeAmt = 0;
        double fixedFee = creditFeeConfig.getFixedFee();
        String isAllowDeduct = creditFeeConfig.getAllowDeduct();
        
        
        if (isAllowDeduct.equals(Constants.ALLOW_DEBUCT_ALLOW)) //1
        { //可抵扣: 最低手續費為 基本手續費
            if (feeAmt>=fixedFee) {
                calFeeAmt = feeAmt;
                logger.info("calDeduct() ALLOW_DEBUCT_ALLOW:"+isAllowDeduct+" fixedFee:"+fixedFee+"  calFeeAmt = feeAmt = "+ calFeeAmt);
            } else {
                calFeeAmt = fixedFee;
                logger.info("calDeduct() ALLOW_DEBUCT_ALLOW:"+isAllowDeduct+" fixedFee:"+fixedFee+"  calFeeAmt = fixedFee = "+ calFeeAmt);
            }
        }
        else 
        { //不可抵扣: 計算結果+基本手續費
            calFeeAmt = feeAmt + fixedFee;
            logger.info("calDeduct() ALLOW_DEBUCT_ALLOW:"+isAllowDeduct+"  calFeeAmt = feeAmt + fixedFee = "+ feeAmt +" + "+ fixedFee +" = "+calFeeAmt);
        }
        return calFeeAmt;
    }
    
    /**
     * 手續費計算法則 TB_FEE_CAL
     * @param calRuleId
     * @return calRuleHm
     */
    protected CreditFeeRule getCalRule(String calRuleId)
    {
    	
    	
    	
    	CreditFeeRule feeRule = new CreditFeeRule();
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select CAL_FORMULA, CARRY_DIGIT, CARRY_TYPE from TB_FEE_CAL");
        sqlCmd.append(" where CAL_RULE_ID='").append(calRuleId).append("'");
        Vector feeRuleVtr = BatchUtil.getInfoListHashMap(sqlCmd.toString());
        
        //todo if feeRuleVtr.size()==0  
        HashMap feeRuleHm = (HashMap) feeRuleVtr.get(0);
        
        feeRule.setCalFormula((String) feeRuleHm.get("CAL_FORMULA"));
        feeRule.setCarryType((String) feeRuleHm.get("CARRY_TYPE"));
        feeRule.setCarryDigit(((Number) feeRuleHm.get("CARRY_DIGIT")).intValue());
        
        sqlCmd.delete(0, sqlCmd.length());
        
        sqlCmd.append("select CAL_RULE_ID, LOWER_BOUND, UPPER_BOUND, FEE_RATE, PRE_TIER_FEE ");
        sqlCmd.append("from TB_FEE_TIER where CAL_RULE_ID=");
        sqlCmd.append("'").append(calRuleId).append("'");

        Vector feeTierVtr = BatchUtil.getInfoListHashMap(sqlCmd.toString());
        //todo if  feeTierVtr.size()==0 
        
        feeRule.setFeeTierVtr(feeTierVtr);
        
        
        return feeRule;
    }
    
    
    /**
     * 計算手續費
     * @param calBaseValue
     * @param calRuleId
     */
    protected CreditFeeResult calFee(CreditFeeConfig creditFeeConfig) throws Exception
    {
    	CreditFeeResult feeResult = new CreditFeeResult();
        
        Vector cardTxnInfoList = getCardTxnInfoList(creditFeeConfig);
        double sumNumOfTxn = 0.0;
        double sumAmtOfTxn = 0.0;
        double sumFeeAmt = 0.0;
        
        for(int i = 0; i < cardTxnInfoList.size(); i++){
        	
        	logger.info(cardTxnInfoList.get(i));
	        //TB_GRANTS_TXN
	        
//        	feeResult.setNumOfTxn(getNumOfTxn((HashMap) cardTxnInfoList.get(i))); //交易總筆數
//	        feeResult.setAmtOfTxn(getAmtOfTxn((HashMap) cardTxnInfoList.get(i))); //交易總金額
        	double numOfTxn = getNumOfTxn((HashMap) cardTxnInfoList.get(i)); //交易總筆數
        	double amtOfTxn = getAmtOfTxn((HashMap) cardTxnInfoList.get(i)); //交易總金額
        	
	        double calBaseValue = 0.0; //計算手續費基準值
	        if (creditFeeConfig.getCalBase().equals(Constants.CAL_BASE_NUMBER)) //N
	        {
	            calBaseValue = numOfTxn; //依筆數計算
	        }
	        else if (creditFeeConfig.getCalBase().equals(Constants.CAL_BASE_AMOUNT)) //A
	        { 
	            calBaseValue = amtOfTxn; //依金額計算
	        }
	        CreditFeeRule feeRule = getCalRule(creditFeeConfig.getCalRuleId()); //TB_FEE_CAL & TB_FEE_TIER
	        CreditFeeTier feeTier = getFeeTier(calBaseValue, feeRule.getFeeTierVtr());
	        //依計算方法計算手續費
	        double feeAmt = 0.0;
	        double feeRate = feeTier.getFeeRate();
	        if (feeRule.getCalFormula().equals(Constants.CAL_FORMULA_FLAT)) //F
	        { //線性Flat: calBaseValue*FeeRate
	            //calBaseValue
	            feeAmt = Calc.mul(calBaseValue, feeRate);
	            logger.info(" CalFormula:"+feeRule.getCalFormula()+" feeAmt=calBaseValue*FeeRate = "+calBaseValue+" * "+feeRate+" = "+feeAmt); 
	        }
	        else if (feeRule.getCalFormula().equals(Constants.CAL_FORMULA_PROG)) //P
	        { //累進Progressive: (calBaseValue-lowerBound)*feeRate+preTierFee
	            double lowerBound = feeTier.getLowerBound(); //Calc.sub(feeTier.getLowerBound(), 1);
	            double preTierFee = feeTier.getPreTierFee();
	            feeAmt = Calc.sub(calBaseValue, lowerBound);
	            feeAmt = Calc.mul(feeAmt, feeRate);
	            feeAmt = Calc.add(feeAmt, preTierFee);
	            logger.info(" CalFormula:"+feeRule.getCalFormula()+" feeAmt=(calBaseValue-lowerBound)*feeRate+preTierFee = ( "+calBaseValue+" - "+lowerBound+" )* "+feeRate+" + "+preTierFee+" = "+feeAmt); 
	        }
	        
	        //依照carryDigital & carryType 計算小數位
	        feeAmt = Calc.roundFloat(feeAmt, feeRule.getCarryDigit(), feeRule.getCarryType());
	        logger.info(" carryDigital:"+feeRule.getCarryDigit()+" carryType:"+feeRule.getCarryType()+", feeAmt => "+feeAmt);
	        
	        sumNumOfTxn = sumNumOfTxn + numOfTxn;
	        sumAmtOfTxn = sumAmtOfTxn + amtOfTxn;
	        sumFeeAmt = sumFeeAmt + feeAmt;
	        
	        feeResult.setFeeRate(feeRate);
        }
        
        feeResult.setNumOfTxn(sumNumOfTxn); //交易總筆數
        feeResult.setAmtOfTxn(sumAmtOfTxn); //交易總金額
        feeResult.setFeeAmt(sumFeeAmt);
        return feeResult;
    }
    
    protected double getNumOfTxn(HashMap txnInfo)
    { //交易總筆數
        String sqlCmd = String.format("select sum(ttl_cnt) from TB_CREDIT_TXN where %s"
                                      , txnQuery.getTxnCondition()
                                        +" and "
                                        +txnQuery.getTxnGroupByCondition() + "AND CARD_NO = '"+txnInfo.get("CARD_NO")+"'");
        
        Vector vtr = BatchUtil.getInfoList(sqlCmd);
        logger.debug("getNumOfTxn():"+sqlCmd);
        double numOfTxn = ((Number) ((Vector) vtr.get(0)).get(0)).doubleValue();
        
        return numOfTxn;
    }

    
    protected double getAmtOfTxn(HashMap txnInfo) throws Exception
    { //交易總金額
        String sqlCmdStr = String.format("select sum(ttl_amt) from TB_CREDIT_TXN where %s"
                                         , txnQuery.getTxnCondition()
                                         +" and "
                                         +txnQuery.getTxnGroupByCondition() + "AND CARD_NO = '"+txnInfo.get("CARD_NO")+"'");

        logger.debug("getAmtOfTxn():"+sqlCmdStr);
        Vector vtr = BatchUtil.getInfoList(sqlCmdStr);
        double amtOfTxn = ((Number) ((Vector) vtr.get(0)).get(0)).doubleValue();
        return amtOfTxn;
    }
    
    /**
     * 依基準值取得對應手續費率
     * @param calBaseValue
     * @param feeTierVtr
     */
    protected CreditFeeTier getFeeTier(double calBaseValue, Vector feeTierVtr)
    {
    	CreditFeeTier feeTier = new CreditFeeTier();
        HashMap hm = null;
        double lBound = 0;
        double uBound = 0;
        for (int i=0; i<feeTierVtr.size(); i++)
        {
            hm = (HashMap) feeTierVtr.get(i);
            lBound = ((Number) hm.get("LOWER_BOUND")).doubleValue();
            uBound = ((Number) hm.get("UPPER_BOUND")).doubleValue();
            
            //if (calBaseValue >= lBound && calBaseValue <= uBound)
            if (calBaseValue > lBound && calBaseValue <= uBound)
            {
                feeTier.setCalRuleId((String) hm.get("CAL_RULE_ID"));
                feeTier.setFeeRate(((Number) hm.get("FEE_RATE")).doubleValue());
                feeTier.setLowerBound(lBound);
                feeTier.setPreTierFee(((Number) hm.get("PRE_TIER_FEE")).doubleValue());
                logger.info("getFeeTier(): lBound:"+lBound+" uBound:"+uBound+" feeRate:"+feeTier.getFeeRate());
                break;
            }
        }
        
        return feeTier;
    }
    
    /**
     * 設定TbFeeResultInfo
     * @param feeResult
     * @param feeConf
     * @param txnInfo
     * @return
     * @throws Exception
     */
    protected TbFeeResultInfo setFeeResultInfo(CreditFeeResult feeResult, CreditFeeConfig creditFeeConfig, HashMap txnInfo)
                 throws Exception
    {
        TbFeeResultInfo feeResultInfo = new TbFeeResultInfo();
        
        String creditUnit = Layer2Util.UNIT_I;
        String debitUnit = Layer2Util.UNIT_A;
        if (creditFeeConfig.getFeeTarget().equals("1")){
        	debitUnit = Layer2Util.UNIT_U;
        }
        String acCode = creditFeeConfig.getAccountCode();
        
//        String creditId = "SALES";
        String debitId = creditFeeConfig.getAcqMemId();
        ////logger.debug("setFeeResult() creditUnit:"+creditUnit+" creditId:"+creditId+" debitUnit:"+debitUnit+" debitId:"+debitId);
        
        feeResultInfo.setFeeConfigId(creditFeeConfig.getFeeConfigId());
        feeResultInfo.setFeeCode(creditFeeConfig.getFeeCode());
        feeResultInfo.setProcDate(getBatchDate());
        feeResultInfo.setExpPayDate(creditFeeConfig.getExpPayDate()); //撥款日

        feeResultInfo.setCreditUnit(creditUnit);
        feeResultInfo.setDebitUnit(debitUnit);
        feeResultInfo.setAccountCode(acCode); // 21040617 遠鑫作為產出資料的帳務分類
        
        feeResultInfo.setCreditId(creditId);
        feeResultInfo.setDebitId(debitId);
    
        feeResultInfo.setNumOfTxn(feeResult.getNumOfTxn());
        feeResultInfo.setAmtOfTxn(feeResult.getAmtOfTxn());
        feeResultInfo.setFeeAmt(feeResult.getFeeAmt());
        feeResultInfo.setFeeRate(feeResult.getFeeRate());
        
        feeResultInfo.setParMon(getBatchDate().substring(4,6));
        feeResultInfo.setParDay(getBatchDate().substring(6,8));
        
        return feeResultInfo;   
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
    protected void remarkSuccess(Connection conn, CreditFeeConfig creditFeeConfig, TbFeeResultInfo feeResultInfo) throws Exception
    {
        StringBuffer sql = new StringBuffer();
        
        sql.append("UPDATE TB_CREDIT_TXN SET ");
        sql.append("FEE_STATUS= '").append("1").append("', ");
        sql.append("FEE_PROC_DATE= '").append(getBatchDate()).append("' ");
        
        
        sql.append("WHERE " ).append(txnQuery.getTxnCondition());

        try
        {
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            logger.info("remarkSuccess() "+sql+"\n");
            conn.commit();
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
    protected void remarkFail(Connection conn, CreditFeeConfig creditFeeConfig) throws Exception
    {
        StringBuffer sql = new StringBuffer();
        
        sql.append("UPDATE TB_CREDIT_TXN SET ");
        sql.append("FEE_STATUS= '").append("9").append("', ");
        sql.append("FEE_PROC_DATE= '").append(getBatchDate()).append("' ");
        
        sql.append("WHERE " ).append(txnQuery.getTxnCondition());

        try
        {
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            logger.info("remarkSuccess() "+sql+"\n");
            conn.commit();
        }
        catch (SQLException e)
        {
            conn.rollback();
            logger.warn(" sql:"+sql);
            throw new Exception("remarkSuccess() "+e);
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
        sql.append(" DELETE TB_FEE_RESULT");
        sql.append(" WHERE PROC_DATE=").append(StringUtil.toSqlValueWithSQuote(getBatchDate()));
        sql.append(" AND CREDIT_ID=").append(StringUtil.toSqlValueWithSQuote(creditId));
        
        if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS(");
        	sql.append(" SELECT 1 FROM TB_FEE_GRANTS_CONFIG, TB_MEMBER");
        	sql.append(" WHERE TB_MEMBER.MEM_ID = TB_FEE_GRANTS_CONFIG.ACQ_MEM_ID");
        	sql.append(" AND TB_FEE_RESULT.FEE_CONFIG_ID = TB_FEE_GRANTS_CONFIG.FEE_CONFIG_ID");
        	sql.append(" AND TB_FEE_RESULT.FEE_CODE = TB_FEE_GRANTS_CONFIG.FEE_CODE");
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
        
        //Update TB_CREDIT_TXN
        sql.append(" UPDATE TB_CREDIT_TXN set ");
        sql.append(" FEE_PROC_DATE = NULL");
        sql.append(" ,STATUS=").append(StringUtil.toSqlValueWithSQuote("0"));
        sql.append(" WHERE FEE_PROC_DATE=").append(StringUtil.toSqlValueWithSQuote(getBatchDate()));
        
        if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS (");
        	sql.append(" SELECT 1 FROM TB_MEMBER");
        	sql.append(" WHERE TB_MEMBER.MEM_ID = TB_CREDIT_TXN.ACQ_MEM_ID");
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
            throw new Exception("recoverData():Update TB_CREDIT_TXN. "+e);
        }
    }

    public String getBatchDate()
    {
        return batchDate;
    }
    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }
    public String getRecoverLevel()
    {
        return recoverLevel;
    }
    public void setRecoverLevel(String recoverLevel)
    {
        this.recoverLevel = recoverLevel;
    }
    public int getSleepTime()
    {
        return sleepTime;
    }
    public void setSleepTime(int sleepTime)
    {
        this.sleepTime = sleepTime;
    }
    public int getCommitCount()
    {
        return commitCount;
    }
    public void setCommitCount(int commitCount)
    {
        this.commitCount = commitCount;
    }
	public String getCreditId() {
		return creditId;
	}
	public void setCreditId(String creditId) {
		this.creditId = creditId;
	}
	public String getAllMember() {
		return allMember;
	}
	public void setAllMember(String allMember) {
		this.allMember = allMember;
	}
	

}
