/* 
 * Version: 2.0.0
 * Date: 2007-01-10
 */

/* 
 * 
 * (版權及授權描述)
 *
 * Copyright 2007 (C) Hyweb. All Rights Reserved.
 *
 * $History: ProcSettle.java $
 * **************************************************
 * 20070823 
 * 新增isCheckSponsorSum在spring設定是不是要檢查出資比率=100% (for boccc)
 * **************************************************
 * User: rock      Date: 2007/07/20     Time:15:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle 
 * update remarkSuccess() 
 * insert tb_settle_result若出現錯誤，原本程式會中斷，現在改為:
 * 記rocde 2420 ，程式繼續執行
 * 
 * **************************************************
 * User: rock      Date: 2007/07/19     Time:15:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle 
 * update setSettleResultInfo() 
 * Tb_settle_result add 2 column: bonus_sdate, bonus_edate
 * 
 * **************************************************
 * User: rock      Date: 2007/06/23     Time:15:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle 
 * update sql, add index in tb_trans, tb_trans_dtl
 * 
 * **************************************************
 * User: rock      Date: 2007/06/02     Time:15:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle (in CTCBD)
 * add TB_SETTLE_RESULT.CAMPAIGN_QTY 
 * update subclass SettleInfo (add campaignQty)
 * add remarkProcessedForNoNeedSettleBonus()
 * add remarkProcessedForNoSetSettleBonus()
 * add remarkProcessedForTxnDtlNoMatch() ->2405
 * add remarkProcessedForTxnNoMatch()
 * add remarkProcessedForNoNeedSettleTxn()
 * add remarkNoProcessedTxnDtl()
 * add tb_trans.txn_note, tb_trans_dtl.txn_note 
 * 
 * **************************************************
 * User: rock      Date: 2007/04/24     Time:15:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.handleSingleCampaign() -> handleSingleSponsor()
 * 
 * **************************************************
 * User: rock      Date: 2007/01/31     Time:15:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.新增調帳處理方式, 修改handleNoCampaign()
 *  
 * **************************************************
 * User: rock      Date: 2007/01/25     Time:15:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.修改remarkSuccess(), 補上TB_SETTLE_RESULT新增四個欄位(wendy)
 * 
 * **************************************************
 * User: rock      Date: 2007/01/24     Time:17:30
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.修改handleSingleCampaign()裡呼叫calSettleAmt()時
 *   傳入的第三個變數由txnRedeemAmt改為sponsorQty
 * 2.新增檢查call campaign 2411的錯誤
 * 
 * **************************************************
 * User: rock      Date: 2007/01/24     Time:11:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.修改calSettleAmt()、calBonusQtyForSponsor(), 乘法計算改為BigDecimal.multiply()方式
 * 
 * **************************************************
 * User: rock      Date: 2007/01/23     Time:14:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.remarkProcessedForInvalidTxn(), 更新trans_dtl，拿掉getUnSettleCond()條件
 * 
 * **************************************************
 * User: rock      Date: 2007/01/18     Time:14:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.modify callCampaign(), call campaign 增加傳入參數 txnCode
 * 2.modify remarkProcessedForInvalidTxn() sql 
 * 
 * **************************************************
 * User: rock      Date: 2007/01/17     Time:10:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.add remarkProcessedForInvalidTxn(), 針對合法性驗證失敗的交易, 若設定不須清算則註記已處理
 * 2.modify handleOrigCancelTxn(), 改為先update tb_trans再update tb_trans_dtl
 * 
 * **************************************************
 * User: rock      Date: 2007/01/16     Time:14:00
 * Updated in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 1.modify process(), settleDate = D01 of batchDate -> D00 of batchDate
 * 2.modify setUnSettleCond(), 能依參數決定是否清算所有已過檔的交易(wendy)
 * 
 * **************************************************
 * User: rock      Date: 2007/01/10     Time:14:00
 * Add in $/JCP_LAYER2/src/main/tw/com/hyweb/batch/settle
 * 
 */
package tw.com.hyweb.svc.yhdp.batch.settle;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Constants;
//import tw.com.hyweb.core.cp.common.misc.Layer2Util;
//import tw.com.hyweb.core.cp.common.misc.DateRange;

import tw.com.hyweb.util.Calc;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbSettleResultInfo; 
import tw.com.hyweb.service.db.mgr.TbSettleResultMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;

import tw.com.hyweb.core.campaign.CampaignPayRate;
import tw.com.hyweb.core.campaign.OriginalTransactionDtlInfo;
import tw.com.hyweb.core.campaign.PayRate;
import tw.com.hyweb.core.campaign.PayRateDetail;
import tw.com.hyweb.core.campaign.PayRateMember;
import tw.com.hyweb.core.yhdp.common.misc.DateRange;
import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

/**
 * 準備交易清算明細<br/>
 * (1) 未考慮多幣別的問題<br/>
 * (2) 已考慮原交易與取消交易是否計算手續費<br/>
 * (3) 不考慮fund_cycle, 不填fund_date<br/>
 * (4) 支援acq_mem_id可以設定tb_sys_config.all_member代表全部的收單單位<br/>
 * (5) 支援iss_mem_id可以設定tb_sys_config.all_member代表全部的發卡單位<br/>
 * (6) 支援reward或redeem才清算<br/>
 * (7) 支援reward和redeem皆清算<br/>
 * 
 * usage:<br/>
 *  ant runProcSettle –Ddate="" -Drecover=""<br/>
 *  batchDate={–Ddate請輸入YYYYMMDD；若不輸入，預設為系統日}<br/>
 *  recoverLevel={–Drecover請輸入ALL:復原全部資料，ERR:復原錯誤部分}<br/>
 * <br/>
 * 
 * spring:<br/>
 *  commitCount=10000<br/>  
 *  sleepTime=100<br/>
 * <br/>
 * 
 * work flow:<br/>
 *  settleConfigInfoList = preCondition<br/>
 *  action(fetch each settleConfigInfoList)<br/>
 * <br/>
 * @author Rock<br/>
 * 
 */
public class ProcSettle extends AbstractBatchBasic 
{
    protected final static Logger logger = Logger.getLogger(ProcSettle.class);

    private static final String SPRING_PATH = "config" + File.separator +
                                              "batch" + File.separator +
                                              "ProcSettle" + File.separator +
                                              "spring.xml";
    
    protected final String TXN_NOTE_HEAD = "SETTLE";
    
    //用這變數是為了在boccc的清算可以有不同的做法
    protected String updateTxnNoteSql = "''";//Layer2Util.getUpdateTxnNoteSql(TXN_NOTE_HEAD);//see recoverData()
        
    protected final String TBNAME_TRANS = "TB_TRANS"; //中信海外部會換table name
    
    protected final String TBNAME_TRANSDTL = "TB_TRANS_DTL"; //中信海外部會換table name
    
    protected Connection conn = BatchUtil.getConnection();

    protected String batchDate; //process date
    
    protected String mon = "";
    
    protected String day = "";
    
    protected DateRange procPeriod = null;
    
    protected String recoverLevel; //ALL 復原全部 or ERR 復原錯誤部分
    
    protected int commitCount = 0; //由spring設定
    
    protected int sleepTime = 0;  //由spring設定, commit之後sleep時間
    
//    protected Vector settleConfigList = new Vector();
    
    protected String unSettleCond;
    
    protected String isOnlyAll;
    
    protected String settlePartition;
    
    protected String allMember;
    
    protected boolean isCheckSponsorSum = true; //spring

    //如果一筆trans_dtl有設多個campaign, 只要有一個campaign fail, 
    //整筆trans_dtl就不insert tb_settle_result
    protected boolean isHandleSponsorFail = false; 
    
    /**
     * Main function<br/>
     * @param args String[]
     */
    public static void main(String[] args)
    {
        ProcSettle instance = getInstance();
        instance.setBatchDate(System.getProperty("date"));
        instance.setRecoverLevel(System.getProperty("recover").toUpperCase());
        instance.run(null);
    }


    /**
     * get a ProcSettle instance by spring <br/>
     * @return instance
     */
    public static ProcSettle getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ProcSettle instance = (ProcSettle) apContext.getBean("ProcSettle");
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
            init();
            //如果是ProcCycle是 Dxx, endDate會是null -> 令 endDate = startDate
            setProcPeriod(Layer2Util.getProcPeriod(getBatchDate(), "D00")); //目前不能改，否則不能加par_mon, par_day的條件
            if ( getRecoverLevel()!=null && 
                 (getRecoverLevel().equals(Constants.RECOVER_LEVEL_ALL)||
                  getRecoverLevel().equals(Constants.RECOVER_LEVEL_ERR)) )
            {
                recoverData();
                return;
            }
            /*定義未清算處理的條件*/
            setUnSettleCond(getProcPeriod());
            
            /*trans 有交易 trans_dtl 沒有 => trans 註記已處理, txn_note:TxnNoMatch */
            remarkProcessedForTxnNoMatch();
            
            /*trans 沒有交易 trans_dtl 有 => trans_dtl註記已處理, rcode:2405, txn_note:TxnDtlNoMatch*/
            remarkProcessedForTxnDtlNoMatch();
           
        //  20081205 因為累計活動的關係，取消交易可能造成與原交易不同之award的異動，所以取消交易一定要清算
            /*原交易與取消交易清算與否, 若不清算  註記已處理, txn_note:OrigCancelTxn*/
            handleOrigCancelTxn();
            
            /*針對合法性驗證失敗就不清算的交易 trans_dtl註記已處理, rcode:2401*/
            remarkProcessedForInvalidTxn();
            
            /*針對不須清算BONUS_ID, trans_dtl註記已處理, txn_note:NoNeedSettleBonus*/
            remarkProcessedForNoNeedSettleBonus();
            
            /*針對未設定於TB_SETTLE_CONFIG的p_code+txn_code, trans_dtl註記已處理, txn_note:NoNeedSettleTxn*/ 
            remarkProcessedForNoNeedSettleTxn(); 
            
            /*針對未設定於TB_SETTLE_CONFIG的p_code+txn_code+bonus_id, trans_dtl註記已處理, txn_note:NoSetSettleBonus*/
            remarkProcessedForNoSetSettleBonus();
            
            //20080630 JESSE:這個不要註記rcode，以免影響報表
            /*超過settle_rate有效期的交易 註記已處理, rcode:2403*/
            //remarkProcessedForInvalidDateRate();
            
            
            Vector settleConfigList = preCondition();
            //檢查JOB_ID、JOB_TIME、MEM_ID
            Vector filterSettleConfigList = filterSettleConfig(settleConfigList);
            
            for (int i=0; i<filterSettleConfigList.size(); i++)
            {
                HashMap settleConfigHm = (HashMap)filterSettleConfigList.get(i);
                action(settleConfigHm);
            }

            syncTransAndTransDtl();
            
