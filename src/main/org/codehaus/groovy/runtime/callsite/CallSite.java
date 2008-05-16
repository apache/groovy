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
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all call sites
 *
 * @author Alex Tkachman
 */
public abstract class CallSite {
    protected final int index;
    public final String name;
    protected final CallSiteArray array;
    public final AtomicInteger usage;

    public CallSite(CallSiteArray array, int index, String name) {
        this.name = name;
        this.index = index;
        this.array = array;
        this.usage = GroovyCategorySupport.getCategoryNameUsage(name);
    }

    public CallSite(CallSite prev) {
        this.name = prev.name;
        this.index = prev.index;
        this.array = prev.array;
        this.usage = prev.usage;
    }

    /**
     * Call method 'name' of receiver with given arguments
     *
     * @param receiver receiver
     * @param args arguments
     * @return result of invocation
     */
    public Object invoke(Object receiver, Object [] args) {
        throw new UnsupportedOperationException();
    }

    public Object invoke(Object receiver, Object arg) {
        if (receiver == null)
          receiver = NullObject.getNullObject();

        return invoke(receiver, ArrayUtil.createArray(arg));
    }

    public CallSite acceptConstructor(Object receiver, Object[] args) {
        throw new UnsupportedOperationException();
    }

    public final Object callSafe(Object receiver, Object[] args) throws Throwable {
        if (receiver == null)
            return null;

        return call (receiver, args);
    }

