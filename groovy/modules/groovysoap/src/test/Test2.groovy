import groovy.net.soap.SoapServer
import groovy.net.soap.SoapClient

//
// Create a SoapServer on localhost using port 6969.
// Register serice represented by by the Groovy script MathService.groovy
//
class Test2 extends GroovyTestCase {

    void testSoapClient() {
        def server = new SoapServer("localhost", 6980)
        server.setNode("MathService")
        System.out.println("start Server & run tests")
        server.start()

        sleep 2000

        try {
            def proxy = new SoapClient("http://localhost:6980/MathServiceInterface?wsdl")
            def result = proxy.add(1.0, 2.0)
            assert (result == 3.0)

            result = proxy.square(3.0)
            assert (result == 9.0)
        } finally {
            server.stop()
        }
    }
}
