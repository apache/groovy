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
package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteAwareMetaMethod;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

import java.lang.reflect.Modifier;

public abstract class NumberNumberMetaMethod extends CallSiteAwareMetaMethod {
    private static final CachedClass    NUMBER_CLASS = ReflectionCache.getCachedClass(Number.class);
    private static final CachedClass [] NUMBER_CLASS_ARR = new CachedClass[] { NUMBER_CLASS };

    protected NumberNumberMetaMethod() {
        parameterTypes = NUMBER_CLASS_ARR;
    }

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    @Override
    public Class getReturnType() {
        return NUMBER_CLASS.getTheClass();
    }

    @Override
    public final CachedClass getDeclaringClass() {
        return NUMBER_CLASS;
    }

    public abstract static class NumberNumberCallSite extends PojoMetaMethodSite {

        final NumberMath math;

        public NumberNumberCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Number receiver, Number arg) {
            super(site, metaClass, metaMethod, params);
            math = NumberMath.getMath(receiver,arg);
        }
    }

    @Override
    public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        Object firstArg = args[0];

        if (receiver instanceof Integer) {
            if (firstArg instanceof Integer)
                return createIntegerInteger(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Long)
                return createIntegerLong(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Float)
                return createIntegerFloat(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Double)
                return createIntegerDouble(site, metaClass, metaMethod, params, receiver, args);
        }

        if (receiver instanceof Long) {
            if (firstArg instanceof Integer)
                return createLongInteger(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Long)
                return createLongLong(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Float)
                return createLongFloat(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Double)
                return createLongDouble(site, metaClass, metaMethod, params, receiver, args);
        }

        if (receiver instanceof Float) {
            if (firstArg instanceof Integer)
                return createFloatInteger(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Long)
                return createFloatLong(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Float)
                return createFloatFloat(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Double)
                return createFloatDouble(site, metaClass, metaMethod, params, receiver, args);
        }

        if (receiver instanceof Double) {
            if (firstArg instanceof Integer)
                return createDoubleInteger(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Long)
                return createDoubleLong(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Float)
                return createDoubleFloat(site, metaClass, metaMethod, params, receiver, args);

            if (firstArg instanceof Double)
                return createDoubleDouble(site, metaClass, metaMethod, params, receiver, args);
        }

        return createNumberNumber(site, metaClass, metaMethod, params, receiver, args);
    }

    public abstract CallSite createIntegerInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createIntegerLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createIntegerFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createIntegerDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createLongInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createLongLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createLongFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createLongDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createFloatInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createFloatLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createFloatFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createFloatDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createDoubleInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createDoubleLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createDoubleFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createDoubleDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
    public abstract CallSite createNumberNumber(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args);
}
