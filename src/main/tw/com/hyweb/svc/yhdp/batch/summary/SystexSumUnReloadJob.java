/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/9/30
 */
package tw.com.hyweb.svc.yhdp.batch.summary;

import java.sql.Connection;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.util.DbUtil;

/**
 * @author Anny
 * 
 */
public class SystexSumUnReloadJob extends GenericBatchJob
{
    private static final Logger LOGGER = Logger.getLogger(SystexSumUnReloadJob.class);

    private final TbMemberInfo member;
    private final Integer overMonth;
    
    /**
     * @param issuer
     */
    public SystexSumUnReloadJob(TbMemberInfo member, Integer overMonth)
    {
	    this.member = member;
	    this.overMonth=overMonth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java
     * .sql.Connection, java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
    	Vector<String> params = new Vector<String>();
    	
    	String qtyCmd ="select sum(qty) as qty from ("+
    		           "select nvl(sum(BAL_BONUS_QTY),0) as qty from TB_CARD_BAL where (CARD_NO,EXPIRY_DATE,BONUS_ID) in (select c.CARD_NO,c.EXPIRY_DATE, p.ECASH_BONUS_ID FROM TB_CARD c,TB_CARD_PRODUCT p where c.CARD_PRODUCT = p.CARD_PRODUCT and c.MEM_ID=? and c.STATUS='3' and c.LAST_RELOAD_DATE <> '00000000' and c.LAST_RELOAD_DATE < '"+DateUtils.getPreviousMonthDate(batchDate, overMonth)+"') and BONUS_EDATE >= '"+batchDate+"'"+
    	               " union "+
    		           "select nvl(sum(BAL_BONUS_QTY),0) from TB_CARD_BAL bal,TB_CARD card, TB_BONUS bonus where bal.CARD_NO=card.CARD_NO and bal.EXPIRY_DATE = card.EXPIRY_DATE and bal.BONUS_ID= bonus.BONUS_ID and card.STATUS='3' and card.MEM_ID=? and card.LAST_RELOAD_DATE <> '00000000' and card.LAST_RELOAD_DATE < '"+DateUtils.getPreviousMonthDate(batchDate,overMonth)+"' and bonus.BONUS_NATURE = 'C' and bonus.COUPON_PURPOSE = 'S' and bal.BONUS_EDATE >= '"+batchDate+"'"+
    	               ")";
    	
    	params.add(member.getMemId());
    	params.add(member.getMemId());
    	LOGGER.info(qtyCmd);
    	
    	Number qty= DbUtil.getNumber(qtyCmd, params, connection);
    
    	params.clear();
    	String updateCmd = "update TB_MEMBER set UNRELOAD_BAL= ? where MEM_ID=?";
    	params.add(qty.toString());
    	params.add(member.getMemId());
    	
    	LOGGER.info(updateCmd);
    	DbUtil.sqlAction(updateCmd, params, connection);
    }


}
