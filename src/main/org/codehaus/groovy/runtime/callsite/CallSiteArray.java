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
 * All call site calls done via CallSiteArray
 * Groovy compiler creates static CallSiteArray field for each compiled class
 * One index in array correspond to one method or constructor call (non-spreaded, spreded ones dispatched regular way)
 *
 * CallSiteArray has several methods of the same type (call, callSafe, callCurrent, callStatic and callConstructor)
 * Each method does more or less the same
 * - ask if existing site is valid for receiver and arguments
 * - if necessary create new site and replace existing one
 * - ask call site to make the call
 *
 * @author Alex Tkachman
 */
public class CallSiteArray {
    private final CallSite[] array;

    public static final Object [] NOPARAM = new Object[0];
    private final Class owner;

    public CallSiteArray(Class owner, int size) {
        this.owner = owner;
        array = new CallSite[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = CallSite.DUMMY;
        }
    }

    public final Object callSafe (int index, String name, Object receiver, Object [] args) throws Throwable {
        try {
            if (receiver == null)
                return null;

            return getCallSite(index, name, receiver, args).call (receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object call (int index, String name, Object receiver, Object [] args) throws Throwable {
        try {
            if (receiver == null)
                receiver = NullObject.getNullObject();

            return getCallSite(index, name, receiver, args).call (receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object callStatic (int index, String name, Object receiver, Object [] args) throws Throwable {
        try {
            return getCallStaticSite(index, name, receiver, args).call (receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object callCurrent (int index, String name, Object receiver, Object [] args) throws Throwable {
        try {
            return getCallCurrentSite(index, name, receiver, args).call (receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
    
    public final Object callConstructor (int index, Object receiver, Object [] args) throws Throwable {
        try {
            return getCallConstructorSite(index, (Class) receiver, args).call (receiver, args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    private CallSite getCallSite(int index, String name, Object receiver, Object [] args) {
        CallSite site = array[index];
        if (site.accept(receiver, args)) {
            return site;
        }
        site = createCallSite(name, receiver, args);
        array[index] = site;
        return site;
    }

    private CallSite getCallStaticSite(int index, String name, Object receiver, Object [] args) {
        CallSite site = array[index];
        if (site.accept(receiver, args)) {
            return site;
        }
        site = createCallStaticSite(name, (Class)receiver, args);
        array[index] = site;
        return site;
    }

    private CallSite getCallCurrentSite(int index, String name, Object receiver, Object [] args) {
        CallSite site = array[index];
        if (site != null && site.accept(receiver, args)) {
            return site;
        }
        site = createCallCurrentSite(name, receiver, args, owner);
        array[index] = site;
        return site;
    }

    private CallSite getCallConstructorSite(int index, Class receiver, Object [] args) {
        CallSite site = array[index];
        if (site.accept(receiver, args)) {
            return site;
        }
        site = createCallConstructorSite(receiver, args);
        array[index] = site;
        return site;
    }

    private static CallSite createCallSite(String name, Object receiver, Object[] args) {
        if (receiver instanceof Class)
          return createCallStaticSite(name, (Class) receiver, args);

        if (!(receiver instanceof GroovyObject)) {
            return createPojoSite(name, receiver, args);
        }

        return createPogoSite(name, receiver, args);
    }

    private static CallSite createCallStaticSite(String name, Class receiver, Object[] args) {
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
        if (metaClass instanceof MetaClassImpl) {
            return ((MetaClassImpl)metaClass).createStaticSite(name, args);
        }
       return new StaticMetaClassSite(name, metaClass);
    }

    private static CallSite createCallConstructorSite(Class receiver, Object[] args) {
       MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
       if (metaClass instanceof MetaClassImpl) {
           return ((MetaClassImpl)metaClass).createConstructorSite(args);
       }
       return new MetaClassConstructorSite(metaClass);
    }

    private static CallSite createCallCurrentSite(String name, Object receiver, Object[] args, Class sender) {
        if (receiver instanceof GroovyInterceptable)
          return new PogoInterceptableSite(name);

        MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
        if (metaClass instanceof MetaClassImpl) {
            return ((MetaClassImpl)metaClass).createPogoCallCurrentSite(sender,name, args);
        }

        return new PogoMetaClassSite(name, metaClass);
    }

    // for MetaClassImpl we try to pick meta method,
    // otherwise or if method doesn't exist we make call via POJO meta class
    private static CallSite createPojoSite(String name, Object receiver, Object[] args) {
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver.getClass());

        if (metaClass instanceof MetaClassImpl) {
          return ((MetaClassImpl)metaClass).createPojoCallSite(name, args);
        }

        return new PojoMetaClassSite(name, metaClass);
    }


    private static CallSite createPogoSite(String name, Object receiver, Object[] args) {
        if (receiver instanceof GroovyInterceptable)
          return new PogoInterceptableSite(name);

        MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();
        if (metaClass instanceof MetaClassImpl) {
            return ((MetaClassImpl)metaClass).createPogoCallSite(name, args);
        }

        return new PogoMetaClassSite(name, metaClass);
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
}