            /* 針對沒處理到的交易註記已處理, txn_note:NoProcessed*/
            /* 因為拆收單的原因，無法分辨是否為後續才需處理的收單，所以改為註記 CUT_DATE < BATCHDATE 的交易*/ 
            remarkNoProcessedTxnDtl();
        }
        catch (Exception e)
        {
            logger.debug(e);
            throw new Exception(e); //throw to AbstractBatchBasic.run()
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
            logger.info("end process!\n");
        }
    }

    private Vector filterSettleConfig(Vector settleConfigList) throws SQLException {
		// TODO Auto-generated method stub
    	logger.debug("-------- filterSettleConfig --------");
    	logger.debug("getBatchResultInfo().getJobId():  " +getBatchResultInfo().getJobId());
    	logger.debug("getBatchResultInfo().getJobTime():" +getBatchResultInfo().getJobTime());
    	logger.debug("getBatchResultInfo().getMemId():  " +getBatchResultInfo().getMemId());
    	Vector settleConfigVtr = null;
    	
    	if (Layer1Constants.JOB_ID_DEFAULT.equalsIgnoreCase(getBatchResultInfo().getJobId())
		&& Layer1Constants.JOB_TIME_DEFAULT.equalsIgnoreCase(getBatchResultInfo().getJobTime())
		&& StringUtil.isEmpty(getBatchResultInfo().getMemId())){
    		return settleConfigList;
    	}
    	else{
    		settleConfigVtr = new Vector();
    		
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
    		
            Statement settleStmt = null;
            ResultSet settleRs = null;
            Connection conn = null;
            
            try {
            	List memIds = new ArrayList<>();
            	conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
            	settleStmt = conn.createStatement();
            	logger.debug("seqnoSql: "+sql.toString());
            	settleRs = settleStmt.executeQuery(sql.toString());
            	while (settleRs.next()) {
            		memIds.add(settleRs.getString(1));
            	}

            	for (int i=0; i<settleConfigList.size(); i++)
                {
                    HashMap settleConfigHm = (HashMap)settleConfigList.get(i);
                    if (memIds.contains(settleConfigHm.get("ACQ_MEM_ID"))){
                    	settleConfigVtr.add(settleConfigHm);
                    }
                    if (Layer1Constants.MEM_LAST.equalsIgnoreCase(getBatchResultInfo().getMemId())){
                    	if (settleConfigHm.get("ACQ_MEM_ID").toString().equalsIgnoreCase(allMember)){
                    		settleConfigVtr.add(settleConfigHm);
                    	}
                    }
                }
            }
            finally {
                ReleaseResource.releaseDB(conn, settleStmt, settleRs);
            }
    		
    	}
    	logger.debug("settleConfigVtr: " + settleConfigVtr);
		return settleConfigVtr;
	}


	/**
     * 初始設定<br/>
     * 
     * 若不指定batchDate, 預設為系統日<br/>
     * get connection<br/>
     * @throws Exception
     */
    protected void init() throws Exception
    { 
        try
        {
            BatchUtil.getNow();
            if (StringUtil.isEmpty(getBatchDate())) {
                setBatchDate(BatchUtil.sysDay); //sysDay is Long
            } else if (!BatchUtil.checkChristianDate(getBatchDate())) {
                String errMsg = "Invalid date for option -Ddate!";
                System.out.println(errMsg); //print in console
                throw new Exception(errMsg);
            }
                  
            setMon(getBatchDate().substring(4,6));
            setDay(getBatchDate().substring(6,8));

            //代表所有單位的代號
            setAllMember(Layer2Util.getBatchConfig("ALL_MEMBER"));
            
            //看參數決定是否清算之前的交易
            setIsOnlyAll(Layer2Util.getBatchConfig("ONLY_ALL_FLAG"));
            
            
            logger.info("batchDate:"+getBatchDate());
            logger.info("recoverLevel:"+getRecoverLevel());
            logger.info("commitCount:"+getCommitCount());
            logger.info("ALL_MEMBER:"+getAllMember());
            logger.info("ONLY_ALL_FLAG:"+getIsOnlyAll());
            
            if (getIsOnlyAll().equals("1")) { //如果要清算之前的交易
                String settlePartition = Layer2Util.getBatchConfig("SETTLE_PARTITION");
                if (settlePartition.equals("")||settlePartition==null) settlePartition = "02";
                setSettlePartition(settlePartition);
                logger.info("SETTLE_PARTITION:"+getSettlePartition());
            }
        }
        catch (Exception e)
        {
            throw new Exception("init():"+e);
        }
        logger.debug("init(): ok.\n");
    }
    
    protected String makeParMonDayCond(String symbo, boolean isAnd)
    {
        StringBuffer cond = new StringBuffer();
        String mon = getMon();
        String day = getDay();
        cond.append((isAnd==true)?" and ":" "); //呼叫時決定要不要加" and "
        
        if (getIsOnlyAll().equals("0")||StringUtil.isEmpty(getIsOnlyAll())||getIsOnlyAll()==null)
        { // isOnlyAll = 0 只清算今天過檔的交易
            cond.append(symbo).append("PAR_MON='").append(mon).append("' and ");
            cond.append(symbo).append("PAR_DAY='").append(day).append("'");
        }
        else if (getIsOnlyAll().equals("1"))
        { // isOnlyAll = 1 清算所有已過檔的交易
            //只清算前面n個partition, n定義在tb_sys_config.settle_partition
            int sp = Integer.parseInt(getSettlePartition());
            if (sp<=0) sp=1;
            if (sp>12) sp=12;
            
            int m = Integer.parseInt(mon);
            int startParMon = m;
            
            //計算從那個月開始
            if (sp <= m) {
                startParMon = m - sp + 1;
            } else {
                startParMon = 12 + (m - sp + 1);
            }
            
            //組成字串: ( par_mon=XX and par_mon=XX and .... )
            cond.append(" ( ");
            for ( int i=0, j=startParMon; i<sp; i++) { 
                cond.append((i==0)?"":" or ");
                if (m==j)cond.append("(");
                cond.append(symbo).append("PAR_MON='").append(StringUtil.pendingKey(j, 2)).append("'");
                if (m==j) cond.append(" and ").append(symbo).append("PAR_DAY<='").append(day).append("'");
                if (m==j)cond.append(")"); 
                j = (j==12) ? 1 : j+1; 
            }
            cond.append(" ) ");            
        }
        
        return cond.toString();
    }
    
    protected String makeCutDateCond(String symbo, boolean isAnd)
    {
        
        String startDate = getProcPeriod().getStartDate();
        String endDate = getProcPeriod().getEndDate();
        
        StringBuffer cond = new StringBuffer();

        cond.append((isAnd==true)?" and ":" "); //呼叫時決定要不要加" and "
        
        String cutDateCond = null;
        if (getIsOnlyAll().equals("0")||StringUtil.isEmpty(getIsOnlyAll())||getIsOnlyAll()==null)
        { // isOnlyAll = 0 只清算今天過檔的交易
            cutDateCond = String.format(" %sCUT_DATE >= '%s' and  %sCUT_DATE <= '%s' ", symbo, startDate, symbo, endDate); 
        }
        else if (getIsOnlyAll().equals("1"))
        { // isOnlyAll = 1 清算所有已過檔的交易
            cutDateCond = String.format(" %sCUT_DATE <= '%s' ", symbo, endDate);
        }
        cond.append(cutDateCond);
        
        return cond.toString();
    }
    /**
     * 定義未清算處理的SQL條件, 指定給unSettleCond <br/>
     * @param procPeriod 傳入startDate, endDate 做為過檔日期的範圍<br/>
     * 能依參數決定是否清算所有已過檔的交易:<br/>
     * 以ONLY_ALL_FLAG =0: 處理過檔日=處理日的交易 
     *                =1: 處理過檔日<=處理日的交易
     */
    protected void setUnSettleCond(DateRange procPeriod)
    {
//        String startDate = procPeriod.getStartDate();
//        String endDate = procPeriod.getEndDate();
        
        StringBuffer cond = new StringBuffer();
        
        //cond.append(makeParMonDayCond("", false));
        
//        String cutDateCond = null;
//        if (getIsOnlyAll().equals("0")||StringUtil.isEmpty(getIsOnlyAll())||getIsOnlyAll()==null)
//        { // isOnlyAll = 0 只清算今天過檔的交易
//            //cutDateCond = String.format(" and (cut_date between '%s' and '%s')", startDate, endDate);
//            cutDateCond = String.format(" cut_date >= '%s' and  cut_date <= '%s' ", startDate, endDate); 
//        }
//        else if (getIsOnlyAll().equals("1"))
//        { // isOnlyAll = 1 清算所有已過檔的交易
//            //cutDateCond = String.format(" and (cut_date <= '%s')", endDate);
//            cutDateCond = String.format(" cut_date <= '%s' ", endDate);
//        }
        //cond.append(cutDateCond);
        cond.append(makeCutDateCond("", false));
    
        unSettleCond = cond.toString();
    }


    /**
     * 取得未清算處理的SQL條件<br/>
     * @return unSettleCond
     */
    protected String getUnSettleCond()
    {
        return unSettleCond;
    }


    /**
     * trans 有交易 trans_dtl 沒有 => trans 註記已處理, txn_note:TxnNoMatch
     * @throws Exception
     */
    protected void remarkProcessedForTxnNoMatch() throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(TBNAME_TRANS).append(" set");
        sql.append("  SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
        sql.append(" ,SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
        sql.append(" ,TXN_NOTE=(TXN_NOTE||'").append(Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "TxnNoMatch")).append("')");
        sql.append(" where ").append(makeParMonDayCond("", false));
        sql.append("     and ").append(makeCutDateCond("", false));
        sql.append("     and settle_proc_date is null ");
        sql.append("     AND (card_no, expiry_date, lms_invoice_no) not in ( ");
        sql.append("      select t.card_no, t.expiry_date, t.lms_invoice_no ");
        sql.append("       from ").append(TBNAME_TRANS).append(" t, ").append(TBNAME_TRANSDTL).append(" d ");
        sql.append("       where ").append(makeParMonDayCond("t.", false));
        sql.append("         and ").append(makeCutDateCond("t.", false));
        sql.append("         and t.settle_proc_date is null ");
        sql.append("         and t.settle_succ_date is null ");
        sql.append("         and t.settle_rcode='").append(Constants.RCODE_0000_OK).append("'");
        sql.append("         and t.card_no=d.card_no and t.expiry_date=d.expiry_date and t.lms_invoice_no=d.lms_invoice_no ) ");
        
        try
        {
            logger.info("remarkProcessedForTxnNoMatch() "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            conn.commit();
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkProcessedForTxnNoMatch():"+e);
        }
    }


    /** 
     * trans 沒有交易 trans_dtl 有 => trans_dtl註記已處理, rcode:2405, txn_note:TxnDtlNoMatch
     * @throws Exception
     */
    protected void remarkProcessedForTxnDtlNoMatch() throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(TBNAME_TRANSDTL).append(" set ");
        sql.append("   SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
        sql.append("  ,SETTLE_RCODE='").append(Constants.RCODE_2405_NO_MATCH_TXN_DTL).append("'");
        sql.append("  ,TXN_NOTE=(TXN_NOTE||'").append(Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "TxnDtlNoMatch")).append("')");
        sql.append(" where ").append(makeParMonDayCond("", false));
        sql.append("   and SETTLE_PROC_DATE is null ");
        sql.append("   and (card_no, expiry_date, lms_invoice_no) in ( ");
        sql.append("      select card_no, expiry_date, lms_invoice_no from ").append(TBNAME_TRANSDTL).append(" ");
        sql.append("       where ").append(makeParMonDayCond("", false));
        sql.append("        and settle_proc_date is null");
        sql.append("      AND NOT EXISTS ( "); 
        sql.append("        select d.card_no, d.expiry_date, d.lms_invoice_no ");
        sql.append("          from ").append(TBNAME_TRANS).append(" t, ").append(TBNAME_TRANSDTL).append(" d ");
        sql.append("         where ").append(makeParMonDayCond("t.", false));
        sql.append("          and ").append(makeCutDateCond("t.", false));
        sql.append("          and t.settle_proc_date is null ");
        sql.append("          and t.card_no=d.card_no and t.expiry_date=d.expiry_date and t.lms_invoice_no=d.lms_invoice_no ) )");
        
        try
        {
            logger.info("remarkProcessedForTxnDtlNoMatch()"+" "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            conn.commit();
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkProcessedForTxnDtlNoMatch():"+e);
        }
    }

  //20081205 因為累計活動的關係，取消交易可能造成與原交易不同之award的異動，所以取消交易一定要清算
    /**
     * 原交易與取消交易清算與否, 若不清算  註記已處理, txn_note:OrigCancelTxn
     * if CANCEL_FLAG=0 (不清算):<br/>
     * @throws Exception
     */
    protected void handleOrigCancelTxn() throws InterruptedException, SQLException, Exception
    {
        String cancelFlag = Layer2Util.getBatchConfig("CANCEL_FLAG");
        
        StringBuffer sql = new StringBuffer();
        
        //logger.debug("cancelFlag:"+cancelFlag);
        if (cancelFlag.equals("0")||StringUtil.isEmpty(cancelFlag)||cancelFlag==null)
        { // 取消交易與其原始皆不清算, 註記已處理, 之後就不會撈到這些交易
            // TB_TRANS_DTL註記已處理
            sql.append("Update ").append(TBNAME_TRANSDTL).append(" set");
            sql.append("   SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append("  ,SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append("  ,TXN_NOTE=(TXN_NOTE||'").append(Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "OrigCancelTxn")).append("')");
            sql.append(" where").append(makeParMonDayCond("", false));
            sql.append("   and SETTLE_PROC_DATE is null ");
            sql.append("   and (CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO) in ");
            sql.append("        (select CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO from ").append(TBNAME_TRANS);
            sql.append("         where").append(makeParMonDayCond("", false));
            sql.append("           and SETTLE_PROC_DATE is null "); 
            sql.append("           and (").append(getUnSettleCond()).append(")");
            sql.append("           and (STATUS = 'C' or P_CODE IN (SELECT P_CODE FROM TB_P_CODE_DEF WHERE IS_SETL_FLAG = '0' AND P_CODE LIKE '%8')) )");

            try
            {
                logger.info("handleOrigCancelTxn()"+" "+sql);
                DBService.getDBService().sqlAction(sql.toString(), conn, false);
            }
            catch (SQLException e)
            {
                conn.rollback();
                logger.warn(" sql:"+sql);
                throw new SQLException("handleOrigCancelTxn():Update TB_TRANS_DTL fail. "+e);
            }
            sql.delete(0, sql.length());

            // TB_TRANS註記已處理 
            sql.append("Update ").append(TBNAME_TRANS).append(" set");
            sql.append(" SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append("  ,SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append("  ,TXN_NOTE=(TXN_NOTE||'").append(Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "OrigCancelTxn")).append("')");
            sql.append(" where ").append(makeParMonDayCond("", false));
            sql.append("   and (").append(getUnSettleCond()).append(")");
            sql.append("   and SETTLE_PROC_DATE is null ");
            sql.append("   and (STATUS = 'C' or P_CODE IN (SELECT P_CODE FROM TB_P_CODE_DEF WHERE IS_SETL_FLAG = '0' AND P_CODE LIKE '%8'))");

            try
            {   logger.info("handleOrigCancelTxn()"+" "+sql);
                DBService.getDBService().sqlAction(sql.toString(), conn, false);
                conn.commit();
            }
            catch (SQLException e)
            {
                conn.rollback();
                logger.warn(" sql:"+sql);
                throw new SQLException("handleOrigCancelTxn():Update TB_TRANS fail. "+e);
            }
        }
        else if (cancelFlag.equals("1"))
        { // 取消交易與其原始皆清算, 之後會撈到這些交易
            logger.info("Cancel trans would be settle.");
            //do nothing
        }
    }

    /**
     * 針對合法性驗證失敗就不清算的交易 trans_dtl註記已處理, rcode:2401
     * @throws Exception
     */
    protected void remarkProcessedForInvalidTxn() throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(TBNAME_TRANSDTL).append(" set ");
        sql.append("   SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
        sql.append("  ,SETTLE_RCODE='").append(Constants.RCODE_2401_INVALID_TXN).append("'");
        sql.append(" where ").append(makeParMonDayCond("", false));
        sql.append("   and SETTLE_PROC_DATE is null ");
        sql.append("   and (CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO) in");
        sql.append("        (select CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO From ").append(TBNAME_TRANS);
        sql.append("          where ").append(makeParMonDayCond("", false));
        sql.append("            and ").append(getUnSettleCond());
        sql.append("            and SETTLE_PROC_DATE is null "); 
        sql.append("            and CHECK_FLAG = 1");
        sql.append("            and (CARD_NO,EXPIRY_DATE,LMS_INVOICE_NO) in");
        sql.append("                (select CARD_NO,EXPIRY_DATE,LMS_INVOICE_NO from TB_TRANS_CHECK_ERR");
        sql.append("                  where CHECK_RCODE in");
        sql.append("                  (Select RCODE From TB_RCODE_CONFIG Where SETTLEMENT_FLAG='0'))");
        sql.append("        )");
        try
        {
            logger.info("remarkProcessedForInvalidTxn()"+" "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            conn.commit();
        }
        catch (SQLException e)
        {
            conn.rollback();
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkProcessedForInvalidTxn(): "+e);
        }
    }

    /**
     * 針對不須清算BONUS_ID, trans_dtl註記已處理, txn_note:NoNeedSettleBonus
     * @throws Exception
     */
    protected void remarkProcessedForNoNeedSettleBonus() throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(TBNAME_TRANSDTL).append(" set ");
        sql.append("   SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
        sql.append("  ,SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
        sql.append("  ,TXN_NOTE=(TXN_NOTE||'").append(Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "NoNeedSettleBonus")).append("')");
        sql.append(" where ").append(makeParMonDayCond("", false));
        sql.append("   and SETTLE_PROC_DATE is null ");
        sql.append("   and BONUS_ID in ( select BONUS_ID from TB_BONUS where SETTLEMENT_FLAG='0')");
        try
        {
            logger.info("remarkProcessedForNoNeedSettleBonus()"+" "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            conn.commit();
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkProcessedForNoNeedSettleBonus():"+e);
        }
    }
    


    /**
     * 針對未設定於TB_SETTLE_CONFIG的p_code+txn_code+bonus_id, trans_dtl註記已處理, txn_note:NoSetSettleBonus
     * @throws Exception
     */
    protected void remarkProcessedForNoSetSettleBonus() throws SQLException
    {         
        StringBuffer noNeedSettlePCodeTxnCodeSb = new StringBuffer();
        noNeedSettlePCodeTxnCodeSb.append("select T.P_CODE, T.TXN_CODE, C.BONUS_ID ");
        noNeedSettlePCodeTxnCodeSb.append(" from TB_SETTLE_CONFIG C, TB_SETTLE_TXN T ");
        noNeedSettlePCodeTxnCodeSb.append(" where C.SETTLE_CODE=T.SETTLE_CODE");
        noNeedSettlePCodeTxnCodeSb.append("   and C.VALID_SDATE<='").append(getBatchDate()).append("'");
        noNeedSettlePCodeTxnCodeSb.append("   and C.VALID_EDATE>='").append(getBatchDate()).append("'");
    
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(TBNAME_TRANSDTL).append(" set ");
        sql.append("   SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
        sql.append("  ,SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'"); //2409
        sql.append("  ,TXN_NOTE=(TXN_NOTE||'").append(Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "NoSetSettleBonus")).append("')");
        sql.append(" where ").append(makeParMonDayCond("", false));
        sql.append("   and SETTLE_PROC_DATE is null ");
        sql.append("   and (P_CODE, TXN_CODE, BONUS_ID) not in (").append(noNeedSettlePCodeTxnCodeSb).append(")");
    
        try
        {
            logger.info("remarkProcessedForNoSetSettleBonus()"+" "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            conn.commit();
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkProcessedForNoSetSettleBonus():"+e);
        }
    }


    /**
     * 針對未設定於TB_SETTLE_CONFIG&的p_code+txn_code, trans_dtl註記已處理, txn_note:NoNeedSettleTxn
     * @throws Exception
     */
    protected void remarkProcessedForNoNeedSettleTxn() throws SQLException
    {
        StringBuffer noNeedSettlePCodeTxnCodeSb = new StringBuffer();
        noNeedSettlePCodeTxnCodeSb.append("select T.P_CODE, T.TXN_CODE ");
        noNeedSettlePCodeTxnCodeSb.append(" from TB_SETTLE_CONFIG C, TB_SETTLE_TXN T ");
        noNeedSettlePCodeTxnCodeSb.append(" where C.SETTLE_CODE=T.SETTLE_CODE");
        noNeedSettlePCodeTxnCodeSb.append("   and C.VALID_SDATE<='").append(getBatchDate()).append("'");
        noNeedSettlePCodeTxnCodeSb.append("   and C.VALID_EDATE>='").append(getBatchDate()).append("'");

        StringBuffer sql = new StringBuffer();
        sql.delete(0, sql.length());
        sql.append("update ").append(TBNAME_TRANSDTL).append(" set ");
        sql.append("  SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
        sql.append(" ,SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'"); 
        sql.append(" ,TXN_NOTE=(TXN_NOTE||'").append(Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "NoSetSettleTxn")).append("')");
        sql.append(" where ").append(makeParMonDayCond("", false));
        sql.append("   and SETTLE_PROC_DATE is null ");
        sql.append("   and (P_CODE, TXN_CODE) not in (").append(noNeedSettlePCodeTxnCodeSb).append(")");
        

        try
        {
            logger.info("remarkProcessedForNoNeedSettleTxn()"+" "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            conn.commit();
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkProcessedForNoNeedSettleTxn():"+e);
        }
    }

    /**
     * 超過settle_rate有效期的交易 註記已處理, rcode:2403 
     * @throws Exception
     */
    protected void remarkProcessedForInvalidDateRate() throws SQLException
    {
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(TBNAME_TRANSDTL).append(" set SETTLE_PROC_DATE=");
        sql.append(" '").append(getBatchDate()).append("',");
        sql.append(" SETTLE_RCODE='").append(Constants.RCODE_2403_NO_SETTLE_RATE).append("'");
        sql.append(" where ").append(makeParMonDayCond("", false));
        sql.append("   and SETTLE_PROC_DATE is null ");
        sql.append("   and (P_CODE, TXN_CODE, BONUS_ID) in ");
        sql.append("       ( select x.P_CODE, x.TXN_CODE, c.BONUS_ID from TB_SETTLE_CONFIG c, TB_SETTLE_TXN x");
        sql.append("          where c.SETTLE_CODE=x.SETTLE_CODE ");
        sql.append("            and (VALID_SDATE>'").append(getBatchDate()).append("' OR VALID_EDATE<'").append(getBatchDate()).append("') ");
        sql.append("         MINUS ");
        sql.append("         select x.P_CODE, x.TXN_CODE, c.BONUS_ID from TB_SETTLE_CONFIG c, TB_SETTLE_TXN x");
        sql.append("          where c.SETTLE_CODE=x.SETTLE_CODE ");
        sql.append("            and VALID_SDATE<='").append(getBatchDate()).append("' AND VALID_EDATE>='").append(getBatchDate()).append("' )");

        /*	
		    select x.P_CODE,x.TXN_CODE,c.BONUS_ID from TB_SETTLE_CONFIG c, TB_SETTLE_TXN x
		    where c.SETTLE_CODE=x.SETTLE_CODE and VALID_SDATE>'20080623' OR VALID_EDATE<'20080623'
		    MINUS
		    select x.P_CODE,x.TXN_CODE,c.BONUS_ID from TB_SETTLE_CONFIG c, TB_SETTLE_TXN x
		    where c.SETTLE_CODE=x.SETTLE_CODE and VALID_SDATE<='20080623' AND VALID_EDATE>='20080623'
         */
        
        try
        {
            logger.info("remarkProcessedForInvalidDateRate()"+" "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            conn.commit();
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkProcessedForInvalidDateRate():"+e);
        }
    }

    /**
     * 撈出TB_SETTLE_CONFIG定義要清算的txn_code<br/>
     * @return settleConfigList
     * @throws Exception
     */
    protected Vector preCondition() throws Exception
    {
        Vector settleConfigVtr = new Vector();
        StringBuffer sql = new StringBuffer();
        sql.append("select SETTLE_CODE, ACQ_MEM_ID, ISS_MEM_ID, BONUS_ID, ");
        sql.append(" SETTLE_FROM, SETTLE_RATE, CARRY_DIGIT, CARRY_TYPE, ");
        sql.append(" VALID_SDATE, VALID_EDATE, FUND_TYPE, SETTLE_TARGET, BANK_ID ");
        sql.append(" FROM TB_SETTLE_CONFIG, TB_MEMBER ");
        sql.append(" WHERE VALID_SDATE<='").append(getBatchDate()).append("'");
        sql.append(" AND VALID_EDATE>='").append(getBatchDate()).append("'");
        sql.append(" AND TB_SETTLE_CONFIG.ACQ_MEM_ID = TB_MEMBER.MEM_ID(+) ");
        
        try
        {
        	//清算處理順序 AllMember
        	settleConfigVtr.addAll(BatchUtil.getInfoListHashMap
            		(sql.toString() + " AND SETTLE_TARGET = '1' "));
        	settleConfigVtr.addAll(BatchUtil.getInfoListHashMap
            		(sql.toString() + " AND ISS_MEM_ID<>'" + getAllMember() + "' and ACQ_MEM_ID<>'" + getAllMember() + "' AND SETTLE_TARGET = '0' "));
            settleConfigVtr.addAll(BatchUtil.getInfoListHashMap
            		(sql.toString() + " AND ISS_MEM_ID<>'" + getAllMember() + "' and ACQ_MEM_ID='" + getAllMember() + "' AND SETTLE_TARGET = '0' "));
            settleConfigVtr.addAll(BatchUtil.getInfoListHashMap
            		(sql.toString() + " AND ISS_MEM_ID='" + getAllMember() + "' and ACQ_MEM_ID<>'" + getAllMember() + "' AND SETTLE_TARGET = '0' "));
            settleConfigVtr.addAll(BatchUtil.getInfoListHashMap
            		(sql.toString() + " AND ISS_MEM_ID='" + getAllMember() + "' and ACQ_MEM_ID='" + getAllMember() + "' AND SETTLE_TARGET = '0' "));
           
            logger.debug("settleConfigVtr: " + settleConfigVtr);
        }
        
        catch (Exception e)
        {
            logger.warn(" sql:"+sql +" " +e);
            throw new Exception("preCondition():"+e);
        }
        logger.info(sql.toString());
        return settleConfigVtr;
    }
    
    
    /**
     * 每次處理一個清算設定<br/>
     * 每處理固定筆數, 就commit一次<br/>
     * @param settleConfigInfo
     * @throws Exception
     */
    protected void action(HashMap settleConfigHm) throws Exception
    {
        logger.debug("action()!");
        //TB_SETTLE_DEF: get CREDIT_UNIT & DEBIT_UNIT & ACCOUTCODE
        String settleCode = (String) settleConfigHm.get("SETTLE_CODE");
        /*//fund_type
        String fundType = (String) settleConfigHm.get("FUND_TYPE");*/
        Vector settleDefVtr = getSettleDef(settleCode);
        if (settleDefVtr.size()==0)
        {
            String errDefMsg = "No data matches settleCode: "+settleCode+" in TB_SETTLE_DEF";
            logger.warn(errDefMsg);
            return;
        }
        //logger.debug("settleDefVtr.size:"+settleDefVtr.size());
        HashMap settleDefHm = (HashMap) settleDefVtr.get(0);
        
        SettleInfo setlInfo = new SettleInfo();
        setlInfo.setSettleInfo(settleConfigHm, settleDefHm); //tb_settle_config + tb_settle_def + 撥款日計算
        
        //TB_SETTLE_TXN:get pCode & txnCode
        Vector settleTxnVtr = getSettleTxn(settleCode);
        if (settleTxnVtr.size()==0)
        {
            String errTxnMsg = "No data matches settleCode: "+settleCode+" in TB_SETTLE_TXN";
            logger.warn(errTxnMsg);
            return;
        }
        logger.debug("settleTxnVtr.size():"+settleTxnVtr.size()); 
        
        Statement stmt = null;
        ResultSet transRs = null;
        
        try {
        	stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);  

	        for (int i=0; i<settleTxnVtr.size(); i++)
	        {
	        	String pCode =  (String) ((HashMap) settleTxnVtr.get(i)).get("P_CODE");
	        	String txnCode = (String) ((HashMap) settleTxnVtr.get(i)).get("TXN_CODE");
	            
	            //int unCommitCnt = 0; //識別未commit交易數
	            logger.debug(i+" pCode="+pCode+" txnCode="+txnCode);
	            
	
	           
	            transRs = stmt.executeQuery( getTransactionSqlCmd(pCode, txnCode, settleConfigHm) );
	            int recCnt = 0;
	            while (transRs.next())
	            {
	            	logger.debug("rs.getRow():"+transRs.getRow());
                    try {
                        settleTransaction(conn, setlInfo, transRs); //******** 處理清算明細 *********
                    } catch (Exception e) {
                        throw new Exception("settleTransaction():"+e);
                    }
                    
                    //要使用isLast() --> 需設定為ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE
                    if (recCnt==getCommitCount() || transRs.isLast()) 
                    { //到了固定筆數 或 已經最後一筆, commit
                        try {
                            conn.commit();
                            logger.debug(recCnt+" sleep:"+getSleepTime());
                            Thread.sleep(getSleepTime());
                        } catch(SQLException e) {
                            conn.rollback();
                            throw new Exception("action():commit failed. "+e);
                        }
                    }
                    recCnt++;
	            } //while
	        } //for i   
		} catch (Exception e) {
			throw e;
		} 
        finally
        {
            ReleaseResource.releaseDB(null, stmt, transRs);
        }  

        logger.debug(settleDefVtr+" settleConfigList:"+settleConfigHm+"\n");
    } //action()

    /** 
     * 入扣帳單位, 撈TB_SETTLE_DEF
     * @param settleCode
     * @return creditUnit, debitUnit, accountCode
     */
    protected Vector getSettleDef(String settleCode) throws Exception
    {
        StringBuffer sql = new StringBuffer();
        sql.append("Select CREDIT_UNIT, DEBIT_UNIT, ACCOUNT_CODE FROM TB_SETTLE_DEF ");
        sql.append(" where SETTLE_CODE='").append(settleCode).append("'");
        
        Vector settleDefVtr;
        try
        {
            settleDefVtr = BatchUtil.getInfoListHashMap(sql.toString());
        }
        catch (Exception e)
        {
            logger.warn(" sql:"+sql);
            throw new Exception("getSettleDef():"+e);
        }
        
        return settleDefVtr;
    }
    
    /** 
     * 須清算的交易P_CODE TXN_CODE
     * @param settleCode
     * @return settleTxnList
     */
    protected Vector getSettleTxn(String settleCode) throws Exception
    {
        StringBuffer sql = new StringBuffer();
        sql.append("Select P_CODE, TXN_CODE FROM TB_SETTLE_TXN ");
        sql.append(" where SETTLE_CODE='").append(settleCode).append("'");
        
        Vector settleTxnVtr;
        try
        {
            settleTxnVtr = BatchUtil.getInfoListHashMap(sql.toString());
        }
        catch (Exception e)
        {
            logger.warn(" sql:"+sql);
            throw new Exception("getSettleTxn():"+e);
        }
        
        return settleTxnVtr;
    }

    /**
     * 找出所有需清算的交易(TB_TRANS & TB_TRANS_DTL )
     * @param pCode
     * @param txnCode
     * @param settleConfigHm
     * @return txnList
     */
    protected Vector getTransaction(String pCode, String txnCode, HashMap settleConfigHm) 
                        throws Exception
    {
        String sql = getTransactionSqlCmd(pCode, txnCode, settleConfigHm);
        logger.debug("getTransaction() "+ " sql: "+sql);
        Vector txnListVtr = new Vector();
        try
        {
            txnListVtr = BatchUtil.getInfoListHashMap(sql);
        }
        catch (Exception e)
        {
            logger.warn(" sql:"+sql);
            throw new Exception("getTransaction() txnListVtr.size()="+txnListVtr.size()+" "+e);
        }       
        logger.debug(txnListVtr.size()+" "+txnListVtr); 
        return txnListVtr;
        //transcationList = txnListVtr;
        //return (transcationList.size()>0)?true:false;
    }
    
    /**
     * for getTransactionSqlCmd()
     */
    protected String getTransSqlColumns()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(" a.CARD_NO, a.EXPIRY_DATE, a.LMS_INVOICE_NO,");
        sb.append(" a.P_CODE, b.TRANS_TYPE, a.TXN_CODE, a.BONUS_ID, "); //20071011 by rock, remove:a.BONUS_ID1, a.BONUS_ID2,
        sb.append(" a.BONUS_SDATE, a.BONUS_EDATE, a.BONUS_QTY, a.TXN_REDEEM_AMT,");
        sb.append(" a.CREDIT_UNIT, a.DEBIT_UNIT, a.CREDIT_ID, a.DEBIT_ID,"); //調帳時要用
        sb.append(" b.ACQ_MEM_ID, b.ISS_MEM_ID, b.MERCH_ID, b.TERM_ID, b.BATCH_NO,");
        sb.append(" b.TERM_SETTLE_DATE, b.TERM_SETTLE_TIME ");
      
        return sb.toString();
    }
    
    /**
     * 依txnCode產生撈取TxnList的SQL
     * @param txnCode
     * @return sqlCmd
     */
    protected String getTransactionSqlCmd(String pCode, String txnCode, HashMap settleConfigHm)
    {
        
        String acqMemId = (String) settleConfigHm.get("ACQ_MEM_ID");
        String issMemId = (String) settleConfigHm.get("ISS_MEM_ID");
        String bonusId = (String) settleConfigHm.get("BONUS_ID");
        String bankId = (String) settleConfigHm.get("BANK_ID");
        String settleTarget = (String) settleConfigHm.get("SETTLE_TARGET");

        //TB_TRANS
        StringBuffer transCond = new StringBuffer(getUnSettleCond());
        if (!StringUtil.isEmpty(acqMemId) && !acqMemId.equals(getAllMember()) && settleTarget.equals("0")) {
            transCond.append(" and ACQ_MEM_ID='"+acqMemId+"'");
        }
        
        if (!StringUtil.isEmpty(issMemId) && !issMemId.equals(getAllMember())) {
            transCond.append(" and ISS_MEM_ID='"+issMemId+"'");
        }
        
        //銀行
        if (settleTarget.equals("1")){
        	transCond.append(" AND (CARD_NO, EXPIRY_DATE) IN ( SELECT CARD_NO, EXPIRY_DATE FROM TB_CARD WHERE BANK_ID = '"+bankId+"')");
        }
        
        transCond.append(makeParMonDayCond("", true));
        //transCond.append(" and rownum <= "+getCommitCount()); //每次只抓trans 前面 10000筆 (spring設定:commitCount)
        
        //TB_TRANS_DTL
        StringBuffer transDtlCond = new StringBuffer();
        transDtlCond.append(" P_CODE='").append(pCode).append("'");
        transDtlCond.append(" and TXN_CODE='").append(txnCode).append("'"); 
        if (!StringUtil.isEmpty(bonusId)) {
            transDtlCond.append(" and BONUS_ID='").append(bonusId).append("'");
        }
        transDtlCond.append(makeParMonDayCond("", true));


        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(getTransSqlColumns()).append(" from ");
        sql.append("(select * from ").append(TBNAME_TRANSDTL).append(" where ").append(transDtlCond).append(") a,");
        sql.append("(select * from ").append(TBNAME_TRANS).append(" where ").append(transCond).append(") b ");
        sql.append(" where a.CARD_NO=b.CARD_NO");
        sql.append("   and a.EXPIRY_DATE=b.EXPIRY_DATE");
        sql.append("   and a.LMS_INVOICE_NO=b.LMS_INVOICE_NO");
        sql.append("   and a.settle_proc_date is null ");
        sql.append("   and a.settle_rcode='").append(Constants.RCODE_0000_OK).append("'");
        sql.append("   and b.settle_proc_date is null ");
        sql.append("   and b.settle_rcode='").append(Constants.RCODE_0000_OK).append("'");
        
        logger.debug("getTransactionSqlCmd():"+sql+"\n");
        return sql.toString();
    }

    /**
     * 每次處理一筆交易
     * @param conn
     * @param creditUnit
     * @param debitUnit
     * @param txnInfo
     * @throws Exception
     */
    protected void settleTransaction(Connection conn, 
                                     SettleInfo setlInfo, 
                                     ResultSet transRs) throws Exception
    {
        //檢查settle_from
        String setlFrom = setlInfo.getSettleFrom().toUpperCase();
        if (!setlFrom.equals(Constants.SETTLE_FROM_BONUS_QTY)&&
            !setlFrom.equals(Constants.SETTLE_FROM_TXN_REDEEM_AMT))
        { //SETTLE_FROM設定錯誤:2407
            logger.debug("remarkFail("+Constants.RCODE_2407_SETTLE_FROM_ERR+")");
            remarkFail(conn, transRs, Constants.RCODE_2407_SETTLE_FROM_ERR);
            return;
        }
        logger.debug("txnInfo: "+transRs.getString("CARD_NO")+" "+transRs.getString("EXPIRY_DATE")+" "+transRs.getString("LMS_INVOICE_NO"));

        String crUnit = setlInfo.getCreditUnit();
        String dbUnit = setlInfo.getDebitUnit();
        if ( !crUnit.equals("S") && !dbUnit.equals("S") )
        { //如果該交易沒有符合任何出資單位
            try
            {
                logger.debug("No Campaign!");
                handleNoCampaign(conn, setlInfo, transRs);
            }
            catch (Exception e)
            {
                throw new Exception("handleNoCampaign():"+e);
            }
        }
        else 
        { //有出資單位:call campaign
            handleSponsorFromCampaign(transRs, setlInfo);
        }
    }
    
    protected void handleSponsorFromCampaign(ResultSet transRs, SettleInfo setlInfo) throws Exception
    {
        List campaignList = getSponsorFromCampaign(transRs); // PayRateDetail

        if (campaignList==null || campaignList.size()==0)
        { //2410 撈不到campaign
            remarkFail(conn, transRs, Constants.RCODE_2410_CAMPAIGN_FAIL);
            return;
        }

        if (setlInfo.getDebitUnit().equals("TXN_REDEEM_AMT") && campaignList.size()>1)
        { //2412 settle_from="TXN_REDEEM_AMT"(REEDEEM), 只能設一筆campaign活動
            remarkFail(conn, transRs, Constants.RCODE_2412_MULTI_CAMPAIGN_OF_REDEEM);
            return;
        }

        if (campaignList.size()>0)
        { //如果該交易明細有符合任何活動 每次處理一個活動的回饋
        	isHandleSponsorFail = false;
            for (int i=0; i<campaignList.size(); i++)
            {//Fetch each campaignDtlList from campaignInfo
                PayRateDetail campaignDtlInfo = (PayRateDetail) campaignList.get(i);
                
                logger.info("Single Campaign! -" + i);
                /* 依每個活動, 計算出資點數, 出資金額 */
                /* "出資點數" 的計算法則: 預設依每個出資單位, 四捨五入到小數2位 */
                handleSingleSponsor(conn, transRs, setlInfo, campaignDtlInfo);
            } //for 
        }
    }
    
    
    protected List getSponsorFromCampaign(ResultSet transRs) throws Exception
    {
        List sponsorList = null;

        //PayRate payRate = null; 
        //payRate = callCampaign(transRs);
        
        //...compaign.OriginalTransactionDtlInfo
        OriginalTransactionDtlInfo oriTxnDtlInfo = new OriginalTransactionDtlInfo();
        oriTxnDtlInfo.setCardNo(transRs.getString("CARD_NO"));
        oriTxnDtlInfo.setExpiryDate(transRs.getString("EXPIRY_DATE"));
        oriTxnDtlInfo.setLmsInvoiceNo(transRs.getString("LMS_INVOICE_NO"));
        oriTxnDtlInfo.setPCode(transRs.getString("P_CODE"));
        oriTxnDtlInfo.setTxnCode(transRs.getString("TXN_CODE"));
        oriTxnDtlInfo.setId(transRs.getString("BONUS_ID"));
        oriTxnDtlInfo.setBonusSDate(transRs.getString("BONUS_SDATE"));
        oriTxnDtlInfo.setBonusEDate(transRs.getString("BONUS_EDATE"));
        
        
        PayRate payRate = new PayRate(); // 存放getDetail()的回傳值
        CampaignPayRate campaignPayRate = new CampaignPayRate();
        try
        {
            campaignPayRate.getDetail(conn, oriTxnDtlInfo, payRate);
        }
        catch (Exception e)
        {
            throw new Exception("getSponsorFromCampaign() call campaignPayRate.getDetail():"+e);
        }
        
        sponsorList =  payRate.getPayRateDetail();
        
        return sponsorList;
    }
    
    
    
    /** TODO 分割此method
     * 每次處理一個campaign
     * @param conn
     * @param txnInfo
     * @param creditUnit
     * @param debitUnit
     * @param settleRateInfoHm
     * @param campaignDtlInfo
     * @throws Exception
     */
    protected void handleSingleSponsor(Connection conn,
    		                           ResultSet transRs, 
                                       SettleInfo setlInfo, 
                                       PayRateDetail campaignDtlInfo) throws Exception
    {
    	if (isHandleSponsorFail==true) {
        	return;
    	}
    	
        String acqMemId = transRs.getString("ACQ_MEM_ID");
        String issMemId = transRs.getString("ISS_MEM_ID");
        String merchId = transRs.getString("MERCH_ID");
        String bonusId = transRs.getString("BONUS_ID");
        
        String crUnit = setlInfo.getCreditUnit();
        String dbUnit = setlInfo.getDebitUnit();
        String acCode = setlInfo.getAccountCode();
        
        String creditId = "";
        String debitId = "";
        logger.debug("crUnit:"+crUnit+" dbUnit:"+dbUnit +" acCode:"+acCode);
        if (!crUnit.equals("S") && !crUnit.equals("T"))
        {//找出入帳單位非"S"或"T"的id //T:BOCCC only
            creditId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, bonusId, crUnit);
        }
        if (!dbUnit.equals("S") && !dbUnit.equals("T"))
        {//找出扣帳單位非"S"或"T"的id //T:BOCCC only
            debitId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, bonusId, dbUnit);
        }
        
        String progId = campaignDtlInfo.getProgId();
        double progQty = campaignDtlInfo.getQty(); 
        //不能用txnInfo.bonusQty,這是sponsor的sum =>用campaignDtlInfo.getQty()
        double txnRedeemAmt = transRs.getDouble("TXN_REDEEM_AMT");//((Number) txnInfo.get("TXN_REDEEM_AMT")).doubleValue();
        
        List sponsorList = campaignDtlInfo.getPayRateMember(); //取得出資單位&出資比率
        logger.info("progId:"+progId+" sponsorList.size:"+sponsorList.size());
        
        String resultCrId = "";
        String resultDbId = "";
        double settleAmt = 0.0;

        if (true == getIsCheckSponsorSum() ) 
        { //檢查sponsor出資比例設定
            logger.debug("isCheckSponsorSum():"+getIsCheckSponsorSum());
            double totSponsorRate = 0;
            for (int s=0; s<sponsorList.size(); s++)
            {
                PayRateMember sponsorInfo = (PayRateMember) sponsorList.get(s);
                logger.debug(sponsorInfo.getRate());
                totSponsorRate += sponsorInfo.getRate(); //出資比例
            }
            
            if (totSponsorRate!=100)
            { //出資比例錯誤:2411
            	isHandleSponsorFail = true;
                remarkFail(conn, transRs, Constants.RCODE_2411_CAMPAIGN_ERROR);
                return;
            }
        }
        
        if (sponsorList.size()==0||sponsorList==null)
        { //無出資單位:2402
        	isHandleSponsorFail = true;
            remarkFail(conn, transRs, Constants.RCODE_2402_NO_SPONSOR);
            return;
        }

        
        double calQty = 0.0;
        for (int i=0; i<sponsorList.size(); i++)
        {
            resultCrId = creditId;
            resultDbId = debitId;
            PayRateMember sponsorInfo = (PayRateMember) sponsorList.get(i);

            String sponsorId = sponsorInfo.getWhoPayId();
            String whoPayType = sponsorInfo.getWhoPayType();

            //logger.debug("crUnit:"+crUnit+" dbUnit:"+dbUnit+" sponsorId="+sponsorId+" whoPayType="+whoPayType);

            //if (Integer.parseInt(sponsorId)==0 || sponsorId.equals("00000000")) {
            if (!whoPayType.equals("04") && (sponsorId.equals("0") || sponsorId.equals("00000000"))) {
                sponsorId = getSponsorByUnit(whoPayType, acqMemId, issMemId, merchId);
                //logger.debug("* sponsorId="+sponsorId);
                if (crUnit.equals("S")) crUnit = transferSponsorUnit(whoPayType);
                if (dbUnit.equals("S")) dbUnit = transferSponsorUnit(whoPayType);
            }
            
            logger.debug("crUnit:"+crUnit+" dbUnit:"+dbUnit+" sponsorId="+sponsorId+" whoPayType="+whoPayType);

            if (crUnit.equals("T")) crUnit = whoPayType;
            if (dbUnit.equals("T")) dbUnit = whoPayType;

            if (StringUtil.isEmpty(resultCrId)) resultCrId = sponsorId; 
            if (StringUtil.isEmpty(resultDbId)) resultDbId = sponsorId; 
            
            calQty = 0.0;
            //有出資單位: settle_amt = (settle_from * 出資比例) * settle_rate
            String settleFrom = setlInfo.getSettleFrom().toUpperCase();
            if (settleFrom.equals(Constants.SETTLE_FROM_BONUS_QTY))
            { //BONUS_QTY
                calQty = progQty;//((Number)txnInfo.get("BONUS_QTY")).doubleValue();
                logger.debug("calQty="+calQty);
            }
            else if (settleFrom.equals(Constants.SETTLE_FROM_TXN_REDEEM_AMT)) 
            { //TXN_REDEEM_AMT
                calQty = txnRedeemAmt;
            }

            double sponsorRate = sponsorInfo.getRate(); //出資比例
            double sponsorQty = calBonusQtyForSponsor(sponsorRate, calQty); //計算出sponsor點數的清算金額
            
            setlInfo.setCampaignQty(sponsorQty);//20070531 wendy 要加tb_settle_result的欄位，方便追查，但只有ctcbd會寫入tb_settle_result
            
            //以sponsorQty計算出點數的清算金額
            settleAmt = calSettleAmt(setlInfo, sponsorQty, sponsorQty); 
            logger.info("crUnit:"+crUnit+" crId:"+resultCrId+" dbUnit:"+dbUnit+" dbId:"+resultDbId+" settleAmt:"+settleAmt+" txnRedeemAmt:"+txnRedeemAmt+" sponsorQty:"+sponsorQty);

            if (resultCrId.equals("")||resultDbId.equals("")||crUnit.equals("")||dbUnit.equals(""))
            {
            	isHandleSponsorFail = true;
                remarkFail(conn, transRs, Constants.RCODE_2404_NO_CR_DB); //無對應的入扣帳單位:2404
            }
            else
            {
                remarkSuccess(conn, transRs, progId, settleAmt, sponsorRate, setlInfo,
                              resultCrId, resultDbId, crUnit, dbUnit, acCode);
            }

            crUnit = setlInfo.getCreditUnit();
            dbUnit = setlInfo.getDebitUnit();
        } //for each sponsorList 
    }
    
    /**
     * 20070418
     * Campaign出資單位的屬性(TB_PROG_PAY_RATE.WHO_PAY_TYPE)為:
     * 05(商店=>"M"), 07(交易當時發卡單位==>"I"), 08(交易當時收單單位==>"A")
     * 若為以上四種屬性, 則活動的"出資單位代號"UI會填'0', 導至Settle_result的DEBIT_ID也會填'0'
     * @param whoPayType
     * @param acqMemId
     * @param issMemId
     * @param merchId
     * @return
     */
    protected String getSponsorByUnit(String whoPayType, 
                     String acqMemId, String issMemId, String merchId)
                     throws Exception
    {
        String unit = "";
        String sponsorId = "";
        try
        {
            unit = transferSponsorUnit(whoPayType);
            sponsorId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, "", unit);
            if (sponsorId.equals("") || sponsorId == null) {
                logger.warn("whoPayType=" + whoPayType + " sponsorId=" + sponsorId);
            }
        }
        catch (Exception e)
        {
            throw new Exception("getSponsorByUnit():"+e);
        }
        return sponsorId;
    }
    
    protected String transferSponsorUnit(String whoPayType)
    {
        String unit = "";
        
        if (whoPayType.equals("04")) unit = "S"; //20070704
        
        if (whoPayType.equals("05")) unit = "M";
        
        if (whoPayType.equals("07")) unit = "I";
        
        if (whoPayType.equals("08")) unit = "A";
        
        return unit;
    }
    
    /**
     * 沒有符合任何活動
     * @param conn
     * @param txnInfo
     * @param creditUnit
     * @param debitUnit
     * @param settleRateInfoHm
     * @throws Exception
     */
    protected void handleNoCampaign(Connection conn,
                                    SettleInfo setlInfo,
                                    ResultSet transRs) throws Exception
    {
        //boolean isAdjTxn = false;
        String creditId = "";
        String debitId = "";
        String crUnit = setlInfo.getCreditUnit();
        String dbUnit = setlInfo.getDebitUnit();
        String acCode = setlInfo.getAccountCode();
        //決定入扣帳單位代號 ****************************************************************
        if (crUnit.equals("N") && dbUnit.equals("N"))
        { //調帳交易 
            //isAdjTxn = true;
            setlInfo.setAdj(true);
            crUnit = transRs.getString("CREDIT_UNIT"); 
            dbUnit = transRs.getString("DEBIT_UNIT"); 
            
            creditId = transRs.getString("CREDIT_ID");
            debitId = transRs.getString("DEBIT_ID");
        }
        //會員銀行交易
        else if (crUnit.equals("U") || dbUnit.equals("U")){
        	setlInfo.setAdj(false);
            String acqMemId = setlInfo.getAcqMemId();
            String issMemId = transRs.getString("ISS_MEM_ID");
            String merchId = transRs.getString("MERCH_ID");
            String bonusId = transRs.getString("BONUS_ID");
            
            creditId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, bonusId, crUnit);
            debitId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, bonusId, dbUnit);
        }
        else
        { //一般交易
            setlInfo.setAdj(false);
            String acqMemId = transRs.getString("ACQ_MEM_ID");
            String issMemId = transRs.getString("ISS_MEM_ID");
            String merchId = transRs.getString("MERCH_ID");
            String bonusId = transRs.getString("BONUS_ID");
            
            creditId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, bonusId, crUnit);
            debitId = Layer2Util.getCreditDebitId(conn, acqMemId, issMemId, merchId, bonusId, dbUnit);
        }
        
        if (creditId.equals("")||debitId.equals("")||crUnit.equals("")||dbUnit.equals(""))
        { //無對應的入扣帳單位:2404
            remarkFail(conn, transRs, Constants.RCODE_2404_NO_CR_DB);
            return;
        }
        //決定入扣帳單位代號 ****************************************************************
        
        String progId = Constants.PROG_ID_NO_CAMPAIGN;//"000000000000";
