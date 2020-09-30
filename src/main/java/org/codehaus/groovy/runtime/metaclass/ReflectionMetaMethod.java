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

public class ReflectionMetaMethod extends MetaMethod {
    protected final CachedMethod method;

    public ReflectionMetaMethod(CachedMethod method) {
        this.method = method;
        setParametersTypes(method.getParameterTypes());
    }

    @Override
    public int getModifiers() {
        return method.getModifiers();
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public Class getReturnType() {
        return method.getReturnType();
    }

    @Override
    public CachedClass getDeclaringClass() {
        return method.cachedClass;
    }

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

    public String toString () {
        return method.toString();
    }

    @Override
    protected Class[] getPT() {
        return method.getNativeParameterTypes();
    }

    public MetaMethod getCachedMethod() {
        return method;
    }
}
