
package tw.com.hyweb.cp.ws.appointreload;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.namespace.QName;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.annotations.jsr181.Jsr181WebAnnotations;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.jaxb2.JaxbTypeRegistry;
import org.codehaus.xfire.service.Endpoint;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.soap.AbstractSoapBinding;
import org.codehaus.xfire.transport.TransportManager;

public class AppointReloadServiceClient {

    private static XFireProxyFactory proxyFactory = new XFireProxyFactory();
    private HashMap endpoints = new HashMap();
    private Service service0;

    public AppointReloadServiceClient() {
        create0();
        Endpoint AppointReloadServicePortTypeLocalEndpointEP = service0 .addEndpoint(new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServicePortTypeLocalEndpoint"), new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServicePortTypeLocalBinding"), "xfire.local://AppointReloadService");
        endpoints.put(new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServicePortTypeLocalEndpoint"), AppointReloadServicePortTypeLocalEndpointEP);
        Endpoint AppointReloadServiceHttpPortEP = service0 .addEndpoint(new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServiceHttpPort"), new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServiceHttpBinding"), "http://10.10.10.31:8080/hyweb-ws-sit/services/AppointReloadService");
        endpoints.put(new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServiceHttpPort"), AppointReloadServiceHttpPortEP);
    }

    public Object getEndpoint(Endpoint endpoint) {
        try {
            return proxyFactory.create((endpoint).getBinding(), (endpoint).getUrl());
        } catch (MalformedURLException e) {
            throw new XFireRuntimeException("Invalid URL", e);
        }
    }

    public Object getEndpoint(QName name) {
        Endpoint endpoint = ((Endpoint) endpoints.get((name)));
        if ((endpoint) == null) {
            throw new IllegalStateException("No such endpoint!");
        }
        return getEndpoint((endpoint));
    }

    public Collection getEndpoints() {
        return endpoints.values();
    }

    private void create0() {
        TransportManager tm = (org.codehaus.xfire.XFireFactory.newInstance().getXFire().getTransportManager());
        HashMap props = new HashMap();
        props.put("annotations.allow.interface", true);
        AnnotationServiceFactory asf = new AnnotationServiceFactory(new Jsr181WebAnnotations(), tm, new AegisBindingProvider(new JaxbTypeRegistry()));
        asf.setBindingCreationEnabled(false);
        service0 = asf.create((tw.com.hyweb.cp.ws.appointreload.AppointReloadServicePortType.class), props);
        {
            AbstractSoapBinding soapBinding = asf.createSoap11Binding(service0, new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServiceHttpBinding"), "http://schemas.xmlsoap.org/soap/http");
        }
        {
            AbstractSoapBinding soapBinding = asf.createSoap11Binding(service0, new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServicePortTypeLocalBinding"), "urn:xfire:transport:local");
        }
    }

    public AppointReloadServicePortType getAppointReloadServicePortTypeLocalEndpoint() {
        return ((AppointReloadServicePortType)(this).getEndpoint(new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServicePortTypeLocalEndpoint")));
    }

    public AppointReloadServicePortType getAppointReloadServicePortTypeLocalEndpoint(String url) {
        AppointReloadServicePortType var = getAppointReloadServicePortTypeLocalEndpoint();
        org.codehaus.xfire.client.Client.getInstance(var).setUrl(url);
        return var;
    }

    public AppointReloadServicePortType getAppointReloadServiceHttpPort() {
        return ((AppointReloadServicePortType)(this).getEndpoint(new QName("http://appointreload.ws.cp.hyweb.com.tw", "AppointReloadServiceHttpPort")));
    }

    public AppointReloadServicePortType getAppointReloadServiceHttpPort(String url) {
        AppointReloadServicePortType var = getAppointReloadServiceHttpPort();
        org.codehaus.xfire.client.Client.getInstance(var).setUrl(url);
        return var;
    }

}
