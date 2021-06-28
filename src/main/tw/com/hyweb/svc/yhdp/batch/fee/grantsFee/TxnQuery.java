package tw.com.hyweb.svc.yhdp.batch.fee.grantsFee;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.DateRange;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.util.string.StringUtil;

public class TxnQuery {

	private final static Logger logger = Logger.getLogger(TxnQuery.class);
	
	protected ProcGrantsFee procGrantsFee;
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
    
    protected TxnQuery(ProcGrantsFee procGrantsFee)
	{
		this.procGrantsFee = procGrantsFee;
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
            if (! StringUtil.isEmpty(field)) list.add(field);
        }
        
        unit = "A";
        if (crUnit.equals(unit) || dbUnit.equals(unit)) 
        {//含有A的才處理
            field = Layer2Util.getUnit2TransField(unit);
            if (! StringUtil.isEmpty(field)) list.add(field);
        }
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
     * 
     * @param crUnit
     * @param dbUnit
     * @return groupByFields
     */
    protected void setGroupByFields( )
    {
    	List<String> list = getTxnGroupByFieldsList();
    	
        StringBuffer sb = new StringBuffer();
        
        for (String field : list) {
            if (false==StringUtil.isEmpty(field)) {
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
        /*StringBuffer sb = new StringBuffer();
        
    	setAcqMemId((String)txnInfo.get("ACQ_MEM_ID"));
        sb.append("ACQ_MEM_ID = '").append(getAcqMemId()).append("' ");

        txnGroupByCondition = sb.toString();*/
		List<String> list = getTxnGroupByFieldsList();

        StringBuffer sb = new StringBuffer();
        for (String field : list) {
            if (false==StringUtil.isEmpty(field)) {
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
    protected void setTxnCondition( GrantsFeeConfig grantsFeeConfig )
    {
        String startDate = getProcPeriod().getStartDate();
        String endDate = getProcPeriod().getEndDate();

        StringBuffer sb = new StringBuffer();
        //所有已清算且未計算手續費的交易
        //imp_date between { startDate } and { endDate } and status=’0’ and fee_proc_date is null
        sb.append("IMP_DATE BETWEEN '").append(startDate).append("' AND '").append(endDate).append("' ");
        sb.append("AND STATUS='0' AND FEE_PROC_DATE IS NULL "); 
        sb.append("AND ACQ_MEM_ID = '").append(grantsFeeConfig.getAcqMemId()).append("' ");
        logger.debug("set Txn Condition():"+sb.toString());
        
        txnCondition = sb.toString();
        
        //設定要groupby的欄位
        //setTxnGroupByFieldsList(feeConf.getCreditUnit(), feeConf.getDebitUnit());
        List<String> list = new Vector<String>();
        list.add("ACQ_MEM_ID");
        txnGroupByFieldsList = list;
        
        //從txnGroupByFieldsList 組成 "field1, field2, ....." 字串
        setGroupByFields();
    }
    
    /**
     * 取得交易的選取條件
     * @return txnCondition
     */
    protected String getTxnCondition()
    {
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
        if (procPeriod.getEndDate()==null || StringUtil.isEmpty(procPeriod.getEndDate()))
        { //如果是ProcCycle是 Dxx, endDate會是null ==> 使endDate=startDate
            procPeriod.setEndDate(procPeriod.getStartDate());
        }
		this.procPeriod = procPeriod;
	}

    protected ProcGrantsFee getProcFee()
    {
		return procGrantsFee;
	}

    protected void setProcFee(ProcGrantsFee procGrantsFee) 
    {
		this.procGrantsFee = procGrantsFee;
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


}
