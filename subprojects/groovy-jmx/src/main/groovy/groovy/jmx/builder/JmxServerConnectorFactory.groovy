/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.jmx.builder

import javax.management.MBeanServer
import javax.management.remote.JMXAuthenticator
import javax.management.remote.JMXConnectorServer
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL
import javax.management.remote.rmi.RMIConnectorServer
import javax.net.ssl.SSLContext
import javax.rmi.ssl.SslRMIClientSocketFactory
import javax.rmi.ssl.SslRMIServerSocketFactory
import java.rmi.server.RMIClientSocketFactory
import java.rmi.server.RMIServerSocketFactory

/**
 * This is the server connector factory used for node JmxBuilder.connectorServer().  A call to this node
 * returns an instance of JMXConnectorServer interface (rmi default).
 * <p>
 * Possible syntax:
 * <pre>
 *    JmxBuilder.connectorServer(
 *        protocol:"rmi",
 *        host:"...",
 *        port:1099,
 *        url:"...",
 *        properties:[
 *            "authenticate":true|false,
 *            "passwordFile":"...",
 *            "accessFile":"...",
 *            "loginConfig":"...",
 *            "sslEnabled" : true | false
 *         ...
 *        ]
 *     )
 * </pre>
 * <p>
 * When {@code authenticate} is true a source of credentials must be supplied, being one of
 * {@code passwordFile}, {@code loginConfig}, or a {@code jmx.remote.authenticator} entry
 * holding a {@link JMXAuthenticator}. A connector which was asked to
 * authenticate but has none of these would start open, so that combination is rejected rather
 * than accepted silently. Any other entry in {@code properties} is passed to the connector
 * environment unaltered.
 * <p>
 * An RMI connector listens on {@code host}, which is {@code localhost} unless given, so by
 * default only clients on the same machine can reach it. Pass a wildcard address to listen on
 * every interface. A connector exposes the MBean server to whoever can reach it and
 * authenticates nobody unless configured to, so widening the host warrants authentication.
 *
 * @see javax.management.remote.JMXConnectorServer
 */
class JmxServerConnectorFactory extends AbstractFactory {

    private static final List SUPPORTED_PROTOCOLS = ["rmi", "jrmp", "jmxmp"]

    // Restrict the SSL server socket factory to modern TLS versions rather than relying on
    // the JVM default protocol set (which may still enable TLS 1.0/1.1 on older/misconfigured JREs).
    // Intersect with the protocols the running JDK actually supports so we never request an
    // unsupported protocol (which the factory would reject), e.g. TLS 1.3 on a JDK without it.
    private static final String[] ENABLED_TLS_PROTOCOLS = pinnedTlsProtocols()

    private static String[] pinnedTlsProtocols() {
        Set<String> preferred = ['TLSv1.3', 'TLSv1.2']
        try {
            Set<String> supported = SSLContext.getDefault().supportedSSLParameters.protocols
            Set<String> usable = preferred.intersect(supported)
            return (usable ?: preferred) as String[]
        } catch (Exception ignored) {
            return preferred as String[]
        }
    }

