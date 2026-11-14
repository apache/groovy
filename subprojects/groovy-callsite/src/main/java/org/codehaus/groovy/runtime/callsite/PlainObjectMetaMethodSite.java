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
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Plain ordinary object call site
 *   metaclass - cached
 *   method - cached
 *
 */
public abstract class PlainObjectMetaMethodSite extends MetaMethodSite {
    /**
     * Caches {@code metaClass}, {@code metaMethod}, and the parameter classes.
     *
     * @param site the site whose name and index are reused
     * @param metaClass the receiver's meta-class
     * @param metaMethod the cached method
     * @param params the parameter classes used to match a later call
     */
    public PlainObjectMetaMethodSite(CallSite site, MetaClass metaClass, MetaMethod metaMethod, Class[] params) {
        super(site, metaClass, metaMethod, params);
    }

    /**
     * Invokes {@code reflect} on {@code receiver} with {@code args} and
     * normalizes a primitive return through {@link MetaClassHelper} so the
     * boxed value keeps the identity reflection produced.
     * The cause of an {@link InvocationTargetException} is rethrown; a
     * {@link GroovyRuntimeException} cause goes through
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     *
     * @param receiver the object {@code reflect} is invoked on
     * @param args the arguments passed to {@code reflect}
     * @param reflect the method to invoke
     * @return the normalized return value
     */
    protected static Object doInvoke(Object receiver, Object[] args, Method reflect) throws Throwable {
        try {
            // GROOVY-12140: keep === identity of primitive returns consistent
            return MetaClassHelper.normalizeBoxedReturn(reflect.invoke(receiver, args), reflect.getReturnType());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof GroovyRuntimeException) {
                throw ScriptBytecodeAdapter.unwrap ((GroovyRuntimeException) cause);
            } else {
                throw cause;
            }
        }
    }
}
