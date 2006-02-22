import org.codehaus.groovy.gsoap.SoapClient

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.server.http.XFireHttpServer;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectInvoker;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;

import test.PersonServiceImpl;
import test.PersonService;

//
// Create a SoapClient using http://localhost:8090/PersonService?wsdl as WSDL
// then it queries the method findPerson which returns one Person object
// 
class Test1 extends GroovyTestCase {

    void testSoapClient() {
        def serviceFactory = new ObjectServiceFactory();
        def service = serviceFactory.create(PersonService.class);
        // Set the implementation class
        service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, PersonServiceImpl.class);
        
        // Register the service in the ServiceRegistry
        def xfire = XFireFactory.newInstance().getXFire();
        xfire.getServiceRegistry().register(service);
        
        // Start the HTTP server
        def server = new XFireHttpServer();
        server.setPort(6969);
        System.out.println("start Server & run tests");
        server.start();

        try {
            def proxy = new SoapClient("http://localhost:6969/PersonService?wsdl")
            def result = proxy.findPerson(1)
            assert (result.firstName == "Guillaume")

            result = proxy.getPersons()
            assert (result.ns1Person.size() == 3)
            assert (result.ns1Person[0].firstName == "Guillaume")
            assert (result.ns1Person[2].firstName == "Jochen")
        } finally {
            server.stop()
        }
    }
}
