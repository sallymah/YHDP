package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpPersoFeedback;

import java.sql.Connection;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.framework.generic.BatchJobHandler;
import tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedbackFactory;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public abstract class ImpPerosFeedbackHandle extends AbstractBatchBasic
{
    private static Logger LOG = Logger.getLogger(ImpPerosFeedbackHandle.class);
    private final ImpPersoFeedbackFactory factory;
    private final String absolutePath;
    
    public ImpPerosFeedbackHandle(ImpPersoFeedbackFactory factory, String absolutePath)
	{
		this.factory = factory;
		this.absolutePath = absolutePath;
	}
    
    /* (non-Javadoc)
	 * @see tw.com.hyweb.cp.ws.SimpleService#handleBatchJob()
	 */
	public String ImpPersoFeedbackByFileName(Connection connection, String fileName) throws Exception 
	{
		factory.setOthersCondition("FULL_FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileName));
		factory.setAbsolutePath(absolutePath);
		return handle(connection);
	}

	private String handle(Connection connection) throws Exception
	{
        String batchDate = DateUtils.getSystemDate();
        String rcode = "";
        
        try
        {         
            BatchJobHandler handle = new BatchJobHandler(factory);
            BatchHandleResult batchHandleResult = handle.handle(connection, batchDate, null);
            rcode = batchHandleResult.getRcode();

        }
        catch (Exception e)
        {
            throw e;
        }
        /*finally
        {
            ReleaseResource.releaseDB(connection);
        }*/
		return rcode;
	}
}
