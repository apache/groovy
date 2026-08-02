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

import org.apache.groovy.util.HiddenClassDefiner;
import org.codehaus.groovy.runtime.Reflector;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflector creation helper. This class is used to define the {@link Reflector} classes.
 *
 * <p>For each {@link ClassLoader} such a loader will be created by {@code MetaClass}.
 * Special about this loader is that it knows the classes from the Groovy runtime.
 * The {@link Reflector} class is resolved in different ways: during the definition
 * of a class {@link Reflector} will resolve to the {@link Reflector} class of the
 * runtime, even if there is another {@link Reflector} class in the parent loader.
 * After the new class is defined {@link Reflector} will resolve like other Groovy
 * classes. This loader is able to resolve all Groovy classes even if the parent
 * does not know them, but the parent serves first (Reflector during a class
 * definition is different).
 *
 * <p>Since Groovy 6.0 this loader preferentially defines each generated
 * Reflector class as a <em>hidden nestmate</em> of {@code ReflectorLoader}
 * itself (via a {@link Lookup} captured in this class — the correct
 * caller-sensitive way to obtain full privilege). Hidden classes are
 * non-discoverable and eligible for eager unloading. When that fails the
 * classic {@link ClassLoader#defineClass} path is used transparently.
 *
 * <p>{@code name} passed to {@link #defineClass(String, byte[], ProtectionDomain)}
 * is the cache / fallback binary name only: a successful hidden definition
 * yields a JVM-assigned name (with a {@code /} suffix) that is not loadable
 * by {@code name}.
 */
public class ReflectorLoader extends ClassLoader {

    /**
     * Full-privilege lookup for this class, captured at class-init time.
     * Used as the nest host for hidden Reflector subclasses.
     */
    private static final Lookup LOOKUP = MethodHandles.lookup();

    private boolean inDefine = false;
    private final Map<String, Class<?>> loadedClasses = new HashMap<>();
    private final ClassLoader delegatationLoader;

    private static final String REFLECTOR = Reflector.class.getName();

    /**
     * Tries to find a Groovy class. Uses the delegation loader to load classes when available.
     *
     * @param name the fully qualified name of the class to find
     * @return the class if found
     * @throws ClassNotFoundException if the class cannot be found
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (delegatationLoader == null) return super.findClass(name);
        return delegatationLoader.loadClass(name);
    }

    /**
     * Loads a class per name. Unlike a normal {@code loadClass} this version
     * behaves differently during a class definition. In that case it checks
     * if the class we want to load is {@link Reflector} and returns that
     * class if the check is successful. If it is not during a class definition
     * it just calls the super class version of {@code loadClass}.
     *
     * @param name    of the class to load
     * @param resolve is {@code true} if the class should be resolved
     * @see Reflector
     * @see ClassLoader#loadClass(String, boolean)
     */
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (inDefine) {
            if (name.equals(REFLECTOR)) return Reflector.class;
        }
        return super.loadClass(name, resolve);
    }

    /**
     * Helper method to define Reflector classes.
     *
     * <p>Prefers a hidden nestmate of {@code ReflectorLoader} and falls back to
     * the classic {@link ClassLoader#defineClass} path when that is not possible.
     *
     * <p>{@code name} is stored in the per-loader cache and used for the visible
     * fallback definition; it is <em>not</em> the runtime name of a successfully
     * defined hidden class.
     *
     * <p>This method sets the {@code inDefine} flag to {@code true} during
     * class definition to ensure {@link Reflector} is resolved correctly.
     *
     * @param name     cache key / fallback binary name of the Reflector class
     * @param bytecode the bytecode of the Reflector class
     * @param domain   the protection domain for the fallback visible-class
     *                 definition; not used when the hidden-class path succeeds
     * @return the newly defined class
     */
    public synchronized Class<?> defineClass(
            final String name,
            final byte[] bytecode,
            final ProtectionDomain domain) {
        inDefine = true;
        try {
            final Class<?> cls = defineReflectorClass(name, bytecode, domain);
            loadedClasses.put(name, cls);
            if (!cls.isHidden()) {
                // Hidden classes do not need resolveClass(); visible classes do.
                resolveClass(cls);
            }
            return cls;
        } finally {
            inDefine = false;
        }
    }

    /**
     * Creates a new ReflectorLoader with the specified parent class loader.
     * This loader is responsible for defining Reflector classes that can resolve
     * the Reflector class from the Groovy runtime correctly.
     *
     * @param parent the parent class loader (should never be {@code null})
     */
    public ReflectorLoader(ClassLoader parent) {
        super(parent);
        delegatationLoader = getClass().getClassLoader();
    }

    /**
     * Retrieves a previously defined Reflector class by name from the cache.
     *
     * <p>The key is the logical binary name passed to
     * {@link #defineClass(String, byte[], ProtectionDomain)}, not the
     * JVM-assigned hidden-class name.
     *
     * @param name the fully qualified name of the Reflector class
     * @return the Reflector class if it has been defined, or {@code null} otherwise
     */
    public synchronized Class<?> getLoadedClass(String name) {
        return loadedClasses.get(name);
    }

    /**
     * Generates the fully qualified name of a Reflector class for the given class.
     *
     * <p>For {@code java.*} classes the name is prefixed with {@code "gjdk."} to
     * avoid the restricted {@code java.} package namespace. Array types are
     * handled specially with a {@code "_GroovyReflectorArray"} suffix and nesting
     * level indicators.
     *
     * @param theClass the class for which to generate the Reflector name
     * @return the fully qualified name of the Reflector class
     */
    static String getReflectorName(Class<?> theClass) {
        String className = theClass.getName();
        if (className.startsWith("java.")) {
            String packagePrefix = "gjdk.";
            String name = packagePrefix + className + "_GroovyReflector";
            if (theClass.isArray()) {
                Class<?> clazz = theClass;
                int level = 0;
                while (clazz.isArray()) {
                    clazz = clazz.getComponentType();
                    level++;
                }
                String componentName = clazz.getName();
                name = packagePrefix + componentName + "_GroovyReflectorArray";
                if (level > 1) name += level;
            }
            return name;
        } else {
            String name = className.replace('$', '_') + "_GroovyReflector";
            if (theClass.isArray()) {
                Class<?> clazz = theClass;
                int level = 0;
                while (clazz.isArray()) {
                    clazz = clazz.getComponentType();
                    level++;
                }
                String componentName = clazz.getName();
                name = componentName.replace('$', '_') + "_GroovyReflectorArray";
                if (level > 1) name += level;
            }
            return name;
        }
    }

    /**
     * Prefers a hidden nestmate of this loader class (caller-owned
     * {@link #LOOKUP}); falls back to a visible class under {@code name}.
     */
    private Class<?> defineReflectorClass(
            final String name,
            final byte[] bytecode,
            final ProtectionDomain domain) {
        final Class<?> hidden = HiddenClassDefiner.tryDefineNestmate(LOOKUP, bytecode, false);
        if (hidden != null) {
            return hidden;
        }
        return defineClass(name, bytecode, 0, bytecode.length, domain);
    }
}
