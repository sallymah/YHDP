/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/9/30
 */
package tw.com.hyweb.svc.yhdp.batch.summary;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;

/**
 * @author Rita
 * 
 */
public class CardInvalidEscrowJobFactory extends DAOBatchJobFactory
{

	private Integer overMonth=12;
	private String pCode1="";
	private String pCode2="";
	public Integer getOverMonth() {
		return overMonth;
	}

	public void setOverMonth(Integer overMonth) {
		this.overMonth = overMonth;
	}

	public String getpCode1() {
		return pCode1;
	}

	public void setpCode1(String pCode1) {
		this.pCode1 = pCode1;
	}

	public String getpCode2() {
		return pCode2;
	}

	public void setpCode2(String pCode2) {
		this.pCode2 = pCode2;
	}

	public CardInvalidEscrowJobFactory()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getDAOInfos
     * (java.sql.Connection, java.lang.String)
     */
    @Override
    protected List<?> getDAOInfos(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();
        new TbMemberMgr(connection).queryMultiple("PB_ESCROW_FLAG = '1' ",result);

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getBatchJob
     * (java.lang.Object)
     */
    @Override
    protected BatchJob getBatchJob(Object info) throws Exception
    {
        return new CardInvalidEscrowJob((TbMemberInfo)info, overMonth,pCode1,pCode2);
    }
}
