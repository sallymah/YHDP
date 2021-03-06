/*
 *
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/9
 */
package tw.com.hyweb.svc.yhdp.batch.persoV2;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;
import tw.com.hyweb.svc.cp.batch.perso.CardDataGenerator;
import tw.com.hyweb.svc.cp.batch.perso.CardNumberGenerator;



/**
 * @author Chris
 *
 */
public class ProcessPersoJobFactory extends DAOBatchJobFactory
{
    private final CardNumberGenerator cardNumberGenerator;
    private final CardDataGenerator cardDataGenerator;
    private final YhdpPersoCardFile persoCardFile;
    private final YhdpCpDeliveryDataGenerator cpDeliveryDataGen;
    private int commitPerRecord = 1000;

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
    public ProcessPersoJobFactory(CardNumberGenerator cardNumberGenerator, CardDataGenerator cardDataGenerator, YhdpPersoCardFile persoCardFile, YhdpCpDeliveryDataGenerator cpDeliveryDataGen)
    {
        this.cardNumberGenerator = cardNumberGenerator;
        this.cardDataGenerator = cardDataGenerator;
        this.persoCardFile = persoCardFile;
        this.cpDeliveryDataGen = cpDeliveryDataGen;
    }

    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory#getBatchJob(java.lang.Object)
     */
    @Override
    protected BatchJob getBatchJob(Object info) throws Exception
    {
        return new ProcessPersoJob((TbPersoInfo) info, cardNumberGenerator, cardDataGenerator, persoCardFile, cpDeliveryDataGen, commitPerRecord);
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
        new TbPersoMgr(connection).queryMultiple(" STATUS='0' AND CHIP_VERSION='2' ", result);

        return result;
    }
}
