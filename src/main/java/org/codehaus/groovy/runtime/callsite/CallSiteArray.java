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

import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.stream.IntStream;


public final class CallSiteArray {
    public static final Object[] NOPARAM = new Object[0];
    public final CallSite[] array;
    public final Class owner;

    public CallSiteArray(Class owner, final String[] names) {
        this.owner = owner;
        this.array = IntStream.range(0, names.length)
                        .mapToObj(i -> new AbstractCallSite(this, i, names[i]))
                        .toArray(CallSite[]::new);
    }

    public static Object defaultCall(CallSite callSite, Object receiver, Object[] args) throws Throwable {
        return createCallSite(callSite, receiver, args).call(receiver, args);
    }

    public static Object defaultCallCurrent(CallSite callSite, GroovyObject receiver, Object[] args) throws Throwable {
        return createCallCurrentSite(callSite, receiver, args, callSite.getArray().owner).callCurrent(receiver, args);
    }

    public static Object defaultCallStatic(CallSite callSite, Class receiver, Object[] args) throws Throwable {
        return createCallStaticSite(callSite, receiver, args).callStatic(receiver,args);
    }

    public static Object defaultCallConstructor(CallSite callSite, Object receiver, Object[] args) throws Throwable {
        return createCallConstructorSite(callSite, (Class) receiver, args).callConstructor(receiver, args);
    }

    private static CallSite createCallStaticSite(CallSite callSite, final Class receiver, Object[] args) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                Class.forName(receiver.getName(), true, receiver.getClassLoader());
            } catch (Exception e) {
                // force <clinit>
            }
            return null;
        });
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
        CallSite site =
                metaClass instanceof MetaClassImpl
                        ? ((MetaClassImpl) metaClass).createStaticSite(callSite, args)
                        : new StaticMetaClassSite(callSite, metaClass);

        replaceCallSite(callSite, site);
        return site;
    }

    private static CallSite createCallConstructorSite(CallSite callSite, Class receiver, Object[] args) {
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
        CallSite site =
                metaClass instanceof MetaClassImpl
                        ? ((MetaClassImpl) metaClass).createConstructorSite(callSite, args)
                        : new MetaClassConstructorSite(callSite, metaClass);

        replaceCallSite(callSite, site);
        return site;
    }

    private static CallSite createCallCurrentSite(CallSite callSite, GroovyObject receiver, Object[] args, Class sender) {
        CallSite site;
        if (receiver instanceof GroovyInterceptable) {
            site = new PogoInterceptableSite(callSite);
        } else {
            MetaClass metaClass = receiver.getMetaClass();
            Class theClass = metaClass.getTheClass();
            if (receiver.getClass() != theClass && !theClass.isInterface()) {
                site = new PogoInterceptableSite(callSite);
            } else if (metaClass instanceof MetaClassImpl) {
                site = ((MetaClassImpl) metaClass).createPogoCallCurrentSite(callSite, sender, args);
            } else {
                site = new PogoMetaClassSite(callSite, metaClass);
            }
        }

        replaceCallSite(callSite, site);
        return site;
    }

    private static CallSite createProxySite(final CallSite callSite, final Object receiver) {
        if (receiver instanceof GroovyObject) {
            return new PogoInterceptableSite(callSite);
        } else {
            ClassInfo classInfo = ClassInfo.getClassInfo(receiver.getClass());
            MetaClass metaClass = classInfo.getMetaClass(receiver);
            return new PojoMetaClassSite(callSite, metaClass);
        }
    }

    // for MetaClassImpl we try to pick meta method,
    // otherwise or if method doesn't exist we make call via POJO meta class
    private static CallSite createPojoSite(CallSite callSite, Object receiver, Object[] args) {
        final Class klazz = receiver.getClass();
        MetaClass metaClass = InvokerHelper.getMetaClass(receiver);
        if (!GroovyCategorySupport.hasCategoryInCurrentThread() && metaClass instanceof MetaClassImpl) {
            final MetaClassImpl mci = (MetaClassImpl) metaClass;
            final ClassInfo info = mci.getTheCachedClass().classInfo;
            if (info.hasPerInstanceMetaClasses()) {
                return new PerInstancePojoMetaClassSite(callSite, info);
            } else {
                return mci.createPojoCallSite(callSite, receiver, args);
            }
        }

        ClassInfo info = ClassInfo.getClassInfo(klazz);
        if (info.hasPerInstanceMetaClasses()) {
            return new PerInstancePojoMetaClassSite(callSite, info);
        } else {
            return new PojoMetaClassSite(callSite, metaClass);
        }
    }

    private static CallSite createPogoSite(CallSite callSite, Object receiver, Object[] args) {
        if (receiver instanceof GroovyInterceptable) {
            return new PogoInterceptableSite(callSite);
        }

        MetaClass metaClass = ((GroovyObject)receiver).getMetaClass();

        if (metaClass instanceof MetaClassImpl) {
            return ((MetaClassImpl)metaClass).createPogoCallSite(callSite, args);
        }

        return new PogoMetaClassSite(callSite, metaClass);
    }

    private static CallSite createCallSite(CallSite callSite, Object receiver, Object[] args) {
        if (receiver == null) {
            return new NullCallSite(callSite);
        }

        CallSite site;
        if (receiver instanceof Class) {
            site = createCallStaticSite(callSite, (Class) receiver, args);
        } else if (Proxy.isProxyClass(receiver.getClass())) {
            site = createProxySite(callSite, receiver);
        } else if (receiver instanceof GroovyObject) {
            site = createPogoSite(callSite, receiver, args);
        } else {
            site = createPojoSite(callSite, receiver, args);
        }

        replaceCallSite(callSite, site);
        return site;
    }

    private static void replaceCallSite(CallSite oldSite, CallSite newSite) {
        oldSite.getArray().array[oldSite.getIndex()] = newSite;
    }
}
