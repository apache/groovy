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
import groovy.util.regex.RegexGuard;
import org.apache.groovy.lang.annotation.GroovyABI;
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
    /**
     * Shared zero-length array used as the argument list for a call
     * that takes no arguments.
     */
    public static final Object[] EMPTY_ARGS = {};
    /**
     * The same array as {@link #EMPTY_ARGS}. {@link #asArray(Object)}
     * returns it when the supplied arguments are {@code null}.
     */
    protected static final Object[] EMPTY_ARGUMENTS = EMPTY_ARGS;
    /**
     * Shared zero-length array of {@code Class} objects.
     */
    protected static final Class[] EMPTY_TYPES = {};

    /**
     * The {@link MetaClassRegistry} from {@link GroovySystem#getMetaClassRegistry()}.
     * Meta-class lookups in this class go through this registry.
     */
    public static final MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
    /**
     * The method name {@code "main"}. {@link #createScript(Class, Binding)}
     * calls a static method of this name when a class cannot be constructed
     * from a {@link Binding}.
     */
    public static final String MAIN_METHOD_NAME = "main";

    /**
     * Drops {@code clazz} from the meta-class registry, from {@link ClassInfo},
     * and from the {@link Introspector} cache.
     *
     * @param clazz the class removed from those caches
     */
    public static void removeClass(Class clazz) {
        metaRegistry.removeMetaClass(clazz);
        ClassInfo.remove(clazz);
        Introspector.flushFromCaches(clazz);
    }

    /**
     * Invokes {@code methodName} on {@code object} when {@code object} is
     * not {@code null}. A {@code null} receiver returns {@code null} and
     * does not call {@link #invokeMethod(Object, String, Object)}.
     *
     * @param object the receiver, or {@code null}
     * @param methodName the method to invoke
     * @param arguments the arguments accepted by {@link #invokeMethod(Object, String, Object)}
     * @return the method result, or {@code null} when {@code object} is {@code null}
     */
    public static Object invokeMethodSafe(Object object, String methodName, Object arguments) {
        if (object != null) {
            return invokeMethod(object, methodName, arguments);
        }
        return null;
    }

    /**
     * Loads {@code klass} with {@code Class.forName(String)} and invokes
     * the static method {@code methodName} on that class.
     *
     * @param klass the class name passed to {@code Class.forName(String)}
     * @param methodName the static method to invoke
     * @param arguments the arguments passed to {@link #invokeStaticMethod(Class, String, Object)}
     * @return the result of the static call
     * @throws ClassNotFoundException if {@code klass} cannot be loaded
     */
    public static Object invokeStaticMethod(String klass, String methodName, Object arguments) throws ClassNotFoundException {
        Class type = Class.forName(klass);
        return invokeStaticMethod(type, methodName, arguments);
    }

    /**
     * Invokes the static method {@code methodName} on {@code type} with
     * {@link #EMPTY_ARGS}.
     *
     * @param type the class that receives the static call
     * @param methodName the static method to invoke
     * @return the result of the static call
     */
    public static Object invokeStaticNoArgumentsMethod(Class type, String methodName) {
        return invokeStaticMethod(type, methodName, EMPTY_ARGS);
    }

    /**
     * Loads {@code klass} with {@code Class.forName(String)} and invokes
     * a constructor of that class.
     *
     * @param klass the class name passed to {@code Class.forName(String)}
     * @param arguments the arguments passed to {@link #invokeConstructorOf(Class, Object)}
     * @return the new instance
     * @throws ClassNotFoundException if {@code klass} cannot be loaded
     */
    public static Object invokeConstructorOf(String klass, Object arguments) throws ClassNotFoundException {
        Class type = Class.forName(klass);
        return invokeConstructorOf(type, arguments);
    }

    /**
     * Invokes a constructor of {@code type} with {@link #EMPTY_ARGS}.
     *
     * @param type the class to instantiate
     * @return the new instance
     */
    public static Object invokeNoArgumentsConstructorOf(Class type) {
        return invokeConstructorOf(type, EMPTY_ARGS);
    }

    /**
     * Invokes {@code doCall} on {@code closure} through
     * {@link #invokeMethod(Object, String, Object)}.
     *
     * @param closure the receiver of {@code doCall}
     * @param arguments the arguments passed to that invocation
     * @return the result of {@code doCall}
     */
    public static Object invokeClosure(Object closure, Object arguments) {
        return invokeMethod(closure, "doCall", arguments);
    }

    /**
     * Adapts {@code value} to a {@link List}. {@code null} becomes
     * {@link Collections#EMPTY_LIST}. A {@link List} is returned as given.
     * When the runtime class is an array, the value is cast to {@code Object[]}
     * and passed to {@link Arrays#asList}; a primitive array fails that cast.
     * An {@link Enumeration} is copied, in iteration order, into a new
     * {@link ArrayList}. Any other value becomes a one-element list.
     *
     * Throws {@code ClassCastException} if {@code value} is a primitive array.
     * @param value the value to adapt
     * @return a list for {@code value}
     */
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
        if (value instanceof Enumeration e) {
            List answer = new ArrayList();
            while (e.hasMoreElements()) {
                answer.add(e.nextElement());
            }
            return answer;
        }
        // let's assume it's a collection of 1
        return Collections.singletonList(value);
    }

    /**
     * Reads the attribute {@code attribute} from {@code object}.
     * A {@code null} receiver is replaced with {@code NullObject.getNullObject()}
     * before the read. A {@code Class} receiver is tested before a
     * {@link GroovyObject} and uses the meta-class registered for that class.
     * A {@link GroovyObject} uses the meta-class from
     * {@link GroovyObject#getMetaClass()}. Every other receiver uses
     * the meta-class registered for its runtime class. The value is then
     * read with {@code getAttribute} on that meta-class.
     *
     * @param object the receiver, or {@code null}
     * @param attribute the attribute name
     * @return the attribute value
     */
    public static Object getAttribute(Object object, String attribute) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

        if (object instanceof Class c) {
            return metaRegistry.getMetaClass(c).getAttribute(object, attribute);
        } else if (object instanceof GroovyObject) {
            return ((GroovyObject) object).getMetaClass().getAttribute(object, attribute);
        } else {
            return metaRegistry.getMetaClass(object.getClass()).getAttribute(object, attribute);
        }
    }

    /**
     * Writes {@code newValue} to the attribute {@code attribute} on
     * {@code object}. A {@code null} receiver is replaced with
     * {@code NullObject.getNullObject()} before the write. A {@code Class}
     * receiver is tested before a {@link GroovyObject} and uses the meta-class
     * registered for that class. A {@link GroovyObject} uses the meta-class
     * from {@link GroovyObject#getMetaClass()}. Every other receiver uses the
     * meta-class registered for its runtime class. The value is then
     * written with {@code setAttribute} on that meta-class.
     *
     * @param object the receiver, or {@code null}
     * @param attribute the attribute name
     * @param newValue the value to assign
     */
    public static void setAttribute(Object object, String attribute, Object newValue) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

        if (object instanceof Class c) {
            metaRegistry.getMetaClass(c).setAttribute(object, attribute, newValue);
        } else if (object instanceof GroovyObject) {
            ((GroovyObject) object).getMetaClass().setAttribute(object, attribute, newValue);
        } else {
            metaRegistry.getMetaClass(object.getClass()).setAttribute(object, attribute, newValue);
        }
    }

    /**
     * Reads the property {@code property} from {@code object}.
     * A {@code null} receiver is replaced with {@code NullObject.getNullObject()}
     * before the read. A {@link GroovyObject} is tested before a {@code Class}
     * and is read through {@link GroovyObject#getProperty(String)}. A
     * {@code Class} receiver uses the meta-class registered for that class.
     * Every other receiver uses
     * {@link MetaClassRegistryImpl#getMetaClass(Object)} and then
     * {@code getProperty} on that meta-class.
     *
     * @param object the receiver, or {@code null}
     * @param property the property name
     * @return the property value
     */
    @GroovyABI(since = "2.5.0")
    public static Object getProperty(Object object, String property) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

        if (object instanceof GroovyObject o) {
            return o.getProperty(property);
        } else if (object instanceof Class c) {
            return metaRegistry.getMetaClass(c).getProperty(object, property);
        } else {
            return ((MetaClassRegistryImpl) metaRegistry).getMetaClass(object).getProperty(object, property);
        }
    }

    /**
     * Reads {@code property} from {@code object} when {@code object} is
     * not {@code null}. A {@code null} receiver returns {@code null} and
     * is not replaced with {@code NullObject}.
     *
     * @param object the receiver, or {@code null}
     * @param property the property name
     * @return the property value, or {@code null} when {@code object} is {@code null}
     */
    @GroovyABI(since = "2.5.0")
    public static Object getPropertySafe(Object object, String property) {
        if (object != null) {
            return getProperty(object, property);
        }
        return null;
    }

    /**
     * Writes {@code newValue} to the property {@code property} on {@code object}.
     * A {@code null} receiver is replaced with {@code NullObject.getNullObject()}
     * before the write. A {@link GroovyObject} is tested before a {@code Class}
     * and is written through {@link GroovyObject#setProperty(String, Object)}.
     * A {@code Class} receiver uses the meta-class registered for that class.
     * Every other receiver uses
     * {@link MetaClassRegistryImpl#getMetaClass(Object)} and then
     * {@code setProperty} on that meta-class.
     *
     * @param object the receiver, or {@code null}
     * @param property the property name
     * @param newValue the value to assign
     */
    @GroovyABI(since = "1.0.0")
    public static void setProperty(Object object, String property, Object newValue) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

        if (object instanceof GroovyObject o) {
            o.setProperty(property, newValue);
        } else if (object instanceof Class c) {
            metaRegistry.getMetaClass(c).setProperty(object, property, newValue);
        } else {
            ((MetaClassRegistryImpl) metaRegistry).getMetaClass(object).setProperty(object, property, newValue);
        }
    }

    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    @GroovyABI(since = "1.0.0")
    public static void setProperty2(Object newValue, Object object, String property) {
        setProperty(object, property, newValue);
    }

    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    @GroovyABI(since = "1.0.0")
    public static void setGroovyObjectProperty(Object newValue, GroovyObject object, String property) {
        object.setProperty(property, newValue);
    }

    /**
     * Reads {@code property} by calling {@link GroovyObject#getProperty(String)}
     * on {@code object}, with no null substitution and no registry lookup.
     *
     * @param object the Groovy receiver
     * @param property the property name
     * @return the property value
     */
    @GroovyABI(since = "1.0.0")
    public static Object getGroovyObjectProperty(GroovyObject object, String property) {
        return object.getProperty(property);
    }

    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    @GroovyABI(since = "1.0.0")
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

    /**
     * Negates {@code value}. {@code Integer}, {@code Long}, {@code Double},
     * and {@code Float} use arithmetic negation. {@code Short} and
     * {@code Byte} are negated and cast back to {@code short} and
     * {@code byte}. {@link BigInteger} and {@link BigDecimal} use
     * {@code negate()}. An {@link ArrayList} yields a new list of the
     * element-wise results. Any other value is passed to
     * {@link #invokeMethod(Object, String, Object)} as the no-argument
     * method {@code negative}.
     *
     * @param value the value to negate
     * @return the negated value
     */
    public static Object unaryMinus(Object value) {
        if (value instanceof Integer number) {
            return -number;
        }
        if (value instanceof Long number) {
            return -number;
        }
        if (value instanceof BigInteger) {
            return ((BigInteger) value).negate();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).negate();
        }
        if (value instanceof Double number) {
            return -number;
        }
        if (value instanceof Float number) {
            return -number;
        }
        if (value instanceof Short number) {
            return (short) -number;
        }
        if (value instanceof Byte number) {
            return (byte) -number;
        }
        if (value instanceof ArrayList list) {
            var newlist = new ArrayList<>();
            for (Object o : list) {
                newlist.add(unaryMinus(o));
            }
            return newlist;
        }
        return invokeMethod(value, "negative", EMPTY_ARGS);
    }

    /**
     * Applies unary plus to {@code value}. {@code Integer}, {@code Long},
     * {@link BigInteger}, {@link BigDecimal}, {@code Double}, {@code Float},
     * {@code Short}, and {@code Byte} are returned unchanged. An
     * {@link ArrayList} yields a new list of the element-wise results.
     * Any other value is passed to {@link #invokeMethod(Object, String, Object)}
     * as the no-argument method {@code positive}.
     *
     * @param value the value to which unary plus is applied
     * @return the numeric value unchanged, or the element-wise or method result
     */
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
        if (value instanceof ArrayList list) {
            var newlist = new ArrayList<>();
            for (Object o : list) {
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
        } else if (right instanceof Pattern pattern) {
            return pattern.matcher(RegexGuard.ambientGuard(stringToCompare));
        } else {
            regexToCompareTo = FormatHelper.toString(right);
        }
        return Pattern.compile(regexToCompareTo).matcher(RegexGuard.ambientGuard(stringToCompare));
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
        Matcher matcher = pattern.matcher(RegexGuard.ambientGuard(stringToCompare));
        RegexSupport.setLastMatcher(matcher);
        return matcher.matches();
    }

    /**
     * Returns a {@link Tuple} constructed from {@code array}.
     *
     * @param array the elements passed to the tuple constructor
     * @return the new tuple
     */
    public static Tuple createTuple(Object[] array) {
        return new Tuple<>(array);
    }

    /**
     * Builds a {@link SpreadMap} from {@code value} when {@code value} is a
     * {@link Map}. Entries are flattened, in key-set iteration order, into
     * an array that alternates key then value, and that array is passed to
     * the {@link SpreadMap} constructor. A non-map value throws
     * {@link SpreadMapEvaluatingException}. {@code null} does not reach that
     * throw: the message calls {@code FormatHelper.typeName(value)}, which
     * dereferences {@code value}.
     *
     * @param value the map to spread
     * @return a spread map of the map's entries
     */
    public static SpreadMap spreadMap(Object value) {
        if (value instanceof Map map) {
            Object[] values = new Object[map.size() * 2];
            int index = 0;
            for (Object key : map.keySet()) {
                values[index++] = key;
                values[index++] = map.get(key);
            }
            return new SpreadMap(values);
        }
        throw new SpreadMapEvaluatingException("Cannot spread the map " + FormatHelper.typeName(value) + ", value " + value);
    }

    /**
     * Returns a new {@link ArrayList} sized to {@code values.length} and
     * filled with those elements in order.
     *
     * @param values the elements to copy into the list
     * @return the new list
     */
    public static List createList(Object[] values) {
        var answer = new ArrayList<>(values.length);

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

    /**
     * Builds a {@link LinkedHashMap} from successive pairs in {@code values}.
     * The initial capacity is {@link #initialCapacity(int)} of
     * {@code values.length / 2}. A pair whose first element is a
     * {@link SpreadMap} and whose second element is a {@link Map} contributes
     * that map's entries. Every other pair is stored as one key and value.
     * A trailing element left without a pair is ignored.
     *
     * @param values alternating keys and values, with optional spread pairs
     * @return the collected entries in insertion order
     */
    public static Map createMap(Object[] values) {
        var answer = new LinkedHashMap<>(initialCapacity(values.length / 2));

        for (int i = 0, n = values.length; i < n - 1; ) {
            if ((values[i] instanceof SpreadMap) && (values[i + 1] instanceof Map smap)) {
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

    /**
     * Throws the {@code AssertionError} returned by
     * {@link #createAssertError(Object, Object)} for {@code expression}
     * and {@code message}.
     *
     * @param expression the expression supplied to the error
     * @param message the message supplied to the error
     */
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

    /**
     * Runs {@code scriptClass} in a {@link Binding} whose {@code args}
     * variable is the supplied array. The instance comes from
     * {@link #createScript(Class, Binding)}, and {@code run} is invoked
     * on it with {@link #EMPTY_ARGS}.
     *
     * @param scriptClass the class passed to {@link #createScript(Class, Binding)}
     * @param args the array stored as the binding variable {@code args}
     * @return the result of {@code run}
     */
    public static Object runScript(Class scriptClass, String[] args) {
        Binding context = new Binding(args);
        Script script = createScript(scriptClass, context);
        return invokeMethod(script, "run", EMPTY_ARGS);
    }

    static class NullScript extends Script {

        /**
         * Creates a script bound to {@code context}. {@code run} returns {@code null}.
         *
         * @param context the binding passed to {@link Script}
         */
        public NullScript(Binding context) {
            super(context);
        }

        /**
         * Creates a script bound to a new empty {@link Binding}.
         * {@code run} returns {@code null}.
         */
        public NullScript() {
            this(new Binding());
        }

        /**
         * Returns {@code null} and runs no script body.
         *
         * @return {@code null}
         */
        @Override
        public Object run() {
            return null;
        }
    }

    /**
     * Adapts {@code scriptClass} to a {@link Script} associated with {@code context}.
     * <p>
     * A {@code null} class produces a {@code NullScript} bound to {@code context};
     * its {@code run} method returns {@code null}. A class assignable to
     * {@link Script} is instantiated with {@link #newScript(Class, Binding)}.
     * <p>
     * For any other class, {@code Binding.class.getName()} is loaded from
     * {@code scriptClass}'s class loader. A binding of that loaded type is
     * constructed from {@code context.getVariables()} through its declared
     * {@link Map} constructor, and {@code scriptClass} is constructed with
     * its declared constructor that takes that binding type. The instance is
     * wrapped in a {@link Script} created with the no-argument constructor,
     * so the wrapper's own binding is a new empty {@link Binding}.
     * The wrapper's {@code run} method invokes {@code run} on the instance
     * with no arguments.
     * <p>
     * If that attempt throws {@code ClassNotFoundException},
     * {@code NoSuchMethodException}, or {@code SecurityException}, the result
     * is a {@link Script} constructed with {@code context}. Its {@code run}
     * method calls the static {@link #MAIN_METHOD_NAME} method with one
     * argument: the binding property {@code args} when that value is a
     * {@code String[]}, otherwise a new empty {@code String[]}. A missing
     * {@code args} property, reported as {@link MissingPropertyException},
     * also selects the empty array. Before the wrapper is returned, each
     * entry of {@code context.getVariables()} whose key's {@code toString()}
     * does not start with {@code '_'} is written as a property of the
     * {@code scriptClass} class object. A
     * {@link MissingPropertyException} from that write is ignored, and so is
     * an {@code IllegalArgumentException} carried as the cause of an
     * {@code InvokerInvocationException}.
     * <p>
     * Any other {@code Exception} is wrapped in a {@link GroovyRuntimeException}
     * that names {@code scriptClass}.
     *
     * @param scriptClass the class to adapt, or {@code null}
     * @param context the binding supplied to the adapted script
     * @return the adapted {@link Script}
     */
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

    /**
     * Instantiates {@code scriptClass} and associates {@code context}.
     * Uses the public {@code (Binding)} constructor when {@code scriptClass}
     * has one. When that constructor is absent, calls the declared
     * no-argument constructor and then {@link Script#setBinding(Binding)}.
     *
     * @param scriptClass the {@link Script} type to construct
     * @param context the binding passed to the constructor, or to {@code setBinding}
     * @return the new script instance
     * @throws NoSuchMethodException if the no-argument constructor is absent
     * @throws InstantiationException if the class that declares the chosen constructor is abstract
     * @throws IllegalAccessException if the chosen constructor is inaccessible
     * @throws InvocationTargetException if the chosen constructor throws an exception
     */
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

    /**
     * Creates the range from {@code from} to {@code to} by delegating to
     * {@code ScriptBytecodeAdapter.createRange}. A {@code RuntimeException}
     * or {@code Error} is rethrown unchanged. Any other {@code Throwable}
     * is wrapped in a {@code RuntimeException}.
     *
     * @param from the start value passed to the delegate
     * @param to the end value passed to the delegate
     * @param exclusiveLeft passed through as the left-exclusive flag
     * @param exclusiveRight passed through as the right-exclusive flag
     * @return the list returned by the delegate
     */
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

    /**
     * Creates a range from {@code from} to {@code to} with the left end
     * included. The call is {@code createRange(from, to, false, !inclusive)},
     * so {@code inclusive} controls whether {@code to} is included.
     *
     * @param from the start value; the left end is inclusive
     * @param to the end value
     * @param inclusive {@code true} to include {@code to}; {@code false} to exclude it
     * @return the list from {@link #createRange(Object, Object, boolean, boolean)}
     */
    public static List createRange(Object from, Object to, boolean inclusive) {
        return createRange(from, to, false, !inclusive);
    }
    /**
     * Bitwise-negates {@code value}. {@code Integer} and {@code Long} use
     * the {@code ~} operator. {@link BigInteger} uses {@code not()}.
     * A {@code String} or {@link GString} is compiled as a regular expression
     * by {@code StringGroovyMethods.bitwiseNegate} on {@code value.toString()}.
     * An {@link ArrayList} yields a new list of the element-wise results.
     * Any other value is passed to {@link #invokeMethod(Object, String, Object)}
     * as the no-argument method {@code bitwiseNegate}.
     *
     * @param value the value to negate
     * @return the negated number, the {@link Pattern} compiled from a string or GString,
     *     a list of the element-wise results, or the value returned by the no-argument
     *     {@code bitwiseNegate} method
     */
    public static Object bitwiseNegate(Object value) {
        if (value instanceof Integer number) {
            return ~number;
        }
        if (value instanceof Long number) {
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
        if (value instanceof ArrayList list) {
            var newlist = new ArrayList<>();
            for (Object o : list) {
                newlist.add(bitwiseNegate(o));
            }
            return newlist;
        }
        return invokeMethod(value, "bitwiseNegate", EMPTY_ARGS);
    }

    /**
     * Returns {@link #metaRegistry}.
     *
     * @return the registry from {@link GroovySystem#getMetaClassRegistry()}
     */
    public static MetaClassRegistry getMetaRegistry() {
        return metaRegistry;
    }

    /**
     * Returns the meta-class used for {@code object}. A {@link GroovyObject}
     * is tested before a {@code Class} and answers with
     * {@link GroovyObject#getMetaClass()}. A {@code Class} answers with
     * {@link MetaClassRegistry#getMetaClass(Class)} on
     * {@link #metaRegistry}. Any other object answers with
     * {@link MetaClassRegistryImpl#getMetaClass(Object)}.
     *
     * @param object the receiver whose meta-class is required
     * @return that meta-class
     */
    public static MetaClass getMetaClass(Object object) {
        if (object instanceof GroovyObject o) {
            return o.getMetaClass();
        } else if (object instanceof Class c) {
            return metaRegistry.getMetaClass(c); // GROOVY-10819
        } else {
            return ((MetaClassRegistryImpl) metaRegistry).getMetaClass(object);
        }
    }

    /**
     * Returns the meta-class registered for {@code cls}.
     *
     * @param cls the class to look up in {@link #metaRegistry}
     * @return {@link MetaClassRegistry#getMetaClass(Class)} for {@code cls}
     */
    public static MetaClass getMetaClass(Class cls) {
        return metaRegistry.getMetaClass(cls);
    }

    /**
     * Invokes the given method on the object.
     */
    @GroovyABI(since = "5.0.0")
    public static Object invokeMethod(Object object, String methodName, Object arguments) {
        if (object == null) {
            object = NullObject.getNullObject();
        }

        // if the object is a Class, call a static method from that class
        if (object instanceof Class<?> theClass) {
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

    /**
     * Invokes {@code methodName} through the meta-class of the superclass
     * of {@code object}'s runtime class. Arguments are normalized with
     * {@link #asArray(Object)}. A {@code null} receiver throws
     * {@code NullPointerException} whose message names {@code methodName}.
     *
     * @param object the receiver; must not be {@code null}
     * @param methodName the method to invoke
     * @param arguments the argument list, a single argument, or {@code null}
     * @return the result of the meta-class invocation
     */
    public static Object invokeSuperMethod(Object object, String methodName, Object arguments) {
        if (object == null) {
            throw new NullPointerException("Cannot invoke method " + methodName + "() on null object");
        }

        MetaClass metaClass = metaRegistry.getMetaClass(object.getClass().getSuperclass());
        return metaClass.invokeMethod(object, methodName, asArray(arguments));
    }

    /**
     * Invokes the static method {@code method} on {@code type} through the
     * meta-class registered for {@code type}. Arguments are normalized with
     * {@link #asArray(Object)}.
     *
     * @param type the class that receives the static call
     * @param method the method name
     * @param arguments the argument list, a single argument, or {@code null}
     * @return the result of the static call
     */
    public static Object invokeStaticMethod(Class type, String method, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(type);
        return metaClass.invokeStaticMethod(type, method, asArray(arguments));
    }

    /**
     * Invokes a constructor of {@code type} through the meta-class
     * registered for that class. Arguments are normalized with
     * {@link #asArray(Object)}.
     *
     * @param type the class to instantiate
     * @param arguments the argument list, a single argument, or {@code null}
     * @return the new instance
     */
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

    /**
     * Normalizes {@code arguments} with {@link #asArray(Object)}, then
     * replaces each {@link PojoWrapper} element with
     * {@link PojoWrapper#unwrap()}. When {@code arguments} is already an
     * {@code Object[]}, that same array is updated and returned.
     *
     * @param arguments the argument list, a single argument, or {@code null}
     * @return the normalized array, with wrappers removed
     */
    public static Object[] asUnwrappedArray(Object arguments) {
        Object[] args = asArray(arguments);

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof PojoWrapper) {
                args[i] = ((PojoWrapper) args[i]).unwrap();
            }
        }

        return args;
    }

    /**
     * Invokes the no-argument method {@code iterator} on {@code o} and
     * casts the result to {@link Iterator}.
     *
     * @param o the receiver of {@code iterator}
     * @return the iterator returned by that call
     */
    @SuppressWarnings("unchecked")
    public static Iterator<Object> asIterator(Object o) {
        return (Iterator) invokeMethod(o, "iterator", EMPTY_ARGS);
    }

    /**
     * Returns the string representation of {@code arguments} produced by
     * {@code FormatHelper.toString(Object)}.
     *
     * @param arguments the object to render
     * @return that string representation
     * @deprecated use {@code FormatHelper.toString(Object)}
     */
    @Deprecated
    @GroovyABI(since = "1.0.0")
    public static String toString(Object arguments) {
        return FormatHelper.toString(arguments);
    }

    /**
     * Returns the inspect rendering of {@code self} produced by
     * {@code FormatHelper.inspect(Object)}, which selects inspect mode
     * with escaped backslashes.
     *
     * @param self the object to inspect
     * @return that rendering
     * @deprecated use {@code FormatHelper.inspect(Object)}
     */
    @Deprecated
    public static String inspect(Object self) {
        return FormatHelper.inspect(self);
    }

    /**
     * Writes {@code object} to {@code out} by calling
     * {@code FormatHelper.write(Writer, Object)}.
     *
     * @param out the writer that receives the text
     * @param object the object to write
     * @throws IOException if writing fails
     * @deprecated use {@code FormatHelper.write(Writer, Object)}
     */
    @Deprecated
    public static void write(Writer out, Object object) throws IOException {
        FormatHelper.write(out, object);
    }

    /**
     * Appends {@code object} to {@code out} by calling
     * {@code FormatHelper.append(Appendable, Object)}.
     *
     * @param out the appendable that receives the text
     * @param object the object to append
     * @throws IOException if appending fails
     * @deprecated use {@code FormatHelper.append(Appendable, Object)}
     */
    @Deprecated
    public static void append(Appendable out, Object object) throws IOException {
        FormatHelper.append(out, object);
    }

    /**
     * Renders {@code arguments} by calling
     * {@code FormatHelper.format(Object, boolean)}. When {@code verbose}
     * is {@code true}, the delegate uses inspect mode with escaped backslashes.
     *
     * @param arguments the object to render
     * @param verbose {@code true} to select inspect mode with escaped backslashes
     * @return the formatted text
     * @deprecated use {@code FormatHelper.format(Object, boolean)}
     */
    @Deprecated
    protected static String format(Object arguments, boolean verbose) {
        return FormatHelper.format(arguments, verbose);
    }

    /**
     * Renders {@code arguments} by calling
     * {@code FormatHelper.format(Object, boolean, int)}. When {@code verbose}
     * is {@code true}, the delegate uses inspect mode with escaped backslashes.
     * {@code maxSize} is an approximate character limit; {@code -1} means no limit.
     *
     * @param arguments the object to render
     * @param verbose {@code true} to select inspect mode with escaped backslashes
     * @param maxSize the approximate character limit, or {@code -1} for no limit
     * @return the formatted text
     * @deprecated use {@code FormatHelper.format(Object, boolean, int)}
     */
    @Deprecated
    public static String format(Object arguments, boolean verbose, int maxSize) {
        return FormatHelper.format(arguments, verbose, maxSize);
    }

    /**
     * Renders {@code arguments} by calling
     * {@code FormatHelper.format(Object, boolean, int, boolean)}.
     * When {@code verbose} is {@code true}, the delegate uses inspect mode
     * with escaped backslashes. {@code maxSize} is an approximate character
     * limit; {@code -1} means no limit. When {@code safe} is {@code true},
     * an {@code Exception} thrown while rendering is swallowed and a fallback
     * representation is used.
     *
     * @param arguments the object to render
     * @param verbose {@code true} to select inspect mode with escaped backslashes
     * @param maxSize the approximate character limit, or {@code -1} for no limit
     * @param safe {@code true} to swallow a rendering {@code Exception}
     * @return the formatted text
     * @deprecated use {@code FormatHelper.format(Object, boolean, int, boolean)}
     */
    @Deprecated
    public static String format(Object arguments, boolean verbose, int maxSize, boolean safe) {
        return FormatHelper.format(arguments, verbose, maxSize, safe);
    }

    /**
     * Returns {@code orig} with backslash, newline, carriage return, tab,
     * and form feed written as escape sequences, by calling
     * {@code FormatHelper.escapeBackslashes(String)}.
     *
     * @param orig the text to escape
     * @return the escaped text
     * @deprecated use {@code FormatHelper.escapeBackslashes(String)}
     */
    @Deprecated
    public static String escapeBackslashes(String orig) {
        return FormatHelper.escapeBackslashes(orig);
    }

    /**
     * Returns a comma-separated list of the runtime type names in
     * {@code arguments}, by calling {@code FormatHelper.toTypeString(Object[])}.
     *
     * @param arguments the values whose types are rendered
     * @return the type list, or {@code "null"} when {@code arguments} is {@code null}
     * @deprecated use {@code FormatHelper.toTypeString(Object[])}
     */
    @Deprecated
    public static String toTypeString(Object[] arguments) {
        return FormatHelper.toTypeString(arguments);
    }

    /**
     * Returns a comma-separated list of the runtime type names in
     * {@code arguments}, by calling {@code FormatHelper.toTypeString(Object[], int)}.
     * Output stops after about {@code maxSize} characters and then appends
     * {@code "..."}; {@code -1} means no limit.
     *
     * @param arguments the values whose types are rendered
     * @param maxSize the approximate character limit, or {@code -1} for no limit
     * @return the type list
     * @deprecated use {@code FormatHelper.toTypeString(Object[], int)}
     */
    @Deprecated
    public static String toTypeString(Object[] arguments, int maxSize) {
        return FormatHelper.toTypeString(arguments, maxSize);
    }

    /**
     * Returns the bracketed rendering of {@code arg}, using {@code [} and
     * {@code ]} boundaries, by calling {@code FormatHelper.toMapString(Map)}.
     *
     * @param arg the map to render
     * @return the map text
     * @deprecated use {@code FormatHelper.toMapString(Map)}
     */
    @Deprecated
    public static String toMapString(Map arg) {
        return FormatHelper.toMapString(arg);
    }

    /**
     * Returns the bracketed rendering of {@code arg}, using {@code [} and
     * {@code ]} boundaries, by calling {@code FormatHelper.toMapString(Map, int)}.
     * Output stops after about {@code maxSize} characters and then appends
     * {@code "..."}; {@code -1} means no limit.
     *
     * @param arg the map to render
     * @param maxSize the approximate character limit, or {@code -1} for no limit
     * @return the map text
     * @deprecated use {@code FormatHelper.toMapString(Map, int)}
     */
    @Deprecated
    public static String toMapString(Map arg, int maxSize) {
        return FormatHelper.toMapString(arg, maxSize);
    }

    /**
     * Returns the bracketed rendering of {@code arg}, using {@code [} and
     * {@code ]} boundaries, by calling {@code FormatHelper.toListString(Collection)}.
     *
     * @param arg the collection to render
     * @return the collection text
     * @deprecated use {@code FormatHelper.toListString(Collection)}
     */
    @Deprecated
    public static String toListString(Collection arg) {
        return FormatHelper.toListString(arg);
    }

    /**
     * Returns the bracketed rendering of {@code arg}, using {@code [} and
     * {@code ]} boundaries, by calling
     * {@code FormatHelper.toListString(Collection, int)}. Output stops after
     * about {@code maxSize} characters and then appends {@code "..."};
     * {@code -1} means no limit.
     *
     * @param arg the collection to render
     * @param maxSize the approximate character limit, or {@code -1} for no limit
     * @return the collection text
     * @deprecated use {@code FormatHelper.toListString(Collection, int)}
     */
    @Deprecated
    public static String toListString(Collection arg, int maxSize) {
        return FormatHelper.toListString(arg, maxSize);
    }

    /**
     * Returns the bracketed rendering of {@code arg}, using {@code [} and
     * {@code ]} boundaries, by calling
     * {@code FormatHelper.toListString(Collection, int, boolean)}.
     * Output stops after about {@code maxSize} characters and then appends
     * {@code "..."}; {@code -1} means no limit. When {@code safe} is
     * {@code true}, an item whose rendering throws an {@code Exception}
     * uses a fallback representation.
     *
     * @param arg the collection to render
     * @param maxSize the approximate character limit, or {@code -1} for no limit
     * @param safe {@code true} to substitute a fallback for an item that fails to render
     * @return the collection text
     * @deprecated use {@code FormatHelper.toListString(Collection, int, boolean)}
     */
    @Deprecated
    public static String toListString(Collection arg, int maxSize, boolean safe) {
        return FormatHelper.toListString(arg, maxSize, safe);
    }

    /**
     * Returns the bracketed rendering of {@code arguments}, using {@code [}
     * and {@code ]} boundaries, by calling
     * {@code FormatHelper.toArrayString(Object[])}.
     *
     * @param arguments the array to render
     * @return the array text
     * @deprecated use {@code FormatHelper.toArrayString(Object[])}
     */
    @Deprecated
    public static String toArrayString(Object[] arguments) {
        return FormatHelper.toArrayString(arguments);
    }

    /**
     * Returns the bracketed rendering of {@code arguments}, using {@code [}
     * and {@code ]} boundaries, by calling
     * {@code FormatHelper.toArrayString(Object[], int, boolean)}.
     * Output stops after about {@code maxSize} characters and then appends
     * {@code "..."}; {@code -1} means no limit. When {@code safe} is
     * {@code true}, an element whose rendering throws an {@code Exception}
     * uses a fallback representation.
     *
     * @param arguments the array to render
     * @param maxSize the approximate character limit, or {@code -1} for no limit
     * @param safe {@code true} to substitute a fallback for an element that fails to render
     * @return the array text
     * @deprecated use {@code FormatHelper.toArrayString(Object[], int, boolean)}
     */
    @Deprecated
    public static String toArrayString(Object[] arguments, int maxSize, boolean safe) {
        return FormatHelper.toArrayString(arguments, maxSize, safe);
    }
}
