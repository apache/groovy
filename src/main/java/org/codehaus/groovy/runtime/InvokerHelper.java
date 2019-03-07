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
package org.codehaus.groovy.runtime;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Range;
import groovy.lang.Script;
import groovy.lang.SpreadMap;
import groovy.lang.SpreadMapEvaluatingException;
import groovy.lang.Tuple;
import groovy.lang.Writable;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.wrappers.PojoWrapper;
import org.w3c.dom.Element;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static helper class to make bytecode generation easier and act as a facade over the Invoker
 */
public class InvokerHelper {
    private static final Object[] EMPTY_MAIN_ARGS = new Object[]{new String[0]};

    public static final Object[] EMPTY_ARGS = {};
    protected static final Object[] EMPTY_ARGUMENTS = EMPTY_ARGS;
    protected static final Class[] EMPTY_TYPES = {};

    // heuristic size to pre-alocate stringbuffers for collections of items
    private static final int ITEM_ALLOCATE_SIZE = 5;

    public static final MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
    public static final String MAIN_METHOD_NAME = "main";

    public static void removeClass(Class clazz) {
        metaRegistry.removeMetaClass(clazz);
        ClassInfo.remove(clazz);
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
            Enumeration e = (Enumeration) value;
            List answer = new ArrayList();
            while (e.hasMoreElements()) {
                answer.add(e.nextElement());
            }
            return answer;
        }
        // let's assume its a collection of 1
        return Collections.singletonList(value);
    }

    public static String toString(Object arguments) {
        return format(arguments, false, -1, false);
    }

    public static String inspect(Object self) {
        return format(self, true);
    }

    public static Object getAttribute(Object object, String attribute) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

        if (object instanceof Class) {
            return metaRegistry.getMetaClass((Class) object).getAttribute(object, attribute);
        } else if (object instanceof GroovyObject) {
            return ((GroovyObject) object).getMetaClass().getAttribute(object, attribute);
        } else {
            return metaRegistry.getMetaClass(object.getClass()).getAttribute(object, attribute);
        }
    }

    public static void setAttribute(Object object, String attribute, Object newValue) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

        if (object instanceof Class) {
            metaRegistry.getMetaClass((Class) object).setAttribute(object, attribute, newValue);
        } else if (object instanceof GroovyObject) {
            ((GroovyObject) object).getMetaClass().setAttribute(object, attribute, newValue);
        } else {
            metaRegistry.getMetaClass(object.getClass()).setAttribute(object, attribute, newValue);
        }
    }

    public static Object getProperty(Object object, String property) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

        if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            return pogo.getProperty(property);
        } else if (object instanceof Class) {
            Class c = (Class) object;
            return metaRegistry.getMetaClass(c).getProperty(object, property);
        } else {
            return ((MetaClassRegistryImpl) metaRegistry).getMetaClass(object).getProperty(object, property);
        }
    }

    public static Object getPropertySafe(Object object, String property) {
        if (object != null) {
            return getProperty(object, property);
        }
        return null;
    }

    public static void setProperty(Object object, String property, Object newValue) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

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
            return -number;
        }
        if (value instanceof Long) {
            Long number = (Long) value;
            return -number;
        }
        if (value instanceof BigInteger) {
            return ((BigInteger) value).negate();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).negate();
        }
        if (value instanceof Double) {
            Double number = (Double) value;
            return -number;
        }
        if (value instanceof Float) {
            Float number = (Float) value;
            return -number;
        }
        if (value instanceof Short) {
            Short number = (Short) value;
            return (short) -number;
        }
        if (value instanceof Byte) {
            Byte number = (Byte) value;
            return (byte) -number;
        }
        if (value instanceof ArrayList) {
            // value is a list.
            List newlist = new ArrayList();
            for (Object o : ((ArrayList) value)) {
                newlist.add(unaryMinus(o));
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
                value instanceof Float ||
                value instanceof Short ||
                value instanceof Byte) {
            return value;
        }
        if (value instanceof ArrayList) {
            // value is a list.
            List newlist = new ArrayList();
            for (Object o : ((ArrayList) value)) {
                newlist.add(unaryPlus(o));
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
        return Pattern.compile(regexToCompareTo).matcher(stringToCompare);
    }

    /**
     * Find the right hand regex within the left hand string and return a matcher.
     *
     * @param left  string to compare
     * @param right regular expression to compare the string to
     */
    public static boolean matchRegex(Object left, Object right) {
        if (left == null || right == null) return false;
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
            for (Object key : ((Map) value).keySet()) {
                values[index++] = key;
                values[index++] = ((Map) value).get(key);
            }
            return new SpreadMap(values);
        }
        throw new SpreadMapEvaluatingException("Cannot spread the map " + typeName(value) + ", value " + value);
    }

    public static List createList(Object[] values) {
        List answer = new ArrayList(values.length);

        // GROOVY-8995: Improve the performance of creating list
        // answer.addAll(Arrays.asList(values));
        Collections.addAll(answer, values);

        return answer;
    }

    public static Map createMap(Object[] values) {
        Map answer = new LinkedHashMap(values.length / 2);
        int i = 0;
        while (i < values.length - 1) {
            if ((values[i] instanceof SpreadMap) && (values[i + 1] instanceof Map)) {
                Map smap = (Map) values[i + 1];
                for (Object e : smap.entrySet()) {
                    Map.Entry entry = (Map.Entry) e;
                    answer.put(entry.getKey(), entry.getValue());
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
            throw new PowerAssertionError(expression.toString());
        }
        throw new AssertionError(String.valueOf(message) + ". Expression: " + expression);
    }

    public static Object runScript(Class scriptClass, String[] args) {
        Binding context = new Binding(args);
        Script script = createScript(scriptClass, context);
        return invokeMethod(script, "run", EMPTY_ARGS);
    }

    static class NullScript extends Script {
        public NullScript() { this(new Binding()); }
        public NullScript(Binding context) { super(context); }
        public Object run() { return null; }
    }

    public static Script createScript(Class scriptClass, Binding context) {
        Script script;

        if (scriptClass == null) {
            script = new NullScript(context);
        } else {
            try {
                if (Script.class.isAssignableFrom(scriptClass)) {
                    script = newScript(scriptClass, context);
                } else {
                    final GroovyObject object = (GroovyObject) scriptClass.newInstance();
                    // it could just be a class, so let's wrap it in a Script
                    // wrapper; though the bindings will be ignored
                    script = new Script(context) {
                        public Object run() {
                            Object argsToPass = EMPTY_MAIN_ARGS;
                            try {
                                Object args = getProperty("args");
                                if (args instanceof String[]) {
                                    argsToPass = args;
                                }
                            } catch (MissingPropertyException e) {
                                // They'll get empty args since none exist in the context.
                            }
                            object.invokeMethod(MAIN_METHOD_NAME, argsToPass);
                            return null;
                        }
                    };
                    Map variables = context.getVariables();
                    MetaClass mc = getMetaClass(object);
                    for (Object o : variables.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        String key = entry.getKey().toString();
                        // assume underscore variables are for the wrapper script
                        setPropertySafe(key.startsWith("_") ? script : object, mc, key, entry.getValue());
                    }
                }
            } catch (Exception e) {
                throw new GroovyRuntimeException(
                        "Failed to create Script instance for class: "
                                + scriptClass + ". Reason: " + e, e);
            }
        }
        return script;
    }

    public static Script newScript(Class<?> scriptClass, Binding context) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Script script;
        try {
            Constructor constructor = scriptClass.getConstructor(Binding.class);
            script = (Script) constructor.newInstance(context);
        } catch (NoSuchMethodException e) {
            // Fallback for non-standard "Script" classes.
            script = (Script) scriptClass.newInstance();
            script.setBinding(context);
        }
        return script;
    }

    /**
     * Sets the properties on the given object
     */
    public static void setProperties(Object object, Map map) {
        MetaClass mc = getMetaClass(object);
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            setPropertySafe(object, mc, key, value);
        }
    }

    private static void setPropertySafe(Object object, MetaClass mc, String key, Object value) {
        try {
            mc.setProperty(object, key, value);
        } catch (MissingPropertyException mpe) {
            // Ignore
        } catch (InvokerInvocationException iie) {
            // GROOVY-5802 IAE for missing properties with classes that extend List
            Throwable cause = iie.getCause();
            if (!(cause instanceof IllegalArgumentException)) throw iie;
        }
    }

    /**
     * Writes an object to a Writer using Groovy's default representation for the object.
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

            try (Reader r = reader) {
                char[] chars = new char[8192];
                for (int i; (i = r.read(chars)) != -1; ) {
                    out.write(chars, 0, i);
                }
            }
        } else {
            out.write(toString(object));
        }
    }

    /**
     * Appends an object to an Appendable using Groovy's default representation for the object.
     */
    public static void append(Appendable out, Object object) throws IOException {
        if (object instanceof String) {
            out.append((String) object);
        } else if (object instanceof Object[]) {
            out.append(toArrayString((Object[]) object));
        } else if (object instanceof Map) {
            out.append(toMapString((Map) object));
        } else if (object instanceof Collection) {
            out.append(toListString((Collection) object));
        } else if (object instanceof Writable) {
            Writable writable = (Writable) object;
            Writer stringWriter = new StringBuilderWriter();
            writable.writeTo(stringWriter);
            out.append(stringWriter.toString());
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
                for (int j = 0; j < i; j++) {
                    out.append(chars[j]);
                }
            }
            reader.close();
        } else {
            out.append(toString(object));
        }
    }

    @SuppressWarnings("unchecked")
    public static Iterator<Object> asIterator(Object o) {
        return (Iterator) invokeMethod(o, "iterator", EMPTY_ARGS);
    }

    protected static String format(Object arguments, boolean verbose) {
        return format(arguments, verbose, -1);
    }

    public static String format(Object arguments, boolean verbose, int maxSize) {
        return format(arguments, verbose, maxSize, false);
    }

    public static String format(Object arguments, boolean verbose, int maxSize, boolean safe) {
        if (arguments == null) {
            final NullObject nullObject = NullObject.getNullObject();
            return (String) nullObject.getMetaClass().invokeMethod(nullObject, "toString", EMPTY_ARGS);
        }
        if (arguments.getClass().isArray()) {
            if (arguments instanceof Object[]) {
                return toArrayString((Object[]) arguments, verbose, maxSize, safe);
            }
            if (arguments instanceof char[]) {
                return new String((char[]) arguments);
            }
            // other primitives
            return formatCollection(DefaultTypeTransformation.arrayAsCollection(arguments), verbose, maxSize, safe);
        }
        if (arguments instanceof Range) {
            Range range = (Range) arguments;
            try {
                if (verbose) {
                    return range.inspect();
                } else {
                    return range.toString();
                }
            } catch (RuntimeException ex) {
                if (!safe) throw ex;
                return handleFormattingException(arguments, ex);
            } catch (Exception ex) {
                if (!safe) throw new GroovyRuntimeException(ex);
                return handleFormattingException(arguments, ex);
            }
        }
        if (arguments instanceof Collection) {
            return formatCollection((Collection) arguments, verbose, maxSize, safe);
        }
        if (arguments instanceof Map) {
            return formatMap((Map) arguments, verbose, maxSize, safe);
        }
        if (arguments instanceof Element) {
            try {
                Method serialize = Class.forName("groovy.xml.XmlUtil").getMethod("serialize", Element.class);
                return (String) serialize.invoke(null, arguments);
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        if (arguments instanceof String) {
            if (verbose) {
                String arg = escapeBackslashes((String) arguments)
                        .replaceAll("'", "\\\\'");    // single quotation mark
                return "\'" + arg + "\'";
            } else {
                return (String) arguments;
            }
        }
        try {
            // TODO: For GROOVY-2599 do we need something like below but it breaks other things
//            return (String) invokeMethod(arguments, "toString", EMPTY_ARGS);
            return arguments.toString();
        } catch (RuntimeException ex) {
            if (!safe) throw ex;
            return handleFormattingException(arguments, ex);
        } catch (Exception ex) {
            if (!safe) throw new GroovyRuntimeException(ex);
            return handleFormattingException(arguments, ex);
        }
    }

    public static String escapeBackslashes(String orig) {
        // must replace backslashes first, as the other replacements add backslashes not to be escaped
        return orig
                .replace("\\", "\\\\")           // backslash
                .replace("\n", "\\n")            // line feed
                .replaceAll("\\r", "\\\\r")      // carriage return
                .replaceAll("\\t", "\\\\t")      // tab
                .replaceAll("\\f", "\\\\f");     // form feed
    }

    private static String handleFormattingException(Object item, Exception ex) {

        String hash;
        try {
            hash = Integer.toHexString(item.hashCode());
        } catch (Exception ignored) {
            hash = "????";
        }
        return "<" + typeName(item) + "@" + hash + ">";
    }

    private static String formatMap(Map map, boolean verbose, int maxSize, boolean safe) {
        if (map.isEmpty()) {
            return "[:]";
        }
        StringBuilder buffer = new StringBuilder(ITEM_ALLOCATE_SIZE * map.size() * 2);
        buffer.append('[');
        boolean first = true;
        for (Object o : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            if (maxSize != -1 && buffer.length() > maxSize) {
                buffer.append("...");
                break;
            }
            Map.Entry entry = (Map.Entry) o;
            if (entry.getKey() == map) {
                buffer.append("(this Map)");
            } else {
                buffer.append(format(entry.getKey(), verbose, sizeLeft(maxSize, buffer), safe));
            }
            buffer.append(":");
            if (entry.getValue() == map) {
                buffer.append("(this Map)");
            } else {
                buffer.append(format(entry.getValue(), verbose, sizeLeft(maxSize, buffer), safe));
            }
        }
        buffer.append(']');
        return buffer.toString();
    }

    private static int sizeLeft(int maxSize, StringBuilder buffer) {
        return maxSize == -1 ? maxSize : Math.max(0, maxSize - buffer.length());
    }

    private static String formatCollection(Collection collection, boolean verbose, int maxSize, boolean safe) {
        StringBuilder buffer = new StringBuilder(ITEM_ALLOCATE_SIZE * collection.size());
        buffer.append('[');
        boolean first = true;
        for (Object item : collection) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            if (maxSize != -1 && buffer.length() > maxSize) {
                buffer.append("...");
                break;
            }
            if (item == collection) {
                buffer.append("(this Collection)");
            } else {
                buffer.append(format(item, verbose, sizeLeft(maxSize, buffer), safe));
            }
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * A helper method to format the arguments types as a comma-separated list.
     *
     * @param arguments the type to process
     * @return the string representation of the type
     */
    public static String toTypeString(Object[] arguments) {
        return toTypeString(arguments, -1);
    }

    /**
     * A helper method to format the arguments types as a comma-separated list.
     *
     * @param arguments the type to process
     * @param maxSize   stop after approximately this many characters and append '...', -1 means don't stop
     * @return the string representation of the type
     */
    public static String toTypeString(Object[] arguments, int maxSize) {
        if (arguments == null) {
            return "null";
        }
        StringBuilder argBuf = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (maxSize != -1 && argBuf.length() > maxSize) {
                argBuf.append("...");
                break;
            } else {
                if (i > 0) {
                    argBuf.append(", ");
                }
                argBuf.append(arguments[i] != null ? typeName(arguments[i]) : "null");
            }
        }
        return argBuf.toString();
    }

    private static Set<String> DEFAULT_IMPORT_PKGS = new HashSet<String>();
    private static Set<String> DEFAULT_IMPORT_CLASSES = new HashSet<String>();
    static {
        for (String pkgName : ResolveVisitor.DEFAULT_IMPORTS) {
            DEFAULT_IMPORT_PKGS.add(pkgName.substring(0, pkgName.length() - 1));
        }
        DEFAULT_IMPORT_CLASSES.add("java.math.BigDecimal");
        DEFAULT_IMPORT_CLASSES.add("java.math.BigInteger");
    }
    /**
     * Gets the type name
     *
     * @param argument the object to find the type for
     * @return the type name (slightly pretty format taking into account default imports)
     */
    private static String typeName(Object argument) {
        Class<?> aClass = argument.getClass();
        String pkgName = aClass.getPackage() == null ? "" : aClass.getPackage().getName();
        boolean useShort = DEFAULT_IMPORT_PKGS.contains(pkgName) || DEFAULT_IMPORT_CLASSES.contains(aClass.getName());
        return useShort ? aClass.getSimpleName() : aClass.getName();
    }

    /**
     * A helper method to return the string representation of a map with bracket boundaries "[" and "]".
     *
     * @param arg the map to process
     * @return the string representation of the map
     */
    public static String toMapString(Map arg) {
        return toMapString(arg, -1);
    }

    /**
     * A helper method to return the string representation of a map with bracket boundaries "[" and "]".
     *
     * @param arg     the map to process
     * @param maxSize stop after approximately this many characters and append '...', -1 means don't stop
     * @return the string representation of the map
     */
    public static String toMapString(Map arg, int maxSize) {
        return formatMap(arg, false, maxSize, false);
    }

    /**
     * A helper method to return the string representation of a list with bracket boundaries "[" and "]".
     *
     * @param arg the collection to process
     * @return the string representation of the collection
     */
    public static String toListString(Collection arg) {
        return toListString(arg, -1);
    }

    /**
     * A helper method to return the string representation of a list with bracket boundaries "[" and "]".
     *
     * @param arg     the collection to process
     * @param maxSize stop after approximately this many characters and append '...'
     * @return the string representation of the collection
     */
    public static String toListString(Collection arg, int maxSize) {
        return toListString(arg, maxSize, false);
    }

    /**
     * A helper method to return the string representation of a list with bracket boundaries "[" and "]".
     *
     * @param arg     the collection to process
     * @param maxSize stop after approximately this many characters and append '...', -1 means don't stop
     * @param safe    whether to use a default object representation for any item in the collection if an exception occurs when generating its toString
     * @return the string representation of the collection
     */
    public static String toListString(Collection arg, int maxSize, boolean safe) {
        return formatCollection(arg, false, maxSize, safe);
    }

    /**
     * A helper method to return the string representation of an array of objects
     * with brace boundaries "[" and "]".
     *
     * @param arguments the array to process
     * @return the string representation of the array
     */
    public static String toArrayString(Object[] arguments) {
        return toArrayString(arguments, false, -1, false);
    }

    private static String toArrayString(Object[] array, boolean verbose, int maxSize, boolean safe) {
        if (array == null) {
            return "null";
        }
        boolean first = true;
        StringBuilder argBuf = new StringBuilder(array.length);
        argBuf.append('[');

        for (Object item : array) {
            if (first) {
                first = false;
            } else {
                argBuf.append(", ");
            }
            if (maxSize != -1 && argBuf.length() > maxSize) {
                argBuf.append("...");
                break;
            }
            if (item == array) {
                argBuf.append("(this array)");
            } else {
                argBuf.append(format(item, verbose, sizeLeft(maxSize, argBuf), safe));
            }
        }
        argBuf.append(']');
        return argBuf.toString();
    }

    /**
     * A helper method to return the string representation of an array of objects
     * with brace boundaries "[" and "]".
     *
     * @param arguments the array to process
     * @param maxSize   stop after approximately this many characters and append '...'
     * @param safe      whether to use a default object representation for any item in the array if an exception occurs when generating its toString
     * @return the string representation of the array
     */
    public static String toArrayString(Object[] arguments, int maxSize, boolean safe) {
        return toArrayString(arguments, false, maxSize, safe);
    }

    public static List createRange(Object from, Object to, boolean inclusive) {
        try {
            return ScriptBytecodeAdapter.createRange(from, to, inclusive);
        } catch (RuntimeException | Error re) {
            throw re;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static Object bitwiseNegate(Object value) {
        if (value instanceof Integer) {
            Integer number = (Integer) value;
            return ~number;
        }
        if (value instanceof Long) {
            Long number = (Long) value;
            return ~number;
        }
        if (value instanceof BigInteger) {
            return ((BigInteger) value).not();
        }
        if (value instanceof String) {
            // value is a regular expression.
            return StringGroovyMethods.bitwiseNegate((CharSequence)value.toString());
        }
        if (value instanceof GString) {
            // value is a regular expression.
            return StringGroovyMethods.bitwiseNegate((CharSequence)value.toString());
        }
        if (value instanceof ArrayList) {
            // value is a list.
            List newlist = new ArrayList();
            for (Object o : ((ArrayList) value)) {
                newlist.add(bitwiseNegate(o));
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
            return (Object[]) arguments;
        }
        return new Object[]{arguments};
    }

    public static Object[] asUnwrappedArray(Object arguments) {

        Object[] args = asArray(arguments);

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof PojoWrapper) {
                args[i] = ((PojoWrapper) args[i]).unwrap();
            }
        }

        return args;
    }
}
