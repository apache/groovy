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

import javax.management.InstanceNotFoundException
import javax.management.NotificationFilterSupport

/**
 * This factory class is used to create a listener() node for JmxBuilder.  Listener nodes are used
 * to create a generic event listener (that is automatically registered with the MBean) that can handle
 * any event broadcasted on the MBeanServer's event bus.
 * <p>
 * Syntax supported:
 * <pre>
 * JmxBuilder.listener(event:"event type", from:"Object Name"|ObjectName(), call:{event->
 *   // event handling code here.
 *})
 * </pre>
 *
 * @see groovy.jmx.builder.JmxEventListener
 */
class JmxListenerFactory extends AbstractFactory {
    /**
     * Creates and registers a listener description.
     *
     * @param builder the active builder
     * @param nodeName the node name
     * @param nodeParam positional node arguments
     * @param nodeAttribs named node attributes
     * @return the normalized listener metadata
     */
    Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttribs) {
        if (nodeParam) {
            throw new JmxBuilderException("Node '${nodeName}' only supports named attributes.")
        }
        JmxBuilder fsb = (JmxBuilder) builder
        def server = fsb.getMBeanServer()
        def map = JmxMetaMapBuilder.createListenerMap(nodeAttribs)

        def broadcaster = map.get("from")
        try {
            def eventType = (String) map.get("event");
            if (!server.isRegistered(broadcaster)) {
                throw new JmxBuilderException("MBean '${broadcaster.toString()}' is not registered in server.")
            }
            if (eventType) {
                NotificationFilterSupport filter = new NotificationFilterSupport()
                filter.enableType(eventType)
                server.addNotificationListener(broadcaster, JmxEventListener.getListener(), filter, map)
            } else {
                server.addNotificationListener(broadcaster, JmxEventListener.getListener(), null, map)
            }
        } catch (InstanceNotFoundException e) {
            throw new JmxBuilderException(e)
        }

        map
    }

    /**
     * Leaves attribute handling to {@link #newInstance(FactoryBuilderSupport, Object, Object, Map)}.
     *
     * @param builder the active builder
     * @param node the current node
     * @param nodeAttribs remaining node attributes
     * @return {@code false}
     */
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return false
    }

    /**
     * Indicates that the listener node is terminal.
     *
     * @return {@code true}
     */
    boolean isLeaf() {
        return true
    }

    /**
     * Adds the created listener metadata to its parent collection when one exists.
     *
     * @param builder the active builder
     * @param parentNode the parent node
     * @param thisNode the listener metadata
     */
    void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        if (parentNode != null) {
            parentNode.add(thisNode)
        }
    }
}
