/*
 *
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/9
 */
package tw.com.hyweb.svc.yhdp.batch.perso;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils;
import tw.com.hyweb.core.yhdp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbHgCardMapInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbHgCardMapMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;
import tw.com.hyweb.svc.cp.batch.perso.CardDataGenerator;
import tw.com.hyweb.svc.cp.batch.perso.CardNumberGenerator;
import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.core.cp.common.impfiles.YhdpBlackListSettingDataGenerator;

/**
 * @author Clare
 *
 */
public class ProcessPersoJob extends GenericBatchJob
{
    private final TbPersoInfo persoInfo;

    private final CardNumberGenerator cardNumberGenerator;
    private final CardDataGenerator cardDataGenerator;
    private final YhdpPersoCardFile persoCardFile;
    private final YhdpCpDeliveryDataGenerator cpDeliveryDataGen;
    
    public final String PERSO_TYPE_STANDARD_CARD = "1";
    
   /* public final String CARD_IDENFY_STANDARD_CARD = "988";
    public final String CARD_IDENFY_HAPPY_GO_CARD = "986";*/
    
    private int commitPerRecord;
    public String cardNoTagOn;

	private static final Logger LOGGER = Logger.getLogger(ProcessPersoJob.class);
    
    private List<TbHgCardMapInfo> hgCardMapInfos = null;
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public ProcessPersoJob(TbPersoInfo persoInfo, CardNumberGenerator cardNumberGenerator, CardDataGenerator cardDataGenerator, YhdpPersoCardFile persoCardFile, YhdpCpDeliveryDataGenerator cpDeliveryDataGen, int commitPerRecord)
    {
        this.persoInfo = persoInfo;
        this.cardNumberGenerator = cardNumberGenerator;
        this.cardDataGenerator = cardDataGenerator;
        this.persoCardFile = persoCardFile;
        this.cpDeliveryDataGen = cpDeliveryDataGen;
        this.commitPerRecord = commitPerRecord;
    }
    
    public void initialCardNoPreFix() {
    	cardNoTagOn = persoInfo.getCardFactoryId() + persoInfo.getCardProduct().substring(2, 5);
    }

    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
    	List<String> cardNumbers = new ArrayList<String>();
    	List<TbHgCardMapInfo> hgCardMapInfos = new ArrayList<TbHgCardMapInfo>();
    	String seqNo = SequenceGenerator.getBatchNoByType(connection, "B");

