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

import groovy.test.GroovyTestCase

import javax.management.remote.rmi.RMIConnector
import javax.management.remote.rmi.RMIConnectorServer

class JmxClientConnectorFactoryTest extends GroovyTestCase {
    def builder
    int defaultPort = 10995
    def rmi

    void setUp() {
        super.setUp()
        def hostAddress = getHostAddress()
        println "hostAddress: ${hostAddress}"
        System.setProperty("java.rmi.server.hostname", hostAddress)
        builder = new JmxBuilder()
        rmi = JmxConnectorHelper.createRmiRegistry(defaultPort)
    }

    void tearDown() {
        super.tearDown()
        JmxConnectorHelper.destroyRmiRegistry(rmi.registry)
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

    static String getHostAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces()
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement()
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses()
                while (addresses.hasMoreElements()) {
                    InetAddress ip = (InetAddress) addresses.nextElement()
                    if (ip != null
                            && ip instanceof Inet4Address
                            && !ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1) {
                        return ip.getHostAddress()
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
        return null
    }

}