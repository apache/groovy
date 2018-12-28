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

import javax.management.DynamicMBean
import javax.management.MBeanServerConnection
import javax.management.MBeanServerFactory
import javax.management.ObjectName
import java.lang.management.ManagementFactory

/**
 * This is a utility class used as a helper for JmxBuilder.
 */
class JmxBuilderTools {
    static String DEFAULT_DOMAIN = "jmx.builder"
    static String DEFAULT_NAME_TYPE = "ExportedObject"
    static String NODE_NAME_ATTRIBUTES = "attributes"
    static String NODE_NAME_ATTRIBS = "attribs"
    static String NODE_NAME_CONSTRUCTORS = "constructors"
    static String NODE_NAME_CTORS = "ctors"
    static String NODE_NAME_OPERATIONS = "operations"
    static String NODE_NAME_OPS = "ops"

    static String ATTRIB_KEY_DESCRIPTION = "description"
    static String ATTRIB_KEY_DESC = "desc"
    static String ATTRIB_KEY_TYPE = "type"
    static String ATTRIB_KEY_DEFAULT = "defaultValue"

    static String JMX_KEY = "jmx"

    static String DESC_KEY = "descriptor"
    static String DESC_KEY_MBEAN_RESOURCE = "resource"
    static String DESC_KEY_MBEAN_RESOURCE_TYPE = "ObjectReference"
    static String DESC_KEY_MBEAN_ATTRIBS = "attributes"
    static String DESC_KEY_MBEAN_OPS = "operations"
    static String DESC_KEY_MBEAN_CTORS = "constructors"
    static String DESC_KEY_MBEAN_NOTES = "notifications"

    static String DESC_KEY_NAME = "name"
    static String DESC_KEY_JMX_NAME = "jmxName"
    static String DESC_KEY_DISPLAY_NAME = "displayName"
    static String DESC_KEY_TYPE = "descriptorType"
    static String DESC_KEY_GETMETHOD = "getMethod"
    static String DESC_KEY_SETMETHOD = "setMethod"

    static String DESC_KEY_EVENT_TYPE = "eventType"
    static String DESC_KEY_EVENT_NAME = "eventName"
    static String DESC_KEY_EVENT_SOURCE = "eventSource"
    static String DESC_KEY_EVENT_MESSAGE = "messageText"

    static String DESC_VAL_TYPE_ATTRIB = "attribute"
    static String DESC_VAL_TYPE_GETTER = "getter"
    static String DESC_VAL_TYPE_SETTER = "setter"
    static String DESC_VAL_TYPE_OP = "operation"
    static String DESC_VAL_TYPE_NOTIFICATION = "notification"
    static String DESC_VAL_TYPE_CTOR = "constructor"
    static String DESC_VAL_TYPE_MBEAN = "mbean"
    static String DESC_KEY_ROLE = "role"
    static String DESC_KEY_READABLE = "readable"
    static String DESC_KEY_WRITABLE = "writable"
    static String DESC_KEY_SIGNATURE = "signature"

    static String EVENT_KEY_CONTEXTS = "eventContexts"
    static String EVENT_KEY_CALLBACK = "eventCallback"
    static String EVENT_KEY_CALLBACK_RESULT = "eventCallbackResult"
    static String EVENT_KEY_METHOD = "eventMethod"
    static String EVENT_KEY_METHOD_RESULT = "eventMethodResult"
    static String EVENT_KEY_ISATTRIB = "eventIsAttrib"
    static String EVENT_KEY_NAME = "eventName"
    static String EVENT_KEY_MESSAGE = "eventMessage"
    static String EVENT_KEY_TYPE = "eventType"
    static String EVENT_KEY_NODE_TYPE = "eventNodeType"
    static String EVENT_VAL_NODETYPE_BROADCASTER = "broadcaster"
    static String EVENT_VAL_NODETYPE_LISTENER = "listener"
    static String EVENT_KEY_TARGETS = "eventListeners"


    static Map PRIMITIVE_TYPES = [
            "char"   : java.lang.Integer.TYPE,
            "byte"   : java.lang.Byte.TYPE,
            "short"  : java.lang.Short.TYPE,
            "int"    : java.lang.Integer.TYPE,
            "long"   : java.lang.Long.TYPE,
            "float"  : java.lang.Float.TYPE,
            "double" : java.lang.Double.TYPE,
            "boolean": java.lang.Boolean.TYPE
    ]
    static Map TYPE_MAP = [
            "object"              : java.lang.Object.class,
            "Object"              : java.lang.Object.class,
            "java.lang.Object"    : java.lang.Object.class,

            "string"              : java.lang.String.class,
            "String"              : java.lang.String.class,
            "java.lang.String"    : java.lang.String.class,

            "char"                : char.class,
            "character"           : java.lang.Character.class,
            "Character"           : java.lang.Character.class,
            "java.lang.Character" : java.lang.Character.class,

            "byte"                : byte.class,
            "Byte"                : java.lang.Byte.class,
            "java.lang.Byte"      : java.lang.Byte.class,

            "short"               : short.class,
            "Short"               : java.lang.Short.class,
            "java.lang.Short"     : java.lang.Short.class,

            "int"                 : int.class,
            "integer"             : java.lang.Integer.class,
            "Integer"             : java.lang.Integer.class,
            "java.lang.Integer"   : java.lang.Integer.class,

            "long"                : long.class,
            "Long"                : java.lang.Long.class,
            "java.lang.Long"      : java.lang.Long.class,

            "float"               : float.class,
            "Float"               : java.lang.Float.class,
            "java.lang.Float"     : java.lang.Float.class,

            "double"              : double.class,
            "Double"              : java.lang.Double.class,
            "java.lang.Double"    : java.lang.Double.class,

            "boolean"             : boolean.class,
            "Boolean"             : java.lang.Boolean.class,
            "java.lang.Boolean"   : java.lang.Boolean.class,

            "bigDec"              : java.math.BigDecimal.class,
            "bigDecimal"          : java.math.BigDecimal.class,
            "BigDecimal"          : java.math.BigDecimal.class,
            "java.math.BigDecimal": java.math.BigDecimal.class,

            "bigInt"              : java.math.BigInteger.class,
            "bigInteger"          : java.math.BigInteger.class,
            "BigInteger"          : java.math.BigInteger.class,
            "java.math.BigInteger": java.math.BigInteger.class,

            "date"                : java.util.Date.class,
            "java.util.Date"      : java.util.Date.class
    ]

