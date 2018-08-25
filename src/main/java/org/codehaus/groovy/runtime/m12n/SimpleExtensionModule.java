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
package org.codehaus.groovy.runtime.m12n;

import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An extension module which provides extension methods using a {@link org.codehaus.groovy.runtime.DefaultGroovyMethods}-like implementation, that
 * is to say using static methods defined in an "extension class".
 * <p>
 * For commodity, multiple extension classes may be defined in a single module, including classes used to define new
 * static methods.
 * <p>
 * Modules are required to implement the {@link #getInstanceMethodsExtensionClasses} for classes defining new instance
 * methods, and {@link #getStaticMethodsExtensionClasses()} for classes defining static methods.
 * <p>
 * For example, to define a module adding methods to the {@link String} class, you can write a helper class:
 * <pre>
 * class StringExtension {
 *     public static int count(String self, char c) {
 *         int result = 0;
 *         for (int i=0;i&lt;self.length(); i++) {
 *             if (self.charAt(i)==c) result++;
 *         }
 *         return result;
 *     }
 * }
 * </pre>
 * <p>
 * This class defines a single static method taking the string instance as first argument, allowing to define
 * a new instance method on the String class: <pre>String#count(char c)</pre>.
 * <p>
 * To define a new static method on a class, as the static modifier is already used for instance methods, you must use
 * another helper class, for example:
 * <p>
 * <pre>
 * class StaticStringExtension {
 *     public static void foo(String self) { System.out.println("foo"); }
 * }
 * </pre>
 * <p>
 * The first argument of the method is only used to tell the class for which we add a static method. You can now define
 * an extension module:
 * <p>
 * <pre>
 * class MyStringModule extends SimpleExtensionModule {
 *     // ...
 *
 *     public List&lt;Class&gt; getInstanceMethodsExtensionClasses() {
 *         return Collections.singletonList(StringExtension.class);
 *     }
 *
 *     public List&lt;Class&gt; getStaticMethodsExtensionClasses() {
 *         return Collections.singletonList(StaticStringExtension.class);
 *     }
 * }
 * </pre>
 *
 * @author Cedric Champeau
 * @since 2.0.0
 */
public abstract class SimpleExtensionModule extends ExtensionModule {

    private static final Logger LOG = Logger.getLogger(SimpleExtensionModule.class.getName());

    public SimpleExtensionModule(final String moduleName, final String moduleVersion) {
        super(moduleName, moduleVersion);
    }


    @Override
    public List<MetaMethod> getMetaMethods() {
        List<MetaMethod> metaMethods = new LinkedList<>();
        List<Class> extensionClasses = getInstanceMethodsExtensionClasses();
        for (Class extensionClass : extensionClasses) {
            try {
                createMetaMethods(extensionClass, metaMethods, false);
            } catch (LinkageError e) {
                LOG.warning("Module ["+getName()+"] - Unable to load extension class ["+extensionClass+"] due to ["+e.getMessage()+"]. Maybe this module is not supported by your JVM version.");
            }
        }
        extensionClasses = getStaticMethodsExtensionClasses();
        for (Class extensionClass : extensionClasses) {
            try {
                createMetaMethods(extensionClass, metaMethods, true);
            } catch (LinkageError e) {
                LOG.warning("Module ["+getName()+"] - Unable to load extension class ["+extensionClass+"] due to ["+e.getMessage()+"]. Maybe this module is not supported by your JVM version.");
            }
        }
        return metaMethods;
    }

    private static void createMetaMethods(final Class extensionClass, final List<MetaMethod> metaMethods, final boolean isStatic) {
        CachedClass cachedClass = ReflectionCache.getCachedClass(extensionClass);
        CachedMethod[] methods = cachedClass.getMethods();
        for (CachedMethod method : methods) {
            if (method.isStatic() && method.isPublic() && method.getParamsCount() > 0) {
                // an extension method is found
                metaMethods.add(isStatic?new NewStaticMetaMethod(method) : new NewInstanceMetaMethod(method));
            }
        }
    }

    /**
     * @return the list of classes defining new instance methods.
     */
    public abstract List<Class> getInstanceMethodsExtensionClasses();

    /**
     * @return the list of classes defining new static methods.
     */
    public abstract List<Class> getStaticMethodsExtensionClasses();
}
