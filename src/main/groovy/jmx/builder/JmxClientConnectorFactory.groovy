package groovy.jmx.builder

import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

class JmxClientConnectorFactory extends AbstractFactory {
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

        if (!port && !url) {
            throw new JmxBuilderException("Node '${nodeName} requires attribute 'port' to specify server's port number.")
        }
        if (!SUPPORTED_PROTOCOLS.contains(protocol)) {
            throw new JmxBuilderException("Connector protocol '${protocol} is not supported at this time. " +
                    "Supported protocols are ${SUPPORTED_PROTOCOLS}.")
        }

        JMXServiceURL serviceUrl = (url) ? new JMXServiceURL(url) : generateServiceUrl(protocol, host, port)
        JMXConnector connector = JMXConnectorFactory.newJMXConnector(serviceUrl, props);

        return connector

    }

    private JMXServiceURL generateServiceUrl(def protocol, def host, def port) {
        String url = "service:jmx:${protocol}:///jndi/${protocol}://${host}:${port}/jmxrmi"
        return new JMXServiceURL(url)
    }

}