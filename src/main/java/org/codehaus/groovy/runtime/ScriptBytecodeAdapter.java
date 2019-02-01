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
import groovy.lang.GroovySystem;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static helper class to interface bytecode and runtime
 */
public class ScriptBytecodeAdapter {
    public static final Object[] EMPTY_ARGS = {};
    private static final Integer ZERO = Integer.valueOf(0);
    private static final Integer MINUS_ONE = Integer.valueOf(-1);
    private static final Integer ONE = Integer.valueOf(1);

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
        Object result = null;
        boolean intercepting = receiver instanceof GroovyInterceptable;
        try {
            try {
                // if it's a pure interceptable object (even intercepting toString(), clone(), ...)
                if (intercepting) {
                    result = receiver.invokeMethod(messageName, messageArguments);
                }
                //else if there's a statically typed method or a GDK method
                else {
                    result = receiver.getMetaClass().invokeMethod(senderClass, receiver, messageName, messageArguments, false, true);
                }
            } catch (MissingMethodException e) {
                if (e instanceof MissingMethodExecutionFailed) {
                    throw (MissingMethodException)e.getCause();
                } else if (!intercepting && receiver.getClass() == e.getType() && e.getMethod().equals(messageName)) {
                    // in case there's nothing else, invoke the object's own invokeMethod()
                    result = receiver.invokeMethod(messageName, messageArguments);
                } else {
                    throw e;
                }
            }
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
        return result;
    }

