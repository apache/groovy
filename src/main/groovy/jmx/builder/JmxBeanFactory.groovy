/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.jmx.builder

import javax.management.MBeanServer
import javax.management.ObjectName
import javax.management.modelmbean.ModelMBeanInfo

/**
 * This factory returns the bean() node.  This node is used to declaratively expose a POGO/POJO
 * to be exported to the MBeanServer for management.
 *
 * <p>The following shows all of the different syntax forms supported by the node.</p>
 *
 * <per>
 *bean(instance)
 *bean(target:instance, name:ObjectName|"...", desc|description:"...")
 *bean(target:instance,
 *    name:ObjectName()|"...",
 *    desc|description:"...",
 *    attribs|attributes:"*",
 *    attribs|attributes:[]
 *    attribs|attributes:["attribName1","attribName2",..."attribNameN"]
 *    attribs|attributes:[
 *        "attribName0":"*",
 *        "attribName1":[desc|description:"...", default:value, writable|editable:true|false,...]
 *         "attribName2":[onChange:{event->
 *             // onChange event callback code here.
 *         ]
 *     ],
 *
 *     ctors|constructors:"*",
 *     ctors|constructors:[
 *        "CtorName0":[],
 *        "CtorName1":["paramType1","paramType2"],
 *        "CtorName2":[
 *            desc|description:"...",
 *            params:[
 *                 "type0":"*"
 *                 "type1":[desc|description:"...", name:"...", ]
 *            ]
 *        ]
 *     ],
 *
 *    ops|operations:"*",
 *    ops|operations:["opName1", "opName2", "opName3",...,"opNameN"],
 *    ops|operations:[
 *        "opName0":"*",
 *        "opName1":["type1","type2,"type3"]
 *        "opName2":[
 *            desc:"description",
 *            params:[
 *                "type0":"*"
 *                "type1":[desc|description:"...", name:"...", ],
 *                "typeN":[desc|description:"...", name:"...", ],
 *            ],
 *        ],
 *        "opName3":[
 *            onCall:{event->
 *                // onCall event callaback code here.
 *            }]
 *        ],
 *
 *    listeners:[
 *        ListenerName:[
 *            event: "eventType", from:"object name"|ObjectName(),
 *            call:{event->
 *                // handler code for event
 *            }],
 *    ]
 *)
 *</pre>
 * 
 * @author Vladimir Vivien
 */
class JmxBeanFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttributes) {

        JmxBuilder fsb = (JmxBuilder) builder
        MBeanServer server = (MBeanServer) fsb.getMBeanServer()
        def target = nodeParam ?: ((nodeAttributes) ? nodeAttributes.target : null)

        if (!target) {
            throw new JmxBuilderException("Unable to export MBean, no target object was specified.")
        }

        def metaMap = initMetaMap(target)
        def declaredMap = retrieveDeclaredMetaMap(target, nodeAttributes) ?: null
        def embeddedMap = retrieveEmbeddedMetaMap(target) ?: null
        ModelMBeanInfo info

        def jmxName = getObjectName(declaredMap) ?:
                getObjectName(embeddedMap) ?:
                JmxBeanInfoManager.buildDefaultObjectName(
                        fsb.getDefaultJmxNameDomain(),
                        fsb.getDefaultJmxNameType(),
                        target)
        metaMap.jmxName = jmxName

        //if target implements MBean,MxBean,or Dyanmic bean, skip metaMap step
        boolean classIsMBean = JmxBuilderTools.isClassMBean(target.getClass())
        if (classIsMBean) {
            metaMap.isMBean = true
        } else {
            // target provided as bean(object)
            if (!declaredMap) {
                // build implicit meta map
                def implicitMap = JmxMetaMapBuilder.buildObjectMapFrom(target)
                metaMap.putAll(combineMaps(embeddedMap, implicitMap))
            } else {
                // if only the name is provided
                if (declaredMap.name && declaredMap.target && !declaredMap.attributes && !declaredMap.constructors && !declaredMap.operations) {
                    declaredMap.putAll(JmxMetaMapBuilder.buildObjectMapFrom(target))
                }
                metaMap.putAll(declaredMap)
            }
        }
        return metaMap
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return false;
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        JmxBuilder fsb = (JmxBuilder) builder
        MBeanServer server = (MBeanServer) fsb.getMBeanServer()
        def metaMap = thisNode

