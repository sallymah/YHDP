package tw.com.hyweb.cp.ws.runbatfile;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.RPC)
public interface RunProgramService {

	public void execute(@WebParam(name="filename") String filename) throws Exception;
}
