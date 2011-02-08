/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.reflection;

import org.codehaus.groovy.runtime.callsite.GroovySunClassLoader;
import org.codehaus.groovy.runtime.callsite.CallSite;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;
import java.security.AccessController;
import java.security.PrivilegedAction;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;

public class ClassLoaderForClassArtifacts extends ClassLoader {
    public final SoftReference<Class> klazz;
    private final Set<String> allocatedNames = new HashSet<String>();

    public ClassLoaderForClassArtifacts(Class klazz) {
        super(klazz.getClassLoader());
        this.klazz = new SoftReference<Class> (klazz);
    }

    public Class define (String name, byte [] bytes) {
        Class cls = defineClass(name, bytes, 0, bytes.length, klazz.get().getProtectionDomain());
        resolveClass(cls);
        return cls;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        Class cls = findLoadedClass(name);
        if (cls != null)
          return cls;

        if (GroovySunClassLoader.sunVM != null) {
            cls = GroovySunClassLoader.sunVM.doesKnow(name);
            if (cls != null)
              return cls;
        }

        return super.loadClass(name);
    }

    public synchronized String createClassName(Method method) {
        final String name;
        final String clsName = klazz.get().getName();
        if (clsName.startsWith("java."))
          name = clsName.replace('.','_') + "$" + method.getName();
        else
          name = clsName + "$" + method.getName();

        if (!allocatedNames.contains(name)) {
          allocatedNames.add(name);
          return name;
        }

        for (int i = 0; ; i++) {
            String newName = name + "$" + i;
            if (!allocatedNames.contains(newName)) {
              allocatedNames.add(newName);
              return newName;
            }
        }
    }

    public Constructor defineClassAndGetConstructor(final String name, final byte[] bytes) {
        final Class cls = AccessController.doPrivileged( new PrivilegedAction<Class>(){
            public Class run() {
                return define(name, bytes);
            }
        });

        if (cls != null) {
            try {
                return cls.getConstructor(CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class, Constructor.class);
            } catch (NoSuchMethodException e) { //
            }
        }
        return null;
    }
}
