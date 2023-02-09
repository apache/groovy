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
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Plain ordinary object call site
 *   meta class - cached
 *   method - cached
 */
public abstract class PlainObjectMetaMethodSite extends MetaMethodSite {

    protected static final VMPlugin VM_PLUGIN = VMPluginFactory.getPlugin();

    protected static Object doInvoke(Object receiver, Object[] args, Method method) throws Throwable {
        try {
            /*if (!VM_PLUGIN.checkAccessible(PlainObjectMetaMethodSite.class, method.getDeclaringClass(), method.getModifiers(), false)) {
                MethodHandle handle = MethodHandles.privateLookupIn(receiver.getClass(), MethodHandles.lookup()).unreflect(method);
                return handle.bindTo(receiver).invokeWithArguments(args);
            }*/
            return method.invoke(receiver, args);
        } catch (InvocationTargetException ite) {
            final Throwable cause = ite.getCause();
            if (cause instanceof GroovyRuntimeException) {
                throw ScriptBytecodeAdapter.unwrap((GroovyRuntimeException) cause);
            } else {
                throw cause;
            }
        }
    }

    public PlainObjectMetaMethodSite(CallSite site, MetaClass metaClass, MetaMethod metaMethod, Class[] params) {
        super(site, metaClass, metaMethod, params);
    }
}
