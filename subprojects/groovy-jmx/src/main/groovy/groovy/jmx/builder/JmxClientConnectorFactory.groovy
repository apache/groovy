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

import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

/**
 * This is the factory for node JmxBuilder.connectorClient.
 * A call to this node returns an instance of the JMXConnector interface.
 *
 * <pre>
 * JmxBuilder.clientConnector (
 *    protocol:"rmi",
 *    host:"...",
 *    port:1099,
 *    url:"...",
 * )
 * </pre>
 *
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/javax/management/remote/JMXConnector.html">JMXConnector</a>
 */
class JmxClientConnectorFactory extends AbstractFactory {

    private static final List SUPPORTED_PROTOCOLS = ["rmi", "jrmp", "iiop", "jmxmp"]

    Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeArgs, Map nodeAttribs) {
        if (nodeArgs) {
            throw new JmxBuilderException("Node '${nodeName}' only supports named attributes.")
        }
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
        JMXConnector connector = JMXConnectorFactory.newJMXConnector(serviceUrl, props)

        return connector
    }

    private JMXServiceURL generateServiceUrl(def protocol, def host, def port) {
        String url = "service:jmx:${protocol}:///jndi/${protocol}://${host}:${port}/jmxrmi"
        return new JMXServiceURL(url)
    }

}
