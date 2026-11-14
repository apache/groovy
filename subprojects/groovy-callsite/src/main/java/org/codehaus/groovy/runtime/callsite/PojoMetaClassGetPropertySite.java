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
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Property-read call site for a plain receiver. {@link #getProperty(Object)}
 * and {@link #callGetProperty(Object)} both read {@code name} through
 * {@link InvokerHelper}. {@link #acceptGetProperty(Object)} keeps this site.
 */
public class PojoMetaClassGetPropertySite extends AbstractCallSite {
    /**
     * Copies the call metadata from {@code parent}.
     *
     * @param parent the site whose name and index are reused
     */
    public PojoMetaClassGetPropertySite(CallSite parent) {
        super(parent);
    }

    /**
     * Accepts any receiver and keeps this site as the property reader.
     *
     * @param receiver the property owner, unused
     * @return this site
     */
    @Override
    public final CallSite acceptGetProperty(Object receiver) {
          return this;
    }

    /**
     * Reads {@code name} from {@code receiver} through
     * {@link InvokerHelper#getProperty(Object, String)}.
     * A {@link GroovyRuntimeException} is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     *
     * @param receiver the property owner
     * @return the property value
     */
    @Override
    public final Object getProperty(Object receiver) throws Throwable {
        try {
            return InvokerHelper.getProperty(receiver, name);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    /**
     * Reads {@code name} from {@code receiver} through
     * {@link InvokerHelper#getProperty(Object, String)}, the same path as
     * {@link #getProperty(Object)}.
     * A {@link GroovyRuntimeException} is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     *
     * @param receiver the property owner
     * @return the property value
     */
    @Override
    public Object callGetProperty(Object receiver) throws Throwable {
        try {
            return InvokerHelper.getProperty(receiver, name);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
}
