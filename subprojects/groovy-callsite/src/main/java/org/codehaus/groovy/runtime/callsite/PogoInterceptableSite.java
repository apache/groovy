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
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site for GroovyInterceptable
*/
public class PogoInterceptableSite extends AbstractCallSite {
    /**
     * Copies the call metadata from {@code site}.
     *
     * @param site the site whose name and index are reused
     */
    public PogoInterceptableSite(CallSite site) {
        super(site);
    }

    /**
     * Sends {@code name} to {@code receiver} through
     * {@link GroovyObject#invokeMethod(String, Object)}, with {@code args}
     * unwrapped by {@link InvokerHelper#asUnwrappedArray(Object)}.
     * A {@link GroovyRuntimeException} is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     *
     * @param receiver a {@link GroovyObject}
     * @param args the call arguments
     * @return the result of {@code invokeMethod}
     */
    public final Object invoke(Object receiver, Object[] args) throws Throwable {
      try {
        return ((GroovyObject)receiver).invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
      } catch (GroovyRuntimeException gre) {
          throw ScriptBytecodeAdapter.unwrap(gre);
      }
    }

    /**
     * When {@code receiver} is a {@link GroovyObject}, sends {@code name}
     * through {@link GroovyObject#invokeMethod(String, Object)} with
     * {@code args} unwrapped. Otherwise uses the default call.
     * A {@link GroovyRuntimeException} from the Groovy receiver is rethrown
     * via {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     *
     * @param receiver the call receiver
     * @param args the call arguments
     * @return the call result
     */
    @Override
    public final Object call(Object receiver, Object[] args) throws Throwable {
        if(receiver instanceof GroovyObject) {
            try {
                return ((GroovyObject) receiver).invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return CallSiteArray.defaultCall(this, receiver, args);
        }
    }

    /**
     * Forwards to {@link #call(Object, Object[])}, so a {@link GroovyObject}
     * receiver is dispatched through {@code invokeMethod}.
     *
     * @param receiver the current object
     * @param args the call arguments
     * @return the call result
     */
    @Override
    public Object callCurrent (GroovyObject receiver, Object [] args) throws Throwable {
        return call(receiver, args);
    }
}
