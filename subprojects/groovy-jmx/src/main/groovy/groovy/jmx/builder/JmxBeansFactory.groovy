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

/**
 * Creates metadata for exporting multiple beans in one builder node.
 */
class JmxBeansFactory extends AbstractFactory {
    /**
     * Creates meta maps for each target in the supplied list.
     *
     * @param builder the active builder
     * @param nodeName the node name
     * @param nodeParam the list of target objects
     * @param nodeAttribs named node attributes
     * @return the normalized bean metadata list
     */
    Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttribs) {
        if (!nodeParam || !(nodeParam instanceof List)) {
            throw new JmxBuilderException("Node '${nodeName}' requires a list of object to be exported.")
        }

        JmxBuilder fsb = (JmxBuilder) builder
        MBeanServer server = (MBeanServer) fsb.getMBeanServer()

        def metaMaps = []
        def targets = nodeParam

        // prepare metaMap for each object
        targets.each { target ->
            def metaMap = JmxMetaMapBuilder.buildObjectMapFrom(target)
            metaMap.server = metaMap.server ?: server
            metaMap.isMBean = JmxBuilderTools.isClassMBean(target.getClass())
            metaMaps.add(metaMap)
        }
        return metaMaps
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
     * Indicates that the beans node is terminal.
     *
     * @return {@code true}
     */
    boolean isLeaf() {
        return true
    }

    /**
     * Registers each exported bean and adds successful registrations to the parent collection.
     *
     * @param builder the active builder
     * @param parentNode the parent export container
     * @param thisNode the bean metadata list
     */
    void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        JmxBuilder fsb = (JmxBuilder) builder
        MBeanServer server = (MBeanServer) fsb.getMBeanServer()
        def metaMaps = thisNode

        def regPolicy = fsb.parentFactory?.registrationPolicy ?: "replace"

        metaMaps.each { metaMap ->
            def registeredBean = JmxBuilderTools.registerMBeanFromMap(regPolicy, metaMap)

            // if replace, remove from parent node and re add.
            if (parentNode != null && registeredBean && regPolicy == "replace") {
                for (Iterator i = parentNode.iterator(); i.hasNext();) {
                    def exportedBean = i.next()
                    if (exportedBean.name().equals(metaMap.jmxName)) {
                        i.remove()
                    }
                }
            }

            // only add if bean was successfully registered.
            if (parentNode != null && registeredBean) {
                parentNode.add(registeredBean)
            }
        }
    }
}
