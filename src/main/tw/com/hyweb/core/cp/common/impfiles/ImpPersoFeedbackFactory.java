/*
 * $Id: SimulateAppointReloadFactory.java 2594 2010-01-18 11:20:20Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Corporation.
 * All Rights Reserved.
 */

package tw.com.hyweb.core.cp.common.impfiles;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.common.framework.generic.DAOBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.mgr.TbInctlMgr;
import tw.com.hyweb.svc.yhdp.batch.expfiles.ExpAffComBlock.ExpAffComBlock;

/**
 * @author  Kevin
 * @version $Revision: 2594 $
 */
public class ImpPersoFeedbackFactory extends DAOBatchJobFactory{

    private static final Logger LOGGER = Logger.getLogger(ImpPersoFeedbackFactory.class);
    private String othersCondition = "";	
    
    private boolean threadFlag = false;
    private String absolutePath;

	@Override
	protected List<?> getDAOInfos(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception 
	{
		Vector<TbInctlInfo> result = new Vector<TbInctlInfo>();
        new TbInctlMgr(connection).queryMultiple("FILE_NAME IN ('IFF','IFF2','IFF_BANK') AND WORK_FLAG='2' AND " + othersCondition, result);
        return result;
	}

	@Override
	protected BatchJob getBatchJob(Object info) throws Exception 
	{
		return new ImpPersoFeedbackJob(absolutePath,(TbInctlInfo)info, threadFlag);
	}

	public String getOthersCondition() {
		return othersCondition;
	}

	public void setOthersCondition(String othersCondition) {
		this.othersCondition = othersCondition;
	}
	
	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public boolean isThreadFlag() {
		return threadFlag;
	}

	public void setThreadFlag(boolean threadFlag) {
		this.threadFlag = threadFlag;
	}
	
}
