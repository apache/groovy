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

/**
 * Caches reflection information about a single field for efficient access and modification.
 * <p>
 * Extends {@link MetaProperty} to integrate with the meta-programming framework.
 * Handles lazy field accessibility and provides property-like access to field values.
 */
public class CachedField extends MetaProperty {

    /**
     * Constructs a {@code CachedField} for the given Java field.
     *
     * @param field the field to cache reflection information for
     */
    public CachedField(final Field field) {
        super(field.getName(), field.getType());
        this.field = field;
    }

    private final Field field;
    private boolean madeAccessible;
    private void makeAccessible() {
        ReflectionUtils.makeAccessibleInPrivilegedAction(field);
        madeAccessible = true;
    }

    /**
     * Returns the underlying Java {@code Field} object, making it accessible if necessary.
     *
     * @return the cached field with accessibility ensured
     */
    public Field getCachedField() {
        if (!madeAccessible) makeAccessible();
        return field;
    }

    /**
     * Returns the class that declares this field.
     *
     * @return the declaring class
     */
    public Class getDeclaringClass() {
        return field.getDeclaringClass();
    }

    /**
     * Checks whether the underlying field has the specified annotation.
     * Unlike {@link #getCachedField()}, this does not trigger accessibility changes.
     *
     * @since 6.0.0
     */
    public boolean isAnnotationPresent(Class<? extends java.lang.annotation.Annotation> annotationType) {
        return field.isAnnotationPresent(annotationType);
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

    /**
     * Creates a method handle that provides getter access to this field via MethodHandles API.
     * Attempts to unreflect the field, automatically making it accessible if needed.
     *
     * @param lookup the method handles lookup context
     * @return a method handle providing getter access to this field
     * @throws IllegalAccessException if the field cannot be accessed even with accessibility adjustments
     */
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
