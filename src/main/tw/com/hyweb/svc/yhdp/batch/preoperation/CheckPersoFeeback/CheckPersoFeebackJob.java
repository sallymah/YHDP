package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.service.db.SqlResult;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.mgr.TbInctlMgr;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor.CheckLineJobRunner;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor.CheckLineJobRunnerFactory;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor.ThreadPoolJobExecutor;
import tw.com.hyweb.util.DbUtil;

public class CheckPersoFeebackJob extends GenericBatchJob 
{
	private static final Logger LOGGER = Logger.getLogger(CheckPersoFeebackJob.class);
	private static final String THREAS_SPRING_PATH = "config" + File.separator + "batch" + File.separator +
			"preoperation" + File.separator + "CheckPersoFeeback" + File.separator + "thread.xml";
	
	private final TbInctlInfo inctlInfo; 
	private final YhdpPersoFeebackDataAction action; 
	private final YhdpPersoFeebackDataCheck check;
	private final YhdpPersoFeebackDataGenerator dataGen;
	
	private ThreadPoolJobExecutor threadPoolJobExecutor = null;
	private CheckLineJobRunnerFactory checkLinejobRunnerFactory = new CheckLineJobRunnerFactory();
	
	public CheckPersoFeebackJob(TbInctlInfo info, YhdpPersoFeebackDataAction action, YhdpPersoFeebackDataCheck check, YhdpPersoFeebackDataGenerator dataGen) 
	{
		this.inctlInfo = info;
		this.action = action;
		this.check = check;
		this.dataGen = dataGen;
		
		//??????errorsql?????????
		this.check.setErrorSqlList(new ArrayList<String>());
	}

	@Override
	public void action(Connection conn, String batchDate) throws Exception
    {
		action.init(conn, inctlInfo);
		
		check.basicChecker(conn, inctlInfo, action.getProcFile());

		BufferedInputStream bis = null;
		try {
			
			ApplicationContext apContext = new FileSystemXmlApplicationContext(THREAS_SPRING_PATH);
	    	ThreadPoolJobExecutor instance = (ThreadPoolJobExecutor) apContext.getBean("threadPoolJobExecutor");
	    	setThreadPoolJobExecutor(instance);
	    	
	    	getThreadPoolJobExecutor().startThreadPoolJob();
			
			bis = new BufferedInputStream(new FileInputStream(new File(action.getProcFile())));
			bis.skip(1*64);
			
			ArrayList<String> sqlList = new ArrayList<>();
			
			for(int idx=0; idx<check.getRowDataCount(); idx++)
			{
				/*if (check.getErrorSqlList().size() > 0 && idx == 0){
					
					if(!getThreadPoolJobExecutor().getExecutor().isShutdown()){
        		    	getThreadPoolJobExecutor().getExecutor().shutdown();
        				LOGGER.info("threadPoolJobExecutor shutdown!!");
        	    	}
					
					int[] result = batchInsert(check.getErrorSqlList(), conn);
			        if(!checkExecuteBatchResult(check.getErrorSqlList(), result))
			        {
			            throw new Exception("update batch result false");
			        }
					break;
				}*/
				
				String dataLine = check.getOneRecord(idx, bis);
				LOGGER.debug("dataLine: " + dataLine);
				
				if(!dataLine.equals("")) {
					
		    		if(getThreadPoolJobExecutor() != null && getThreadPoolJobExecutor().getExecutor() != null)
			        {
		    			if (sqlList != null){
		    				
			    			ThreadPoolExecutor threadPoolExecutor = getThreadPoolJobExecutor().getExecutor();
			                
			                CheckLineJobRunner jobRunnerExecute = checkLinejobRunnerFactory.create(check, dataGen, sqlList, idx, dataLine, inctlInfo);
			                
			                threadPoolExecutor.execute(jobRunnerExecute);
			                
			                idx++;
		    			}
			        }
		    		else{
		    			throw new Exception("getThreadPoolJobExecutor(): " + getThreadPoolJobExecutor() 
		    					+ "  getThreadPoolJobExecutor().getExecutor():" + getThreadPoolJobExecutor().getExecutor());
		    		}
				}
				
//	    		if(!dataLine.equals("")) {
//					/*HashMap<String, String> hm = IFFUtils.cacheRowData("IFF", dataLine);
//					cardNo = hm.get("field02");
//					expiryDate = hm.get("field02");*/
//					
//					dataBean = IFFUtils.organizeRowData(dataLine);
//					//index:0 = ?????????
//					check.checkDataLine(conn, idx, dataLine, inctlInfo, dataBean);	
//				}
//				
//				if(check.getPersoInfo().getPersoType().equals("3") || check.getPersoInfo().getPersoType().equals("4")) 
//				{
//					dataGen.addIffFeedbackDtlData(conn, inctlInfo, dataBean);
//				}
//				
//				if (check.getPersoInfo().getPersoType().equals("4")) {
//					updateTelcoCardDtl(conn);
//				}
			}
			
			while (getThreadPoolJobExecutor().getExecutor().getActiveCount() != 0) {
	    		Thread.sleep(2000);
	    		LOGGER.debug("ActiveCount:"+getThreadPoolJobExecutor().getExecutor().getActiveCount()
	    				+" CompletedCount:"+getThreadPoolJobExecutor().getExecutor().getCompletedTaskCount());
			}
	    	
	    	if(!getThreadPoolJobExecutor().getExecutor().isShutdown()){
		    	getThreadPoolJobExecutor().getExecutor().shutdown();
				LOGGER.info("threadPoolJobExecutor shutdown!!");
	    	}
			
	    	if (check.getErrorSqlList().size() > 0){
				
	    		//????????????????????????
	    		sqlList.clear();

	    		int[] result = batchInsert(check.getErrorSqlList(), conn);
		        if(!checkExecuteBatchResult(check.getErrorSqlList(), result))
		        {
		            throw new Exception("update error batch result false");
		        }
		        throw new Exception(inctlInfo.getFullFileName() + " date is wrong.");
			}
	    	else{
	    		if (sqlList.size() > 0){
			    	int[] result = batchInsert(sqlList, conn);
			        if(!checkExecuteBatchResult(sqlList, result))
			        {
			            throw new Exception("update batch result false");
			        }
		    	}
		    	
				if(check.getRowDataCount() >0){
					dataGen.addPersoFeedbackUptData(conn, inctlInfo, check);
				}
	    	}
		}
		finally {
			bis.close();
		}
    }
	
