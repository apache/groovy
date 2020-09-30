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

/**
 * Call site for constructor
 *   meta class - cached
 *   method - not cached
*/
public class MetaClassConstructorSite extends MetaClassSite {

    private final ClassInfo classInfo;
    private final int version;

    public MetaClassConstructorSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
        classInfo = ClassInfo.getClassInfo(metaClass.getTheClass());
        version = classInfo.getVersion();
    }

    @Override
    public Object callConstructor(Object receiver, Object[] args) throws Throwable {
        try {
            if (receiver == metaClass.getTheClass()
                && version == classInfo.getVersion()) // metaClass still be valid
                return metaClass.invokeConstructor(args);
            else
              return CallSiteArray.defaultCallConstructor(this, receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
}
