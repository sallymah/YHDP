package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.DAOBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.mgr.TbInctlMgr;
import tw.com.hyweb.util.string.StringUtil;

public class CheckPersoFeebackFactory extends DAOBatchJobFactory {
	
	private static final Logger LOGGER = Logger.getLogger(CheckPersoFeebackFactory.class);

	public YhdpPersoFeebackDataAction action;
	public YhdpPersoFeebackDataCheck check;
	public YhdpPersoFeebackDataGenerator dataGenerator;

	public CheckPersoFeebackFactory(YhdpPersoFeebackDataAction action, YhdpPersoFeebackDataCheck check, YhdpPersoFeebackDataGenerator dataGenerator) 
	{
		this.action = action;
		this.check = check;
		this.dataGenerator = dataGenerator;
	}	

	@Override
	protected List<?> getDAOInfos(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {
		
		if ( action.getPersoFileDataList().size() > 0 ){
			String fileNames = "";
			for (int i = 0; i < action.getPersoFileDataList().size(); i++){
				fileNames = fileNames + StringUtil.toSqlValueWithSQuote(action.getPersoFileDataList().get(i).getFileName());
				if ( i+1 < action.getPersoFileDataList().size() ){
					fileNames = fileNames + ", ";
				}
			}	
			Vector<TbInctlInfo> result = new Vector<TbInctlInfo>();
		    new TbInctlMgr(connection).queryMultiple("FILE_NAME IN ("+fileNames+") AND WORK_FLAG='1'", result);
	
		    return result;
		}
		return null;
	}

	@Override
	protected BatchJob getBatchJob(Object info) throws Exception {
		LOGGER.info(info);
		return new CheckPersoFeebackJob((TbInctlInfo) info, action, check, dataGenerator);
	}
}
