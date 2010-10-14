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

import groovy.lang.*;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.ArrayUtil;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.lang.reflect.Method;

/**
 * Base class for all call sites
 *
 * @author Alex Tkachman
 */
public class AbstractCallSite implements CallSite {
    protected final int index;
    protected final String name;
    protected final CallSiteArray array;

    public AbstractCallSite(CallSiteArray array, int index, String name) {
        this.name = name;
        this.index = index;
        this.array = array;
    }

    public AbstractCallSite(CallSite prev) {
        this.name = prev.getName();
        this.index = prev.getIndex();
        this.array = prev.getArray();
    }

    public int getIndex() {
        return index;
    }

    public CallSiteArray getArray() {
        return array;
    }

    public String getName() {
        return name;
    }

    public final Object callSafe(Object receiver, Object[] args) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, args);
    }

    public final Object callSafe(Object receiver) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver);
    }

    public final Object callSafe(Object receiver, Object arg1) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1);
    }

    public final Object callSafe(Object receiver, Object arg1, Object arg2) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2);
    }

    public final Object callSafe(Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2, arg3);
    }

    public Object callSafe(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2, arg3, arg4);
    }


    public Object call(Object receiver, Object[] args) throws Throwable {
        return CallSiteArray.defaultCall(this, receiver, args);
    }

    public Object call(Object receiver) throws Throwable {
        return call(receiver, CallSiteArray.NOPARAM);
    }

    public Object call(Object receiver, Object arg1) throws Throwable {
        return call(receiver, ArrayUtil.createArray(arg1));
    }

    public Object call(Object receiver, Object arg1, Object arg2) throws Throwable {
        return call(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    public Object call(Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        return call(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    public Object call(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return call(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }


    public Object callCurrent(GroovyObject receiver, Object[] args) throws Throwable {
        return CallSiteArray.defaultCallCurrent(this, receiver, args);
    }

    public Object callCurrent(GroovyObject receiver) throws Throwable {
        return callCurrent(receiver, CallSiteArray.NOPARAM);
    }

    public Object callCurrent(GroovyObject receiver, Object arg1) throws Throwable {
        return callCurrent(receiver, ArrayUtil.createArray(arg1));
    }

    public Object callCurrent(GroovyObject receiver, Object arg1, Object arg2) throws Throwable {
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    public Object callCurrent(GroovyObject receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    public Object callCurrent(GroovyObject receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    public Object callStatic(Class receiver, Object[] args) throws Throwable {
        return CallSiteArray.defaultCallStatic(this, receiver, args);
    }

    public Object callStatic(Class receiver) throws Throwable {
        return callStatic(receiver, CallSiteArray.NOPARAM);
    }

    public Object callStatic(Class receiver, Object arg1) throws Throwable {
        return callStatic(receiver, ArrayUtil.createArray(arg1));
    }

    public Object callStatic(Class receiver, Object arg1, Object arg2) throws Throwable {
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    public Object callStatic(Class receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    public Object callStatic(Class receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }


    public Object callConstructor(Object receiver, Object[] args) throws Throwable {
        return CallSiteArray.defaultCallConstructor(this, receiver, args);
    }

    public Object callConstructor(Object receiver) throws Throwable {
        return callConstructor(receiver, CallSiteArray.NOPARAM);
    }

    public Object callConstructor(Object receiver, Object arg1) throws Throwable {
        return callConstructor(receiver, ArrayUtil.createArray(arg1));
    }

    public Object callConstructor(Object receiver, Object arg1, Object arg2) throws Throwable {
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    public Object callConstructor(Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    public Object callConstructor(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    static boolean noCoerce(ParameterTypes metaMethod, Object[] args) {
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

    static boolean noWrappers(Object[] args) {
        for (int i = 0; i != args.length; ++i)
            if (args[i] instanceof Wrapper)
                return false;
        return true;
    }


    public Object callGetProperty(Object receiver) throws Throwable {
        return acceptGetProperty(receiver).getProperty(receiver);
    }

    public Object callGroovyObjectGetProperty(Object receiver) throws Throwable {
        return acceptGroovyObjectGetProperty(receiver).getProperty(receiver);
    }

    public CallSite acceptGetProperty(Object receiver) {
        return createGetPropertySite(receiver);
    }

    public CallSite acceptGroovyObjectGetProperty(Object receiver) {
        return createGroovyObjectGetPropertySite(receiver);
    }

    protected final CallSite createGetPropertySite(Object receiver) {
        if (receiver == null) {
            return new NullCallSite(this);
        } else if (receiver instanceof GroovyObject) {
            return createGroovyObjectGetPropertySite(receiver);
        } else if (receiver instanceof Class) {
            return createClassMetaClassGetPropertySite((Class) receiver);
        }
        return createPojoMetaClassGetPropertySite(receiver);
    }

    protected final CallSite createGroovyObjectGetPropertySite(Object receiver) {
        Class aClass = receiver.getClass();
        try {
            final Method method = aClass.getMethod("getProperty", String.class);
            if (method != null && method.isSynthetic() && ((GroovyObject) receiver).getMetaClass() instanceof MetaClassImpl)
                return createPogoMetaClassGetPropertySite((GroovyObject) receiver);
        } catch (NoSuchMethodException e) {
            // fall threw
        }
        if (receiver instanceof Class) {
            return createClassMetaClassGetPropertySite((Class) receiver);
        } else {
            return createPogoGetPropertySite(aClass);
        }
    }

    public Object getProperty(Object receiver) throws Throwable {
        throw new UnsupportedOperationException();
    }

    private CallSite createPojoMetaClassGetPropertySite(Object receiver) {
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

    private CallSite createClassMetaClassGetPropertySite(Class aClass) {
        CallSite site = new ClassMetaClassGetPropertySite(this, aClass);
        array.array[index] = site;
        return site;
    }

    private CallSite createPogoMetaClassGetPropertySite(GroovyObject receiver) {
        MetaClass metaClass = receiver.getMetaClass();

        CallSite site;
        if (metaClass.getClass() != MetaClassImpl.class || GroovyCategorySupport.hasCategoryInCurrentThread()) {
            site = new PogoMetaClassGetPropertySite(this, metaClass);
        } else {
            final MetaProperty effective = ((MetaClassImpl) metaClass).getEffectiveGetMetaProperty(metaClass.getClass(), receiver, name, false);
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

    private CallSite createPogoGetPropertySite(Class aClass) {
        CallSite site = new PogoGetPropertySite(this, aClass);
        array.array[index] = site;
        return site;
    }

    public final Object callGetPropertySafe(Object receiver) throws Throwable {
        if (receiver == null)
            return null;
        else
            return callGetProperty(receiver);
    }

    public final Object callGroovyObjectGetPropertySafe(Object receiver) throws Throwable {
        if (receiver == null)
            return null;
        else
            return callGroovyObjectGetProperty(receiver);
    }

}

