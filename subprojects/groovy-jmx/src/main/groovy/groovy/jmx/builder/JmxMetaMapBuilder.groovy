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

import javax.management.ObjectName
import java.lang.reflect.Constructor

/**
 * The JmxMetaMapBuilder class is used to collect meta data passed in JmxBuilder nodes.  Once collected,
 * the data is then normalized to be represented uniformly regardless of the resource where it was obtained.
 */
class JmxMetaMapBuilder {

    private static final ATTRIB_EXCEPTION_LIST = ["class", "descriptor", "jmx", "metaClass"]
    private static final OPS_EXCEPTION_LIST = [
            "clone",
            "equals",
            "finalize",
            "getClass", "getProperty",
            "hashCode",
            "invokeMethod",
            "notify", "notifyAll",
            "setProperty",
            "toString",
            "wait"
    ]

    /**
     * Builds a complete meta map graph for the specified object using default values.
     * All generated attributes are set to read-only.
     * @param object used to build meta data graph
     * @return fully-realized meta map of the object
     * @see #buildAttributeMapFrom(Object)
     * @see #buildConstructorMapFrom(Object)
     * @see #buildOperationMapFrom(Object)
     */
    static Map buildObjectMapFrom(def object) {
        if (!object) {
            throw new JmxBuilderException("Unable to create MBean, missing target object.")
        }

        def map

        // 1. look for embedded descriptor,
        // 2. if none is found, build default
        def metaProp = object.metaClass.getMetaProperty("descriptor") ?: object.metaClass.getMetaProperty("jmx")
        if (metaProp) {
            def descriptor = object.metaClass.getProperty(object.getClass(), metaProp?.name)

            // if only the jmx name is provided fill in the rest.
            if (descriptor.size() == 1 && descriptor.name) {
                map = [
                        target      : object,
                        name        : object.getClass().name,
                        jmxName     : getObjectName(descriptor),
                        displayName : "JMX Managed Object ${object.class.canonicalName}".toString(),
                        attributes  : buildAttributeMapFrom(object),
                        constructors: buildConstructorMapFrom(object),
                        operations  : buildOperationMapFrom(object)
                ]

            }
            // else, build description from descriptor
            else {
                map = [
                        target      : object,
                        name        : object.getClass().name,
                        displayName : descriptor.desc ?: descriptor.desc,
                        attributes  : buildAttributeMapFrom(object, descriptor.attributes ?: descriptor.attribs),
                        constructors: buildConstructorMapFrom(object, descriptor.constructors ?: descriptor.ctors),
                        operations  : buildOperationMapFrom(object, descriptor.operations ?: descriptor.ops),
                        listeners   : buildListenerMapFrom(descriptor.listeners),
                        mbeanServer : descriptor.server ?: descriptor.mbeanServer
                ]

                // validate object Name
                map.jmxName = getObjectName(descriptor) ?:
                        JmxBeanInfoManager.buildDefaultObjectName(
                                JmxBuilderTools.DEFAULT_DOMAIN,
                                JmxBuilderTools.DEFAULT_NAME_TYPE,
                                object)
            }


        }
        // build meta map with default info if no descriptor is provided.
        else {
            map = [
                    target      : object,
                    name        : object.getClass().name,
                    jmxName     : JmxBeanInfoManager.buildDefaultObjectName(
                            JmxBuilderTools.DEFAULT_DOMAIN,
                            JmxBuilderTools.DEFAULT_NAME_TYPE,
                            object),
                    displayName : "JMX Managed Object ${object.class.canonicalName}".toString(),
                    attributes  : buildAttributeMapFrom(object),
                    constructors: buildConstructorMapFrom(object),
                    operations  : buildOperationMapFrom(object)
            ]
        }

        return map
    }

