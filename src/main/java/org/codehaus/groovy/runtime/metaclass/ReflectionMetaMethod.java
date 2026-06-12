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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.lang.reflect.InvocationTargetException;

/**
 * A MetaMethod implementation that wraps a reflection-based method call.
 * This class provides access to a cached method and delegates method invocations
 * to the underlying cached method.
 * <p>
 * This implementation is for internal use by the Groovy runtime.
 */
public class ReflectionMetaMethod extends MetaMethod {
    /**
     * The underlying cached method that this MetaMethod wraps
     */
    protected final CachedMethod method;

    /**
     * Constructs a new ReflectionMetaMethod.
     *
     * @param method the cached method to wrap
     */
    public ReflectionMetaMethod(CachedMethod method) {
        this.method = method;
        setParametersTypes(method.getParameterTypes());
    }

    /**
     * Returns the modifiers of the wrapped cached method.
     *
     * @return the method modifiers
     */
    @Override
    public int getModifiers() {
        return method.getModifiers();
    }

    /**
     * Returns the name of the wrapped cached method.
     *
     * @return the method name
     */
    @Override
    public String getName() {
        return method.getName();
    }

    /**
     * Returns the return type of the wrapped cached method.
     *
     * @return the return type
     */
    @Override
    public Class getReturnType() {
        return method.getReturnType();
    }

    /**
     * Returns the class that declares the wrapped cached method.
     *
     * @return the declaring class
     */
    @Override
    public CachedClass getDeclaringClass() {
        return method.cachedClass;
    }

    /**
     * Invokes the wrapped cached method on the given object with the specified arguments.
     *
     * @param object the object to invoke the method on
     * @param arguments the method arguments
     * @return the method return value
     */
    @Override
    public Object invoke(Object object, Object[] arguments) {
        try {
            return method.setAccessible().invoke(object, arguments);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InvokerInvocationException(e);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof RuntimeException ? (RuntimeException)e.getCause() : new InvokerInvocationException(e);
        }
    }

    /**
     * Returns a string representation of the wrapped cached method.
     *
     * @return the string representation
     */
    @Override
    public String toString () {
        return method.toString();
    }

    @Override
    protected Class[] getPT() {
        return method.getNativeParameterTypes();
    }

    /**
     * Returns the cached method wrapped by this MetaMethod.
     *
     * @return the cached method
     */
    public MetaMethod getCachedMethod() {
        return method;
    }
}
