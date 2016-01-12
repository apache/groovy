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

import javax.management.remote.JMXServiceURL
import javax.management.remote.rmi.RMIConnectorServer

class JmxServerConnectorFactoryTest extends GroovyTestCase {
    def builder
    int defaultPort = 10997
    def rmi

    void setUp() {
        builder = new JmxBuilder()
        rmi = JmxConnectorHelper.createRmiRegistry(defaultPort)
    }

    void tearDown() {
        JmxConnectorHelper.destroyRmiRegistry(rmi.registry)
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