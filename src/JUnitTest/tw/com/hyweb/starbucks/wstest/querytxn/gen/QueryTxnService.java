package tw.com.hyweb.starbucks.wstest.querytxn.gen;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * This class was generated by Apache CXF 2.2.4
 * Fri Aug 31 16:33:09 CST 2012
 * Generated source version: 2.2.4
 * 
 */
 
@WebService(targetNamespace = "http://querytxn.ws.starbucks.hyweb.com.tw/", name = "QueryTxnService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface QueryTxnService {

    @WebResult(name = "return", targetNamespace = "http://querytxn.ws.starbucks.hyweb.com.tw/", partName = "return")
    @WebMethod
    public java.lang.String queryTxn(
        @WebParam(partName = "arg0", name = "arg0")
        java.lang.String arg0
    );
}
