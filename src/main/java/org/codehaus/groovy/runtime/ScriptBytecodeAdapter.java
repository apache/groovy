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

import groovy.lang.Closure;
import groovy.lang.EmptyRange;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.IntRange;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.NumberRange;
import groovy.lang.ObjectRange;
import groovy.lang.Tuple;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;
import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.wrappers.GroovyObjectWrapper;
import org.codehaus.groovy.runtime.wrappers.PojoWrapper;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.BaseStream;

/**
 * A static helper class to interface bytecode and runtime
 */
public class ScriptBytecodeAdapter {

    public static final Object[] EMPTY_ARGS = {};
    private static final Integer ONE = Integer.valueOf(1);
    private static final Integer ZERO = Integer.valueOf(0);
    private static final Integer MINUS_ONE = Integer.valueOf(-1);

    //  --------------------------------------------------------
    //                   exception handling
    //  --------------------------------------------------------
    public static Throwable unwrap(GroovyRuntimeException gre) {
        if (gre.getCause()==null) {
            if (gre instanceof MissingPropertyExceptionNoStack) {
                MissingPropertyExceptionNoStack noStack = (MissingPropertyExceptionNoStack) gre;
                return new MissingPropertyException(noStack.getProperty(), noStack.getType());
            }

            if (gre instanceof MissingMethodExceptionNoStack) {
                MissingMethodExceptionNoStack noStack = (MissingMethodExceptionNoStack) gre;
                return new MissingMethodException(noStack.getMethod(), noStack.getType(), noStack.getArguments(), noStack.isStatic());
            }
        }

        Throwable th = gre;
        if (th.getCause() != null && th.getCause() != gre) th = th.getCause();
        if (th != gre && (th instanceof GroovyRuntimeException)) return unwrap((GroovyRuntimeException) th);
        return th;
    }

