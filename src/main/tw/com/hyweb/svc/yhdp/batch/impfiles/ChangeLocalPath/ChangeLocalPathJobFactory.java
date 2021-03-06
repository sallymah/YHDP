package tw.com.hyweb.svc.yhdp.batch.impfiles.ChangeLocalPath;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.util.string.StringUtil;

public class ChangeLocalPathJobFactory extends CursorBatchJobFactory {
	
	private static final Logger logger = Logger.getLogger(ChangeLocalPathJobFactory.class);
	
	protected List filenameBeans = new ArrayList();
	
	public void init(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
		for(int i = 0; i < filenameBeans.size(); i++) {
			((FilenameBean)filenameBeans.get(i)).toInit(connection);
		}
		
		super.init(connection, batchDate, tbBatchResultInfo);
    }

	@Override
	protected String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();
		
		sql.append(" SELECT FULL_FILE_NAME, LOCAL_PATH, FTP_IP, REMOTE_PATH, SYS_DATE, SYS_TIME FROM TB_FTP_LOG");
		sql.append(" WHERE IN_OUT = 'I'");
		sql.append(" AND STATUS = '1'");
		sql.append(" AND SYS_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
		
		return sql.toString();
	}

	@Override
	protected BatchJob makeBatchJob(Map<String, String> resultMap) throws Exception {
		// TODO Auto-generated method stub
		return new ChangeLocalPathJob(resultMap, filenameBeans);
	}

	public List getFilenameBeans() {
		return filenameBeans;
	}

	public void setFilenameBeans(List filenameBeans) {
		this.filenameBeans = filenameBeans;
	}
}
