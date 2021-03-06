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
package tw.com.hyweb.svc.yhdp.batch.fee;

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

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.DateRange;
//import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.BatchException;
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
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * 計算手續費<br/>
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
public class ProcFee extends AbstractBatchBasic
                       implements IBatchResult, IBatchProcess
{
    private final static Logger logger = Logger.getLogger(ProcFee.class);

    private static final String SPRING_PATH = "config" + File.separator +
                                              "batch" + File.separator +
                                              "ProcFee" + File.separator +
                                              "spring.xml";
    
    protected Connection conn = BatchUtil.getConnection();

    protected String batchDate; //process date
    
    protected String recoverLevel; //ALL 復原全部 or ERR 復原錯誤部分

    protected int sleepTime = 0; //由spring設定, commit之後sleep時間
    
    protected int commitCount = 1000; //spring
    
    protected int scale = 0; //小數點後幾位
    
    protected String settleCond; //spring定義,是否清算成功(有填settle_succ_date)的才處理

    protected TxnQuery txnQuery;
    
    protected String allMember;
    
    protected final String TXN_NOTE_HEAD = "FEE";
    
    protected String updateTxnNoteSql;//see recoverData()
    
    protected String cancelFlag = Layer2Util.getBatchConfig("CANCEL_FLAG");
    
    private List notInCreditIds = null;
    
    /**
     * Main function<br/>
     * @param args String[]
     */
    public static void main(String[] args)
    {
        ProcFee instance = getInstance(); 
        
        String batchDate = System.getProperty("date");
        if (StringUtil.isEmpty(batchDate)) {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }
        else if (!DateUtil.isValidDate(batchDate)) {
        	logger.info("invalid batchDate('" + batchDate + "') using system date!");
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }
        instance.setBatchDate(batchDate);
        instance.setRecoverLevel(System.getProperty("recover").toUpperCase());
        instance.run(null); //run work flow
    }


    /**
     * get a ProcFee instance by spring <br/>
     * @return instance
     */
    public static ProcFee getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ProcFee instance = (ProcFee) apContext.getBean("ProcFee");
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
            
            Vector feeConfigList = preCondition();
            //檢查JOB_ID、JOB_TIME、MEM_ID
            Vector filterFeeConfigList = filterFeeConfig(feeConfigList);
            
            logger.info("filterFeeConfigList.size():"+filterFeeConfigList.size()+"\n");
            for (int i=0; i<filterFeeConfigList.size(); i++)
            {
                logger.info("action("+i+") ***feeConfig: "+filterFeeConfigList.get(i));
                action((HashMap) filterFeeConfigList.get(i));
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

        //代表所有單位的代號
        setAllMember(Layer2Util.getBatchConfig("ALL_MEMBER"));
        
        setUpdateTxnNoteSql();
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
     * @return feeConfigList
     * @throws Exception
     */
    protected Vector preCondition() throws Exception
    {
        Vector feeConfigList = new Vector();
        StringBuffer sql = new StringBuffer();
        sql.append("select FEE_CONFIG_ID, FEE_CONFIG_DESC, FEE_CODE, ");
        sql.append("ISS_MEM_ID, ACQ_MEM_ID, ALLOW_DEDUCT, FIXED_FEE, CAL_BASE, ");
        sql.append("CAL_FROM, CAL_RULE_ID, PROC_CYCLE, VALID_SDATE, VALID_EDATE, FUND_TYPE, FEE_TARGET, BANK_ID, SALE_CODE ");
        sql.append("FROM TB_FEE_CONFIG, TB_MEMBER ");
        sql.append("WHERE '").append(getBatchDate()).append("' ");
        sql.append("BETWEEN VALID_SDATE and VALID_EDATE ");
        sql.append("AND TB_FEE_CONFIG.ACQ_MEM_ID = TB_MEMBER.MEM_ID(+) ");
        
        String order = "order by FEE_CONFIG_ID, FEE_CODE ";
        
        //銀行手續費
        feeConfigList.addAll(BatchUtil.getInfoListHashMap
        		(sql.toString() + "AND ISS_MEM_ID<>'" + allMember + "' AND ACQ_MEM_ID<>'" + allMember + "' AND FEE_TARGET = '1' " + order, conn));
        //收單手續費
        feeConfigList.addAll(BatchUtil.getInfoListHashMap
        		(sql.toString() + "AND ISS_MEM_ID<>'" + allMember + "' AND ACQ_MEM_ID<>'" + allMember + "' AND FEE_TARGET = '0' " + order, conn));
        feeConfigList.addAll(BatchUtil.getInfoListHashMap
        		(sql.toString() + "AND ISS_MEM_ID<>'" + allMember + "' AND ACQ_MEM_ID='" + allMember + "' AND FEE_TARGET = '0' " + order, conn));
        feeConfigList.addAll(BatchUtil.getInfoListHashMap
        		(sql.toString() + "AND ISS_MEM_ID='" + allMember + "' AND ACQ_MEM_ID<>'" + allMember + "' AND FEE_TARGET = '0' " + order, conn));
        feeConfigList.addAll(BatchUtil.getInfoListHashMap
        		(sql.toString() + "AND ISS_MEM_ID='" + allMember + "' AND ACQ_MEM_ID='" + allMember + "' AND FEE_TARGET = '0' " + order, conn));
        
        logger.debug("feeConfigList: " + feeConfigList);
        
        return feeConfigList;
    }

    /**
     * 每次處理一個手續費設定<br/>
     * 固定每日計算手續費<br/>
     * @param feeConfigInfo
     * @throws Exception
     */
    protected void action(HashMap feeConfigInfo) throws Exception
    {
        FeeConfig feeConf = setFeeConfig(feeConfigInfo);
        //logger.info("batchDate:"+getBatchDate()+" ProcCycle:"+feeConf.getProcCycle()+" DayOfWeek:"+DateUtil.getDayOfWeek(getBatchDate()));
        
        //DateRange procPeriod = Layer2Util.getProcPeriod(getBatchDate(), feeConf.getProcCycle());
        DateRange procPeriod = BatchDateUtil.getProcPeriod(getBatchDate(), feeConf.getProcCycle());
        
        if (procPeriod==null) { 
            logger.error("procPeriod is wrong!");
        }
        else
        {
        	logger.info("procPeriod:"+procPeriod);
        	
            txnQuery = newTxnQuery();

			txnQuery.setProcPeriod(procPeriod);

			Vector pCodeVtr = getFeeTxn(feeConf.getFeeCode()); // TB_FEE_TXN
			if (pCodeVtr.size() == 0)
				return;
			String pCodeList = getPCodeList(pCodeVtr); // TB_FEE_TXN

			txnQuery.setTxnCondition(feeConf, pCodeList, feeConf.getFeeCode());

			// 原交易與取消交易清算與否 if cancel_flag=0 將trans交易填上手續費處理日
			handleOrigCancelTxn(); // no group by...

			// TB_TRANS 找出當天有交易的記錄
			Vector txnInfoList = getTxnInfoList(feeConf); // select TB_TRANS
															// where getTxn
									                        // Condition()
			for (int i = 0; i < txnInfoList.size(); i++) {
				try {
					
					logger.info(txnInfoList.get(i));
					
					txnQuery.setTxnGroupByCondition((HashMap) txnInfoList
							.get(i));
			        
					FeeResult feeResult = calFee(feeConf);// ***** 計算手續費 *****

					// if手續費抵扣最低基本手續費
					feeResult.setFeeAmt(calDeduct(feeConf, feeResult.getFeeAmt())); 

					TbFeeResultInfo feeResultInfo = setFeeResultInfo(feeResult, feeConf, (HashMap) txnInfoList.get(i));

					// insert TB_FEE_RESULT
					insertFeeResult(feeResultInfo);

					remarkSuccess(conn, feeConf, feeResultInfo); 
					// 成功全部一起commit

					// i % commitCount
					if (0 == Math.IEEEremainder(i, getCommitCount())) { 
						logger.debug("sleep:" + getSleepTime());
						Thread.sleep(getSleepTime());
					}
				} catch (Exception e) {
					logger.warn("action():" + e);
					remarkFail(conn, feeConf); // 失敗, 全部一起rollback
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
    protected FeeConfig setFeeConfig(HashMap feeConfigInfo) throws Exception
    {
        FeeConfig feeConfig = new FeeConfig();
        try
        {
            feeConfig.setFeeConfigId((String) feeConfigInfo.get("FEE_CONFIG_ID"));
            feeConfig.setAcqMemId((String) feeConfigInfo.get("ACQ_MEM_ID"));
            feeConfig.setIssMemId((String) feeConfigInfo.get("ISS_MEM_ID"));
            feeConfig.setFeeCode((String) feeConfigInfo.get("FEE_CODE"));
            feeConfig.setFixedFee(((Number) feeConfigInfo.get("FIXED_FEE")).doubleValue());
            feeConfig.setIsAllowDeduct((String) feeConfigInfo.get("ALLOW_DEDUCT"));
            feeConfig.setCalBase((String) feeConfigInfo.get("CAL_BASE"));
            feeConfig.setCalFrom((String) feeConfigInfo.get("CAL_FROM"));
            feeConfig.setCalRuleId((String) feeConfigInfo.get("CAL_RULE_ID"));
            feeConfig.setProcCycle((String) feeConfigInfo.get("PROC_CYCLE"));
            feeConfig.setValidSdate((String) feeConfigInfo.get("VALID_SDATE"));
            feeConfig.setValidEdate((String) feeConfigInfo.get("VALID_EDATE"));
            feeConfig.setFundType((String) feeConfigInfo.get("FUND_TYPE"));
            if ( feeConfig.getFundType() == null  || 
            		feeConfig.getFundType().equalsIgnoreCase("null") || 
            		feeConfig.getFundType().equalsIgnoreCase("000") )
            	feeConfig.setExpPayDate(null);
            else
            	feeConfig.setExpPayDate(Layer2Util.getCycleDate(conn, getBatchDate(), feeConfig.getFundType()));
            feeConfig.setBankId((String) feeConfigInfo.get("BANK_ID"));
            feeConfig.setSaleCode((String) feeConfigInfo.get("SALE_CODE"));
            feeConfig.setFeeTarget((String) feeConfigInfo.get("FEE_TARGET"));
            
            HashMap crdbUnitHm = getFeeDef(feeConfig.getFeeCode());
            feeConfig.setCreditUnit((String) crdbUnitHm.get("CREDIT_UNIT"));
            feeConfig.setDebitUnit((String) crdbUnitHm.get("DEBIT_UNIT"));
            feeConfig.setAccountCode((String) crdbUnitHm.get("ACCOUNT_CODE"));          
            
        }
        catch (Exception e)
        {
            throw new Exception("setFeeConfig()"+e);
        }
        logger.debug("feeConfig: {" + feeConfig.toString() + "}");
        return feeConfig;
    }


    /**
     * 
     * @param feeCode
     * @return targetTxnVtr
     */
    protected Vector getFeeTxn(String feeCode) throws Exception
    {
        StringBuffer sqlCmd = new StringBuffer();
        sqlCmd.append("select P_CODE from TB_FEE_TXN"); //20070625 , CREDIT_UNIT, DEBIT_UNIT 拿掉 改放在tb_settle_def
        sqlCmd.append(" where FEE_CODE='").append(feeCode).append("'");
        Vector targetTxnVtr = BatchUtil.getInfoList(sqlCmd.toString());

        Vector<String> vtr = new Vector<String>();
        for (int i=0; i<targetTxnVtr.size(); i++)
        {
            String pCode = (String) ((Vector) targetTxnVtr.get(i)).get(0);
            vtr.add(pCode);
        }

        if (vtr.size()==0) {
            logger.info("getFeeTxn(): no P_CODE in TB_FEE_TXN (feeCode="+feeCode+")");
        }
        return vtr;
    }

    /**
     * @param feeCode
     * @return targetDefVtr
     */
    protected HashMap getFeeDef(String feeCode) throws Exception
    {
        StringBuffer sql = new StringBuffer();
        sql.append("select CREDIT_UNIT, DEBIT_UNIT, ACCOUNT_CODE from TB_FEE_DEF ");
        sql.append(" where FEE_CODE='").append(feeCode).append("'");
        Vector targetDefVtr = BatchUtil.getInfoListHashMap(sql.toString());
        
        if (targetDefVtr.size()==0) logger.warn("getFeeDef(): no cr db unit ("+feeCode+") in TB_FEE_DEF");
        return (HashMap) targetDefVtr.get(0);
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

    protected Vector getTxnInfoList(FeeConfig feeConf) throws SQLException
    {
        Vector txnInfoVtr = new Vector();
        
        StringBuffer txnCond = new StringBuffer( txnQuery.getTxnCondition() );
        
        
        //recover後重跑，已經計算過手續費的不再重覆計算
        txnCond.append(" and (0=(select count(*) from TB_FEE_RESULT ");
        txnCond.append(" where FEE_CONFIG_ID='").append(feeConf.getFeeConfigId()).append("'");
        txnCond.append(" and PROC_DATE='").append(getBatchDate()).append("'");
        
        
        if (!StringUtil.isEmpty(feeConf.getAcqMemId())
        		&& !StringUtil.isEmpty(feeConf.getIssMemId())
        		&& !feeConf.getAcqMemId().equalsIgnoreCase(allMember)
        		&& !feeConf.getIssMemId().equalsIgnoreCase(allMember)){
        	
        	Connection conn = null;
        	try
            {
        		conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
        		
        		String creditId = Layer2Util.getCreditDebitId(conn, feeConf.getAcqMemId(), feeConf.getIssMemId(), 
            			null, null, feeConf.getCreditUnit());
                String debitId = Layer2Util.getCreditDebitId(conn, feeConf.getAcqMemId(), feeConf.getIssMemId(), 
                		null, null, feeConf.getDebitUnit());
            	
            	txnCond.append(" and CREDIT_ID= '").append(creditId).append("' ");
            	txnCond.append(" and DEBIT_ID= '").append(debitId).append("' ");
            	txnCond.append("))");
        		
            	logger.debug("getTxnInfoList() sqlCmd: " + "SELECT COUNT(1) FROM TB_TRANS WHERE " + txnCond.toString());
        		
        		int txnCnt = DbUtil.getInteger("SELECT COUNT(1) FROM TB_TRANS WHERE " + txnCond.toString(), conn);
                if (txnCnt > 0){
                	HashMap info = new HashMap<>();
                	info.put("ACQ_MEM_ID", feeConf.getAcqMemId());
                	info.put("ISS_MEM_ID", feeConf.getIssMemId());
                	txnInfoVtr.add(info);
                }
                
                logger.debug("getTxnInfoList() "+txnInfoVtr);
                return txnInfoVtr;
            }
            catch (SQLException e)
            {
                throw new SQLException("getInfoList: fail " + e.toString());
            }
            finally
            {
                BatchUtil.closeConnection(conn);
            }
        }
        
        String crField = Layer2Util.getUnit2TransField( feeConf.getCreditUnit() );
        String dbField = Layer2Util.getUnit2TransField( feeConf.getDebitUnit() );
        if (!StringUtil.isEmpty(crField) ) {
        	txnCond.append(" and CREDIT_ID=TB_TRANS.").append(crField).append("");
        }
        if (!StringUtil.isEmpty(dbField) ) {
        	txnCond.append(" and DEBIT_ID=TB_TRANS.").append(dbField).append("");
        }
        txnCond.append("))");
        
        String sql = String.format( "select %s from TB_TRANS where %s group by %s"
                            		, txnQuery.getTxnGroupByFields()
                            		, txnCond.toString()
                            		, txnQuery.getTxnGroupByFields() );
        
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


    protected double calDeduct(FeeConfig feeConf, double feeAmt)
    { 
        double calFeeAmt = 0;
        double fixedFee = feeConf.getFixedFee();
        String isAllowDeduct = feeConf.getIsAllowDeduct();
        
        
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
    
    
 

    
    protected double getNumOfTxn()
    { //交易總筆數
        String sqlCmd = String.format("select count(*) from TB_TRANS where %s"
                                      , txnQuery.getTxnCondition()
                                        +" and "
                                        +txnQuery.getTxnGroupByCondition() );
        
        Vector vtr = BatchUtil.getInfoList(sqlCmd);
        logger.debug("getNumOfTxn():"+sqlCmd);
        double numOfTxn = ((Number) ((Vector) vtr.get(0)).get(0)).doubleValue();
        
        return numOfTxn;
    }

    
    protected double getAmtOfTxn(String calFrom) throws Exception
    { //交易總金額
        if (calFrom==null||calFrom.equals("")) {
            return 0;
            //logger.warn("getAmtOfTxn():tb_fee_config.cal_from did'nt set up! ");
            //throw new Exception("getAmtOfTxn():tb_fee_config.cal_from did'nt set up! ");
        }
        
        String sqlCmdStr = String.format("select sum(%s) from TB_TRANS where %s"
                                         , calFrom
                                         , txnQuery.getTxnCondition()+" and "+txnQuery.getTxnGroupByCondition());

        logger.debug("getAmtOfTxn():"+sqlCmdStr);
        Vector vtr = BatchUtil.getInfoList(sqlCmdStr);
        double amtOfTxn = ((Number) ((Vector) vtr.get(0)).get(0)).doubleValue();
        return amtOfTxn;
    }
    
    
    /**
     * 手續費計算法則 TB_FEE_CAL
     * @param calRuleId
     * @return calRuleHm
     */
    protected FeeRule getCalRule(String calRuleId)
    {
        FeeRule feeRule = new FeeRule();
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
        
        sqlCmd.append("select CAL_RULE_ID, LOWER_BOUND, UPPER_BOUND, FEE_RATE, PRE_TIER_FEE");
        sqlCmd.append("  from TB_FEE_TIER where CAL_RULE_ID=");
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
    protected FeeResult calFee(FeeConfig feeConf) throws Exception
    {
        FeeResult feeResult = new FeeResult();
        
        //TB_TRANS
        feeResult.setNumOfTxn(getNumOfTxn()); //交易總筆數
        feeResult.setAmtOfTxn(getAmtOfTxn(feeConf.calFrom)); //交易總金額
        
        double calBaseValue = 0.0; //計算手續費基準值
        if (feeConf.getCalBase().equals(Constants.CAL_BASE_NUMBER)) //N
        {
            calBaseValue = feeResult.getNumOfTxn(); //依筆數計算
        }
        else if (feeConf.getCalBase().equals(Constants.CAL_BASE_AMOUNT)) //A
        { 
            calBaseValue = feeResult.getAmtOfTxn(); //依金額計算
        }
        
        FeeRule feeRule = getCalRule(feeConf.getCalRuleId()); //TB_FEE_CAL & TB_FEE_TIER

        FeeTier feeTier = getFeeTier(calBaseValue, feeRule.getFeeTierVtr());

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
        
        feeResult.setFeeAmt(feeAmt);
        feeResult.setFeeRate(feeRate);
        return feeResult;
    }
    
    /**
     * 依基準值取得對應手續費率
     * @param calBaseValue
     * @param feeTierVtr
     */
    protected FeeTier getFeeTier(double calBaseValue, Vector feeTierVtr)
    {
        FeeTier feeTier = new FeeTier();
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
    protected TbFeeResultInfo setFeeResultInfo(FeeResult feeResult, FeeConfig feeConf, HashMap txnInfo)
                 throws Exception
    {
        TbFeeResultInfo feeResultInfo = new TbFeeResultInfo();
        
        String issMemId = txnQuery.getIssMemId();
        String acqMemId = txnQuery.getAcqMemId();
        String merchId = txnQuery.getMerchId();
        String creditUnit = feeConf.getCreditUnit();
        String debitUnit = feeConf.getDebitUnit();
        String acCode = feeConf.getAccountCode();
        
        //會員銀行 入扣帳單位改為feeConf設定的 銀行單位
        if (creditUnit.equals("U")|| debitUnit.equals("U")){
        	acqMemId = feeConf.getAcqMemId();
        }
        
        String creditId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, null, creditUnit);
        String debitId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, null, debitUnit);
        ////logger.debug("setFeeResult() creditUnit:"+creditUnit+" creditId:"+creditId+" debitUnit:"+debitUnit+" debitId:"+debitId);
        
        feeResultInfo.setFeeConfigId(feeConf.getFeeConfigId());
        feeResultInfo.setFeeCode(feeConf.getFeeCode());
        feeResultInfo.setProcDate(getBatchDate());
        feeResultInfo.setExpPayDate(feeConf.getExpPayDate()); //撥款日

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
    protected void remarkSuccess(Connection conn, FeeConfig feeConf, TbFeeResultInfo feeResultInfo) throws Exception
    {
        StringBuffer sql = new StringBuffer();
        
        sql.append("update TB_TRANS set");
        sql.append(" FEE_PROC_DATE= '").append(getBatchDate()).append("', ");
        sql.append(" FEE_SUCC_DATE= '").append(getBatchDate()).append("', ");
        sql.append(" FEE_CODE_LIST=FEE_CODE_LIST||'").append(feeConf.getFeeCode()).append(":', ");
        sql.append(" FEE_RCODE='").append(Constants.RCODE_0000_OK).append("', ");
        sql.append(" PER_TXN_FEE= ");
        double feeAmtTrans = 0.0;
        if (feeResultInfo.getFeeAmt().doubleValue() != 0){
	        if (feeConf.getCalBase().equals(Constants.CAL_BASE_NUMBER)) //N
	        {
	        	//依筆數計算
	        	feeAmtTrans = Calc.div(feeResultInfo.getFeeAmt().doubleValue(), feeResultInfo.getNumOfTxn().doubleValue(), getScale()); 
	        	sql.append(feeAmtTrans);
	        }
	        else if (feeConf.getCalBase().equals(Constants.CAL_BASE_AMOUNT)) //A
	        { 
	        	//依金額計算
	        	feeAmtTrans = Calc.div(feeResultInfo.getFeeAmt().doubleValue(), feeResultInfo.getAmtOfTxn().doubleValue(), getScale());
	        	sql.append("TXN_AMT* ").append(feeAmtTrans);
	        }
        }
        else{
        	sql.append(feeAmtTrans);
        }
        
        sql.append(" where " ).append(txnQuery.getTxnCondition()).append(" and ");
        sql.append(txnQuery.getTxnGroupByCondition());

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
    protected void remarkFail(Connection conn, FeeConfig feeConf) throws Exception
    {
        conn.rollback();
        
        super.setRcode(Constants.RCODE_2001_WARN);
        
        String feeConfigId = feeConf.getFeeConfigId();
        
        StringBuffer sql = new StringBuffer();
        sql.append("update TB_TRANS set");
        sql.append(" FEE_PROC_DATE= '").append(getBatchDate()).append("',");
        sql.append(" FEE_RCODE='").append(Constants.RCODE_2500_FEE_ERR).append("',");
        //TODO TXN_NOTE
        sql.append(" TXN_NOTE=(TXN_NOTE||'").append(Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "feeConfigId="+feeConfigId)).append("'),");
        sql.append(" FEE_CODE_LIST=FEE_CODE_LIST||'").append(feeConf.getFeeCode()).append(":' ");
        sql.append(" where " ).append(txnQuery.getTxnCondition()).append(" and ");
        sql.append(txnQuery.getTxnGroupByCondition());
        
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
     * 檢查取消交易與原交易是否清算,若不清算則也不算手續費:<br/>
     * 在TB_TRANS註記已處理(填上FEE_PROC_DATE)
     * @throws Exception
     */
    protected void handleOrigCancelTxn() throws Exception
    {
        
        
        StringBuffer sql = new StringBuffer();
        
        //logger.debug("cancelFlag:"+cancelFlag);
        if (cancelFlag.equals("0")||StringUtil.isEmpty(cancelFlag))
        { // 取消交易與其原始皆不清算, 註記已處理, 之後就不會撈到這些交易

            // TB_TRANS註記已處理 
            sql.append("Update TB_TRANS set");
            sql.append(" FEE_PROC_DATE='").append(getBatchDate()).append("', ");
            sql.append(" FEE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append(" where (").append(txnQuery.getTxnCondition()).append(")");
            sql.append(" and ").append(txnQuery.getTxnAndCancelCondition());
            
            logger.info("handleOrigCancelTxn():"+sql.toString());
            try
            {
                DBService.getDBService().sqlAction(sql.toString(), conn, false);
            }
            catch (SQLException e)
            {
                conn.rollback();
                logger.warn(" sql:"+sql);
                throw new SQLException("handleOrigCancelTxn():Update TB_TRANS. "+e);
            }
            
            
            try
            {
                conn.commit();
            }
            catch(SQLException e)
            {
                conn.rollback();
                throw new Exception("handleOrigCancelTxn():commit. "+e);
            }
         
        }
        else if (cancelFlag.equals("1"))
        { // 取消交易與其原始皆清算, 之後會撈到這些交易
            logger.info("handleOrigCancelTxn():Cancel trans would be count.");
            //do nothing
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
        sql.append(" WHERE PROC_DATE='").append(getBatchDate()).append("' ");
        
        if ( notInCreditIds != null && notInCreditIds.size() > 0 ){
        	if ( notInCreditIds.size() > 1 ){
        		sql.append("AND CREDIT_ID NOT IN (");
        		for ( int i =0; i < notInCreditIds.size(); i++ ){
        			sql.append("'").append(notInCreditIds.get(i)).append("'");
        			if( i != notInCreditIds.size()-1 )
        				sql.append(", ");
        		}
        		sql.append(")");
        	}
        	else{
        		sql.append("AND CREDIT_ID <>'").append(notInCreditIds.get(0)).append("'");
        	}
        }
        
        
        if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS(");
        	sql.append(" SELECT 1 FROM TB_FEE_CONFIG, TB_MEMBER");
        	sql.append(" WHERE TB_MEMBER.MEM_ID = TB_FEE_CONFIG.ACQ_MEM_ID");
        	sql.append(" AND TB_FEE_RESULT.FEE_CONFIG_ID = TB_FEE_CONFIG.FEE_CONFIG_ID");
        	sql.append(" AND TB_FEE_RESULT.FEE_CODE = TB_FEE_CONFIG.FEE_CODE");
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
        
        //Update TB_TRANS
        sql.append(" UPDATE TB_TRANS SET ");
        sql.append(" FEE_PROC_DATE = NULL");
        sql.append(" ,FEE_SUCC_DATE = NULL");
        sql.append(" ,FEE_CODE_LIST = NULL");
        sql.append(" ,PER_TXN_FEE = NULL");
        sql.append(" ,FEE_RCODE=").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
        sql.append(" , TXN_NOTE=" + getUpdateTxnNoteSql());
        sql.append(" WHERE FEE_PROC_DATE=").append(StringUtil.toSqlValueWithSQuote(getBatchDate()));
        
        if(jobWhereSql.length() > 0){
        	sql.append(" AND EXISTS (");
        	sql.append(" SELECT 1 FROM TB_MEMBER");
        	sql.append(" WHERE TB_MEMBER.MEM_ID = TB_TRANS.ACQ_MEM_ID");
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
            throw new Exception("recoverData():Update TB_TRANS. "+e);
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


    public void setSettleCond(String settleCond)
    {
        this.settleCond = settleCond;
    }


    public String getSettleCond()
    {
        return settleCond;
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
    
    public String getAllMember()
    {
        return allMember;
    }

    public void setAllMember(String allMember)
    {
        this.allMember = allMember;
    }
    public int getCommitCount()
    {
        return commitCount;
    }

    public void setCommitCount(int commitCount)
    {
        this.commitCount = commitCount;
    }
    
    
    public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}


	protected class FeeConfig
    {
        String feeConfigId;
        String acqMemId;
        String issMemId;
        String feeCode;
        double fixedFee;
        String isAllowDeduct;
        String calBase;
        String calFrom;
        String calRuleId;
        String procCycle;
        String validSdate;
        String validEdate;
        String creditUnit;
        String debitUnit;
        String accountCode;
        String fundType;
        String bankId;
        String saleCode;
        String feeTarget;
        String expPayDate;
        
        public String getAcqMemId()
        {
            return acqMemId;
        }
        public void setAcqMemId(String acqMemId)
        {
            this.acqMemId = acqMemId;
        }
        public String getCalBase()
        {
            return calBase;
        }
        public void setCalBase(String calBase)
        {
            this.calBase = calBase;
        }
        public String getCalRuleId()
        {
            return calRuleId;
        }
        public void setCalRuleId(String calRuleId)
        {
            this.calRuleId = calRuleId;
        }
        public String getFeeCode()
        {
            return feeCode;
        }
        public void setFeeCode(String feeCode)
        {
            this.feeCode = feeCode;
        }
        public double getFixedFee()
        {
            return fixedFee;
        }
        public void setFixedFee(double fixedFee)
        {
            this.fixedFee = fixedFee;
        }
        public String getIsAllowDeduct()
        {
            return isAllowDeduct;
        }
        public void setIsAllowDeduct(String isAllowDeduct)
        {
            this.isAllowDeduct = isAllowDeduct;
        }
        public String getIssMemId()
        {
            return issMemId;
        }
        public void setIssMemId(String issMemId)
        {
            this.issMemId = issMemId;
        }
        public String getProcCycle()
        {
            return procCycle;
        }
        public void setProcCycle(String procCycle)
        {
            this.procCycle = procCycle;
        }
        public String getFeeConfigId()
        {
            return feeConfigId;
        }
        public void setFeeConfigId(String feeConfigId)
        {
            this.feeConfigId = feeConfigId;
        }
        public String getCalFrom()
        {
            return calFrom;
        }
        public void setCalFrom(String calFrom)
        {
            this.calFrom = calFrom;
        }
        public String getValidEdate()
        {
            return validEdate;
        }
        public void setValidEdate(String validEdate)
        {
            this.validEdate = validEdate;
        }
        public String getValidSdate()
        {
            return validSdate;
        }
        public void setValidSdate(String validSdate)
        {
            this.validSdate = validSdate;
        }
        public String getCreditUnit()
        {
            return creditUnit;
        }
        public void setCreditUnit(String creditUnit)
        {
            this.creditUnit = creditUnit;
        }
        public String getDebitUnit()
        {
            return debitUnit;
        }
        public void setDebitUnit(String debitUnit)
        {
            this.debitUnit = debitUnit;
        }
		public String getAccountCode() {
			return accountCode;
		}
		public void setAccountCode(String accountCode) {
			this.accountCode = accountCode;
		}
		public String getFundType() {
			return fundType;
		}
		public void setFundType(String fundType) {
			this.fundType = fundType;
		}
		public String getExpPayDate() {
			return expPayDate;
		}
		public void setExpPayDate(String expPayDate) {
			this.expPayDate = expPayDate;
		}
		public String getBankId() {
			return bankId;
		}
		public void setBankId(String bankId) {
			this.bankId = bankId;
		}
		public String getSaleCode() {
			return saleCode;
		}
		public void setSaleCode(String saleCode) {
			this.saleCode = saleCode;
		}
		public String getFeeTarget() {
			return feeTarget;
		}
		public void setFeeTarget(String feeTarget) {
			this.feeTarget = feeTarget;
		}
		public String toString() {
			return "feeConfigId "+feeConfigId        +
					", acqMemId "+acqMemId            +
					", issMemId "+issMemId            +
					", feeCode "+feeCode              +
					", fixedFee "+fixedFee            +
					", isAllowDeduct "+isAllowDeduct  +
					", calBase "+calBase              +
					", calFrom "+calFrom              +
					", calRuleId "+calRuleId          +
					", procCycle "+procCycle          +
					", validSdate "+validSdate        +
					", validEdate "+validEdate        +
					", creditUnit "+creditUnit        +
					", debitUnit "+debitUnit          +
					", accountCode "+accountCode      +
					", fundType "+fundType            +
					", bankId "+bankId                +
					", saleCode "+saleCode                +
					", feeTarget "+feeTarget          +
					", expPayDate "+expPayDate        ;
		}
    }
    
    
    protected class FeeRule
    {
        private String calFormula = "";
        private String carryType = "";
        private int carryDigit = 0;
        private Vector FeeTierVtr = null;
        
        public String getCalFormula()
        {
            return calFormula;
        }
        public void setCalFormula(String calFormula)
        {
            this.calFormula = calFormula;
        }
        public int getCarryDigit()
        {
            return carryDigit;
        }
        public void setCarryDigit(int carryDigit)
        {
            this.carryDigit = carryDigit;
        }
        public String getCarryType()
        {
            return carryType;
        }
        public void setCarryType(String carryType)
        {
            this.carryType = carryType;
        }
        public Vector getFeeTierVtr()
        {
            return FeeTierVtr;
        }
        public void setFeeTierVtr(Vector feeTierVtr)
        {
            FeeTierVtr = feeTierVtr;
        }
        
    }
    
    
    protected class FeeTier
    {
        private String calRuleId = "";
        private double lowerBound = 0.0;
        private double feeRate = 0.0;
        private double preTierFee = 0.0;
        public String getCalRuleId()
        {
            return calRuleId;
        }
        public void setCalRuleId(String calRuleId)
        {
            this.calRuleId = calRuleId;
        }
        public double getFeeRate()
        {
            return feeRate;
        }
        public void setFeeRate(double feeRate)
        {
            this.feeRate = feeRate;
        }
        public double getLowerBound()
        {
            return lowerBound;
        }
        public void setLowerBound(double lowerBound)
        {
            this.lowerBound = lowerBound;
        }
        public double getPreTierFee()
        {
            return preTierFee;
        }
        public void setPreTierFee(double preTierFee)
        {
            this.preTierFee = preTierFee;
        }
    }


    protected class FeeResult
    {
        private double feeAmt = 0.0;
        private double feeRate = 0.0;
        private double numOfTxn = 0.0;
        private double amtOfTxn = 0.0;
        public double getFeeAmt()
        {
            return feeAmt;
        }
        public void setFeeAmt(double feeAmt)
        {
            this.feeAmt = feeAmt;
        }
        public double getFeeRate()
        {
            return feeRate;
        }
        public void setFeeRate(double feeRate)
        {
            this.feeRate = feeRate;
        }
        public double getAmtOfTxn()
        {
            return amtOfTxn;
        }
        public void setAmtOfTxn(double amtOfTxn)
        {
            this.amtOfTxn = amtOfTxn;
        }
        public double getNumOfTxn()
        {
            return numOfTxn;
        }
        public void setNumOfTxn(double numOfTxn)
        {
            this.numOfTxn = numOfTxn;
        }
    }
    
    
    protected class TxnQuery
    {

    	protected ProcFee procFee;
        protected DateRange procPeriod;
        protected String merchId = "";
        protected String issMemId = "";
        protected String acqMemId = "";
    	
        protected String txnCondition;
        protected List<String> txnGroupByFieldsList;
        protected String txnGroupByFields;
        
        protected String txnGroupByCondition;

        protected final String cancelTxnCondition = "(STATUS = 'C' or P_CODE IN (SELECT P_CODE FROM TB_P_CODE_DEF WHERE IS_SETL_FLAG = '0' AND P_CODE LIKE '%8'))";
        
        private TxnQuery(){}
        
        protected TxnQuery(ProcFee procFee)
    	{
    		this.procFee = procFee;
    	}
		
		
		/**
		 * @param crUnit
		 * @param dbUnit
		 */
		protected void setTxnGroupByFieldsList(String crUnit, String dbUnit)
        {
        	List<String> list = new Vector<String>();
        	
            if (crUnit.equals("M") || dbUnit.equals("M")) { //TODO 20070907
                list.add(Layer2Util.getUnit2TransField("M"));
            }
            
            String field = "";
            String unit = "I";
            if (crUnit.equals(unit) || dbUnit.equals(unit)) 
            {//含有I的才處理
                field = Layer2Util.getUnit2TransField(unit);
                if (! StringUtil.isEmpty(field)) {
                	list.add(field);
                }
            }
            
            unit = "A";
            if (crUnit.equals(unit) || dbUnit.equals(unit)) 
            {//含有A的才處理
                field = Layer2Util.getUnit2TransField(unit);
                if (! StringUtil.isEmpty(field)) {
                	list.add(field);
                }
            }
            
            //U 會員銀行  故撈取不須acq_mem_id，針對 iss_mem_id group
            
            txnGroupByFieldsList = list;
        }
        
		protected List<String> getTxnGroupByFieldsList() 
        {
			return txnGroupByFieldsList;
		}
		
    	/**
         * 傳入credit unit & debit unit, 回傳 group by 欄位 <br/>
         * M-C or C-M => merch_Id<br/>
         * M-I or I-M => merch_Id, iss_mem_id<br/>
         * C-I or I-C => merch_Id, iss_mem_id<br/>
         * M-A or A-M => merch_Id, acq_mem_id<br/>
         * C-A or A-C => acq_mem_id<br/>
         * I-A or A-I => iss_mem_id, acq_mem_id<br/>
         * U-I or I-U => iss_mem_id<br/>
         * 
         * @param crUnit
         * @param dbUnit
         * @return groupByFields
         */
        protected void setGroupByFields(String crUnit, String dbUnit)
        {
        	List<String> list = getTxnGroupByFieldsList();
        	
            StringBuffer sb = new StringBuffer();
            
            for (String field : list) {
                if (!StringUtil.isEmpty(field)) {
                	sb.append( ( sb.length()==0)?"":", ").append(field);
                }
            }
            txnGroupByFields = sb.toString();
        } 
        
        protected String getTxnGroupByFields() 
    	{
			return txnGroupByFields;
		}

		/**
         * 在group by撈出交易的回圈裡, 設定用來 sum(金額)/count(筆數) 的條件
		 * @param txnInfo
		 */
		protected void setTxnGroupByCondition(HashMap txnInfo) 
		{
			List<String> list = getTxnGroupByFieldsList();

            StringBuffer sb = new StringBuffer();
            for (String field : list) {
                if (!StringUtil.isEmpty(field)) {
                	sb.append( ( sb.length()==0)?"":" and ").append(field)
                	  .append("='").append((String)txnInfo.get(field)).append("'");
                    
                    if ("MERCH_ID".equals(field)) setMerchId((String)txnInfo.get(field));
                    if ("ISS_MEM_ID".equals(field)) setIssMemId((String)txnInfo.get(field));
                    if ("ACQ_MEM_ID".equals(field)) setAcqMemId((String)txnInfo.get(field));
                }
            }
            txnGroupByCondition = sb.toString();
		}

		protected String getTxnGroupByCondition() 
		{
			return txnGroupByCondition;
		}

        /**
         * 設定交易的選取條件
         * @param feeConf
         * @param pCodeList
         */
        protected void setTxnCondition(FeeConfig feeConf, String pCodeList, String feeCode)
        {
            String startDate = getProcPeriod().getStartDate();
            String endDate = getProcPeriod().getEndDate();

            StringBuffer sb = new StringBuffer();
            //所有已清算且未計算手續費的交易
            sb.append(" ( SETTLE_PROC_DATE between '").append(startDate).append("' and '").append(endDate).append("' )");
            sb.append(" and P_CODE in (").append(pCodeList).append(") "); 
            
            if (!feeConf.getIssMemId().equals(procFee.getAllMember())) {
                sb.append(" and ISS_MEM_ID='").append(feeConf.getIssMemId()).append("' ");
            }
            if (!feeConf.getAcqMemId().equals(procFee.getAllMember()) && feeConf.getFeeTarget().equals("0")) {
                sb.append(" and ACQ_MEM_ID='").append(feeConf.getAcqMemId()).append("' ");
            }
            if (feeConf.getFeeTarget().equals("1")){
            	sb.append(" AND (CARD_NO, EXPIRY_DATE) IN (");
            	sb.append(" SELECT CARD_NO, EXPIRY_DATE FROM TB_CARD");
            	sb.append(" WHERE BANK_ID = '").append(feeConf.getBankId()).append("'");
            	sb.append(" AND SALE_CODE = '").append(feeConf.getSaleCode()).append("'");
            	sb.append(" )");
            }
            sb.append(procFee.getSettleCond()); //spring.xml	
            sb.append(" and (FEE_CODE_LIST is null or FEE_CODE_LIST not like '%" + feeCode + "%')");
            
            logger.debug("set Txn Condition():"+sb.toString());
            txnCondition = sb.toString();
            
            //設定要groupby的欄位
            setTxnGroupByFieldsList(feeConf.getCreditUnit(), feeConf.getDebitUnit());
            
            //從txnGroupByFieldsList 組成 "field1, field2, ....." 字串
            setGroupByFields(feeConf.getCreditUnit(), feeConf.getDebitUnit());
        }

        /**
         * 取得交易的選取條件
         * @return txnCondition
         */
        protected String getTxnCondition()
        {
            String flag = procFee.getCancelFlag();
            if (flag.equals("0")||StringUtil.isEmpty(flag)) {
                return txnCondition + " and not " + getCancelTxnCondition();
            }
            return txnCondition;
        }
        
        //for handleOrigCancelTxn()
        protected String getTxnAndCancelCondition() 
        {
                return txnCondition + " and " + getCancelTxnCondition();
        }

        protected DateRange getProcPeriod() {
			return procPeriod;
		}

        protected void setProcPeriod(DateRange procPeriod)
        {
            if (StringUtil.isEmpty(procPeriod.getEndDate()))
            { //如果是ProcCycle是 Dxx, endDate會是null ==> 使endDate=startDate
                procPeriod.setEndDate(procPeriod.getStartDate());
            }
			this.procPeriod = procPeriod;
		}

        protected ProcFee getProcFee()
        {
			return procFee;
		}

        protected void setProcFee(ProcFee procFee) 
        {
			this.procFee = procFee;
		}

		protected String getAcqMemId()
		{
			return acqMemId;
		}

		protected void setAcqMemId(String acqMemId) 
		{
			this.acqMemId = acqMemId;
		}

		protected String getIssMemId() 
		{
			return issMemId;
		}

		protected void setIssMemId(String issMemId) 
		{
			this.issMemId = issMemId;
		}

		protected String getMerchId() 
		{
			return merchId;
		}

		protected void setMerchId(String merchId) 
		{
			this.merchId = merchId;
		}

        protected String getCancelTxnCondition()
        {
            return cancelTxnCondition;
        }

    } //TxnQuery


    protected String getUpdateTxnNoteSql()
    {
        return updateTxnNoteSql;
    }


    protected void setUpdateTxnNoteSql()
    {
        this.updateTxnNoteSql =  Layer2Util.getUpdateTxnNoteSql(TXN_NOTE_HEAD);
    }


    public String getCancelFlag()
    {
        return cancelFlag;
    }


    public void setCancelFlag(String cancelFlag)
    {
        this.cancelFlag = cancelFlag;
    }


	public List getNotInCreditIds() {
		return notInCreditIds;
	}


	public void setNotInCreditIds(List notInCreditIds) {
		this.notInCreditIds = notInCreditIds;
	}



}
