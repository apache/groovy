package groovy.jmx.builder

import javax.management.MBeanServer
import javax.management.remote.JMXConnectorServer
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL
import javax.management.remote.rmi.RMIConnectorServer
import javax.rmi.ssl.SslRMIClientSocketFactory
import javax.rmi.ssl.SslRMIServerSocketFactory

class JmxServerConnectorFactory extends AbstractFactory {
    private static List SUPPORTED_PROTOCOLS = ["rmi", "jrmp", "iiop", "jmxmp"]

    public Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeArgs, Map nodeAttribs) {
        if (nodeArgs) {
            throw new JmxBuilderException("Node '${nodeName}' only supports named attributes.")
        }
        JmxBuilder fsb = (JmxBuilder) builder
        def protocol = nodeAttribs?.remove("protocol") ?: nodeAttribs?.remove("transport") ?: "rmi"
        def port = nodeAttribs?.remove("port")
        def host = nodeAttribs?.remove("host") ?: nodeAttribs?.remove("address") ?: "localhost"
        def url = nodeAttribs?.remove("url")
        def props = nodeAttribs?.remove("properties") ?: nodeAttribs?.remove("props") ?: nodeAttribs?.remove("env")

        def env = confiConnectorProperties(protocol, port, props)

        nodeAttribs.clear()

        if (!port && !url) {
            throw new JmxBuilderException("Node '${nodeName} requires attribute 'port' to specify server's port number.")
        }
        if (!SUPPORTED_PROTOCOLS.contains(protocol)) {
            throw new JmxBuilderException("Connector protocol '${protocol} is not supported at this time. " +
                    "Supported protocols are ${SUPPORTED_PROTOCOLS}.")
        }

        MBeanServer server = (MBeanServer) fsb.getMBeanServer()
        JMXServiceURL serviceUrl = (url) ? new JMXServiceURL(url) : generateServiceUrl(protocol, host, port)
        JMXConnectorServer connector = JMXConnectorServerFactory.newJMXConnectorServer(serviceUrl, env, server);



        return connector
    }


    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return true;
    }

    public boolean isLeaf() {
        return false
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        //
    }

    private Map confiConnectorProperties(String protocol, int port, Map props) {
        if (!props) return null
        HashMap<String, Object> env = new HashMap<String, Object>()

        // secure connection
        def auth = props.remove("com.sun.management.jmxremote.authenticate") ?: props.remove("authenticate")
        env.put("com.sun.management.jmxremote.authenticate", auth)
        def pFile = props.remove("com.sun.management.jmxremote.password.file") ?: props.remove("passwordFile")
        env.put("com.sun.management.jmxremote.password.file", pFile)
        def aFile = props.remove("com.sun.management.jmxremote.access.file") ?: props.remove("accessFile")
        env.put("com.sun.management.jmxremote.access.file", aFile)

        // SSL connection
        def ssl = props.remove("com.sun.management.jmxremote. ssl") ?: props.remove("sslEnabled")
        env.put("com.sun.management.jmxremote.ssl", ssl)

        // config other rmi props
        if (protocol == "rmi") {
            if (ssl) {
                def csf = props.remove(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE) ?: new SslRMIClientSocketFactory()
                def ssf = props.remove(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE) ?: new SslRMIServerSocketFactory()
                env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
                env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
            }
        }

        props.each {key, value ->
            env.put(key, value)
        }

        props.clear()
    }

    private JMXServiceURL generateServiceUrl(def protocol, def host, def port) {
        String url = "service:jmx:${protocol}:///jndi/${protocol}://${host}:${port}/jmxrmi"
        return new JMXServiceURL(url)
    }
}