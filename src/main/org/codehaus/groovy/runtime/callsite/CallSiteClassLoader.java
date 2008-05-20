/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.runtime.callsite;

import org.codehaus.groovy.reflection.CachedMethod;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class CallSiteClassLoader extends ClassLoader {
    public final Class parent;

    private final static Set<String> knownClasses = new HashSet<String>();
    static {
        Collections.addAll(knownClasses
                , "org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite"
                , "org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite"
                , "org.codehaus.groovy.runtime.callsite.StaticMetaMethodSite"
                , "org.codehaus.groovy.runtime.callsite.CallSite"
                , "org.codehaus.groovy.runtime.callsite.CallSiteArray"
                , "groovy.lang.MetaMethod"
                , "groovy.lang.MetaClassImpl"
                );
    }

    private final Set<String> allocatedNames = new HashSet<String> ();

    public CallSiteClassLoader(Class parent) {
        super(parent.getClassLoader());
        this.parent = parent;
    }

    public Class define (String name, byte [] bytes) {
        Class cls = defineClass(name, bytes, 0, bytes.length, parent.getProtectionDomain());
        resolveClass(cls);
        return cls;
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class cls = findLoadedClass(name);
        if (cls != null)
          return cls;

        if (GroovySunClassLoader.sunVM != null) {
            final Class aClass = GroovySunClassLoader.sunVM.doesKnow(name);
            if (aClass != null)
              return aClass;
        }

        if (knownClasses.contains(name))
          return getClass().getClassLoader().loadClass(name);
        else {
            try {
                return super.loadClass(name, resolve);
            }
            catch (ClassNotFoundException e) {
                return getClass().getClassLoader().loadClass(name);
            }
        }
    }

    public synchronized String createCallSiteClassName(CachedMethod cachedMethod) {
        final String name;
        final String clsName = parent.getName();
        if (clsName.startsWith("java."))
          name = clsName.replace('.','_') + "$" + cachedMethod.getName();
        else
          name = clsName + "$" + cachedMethod.getName();

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
}