//        double bonusQty = ((Number) txnInfo.get("BONUS_QTY")).doubleValue();
//        double txnRedeemAmt = ((Number) txnInfo.get("TXN_REDEEM_AMT")).doubleValue();
        double bonusQty = transRs.getDouble("BONUS_QTY");
        double txnRedeemAmt = transRs.getDouble("TXN_REDEEM_AMT");

        double settleAmt = 0.0; 
        //調整交易也要抓settle_rate 20080729 Clare
/*        if (setlInfo.isAdj()==true)
        { //如果 是調帳 交易, 不乘settle_rate
            
            if (bonusQty>0 && txnRedeemAmt>0)
            { //2406 調帳時trans_dtl的點數和金額 同時只能設一個，設那一個那個就是清算金額
                remarkFail(conn, transRs, Constants.RCODE_2406_ADJ_QTY_AMT_ERR); 
                return; 
            }
            
            if (bonusQty>0) {
                settleAmt = bonusQty;
            } else if (txnRedeemAmt>0) {
                settleAmt = txnRedeemAmt;
            } else {
                settleAmt = 0;  //如果點數和金額都是零
            }
            
            //調帳時不傳 setlInfo
            logger.info("bonusQty:"+bonusQty+" txnRedeemAmt:"+txnRedeemAmt+" settleAmt:"+settleAmt);
        }
        else 
        { //如果 不是調帳 交易, 要乘settle_rate
            settleAmt = calSettleAmt(setlInfo, bonusQty, txnRedeemAmt);
        }*/

        settleAmt = calSettleAmt(setlInfo, bonusQty, txnRedeemAmt);
        
        remarkSuccess(conn, transRs, progId, settleAmt, 0, setlInfo, creditId, debitId, crUnit, dbUnit, acCode);
    }


    /**
     * 呼叫Campaign API取得回饋明細
     * @param cardNo
     * @param expiryDate
     * @param lmsInvoiceNo
     * @param txnCode
     * @param bonusId
     * @return payRate
     */