    /**
     * Builds a complete meta map graph for a given target and descriptor.
     * @param object used to build meta data graph
     * @param descriptor a full descriptor map describing object attributes and ops.
     * @return fully-realized meta map of the object
     * @see #buildAttributeMapFrom(Object, Map)
     * @see #buildConstructorMapFrom(Object, Map)
     * @see #buildOperationMapFrom(Object, Map)
     */
    static Map buildObjectMapFrom(def object, def descriptor) {
        if (!object) {
            throw new JmxBuilderException("Unable to create MBean, missing target object.")
        }
        def map
        // if only the name & target is specified, fill in rest with defaults
        if (descriptor.size() == 2 && (descriptor.name && descriptor.target)) {
            map = [
                    target      : object,
                    jmxName     : getObjectName(descriptor),
                    name        : object.getClass().name,
                    displayName : "JMX Managed Object ${object.class.canonicalName}".toString(),
                    attributes  : buildAttributeMapFrom(object),
                    constructors: buildConstructorMapFrom(object),
                    operations  : buildOperationMapFrom(object)
            ]

        }
        // assume all needed info is there
        else {

            map = [
                    target      : object,
                    name        : object.getClass().name,
                    displayName : descriptor.desc ?: descriptor.desc,
                    attributes  : buildAttributeMapFrom(object, descriptor.attributes ?: descriptor.attribs),
                    constructors: buildConstructorMapFrom(object, descriptor.constructors ?: descriptor.ctors),
                    operations  : buildOperationMapFrom(object, descriptor.operations ?: descriptor.ops),
                    listeners   : buildListenerMapFrom(descriptor.listeners),
                    mbeanServer : descriptor.server ?: descriptor.mbeanServer
            ]

            map.jmxName = getObjectName(descriptor) ?:
                    JmxBeanInfoManager.buildDefaultObjectName(
                            JmxBuilderTools.DEFAULT_DOMAIN,
                            JmxBuilderTools.DEFAULT_NAME_TYPE,
                            object)
        }

        map
    }

    private static ObjectName getObjectName(def map) {
        if (!map) return null
        def jmxName
        if (map.name instanceof String) {
            jmxName = new ObjectName(map.name)
        } else if (map.name instanceof ObjectName) {
            jmxName = map.name
        }
        jmxName
    }

    /** *
     * Builds attribute meta map with default information from an instance of an object.
     * @param the object from which attribute info will be retrieved
     * @return the meta map for the attribute
     */
    static Map buildAttributeMapFrom(def object) {
        def properties = object.metaClass.getProperties()

        def attribs = [:]
        properties.each { MetaProperty prop ->
            if (!ATTRIB_EXCEPTION_LIST.contains(prop.name)) {
                def attrib = [:]
                def getterPrefix = (prop.type.name == "java.lang.Boolean" || prop.type.name == "boolean") ? "is" : "get"
                def name = JmxBuilderTools.capitalize(prop.name)
                attrib.name = name
                attrib.displayName = "Property ${prop.name}".toString()
                attrib.readable = true
                attrib.getMethod = getterPrefix + name
                attrib.writable = false
                attrib.type = prop.type.name
                attrib.property = prop
                attribs.put(name, attrib)
            }
        }
        return attribs
    }

    /** *
     * Sanitizes and builds an attribute meta map from a descriptor collection.
     * The collection can either be a map of the form [attribName:[descriptor...],...]
     * or [attribName1,...attribNameN].
     * The code guests sensible defaults when values are not provided
     * @param the object about which meta data is collected
     * @param descriptor collection: either a map or a list
     * @return a Map of meta information about the object.
     */
    static Map buildAttributeMapFrom(def object, def descCollection) {
        def map = [:]

        // attribs:"*"
        if (descCollection instanceof String && descCollection.equals("*")) {
            map = buildAttributeMapFrom(object)
        }
        // attribs:["attrName1",attrName2",...,"attrNameN"]
        if (descCollection && descCollection instanceof List) {
            descCollection.each { attrib ->
                MetaProperty prop = object.metaClass.getMetaProperty(JmxBuilderTools.uncapitalize(attrib))
                if (prop) {
                    map.put(JmxBuilderTools.capitalize(attrib), createAttributeMap(prop, attrib, "*"))
                }
            }
        }
        //attribs:[desc:..., readable:..., writable:...]
        if (descCollection && descCollection instanceof Map) {
            descCollection.each { attrib, attrDescriptor ->
                MetaProperty prop = object.metaClass.getMetaProperty(JmxBuilderTools.uncapitalize(attrib))
                if (prop) {
                    map.put(JmxBuilderTools.capitalize(attrib), createAttributeMap(prop, attrib, attrDescriptor))
                }
            }
        }

        return map
    }

