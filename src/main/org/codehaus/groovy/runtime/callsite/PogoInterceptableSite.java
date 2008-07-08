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

import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Call site for GroovyInterceptable
 *
 * @author Alex Tkachman
*/
public class PogoInterceptableSite extends AbstractCallSite {
    public PogoInterceptableSite(CallSite site) {
        super(site);
    }

    public final Object invoke(Object receiver, Object[] args) {
      return ((GroovyObject)receiver).invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
    }

    public final Object call(Object receiver, Object[] args) {
        if(receiver instanceof GroovyInterceptable)
          return ((GroovyObject) receiver).invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
        else
          return CallSiteArray.defaultCall(this, receiver, args);
    }

    public Object callCurrent (GroovyObject receiver, Object [] args) throws Throwable {
        return call(receiver, args);
    }
}
