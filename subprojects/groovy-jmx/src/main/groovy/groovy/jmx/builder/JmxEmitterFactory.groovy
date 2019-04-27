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
import javax.management.NotificationFilterSupport
import javax.management.ObjectName

/**
 * This class is the factory for the emitter node.  This node facilitates the declaration of a JMX event emitter.
 * The emitter is used to broadcast arbitrary event on the MBeanServer's event bus.  Registered listeners are
 * able to consume event once sent.
 * <p>
 * Supported syntax:
 * <pre>
 * def jmx = JmxBuilder()
 * jmx.emitter(name:"Object name"|ObjectName(), event:"event type")
 * ...
 * jmx.emitter.send(object)
 * </pre>
 */
class JmxEmitterFactory extends AbstractFactory {
    def newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttribs) {
        if (nodeParam) {
            throw new JmxBuilderException("Node '${nodeName}' only supports named attributes.")
        }

        JmxBuilder fsb = (JmxBuilder) builder
        def server = (MBeanServer) fsb.getMBeanServer()

        def emitter = new JmxEventEmitter()
        def name = getObjectName(fsb, emitter, nodeAttribs.remove("name"))
        def event = nodeAttribs.remove("event") ?: nodeAttribs.remove("type") ?: "jmx.builder.event.emitter"

        def listeners = nodeAttribs.remove("listeners") ?: nodeAttribs.remove("recipients")

        // notification filter
        NotificationFilterSupport filter = null
        if (event) {
            filter = new NotificationFilterSupport()
            filter.enableType(event)
            emitter.setEvent(event)
        }

        // build model mbean for emitter and register it
        if (server.isRegistered(name)) {
            server.unregisterMBean(name)
        }
        server.registerMBean(emitter, name)

        // register listeners
        if (listeners && !(listeners instanceof List)) {
            throw new JmxBuilderException("Listeners must be provided as a list [listener1,...,listenerN]")
        }

        listeners.each { l ->
            def listener = l
            try {
                if (listener instanceof String) {
                    listener = new ObjectName(l)
                }
                if (listener instanceof ObjectName) {
                    server.addNotificationListener(name, listener, filter, null)
                } else {
                    emitter.addNotificationListener(listener, filter, null)
                }
            } catch (e) {
                throw new JmxBuilderException(e)
            }
        }

        return new GroovyMBean(fsb.getMBeanServer(), name)
    }


    private ObjectName getObjectName(fsb, emitter, name) {
        if (name && name instanceof ObjectName) return name
        if (name && name instanceof String) return new ObjectName(name)
        if (!name) return new ObjectName("${fsb.getDefaultJmxNameDomain()}:type=Emitter,name=Emitter@${emitter.hashCode()}")
    }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return false
    }

    boolean isLeaf() {
        return true
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        if (parentNode != null) {
            parentNode.add(thisNode)
        }
    }

}