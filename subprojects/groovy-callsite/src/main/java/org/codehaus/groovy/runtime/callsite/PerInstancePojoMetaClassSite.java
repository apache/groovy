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
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site that dispatches through a receiver's per-instance meta class
 * while {@link ClassInfo} reports that such meta classes are in use.
 */
public class PerInstancePojoMetaClassSite extends AbstractCallSite{
    private final ClassInfo info;

    /**
     * Copies the parent site and remembers where per-instance meta classes are tracked.
     *
     * @param site the site whose name and index are copied
     * @param info class information used to detect per-instance meta classes
     */
    public PerInstancePojoMetaClassSite(CallSite site, ClassInfo info) {
        super(site);
        this.info = info;
    }

    /**
     * Invokes this site's method on the receiver's meta class when the
     * receiver is non-null and per-instance meta classes are in use.
     * A {@link GroovyRuntimeException} is unwrapped. Otherwise uses the
     * default call.
     */
    @Override
    public Object call(Object receiver, Object[] args) throws Throwable {
        if (receiver != null && info.hasPerInstanceMetaClasses()) {
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