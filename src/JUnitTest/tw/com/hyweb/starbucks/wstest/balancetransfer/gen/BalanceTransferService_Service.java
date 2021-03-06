
/*
 * 
 */

package tw.com.hyweb.starbucks.wstest.balancetransfer.gen;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.4
 * Wed Jul 24 09:40:17 CST 2013
 * Generated source version: 2.2.4
 * 
 */


@WebServiceClient(name = "BalanceTransferService", 
                  wsdlLocation = "file:BalanceTransferService.wsdl",
                  targetNamespace = "http://balancetransfer.ws.starbucks.hyweb.com.tw/") 
public class BalanceTransferService_Service extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://balancetransfer.ws.starbucks.hyweb.com.tw/", "BalanceTransferService");
    public final static QName BalanceTransferServiceImplPort = new QName("http://balancetransfer.ws.starbucks.hyweb.com.tw/", "BalanceTransferServiceImplPort");
    static {
        URL url = null;
        try {
            url = new URL("file:BalanceTransferService.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:BalanceTransferService.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public BalanceTransferService_Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public BalanceTransferService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public BalanceTransferService_Service() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     * 
     * @return
     *     returns BalanceTransferService
     */
    @WebEndpoint(name = "BalanceTransferServiceImplPort")
    public BalanceTransferService getBalanceTransferServiceImplPort() {
        return super.getPort(BalanceTransferServiceImplPort, BalanceTransferService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns BalanceTransferService
     */
//    @WebEndpoint(name = "BalanceTransferServiceImplPort")
//    public BalanceTransferService getBalanceTransferServiceImplPort(WebServiceFeature... features) {
//        return super.getPort(BalanceTransferServiceImplPort, BalanceTransferService.class, features);
//    }

}
