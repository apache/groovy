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

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassLoaderForClassArtifacts extends ClassLoader {
    public final SoftReference<Class> klazz;
    private final AtomicInteger classNamesCounter = new AtomicInteger(-1);

    public ClassLoaderForClassArtifacts(Class klazz) {
        super(klazz.getClassLoader());
        this.klazz = new SoftReference<>(klazz);
    }

    public Class define(String name, byte[] bytes) {
        Class cls = defineClass(name, bytes, 0, bytes.length, klazz.get().getProtectionDomain());
        resolveClass(cls);
        return cls;
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        Class cls = findLoadedClass(name);
        if (cls != null)
            return cls;

        return super.loadClass(name);
    }

    public String createClassName(Method method) {
        return createClassName(method.getName());
    }

    public String createClassName(String methodName) {
        final String name;
        final String clsName = klazz.get().getName();
        if (clsName.startsWith("java."))
            name = clsName.replace('.', '_') + "$" + methodName;
        else
            name = clsName + "$" + methodName;
        int suffix = classNamesCounter.getAndIncrement();
        return suffix == -1 ? name : name + "$" + suffix;
    }

    public Constructor defineClassAndGetConstructor(final String name, final byte[] bytes) {
        final Class cls = definePrivileged(name, bytes);

        if (cls != null) {
            try {
                return cls.getConstructor(CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class, Constructor.class);
            } catch (NoSuchMethodException e) { //
            }
        }
        return null;
    }

    @SuppressWarnings("removal") // TODO a future Groovy version should perform the operation not as a privileged action
    private Class definePrivileged(String name, byte[] bytes) {
        return java.security.AccessController.doPrivileged((PrivilegedAction<Class>) () -> define(name, bytes));
    }
}