    /**
     * Capitalize the string passed.
     * @param value - string to capitalize.
     * @return - a capitalized string.
     */
    static String capitalize(String value) {
        if (!value) return null
        if (value.length() == 1) return value.toUpperCase()
        return (value.length() > 1) ? value[0].toUpperCase() + value[1..-1] : value.toUpperCase()
    }

    /**
     * Uncapitalizes a string.
     * @param value - string to uncap.
     * @return uncap'ed string.
     */
    static String uncapitalize(String value) {
        if (!value) return null
        if (value.length() == 1) return value.toLowerCase()
        return (value.length() > 1) ? value[0].toLowerCase() + value[1..-1] : value.toLowerCase()
    }

    /**
     * Builds a default ObjectName() instance for a given backing POJO/POGO
     * @param obj - the backing pojo/pogo
     * @return the generated ObjectName() instance.
     */
    static ObjectName getDefaultObjectName(def obj) {
        String name = DEFAULT_DOMAIN + ":name=${obj.getClass().getName()},hashCode=${obj.hashCode()}"
        try {
            return new ObjectName(name)
        } catch (Exception ex) {
            throw new JmxBuilderException("Unable to create JMX ObjectName $name", ex)
        }
    }

    /**
     * Returns an MBeanServerConnection instance.  It searches for declared MBeanServers
     * from the MBeanServerFactory.  If none is found, the default Platform MBeanServer is returned.
     */
    static MBeanServerConnection getMBeanServer() {
        def servers = MBeanServerFactory.findMBeanServer(null);
        def server = servers.size() > 0 ? servers[0] : ManagementFactory.getPlatformMBeanServer()
        return server
    }

    /** *
     * Returns method signature (as Class[]) given the meta map that describes the method.
     * @param params - the map with parameter info.
     * @return Class[] that represent the method signature.
     */
    static Class[] getSignatureFromParamInfo(def params) {
        if (params == null || params.size() == 0) return null
        Object[] result = new Object[params.size()]
        params.eachWithIndex { param, i ->
            def type = TYPE_MAP.get(param.getType()) ?: Class.forName(param.getType()) ?: null
            result.putAt i, type
        }

        return result
    }

    /**
     * Returns the proper type's class name when a short version is provided (i.e. String returns java.lang.String)
     * @param type - the type name to normalize
     * @return the normalized type name.
     */
    static String getNormalizedType(String type) {
        if (typeIsPrimitive(type))
            return PRIMITIVE_TYPES[type].name
        return TYPE_MAP[type]?.name ?: Class.forName(type)?.name ?: null
    }

    /**
     * Tests whether a type name is a primitive.
     * @param typeName - the type name to test.
     * @return true = if primitive
     */
    private static boolean typeIsPrimitive(String typeName) {
        PRIMITIVE_TYPES.containsKey(typeName)
    }

    /**
     * Tests whether the provided class implements MBean.  It uses the following runes
     * <p>
     * <pre>
     * if(
     *     DynamicMBean.class.isAssignable(cls) ||
     *     cls.getName().endsWith("MBean") ||
     *     cls.getName().endsWith("MXBean")
     * ) then class is MBean
     * </pre>
     *
     * @param cls - class to test
     * @return true = if class implements DynamicMBean or interface name that ends in MBean or MXBean.
     */
    static boolean isClassMBean(Class cls) {
        boolean result = false
        if (cls == null) result = false
        if (DynamicMBean.class.isAssignableFrom(cls)) {
            result = true
        }

        for (face in cls.getInterfaces()) {
            String name = face.getName()
            if (name.endsWith("MBean") || name.endsWith("MXBean")) {
                result = true
                break
            }
        }

        return result
    }

    static GroovyMBean registerMBeanFromMap(String regPolicy, Map metaMap) {
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
                mbean.addEventListeners metaMap.server, metaMap.listeners
            }
        }

        def gbean
        switch (regPolicy) {
            case "replace":
                if (metaMap.server.isRegistered(metaMap.jmxName)) {
                    metaMap.server.unregisterMBean metaMap.jmxName
                }
                metaMap.server.registerMBean(mbean, metaMap.jmxName)
                gbean = new GroovyMBean(metaMap.server, metaMap.jmxName)
                break
            case "ignore":
                if (metaMap.server.isRegistered(metaMap.jmxName))
                    break
            case "error":
            default:
                if (metaMap.server.isRegistered(metaMap.jmxName)) {
                    throw new JmxBuilderException("A Bean with name ${metaMap.jmxName} is already registered on the server.")
                } else {
                    metaMap.server.registerMBean(mbean, metaMap.jmxName)
                    gbean = new GroovyMBean(metaMap.server, metaMap.jmxName)
                }
        }

        gbean
    }

}