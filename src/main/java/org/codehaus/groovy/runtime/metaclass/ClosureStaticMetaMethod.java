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

import groovy.lang.Closure;
import groovy.lang.ClosureInvokingMethod;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;

import java.lang.reflect.Modifier;

/**
 * This class represents a MetaMethod that is a closure that pretends to be a static method.
 * It is used by ExpandoMetaClass to allow addition of static methods defined as closures
 *
 * @since 01.1
 */
public class ClosureStaticMetaMethod extends MetaMethod implements ClosureInvokingMethod {

    private final Closure callable;
    private final CachedClass declaringClass;
    private final String name;

    /**
     *
     * @param name The name of the MetaMethod
     * @param declaringClass The class which declared the MetaMethod
     * @param c The closure that this ClosureMetaMethod will invoke when called
     */
    public ClosureStaticMetaMethod(String name, Class declaringClass, Closure c) {
        this(name, declaringClass, c, c.getParameterTypes());
    }

    /**
     * Constructs a new ClosureStaticMetaMethod with the specified parameter types.
     *
     * @param name The name of the MetaMethod
     * @param declaringClass The class which declared the MetaMethod
     * @param c The closure that this ClosureStaticMetaMethod will invoke when called
     * @param paramTypes The parameter types for the closure method
     */
    public ClosureStaticMetaMethod(String name, Class declaringClass, Closure c, Class[] paramTypes) {
        super(paramTypes);
        this.callable = c;
        this.declaringClass = ReflectionCache.getCachedClass(declaringClass);
        this.name = name;
    }

    /**
     * Invokes the static closure method with the given arguments. The closure's delegate is set
     * to the object parameter.
     *
     * @param object the object to set as the closure's delegate
     * @param arguments the arguments to pass to the closure
     * @return the result of the closure invocation
     */
    @Override
    public Object invoke(Object object, Object[] arguments) {
        Closure cloned = (Closure) callable.clone();
        cloned.setDelegate(object);
        return cloned.call(arguments);
    }

    /**
     * Returns the modifiers for this meta method (PUBLIC | STATIC).
     *
     * @return the modifiers indicating this is a public static method
     */
    @Override
    public int getModifiers() {
        return Modifier.PUBLIC | Modifier.STATIC;
    }

    /**
     * Returns the name of this static meta method.
     *
     * @return the name of the method
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the return type of this method.
     *
     * @return Object.class, as closures return Object
     */
    @Override
    public Class getReturnType() {
        return Object.class;
    }

    /**
     * Returns the cached class that declares this meta method.
     *
     * @return the declaring class
     */
    @Override
    public CachedClass getDeclaringClass() {
        return this.declaringClass;
    }

    /**
     * Retrieves the closure that is invoked by this MetaMethod
     *
     * @return The closure
     */
    @Override
    public Closure getClosure() {
        return this.callable;
    }
}
