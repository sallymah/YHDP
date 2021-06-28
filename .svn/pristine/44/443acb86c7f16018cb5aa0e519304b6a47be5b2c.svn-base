package tw.com.hyweb.yhdp.ws.runshellfile;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.RPC)
public interface RunShellProgramService {

	public boolean execute(@WebParam(name="shellfilename") String shellfilename,  @WebParam(name="fullfilename") String fullfilename) throws Exception;
}
