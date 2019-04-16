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

import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

import java.lang.reflect.Field;

public class GetEffectivePogoFieldSite extends AbstractCallSite {
    private final MetaClass metaClass;
    private final Field effective;

    public GetEffectivePogoFieldSite(CallSite site, MetaClass metaClass, CachedField effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective.getCachedField();
    }

    public final Object callGetProperty (Object receiver) throws Throwable {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject) receiver).getMetaClass() != metaClass) {
            return createGetPropertySite(receiver).getProperty(receiver);
        } else {
            return getProperty(receiver);
        }
    }

    public final CallSite acceptGetProperty(Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
            return createGetPropertySite(receiver);
        } else {
            return this;
        }
    }

    public final Object callGroovyObjectGetProperty (Object receiver) throws Throwable {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || ((GroovyObject) receiver).getMetaClass() != metaClass) {
            return createGroovyObjectGetPropertySite(receiver).getProperty(receiver);
        } else {
            return getProperty(receiver);
        }
    }

    public final CallSite acceptGroovyObjectGetProperty(Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
            return createGroovyObjectGetPropertySite(receiver);
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
