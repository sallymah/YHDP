/*
 * 
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/12/1
 */
package tw.com.hyweb.svc.yhdp.batch.persoV2;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.service.db.info.TbIffFeedbackInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbIffFeedbackMgr;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Chris
 *
 */
public class YhdpPersoCardJobDecorator  implements BatchJob
{
	private static final Logger LOGGER = Logger.getLogger(YhdpPersoCardJobDecorator.class);
    private final ProcessPersoJob job;  

    /**
     * @param job
     */
    public YhdpPersoCardJobDecorator(ProcessPersoJob job)
    {
        this.job = job;
    }

    /**
     * @param connection
     * @param batchDate
     * @throws Exception
     * @see tw.com.hyweb.svc.cp.batch.perso.ProcessPersoJob#action(java.sql.Connection,
     *      java.lang.String)
     */
    public void action(Connection connection, String batchDate) throws Exception
    {
    	job.initialCardNoPreFix();
    	
        TbPersoInfo persoInfo = job.getPersoInfo();

        LOGGER.info("perso type: [" + persoInfo.getPersoType() + "]");
        generateCardNo(connection, persoInfo.getIssuerCode(), job.getCardNoTagOn(), persoInfo.getPersoQty().intValue());
        
        
        /*if(persoInfo.getPersoType().equals(job.PERSO_TYPE_STANDARD_CARD)) {
        	//20170620 改為判斷HgCardGroupId是否有填
        	if(StringUtil.isEmpty(persoInfo.getHgCardGroupId()))  {
        		LOGGER.info("perso type 1: [" + persoInfo.getPersoType() + "]");
            	generateCardNo(connection, job.CARD_IDENFY_STANDARD_CARD, job.getCardNoTagOn(), persoInfo.getPersoQty().intValue());
        	}
        	else {
        		LOGGER.info("perso type 1: [" + persoInfo.getPersoType() + "]");
            	generateCardNo(connection, job.CARD_IDENFY_HAPPYGO_CARD, job.getCardNoTagOn(), persoInfo.getPersoQty().intValue());
        	}
        }
        else if(persoInfo.getPersoType().equals(job.PERSO_TYPE_PHONE_CARD)) {
        	//20170620 改為判斷HgCardGroupId是否有填
        	if(StringUtil.isEmpty(persoInfo.getHgCardGroupId())) {
        		LOGGER.info("perso type 4: [" + persoInfo.getPersoType() + "]");
            	generateCardNo(connection, job.CARD_IDENFY_STANDARD_CARD, job.getCardNoTagOn(), persoInfo.getPersoQty().intValue());
        	}
        	else {
        		LOGGER.info("perso type 4: [" + persoInfo.getPersoType() + "]");
            	generateCardNo(connection, job.CARD_IDENFY_HAPPYGO_CARD, job.getCardNoTagOn(), persoInfo.getPersoQty().intValue());
        	}
        }
        else if(persoInfo.getPersoType().equals(job.PERSO_TYPE_BANK_CARD)){
        	LOGGER.info("perso type 3: [" + persoInfo.getPersoType() + "]");
        	if(persoInfo.getPtaUnitNo()==null || persoInfo.getPtaUnitNo().equals("")) {
        		throw new Exception("Pta Unit No is null or empty !");
        	} else {
        		generateCardNo(connection, job.CARD_IDENFY_BANK_CARD+persoInfo.getPtaUnitNo(), job.getCardNoTagOn(), persoInfo.getPersoQty().intValue());
        	}
        }      */ 	
        
        job.action(connection, batchDate);
        
        new TbPersoMgr(connection).update(persoInfo);
    }

	/**
     * 
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess(java.sql.Connection,
     *      java.lang.String)
     */
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
    	if(job.getPersoInfo().getPersoType().equals(job.PERSO_TYPE_BANK_CARD) || 
    			job.getPersoInfo().getPersoType().equals(job.PERSO_TYPE_PHONE_CARD)) {
    		addRecord2IFFFeedback(connection, job.getPersoInfo());
    	}
    	job.remarkSuccess(connection, batchDate);
    	job.getPersoInfo().setStatus("2");

        new TbPersoMgr(connection).update(job.getPersoInfo());
    }

    private void addRecord2IFFFeedback(Connection conn, TbPersoInfo tbPersoInfo) throws Exception 
    {
    	TbIffFeedbackInfo iffFeedbackInfo = new TbIffFeedbackInfo(); 
    	iffFeedbackInfo.setMemId(tbPersoInfo.getMemId());
    	iffFeedbackInfo.setPersoBatchNo(tbPersoInfo.getPersoBatchNo());
    	iffFeedbackInfo.setPersoQty(tbPersoInfo.getPersoQty());
    	iffFeedbackInfo.setIffQty(0);
    	iffFeedbackInfo.setCardTypeId(tbPersoInfo.getCardTypeId());
    	iffFeedbackInfo.setCardCatId(tbPersoInfo.getCardCatId());
    	iffFeedbackInfo.setCardProduct(tbPersoInfo.getCardProduct());
    	iffFeedbackInfo.setStatus("0");

        new TbIffFeedbackMgr(conn).insert(iffFeedbackInfo);		
	}

	/**
     * @param connection
     * @param batchDate
     * @param batchJobException
     * @throws Exception
     * @see tw.com.hyweb.svc.cp.batch.perso.ProcessPersoJob#remarkFailure(java.sql.Connection,
     *      java.lang.String,
     *      tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException)
     */
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {
        job.remarkFailure(connection, batchDate, batchJobException);
    }
    
    private void generateCardNo(Connection connection, String identifier, String cardNoTagOn, int qty) throws SQLException
    {
    	String cardNoPrefix = identifier+cardNoTagOn;
    	
    	LOGGER.info("cardNoPrefix: [" + cardNoPrefix + "]");
    	
    	String startCardNo = cardNoPrefix + "FFFFFFFFFFF";
    	LOGGER.info("startCardNo: [" + startCardNo + "]");
    	job.getPersoInfo().setStartCardNo(startCardNo);
    	
    	//Kevin qty-1
    	String endCardNo = startCardNo;
    	LOGGER.info("endCardNo: [" + endCardNo + "]");
    	job.getPersoInfo().setEndCardNo(endCardNo);
    	
    }
        
    public String get7Seq( int seq){
    	
    	String seqStr = ("0000000"+seq);
    	
    	seqStr = seqStr.substring(seqStr.length()-7, seqStr.length());
    	
    	return seqStr;
    	
    }

    /**
     * @return
     * @see tw.com.hyweb.svc.cp.batch.perso.ProcessPersoJob#toString()
     */
    public String toString()
    {
        return job.toString();
    }
}
