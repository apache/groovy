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
package org.codehaus.groovy.reflection;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaProperty;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToType;

public class CachedField extends MetaProperty {

    public CachedField(final Field field) {
        super(field.getName(), field.getType());
        this.field = field;
    }

    private final Field field;
    private boolean madeAccessible;
    private void makeAccessible() {
        ReflectionUtils.makeAccessibleInPrivilegedAction(field);
        AccessPermissionChecker.checkAccessPermission(field);
        madeAccessible = true;
    }

    public Field getCachedField() {
        if (!madeAccessible) makeAccessible();
        return field;
    }

    public Class getDeclaringClass() {
        return field.getDeclaringClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getModifiers() {
        return field.getModifiers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(final Object object) {
        var field = getCachedField();
        try {
            return field.get(object);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new GroovyRuntimeException("Cannot get the property '" + name + "'.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public  void  setProperty(final Object object, Object newValue) {
        if (isFinal()) {
            throw new GroovyRuntimeException("Cannot set the property '" + name + "' because the backing field is final.");
        }
        newValue = castToType(newValue, field.getType());
        var field = getCachedField();
        try {
            field.set(object, newValue);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new GroovyRuntimeException("Cannot set the property '" + name + "'.", e);
        }
    }

    public MethodHandle asAccessMethod(final MethodHandles.Lookup lookup) throws IllegalAccessException {
        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            if (!madeAccessible) {
                try {
                    makeAccessible();
                    return lookup.unreflectGetter(field);
                } catch (IllegalAccessException ignore) {
                } catch (Throwable t) {
                    e.addSuppressed(t);
                }
            }
            throw e;
        }
    }
}