    /**
     * Builds a fully-normalized meta map for a given POJO property and its associated descriptor.
     * The method fills in defaults where possible and creates listeners for onChange attribute events.
     * @param prop - property to build meta map for.
     * @param attribName - the descriptive name of attribute.
     * @param descriptor - the descriptor collected from JmxBuilder.bean() node.
     * @return - a well-formed meta map.
     */
    private static Map createAttributeMap(prop, attribName, descriptor) {
        def desc = (descriptor instanceof Map) ? descriptor : [:]
        def map = [:]
        def name = JmxBuilderTools.capitalize(attribName)
        def getterPrefix = (prop.type.name == "java.lang.Boolean" || prop.type.name == "boolean") ? "is" : "get"

        map.name = name
        map.displayName = desc.desc ?: desc.description ?: "Property ${name}".toString()
        map.type = prop.type.name
        map.readable = (desc.readable != null) ? desc.readable : true
        if (map.readable) {
            map.getMethod = getterPrefix + name
        }
        map.writable = (desc.writable != null) ? desc.writable : false
        if (map.writable) {
            map.setMethod = "set" + name
        }
        map.defaultValue = desc.defaultValue ?: desc.default
        map.property = prop

        // attrib change listener setup
        def listener = desc.onChange ?: desc.onChanged
        if (listener) {
            map.methodListener = [:]
            map.methodListener.callback = listener
            map.methodListener.target = "set" + name
            map.methodListener.type = "attributeChangeListener"
            map.methodListener.attribute = name
        }
        return map
    }

    // ******************* CONSTRUCTORS ********************** */

    /**
     * Returns a meta map of constructors from given object.
     * @param object to profile
     * @return The meta map generated.
     */
    static Map buildConstructorMapFrom(def object) {
        def methods = object.getClass().getDeclaredConstructors()
        def ctors = [:]
        def cntr = 0
        for (Constructor ctor in methods) {
            def map = [:]
            map.name = ctor.name
            map.displayName = "Constructor for class ${object.getClass().getName()}".toString()
            map.role = "constructor"
            map.constructor = ctor
            map.put("params", buildParameterMapFrom(ctor))
            ctors.put(ctor.name[ctor.name.lastIndexOf(".") + 1..-1] + "@" + cntr++, map)
        }
        return ctors
    }

    /**
     * Builds a fully normalized constructor meta map.
     * @param the object where constructor is defined.
     * @param the meta map that will be normalized
     * @return a normalized meta map for the constructor
     */
    static Map buildConstructorMapFrom(def object, def descCollection) {
        def map = [:]

        // ctors:"*"
        if (descCollection && descCollection instanceof String && descCollection.equals("*")) {
            map = buildConstructorMapFrom(object)
        }

        // ctor:[ctorName:...]
        if (descCollection && descCollection instanceof Map) {
            descCollection.each { ctorKey, descriptor ->
                def params

                // ctorName:[]
                if (descriptor && (descriptor instanceof List && descriptor.size() == 0)) {
                    params = null // represnts a ctor with no params
                }
                // ctorName:["paramType1","paramType2"..."paramTypeN"]
                if (descriptor && (descriptor instanceof List && descriptor.size() > 0)) {
                    params = []
                    descriptor.each { param ->
                        params << JmxBuilderTools.TYPE_MAP[JmxBuilderTools.getNormalizedType(param)]
                    }
                }
                // ctorName:[desc:"...", params:[paramName1:"*", paramName2:[name:"...",desc:"..."]]
                if (descriptor && descriptor instanceof Map) {
                    def paramTypes = []
                    if (descriptor.params && descriptor.params instanceof Map) {
                        paramTypes = descriptor.params.keySet().toList()
                    } else if (descriptor.params && descriptor.params instanceof List) {
                        paramTypes = descriptor.params
                    }

                    params = []
                    paramTypes.each { p ->
                        params << JmxBuilderTools.TYPE_MAP[JmxBuilderTools.getNormalizedType(p)]
                    }
                }
                // find matching constructors
                Constructor ctor = object.class.getDeclaredConstructor((params != null) ? (Class[]) params : null)
                map.put(ctorKey, createConstructorMap(ctor, descriptor))
            }
        }

        return map
    }

