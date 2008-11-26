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

import groovy.lang.GroovyRuntimeException;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.reflection.ClassInfo;

public class PerInstancePojoMetaClassSite extends AbstractCallSite{
    private final ClassInfo info;

    public PerInstancePojoMetaClassSite(CallSite site, ClassInfo info) {
        super(site);
        this.info = info;
    }

    public Object call(Object receiver, Object[] args) throws Throwable {
        if (info.hasPerInstanceMetaClasses()) {
          try {
              return InvokerHelper.getMetaClass(receiver).invokeMethod(receiver, name, args);
          } catch (GroovyRuntimeException gre) {
              throw ScriptBytecodeAdapter.unwrap(gre);
          }
        } else {
          return CallSiteArray.defaultCall(this, receiver, args);
        }
    }
}