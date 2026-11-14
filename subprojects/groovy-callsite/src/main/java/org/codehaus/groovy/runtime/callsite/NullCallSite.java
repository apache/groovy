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
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site that sends a {@code null} receiver to {@link NullObject}
 * before the default call or property read. A non-null receiver is passed through unchanged.
 */
public final class NullCallSite extends AbstractCallSite {
    /**
     * Copies the call metadata from {@code callSite}.
     *
     * @param callSite the site whose name and index are reused
     */
    public NullCallSite(CallSite callSite) {
        super(callSite);
    }

    /**
     * When {@code receiver} is {@code null}, dispatches through
     * {@link NullObject#getNullObject()}. Otherwise forwards {@code receiver}
     * unchanged. A {@link GroovyRuntimeException} from the null path is
     * rethrown via {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     *
     * @param receiver the call receiver, possibly {@code null}
     * @param args the call arguments
     * @return the call result
     */
    @Override
    public Object call(Object receiver, Object[] args) throws Throwable {
        if (receiver == null) {
            try{
                return CallSiteArray.defaultCall(this, NullObject.getNullObject(), args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return CallSiteArray.defaultCall(this, receiver, args);
        }
    }

    /**
     * When {@code receiver} is {@code null}, reads {@code name} from
     * {@link NullObject#getNullObject()} through
     * {@link InvokerHelper#getProperty(Object, String)}. Otherwise selects
     * a property site with {@link #acceptGetProperty(Object)} and reads
     * from {@code receiver}. A {@link GroovyRuntimeException} from the null
     * path is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     *
     * @param receiver the property owner, possibly {@code null}
     * @return the property value
     */
    @Override
    public Object getProperty(Object receiver) throws Throwable {
        if (receiver == null) {
            try{
                return InvokerHelper.getProperty(NullObject.getNullObject(), name);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return acceptGetProperty(receiver).getProperty(receiver);
        }
    }
}