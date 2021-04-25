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
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedConstructor;
import org.codehaus.groovy.reflection.ReflectionCache;

import java.util.Arrays;

/**
 * Represents a method on an object using a closure, which can be invoked at any
 * time.
 */
public class MethodClosure extends Closure {

    public static boolean ALLOW_RESOLVE; // choose readObject/readResolve return/throw
    public static final String ANY_INSTANCE_METHOD_EXISTS = "anyInstanceMethodExists";
    public static final String NEW = "new";

    //

    private static final long serialVersionUID = -2491254866810955844L;

    /**
     * Indicates if this may be related to an instance method.
     */
    private boolean anyInstanceMethodExists;

    private final String method;

    //--------------------------------------------------------------------------

    public MethodClosure(final Object owner, final String method) {
        super(owner);
        this.method = method;
        this.maximumNumberOfParameters = 0;
        this.parameterTypes = MetaClassHelper.EMPTY_TYPE_ARRAY;

        Class<?> clazz = owner.getClass() == Class.class ? (Class<?>) owner : owner.getClass();

        if (NEW.equals(method)) {
            if (clazz.isArray()) {
                Class<?>[] sizeTypes = new Class[ArrayTypeUtils.dimension(clazz)];
                Arrays.fill(sizeTypes, int.class);
                setParameterTypesAndNumber(sizeTypes);
            } else {
                for (CachedConstructor c : ReflectionCache.getCachedClass(clazz).getConstructors()) {
                    setParameterTypesAndNumber(c.getNativeParameterTypes());
                }
            }
        } else {
            for (MetaMethod m : InvokerHelper.getMetaClass(clazz).respondsTo(owner, method)) {
                setParameterTypesAndNumber(makeParameterTypes(owner, m));
                if (!m.isStatic()) {
                    this.anyInstanceMethodExists = true;
                }
            }
        }
    }

    private void setParameterTypesAndNumber(final Class[] newParameterTypes) {
        if (!(newParameterTypes.length > this.maximumNumberOfParameters)) {
            return;
        }
        this.maximumNumberOfParameters = newParameterTypes.length;
        this.parameterTypes = newParameterTypes;
    }

    /*
     * Create a new array of parameter type.
     *
     * If the owner is a class instance(e.g. String) and the method is instance method,
     * we expand the original array of parameter type by inserting the owner at the first place of the expanded array
     */
    private Class[] makeParameterTypes(final Object owner, final MetaMethod m) {
        Class[] newParameterTypes;

        if (owner instanceof Class && !m.isStatic()) {
            Class[] nativeParameterTypes = m.getNativeParameterTypes();
            newParameterTypes = new Class[nativeParameterTypes.length + 1];

            System.arraycopy(nativeParameterTypes, 0, newParameterTypes, 1, nativeParameterTypes.length);
            newParameterTypes[0] = (Class) owner;
        } else {
            newParameterTypes = m.getNativeParameterTypes();
        }

        return newParameterTypes;
    }

    //--------------------------------------------------------------------------

    public String getMethod() {
        return method;
    }

    @Override
    public Object getProperty(final String property) {
        switch (property) {
          case "method":
            return getMethod();
          case ANY_INSTANCE_METHOD_EXISTS:
            return anyInstanceMethodExists;
          default:
            return super.getProperty(property);
        }
    }

    // TODO: This method seems to be never called..., because MetaClassImpl.invokeMethod will intercept calls and return the result.
    protected Object doCall(final Object arguments) {
        return InvokerHelper.invokeMethod(getOwner(), getMethod(), arguments);
    }

    private void readObject(final java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        if (ALLOW_RESOLVE) {
            stream.defaultReadObject();
        }
        throw new UnsupportedOperationException();
    }

    private Object readResolve() {
        if (ALLOW_RESOLVE) {
            return this;
        }
        throw new UnsupportedOperationException();
    }
}
