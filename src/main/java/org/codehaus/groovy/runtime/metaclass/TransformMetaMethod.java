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

/**
 * A MetaMethod implementation useful for implementing coercion based invocations.
 * This method wraps another metamethod and allows for argument transformation or
 * other custom handling during method invocation.
 */
public class TransformMetaMethod extends MetaMethod {

    /**
     * The underlying metamethod being wrapped
     */
    private final MetaMethod metaMethod;

    /**
     * Constructs a new TransformMetaMethod.
     *
     * @param metaMethod the metamethod to wrap
     */
    public TransformMetaMethod(final MetaMethod metaMethod) {
        super(metaMethod.getNativeParameterTypes());
        setParametersTypes(metaMethod.getParameterTypes());
        this.metaMethod = metaMethod;
    }

    /**
     * Returns the modifiers of the wrapped metamethod.
     *
     * @return the method modifiers
     */
    @Override
    public int getModifiers() {
        return metaMethod.getModifiers();
    }

    /**
     * Returns the name of the wrapped metamethod.
     *
     * @return the method name
     */
    @Override
    public String getName() {
        return metaMethod.getName();
    }

    /**
     * Returns the return type of the wrapped metamethod.
     *
     * @return the return type
     */
    @Override
    public Class getReturnType() {
        return metaMethod.getReturnType();
    }

    /**
     * Returns the class that declares the wrapped metamethod.
     *
     * @return the declaring class
     */
    @Override
    public CachedClass getDeclaringClass() {
        return metaMethod.getDeclaringClass();
    }

    /**
     * Invokes the wrapped metamethod on the given object with the specified arguments.
     *
     * @param object the object to invoke the method on
     * @param arguments the method arguments
     * @return the method return value
     */
    @Override
    public Object invoke(final Object object, final Object[] arguments) {
        return metaMethod.invoke(object, arguments);
    }

    /**
     * Invokes the wrapped metamethod without argument coercion.
     * This method skips the default argument coercion performed by the parent invoke method.
     *
     * @param object the object to invoke the method on
     * @param arguments the method arguments
     * @return the method return value
     */
    @Override
    public Object doMethodInvoke(final Object object, final Object[] arguments) {
        // no coerceArgumentsToClasses
        try {
            return invoke(object, arguments);
        } catch (final Exception ex) {
            throw processDoMethodInvokeException(ex, object, arguments);
        }
    }
}
