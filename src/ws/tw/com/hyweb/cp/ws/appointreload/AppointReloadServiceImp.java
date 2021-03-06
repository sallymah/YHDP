package tw.com.hyweb.cp.ws.appointreload;

import java.sql.Connection;

import javax.jws.WebService;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchHandleResult;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobHandler;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.common.appointreload.SimulateAppointReloadFactory;
import tw.com.hyweb.cp.ws.appointreload.AppointReloadService;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

@WebService(endpointInterface = "tw.com.hyweb.cp.ws.appointreload.AppointReloadService",serviceName="AppointReloadService")

public class AppointReloadServiceImp implements AppointReloadService 
{
    private static Logger LOG = Logger.getLogger(AppointReloadServiceImp.class);
    private final DataSource dataSource;
    private final SimulateAppointReloadFactory factory;
    
	AppointReloadServiceImp(SimulateAppointReloadFactory factory, DataSource dataSource)
	{
		this.factory = factory;
        this.dataSource = dataSource;
	}
	
	/* (non-Javadoc)
	 * @see tw.com.hyweb.cp.ws.SimpleService#handleBatchJob()
	 */
	public boolean simulateAppointReloadByFileName(String fileName) throws Exception {
		
		factory.setOthersCondition(" INFILE = " + StringUtil.toSqlValueWithSQuote(fileName));
        
		return handle();
	}

	/* (non-Javadoc)
	 * @see tw.com.hyweb.cp.ws.appointreload.AppointReloadService#simulateAppointReload(java.lang.String, java.lang.String)
	 */
	public boolean simulateAppointReloadByArSerno(String balanceId, String arSerno)
			throws Exception {
		
		factory.setOthersCondition(" balance_id = " + StringUtil.toSqlValueWithSQuote(balanceId) + " and ar_serno = " + StringUtil.toSqlValueWithSQuote(arSerno));
        
		return handle();
	}

	private boolean handle()
	{
        Connection connection = null;
        String batchDate = DateUtils.getSystemDate();
        Boolean result = false;
        
        try
        {
            connection = dataSource.getConnection();
         
            BatchJobHandler handle = new BatchJobHandler(factory);
            BatchHandleResult batchHandleResult = handle.handle(connection, batchDate, null);
            String rcode = batchHandleResult.getRcode();
            
            if ("0000".equals(rcode))
            	result = true;
            else
            	LOG.error("error:" + rcode);

        }
        catch (Exception e)
        {
            LOG.error("exception:", e);
            result = false;
        }
        finally
        {
            ReleaseResource.releaseDB(connection);
        }
        
		return result;
	}
}
