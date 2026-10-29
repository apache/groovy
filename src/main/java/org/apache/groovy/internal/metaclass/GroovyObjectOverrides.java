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
import java.util.function.Predicate;

/**
 * Whether a class overrides the {@code setProperty} or {@code getProperty} that
 * {@link GroovyObject} provides, which decides how the runtime dispatches a dynamic
 * property write or read to its instances.
 * <p>
 * An override receives every access. The {@code GroovyObject} defaults only hand the access
 * to the metaclass, so the runtime calls the metaclass itself instead and tells it which
 * class is making the access; that is what lets, say, a superclass reach its own private
 * field on an instance of a subclass.
 * <p>
 * For {@link #overridesSetProperty(Class)} and {@link #overridesGetProperty(Class)}, an
 * override is any implementation other than the {@code GroovyObject} default itself,
 * whether the class declares it, inherits it from a superclass, or takes it from a default
 * method of another interface, such as one extending {@code GroovyObject}.
 * {@link #hasCustomGetProperty(Class)} also sets aside implementations that are synthetic
 * or marked {@link Internal}.
 * <p>
 * Reads have two criteria, which differ on purpose. {@code ScriptBytecodeAdapter.getProperty}
 * uses {@link #overridesGetProperty(Class)}; the property-read call sites, invokedynamic and
 * classic, use {@link #hasCustomGetProperty(Class)}. They disagree in one case: a
 * {@code getProperty} other than the {@code GroovyObject} default that is synthetic or
 * marked {@code @Internal}, which the adapter passes the read to and the call sites go
 * around to the metaclass. That split is older than this class, and giving one path the
 * other's criterion would change which implementation answers such a read.
 * <p>
 * Writes have one criterion, {@link #overridesSetProperty(Class)}, which
 * {@code ScriptBytecodeAdapter.setProperty} and {@code setGroovyObjectProperty} and the
 * invokedynamic property-write sites share. No write path sets aside a synthetic or
 * {@code @Internal} {@code setProperty}, and classic compilation has no property-write call
 * sites: its writes go through {@code ScriptBytecodeAdapter}.
 * <p>
 * Not user API.
 */
@Internal
public final class GroovyObjectOverrides {

    private static final ClassValue<Boolean> SET_PROPERTY =
            answer("setProperty", new Class<?>[]{String.class, Object.class},
                    method -> method.getDeclaringClass() != GroovyObject.class);

    private static final ClassValue<Boolean> GET_PROPERTY =
            answer("getProperty", new Class<?>[]{String.class},
                    method -> method.getDeclaringClass() != GroovyObject.class);

    private static final ClassValue<Boolean> CUSTOM_GET_PROPERTY =
            answer("getProperty", new Class<?>[]{String.class},
                    method -> !method.isSynthetic() && method.getAnnotation(Internal.class) == null);

    private GroovyObjectOverrides() {
    }

    /**
     * Caches, per class, whether the public method with the given signature passes the
     * given test, and {@code true} when that method cannot be read.
     */
    private static ClassValue<Boolean> answer(final String name, final Class<?>[] parameterTypes, final Predicate<Method> test) {
        return new ClassValue<>() {
            @Override
            protected Boolean computeValue(final Class<?> type) {
                try {
                    return test.test(type.getMethod(name, parameterTypes));
                } catch (NoSuchMethodException | LinkageError ignore) {
                    // the public methods cannot be read: a native image without reflection
                    // metadata for the class throws NoSuchMethodException (or, built with exact
                    // metadata, MissingReflectionRegistrationError, a LinkageError), and a public
                    // method naming a type absent at run time throws NoClassDefFoundError
                    return Boolean.TRUE;
                }
            }
        };
    }

    /**
     * Tells whether instances of the given class take {@code setProperty(String, Object)}
     * from an implementation other than the {@link GroovyObject} default, including a
     * default method of another interface. Computed once per class.
     * <p>
     * When reflection cannot read the public methods of the class, the answer is
     * {@code true}. A caller then invokes {@link GroovyObject#setProperty(String, Object)},
     * which reaches whichever implementation the object really has; if that is the
     * {@code GroovyObject} default, the write still reaches the metaclass, but without the sender, so it is
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
     * Tells whether instances of the given class take {@code getProperty(String)} from an
     * implementation other than the {@link GroovyObject} default, including a default method
     * of another interface. This is the criterion of {@code ScriptBytecodeAdapter.getProperty}. Computed once per class, and {@code true}
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
     * of the property-read call sites, invokedynamic and classic. Unlike
     * {@link #overridesGetProperty(Class)}, it treats an implementation that is synthetic or
     * marked {@code @Internal} as generated, like the {@code GroovyObject} default, which
     * carries {@code @Internal} for this reason.
     * Computed once per class, and {@code true} when reflection cannot read the public
     * methods of the class, for the reasons given for {@link #overridesSetProperty(Class)}:
     * the invokedynamic and classic call sites alike then pass the read to the object's own
     * {@code getProperty} rather than to a metaclass path that would skip a real override.
     *
     * @param type a class that implements {@link GroovyObject}
     * @return {@code true} if a read should be passed to the object's own
     *         {@code getProperty} rather than to its metaclass
     */
    public static boolean hasCustomGetProperty(final Class<?> type) {
        return CUSTOM_GET_PROPERTY.get(type);
    }
}
