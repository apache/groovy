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

    public ClosureStaticMetaMethod(String name, Class declaringClass, Closure c, Class[] paramTypes) {
        super(paramTypes);
        this.callable = c;
        this.declaringClass = ReflectionCache.getCachedClass(declaringClass);
        this.name = name;
    }
    
    public Object invoke(Object object, Object[] arguments) {
        Closure cloned = (Closure) callable.clone();
        cloned.setDelegate(object);
        return cloned.call(arguments);
    }

    public int getModifiers() {
        return Modifier.PUBLIC | Modifier.STATIC;
    }

    public String getName() {
        return name;
    }

    public Class getReturnType() {
        return Object.class;
    }

    public CachedClass getDeclaringClass() {
        return this.declaringClass;
    }

    /**
     * Retrieves the closure that is invoked by this MetaMethod
     *
     * @return The closure
     */
    public Closure getClosure() {
        return this.callable;
    }
}