    /**
     * Creates a server connector for the supplied connection settings.
     *
     * @param builder the active builder
     * @param nodeName the node name
     * @param nodeArgs positional node arguments
     * @param nodeAttribs named node attributes
     * @return the configured connector server
     */
    Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeArgs, Map nodeAttribs) {
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
        if (!url && protocol == "rmi") {
            env = confineToHost(env, host)
        }
        JMXConnectorServer connector = JMXConnectorServerFactory.newJMXConnectorServer(serviceUrl, env, server)
        warnIfReachableAndOpen(env, url, host)

        return connector
    }


    /**
     * Consumes node attributes without additional processing.
     *
     * @param builder the active builder
     * @param node the current node
     * @param nodeAttribs remaining node attributes
     * @return {@code true}
     */
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return true
    }

    /**
     * Indicates that the connector server node may contain children.
     *
     * @return {@code false}
     */
    boolean isLeaf() {
        return false
    }

    /**
     * Completes connector server node creation.
     *
     * @param builder the active builder
     * @param parentNode the parent node
     * @param thisNode the created connector server
     */
    void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        //
    }

    private Map confiConnectorProperties(String protocol, int port, Map props) {
        if (!props) return null
        HashMap<String, Object> env = new HashMap<String, Object>()

        // Authentication. The com.sun.management.jmxremote.* names belong to the JDK's
        // out-of-the-box management agent, which translates them into the jmx.remote.x.*
        // names a connector server actually consumes (see sun.management.jmxremote.
        // ConnectorBootstrap). Nothing performs that translation here, so do it: putting the
        // agent's names into a connector environment leaves the connector with no
        // authenticator at all, and it accepts credential-less clients.
        def auth = props.remove("com.sun.management.jmxremote.authenticate") ?: props.remove("authenticate")
        def pFile = props.remove("com.sun.management.jmxremote.password.file") ?: props.remove("passwordFile")
        def aFile = props.remove("com.sun.management.jmxremote.access.file") ?: props.remove("accessFile")
        def loginConfig = props.remove("com.sun.management.jmxremote.login.config") ?: props.remove("loginConfig")

        if (Boolean.valueOf(auth?.toString())) {
            // A caller may instead pass a JMXAuthenticator straight through, which is the
            // standard JSR-160 route for custom authentication and is a credential source too.
            // Validate the value, not just the key: a present-but-null (or wrong-typed) entry is
            // not a credential source and would leave the connector unauthenticated.
            boolean suppliedAuthenticator = props.get(JMXConnectorServer.AUTHENTICATOR) instanceof JMXAuthenticator
            if (!pFile && !loginConfig && !suppliedAuthenticator) {
                throw new JmxBuilderException("Connector authentication was requested but no source " +
                        "of credentials was provided; supply 'passwordFile', 'loginConfig' or a " +
                        "'${JMXConnectorServer.AUTHENTICATOR}' entry, otherwise the connector would " +
                        "start unauthenticated.")
            }
            if (pFile) env.put("jmx.remote.x.password.file", pFile)
            if (loginConfig) env.put("jmx.remote.x.login.config", loginConfig)
            if (aFile) env.put("jmx.remote.x.access.file", aFile)
        }

        // SSL connection
        def ssl = props.remove("com.sun.management.jmxremote.ssl") ?: props.remove("sslEnabled")

        // config other rmi props
        if (protocol == "rmi") {
            if (ssl) {
                def csf = props.remove(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE) ?: new SslRMIClientSocketFactory()
                def ssf = props.remove(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE) ?: new SslRMIServerSocketFactory((String[]) null, ENABLED_TLS_PROTOCOLS, false)
                env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf)
                env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf)
            }
        }

        props.each { key, value ->
            env.put(key, value)
        }

        props.clear()

        return env
    }

    /**
     * Adds a server socket factory that binds the host the connector was asked for, so that a
     * connector listens where its service URL says it does. Without one, the RMI object is
     * exported on every interface whatever host the URL names, and {@code host} restricts only
     * the registry the stub is bound into. A caller who supplied a socket factory keeps it, and
     * a wildcard host is taken as a request to listen everywhere.
     *
     * @param env connector environment, possibly null
     * @param host host the connector was asked to listen on
     * @return the environment to create the connector with
     */
    private static Map confineToHost(Map env, String host) {
        Map result = env != null ? env : new HashMap<String, Object>()
        if (result.containsKey(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE)
                || result.containsKey(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE)) {
            return result
        }
        InetAddress address
        try {
            address = InetAddress.getByName(host)
        } catch (UnknownHostException ignored) {
            return result
        }
        if (address.isAnyLocalAddress()) {
            return result
        }
        // both halves are needed: the server factory decides where the exported object listens,
        // and the stub carries the client factory, which decides where a client dials. Binding
        // without the second leaves clients dialling the host RMI advertises, where nothing listens.
        result.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, new BoundServerSocketFactory(address))
        result.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new BoundClientSocketFactory(address.hostAddress))
        return result
    }

    /**
     * Warns when a connector both accepts connections from other hosts and authenticates nobody.
     * Staying quiet for a connector confined to loopback keeps the warning to the case where a
     * peer other than the operator can reach it.
     *
     * @param env connector environment, possibly null
     * @param url explicit service URL, if one was supplied
     * @param host host the connector was asked to listen on
     */
    private static void warnIfReachableAndOpen(Map env, String url, String host) {
        boolean authenticated = env != null && (env.get(JMXConnectorServer.AUTHENTICATOR) != null
                || env.get("jmx.remote.x.password.file") != null
                || env.get("jmx.remote.x.login.config") != null)
        if (authenticated) {
            return
        }
        boolean reachable = true
        if (!url) {
            try {
                reachable = !InetAddress.getByName(host).isLoopbackAddress()
            } catch (UnknownHostException ignored) {
            }
        }
        if (reachable) {
            System.err.println("warning: JMX connector on '${url ?: host}' accepts connections from other hosts " +
                    "and authenticates nobody; it exposes the MBean server to any peer that can reach it")
        }
    }

    /**
     * Client socket factory that dials the address the connector was bound to. It travels to the
     * client inside the stub, so it must be serializable.
     */
    private static class BoundClientSocketFactory implements RMIClientSocketFactory, Serializable {
        private static final long serialVersionUID = 1L
        private final String host

        BoundClientSocketFactory(String host) {
            this.host = host
        }

        @Override
        Socket createSocket(String ignoredHost, int port) throws IOException {
            return new Socket(host, port)
        }

        @Override
        boolean equals(Object other) {
            other instanceof BoundClientSocketFactory && ((BoundClientSocketFactory) other).host == host
        }

        @Override
        int hashCode() {
            host.hashCode()
        }
    }

    /**
     * Server socket factory that binds one address rather than every interface.
     */
    private static class BoundServerSocketFactory implements RMIServerSocketFactory {
        private final InetAddress address

        BoundServerSocketFactory(InetAddress address) {
            this.address = address
        }

        @Override
        ServerSocket createServerSocket(int port) throws IOException {
            return new ServerSocket(port, 0, address)
        }

        @Override
        boolean equals(Object other) {
            other instanceof BoundServerSocketFactory && ((BoundServerSocketFactory) other).address == address
        }

        @Override
        int hashCode() {
            address.hashCode()
        }
    }

    private JMXServiceURL generateServiceUrl(def protocol, def host, def port) {
        String url = "service:jmx:${protocol}:///jndi/${protocol}://${host}:${port}/jmxrmi"
        return new JMXServiceURL(url)
    }
}
