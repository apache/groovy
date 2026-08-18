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

import static groovy.test.GroovyAssert.shouldFail

import javax.management.remote.JMXAuthenticator
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorServer
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

    // GROOVY-12270: authentication properties must land on the keys a connector server
    // consumes, not on the JDK management agent's names, which it ignores.
    @Test
    void testAuthenticationPropertiesMapToConsumedKeys() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port,
                [authenticate: true, passwordFile: 'pwd.properties', accessFile: 'access.properties'])

        assert env['jmx.remote.x.password.file'] == 'pwd.properties'
        assert env['jmx.remote.x.access.file'] == 'access.properties'
        assert !env.containsKey('com.sun.management.jmxremote.password.file')
        assert !env.containsKey('com.sun.management.jmxremote.access.file')
    }

    @Test
    void testLoginConfigMapsToConsumedKey() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port,
                [authenticate: true, loginConfig: 'MyLoginModule'])

        assert env['jmx.remote.x.login.config'] == 'MyLoginModule'
    }

    // Credentials are only configured when authentication was actually asked for.
    @Test
    void testCredentialsIgnoredWhenAuthenticationNotRequested() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port,
                [authenticate: false, passwordFile: 'pwd.properties'])

        assert !env.containsKey('jmx.remote.x.password.file')
    }

    // Asking for authentication without any source of credentials would start an open
    // connector, which is the failure this ticket is about, so it is rejected.
    @Test
    void testAuthenticationWithoutCredentialSourceIsRejected() {
        def factory = new JmxServerConnectorFactory()
        def ex = shouldFail(JmxBuilderException) {
            factory.confiConnectorProperties('rmi', rmi.port, [authenticate: true])
        }
        assert ex.message.contains('passwordFile')
        assert ex.message.contains('loginConfig')
    }

    // A caller may supply their own JMXAuthenticator instead of a password file; that is the
    // standard JSR-160 route and must count as a source of credentials.
    @Test
    void testCallerSuppliedAuthenticatorSatisfiesAuthenticationRequest() {
        def factory = new JmxServerConnectorFactory()
        def authenticator = { env -> new javax.security.auth.Subject() } as JMXAuthenticator
        def env = factory.confiConnectorProperties('rmi', rmi.port,
                [authenticate: true, (JMXConnectorServer.AUTHENTICATOR): authenticator])

        assert env[JMXConnectorServer.AUTHENTICATOR].is(authenticator)
    }

    // A present-but-null (or wrong-typed) authenticator entry is not a credential source; it must
    // be rejected rather than pass the check on the key's presence alone and leave the connector
    // unauthenticated.
    @Test
    void testNullAuthenticatorIsNotACredentialSource() {
        def factory = new JmxServerConnectorFactory()
        def ex = shouldFail(JmxBuilderException) {
            factory.confiConnectorProperties('rmi', rmi.port,
                    [authenticate: true, (JMXConnectorServer.AUTHENTICATOR): null])
        }
        assert ex.message.contains(JMXConnectorServer.AUTHENTICATOR)

        shouldFail(JmxBuilderException) {
            factory.confiConnectorProperties('rmi', rmi.port,
                    [authenticate: true, (JMXConnectorServer.AUTHENTICATOR): 'not-an-authenticator'])
        }
    }

    // End-to-end: a connector configured to authenticate must reject a credential-less client.
    @Test
    void testAuthenticatedConnectorRejectsAnonymousClient() {
        File dir = File.createTempDir()
        try {
            File password = new File(dir, 'jmxremote.password')
            password.text = 'probeuser probepass\n'
            File access = new File(dir, 'jmxremote.access')
            access.text = 'probeuser readwrite\n'
            [password, access].each { it.setReadable(false, false); it.setReadable(true, true) }

            def server = builder.serverConnector(port: rmi.port,
                    properties: [authenticate: true, passwordFile: password.path, accessFile: access.path])
            server.start()
            try {
                JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:${rmi.port}/jmxrmi")
                shouldFail(SecurityException) {
                    JMXConnectorFactory.connect(url, null).withCloseable { it.MBeanServerConnection.MBeanCount }
                }
                // ...and accept the configured one.
                def creds = [(JMXConnector.CREDENTIALS): ['probeuser', 'probepass'] as String[]]
                JMXConnectorFactory.connect(url, creds).withCloseable {
                    assert it.MBeanServerConnection.MBeanCount > 0
                }
            } finally {
                server.stop()
            }
        } finally {
            dir.deleteDir()
        }
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
        // GROOVY-12270: the com.sun.management.jmxremote.* names belong to the JDK management
        // agent and mean nothing in a connector environment, so they are no longer copied into
        // it; authentication was not requested here, so nothing is configured for it.
        assert !env.containsKey('com.sun.management.jmxremote.authenticate')
        assert !env.containsKey('jmx.remote.x.password.file')
    }

    // GROOVY-12119: when SSL is requested the env map must carry the SSL socket factories
    @Test
    void testConnectorPropertiesApplySsl_Groovy12119() {
        def factory = new JmxServerConnectorFactory()
        def env = factory.confiConnectorProperties('rmi', rmi.port, [sslEnabled: true])

        assert env != null
        // The socket factories are what actually enable SSL; see GROOVY-12270 for why the
        // com.sun.management.jmxremote.ssl key itself is no longer placed in the environment.
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
        // Recognition is evidenced by the socket factories being configured from it.
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
