package groovy.jmx.builder.vm5

import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import javax.management.remote.JMXServiceURL
import javax.management.remote.rmi.RMIConnectorServer
import groovy.jmx.builder.*

class JmxServerConnectorFactoryTest extends GroovyTestCase {
  def builder

  void setUp() {
    builder = new JmxBuilder()
    builder.registerFactory "serverConnector", new JmxServerConnectorFactory()
  }

  void testJmxServerConnectorNode() {
    RMIConnectorServer result = builder.serverConnector(
            port: 1099
    )

    assert result
    Registry reg = LocateRegistry.createRegistry(1099)
    result.start()
    assert result.isActive()
    result.stop()
    java.rmi.server.UnicastRemoteObject.unexportObject(reg, true)
  }


  void testJmxServerConnectorClient() {
    RMIConnectorServer result = builder.serverConnector(
            port: 1099
    )

    assert result
    Registry reg = LocateRegistry.createRegistry(1099)
    result.start()
    assert result.isActive()

    JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi")
    javax.management.remote.JMXConnector conn = javax.management.remote.JMXConnectorFactory.newJMXConnector(url, null)
    conn.connect();

    result.stop()
    java.rmi.server.UnicastRemoteObject.unexportObject(reg, true)
  }

}