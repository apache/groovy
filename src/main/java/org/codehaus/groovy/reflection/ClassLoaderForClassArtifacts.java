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

import org.apache.groovy.util.HiddenClassDefiner;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A specialized {@link ClassLoader} used to define per-class <em>artifact</em>
 * classes such as generated meta-method dispatchers.
 *
 * <p>Since Groovy 6.0 this loader first attempts to define each artifact as a
 * <em>hidden nestmate</em> of the target class via
 * {@link HiddenClassDefiner#tryDefineNestmate(Class, byte[], boolean)} (best-effort
 * {@code privateLookupIn} into the target). Benefits when that succeeds:
 * <ul>
 *   <li>non-discoverable by name;</li>
 *   <li>same defining loader / package / protection domain as the target;</li>
 *   <li>nestmate of the target;</li>
 *   <li>weak lifecycle — eligible for eager unloading once the {@link Class}
 *       object is unreachable.</li>
 * </ul>
 *
 * <p>If hidden-class definition is refused (module not open, target unsuitable,
 * …) the loader falls back to {@link ClassLoader#defineClass} with the
 * protection domain captured at construction time.
 */
public class ClassLoaderForClassArtifacts extends ClassLoader {

    /** Soft reference to the class for which artifacts are generated. */
    public final SoftReference<Class> klazz;

    /** Binary name of the target, retained even if {@link #klazz} is cleared. */
    private final String className;

    /**
     * Protection domain of the target, captured strongly so fallback
     * {@code defineClass} stays deterministic if the soft reference is cleared.
     */
    private final ProtectionDomain protectionDomain;

    private final AtomicInteger classNamesCounter = new AtomicInteger(-1);

    /**
     * Creates a new artifact class loader for the specified class.
     *
     * @param klazz the class whose artifact classes are to be defined via this loader
     */
    public ClassLoaderForClassArtifacts(final Class klazz) {
        super(klazz.getClassLoader());
        this.klazz = new SoftReference<>(klazz);
        this.className = klazz.getName();
        this.protectionDomain = klazz.getProtectionDomain();
    }

    /**
     * Defines a class from bytecode, preferring a hidden nestmate of the target.
     *
     * @param name  the binary name used for the fallback (visible-class) path
     * @param bytes the class-file bytes
     * @return the defined class
     */
    public Class define(final String name, final byte[] bytes) {
        final Class<?> host = klazz.get();
        if (host != null) {
            // Foreign host: best-effort privateLookupIn (see HiddenClassDefiner).
            final Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(host, bytes, false);
            if (hidden != null) {
                return hidden;
            }
        }

        final Class<?> cls = defineClass(name, bytes, 0, bytes.length, protectionDomain);
        resolveClass(cls);
        return cls;
    }

    /**
     * Defines a class from bytecode and returns the <em>public</em> constructor
     * matching the given parameter types, or {@code null} if definition or
     * lookup fails.
     *
     * <p>Uses {@link Class#getConstructor(Class[])} so the returned constructor
     * is always publicly accessible (same contract as before hidden-class support).
     *
     * @param name           the binary name (for fallback visible-class definition)
     * @param bytes          the class-file bytes
     * @param parameterTypes the constructor parameter types to look up
     * @return the matching public constructor, or {@code null}
     */
    public Constructor defineClassAndGetConstructor(
            final String name,
            final byte[] bytes,
            final Class<?>... parameterTypes) {
        try {
            final Class<?> cls = define(name, bytes);
            return cls.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class loadClass(final String name) throws ClassNotFoundException {
        final Class cls = findLoadedClass(name);
        if (cls != null) {
            return cls;
        }
        return super.loadClass(name);
    }

    /**
     * Generates a unique class name for an artifact associated with the given method.
     *
     * @param method the method for which the artifact is generated
     * @return a unique class name
     */
    public String createClassName(final Method method) {
        return createClassName(method.getName());
    }

    /**
     * Generates a unique class name for an artifact associated with the given
     * method name.
     *
     * <p>For classes in the {@code java.*} package hierarchy the name is
     * prefixed to avoid the restricted {@code java.} namespace. The counter
     * suffix ensures uniqueness when multiple artifacts share the same logical
     * name.
     *
     * @param methodName the method name component of the artifact class name
     * @return a unique class name
     */
    public String createClassName(final String methodName) {
        final String base = className.startsWith("java.")
                ? className.replace('.', '_') + "$" + methodName
                : className + "$" + methodName;
        final int suffix = classNamesCounter.getAndIncrement();
        return suffix == -1 ? base : base + "$" + suffix;
    }
}
