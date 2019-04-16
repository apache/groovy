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
import groovy.lang.MetaClassImpl;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

import java.lang.reflect.Field;

class GetEffectivePojoFieldSite extends AbstractCallSite {
    private final MetaClassImpl metaClass;
    private final Field effective;
    private final int version;

    public GetEffectivePojoFieldSite(CallSite site, MetaClassImpl metaClass, CachedField effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective.getCachedField();
        version = metaClass.getVersion();
    }

//    public final Object callGetProperty (Object receiver) throws Throwable {
//        return acceptGetProperty(receiver).getProperty(receiver);
//    }

    public final CallSite acceptGetProperty(Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || receiver.getClass() != metaClass.getTheClass()
            || version != metaClass.getVersion()) { // metaClass is invalid
            return createGetPropertySite(receiver);
        } else {
            return this;
        }
    }

    public final Object getProperty(Object receiver) {
        try {
            return effective.get(receiver);
        } catch (IllegalAccessException e) {
            throw new GroovyRuntimeException("Cannot get the property '" + name + "'.", e);
        }
    }
}
