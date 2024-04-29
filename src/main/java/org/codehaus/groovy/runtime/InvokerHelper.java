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
import groovy.lang.Script;
import groovy.lang.SpreadMap;
import groovy.lang.SpreadMapEvaluatingException;
import groovy.lang.Tuple;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;
import org.codehaus.groovy.runtime.wrappers.PojoWrapper;

import java.beans.Introspector;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static helper class to make bytecode generation easier and act as a facade over the Invoker
 */
public class InvokerHelper {
    public static final Object[] EMPTY_ARGS = {};
    protected static final Object[] EMPTY_ARGUMENTS = EMPTY_ARGS;
    protected static final Class[] EMPTY_TYPES = {};

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
        // let's assume it's a collection of 1
        return Collections.singletonList(value);
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
            metaRegistry.getMetaClass((Class) object).setProperty(object, property, newValue);
        } else {
            ((MetaClassRegistryImpl) metaRegistry).getMetaClass(object).setProperty(object, property, newValue);
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
     * Returns a method closure for the given object and name.
     */
    @SuppressWarnings("rawtypes")
    public static Closure getMethodPointer(final Object object, final String methodName) {
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
            stringToCompare = FormatHelper.toString(left);
        }
        String regexToCompareTo;
        if (right instanceof String) {
            regexToCompareTo = (String) right;
        } else if (right instanceof Pattern) {
            Pattern pattern = (Pattern) right;
            return pattern.matcher(stringToCompare);
        } else {
            regexToCompareTo = FormatHelper.toString(right);
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
            pattern = Pattern.compile(FormatHelper.toString(right));
        }
        String stringToCompare = FormatHelper.toString(left);
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
        throw new SpreadMapEvaluatingException("Cannot spread the map " + FormatHelper.typeName(value) + ", value " + value);
    }

    public static List createList(Object[] values) {
        List answer = new ArrayList(values.length);

        // GROOVY-8995: Improve the performance of creating list
        // answer.addAll(Arrays.asList(values));
        Collections.addAll(answer, values);

        return answer;
    }

    /**
     * According to the initial entry count, calculate the initial capacity of hash map, which is power of 2
     * (SEE https://stackoverflow.com/questions/8352378/why-does-hashmap-require-that-the-initial-capacity-be-a-power-of-two)
     *
     * @param initialEntryCnt the initial entry count
     * @return the initial capacity
     */
    public static int initialCapacity(int initialEntryCnt) {
        if (0 == initialEntryCnt) return 16;

        return Integer.highestOneBit(initialEntryCnt) << 1;
    }

    public static Map createMap(Object[] values) {
        Map answer = new LinkedHashMap(initialCapacity(values.length / 2));

        for (int i = 0, n = values.length; i < n - 1; ) {
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

    public static void assertFailed(final Object expression, final Object message) {
        throw createAssertError(expression, message);
    }

    /**
     * @since 4.0.7
     */
    public static AssertionError createAssertError(final Object expression, final Object message) {
        if (message == null || "".equals(message)) {
            return new PowerAssertionError(expression.toString());
        }
        return new AssertionError(message + ". Expression: " + expression);
    }

    public static Object runScript(Class scriptClass, String[] args) {
        Binding context = new Binding(args);
        Script script = createScript(scriptClass, context);
        return invokeMethod(script, "run", EMPTY_ARGS);
    }

    static class NullScript extends Script {

        public NullScript(Binding context) {
            super(context);
        }

        public NullScript() {
            this(new Binding());
        }

        @Override
        public Object run() {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Script createScript(final Class scriptClass, final Binding context) {
        if (scriptClass == null) {
            return new NullScript(context);
        }

        try {
            Script script;
            if (Script.class.isAssignableFrom(scriptClass)) {
                script = newScript(scriptClass, context);
            } else {
                try {
                    Class<?> glBinding = scriptClass.getClassLoader().loadClass(Binding.class.getName());
                    Constructor<?> contextualConstructor = scriptClass.getDeclaredConstructor(glBinding);
                    Object binding = glBinding.getDeclaredConstructor(Map.class).newInstance(context.getVariables());
                    Object scriptx = contextualConstructor.newInstance(binding);
                    // adapt "new ScriptClass(binding).run()" to Script
                    script = new Script() {
                        @Override
                        public Object run() {
                            return InvokerHelper.invokeMethod(scriptx, "run", EMPTY_ARGUMENTS);
                        }
                    };
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ignore) {
                    // adapt "ScriptClass.main(args)" to Script
                    script = new Script(context) {
                        @Override
                        public Object run() {
                            Object[] mainArgs = {new String[0]};
                            try {
                                Object args = getProperty("args");
                                if (args instanceof String[]) {
                                    mainArgs[0] = args;
                                }
                            } catch (MissingPropertyException mpe) {
                                // call with empty array
                            }
                            return InvokerHelper.invokeStaticMethod(scriptClass, MAIN_METHOD_NAME, mainArgs);
                        }
                    };

                    MetaClass smc = getMetaClass(scriptClass);
                    ((Map<?, ?>) context.getVariables()).forEach((key, value) -> {
                        String name = key.toString();
                        if (!name.startsWith("_")) { // assume underscore variables are for the wrapper
                            setPropertySafe(scriptClass, smc, name, value);
                        }
                    });
                }
            }
            return script;
        } catch (Exception e) {
            throw new GroovyRuntimeException("Failed to create Script instance for class: " + scriptClass + ". Reason: " + e, e);
        }
    }

    public static Script newScript(Class<? extends Script> scriptClass, Binding context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Script script;
        try {
            Constructor<? extends Script> constructor = scriptClass.getConstructor(Binding.class);
            script = constructor.newInstance(context);
        } catch (NoSuchMethodException e) {
            // Fallback for non-standard "Script" classes.
            script = scriptClass.getDeclaredConstructor().newInstance();
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

    public static List createRange(Object from, Object to, boolean exclusiveLeft, boolean exclusiveRight) {
        try {
            return ScriptBytecodeAdapter.createRange(from, to, exclusiveLeft, exclusiveRight);
        } catch (RuntimeException | Error re) {
            throw re;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // Kept in for backwards compatibility

    public static List createRange(Object from, Object to, boolean inclusive) {
        return createRange(from, to, false, !inclusive);
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
            return StringGroovyMethods.bitwiseNegate(value.toString());
        }
        if (value instanceof GString) {
            // value is a regular expression.
            return StringGroovyMethods.bitwiseNegate(value.toString());
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
        if (object instanceof GroovyObject) {
            return ((GroovyObject) object).getMetaClass();
        } else if (object instanceof Class) {
            return metaRegistry.getMetaClass((Class<?>) object); // GROOVY-10819
        } else {
            return ((MetaClassRegistryImpl) metaRegistry).getMetaClass(object);
        }
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
        }

        // if the object is a Class, call a static method from that class
        if (object instanceof Class) {
            Class<?> theClass = (Class<?>) object;
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
     * Converts the given object into an array; if it's an array then just
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

    @SuppressWarnings("unchecked")
    public static Iterator<Object> asIterator(Object o) {
        return (Iterator) invokeMethod(o, "iterator", EMPTY_ARGS);
    }

    @Deprecated
    public static String toString(Object arguments) {
        return FormatHelper.toString(arguments);
    }

    @Deprecated
    public static String inspect(Object self) {
        return FormatHelper.inspect(self);
    }

    @Deprecated
    public static void write(Writer out, Object object) throws IOException {
        FormatHelper.write(out, object);
    }

    @Deprecated
    public static void append(Appendable out, Object object) throws IOException {
        FormatHelper.append(out, object);
    }

    @Deprecated
    protected static String format(Object arguments, boolean verbose) {
        return FormatHelper.format(arguments, verbose);
    }

    @Deprecated
    public static String format(Object arguments, boolean verbose, int maxSize) {
        return FormatHelper.format(arguments, verbose, maxSize);
    }

    @Deprecated
    public static String format(Object arguments, boolean verbose, int maxSize, boolean safe) {
        return FormatHelper.format(arguments, verbose, maxSize, safe);
    }

    @Deprecated
    public static String escapeBackslashes(String orig) {
        return FormatHelper.escapeBackslashes(orig);
    }

    @Deprecated
    public static String toTypeString(Object[] arguments) {
        return FormatHelper.toTypeString(arguments);
    }

    @Deprecated
    public static String toTypeString(Object[] arguments, int maxSize) {
        return FormatHelper.toTypeString(arguments, maxSize);
    }

    @Deprecated
    public static String toMapString(Map arg) {
        return FormatHelper.toMapString(arg);
    }

    @Deprecated
    public static String toMapString(Map arg, int maxSize) {
        return FormatHelper.toMapString(arg, maxSize);
    }

    @Deprecated
    public static String toListString(Collection arg) {
        return FormatHelper.toListString(arg);
    }

    @Deprecated
    public static String toListString(Collection arg, int maxSize) {
        return FormatHelper.toListString(arg, maxSize);
    }

    @Deprecated
    public static String toListString(Collection arg, int maxSize, boolean safe) {
        return FormatHelper.toListString(arg, maxSize, safe);
    }

    @Deprecated
    public static String toArrayString(Object[] arguments) {
        return FormatHelper.toArrayString(arguments);
    }

    @Deprecated
    public static String toArrayString(Object[] arguments, int maxSize, boolean safe) {
        return FormatHelper.toArrayString(arguments, maxSize, safe);
    }
}
