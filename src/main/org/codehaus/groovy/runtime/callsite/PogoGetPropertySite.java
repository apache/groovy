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

import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;

public class PogoGetPropertySite extends AbstractCallSite {
    private final Class aClass;

    public PogoGetPropertySite(CallSite parent, Class aClass) {
        super(parent);
        this.aClass = aClass;
    }

    public CallSite acceptGetProperty(Object receiver) {
        if (receiver.getClass() != aClass)
            return createGetPropertySite(receiver);
        else
          return this;
    }

    public CallSite acceptGroovyObjectGetProperty(Object receiver) {
        if (receiver.getClass() != aClass)
            return createGroovyObjectGetPropertySite(receiver);
        else
          return this;
    }

    public Object getProperty(Object receiver) throws Throwable {
        try{
            return ((GroovyObject)receiver).getProperty(name);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
}