    /**
     * Builds a fully-normalized constructor meta map for the specified constructor.  The method provides
     * defaults where necessary and uses the descriptor to fill in the collected data from JmxBuilder.bean() node..
     * @param ctor - the Constructor instance being described.
     * @param descriptor - descriptor meta data collected from JmxBuilder.bean() node.
     * @return a fully-normalized meta map of the constructor.
     */
    private static Map createConstructorMap(ctor, descriptor) {
        def desc = (descriptor && descriptor instanceof Map) ? descriptor : [:]
        def map = [:]

        map.name = ctor.name
        map.displayName = desc.description ?: desc.desc ?: "Class constructor"
        map.role = "constructor"
        map.constructor = ctor
        if (desc.params)
            map.put("params", buildParameterMapFrom(ctor, desc.params))
        else
            map.put("params", buildParameterMapFrom(ctor))

        return map
    }

    /* **************************************
     * OPERATIONS
     * **************************************/
    /**
     * Returns a meta map of operations from given object.
     * @param object to profile
     * @return The meta map generated.
     */
    static Map buildOperationMapFrom(def object) {
        def methods = object.metaClass.getMethods()
        def ops = [:]

        def declaredMethods = object.getClass().getDeclaredMethods()*.name

        methods.each { method ->
            // avoid picking up extra methods from parents
            if ((declaredMethods.contains(method.name) && !OPS_EXCEPTION_LIST.contains(method.name)) || (!OPS_EXCEPTION_LIST.contains(method.name))) {
                String mName = method.name
                MetaProperty prop = (mName.startsWith("get") || mName.startsWith("set")) ? object.metaClass.getMetaProperty(JmxBuilderTools.uncapitalize(mName.length() == 3 ? mName : mName[3..-1])) : null
                // skip exporting getters/setters to avoid dbl exposure.  They are exported differently.
                if (!prop) {
                    def map = [:]
                    map.name = mName
                    map.displayName = "Method ${method.name} for class ${object.getClass().getName()}".toString()
                    map.role = "operation"
                    map.method = method
                    map.put("params", buildParameterMapFrom(method))
                    ops.put(mName, map)
                }
            }
        }
        return ops
    }

    static Map buildOperationMapFrom(object, descCollection) {
        def map = [:]

        // operations:"*"
        if (descCollection && descCollection instanceof String && descCollection.equals("*")) {
            map = buildOperationMapFrom(object)
        }

        // operations:["opName1","opName2",..., "opNameN"]
        if (descCollection && descCollection instanceof List) {
            descCollection.each { opName ->
                // find the first method that matches the name
                def method
                for (m in object.metaClass.getMethods()) {
                    if (m.name.equals(opName)) {
                        method = m
                        break
                    }
                }

                if (method) {
                    map.put(opName, createOperationMap(object, method, "*"))
                }
            }
        }

        // operations:[foo1:[:], foo2[:], foo3:[:]...]
        if (descCollection && descCollection instanceof Map) {
            descCollection.each { opName, descriptor ->
                def params
                def method

                // opName:"*"
                if (descriptor && (descriptor instanceof String && descriptor.equals("*"))) {
                    method = object.metaClass.respondsTo(object, opName)[0] // grab the first method that matches
                } else {
                    // foo:["int",...,"bool"]
                    if (descriptor && descriptor instanceof List) {
                        params = descriptor
                    }

                    // foo:[:]
                    if (descriptor && descriptor instanceof Map) {
                        // foo:[params:["paramTypeName0":[name:"",desc:""], paramTypeNameN[:]]]
                        if (descriptor.params && descriptor.params instanceof Map) {
                            params = descriptor?.params.keySet().toList()
                        }
                        // foo:[params:["paramTypeName0",...,"paramTypeNameN"]]
                        if (descriptor.params && descriptor.params instanceof List) {
                            params = descriptor.params
                        }
                    }

                    // gather clas array Class[]
                    if (params) {
                        def paramTypes = []
                        params?.each { key ->
                            paramTypes << JmxBuilderTools.TYPE_MAP[JmxBuilderTools.getNormalizedType(key)]
                        }
                        params = paramTypes ?: null
                    }
                    def signature = (params != null) ? (Object[]) params : null
                    def methods = object.metaClass.respondsTo(object, opName, signature)
                    method = methods[0] ?: null
                }

                if (method) {
                    map.put(opName, createOperationMap(object, method, descriptor))
                }
            }
        }

        return map
    }