    public Object callSafe (Object receiver) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver);
    }

    public Object callSafe (Object receiver, Object arg1) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1);
    }

    public Object callSafe (Object receiver, Object arg1, Object arg2) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2);
    }

    public Object callSafe (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2, arg3);
    }

    public Object callSafe (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        if (receiver == null)
            return null;

        return call(receiver, arg1, arg2, arg3, arg4);
    }



    public Object call(Object receiver, Object[] args) {
        return defaultCall(receiver, args);
    }

    public Object call (Object receiver) throws Throwable {
        return call(receiver, CallSiteArray.NOPARAM);
    }

    public Object call (Object receiver, Object arg1) throws Throwable {
        return call(receiver, ArrayUtil.createArray(arg1));
    }

    public Object call (Object receiver, Object arg1, Object arg2) throws Throwable {
        return call(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    public Object call (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        return call(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    public Object call (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return call(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }



    public Object callCurrent (Object receiver, Object [] args) throws Throwable {
        return defaultCallCurrent(receiver, args);
    }
    
    public Object callCurrent (Object receiver) throws Throwable {
        return callCurrent(receiver, CallSiteArray.NOPARAM);
    }

    public Object callCurrent (Object receiver, Object arg1) throws Throwable {
        return callCurrent(receiver, ArrayUtil.createArray(arg1));
    }

    public Object callCurrent (Object receiver, Object arg1, Object arg2) throws Throwable {
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    public Object callCurrent (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    public Object callCurrent (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return callCurrent(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }


    public Object callStatic (Object receiver, Object [] args) {
        return defaultCallStatic(receiver, args);
    }

    public Object callStatic (Object receiver) throws Throwable {
        return callStatic(receiver, CallSiteArray.NOPARAM);
    }

    public Object callStatic (Object receiver, Object arg1) throws Throwable {
        return callStatic(receiver, ArrayUtil.createArray(arg1));
    }

    public Object callStatic (Object receiver, Object arg1, Object arg2) throws Throwable {
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    public Object callStatic (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    public Object callStatic (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return callStatic(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }


    public final Object callConstructor (Object receiver, Object [] args) throws Throwable {
        return acceptConstructor(receiver, args).invoke(receiver, args);
    }

    public Object callConstructor (Object receiver) throws Throwable {
        return callConstructor(receiver, CallSiteArray.NOPARAM);
    }

    public Object callConstructor (Object receiver, Object arg1) throws Throwable {
        return callConstructor(receiver, ArrayUtil.createArray(arg1));
    }

    public Object callConstructor (Object receiver, Object arg1, Object arg2) throws Throwable {
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2));
    }

    public Object callConstructor (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable {
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2, arg3));
    }

    public Object callConstructor (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return callConstructor(receiver, ArrayUtil.createArray(arg1, arg2, arg3, arg4));
    }

    protected final Object defaultCall(Object receiver, Object[] args) {
        return createCallSite(receiver, args).invoke(receiver, args);
    }

    protected final Object defaultCallCurrent(Object receiver, Object[] args) {
        return createCallCurrentSite(receiver, args, array.owner).invoke(receiver, args);
    }

    protected final Object defaultCallStatic(Object receiver, Object[] args) {
        return createCallStaticSite((Class) receiver, args).invoke(receiver,args);
    }

    final CallSite createCallStaticSite(Class receiver, Object[] args) {
        CallSite site;
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
        if (metaClass instanceof MetaClassImpl) {
            site = ((MetaClassImpl)metaClass).createStaticSite(this, args);
        }
        else
          site = new StaticMetaClassSite(this, metaClass);

        array.array [index] = site;
        return site;
    }

    protected final CallSite createCallConstructorSite(Class receiver, Object[] args) {
       MetaClass metaClass = InvokerHelper.getMetaClass(receiver);

       CallSite site;
       if (metaClass instanceof MetaClassImpl) {
           site = ((MetaClassImpl)metaClass).createConstructorSite(this, args);
       }
       else
         site = new MetaClassConstructorSite(this, metaClass);

       array.array [index] = site;
       return site;
    }

    protected final CallSite createCallCurrentSite(Object receiver, Object[] args, Class sender) {
        CallSite site;
        if (receiver instanceof GroovyInterceptable)
          site = new PogoInterceptableSite(this);
        else {
            MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
            if (metaClass instanceof MetaClassImpl) {
                site = ((MetaClassImpl)metaClass).createPogoCallCurrentSite(this, sender, args);
            }
            else
              site = new PogoMetaClassSite(this, metaClass);
        }

        array.array [index] = site;
        return site;
    }

    // for MetaClassImpl we try to pick meta method,
    // otherwise or if method doesn't exist we make call via POJO meta class
    final CallSite createPojoSite(Object receiver, Object[] args) {
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver.getClass());

        if (usage.get() == 0 && metaClass instanceof MetaClassImpl) {
          return ((MetaClassImpl)metaClass).createPojoCallSite(this, receiver, args);
        }

        return new PojoMetaClassSite(this, metaClass);
    }

    final CallSite createPogoSite(Object receiver, Object[] args) {
        if (receiver instanceof GroovyInterceptable)
          return new PogoInterceptableSite(this);

        MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
        if (metaClass instanceof MetaClassImpl) {
            return ((MetaClassImpl)metaClass).createPogoCallSite(this, args);
        }

        return new PogoMetaClassSite(this, metaClass);
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
          if (args [i] instanceof Wrapper)
            return false;
        return true;
    }

    /**
     * Call site which never accept any receiver/arguments.
     * We use it as initial value for any call site.
     * It allow us to avoid additional null check on each call
     */
    public static class DummyCallSite extends CallSite {
        public DummyCallSite(CallSiteArray array, int index, String name) {
            super(array, index,name);
        }

        public Object invoke(Object receiver, Object[] args) {
            return null;
        }

        public final Object callCurrent(Object receiver, Object[] args) {
            return defaultCallCurrent(receiver, args);
        }

        public final CallSite acceptConstructor(Object receiver, Object[] args) {
            return createCallConstructorSite((Class) receiver, args);
        }
    }

    protected final CallSite createCallSite(Object receiver, Object[] args) {
        CallSite site;
        if (receiver == null)
          return new CallSite(this) {
              public Object invoke(Object receiver, Object[] args) {
                      return InvokerHelper.invokeMethod(NullObject.getNullObject(), name, args);
              }
          };
        
        if (receiver instanceof Class)
          site = createCallStaticSite((Class) receiver, args);
        else if (receiver instanceof GroovyObject) {
            site = createPogoSite(receiver, args);
        } else {
            site = createPojoSite(receiver, args);
        }

        array.array[index] = site;
        return site;
    }

    public Object callGetProperty (Object receiver) {
        return acceptGetProperty(receiver).getProperty(receiver);
    }

    public Object callGroovyObjectGetProperty (Object receiver) {
        return acceptGroovyObjectGetProperty(receiver).getProperty(receiver);
    }

    public CallSite acceptGetProperty(Object receiver) {
        return createGetPropertySite(receiver);
    }

    public CallSite acceptGroovyObjectGetProperty(Object receiver) {
        return createGroovyObjectGetPropertySite(receiver);
    }

    protected final CallSite createGetPropertySite(Object receiver) {
        if (receiver instanceof GroovyObject) {
            return createGroovyObjectGetPropertySite(receiver);
        }
        if (receiver instanceof Class) {
            return createClassMetaClassGetPropertySite ((Class) receiver);
        }
        return createPojoMetaClassGetPropertySite(receiver);
    }

    protected final CallSite createGroovyObjectGetPropertySite(Object receiver) {
        Class aClass = receiver.getClass();
        try {
            final Method method = aClass.getMethod("getProperty", String.class);
            if (method != null && method.isSynthetic())
              return createPogoMetaClassGetPropertySite ((GroovyObject)receiver);
        } catch (NoSuchMethodException e) {
            // fall threw
        }
        return createPogoGetPropertySite (aClass);
    }

    public Object getProperty(Object receiver) {
        throw new UnsupportedOperationException();
    }

    private CallSite createPojoMetaClassGetPropertySite(Object receiver) {
        final Class aClass = receiver.getClass();
        final MetaClass metaClass = InvokerHelper.getMetaClass(aClass);

        CallSite site;
        if (metaClass.getClass() != MetaClassImpl.class || GroovyCategorySupport.hasCategoryInCurrentThread()) {
            site = new PojoMetaClassGetPropertySite(this, metaClass);
        }
        else {
            final MetaProperty effective = ((MetaClassImpl) metaClass).getEffectiveGetMetaProperty(aClass, receiver, name, false);
            if (effective != null) {
                if (effective instanceof CachedField)
                    site = new GetEffectivePojoFieldSite(this, metaClass, (CachedField) effective);
                else
                    site = new GetEffectivePojoPropertySite(this, metaClass, effective);
            }
            else {
                site = new PojoMetaClassGetPropertySite(this, metaClass);
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
        final MetaClass metaClass = receiver.getMetaClass();

        CallSite site;
        if (metaClass.getClass() != MetaClassImpl.class || GroovyCategorySupport.hasCategoryInCurrentThread()) {
            site = new PogoMetaClassGetPropertySite(this, metaClass);
        }
        else {
            final MetaProperty effective = ((MetaClassImpl) metaClass).getEffectiveGetMetaProperty(metaClass.getClass(), receiver, name, false);
            if (effective != null) {
                if (effective instanceof CachedField)
                    site = new GetEffectivePogoFieldSite(this, metaClass, (CachedField) effective);
                else
                    site = new GetEffectivePogoPropertySite(this, metaClass, effective);
            }
            else {
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

    public final Object callGetPropertySafe (Object receiver) {
        if (receiver == null)
          return null;
        else
          return callGetProperty(receiver);
    }

    public final Object callGroovyObjectGetPropertySafe (Object receiver) {
        if (receiver == null)
          return null;
        else
          return callGroovyObjectGetProperty(receiver);
    }

    private static class GetEffectivePogoPropertySite extends CallSite {
        private final MetaClass metaClass;
        private final MetaProperty effective;

        public GetEffectivePogoPropertySite(CallSite site, MetaClass metaClass, MetaProperty effective) {
            super(site);
            this.metaClass = metaClass;
            this.effective = effective;
        }

        public final Object callGetProperty (Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject) receiver).getMetaClass() != metaClass) {
                return createGetPropertySite(receiver).getProperty(receiver);
            } else
                return effective.getProperty(receiver);
        }

        public final CallSite acceptGetProperty(Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
                return createGetPropertySite(receiver);
            } else {
                return this;
            }
        }

        public final Object callGroovyObjectGetProperty (Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject) receiver).getMetaClass() != metaClass) {
                return createGetPropertySite(receiver).getProperty(receiver);
            } else
                return effective.getProperty(receiver);
        }

        public final CallSite acceptGroovyObjectGetProperty(Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
                return createGroovyObjectGetPropertySite(receiver);
            } else {
                return this;
            }
        }

        public final Object getProperty(Object receiver) {
            return effective.getProperty(receiver);
        }
    }

    private static class GetEffectivePojoPropertySite extends CallSite {
        private final MetaClass metaClass;
        private final MetaProperty effective;

        public GetEffectivePojoPropertySite(CallSite site, MetaClass metaClass, MetaProperty effective) {
            super(site);
            this.metaClass = metaClass;
            this.effective = effective;
        }

        public final Object callGetProperty (Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || receiver.getClass() != metaClass.getTheClass()) {
                return createGetPropertySite(receiver).getProperty(receiver);
            } else
                return effective.getProperty(receiver);
        }

        public final CallSite acceptGetProperty(Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
                return createGetPropertySite(receiver);
            } else {
                return this;
            }
        }

        public final Object getProperty(Object receiver) {
            return effective.getProperty(receiver);
        }
    }

    private static class GetEffectivePogoFieldSite extends CallSite {
        private final MetaClass metaClass;
        private final Field effective;

        public GetEffectivePogoFieldSite(CallSite site, MetaClass metaClass, CachedField effective) {
            super(site);
            this.metaClass = metaClass;
            this.effective = effective.field;
        }

        public final Object callGetProperty (Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject) receiver).getMetaClass() != metaClass) {
                return createGetPropertySite(receiver).getProperty(receiver);
            } else {
                return getProperty(receiver);
            }
        }

        public final CallSite acceptGetProperty(Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
                return createGetPropertySite(receiver);
            } else {
                return this;
            }
        }

        public final Object callGroovyObjectGetProperty (Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || ((GroovyObject) receiver).getMetaClass() != metaClass) {
                return createGroovyObjectGetPropertySite(receiver).getProperty(receiver);
            } else {
                return getProperty(receiver);
            }
        }

        public final CallSite acceptGroovyObjectGetProperty(Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
                return createGroovyObjectGetPropertySite(receiver);
            } else {
                return this;
            }
        }

        public final Object getProperty(Object receiver) {
            try {
                return effective.get(receiver);
            } catch (IllegalAccessException e) {
                throw new GroovyRuntimeException("Cannot get the property '" + name + "'.", e);
            }
        }
    }

    private static class GetEffectivePojoFieldSite extends CallSite {
        private final MetaClass metaClass;
        private final Field effective;

        public GetEffectivePojoFieldSite(CallSite site, MetaClass metaClass, CachedField effective) {
            super(site);
            this.metaClass = metaClass;
            this.effective = effective.field;
        }

        public final Object callGetProperty (Object receiver) {
            return acceptGetProperty(receiver).getProperty(receiver);
        }

        public final CallSite acceptGetProperty(Object receiver) {
            if (GroovyCategorySupport.hasCategoryInCurrentThread() || receiver.getClass() != metaClass.getTheClass()) {
                return createGetPropertySite(receiver);
            } else {
                return this;
            }
        }

        public final Object getProperty(Object receiver) {
            try {
                return effective.get(receiver);
            } catch (IllegalAccessException e) {
                throw new GroovyRuntimeException("Cannot get the property '" + name + "'.", e);
            }
        }
    }

    private static class ClassMetaClassGetPropertySite extends CallSite {
        final MetaClass metaClass;
        private final Class aClass;

        public ClassMetaClassGetPropertySite(CallSite parent, Class aClass) {
            super(parent);
            this.aClass = aClass;
            metaClass = InvokerHelper.getMetaClass(aClass);
        }

        public final CallSite acceptGetProperty(Object receiver) {
            if (receiver != aClass)
                return createGetPropertySite(receiver);
            else
              return this;
        }

        public final Object getProperty(Object receiver) {
            return metaClass.getProperty(aClass, name);
        }
    }

    private static class PogoMetaClassGetPropertySite extends CallSite {
        private final MetaClass metaClass;

        public PogoMetaClassGetPropertySite(CallSite parent, MetaClass metaClass) {
            super(parent);
            this.metaClass = metaClass;
        }

        public final CallSite acceptGetProperty(Object receiver) {
            if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
                return createGetPropertySite(receiver);
            else
              return this;
        }

        public final CallSite acceptGroovyObjectGetProperty(Object receiver) {
            if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
                return createGroovyObjectGetPropertySite(receiver);
            else
              return this;
        }

        public final Object getProperty(Object receiver) {
            return metaClass.getProperty(receiver, name);
        }
    }

    private static class PojoMetaClassGetPropertySite extends CallSite {
        private final MetaClass metaClass;

        public PojoMetaClassGetPropertySite(CallSite parent, MetaClass metaClass) {
            super(parent);
            this.metaClass = metaClass;
        }

        public final CallSite acceptGetProperty(Object receiver) {
            if (receiver.getClass() != metaClass.getTheClass())
              return createGetPropertySite(receiver);
            else
              return this;
        }

        public final Object getProperty(Object receiver) {
            return metaClass.getProperty(receiver, name);
        }
    }

    private static class PogoGetPropertySite extends CallSite {
        private final Class aClass;

        public PogoGetPropertySite(CallSite parent, Class aClass) {
            super(parent);
            this.aClass = aClass;
        }

        public CallSite acceptGetProperty(Object receiver) {
            if (receiver.getClass() != aClass)
                return createGetPropertySite(receiver);
            else
              return this;
        }

        public CallSite acceptGroovyObjectGetProperty(Object receiver) {
            if (receiver.getClass() != aClass)
                return createGroovyObjectGetPropertySite(receiver);
            else
              return this;
        }

        public Object getProperty(Object receiver) {
            return ((GroovyObject)receiver).getProperty(name);
        }
    }
}

