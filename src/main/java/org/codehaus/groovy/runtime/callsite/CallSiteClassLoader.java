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
package org.codehaus.groovy.runtime.callsite;

import org.codehaus.groovy.reflection.ClassLoaderForClassArtifacts;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CallSiteClassLoader extends ClassLoaderForClassArtifacts {

    private static final Set<String> KNOWN_CLASSES = new HashSet<>();
    static {
        Collections.addAll(KNOWN_CLASSES
                , "org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite"
                , "org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite"
                , "org.codehaus.groovy.runtime.callsite.StaticMetaMethodSite"
                , "org.codehaus.groovy.runtime.callsite.CallSite"
                , "org.codehaus.groovy.runtime.callsite.CallSiteArray"
                , "groovy.lang.MetaMethod"
                , "groovy.lang.MetaClassImpl"
                );
    }

    public CallSiteClassLoader(Class klazz) {
        super(klazz);
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (KNOWN_CLASSES.contains(name))
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

}
