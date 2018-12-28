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
 * This factory returns the bean() node.  This node is used to declaratively expose a POGO/POJO
 * to be exported to the MBeanServer for management.  You can use the builder node
 * to declare the descriptor or embed the descriptor directly in the object being
 * exposed using static variable descriptor (or jmx).  You should note that
 * embedded descriptor takes precedence over declared builder descriptor.
 * <p>
 * The following shows all of the different syntax forms supported by the node.
 * <pre><code>
 * bean(instance)
 * bean(target:instance, name:ObjectName|"...", desc|description:"...")
 * bean(target:instance,
 *     server:MBeanServerConnection
 *     name:ObjectName()|"...",
 *     desc|description:"...",
 *     attribs|attributes:"*",
 *     attribs|attributes:[]
 *     attribs|attributes:["attribName1","attribName2",..."attribNameN"]
 *     attribs|attributes:[
 *         "attribName0":"*",
 *         "attribName1":[desc|description:"...", default:value, writable|editable:true|false,...]
 *          "attribName2":[onChange:{event->
 *              // onChange event callback code here.
 *          ]
 *      ],
 *
 *     ctors|constructors:"*",
 *     ctors|constructors:[
 *         "CtorName0":[],
 *         "CtorName1":["paramType1","paramType2"],
 *         "CtorName2":[
 *             desc|description:"...",
 *             params:[
 *                  "type0":"*"
 *                  "type1":[desc|description:"...", name:"...", ]
 *             ]
 *         ]
 *     ],
 *
 *     ops|operations:"*",
 *     ops|operations:["opName1", "opName2", "opName3",...,"opNameN"],
 *     ops|operations:[
 *         "opName0":"*",
 *         "opName1":["type1","type2,"type3"]
 *         "opName2":[
 *             desc:"description",
 *             params:[
 *                 "type0":"*"
 *                 "type1":[desc|description:"...", name:"...", ],
 *                 "typeN":[desc|description:"...", name:"...", ],
 *             ],
 *         ],
 *         "opName3":[
 *             onCall:{event->
 *                 // onCall event callback code here.
 *}]
 *         ],
 *
 *     listeners:[
 *         ListenerName:[
 *             event: "eventType", from:"object name"|ObjectName(),
 *             call:{event->
 *                 // handler code for event
 *}],
 *     ]
 * )
 * </code></pre>
 */
class JmxBeanFactory extends AbstractFactory {
    Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttributes) {

        JmxBuilder fsb = (JmxBuilder) builder
        MBeanServer server = (MBeanServer) fsb.getMBeanServer()
        def metaMap
        def target
        // embedded descriptor or implied descriptor
        if (nodeParam) {
            target = nodeParam
            metaMap = initMetaMap(target)
            metaMap = JmxMetaMapBuilder.buildObjectMapFrom(target)
        }
        // descriptor provided
        else if (nodeAttributes) {
            target = nodeAttributes.target
            metaMap = initMetaMap(target)
            metaMap = JmxMetaMapBuilder.buildObjectMapFrom(target, nodeAttributes)
        }

        // make sure a server instance is there
        metaMap.server = metaMap.server ?: server

        //if target implements MBean, MxBean,or Dynamic bean, skip metaMap step
        metaMap.isMBean = JmxBuilderTools.isClassMBean(target.getClass())
        return metaMap
    }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return false
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        JmxBuilder fsb = (JmxBuilder) builder
        MBeanServer server = (MBeanServer) fsb.getMBeanServer()
        def metaMap = thisNode

        def regPolicy = fsb.parentFactory?.registrationPolicy ?: "replace"
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

    boolean isLeaf() {
        return false
    }

    private def initMetaMap(target) {
        if (!target) {
            throw new JmxBuilderException("You must specify a target object to " +
                    " export as MBean i.e. JmxBuilder.bean(targetInstance), " +
                    " JmxBuilder.bean([target:instance]), JmxBuilder.beans([instanceList]).")
        }

        def metaMap = [:]
        metaMap.target = target
        metaMap.name = target.class.canonicalName
        metaMap
    }
}