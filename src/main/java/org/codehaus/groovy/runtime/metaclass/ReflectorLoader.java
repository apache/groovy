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

import org.codehaus.groovy.runtime.Reflector;

import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflector creation helper historically used to define {@link Reflector}
 * subclasses.
 *
 * <p><strong>Deprecated since 6.0.0.</strong> Production MetaClass / call-site
 * paths no longer construct or use this loader; the only remaining references
 * are tests. Prefer modern reflective / method-handle dispatch. Retained for
 * binary compatibility only; will be removed in a future major release.
 *
 * <p>Special about this loader is that it knows the classes from the Groovy
 * runtime. During class definition {@link Reflector} resolves to the runtime's
 * {@link Reflector} even if a different one exists in the parent loader.
 *
 * @deprecated No production callers remain; kept only for binary compatibility.
 */
@Deprecated(since = "6.0.0", forRemoval = true)
public class ReflectorLoader extends ClassLoader {

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
     * Defines a Reflector class via {@link ClassLoader#defineClass} and caches
     * it under {@code name}.
     *
     * <p>Sets {@code inDefine} during definition so {@link Reflector} resolves
     * to the runtime class.
     *
     * @param name     cache key and binary name of the Reflector class
     * @param bytecode the bytecode of the Reflector class
     * @param domain   the protection domain
     * @return the newly defined class
     */
    public synchronized Class<?> defineClass(
            final String name,
            final byte[] bytecode,
            final ProtectionDomain domain) {
        inDefine = true;
        try {
            final Class<?> cls = defineClass(name, bytecode, 0, bytecode.length, domain);
            resolveClass(cls);
            loadedClasses.put(name, cls);
            return cls;
        } finally {
            inDefine = false;
        }
    }

    /**
     * Creates a new ReflectorLoader with the specified parent class loader.
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
     * avoid the restricted {@code java.} package namespace. Array types use a
     * {@code "_GroovyReflectorArray"} suffix (with nesting level when {@code > 1}).
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
}
