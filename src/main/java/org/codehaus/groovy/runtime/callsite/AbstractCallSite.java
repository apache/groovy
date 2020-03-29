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
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaProperty;
import groovy.transform.Internal;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.ArrayUtil;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.lang.reflect.Method;

/**
 * Base class for all call sites.
 */
public class AbstractCallSite implements CallSite {

    protected final int index;
    protected final String name;
    protected final CallSiteArray array;

    public AbstractCallSite(final CallSiteArray array, final int index, final String name) {
        this.name = name;
        this.index = index;
        this.array = array;
    }

    public AbstractCallSite(final CallSite prev) {
        this.name = prev.getName();
        this.index = prev.getIndex();
        this.array = prev.getArray();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public CallSiteArray getArray() {
        return array;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public final Object callSafe(final Object receiver, final Object[] args) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, args);
    }

    @Override
    public final Object callSafe(final Object receiver) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver);
    }

    @Override
    public final Object callSafe(final Object receiver, final Object arg1) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1);
    }

    @Override
    public final Object callSafe(final Object receiver, final Object arg1, final Object arg2) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2);
    }

    @Override
    public final Object callSafe(final Object receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2, arg3);
    }

    @Override
    public Object callSafe(final Object receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2, arg3, arg4);
    }

    @Override
    public Object call(final Object receiver, final Object[] args) throws Throwable {
        return CallSiteArray.defaultCall(this, receiver, args);
    }

    @Override
    public Object call(final Object receiver) throws Throwable {
        return call(receiver, CallSiteArray.NOPARAM);
    }

    @Override
    public Object call(final Object receiver, final Object arg1) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.call(receiver, arg1);
        }
        return call(receiver, ArrayUtil.createArray(arg1));
    }

    @Override
    public Object call(final Object receiver, final Object arg1, final Object arg2) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.call(receiver, arg1, arg2);
        }
        return call(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    @Override
    public Object call(final Object receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.call(receiver, arg1, arg2, arg3);
        }
        return call(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    @Override
    public Object call(final Object receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.call(receiver, arg1, arg2, arg3, arg4);
        }
        return call(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    @Override
    public Object callCurrent(final GroovyObject receiver, final Object[] args) throws Throwable {
        return CallSiteArray.defaultCallCurrent(this, receiver, args);
    }

    @Override
    public Object callCurrent(final GroovyObject receiver) throws Throwable {
        return callCurrent(receiver, CallSiteArray.NOPARAM);
    }

    @Override
    public Object callCurrent(final GroovyObject receiver, final Object arg1) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callCurrent(receiver, arg1);
        }
        return callCurrent(receiver, ArrayUtil.createArray(arg1));
    }

    @Override
    public Object callCurrent(final GroovyObject receiver, final Object arg1, final Object arg2) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callCurrent(receiver, arg1, arg2);
        }
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    @Override
    public Object callCurrent(final GroovyObject receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callCurrent(receiver, arg1, arg2, arg3);
        }
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    @Override
    public Object callCurrent(final GroovyObject receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callCurrent(receiver, arg1, arg2, arg3, arg4);
        }
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    @Override
    public Object callStatic(final Class receiver, final Object[] args) throws Throwable {
        return CallSiteArray.defaultCallStatic(this, receiver, args);
    }

    @Override
    public Object callStatic(final Class receiver) throws Throwable {
        return callStatic(receiver, CallSiteArray.NOPARAM);
    }

    @Override
    public Object callStatic(final Class receiver, final Object arg1) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callStatic(receiver, arg1);
        }
        return callStatic(receiver, ArrayUtil.createArray(arg1));
    }

    @Override
    public Object callStatic(final Class receiver, final Object arg1, final Object arg2) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callStatic(receiver, arg1, arg2);
        }
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    @Override
    public Object callStatic(final Class receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callStatic(receiver, arg1, arg2, arg3);
        }
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    @Override
    public Object callStatic(final Class receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callStatic(receiver, arg1, arg2, arg3, arg4);
        }
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    @Override
    public Object callConstructor(final Object receiver, final Object[] args) throws Throwable {
        return CallSiteArray.defaultCallConstructor(this, receiver, args);
    }

    @Override
    public Object callConstructor(final Object receiver) throws Throwable {
        return callConstructor(receiver, CallSiteArray.NOPARAM);
    }

    @Override
    public Object callConstructor(final Object receiver, final Object arg1) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callConstructor(receiver, arg1);
        }
        return callConstructor(receiver, ArrayUtil.createArray(arg1));
    }

    @Override
    public Object callConstructor(final Object receiver, final Object arg1, final Object arg2) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callConstructor(receiver, arg1, arg2);
        }
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    @Override
    public Object callConstructor(final Object receiver, final Object arg1, final Object arg2, final Object arg3) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callConstructor(receiver, arg1, arg2, arg3);
        }
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    @Override
    public Object callConstructor(final Object receiver, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws Throwable {
        CallSite stored = array.array[index];
        if (stored!=this) {
            return stored.callConstructor(receiver, arg1, arg2, arg3, arg4);
        }
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    static boolean noCoerce(final ParameterTypes metaMethod, final Object[] args) {
        final CachedClass[] paramClasses = metaMethod.getParameterTypes();
        if (paramClasses.length != args.length)
            return false;

        for (int i = 0; i < paramClasses.length; i++) {
            CachedClass paramClass = paramClasses[i];
            if (args[i] != null && !paramClass.isDirectlyAssignable(args[i]))
                return true;
        }
        return false;
    }

    static boolean noWrappers(final Object[] args) {
        for (int i = 0; i != args.length; i += 1)
            if (args[i] instanceof Wrapper)
                return false;
        return true;
    }

    @Override
    public Object callGetProperty(final Object receiver) throws Throwable {
        return acceptGetProperty(receiver).getProperty(receiver);
    }

    @Override
    public Object callGroovyObjectGetProperty(final Object receiver) throws Throwable {
        if (receiver == null) {
            try {
                return InvokerHelper.getProperty(NullObject.getNullObject(), name);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return acceptGroovyObjectGetProperty(receiver).getProperty(receiver);
        }
    }

    public CallSite acceptGetProperty(final Object receiver) {
        return createGetPropertySite(receiver);
    }

    public CallSite acceptGroovyObjectGetProperty(final Object receiver) {
        return createGroovyObjectGetPropertySite(receiver);
    }

    protected final CallSite createGetPropertySite(final Object receiver) {
        if (receiver == null) {
            return new NullCallSite(this);
        } else if (receiver instanceof GroovyObject) {
            return createGroovyObjectGetPropertySite(receiver);
        } else if (receiver instanceof Class) {
            return createClassMetaClassGetPropertySite((Class<?>) receiver);
        }
        return createPojoMetaClassGetPropertySite(receiver);
    }

    protected final CallSite createGroovyObjectGetPropertySite(final Object receiver) {
        Class<?> aClass = receiver.getClass();
        try {
            final Method method = aClass.getMethod("getProperty", String.class);
            if (method != null && (method.isSynthetic() || isMarkedInternal(method)) && ((GroovyObject) receiver).getMetaClass() instanceof MetaClassImpl)
                return createPogoMetaClassGetPropertySite((GroovyObject) receiver);
        } catch (NoSuchMethodException e) {
            // fall threw
        }
        if (receiver instanceof Class) {
            return createClassMetaClassGetPropertySite((Class<?>) receiver);
        } else {
            return createPogoGetPropertySite(aClass);
        }
    }

    private boolean isMarkedInternal(final Method method) {
        return method.getAnnotation(Internal.class) != null;
    }

    @Override
    public Object getProperty(final Object receiver) throws Throwable {
        throw new UnsupportedOperationException();
    }

    private CallSite createPojoMetaClassGetPropertySite(final Object receiver) {
        final MetaClass metaClass = InvokerHelper.getMetaClass(receiver);

        CallSite site;
        if (metaClass.getClass() != MetaClassImpl.class || GroovyCategorySupport.hasCategoryInCurrentThread()) {
            site = new PojoMetaClassGetPropertySite(this);
        } else {
            final MetaProperty effective = ((MetaClassImpl) metaClass).getEffectiveGetMetaProperty(receiver.getClass(), receiver, name, false);
            if (effective != null) {
                if (effective instanceof CachedField)
                    site = new GetEffectivePojoFieldSite(this, (MetaClassImpl) metaClass, (CachedField) effective);
                else
                    site = new GetEffectivePojoPropertySite(this, (MetaClassImpl) metaClass, effective);
            } else {
                site = new PojoMetaClassGetPropertySite(this);
            }
        }

        array.array[index] = site;
        return site;
    }

    private CallSite createClassMetaClassGetPropertySite(final Class<?> aClass) {
        CallSite site = new ClassMetaClassGetPropertySite(this, aClass);
        array.array[index] = site;
        return site;
    }

    private CallSite createPogoMetaClassGetPropertySite(final GroovyObject receiver) {
        MetaClass metaClass = receiver.getMetaClass();

        CallSite site;
        if (metaClass.getClass() != MetaClassImpl.class || GroovyCategorySupport.hasCategoryInCurrentThread()) {
            site = new PogoMetaClassGetPropertySite(this, metaClass);
        } else {
            final MetaProperty effective = ((MetaClassImpl) metaClass).getEffectiveGetMetaProperty(this.array.owner, receiver, name, false);
            if (effective != null) {
                if (effective instanceof CachedField)
                    site = new GetEffectivePogoFieldSite(this, metaClass, (CachedField) effective);
                else
                    site = new GetEffectivePogoPropertySite(this, metaClass, effective);
            } else {
                site = new PogoMetaClassGetPropertySite(this, metaClass);
            }
        }

        array.array[index] = site;
        return site;
    }

    private CallSite createPogoGetPropertySite(final Class<?> aClass) {
        CallSite site = new PogoGetPropertySite(this, aClass);
        array.array[index] = site;
        return site;
    }

    @Override
    public final Object callGetPropertySafe(final Object receiver) throws Throwable {
        if (receiver == null) return null;
        return callGetProperty(receiver);
    }

    @Override
    public final Object callGroovyObjectGetPropertySafe(final Object receiver) throws Throwable {
        if (receiver == null) return null;
        return callGroovyObjectGetProperty(receiver);
    }
}