    public static Object invokeMethodOnCurrentNSafe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        return invokeMethodOnCurrentN(senderClass, receiver, messageName, messageArguments);
    }

    public static Object invokeMethodOnCurrentNSpreadSafe(Class senderClass, GroovyObject receiver, String messageName, Object[] messageArguments) throws Throwable {
        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
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
        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
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
        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
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
        MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(which);
        try {
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
        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
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
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
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
        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
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
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setFieldSafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              normal GroovyObject field handling : get
    //  --------------------------------------------------------

    public static Object getGroovyObjectField(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try  {
            return receiver.getMetaClass().getAttribute(receiver, messageName);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object getGroovyObjectFieldSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        try {
            return receiver.getMetaClass().getAttribute(receiver, messageName);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object getGroovyObjectFieldSpreadSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getFieldSafe(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              normal field handling : set
    //  --------------------------------------------------------

    public static void setGroovyObjectField(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try {
            receiver.getMetaClass().setAttribute(receiver, messageName, messageArgument);
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
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setFieldSafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              Property handling super: get
    //  --------------------------------------------------------

    public static Object getPropertyOnSuper(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        return invokeMethodOnSuperN(senderClass, receiver, "getProperty", new Object[]{messageName});
    }

    public static Object getPropertyOnSuperSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        return getPropertyOnSuper(senderClass, receiver, messageName);
    }

    public static Object getPropertyOnSuperSpreadSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getPropertySafe(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              Property handling super: set
    //  --------------------------------------------------------

    public static void setPropertyOnSuper(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        try {
            InvokerHelper.setAttribute(receiver, messageName, messageArgument);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static void setPropertyOnSuperSafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        setPropertyOnSuper(messageArgument, senderClass, receiver, messageName);
    }

    public static void setPropertyOnSuperSpreadSafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setPropertySafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              normal Property handling : get
    //  --------------------------------------------------------

    public static Object getProperty(Class senderClass, Object receiver, String messageName) throws Throwable {
        try {
            return InvokerHelper.getProperty(receiver, messageName);
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

        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            answer.add(getPropertySafe(senderClass, it.next(), messageName));
        }
        return answer;
    }

    //  --------------------------------------------------------
    //              normal Property handling : set
    //  --------------------------------------------------------

    public static void setProperty(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        try {
            if (receiver==null) receiver=NullObject.getNullObject();
            InvokerHelper.setProperty(receiver, messageName, messageArgument);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static void setPropertySafe(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        setProperty(messageArgument, senderClass, receiver, messageName);
    }

    public static void setPropertySpreadSafe(Object messageArgument, Class senderClass, Object receiver, String messageName) throws Throwable {
        if (receiver == null) return;

        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
            setPropertySafe(messageArgument, senderClass, it.next(), messageName);
        }
    }

    //  --------------------------------------------------------
    //              normal GroovyObject Property handling : get
    //  --------------------------------------------------------

    public static Object getGroovyObjectProperty(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        return receiver.getProperty(messageName);
    }

    public static Object getGroovyObjectPropertySafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return null;
        return getGroovyObjectProperty(senderClass, receiver, messageName);
    }

    public static Object getGroovyObjectPropertySpreadSafe(Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return null;

        List answer = new ArrayList();
        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
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
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static void setGroovyObjectPropertySafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return;
        receiver.setProperty(messageName, messageArgument);
    }

    public static void setGroovyObjectPropertySpreadSafe(Object messageArgument, Class senderClass, GroovyObject receiver, String messageName) throws Throwable {
        if (receiver == null) return;

        for (Iterator it = InvokerHelper.asIterator(receiver); it.hasNext();) {
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
     * Returns the method pointer for the given object name
     *
     * @param object the object containing the method
     * @param methodName the name of the method of interest
     * @return the resulting Closure
     */
    public static Closure getMethodPointer(Object object, String methodName) {
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

    public static List createRange(Object from, Object to, boolean inclusive) throws Throwable {
        if (from instanceof Integer && to instanceof Integer) {
            int ifrom = (Integer) from;
            int ito = (Integer) to;
            if (inclusive || ifrom != ito) {
                return new IntRange(inclusive, ifrom, ito);
            } // else fall through for EmptyRange
        }
        if (!inclusive && compareEqual(from, to)) {
            return new EmptyRange((Comparable) from);
        }
        if (from instanceof Number && to instanceof Number) {
            return new NumberRange(comparableNumber((Number) from), comparableNumber((Number) to), inclusive);
        }
        if (!inclusive) {
            if (compareGreaterThan(from, to)) {
                to = invokeMethod0(ScriptBytecodeAdapter.class, to, "next");
            } else {
                to = invokeMethod0(ScriptBytecodeAdapter.class, to, "previous");
            }
        }

        return new ObjectRange((Comparable) from, (Comparable) to);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number & Comparable> T comparableNumber(Number n) {
        return (T) n;
    }

    //assert
    public static void assertFailed(Object expression, Object message) {
        InvokerHelper.assertFailed(expression, message);
    }

    //isCase
    //TODO: set sender class
    public static boolean isCase(Object switchValue, Object caseExpression) throws Throwable {
        if (caseExpression == null) {
            return switchValue == null;
        }
        return DefaultTypeTransformation.castToBoolean(invokeMethodN(caseExpression.getClass(), caseExpression, "isCase", new Object[]{switchValue}));
    }

    //compare
    public static boolean compareIdentical(Object left, Object right) {
        return left == right;
    }

    public static boolean compareNotIdentical(Object left, Object right) {
        return left != right;
    }

    public static boolean compareEqual(Object left, Object right) {
        if (left==right) return true;
        Class<?> leftClass = left==null?null:left.getClass();
        Class<?> rightClass = right==null?null:right.getClass();
        if (leftClass ==Integer.class && rightClass==Integer.class) {
            return left.equals(right);
        }
        if (leftClass ==Double.class && rightClass==Double.class) {
            return left.equals(right);
        }
        if (leftClass ==Long.class && rightClass==Long.class) {
            return left.equals(right);
        }
        return DefaultTypeTransformation.compareEqual(left, right);
    }

    public static boolean compareNotEqual(Object left, Object right) {
        return !compareEqual(left, right);
    }

    public static Integer compareTo(Object left, Object right) {
        int answer = DefaultTypeTransformation.compareTo(left, right);
        if (answer == 0) {
            return ZERO;
        } else {
            return answer > 0 ? ONE : MINUS_ONE;
        }
    }

    public static boolean compareLessThan(Object left, Object right) {
        Class<?> leftClass = left==null?null:left.getClass();
        Class<?> rightClass = right==null?null:right.getClass();
        if (leftClass ==Integer.class && rightClass==Integer.class) {
            return (Integer) left < (Integer) right;
        }
        if (leftClass ==Double.class && rightClass==Double.class) {
            return (Double) left < (Double) right;
        }
        if (leftClass ==Long.class && rightClass==Long.class) {
            return (Long) left < (Long) right;
        }
        return compareTo(left, right).intValue() < 0;
    }

    public static boolean compareLessThanEqual(Object left, Object right) {
        Class<?> leftClass = left==null?null:left.getClass();
        Class<?> rightClass = right==null?null:right.getClass();
        if (leftClass ==Integer.class && rightClass==Integer.class) {
            return (Integer) left <= (Integer) right;
        }
        if (leftClass ==Double.class && rightClass==Double.class) {
            return (Double) left <= (Double) right;
        }
        if (leftClass ==Long.class && rightClass==Long.class) {
            return (Long) left <= (Long) right;
        }
        return compareTo(left, right).intValue() <= 0;
    }

    public static boolean compareGreaterThan(Object left, Object right) {
        Class<?> leftClass = left==null?null:left.getClass();
        Class<?> rightClass = right==null?null:right.getClass();
        if (leftClass ==Integer.class && rightClass==Integer.class) {
            return (Integer) left > (Integer) right;
        }
        if (leftClass ==Double.class && rightClass==Double.class) {
            return (Double) left > (Double) right;
        }
        if (leftClass ==Long.class && rightClass==Long.class) {
            return (Long) left > (Long) right;
        }
        return compareTo(left, right).intValue() > 0;
    }

    public static boolean compareGreaterThanEqual(Object left, Object right) {
        Class<?> leftClass = left==null?null:left.getClass();
        Class<?> rightClass = right==null?null:right.getClass();
        if (leftClass ==Integer.class && rightClass==Integer.class) {
            return (Integer) left >= (Integer) right;
        }
        if (leftClass ==Double.class && rightClass==Double.class) {
            return (Double) left >= (Double) right;
        }
        if (leftClass ==Long.class && rightClass==Long.class) {
            return (Long) left >= (Long) right;
        }
        return compareTo(left, right).intValue() >= 0;
    }

    //regexpr
    public static Pattern regexPattern(Object regex) {
        return StringGroovyMethods.bitwiseNegate((CharSequence)regex.toString());
    }

    public static Matcher findRegex(Object left, Object right) throws Throwable {
            return InvokerHelper.findRegex(left, right);
    }

    public static boolean matchRegex(Object left, Object right) {
        return InvokerHelper.matchRegex(left, right);
    }

    //spread expressions
    public static Object[] despreadList(Object[] args, Object[] spreads, int[] positions) {
        List ret = new ArrayList();
        int argsPos = 0;
        int spreadPos = 0;
        for (int pos = 0; pos < positions.length; pos++) {
            for (; argsPos < positions[pos]; argsPos++) {
                ret.add(args[argsPos]);
            }
            Object value = spreads[spreadPos];
            if (value == null) {
                ret.add(null);
            } else if (value instanceof List) {
                ret.addAll((List) value);
            } else if (value.getClass().isArray()) {
                ret.addAll(DefaultTypeTransformation.primitiveArrayToList(value));
            } else {
                String error = "cannot spread the type " + value.getClass().getName() + " with value " + value;
                if (value instanceof Map) {
                    error += ", did you mean to use the spread-map operator instead?";
                }
                throw new IllegalArgumentException(error);
            }
            spreadPos++;
        }
        for (; argsPos < args.length; argsPos++) {
            ret.add(args[argsPos]);
        }
        return ret.toArray();
    }

    public static Object spreadMap(Object value) {
        return InvokerHelper.spreadMap(value);
    }

    public static Object unaryMinus(Object value) throws Throwable {
            return InvokerHelper.unaryMinus(value);
    }

    public static Object unaryPlus(Object value) throws Throwable {
        try {
            return InvokerHelper.unaryPlus(value);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static Object bitwiseNegate(Object value) throws Throwable {
        try {
            return InvokerHelper.bitwiseNegate(value);
        } catch (GroovyRuntimeException gre) {
            throw unwrap(gre);
        }
    }

    public static MetaClass initMetaClass(Object object) {
        return InvokerHelper.getMetaClass(object.getClass());
    }
}
