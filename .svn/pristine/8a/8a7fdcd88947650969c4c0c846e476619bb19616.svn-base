/*
 *
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/9
 */
package tw.com.hyweb.svc.yhdp.batch.persoOTA;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;

/**
 * @author Chris
 *
 */
public class ProcessPersoJobFactory extends DAOBatchJobFactory
{
    private final YhdpPersoCardFile persoCardFile;
    private int commitPerRecord = 1000;
    private String chipVerion = "3";

    public int getCommitPerRecord() {
		return commitPerRecord;
	}

	public void setCommitPerRecord(int commitPerRecord) {
		this.commitPerRecord = commitPerRecord;
	}

	/**
     * @param cardNumberGenerator
     * @param cardDataGenerator
     * @param persoDetailGenerator
     */
    public ProcessPersoJobFactory(YhdpPersoCardFile persoCardFile)
    {
        this.persoCardFile = persoCardFile;
    }

    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getBatchJob(java.lang.Object)
     */
    @Override
    protected BatchJob getBatchJob(Object info) throws Exception
    {
        return new ProcessPersoJob((TbPersoInfo) info, persoCardFile, commitPerRecord);
    }

    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getDAOInfos(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    protected List<?> getDAOInfos(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        Vector<TbPersoInfo> result = new Vector<TbPersoInfo>();
        new TbPersoMgr(connection).queryMultiple(" STATUS='0' AND CHIP_VERSION='"+ getChipVerion() +"' ", result);

        return result;
    }

	public String getChipVerion() {
		return chipVerion;
	}

	public void setChipVerion(String chipVerion) {
		this.chipVerion = chipVerion;
	}
}