	public int[] batchInsert(List<String> commandList, Connection conn) throws SQLException
    {
        int[] ret = null;
        if ((commandList != null) && (commandList.size() > 0) && (conn != null))
        {
          Statement statement = null;
          try
          {
            statement = conn.createStatement();
            long before = System.currentTimeMillis();
            for (int i = 0; i < commandList.size(); i++) {
              if ((commandList.get(i) != null) && (((String)commandList.get(i)).length() > 0))
              {
            	  LOGGER.debug("SQL Command : " + (String)commandList.get(i));
            	  statement.addBatch((String)commandList.get(i));
              }
            }
            ret = statement.executeBatch();
            long diffTime = System.currentTimeMillis() - before;
            LOGGER.info("SQL batch insert (count:" + commandList.size() + ") time: " + diffTime + " ms");
          }
          catch (SQLException sqle)
          {
            throw sqle;
          }
          finally
          {
              statement.close();
          }
        }
        return ret;
    }
	
	public boolean checkExecuteBatchResult(List<String> commandList, int[] result)
    {
        if(null != result)
        {
            for(int idx = 0; idx < result.length; idx++)
            {
                if(result[idx] != 1)
                {
                	LOGGER.error(commandList.get(idx));
                	LOGGER.error("update count != 1, result[" + idx + "] ,"+result[idx]);
                    return false;
                }
            }
        }
        return true;
    }
	
	@Override
	public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
		if(check.getPersoInfo().getPersoType().equals("3") || 
				check.getPersoInfo().getPersoType().equals("4")) 
		{
			updateIffFeedbackImpCnt(connection);
			
		}
		inctlInfo.setWorkFlag(Layer1Constants.WORKFLAG_PROCESSING);
		new TbInctlMgr(connection).update(inctlInfo);
    }
	
	/*private void updateTelcoCardDtl(Connection conn) throws Exception 
	{
		sb.delete(0, sb.length());
		sb.append("update TB_TELCO_CARD_DTL set");
		sb.append(" IFF_FILE_NAME='").append(inctlInfo.getFullFileName()).append("'");
		sb.append(", IFF_FILE_PROC_DATE='").append(dataGen.sysDate).append("'");
		sb.append(", IFF_FILE_PROC_TIME='").append(dataGen.sysTime).append("'");
		sb.append(", CARD_NO='").append(dataBean.getCardNo()).append("'");
		
		String utExpiryDate = dataBean.getExpiryDate();
		long hexUtExpiryDate = Long.parseLong(utExpiryDate, 16);
		String expiryDate = new SimpleDateFormat("yyyyMMdd").format(new Date(Long.valueOf(hexUtExpiryDate) * 1000L));
		sb.append(", EXPIRY_DATE='").append(expiryDate).append("'");
		
		sb.append(" where IMP_TELCO_FILE_NAME='").append(check.getPersoInfo().getImpTelcoFileName()).append("'");
		sb.append(" and MIFARE_UL_UID='").append(dataBean.getMifareId()).append("'");
		sb.append(" and IFF_FILE_NAME is null");
		
		String sqlCmd = sb.toString();
		
		SqlResult sr = DbUtil.sqlAction(sqlCmd, conn);
		LOGGER.info("sql: " + sqlCmd.toString() + ", count: " + sr.getRecordCount());
	}*/
	
	private void updateIffFeedbackImpCnt(Connection conn) throws Exception 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.delete(0, sb.length());
		sb.append("update TB_IFF_FEEDBACK set");
		sb.append(" IFF_QTY=IFF_QTY+").append(check.getRowDataCount());
		sb.append(" where PERSO_BATCH_NO='").append(check.getPersoInfo().getPersoBatchNo()).append("'");
		
		String sqlCmd = sb.toString();
		
		SqlResult sr = DbUtil.sqlAction(sqlCmd, conn);
		LOGGER.info("sql: " + sqlCmd.toString() + ", count: " + sr.getRecordCount());
	}

	@Override
	public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {
    	inctlInfo.setWorkFlag(Layer1Constants.WORKFLAG_PROCESSFAIL);
		new TbInctlMgr(connection).update(inctlInfo);
    }

	public ThreadPoolJobExecutor getThreadPoolJobExecutor() {
		return threadPoolJobExecutor;
	}

	public void setThreadPoolJobExecutor(ThreadPoolJobExecutor threadPoolJobExecutor) {
		this.threadPoolJobExecutor = threadPoolJobExecutor;
	}
	
}
