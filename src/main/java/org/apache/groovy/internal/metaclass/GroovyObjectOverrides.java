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
package org.apache.groovy.internal.metaclass;

import groovy.lang.GroovyObject;
import groovy.transform.Internal;

import java.lang.reflect.Method;

/**
 * Whether a class overrides the {@code setProperty} or {@code getProperty} that
 * {@link GroovyObject} provides, which decides how the runtime dispatches a dynamic
 * property write or read to its instances.
 * <p>
 * An override, declared by the class or inherited from a superclass, receives every
 * access. The interface defaults only hand the access to the metaclass, so the runtime
 * calls the metaclass itself instead and tells it which class is making the access;
 * that is what lets, say, a superclass reach its own private field on an instance of
 * a subclass.
 * <p>
 * Not user API.
 */
@Internal
public final class GroovyObjectOverrides {

    private static final ClassValue<Boolean> SET_PROPERTY = new ClassValue<>() {
        @Override
        protected Boolean computeValue(final Class<?> type) {
            try {
                return !type.getMethod("setProperty", String.class, Object.class).isDefault();
            } catch (NoSuchMethodException | LinkageError ignore) {
                // the public methods cannot be read: a native image without reflection
                // metadata for the class throws NoSuchMethodException (or, built with exact
                // metadata, MissingReflectionRegistrationError, a LinkageError), and a public
                // method naming a type absent at run time throws NoClassDefFoundError
                return Boolean.TRUE;
            }
        }
    };

    private static final ClassValue<Boolean> GET_PROPERTY = new ClassValue<>() {
        @Override
        protected Boolean computeValue(final Class<?> type) {
            try {
                return !type.getMethod("getProperty", String.class).isDefault();
            } catch (NoSuchMethodException | LinkageError ignore) {
                return Boolean.TRUE; // as for setProperty
            }
        }
    };

    private static final ClassValue<Boolean> CUSTOM_GET_PROPERTY = new ClassValue<>() {
        @Override
        protected Boolean computeValue(final Class<?> type) {
            try {
                Method method = type.getMethod("getProperty", String.class);
                return !method.isSynthetic() && method.getAnnotation(Internal.class) == null;
            } catch (NoSuchMethodException | LinkageError ignore) {
                return Boolean.TRUE; // as for setProperty
            }
        }
    };

    private GroovyObjectOverrides() {
    }

    /**
     * Tells whether instances of the given class take {@code setProperty(String, Object)}
     * from anywhere but the {@link GroovyObject} interface default. Computed once per class.
     * <p>
     * When reflection cannot read the public methods of the class, the answer is
     * {@code true}. A caller then invokes {@link GroovyObject#setProperty(String, Object)},
     * which reaches whichever implementation the object really has; if that is the
     * default, the write still reaches the metaclass, but without the sender, so it is
     * resolved as if the object's own class were making it. Answering {@code false} would
     * instead hand the write straight to the metaclass, bypassing any override.
     *
     * @param type a class that implements {@link GroovyObject}
     * @return {@code true} if a write should be passed to the object's own
     *         {@code setProperty} rather than to its metaclass
     */
    public static boolean overridesSetProperty(final Class<?> type) {
        return SET_PROPERTY.get(type);
    }

    /**
     * Tells whether instances of the given class take {@code getProperty(String)} from
     * anywhere but the {@link GroovyObject} interface default: the criterion of
     * {@code ScriptBytecodeAdapter.getProperty}. Computed once per class, and {@code true}
     * when reflection cannot read the public methods of the class, for the reasons given
     * for {@link #overridesSetProperty(Class)}.
     *
     * @param type a class that implements {@link GroovyObject}
     * @return {@code true} if a read should be passed to the object's own
     *         {@code getProperty} rather than to its metaclass
     * @see #hasCustomGetProperty(Class)
     */
    public static boolean overridesGetProperty(final Class<?> type) {
        return GET_PROPERTY.get(type);
    }

    /**
     * Tells whether instances of the given class take {@code getProperty(String)} from an
     * implementation that is neither synthetic nor marked {@link Internal}: the criterion
     * of the invokedynamic property-read call sites. Unlike
     * {@link #overridesGetProperty(Class)}, it treats such an implementation as generated,
     * like the interface default, which carries {@code @Internal} for this reason.
     * Computed once per class, and {@code true} when reflection cannot read the public
     * methods of the class, for the reasons given for {@link #overridesSetProperty(Class)}.
     *
     * @param type a class that implements {@link GroovyObject}
     * @return {@code true} if a read should be passed to the object's own
     *         {@code getProperty} rather than to its metaclass
     */
    public static boolean hasCustomGetProperty(final Class<?> type) {
        return CUSTOM_GET_PROPERTY.get(type);
    }
}
