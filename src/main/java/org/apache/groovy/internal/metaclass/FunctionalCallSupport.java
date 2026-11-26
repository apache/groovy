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
package org.apache.groovy.internal.metaclass;

import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.transform.Internal;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.ConversionHandler;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;

/**
 * Finds the method that call syntax, {@code f(args)} or {@code f.call(args)},
 * stands for on a functional value that has no {@code call} method.
 * <p>
 * Only two kinds of object are functional values:
 * <ul>
 * <li>a lambda or method reference, that is, an instance of a class created by
 *     {@code java.lang.invoke.LambdaMetafactory};</li>
 * <li>a Groovy coercion proxy, that is, a {@link Proxy} whose invocation handler
 *     is a {@link ConversionHandler}, as {@code closure as F} and {@code map as F}
 *     create. The handler belongs to the instance: a proxy class is shared by
 *     every proxy for the same interfaces, so this is not a property of the class.</li>
 * </ul>
 * The method is the single abstract method of the functional interface the value
 * implements, as {@link CachedSAMClass#getSAMMethod(Class)} defines it, which is the
 * rule a closure is coerced by. No other object qualifies: not a collection, a thread
 * or any other instance of a class that happens to implement a functional interface.
 * <p>
 * The meta-class consults this class only when a call is about to fail with a
 * {@link MissingMethodException}; the invokedynamic selector uses it to
 * bind such a call at the call site.
 * <p>
 * Not user API.
 */
@Internal
public final class FunctionalCallSupport {

    private static final String CALL = "call";

    /**
     * The abstract method call syntax stands for, by class. Empty for a class
     * whose instances are never functional values.
     */
    private static final ClassValue<Optional<CachedMethod>> CALL_METHODS = new ClassValue<>() {
        @Override
        protected Optional<CachedMethod> computeValue(final Class<?> type) {
            try {
                return Optional.ofNullable(findCallMethod(type));
            } catch (LinkageError | RuntimeException e) {
                return Optional.empty(); // the call fails as it would without this lookup
            }
        }
    };

    private FunctionalCallSupport() {
    }

    /**
     * Returns the method that {@code receiver.call(arguments)} stands for.
     *
     * @param receiver  the object call syntax is used on
     * @param arguments the arguments of the call
     * @return the abstract method of the receiver's functional interface, or {@code null}
     *         if the receiver is not a functional value or the method does not accept the arguments
     */
    public static CachedMethod getCallMethod(final Object receiver, final Object[] arguments) {
        if (receiver == null) return null;
        Class<?> type = receiver.getClass();
        CachedMethod method = CALL_METHODS.get(type).orElse(null);
        if (method == null || !acceptsArguments(method, arguments)) return null;
        if (Proxy.isProxyClass(type) && !isCoercionProxy(receiver)) return null;
        return method;
    }

    /**
     * Whether the object is a proxy created by a Groovy closure or map coercion.
     *
     * @param object the object to test, which may be {@code null}
     */
    public static boolean isCoercionProxy(final Object object) {
        return object != null && Proxy.isProxyClass(object.getClass())
                && Proxy.getInvocationHandler(object) instanceof ConversionHandler;
    }

    //--------------------------------------------------------------------------

    private static CachedMethod findCallMethod(final Class<?> type) {
        if (GroovyObject.class.isAssignableFrom(type) || !(isLambdaClass(type) || Proxy.isProxyClass(type))) {
            return null;
        }
        Method target = null;
        for (Class<?> face : type.getInterfaces()) {
            Method sam = CachedSAMClass.getSAMMethod(face);
            if (sam == null) continue; // a marker interface, for example
            if (target == null) {
                target = sam;
            } else if (!target.getName().equals(sam.getName())
                    || !Arrays.equals(target.getParameterTypes(), sam.getParameterTypes())) {
                return null; // no single abstract method
            }
        }
        if (target == null || CALL.equals(target.getName())) {
            return null;
        }
        // an interface this runtime cannot reach, of an unexported package for example, stays out
        CachedMethod method = CachedMethod.find(target);
        return method != null && method.getCachedMethod().trySetAccessible() ? method : null;
    }

    /**
     * Whether the class was created by {@code LambdaMetafactory}. Such a class is
     * hidden and synthetic, and is named after the class that contains the lambda.
     * Being hidden is not enough: other generated classes, proxies among them, can be.
     */
    private static boolean isLambdaClass(final Class<?> type) {
        return type.isHidden() && type.isSynthetic() && type.getName().contains("$$Lambda");
    }

    /**
     * Whether the method accepts the arguments, by the rule method selection
     * uses, except that a missing argument does not stand for {@code null}.
     */
    private static boolean acceptsArguments(final CachedMethod method, final Object[] arguments) {
        if (arguments.length == 0 && method.getParamsCount() == 1 && !method.isVargsMethod()) {
            return false;
        }
        return method.isValidMethod(MetaClassHelper.convertToTypeArray(arguments));
    }
}
