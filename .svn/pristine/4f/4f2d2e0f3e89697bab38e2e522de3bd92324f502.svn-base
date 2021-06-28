package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpPersoFeedback;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchDataSource;
import tw.com.hyweb.core.cp.batch.framework.generic.PreparedStatementManagedConnection;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedbackFactory;
import tw.com.hyweb.core.yhdp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.info.TbPersoFeedbackInfo;
import tw.com.hyweb.service.db.info.TbPersoFeedbackUptInfo;
import tw.com.hyweb.service.db.mgr.TbPersoFeedbackMgr;
import tw.com.hyweb.util.DbUtil;

public class ImpPersoFeedback extends ImpPerosFeedbackHandle
{
	private static Logger LOG = Logger.getLogger(ImpPerosFeedbackHandle.class);
	
	private final String date = DateUtils.getSystemDate();
	private final String time = DateUtils.getSystemTime();
	
    private DataSource dataSource;
	private String batchNo;
    
	public ImpPersoFeedback(ImpPersoFeedbackFactory factory, String absolutePath, DataSource dataSource) 
	{    
		super(factory, absolutePath);
		this.dataSource = dataSource;
	}

	/**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            String springConfigPath = FilenameUtils.normalize(FilenameUtils.separatorsToSystem(args[0]));
            ApplicationContext context = new FileSystemXmlApplicationContext(springConfigPath);

            AbstractBatchBasic processor = ((AbstractBatchBasic) context.getBean(args[1]));
            processor.run(null);
        }
        catch (Throwable e)
        {
        	LOG.warn("", e);
        }
    }

	@Override
	public void process(String[] argv) throws Exception 
	{
		Connection connection = null;
		
	 	if (dataSource == null)
        {
            dataSource = BatchDataSource.getDataSource();
        }
	 	batchNo = System.getProperty("batchNo");
	 	if(batchNo==null) batchNo="";

        try {
        	connection = new PreparedStatementManagedConnection(dataSource.getConnection());
        	
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT MEM_ID, PERSO_BATCH_NO, IFF_FILE_NAME, PERSO_QTY, IFF_QTY, CARD_TYPE_ID, CARD_CAT_ID, CARD_PRODUCT, UPT_DATE, UPT_TIME");
            sb.append(" FROM TB_PERSO_FEEDBACK_UPT");
            sb.append(" WHERE APRV_STATUS IN ('0')");
            sb.append(" AND EXISTS (SELECT 1 FROM TB_PERSO WHERE PERSO_TYPE IN ('3','4')");

            if(!batchNo.equals("")) {
            	sb.append(" AND TB_PERSO.PERSO_BATCH_NO='").append(batchNo).append("'");
            }    
            sb.append(" AND TB_PERSO_FEEDBACK_UPT.PERSO_BATCH_NO=TB_PERSO.PERSO_BATCH_NO)");
            sb.append(" ORDER BY PERSO_BATCH_NO DESC");
            String sql = sb.toString();
            
            Vector vtr = BatchUtil.getInfoList(sql);
            
            if(vtr.size()==0) LOG.info("TB_PERSO_FEEDBACK_UPT no data found !");
            
            for (int i = 0; i < vtr.size(); i++) 
            {
                Vector row = (Vector) vtr.get(i);
                
                TbPersoFeedbackUptInfo persoFeedbackUptInfo = new TbPersoFeedbackUptInfo();
            	persoFeedbackUptInfo.setMemId((String) row.get(0));
            	persoFeedbackUptInfo.setPersoBatchNo((String) row.get(1));
            	persoFeedbackUptInfo.setIffFileName((String) row.get(2));
            	persoFeedbackUptInfo.setPersoQty((Number) row.get(3));
            	persoFeedbackUptInfo.setIffQty((Number) row.get(4));
            	persoFeedbackUptInfo.setCardTypeId((String) row.get(5));
            	persoFeedbackUptInfo.setCardCatId((String) row.get(6));
            	persoFeedbackUptInfo.setCardProduct((String) row.get(7));
            	persoFeedbackUptInfo.setUptDate((String) row.get(8));
            	persoFeedbackUptInfo.setUptTime((String) row.get(9));
            	
            	ImpPersoFeedbackByFileName(connection, persoFeedbackUptInfo.getIffFileName());
            	
            	insetPersoFeedback(connection, persoFeedbackUptInfo);
            	updatePersoFeedbackUpt(connection, persoFeedbackUptInfo);
            	//已經不需要分銀行卡或一般卡
//            	updateCardInfo(connection, persoFeedbackUptInfo);
            	
            	connection.commit();
            }
		} catch (Exception e) {
			throw e;
		} 
	}

	private void updatePersoFeedbackUpt(Connection connection, TbPersoFeedbackUptInfo persoFeedbackUptInfo) throws SQLException 
	{
		StringBuffer sb = new StringBuffer();
        sb.append("UPDATE TB_PERSO_FEEDBACK_UPT SET APRV_STATUS=?, APRV_USERID=?, APRV_DATE=?, APRV_TIME=?");
        sb.append(" WHERE PERSO_BATCH_NO='").append(persoFeedbackUptInfo.getPersoBatchNo()).append("'");
        sb.append(" AND IFF_FILE_NAME='").append(persoFeedbackUptInfo.getIffFileName()).append("'");
        sb.append(" AND UPT_DATE='").append(persoFeedbackUptInfo.getUptDate()).append("'");
        sb.append(" AND UPT_TIME='").append(persoFeedbackUptInfo.getUptTime()).append("'");
		
        String sqlCmd = sb.toString();
        
		Vector<Object> params = new Vector<Object>();
		params.add((Integer)1);
		params.add((String)"BATCH");
		params.add(date);
		params.add(time);

		DbUtil.sqlAction(sqlCmd, params, connection);
	}

	/*private void updateCardInfo(Connection connection, TbPersoFeedbackUptInfo persoFeedbackUptInfo) throws SQLException 
	{
		StringBuffer sb = new StringBuffer();
        sb.append("UPDATE TB_CARD SET LIFE_CYCLE=STATUS, PREVIOUS_LIFE_CYCLE=?, PREVIOUS_STATUS=?, ");
        sb.append("APRV_USERID=? ");
        sb.append(" WHERE EXISTS (SELECT * FROM TB_PERSO_FEEDBACK ");
        sb.append(" WHERE PERSO_BATCH_NO='").append(persoFeedbackUptInfo.getPersoBatchNo()).append("'");
        sb.append(" AND IFF_FILE_NAME='").append(persoFeedbackUptInfo.getIffFileName()).append("'");
        sb.append(" AND UPT_DATE='").append(date).append("'");
        sb.append(" AND TB_CARD.PERSO_BATCH_NO=TB_PERSO_FEEDBACK.PERSO_BATCH_NO");
        sb.append(" AND TB_CARD.IFF_FILE_NAME=TB_PERSO_FEEDBACK.IFF_FILE_NAME)");
		
        String sqlCmd = sb.toString();
        String sqlUptCmd = sb.toString().replaceAll("TB_CARD", "TB_CARD_UPT");
		
		Vector<Object> params = new Vector<Object>();
		params.add((Integer)1);
		params.add((Integer)1);
		params.add("BATCH_AUTO_IFF");

		DbUtil.sqlAction(sqlCmd, params, connection);
		DbUtil.sqlAction(sqlUptCmd, params, connection);
	}*/

