
package tw.com.hyweb.starbucks.wstest.querybonus.gen;

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
 * Mon Jul 29 14:20:57 CST 2013
 * Generated source version: 2.2.4
 * 
 */

public final class QueryBonusService_QueryBonusServiceJobPort_Client {

    private static final QName SERVICE_NAME = new QName("http://querybonus.ws.starbucks.hyweb.com.tw/", "QueryBonusService");

    private QueryBonusService_QueryBonusServiceJobPort_Client() {
    }

    public static void main(String args[]) throws Exception {
        URL wsdlURL = QueryBonusService_Service.WSDL_LOCATION;
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
      
        QueryBonusService_Service ss = new QueryBonusService_Service(wsdlURL, SERVICE_NAME);
        QueryBonusService port = ss.getQueryBonusServiceJobPort();  
        
        {
        System.out.println("Invoking queryBonus...");
        java.lang.String _queryBonus_arg0 = "";
        java.lang.String _queryBonus__return = port.queryBonus(_queryBonus_arg0);
        System.out.println("queryBonus.result=" + _queryBonus__return);


        }

        System.exit(0);
    }

}
