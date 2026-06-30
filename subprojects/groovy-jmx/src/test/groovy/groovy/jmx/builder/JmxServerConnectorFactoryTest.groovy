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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL
import javax.management.remote.rmi.RMIConnectorServer
import javax.net.ssl.SSLContext
import javax.rmi.ssl.SslRMIClientSocketFactory
import javax.rmi.ssl.SslRMIServerSocketFactory

@ExtendWith(CgroupV2NpeMitigationExtension)
class JmxServerConnectorFactoryTest {
    def builder
    int defaultPort = 10995
    def rmi

    @BeforeEach
    void setUp() {
        builder = new JmxBuilder()
        rmi = JmxConnectorHelper.createRmiRegistry(defaultPort)
    }

    @AfterEach
    void tearDown() {
        JmxConnectorHelper.destroyRmiRegistry(rmi.registry)
    }

    @Test
    void testJmxServerConnectorNode() {
        RMIConnectorServer result = builder.serverConnector(port: rmi.port)

        assert result
        result.start()
        assert result.isActive()
        result.stop()
    }

    @Test
    void testJmxServerConnectorClient() {
        RMIConnectorServer result = builder.serverConnector(port: rmi.port)

        assert result
        result.start()
        assert result.isActive()

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:${rmi.port}/jmxrmi")
        JMXConnector conn = JMXConnectorFactory.newJMXConnector(url, null)
        conn.connect()

        result.stop()
    }

    // GROOVY-12119: connector properties were silently discarded because the property-building
    // method ended in props.clear() (a void call) and so implicitly returned null instead of the env map.
    @Test
    void testConnectorPropertiesAreReturned_Groovy12119() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port, [authenticate: false])

        assert env != null : 'connector environment map must not be discarded'
        // supplied/derived entries are present
        assert env.containsKey('com.sun.management.jmxremote.authenticate')
    }

    // GROOVY-12119: when SSL is requested the env map must carry the SSL socket factories
    @Test
    void testConnectorPropertiesApplySsl_Groovy12119() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port, [sslEnabled: true])

        assert env != null
        assert env['com.sun.management.jmxremote.ssl']
        assert env[RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE] instanceof SslRMIServerSocketFactory
        assert env[RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE] instanceof SslRMIClientSocketFactory
    }

    // GROOVY-12119: without SSL no socket factories should be added (but env is still returned)
    @Test
    void testConnectorPropertiesWithoutSsl_Groovy12119() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port, [authenticate: false])

        assert env != null
        assert !env.containsKey(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE)
        assert !env.containsKey(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE)
    }

    // GROOVY-12119: an SSL-enabled connector is built and started without error (end-to-end smoke test)
    @Test
    void testJmxServerConnectorWithSsl_Groovy12119() {
        RMIConnectorServer result = builder.serverConnector(port: rmi.port, properties: [sslEnabled: true])

        assert result
        result.start()
        assert result.isActive()
        result.stop()
    }

    // GROOVY-12119: the canonical 'com.sun.management.jmxremote.ssl' property key is recognised
    // (previously the key literal contained a stray space so the standard key never matched)
    @Test
    void testConnectorRecognizesCanonicalSslKey_Groovy12119() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port, ['com.sun.management.jmxremote.ssl': true])

        assert env != null
        assert env['com.sun.management.jmxremote.ssl']
        assert env[RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE] instanceof SslRMIServerSocketFactory
    }

    // GROOVY-12119: the SSL server socket factory is pinned to modern TLS rather than the JVM default set,
    // but only to protocols the running JDK actually supports (so old JDKs without TLS 1.3 are not locked out)
    @Test
    void testSslServerSocketFactoryRestrictsProtocols_Groovy12119() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port, [sslEnabled: true])

        SslRMIServerSocketFactory ssf = (SslRMIServerSocketFactory) env[RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE]
        def enabled = ssf.enabledProtocols as Set
        def supported = SSLContext.getDefault().supportedSSLParameters.protocols as Set

        assert !enabled.isEmpty()
        assert enabled.every { it in supported }                  // never requests an unsupported protocol -> no lockout
        assert enabled.every { it in ['TLSv1.3', 'TLSv1.2'] }     // modern TLS only, no legacy 1.0/1.1
        assert 'TLSv1.2' in enabled                               // always present on JDK 8+
    }

}