        // get modelmbean info from meta map
        def info = JmxBeanInfoManager.getModelMBeanInfoFromMap(metaMap)

        // Do mbean export: if target is already mbean, ignore, otherwise build modelmbean
        def mbean
        if (metaMap.isMBean) {
            mbean = metaMap.target
        } else {
            mbean = new JmxBuilderModelMBean(info)
            mbean.setManagedResource(metaMap.target)
            mbean.addOperationCallListeners metaMap.attributes
            mbean.addOperationCallListeners metaMap.operations

            if (metaMap.listeners) {
                mbean.addEventListeners server, metaMap.listeners
            }
        }

        def regPolicy = fsb.getParentFactory()?.registrationPolicy ?: "replace"
        def registeredBean = registerObject(server, regPolicy, mbean, metaMap.jmxName, parentNode)

        // only add if bean was successfully registered.
        if (parentNode != null && registeredBean) {
            parentNode.add(registeredBean)
        }
    }

    public boolean isLeaf() {
        return false
    }

    private def initMetaMap(target) {
        def metaMap = [:]
        metaMap.target = target
        metaMap.name = target.class.canonicalName
        metaMap
    }


    private def getObjectName(def map) {
        if (!map) return null
        def jmxName
        if (map && map?.name instanceof String) {
            jmxName = new ObjectName(map?.name)
        } else if (map && map.name instanceof ObjectName) {
            jmxName = map.name
        }
        jmxName
    }

    private def retrieveDeclaredMetaMap(def target, def nodeAttribs) {
        if (!nodeAttribs) return null

        def map = [:]
        map.name = nodeAttribs.name
        map.target = target
        map.attributes = JmxMetaMapBuilder.buildAttributeMapFrom(target, nodeAttribs.attributes ?: nodeAttribs.attribs)
        map.constructors = JmxMetaMapBuilder.buildConstructorMapFrom(target, nodeAttribs.constructors ?: nodeAttribs.ctors)
        map.operations = JmxMetaMapBuilder.buildOperationMapFrom(target, nodeAttribs.operations ?: nodeAttribs.ops)
        map.listeners = JmxMetaMapBuilder.buildListenerMapFrom(nodeAttribs.listeners ?: nodeAttribs.notifications)

        return map
    }

    private def retrieveEmbeddedMetaMap(def object) {
        if (!object) return null
        def prop = object.metaClass.getMetaProperty("descriptor") ?: object.metaClass.getMetaProperty("jmx")
        if (prop)
            return object.metaClass.getProperty(object.getClass(), prop?.name)
        else null
    }

    // rolls map1 into map0
    private def combineMaps(embedded, implicit) {
        def map0 = [:]
        map0.displayName = embedded?.description ?: embedded?.desc ?: implicit?.displayName
        map0.attributes = embedded?.attributes ?: embedded?.attribs ?: implicit?.attributes
        map0.constructors = embedded?.constructors ?: embedded?.ctors ?: implicit?.constructors
        map0.operations = embedded?.operations ?: embedded?.ops ?: implicit?.operations

        // Todo pull notifications
        map0.notifications = embedded?.notifications ?: implicit?.notifications

        return map0
    }

    private def registerObject(server, policy, bean, name, exportList) {
        def gbean
        switch (policy) {
            case "replace":
                if (server.isRegistered(name)) {
                    for (Iterator i = exportList.iterator(); i.hasNext();) {
                        def exportedBean = i.next()
                        if (exportedBean.name().equals(name)) {
                            i.remove()
                        }
                    }
                    server.unregisterMBean name
                }
                server.registerMBean(bean, name)
                gbean = new GroovyMBean(server, name)
                break
            case "ignore":
                if (server.isRegistered(name))
                    break
            case "error":
            default:
                if (server.isRegistered(name)) {
                    throw new JmxBuilderException("A Bean with name ${name} is already registered on the server.")
                } else {
                    server.registerMBean(bean, name)
                    gbean = new GroovyMBean(server, name)
                }
        }
        return gbean
    }

}