        try
        {
        	List<TbHgCardMapInfo> allHgCardMapInfos = null;
        	//20170620 ????????????HgCardGroupId????????????
        	if(!StringUtil.isEmpty(persoInfo.getHgCardGroupId())) {
        		allHgCardMapInfos = getHgCardInfo(connection, persoInfo);
        		
        		if(allHgCardMapInfos == null || allHgCardMapInfos.size() == 0) {
        			throw new Exception("TB_HG_CARD_MAP no data found !");
        		}
        		
        		for(int idx=0; idx<persoInfo.getPersoQty().intValue(); idx++)
        		{
        			hgCardMapInfos.add(allHgCardMapInfos.get(idx));
        		}

        		if(hgCardMapInfos.size() < persoInfo.getPersoQty().intValue()) {
        			throw new Exception("fetch table(TB_HG_CARD_MAP) data < perso qty !");
        		}
        		       		
        	}
        	
        	if (persoInfo.getIsHgCard().equals("Y")){
    			for(int i=0; i < hgCardMapInfos.size(); i++)
        		{
        			if(persoInfo.getPersoType().equals(PERSO_TYPE_STANDARD_CARD)) {
        				cardNumbers.add(hgCardMapInfos.get(i).getCardNo());
        	        }
        		}
    		}
    		else{
    			cardNumbers = getCardNumbers();
    		}
        	
        	/*if(cardNumbers.size() != (Integer) persoSetting.getPersoQty()) {
        		throw new Exception("perso card number not match perso setting qty !");
        	}*/
        	
            String regionId = getRegionId(connection, persoInfo.getMemId());

            TbCardMgr cardManager = new TbCardMgr(connection);
            
            persoCardFile.init(connection, batchDate, persoInfo);
            persoCardFile.makePersoHeader(batchDate, persoInfo, seqNo);
            
            LOGGER.debug("commit for per: " + commitPerRecord + " Record");
            for (int i = 0; i < persoInfo.getPersoQty().intValue(); ++i)
            {           
            	
            	TbCardInfo tbCardInfo = new TbCardInfo();
            	tbCardInfo = cardDataGenerator.makeCardData(connection, batchDate, regionId, cardNumbers.get(i), persoInfo);
            	//TB_CP_DELIVERY
            	cpDeliveryDataGen.makeCpDeliveryData(connection, cardNumbers.get(i), batchDate, persoInfo);
            	
            	//20170620 ????????????HgCardGroupId????????????
            	if(StringUtil.isEmpty(persoInfo.getHgCardGroupId())) {
            		persoCardFile.makePerso(connection, cardNumbers.get(i), persoInfo, null);
            	} else{
            		persoCardFile.makePerso(connection, cardNumbers.get(i), persoInfo, hgCardMapInfos.get(i));
            		tbCardInfo.setHgCardNo(hgCardMapInfos.get(i).getBarcode1());
            		updateHgCardMap(connection, cardNumbers.get(i), hgCardMapInfos.get(i));
            	}
            	
            	LOGGER.debug("is test: " + persoInfo.getIsTest());
            	if(persoInfo.getIsTest().equals("1")) {
            		LOGGER.debug("ImpBlaceListSetting");
            		new YhdpBlackListSettingDataGenerator().makeBlackListSetgingData(connection, jobDate, jobTime, cardNumbers.get(i), persoInfo);
            	}

                cardManager.insert(tbCardInfo);

                if (i % commitPerRecord == 0)
                {
                    connection.commit();
                }
            }
            connection.commit();
            persoInfo.setStartCardNo(cardNumbers.get(0));
            persoInfo.setEndCardNo(cardNumbers.get(persoInfo.getPersoQty().intValue()-1));
            persoCardFile.makePersoDone();
            persoCardFile.encryptPerso2File(persoInfo, connection);
            persoCardFile.updateOutControl(connection);
        }
        catch (BatchJobException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new BatchJobException(e, "2910");
        }
    }

    private void updateHgCardMap(Connection conn, String cardNo, TbHgCardMapInfo tbHgCardMapInfo) throws Exception {
    	
    	if (cardNo.equalsIgnoreCase(tbHgCardMapInfo.getCardNo())){
	    	String sql = "UPDATE TB_HG_CARD_MAP SET STATUS=? WHERE BARCODE1=?";
	    	Object[] parameterValues = {"1", tbHgCardMapInfo.getBarcode1()};
	    	PreparedStatementUtils.executeUpdate(conn, sql, parameterValues);
    	}
    	else{
    		String sql = "UPDATE TB_HG_CARD_MAP SET CARD_NO = ?, STATUS=? WHERE BARCODE1=?";
	    	Object[] parameterValues = {cardNo, "1", tbHgCardMapInfo.getBarcode1()};
	    	PreparedStatementUtils.executeUpdate(conn, sql, parameterValues);
    	}
		
	}

	private List<TbHgCardMapInfo> getHgCardInfo(Connection conn, TbPersoInfo persoInfo) throws Exception 
	{
		int persoQty = persoInfo.getPersoQty().intValue();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT ROWID, CARD_NO, BARCODE1, BARCODE2, IMP_FILE_NAME, IMP_FILE_DATE, STATUS, HG_CARD_GROUP_ID FROM (");
		sb.append(" SELECT ROWID, CARD_NO, BARCODE1, BARCODE2, IMP_FILE_NAME, IMP_FILE_DATE, STATUS, HG_CARD_GROUP_ID");
		sb.append(" FROM TB_HG_CARD_MAP WHERE Status='0' AND HG_CARD_GROUP_ID='").append(persoInfo.getHgCardGroupId()).append("'");
		sb.append(" ORDER BY IMP_FILE_DATE, SUBSTR(CARD_NO,1,15))");
		sb.append(" WHERE ROWNUM<=").append(persoQty);
		
		String sqlCmd = sb.toString();
		
		Vector<HashMap<String, Object>> result = BatchUtil.getInfoListHashMap(sqlCmd, conn);
		
		List<TbHgCardMapInfo> hgCardMapInfos = new ArrayList<TbHgCardMapInfo>();
		
		for(int idx=0; idx<result.size(); idx++)
		{			
			TbHgCardMapInfo info = new TbHgCardMapInfo();
			
			info.setCardNo((String)(result.get(idx).get("CARD_NO")));
			info.setBarcode1((String)(result.get(idx).get("BARCODE1")));
			info.setBarcode2((String)(result.get(idx).get("BARCODE2")));
			info.setImpFileName((String)(result.get(idx).get("IMP_FILE_NAME")));
			info.setImpFileDate((String)(result.get(idx).get("IMP_FILE_DATE")));
			info.setStatus((String)(result.get(idx).get("STATUS")));
			info.setHgCardGroupId((String)(result.get(idx).get("HG_CARD_GROUP_ID")));
			
			hgCardMapInfos.add(info);
		}					
		return hgCardMapInfos;
	}

	/**
     * 
     *
     * @param connection
     * @param memberId
     * @return
     * @throws SQLException
     * @throws BatchJobException
     */
    private String getRegionId(Connection connection, String memberId) throws SQLException, BatchJobException
    {
    	TbMemberInfo member = null;
    	Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();
    	new TbMemberMgr(connection).queryMultiple("MEM_ID = '" + memberId + "'", result);
    	
    	for(int i=0; i<result.size(); i++) 
    	{
    		member = result.get(0);
    	}
    	
        if (member == null)
        {
            throw new BatchJobException("get TB_MEMBER.REGION_ID fails for MEMBER_ID:" + memberId, "2919");
        }

        return member.getRegionId();
    }

    /**
     * 
     *
     * @return
     * @throws Exception
     */
    private List<String> getCardNumbers() throws Exception
    {
    	List<String> cardNumbers = new ArrayList<String>();

        cardNumbers.add(persoInfo.getStartCardNo());

        for (int i = 1; i < persoInfo.getPersoQty().intValue(); ++i)
        {
            cardNumbers.add(cardNumberGenerator.inqueryNextCardNo(cardNumbers.get(i - 1), cardNoTagOn));
        }

        if (!cardNumbers.get(persoInfo.getPersoQty().intValue() - 1).equals(persoInfo.getEndCardNo()))
        {
            throw new BatchJobException(new Exception("end card number is not match! (persoSetting:" + persoInfo.getEndCardNo() + ", cardNumberGenerator:" + cardNumbers.get(persoInfo.getPersoQty().intValue() - 1) + ")"), "2911");
        }

        return cardNumbers;
    }

    /**
     *
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
    	persoInfo.setProcDate(batchDate);
    	persoInfo.setProcTime(jobTime);
    	persoInfo.setStatus("2");

        new TbPersoMgr(connection).update(persoInfo);
    }

    /**
     * perso setting
     *
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkFailure(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {
    	persoInfo.setStartCardNo("");
    	persoInfo.setEndCardNo("");
    	persoInfo.setProcDate(batchDate);
    	persoInfo.setProcTime(jobTime);
    	persoInfo.setPersoRcode(batchJobException.getRcode());

        new TbPersoMgr(connection).update(persoInfo);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "process perso job(mem_id:" + persoInfo.getMemId() + ", perso_batch_no:" + persoInfo.getPersoBatchNo() + ")";
    }

    /**
     * @return the persoSetting
     */
    public TbPersoInfo getPersoInfo()
    {
        return persoInfo;
    }
    
    public String getCardNoTagOn() {
		return cardNoTagOn;
	}

	public void setCardNoTagOn(String cardNoTagOn) {
		this.cardNoTagOn = cardNoTagOn;
	}
}