	private void insetPersoFeedback(Connection connection, TbPersoFeedbackUptInfo persoFeedbackUptInfo) throws SQLException 
	{
		TbPersoFeedbackInfo persoFeedbackInfo = new TbPersoFeedbackInfo();
		persoFeedbackInfo.setMemId(persoFeedbackUptInfo.getMemId());
		persoFeedbackInfo.setPersoBatchNo(persoFeedbackUptInfo.getPersoBatchNo());
		persoFeedbackInfo.setIffFileName(persoFeedbackUptInfo.getIffFileName());
		persoFeedbackInfo.setPersoQty(persoFeedbackUptInfo.getPersoQty());
		persoFeedbackInfo.setIffQty(persoFeedbackUptInfo.getIffQty());
		persoFeedbackInfo.setCardTypeId(persoFeedbackUptInfo.getCardTypeId());
		persoFeedbackInfo.setCardCatId(persoFeedbackUptInfo.getCardCatId());
		persoFeedbackInfo.setCardProduct(persoFeedbackUptInfo.getCardProduct());
		persoFeedbackInfo.setFeedbackRcode("0000");
		persoFeedbackInfo.setIffFileProcDate(date);
		persoFeedbackInfo.setIffFileProcTime(time);
		persoFeedbackInfo.setUptUserid("BATCH");
		persoFeedbackInfo.setUptDate(date);
		persoFeedbackInfo.setUptTime(time);
		persoFeedbackInfo.setAprvUserid("BATCH");
		persoFeedbackInfo.setAprvDate(date);
		persoFeedbackInfo.setAprvTime(time);
		
		new TbPersoFeedbackMgr(connection).insert(persoFeedbackInfo);
	}
}
