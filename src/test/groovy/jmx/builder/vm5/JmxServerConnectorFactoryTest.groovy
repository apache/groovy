package groovy.jmx.builder.vm5

import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import javax.management.remote.JMXServiceURL
import javax.management.remote.rmi.RMIConnectorServer
import groovy.jmx.builder.*
import java.rmi.RemoteException

class JmxServerConnectorFactoryTest extends GroovyTestCase {
    def builder
    int DEFAULT_PORT = 10997
    def rmi

    void setUp() {
        builder = new JmxBuilder()
        rmi = JmxConnectorHelper.createRmiRegistry(DEFAULT_PORT)
    }

    void tearDown() {
        JmxConnectorHelper.destroyRmiRegistry (rmi.registry)
    }

    void testJmxServerConnectorNode() {
        RMIConnectorServer result = builder.serverConnector(port: rmi.port)

        assert result
        result.start()
        assert result.isActive()
        result.stop()
    }


    void testJmxServerConnectorClient() {
        RMIConnectorServer result = builder.serverConnector(port: rmi.port)

        assert result
        result.start()
        assert result.isActive()

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:${rmi.port}/jmxrmi")
        javax.management.remote.JMXConnector conn = javax.management.remote.JMXConnectorFactory.newJMXConnector(url, null)
        conn.connect();

        result.stop()
    }

}