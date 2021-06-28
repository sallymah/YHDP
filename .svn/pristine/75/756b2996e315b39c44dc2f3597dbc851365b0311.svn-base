package tw.com.hyweb.cp.ws.enduser;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.RPC)
public interface RegisterCustService {

	public String CardRegisterOrNot(String cardNo) throws Exception;
	
	public String QueryCust(String cardNo) throws Exception;
	
	public String NewCust(String cardNo, String custFields) throws Exception;
	
	public String EditCust(String cardNo, String custFields) throws Exception;
}
