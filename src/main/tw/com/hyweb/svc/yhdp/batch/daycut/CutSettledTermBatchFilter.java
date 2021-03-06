/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/17
 */
package tw.com.hyweb.svc.yhdp.batch.daycut;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Clare
 * 
 */
public class CutSettledTermBatchFilter implements CutTermBatchFilter
{
	private static final Logger LOGGER = Logger.getLogger(CutSettledTermBatchFilter.class);
	
    private final String[] txnSrcs;

    public CutSettledTermBatchFilter(String[] txnSrcs)
    {
        this.txnSrcs = txnSrcs;
    }

    /**
     * 傳回結帳成功且未過檔的term batch之SQL條件
     * 2015027 剔除測試單位的資料不過檔
     * 
     * @see tw.com.hyweb.svc.yhdp.batch.daycut.CutTermBatchFilter#getCutCondition()
     */
    public String getCutCondition(TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	StringBuffer sql = new StringBuffer();
    	
    	sql.append(getTxnSrcAndSettleTimeCondition(txnSrcs));
    	sql.append(" AND CUT_DATE IS NULL");
    	sql.append(" AND STATUS='1'");
    	sql.append(" AND CUT_RCODE=").append(StringUtil.toSqlValueWithSQuote(Constants.RCODE_0000_OK));
    	sql.append(" AND EXISTS");
    	sql.append(" (SELECT 1 FROM TB_MERCH, TB_MEMBER");
    	sql.append(" WHERE TEST_FLAG <> '1'");
    	
    	if (null != tbBatchResultInfo){
    		if (Layer1Constants.MEM_LAST.equalsIgnoreCase(tbBatchResultInfo.getMemId())){
    			sql.append(" AND JOB_ID IS NULL");
    			sql.append(" AND JOB_TIME IS NULL");
    		}
    		else{
		    	if(!StringUtil.isEmpty(tbBatchResultInfo.getJobId()) 
				&& !tbBatchResultInfo.getJobId().equalsIgnoreCase(Layer1Constants.JOB_ID_DEFAULT)){
		    		sql.append(" AND JOB_ID=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobId()));
		    		if(!StringUtil.isEmpty(tbBatchResultInfo.getJobTime()) 
					&& !tbBatchResultInfo.getJobTime().equalsIgnoreCase(Layer1Constants.JOB_TIME_DEFAULT)){
			    		sql.append(" AND JOB_TIME=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getJobTime()));
			    	}
		    	}
		    	if(!StringUtil.isEmpty(tbBatchResultInfo.getMemId())){
		    		sql.append(" AND TB_MEMBER.MEM_ID=").append(StringUtil.toSqlValueWithSQuote(tbBatchResultInfo.getMemId()));
		    	}
    		}
    	}
    	else{
    		LOGGER.warn("tbBatchResultInfo is null.");
    	}
    	
    	sql.append(" AND TB_MERCH.MEM_ID = TB_MEMBER.MEM_ID");
    	sql.append(" AND TB_TERM_BATCH.MERCH_ID = TB_MERCH.MERCH_ID)");
        
        return sql.toString();
    }

    /**
     * 根據所有的txn src以及相對應的term settle time組成SQL條件
     * 
     * @param txnSrcs
     * @return
     */
    private String getTxnSrcAndSettleTimeCondition(String[] txnSrcs)
    {
        StringBuilder condition = new StringBuilder();

        condition.append('(');

        for (int i = 0; i < txnSrcs.length; ++i)
        {
        	if(txnSrcs[i].equals("E")) {
        		condition.append("(TERM_SETTLE_DATE||TERM_SETTLE_TIME<='" + Layer2Util.getExpectCutTime(txnSrcs[i]) + "' and TXN_SRC='" + txnSrcs[i] + "' and IMP_FILE_NAME is not null)");
        	}
        	else {
        		condition.append("(TERM_SETTLE_DATE||TERM_SETTLE_TIME<='" + Layer2Util.getExpectCutTime(txnSrcs[i]) + "' and TXN_SRC='" + txnSrcs[i] + "')");
        	}

            if (i != txnSrcs.length - 1)
            {
                condition.append(" or ");
            }
        }

        condition.append(')');

        return condition.toString();
    }
}