    //  --------------------------------------------------------
    //                       methods for this
    //  --------------------------------------------------------
    public static Object invokeMethodOnCurrentN(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        try {
            boolean intercepting = (receiver instanceof GroovyInterceptable);
            try {
                if (intercepting) {
                    return receiver.invokeMethod(messageName, messageArguments);
                } else {
                    return receiver.getMetaClass().invokeMethod(senderClass, receiver, messageName, messageArguments, false, true);
                }
            } catch (MissingMethodException e) {
                if (e instanceof MissingMethodExecutionFailed) {
                    throw (MissingMethodException)e.getCause();
                } else if (!intercepting && receiver.getClass() == e.getType() && e.getMethod().equals(messageName)) {
                    // in case there's nothing else, invoke the object's own invokeMethod()
                    return receiver.invokeMethod(messageName, messageArguments);
                } else {
                    throw e;
                }
            }
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object invokeMethodOnCurrentNSafe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        return invokeMethodOnCurrentN(senderClass, receiver, messageName, messageArguments);
    }

    public static Object invokeMethodOnCurrentNSpreadSafe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(invokeMethodNSafe(senderClass, it.next(), messageName, messageArguments));
        }
        return answer;
    }

    public static Object invokeMethodOnCurrent0(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        return invokeMethodOnCurrentN(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    public static Object invokeMethodOnCurrent0Safe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        return invokeMethodOnCurrentNSafe(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    public static Object invokeMethodOnCurrent0SpreadSafe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        return invokeMethodOnCurrentNSpreadSafe(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    //  --------------------------------------------------------
    //                       methods for super
    //  --------------------------------------------------------
    public static Object invokeMethodOnSuperN(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        MetaClass metaClass = receiver.getMetaClass();
        // ignore interception and missing method fallback
        Object result = null;
        try {
            result = metaClass.invokeMethod(senderClass, receiver, messageName, messageArguments, true, true);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
        return result;
    }

    public static Object invokeMethodOnSuperNSafe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        return invokeMethodOnSuperN(senderClass, receiver, messageName, messageArguments);
    }

    public static Object invokeMethodOnSuperNSpreadSafe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(invokeMethodNSafe(senderClass, it.next(), messageName, messageArguments));
        }
        return answer;
    }

    public static Object invokeMethodOnSuper0(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        return invokeMethodOnSuperN(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    public static Object invokeMethodOnSuper0Safe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        return invokeMethodOnSuperNSafe(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    public static Object invokeMethodOnSuper0SpreadSafe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        return invokeMethodOnSuperNSpreadSafe(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    //  --------------------------------------------------------
    //              normal method invocation
    //  --------------------------------------------------------
    public static Object invokeMethodN(Class senderClass, Object receiver, String messageName, Object[] messageArguments) throws Throwable {
        try {
            return InvokerHelper.invokeMethod(receiver, messageName, messageArguments);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object invokeMethodNSafe(Class senderClass, Object receiver, String messageName, Object[] messageArguments) throws Throwable {
        if (receiver == null) return null;
        return invokeMethodN(senderClass, receiver, messageName, messageArguments);
    }

    public static Object invokeMethodNSpreadSafe(Class senderClass, Object receiver, String messageName, Object[] messageArguments) throws Throwable {
        if (receiver == null) return null;
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(invokeMethodNSafe(senderClass, it.next(), messageName, messageArguments));
        }
        return answer;
    }

    public static Object invokeMethod0(Class senderClass, Object receiver, String messageName) throws Throwable {
        return invokeMethodN(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    public static Object invokeMethod0Safe(Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        return invokeMethodNSafe(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    public static Object invokeMethod0SpreadSafe(Class senderClass, Object receiver, String messageName) throws Throwable {
        return invokeMethodNSpreadSafe(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    //  --------------------------------------------------------
    //                static normal method invocation
    //  --------------------------------------------------------
    public static Object invokeStaticMethodN(Class senderClass, Class receiver, String messageName, Object[] messageArguments) throws Throwable {
        try {
            return InvokerHelper.invokeStaticMethod(receiver, messageName, messageArguments);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object invokeStaticMethod0(Class senderClass, Class receiver, String messageName) throws Throwable {
        return invokeStaticMethodN(senderClass, receiver, messageName, EMPTY_ARGS);
    }

    //  --------------------------------------------------------
    //              normal constructor invocation (via new)
    //  --------------------------------------------------------
    public static Object invokeNewN(Class senderClass, Class receiver, Object arguments) throws Throwable {
        try {
            return InvokerHelper.invokeConstructorOf(receiver, arguments);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object invokeNew0(Class senderClass, Class receiver) throws Throwable {
        return invokeNewN(senderClass, receiver, EMPTY_ARGS);
    }

    //  --------------------------------------------------------
    //       special constructor invocation (via this/super)
    //  --------------------------------------------------------

    public static int selectConstructorAndTransformArguments(Object[] arguments, int numberOfConstructors, Class which) throws Throwable {
        try {
            MetaClass metaClass = InvokerHelper.getMetaClass(which);
            return metaClass.selectConstructorAndTransformArguments(numberOfConstructors, arguments);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    //  --------------------------------------------------------
    //              field handling super: get
    //  --------------------------------------------------------

    public static Object getFieldOnSuper(Class senderClass, Object receiver, String messageName) throws Throwable {
        try {
            if (receiver instanceof Class) {
                return InvokerHelper.getAttribute(receiver, messageName);
            } else {
                MetaClass mc = ((GroovyObject) receiver).getMetaClass();
                return mc.getAttribute(senderClass, receiver, messageName, true);
            }
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object getFieldOnSuperSafe(Class senderClass, Object receiver, String messageName) throws Throwable {
        return getFieldOnSuper(senderClass, receiver, messageName);
    }

    public static Object getFieldOnSuperSpreadSafe(Class senderClass, Object receiver, String messageName) throws Throwable {
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getFieldOnSuper(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              field handling super: set
    //  --------------------------------------------------------

    public static void setFieldOnSuper(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        try {
            if (receiver instanceof Class) {
                InvokerHelper.setAttribute(receiver, messageName, messageArgument);
            } else {
                MetaClass mc = ((GroovyObject) receiver).getMetaClass();
                mc.setAttribute(senderClass, receiver, messageName, messageArgument, true, true);
            }
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static void setFieldOnSuperSafe(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        setFieldOnSuper(messageArgument, senderClass, receiver, messageName);
    }

    public static void setFieldOnSuperSpreadSafe(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setFieldOnSuper(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              normal field handling : get
    //  --------------------------------------------------------

    public static Object getField(Class senderClass, Object receiver, String messageName) throws Throwable {
        try {
            return InvokerHelper.getAttribute(receiver, messageName);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object getFieldSafe(Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        return getField(senderClass, receiver, messageName);
    }

    public static Object getFieldSpreadSafe(Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getFieldSafe(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              normal field handling : set
    //  --------------------------------------------------------

    public static void setField(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        try {
            InvokerHelper.setAttribute(receiver, messageName, messageArgument);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static void setFieldSafe(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        setField(messageArgument, senderClass, receiver, messageName);
    }

    public static void setFieldSpreadSafe(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setFieldSafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              normal GroovyObject field handling : get
    //  --------------------------------------------------------

    public static Object getGroovyObjectField(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try {
            return receiver.getMetaClass().getAttribute(senderClass, receiver, messageName, false);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object getGroovyObjectFieldSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        try {
            return receiver.getMetaClass().getAttribute(senderClass, receiver, messageName, false);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object getGroovyObjectFieldSpreadSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getFieldSafe(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              normal field handling : set
    //  --------------------------------------------------------

    public static void setGroovyObjectField(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try {
            receiver.getMetaClass().setAttribute(senderClass, receiver, messageName, messageArgument, false, false);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static void setGroovyObjectFieldSafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        try {
            receiver.getMetaClass().setAttribute(receiver, messageName, messageArgument);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static void setGroovyObjectFieldSpreadSafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setFieldSafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              Property handling super: get
    //  --------------------------------------------------------

    public static Object getPropertyOnSuper(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try {
            return receiver.getMetaClass().getProperty(senderClass, receiver, messageName, true, false);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object getPropertyOnSuperSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        return getPropertyOnSuper(senderClass, receiver, messageName);
    }

    public static Object getPropertyOnSuperSpreadSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getPropertySafe(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              Property handling super: set
    //  --------------------------------------------------------

    public static void setPropertyOnSuper(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try {
            receiver.getMetaClass().setProperty(senderClass, receiver, messageName, messageArgument, true, false);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static void setPropertyOnSuperSafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        setPropertyOnSuper(messageArgument, senderClass, receiver, messageName);
    }

    public static void setPropertyOnSuperSpreadSafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setPropertySafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              normal Property handling : get
    //  --------------------------------------------------------

    public static Object getProperty(Class senderClass, Object receiver, String messageName) throws Throwable {
        try {
            if (receiver instanceof GroovyObject) {
                var groovyObject = (GroovyObject) receiver;
                return groovyObject.getProperty(messageName);
            } else {
                MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
                return metaClass.getProperty(senderClass, receiver, messageName, false, false);
            }
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object getPropertySafe(Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        return getProperty(senderClass, receiver, messageName);
    }

    public static Object getPropertySpreadSafe(Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getPropertySafe(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              normal Property handling : set
    //  --------------------------------------------------------

    public static void setProperty(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        try {
            if (receiver instanceof GroovyObject) {
                var groovyObject = (GroovyObject) receiver;
                groovyObject.setProperty(messageName, messageArgument);
            } else {
                MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
                metaClass.setProperty(senderClass, receiver, messageName, messageArgument, false, false);
            }
        } catch (GroovyRuntimeException gre) {
            if (gre instanceof MissingPropertyException
                    && receiver instanceof GroovyObject
                    && GeneratedClosure.class.isAssignableFrom(senderClass)) {
                do {
                    senderClass = senderClass.getEnclosingClass();
                } while (GeneratedClosure.class.isAssignableFrom(senderClass));
                if (senderClass != receiver.getClass() && senderClass.isInstance(receiver)) { // GROOVY-3142: retry with super sender class?
                    ((GroovyObject) receiver).getMetaClass().setProperty(senderClass, receiver, messageName, messageArgument, false, false);
                    return;
                }
            }
            throw unwrap(gre);
        }
    }

    public static void setPropertySafe(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        setProperty(messageArgument, senderClass, receiver, messageName);
    }

    public static void setPropertySpreadSafe(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setPropertySafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              normal GroovyObject Property handling : get
    //  --------------------------------------------------------

    public static Object getGroovyObjectProperty(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try {
            return receiver.getProperty(messageName);
        } catch (GroovyRuntimeException gre) {
            if (gre instanceof MissingPropertyException && senderClass!=receiver.getClass() && senderClass.isInstance(receiver)) {
                return receiver.getMetaClass().getProperty(senderClass, receiver, messageName, false, false);
            }
            throw unwrap(gre);
        }
    }

    public static Object getGroovyObjectPropertySafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        return getGroovyObjectProperty(senderClass, receiver, messageName);
    }

    public static Object getGroovyObjectPropertySpreadSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        List<Object> answer = new ArrayList<>();
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getPropertySafe(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              normal GroovyObject Property handling : set
    //  --------------------------------------------------------

    public static void setGroovyObjectProperty(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try {
            receiver.setProperty(messageName, messageArgument);
        } catch (GroovyRuntimeException gre) { // GROOVY-3142: retry with super sender class?
            if (gre instanceof MissingPropertyException && senderClass != receiver.getClass() && senderClass.isInstance(receiver)) {
                receiver.getMetaClass().setProperty(senderClass, receiver, messageName, messageArgument, false, false);
                return;
            }
            throw unwrap(gre);
        }
    }

    public static void setGroovyObjectPropertySafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        receiver.setProperty(messageName, messageArgument);
    }

    public static void setGroovyObjectPropertySpreadSafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        for (Iterator<?> it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setPropertySafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  **********************************************************************************
    //  **********************************************************************************
    //  **************          methods not covered by the new MOP          **************
    //  **********************************************************************************
    //  **********************************************************************************

    //  --------------------------------------------------------
    //                     Closures
    //  --------------------------------------------------------

    /**
     * Returns a method closure for the given object and name.
     *
     * @param object the object or class providing the method
     * @param methodName the method(s) of interest
     * @return the resulting Closure
     */
    public static Closure getMethodPointer(final Object object, final String methodName) {
        return InvokerHelper.getMethodPointer(object, methodName);
    }

    // TODO: set sender class
    public static Object invokeClosure(Object closure, Object[] arguments) throws Throwable {
        return invokeMethodN(closure.getClass(), closure, "call", arguments);
    }

    //  --------------------------------------------------------
    //                     type conversion
    //  --------------------------------------------------------

    /**
     * Provides a hook for type coercion of the given object to the required type
     *
     * @param type   of object to convert the given object to
     * @param object the object to be converted
     * @return the original object or a new converted value
     * @throws Throwable if the coercion fails
     */
    public static Object asType(Object object, Class type) throws Throwable {
        if (object == null) object = NullObject.getNullObject();
        return invokeMethodN(object.getClass(), object, "asType", new Object[]{type});
    }

    /**
     * Provides a hook for type casting of the given object to the required type
     *
     * @param type   of object to convert the given object to
     * @param object the object to be converted
     * @return the original object or a new converted value
     * @throws Throwable if the type casting fails
     */
    public static Object castToType(Object object, Class type) throws Throwable {
            return DefaultTypeTransformation.castToType(object, type);
    }

    public static Tuple createTuple(Object[] array) {
        return new Tuple(array);
    }

    public static List createList(Object[] values) {
        return InvokerHelper.createList(values);
    }

    public static Wrapper createPojoWrapper(Object val, Class clazz) {
        return new PojoWrapper(val, clazz);
    }

    public static Wrapper createGroovyObjectWrapper(GroovyObject val, Class clazz) {
        return new GroovyObjectWrapper(val, clazz);
    }

    public static Map createMap(Object[] values) {
        return InvokerHelper.createMap(values);
    }

    public static List createRange(Object from, Object to, boolean exclusiveLeft, boolean exclusiveRight) throws Throwable {
        if (exclusiveLeft && exclusiveRight) {
            if (compareEqual(from, to)) {
                return new EmptyRange((Comparable) from);
            }
            Object tmpFrom;
            if (compareLessThan(from, to)) {
                tmpFrom = invokeMethod0(ScriptBytecodeAdapter.class, from, "next");
            } else {
                tmpFrom = invokeMethod0(ScriptBytecodeAdapter.class, from, "previous");
            }
            // Create an empty range if the difference between from and to is one and they have the same sign. This
            // means that range syntaxes like 5<..<6 will result in an empty range, but 0<..<-1 won't, since the latter
            // is used in list indexing where negative indices count from the end towards the beginning. Note that
            // positive numbers and zeros are considered to have the same sign to make ranges like 0<..<1 be EmptyRanges
            int fromComp = compareTo(from, 0);
            int toComp = compareTo(to, 0);
            boolean sameSign = (fromComp >= 0 && toComp >= 0) || (fromComp < 0 && toComp < 0);
            if (compareEqual(tmpFrom, to) && sameSign) {
                return new EmptyRange((Comparable) from);
            }
        }
        if ((exclusiveLeft || exclusiveRight) && compareEqual(from, to)) {
            return new EmptyRange((Comparable) from);
        }
        if (from instanceof Integer && to instanceof Integer) {
            // Currently, empty ranges where from != to, the range is full exclusive (e.g. 0<..<-1) and from and to
            // have a different sign are constructed as IntRanges. This is because these ranges can still be used to
            // index into lists.
            return new IntRange(!exclusiveLeft, !exclusiveRight, (Integer) from, (Integer) to);
        }
        if (from instanceof Number && to instanceof Number) {
            return new NumberRange(comparableNumber((Number) from), comparableNumber((Number) to), !exclusiveLeft, !exclusiveRight);
        }
        // ObjectRange does not include information about inclusivity, so we need to consider it here
        if (exclusiveRight) {
            if (compareGreaterThan(from, to)) {
                to = invokeMethod0(ScriptBytecodeAdapter.class, to, "next");
            } else {
                to = invokeMethod0(ScriptBytecodeAdapter.class, to, "previous");
            }
        }
        if (exclusiveLeft) {
            if (compareGreaterThan(from, to)) {
                from = invokeMethod0(ScriptBytecodeAdapter.class, from, "previous");
            } else {
                from = invokeMethod0(ScriptBytecodeAdapter.class, from, "next");
            }
        }
        return new ObjectRange((Comparable) from, (Comparable) to);
    }

    // Kept in for backwards compatibility
    public static List createRange(Object from, Object to, boolean inclusive) throws Throwable {
        return createRange(from, to, false, !inclusive);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number & Comparable> T comparableNumber(Number n) {
        return (T) n;
    }

    public static MetaClass initMetaClass(final Object object) {
        return InvokerHelper.getMetaClass(object.getClass());
    }

    //--------------------------------------------------------------------------

    //assert
    public static void assertFailed(final Object expression, final Object message) {
        InvokerHelper.assertFailed(expression, message);
    }

    //isCase
    public static boolean isCase(final Object switchValue, final Object caseExpression) throws Throwable {
        if (caseExpression == null) {
            return switchValue == null;
        }
        return DefaultTypeTransformation.castToBoolean(invokeMethodN(caseExpression.getClass(), caseExpression, "isCase", new Object[]{switchValue}));
    }

    public static boolean isNotCase(final Object switchValue, final Object caseExpression) throws Throwable {
        return !isCase(switchValue, caseExpression);
    }

    //compare
    public static boolean compareIdentical(final Object left, final Object right) {
        return left == right;
    }

    public static boolean compareNotIdentical(final Object left, final Object right) {
        return left != right;
    }

    public static boolean compareEqual(final Object left, final Object right) {
        if (left == right) return true;
        if (left != null && right != null) {
            Class leftClass = left.getClass();
            if (leftClass == right.getClass()) {
                if (leftClass == Integer.class) {
                    return left.equals(right);
                }
                if (leftClass == BigDecimal.class) {
                    return ((BigDecimal) left).compareTo((BigDecimal) right) == 0;
                }
                if (leftClass == BigInteger.class) {
                    return ((BigInteger) left).compareTo((BigInteger) right) == 0;
                }
                if (leftClass == Long.class) {
                    return left.equals(right);
                }
                if (leftClass == Double.class) {
                    return left.equals(right);
                }
                if (leftClass == Float.class) {
                    return left.equals(right);
                }
                if (leftClass == String.class) {
                    return left.equals(right);
                }
                if (leftClass == GStringImpl.class) {
                    return left.equals(right);
                }
            }
        }
        return DefaultTypeTransformation.compareEqual(left, right);
    }

    public static boolean compareNotEqual(final Object left, final Object right) {
        return !compareEqual(left, right);
    }

    public static Integer compareTo(final Object left, final Object right) {
        int answer = DefaultTypeTransformation.compareTo(left, right);
        if (answer == 0) {
            return ZERO;
        }
        return answer > 0 ? ONE : MINUS_ONE;
    }

    public static boolean compareLessThan(final Object left, final Object right) {
        if (left != null && right != null) {
            Class leftClass = left.getClass();
            if (leftClass == right.getClass()) {
                if (leftClass == Integer.class) {
                    return (Integer) left < (Integer) right;
                }
                if (leftClass == BigDecimal.class) {
                    return ((BigDecimal) left).compareTo((BigDecimal) right) < 0;
                }
                if (leftClass == BigInteger.class) {
                    return ((BigInteger) left).compareTo((BigInteger) right) < 0;
                }
                if (leftClass == Long.class) {
                    return (Long) left < (Long) right;
                }
                if (leftClass == Double.class) {
                    return (Double) left < (Double) right;
                }
                if (leftClass == Float.class) {
                    return (Float) left < (Float) right;
                }
            }
        }
        return compareTo(left, right) == MINUS_ONE;
    }

    public static boolean compareLessThanEqual(final Object left, final Object right) {
        if (left != null && right != null) {
            Class leftClass = left.getClass();
            if (leftClass == right.getClass()) {
                if (leftClass == Integer.class) {
                    return (Integer) left <= (Integer) right;
                }
                if (leftClass == BigDecimal.class) {
                    return ((BigDecimal) left).compareTo((BigDecimal) right) <= 0;
                }
                if (leftClass == BigInteger.class) {
                    return ((BigInteger) left).compareTo((BigInteger) right) <= 0;
                }
                if (leftClass == Long.class) {
                    return (Long) left <= (Long) right;
                }
                if (leftClass == Double.class) {
                    return (Double) left <= (Double) right;
                }
                if (leftClass == Float.class) {
                    return (Float) left <= (Float) right;
                }
            }
        }
        Integer result = compareTo(left, right);
        return  result == MINUS_ONE || result == ZERO;
    }

    public static boolean compareGreaterThan(final Object left, final Object right) {
        if (left != null && right != null) {
            Class leftClass = left.getClass();
            if (leftClass == right.getClass()) {
                if (leftClass == Integer.class) {
                    return (Integer) left > (Integer) right;
                }
                if (leftClass == BigDecimal.class) {
                    return ((BigDecimal) left).compareTo((BigDecimal) right) > 0;
                }
                if (leftClass == BigInteger.class) {
                    return ((BigInteger) left).compareTo((BigInteger) right) > 0;
                }
                if (leftClass == Long.class) {
                    return (Long) left > (Long) right;
                }
                if (leftClass == Double.class) {
                    return (Double) left > (Double) right;
                }
                if (leftClass == Float.class) {
                    return (Float) left > (Float) right;
                }
            }
        }
        return compareTo(left, right) == ONE;
    }

    public static boolean compareGreaterThanEqual(final Object left, final Object right) {
        if (left != null && right != null) {
            Class leftClass = left.getClass();
            if (leftClass == right.getClass()) {
                if (leftClass == Integer.class) {
                    return (Integer) left >= (Integer) right;
                }
                if (leftClass == BigDecimal.class) {
                    return ((BigDecimal) left).compareTo((BigDecimal) right) >= 0;
                }
                if (leftClass == BigInteger.class) {
                    return ((BigInteger) left).compareTo((BigInteger) right) >= 0;
                }
                if (leftClass == Long.class) {
                    return (Long) left >= (Long) right;
                }
                if (leftClass == Double.class) {
                    return (Double) left >= (Double) right;
                }
                if (leftClass == Float.class) {
                    return (Float) left >= (Float) right;
                }
            }
        }
        Integer result = compareTo(left, right);
        return  result == ONE || result == ZERO;
    }

    //regexpr
    public static Matcher findRegex(final Object left, final Object right) throws Throwable {
            return InvokerHelper.findRegex(left, right);
    }

    public static boolean matchRegex(final Object left, final Object right) {
        return InvokerHelper.matchRegex(left, right);
    }

    public static Pattern regexPattern(final Object regex) {
        return StringGroovyMethods.bitwiseNegate((CharSequence)regex.toString());
    }

    //spread
    public static Object[] despreadList(final Object[] args, final Object[] spreads, final int[] positions) {
        List<Object> ret = new ArrayList<>();
        int argsPos = 0, spreadsPos = 0;
        for (int position : positions) {
            for (; argsPos < position; ++argsPos) {
                ret.add(args[argsPos]);
            }
            Object value = spreads[spreadsPos];
            if (value == null) {
                ret.add(null);
            } else if (value instanceof List) {
                ret.addAll((List<?>) value);
            } else if (value instanceof Iterable) {
                ((Iterable<?>) value).forEach(ret::add);
            } else if (value instanceof Iterator) {
                ((Iterator<?>) value).forEachRemaining(ret::add);
            } else if (value instanceof BaseStream) {
                ((BaseStream<?,?>) value).iterator().forEachRemaining(ret::add);
            } else if (value.getClass().isArray()) {
                ret.addAll(DefaultTypeTransformation.primitiveArrayToList(value));
            } else {
                String error = "cannot spread the type " + value.getClass().getName() + " with value " + value;
                if (value instanceof Map) {
                    error += ", did you mean to use the spread-map operator instead?";
                }
                throw new IllegalArgumentException(error);
            }
            ++spreadsPos;
        }
        for (; argsPos < args.length; ++argsPos) {
            ret.add(args[argsPos]);
        }
        return ret.toArray();
    }

    public static Object spreadMap(final Object value) {
        return InvokerHelper.spreadMap(value);
    }

    //unary
    public static Object unaryMinus(final Object value) throws Throwable {
            return InvokerHelper.unaryMinus(value);
    }

    public static Object unaryPlus(final Object value) throws Throwable {
        try {
            return InvokerHelper.unaryPlus(value);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object bitwiseNegate(final Object value) throws Throwable {
        try {
            return InvokerHelper.bitwiseNegate(value);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }
}
