/*
 * 
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/12/1
 */
package tw.com.hyweb.svc.yhdp.batch.perso;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;

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

        /*
        if (isUltraLite(connection, persoSetting.getCardProduct()))
        {
            int count = new TbUlCardImportDtlMgr(connection).getCount("PERSO_BATCH_NO='" + persoSetting.getPersoBatchNo() + "' and STATUS='0'");

            if (count != persoSetting.getPersoQty().intValue())
            {
                throw new BatchJobException("size of TB_UL_CARD_IMPORT_DTL and TB_PERSO_SETTING.PERSO_QTY is inconsistent!", "2918");
            }
        }*/              
        
       /* if(persoInfo.getPersoType().equals(job.PERSO_TYPE_STANDARD_CARD)) {
        	LOGGER.info("perso type 1: [" + persoInfo.getPersoType() + "]");
        	generateCardNo(connection, job.CARD_IDENFY_STANDARD_CARD, job.getCardNoTagOn(), persoInfo.getPersoQty().intValue());
        }*/
        
        LOGGER.info("perso type: [" + persoInfo.getPersoType() + "]");
    	generateCardNo(connection, persoInfo.getIssuerCode(), job.getCardNoTagOn(), persoInfo.getPersoQty().intValue());
        
        job.action(connection, batchDate);
        
        new TbPersoMgr(connection).update(persoInfo);
        
        

        /**
        if (isUltraLite(connection, persoSetting.getCardProduct()))
        {
            List<Map<String, String>> cards = executeQuery(connection, "select CARD_NO,EXPIRY_DATE from TB_CARD where MEM_ID=? and PERSO_BATCH_NO=? order by CARD_NO", persoSetting.getMemId(), persoSetting.getPersoBatchNo());
            List<Map<String, String>> ultraLites = executeQuery(connection, "select SHOW_CARD_NO,MIFARE_UL_UID from TB_UL_CARD_IMPORT_DTL where PERSO_BATCH_NO=? and STATUS=? order by SHOW_CARD_NO", persoSetting.getPersoBatchNo(), "0");

            for (int i = 0; i < cards.size(); ++i)
            {
                Map<String, String> card = cards.get(i);
                Map<String, String> ultraLite = ultraLites.get(i);

                executeUpdate(connection, "update TB_CARD set SHOW_CARD_NO=?,CARD_NO=?,MIFARE_UL_UID=? where CARD_NO=? and EXPIRY_DATE=?", card.get("CARD_NO"), ultraLite.get("SHOW_CARD_NO"), ultraLite.get("MIFARE_UL_UID"), card.get("CARD_NO"), card.get("EXPIRY_DATE"));
            }
        }**/
    }

	/**
     * 
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess(java.sql.Connection,
     *      java.lang.String)
     */
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
    	job.remarkSuccess(connection, batchDate);
    	job.getPersoInfo().setStatus("2");

        new TbPersoMgr(connection).update(job.getPersoInfo());
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
    	
    	int cardNoSeq = 0;
    	 	
    	cardNoSeq = this.getMaxCardNo(connection, cardNoPrefix)+1;
    	LOGGER.info("cardNoSeq: [" + cardNoSeq + "]");    	
    	
    	String startCardNo = this.getFullCardNoWithCheck(cardNoPrefix+get7Seq(cardNoSeq));
    	LOGGER.info("startCardNo: [" + startCardNo + "]");
    	job.getPersoInfo().setStartCardNo(startCardNo);
    	
    	//Kevin qty-1
    	String endCardNo = this.getFullCardNoWithCheck(cardNoPrefix+get7Seq(cardNoSeq+(qty-1)));
    	LOGGER.info("endCardNo: [" + endCardNo + "]");
    	job.getPersoInfo().setEndCardNo(endCardNo);
    	
    }
        
    public String get7Seq( int seq){
    	
    	String seqStr = ("0000000"+seq);
    	
    	seqStr = seqStr.substring(seqStr.length()-7, seqStr.length());
    	
    	return seqStr;
    	
    }
    
    private int getMaxCardNo(Connection connection, String cardNoPrefix) throws SQLException{
    	Object[] paramObjects = {cardNoPrefix+"%"};
    	//List<Map<String, String>> cards = executeQuery(connection, "select substr(max(card_no),9,7) as LAST_NO from tb_card where mem_id=? and substr(card_no,1,8)=?", paramObjects);
    	List<Map<String, String>> cards = executeQuery(connection, "select substr(max(card_no),9,7) as LAST_NO from tb_card where card_no like ?", paramObjects);
    	
    	if(cards.get(0).get("LAST_NO")==null){
    		return 0;
    	}else
    		return Integer.parseInt(cards.get(0).get("LAST_NO"));
    }
    
    
    private String getFullCardNoWithCheck(String cardNoPrefixSeq){
    	
    	return cardNoPrefixSeq+this.getCardNoCheckCode(cardNoPrefixSeq);
    }
    
    private String getCardNoCheckCode(String cardNo18)
    {
        int checkSum = 0;
        int result = 0;
        for(int i= 0;i<cardNo18.length();i++)
        {
        int checkUnit =0;
         
         if(cardNo18.length()% 2 != 0)
             cardNo18="0" + cardNo18;
          
            if (i % 2 == 0)
            {
                checkUnit = Integer.parseInt(cardNo18.substring(i, (i+1))) * 1;
            }
            else
            {
                checkUnit = Integer.parseInt(cardNo18.substring(i, (i+1))) * 2;
            }
 
            if (checkUnit > 9)
            {
                checkSum += (checkUnit / 10) + (checkUnit % 10);
            }
            else
            {
                checkSum += checkUnit;
            }
        }
        if (checkSum % 10 != 0)
        {
            result = 10 - (checkSum % 10);
        }
 
        return String.valueOf(result);
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
