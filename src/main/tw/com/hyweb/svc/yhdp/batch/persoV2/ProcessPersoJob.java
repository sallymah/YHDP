/*
 *
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/9
 */
package tw.com.hyweb.svc.yhdp.batch.persoV2;

import java.sql.Connection;
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
import tw.com.hyweb.service.db.info.TbHgCardMapInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbPersoMgr;
import tw.com.hyweb.svc.cp.batch.perso.CardDataGenerator;
import tw.com.hyweb.svc.cp.batch.perso.CardNumberGenerator;
import tw.com.hyweb.util.string.StringUtil;

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
    
    public final static String OWN_CARD_FACTORY = "00"; 	//自行製卡
    
    public final static String PERSO_TYPE_STANDARD_CARD = "1";
    //public final static String PERSO_TYPE_ENTERPRISE_CARD = "2";
    public final static String PERSO_TYPE_BANK_CARD = "3";
    public final static String PERSO_TYPE_PHONE_CARD = "4";
    
    //20170620 改填ISSUER_CODE
/*    public final String CARD_IDENFY_STANDARD_CARD = "900";
    public final String CARD_IDENFY_HAPPYGO_CARD = "985";
    //public final String CARD_IDENFY_ENTERPRISE_CARD = "977";
    //public final String CARD_IDENFY_BANK_CARD = "899";
    public final String CARD_IDENFY_BANK_CARD = "8";*/
    
    private int commitPerRecord;
    public String cardNoTagOn;
    private List<TbHgCardMapInfo> hgCardMapInfos = null;

	private static final Logger LOGGER = Logger.getLogger(ProcessPersoJob.class);
    
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
    	//cardNoTagOn = persoInfo.getCardFactoryId() + persoInfo.getCardProduct().substring(2, 5);
    	cardNoTagOn = persoInfo.getChipType()+persoInfo.getCardFactoryId().substring(1);
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
    	String seqNo = SequenceGenerator.getBatchNoByType(connection, "B");
    	List<TbHgCardMapInfo> hgCardMapInfos = new ArrayList<TbHgCardMapInfo>();
        try
        {   
        	//20170620 改為判斷HgCardGroupId是否有填
        	if (!StringUtil.isEmpty(persoInfo.getHgCardGroupId()))
        	{
        		List<TbHgCardMapInfo> allHgCardMapInfos = getHgCardInfo(connection, persoInfo);
        		
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
        	
            persoCardFile.init(connection, batchDate, persoInfo);
            persoCardFile.makePersoHeader(batchDate, persoInfo, seqNo);
            
            LOGGER.debug("commit for per: " + commitPerRecord + " Record");
            for (int i = 0; i < persoInfo.getPersoQty().intValue(); ++i)
            {     
            	//20170620 改為判斷HgCardGroupId是否有填
            	if(!StringUtil.isEmpty(persoInfo.getHgCardGroupId())) {
            		persoCardFile.makePerso(connection, persoInfo.getStartCardNo(), persoInfo, hgCardMapInfos.get(i));
            		updateHgCardStatus(connection, hgCardMapInfos.get(i).getBarcode1());
            	}else {
            		persoCardFile.makePerso(connection, persoInfo.getStartCardNo(), persoInfo, null);
            	}
            	
                if (i % commitPerRecord == 0)
                {
                    connection.commit();
                }
            }
            connection.commit();
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
    
    private void updateHgCardStatus(Connection conn, String barcode1) throws Exception 
    { 	
    	String sql = "UPDATE TB_HG_CARD_MAP SET STATUS=? WHERE BARCODE1=?";
    	Object[] parameterValues = {"2", barcode1};
    	
		PreparedStatementUtils.executeUpdate(conn, sql, parameterValues);
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
     * @see tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
    	persoInfo.setStartCardNo("");
    	persoInfo.setEndCardNo("");
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