//    protected PayRate callCampaign(ResultSet transRs) throws Exception
//    {
//        
//        //...compaign.OriginalTransactionDtlInfo
//        OriginalTransactionDtlInfo oriTxnDtlInfo = new OriginalTransactionDtlInfo();
//        oriTxnDtlInfo.setCardNo(transRs.getString("CARD_NO"));
//        oriTxnDtlInfo.setExpiryDate(transRs.getString("EXPIRY_DATE"));
//        oriTxnDtlInfo.setLmsInvoiceNo(transRs.getString("LMS_INVOICE_NO"));
//        oriTxnDtlInfo.setPCode(transRs.getString("P_CODE"));
//        oriTxnDtlInfo.setTxnCode(transRs.getString("TXN_CODE"));
//        oriTxnDtlInfo.setId(transRs.getString("BONUS_ID"));
//        oriTxnDtlInfo.setBonusSDate(transRs.getString("BONUS_SDATE"));
//        oriTxnDtlInfo.setBonusEDate(transRs.getString("BONUS_EDATE"));
//        
//        
//        PayRate payRate = new PayRate(); // 存放getDetail()的回傳值
//        CampaignPayRate campaignPayRate = new CampaignPayRate();
//        try
//        {
//            campaignPayRate.getDetail(conn, oriTxnDtlInfo, payRate);
//        }
//        catch (Exception e)
//        {
//            throw new Exception("callCampaign() call campaignPayRate.getDetail():"+e);
//        }
//        return payRate;    
//    }
    
    /**
     * 計算出點數的清算金額
     * @param settleRateInfo
     * @param bonusQty
     * @param txnRedeemAmt
     * @return settleAmt
     */
    protected double calSettleAmt(SettleInfo setlInfo, double bonusQty, double txnRedeemAmt)
    {
        double settleAmt = 0.0;
        double settleRate = setlInfo.getSettleRate();
        String settleFrom = setlInfo.getSettleFrom().toUpperCase();
        String carryType = setlInfo.getCarryType();
        int carryDigit = setlInfo.getCarryDigit();
        
        if (settleFrom.equals(Constants.SETTLE_FROM_BONUS_QTY))
        { //BONUS_QTY: 用點數bonusQty來計算實際金額
            settleAmt = Calc.mul(bonusQty, settleRate);
            logger.info("settleAmt = bonusQty * settleRate = "+bonusQty+" * "+settleRate+" = "+settleAmt);
        }
        else if (settleFrom.equals(Constants.SETTLE_FROM_TXN_REDEEM_AMT)) 
        { //TXN_REDEEM_AMT: 用金額txnRedeemAmt來計算實際金額 
            settleAmt = Calc.mul(txnRedeemAmt, settleRate);
            logger.info("settleAmt = txnRedeemAmt * settleRate = "+txnRedeemAmt+" * "+settleRate+" = "+settleAmt);
        }
        
        //無條件進位:U 無條件捨去:D 四捨五入:O
        settleAmt = Calc.roundFloat(settleAmt, carryDigit, carryType);
        return settleAmt;
    }
    
    /**
     * 計算出點數的清算金額 預設四捨五入到小數2位 
     * @param sponsorRate
     * @param bonusQty
     * @return caledQty
     */
    protected double calBonusQtyForSponsor(double sponsorRate, double bonusQty)
    {
        double caledQty = Calc.div(Calc.mul(sponsorRate, bonusQty), 100, 4);
        return Calc.roundFloat(caledQty, 2, Constants.CARRY_TYPE_ROUND_HALF_UP); //四捨五入:O 
    }
    
    /**
     * 註記清算成功
     * @param conn
     * @param txnInfo
     * @param progId
     * @param settleAmt
     * @param settleRate
     * @param creditUnit
     * @param creditId
     * @param debitUnit
     * @param debitId
     * @throws Exception
     */
    protected void remarkSuccess(Connection conn, 
    		                     ResultSet transRs, 
                                 String progId, 
                                 double settleAmt,
                                 double sponsorRate,
                                 SettleInfo setlInfo,
                                 String creditId,
                                 String debitId,
                                 String creditUnit,
                                 String debitUnit,
                                 String acCode) throws Exception
    {
        Number bonusQty = 0;
        Number txnRedeemAmt = 0;
        double settleRate = 0;
        String settleFrom = "";
        
        logger.debug("remarkSuccess!");
        if (true==setlInfo.isAdj())
        { //調帳時(此時無settlefrom), 用txnInfo的值
            logger.debug("adj!");
            settleRate = setlInfo.getSettleRate(); //20080901  調帳也要抓settle rate
            bonusQty = transRs.getDouble("BONUS_QTY");//(Number)txnInfo.get("BONUS_QTY");
            txnRedeemAmt = transRs.getDouble("TXN_REDEEM_AMT");//(Number)txnInfo.get("TXN_REDEEM_AMT");
        }
        else
        {
            settleRate = setlInfo.getSettleRate();
            settleFrom = setlInfo.getSettleFrom().toUpperCase();
            if (settleFrom.equals(Constants.SETTLE_FROM_BONUS_QTY))
            { 
                 bonusQty = transRs.getDouble("BONUS_QTY");//(Number)txnInfo.get("BONUS_QTY");
            }
            else if (settleFrom.equals(Constants.SETTLE_FROM_TXN_REDEEM_AMT))
            {
                 txnRedeemAmt = transRs.getDouble("TXN_REDEEM_AMT");//(Number)txnInfo.get("TXN_REDEEM_AMT");
            }
        }
        
        
        TbSettleResultMgr settleResultMgr = new TbSettleResultMgr(conn);
        TbSettleResultInfo settleResultInfo = new TbSettleResultInfo();

        //logger.debug("setSettleResultInfo!");
        settleResultInfo = setSettleResultInfo(transRs, 
                                               progId, 
                                               settleAmt,
                                               sponsorRate,
                                               setlInfo,
                                               creditId,
                                               debitId,
                                               creditUnit,
                                               debitUnit,
                                               acCode,
                                               bonusQty,
                                               txnRedeemAmt,
                                               settleRate
                                               );
    
        try
        {
            logger.debug("settleResultInfo.getSettleAmt():"+settleResultInfo.getSettleAmt());
            settleResultMgr.insert(settleResultInfo);
        }
        catch (SQLException sqle)
        {
            String errMsg = "remarkSuccess() insert TB_SETTLE_RESULT:"+sqle;
            logger.warn(errMsg+" settleResultInfo:"+settleResultInfo); 
            remarkFail(conn, transRs, Constants.RCODE_2420_INSERT_SETTLE_RESULT_ERR); 
            return;
            //throw new Exception(errMsg);
        }
        
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(TBNAME_TRANSDTL).append(" set");
        sql.append(" SETTLE_PROC_DATE= '").append(getBatchDate()).append("', ");
        sql.append(" SETTLE_SUCC_DATE='").append(getBatchDate()).append("'");
        sql.append(" where CARD_NO='").append(transRs.getString("CARD_NO")).append("'");
        sql.append(" and EXPIRY_DATE='" ).append(transRs.getString("EXPIRY_DATE")).append("'");
        sql.append(" and LMS_INVOICE_NO='" ).append(transRs.getString("LMS_INVOICE_NO")).append("'");
        sql.append(" and P_CODE='" ).append(transRs.getString("P_CODE")).append("'");
        sql.append(" and TXN_CODE='" ).append(transRs.getString("TXN_CODE")).append("'");
        sql.append(" and BONUS_ID='" ).append(transRs.getString("BONUS_ID")).append("'");
        sql.append(" and BONUS_SDATE='" ).append(transRs.getString("BONUS_SDATE")).append("'");
        sql.append(" and BONUS_EDATE='" ).append(transRs.getString("BONUS_EDATE")).append("'");
        
        try
        {
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new Exception("remarkSuccess() update TB_TRANS_DTL:"+e);
        }
 
//        try
//        {
//            conn.commit();
//        }
//        catch (SQLException e)
//        {
//            throw new Exception("remarkSuccess() commit:"+e);
//        }
    }
    
    protected TbSettleResultInfo setSettleResultInfo(ResultSet transRs, 
                                                     String progId, 
                                                     double settleAmt,
                                                     double sponsorRate,
                                                     SettleInfo setlInfo,
                                                     String creditId,
                                                     String debitId,
                                                     String creditUnit,
                                                     String debitUnit,
                                                     String acCode,
                                                     Number bonusQty,
                                                     Number txnRedeemAmt,
                                                     double settleRate) throws Exception
    {


        
        TbSettleResultInfo settleResultInfo = new TbSettleResultInfo();
        try
        {
            settleResultInfo.setSettleCode(setlInfo.getSettleCode());
            settleResultInfo.setExpPayDate(setlInfo.getExpPayDate());//EXP_PAY_DATE
            
            settleResultInfo.setIssMemId(transRs.getString("ISS_MEM_ID"));//不能用setlInfo，因為有可能是八個零
            settleResultInfo.setAcqMemId(transRs.getString("ACQ_MEM_ID"));//不能用setlInfo，因為有可能是八個零
            settleResultInfo.setMerchId(transRs.getString("MERCH_ID"));
            settleResultInfo.setTermId(transRs.getString("TERM_ID"));
            settleResultInfo.setBatchNo(transRs.getString("BATCH_NO"));
            settleResultInfo.setTermSettleDate(transRs.getString("TERM_SETTLE_DATE"));
            settleResultInfo.setTermSettleTime(transRs.getString("TERM_SETTLE_TIME"));
            settleResultInfo.setCardNo(transRs.getString("CARD_NO"));
            settleResultInfo.setExpiryDate(transRs.getString("EXPIRY_DATE"));
            settleResultInfo.setLmsInvoiceNo(transRs.getString("LMS_INVOICE_NO"));
            settleResultInfo.setPCode(transRs.getString("P_CODE"));
            //20190116 交通代碼對應
            settleResultInfo.setTransType(transRs.getString("TRANS_TYPE"));
            settleResultInfo.setTxnCode(transRs.getString("TXN_CODE"));
            settleResultInfo.setProgId(progId);
            settleResultInfo.setBonusId(transRs.getString("BONUS_ID"));
            //20071011 by rock
            //settleResultInfo.setBonusId1(transRs.getString("BONUS_ID1"));
            //settleResultInfo.setBonusId2(transRs.getString("BONUS_ID2"));

            settleResultInfo.setBonusSdate(transRs.getString("BONUS_SDATE")); //20070719
            settleResultInfo.setBonusEdate(transRs.getString("BONUS_EDATE")); //20070719 
            settleResultInfo.setProcDate(getBatchDate());
            settleResultInfo.setCreditUnit(creditUnit);
            settleResultInfo.setDebitUnit(debitUnit);
            settleResultInfo.setAccountCode(acCode); // 21040617 遠鑫作為產出資料的帳務分類
            settleResultInfo.setCreditId(creditId);
            settleResultInfo.setDebitId(debitId);
            settleResultInfo.setBonusQty(bonusQty);
            settleResultInfo.setTxnRedeemAmt(txnRedeemAmt);
            settleResultInfo.setSponsorRate(sponsorRate);
            settleResultInfo.setSettleRate(settleRate);
            settleResultInfo.setSettleAmt(settleAmt);
            settleResultInfo.setParMon(getBatchDate().substring(4, 6));
            settleResultInfo.setParDay(getBatchDate().substring(6, 8));
        }
        catch (Exception e)
        {

            logger.debug("setlInfo:"+setlInfo);
            logger.debug("txnInfo:"+transRs);
            logger.debug("settleResultInfo:"+settleResultInfo);
            throw new Exception(e);
        }        
        return settleResultInfo;
    }
    
    
    
    /**
     * 註記清算失敗
     * @param conn
     * @param txnInfo
     * @param settleRcode
     * @throws Exception
     */
    protected void remarkFail(Connection conn,
    						  ResultSet transRs, 
                              String settleRcode) throws Exception
    {
        logger.warn("remarkFail");
        super.setRcode(Constants.RCODE_2001_WARN);
        
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(TBNAME_TRANSDTL).append(" set");
        sql.append(" SETTLE_PROC_DATE= '").append(getBatchDate()).append("', ");
        sql.append(" SETTLE_RCODE='").append(settleRcode).append("'");
        sql.append(" where CARD_NO='").append(transRs.getString("CARD_NO")).append("'");
        sql.append(" and EXPIRY_DATE='" ).append(transRs.getString("EXPIRY_DATE")).append("'");
        sql.append(" and LMS_INVOICE_NO='" ).append(transRs.getString("LMS_INVOICE_NO")).append("'");
        sql.append(" and P_CODE='" ).append(transRs.getString("P_CODE")).append("'");
        sql.append(" and TXN_CODE='" ).append(transRs.getString("TXN_CODE")).append("'");
        sql.append(" and BONUS_ID='" ).append(transRs.getString("BONUS_ID")).append("'");
        sql.append(" and BONUS_SDATE='" ).append(transRs.getString("BONUS_SDATE")).append("'");
        sql.append(" and BONUS_EDATE='" ).append(transRs.getString("BONUS_EDATE")).append("'");
        
        try
        {   
            //logger.info("remarkFail("+settleRcode+")");
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkFail() update TB_TRANS_DTL:"+e);
        }
        
//        try
//        {
//            conn.commit();
//            Thread.sleep(getSleepTime());
//        }
//        catch (InterruptedException e)
//        {
//            throw new InterruptedException("remarkFail() Thread.sleep():"+e);
//        }
    }
    
    /** 
     * 針對沒處理到的交易註記已處理, txn_note:NoProcessed<br/>
     * @throws Exception
     */
    protected void remarkNoProcessedTxnDtl() throws Exception
    {
        StringBuffer sql = new StringBuffer();

        try
        {
        	StringBuffer jobWhereSql = new StringBuffer();
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
        	
            String txnNoteStr = Layer2Util.makeTxnNote(TXN_NOTE_HEAD, "NoProcessed");
            sql.append("update ").append(TBNAME_TRANS).append(" set ");
            sql.append("  SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append(" ,SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append(" ,TXN_NOTE=(txn_note||'").append(txnNoteStr).append("')");
            sql.append(" where CUT_DATE < '").append(batchDate).append("'");
            sql.append(" and SETTLE_PROC_DATE is null ");
            sql.append(" and NOT EXISTS (SELECT 1 FROM TB_SETTLE_TXN, TB_SETTLE_CONFIG");
            sql.append(" WHERE TB_SETTLE_TXN.SETTLE_CODE = TB_SETTLE_CONFIG.SETTLE_CODE"); 
            sql.append(" AND TB_SETTLE_TXN.P_CODE = ").append(TBNAME_TRANS).append(".P_CODE");
            sql.append(" AND SETTLE_TARGET = '1') ");
            
            if(jobWhereSql.length() > 0){
            	sql.append(" AND EXISTS (SELECT 1 FROM TB_MEMBER");
            	sql.append(" WHERE TB_MEMBER.MEM_ID = ").append(TBNAME_TRANS).append(".ACQ_MEM_ID");;
            	sql.append(jobWhereSql.toString());
            	sql.append(" )");
            }

            logger.info("remarkNoProcessedTxn() "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            
            sql.delete(0, sql.length());
            
            sql.append("update ").append(TBNAME_TRANSDTL).append(" set ");
            sql.append("  SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append(" ,SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append(" ,TXN_NOTE=(txn_note||'").append(txnNoteStr).append("')");
            sql.append(" where CUT_DATE < '").append(batchDate).append("'");
            sql.append("   and SETTLE_PROC_DATE is null ");
            sql.append(" and NOT EXISTS (SELECT 1 FROM TB_SETTLE_TXN, TB_SETTLE_CONFIG");
            sql.append(" WHERE TB_SETTLE_TXN.SETTLE_CODE = TB_SETTLE_CONFIG.SETTLE_CODE"); 
            sql.append(" AND TB_SETTLE_TXN.P_CODE = ").append(TBNAME_TRANSDTL).append(".P_CODE");
            sql.append(" AND TB_SETTLE_TXN.TXN_CODE = ").append(TBNAME_TRANSDTL).append(".TXN_CODE");
            sql.append(" AND SETTLE_TARGET = '1') ");
            
            if(jobWhereSql.length() > 0){
            	sql.append(" AND EXISTS (SELECT 1 FROM TB_TRANS, TB_MEMBER");
            	sql.append(" WHERE CUT_DATE < ").append(StringUtil.toSqlValueWithSQuote(getBatchDate()));
            	sql.append(" AND TB_TRANS.ACQ_MEM_ID = TB_MEMBER.MEM_ID");
            	sql.append(" AND TB_TRANS_DTL.CARD_NO = TB_TRANS.CARD_NO");
            	sql.append(" AND TB_TRANS_DTL.EXPIRY_DATE = TB_TRANS.EXPIRY_DATE");
            	sql.append(" AND TB_TRANS_DTL.LMS_INVOICE_NO = TB_TRANS.LMS_INVOICE_NO");
            	sql.append(jobWhereSql.toString());
            	sql.append(" )");
            }
            
            logger.info("remarkNoProcessedTxnDtl() "+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            

            conn.commit();
        }
        catch (SQLException e)
        {
            logger.warn(" sql:"+sql);
            throw new SQLException("remarkNoProcessedTxnDtl():"+e);
        }
    }

    
    /**
     * 將TB_TRANS_DTL已清算的註記資訊同步到TB_TRANS
     * @param conn
     * @throws Exception
     */
    protected void syncTransAndTransDtl() throws Exception
    {

        StringBuffer sql = new StringBuffer();
        try
        {
            //(1) TB_TRANS_DTL發生RCODE , 其對應TB_TRANS 就註記RCODE=2400
            sql.append("update ").append(TBNAME_TRANS).append(" set ");
            sql.append(" SETTLE_RCODE='").append(Constants.RCODE_2400_SETTLE_ERR).append("'");
            sql.append(",SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append("  where ").append(makeParMonDayCond("", false));
            sql.append("   and ").append(makeCutDateCond("", false));
            sql.append("   and SETTLE_PROC_DATE is null ");
            sql.append("   and (CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO) in");
            sql.append("       (select CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO ");
            sql.append("        from ").append(TBNAME_TRANSDTL);
            sql.append("        where ").append(makeParMonDayCond("", false));
            sql.append("          and SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append("          and SETTLE_RCODE <> '").append(Constants.RCODE_0000_OK).append("')");
            logger.info("syncTrans 2400!:"+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            
            
            sql.delete(0, sql.length()); //清空sql
            
            //(2-1)TB_TRANS_DTL全部都清算成功 , 其對應TB_TRANS 就註記RCODE=0000 並填上SETTLE_PROC_DATE
            sql.append("update ").append(TBNAME_TRANS).append(" set ");
            sql.append(" SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append(",SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append(" where ").append(makeParMonDayCond("", false));
            sql.append("   and ").append(makeCutDateCond("", false));
            sql.append("   and SETTLE_PROC_DATE is null ");
            sql.append("   and SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append("   and (CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO) in");
            sql.append("      (select CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO ");
            sql.append("       from ").append(TBNAME_TRANSDTL);
            sql.append("       where ").append(makeParMonDayCond("", false));
            sql.append("         and SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append("         and SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append("         Group by CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO)");
            logger.info("syncTrans 0000!:"+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            
            sql.delete(0, sql.length()); //清空sql
            
            //(2-2)TB_TRANS_DTL全部都清算成功 , 其對應TB_TRANS 填上SETTLE_SUCC_DATE
            sql.append("update ").append(TBNAME_TRANS).append(" set");
            sql.append(" SETTLE_SUCC_DATE='").append(getBatchDate()).append("'");
            sql.append(" where ").append(makeParMonDayCond("", false));
            sql.append("   and ").append(makeCutDateCond("", false));
            sql.append("   and SETTLE_PROC_DATE is not null ");
            sql.append("   and SETTLE_SUCC_DATE is null ");
            sql.append("   and SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append("   and (CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO) in");
            sql.append("      (select CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO ");
            sql.append("       from ").append(TBNAME_TRANSDTL);
            sql.append("       where ").append(makeParMonDayCond("", false));
            sql.append("         and SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            sql.append("         and SETTLE_SUCC_DATE='").append(getBatchDate()).append("'");
            sql.append("         and SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append("         Group by CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO)");
            logger.info("syncTrans succ!:"+sql);
            DBService.getDBService().sqlAction(sql.toString(), conn, false);
            
            conn.commit();
            Thread.sleep(getSleepTime());
        }
        catch (SQLException e)
        {
            conn.rollback();
            logger.warn(" sql:"+sql);
            throw new SQLException("handleOrigCancelTxn():"+e);
        }
        catch(Exception e)
        {
            conn.rollback();
            throw new Exception("handleOrigCancelTxn():"+e);
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
        Connection connSelf = null;
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
        
        
        StringBuffer sql = new StringBuffer();
        
        try
        {
            connSelf = BatchUtil.getConnection();
            
            if ( getRecoverLevel().equals(Constants.RECOVER_LEVEL_ALL)) 
            { //RecoverLevel=ALL時 Delete TB_SETTLE_RESULT
                sql.append(" DELETE TB_SETTLE_RESULT");
                sql.append(" WHERE PROC_DATE=").append(StringUtil.toSqlValueWithSQuote(getBatchDate()));
                if(jobWhereSql.length() > 0){
                	sql.append(" AND EXISTS (");
                	sql.append(" SELECT 1 FROM TB_MEMBER");
                	sql.append(" WHERE TB_MEMBER.MEM_ID = TB_SETTLE_RESULT.ACQ_MEM_ID");
                	sql.append(jobWhereSql.toString());
                	sql.append(" )");
                }
                
                logger.info(" recoverData():"+sql.toString());
                DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
    
                sql.delete(0, sql.length()); //清空sql
            }
            
          	//Update TB_TRANS_DTL
            sql.append("UPDATE ").append(TBNAME_TRANSDTL).append(" SET ");
            sql.append(" SETTLE_PROC_DATE = NULL");
            sql.append(" ,SETTLE_SUCC_DATE = NULL");
            sql.append(" ,SETTLE_RCODE=").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
            sql.append(" ,TXN_NOTE=" + getUpdateTxnNoteSql());
            sql.append(" WHERE SETTLE_PROC_DATE=").append(StringUtil.toSqlValueWithSQuote(getBatchDate()));
            if ( getRecoverLevel().equals(Constants.RECOVER_LEVEL_ERR) ) 
            {
                sql.append(" AND SETTLE_RCODE <> ").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
            }
            if(jobWhereSql.length() > 0){
            	sql.append(" AND EXISTS (SELECT 1 FROM TB_TRANS, TB_MEMBER");
            	sql.append(" WHERE SETTLE_PROC_DATE = ").append(StringUtil.toSqlValueWithSQuote(getBatchDate()));
            	sql.append(" AND TB_TRANS.ACQ_MEM_ID = TB_MEMBER.MEM_ID");
            	sql.append(" AND TB_TRANS_DTL.CARD_NO = TB_TRANS.CARD_NO");
            	sql.append(" AND TB_TRANS_DTL.EXPIRY_DATE = TB_TRANS.EXPIRY_DATE");
            	sql.append(" AND TB_TRANS_DTL.LMS_INVOICE_NO = TB_TRANS.LMS_INVOICE_NO");
            	sql.append(jobWhereSql.toString());
            	sql.append(" )");
            }
    
            logger.info(" recoverData():"+sql.toString());
            DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
            
            
            sql.delete(0, sql.length()); //清空sql
            //Update TB_TRANS
            sql.append("update ").append(TBNAME_TRANS).append(" set ");
            sql.append("    SETTLE_PROC_DATE=null ");
            sql.append("  , SETTLE_SUCC_DATE=null");
            sql.append("  , SETTLE_RCODE='").append(Constants.RCODE_0000_OK).append("'");
            sql.append("  , TXN_NOTE=" + getUpdateTxnNoteSql());
            sql.append(" where ").append(makeParMonDayCond("", false));
            sql.append("   and ").append(makeCutDateCond("", false));
            sql.append("   and SETTLE_PROC_DATE='").append(getBatchDate()).append("'");
            if ( getRecoverLevel().equals(Constants.RECOVER_LEVEL_ERR) )
            {
                sql.append(" and SETTLE_RCODE <> '").append(Constants.RCODE_0000_OK).append("'");
            }
            if(jobWhereSql.length() > 0){
            	sql.append(" AND EXISTS (SELECT 1 FROM TB_MEMBER");
            	sql.append(" WHERE TB_TRANS.ACQ_MEM_ID = TB_MEMBER.MEM_ID");
            	sql.append(jobWhereSql.toString());
            	sql.append(" )");
            }
    
            logger.info(" recoverData():"+sql.toString());
            DBService.getDBService().sqlAction(sql.toString(), connSelf, false);
            
            connSelf.commit();
        }
        catch (SQLException e)
        {
            connSelf.rollback();
            logger.warn(" sql:"+sql);
            throw new Exception("recoverData():update TB_TRANS_DTL. "+e);
        }
        finally
        {
            ReleaseResource.releaseDB(connSelf);
        }
    }

    public int getCommitCount()
    {
        return commitCount;
    }

    public void setCommitCount(int commitCount)
    {
        this.commitCount = commitCount;
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

    public String getIsOnlyAll()
    {
        return isOnlyAll;
    }

    public void setIsOnlyAll(String isOnlyAll)
    {
        this.isOnlyAll = isOnlyAll;
    }

    public String getSettlePartition()
    {
        return settlePartition;
    }

    public void setSettlePartition(String settlePartition)
    {
        this.settlePartition = settlePartition;
    }

    
    protected class SettleInfo
    {
        //tb_settle_config
        String settleCode;
        String acqMemId;
        String issMemId;
        String BonusId;
        String settleFrom;
        double campaignQty;
        double settleRate;
        int carryDigit;
        String carryType;
        String validSdate;
        String validEdate;
        String fundType;
        String bankId;
        String settleTarget;
        String expPayDate;
        
        //tb_settle_def
        String creditUnit;
        String debitUnit;
        String accountCode;
        
        boolean isAdj = false;

        public void setSettleInfo(HashMap settleConfigHm, 
                                  HashMap settleDefHm) throws Exception
        {
            try
            {
                setSettleCode((String) settleConfigHm.get("SETTLE_CODE"));
                setAcqMemId((String) settleConfigHm.get("ACQ_MEM_ID"));
                setIssMemId((String) settleConfigHm.get("ISS_MEM_ID"));
                setBonusId(((String) settleConfigHm.get("BONUS_ID")));
                setSettleFrom((String) settleConfigHm.get("SETTLE_FROM"));
                setSettleRate(((Number) settleConfigHm.get("SETTLE_RATE")).doubleValue());
                setCarryDigit(((Number) settleConfigHm.get("CARRY_DIGIT")).intValue());
                setCarryType((String) settleConfigHm.get("CARRY_TYPE"));
                setValidSdate((String) settleConfigHm.get("VALID_SDATE"));
                setValidEdate((String) settleConfigHm.get("VALID_EDATE"));
                setFundType((String) settleConfigHm.get("FUND_TYPE"));
                setBankId((String) settleConfigHm.get("BANK_ID"));
                setSettleTarget((String) settleConfigHm.get("SETTLE_TARGET"));
                
                if ( getFundType() == null  || getFundType().equalsIgnoreCase("null") || getFundType().equalsIgnoreCase("000") )
                	setExpPayDate(null);
                else
                	setExpPayDate(Layer2Util.getCycleDate(conn, getBatchDate(), getFundType()));
                
                
                setCreditUnit((String) settleDefHm.get("CREDIT_UNIT"));
                setDebitUnit((String) settleDefHm.get("DEBIT_UNIT"));
                setAccountCode((String) settleDefHm.get("ACCOUNT_CODE"));
                
                //settleInfo.setSettleTxnVtr(settleTxnVtr);
            }
            catch (Exception e)
            {
                throw new Exception("setSettleInfo()"+e);
            }
        }
        
        public String getAcqMemId()
        {
            return acqMemId;
        }
        public void setAcqMemId(String acqMemId)
        {
            this.acqMemId = acqMemId;
        }
        public String getBonusId()
        {
            return BonusId;
        }
        public void setBonusId(String bonusId)
        {
            BonusId = bonusId;
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
        public String getIssMemId()
        {
            return issMemId;
        }
        public void setIssMemId(String issMemId)
        {
            this.issMemId = issMemId;
        }
        public String getSettleCode()
        {
            return settleCode;
        }
        public void setSettleCode(String settleCode)
        {
            this.settleCode = settleCode;
        }
        public String getSettleFrom()
        {
            return settleFrom;
        }
        public void setSettleFrom(String settleFrom)
        {
            this.settleFrom = settleFrom;
        }
        public double getSettleRate()
        {
            return settleRate;
        }
        public void setSettleRate(double settleRate)
        {
            this.settleRate = settleRate;
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
		public String getFundType() {
			return fundType;
		}
		public void setFundType(String fundType) {
			this.fundType = fundType;
		}
        public String getBankId() {
			return bankId;
		}
		public void setBankId(String bankId) {
			this.bankId = bankId;
		}
		public String getExpPayDate() {
			return expPayDate;
		}
		public void setExpPayDate(String expPayDate) {
			this.expPayDate = expPayDate;
		}
		public String getSettleTarget() {
			return settleTarget;
		}
		public void setSettleTarget(String settleTarget) {
			this.settleTarget = settleTarget;
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

		public double getCampaignQty()
        {
            return campaignQty;
        }

        public void setCampaignQty(double campaignQty)
        {
            this.campaignQty = campaignQty;
        }

        public boolean isAdj()
        {
            return isAdj;
        }

        public void setAdj(boolean isAdj)
        {
            this.isAdj = isAdj;
        }

        
    }


    public String getDay()
    {
        return day;
    }


    public void setDay(String day)
    {
        this.day = day;
    }


    public String getMon()
    {
        return mon;
    }


    public void setMon(String mon)
    {
        this.mon = mon;
    }


    public DateRange getProcPeriod()
    {
        return procPeriod;
    }


    public void setProcPeriod(DateRange procPeriod)
    {
        if ( StringUtil.isEmpty(procPeriod.getEndDate())||procPeriod.getEndDate()==null) 
        { //如果是ProcCycle是 Dxx, endDate會是null -> 令 endDate = startDate
            procPeriod.setEndDate(procPeriod.getStartDate());
        }
        this.procPeriod = procPeriod;
    }


    public String getAllMember()
    {
        return allMember;
    }


    public void setAllMember(String allMember)
    {
        this.allMember = allMember;
    }


    public boolean getIsCheckSponsorSum()
    {
        return isCheckSponsorSum;
    }


    public void setIsCheckSponsorSum(boolean isCheckSponsorSum)
    {
        this.isCheckSponsorSum = isCheckSponsorSum;
    }


    public String getUpdateTxnNoteSql()
    {
        return updateTxnNoteSql;
    }


    public void setUpdateTxnNoteSql(String updateTxnNoteSql)
    {
        this.updateTxnNoteSql = updateTxnNoteSql;
    }
    
}
