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

import groovy.lang.MetaClass;
import groovy.lang.GroovyRuntimeException;

import java.lang.reflect.Field;

import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

class GetEffectivePojoFieldSite extends AbstractCallSite {
    private final MetaClass metaClass;
    private final Field effective;

    public GetEffectivePojoFieldSite(CallSite site, MetaClass metaClass, CachedField effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective.field;
    }

    public final Object callGetProperty (Object receiver) throws Throwable {
        return acceptGetProperty(receiver).getProperty(receiver);
    }

    public final CallSite acceptGetProperty(Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || receiver.getClass() != metaClass.getTheClass()) {
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
