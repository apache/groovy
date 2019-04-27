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

import javax.management.MBeanServerConnection

/** *
 * This is JmxBuilder's factory builder class.  It is the parent node to all other children nodes supported
 * by JmxBuilder.
 */
class JmxBuilder extends FactoryBuilderSupport {
    private MBeanServerConnection server
    private String defaultNameDomain = JmxBuilderTools.DEFAULT_DOMAIN
    private String defaultNameType = JmxBuilderTools.DEFAULT_NAME_TYPE
    private String mode = "markup"

    JmxBuilder() {
        registerFactories()
    }

    JmxBuilder(MBeanServerConnection svrConnection) {
        this()
        server = svrConnection
    }

    protected void registerFactories() {
        registerFactory "export", new JmxBeanExportFactory()
        registerFactory "bean", new JmxBeanFactory()
        registerFactory "beans", new JmxBeansFactory()
        registerFactory "timer", new JmxTimerFactory()
        registerFactory "listener", new JmxListenerFactory()
        registerFactory "emitter", new JmxEmitterFactory()

        JmxServerConnectorFactory svrFactory = new JmxServerConnectorFactory()
        registerFactory "server", svrFactory
        registerFactory "connectorServer", svrFactory
        registerFactory "serverConnector", svrFactory

        def newClientFactory = { new JmxClientConnectorFactory() }
        registerFactory "client", newClientFactory()
        registerFactory "connector", newClientFactory()
        registerFactory "clientConnector", newClientFactory()
        registerFactory "connectorClient", newClientFactory()
    }

    MBeanServerConnection getMBeanServer() {
        if (!server) {
            server = JmxBuilderTools.getMBeanServer()
        }
        return server
    }

    void setDefaultJmxNameDomain(String domain) {
        this.defaultNameDomain = domain
    }

    String getDefaultJmxNameDomain() {
        return this.defaultNameDomain
    }

    void setDefaultJmxNameType(String type) {
        this.defaultNameType = type
    }

    String getDefaultJmxNameType() {
        return this.defaultNameType
    }

    void setMBeanServer(MBeanServerConnection svr) {
        server = svr
    }

    void setMode(String mode) {
        this.mode = mode
    }

    String getMode() {
        return mode
    }

    protected Factory resolveFactory(Object name, Map attributes, Object value) {
        Factory factory = super.resolveFactory(name, attributes, value);
        if (!factory) {
            factory = getParentFactory()?.childFactory
        }
        return factory
    }

}