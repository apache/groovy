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
import groovy.lang.GroovyObject;

public class PogoMetaClassGetPropertySite extends AbstractCallSite {
    private final MetaClass metaClass;

    public PogoMetaClassGetPropertySite(CallSite parent, MetaClass metaClass) {
        super(parent);
        this.metaClass = metaClass;
    }

    public final CallSite acceptGetProperty(Object receiver) {
        if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
            return createGetPropertySite(receiver);
        else
          return this;
    }

    public final CallSite acceptGroovyObjectGetProperty(Object receiver) {
        if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
            return createGroovyObjectGetPropertySite(receiver);
        else
          return this;
    }

    public final Object getProperty(Object receiver) {
        return metaClass.getProperty(receiver, name);
    }
}
