package groovy.jmx.builder.vm5

import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import javax.management.remote.rmi.RMIConnector
import javax.management.remote.rmi.RMIConnectorServer
import groovy.jmx.builder.*

class JmxClientConnectorFactoryTest extends GroovyTestCase {
    def builder
    int DEFAULT_PORT = 10995
    def rmi

    void setUp() {
        builder = new JmxBuilder()
        rmi = JmxConnectorHelper.createRmiRegistry(DEFAULT_PORT)
    }

    void tearDown() {
        JmxConnectorHelper.destroyRmiRegistry (rmi.registry)
    }

    void testJmxClientConnectorNode() {
        RMIConnectorServer server = builder.serverConnector(port: rmi.port)
        server.start()

        RMIConnector client = builder.clientConnector(
                port: rmi.port
        )

        assert client
        client.connect()
        server.stop()
    }

    void testJmxClientConnectorUrl() {
        RMIConnectorServer server = builder.serverConnector(port: rmi.port)
        server.start()

        RMIConnector client = builder.clientConnector(url: "service:jmx:rmi:///jndi/rmi://localhost:${rmi.port}/jmxrmi")
        client.connect()
        client.close()
        server.stop()
    }

    void testJmxClientConnectorFailure() {
        shouldFail {
            RMIConnector client = builder.clientConnector(
                port: 1099
            )
            assert client
            client.connect()
        }
    }

}