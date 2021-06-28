
/*
 * 
 */

package tw.com.hyweb.starbucks.wstest.querytxn.gen;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.4
 * Fri Aug 31 16:33:09 CST 2012
 * Generated source version: 2.2.4
 * 
 */


@WebServiceClient(name = "QueryTxnService", 
                  wsdlLocation = "file:querytxn.wsdl",
                  targetNamespace = "http://querytxn.ws.starbucks.hyweb.com.tw/") 
public class QueryTxnService_Service extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://querytxn.ws.starbucks.hyweb.com.tw/", "QueryTxnService");
    public final static QName QueryTxnServiceImplPort = new QName("http://querytxn.ws.starbucks.hyweb.com.tw/", "QueryTxnServiceImplPort");
    static {
        URL url = null;
        try {
            url = new URL("file:querytxn.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:querytxn.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public QueryTxnService_Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public QueryTxnService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public QueryTxnService_Service() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     * 
     * @return
     *     returns QueryTxnService
     */
    @WebEndpoint(name = "QueryTxnServiceImplPort")
    public QueryTxnService getQueryTxnServiceImplPort() {
        return super.getPort(QueryTxnServiceImplPort, QueryTxnService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns QueryTxnService
     */
//    @WebEndpoint(name = "QueryTxnServiceImplPort")
//    public QueryTxnService getQueryTxnServiceImplPort(WebServiceFeature... features) {
//        return super.getPort(QueryTxnServiceImplPort, QueryTxnService.class, features);
//    }

}