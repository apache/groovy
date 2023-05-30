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

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * This class is a general adapter to adapt a closure to any Java interface.
 */
public class ConvertedClosure extends ConversionHandler implements Serializable {

    private static final long serialVersionUID = 1162833713450835227L;

    private final String methodName;

    /**
     * @throws IllegalArgumentException if closure is null
     */
    public ConvertedClosure(final Closure closure) {
        this(closure, null);
    }

    /**
     * @throws IllegalArgumentException if closure is null
     */
    public ConvertedClosure(final Closure closure, final String methodName) {
        super(closure); this.methodName = methodName;
    }

    @Override
    public Object invokeCustom(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (methodName != null && !methodName.equals(method.getName())) return null;
        Object result = ((Closure)getDelegate()).call(args);
        return result;
    }
}
