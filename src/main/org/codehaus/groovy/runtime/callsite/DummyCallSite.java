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
import org.codehaus.groovy.runtime.InvokerHelper;
import groovy.lang.GroovyObject;

public class DummyCallSite extends AbstractCallSite {
    private int warmup = 2;

    public DummyCallSite(CallSiteArray array, int index, String name) {
        super(array, index,name);
    }

    public Object call(Object receiver, Object[] args) {
        if (warmup-- > 0)
           return InvokerHelper.invokeMethod(receiver, name, args);
        else
           return CallSiteArray.defaultCall(this, receiver, args);
    }

    public final Object callCurrent(GroovyObject receiver, Object[] args) throws Throwable {
        if (warmup-- > 0)
          return ScriptBytecodeAdapter.invokeMethodOnCurrentN(array.owner, (GroovyObject)receiver, name, args);
        else
          return CallSiteArray.defaultCallCurrent(this, receiver, args);
    }

    public Object callStatic(Class receiver, Object[] args) {
        if (warmup-- > 0)
          return InvokerHelper.invokeStaticMethod((Class)receiver, name, args);
        else
          return CallSiteArray.defaultCallStatic(this, receiver, args);
    }

    public final Object callConstructor(Object receiver, Object[] args) throws Throwable {
        if (warmup-- > 0)
          return InvokerHelper.invokeConstructorOf((Class)receiver, args);
        else
          return CallSiteArray.defaultCallConstructor(this, receiver, args);
    }
}
