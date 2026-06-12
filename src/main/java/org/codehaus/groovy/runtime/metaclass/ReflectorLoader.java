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
 * Reflector creation helper. This class is used to define the Reflector classes.
 * For each ClassLoader such a loader will be created by MetaClass.
 * Special about this loader is, that it knows the classes form the 
 * Groovy Runtime. The Reflector class is resolved in different ways: During
 * the definition of a class Reflector will resolve to the Reflector class of
 * the runtime, even if there is another Reflector class in the parent loader.
 * After the new class is defined Reflector will resolve like other Groovy
 * classes. This loader is able to resolve all Groovy classes even if the
 * parent does not know them, but the parent serves first (Reflector during a
 * class definition is different). 
 */
public class ReflectorLoader extends ClassLoader {
    private boolean inDefine = false;
    private final Map loadedClasses = new HashMap();
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
    protected Class findClass(String name) throws ClassNotFoundException {
        if (delegatationLoader==null) return super.findClass(name);
        return delegatationLoader.loadClass(name);
    }

    /**
     * Loads a class per name. Unlike a normal loadClass this version
     * behaves different during a class definition. In that case it
     * checks if the class we want to load is Reflector and returns 
     * class if the check is successful. If it is not during a class
     * definition it just calls the super class version of loadClass. 
     * 
     * @param name of the class to load
     * @param resolve is true if the class should be resolved
     * @see Reflector
     * @see ClassLoader#loadClass(String, boolean)
     */
    @Override
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (inDefine) {
            if (name.equals(REFLECTOR)) return Reflector.class;
        }
        return super.loadClass(name, resolve);
    }

    /**
     * Helper method to define Reflector classes. This method sets the inDefine flag to true
     * during class definition to ensure Reflector is resolved correctly, then resolves the
     * newly defined class and stores it in the loadedClasses cache.
     *
     * @param name the fully qualified name of the Reflector class
     * @param bytecode the bytecode of the Reflector class
     * @param domain the protection domain for the class
     * @return the newly defined class
     */
    public synchronized Class defineClass(String name, byte[] bytecode, ProtectionDomain domain) {
        inDefine = true;
        Class c = defineClass(name, bytecode, 0, bytecode.length, domain);
        loadedClasses.put(name,c); 
        resolveClass(c);
        inDefine = false;
        return c;
    }

    /**
     * Creates a new ReflectorLoader with the specified parent class loader.
     * This loader is responsible for defining Reflector classes that can resolve
     * the Reflector class from the Groovy runtime correctly.
     *
     * @param parent the parent class loader (should never be null)
     */
    public ReflectorLoader(ClassLoader parent) {
        super(parent);
        delegatationLoader = getClass().getClassLoader();
    }

    /**
     * Retrieves a previously defined Reflector class by name from the cache.
     *
     * @param name the fully qualified name of the Reflector class
     * @return the Reflector class if it has been defined, or null otherwise
     */
    public synchronized Class getLoadedClass(String name) {
        return (Class)loadedClasses.get(name);
    }

    /**
     * Generates the fully qualified name of a Reflector class for the given class.
     * For java.* classes, the name is prefixed with "gjdk."; otherwise the package
     * and class name are used. Array types are handled specially with "_GroovyReflectorArray"
     * suffix and nesting level indicators.
     *
     * @param theClass the class for which to generate the Reflector name
     * @return the fully qualified name of the Reflector class
     */
    static String getReflectorName(Class theClass) {
        String className = theClass.getName();
        if (className.startsWith("java.")) {
            String packagePrefix = "gjdk.";
            String name = packagePrefix + className + "_GroovyReflector";
            if (theClass.isArray()) {
                   Class clazz = theClass;
                   name = packagePrefix;
                   int level = 0;
                   while (clazz.isArray()) {
                      clazz = clazz.getComponentType();
                      level++;
                   }
                String componentName = clazz.getName();
                name = packagePrefix + componentName + "_GroovyReflectorArray";
                if (level>1) name += level;
            }
            return name;
        }
        else {
            String name = className.replace('$','_') + "_GroovyReflector";
            if (theClass.isArray()) {
                   Class clazz = theClass;
                   int level = 0;
                   while (clazz.isArray()) {
                      clazz = clazz.getComponentType();
                      level++;
                   }
                String componentName = clazz.getName();
                name = componentName.replace('$','_') + "_GroovyReflectorArray";
                if (level>1) name += level;
            }
            return name;
        }
    }
}
