package groovy.jmx.builder

import java.lang.management.ManagementFactory
import javax.management.DynamicMBean
import javax.management.MBeanServerConnection
import javax.management.MBeanServerFactory
import javax.management.ObjectName

class JmxBuilderTools {
    static String DEFAULT_DOMAIN = "groovy.builder.jmx:"
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
            "char": java.lang.Integer.TYPE,
            "byte": java.lang.Byte.TYPE,
            "short": java.lang.Short.TYPE,
            "int": java.lang.Integer.TYPE,
            "long": java.lang.Long.TYPE,
            "float": java.lang.Float.TYPE,
            "double": java.lang.Double.TYPE,
            "boolean": java.lang.Boolean.TYPE
    ]
    static Map TYPE_MAP = [
            "object": java.lang.Object.class,
            "Object": java.lang.Object.class,
            "java.lang.Object": java.lang.Object.class,

            "string": java.lang.String.class,
            "String": java.lang.String.class,
            "java.lang.String": java.lang.String.class,

            "char": char.class,
            "character": java.lang.Character.class,
            "Character": java.lang.Character.class,
            "java.lang.Character": java.lang.Character.class,

            "byte": byte.class,
            "Byte": java.lang.Byte.class,
            "java.lang.Byte": java.lang.Byte.class,

            "short": short.class,
            "Short": java.lang.Short.class,
            "java.lang.Short": java.lang.Short.class,

            "int": int.class,
            "integer": java.lang.Integer.class,
            "Integer": java.lang.Integer.class,
            "java.lang.Integer": java.lang.Integer.class,

            "long": long.class,
            "Long": java.lang.Long.class,
            "java.lang.Long": java.lang.Long.class,

            "float": float.class,
            "Float": java.lang.Float.class,
            "java.lang.Float": java.lang.Float.class,

            "double": double.class,
            "Double": java.lang.Double.class,
            "java.lang.Double": java.lang.Double.class,

            "boolean": boolean.class,
            "Boolean": java.lang.Boolean.class,
            "java.lang.Boolean": java.lang.Boolean.class,

            "bigDec": java.math.BigDecimal.class,
            "bigDecimal": java.math.BigDecimal.class,
            "BigDecimal": java.math.BigDecimal.class,
            "java.math.BigDecimal": java.math.BigDecimal.class,

            "bigInt": java.math.BigInteger.class,
            "bigInteger": java.math.BigInteger.class,
            "BigInteger": java.math.BigInteger.class,
            "java.math.BigInteger": java.math.BigInteger.class,

            "date": java.util.Date.class,
            "java.util.Date": java.util.Date.class
    ]

    public static String capitalize(String value) {
        if (!value) return null
        if (value.length() == 1) return value.toUpperCase()
        return (value.length() > 1) ? value[0].toUpperCase() + value[1..-1] : value.toUpperCase()
    }

    public static String uncapitalize(String value) {
        if (!value) return null
        if (value.length() == 1) return value.toLowerCase()
        return (value.length() > 1) ? value[0].toLowerCase() + value[1..-1] : value.toLowerCase()
    }

    public static ObjectName getDefaultObjectName(def obj) {
        String name = DEFAULT_DOMAIN + "name=${obj.getClass().getName()},hashCode=${obj.hashCode()}"
        try {
            return new ObjectName(name)
        } catch (Exception ex) {
            throw new JmxBuilderException("Unable to create JMX ObjectName $name", ex)
        }
    }

    public static MBeanServerConnection getMBeanServer() {
        def servers = MBeanServerFactory.findMBeanServer(null);
        def server = servers.size() > 0 ? servers[0] : ManagementFactory.getPlatformMBeanServer()
        return server
    }


    public static Class[] getSignatureFromParamInfo(def params) {
        if (params == null || params.size() == 0) return null
        Object[] result = new Object[params.size()]
        params.eachWithIndex {param, i ->
            def type = TYPE_MAP.get(param.getType()) ?: Class.forName(param.getType()) ?: null
            result.putAt i, type
        }

        return result
    }

    public static String getNormalizedType(String type) {
        if (typeIsPrimitive(type))
            return PRIMITIVE_TYPES[type].name
        return TYPE_MAP[type]?.name ?: Class.forName(type)?.name ?: null
    }

    private static boolean typeIsPrimitive(String typeName) {
        PRIMITIVE_TYPES.containsKey(typeName)
    }

    public static boolean isClassMBean(Class cls) {
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
}