
package tw.com.hyweb.starbucks.wstest.balancetransfer.gen;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * This class was generated by Apache CXF 2.2.4
 * Wed Jul 24 09:40:17 CST 2013
 * Generated source version: 2.2.4
 * 
 */

public final class BalanceTransferService_BalanceTransferServiceImplPort_Client {

    private static final QName SERVICE_NAME = new QName("http://balancetransfer.ws.starbucks.hyweb.com.tw/", "BalanceTransferService");

    private BalanceTransferService_BalanceTransferServiceImplPort_Client() {
    }

    public static void main(String args[]) throws Exception {
        URL wsdlURL = BalanceTransferService_Service.WSDL_LOCATION;
        if (args.length > 0) { 
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
      
        BalanceTransferService_Service ss = new BalanceTransferService_Service(wsdlURL, SERVICE_NAME);
        BalanceTransferService port = ss.getBalanceTransferServiceImplPort();  
        
        {
        System.out.println("Invoking balanceTransfer...");
        java.lang.String _balanceTransfer_arg0 = "";
        java.lang.String _balanceTransfer__return = port.balanceTransfer(_balanceTransfer_arg0);
        System.out.println("balanceTransfer.result=" + _balanceTransfer__return);


        }

        System.exit(0);
    }

}