    /**
     * Creates a fully-normalized meta map for a given method (or operation).  The method uses the descriptor
     * to create a map object of the meta data provided with defaults where necessary.
     * @param method - the method being described
     * @param descriptor - the meta data collected from JmxBuilder.bean()
     * @return fully-normalized meta map
     */
    private static Map createOperationMap(object, method, descriptor) {
        def desc = (descriptor && descriptor instanceof Map) ? descriptor : [:]
        def map = [:]

        map.name = method.name
        map.displayName = desc.description ?: desc.desc ?: "Method ${method.name} for class ${object.getClass().getName()}".toString()
        map.role = "operation"
        map.method = method
        if (desc.size() > 0 && desc.params) {
            map.put("params", buildParameterMapFrom(method, desc.params))
        } else {
            map.put("params", buildParameterMapFrom(method))
        }
        // operation invoke listener setup
        def listener = desc.onInvoke ?: desc.onInvoked ?: desc.onCall ?: desc.onCalled
        if (listener) {
            map.methodListener = [:]
            map.methodListener.callback = listener
            map.methodListener.target = method.name
            map.methodListener.type = "operationCallListener"
        }

        return map
    }

    /* **************************************
     * OPERATION PARAMETERS
     * **************************************/
    /** *
     * Builds a normalized parameter meta map for all params on provided method.
     * @param the method with parameters to normalized
     * @return the normalized map
     */
    static Map buildParameterMapFrom(def method) {
        def map = [:]
        if (!method) return map
        def params = method.getParameterTypes()
        if (params) {
            params.each { def param ->
                map.put(param.name, createParameterMap(method, param, "*"))
            }
        }
        return map
    }

    /** *
     * Builds a fully normalized parameter meta map for the method and the given meta map.
     * @param the method from which to extract normalized info.
     * @param a given meta map which will be normalized
     * @return the normalized map
     */
    static Map buildParameterMapFrom(method, descCollection) {
        def map = [:]
        if (!method) return map

        if (descCollection && descCollection instanceof Map) {
            descCollection.each { param, paramMap ->
                def type = getParamTypeByName(method, JmxBuilderTools.getNormalizedType(param))
                if (type) {
                    map.put(type.name, createParameterMap(method, type, paramMap))
                }
            }
        } else if (descCollection && descCollection instanceof List) {
            descCollection.each { param ->
                def type = getParamTypeByName(method, JmxBuilderTools.getNormalizedType(param))
                if (type) {
                    map.put(type.name, createParameterMap(method, type, "*"))
                }
            }
        }
        return map
    }

    /** *
     * Creates a fully-normalized meta map for a given parameter type on a give method.
     * The descriptor represents data collected from JmxBuilder.bean() node.
     * @param method - method with parameter being described
     * @param type - type of parameter being described
     * @param descriptor - descriptor from JmxBuilder.bean() node.
     * @return - a fully-realized meta map.
     */
    private static Map createParameterMap(method, type, descriptor) {
        def desc = (descriptor instanceof Map) ? descriptor : [:]
        def map = [:]
        map.name = desc.name ?: type.name
        map.displayName = desc.description ?: desc.desc ?: "Parameter ${type.name} for ${method.name}".toString()
        map.type = type
        map.method = method

        return map
    }

    private static def getParamTypeByName(method, typeName) {
        for (type in method.getParameterTypes()) {
            if (type.name.equals(typeName))
                return type
        }
        return null
    }

    /* **************************************
     * LISTENERS
     * **************************************/

    /**
     * Creates a fully-normalized meta map for a given collection of listeners.
     * @param - collection of descriptors to normalize
     * @see JmxMetaMapBuilder#createListenerMap(Object)
     */
    static buildListenerMapFrom(descCollection) {
        def map = [:]
        if (descCollection && descCollection instanceof Map) {
            descCollection.each { name, listenerMap ->
                map.put(name, createListenerMap(listenerMap))
            }
        }

        return map
    }

    /**
     * Builds normalized meta map of the provided listener descriptor.
     * @param descriptor - descriptive data collected from JmxBuilder listener nodes.
     * @return - fully normalized meta map of listener data.
     */
    static Map createListenerMap(descriptor) {
        def map = [:]
        map.type = "eventListener"
        map.event = descriptor.event

        map.from = descriptor.from ?: descriptor.source ?: descriptor.broadcaster
        if (!map.from) {
            throw new JmxBuilderException("Missing event source: specify source ObjectName (i.e. from:'...').")
        }
        try {
            map.from = (map.from instanceof String) ? new ObjectName(map.from) : map.from
        } catch (Exception e) {
            throw new JmxBuilderException(e)
        }
        map.callback = descriptor.call

        map
    }
}
