package tw.com.hyweb.yhdp.ws.impfiles;

import java.sql.Connection;

import javax.jws.WebService;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.framework.generic.BatchJobHandler;
import tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedbackFactory;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

@WebService(endpointInterface = "tw.com.hyweb.yhdp.ws.impfiles.ImpFilesService",serviceName="ImpFilesService")

public class ImpFilesServiceImpl implements ImpFilesService 
{
    private static Logger LOG = Logger.getLogger(ImpFilesServiceImpl.class);
    private final DataSource dataSource;
    private final ImpPersoFeedbackFactory factory;
    
	ImpFilesServiceImpl(ImpPersoFeedbackFactory factory, DataSource dataSource)
	{
		this.factory = factory;
        this.dataSource = dataSource;
	}
	
	/* (non-Javadoc)
	 * @see tw.com.hyweb.cp.ws.SimpleService#handleBatchJob()
	 */
	public String impPersoFeedbackByFileName(String fileName) throws Exception 
	{
		factory.setOthersCondition("FULL_FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileName));     
		return handle();
	}

	private String handle()
	{
        Connection connection = null;
        String batchDate = DateUtils.getSystemDate();
        String rcode = "";
        //Boolean result = false;
        
        try
        {
            connection = dataSource.getConnection();
         
            BatchJobHandler handle = new BatchJobHandler(factory);
            BatchHandleResult batchHandleResult = handle.handle(connection, batchDate, null);
            rcode = batchHandleResult.getRcode();
            
            /*if ("0000".equals(rcode))
            	result = true;
            else
            	LOG.error("error:" + rcode);*/

        }
        catch (Exception e)
        {
            LOG.error("exception:", e);
            rcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
        }
        finally
        {
            ReleaseResource.releaseDB(connection);
        }
		return rcode;
	}
}
