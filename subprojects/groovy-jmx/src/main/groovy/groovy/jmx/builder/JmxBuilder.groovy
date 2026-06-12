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

/**
 * This is JmxBuilder's factory builder class.  It is the parent node to all other children nodes supported
 * by JmxBuilder.
 */
class JmxBuilder extends FactoryBuilderSupport {
    private MBeanServerConnection server
    private String defaultNameDomain = JmxBuilderTools.DEFAULT_DOMAIN
    private String defaultNameType = JmxBuilderTools.DEFAULT_NAME_TYPE
    private String mode = "markup"

    /** Creates a builder backed by the default MBean server connection. */
    JmxBuilder() {
        registerFactories()
    }

    /**
     * Creates a builder backed by the supplied MBean server connection.
     *
     * @param svrConnection the server connection to use
     */
    JmxBuilder(MBeanServerConnection svrConnection) {
        this()
        server = svrConnection
    }

    /** Registers the builder nodes supported by {@code JmxBuilder}. */
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

    /**
     * Returns the active MBean server connection, creating the default one on demand.
     *
     * @return the active server connection
     */
    MBeanServerConnection getMBeanServer() {
        if (!server) {
            server = JmxBuilderTools.getMBeanServer()
        }
        return server
    }

    /**
     * Sets the default domain used when generating object names.
     *
     * @param domain the default JMX domain
     */
    void setDefaultJmxNameDomain(String domain) {
        this.defaultNameDomain = domain
    }

    /**
     * Returns the default domain used when generating object names.
     *
     * @return the default JMX domain
     */
    String getDefaultJmxNameDomain() {
        return this.defaultNameDomain
    }

    /**
     * Sets the default type used when generating object names.
     *
     * @param type the default JMX type
     */
    void setDefaultJmxNameType(String type) {
        this.defaultNameType = type
    }

    /**
     * Returns the default type used when generating object names.
     *
     * @return the default JMX type
     */
    String getDefaultJmxNameType() {
        return this.defaultNameType
    }

    /**
     * Replaces the active MBean server connection.
     *
     * @param svr the server connection to use
     */
    void setMBeanServer(MBeanServerConnection svr) {
        server = svr
    }

    /**
     * Sets the builder mode.
     *
     * @param mode the builder mode
     */
    void setMode(String mode) {
        this.mode = mode
    }

    /**
     * Returns the current builder mode.
     *
     * @return the builder mode
     */
    String getMode() {
        return mode
    }

    /**
     * Resolves a factory for the requested node, falling back to the parent factory when needed.
     *
     * @param name the node name
     * @param attributes the node attributes
     * @param value the node value
     * @return the resolved factory, if any
     */
    protected Factory resolveFactory(Object name, Map attributes, Object value) {
        Factory factory = super.resolveFactory(name, attributes, value);
        if (!factory) {
            factory = getParentFactory()?.childFactory
        }
        return factory
    }

}
