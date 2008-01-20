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
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

/**
 * Base class for all call sites
 *
 * @author Alex Tkachman
 */
public abstract class CallSite {
    protected final int index;
    public final String name;
    protected final CallSiteArray array;

    public CallSite(CallSiteArray array, int index, String name) {
        this.name = name;
        this.index = index;
        this.array = array;
    }

    public CallSite(CallSite prev) {
        this.name = prev.name;
        this.index = prev.index;
        this.array = prev.array;
    }

    /**
     * Call method 'name' of receiver with given arguments
     *
     * @param receiver receiver
     * @param args arguments
     * @return result of invocation
     */
    public abstract Object invoke(Object receiver, Object [] args);

    /**
     * Check if receiver/arguments are "exactly the same" as when this site was created.
     *
     * Exact meaning of "exactly the same" depends on type of the site.
     * For example, for GroovyInterceptable it is enough to check that receiver is GroovyInterceptable
     * but for site with meta method we need to be sure that classes of arguments are exactly the same
     * in the strongest possible meaning.
     *
     * @param receiver receiver
     * @param args arguments
     * @return if receiver/arguments are valid for this site
     */
    public abstract boolean accept(Object receiver, Object[] args);

    public final Object callSafe(Object receiver, Object[] args) throws Throwable {
        try {
            if (receiver == null)
                return null;

            return getCallSite(receiver, args).invoke(receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object call(Object receiver, Object[] args) throws Throwable {
        try {
            if (receiver == null)
                receiver = NullObject.getNullObject();

            return getCallSite(receiver, args).invoke(receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object callCurrent (Object receiver, Object [] args) throws Throwable {
        try {
            return getCallCurrentSite(receiver, args).invoke(receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
    
    public final Object callConstructor (Object receiver, Object [] args) throws Throwable {
        try {
            return getCallConstructorSite((Class) receiver, args).invoke(receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    final CallSite createCallStaticSite(Class receiver, Object[] args) {
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
        if (metaClass instanceof MetaClassImpl) {
            return ((MetaClassImpl)metaClass).createStaticSite(this, args);
        }
       return new StaticMetaClassSite(this, metaClass);
    }

    final CallSite createCallConstructorSite(Class receiver, Object[] args) {
       MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
       if (metaClass instanceof MetaClassImpl) {
           return ((MetaClassImpl)metaClass).createConstructorSite(this, args);
       }
       return new MetaClassConstructorSite(this, metaClass);
    }

    final CallSite createCallCurrentSite(Object receiver, Object[] args, Class sender) {
        if (receiver instanceof GroovyInterceptable)
          return new PogoInterceptableSite(this);

        MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
        if (metaClass instanceof MetaClassImpl) {
            return ((MetaClassImpl)metaClass).createPogoCallCurrentSite(this, sender, args);
        }

        return new PogoMetaClassSite(this, metaClass);
    }

    // for MetaClassImpl we try to pick meta method,
    // otherwise or if method doesn't exist we make call via POJO meta class
    final CallSite createPojoSite(Object receiver, Object[] args) {
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver.getClass());

        if (metaClass instanceof MetaClassImpl) {
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
    static class DummyCallSite extends CallSite {
        public DummyCallSite(CallSiteArray array, int index, String name) {
            super(array, index,name);
        }

        public Object invoke(Object receiver, Object[] args) {
            return null;
        }

        public boolean accept(Object receiver, Object[] args) {
            return false;
        }
    }

    public final CallSite getCallCurrentSite(Object receiver, Object[] args) {
        if (!accept(receiver, args)) {
            CallSite site = createCallCurrentSite(receiver, args, array.owner);
            array.array[index] = site;
            return site;
        }
        return this;
    }

    public final CallSite getCallConstructorSite(Class receiver, Object [] args) {
        if (!accept(receiver, args)) {
            CallSite site = createCallConstructorSite(receiver, args);
            array.array[index] = site;
            return site;
        }
        return this;
    }

    public final CallSite getCallSite(Object receiver, Object [] args) {
        if (!accept(receiver, args)) {
            CallSite site = createCallSite(receiver, args);
            array.array[index] = site;
            return site;
        }
        return this;
    }

    private CallSite createCallSite(Object receiver, Object[] args) {
        if (receiver instanceof Class)
          return createCallStaticSite((Class) receiver, args);

        if (!(receiver instanceof GroovyObject)) {
            return createPojoSite(receiver, args);
        }

        return createPogoSite(receiver, args);
    }
}
