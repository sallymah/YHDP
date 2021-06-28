
package tw.com.hyweb.cp.ws.appointreload;

import javax.jws.WebService;

@WebService(serviceName = "AppointReloadService", targetNamespace = "http://appointreload.ws.cp.hyweb.com.tw", endpointInterface = "tw.com.hyweb.cp.ws.appointreload.AppointReloadServicePortType")
public class AppointReloadServiceImpl
    implements AppointReloadServicePortType
{


    public boolean simulateAppointReload1(String in0, String in1) {
        throw new UnsupportedOperationException();
    }

    public boolean simulateAppointReload(String in0) {
        throw new UnsupportedOperationException();
    }

}
