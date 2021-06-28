package tw.com.hyweb.yhdp.ws.impfiles;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.RPC)
public interface ImpFilesService
{
    
	/**
	 * Handle Import Perso Feedback by fileName
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public String impPersoFeedbackByFileName(@WebParam(name="filename") String fileName) throws Exception;
}
