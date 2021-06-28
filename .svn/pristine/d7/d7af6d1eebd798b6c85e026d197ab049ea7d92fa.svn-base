package tw.com.hyweb.cp.ws.appointreload;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.RPC)
public interface AppointReloadService
{
    
	/**
	 * Handle Simulate AppointReload by fileName
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public boolean simulateAppointReloadByFileName(String fileName) throws Exception;
    
	/**
	 * Handle Simulate AppointReload by balance_id and arSerno
	 * 
	 * @param balanceId
	 * @param arSerno
	 * @return
	 * @throws Exception
	 */
    public boolean simulateAppointReloadByArSerno(String balanceId, String arSerno) throws Exception;
}
