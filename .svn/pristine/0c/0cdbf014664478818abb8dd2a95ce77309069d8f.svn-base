package tw.com.hyweb.cp.ws.enduser;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.RPC)
public interface TxnService {

	//public String QueryCardTxn(String cardNo) throws Exception;
	
	public String QueryCardTxn(String cardNo, String expiryDate) throws Exception;
	
	public String QueryTxnDtl(String cardNo, String expiryDate, String lmsInvoiceNo) throws Exception;
	
}
