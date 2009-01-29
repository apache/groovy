/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.runtime;

import groovy.lang.*;
import groovy.xml.XmlUtil;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.wrappers.PojoWrapper;
import org.w3c.dom.Element;

import java.beans.Introspector;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static helper class to make bytecode generation easier and act as a facade over the Invoker
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InvokerHelper {
    private   static final Object[] EMPTY_MAIN_ARGS = new Object[]{new String[0]};

    public    static final Object[] EMPTY_ARGS = {};
    protected static final Object[] EMPTY_ARGUMENTS = EMPTY_ARGS;
    protected static final Class[]  EMPTY_TYPES = {};

    public static final MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();

    public static void removeClass(Class clazz) {
        metaRegistry.removeMetaClass(clazz);
        Introspector.flushFromCaches(clazz);
    }

    public static Object invokeMethodSafe(Object object, String methodName, Object arguments) {
        if (object != null) {
            return invokeMethod(object, methodName, arguments);
        }
        return null;
    }

    public static Object invokeStaticMethod(String klass, String methodName, Object arguments) throws ClassNotFoundException {
        Class type = Class.forName(klass);
        return invokeStaticMethod(type, methodName, arguments);
    }


    public static Object invokeStaticNoArgumentsMethod(Class type, String methodName) {
        return invokeStaticMethod(type, methodName, EMPTY_ARGS);
    }

    public static Object invokeConstructorOf(String klass, Object arguments) throws ClassNotFoundException {
        Class type = Class.forName(klass);
        return invokeConstructorOf(type, arguments);
    }

    public static Object invokeNoArgumentsConstructorOf(Class type) {
        return invokeConstructorOf(type, EMPTY_ARGS);
    }

    public static Object invokeClosure(Object closure, Object arguments) {
        return invokeMethod(closure, "doCall", arguments);
    }

    public static List asList(Object value) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }
        if (value instanceof List) {
            return (List) value;
        }
        if (value.getClass().isArray()) {
            return Arrays.asList((Object[]) value);
        }
        if (value instanceof Enumeration) {
            List answer = new ArrayList();
            for (Enumeration e = (Enumeration) value; e.hasMoreElements();) {
                answer.add(e.nextElement());
            }
            return answer;
        }
        // lets assume its a collection of 1
        return Collections.singletonList(value);
    }

    public static String toString(Object arguments) {
        if (arguments instanceof Object[])
            return toArrayString((Object[]) arguments);
        if (arguments instanceof Collection)
            return toListString((Collection) arguments);
        if (arguments instanceof Map)
            return toMapString((Map) arguments);
        return format(arguments, false);
    }

    public static String inspect(Object self) {
        return format(self, true);
    }

    public static Object getAttribute(Object object, String attribute) {
//        if (object == null) {
//            throw new NullPointerException("Cannot get attribute: " + attribute + " on null object");
//        }

        if (object instanceof Class) {
            return metaRegistry.getMetaClass((Class) object).getAttribute(object, attribute);
        } else if (object instanceof GroovyObject) {
            return ((GroovyObject) object).getMetaClass().getAttribute(object, attribute);
        } else {
            return metaRegistry.getMetaClass(object.getClass()).getAttribute(object, attribute);
        }
    }

    public static void setAttribute(Object object, String attribute, Object newValue) {
//        if (object == null) {
//            throw new GroovyRuntimeException("Cannot set attribute on null object");
//        }

        if (object instanceof Class) {
            metaRegistry.getMetaClass((Class) object).setAttribute(object, attribute, newValue);
        } else if (object instanceof GroovyObject) {
            ((GroovyObject) object).getMetaClass().setAttribute(object, attribute, newValue);
        } else {
            metaRegistry.getMetaClass(object.getClass()).setAttribute(object, attribute, newValue);
        }
    }

    public static Object getProperty(Object object, String property) {
//        if (object == null) {
//            throw new NullPointerException("Cannot get property: " + property + " on null object");
//        }
        if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            return pogo.getProperty(property);
        } else if (object instanceof Class) {
            Class c = (Class) object;
            return metaRegistry.getMetaClass(c).getProperty(object, property);
        } else {
            return ((MetaClassRegistryImpl)metaRegistry).getMetaClass(object).getProperty(object, property);
        }
    }

    public static Object getPropertySafe(Object object, String property) {
        if (object != null) {
            return getProperty(object, property);
        }
        return null;
    }

    public static void setProperty(Object object, String property, Object newValue) {
//        if (object == null) {
//            throw new GroovyRuntimeException("Cannot set property on null object");
//        }
        if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            pogo.setProperty(property, newValue);
        } else if (object instanceof Class) {
            metaRegistry.getMetaClass((Class) object).setProperty((Class) object, property, newValue);
        } else {
            ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(object).setProperty(object, property, newValue);
        }
    }

    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    public static void setProperty2(Object newValue, Object object, String property) {
        setProperty(object, property, newValue);
    }


    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    public static void setGroovyObjectProperty(Object newValue, GroovyObject object, String property) {
        object.setProperty(property, newValue);
    }

    public static Object getGroovyObjectProperty(GroovyObject object, String property) {
        return object.getProperty(property);
    }


    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    public static void setPropertySafe2(Object newValue, Object object, String property) {
        if (object != null) {
            setProperty2(newValue, object, property);
        }
    }

    /**
     * Returns the method pointer for the given object name
     */
    public static Closure getMethodPointer(Object object, String methodName) {
        if (object == null) {
            throw new NullPointerException("Cannot access method pointer for '" + methodName + "' on null object");
        }
        return new MethodClosure(object, methodName);
    }

    public static Object unaryMinus(Object value) {
        if (value instanceof Integer) {
            Integer number = (Integer) value;
            return Integer.valueOf(-number.intValue());
        }
        if (value instanceof Long) {
            Long number = (Long) value;
            return new Long(-number.longValue());
        }
        if (value instanceof BigInteger) {
            return ((BigInteger) value).negate();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).negate();
        }
        if (value instanceof Double) {
            Double number = (Double) value;
            return new Double(-number.doubleValue());
        }
        if (value instanceof Float) {
            Float number = (Float) value;
            return new Float(-number.floatValue());
        }
        if (value instanceof ArrayList) {
            // value is an list.
            List newlist = new ArrayList();
            Iterator it = ((ArrayList) value).iterator();
            for (; it.hasNext();) {
                newlist.add(unaryMinus(it.next()));
            }
            return newlist;
        }
        return invokeMethod(value, "negative", EMPTY_ARGS);
    }

    public static Object unaryPlus(Object value) {
        if (value instanceof Integer ||
                value instanceof Long ||
                value instanceof BigInteger ||
                value instanceof BigDecimal ||
                value instanceof Double ||
                value instanceof Float) {
            return value;
        }
        if (value instanceof ArrayList) {
            // value is an list.
            List newlist = new ArrayList();
            Iterator it = ((ArrayList) value).iterator();
            for (; it.hasNext();) {
                newlist.add(unaryPlus(it.next()));
            }
            return newlist;
        }
        return invokeMethod(value, "positive", EMPTY_ARGS);
    }

    /**
     * Find the right hand regex within the left hand string and return a matcher.
     *
     * @param left  string to compare
     * @param right regular expression to compare the string to
     */
    public static Matcher findRegex(Object left, Object right) {
        String stringToCompare;
        if (left instanceof String) {
            stringToCompare = (String) left;
        } else {
            stringToCompare = toString(left);
        }
        String regexToCompareTo;
        if (right instanceof String) {
            regexToCompareTo = (String) right;
        } else if (right instanceof Pattern) {
            Pattern pattern = (Pattern) right;
            return pattern.matcher(stringToCompare);
        } else {
            regexToCompareTo = toString(right);
        }
        Matcher matcher = Pattern.compile(regexToCompareTo).matcher(stringToCompare);
        return matcher;
    }


    /**
     * Find the right hand regex within the left hand string and return a matcher.
     *
     * @param left  string to compare
     * @param right regular expression to compare the string to
     */
    public static boolean matchRegex(Object left, Object right) {
        Pattern pattern;
        if (right instanceof Pattern) {
            pattern = (Pattern) right;
        } else {
            pattern = Pattern.compile(toString(right));
        }
        String stringToCompare = toString(left);
        Matcher matcher = pattern.matcher(stringToCompare);
        RegexSupport.setLastMatcher(matcher);
        return matcher.matches();
    }

    public static Tuple createTuple(Object[] array) {
        return new Tuple(array);
    }

    public static SpreadMap spreadMap(Object value) {
        if (value instanceof Map) {
            Object[] values = new Object[((Map) value).keySet().size() * 2];
            int index = 0;
            Iterator it = ((Map) value).keySet().iterator();
            for (; it.hasNext();) {
                Object key = it.next();
                values[index++] = key;
                values[index++] = ((Map) value).get(key);
            }
            return new SpreadMap(values);
        }
        throw new SpreadMapEvaluatingException("Cannot spread the map " + value.getClass().getName() + ", value " + value);
    }

    public static List createList(Object[] values) {
        List answer = new ArrayList(values.length);
        answer.addAll(Arrays.asList(values));
        return answer;
    }

    public static Map createMap(Object[] values) {
        Map answer = new LinkedHashMap(values.length / 2);
        int i = 0;
        while (i < values.length - 1) {
            if ((values[i] instanceof SpreadMap) && (values[i + 1] instanceof Map)) {
                Map smap = (Map) values[i + 1];
                Iterator iter = smap.keySet().iterator();
                for (; iter.hasNext();) {
                    Object key = iter.next();
                    answer.put(key, smap.get(key));
                }
                i += 2;
            } else {
                answer.put(values[i++], values[i++]);
            }
        }
        return answer;
    }

    public static void assertFailed(Object expression, Object message) {
        if (message == null || "".equals(message)) {
            throw new AssertionError("Expression: " + expression);
        }
        throw new AssertionError(String.valueOf(message) + ". Expression: " + expression);
    }

    public static Object runScript(Class scriptClass, String[] args) {
        Binding context = new Binding(args);
        Script script = createScript(scriptClass, context);
        return invokeMethod(script, "run", EMPTY_ARGS);
    }

    public static Script createScript(Class scriptClass, Binding context) {
        Script script = null;
        // for empty scripts
        if (scriptClass == null) {
            script = new Script() {
                public Object run() {
                    return null;
                }
            };
        } else {
            try {
                final GroovyObject object = (GroovyObject) scriptClass
                        .newInstance();
                if (object instanceof Script) {
                    script = (Script) object;
                } else {
                    // it could just be a class, so lets wrap it in a Script
                    // wrapper
                    // though the bindings will be ignored
                    script = new Script() {
                        public Object run() {
                            Object args = getBinding().getVariables().get("args");
                            Object argsToPass = EMPTY_MAIN_ARGS;
                            if(args != null && args instanceof String[]) {
                                argsToPass = args;
                            }
                            object.invokeMethod("main", argsToPass);
                            return null;
                        }
                    };
                    setProperties(object, context.getVariables());
                }
            } catch (Exception e) {
                throw new GroovyRuntimeException(
                        "Failed to create Script instance for class: "
                                + scriptClass + ". Reason: " + e, e);
            }
        }
        script.setBinding(context);
        return script;
    }

    /**
     * Sets the properties on the given object
     */
    public static void setProperties(Object object, Map map) {
        MetaClass mc = getMetaClass(object);
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = entry.getKey().toString();

            Object value = entry.getValue();
            try {
                mc.setProperty(object, key, value);
            } catch (MissingPropertyException mpe) {
                // Ignore
            }
        }
    }

    public static String getVersion() {
        String version = null;
        Package p = Package.getPackage("groovy.lang");
        if (p != null) {
            version = p.getImplementationVersion();
        }
        if (version == null) {
            version = "";
        }
        return version;
    }

    /**
     * Writes the given object to the given stream
     */
    public static void write(Writer out, Object object) throws IOException {
        if (object instanceof String) {
            out.write((String) object);
        } else if (object instanceof Object[]) {
            out.write(toArrayString((Object[]) object));
        } else if (object instanceof Map) {
            out.write(toMapString((Map) object));
        } else if (object instanceof Collection) {
            out.write(toListString((Collection) object));
        } else if (object instanceof Writable) {
            Writable writable = (Writable) object;
            writable.writeTo(out);
        } else if (object instanceof InputStream || object instanceof Reader) {
            // Copy stream to stream
            Reader reader;
            if (object instanceof InputStream) {
                reader = new InputStreamReader((InputStream) object);
            } else {
                reader = (Reader) object;
            }
            char[] chars = new char[8192];
            int i;
            while ((i = reader.read(chars)) != -1) {
                out.write(chars, 0, i);
            }
            reader.close();
        } else {
            out.write(toString(object));
        }
    }

    public static Iterator asIterator(Object o) {
        return (Iterator) invokeMethod(o, "iterator", EMPTY_ARGS);
    }

    protected static String format(Object arguments, boolean verbose) {
        if (arguments == null) {
            final NullObject nullObject = NullObject.getNullObject();
            return (String) nullObject.getMetaClass().invokeMethod(nullObject, "toString", EMPTY_ARGS);
        }
        if (arguments.getClass().isArray()) {
            if (arguments instanceof char[]) {
                return new String((char[]) arguments);
            }
            return format(DefaultTypeTransformation.asCollection(arguments), verbose);
        }
        if (arguments instanceof Range) {
            Range range = (Range) arguments;
            if (verbose) {
                return range.inspect();
            } else {
                return range.toString();
            }
        }
        if (arguments instanceof Collection) {
            return formatList((Collection) arguments, verbose);
        }
        if (arguments instanceof Map) {
            return formatMap((Map) arguments, verbose);
        }
        if (arguments instanceof Element) {
            return XmlUtil.serialize((Element) arguments);
        }
        if (arguments instanceof String) {
            if (verbose) {
                String arg = ((String) arguments).replaceAll("\\n", "\\\\n");    // line feed
                arg = arg.replaceAll("\\r", "\\\\r");      // carriage return
                arg = arg.replaceAll("\\t", "\\\\t");      // tab
                arg = arg.replaceAll("\\f", "\\\\f");      // form feed
                arg = arg.replaceAll("\\\"", "\\\\\"");    // double quotation mark
                arg = arg.replaceAll("\\\\", "\\\\");      // backslash
                return "\"" + arg + "\"";
            } else {
                return (String) arguments;
            }
        }
        // TODO: For GROOVY-2599 do we need something like below but it breaks other things
//        return (String) invokeMethod(arguments, "toString", EMPTY_ARGS);
        return arguments.toString();
    }

    private static String formatMap(Map map, boolean verbose) {
        if (map.isEmpty()) {
            return "[:]";
        }
        StringBuffer buffer = new StringBuffer("[");
        boolean first = true;
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            Map.Entry entry = (Map.Entry) iter.next();
            buffer.append(format(entry.getKey(), verbose));
            buffer.append(":");
            if (entry.getValue() == map) {
                buffer.append("this Map_");
            } else {
                buffer.append(format(entry.getValue(), verbose));
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    private static String formatList(Collection list, boolean verbose) {
        StringBuffer buffer = new StringBuffer("[");
        boolean first = true;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(format(iter.next(), verbose));
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * A helper method to format the arguments types as a comma-separated list.
     *
     * @param arguments the type to process
     * @return the string representation of the type
     */
    public static String toTypeString(Object[] arguments) {
        if (arguments == null) {
            return "null";
        }
        StringBuffer argBuf = new StringBuffer();
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                argBuf.append(", ");
            }
            argBuf.append(arguments[i] != null ? arguments[i].getClass().getName() : "null");
        }
        return argBuf.toString();
    }

    /**
     * A helper method to return the string representation of a map with bracket boundaries "[" and "]".
     *
     * @param arg the map to process
     * @return the string representation of the map
     */
    public static String toMapString(Map arg) {
        return formatMap(arg, false);
    }

    /**
     * A helper method to return the string representation of a list with bracket boundaries "[" and "]".
     *
     * @param arg the collection to process
     * @return the string representation of the collection
     */
    public static String toListString(Collection arg) {
        return formatList(arg, false);
    }

    /**
     * A helper method to return the string representation of an array of objects
     * with brace boundaries "{" and "}".
     *
     * @param arguments the array to process
     * @return the string representation of the array
     */
    public static String toArrayString(Object[] arguments) {
        if (arguments == null) {
            return "null";
        }
        String sbdry = "[";
        String ebdry = "]";
        StringBuffer argBuf = new StringBuffer(sbdry);
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                argBuf.append(", ");
            }
            argBuf.append(format(arguments[i], false));
        }
        argBuf.append(ebdry);
        return argBuf.toString();
    }

    public static List createRange(Object from, Object to, boolean inclusive) {
        try {
            return ScriptBytecodeAdapter.createRange(from, to, inclusive);
        } catch (RuntimeException re) {
            throw re;
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static Object bitwiseNegate(Object value) {
        if (value instanceof Integer) {
            Integer number = (Integer) value;
            return Integer.valueOf(~number.intValue());
        }
        if (value instanceof Long) {
            Long number = (Long) value;
            return new Long(~number.longValue());
        }
        if (value instanceof BigInteger) {
            return ((BigInteger) value).not();
        }
        if (value instanceof String) {
            // value is a regular expression.
            return DefaultGroovyMethods.bitwiseNegate(value.toString());
        }
        if (value instanceof GString) {
            // value is a regular expression.
            return DefaultGroovyMethods.bitwiseNegate(value.toString());
        }
        if (value instanceof ArrayList) {
            // value is an list.
            List newlist = new ArrayList();
            Iterator it = ((ArrayList) value).iterator();
            for (; it.hasNext();) {
                newlist.add(bitwiseNegate(it.next()));
            }
            return newlist;
        }
        return invokeMethod(value, "bitwiseNegate", EMPTY_ARGS);
    }

    public static MetaClassRegistry getMetaRegistry() {
        return metaRegistry;
    }

    public static MetaClass getMetaClass(Object object) {
        if (object instanceof GroovyObject)
            return ((GroovyObject) object).getMetaClass();
        else
            return ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(object);
    }

    public static MetaClass getMetaClass(Class cls) {
        return metaRegistry.getMetaClass(cls);
    }

    /**
     * Invokes the given method on the object.
     */
    public static Object invokeMethod(Object object, String methodName, Object arguments) {
        if (object == null) {
            object = NullObject.getNullObject();
            //throw new NullPointerException("Cannot invoke method " + methodName + "() on null object");
        }

        // if the object is a Class, call a static method from that class
        if (object instanceof Class) {
            Class theClass = (Class) object;
            MetaClass metaClass = metaRegistry.getMetaClass(theClass);
            return metaClass.invokeStaticMethod(object, methodName, asArray(arguments));
        }

        // it's an instance; check if it's a Java one
        if (!(object instanceof GroovyObject)) {
            return invokePojoMethod(object, methodName, arguments);
        }

        // a groovy instance (including builder, closure, ...)
        return invokePogoMethod(object, methodName, arguments);
    }

    static Object invokePojoMethod(Object object, String methodName, Object arguments) {
        MetaClass metaClass = InvokerHelper.getMetaClass(object);
        return metaClass.invokeMethod(object, methodName, asArray(arguments));
    }

    static Object invokePogoMethod(Object object, String methodName, Object arguments) {
        GroovyObject groovy = (GroovyObject) object;
        boolean intercepting = groovy instanceof GroovyInterceptable;
        try {
            // if it's a pure interceptable object (even intercepting toString(), clone(), ...)
            if (intercepting) {
                return groovy.invokeMethod(methodName, asUnwrappedArray(arguments));
            }
            //else try a statically typed method or a GDK method
            return groovy.getMetaClass().invokeMethod(object, methodName, asArray(arguments));
        } catch (MissingMethodException e) {
            if (e instanceof MissingMethodExecutionFailed) {
                throw (MissingMethodException) e.getCause();
            } else if (!intercepting && e.getMethod().equals(methodName) && object.getClass() == e.getType()) {
                // in case there's nothing else, invoke the object's own invokeMethod()
                return groovy.invokeMethod(methodName, asUnwrappedArray(arguments));
            } else {
                throw e;
            }
        }
    }

    public static Object invokeSuperMethod(Object object, String methodName, Object arguments) {
        if (object == null) {
            throw new NullPointerException("Cannot invoke method " + methodName + "() on null object");
        }

        Class theClass = object.getClass();

        MetaClass metaClass = metaRegistry.getMetaClass(theClass.getSuperclass());
        return metaClass.invokeMethod(object, methodName, asArray(arguments));
    }

    public static Object invokeStaticMethod(Class type, String method, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(type);
        return metaClass.invokeStaticMethod(type, method, asArray(arguments));
    }

    public static Object invokeConstructorOf(Class type, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(type);
        return metaClass.invokeConstructor(asArray(arguments));
    }

    /**
     * Converts the given object into an array; if its an array then just
     * cast otherwise wrap it in an array
     */
    public static Object[] asArray(Object arguments) {

    	if (arguments == null) {
    		return EMPTY_ARGUMENTS;
    	}
    	if (arguments instanceof Object[]) {
    		return  (Object[]) arguments;
    	}
    	return new Object[]{arguments};
    }

    public static Object[] asUnwrappedArray(Object arguments) {

        Object[] args = asArray(arguments);

        for (int i=0; i<args.length; i++) {
            if (args[i] instanceof PojoWrapper) {
                args[i] = ((PojoWrapper)args[i]).unwrap();
            }
        }

        return args;
    }
}
