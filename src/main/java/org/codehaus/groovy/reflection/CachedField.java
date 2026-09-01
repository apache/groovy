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
    private volatile boolean madeAccessible;  // outcome of the deep-reflection attempt
    private volatile boolean accessAttempted; // Groovy's deep-reflection path has been tried
    private volatile MethodHandle getter, setter; // deep-reflection handles, which are
        // caller-independent and so may be cached; a handle obtained from a caller's
        // lookup carries that caller's access rights and is never cached

    /**
     * Tries once to establish deep-reflection access to the field, remembering
     * either outcome: a failed attempt (strongly encapsulated declaring class)
     * cannot succeed later. Lock-free: a concurrent duplicate attempt is benign
     * (both threads force the same {@code Field}, idempotently), and the attempt
     * is recorded only after it completed, so a thread that observes
     * {@code accessAttempted} also observes the outcome and the field's
     * accessibility.
     */
    private boolean makeAccessible() {
        if (!accessAttempted) {
            madeAccessible = ReflectionUtils.makeAccessibleInPrivilegedAction(field).isPresent();
            accessAttempted = true;
        }
        return madeAccessible;
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
     * When deep-reflection access can be established ({@code setAccessible} on the field),
     * the resulting handle is caller-independent and is created once and cached; otherwise
     * the given caller lookup decides, and its handle -- which carries that caller's access
     * rights -- is never cached.
     *
     * @param lookup the method handles lookup context of the caller
     * @return a method handle providing getter access to this field
     * @throws IllegalAccessException if neither deep reflection nor the caller's lookup can access the field
     */
    public MethodHandle asAccessMethod(final MethodHandles.Lookup lookup) throws IllegalAccessException {
        MethodHandle h = getter;
        if (h == null && makeAccessible()) {
            h = getter = MethodHandles.lookup().unreflectGetter(field); // cannot fail: access was forced
        }
        return h != null ? h : lookup.unreflectGetter(field);
    }

    /**
     * Creates a method handle that writes this field via the MethodHandles API,
     * the mutating counterpart of {@link #asAccessMethod(MethodHandles.Lookup)},
     * with the same caching policy. No deep-reflection handle is created for a
     * final field: writing it must keep failing the caller's access check.
     *
     * @param lookup the method handles lookup context of the caller
     * @return a setter handle of type {@code (declaringClass, fieldType)void}
     * @throws IllegalAccessException if neither deep reflection nor the caller's lookup can write the field
     * @since 6.0.0
     */
    public MethodHandle asWriteAccessMethod(final MethodHandles.Lookup lookup) throws IllegalAccessException {
        MethodHandle h = setter;
        if (h == null && !isFinal() && makeAccessible()) {
            h = setter = MethodHandles.lookup().unreflectSetter(field); // cannot fail: access was forced
        }
        return h != null ? h : lookup.unreflectSetter(field);
    }
}
