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

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

class ClassMetaClassGetPropertySite extends AbstractCallSite {
    final MetaClass metaClass;
    private final Class aClass;
    private final ClassInfo classInfo;
    private final int version;

    public ClassMetaClassGetPropertySite(CallSite parent, Class aClass) {
        super(parent);
        this.aClass = aClass;
        classInfo = ClassInfo.getClassInfo(aClass);
        version = classInfo.getVersion();
        metaClass = classInfo.getMetaClass();
    }

    @Override
    public final CallSite acceptGetProperty(Object receiver) {
        if (receiver != aClass
            || version != classInfo.getVersion()) // metaClass is invalid
            return createGetPropertySite(receiver);
        else
          return this;
    }

    @Override
    public final Object getProperty(Object receiver) throws Throwable{
        try {
            return metaClass.getProperty(aClass, name);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
}
