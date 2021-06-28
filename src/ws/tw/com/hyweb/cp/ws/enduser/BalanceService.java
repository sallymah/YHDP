package tw.com.hyweb.cp.ws.enduser;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.RPC)
public interface BalanceService {

	public String QueryCardBalance(String cardNo,String expiryDate) throws Exception;
	
}
