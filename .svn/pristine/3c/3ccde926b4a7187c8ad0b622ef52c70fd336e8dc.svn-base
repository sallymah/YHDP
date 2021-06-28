
package tw.com.hyweb.cp.ws.appointreload;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(name = "AppointReloadServicePortType", targetNamespace = "http://appointreload.ws.cp.hyweb.com.tw")
@SOAPBinding(use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface AppointReloadServicePortType {


    @WebMethod(operationName = "simulateAppointReload1", action = "")
    @WebResult(name = "out", targetNamespace = "http://appointreload.ws.cp.hyweb.com.tw")
    public boolean simulateAppointReload1(
        @WebParam(name = "in0", targetNamespace = "http://appointreload.ws.cp.hyweb.com.tw")
        String in0,
        @WebParam(name = "in1", targetNamespace = "http://appointreload.ws.cp.hyweb.com.tw")
        String in1);

    @WebMethod(operationName = "simulateAppointReload", action = "")
    @WebResult(name = "out", targetNamespace = "http://appointreload.ws.cp.hyweb.com.tw")
    public boolean simulateAppointReload(
        @WebParam(name = "in0", targetNamespace = "http://appointreload.ws.cp.hyweb.com.tw")
        String in0);

}
