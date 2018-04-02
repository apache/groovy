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
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Plain ordinary object call site
 *   meta class - cached
 *   method - cached
 *
 */
public abstract class PlainObjectMetaMethodSite extends MetaMethodSite {
    public PlainObjectMetaMethodSite(CallSite site, MetaClass metaClass, MetaMethod metaMethod, Class[] params) {
        super(site, metaClass, metaMethod, params);
    }

    protected static Object doInvoke(Object receiver, Object[] args, Method reflect) throws Throwable {
        try {
            return reflect.invoke(receiver, args);
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
