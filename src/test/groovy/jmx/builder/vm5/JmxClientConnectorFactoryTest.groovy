package groovy.jmx.builder.vm5

import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import javax.management.remote.rmi.RMIConnector
import javax.management.remote.rmi.RMIConnectorServer
import groovy.jmx.builder.*

class JmxClientConnectorFactoryTest extends GroovyTestCase {
  def builder

  void setUp() {
    builder = new JmxBuilder()
    builder.registerFactory "clientConnector", new JmxClientConnectorFactory()
    builder.registerFactory "serverConnector", new JmxServerConnectorFactory()
  }

  void tearDown() {
  }

  void testJmxClientConnectorNode() {
    RMIConnectorServer server = builder.serverConnector(port: 1098)
    Registry reg = LocateRegistry.createRegistry(1098)
    server.start()

    RMIConnector client = builder.clientConnector(
            port: 1098
    )

    assert client
    client.connect()
    server.stop()
    java.rmi.server.UnicastRemoteObject.unexportObject(reg, true)
  }

  void testJmxClientConnectorUrl() {
    RMIConnectorServer server = builder.serverConnector(port: 1099)
    Registry reg = LocateRegistry.createRegistry(1099)
    server.start()

    RMIConnector client = builder.clientConnector(url: "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi")
    client.connect()
    client.close()
    server.stop()
    java.rmi.server.UnicastRemoteObject.unexportObject(reg, true)
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